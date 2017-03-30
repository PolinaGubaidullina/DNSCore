package daweb3
import java.text.SimpleDateFormat;


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
 * AIP
 *@Author Jens Peters
 * @Author Scuy
 */
class Object {

    static constraints = {
		dynamic_nondisclosure_limit nullable : true
		static_nondisclosure_limit nullable: true
    }
	
	static hasMany = [packages: Package]
    
    static mapping = {
		table 'objects'
		version false
		id column:'data_pk'
		user column: 'user_id'
		packages joinTable: [key: 'objects_data_pk', column: 'packages_id']
		createdAt column: 'created_at'
		modifiedAt column: 'modified_at'
    }	
	int id
	String urn
	String identifier
	User user
	String origName
	int object_state
	int published_flag
	
	// due to now unused iRODS functions these fields are still strings, should be 
	// refactored to normal Dates
	Date createdAt
	Date modifiedAt
	
	Date static_nondisclosure_limit
	String dynamic_nondisclosure_limit
	Date last_checked
	String original_formats
	String most_recent_formats;
	String most_recent_secondary_attributes
	Boolean ddb_exclusion
	
	
	String getIdAsString() {
		return id.toString();
	}
	/**
	 * retrieves a status code based on the object_state
	 * 0 means normal (grey)
	 * 1 means error (red)
	 * 2 means currently running (yellow)
	 * https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/object_model.md
	 */
	def getStatusCode() {

		if ( object_state == 50 ) return 2
		if ( object_state == 60 ) return 2
		if ( object_state == 51 ) return 1
		if (object_state == 100 ) return 0
		return 1;
	}
	
	def getPublicPresLink() {
		
		if (identifier!=null && identifier!="" && identifier!="NULL") {
			def grailsApplication = new Object().domainClass.grailsApplication
			def ctx = grailsApplication.mainContext
			def config = grailsApplication.config
			
			def preslink = config.fedora.urlPrefix + "danrw:"+ identifier
			return preslink
		}
		return ""
	}
	def getInstPresLink() {
		
		if (identifier!=null && identifier!="" && identifier!="NULL") {
			def grailsApplication = new Object().domainClass.grailsApplication
			def ctx = grailsApplication.mainContext
			def config = grailsApplication.config
			
			def preslink = config.fedora.urlPrefix + "danrw-closed:"+ identifier
			return preslink
		}
		return ""
	}
	
	/**
	 * Ask for Workflow state
	 * @author jpeters
	 * @return
	 */
	
	boolean isInWorkflowButton() {
		if (object_state==50) {
			return true;
		}
		return false;
	}
	
	String getTextualObjectState() {
		String state = (String)object_state
		if (object_state==100) {
			state = "archived"
		} else if (object_state==50) {
			state = "Object is in transient state"
		} else {
			state = "archived - but check needed"
		}
		return state;
	}
	
	def getFormattedUrn() {
		if (urn!=null && urn!="" && urn!="NULL") {
			def formurn = urn.replaceAll(~"\\+",":")
			return formurn
		}
		return ""
	}
	
	String toString() {
		return "Objekt " + getFormattedUrn()
	}

	def getFormattedCreatedDate() {
		
	if (createdAt!=null) {
		String sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(createdAt)
		return sdf
	}
	return "";
	}

	def getFormattedModifiedDate() {
	
	if (modifiedAt) {
		String sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(modifiedAt)
		return sdf
	}
	return ""
	
	}
	
	static Date convertDateIntoDate(String sDate) {

		if (sDate!=null && sDate!="") {
		try {
				SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
				Date dt = df.parse(sDate)
				return dt
			} catch (Exception ex) { 
				return null;
			}
		}
		return null;
	}

	static String convertDateIntoStringDate(String sDate) {

		if (sDate!=null && sDate!="") {
		try {
				SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
				Date dt = df.parse(sDate)
				return String.valueOf(Math.round(dt.getTime()/1000L))
			} catch (Exception ex) { 
				return null;
			}
		}
		return null;
	}
}
