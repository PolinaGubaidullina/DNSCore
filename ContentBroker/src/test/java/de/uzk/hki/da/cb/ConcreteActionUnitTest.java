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

import static de.uzk.hki.da.test.TC.IDENTIFIER;
import static de.uzk.hki.da.test.TC.URN;
import static de.uzk.hki.da.utils.C.TEST_USER_SHORT_NAME;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.junit.Before;

import de.uzk.hki.da.action.AbstractAction;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.Package;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.model.User;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.Path;

/**
 * Provides the basic framework for effective tests of the business code
 * distributed over the different actions.
 * 
 * Subclasses should use 
 * <pre>
 * "@ActionUnderTest
 * ConcreteAction action = new ConcreteAction();
 * </pre> to mark which action is under test. The action then gets initialized
 * automatically as if it came directly from the ActionFactory.
 * <br>
 * <b>Note</b> that you must not use the JUnit @Before and @After annotations in subclasses.
 * 
 * @author Daniel M. de Oliveira
 *
 */
public class ConcreteActionUnitTest {

	PreservationSystem ps;
	Node n = null;
	Object o;
	Job j;
	WorkArea wa;
	private User c;
	
	protected static AbstractAction a;
	

	
	@Before
	public void setUpBeforeActionTest() throws Exception{
		ps = new PreservationSystem();
		ps.setId(1);
		ps.setPresServer("localnode");
		ps.setOpenCollectionName("open-collection");
		ps.setClosedCollectionName("closed-collection");
		ps.setUrnNameSpace("urn:nbn:de");
		User psadmin = new User();
		psadmin.setShort_name("TEST_PSADMIN");
		psadmin.setEmailAddress("noreply");
		ps.setAdmin(psadmin);
		ps.setUrisCho("cho");
		ps.setUrisAggr("aggr");
		ps.setUrisLocal("local");
		ps.setMinRepls(3);
		ps.setLicenseValidationTestCSNFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_YES);
		ps.setLicenseValidationFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_YES);
		n = new Node();
		n.setName("testnode");
		n.setAdmin(psadmin);
		
		ps.getNodes().add(n);
		
		c = new User();
		c.setShort_name(TEST_USER_SHORT_NAME);
		c.setUsername(c.getShort_name());
		c.setEmailAddress("noreply");
		
		Package pkg = new Package();
		pkg.setDelta(1);
		pkg.setId(1);
		pkg.setContainerName("testcontainer.tgz");
		
		o = new Object();
		o.setContractor(c);
		o.setIdentifier(IDENTIFIER);
		o.getPackages().add(pkg);
		o.setUrn(URN);
		o.setInitial_node("testnode");
		o.setLicense_flag(C.LICENSEFLAG_METS);
		j = new Job();
		j.setObject(o);
		
		ActionUnderTestAnnotationParser parser = new ActionUnderTestAnnotationParser();
		Field f = parser.parse(this.getClass());
		AbstractAction a = (AbstractAction) f.get(this);
		
		a.setTestContractors(new HashSet<String>(){{this.add(c.getUsername());}});
		a.setObject(o);
		a.setLocalNode(n);
		a.setJob(j);
		a.setPSystem(ps);

		n.setWorkAreaRootPath(Path.make("mock"));
		wa=new WorkArea(n,o);
		a.setWorkArea(wa);
	}
}
