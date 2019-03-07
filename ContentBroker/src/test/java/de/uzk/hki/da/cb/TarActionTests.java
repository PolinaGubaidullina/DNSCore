/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2014 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln
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
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Package;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.RelativePath;


/**
 * The Class TarActionTests.
 *
 * @author Daniel M. de Oliveira
 */
public class TarActionTests extends ConcreteActionUnitTest {

	
	@ActionUnderTest
	TarAction action = new TarAction();
	
	static String workAreaRootPath = "src/test/resources/cb/TarActionTests/Implementation/";
	
	/** The backup package path. */
	static String backupPackagePath = workAreaRootPath+"work/TEST/identifier_/";
	
	/** The package fork path. */
	static String packageForkPath = workAreaRootPath+"work/TEST/identifier/";
	
	/** The unpacked package path. */
	static String unpackedPackagePath = workAreaRootPath+"work/csn/identifier_unpacked/";
	
	/** The target tar file. */
	static File targetTarFile = new File(workAreaRootPath+"work/TEST/identifier.pack_2.tar");
	
	/** The job. */
	static Job job = new Job("csn","vm3");
	
	/** The rep name. */
	static String repName = "2012_01_01+12_12+";
	
	@Before
	public void setUp() throws IOException{
		
		n.setWorkingResource("vm3");
		n.setWorkAreaRootPath(new RelativePath(workAreaRootPath));
		
		Package pkg = new Package();
		pkg.setDelta(2);
		o.getPackages().add(pkg);
		
		job.setRep_name(repName);

		action.setDistributedConversionAdapter(mock(DistributedConversionAdapter.class));
		
		FileUtils.copyDirectory(new File(backupPackagePath), new File(packageForkPath));
	}
	
	
	
	
	/**
	 * Tear down.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@After
	public void tearDown() throws IOException{
		if (targetTarFile.exists()){targetTarFile.delete();}
		
		if (new File(packageForkPath).exists()) FolderUtils.deleteDirectorySafe(new File(packageForkPath)); 
		if (new File(unpackedPackagePath).exists()) FolderUtils.deleteDirectorySafe(new File(unpackedPackagePath));
		
	}
	
	/**
	 * Test tar creation.
	 * @throws IOException 
	 */
	@Test
	public void testTarCreation() throws IOException{
		action.implementation();
		assertTrue(targetTarFile.exists());
	}
	
	
	/**
	 * In case the package still exists in the fork directory, we can safely remove any
	 * (possibly partially) created tar.
	 * @author Daniel M. de Oliveira
	 * @throws IOException 
	 */
	@Test
	public void rollback() throws IOException {
		action.implementation();
		assertTrue(targetTarFile.exists());
		
		action.rollback();
		assertFalse(targetTarFile.exists());
	}
}
