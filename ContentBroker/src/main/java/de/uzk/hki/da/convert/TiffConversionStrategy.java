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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.format.FormatCmdLineExecutor;
import de.uzk.hki.da.format.ImageMagickSubformatIdentifier;
import de.uzk.hki.da.format.KnownFormatCmdLineErrors;
import de.uzk.hki.da.format.UserFileFormatException;
import de.uzk.hki.da.model.ConversionInstruction;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Event;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.utils.CommandLineConnector;
import de.uzk.hki.da.utils.Path;
import de.uzk.hki.da.utils.StringUtilities;

/**
 * Scans a Tiff file for compression and in case a compression has been detected
 * it converts the file to a non-compressed version. In case the file is a
 * multipage tif multiple output files get generated (TODO true?). An event for
 * each target file gets created.
 * 
 * @author Daniel M. de Oliveira
 * @author Jens Peters
 *
 */
public class TiffConversionStrategy implements ConversionStrategy {

	/** The encoding. */
	String encoding;

	boolean prune;
	
	/** The logger. */
	private static Logger logger = LoggerFactory
			.getLogger(TiffConversionStrategy.class);

	/** The object. */
	private Object object;

	private CommandLineConnector cliConnector;

	private KnownFormatCmdLineErrors knownErrors;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uzk.hki.da.convert.ConversionStrategy#convertFile(de.uzk.hki.da.model
	 * .ConversionInstruction)
	 */
	@Override
	public List<Event> convertFile(WorkArea wa, ConversionInstruction ci) {

		List<Event> resultEvents = new ArrayList<Event>();

		String input = wa.toFile(ci.getSource_file()).getAbsolutePath();
		String[] commandAsArray;
		String prunedError = "";
		FormatCmdLineExecutor cle = new FormatCmdLineExecutor(cliConnector, knownErrors);
		try {
			// Codec identification is done by subformatidentification
			ImageMagickSubformatIdentifier imsf = new ImageMagickSubformatIdentifier();
			imsf.setKnownFormatCommandLineErrors(knownErrors);
			imsf.setCliConnector(cliConnector);
		
			if (imsf.identify(new File(input),prune)
					.indexOf("None")>=0)
				return resultEvents;

			// create subfolder if necessary
			Path.make(wa.dataPath(), object.getNameOfLatestBRep(),
					ci.getTarget_folder()).toFile().mkdirs();
			
			commandAsArray = new String[] { "convert", "+compress",
					input, generateTargetFilePath(wa, ci) };
			
			
		
			logger.debug("Try to Execute conversion command: " +  StringUtils.join(commandAsArray,","));
			
			cle.setPruneExceptions(prune);
			try {
			cle.execute(commandAsArray);
			} catch (UserFileFormatException ufe) {
				if (!ufe.isWasPruned())
					throw ufe;
				prunedError = " " + ufe.getKnownError().getError_name()  + " ISSUED WAS PRUNED BY USER!";
			}
			} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		File result = new File(generateTargetFilePath(wa, ci));

		String baseName = FilenameUtils.getBaseName(result.getAbsolutePath());
		String extension = FilenameUtils.getExtension(result.getAbsolutePath());
		logger.info("Finding files matching wildcard expression \""
				+ baseName
				+ "*."
				+ extension
				+ "\" in order to check them and test if conversion was successful");
		List<File> results = findFilesWithWildcard(
				new File(FilenameUtils.getFullPath(result.getAbsolutePath())),
				baseName + "*." + extension);
		
		for (File f : results) {
			DAFile daf = new DAFile(object.getNameOfLatestBRep(),
					StringUtilities.slashize(ci.getTarget_folder())
							+ f.getName());
			logger.debug("new dafile:" + daf);

			Event e = new Event();
			e.setType("CONVERT");
			
			e.setDetail(StringUtilities.createString(commandAsArray) +  prunedError);
			e.setSource_file(ci.getSource_file());
			e.setTarget_file(daf);
			e.setDate(new Date());

			resultEvents.add(e);
		}

		return resultEvents;
	}

	/**
	 * Find files with wildcard.
	 *
	 * @param folderToScan
	 *            the folder to scan
	 * @param wildcardExpression
	 *            the wildcard expression
	 * @return all files matching wildcardExpression
	 */
	private List<File> findFilesWithWildcard(File folderToScan,
			String wildcardExpression) {

		List<File> result = new ArrayList<File>();

		FileFilter fileFilter = new WildcardFileFilter(wildcardExpression);
		File[] files = folderToScan.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			result.add(files[i]);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uzk.hki.da.convert.ConversionStrategy#setParam(java.lang.String)
	 */
	@Override
	public void setParam(String param) {
	}

	/**
	 * Generate target file path.
	 *
	 * @param ci
	 *            the ci
	 * @return the string
	 */
	public String generateTargetFilePath(WorkArea wa, ConversionInstruction ci) {
		String input = wa.toFile(ci.getSource_file()).getAbsolutePath();
		return wa.dataPath() + "/" + object.getNameOfLatestBRep() + "/"
				+ StringUtilities.slashize(ci.getTarget_folder())
				+ FilenameUtils.getName(input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uzk.hki.da.convert.ConversionStrategy#setCLIConnector(de.uzk.hki.da
	 * .convert.CLIConnector)
	 */
	@Override
	public void setCLIConnector(CommandLineConnector cliConnector) {
		this.cliConnector = cliConnector;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uzk.hki.da.convert.ConversionStrategy#setObject(de.uzk.hki.da.model
	 * .Object)
	 */
	@Override
	public void setObject(Object obj) {
		this.object = obj;
	}

	@Override
	public void setPruneErrorOrWarnings(boolean prune) {
		this.prune = prune;
		
	}

	@Override
	public void setKnownFormatCommandLineErrors(
			KnownFormatCmdLineErrors knownErrors) {
		this.knownErrors = knownErrors;
	}
}
