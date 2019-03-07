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

package de.uzk.hki.da.grid;

/**
 * @author Jens Peters
 * The Federated Grid Facade for having a Federation of independent 
 * iRODS Servers. Depends on special configuration on your grid
 */
import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.StoragePolicy;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.MD5Checksum;
import de.uzk.hki.da.utils.StringUtilities;



/**
 * The Class IrodsFederatedGridFacade.
 */
public class IrodsFederatedGridFacade extends IrodsGridFacade {

	/** The logger. */
	private static Logger logger = LoggerFactory
			.getLogger(IrodsFederatedGridFacade.class);

	/* (non-Javadoc)
	 * @see de.uzk.hki.da.grid.IrodsGridFacadeBase#put(java.io.File, java.lang.String)
	 */
	@Override
	public boolean put(File file, String address_dest , StoragePolicy sp, String checksum) throws IOException {
		IrodsCommandLineConnector iclc = new IrodsCommandLineConnector();
		if (!address_dest.startsWith("/")) address_dest = "/" + address_dest;
		String gridPath = "/" + irodsSystemConnector.getZone() + "/" + WorkArea.AIP + address_dest;
		String destCollection = FilenameUtils.getFullPath(gridPath);
		
		if (!iclc.exists(destCollection)) {
			logger.debug("creating Coll " + destCollection);
			iclc.mkCollection(destCollection);
		}
		
		if (iclc.put(file, gridPath, sp.getCommonStorageRescName())) {
			if (sp.getForbiddenNodes()!=null && !sp.getForbiddenNodes().isEmpty()) iclc.setIMeta(gridPath, "FORBIDDEN_NODES", String.valueOf(sp));
			iclc.setIMeta(gridPath, "MIN_COPIES", String.valueOf(sp.getMinNodes()));
			String checksumAfterPut = iclc.getChecksum(gridPath);

			if (StringUtilities.isNotSet(checksumAfterPut)) {
				throw new IOException("iRODS found no checksum for " + gridPath);
			}
			
			if (!StringUtilities.isNotSet(checksum)) {
				if (!checksumAfterPut.equals(checksum)) {
					logger.error("Given Checksum of Package has to be " + checksum);
					logger.error("Checksum is " + checksumAfterPut);
					throw new IOException("Checksum not correct on put!");
				}
			}
			iclc.setIMeta(gridPath, "chksum", checksumAfterPut);
			iclc.setIMeta(gridPath, "FEDERATED", "0");
			return true;
		} else {
			logger.debug("Unable to put the aip file to irods");
			return false;
		}	
	}
	
	@Override
	public boolean putToReplDir(File file, String address_dest , StoragePolicy sp, String checksum) throws IOException {
		IrodsCommandLineConnector iclc = new IrodsCommandLineConnector();
		String destCollection = FilenameUtils.getFullPath(address_dest);
		if (!iclc.exists(destCollection)) {
			logger.debug("creating Coll " + destCollection);
			iclc.mkCollection(destCollection);
		}
		
		logger.debug("dest recource "+sp.getWorkingResource());
	
		if (iclc.put(file, address_dest, sp.getWorkingResource())) {
			logger.debug("set destination resource "+sp.getWorkingResource());
			String checksumAfterPut = iclc.getChecksum(address_dest);

			if (StringUtilities.isNotSet(checksumAfterPut)) {
				throw new IOException("iRODS found no checksum for " + address_dest);
			}
			
			if (!StringUtilities.isNotSet(checksum)) {
				if (!checksumAfterPut.equals(checksum)) {
					logger.error("Given Checksum of Package has to be " + checksum);
					logger.error("Checksum is " + checksumAfterPut);
					throw new IOException("Checksum not correct on put!");
				}
			}
			return true;
		} else {
			logger.debug("Unable to put the aip file to irods repl directory");
			return false;
		}
	}
	
	@Override
	public boolean storagePolicyAchieved(String gridPath2, StoragePolicy sp, String checksum, Set<Node> cnodes) {
		try {
			String gridPath = "/" + irodsSystemConnector.getZone() + "/" + WorkArea.AIP + "/" + gridPath2;
			IrodsCommandLineConnector iclc = new IrodsCommandLineConnector();
			logger.debug("StoragePolicy checking called!");
			int minNodes = sp.getMinNodes();
			if (minNodes == 0 ) {
				logger.error("Given minnodes setting 0 violates long term preservation");
				return false;
			}
			
			logger.info("Checking copies of " + gridPath );
			int numberOfCopies = 0;
			if (checksum!=null && !checksum.equals("")) {
				if (iclc.existsWithChecksum(gridPath, checksum)) {
					numberOfCopies++;
				}
			} else {
				if (iclc.exists(gridPath)) numberOfCopies++;
			}
			if (cnodes!=null) {
			for (Node node: cnodes) {
				String remoteGridPath = "/"+ node.getIdentifier() +"/federated" + gridPath;
				logger.info("Checking existence of remote Copy at " + remoteGridPath);
				try {
					if (iclc.existsWithChecksum(remoteGridPath, checksum)) {
						numberOfCopies++;
					} 
				} catch (Exception irex) {
					logger.error("recieved Exception while checking "+ remoteGridPath + " "+ irex.getMessage());
				}
			}
			} else logger.debug("Cooperating nodes was NULL, checking only local copy");
			if (numberOfCopies>= minNodes) {
				logger.info ("Reached number of Copies :" + numberOfCopies);
				return true;
			} else {
				logger.debug("Found only " +numberOfCopies + " yet, return later. ");
			}
		} catch (Exception irex) {
			logger.error("recieved Exception while checking storagePolicy - interpreting as false: " + irex.getMessage());
		}
		return false;

	}
	// deprecated !!!
	@Override
	public boolean isValid(String gridPath) {
	String address_dest = "/" + irodsSystemConnector.getZone() + "/" + WorkArea.AIP + "/" + gridPath;
		logger.debug("checking validity of " + address_dest);
		try {
			irodsSystemConnector.establishConnect();;	
	String check = irodsSystemConnector.executeRule("checkItemsQuick {\n"
	      + "*state=0\n"
	      + "acIsValid(*dataObj,*state)\n"
	      + "}\n"
	      + "INPUT *dataObj=\"" + address_dest +"\"\n"
	      + "OUTPUT ruleExecOut", "ruleExecOut");
		if (check!=null && !check.isEmpty() ) {
			if (check.indexOf("state 1")>0) {
				logger.debug("claimed state by iRODS Datagrid is: true");
				return true;
			}
		}	
		irodsSystemConnector.logoff();
		} catch (Exception e) {
			logger.error("Catched Exception " + e.getMessage());
			
		}
		logger.debug("claimed state by iRODS Datagrid is: false");
		return false;
	}
	
	@Override
	public void distribute(Node node, File fileToDistribute, String address_dest, StoragePolicy sp) {
		if (!address_dest.startsWith("/")) address_dest = "/" + address_dest;
		String gridPath = "/" + irodsSystemConnector.getZone() + "/" + WorkArea.AIP + address_dest;	
		
		for (Node cn: node.getCooperatingNodes()) {
			CreateCopyJob cj = new CreateCopyJob();
			logger.debug("Create copy job. Source: "+fileToDistribute.getPath());
			try {
				cj.createCopyJob(fileToDistribute.getPath(), gridPath, cn.getIdentifier(), node.getIdentifier(),sp.getCommonStorageRescName());
			} catch (Exception e) {
				throw new RuntimeException("Unable to create copy job!");
			}		
		}
	}
	
	
	@Override
	public long getFileSize(String contrObj) throws IOException {
		String address_dest = "/" + irodsSystemConnector.getZone() + "/" + WorkArea.AIP + "/" + contrObj;
		try {
			IrodsCommandLineConnector iclc = new IrodsCommandLineConnector();
			String ireturn = iclc.iquestDataObjectField(address_dest, "SUM(DATA_SIZE)");
			logger.info("ireturn(" + ireturn + ") for " + address_dest);
			if (ireturn != null && !ireturn.trim().isEmpty()) {
				logger.info("Size(" + ireturn + ") for " + address_dest);
				return new Long(ireturn);
			}else{
				logger.error("Error Size(" + ireturn + ") for " + address_dest);
			}

		} catch (Exception e) {
			logger.error("Catched Exception " + e.getMessage());
		}
		
		return C.UNKNOWN_FILESIZE;
	}

	@Override
	public boolean remove(String dest) {
		IrodsCommandLineConnector iclc = new IrodsCommandLineConnector();
		if (iclc.remove(dest)) {
			return true;
		} else {
			logger.error("Unable to remove the aip file from local repl directory");
			return false;
		}
	}

	@Override
	public void get(File destination, String gridFileAdress) throws IOException  { 
		getFile(irodsSystemConnector.getZone(),destination,gridFileAdress);
	}
	
	@Override
	public void getFederated(String federatedZone, File destination, String gridFileAdress) throws IOException  { 
		getFile(irodsSystemConnector.getZone()+ "/" + WorkArea.FEDERATED+ "/"+federatedZone,destination,gridFileAdress);
	}
	
	
	private void getFile(String zone, File destination, String gridFileAdress) throws IOException  { 
		IrodsCommandLineConnector iclc = new IrodsCommandLineConnector();
		String prefixedGridFileAdress = "/" + zone+ "/" + WorkArea.AIP + "/" + gridFileAdress;
		iclc.get(destination, prefixedGridFileAdress);
	}
	
}
