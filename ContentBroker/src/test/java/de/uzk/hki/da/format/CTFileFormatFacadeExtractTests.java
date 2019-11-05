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

package de.uzk.hki.da.format;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uzk.hki.da.model.FormatMapping;
import de.uzk.hki.da.model.JHoveParameterMapping;
import de.uzk.hki.da.test.CTTestHelper;
import de.uzk.hki.da.test.TC;
import de.uzk.hki.da.utils.CommandLineConnector;
import de.uzk.hki.da.utils.Path;

/**
 * @author Daniel M. de Oliveira
 */
public class CTFileFormatFacadeExtractTests {
	private static final String PUID_XML = "fmt/101";
	private static final String MIME_XML = "application/xml";
	private static final String JHOVE_OPT_XML = "-m UTF8-hul";
	
	private static final Path testRoot = Path.make(TC.TEST_ROOT_FORMAT,"CTFileFormatFacadeExtract");
	private static final ConfigurableFileFormatFacade fff = new ConfigurableFileFormatFacade();;
	private static final JhoveMetadataExtractor metadataExtractor = new JhoveMetadataExtractor();
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		CTTestHelper.prepareWhiteBoxTest();
		metadataExtractor.setCli(new CommandLineConnector());
		if (!metadataExtractor.isConnectable()) fail();
		fff.setMetadataExtractor(metadataExtractor);
		fff.setFormatScanService(new FakeFormatScanService());
		setupMetadataExtractorByReflection(metadataExtractor);
	}
	
	@Before
	public  void setUpBefore() throws IOException {
		CTTestHelper.prepareWhiteBoxTest();
		//some tests do cleanupWhiteBoxTest, therefore it shuld be executed before each test
	}
	
	private static void setupMetadataExtractorByReflection(JhoveMetadataExtractor jhove){
		try {
			//private variable in jhove definition with reflections to avoid DB fetches 
			// to avoid java.lang.IllegalStateException: sessionFactory is null in HibernateUtil
			List<JHoveParameterMapping> possibleOptions = new ArrayList<JHoveParameterMapping>();
			possibleOptions.add(new JHoveParameterMapping(MIME_XML,JHOVE_OPT_XML));
			Field possibleOptionsField;
			possibleOptionsField = jhove.getClass().getDeclaredField("possibleOptions");
			possibleOptionsField.setAccessible(true);
			possibleOptionsField.set(jhove, possibleOptions);			

			List<FormatMapping> pronomMimetypeList = new ArrayList<FormatMapping>();
			FormatMapping fMapping=new FormatMapping();
			fMapping.setPuid(PUID_XML);
			fMapping.setMime_type(MIME_XML);
			pronomMimetypeList.add(fMapping);
			Field pronomMimetypeListField = jhove.getClass().getDeclaredField("pronomMimetypeList");
			pronomMimetypeListField.setAccessible(true);
			pronomMimetypeListField.set(jhove, pronomMimetypeList);


		} catch (Exception e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
	} 
	
	
	
	@AfterClass
	public static void tearDownAfterClass() throws IOException {
		CTTestHelper.cleanUpWhiteBoxTest();
	}
	
	@Test
	public void extractEAD() {
		assertTrue(fff.connectivityCheck());
		
		try {
			fff.extract(Path.makeFile(testRoot,"vda3.XML"), Path.makeFile(testRoot,"vda3.XML.output"),"application/xml");
		} catch (ConnectionException e) {
			fail();
		} catch (Exception e) {
			fail(e.getClass()+"\n"+e.getStackTrace()[0]+"\n"+e.getMessage());
		}
	}

	@Test
	public void binaryNotPresent() {
		CTTestHelper.cleanUpWhiteBoxTest();

		try {
			fff.extract(Path.makeFile(testRoot,"vda3.XML"), Path.makeFile(testRoot,"vda3.XML.output"),"application/xml");
			fail();
		} catch (ConnectionException e) {
			assertTrue(e!=null);
		} catch (Exception e) {
			fail();
		}
	}	
	
}
