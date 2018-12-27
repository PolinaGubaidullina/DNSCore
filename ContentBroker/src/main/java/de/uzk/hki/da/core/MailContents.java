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

package de.uzk.hki.da.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.action.AbstractAction;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.model.User;
import de.uzk.hki.da.service.Mail;
import de.uzk.hki.da.utils.StringUtilities;

/**
 * Encapsulates the content of business code related emails.
 * @author Daniel M. de Oliveira
 * @author Jens Peters
 * @author Thomas Kleinke
 * @author Josef Hammer
 */
public class MailContents {

	private static final String PRESERVATION_SYSTEM_NAME = "DA-NRW";

	private static final Logger logger = LoggerFactory.getLogger(MailContents.class);
	
	private PreservationSystem preservationSystem;
	private Node localNode;
	
	
	/**
	 * @param preservationSystem
	 * @param localNode
	 */
	public MailContents(PreservationSystem preservationSystem,Node localNode){
		if (preservationSystem==null) throw new IllegalArgumentException("preservation system must not be null");
		if (preservationSystem.getAdmin()==null) throw new IllegalArgumentException("preservation system must have an admin");
		if (preservationSystem.getAdmin().getEmailAddress()==null||preservationSystem.getAdmin().getEmailAddress().isEmpty()) throw new IllegalStateException("preservation systems admin must have an email address");
		if (localNode==null) throw new IllegalArgumentException("local node must not be null");
		if (localNode.getAdmin()==null) throw new IllegalArgumentException("local node must have an admin");
		if (localNode.getAdmin().getEmailAddress()==null||localNode.getAdmin().getEmailAddress().isEmpty()) throw new IllegalArgumentException("local nodes admin must have an email address");
		this.preservationSystem = preservationSystem;
		this.localNode = localNode;
	}
	
	private void checkObject(Object object){
		if (object==null) throw new IllegalArgumentException("object must not be null");
		if (object.getIdentifier()==null) throw new IllegalArgumentException("obj identifier must not be null");
		if (object.getContractor()==null) throw new IllegalArgumentException("obj has not contractor");
		if (object.getContractor().getEmailAddress()==null||object.getContractor().getEmailAddress().isEmpty()) throw new IllegalArgumentException("objs contractor has no email adress");
	}
	
	public void informUserAboutPendingDecision(Object obj,String message){
		checkObject(obj);
		
		String subject = "[" + PRESERVATION_SYSTEM_NAME + "] Entscheidung erforderlich für "+obj.getIdentifier();
		String msg = "Bitte treffen Sie eine Entscheidung in der DAWeb-Maske \"Entscheidungsübersicht\"";
		if (!StringUtilities.isNotSet(message)) {
			msg += "\n" + message ;
		}
		List<String> allMailAdrList = obj.getContractor().getAllEmailAdrForContratcor(obj.getContractor().getShort_name());
		
		try {
			this.queueMail(preservationSystem.getAdmin(), obj.getContractor(), allMailAdrList, subject, msg);
		} catch (MessagingException e) {
			logger.error("Sending email problem report for " +  obj.getIdentifier() + " failed");
		}
	}
	
	public void informUserAboutPendingDecision(Object obj){
		checkObject(obj);
		
		String subject = "[" + PRESERVATION_SYSTEM_NAME + "] Entscheidung erforderlich für "+obj.getIdentifier();
		// DANRW-1523: Mailtext korrigiert
		String msg = "Bitte treffen Sie eine Entscheidung in der DAWeb-Maske \"Entscheidungsübersicht\" zu dem Paket mit Identifier " + obj.getIdentifier();
		List<String> allMailAdrList = obj.getContractor().getAllEmailAdrForContratcor(obj.getContractor().getShort_name());
		try {
			this.queueMail(preservationSystem.getAdmin(), obj.getContractor(), allMailAdrList, subject, msg);
		} catch (MessagingException e) {
			logger.error("Sending email problem report for " +  obj.getIdentifier() + " failed");
		}
	}
	
	
	/**
	 * Informs the Node Admin about the problems being found
	 * 
	 * @param logicalPath
	 * @param msg
	 * @author Jens Peters
	 */
	public void auditInformNodeAdmin(Object obj, String msg) {
		// send Mail to Admin with Package in Error
		checkObject(obj);

		String subject = "[" + PRESERVATION_SYSTEM_NAME + "] Problem Report für " + obj.getIdentifier();
		try {
			this.queueMailNodeAdmin(preservationSystem.getAdmin(), localNode.getAdmin(), subject, msg);
		} catch (MessagingException e) {
			logger.error("Sending email problem report for " +  obj.getIdentifier() + " failed");
		}
	}
	
	
	/**
	 * @author Jens Peters
	 * @param email
	 * @param objectIdentifier
	 * @param csn
	 */
	public void retrievalReport(Object object){
		checkObject(object);
		
		String subject = "[" + PRESERVATION_SYSTEM_NAME + "] Retrieval Report für " + object.getIdentifier();
		String msg = "Ihr angefordertes Objekt mit dem Identifier \""+ object.getIdentifier() + "\" (Originalname " + object.getOrig_name() + ") wurde unter Ihrem Outgoing Ordner unter " 
				+ object.getContractor().getShort_name() + "/outgoing/"+ object.getIdentifier() +".tar abgelegt und steht jetzt"
				+ " zum Retrieval bereit!\n\n";
		List<String> allMailAdrList = object.getContractor().getAllEmailAdrForContratcor(object.getContractor().getShort_name());
		try {
			this.queueMail(preservationSystem.getAdmin(),object.getContractor(), allMailAdrList, subject, msg);
		} catch (MessagingException e) {
			logger.error("Sending email retrieval reciept for " + object.getIdentifier() + "failed", e);
		}
	}
	
	/**
	 * @author Jens Peters
	 * @param email
	 * @param objectIdentifier
	 * @param csn
	 */
	public void deleteObjectFromWorklfow(Object object){
		checkObject(object);
		String delta = "";
		if (object.isDelta()) delta = "Delta-"; 
		String subject = "[" + PRESERVATION_SYSTEM_NAME + "] Entfernung von SIP aus Workflow für " + object.getIdentifier();
		String msg = "Ihr abgegebenes SIP " + delta + "Paket mit dem Namen \""+ object.getLatestPackage().getContainerName() + "\" wurde aus der Verarbeitungswarteschlange "+
				"entfernt. Die Datei kann so nicht vom DNS verarbeitet werden. Korrigieren Sie ggfs. das Paket und bitte versuchen "
						+ "Sie eine erneute Ablieferung. Das Paket wurde nicht archiviert. ";
		List<String> allMailAdrList = object.getContractor().getAllEmailAdrForContratcor(object.getContractor().getShort_name());
		try {
			this.queueMail(preservationSystem.getAdmin(), object.getContractor(), allMailAdrList, subject, msg);
		} catch (MessagingException e) {
			logger.error("Sending email retrieval reciept for " + object.getIdentifier() + "failed", e);
		}
	}
	
	
	
	/**
	 * @author Jens Peters
	 * Sends an Reciept to the deliverer of package
	 */
	public boolean sendReciept(Job job, Object obj){
		checkObject(obj);
		
		String subject;
		String msg;
		if (obj.isDelta())
		{
			subject = "[" + PRESERVATION_SYSTEM_NAME + "] Einlieferungsbeleg für Ihr Delta zum Objekt " + obj.getIdentifier();
			msg = "Ihrem archivierten Objekt mit dem Identifier " + obj.getIdentifier() + " und der URN " + obj.getUrn() +
					" wurde erfolgreich ein Delta mit dem Paketnamen \"" + obj.getLatestPackage().getContainerName() + "\" hinzugefügt.";
		}
		else
		{
			subject = "[" + PRESERVATION_SYSTEM_NAME + "] Einlieferungsbeleg für " + obj.getIdentifier();
			msg = "Ihr eingeliefertes Paket mit dem Namen \""+ obj.getLatestPackage().getContainerName() + "\" wurde erfolgreich im DA-NRW archiviert.\n\n" +
			"Identifier: " + obj.getIdentifier() + "\n" +
			"URN: " + obj.getUrn();
		}

		List<String> allMailAdrList = obj.getContractor().getAllEmailAdrForContratcor(obj.getContractor().getShort_name());
		
		logger.debug(subject);
		logger.debug("");
		logger.debug(msg);
		
		try {
			this.queueMail(preservationSystem.getAdmin(), obj.getContractor(), allMailAdrList, subject, msg);
		} catch (MessagingException e) {
			logger.error("Sending email reciept for " + obj.getIdentifier() + " failed",e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * @author Polina Gubaidullina
	 * Sends Email to the User
	 */
	public boolean missingReferences(Object obj, List<String> missingReferences){
		checkObject(obj);
		
		String subject = "[" + PRESERVATION_SYSTEM_NAME + "] Fehlende Referenzen in den Metadaten des Objekts " + obj.getIdentifier();
		String msg = "Ihr archiviertes Objekt mit dem Identifier " + obj.getIdentifier() + " und der URN " + obj.getUrn() +
					" ist nicht konsistent. SIP Paketname ist: " + obj.getLatestPackage().getContainerName() +". Folgende Files sind nicht in den mitgelieferten Metadaten referenziert: "
					+ missingReferences+". Die Verarbeitung findet dennoch statt.";
		
		List<String> allMailAdrList = obj.getContractor().getAllEmailAdrForContratcor(obj.getContractor().getShort_name());
		
		logger.info(subject);
		logger.info("");
		logger.info(msg);
		
		try {
			this.queueMail(preservationSystem.getAdmin(), obj.getContractor(), allMailAdrList, subject, msg);
		} catch (MessagingException e) {
			logger.error("Sending email reciept for " + obj.getIdentifier() + " failed",e);
			return false;
		}
		
		return true;
	}

	/**
	 * Creates report about the error
	 * Sends Email to the Admin
	 * @author Jpeters
	 */
	public void abstractActionCreateAdminReport(Exception e,Object object,AbstractAction action) {

		String errorStatus = action.getStartStatus().substring(0,action.getStartStatus().length()-1) + "1";
		String email = localNode.getAdmin().getEmailAddress();
		String subject = "[" + PRESERVATION_SYSTEM_NAME + "] Fehlerreport für " + object.getIdentifier() + " : Status (" + errorStatus + ")" ;
		String msg = e.getMessage();
		msg +="\n\n";
		StringWriter s = new StringWriter();
	    e.printStackTrace(new PrintWriter(s));
	    msg += s.toString();
		
		if (email!=null && !email.equals("")) {
		try {
			this.queueMailNodeAdmin(preservationSystem.getAdmin(), localNode.getAdmin(), subject, msg);
		} catch (MessagingException ex) {
			logger.error("Sending email reciept for " + object.getIdentifier() + " failed",ex);
		}
		} else logger.info(localNode.getName() + " has no valid email address!");
	}
	
	
	
	/**
	 * Creates report about the error
	 * Sends e-mail to the User
	 * @author Thomas Kleinke
	 */
	public void userExceptionCreateUserReport(UserExceptionManager uem,UserException e,Object object) {
		if (object==null) throw new IllegalArgumentException("object must not be null");
		
		List<String> allMailAdrList = object.getContractor().getAllEmailAdrForContratcor(object.getContractor().getShort_name());
		System.out.println("allMailAdrList: " + allMailAdrList);
		
		
		String subject = "[" + PRESERVATION_SYSTEM_NAME + "] Fehlerreport für " + object.getIdentifier();
		String message = uem.getMessage(e.getUserExceptionId());
		
		message = message.replace("%OBJECT_IDENTIFIER", object.getIdentifier())
			 .replace("%CONTAINER_NAME", object.getLatestPackage().getContainerName())
			 .replace("%ERROR_INFO", e.getErrorInfo());
				
		logger.debug("Sending mail to List: " + allMailAdrList + "\n" + subject + "\n" + message);
		
		if (allMailAdrList.size() == 0) {
			logger.warn(object.getContractor().getShort_name() + " has no valid email address!");		
			return;
		}
		try {
			this.queueMail(preservationSystem.getAdmin(), object.getContractor(), allMailAdrList, subject, message);
		} catch (MessagingException ex) {
			logger.error("Sending email reciept for " + object.getIdentifier() + " failed", ex);
		}
	}	

	protected void queueMail(User fromUser, User contractor, List<String> sendMailAdrList,
			String subject, String message) throws MessagingException{
		String fromAddress = fromUser.getEmailAddress();
		Boolean isReport = contractor.isMailsPooled();
		Mail.queueMail(localNode.getName(), fromAddress, sendMailAdrList, subject, message, isReport);
		
	}

	protected void queueMailNodeAdmin(User fromUser, User toUser, String subject, String message) throws MessagingException{
		String fromAddress = fromUser.getEmailAddress();
		String toAddress = toUser.getEmailAddress();
		Boolean isReport = toUser.isMailsPooled();
		
		Mail.queueMailNodeAdmin(localNode.getName(), fromAddress, toAddress, subject, message, isReport);
	}
}
