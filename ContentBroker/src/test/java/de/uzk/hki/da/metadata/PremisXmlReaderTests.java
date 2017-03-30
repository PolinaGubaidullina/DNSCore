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
package de.uzk.hki.da.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.uzk.hki.da.model.Event;
import de.uzk.hki.da.model.MigrationRight;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.ObjectPremisXmlReader;
import de.uzk.hki.da.model.Package;
import de.uzk.hki.da.model.PremisXmlValidator;
import de.uzk.hki.da.model.PublicationRight.Audience;
import de.uzk.hki.da.utils.C;

/**
 * The Class PremisXmlReaderTests.
 * 
 * @author <a href="mailto:eugen.trebunski@lvr.de">Eugen Trebunski</a>
 */
public class PremisXmlReaderTests {

	/**
	 * Sets the up.
	 * 
	 * @throws IOException
	 */
	@Before
	public void setUp() throws IOException {
		FileUtils.copyFileToDirectory(C.PREMIS_XSD_TEST, new File("conf/"));
		FileUtils.copyFileToDirectory(C.XLINK_XSD_TEST, new File("conf/"));
		FileUtils.copyFileToDirectory(C.CONTRACT_XSD_TEST, new File("conf/"));
	}

	@After
	public void cleanUp() {
		if (new File("conf/premis.xsd").exists())
			new File("conf/premis.xsd").delete();
		if (new File("conf/danrw-contract-1.xsd").exists())
			new File("conf/danrw-contract-1.xsd").delete();
		if (new File("conf/xlink.xsd").exists())
			new File("conf/xlink.xsd").delete();
	}

	/**
	 * Test.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParseException
	 *             the parse exception
	 */
	@Test
	public void test() throws IOException, ParseException {
		ObjectPremisXmlReader reader = new ObjectPremisXmlReader();
		Object premisObject = reader.deserialize(new File("src/test/resources/metadata/premisMainSection.xml"));

		Package pkg = premisObject.getPackages().get(0);

		assertEquals("2013_07_31+11_54+a", pkg.getFiles().get(0).getRep_name());
		assertEquals("premis.xml", pkg.getFiles().get(0).getRelative_path());
		assertEquals("09d47bde493c1ccc5dcf0ea682c67054", pkg.getFiles().get(0).getChksum());
		assertEquals("3844", pkg.getFiles().get(0).getSize());

		assertEquals("CONVERT", pkg.getEvents().get(0).getType());
		assertEquals("SIP_CREATION", pkg.getEvents().get(1).getType());
		assertEquals("INGEST", pkg.getEvents().get(2).getType());

		assertEquals("CONTRACTOR", pkg.getEvents().get(2).getAgent_type());
		assertEquals("TEST", pkg.getEvents().get(2).getAgent_name());
		assertEquals("APPLICATION", pkg.getEvents().get(1).getAgent_type());
		assertEquals("DA-NRW SIP-Builder 0.5.3", pkg.getEvents().get(1).getAgent_name());

		// test events
		for (Event e : pkg.getEvents()) {
			System.out.println(e);
		}

	}

	/**
	 * Test rights.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParseException
	 *             the parse exception
	 */
	@Test
	public void testRights() throws IOException, ParseException {

		ObjectPremisXmlReader reader = new ObjectPremisXmlReader();
		Object premisObject = reader.deserialize(new File("src/test/resources/metadata/premis_xml_metadata_reader_test.xml"));

		System.out.println(premisObject.getRights());
		assertNotNull(premisObject.getRights());
		assertNotNull(premisObject.getRights().getPublicationRights());
		assertFalse(premisObject.getRights().getPublicationRights().isEmpty());
		assertEquals("640", premisObject.getRights().getPublicationRights().get(0).getImageRestriction().getWidth());
		assertEquals("480", premisObject.getRights().getPublicationRights().get(0).getImageRestriction().getHeight());

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);
		calendar.set(2023, 5, 23, 0, 0, 0);

		assertEquals(MigrationRight.Condition.NOTIFY, premisObject.getRights().getMigrationRight().getCondition());

		assertEquals("ePflicht", premisObject.getRights().getPublicationRights().get(0).getLawID());
		assertEquals(2, premisObject.getRights().getPublicationRights().get(0).getTextRestriction().getCertainPages().length);
		assertEquals(23, premisObject.getRights().getPublicationRights().get(0).getTextRestriction().getCertainPages()[0]);
		assertEquals(42, premisObject.getRights().getPublicationRights().get(0).getTextRestriction().getCertainPages()[1]);

		assertTrue(!premisObject.grantsPublicationRight(Audience.PUBLIC));
		assertTrue(!premisObject.grantsPublicationRight(Audience.INSTITUTION));

	}

	@Test
	public void testXSDValidation() throws IOException, ParseException,
			SAXException {
		assertTrue(PremisXmlValidator.validatePremisFile(new File("src/test/resources/metadata/sipbuilder201603_premis.xml")));
		assertTrue(PremisXmlValidator.validatePremisFile(new File("src/test/resources/metadata/sipbuilder201603_premisDDBExc.xml")));
	}

	@Test(expected = SAXParseException.class)
	public void testXSDValidationFailWrongAttributeType() throws IOException,
			ParseException, SAXException {
		assertTrue(PremisXmlValidator.validatePremisFile(new File("src/test/resources/metadata/sipbuilder201603_premisWrongTextRestrictionValue.xml")));
	}

	@Test(expected = SAXParseException.class)
	public void testXSDValidationFailAdditionlAttribute() throws IOException,
			ParseException, SAXException {
		assertTrue(PremisXmlValidator.validatePremisFile(new File("src/test/resources/metadata/sipbuilder201603_premisFailRestrictAudioAdditionalAttribute.xml")));
	}

	@Test
	public void testXSDValidationFailWithoutSipCreationEvent() throws IOException,
			ParseException, SAXException {
		assertFalse(PremisXmlValidator.validatePremisFile(new File("src/test/resources/metadata/sipbuilder201603_premisFailWithoutSipCreationEvent.xml")));
	}
	
	@Test
	public void testXSDValidationFailWithoutRightsSection() throws IOException,
			ParseException, SAXException {
		assertFalse(PremisXmlValidator.validatePremisFile(new File("src/test/resources/metadata/sipbuilder201603_premisFailWithoutRightsSection.xml")));
	}

}
