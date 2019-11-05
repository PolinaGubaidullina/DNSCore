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

import de.uzk.hki.da.utils.CommandLineConnector;

/**
 * @author Daniel M. de Oliveira
 */
public class FailSubformatIdentifier implements FormatIdentifier, Connector{

	public String identify(File file,boolean pruneExceptions) {
		throw new RuntimeException("Subformat identification failed");
	}

	@Override
	public boolean isConnectable() {
		return true;
	}

	@Override
	public void setCliConnector(CommandLineConnector cli) {
	}

	@Override
	public CommandLineConnector getCliConnector() {
		return null;
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
