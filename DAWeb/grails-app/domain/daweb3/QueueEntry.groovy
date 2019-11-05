package daweb3
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
/**
 * SIP to AIP or AIP - DIP monitoring. 
@Author Jens Peters
@Author Scuy
*/
import java.text.SimpleDateFormat;
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType

class QueueEntry {
	
	private static final String WORKFLOW_STATUS_DIGIT_WAITING_FOR_DECISION="645";  
	private static final String WORKFLOW_STATUS_DIGIT_USER_ERROR="4";
	private static final String WORKFLOW_STATUS_DIGIT_WAITING="0";
	private static final String WORKFLOW_STATUS_DIGIT_WORKING="2";
	private static final String WORKFLOW_STATUS_DIGIT_ERROR_PROPERLY_HANDLED = "1";
	private static final String WORKFLOW_STATUS_DIGIT_ERROR_BAD_ROLLBACK = "3";
	private static final String WORKFLOW_STATUS_DIGIT_ERROR_MODEL_INCONSISTENT = "5";
	private static final String WORKFLOW_STATUS_DIGIT_ERROR_PRECONDITIONS_NOT_MET = "6";
	private static final String WORKFLOW_STATUS_DIGIT_ERROR_BAD_CONFIGURATION = "7";
	private static final String WORKFLOW_STATUS_DIGIT_UP_TO_ROLLBACK = "8";
	
	int id
	String status
	String initialNode
	Date createdAt
	Date modifiedAt
	Object obj
	String question
	String answer
	String errorText

    static constraints = {
		status(nullable:false)
		createdAt(nullable:true)
		modifiedAt(nullable:true)
		question(nullable:true)
		answer(nullable:true)
		errorText(nullable:true)
	}
	
	static mapping = {
		table 'queue'
		version false
		id column: 'id'
		obj column: 'objects_id'
		// necessary because dateCreated and dateModified seem to be reserved by grails
		createdAt column: 'created_at'
		modifiedAt column: 'modified_at'
		errorText column: 'error_text'
	}
	
	static List getAllQueueEntriesForShortNameAndUrn(String shortName, String urn) {
		return createCriteria().list  {
			createAlias('obj', 'o', JoinType.INNER_JOIN.getJoinTypeValue()) //CriteriaSpecification.INNER_JOIN)
			createAlias('o.user', 'user', JoinType.INNER_JOIN) // CriteriaSpecification.INNER_JOIN)
			eq("user.shortName", shortName)
			eq("o.urn", urn)
		}
	}
	
	
	def getFormattedCreatedDate() {
			
		if (createdAt!=null) {
			String sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(createdAt) 
			return sdf
		}
		return "";
		
	}

	def getFormattedModifiedDate() {
		
		if (modifiedAt!=null) {
			String sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(modifiedAt)
			return sdf
		}
		return ""
		
	}
	
	String toString() {
		return "Status (Queue)";
	}
	
	Integer getStatusAsInteger() {
		return Integer.parseInt(status);
	}
	
	/**
	 * 
	 * @return
	 */
	boolean showTrafficLightGreen() {
		def checkfor = [
			WORKFLOW_STATUS_DIGIT_WAITING ,
			WORKFLOW_STATUS_DIGIT_WORKING]
		def ch = status[-1]
		if (checkfor.contains(ch)) return true;
		return false;
	}

	/**
	 * 	
	 * @return
	 */
	boolean showTrafficLightYellow() {
		def checkfor = [
			WORKFLOW_STATUS_DIGIT_WAITING_FOR_DECISION]
		def ch = status
		if (checkfor.contains(ch)) return true;
		return false;
	}

	
	/**
	 * 
	 * @return
	 */
	boolean showTrafficLightRed() {
		def checkfor = [
		WORKFLOW_STATUS_DIGIT_ERROR_BAD_CONFIGURATION ,
		WORKFLOW_STATUS_DIGIT_ERROR_BAD_ROLLBACK,
		WORKFLOW_STATUS_DIGIT_ERROR_MODEL_INCONSISTENT,
		WORKFLOW_STATUS_DIGIT_ERROR_PRECONDITIONS_NOT_MET,
		WORKFLOW_STATUS_DIGIT_USER_ERROR,
		WORKFLOW_STATUS_DIGIT_ERROR_PROPERLY_HANDLED]	
		def ch = status[-1]
		if (checkfor.contains(ch)) return true;
		return false;
	}
	
	/**
	 * @author jpeters
	 * Show Deletion button
	 * @return
	 */
	boolean showDeletionButton() {
		def checkfor = [
			WORKFLOW_STATUS_DIGIT_ERROR_BAD_ROLLBACK ,
			WORKFLOW_STATUS_DIGIT_USER_ERROR,
			WORKFLOW_STATUS_DIGIT_ERROR_PRECONDITIONS_NOT_MET,
			WORKFLOW_STATUS_DIGIT_ERROR_PROPERLY_HANDLED]
		def ch = status[-1]
		if (checkfor.contains(ch) && getStatusAsInteger()<407) return true;
		return false;
	}
	
	/**
	 * Show RecoverButton on workflow items
	 * @author jpeters 
	 * @return
	 */
	
	boolean showRecoverButton() {
		def checkfor = [
			WORKFLOW_STATUS_DIGIT_ERROR_BAD_ROLLBACK,
			WORKFLOW_STATUS_DIGIT_ERROR_MODEL_INCONSISTENT,
			WORKFLOW_STATUS_DIGIT_ERROR_PRECONDITIONS_NOT_MET]
		def ch = status[-1]
		if (checkfor.contains(ch)) {
			if (getStatusAsInteger()>=123 & getStatusAsInteger()<=357) return true;
		}
		return false;
	}
	/**
	 * @author jpeters 
	 * Show Retry Button if state is 1
	 * @return
	 */
	
	boolean showRetryButton() {
		def checkfor = [
			WORKFLOW_STATUS_DIGIT_ERROR_PROPERLY_HANDLED,
			WORKFLOW_STATUS_DIGIT_ERROR_BAD_CONFIGURATION,
			WORKFLOW_STATUS_DIGIT_ERROR_MODEL_INCONSISTENT]
		def ch = status[-1]
		if (checkfor.contains(ch)) {
			return true;
		}
		return false;
	}
	
	String getIdAsString(){
		return id.toString()
	}
	
	/**
	 * @author jpeters shows recover button after some time (48 hours) and state < 401
	 * 
	 */
	
	boolean showRecoverButtonAfterSomeTime(){
		def checkfor = [WORKFLOW_STATUS_DIGIT_WORKING]
		def ch = status[-1]
		if (checkfor.contains(ch)) {
			if (modifiedAt !=null) {
				long diff = new Date().getTime() - modifiedAt.getTime();
				if (diff > 2 * 24 * 60 * 60 * 1000 && getStatusAsInteger()<=400) {
					return true;
				}
			}
		}
		return false;
	}
	
	String getInformation() {
			if (getStatusAsInteger()<440) {
					def checkfor = [WORKFLOW_STATUS_DIGIT_WORKING,WORKFLOW_STATUS_DIGIT_WAITING]
					def ch = status[-1]
					if (checkfor.contains(ch)) {
						return "arbeitend (" + status +")"
					} else return "fehlerhaft (" + status +")"
			} 
			if (getStatusAsInteger()>=440 && getStatusAsInteger()<500 ) {
				def checkfor = [WORKFLOW_STATUS_DIGIT_WORKING,WORKFLOW_STATUS_DIGIT_WAITING]
				def ch = status[-1]
				if (checkfor.contains(ch)) {
					return "repliziere (" + status +")"
				} else return "Replikation fehlerhaft (" + status +")"
		}
		
			if (getStatusAsInteger()>500 && getStatusAsInteger()<600) {
					def checkfor = [WORKFLOW_STATUS_DIGIT_WORKING,WORKFLOW_STATUS_DIGIT_WAITING]
					def ch = status[-1]
					if (checkfor.contains(ch)) {
						return "archiviert. Verarbeite PIP (" + status +")"
					} else return "archiviert. Fehler beim PIP (" + status +")"
			}
			if (getStatusAsInteger()>640 && getStatusAsInteger()<=645 ) {
				return "Warten auf Rückfrage (" + status +")"
			}
			if (getStatusAsInteger()>=900 && getStatusAsInteger()<=952 ) {
				return "Retrieval (" + status +")"
			}
			return status
	}
}
