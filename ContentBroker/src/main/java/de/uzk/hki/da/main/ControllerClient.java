
package de.uzk.hki.da.main;

import java.util.List;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import de.uzk.hki.da.action.ActionDescription;
import de.uzk.hki.da.util.ConfigurationException;
import de.uzk.hki.da.utils.C;

/**
 * 
 * @author Jens Peters
 * The Client for communicating to the running CB based on Active MQ
 *
 */
public class ControllerClient {

	/**
	 * For example
	 * tcp://localhost:4455 STOP_FACTORY
	 * 
	 * The following commands are actually allowed
	 * <ol>
	 * <li>STOP_FACTORY : let the ActionFactory catch another job</li>
	 * <li>START_FACTORY: disable fetching of new jobs, while already fetched jobs keep working</li>
	 * <li>GRACEFUL_SHUTDOWN: stop running ContentBroker, when all working jobs are finished. (Preferred shutdown sequence)</li>
	 * <li>SHOW_ACTIONS: gets list of running actions, performed by ContentBroker</li>
	 * </ol> 
	 * 
	 * @author Jens Peters
	 * @param String ConnectionUri String command 
	 * @throws JMSException
	 */
	
	
	public static void main(String[] args) throws JMSException {
		if (args==null) throw new IllegalArgumentException("Needed parameters not set! Specify at least <host> <command>");		
		if (args.length<2){
			System.out.print("Specify Active MQ host and specify your command!");
			throw new ConfigurationException("Needed parameters not set! Specify <host> <command>");
		}
		String toQueueName = C.QUEUE_TO_SERVER;
		if (args.length==3){
			toQueueName = args[2];
			System.out.print("Specified queue named : " + toQueueName);
		}
		String serverName =  args[0];
		System.out.println("... Client started, talking to " + args[0]);
		
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(serverName);

		Connection connection = connectionFactory.createConnection();
        connection.start();
        
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination toClient = session.createQueue(C.QUEUE_TO_CLIENT);
        Destination toServer = session.createQueue(toQueueName);
        
        String text = args[1];
       	MessageProducer producer = session.createProducer(toServer);    
    	producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    	TextMessage message = session.createTextMessage(text);
    	
    	producer.send(message);
      	
        MessageConsumer consumer = session.createConsumer(toClient);
        System.out.println("(to stop client hit CTRL-C)");
        while (true) {
        Message messageRecieve = consumer.receive(1000);
        	if (messageRecieve instanceof TextMessage) {
        		TextMessage textMessage = (TextMessage) messageRecieve;
            	 textMessage = (TextMessage) messageRecieve;
                 System.out.println("Recieved:" + textMessage.getText());
        	} else if (messageRecieve instanceof ObjectMessage) {
        		
        		
            		  ObjectMessage om = (ObjectMessage)messageRecieve;
            		  @SuppressWarnings("unchecked")
					List<ActionDescription> ads = (List<ActionDescription>) om.getObject();
            		  for (ActionDescription ad : ads) {
            			  System.out.println(ad.toString());
            		  }
        	}
        }
              
        
	}

}
