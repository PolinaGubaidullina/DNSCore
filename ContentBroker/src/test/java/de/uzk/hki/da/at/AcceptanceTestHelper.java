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

package de.uzk.hki.da.at;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;

import de.uzk.hki.da.grid.GridFacade;
import de.uzk.hki.da.model.Copy;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.ObjectNamedQueryDAO;
import de.uzk.hki.da.model.Package;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.model.StoragePolicy;
import de.uzk.hki.da.model.User;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.pkg.NativeJavaTarArchiveBuilder;
import de.uzk.hki.da.repository.MetadataIndex;
import de.uzk.hki.da.service.HibernateUtil;
import de.uzk.hki.da.test.TC;
import de.uzk.hki.da.util.FileIdGenerator;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.CommandLineConnector;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.GenericChecksum;
import de.uzk.hki.da.utils.MD5Checksum;
import de.uzk.hki.da.utils.Path;
import de.uzk.hki.da.utils.ProcessInformation;
import de.uzk.hki.da.utils.RelativePath;
import de.uzk.hki.da.utils.StringUtilities;

/**
 * @author Daniel M. de Oliveira
 */
public class AcceptanceTestHelper {
	public static final String TEST_RESOURCES_PATH_PROPERTY="WorkaroundToPathTestCaseFilesPathToJUNITTests"; //Name for a system property to pass the testfiles directory
	public static final String NO_DIRTY_CLEANUP_AFTER_EACH_TEST_PROPERTY="WorkaroundNoCleanupCompleteDB"; //Name for a system property to avoid cleanups, because they cleanup all data, not only testdata

	private static final String MSG_READY = "ready";
	private static final String MSG_ERROR_WHEN_TIMEOUT_REACHED = "waited to long. test considered failed";
	private static final String TEMP_FOLDER = "/tmp/";
	static final String URN_NBN_DE_DANRW = "urn:nbn:de:danrw-";
	protected static Path TEST_DATA_ROOT_PATH = new RelativePath("src/test/resources/at/");
	
	
	private static final int INTERVAL=2000; // in ms
	private int TIMEOUT=1200000; // ins ms
	
	private GridFacade gridFacade;
	private Node localNode;
	private PreservationSystem preservationSystem;
	private User testContractor;
	private StoragePolicy sp;
	private String logPath = null;
	private String fedoraUrlTemplate=null;
			//"http://localhost:8080/fedora/objects/collection-open:IDENTIFIER/datastreams/FILENAME/content";
			//"https://danrw-q-repo.hbz-nrw.de/file/IDENTIFIER/FILENAME";
	
	
	public AcceptanceTestHelper(
			GridFacade gridFacade,
			Node localNode,
			User testContractor,
			StoragePolicy sp,
			PreservationSystem ps){
		this.gridFacade=gridFacade;
		this.localNode=localNode;
		this.testContractor=testContractor;
		this.sp = sp;
		if(System.getProperty(TEST_RESOURCES_PATH_PROPERTY)!=null)
			TEST_DATA_ROOT_PATH = Path.make(System.getProperty(TEST_RESOURCES_PATH_PROPERTY));
		
		String localNodeWorkArea = localNode.getWorkAreaRootPath().toString();
		String localNodeTMP = localNodeWorkArea.replace("/storage/WorkArea", "");
		logPath=(new File(localNodeTMP+"/ContentBroker/log")).getAbsolutePath();//default logpath
		preservationSystem=ps;
	}
			
			
	
	/**
	 * Retrieves a package and unpacks it to a target folder.
	 * <br>
	 * <strong>!</strong> Make sure to delete targetFolder at tearDown in acceptance tests.
	 * 
	 * @param originalName of the object.
	 * @param targetFolder to extract the DIP to.
	 * @param packageName number of the package of the object.
	 * @return a new instance that represents the object. fetched from the from the database. 
	 * @throws IOException if cannot fetch file from grid.
	 * 
	 */
	void retrieveAIP(Object o,File targetFolder,String packageName) throws IOException{

		final String packSuffix = ".pack_";
		
		Object object=getObject(o.getOrig_name());
		if (object==null) throw new RuntimeException("cannot find object");
		System.out.println("object: "+object.getIdentifier());
		
		Path.makeFile(TEMP_FOLDER,object.getIdentifier()+packSuffix+packageName+C.FILE_EXTENSION_TAR).delete(); // if exists
		gridFacade.get(Path.makeFile(TEMP_FOLDER,object.getIdentifier()+packSuffix+packageName+C.FILE_EXTENSION_TAR), 
			testContractor.getShort_name()+
			    "/"+object.getIdentifier()+"/"+object.getIdentifier()+packSuffix+packageName+C.FILE_EXTENSION_TAR);
		try {
			new NativeJavaTarArchiveBuilder().unarchiveFolder(Path.makeFile(TEMP_FOLDER,object.getIdentifier()+packSuffix+packageName+C.FILE_EXTENSION_TAR), 
					Path.makeFile(TEMP_FOLDER));
		} catch (Exception e) {
			fail("could not find source file or unarchive source file to tmp");
		}
		
		if (targetFolder.exists()) FolderUtils.deleteDirectorySafe(targetFolder);
		FileUtils.moveDirectory(Path.makeFile(TEMP_FOLDER,object.getIdentifier()+packSuffix+packageName),targetFolder);
		Path.makeFile(TEMP_FOLDER,object.getIdentifier()+packSuffix+packageName+C.FILE_EXTENSION_TAR).delete();
	}
	
	/**
	 * 
	 * @param originalName
	 * @return null if no object found.
	 */
	Object getObject(String originalName){
		Object object = null;
		try {
			object = new ObjectNamedQueryDAO().getUniqueObject(originalName, testContractor.getShort_name());
		} catch (Exception e) {
			System.out.println("Catched exception " + e.getMessage() +". Try again.");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
			object = new ObjectNamedQueryDAO().getUniqueObject(originalName, testContractor.getShort_name());
			} catch (Exception e2) {
			fail("Exception loading Object called " + originalName + " " + e2.getStackTrace()); 
			}
		}
		return object;
	}

	
	
	/**
	 * Checks in regular intervals if the object with the originalName
	 * is in a desired object state. 
	 * 
	 * @param originalName
	 * @param awaitedObjectState
	 * @return when the object is in the desired state
	 * @throws RuntimeException if a job associated with the object is found in an job error state.
	 * @throws RuntimeException after a globally defined amount of time. 
	 * This is to prevent an infinite loop in case the desired state gets not reached.
	 */
	void awaitObjectState(String originalName,int awaitedObjectState){
		int waited_ms_total=0;
		while (true){

			waited_ms_total=updateTimeout(waited_ms_total,TIMEOUT,INTERVAL);
			Job job = null;
			Object o = null;
			try {
				o = getObject(originalName);
			if (o==null) {
				System.out.println("Object not found (yet). ");
				Thread.sleep(6000);
				
				continue;
			}
			System.out.print("Awaiting object state "+awaitedObjectState+". Identifier: "+o.getIdentifier()+
					". Orig name: "+o.getOrig_name()+". Object state: "+o.getObject_state()+". ");
				job = getJob(originalName);
			} catch (Exception e) {
				System.out.println("Catched hibernate exception. Try again.");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				job = getJob(originalName);
				o = getObject(originalName);
			}
			if (job==null) {
				// to avoid false positive for slow starters
				try {
					Thread.sleep(1000);
					job = getJob(originalName);
				} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			}
//			
			if (job!=null) {
				if (isInErrorState(job)) {
					try {
						// just sleep for a while if state persists
						Thread.sleep(3000);
						job = getJob(originalName);
					} catch (InterruptedException e1) {}
					if (isInErrorState(job))
					evaluateErrorDetails(job);
				} else
					System.out.println(" Job state: "+job.getStatus()+".");
			} else {
					System.out.println(" Job is null");
			}
			if (o.getObject_state()==awaitedObjectState && waited_ms_total>0) {
				return;
			}
			
		}
	}

	
	void waitForDefinedPublishedState(String origName) {
		waitForObjectPublishedState(origName, -1);
	}

	void waitForObjectPublishedState(String origName,int publishedFlag) {
		int waited_ms_total=0;
		while (true) {
			waited_ms_total=updateTimeout(waited_ms_total,TIMEOUT,INTERVAL);
			
			Object o=getObject(origName);
			if (o==null) {
				System.out.println("Object not found (yet). ");
				continue;
			}
			System.out.println("Awaiting object to be published. Identifier: "+o.getIdentifier()+
					". Orig name: "+o.getOrig_name()+". Published flag: "+o.getPublished_flag()+". ");
			
			if (publishedFlag==0 && o.getPublished_flag()==0) break;
			else
				if ((o.getPublished_flag() & publishedFlag) >0) break; // Binary and operator!!!
		}
	}



	void waitForJobToBeInErrorStatus(String originalName,String errorStatusLastDigit) throws InterruptedException{
		
		waitForJobToBeInErrorStatus(originalName,errorStatusLastDigit,TIMEOUT);
	}
	
	
	/**
	 * Checking the database in regular intervals for a job in an error state ending with errorStatusLastDigit.
	 * 
	 * @param originalName
	 * @param errorStatusLastDigit
	 * @param timeout wait timeout ms until you consider the test failed.
	 * @return job if found job in error state
	 * @throws RuntimeException to signal the test considered failed. For example if it takes longer than timeout to reach the status.
	 */
	void waitForJobToBeInErrorStatus(String originalName,String errorStatusLastDigit,int timeout) throws InterruptedException{
		
		int waited_ms_total=0;
		while (true){
			waited_ms_total=updateTimeout(waited_ms_total,timeout,INTERVAL);
			
			Job job = getJob(originalName);

			if (job==null) {
				System.out.println("no job found in db");
				continue;
			}
	
			System.out.println("Awaiting job to be in error state ending with "+errorStatusLastDigit+". Identifier: "+job.getObject().getIdentifier()+
					". Orig name: "+job.getObject().getOrig_name()+". Job state: "+job.getStatus()+".");
			
			if (job.getStatus().endsWith(errorStatusLastDigit)){
				System.out.println(MSG_READY);
				return;
			}
		}
	}
	
	
	
	/**
	 * Waits for a job to reach a certain status.
	 * 
	 * @param originalName
	 * @param status
	 * @param timeout
	 * @return
	 * @throws InterruptedException
	 */
	void waitForJobToBeInStatus(String originalName,String status) 
			throws InterruptedException{
		System.out.println("waiting for job of object with original name "+originalName+" to be in status "+status);
		
		int waited_ms_total=0;
		while (true){
			waited_ms_total=updateTimeout(waited_ms_total, TIMEOUT,INTERVAL);
	
			Job job = getJob(originalName);
			
			if (job!=null){
				
				System.out.println("Awaiting job to be in state "+status+". Identifier: "+job.getObject().getIdentifier()+
						". Orig name: "+job.getObject().getOrig_name()+". Job state: "+job.getStatus()+".");
				
				if (job.getStatus().equals(status)){
					System.out.println(MSG_READY);
					return;
				} else if (isInErrorState(job)) {
					String msg = "ERROR: Job in error state: " + job.getStatus();
					System.out.println(msg);
					
					throw new RuntimeException(msg);
				}

			} else{
				System.out.println("Awaiting job (OriginalName: "+originalName+") to be in state "+status+". Job is NULL ");		
			}
		}
	}	
	
	
	public Job getJob(String originalName) {
		Job job;
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		job = getJob(session, originalName, testContractor.getShort_name());
		session.close();
		return job;
	}


	void waitForObjectToBeIndexed(MetadataIndex mi,String indexName,String identifier) {
		int waited_ms_total=0;
		while (true) {
			waited_ms_total=updateTimeout(waited_ms_total,TIMEOUT,INTERVAL);
			System.out.println("Awaiting object to be indexed. Identifier: "+identifier+
					". Index: "+indexName+". MetadataIndex: "+mi+". ");
			if (mi.getIndexedMetadata(indexName, identifier).contains(identifier)) break;
		}
	}



	/**
	 * Gets the job.
	 *
	 * @param orig_name the orig_name
	 * @param csn the csn
	 * @return the job
	 */
	@SuppressWarnings("unchecked")
	private Job getJob(Session session, String orig_name, String csn) {
		List<Job> l = null;
	
		try {
			l = session.createQuery(
					"SELECT j FROM Job j left join j.obj as o left join o.user as c where o.orig_name=?1 and c.short_name=?2"
					)
							.setParameter("1", orig_name)
							.setParameter("2", csn)
							.setReadOnly(true).list();
			
			return l.get(0);
		} catch (IndexOutOfBoundsException e) {

			return null;
		}
	}
	
	
	private int updateTimeout(int waited_ms_total,int timeout, int interval){
		System.out.println("(total time: "+waited_ms_total+"ms / timeout: "+timeout+"ms) ");
		if (waited_ms_total>timeout) throw new RuntimeException(MSG_ERROR_WHEN_TIMEOUT_REACHED);
		try {
			Thread.sleep(interval);
		} catch (InterruptedException e) {} // no problem
		return waited_ms_total+=interval;
	}
	
	private void evaluateErrorDetails(Job job) {
		String oid=job.getObject().getIdentifier();
		String msg = "ERROR: Job in error state: " + job.getStatus() + " in Object-Id "+ oid;
		System.out.println(msg);
		
		if (job.getObject().getIdentifier()!=null){
			try {
				System.out.println("SHOWING OBJECT LOG:");
				System.out.println(FileUtils.readFileToString(new File(Path.make(logPath, "object-logs")+"/"+job.getObject().getIdentifier()+".log")));
				System.out.println("END OF OBJECT LOG: "+job.getObject().getIdentifier());
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		throw new RuntimeException(msg);
	}

	
	/**
	 * Makes a copy of a file from src/test/resources/at/[sourcePackagename].[ext]
	 * and puts it to the nodes IngestArea at ingestAreaRootPath/test/[originalName].[ext].
	 * 
	 * @param originalName
	 * @throws IOException 
	 */
	void putSIPtoIngestArea(String sourcePackageName,String ext,String originalName) throws IOException {
		
		System.out.println("putSIPtoIngestArea(" + sourcePackageName + ", "+ext+", "+originalName+")");
		if (localNode==null) throw new IllegalStateException();
		if (localNode.getIngestAreaRootPath()==null) throw new IllegalStateException();
		File source;
		File target;
		if (StringUtilities.isSet(ext)){
		source = Path.makeFile(TEST_DATA_ROOT_PATH,sourcePackageName+"."+ext);
		target = Path.makeFile(localNode.getIngestAreaRootPath(),testContractor.getShort_name(),originalName+"."+ext);
		FileUtils.copyFile( source, target );
		} else {
			source = Path.makeFile(TEST_DATA_ROOT_PATH,sourcePackageName);
			target = Path.makeFile(localNode.getIngestAreaRootPath(),testContractor.getShort_name(),originalName);
			if (target.exists()) FolderUtils.deleteDirectorySafe(target);
			FileUtils.copyDirectory(source, target, false);
			}	
	}
	
	
	Object putAIPToLongTermStorage(String identifier,String originalName, Date createddate, int object_state) throws IOException{
		return putAIPToLongTermStorage(identifier, originalName, createddate, object_state, null, null);
	}
	

	/**
	 * @throws IOException 
	 */
	Object putAIPToLongTermStorage(String identifier,String originalName, 
			Date createddate, int object_state, 
			String packageType,
			String metadataFile) throws IOException{
		
		String PACKAGE_NAME = "1";
		int timeout = 2000;
		
		if (createddate==null) createddate = new Date();
		String urn =   URN_NBN_DE_DANRW+identifier;
		gridFacade.put(Path.makeFile(TC.TEST_ROOT_AT,identifier+".pack_"+PACKAGE_NAME+C.FILE_EXTENSION_TAR), 
				new RelativePath(testContractor.getUsername(),identifier,identifier+".pack_"+PACKAGE_NAME+C.FILE_EXTENSION_TAR).toString(), sp, null);
		int i = 0;
		while (!gridFacade.storagePolicyAchieved(new RelativePath(testContractor.getUsername(),identifier,identifier+".pack_"+PACKAGE_NAME+C.FILE_EXTENSION_TAR).toString(), sp, null, null)) {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {} // no problem
			if (i>200) fail("Package was not replicated to archive resc");
		}
		
		Object object = new Object();
		
		object.setPackage_type(packageType);
		object.setMetadata_file(metadataFile);
		object.setContractor(testContractor);
		object.setInitial_node(localNode.getName());
		object.setIdentifier(identifier);
		object.setObject_state(object_state);
		object.setUrn(urn);
		object.setCreatedAt(createddate);
		object.setModifiedAt(createddate);
		object.setLast_checked(createddate);
		object.setOrig_name(originalName);
		Package pkg = new Package();
		pkg.setName(PACKAGE_NAME);
		pkg.setContainerName(originalName+"."+C.FILE_EXTENSION_TGZ);
		
		Copy copy = new Copy();
		String checksum=GenericChecksum.getChecksumForLocalFile(Path.makeFile(TC.TEST_ROOT_AT,identifier+".pack_"+PACKAGE_NAME+C.FILE_EXTENSION_TAR));
		
		pkg.setChecksum(checksum);
		copy.setChecksum(checksum);
		copy.setChecksumBase64(GenericChecksum.encodeBase64(checksum));
		copy.setChecksumType(GenericChecksum.DEFAULT_CHECKSUM_ALGO.toString());
		copy.setChecksumDate(new Date());
		for (Copy c:pkg.getCopies()) c.getId();
		pkg.getCopies().add(copy);
		object.getPackages().add(pkg);
		
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		session.save(object);
		session.getTransaction().commit();
		session.close();
		
		return object;
	}

	
	void createJob(String origName,String jobStatus) {
		createJob(origName, jobStatus, new Date());
	}
	
	void createJob(String origName,String jobStatus, Date createdAt) {
		Object o = getObject(origName);
		
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		
		Node node = (Node) session.load(Node.class, localNode .getId());
		
		Job j = new Job();
		j.setResponsibleNodeName(node.getName());
		j.setObject(o);
		j.setCreatedAt(createdAt);
		j.setModifiedAt(createdAt);
		j.setStatus(jobStatus);
	
		session.save(j);
		
		session.getTransaction().commit();
		session.close();
	}

	private boolean isInErrorState(Job job){
		if (job.getStatus().endsWith(C.WORKFLOW_STATUS_DIGIT_ERROR_BAD_ROLLBACK) || 
				job.getStatus().endsWith(C.WORKFLOW_STATUS_DIGIT_ERROR_PROPERLY_HANDLED)
				|| job.getStatus().endsWith(C.WORKFLOW_STATUS_DIGIT_USER_ERROR)) return true;
		return false;
	}

	public void setLogPath(String newLogPath) {
		logPath=newLogPath;
	}


	public void setTIMEOUT(int tIMEOUT) {
		TIMEOUT = tIMEOUT;
	}



	public void setFedoraUrlTemplate(String fedoraUrlTemplate) {
		this.fedoraUrlTemplate = fedoraUrlTemplate;
	}

	public File loadDefaultMetsFileFromPip(String identifier) throws IOException {
		return loadFileFromPip(identifier, C.CB_PACKAGETYPE_METS + C.FILE_EXTENSION_XML);
	}
	
	public File loadDefaultLidoFileFromPip(String identifier) throws IOException {
		return loadFileFromPip(identifier, C.CB_PACKAGETYPE_LIDO + C.FILE_EXTENSION_XML);
	}

	public File loadFileFromPip(String identifier, String fileName) throws IOException {
		Path contractorsPipsPublic = Path.make(localNode.getWorkAreaRootPath(), WorkArea.PIPS, WorkArea.PUBLIC,	testContractor.getUsername());

		Path targetDir = Path.make(contractorsPipsPublic, identifier);
		Path filePath = Path.make(targetDir, fileName);
		File tmpFile = filePath.toFile();

		if (localNode.getName().equals(preservationSystem.getPresServer())) {
			if (!tmpFile.exists()) {
				//throw new IOException("File: " + tmpFile + " doesn't exists");
				System.out.println("File: " + tmpFile + " doesn't exists");
			}
		} else {
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
			Files.createDirectories(Paths.get(targetDir.toFile().getAbsolutePath()));
			tmpFile.getParentFile().mkdirs(); //if filename contains directories
			if (fedoraUrlTemplate == null)
				throw new IOException("File: " + tmpFile + " is not downloadable, fedora Url is not seted");
			fetchFileFromFedora(identifier, fileName, tmpFile);
		}
		return tmpFile;
	}

	public void fetchFileFromFedora(String identifier, String srcFileName, File targetFile) throws IOException {
		String url = fedoraUrlTemplate.replace("IDENTIFIER", identifier).replace("FILENAME", FileIdGenerator.getFileId(srcFileName));
		// String curlUrlCommand="curl X GET \"" + url.replace("IDENTIFIER",
		// identifier).replace("FILENAME", srcFileName)+"\"";
		// ProcessInformation pi = new
		// CommandLineConnector().runCmdSynchronously(new String[]{"curl X GET
		// ", url},null,60000);
		String htmlString = null;
		try {
			htmlString = getFileAsHTMContent(url); // pi.getStdOut();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: " + url + " is not awaible!!! " + e);
		}
		if (htmlString != null) {
			String[] htmlStringSplit = htmlString.split("</html>");
			FileWriter fw = new FileWriter(targetFile);
			fw.write(htmlStringSplit[htmlStringSplit.length - 1]); // get the part of content after html-stuff
			fw.close();
		}
		// curl X GET
		// "http://localhost:8080/fedora/objects/collection-open:1-2016102534/datastreams/_1175-mets_1175.xml/content"
		// https://danrw-q-repo.hbz-nrw.de/file/8-2016101915371/_b26200b1649a6be51841b740ae745f3b.jpgn -> danrw:8-2016101915371/...
		// https://danrw-q-repo.hbz-nrw.de/file/8-2016101915371/METS.xml
	}

	private static String getFileAsHTMContent(String urlString) throws UnsupportedEncodingException, IOException {
		System.out.println("Fetch File from fedora: "+urlString);
		URL url = new URL(urlString);
		URLConnection urlc = url.openConnection();
		urlc.addRequestProperty("User-Agent", "Mozilla/5.0");
		StringBuilder ret = new StringBuilder();
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			for (String line; (line = reader.readLine()) != null;) {
				System.out.println(line);
				ret.append(line);
			}
		

		return ret.toString();
	}
}
