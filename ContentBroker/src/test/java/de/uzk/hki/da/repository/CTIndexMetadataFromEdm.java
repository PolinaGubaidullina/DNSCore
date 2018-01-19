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

package de.uzk.hki.da.repository;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.RelativePath;


/**
 * @author Polina Gubaidullina
 * @author Daniel M. de Oliveira
 *
 */
public class CTIndexMetadataFromEdm {

    // index name generated from elasticsearch.indexName + _test for the TEST contractors
	private static final String INDEX_NAME = "portal_ci"+MetadataIndex.TEST_INDEX_SUFFIX;
	
	File edmFile = new RelativePath("src", "test", "resources", "repository", "CTIndexMetadataFromEdmTests", "edmContent").toFile();
	File metsEdm = new RelativePath("src", "test", "resources", "repository", "CTIndexMetadataFromEdmTests", "metsEdm").toFile();
	File lidoEdm = new RelativePath("src", "test", "resources", "repository", "CTIndexMetadataFromEdmTests", "lidoEdm").toFile();
	File eadEdm = new RelativePath("src", "test", "resources", "repository", "CTIndexMetadataFromEdmTests", "eadEdm").toFile();
	File metsContentEdm = new RelativePath("src", "test", "resources", "repository", "CTIndexMetadataFromEdmTests", "edmContentFromMets").toFile();
	ElasticsearchMetadataIndex esmi;
	
	@Before 
	public void setUp() throws MalformedURLException {
		esmi = new ElasticsearchMetadataIndex(); 
		esmi.setCluster("cluster_ci");
		String[] hosts={"http://localhost:9200/"};
		esmi.setHosts(hosts);
		esmi.setEdmJsonFrame(new RelativePath("src", "main", "resources", "frame.jsonld").toString());		
	}
	
	
	@Test
	public void testMetsEdm() throws FileNotFoundException, IOException {
		
		String metsEdmContent = IOUtils.toString(new FileInputStream(metsEdm), C.ENCODING_UTF_8);
	
		try {
			esmi.prepareAndIndexMetadata(INDEX_NAME, "1-123456789","BibliothekTest", metsEdmContent);
		} catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			Thread.sleep(4711);
		} catch (InterruptedException e) {}

		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "1-123456789").contains("\"edm:dataProvider\":\"Universitäts- und Landesbibliothek Münster\""));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "1-123456789").contains("\"dc:title\":[\"und der größeren evangelischen Gemeinde in derselben\",\"Chronik der Stadt Hoerde\"]"));	
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "1-123456789").contains("\"dcterms:issued\":[\"1836\"]"));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "1-123456789").contains("\"dcterms:created\":[\"2011\"]"));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "1-123456789").contains("\"dc:publisher\":[\"Münster\",\"Hoerde\"]"));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "1-123456789").contains("\"@institutionType\":\"BibliothekTest\""));
		
	}	
	
	
	@Test
	public void testLidoEdm() throws FileNotFoundException, IOException {
		
		String lidoEdmContent = IOUtils.toString(new FileInputStream(lidoEdm), C.ENCODING_UTF_8);
	
		try {
			esmi.prepareAndIndexMetadata(INDEX_NAME, "2-123456789","MuseumTest", lidoEdmContent);
		} catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			Thread.sleep(4711);
		} catch (InterruptedException e) {}

		System.out.println(esmi.getIndexedMetadata(INDEX_NAME, "2-123456789-f838082dc50949e8b57346d904efdd3d"));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "2-123456789-f838082dc50949e8b57346d904efdd3d")
				.contains("\"dc:title\":[\"Vier Mädchen auf einer Altane\",\"Mädchen auf Altane, Stadt im Hintergrund\"]"));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "2-123456789-f838082dc50949e8b57346d904efdd3d").contains("\""+C.EDM_DATE_ISSUED+"\":[\"1913\"]"));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "2-123456789").contains("\"@institutionType\":\"MuseumTest\""));
	}	
	
	
	
	@Test
	public void eadEdmTest() throws FileNotFoundException, IOException {
		
		String eadEdmContent = IOUtils.toString(new FileInputStream(eadEdm), C.ENCODING_UTF_8);
	
		try {
			esmi.prepareAndIndexMetadata(INDEX_NAME, "3-123456789","ArchivTest", eadEdmContent);
		} catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			Thread.sleep(4711);
		} catch (InterruptedException e) {}
		

		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "3-123456789-569c0c3d21aa45b8bb230d4b3bdec00e").contains("\"dc:title\":[\"Jugendherbergsverband, Schriftwechsel\"]"));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "3-123456789-457009589ee34cdcaa71ed56a2dad8a6").contains("\""+C.EDM_DATE_ISSUED+"\":[\"1937-01-01/1938-12-31\"]"));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "3-123456789-457009589ee34cdcaa71ed56a2dad8a6").contains("\"dc:title\":[\"Volksdeutsches Rundfunkreferat\"]"));	
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "3-123456789-457009589ee34cdcaa71ed56a2dad8a6").contains("\"dcterms:isPartOf\""));
		assertTrue(esmi.getIndexedMetadata(INDEX_NAME, "3-123456789").contains("\"@institutionType\":\"ArchivTest\""));
	}	

}
