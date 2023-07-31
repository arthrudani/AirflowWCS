package com.daifukuamerica.wrxj.web.core.messaging;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.client.ActiveMQDestination;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@Configurable
public class JMSProducer {

	private static final Logger logger = LoggerFactory.getLogger("JMSProducer-Messaging");

	// String constants, because magic strings suck
	static final String MESSAGE_SENDER = "MsgSndr";
	static final String MESSAGE_SENDER_GROUP = "MsgSndrCG";
	static final String MESSAGE_DATA = "MsgData";
	static final String MESSAGE_EVENT_TYPE = "EvtType";
	static final String MESSAGE_EVENT = "Event";
	static final String MESSAGE_TIME = "MsgTxTime";

	// JMS publish defaults
	static final int DEFAULT_JMS_PRIORITY = 4;
	static final int DEFAULT_STATUS_TIME_TO_LIVE = 60000;
	static final int DEFAULT_TIME_TO_LIVE = 900000;//15 minutes
    private JmsTemplate wrxJmsTemplate;

    public JMSProducer()
    {
    	/// default no arg
    }

    public JMSProducer(JmsTemplate jmsTemplate)
    {
    	this.wrxJmsTemplate = jmsTemplate;
    }


	public JmsTemplate getWrxJmsTemplate()
	{
		return wrxJmsTemplate;
	}

	public void setWrxJmsTemplate(JmsTemplate wrxJmsTemplate)
	{
		this.wrxJmsTemplate = wrxJmsTemplate;
	}

	/**
	 * Send test messages
	 * @throws JMSException
	 */
	public AjaxResponse sendTestMessages(String testMessage) {
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			ajaxResponse = publishArtemisEvent("WebMessageService", "Ctlrs", testMessage, 0, 0, "TEST", "WRxJTopic");
		} catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "ERROR:  errorMessage" + e.getMessage());
			logger.error("ERROR | errorLog{}", e.getMessage());
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
    }

	/**
	 * Publish a load event with messageText as body, flag for iMessageType
	 * and destination 'reciever' is dest device. Send the message to the specified topic.
	 *
	 * @param messageText - Message Body
	 * @param iMessageType - MessageEventConsts message type
	 * @param reciever - Destination device id
	 * @param topic - JMS Topic destination
	 * @return
	 */
	public AjaxResponse publishLoadEvent(String messageText, int iMessageType, String reciever,
			String topic)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{

			ajaxResponse = publishEvent("WebMessageService", "Ctlrs", messageText, iMessageType, MessageEventConsts.LOAD_EVENT_TYPE,
			        MessageEventConsts.LOAD_EVENT_TYPE_TEXT+reciever, topic);
		} catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "ERROR:  errorMessage" + e.getMessage());
			logger.error("ERROR | errorLog{}", e.getMessage());
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	public AjaxResponse publishCustomEvent(String messageText, int iMessageType, String reciever,
											int iEventType, String eventName, String topic)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			ajaxResponse = publishEvent("WebMessageService", "Ctlrs", messageText, iMessageType, iEventType, eventName+reciever, topic);
		} catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "ERROR:  errorMessage" + e.getMessage());
			logger.error("ERROR | errorLog{}", e.getMessage());
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	public AjaxResponse publishArtemisEvent(String messageSender,
		      String controllerGroup, String messageText, long messageData,
		      int eventType, String event, String topic) throws JMSException
	{
		Connection connection = null;
		Session session = null;
		ConnectionFactory factory = null;
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			logger.debug("Establishing JMS Connection through SPRING JMS");
			factory = new ActiveMQJMSConnectionFactory(
					Application.getString("IpcMessageService.JmsProviderUrl", "tcp://WMS01:61616"));

			// Some default values
			if (messageSender == null)
			{
				messageSender = "wrxj-web";
			}
			if (controllerGroup == null)
			{
				controllerGroup = "Client";
			}
			if (topic == null)
			{
				// WRx does the topic lookup a bit differently, so convert it
				topic = Application.getString("IpcMessageService.JmsTopicName");
				if (topic != null)
				{
					int index = topic.indexOf("/");
					if (index > 0)
					{
						topic = ActiveMQDestination.TOPIC_QUALIFIED_PREFIX + topic.substring(index+1);
					}
				}
			}

			Destination destination = ActiveMQDestination.fromPrefixedName(topic);

			Connection conn = factory.createConnection();
			session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(destination);
			TextMessage publishedMessage = session.createTextMessage();

			publishedMessage.setText(messageText);
			publishedMessage.setLongProperty(MESSAGE_DATA, messageData);
			publishedMessage.setStringProperty(MESSAGE_SENDER, messageSender);
			publishedMessage.setStringProperty(MESSAGE_SENDER_GROUP, controllerGroup);
			publishedMessage.setIntProperty(MESSAGE_EVENT_TYPE, eventType);
			// Don't let these messages queue up forever
			int vnTimeToLive = DEFAULT_TIME_TO_LIVE;
			switch (eventType)
			{
				case MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE:
				case MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE:
				case MessageEventConsts.STATUS_EVENT_TYPE:
				case MessageEventConsts.UPDATE_EVENT_TYPE:
					vnTimeToLive = DEFAULT_STATUS_TIME_TO_LIVE;
			}

			// The "event" string is used by the subscriber event selector.
			publishedMessage.setStringProperty(MESSAGE_EVENT, event);
			publishedMessage.setLongProperty(MESSAGE_TIME, new Date().getTime());

			// Log the message we are about to publish (but not log or heart beats)
			if ((eventType != MessageEventConsts.LOG_EVENT_TYPE)
					&& (eventType != MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE)
					&& (eventType != MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE))
			{
				logger.debug("MESSAGE SENT TO TOPIC {}| {}", topic, messageText);
			}
			// publish the message
			producer.send(publishedMessage, DeliveryMode.NON_PERSISTENT,
		            DEFAULT_JMS_PRIORITY, vnTimeToLive);

			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS,
					"Successfully sent message: " + event + " - TO - topic: " + topic);
	    }
		catch (JMSException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,
					"Unable to send message: " + event + " - TO - topic: " + topic + " - CAUSE: " + e.getMessage());
			logger.error("ERROR PUBLISHING EVENT {}", e.getMessage());
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,
					"Unable to send message: " + event + " - TO - topic: " + topic + " - CAUSE: " + e.getMessage());
			logger.error("ERROR PUBLISHING EVENT {}", e.getMessage());
		}
		finally
		{
			if (session != null && connection != null)
			{
				session.close();
				logger.debug("JMS session closed for event: {} | Client ID: {}", event, connection.getClientID());
			}
			if (connection != null)
			{
				logger.debug("JMS connection closing for event: {} | Client ID {}", event, connection.getClientID());
				connection.close();
			}
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	public AjaxResponse publishEvent(String messageSender,
		      String controllerGroup, String messageText, long messageData,
		      int eventType, String event, String topic) throws JMSException
	{
		Connection connection = null;
		Session session = null;
		AjaxResponse ajaxResponse = new AjaxResponse();
	    try
	    {
	    	logger.debug("Establishing JMS Connection through SPRING JMS");
	    	ConnectionFactory connectionFactory = getWrxJmsTemplate().getConnectionFactory();
	    	connection =  connectionFactory.createConnection();
	    	logger.debug("Client ID: {} connection established for -{}. Establishing JMS session ", connection.getClientID(), event);
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			logger.debug("JMS session established for -{} | Client ID : {}", event, connection.getClientID());
	        TextMessage publishedMessage = session.createTextMessage();

	        publishedMessage.setText(messageText);
	        publishedMessage.setLongProperty(MESSAGE_DATA, messageData);
	        publishedMessage.setStringProperty(MESSAGE_SENDER, messageSender);
	        publishedMessage.setStringProperty(MESSAGE_SENDER_GROUP, controllerGroup);
	        publishedMessage.setIntProperty(MESSAGE_EVENT_TYPE, eventType);

	        // Don't let these messages queue up forever
	        int vnTimeToLive = DEFAULT_TIME_TO_LIVE;
	        switch (eventType)
	        {
	          case MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE:
	          case MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE:
	          case MessageEventConsts.STATUS_EVENT_TYPE:
	          case MessageEventConsts.UPDATE_EVENT_TYPE:
	            vnTimeToLive = DEFAULT_STATUS_TIME_TO_LIVE;
	        }

	        // The "event" string is used by the subscriber event selector.
	        publishedMessage.setStringProperty(MESSAGE_EVENT, event);
	        publishedMessage.setLongProperty(MESSAGE_TIME, new Date().getTime());

	        // Log the message we are about to publish (but not log or heart beats)
	        if ((eventType != MessageEventConsts.LOG_EVENT_TYPE)
	            && (eventType != MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE)
	            && (eventType != MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE))
	        {
	          logger.debug("MESSAGE SENT TO TOPIC {}| {}", topic, messageText);
	        }

	        // publish the message
	        wrxJmsTemplate.convertAndSend(topic, publishedMessage);
	        ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS,
	        		"Successfully sent message: " + event + " - TO - topic: " + topic);

	    }
	    catch (Exception e)
	    {
	       ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to send message: " + event
	    		   	+ " - TO - topic: " + topic + " - CAUSE: " + e.getMessage());
		   logger.error("ERROR PUBLISHING EVENT {}", e.getMessage());
	    }finally{
	    	if(session!=null && connection!=null){
	    		session.close();
	    		logger.debug("JMS session closed for event: {} | Client ID: {}", event, connection.getClientID());
	    	}

	    	if(connection!=null){
	    		logger.debug("JMS connection closing for event: {} | Client ID {}", event, connection.getClientID());
	    		connection.close();

	    	}

	    }

	    if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed" );
		return ajaxResponse;

	}

    public void receiveMessages() throws JMSException{
        System.out.println("Getting message from queue "+ wrxJmsTemplate.receive().getStringProperty("text"));
    }



}