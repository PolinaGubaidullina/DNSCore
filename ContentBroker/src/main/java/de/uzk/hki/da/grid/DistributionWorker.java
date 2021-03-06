package de.uzk.hki.da.grid;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.MDC;

import de.uzk.hki.da.core.Worker;
import de.uzk.hki.da.model.CopyJob;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.service.HibernateUtil;

/**
 * Distributes secondary copies
 * @author Jens Peters
 */
public class DistributionWorker extends Worker {

	private Node node;
	private int localNodeId;
	private PreservationSystem pSystem;
	private JobExecutor jobExecutor;
	
	public void init(){
		node = new Node(); 	
		node.setId(localNodeId);
		setpSystem(new PreservationSystem()); getPSystem().setId(1);
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		session.refresh(getPSystem());
		session.refresh(node);
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public void scheduleTaskImplementation() {
		if (jobExecutor==null) {
			logger.error("jobExecutor is null");
			throw new RuntimeException("jobExecutor is null");
		}
		try {
			CopyJob cj= fetchSynchronizeJob(node.getIdentifier());
			if (cj!= null) {
				logger.debug("Try to sync "+cj.getSource()+" & "+cj.getDest_name());
				if (jobExecutor.execute(cj)) {
					logger.info("successfully executed syncing of " + cj.getSource() + " to " + cj.getDest_name() );
					deleteCopyJob(cj);
				}
				else {
					logger.error("error executing syncing of " + cj.getSource() + " to " + cj.getDest_name() );
					updateCopyJob(cj);
				}
			} else logger.debug("fetchSynchronizeJob() returns null for CopyJob request for "+ node.getIdentifier() +"!");
		} catch (Exception e) {			
			logger.error("execute CopyJob Worker caused exception " + e.getCause(), e);
		}
	}
	
	/** 
	 * 
	 * @return the next synchronizeJob
	 * @author Jens Peters
	 */
	private synchronized CopyJob fetchSynchronizeJob(String source_name) {
		
		Session session = null;
		try {
			session = HibernateUtil.openSession();
			session.beginTransaction();
			@SuppressWarnings("rawtypes")
			List l = null;
			l = session.createSQLQuery("select id from CopyJob c where c.source_node_identifier = ?1 "
			+ "order by c.last_tried asc NULLS FIRST")
							.setParameter("1",source_name)
							.setReadOnly(true).list();
	         
			@SuppressWarnings("rawtypes")
			List k = null;
			k = session.createQuery("from CopyJob c where c.id = ?1 ").setParameter("1",l.get(0))
					
					.setReadOnly(true).list();
			
			CopyJob cj = (CopyJob)k.get(0);
			session.close();	
			return cj;
		
		} catch (IndexOutOfBoundsException e){
			if (session!=null) session.close();
			return null;
		}
	}
	private void updateCopyJob(CopyJob job) {
		job.setLast_tried(new Date());
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		session.update(job);
		session.getTransaction().commit();
		session.close();
	}
	
	private void deleteCopyJob(CopyJob job) {
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		session.delete(job);
		session.getTransaction().commit();
		session.close();
	}
	
	@Override
	public void setMDC() {
		MDC.put(WORKER_ID, "distribution");
	}
	public PreservationSystem getPSystem() {
		return pSystem;
	}

	public void setpSystem(PreservationSystem pSystem) {
		this.pSystem = pSystem;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public int getLocalNodeId() {
		return localNodeId;
	}
	public void setLocalNodeId(int localNodeId) {
		this.localNodeId = localNodeId;
	}
	public JobExecutor getJobExecutor() {
		return jobExecutor;
	}
	public void setJobExecutor(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}

}
