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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.grid.DistributedConversionAdapter;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.RelativePath;


/**
 * The Class PrepareSendToPresenterActionTests.
 */
public class PrepareSendToPresenterActionTests extends ConcreteActionUnitTest{

	@ActionUnderTest
	PrepareSendToPresenterAction action = new PrepareSendToPresenterAction();

	private static final String WORK_AREA_ROOT_PATH = "src/test/resources/cb/PrepareSendToPresenterActionTests";
	
	private File publicFile = new File(WORK_AREA_ROOT_PATH+"/pips/public/TEST/identifier_1_1/a.txt");
	private File institutionFile = new File(WORK_AREA_ROOT_PATH+"/pips/institution/TEST/identifier_1_1/a.txt");

	/**
	 * Sets the up.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Before
	public void setUp() throws IOException {
		n.setWorkAreaRootPath(new RelativePath(WORK_AREA_ROOT_PATH));
		action.setWorkArea(new WorkArea(n, o));
		action.setDistributedConversionAdapter(mock (DistributedConversionAdapter.class));

		o.setIdentifier("identifier_1");
		Node dipNode = new Node(); dipNode.setName("dipNode");

		DAFile premis = new DAFile("rep_b","premis.xml");
		o.getLatestPackage().getFiles().add(premis);
		
		
		new File(WORK_AREA_ROOT_PATH+"/pips/institution").mkdirs();
		new File(WORK_AREA_ROOT_PATH+"/pips/public").mkdirs();
		
		FileUtils.copyDirectory(new File(WORK_AREA_ROOT_PATH+"/sources/1"), new File(WORK_AREA_ROOT_PATH+"/work/TEST/identifier_1"));
		FileUtils.copyDirectory(new File(WORK_AREA_ROOT_PATH+"/sources/2"), new File(WORK_AREA_ROOT_PATH+"/work/TEST/identifier_2"));
	}
	
	/**
	 * Publish everything.
	 * @throws IOException 
	 */
	@Test
	public void publishEverything() throws IOException {
		
		action.implementation();
		
		assertTrue (publicFile.exists() );
		assertTrue (institutionFile.exists() );
	}
	
	/**
	 * Publish nothing.
	 * @throws IOException 
	 */
	@Test
	public void publishNothing() throws IOException {
		o.setIdentifier("identifier_2");
		o.getLatestPackage().setName("2");
		
		action.implementation();
		
		assertFalse (publicFile.exists() );
		assertFalse (institutionFile.exists() );
	}
	
	
	
	/**
	 * Tear down.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@After
	public void tearDown() throws IOException {
		
		FolderUtils.deleteDirectorySafe(new File(WORK_AREA_ROOT_PATH+"/work/TEST/identifier_1"));
		FolderUtils.deleteDirectorySafe(new File(WORK_AREA_ROOT_PATH+"/work/TEST/identifier_2"));
		FolderUtils.deleteDirectorySafe(new File(WORK_AREA_ROOT_PATH+"/pips/institution"));
		FolderUtils.deleteDirectorySafe(new File(WORK_AREA_ROOT_PATH+"/pips/public"));
	}
	
	
}
