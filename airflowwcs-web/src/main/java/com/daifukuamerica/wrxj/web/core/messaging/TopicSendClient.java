package com.daifukuamerica.wrxj.web.core.messaging;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.daifukuamerica.wrxj.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 *  A JMS client example program that sends a TextMessage to a Topic
 *    
 *  @author dystout
 */
public class TopicSendClient
{
    TopicConnection conn = null;
    TopicSession session = null;
    Topic topic = null;
    String msDestinationTopic = "topic/WRxJTopic"; //default topic 
    
    /**
	* Log4j logger: TopicSendClient
	*/
	private static final Logger logger = LoggerFactory.getLogger(TopicSendClient.class);

    
    public void setupPubSub()
        throws JMSException, NamingException
    {
    	msDestinationTopic = getConfig("IpcMessageService.JmsTopicName");
        InitialContext iniCtx = new InitialContext();
        Object tmp = iniCtx.lookup("ConnectionFactory");
        TopicConnectionFactory tcf = (TopicConnectionFactory) tmp;
        conn = tcf.createTopicConnection();
        topic = (Topic) iniCtx.lookup(msDestinationTopic);
        session = conn.createTopicSession(false,
                                          TopicSession.AUTO_ACKNOWLEDGE);
        conn.start();
    }
    
    public void sendAsync(String text)
        throws JMSException, NamingException
    {
        System.out.println("Begin sendAsync");
        // Setup the pub/sub connection, session
        setupPubSub();
        // Send a text msg
        TopicPublisher send = session.createPublisher(topic);
        TextMessage tm = session.createTextMessage(text);
        send.publish(tm);
        System.out.println("sendAsync, sent text=" +  tm.getText());
        send.close();
        System.out.println("End sendAsync");
    }
    
    public void stop() 
        throws JMSException
    {
        conn.stop();
        session.close();
        conn.close();
    }
    
    public static void main(String args[]) 
        throws Exception
    {
        System.out.println("Begin TopicSendClient, now=" + 
		                   System.currentTimeMillis());
        TopicSendClient client = new TopicSendClient();
	    client.sendAsync("A text msg, now="+System.currentTimeMillis());
        client.stop();
        System.out.println("End TopicSendClient");
        System.exit(0);
    }
    
    /**
     * Get an application property with logging
     * 
     * @param name
     * @return
     */
    private String getConfig(String name) {
      String value = Application.getString(name);
      if (value == null)
      {
        logger.error("JMS Configuration: \"{}\" is MISSING", name);
        value = "*MISSING Config!*";
      }
      else
      {
        logger.debug("JMS Configuration: \"{}\" = \"{}\"", name, value);
      }
      return value;
    }
}