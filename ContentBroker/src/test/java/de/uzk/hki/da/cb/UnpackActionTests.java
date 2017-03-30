/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln
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

package de.uzk.hki.da.cb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.core.IngestGate;
import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.test.TC;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.Path;


/**
 * The Class UnpackActionBagitAndDeltaTests.
 * @author Daniel M. de Oliveira
 */
public class UnpackActionTests extends ConcreteActionUnitTest {

	@ActionUnderTest
	UnpackAction action = new UnpackAction();
	
	private static final String NUM_EXPECTED_ERRORS = "2";
	private static final String SIDECAR_EXTENSIONS = "xmp";
	private static final String SIDECAR_EXTENSIONS_COMMA_SPLIT = "xmp,xml";
	private static final String SIDECAR_EXTENSIONS_SEMIKOLON_SPLIT = "xmp;xml";
	private static final String CONF = "conf";
	
	private Path workAreaRootPath = Path.make(TC.TEST_ROOT_CB,"UnpackAction");
	private Path ingestAreaRootPath = Path.make(TC.TEST_ROOT_CB,"UnpackAction","ingest");
	private Path csnPath = Path.make(workAreaRootPath,"/work/TEST/");
	
	private static final String BAGIT_PACKAGE = "bagitPackage.tgz";
	private static final String SIDECAR_FILES_PACKAGE = "sidecarFiles.tgz";
	
	
	


	private IngestGate gate = new IngestGate();

	private static final PreservationSystem pSystem = new PreservationSystem();
	
	
	/**
	 * Sets the up.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Before
	public void setUp() throws IOException{
		n.setIngestAreaRootPath(ingestAreaRootPath);
		n.setWorkAreaRootPath(workAreaRootPath);
		
		new File(CONF).mkdir();
		FileUtils.copyDirectory(Path.makeFile(workAreaRootPath,"ingest_"), Path.makeFile(workAreaRootPath,"ingest"));
		FileUtils.copyFileToDirectory(C.PREMIS_XSD_TEST, new File(CONF));
		FileUtils.copyFileToDirectory(C.XLINK_XSD_TEST, new File(CONF));
		FileUtils.copyFileToDirectory(C.CONTRACT_XSD_TEST, new File(CONF));
		
		gate.setWorkAreaRootPath(workAreaRootPath.toString());
		gate.setFreeDiskSpacePercent(5);
		gate.setFileSizeFactor(3);
		action.setIngestGate(gate);
		
		ps.setSidecarExtensions(SIDECAR_EXTENSIONS);
	}

	/**
	 * Tear down.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@After
	public void tearDown() throws IOException{
		FolderUtils.deleteQuietlySafe(new File(CONF));
		FolderUtils.deleteDirectorySafe(Path.makeFile(workAreaRootPath,"ingest"));
		FolderUtils.deleteDirectorySafe(Path.makeFile(csnPath,o.getIdentifier()));
	}

	/**
	 * Test unpack std package.
	 * @throws IOException 
	 */
	@Test
	public void testUnpackStdPackage() throws IOException{
		
		o.getPackages().get(0).setContainerName(BAGIT_PACKAGE);
		
		action.implementation();

		assertTrue(new File(wa.objectPath()+"/data").exists());
		assertTrue(new File(wa.objectPath()+"/bagit.txt").exists());
		assertTrue(new File(wa.objectPath()+"/manifest-md5.txt").exists());
		assertTrue(new File(wa.objectPath()+"/bag-info.txt").exists());
		assertTrue(new File(wa.objectPath()+"/tagmanifest-md5.txt").exists());
		assertTrue(new File(wa.objectPath()+"/data/140849.tif").exists());
		assertTrue(new File(wa.objectPath()+"/data/premis.xml").exists());
	}
	
	@Test
	public void testRejectPackageWithDuplicateDocumentNames() throws IOException{
		final String DUPLICATE_DOCUMENTS_PACKAGE = "duplicateDocuments.tgz";
		o.getPackages().get(0).setContainerName(DUPLICATE_DOCUMENTS_PACKAGE);
		
		try{
			action.implementation();
			fail();
		}
		catch(UserException e){
			System.out.println(e.getMessage());
			if (!e.getMessage().endsWith(NUM_EXPECTED_ERRORS)) fail();
		}
	}
	
	@Test
	public void testWhenDuplicatesAreNotDuplicates() throws IOException{
		final String WHEN_DUPLICATES_PACKAGE = "whenDuplicatesAreNotDuplicates.tgz";
		o.getPackages().get(0).setContainerName(WHEN_DUPLICATES_PACKAGE);
		
		try{
			action.implementation();
		}
		catch(UserException e){
			System.out.println(e.getMessage());
			fail();
		}
	}
	
	@Test
	public void acceptSidecarFiles() throws IOException{
		
		o.getPackages().get(0).setContainerName(SIDECAR_FILES_PACKAGE);
	
		pSystem.setSidecarExtensions(SIDECAR_EXTENSIONS);
		try{
			action.implementation();
		}catch(UserException e){
			fail(e.getMessage());
		}
	}

	
	
	/**
	 * The special condition here was that contrary to the other testpackage this package only contains one pair of file to sidecar file. 
	 * @throws IOException
	 */
	@Test
	public void acceptSidecarFilesWithAnotherPackage() throws IOException{
		

		final String SIDECAR_FILES_PACKAGE_WHICH_BROKE = "LVR_ILR_4_PDF_TF18.tar";
		o.getPackages().get(0).setContainerName(SIDECAR_FILES_PACKAGE_WHICH_BROKE);
	
		pSystem.setSidecarExtensions(SIDECAR_EXTENSIONS);
		try{
			action.implementation();
		}catch(UserException e){
			fail(e.getMessage());
		}
	}
	
	
	@Test
	public void acceptSidecarFile_withUppercaseExtension() throws IOException{
		final String SIDECAR_UPPERCASE_PACKAGE = "SidecarFileWithUppercaseExtension.tgz";
		
		o.getPackages().get(0).setContainerName(SIDECAR_UPPERCASE_PACKAGE);
		
		pSystem.setSidecarExtensions(SIDECAR_EXTENSIONS);
		try{
			action.implementation();
		}catch(UserException e){
			fail(e.getMessage());
		}
	}
	
	
	@Test
	public void acceptSidecar_SideCarExtensionsSplitByComma() throws IOException{
		
		
		o.getPackages().get(0).setContainerName(SIDECAR_FILES_PACKAGE);
	
		pSystem.setSidecarExtensions(SIDECAR_EXTENSIONS_COMMA_SPLIT);
		try{
			action.implementation();
		}catch(UserException e){
			fail(e.getMessage());
		}
	}
	
	
	@Test
	public void acceptSidecar_SideCarExtensionsSplitBySemikolon() throws IOException{
		
		
		o.getPackages().get(0).setContainerName(SIDECAR_FILES_PACKAGE);
	
		pSystem.setSidecarExtensions(SIDECAR_EXTENSIONS_SEMIKOLON_SPLIT);
		try{
			action.implementation();
		}catch(UserException e){
			fail(e.getMessage());
		}
	}
	
	
	
	
	/**
	 * Test delete source package.
	 * @throws IOException 
	 */
	@Test
	public void testDeleteSourcePackage() throws IOException{
		o.getPackages().get(0).setContainerName(BAGIT_PACKAGE);
		
		action.implementation();
		assertFalse(Path.makeFile(csnPath,BAGIT_PACKAGE).exists());
	} 

	
	

	@Test
	public void throwExceptionWhenPackageDoesntExist() throws IOException{
		o.getPackages().get(0).setContainerName("notExistent.tgz");
		try{		
			action.implementation();
			fail();
		}
		catch(Exception e){
			action.rollback();
			System.out.println("Exception caught as expected: "+e);	
		}		
	}
	
	@Test
	public void rollback() throws IOException {
		o.getPackages().get(0).setContainerName(BAGIT_PACKAGE);
		action.rollback();
		action.implementation();
	}
	
	@Test
	public void testIsNotABagitPackage() throws IOException {
		o.getPackages().get(0).setContainerName("abc.zip");
		try {
			action.implementation();
			fail();
		} catch (UserException e) {
			assertTrue(e.getMessage().contains("BagIt"));
		}
	}
}
