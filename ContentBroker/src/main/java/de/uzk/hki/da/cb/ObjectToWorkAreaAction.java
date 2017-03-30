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

import java.io.IOException;

import de.uzk.hki.da.action.AbstractAction;
import de.uzk.hki.da.core.IngestGate;
import de.uzk.hki.da.grid.GridFacade;
import de.uzk.hki.da.model.DocumentsGenService;
import de.uzk.hki.da.util.ConfigurationException;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.Path;


/**
 * @author Daniel M. de Oliveira
 */

public class ObjectToWorkAreaAction extends AbstractAction {

	private IngestGate ingestGate;
	private GridFacade gridFacade;
	private DocumentsGenService dgs = new DocumentsGenService();
	
	public ObjectToWorkAreaAction(){SUPPRESS_OBJECT_CONSISTENCY_CHECK=true;}
	
	@Override
	public void checkConfiguration() {
		if (ingestGate==null) throw new ConfigurationException("ingestGate");
		if (gridFacade==null) throw new ConfigurationException("gridFacade");
	}


	@Override
	public void checkPreconditions() {
	}
	
	@Override
	public boolean implementation() {
		
		Path.makeFile(wa.dataPath()).mkdirs();
		
		RetrievePackagesHelper retrievePackagesHelper = new RetrievePackagesHelper(getGridFacade(),wa);
		
		try {
			if (!ingestGate.canHandle(retrievePackagesHelper.getObjectSize(o, j))) {
//				JmsMessage jms = new JmsMessage(C.QUEUE_TO_CLIENT,C.QUEUE_TO_SERVER,o.getIdentifier() + " - Please check WorkArea space limitations: " + ingestGate.getFreeDiskSpacePercent() +" % free needed " );
//				super.getJmsMessageServiceHandler().sendJMSMessage(jms);	
				logger.warn("ResourceMonitor prevents further processing of package due to space limitations - Setting job back to start state.");
				return false;
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to determine object size for object " + o.getIdentifier(), e);
		}
		
		try {
			retrievePackagesHelper.loadPackages(o, true);
		} catch (IOException e) {
			throw new RuntimeException("error while trying to get existing packages from lza area",e);
		}
		
		dgs.addDocumentsToObject(o);
		return true;
	}

	
	
	
	
	@Override
	public void rollback() throws Exception {
		FolderUtils.deleteQuietlySafe(Path.makeFile(wa.dataPath()));
	}





	public IngestGate getIngestGate() {
		return ingestGate;
	}





	public void setIngestGate(IngestGate ingestGate) {
		this.ingestGate = ingestGate;
	}




	public GridFacade getGridFacade() {
		return gridFacade;
	}





	public void setGridFacade(GridFacade gridFacade) {
		this.gridFacade = gridFacade;
	}

}
