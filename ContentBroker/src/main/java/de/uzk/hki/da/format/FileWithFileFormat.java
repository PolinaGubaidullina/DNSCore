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

import java.util.List;

import de.uzk.hki.da.model.KnownError;
import de.uzk.hki.da.utils.Path;

/**
 * @author Daniel M. de Oliveira
 */
public interface FileWithFileFormat {

	public String getFormatPUID();

	public void setFormatPUID(String formatPUID);

	public String getSubformatIdentifier();

	public void setSubformatIdentifier(String formatSecondaryAttribute);

	Path getPath();
	
	public List<KnownError> getKnownErrors();
	
	public void setKnownErrors(List<KnownError> knownError);
	
	/**
	 * List to gather all Exceptions thrown at File identification/validation 
	 * @return List
	 */
	public List<RuntimeException> getUnknownIdentificationErrorList();
}
