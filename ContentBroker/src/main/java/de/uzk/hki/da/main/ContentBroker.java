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

package de.uzk.hki.da.main;

import java.io.IOException;
import java.util.List;

import org.apache.activemq.xbean.XBeanBrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;

import de.uzk.hki.da.action.ActionDescription;
import de.uzk.hki.da.action.ActionFactory;
import de.uzk.hki.da.action.ActionInformation;
import de.uzk.hki.da.action.Controller;
import de.uzk.hki.da.service.HibernateUtil;
import de.uzk.hki.da.service.JmsMessageServiceHandler;
import sun.misc.Signal;
import sun.misc.SignalHandler;




/**
 * ContentBroker is the main application for DNSCore, serving all needed actions 
 * for SIP to AIP and PIP transformation
 * 
 * @author: Daniel M. de Oliveira
 * @author: Sebastian Cuy
 * @author: Jens Peters
 * @author: Thomas Kleinke
 * @author: Polina Gubaidullina
 * @author: Martin Fischer
 * @author: Johanna Puhl
 * @author: Lisa Rau
 * @author: Chris Weitz
 */
public class ContentBroker {
	
	// set path to logback xml before anything else happens
	static { 
		System.setProperty("logback.configurationFile", "conf/logback.xml");
	}

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger("de.uzk.hki.da.core");

	/** The task executor. */
	private TaskExecutor taskExecutor;
	
	/** The action factory. */
	private ActionFactory actionFactory;
	
	/** The action factory. */
	private ActionInformation actionInformation;
	
	/** The controller. */
	private Controller controller;
	
	/** The server socket number. */
	private int serverSocketNumber;
	
	/** The MqBroker */
	private JmsMessageServiceHandler jmsMessageServiceHandler ;
	

	private XBeanBrokerService mqBroker;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main( String[] args) throws IOException {		

		if ((args.length>0)&&(args[0].equals("createSchema"))){
			HibernateUtil.createSchema("conf/hibernateCentralDB.cfg.xml");
			System.exit(0);
		}
		
		if ((args.length>0)&&(args[0].equals("diagnostics"))){
			if (Diagnostics.run()!=0)
				System.exit(1);
			else
				System.exit(0);
		}
		
		if (!((args.length>0)&&(args[0].equals("suppress_diagnostics")))){
			if (Diagnostics.run()!=0)
			{
				logger.error("Diagnostics has detected one or more pre-conditions for running the ContentBroker have not been met.");
				logger.error("For more information run \"java -jar ContentBroker.jar diagnostics.\"");
				System.exit(1);
			}
		}
		
		setUpContentBroker();
	}
	
	private static void setUpContentBroker(){
		
		logger.info("Starting ContentBroker ...");

		logger.info("Setting up HibernateUtil ...");
		try {
			HibernateUtil.init("conf/hibernateCentralDB.cfg.xml");
		} catch (Exception e) {
			logger.error("Exception in main!",e);
			System.exit(1);
		}
		
		logger.info("Setting up Spring application context ...");
		try {
			@SuppressWarnings("resource")
			AbstractApplicationContext context =
			new FileSystemXmlApplicationContext("conf/beans.xml");
			context.registerShutdownHook();
			logger.info("ContentBroker is up and running");
		} catch (Exception e) {
			logger.error("Exception in main!",e);
			System.exit(1);
		}

	}
	
	
	
	
	/**
	 * Instantiates a new content broker.
	 */
	public ContentBroker() {
		
	}
	
	
	/**
	 * to be called after setters.
	 */
	public void init(){
		controller = new Controller("localhost",
				getServerSocketNumber(),
				actionFactory, actionInformation, mqBroker, jmsMessageServiceHandler );
		(new Thread(controller)).start();
		
 
		// Runtime.addShutdownHook doens't work properly. SIGKILL is -9 it is used by vm and can not be used by developer. 
		//SIGTERM is -15
		Signal.handle(new Signal("TERM"), new SignalHandler () {
		      public void handle(Signal sig) {
		    	  logger.info("ContentBrocker is killed by SIGTERM try to execute a 'bit gracefully shutdown'");
		        actionFactory.setPaused(true);
		        List<ActionDescription> tmpList=actionFactory.getActionRegistry().getCurrentActionDescriptions();

		        for(int maxTry=25;tmpList.size()>0 &&maxTry>=0;maxTry-=5){
		        	logger.info("Give a chance for running actions ("+tmpList.size()+") to exit themself");
		        	for(int i=0;i<tmpList.size();i++){
		        		ActionDescription ac=tmpList.get(i);
		        		logger.info("Give a chance for Action("+i+"): "+" ActionType: "+ac.getActionType()+"("+ac.getStartStatus()+"-"+ac.getEndStatus()+") jobId: "+ac.getJobId()+" "+ac.getDescription()+" package: "+ac.getPackageId());
		        	}
		        	tmpList=actionFactory.getActionRegistry().getCurrentActionDescriptions();
		        	try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		        }
		        logger.info("Time is out for a 'bit gracefully shutdown', "+actionFactory.getActionRegistry().getCurrentActionDescriptions().size()+" actions are still running");
		        System.exit(0);
		      }
		    });
		
	}
	
	

	public JmsMessageServiceHandler getJmsMessageService() {
		return jmsMessageServiceHandler;
	}

	public void setJmsMessageServiceHandler(JmsMessageServiceHandler jmsMessageService) {
		this.jmsMessageServiceHandler = jmsMessageService;
	}

	/**
	 * Gets the task executor.
	 *
	 * @return the task executor
	 */
	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}


	/**
	 * Sets the task executor.
	 *
	 * @param executor the new task executor
	 */
	public void setTaskExecutor(TaskExecutor executor) {
		this.taskExecutor = executor;
	}


	/**
	 * Gets the action factory.
	 *
	 * @return the action factory
	 */
	public ActionFactory getActionFactory() {
		return actionFactory;
	}


	/**
	 * Sets the action factory.
	 *
	 * @param actionFactory the new action factory
	 */
	public void setActionFactory(ActionFactory actionFactory) {
		this.actionFactory = actionFactory;
	}

	/**
	 * Gets the server socket number.
	 *
	 * @return the server socket number
	 */
	public int getServerSocketNumber() {
		return serverSocketNumber;
	}


	/**
	 * Sets the server socket number.
	 *
	 * @param serverSocketNumber the new server socket number
	 */
	public void setServerSocketNumber(int serverSocketNumber) {
		if (serverSocketNumber<=0)
			throw new RuntimeException("serverSocketNumber must be a value greater 0");
		
		this.serverSocketNumber = serverSocketNumber;
	}


	/**
	 * @param mqBroker the mqBroker to set
	 */
	public void setMqBroker(XBeanBrokerService mqBroker) {
		this.mqBroker = mqBroker;
	}


	/**
	 * @return the mqBroker
	 */
	public XBeanBrokerService getMqBroker() {
		return mqBroker;
	}	


	/**
	 * @return the actionInformation
	 */
	public ActionInformation getActionInformation() {
		return actionInformation;
	}


	/**
	 * @param actionInformation the actionInformation to set
	 */
	public void setActionInformation(ActionInformation actionInformation) {
		this.actionInformation = actionInformation;
	}

	
	
}
