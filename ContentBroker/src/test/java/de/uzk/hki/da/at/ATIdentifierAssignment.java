/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2015 Historisch-Kulturwissenschaftliche Informationsverarbeitung
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
package de.uzk.hki.da.at;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uzk.hki.da.model.Object;

/**
 * @author Daniel M. de Oliveira
 * @author Thomas Kleinke
 *
 */
public class ATIdentifierAssignment extends AcceptanceTest {

	private static final String ORIG_NAME_DELTA_ORIGINAL_URN = "urnDelta";
	private static final String ORIG_NAME_READ_URN_FROM_SIP = "ATReadURNFromSIP";
	private static final String ORIG_NAME_URN_BASED_ON_TECHNICAL_IDENTIFIER = "urnBasedOnTechnicalIdentifier";

	@BeforeClass
	public static void setUp() throws IOException {
		ath.putAIPToLongTermStorage("ATIdentifierAssignmentURNDelta", ORIG_NAME_DELTA_ORIGINAL_URN, new Date(), 100);
	}

	@Test
	public void urnBasedOnTechnicalIdentifier() throws IOException {
		ath.putSIPtoIngestArea("ATUseCaseIngest1", "tgz", ORIG_NAME_URN_BASED_ON_TECHNICAL_IDENTIFIER);

		ath.awaitObjectState(ORIG_NAME_URN_BASED_ON_TECHNICAL_IDENTIFIER, Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
		Object object = ath.getObject(ORIG_NAME_URN_BASED_ON_TECHNICAL_IDENTIFIER);
		assertTrue(object.getUrn().startsWith(AcceptanceTestHelper.URN_NBN_DE_DANRW + localNode.getId()));
	}

	@Test
	public void urnByUserAssignment() throws IOException, InterruptedException {

		ath.putSIPtoIngestArea(ORIG_NAME_READ_URN_FROM_SIP, "tgz", ORIG_NAME_READ_URN_FROM_SIP);
		ath.awaitObjectState(ORIG_NAME_READ_URN_FROM_SIP, Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
		Object object = ath.getObject(ORIG_NAME_READ_URN_FROM_SIP);

		assertEquals("urn:nbn:de:xyz-1-20131008367735", object.getUrn());
	}

	@Test
	public void keepURNOnDeltaIngest() throws IOException {
		ath.putSIPtoIngestArea(ORIG_NAME_READ_URN_FROM_SIP, "tgz", ORIG_NAME_DELTA_ORIGINAL_URN);
		// user assigned urn gets provided on delta. however the original urn
		// was based on technical identifier.

		ath.awaitObjectState(ORIG_NAME_DELTA_ORIGINAL_URN, Object.ObjectStatus.InWorkflow);
		ath.awaitObjectState(ORIG_NAME_DELTA_ORIGINAL_URN, Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
		Object object = ath.getObject(ORIG_NAME_DELTA_ORIGINAL_URN);
		assertEquals(AcceptanceTestHelper.URN_NBN_DE_DANRW + "ATIdentifierAssignmentURNDelta", object.getUrn());
	}
}
