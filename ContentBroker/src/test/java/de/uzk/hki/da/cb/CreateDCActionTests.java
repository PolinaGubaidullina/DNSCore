/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2015 LVR-Infokom
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

import static de.uzk.hki.da.utils.C.CB_PACKAGETYPE_EAD;
import static de.uzk.hki.da.utils.C.ENCODING_UTF_8;
import static de.uzk.hki.da.utils.C.FILE_EXTENSION_XML;
import static de.uzk.hki.da.utils.C.METADATA_STREAM_ID_DC;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.uzk.hki.da.core.PreconditionsNotMetException;
import de.uzk.hki.da.core.SubsystemNotAvailableException;
import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.repository.RepositoryException;
import de.uzk.hki.da.test.TC;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.Path;

/**
 * @author Daniel M. de Oliveira
 */
public class CreateDCActionTests extends ConcreteActionUnitTest{


	@ActionUnderTest
	CreateDCAction action = new CreateDCAction();
	
	private static final String UNDERSCORE = "_";
	private static final Path WORK_AREA_ROOT_PATH = Path.make(TC.TEST_ROOT_CB,"CreateDCAction");
	
	
	@Before
	public void setUp() throws IOException {
		n.setWorkAreaRootPath(WORK_AREA_ROOT_PATH);
		FileUtils.copyDirectory(Path.makeFile(WORK_AREA_ROOT_PATH,UNDERSCORE+WorkArea.PIPS), Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.PIPS));
		
		o.setPackage_type(CB_PACKAGETYPE_EAD);
		Map<String, String> dcMappings = new HashMap<String,String>();
		dcMappings.put(CB_PACKAGETYPE_EAD, "src/main/xslt/dc/ead_to_dc.xsl");
		action.setDcMappings(dcMappings);
	}
	
	@After 
	public void tearDown() throws IOException {
		FolderUtils.deleteDirectorySafe(Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.PIPS));
	}
	
	
	@Test
	public void createPublicAndInstitutionDC() throws FileNotFoundException, UserException, IOException, 
			RepositoryException, JDOMException, ParserConfigurationException, SAXException, SubsystemNotAvailableException {
		
		action.implementation();
		
		assertTrue(makeMetadataFile(WorkArea.PUBLIC,C.METADATA_STREAM_ID_DC).exists());
		assertTrue(makeMetadataFile(WorkArea.WA_INSTITUTION,C.METADATA_STREAM_ID_DC).exists());
	}
	
	@Test
	public void createPublicDC() throws IOException, UserException, RepositoryException, JDOMException, ParserConfigurationException, SAXException, SubsystemNotAvailableException {
		FolderUtils.deleteDirectorySafe(Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.WA_INSTITUTION));
	
		action.implementation();
		
		assertTrue(makeMetadataFile(WorkArea.PUBLIC,C.METADATA_STREAM_ID_DC).exists());
		assertFalse(makeMetadataFile(WorkArea.WA_INSTITUTION,C.METADATA_STREAM_ID_DC).exists());
		
	}
	
	@Test
	public void createInsitutionDC() throws FileNotFoundException, UserException, IOException, RepositoryException, JDOMException, ParserConfigurationException, SAXException, SubsystemNotAvailableException {
		FolderUtils.deleteDirectorySafe(Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.PUBLIC));
		
		action.implementation();
		
		assertFalse(makeMetadataFile(WorkArea.PUBLIC,C.METADATA_STREAM_ID_DC).exists());
		assertTrue(makeMetadataFile(WorkArea.WA_INSTITUTION,C.METADATA_STREAM_ID_DC).exists());
	}
	
	@Test
	public void createNoDC() throws IOException, UserException, RepositoryException, JDOMException, ParserConfigurationException, SAXException, SubsystemNotAvailableException {
		FolderUtils.deleteDirectorySafe(Path.makeFile(WORK_AREA_ROOT_PATH, WorkArea.PIPS,WorkArea.PUBLIC));
		FolderUtils.deleteDirectorySafe(Path.makeFile(WORK_AREA_ROOT_PATH, WorkArea.PIPS,WorkArea.WA_INSTITUTION));

		action.implementation();

		assertFalse(makeMetadataFile(WorkArea.PUBLIC,C.METADATA_STREAM_ID_DC).exists());
		assertFalse(makeMetadataFile(WorkArea.WA_INSTITUTION,C.METADATA_STREAM_ID_DC).exists());
	}
	
	
	@Test
	public void noPackageType() throws FileNotFoundException, UserException, IOException, RepositoryException, JDOMException, ParserConfigurationException, SAXException, SubsystemNotAvailableException {
		o.setPackage_type(null);
		action.implementation();
		assertFalse(makeMetadataFile(WorkArea.PUBLIC,C.METADATA_STREAM_ID_DC).exists());
		assertFalse(makeMetadataFile(WorkArea.WA_INSTITUTION,C.METADATA_STREAM_ID_DC).exists());
	}
	
	@Test
	public void packageTypeSetAndNoXSLTSet() {
		Map<String, String> dcMappings = new HashMap<String,String>();
		action.setDcMappings(dcMappings);
		
		try {
			action.checkPreconditions();
			fail();
		}catch(PreconditionsNotMetException expected) {}
	}
	
	@Test
	public void packageTypeSetAndNoXSLTExistent() {
		Map<String, String> dcMappings = new HashMap<String,String>();
		dcMappings.put(CB_PACKAGETYPE_EAD, "notexisting.xsl");
		action.setDcMappings(dcMappings);
		
		try {
			action.checkPreconditions();
			fail();
		}catch(PreconditionsNotMetException expected) {}
	}
	

	@Test
	public void packageTypeSetButPublicMetadataFileNotExistent() {
		makeMetadataFile(WorkArea.PUBLIC, CB_PACKAGETYPE_EAD).delete();
		try {
			action.checkPreconditions();
			fail();
		}catch(Exception expected) {}
	}
	
	@Test
	public void packageTypeSetButInstitutionMetadataFileNotExistent() {
		makeMetadataFile(WorkArea.WA_INSTITUTION, CB_PACKAGETYPE_EAD).delete();
		try {
			action.checkPreconditions();
			fail();
		}catch(Exception expected) {}
	}
	
	
	@Test 
	public void writeEAD() throws FileNotFoundException, UserException, IOException, RepositoryException, JDOMException, ParserConfigurationException, SAXException, SubsystemNotAvailableException {
		action.implementation(); 
		
		FileInputStream in = new FileInputStream(makeMetadataFile(WorkArea.PUBLIC, METADATA_STREAM_ID_DC ));
		String dcContent = IOUtils.toString(in, ENCODING_UTF_8);
		
		assertTrue(dcContent.contains(
			"<dc:title>Forschungsstelle RheinlÃ¤nder in aller Welt; Bezirksstelle West des Vereins fÃ¼r das Deutschtum im Ausland</dc:title>"
		));
		assertTrue(dcContent.contains(
			"<dc:identifier>urn</dc:identifier>"
		));
		assertTrue(dcContent.contains(
			"<dc:format>EAD</dc:format>"
		));
	}
	
	
	
	
	@Test
	public void rollback() throws Exception {
		
		action.implementation();
		action.rollback();
		
		assertFalse(makeMetadataFile(WorkArea.PUBLIC,C.METADATA_STREAM_ID_DC).exists());
		assertFalse(makeMetadataFile(WorkArea.WA_INSTITUTION,C.METADATA_STREAM_ID_DC).exists());
	}
	
	
	
	private File makeMetadataFile(String pipType,String fileName) {
		return Path.makeFile(
				n.getWorkAreaRootPath(),WorkArea.PIPS,pipType,
				o.getContractor().getShort_name(),o.getIdentifier(),fileName+FILE_EXTENSION_XML);
	}
}
