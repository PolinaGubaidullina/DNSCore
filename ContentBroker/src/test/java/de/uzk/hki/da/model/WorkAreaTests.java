/*ileleee
 DA-NRW Software Suite | ContentBroker
 Copyright (C) 2014 Historisch-Kulturwissenschaftliche Informationsverarbeitung
 Universität zu Köln
 Copyright (C) 2015 LVR-InfoKom
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

package de.uzk.hki.da.model;

import static de.uzk.hki.da.utils.C.FILE_EXTENSION_XML;
import static de.uzk.hki.da.utils.C.TEST_USER_SHORT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.test.TC;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.Path;
import de.uzk.hki.da.utils.RelativePath;

/**
 * @author Daniel M. de Oliveira
 */
public class WorkAreaTests {
	
	private static final String CONTAINER_NAME = "sip.txt";
	private static final String UNDERSCORE = "_";
	private static final Path WORK_AREA_ROOT_PATH = Path.make(TC.TEST_ROOT_MODEL,"WorkArea");
	private static final Path WORK_AREA_ROOT_PATH_TMP = Path.make(TC.TEST_ROOT_MODEL,"WorkArea_");
	Object o = new Object();
	Node n = new Node();
	private WorkArea wa;
	
	@Before
	public void setUp() throws IOException {
		FileUtils.copyDirectory(WORK_AREA_ROOT_PATH_TMP.toFile(), WORK_AREA_ROOT_PATH.toFile());
		
		o.setIdentifier(TC.IDENTIFIER);
		n.setWorkAreaRootPath(WORK_AREA_ROOT_PATH);
		User c = new User();
		c.setShort_name(TEST_USER_SHORT_NAME);
		o.setContractor(c);
		Package pkg = new Package();
		pkg.setName("1");
		pkg.setContainerName(CONTAINER_NAME);
		pkg.setId(1);
		o.getPackages().add(pkg);
		wa = new WorkArea(n,o);
	}
	
	@After 
	public void tearDown() throws IOException {
		FolderUtils.deleteDirectorySafe(WORK_AREA_ROOT_PATH.toFile());
	}
	
	@Test
	public void pipFolder() {
		Path rp = wa.pipFolder(WorkArea.PUBLIC);
		assertEquals(Path.make(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.PUBLIC,o.getContractor().getShort_name(),o.getIdentifier()),rp);
		Path ri = wa.pipFolder(WorkArea.WA_INSTITUTION);
		assertEquals(Path.make(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.WA_INSTITUTION,o.getContractor().getShort_name(),o.getIdentifier()),ri);
	}
	
	@Test
	public void pipSourceFolderPath() {
		Path rp = wa.pipSourceFolderPath(WorkArea.PUBLIC);
		assertEquals(Path.make(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.PUBLIC,
				o.getContractor().getShort_name(),o.getIdentifier()+UNDERSCORE+o.getLatestPackage().getId()),rp);
		Path ri = wa.pipSourceFolderPath(WorkArea.WA_INSTITUTION);
		assertEquals(Path.make(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.WA_INSTITUTION,
				o.getContractor().getShort_name(),o.getIdentifier()+UNDERSCORE+o.getLatestPackage().getId()),ri);
	}
	
	@Test
	public void pipSourceFolder() {
		File rp = wa.pipSourceFolder(WorkArea.PUBLIC);
		assertEquals(Path.make(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.PUBLIC,
				o.getContractor().getShort_name(),o.getIdentifier()+UNDERSCORE+o.getLatestPackage().getId()).toFile(),rp);
		File ri = wa.pipSourceFolder(WorkArea.WA_INSTITUTION);
		assertEquals(Path.make(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.WA_INSTITUTION,
				o.getContractor().getShort_name(),o.getIdentifier()+UNDERSCORE+o.getLatestPackage().getId()).toFile(),ri);
	}
	
	@Test
	public void pipSourceFolderRelativePath() {
		Path rp = wa.pipSourceFolderRelativePath(WorkArea.PUBLIC);
		assertEquals(new RelativePath(WorkArea.PIPS,WorkArea.PUBLIC,
				o.getContractor().getShort_name(),o.getIdentifier()+UNDERSCORE+o.getLatestPackage().getId()),rp);
		Path ri = wa.pipSourceFolderRelativePath(WorkArea.WA_INSTITUTION);
		assertEquals(new RelativePath(WorkArea.PIPS,WorkArea.WA_INSTITUTION,
				o.getContractor().getShort_name(),o.getIdentifier()+UNDERSCORE+o.getLatestPackage().getId()),ri);
	}
	
	@Test
	public void metadataFile() {
		final String metadataFileName = "metadata";
		File r = wa.pipMetadataFile(WorkArea.PUBLIC,metadataFileName);
		assertEquals(Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.PIPS,WorkArea.PUBLIC,o.getContractor().getShort_name(),o.getIdentifier(),metadataFileName+FILE_EXTENSION_XML),r);
	}
	
	@Test
	public void toFile() {
		final String repName="rep+a";
		final String relativePath="sub/file.txt"; 
		DAFile daf = new DAFile(repName,relativePath);
		File r = wa.toFile(daf);
		assertEquals(Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.WORK,o.getContractor().getShort_name(),o.getIdentifier(),WorkArea.DATA,repName,relativePath),r);
	}
	
	@Test
	public void ingestSIP() throws IOException {
		wa.ingestSIP(Path.makeFile(WORK_AREA_ROOT_PATH,CONTAINER_NAME));
		assertTrue(Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.WORK,o.getContractor().getShort_name(),CONTAINER_NAME).exists());
	}
	
	@Test
	public void sipFile() {
		assertEquals(Path.makeFile(WORK_AREA_ROOT_PATH,WorkArea.WORK,o.getContractor().getShort_name(),o.getLatestPackage().getContainerName()),wa.sipFile());
	}
	
	@Test
	public void objectPath() {
		assertEquals(Path.make(WORK_AREA_ROOT_PATH,WorkArea.WORK,o.getContractor().getShort_name(),o.getIdentifier()),wa.objectPath());
	}
	
	
	
	
}
