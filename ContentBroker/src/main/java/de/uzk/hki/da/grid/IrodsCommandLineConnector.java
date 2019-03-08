/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 HKI, 2014 LVRInfKom

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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.utils.CommandLineConnector;
import de.uzk.hki.da.utils.GenericChecksum;
import de.uzk.hki.da.utils.ProcessInformation;
import de.uzk.hki.da.utils.StringUtilities;


/**
 * Interface for the classic ICommands of IRODS
 * @author Jens Peters
 */
public class IrodsCommandLineConnector {
	/** configured checksum type in irods configs */
	static final String CHECKSUM_TYPE=GenericChecksum.DEFAULT_CHECKSUM_ALGO.toString();//GenericChecksum.Algorithm.SHA256.toString(); GenericChecksum.Algorithm.MD5.toString();
	/** CommandLineConnector */
	private CommandLineConnector clc;
	/** The logger. */
	private static Logger logger = LoggerFactory
			.getLogger(IrodsCommandLineConnector.class);
	
	/** Constructor */
	public IrodsCommandLineConnector() {
		this.clc = new CommandLineConnector();
	}
	
	/**
	 * Executes given Icommand 
	 * @author Jens Peters
	 * @param commandAsArray
	 * @return
	 */
	public String executeIcommand(String[] commandAsArray) {
		ProcessInformation pi = null;
		try {
			pi = clc.runCmdSynchronously(commandAsArray,0);
		} catch (IOException e1) {
			throw new RuntimeException("Icommand did not succeed, not found: " + Arrays.toString(commandAsArray) + "because of " +e1);
		}
		if (pi.getExitValue()!=0) {
			logger.error("Icommand did not succeed: " + Arrays.toString(commandAsArray) + " returned " +pi.getStdErr() );
			throw new RuntimeException("Icommand did not succeed" + Arrays.toString(commandAsArray) + pi.getStdErr());
		}
		logger.debug (pi.getStdOut().trim());
		return pi.getStdOut();
	}

	/**
	 * put file to irods
	 * @author Jens Peters
	 * @param file
	 * @param dest
	 * @return
	 */
	public boolean put(File file, String dest) {
		return put (file, dest, "");
	}

	/**
	 * Get File from irods
	 * @author Jens Peters
	 * @param file
	 * @param sourceGridFile
	 * @return
	 */
	public boolean get(File file, String sourceGridFile) {
		return get (file, sourceGridFile, "");
	}
	
	/**
	 * Get File from irods
	 * @author Jens Peters
	 * @param file
	 * @param sourceGridFile
	 * @param sourceRescName
	 * @return
	 */
	
	public boolean get(File file, String sourceGridFile, String sourceRescName) {
		ArrayList<String>  commandAsList = new ArrayList<String>();
		commandAsList.add("iget");
		commandAsList.add("-K");
		if (!sourceRescName.equals("")) {
			commandAsList.add("-R");
			commandAsList.add(sourceRescName);
		}
		commandAsList.add(sourceGridFile);
		commandAsList.add(file.getAbsoluteFile().toString());
		String[] commandAsArray = new String[commandAsList.size()];
		commandAsArray = commandAsList.toArray(commandAsArray);
		
		try {
			executeIcommand(commandAsArray);
		} catch (RuntimeException e) {
			return false;
		}
		if (!file.exists()) return false;
		return true;
	}
	
	/**
	 * Put file to destination
	 * @author Jens Peters
	 * @param file
	 * @param dest
	 * @param resourceName
	 * @return
	 */
	
	public boolean put(File file, String dest, String resourceName) {
		ArrayList<String>  commandAsList = new ArrayList<String>();
		commandAsList.add("iput");
		commandAsList.add("-K");
		if (!resourceName.equals("")) {
			commandAsList.add("-R");
			commandAsList.add(resourceName);
		}
		commandAsList.add(file.getAbsoluteFile().toString());
		commandAsList.add(dest);
		String[] commandAsArray = new String[commandAsList.size()];
		commandAsArray = commandAsList.toArray(commandAsArray);
		try {
		executeIcommand(commandAsArray);
		}catch (RuntimeException e) {
			return false;
		} return true;
	}
	
	/**
	 * Removes Destination
	 * @author Jens Peters
	 * @param dest
	 * @return
	 */
	public boolean remove(String dest) {
		String commandAsArray[] = new String[]{
				"irm", "-rf" , dest 
		}; 
		try {
			executeIcommand(commandAsArray);
		} catch (RuntimeException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Tests if given dest DAO exists
	 * @author Jens Peters
	 * @param dest
	 * @return
	 */
	public boolean exists(String dest) {
		String commandAsArray[] = new String[]{
				"ils", dest 
		}; 
		try {
			executeIcommand(commandAsArray);
		} catch (RuntimeException e) {

			 return false;
		}
		return true;
	}
	
	/**
	 * Checks whether the dataobject exists at that given dest with the given 
	 * md5sum. 
	 * @author Jens Peters
	 * @param dest
	 * @param checksum
	 * @return
	 */
	public boolean existsWithChecksum(String dest, String checksum) {
		if (checksum.equals(null) || checksum.trim().equals("")) throw new RuntimeException("checksum not given");
		checksum=prepareDNSCheckSumForIrods(checksum);
		String data_name = FilenameUtils.getName(dest);
		String commandAsArray[] = new String[]{
				"ils","-L", dest 
		}; 
		String out = executeIcommand(commandAsArray);
		if (out.indexOf(data_name)>=0 && out.indexOf(checksum)>0) return true;
		return false;
	}
	
	/**
	 * Computes Checksum and stores it to ICAT and returns it. 
	 * @author Jens Peters
	 * @param destDao
	 * @return
	 */
	public String computeChecksumForce(String destDao) {
		String commandAsArray[] = new String[]{
				"nice","-n","10","ichksum","-fa",destDao
		};	
		executeIcommand(commandAsArray);
		return getChecksum(destDao);
	}
	
	/**
	 * Gets checksum from ICAT for the newest instance destDao
	 * @author Jens Peters
	 * @param destDao
	 * @return
	 */
	public String getChecksum(String destDao) {
		String ret = "";
		String commandAsArray[] = new String[]{
				"ichksum",destDao
		};	
		String out = executeIcommand(commandAsArray);
		if (out.indexOf("ERROR")>=0) throw new RuntimeException(" Get Checksum of " + destDao + " failed !" );
		Scanner scanner = new Scanner(out);
		String data_name = FilenameUtils.getName(destDao);
		if (data_name.length()>30) data_name = data_name.substring(0, 30);
		while (scanner.hasNextLine()) {
		  String line = scanner.nextLine();
		  if (line.contains(data_name)) {
			  ret = line.substring(38,line.length());
		  }
		}
		scanner.close();
		return prepareIrodsCheckSumForDNS(ret);
	}
	
	public static String prepareIrodsCheckSumForDNS(String chksum){
		if(CHECKSUM_TYPE.equals(GenericChecksum.Algorithm.SHA256.toString())){
			return GenericChecksum.decodeBase64(chksum.split("sha2:")[1]);
		}else if(CHECKSUM_TYPE.equals(GenericChecksum.Algorithm.MD5.toString()))
			return chksum;
		else
			throw new RuntimeException("Unknown checksum type: "+CHECKSUM_TYPE);
	}
	
	public static String prepareDNSCheckSumForIrods(String chksum){
		if(CHECKSUM_TYPE.equals(GenericChecksum.Algorithm.SHA256.toString())){
			return "sha2:"+GenericChecksum.encodeBase64(chksum);
		}else if(CHECKSUM_TYPE.equals(GenericChecksum.Algorithm.MD5.toString()))
			return chksum;
		else
			throw new RuntimeException("Unknown checksum type: "+CHECKSUM_TYPE);
	}
	
	/**
	 * Gets checksum from ICAT for the newest instance destDao
	 * @author Jens Peters
	 * @param destDao
	 * @return
	 */
	public String getChecksumOfLatestReplica(String destDao) {
		String ret = "";
		String commandAsArray[] = new String[]{
				"ils -L",destDao
		};	
		String out = executeIcommand(commandAsArray);
		if (out.indexOf("ERROR")>=0) throw new RuntimeException(" Get Checksum of " + destDao + " failed !" );
		Scanner scanner = new Scanner(out);
		String data_name = FilenameUtils.getName(destDao);
		while (scanner.hasNextLine()) {
		  String line = scanner.nextLine();
		  if (line.contains(data_name)) {
			  ret = line.substring(38,line.length());
		  }
		}
		scanner.close();
		return ret;
	}
	
	/**
	 * Validates all Copies in the zone, checks against stored entry in ICAT
	 * @author Jens Peters
	 * @param dao
	 * @return
	 */
	public boolean isValid(String dao) {
		String commandAsArray[] = new String[]{
				"ichksum","-Ka",dao
		};	
		try {
			executeIcommand(commandAsArray);
		} catch (RuntimeException r) {
			return false;
		}
		return true;
	}
	
	/**
	 * Creates Collection 
	 * @author Jens Peters
	 * @param destColl
	 */
	public void unregColl(String destColl) {
		String mKcommandAsArray[] = new String[]{
				"irm","-rU",destColl
		};
		executeIcommand(mKcommandAsArray);	
	}
	/**
	 * Creates Collection 
	 * @author Jens Peters
	 * @param destColl
	 */
	public void mkCollection(String destColl) {
		String mKcommandAsArray[] = new String[]{
				"imkdir","-p",destColl
		};
		executeIcommand(mKcommandAsArray);	
	}
	
	/**
	 * irsyncs Dataobject to destination on given resource
	 * @author Jens Peters
	 * @param dao
	 * @param destRescName
	 * @return
	 */
	public String rsync(String dao, String destDao, String destRescName) {
		if (StringUtilities.isSet(destRescName)) {
			String commandAsArray[] = new String[]{
					"irsync","-KVR",destRescName, "i:"  + dao,"i:"+ destDao
			};	
			return executeIcommand(commandAsArray);	
		} else {
			String commandAsArray[] = new String[]{
					"irsync","-KV","i:"  + dao,"i:"+ destDao
			};	
			return executeIcommand(commandAsArray);
		}
	}
	
	/**
	 * irsyncs whole subtree to destination on given resource
	 * @author Jens Peters
	 * @param dao
	 * @param destRescName
	 * @return
	 */
	public String rsyncDir(String sourceColl, String destColl, String destRescName) {
		if (StringUtilities.isSet(destRescName)) {
			String commandAsArray[] = new String[]{
					"irsync","-rKVR",destRescName, "i:"  + sourceColl,"i:"+ destColl
			};	
			return executeIcommand(commandAsArray);	
		} else {
			String commandAsArray[] = new String[]{
					"irsync","-rKV","i:"  + sourceColl,"i:"+ destColl
			};	
			return executeIcommand(commandAsArray);
		}
	}
	
	/**
	 * Sets AVU Triptlet to ICAT
	 * @author Jens Peters
	 * @param dao
	 * @param name
	 * @param value
	 */
	public void setIMeta(String dao, String name, String value) {
		String commandAsArray[] = new String[]{
				"imeta","set","-d", dao, name, value
		};	
		executeIcommand(commandAsArray);
	}
	
	/**
	 * Deletes AVU triplet from ICAT
	 * @author Jens Peters
	 * @param dao
	 * @param name
	 * @param value
	 */
	public void removeIMeta(String dao, String name, String value) {
		String commandAsArray[] = new String[]{
				"imeta","rm","-d", dao, name, value
		};	
		executeIcommand(commandAsArray);
	}
	
	
	/**
	 * Iquests ICAT for the given AVU
	 * @author Jens Peters
	 * @param dao
	 * @param avufield
	 * @return
	 */
	public String iquestDataObjectForAVU(String dao, String avufield) {
		String coll_name = FilenameUtils.getFullPath(dao);
		String data_name =  FilenameUtils.getName(dao);
		coll_name = coll_name.substring(0,coll_name.length()-1);

		String commandAsArray[] = new String[]{
				"iquest", "\"SELECT DATA_NAME, COLL_NAME, META_DATA_ATTR_NAME, META_DATA_ATTR_VALUE where COLL_NAME = \'"+ coll_name+"\' and DATA_NAME = \'"+ data_name+"\' and META_DATA_ATTR_NAME = \'"  + avufield + "\'" 
		}; 
		String iquest = executeIcommand(commandAsArray);
		try {
			return parseResultForAVUField(iquest, "META_DATA_ATTR_VALUE");
		} catch (IOException e) {
			logger.error("Error in parsing Resultlist searching for " + avufield,e);
		}
		return "";
	}
	
	/**
	 * Iquests ICAT for a given field on the DataObject
	 * @author Jens Peters
	 * @param dao
	 * @param field
	 * @return
	 */

	public String iquestDataObjectField(String dao, String field) {
		String coll_name = FilenameUtils.getFullPath(dao);
		String data_name =  FilenameUtils.getName(dao);
		coll_name = coll_name.substring(0,coll_name.length()-1);
		String commandAsArray[]=null;
		if(!data_name.isEmpty()){//specific file
			commandAsArray = new String[]{
					"iquest", "\"SELECT "  + field.toUpperCase() +"  where COLL_NAME = \'"+ coll_name+"\' and DATA_NAME = \'"+ data_name+"\'" 
			}; 
		}else{//directory
			commandAsArray = new String[]{
					"iquest", "\"SELECT "  + field.toUpperCase() +"  where COLL_NAME = \'"+ coll_name+"\'" 
			}; 
		}
		logger.info("iquest: "+Arrays.toString(commandAsArray));
		String iquest = executeIcommand(commandAsArray);
		logger.info("iquest result: "+iquest);
		try {
			return parseResultForAVUField(iquest, field);
		} catch (IOException e) {
			logger.error("Error in parsing Resultlist searching for " + field,e);
		}
		return "";
	}

	/**
	 * Parses parses result set for a given AVU 
	 * @author Jens Peters
	 * @param result
	 * @param field
	 * @return
	 * @throws IOException
	 */
	private String parseResultForAVUField(String result, String field) throws IOException {
		String[] props = result.split("------------------------------------------------------------");
		final String SUM="sum(";
		for (int i=0;i<props.length; i++) {
			Properties properties = new Properties();
			StringReader sr = new StringReader(props[i]);
			properties.load(sr);
			sr.close();
			if (properties.get(field)!=null) {
				logger.info("iquest avu: "+properties.get(field).toString());
				return properties.get(field).toString();
			}else if(field.toLowerCase().contains(SUM)){//capture the case field="SUM(FIELDNAME)"
				String fieldNoSum=field.trim();
				if(fieldNoSum.toLowerCase().indexOf(SUM)==0 && fieldNoSum.endsWith(")")){
					fieldNoSum=fieldNoSum.substring(SUM.length(), fieldNoSum.length()-1);
					if (properties.get(fieldNoSum)!=null) {
						logger.info("iquest sum avu: "+properties.get(fieldNoSum).toString());
						return properties.get(fieldNoSum).toString();
					}
				}
			}
		}
		return "";
	}
	
	/**
	 * executes given rule against the iRODS Server  
	 * @author Jens Peters
	 * @param rule
	 * @return
	 */
	public String executeIrule(File rule) {
		String commandAsArray[] = new String[]{
				"irule","-F",rule.getAbsoluteFile().toString()
		};	
		return executeIcommand(commandAsArray);
	}
	
	/**
	 * repl to resource name 
	 * @author Jens Peters
	 * @param rule
	 * @return
	 */
	public String repl(String dao, String resourceName) {
		String commandAsArray[] = new String[]{
				"irepl","-aBR",resourceName,dao
		};	
		return executeIcommand(commandAsArray);
	}	
	
	/**
	 * itrim  resource name 
	 * @author Jens Peters
	 * @return
	 */
	public String itrim(String dao, String resourceName, int numberToKeep, int replNumber) {
		ArrayList<String>  commandAsList = new ArrayList<String>();
		commandAsList.add("itrim");
		if (!resourceName.equals("")) {
			commandAsList.add("-S");
			commandAsList.add(resourceName);
		}
		commandAsList.add("-N");
		commandAsList.add(String.valueOf(numberToKeep));
		commandAsList.add("-n");
		commandAsList.add(String.valueOf(replNumber));
		commandAsList.add(dao);
		String[] commandAsArray = new String[commandAsList.size()];
		commandAsArray = commandAsList.toArray(commandAsArray);
		return executeIcommand(commandAsArray);
	}	
	/**
	 * ireg on rescName 
	 * @author Jens Peters
	 * @param rule
	 * @return
	 */
	public String ireg(File source, String targetRescName, String dao, boolean isDirectory) {
		ArrayList<String>  commandAsList = new ArrayList<String>();
		commandAsList.add("ireg");
		commandAsList.add("-f");
		commandAsList.add("-K");
		if (!targetRescName.equals("")) {
			commandAsList.add("-R");
			commandAsList.add(targetRescName);
		}
		if (isDirectory) {
			commandAsList.add("-C");
			
		}
		commandAsList.add(source.getAbsoluteFile().toString());
		commandAsList.add(dao);
		String[] commandAsArray = new String[commandAsList.size()];
		commandAsArray = commandAsList.toArray(commandAsArray);
		return executeIcommand(commandAsArray);
	}
}
