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
package daweb3

import org.apache.commons.logging.LogFactory


/**
 * Create a queue entry in dedicated state
 * @author Jens Peters, Sebastian Cuy
 *
 */

class QueueUtils {

	/**
	 * Creates a job with status on responsibleNode
	 * for a selected object. Also sets the object_state to 50
	 * to indicate it is in workflow state.
	 *
	 * @param objectimport groovy.util.logging.Log4j
	 * @param status
	 * @param responsibleNodeName The name of the node which gets assigned responsibility for executing the job.
	 * @throws Exception if entry could not be created.
	 * @throws Exception if object state could not be updated.
	 * @throws IllegalArgumentException if object is null.
	 * @author Jens Petersimport groovy.util.logging.Log4j
	 * @author Daniel M. de Oliveira
	 *
	 */
	static final LOG = LogFactory.getLog(this)
	
	
	String createJob( daweb3.Object object, String status, responsibleNodeName, additionalQuestion) {
		
		if (object == null) throw new IllegalArgumentException ( "Object is not valid" )
		if (responsibleNodeName == null) throw new IllegalArgumentException("responsibleNodeName must not be null")
		object.setObjectState(50)
		LOG.debug("Create job object.user.shortName: " + object.user.shortName)
	
		def list = QueueEntry.findByObj(object)
		if (list != null) throw new RuntimeException ("Es gibt bereits einen laufenden Arbeitsauftrag für dieses Objekt");
		
		def job = new QueueEntry()
		job.setStatus(status)
		job.setObj(object);
		if (additionalQuestion!=null && !additionalQuestion.equals(""))
			job.setQuestion(additionalQuestion)
			
		job.setCreatedAt(new Date())
		job.setModifiedAt(new Date())
		
		job.setInitialNode(responsibleNodeName)
		
					 
		def errorMsg = ""
		if( !object.save(flush: true) ) {	
			object.errors.each { errorMsg += it }
			LOG.error "Saving object failed " + errorMsg
			throw new Exception(errorMsg)
		}
		errorMsg = ""
		if( !job.save(flush: true)  ) {
			job.errors.each { errorMsg += it }
			LOG.error "Saving job failed " + errorMsg
			throw new Exception(errorMsg)
		}
	}
	
	String createJob( daweb3.Object object, status, responsibleNodeName) {
		return createJob(object,status,responsibleNodeName, "" )	
	}
	/**
	 * Modifies a job and sets it to new Workflow state
	 * @param id 
	 * @param status
	 * @author Jens Peters
	 */
	String modifyJob (String id, String newStatus, String additionalAnswer) {
		def queueEntryInstance = QueueEntry.get(Integer.parseInt(id))
		if (queueEntryInstance) {
			def status = queueEntryInstance.getStatus()
			if (additionalAnswer!=null && additionalAnswer!="")
			queueEntryInstance.answer = additionalAnswer
			queueEntryInstance.setStatus(newStatus)
			queueEntryInstance.modifiedAt = new Date()
			def errorMsg = ""
			if( !queueEntryInstance.save(flush: true)	 ) {
				queueEntryInstance.errors.each { 
					errorMsg += it 
 					LOG.error(it)
				}
				throw new Exception(errorMsg)
			}
			return "Paket "+ id  + " (Identifier: " +  queueEntryInstance.obj.identifier + ")" +"  in Status: " + newStatus + " " + additionalAnswer 
		} else return "Paket nicht gefunden!"
	}
	/**
	 * Modifies a job and sets it to new Workflow state
	 * @param id
	 * @param status
	 * @author Jens Peters
	 */
	String modifyJob (String id, newStatus) {
		return modifyJob (id, newStatus, "") 
	
	}
}
