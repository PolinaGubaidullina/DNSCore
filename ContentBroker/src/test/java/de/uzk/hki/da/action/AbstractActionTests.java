/*
  DA-NRW Software Suite | ContentBroker
  
  Copyright (C) 2014 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln
  
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

package de.uzk.hki.da.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.cb.NullAction;
import de.uzk.hki.da.core.SubsystemNotAvailableException;
import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.core.UserException.UserExceptionId;
import de.uzk.hki.da.core.UserExceptionManager;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.Package;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.model.User;
import de.uzk.hki.da.service.JmsMessage;
import de.uzk.hki.da.service.JmsMessageServiceHandler;
import de.uzk.hki.da.utils.C;

/**
 * 
 * @author Daniel M. de Oliveira
 *
 */
public class AbstractActionTests {

	Session mockSession = null;
	private Object object;
	private Job job;
	JmsMessageServiceHandler ams;
	
	@Before
	public void setUp(){
		mockSession = mock(Session.class);
		Transaction mockTransaction = mock(Transaction.class);
		when(mockSession.getTransaction()).thenReturn(mockTransaction);
	}
	
	
	
	private void setCommonProperties(
			AbstractAction action, 
			String startStatus,String endStatus){
		
		Package pkg = new Package();
		pkg.setDelta(1);
		pkg.setContainerName("CONTAINER");
		ams = mock (JmsMessageServiceHandler.class);
		
		UserExceptionManager userExceptionManager = mock(UserExceptionManager.class);
		when(userExceptionManager.getMessage((UserExceptionId) anyObject())).thenReturn("Ihr eingeliefertes Paket mit dem Namen %CONTAINER_NAME konnte im DA NRW nicht archiviert werden.\n\nGrund: Package ist nicht konsistent!\n\nMeldung:\n%ERROR_INFO\nEs ist wahrscheinlich, dass Fehler bei der Übertragung aufgetreten sind. Bitte versuchen Sie eine erneute Ablieferung.");
		action.setUserExceptionManager(userExceptionManager);
		
		action.setActionMap(mock(ActionRegistry.class));
		action.setActionFactory(mock(ActionFactory.class));
		
		job = new Job(); job.setStatus(startStatus);
		action.setJob(job);
		User c = new User(); c.setShort_name("TEST");c.setUsername(c.getShort_name()); c.setEmailAddress("noreply");
		object = new Object();
		object.setIdentifier("ID");
		object.setContractor(c);
		object.getPackages().add(pkg);
		job.setObject(object);
		action.setObject(object);
		action.setTestContractors(new HashSet<String>(Arrays.asList(c.getShort_name())));
		action.setJmsMessageServiceHandler(ams);
		action.setStartStatus(startStatus);
		action.setEndStatus(endStatus);
		PreservationSystem ps = new PreservationSystem(); ps.setAdmin(c);
		action.setPSystem(ps);
		Node node = new Node(); node.setAdmin(c);
		action.setLocalNode(node);
	}
	
	
	@Test
	public void implementationSuccesful() {
		SuccessfulAction action = new SuccessfulAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		verify(mockSession,times(1)).update(action.getJob());
		verify(mockSession,times(1)).update(action.getObject());
		assertEquals("200",action.getJob().getStatus());
	}
	
	@Test
	public void killAtExit(){
		KillAtExitAction action = new KillAtExitAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		verify(mockSession,times(0)).update(action.getJob());
		verify(mockSession,times(1)).delete(action.getJob());
	}
	
	@Test
	public void canIgnoreLicenseValidation(){
		DeleteObjectAction action = new DeleteObjectAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		
		User noTestUser = new User(); noTestUser.setShort_name("NOTEST"); noTestUser.setUsername(noTestUser.getShort_name());noTestUser.setEmailAddress("noreply");
		User testUser =action.getObject().getContractor();
		
		//default settings
		action.getPreservationSystem().setLicenseValidationFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_YES);
		action.getPreservationSystem().setLicenseValidationTestCSNFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_YES);
		

		//not testuser and validation DEactivated in preservationsystem
		action.getPreservationSystem().setLicenseValidationFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_NO);
		action.getObject().setContractor(noTestUser);
		Assert.assertEquals(true,action.canIgnoreLicenseValidation()); 
		action.getPreservationSystem().setLicenseValidationFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_YES); //rollback
		
		//testuser and validation DEactivated in preservationsystem
		action.getPreservationSystem().setLicenseValidationFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_NO);
		action.getObject().setContractor(testUser);
		Assert.assertEquals(true,action.canIgnoreLicenseValidation()); 
		action.getPreservationSystem().setLicenseValidationFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_YES); //rollback
		
		
		
		
		//not testuser and validation DEactivated in preservationsystem only for csn-users
		action.getPreservationSystem().setLicenseValidationTestCSNFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_NO);
		action.getObject().setContractor(noTestUser);
		Assert.assertEquals(false,action.canIgnoreLicenseValidation());
		
		//testuser and validation DEactivated in preservationsystem only for csn-users
		action.getPreservationSystem().setLicenseValidationTestCSNFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_NO);
		action.getObject().setContractor(testUser);
		Assert.assertEquals(true,action.canIgnoreLicenseValidation());
		
		//not testuser and validation activated in preservationsystem only for csn-users
		action.getPreservationSystem().setLicenseValidationTestCSNFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_YES);
		action.getObject().setContractor(noTestUser);
		Assert.assertEquals(false,action.canIgnoreLicenseValidation());
		
		//testuser and validation activated in preservationsystem only for csn-users
		action.getPreservationSystem().setLicenseValidationTestCSNFlag(C.PRESERVATIONSYS_LICENSE_VALIDATION_YES);
		action.getObject().setContractor(testUser);
		Assert.assertEquals(false,action.canIgnoreLicenseValidation());
	}
	
	@Test
	public void deleteObject(){
		
		DeleteObjectAction action = new DeleteObjectAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		verify(mockSession,times(1)).delete(action.getObject());
		verify(mockSession,times(0)).update(action.getObject());
	}
	
	@Test
	public void createJob(){
		CreateJobAction action = new CreateJobAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		verify(mockSession,times(1)).save((Job)anyObject());
	}
	
	
	@Test
	public void implementationExecutionAborted() {
		ExecutionAbortedAction action = new ExecutionAbortedAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		verify(mockSession,times(1)).update(action.getJob());
		verify(mockSession,times(1)).update(action.getObject());
		assertEquals("190",action.getJob().getStatus());
	}

	
	@Test
	public void revertModifierWhenimplementationExecutionAborted() {
		ExecutionAbortedAction action = new ExecutionAbortedAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		verify(mockSession,times(0)).delete(action.getJob());
		verify(mockSession,times(0)).delete(action.getObject());
		verify(mockSession,times(0)).save((Job)anyObject());
	}
	
	
	@Test
	public void userException(){
		UserExceptionAction action = new UserExceptionAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		assertEquals(action.getEndStatus(),"IMPLEMENTATION");
		assertFalse(action.getEndStatus().equals("ROLLBACK"));
		verify(mockSession,times(1)).update(action.getJob());
		verify(mockSession,times(0)).delete(action.getJob());
		verify(mockSession,times(0)).delete(action.getObject());
		verify(mockSession,times(0)).save((Job)anyObject());
		verify(ams, times(1)).sendJMSMessage((JmsMessage)anyObject());
		assertEquals("194",action.getJob().getStatus());
	}
	
	
	@Test
	public void subsystemNotAvailableExceptionProperlyHandled() {
		SubsystemNotAvailableExceptionAction action = new SubsystemNotAvailableExceptionAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		verifyTechnicalExceptionProperlyHandled(action, mockSession);
		
		verify(action.getActionFactory(),times(1)).setOnHalt(true,"SUBSYSTEM NOT AVAILABLE");
	}
	
	
	
	private void verifyTechnicalExceptionProperlyHandled(AbstractAction action,Session mockSession) {


		verify(mockSession,times(1)).update(action.getJob());
		verify(mockSession,times(0)).delete(action.getJob());
		verify(mockSession,times(0)).delete(action.getObject());
		verify(mockSession,times(0)).save((Job)anyObject());
		verify(ams, times(1)).sendJMSMessage((JmsMessage)anyObject());
		assertEquals("19"+C.WORKFLOW_STATUS_DIGIT_ERROR_PROPERLY_HANDLED,action.getJob().getStatus());
	}
	
	
	@Test
	public void technicalExceptionProperlyHandled(){
		TechnicalExceptionAction action = new TechnicalExceptionAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		verifyTechnicalExceptionProperlyHandled(action, mockSession);
	}

	@Test
	public void technicalExceptionNotProperlyHandled(){
		TechnicalExceptionNotProperlyHandledAction action = new TechnicalExceptionNotProperlyHandledAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		
		action.run();
		
		verify(mockSession,times(1)).update(action.getJob());
		verify(mockSession,times(0)).delete(action.getJob());
		verify(mockSession,times(0)).delete(action.getObject());
		verify(mockSession,times(0)).save((Job)anyObject());
		verify(ams, times(1)).sendJMSMessage((JmsMessage)anyObject());
		assertEquals("19"+C.WORKFLOW_STATUS_DIGIT_ERROR_BAD_ROLLBACK,action.getJob().getStatus());
	}
	

	@Test
	public void retryWhenTransactionNotSucceeds() {
		
		SuccessfulAction action = new SuccessfulAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		doThrow(new RuntimeException("sqlException_1")).doThrow(
				new RuntimeException("sqlException_2")).doNothing().
				when(mockSession).update(object);
		doThrow(new RuntimeException("sqlException_1")).doThrow(
				new RuntimeException("sqlException_2")).doNothing().
				when(mockSession).update(job);
		
		action.run();
		
		verify(mockSession,times(3)).update(object);
		verify(mockSession,times(3)).update(object);
		assertEquals("191",action.getJob().getStatus());
	}
	
	@Test
	public void execRollbackOnly() {
		RollbackOnlyAction action = new RollbackOnlyAction();
		action.setSession(mockSession);
		setCommonProperties(action, "190", "200");
		action.setROLLBACKONLY(true);
		action.run();
		assertEquals("190",action.getJob().getStatus());
		assertFalse(action.getJob().equals("IMPLEMENTATION"));
	}
	
	
	
	class CreateJobAction extends NullAction{
		@Override
		public boolean implementation() {
			toCreate=new Job();
			return true;
		}
	}

	class DeleteObjectAction extends NullAction{
		@Override
		public boolean implementation() {
			DELETEOBJECT=true;
			return true;
		}
	}
	
	class KillAtExitAction extends NullAction{
		@Override
		public boolean implementation() {
			setKILLATEXIT(true);
			return true;
		}
	}
	
	class SuccessfulAction extends NullAction{
		@Override
		public boolean implementation() {
			return true;
		}
	}
	
	class ExecutionAbortedAction extends NullAction{
		@Override
		public boolean implementation() {
			DELETEOBJECT=true;
			setKILLATEXIT(true);
			toCreate=new Job();
			return false;
		}
	}
	
	class UserExceptionAction extends NullAction{
		@Override
		public boolean implementation() {
			DELETEOBJECT=true;
			setKILLATEXIT(true);
			toCreate=new Job();
			endStatus="IMPLEMENTATION";
			throw new UserException(UserExceptionId.INCONSISTENT_PACKAGE,"ERROR","ERROR");
		}
		
		@Override
		public void rollback() {
			endStatus="ROLLBACK";
		}
	}
	
	class SubsystemNotAvailableExceptionAction extends NullAction{
		@Override
		public boolean implementation() throws SubsystemNotAvailableException {
			throw new SubsystemNotAvailableException("SUBSYSTEM NOT AVAILABLE");
		}
	}
	
	class TechnicalExceptionAction extends NullAction{
		@Override
		public boolean implementation() {
			DELETEOBJECT=true;
			setKILLATEXIT(true);
			toCreate=new Job();
			throw new RuntimeException("RUNTIME ERROR");
		}
	}
	
	class TechnicalExceptionNotProperlyHandledAction extends NullAction{
		@Override
		public boolean implementation() {
			DELETEOBJECT=true;
			setKILLATEXIT(true);
			toCreate=new Job();
			throw new RuntimeException("RUNTIME ERROR");
		}
		
		@Override
		public void rollback() {
			throw new RuntimeException("rollback RUNTIME ERROR");
		}
	}

	
	
	class RollbackOnlyAction extends NullAction{
		
		@Override
		public boolean implementation() {
			j.setStatus("IMPLEMENTATION");
			return true;
		}
		
		@Override 
		public void rollback() {}
	}
	
}
