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

package de.uzk.hki.da.core;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.xbean.XBeanBrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Controls and coordinates the work of the action factory and its associate
 * classes. Server is now using NIO
 * 
 * @author Daniel M. de Oliveira
 * @author Jens Peters
 * 
 */
public class Controller implements Runnable {

	static final Logger logger = LoggerFactory.getLogger(Controller.class);

	private ActionFactory actionFactory;
	private ActionRegistry actionRegistry;

	private int socketNumber;
	private String serverName;

	private XBeanBrokerService mqBroker;
	private ActiveMQConnectionFactory mqConnectionFactory;
	
 	public Controller(String serverName, int socketNumber,
			ActionFactory actionFactory, ActionRegistry actionRegistry, XBeanBrokerService mqBroker, ActiveMQConnectionFactory mqConnectionFactory) {

		this.serverName = serverName;
		this.actionRegistry = actionRegistry;
		this.socketNumber = socketNumber;
		this.actionFactory = actionFactory;
		this.mqBroker = mqBroker;
		this.mqConnectionFactory = mqConnectionFactory;
	}

 	/**
 	 * (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 * @Author Jens Peters
 	 */
	@Override
	public void run() {
		try {
			if (mqBroker==null) {
				logger.error("");
				return;
			}
			logger.debug("starting JMS -Service at: " + serverName + " "+ socketNumber);
			mqBroker.start();
			logger.debug("MQ-Broker is started: " + mqBroker.isStarted());
			
			reloadLoggers();
            List<ActionDescription> list = null;
            for (;;) {
            	Connection connection = mqConnectionFactory.createConnection();
                connection.start();
                
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination toServer = session.createQueue("CB.SYSTEM");
                Destination toClient = session.createQueue("CB.CLIENT");
                
            	MessageProducer producer = session.createProducer(toClient);
            	producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            	 MessageConsumer consumer = session.createConsumer(toServer);
                 
			String messageSend = "";
            Message messageRecieve = consumer.receive(1000);
            
            if (messageRecieve instanceof TextMessage) {
            	TextMessage textMessage = (TextMessage) messageRecieve;
                String command = textMessage.getText();
                if (!command.equals("")) logger.debug("Received: " + command);
                if (command.indexOf("STOP_FACTORY") >= 0) {
					
                	logger.debug("STOPPING FACTORY");
					messageSend = "...STOPPING FACTORY done";
					actionFactory.pause(true);
					
				} else if (command.indexOf("START_FACTORY")>=0) {
					logger.debug("STARTING FACTORY");
					messageSend = "...STARTING FACTORY done";
					actionFactory.pause(false);
				} else if (command.indexOf("SHOW_ACTIONS")>=0){ 
					list = actionRegistry.getCurrentActionDescriptions();
					logger.debug("SHOW_ACTIONS");
					messageSend = "found " + list.size()+ " working actions"; 
					ObjectMessage om = session.createObjectMessage((Serializable) list);
		            producer.send(om);
				} else if (command.indexOf("GRACEFUL_SHUTDOWN")>=0){ 
					list = actionRegistry.getCurrentActionDescriptions();
					actionFactory.pause(true);
					int i = 0;
					while (list.size()>0) {
						String text = "waiting for actions to complete before shut down (" + list.size() +")";
						TextMessage message = session.createTextMessage(text);
	                    producer.send(message);
	                    Thread.sleep(3000);
	                    list = actionRegistry.getCurrentActionDescriptions();
	                    i++;
					}
					String text = "ContentBroker at " + serverName + " exiting now!";
					TextMessage message = session.createTextMessage(text);
                    producer.send(message);
                    Thread.sleep(3000);
                    System.exit(0);
				} 
                if (!messageSend.equals("")) {
                	String text = "Hello Client, this is ContentBroker running at " + serverName;
                	TextMessage messageGreeting = session.createTextMessage(text);
                	TextMessage message = session.createTextMessage(messageSend);
                	
                    producer.send(messageGreeting);
                    producer.send(message);
                }
            }
            consumer.close();
            producer.close();
            session.close();
            connection.close();
			}
		} catch (Exception e) {
			logger.error("Error creating/execution/usage of CB-Controller thread: " + e,e );
		}
			
		}

	private void reloadLoggers() {
		    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		    ContextInitializer ci = new ContextInitializer(loggerContext);
		    URL url = ci.findURLOfDefaultConfigurationFile(true);

		    try {
		        JoranConfigurator configurator = new JoranConfigurator();
		        configurator.setContext(loggerContext);
		        loggerContext.reset();
		        configurator.doConfigure(url);
		    } catch (JoranException je) {
		        // StatusPrinter will handle this
		    }
		    StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
			}
}
