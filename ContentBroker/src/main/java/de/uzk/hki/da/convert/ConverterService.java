/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.uzk.hki.da.convert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.format.KnownFormatCmdLineErrors;
import de.uzk.hki.da.format.QualityLevelException;
import de.uzk.hki.da.model.ConversionInstruction;
import de.uzk.hki.da.model.Event;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.CommandLineConnector;
import de.uzk.hki.da.utils.Path;

/**
 * Helps converting files based on lists of ConversionInstructions.
 * 
 * @author Daniel M. de Oliveira
 *
 */
public class ConverterService {
	
	static final Logger logger = LoggerFactory.getLogger(ConverterService.class);

	private boolean prune;
	
	private KnownFormatCmdLineErrors knownFormatCmdLineErrors;
	
	public ConverterService(boolean prune, KnownFormatCmdLineErrors knownFormatCmdLineErrors) {
		this.prune = prune;
		this.knownFormatCmdLineErrors = knownFormatCmdLineErrors;
	}
	
	/**
	 * Executes a batch of conversion instructions on the local node.
	 * 
	 * @author Daniel M. de Oliveira
	 * 
	 * so we can determine if really every new file has the relavant information for generating the premis history.
	 *
	 * @param premisFile
	 * @param object
	 * @param conversionInstructions
	 * @return events for all conversions. 
	 * @throws IOException 
	 */
	public List<Event> convertBatch(
			WorkArea wa,
			Object object,
			List<ConversionInstruction> conversionInstructions) throws IOException {
		
		List<Event> results = new ArrayList<Event>();
		
		// to register the pip subfolder into irods, it must exist (even if it is empty).
		Path.make(wa.dataPath(),WorkArea.TMP_PIPS).toFile().mkdir();
		Path.makeFile(wa.dataPath(),object.getNameOfLatestBRep()).mkdir();
		
		for (ConversionInstruction ci:conversionInstructions){
			waitUntilThereIsSufficientSpaceOnCacheResource(wa.dataPath().toString(),2097152,10000);
			try{
				List<Event> partialResults = executeConversionInstruction(wa,ci,object);
				results.addAll(partialResults);
			}catch(RuntimeException e){
				logger.debug("Conversion failed by exeption: "+e);
				Event qualityEvent = new Event();
				
				qualityEvent.setDate(new Date());
				qualityEvent.setAgent_name(object.getInitial_node());
				qualityEvent.setAgent_type(C.AGENT_TYPE_NODE);
				qualityEvent.setType(C.EVENT_TYPE_QUALITY_FAULT_CONVERSION);
				qualityEvent.setSource_file(ci.getSource_file());
				String msg=QualityLevelException.Type.CONVERSION+" | "+ci.getConversion_routine()+" | "+e.getMessage();
				if(msg.length()>Event.MAX_DETAIL_STR_LEN)
					msg=msg.substring(0,Event.MAX_DETAIL_STR_LEN);
				qualityEvent.setDetail(msg);

				logger.debug("QualityEvent created: "+qualityEvent);
				object.getLatestPackage().getEvents().add(qualityEvent);
				//results.add(qualityEvent); //if the quality-events are passing to calling method, it have to be insured to expect not only CONVERT events
			}
		}

		logger.info("Resulting Events");
		for (Event e: results){
			e.setAgent_type("NODE");
			e.setAgent_name(wa.getNode().getName());
			
			logger.info(e.getTarget_file().getRep_name()+"/"+e.getTarget_file().getRelative_path());
			// Double check if the file really exists
			if (!wa.toFile(e.getTarget_file()).exists()) 
				throw new RuntimeException("Target file " + wa.toFile(e.getTarget_file()).getAbsolutePath() +
						" does not exist");
		}
		
		return results;
	}

	
	
	
	private void waitUntilThereIsSufficientSpaceOnCacheResource(String path,int neededAmountInKb,int sleepInterval){
		long freeSpaceKb = 0;
		do {
			try {
				freeSpaceKb = FileSystemUtils.freeSpaceKb(path);
				
			} catch (IOException e) {
				throw new RuntimeException("Error while trying to determine free space on cache resouce",e);
			}
			logger.debug("free space on cache resource is: "+freeSpaceKb+"kb");
			
			if (freeSpaceKb > neededAmountInKb) break; // to prevent waiting when sufficient space is there
			try {
				Thread.sleep(sleepInterval);          
			} catch (InterruptedException e) {
				throw new RuntimeException("Problem with thread sleep",e);
			}
			
		} while ( freeSpaceKb < neededAmountInKb );
	}
	
	
	/**
	 * Executes a conversion routine. Therefore creates an instance of the java class mentioned in the ConversionInstruction ci.
	 * Checks whether the resulting file exists and is not a null byte file.
	 * @throws IOException 
	 * 
	 * @throws RuntimeException if resulting file doesn't exit or is null byte file.
	 */
	private List<Event> executeConversionInstruction(WorkArea wa,ConversionInstruction ci,Object object) throws IOException  {
		if (ci.getConversion_routine()==null) throw new InvalidParameterException("ConversionRoutine not set in ConversionInstruction");
		
		final ConversionStrategy strategy;
		try {
			strategy = getStrategy(ci.getConversion_routine().getType());
		} catch (Exception e) {
			throw new RuntimeException("Critical Error: Could not create strategy of type: "+ci.getConversion_routine().getType()+
					". Please inform the DA-Admin to view the settings for the conversionRoutine "+ci.getConversion_routine().getName(),e);
		} 
		
		logger.info("Preparing conversion of: "+ci.getSource_file()+"\"->\""+ci.getTarget_folder()+"\" " +
				"(CI with id \""+ci.getId()+"\" will be executed with "+ci.getConversion_routine().getName()+" which is of type "+
				strategy.getClass().toString()+").");
		

		strategy.setCLIConnector(new CommandLineConnector());
		strategy.setObject(object);
		strategy.setParam(ci.getConversion_routine().getParams());
		strategy.setKnownFormatCommandLineErrors(knownFormatCmdLineErrors);
		List<Event> results;
		// TODO remove try catch. evaluate in convertbatch and not in convertfile
		try {
			results = strategy.convertFile(wa,ci);
		}	
		catch (FileNotFoundException e) {
			throw new RuntimeException("Resulting file of conversion strategy does not exist",e );
		}
		return results;
	}
	
	
	
	/**
	 * Gets an instance of a strategy with the fully qualified java name strategyName.
	 * 
	 * @author Daniel M. de Oliveira
	 * @param strategyName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public ConversionStrategy getStrategy(String strategyName) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
		ConversionStrategy strategy=null;

		Class<ConversionStrategy> c;
		c = (Class<ConversionStrategy>) Class.forName(strategyName);
		java.lang.reflect.Constructor<ConversionStrategy> co = c.getConstructor();
		strategy= co.newInstance();
		strategy.setPruneErrorOrWarnings(prune);
		return strategy;
	}
	
	
}
