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
package de.uzk.hki.da.model;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.service.HibernateUtil;

/**
 * @author Daniel M. de Oliveira
 */
public class JobNamedQueryDAO {

	private static final Logger logger = LoggerFactory.getLogger(JobNamedQueryDAO.class);
	
	/**
	 * XXX locking synchronized, against itself and against get object need audit
	 * 
	 * IMPORTANT NOTE: Fetch objects from queue opens a new session.
	 *
	 * @param status the status
	 * @param workingStatus typically a numerical value with three digits, stored as string.
	 * working status third digit typically is 2 by convention.
	 * @param node the node
	 * @return the job
	 * @author Daniel M. de Oliveira
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public Job fetchJobFromQueue(String status, String workingStatus, Node node) {
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		logger.trace("Fetch job for node name " + node.getName());
		List<Job> joblist=null;
		try{
			
			joblist = session
					.createQuery("SELECT j FROM Job j LEFT JOIN j.obj as o where j.status=?1 and "
							+ "j.responsibleNodeName=?2 and o.object_state IN (100, 50, 40) and o.orig_name!=?3 order by j.modifiedAt asc ")
					.setParameter("1", status).setParameter("2", node.getName()).setParameter("3","integrationTest").setCacheable(false).setMaxResults(1).list();

			if ((joblist == null) || (joblist.isEmpty())){
				logger.trace("no job found for status {}.",status);
				session.close();
				return null;
			}
			
			Job job = joblist.get(0);
			
			// To circumvent lazy initialization issues
			for (ConversionInstruction ci:job.getConversion_instructions()){}
			for (Job j:job.getChildren()){}
			for (Package p:job.getObject().getPackages()){
				for (DAFile f:p.getFiles()){}
				for (Event e:p.getEvents()){}
				for (Copy copy:p.getCopies()) {}
			}
			//-
			
			job.setStatus(workingStatus);
			job.setModifiedAt(new Date());
			session.merge(job);
			session.getTransaction().commit();
			session.close();
			
			logger.debug("Fetched job of object "+job.getObject().getIdentifier()+" +. Set job status to "+job.getStatus()+".");
		
		}catch(Exception e){
			session.close();
			logger.error("Caught error in fetchJobFromQueue");
			
			throw new RuntimeException(e.getMessage(), e);
		}
		return joblist.get(0);
	}
	
	public void updateJobStatus(Job j,String status) {
		j.setStatus(status);
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		session.update(j);
		session.getTransaction().commit();
		session.close();
	}
	
}
