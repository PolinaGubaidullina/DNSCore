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
package de.uzk.hki.da.cb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.uzk.hki.da.action.AbstractAction;
import de.uzk.hki.da.core.MailContents;
import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.Path;

/**
 * For a given object 
 * <li>deletes the object folder from the UserArea
 * <li>deletes any existing packages belonging to the Object from the IngestArea
 * @author Daniel M. de Oliveira
 */
public class DeleteObjectAction extends AbstractAction {

	
	public DeleteObjectAction() {
		setKILLATEXIT(true);
	}
	
	@Override
	public void checkConfiguration() {
	}
	

	@Override
	public void checkPreconditions() {
	}
	
	@Override
	public boolean implementation() throws FileNotFoundException, IOException,
			UserException {

		if (o.getPackages().size()==1){
			logger.info("Deleting object from database");
			DELETEOBJECT=true;
		}
		else 
		if (o.getPackages().size()>1){
			o.getPackages().remove(o.getLatestPackage());
		}
		logger.info("Deleting object from WorkArea: "+wa.objectPath());
		FolderUtils.deleteDirectorySafe(wa.objectPath().toFile());
		
		if (fileInWorkArea().exists()) {
			logger.info("Delete container from WorkArea: " + fileInWorkArea() );
			fileInWorkArea().delete();
		}
		if (fileInIngestArea().exists()) {
			logger.info("Delete container from WorkArea: " + fileInIngestArea() );
			fileInIngestArea().delete();
		}
		new MailContents(preservationSystem,n).deleteObjectFromWorklfow(o);
		return true;
	}

	
	@Override
	public void rollback() throws Exception {
		// Nothing to do.
	}


	private File fileInIngestArea() {
		return Path.makeFile(
				n.getIngestAreaRootPath(),
				o.getContractor().getShort_name(),o.getLatestPackage().getContainerName());
	}

	private File fileInWorkArea() {
		return Path.makeFile(
				n.getWorkAreaRootPath(),"work",
				o.getContractor().getShort_name(),o.getLatestPackage().getContainerName());
	}

}
