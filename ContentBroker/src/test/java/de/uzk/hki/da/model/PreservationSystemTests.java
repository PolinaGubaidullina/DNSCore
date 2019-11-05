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

package de.uzk.hki.da.model;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uzk.hki.da.service.HibernateUtil;
import de.uzk.hki.da.utils.C;



/**
 * The Class PreservationSystemTests.
 */

/**
 * @author Daniel M. de Oliveira
 *
 */
public class PreservationSystemTests {

	/**
	 * Sets the up before class.
	 *
	 * @throws Exception the exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * Tear down after class.
	 *
	 * @throws Exception the exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private PreservationSystem preservationSystem;
	
	/** The file. */
	private DAFile file;

	private ConversionPolicy one;

	private ConversionPolicy two;

	private ConversionPolicy three;

	private ConversionPolicy four;
	
	private ConversionPolicy unrecognizedPuid;
	
	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
	
		HibernateUtil.init("src/main/xml/hibernateCentralDB.cfg.xml.inmem");
		
		ConversionRoutine routine = new ConversionRoutine();
		routine.setName("COPY");
		
		// Conversion Policies
		one = new ConversionPolicy();
		one.setSource_format("fmt/10");
		one.setConversion_routine(routine);
		two = new ConversionPolicy();
		two.setSource_format("fmt/10");
		two.setConversion_routine(routine);
		
		three = new ConversionPolicy();
		three.setSource_format("fmt/10");
		three.setConversion_routine(routine);
		three.setPresentation(true);
		four = new ConversionPolicy();
		four.setSource_format("fmt/10");
		four.setConversion_routine(routine);
		four.setPresentation(true);
		unrecognizedPuid = new ConversionPolicy();
		unrecognizedPuid.setSource_format(C.UNRECOGNIZED_PUID);
		unrecognizedPuid.setConversion_routine(routine);
		unrecognizedPuid.setPresentation(false);
		
		User admin = new User(); 
		
		preservationSystem = new PreservationSystem();
		
		preservationSystem.setAdmin(admin);
		preservationSystem.setMinRepls(1);
		preservationSystem.getConversion_policies().add(one);
		preservationSystem.getConversion_policies().add(two);
		preservationSystem.getConversion_policies().add(three);
		preservationSystem.getConversion_policies().add(four);
		preservationSystem.getConversion_policies().add(unrecognizedPuid);
		
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		session.save(admin);
		session.save(routine);
		session.save(one);
		session.save(two);
		session.save(three);
		session.save(four);
		session.save(unrecognizedPuid);
		session.save(preservationSystem);
		session.getTransaction().commit();
		session.close();

		// creating a valid file
		file = new DAFile("","");
		file.setFormatPUID("fmt/10");
		
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void tearDown() throws Exception {
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		session.delete(preservationSystem);
		session.getTransaction().commit();
		session.close();
	}

	/**
	 * Test file has no file format.
	 */
	@Test
	public void testFileHasNoFileFormat(){
		
		DAFile fileundef = new DAFile("","");
		fileundef.setFormatPUID("");
		try {
			//According to Quality Levels no PUID is becoming one of usual cases
			List<ConversionPolicy> polList=preservationSystem.getApplicablePolicies(fileundef, false);
			assertTrue(polList.isEmpty());
			//fail(); 
		} catch (Exception  e) {
			
		} 	
	}
	
	/**
	 * Test file has no file format.
	 */
	@Test
	public void testFileHasUndefinedFileFormat(){
		
		DAFile fileundef = new DAFile("","");
		fileundef.setFormatPUID(C.UNRECOGNIZED_PUID);
		try {
			//According to Quality Levels undef PUID is becoming one of usual cases
			List<ConversionPolicy> polList=preservationSystem.getApplicablePolicies(fileundef, false);
			assertTrue(polList.isEmpty());
			//fail(); 
		} catch (Exception  e) {
			
		} 	
	}
	
	/**
	 * Test success scenario.
	 */
	@Test
	public void testSuccessScenario() {

		List<ConversionPolicy> policies = 
				preservationSystem.getApplicablePolicies(file,false);
		assertTrue(one.getId().equals(policies.get(0).getId()));
		assertTrue(two.getId().equals(policies.get(1).getId()));
		
		List<ConversionPolicy> policies2 = 
				preservationSystem.getApplicablePolicies(file,true);
		assertTrue(three.getId().equals(policies2.get(0).getId()));
		assertTrue(four.getId().equals(policies2.get(1).getId()));
	}
}
