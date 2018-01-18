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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.StoragePolicy;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.utils.GenericChecksum;
import de.uzk.hki.da.utils.MD5Checksum;


/**
 * The Class IrodsGridFacadeBase.
 */
public abstract class IrodsGridFacadeBase implements GridFacade {

	/** The logger. */
	private static Logger logger = LoggerFactory
			.getLogger(IrodsGridFacadeBase.class);
	
	/** The irods system connector. */
	protected IrodsSystemConnector irodsSystemConnector;
	
	
	/* (non-Javadoc)
	 * @see de.uzk.hki.da.grid.GridFacade#put(java.io.File, java.lang.String)
	 */
	@Override
	public abstract boolean put(File file, String gridPath, StoragePolicy sp, String checksum) throws IOException;


	/**
	 * Prepare replication.
	 *
	 * @param file the file
	 * @param relative_address_dest the relative_address_dest
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected boolean PrepareReplication(File file, String relative_address_dest, StoragePolicy sp)
			throws IOException {
		
		if (sp.getGridCacheAreaRootPath()==null) throw new IOException("gridCacheAreaRootPath is not set");
		
		if (irodsSystemConnector.getDefaultStorage()==null) logger.error("Default Storage for node named " + sp.getNodeName()+ " must be set!");
		if (irodsSystemConnector.getZone()==null) throw new IOException("MyZone is not set");
		if (!file.exists()) throw new IOException ("Not an existing File to put: "+file);
		
		String address_dest = relative_address_dest;
		if (!relative_address_dest.startsWith("/")) 
			address_dest = "/" + relative_address_dest;
		String targetPhysically = sp.getGridCacheAreaRootPath() + "/" + WorkArea.AIP + address_dest;
		String targetAbsoluteLogicalPath  = "/" + irodsSystemConnector.getZone() + "/" + WorkArea.AIP + address_dest;	
		
		File gridfile = new File (targetPhysically); 	
		
		if (gridfile.exists()) {
			
			if (!replicatedOnlyToCache(targetAbsoluteLogicalPath))
				throw new java.io.IOException("Grid File " +gridfile+" "+targetAbsoluteLogicalPath+" not exclusively or not only available on cache group devices!");
			if (GenericChecksum.getChecksumForLocalFile(file).equals(GenericChecksum.getChecksumForLocalFile(gridfile))){	
				logger.info("GridFile is valid and only available on cache devices");
				return true;
			} else {
				logger.error("Leftovers or invalid file on the grid!");
				irodsSystemConnector.removeFile(targetAbsoluteLogicalPath);
				if (gridfile.exists()) gridfile.delete();
			}
		} 
		
		
		try {
			new File (FilenameUtils.getFullPath(targetPhysically)).mkdir(); 
			FileUtils.copyFile(file, gridfile);
		} catch (IOException e) { 
			logger.error("Error while creating File or directory physically on the GridCachePath, target file may already exist " + e.getMessage());
			return false; 
		}
		if (!GenericChecksum.getChecksumForLocalFile(file).equals(GenericChecksum.getChecksumForLocalFile(gridfile))){
			logger.error(" put of " + file +" failed");
			return false;
		}
		return true;
	}	
	
	
	/**
	 * Sets the irods system connector.
	 *
	 * @param isc the new irods system connector
	 */
	public void setIrodsSystemConnector(IrodsSystemConnector isc) {
		irodsSystemConnector = isc;
	}
	
	/**
	 * Gets the irods system connector.
	 *
	 * @return the irods system connector
	 */
	public IrodsSystemConnector getirodsSystemConnector() {
		return irodsSystemConnector; 
	}
		
	/* (non-Javadoc)
	 * @see de.uzk.hki.da.grid.GridFacade#storagePolicyAchieved(java.lang.String, int)
	 */
	@Override
	public abstract boolean storagePolicyAchieved(String gridPath, StoragePolicy sp, String checksum, Set<Node> cnodes);
	
	/**
	 * Replication is solely on cache.
	 *
	 * @param targetLogically the target logically
	 * @return true, if successful
	 * @author Jens Peters
	 */
	private boolean replicatedOnlyToCache(String targetLogically) {
		String nr = irodsSystemConnector.executeRule("repls { \n " +
				"*nr=0;\n" +
				"acGetTotalReplNumber(*obj,*nr);\n"
		+"}\n"
		+"INPUT *obj=\""+targetLogically+"\"\n"
		+"OUTPUT *nr","*nr");
		logger.debug("iRODS tells us, file " +targetLogically+ " has already >" +nr+"< Repls");
		
		String nr2 = irodsSystemConnector.executeRule("repls { \n " +
				"*nr=0;\n" +
				"acGetTotalReplNumberPerGroup(*obj,\"cache\",*nr);\n"
		+"}\n"
		+"INPUT *obj=\""+targetLogically+"\"\n"
		+"OUTPUT *nr","*nr");
		logger.debug("iRODS tells us, file " +targetLogically+ " has cache >" +nr+"< Repls");
		if (nr.equals("1") && nr2.equals("1")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Does the implementation for iRODS
	 * Verifies Checksum of all replicas in the zone.
	 *
	 * @param address_dest the address_dest
	 * @return true, if is valid
	 * @author jpeters
	 */
	@Override
	public boolean isValid(String address_dest) {

		address_dest = "/" + irodsSystemConnector.getZone() + "/" + WorkArea.AIP + "/" + address_dest;
		irodsSystemConnector.establishConnect();
		try{
			irodsSystemConnector.getChecksum(address_dest);
		}catch(IrodsRuntimeException e){
			logger.error("No checksum found for: " + address_dest);
			return false;
		}
			
		try {
			String status = irodsSystemConnector.executeRule("validity { \n " +
					"*status=0;\n" +
					"acVerifyChecksum(*obj,*status);\n"
			+"}\n"
			+"INPUT *obj=\""+address_dest+"\"\n"
			+"OUTPUT *status","*status");
			logger.debug("iRODS tells us, file " +address_dest+ " is in state >" +status+"<");
			if (status.equals("1")) {
				irodsSystemConnector.logoff();
				return true;
			}
			irodsSystemConnector.logoff();
			return false;
		} catch (Exception e) {
			logger.error("Error while trying to connect to iRODS to check validity of File " + address_dest);
			irodsSystemConnector.logoff();
			return false;
		}
	}
	
	
	/**
	 * Does the implementation for iRODS
	 * Checks whether file is valid
	 * Unloads the File given by address_dest to file destination.
	 *
	 * @param destination the destination
	 * @param gridFileAdress 
	 * @throws IOException on error
	 * @author jpeters
	 */
	@Override
	public void get(File destination, String gridFileAdress) throws IOException  { 
		getFile(irodsSystemConnector.getZone(),destination,gridFileAdress);
	}
	
	@Override
	public void getFederated(String federatedZone, File destination, String gridFileAdress) throws IOException  { 
		getFile(irodsSystemConnector.getZone()+ "/" + WorkArea.FEDERATED+ "/"+federatedZone,destination,gridFileAdress);
	}
	
	
	private void getFile(String zone, File destination, String gridFileAdress) throws IOException  { 
		
		String prefixedGridFileAdress = "/" + zone+ "/" + WorkArea.AIP + "/" + gridFileAdress;
		irodsSystemConnector.establishConnect();
		try {
			
//			if (!isValid(gridFileAdress))  throw new java.io.IOException("File has corrupt replicas, please check first! File: " + gridFileAdress);
			irodsSystemConnector.get(prefixedGridFileAdress, destination);
		} catch (Exception e) {
			// TODO should throw gridexception
			throw new java.io.IOException("Error in retrieving file: " + prefixedGridFileAdress, e);
		}
		if (!GenericChecksum.getChecksumForLocalFile(destination).equals(irodsSystemConnector.getChecksum(prefixedGridFileAdress))){
			throw new java.io.IOException("The unloaded file differs from the Grid's file! Local:"
					+GenericChecksum.getChecksumForLocalFile(destination)+" vs Remote:"+irodsSystemConnector.getChecksum(prefixedGridFileAdress));
		}
		
		if (!destination.exists()) throw new java.io.IOException("The destination file has " + destination + " not been created!");
		
		irodsSystemConnector.logoff();
	}
	
	
	/* (non-Javadoc)
	 * @see de.uzk.hki.da.grid.GridFacade#getFileSize(java.lang.String)
	 */
	@Override
	public long getFileSize(String address_dest) throws IOException {
		irodsSystemConnector.establishConnect();
		
		long filesize = irodsSystemConnector.getFileSize("/" + irodsSystemConnector.getZone()+ "/" + WorkArea.AIP + "/" + address_dest);
		
		irodsSystemConnector.logoff();
		return filesize;
	}
	
}
