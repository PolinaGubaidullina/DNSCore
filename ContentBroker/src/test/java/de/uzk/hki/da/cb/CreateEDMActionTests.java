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


package de.uzk.hki.da.cb;

import static de.uzk.hki.da.test.TC.TEST_ROOT_CB;
import static de.uzk.hki.da.utils.C.CB_PACKAGETYPE_EAD;
import static de.uzk.hki.da.utils.C.CB_PACKAGETYPE_METS;
import static de.uzk.hki.da.utils.C.EDM_FOR_ES_INDEX_METADATA_STREAM_ID;
import static de.uzk.hki.da.utils.C.FILE_EXTENSION_XML;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.uzk.hki.da.core.PreconditionsNotMetException;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.repository.Fedora3RepositoryFacade;
import de.uzk.hki.da.repository.RepositoryException;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.Path;


/**
 * @author Daniel M. de Oliveira
 */
public class CreateEDMActionTests extends ConcreteActionUnitTest{

	@ActionUnderTest
	CreateEDMAction action = new CreateEDMAction();

	private static final String METS_MODS_TO_EDM_XSL = "src/main/xslt/edm/mets-mods_to_edm.xsl";
	private static final String EAD_TO_EDM_XSL = "src/main/xslt/edm/ead_to_edm.xsl";
	private static final Path WORK_AREA_ROOT_PATH = Path.make(TEST_ROOT_CB,"CreateEDMAction");
	
	@Before
	public void setUp() throws FileNotFoundException, RepositoryException, IOException {
		n.setWorkAreaRootPath(WORK_AREA_ROOT_PATH);
		o.setPackage_type(CB_PACKAGETYPE_EAD);
		action.setWorkArea(new WorkArea(n,o));
		Map<String,String> edmMappings = new HashMap<String,String>();
		edmMappings.put(CB_PACKAGETYPE_EAD, EAD_TO_EDM_XSL);
		edmMappings.put(CB_PACKAGETYPE_METS,METS_MODS_TO_EDM_XSL);
		action.setEdmMappings(edmMappings);
		action.setRepositoryFacade(mock(Fedora3RepositoryFacade.class));
		
		FileUtils.copyDirectory(Path.makeFile(WORK_AREA_ROOT_PATH,"_"+WorkArea.PIPS),Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.PIPS));
		
	}
	
	public File makeMetadataFile(String fileName) {
		return Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.PUBLIC,o.getContractor().getShort_name(),
				o.getIdentifier(),fileName+FILE_EXTENSION_XML);
	}
	
	
	
	@After
	public void tearDown() {
		FolderUtils.deleteQuietlySafe(Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.PIPS));
	}
	
	@Test
	public void missingPackageType() throws IOException, RepositoryException {
		o.setPackage_type(null);
		try {
			action.checkPreconditions();
			fail();
		} catch (PreconditionsNotMetException e) {
			assertTrue(e.getMessage().contains("package type"));
		}
	}
	

	@Test
	public void missingMetadataFileInPublicPIP() throws IOException, RepositoryException, JDOMException, ParserConfigurationException, SAXException {
		FolderUtils.deleteQuietlySafe(makeMetadataFile(CB_PACKAGETYPE_EAD));
		try {
			action.implementation();
			fail();
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains(CB_PACKAGETYPE_EAD));
		}
	}
	
	@Test
	public void fileCreation() throws IOException, RepositoryException, JDOMException, ParserConfigurationException, SAXException{
		
		action.implementation();
		assertTrue(makeMetadataFile(EDM_FOR_ES_INDEX_METADATA_STREAM_ID).exists());
	}
	
	@Test
	public void fileDeletion() throws Exception {
		
		action.implementation();
		assertTrue(makeMetadataFile(EDM_FOR_ES_INDEX_METADATA_STREAM_ID).exists());
		action.rollback();
		assertFalse(makeMetadataFile(EDM_FOR_ES_INDEX_METADATA_STREAM_ID).exists());
	}

	
	
}
