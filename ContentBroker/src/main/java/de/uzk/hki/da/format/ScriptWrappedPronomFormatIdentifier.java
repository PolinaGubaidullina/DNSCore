/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2014 LVRInfoKom
  Landschaftsverband Rheinland

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

package de.uzk.hki.da.format;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.CommaSeparatedList;
import de.uzk.hki.da.utils.CommandLineConnector;
import de.uzk.hki.da.utils.ProcessInformation;



/**
 * Connects to an external application to determine the PUID of files.
 * 
 * @author Daniel M. de Oliveira
 */
public class ScriptWrappedPronomFormatIdentifier implements FormatIdentifier, Connector {

	private static final Logger logger = LoggerFactory.getLogger(ScriptWrappedPronomFormatIdentifier.class);

	private File conversionScript = null;
	
	private CommandLineConnector cli = null;
	
	public ScriptWrappedPronomFormatIdentifier(File cs){
		this.conversionScript=cs;
	}
	
	/**
	 * Lets the fido program determine a set of pronom identifiers, 
	 * from which the last one is chosen and returned.
	 * 
	 * @param fff
	 * @return PUID or UNDEFINED if fido cannot determine the file format. 
	 * @throws IOException if it is not possible to run the script successfully for some reason.
	 */
	@Override
	public
	String identify(File file,boolean pruneExceptions) throws IOException{
		if (!conversionScript.exists()) throw new IllegalStateException(
				"ConversionScript doesn't exist: "+conversionScript.getAbsolutePath());
		
		
		if (!file.exists()) throw new Error("File ("+file.getAbsolutePath()+") doesn't exist");

		
		ProcessInformation pi = new CommandLineConnector().runCmdSynchronously( new String[]{
				
				conversionScript.getAbsolutePath(),
				file.getAbsolutePath()
		},null,60000);
		
		
		if (pi.getExitValue()!=0){
			logger.warn("stdout from identification: "+pi.getStdErr());
			logger.warn("FormatIdentifier with exit value: " + pi.getExitValue());
			throw new RuntimeException("PUID is "+C.UNRECOGNIZED_PUID);
			//return C.UNRECOGNIZED_PUID;
		}
		
		logger.debug("stdout from identification: "+pi.getStdOut());
		Set<String> fileFormatsIdentifiers= new HashSet<String>(new CommaSeparatedList(pi.getStdOut()).toList());
		
		String result="";
		for (String r:fileFormatsIdentifiers){
			result=r;
		}
		return result;
	}

	@Override
	public boolean isConnectable() {

		return false;
	}

	@Override
	public void setCliConnector(CommandLineConnector cli) {
			this.cli = cli;
		
	}

	@Override
	public CommandLineConnector getCliConnector() {
		if (this.cli==null) this.cli = new CommandLineConnector();
		return this.cli;
	}

	@Override
	public void setKnownFormatCommandLineErrors(
			KnownFormatCmdLineErrors knownErrors) {
	}

	@Override
	public KnownFormatCmdLineErrors getKnownFormatCommandLineErrors() {
	
		return null;
	}
}
