/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2015 LVR InfoKom

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

/**
 * @author jens Peters
 */
package de.uzk.hki.da.at;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.SystemEvent;
import de.uzk.hki.da.model.User;
import de.uzk.hki.da.service.CSVFileHandler;
import de.uzk.hki.da.service.HibernateUtil;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.FolderUtils;


/**
 * 
 * @author Jens Peters
 * Acceptance Tests for CSV Queries send per file
 *
 */
public class ATCSVQueries extends AcceptanceTest {
	private static final int DEFAULT_STATUS = 100;
	static String ORIGINAL_NAME_ARCHIVED = "ATCSVReportObjectArchived";
	static String ORIGINAL_NAME_ERROR = "ATCSVReportJobInError";
	static String ORIGINAL_NAME_RETRIEVAL = "ATCSVRetrieval";
	
	@BeforeClass
	public static void setUp() throws IOException {
		ath.putAIPToLongTermStorage(ORIGINAL_NAME_ARCHIVED, ORIGINAL_NAME_ARCHIVED, new Date(), 100);
		ath.putSIPtoIngestArea("ATCSVReportJobInError", "tgz", "ATCSVReportJobInError");
		ath.putAIPToLongTermStorage(ORIGINAL_NAME_RETRIEVAL, ORIGINAL_NAME_RETRIEVAL, new Date(), 100);
		
	}

	@Test
	public void testCSVReportJobInError ()  throws IOException, InterruptedException {
		ath.waitForJobToBeInErrorStatus(ORIGINAL_NAME_ERROR, C.WORKFLOW_STATUS_DIGIT_USER_ERROR);
		Object object=ath.getObject(ORIGINAL_NAME_ERROR);
		createDefaultCSVFile(ORIGINAL_NAME_ERROR);
		File csv = new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/incoming/"+ORIGINAL_NAME_ERROR+".csv");
		
		assertTrue(csv.exists());
		long lm = csv.lastModified();
		createSystemEvent("CreateStatusReportEvent");
	
		assertTrue(waitUntilFileIsUpdated(csv,lm));
		assertTrue(readCSVFileStatusReporting(ORIGINAL_NAME_ERROR, "identifier",object.getIdentifier()));
		assertTrue(readCSVFileStatusReporting(ORIGINAL_NAME_ERROR, "erfolg","false"));
	}
	
	@Test
	public void testCSVStatusReport () throws IOException, InterruptedException {
		createDefaultCSVFile(ORIGINAL_NAME_ARCHIVED);
		File csv = new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/incoming/"+ORIGINAL_NAME_ARCHIVED+".csv");
		
		assertTrue(csv.exists());
		long lm = csv.lastModified();
		createSystemEvent("CreateStatusReportEvent");
	
		assertTrue(waitUntilFileIsUpdated(csv,lm));
		assertTrue(readCSVFileStatusReporting(ORIGINAL_NAME_ARCHIVED, "identifier",ORIGINAL_NAME_ARCHIVED));
		assertTrue(readCSVFileStatusReporting(ORIGINAL_NAME_ARCHIVED, "erfolg","true"));
	}

	/**
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAutomaticCSVStatusReport () throws IOException, InterruptedException {
		//Create csv File with valid default statuscode
		createDefaultCSVFileWithStatus(ORIGINAL_NAME_ARCHIVED);
		File csv = new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/incoming/"+ORIGINAL_NAME_ARCHIVED+".csv");
		
		assertTrue(csv.exists());
		assertTrue(readCSVFileStatusReporting(csv,ORIGINAL_NAME_ARCHIVED, "statuscode",""+DEFAULT_STATUS));
		
		long lm = csv.lastModified();
		assertTrue(!systemEventExist("AutomaticStatusReportEvent"));
		createSystemEvent("AutomaticStatusReportEvent");
		
		assertTrue(waitUntilFileIsUpdated(csv,lm));
		assertTrue(readCSVFileStatusReporting(ORIGINAL_NAME_ARCHIVED, "identifier",ORIGINAL_NAME_ARCHIVED));
		assertTrue(readCSVFileStatusReporting(ORIGINAL_NAME_ARCHIVED, "erfolg","true"));
		assertTrue(readCSVFileStatusReporting(ORIGINAL_NAME_ARCHIVED, "statuscode",null));
		assertTrue(systemEventExist("AutomaticStatusReportEvent")); //check for existence of event after execution
		deleteSystemEvent("AutomaticStatusReportEvent"); //delete to prevent influence on another tests
		assertTrue(!systemEventExist("AutomaticStatusReportEvent"));
	}
	
	@Test
	public void testCSVRetrievalRequests () throws IOException, InterruptedException {
		createDefaultCSVFile(ORIGINAL_NAME_RETRIEVAL);
		File csv = new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/incoming/"+ORIGINAL_NAME_RETRIEVAL+".csv");
		assertTrue(csv.exists());
		createSystemEvent("CreateRetrievalRequestsEvent");
		ath.waitForJobToBeInStatus(ORIGINAL_NAME_RETRIEVAL, "952");
	}
	
	
	@AfterClass
	public static void tearDown(){
		distributedConversionAdapter.remove("aip/"+testContractor.getUsername()+"/"+ORIGINAL_NAME_ARCHIVED); 
		distributedConversionAdapter.remove("aip/"+testContractor.getUsername()+"/"+ORIGINAL_NAME_RETRIEVAL); 
		
		FolderUtils.deleteQuietlySafe(new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/incoming/"+ORIGINAL_NAME_ARCHIVED+".csv"));
		FolderUtils.deleteQuietlySafe(new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/incoming/"+ORIGINAL_NAME_ERROR+".csv"));
		FolderUtils.deleteQuietlySafe(new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/incoming/"+ORIGINAL_NAME_RETRIEVAL+".csv"));
		
		FolderUtils.deleteQuietlySafe(new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/outgoing/"+ORIGINAL_NAME_ARCHIVED+".csv"));
		FolderUtils.deleteQuietlySafe(new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/outgoing/"+ORIGINAL_NAME_ERROR+".csv"));
		FolderUtils.deleteQuietlySafe(new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/outgoing/"+ORIGINAL_NAME_RETRIEVAL+".csv"));
		
	}

	
	//---------------------------------------------------------------------------------
	
	private boolean waitUntilFileIsUpdated( File file, long timeStamp ) throws InterruptedException {
	
		long timeStampOld;
		timeStampOld = timeStamp;
		int i = 0;
		while (timeStampOld==timeStamp) {
			Thread.sleep(1000l);
			timeStamp = file.lastModified();
			i++;
			if (i>120) {
				System.out.println(file + " was NOT changed!");
				return false;
			}
		}
		System.out.println(file + " was changed!");
		return true;
	}
	
	private void createSystemEvent(String eventName) {
		
		try {
		SystemEvent se = new SystemEvent();
		se.setNode(localNode);
		se.setType(eventName);
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		se.setOwner(testContractor);
		session.save(se);
		session.getTransaction().commit();
		session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean systemEventExist(String eventName) {
		boolean ret=false;
		try {
			Session session = HibernateUtil.openSession();
			Transaction transaction = session.beginTransaction();
			Query query = session.createQuery("SELECT se FROM SystemEvent se where type = '"+eventName+"'");
			@SuppressWarnings("unchecked")
			List<SystemEvent> seList = query.list();
			ret= (seList!=null && seList.size()==1);
			
			transaction.commit();
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("systemEventExist:"+ret);
		return ret;
	}
	
	private void deleteSystemEvent(String eventName) {
		try {
			Session session = HibernateUtil.openSession();
			Transaction transaction = session.beginTransaction();
			Query query = session.createQuery("delete FROM SystemEvent where type = '" + eventName + "'");
			query.executeUpdate();
			transaction.commit();
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private boolean readCSVFileStatusReporting(String origName, String field, String mustcontain) throws IOException {
		File targetFile=new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/outgoing/"+origName+".csv");
		return readCSVFileStatusReporting(targetFile,origName,field,mustcontain);
	}
	
	@SuppressWarnings("unchecked")
	private boolean readCSVFileStatusReporting(File targetFile,String origName, String field, String mustcontain) throws IOException {
		CSVFileHandler csf = new CSVFileHandler();
		System.out.println("search CSV Report field " + field + " value " + mustcontain);
		
		for(int i=0;i<5 && !targetFile.exists();i++){
			FolderUtils.waitToCompleteNFSAwareFileOperation();
			System.out.println("Target("+targetFile+") file doesnt exist yet, wait: "+i);
		}
		
		csf.parseFile(targetFile);
		for (Map<String, java.lang.Object> csvEntry :csf.getCsvEntries()) {
			if (csvEntry.get("origName").equals(origName)){
				if (mustcontain !=null && mustcontain.equals(csvEntry.get(field))) return true;
				else if(mustcontain ==null && csvEntry.get(field)==null) return true;
			}
 		} 
		System.out.println("nothing found in csv reports!");
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int createDefaultCSVFileWithStatus(String origName) throws IOException {
		ArrayList<Map> csvEntries = new ArrayList();
		Map<String, java.lang.Object> csvEntry = new HashMap<String, java.lang.Object>();
		csvEntry.put("origName", (java.lang.Object) origName);
		csvEntry.put("statuscode", (java.lang.Object) DEFAULT_STATUS);
		csvEntries.add(csvEntry);
		return createCSVFile(origName,csvEntries);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int createDefaultCSVFile(String origName) throws IOException {
		ArrayList<Map> csvEntries = new ArrayList();
		Map<String, java.lang.Object> csvEntry = new HashMap<String, java.lang.Object>();
		csvEntry.put("origName", (java.lang.Object) origName);
		csvEntries.add(csvEntry);
		return createCSVFile(origName,csvEntries);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int createCSVFile(String origName,ArrayList<Map> csvEntries) throws IOException {
		CSVFileHandler csf = new CSVFileHandler();
		csf.setEncoding("CP1252");
		csf.setCsvEntries(csvEntries);
		csf.persistStates(new File(localNode.getUserAreaRootPath()+"/"+testContractor.getUsername()+"/incoming/"+origName+".csv"));
		return csvEntries.size();
	}
		
}
