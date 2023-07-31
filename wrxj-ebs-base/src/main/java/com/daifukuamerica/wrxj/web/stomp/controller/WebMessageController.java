package com.daifukuamerica.wrxj.web.stomp.controller;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageEventHandling;
import com.daifukuamerica.wrxj.ipc.MessageServiceImpl;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.stationevent.StationEventDataFormat;
import com.google.gson.Gson;


/**
 * @author dystout
 * Date: May 10, 2017
 *
 * Description: Controller Class to reformat/filter/republish subscribed load & station event
 * messages to a separate topic "WebTopic". This topic "WebTopic" will subscribed to by websocket
 * STOMP clients in order to perform server sent events on the UI (I.E. load arrival on store screen)
 * 
 */
public class WebMessageController extends Controller
{
	

	/**
	 * Topic Destination
	 */
	public static final String DESTINATION = "topic/WebTopic"; 
	private String msDestination; 
	private MessageServiceImpl webMS; 

	/**
	 *  Event Constants
	 */
	private static final int LOAD_EVENT = 1;
	private static final int STATION_EVENT = 2;
	/**
	 * construct and instance of WebMessageController()
	 */
	public WebMessageController()
	{
	}
	
	/**
	 * Subscribe to WRxJTopic events: 
	 *
	 * <li>Load Events</li>
	 * <li>Station Events</li>
	 * </ul>
	 */
	@Override
	public void initialize(String aControllerKeyName)
	{
		super.initialize(aControllerKeyName);
		subscribeLoadEvent("%");
		subscribeStationEvent("%");
		logger.logDebug("WebMessageController.initialize() - End");
	}

	/**
	 * Startup the WebMessageController 
	 */
	@Override
	public void startup()
	{
		logger.logDebug(getClass().getSimpleName() + ".startup() - Start ");
		super.startup();
		try
		{
			setDestination(DESTINATION);
			webMS = Factory.create(MessageServiceImpl.class, msDestination); 
			webMS.setMessageEventHandler(new MessageEventHandling()
			{

				@Override
				public void onTextMessage(IpcMessage msg) {
					// If we wanted to send messages back into 
					//	wrxj topic I suppose we could do it here?
					//  Probably won't want to send messages 
					//  from the client model standpoint? 
				}
			});
			webMS.initialize(getName());
			webMS.addSubscriber("", null);
			webMS.startup(); 
			setControllerStatus(ControllerConsts.STATUS_RUNNING);
		}
		catch(Exception e)
		{
			setControllerStatus(ControllerConsts.STATUS_ERROR); 
			logger.logException(e);
		}
		logger.logDebug(getClass().getSimpleName() + ".startup() - End ");
	}

	/**
	 * Shutdown the WebMessageController
	 */
	@Override
	public void shutdown()
	{
		logger.logDebug(getClass().getSimpleName() + ".shutdown() -- Start");
		webMS.shutdown();
		super.shutdown();
		logger.logDebug(getClass().getSimpleName() + ".shutdown() -- End");
	}

	/**
	 * Process WRxJTopic message when recieved.
	 */
	@Override
	protected void processIPCReceivedMessage()
	{
		super.processIPCReceivedMessage();
		if(!receivedMessageProcessed)
		{
			receivedMessageProcessed = true;
			switch (receivedEventType)
			{
			case MessageEventConsts.LOAD_EVENT_TYPE:
				processLoadEvent(receivedText);
				break;
			case MessageEventConsts.STATION_EVENT_TYPE:
				processStationEvent(receivedText);
				break;
			default: receivedMessageProcessed = false;
			}
		}

	}
	
	/**
	 * Format the station event message and publish it to the WebTopic 
	 * in JSON format.
	 * 
	 * @param receivedEventString
	 */
	private void processStationEvent(String receivedEventString)
	{
		StationEventDataFormat vpSEDF = Factory.create(StationEventDataFormat.class, getName()); 
		logger.logOperation("Formatting station event and republishing to WebTopic");
		vpSEDF.decodeReceivedString(receivedEventString);
		vpSEDF.setMessageID(STATION_EVENT);
		publishToWebTopic(new Gson().toJson(vpSEDF), "stationEvent"); 
	}

	/**
	 * Format the load event message and publish it to the WebTopic 
	 * in JSON format.
	 * 
	 * @param receivedEventString
	 */
	private void processLoadEvent(String receivedEventString)
	{
	    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class, getName());
	    logger.logOperation("*Formatting load event and republishing to WebTopic");
	    vpLEDF.decodeReceivedString(receivedEventString);
	    vpLEDF.setMessageID(LOAD_EVENT);
		publishToWebTopic(new Gson().toJson(vpLEDF), "loadEvent"); 
	}

	/**
	 * Publish a message to the WebTopic via the Web MessageServiceImpl
	 * 
	 * @param message - body of message
	 * @param messageType - description of message type
	 */
	private void publishToWebTopic(String message, String messageType) 
	{
		webMS.publishEvent(getClass().getSimpleName(), getClass().getSimpleName(), message, 0, 0, messageType);
	}
    
	/**
	 * Setter for destination used in create()
	 * 
	 * @param destination - topic destination
	 */
    private void setDestination(String destination)
    {
    	msDestination = destination; 
    }
    
    /*========================================================================*/
    /* Required Factory Method                                                */
    /*========================================================================*/
    
    /**
     * Factory for ControllerImplFactory.
     *
     * <p><b>Details:</b> <code>create</code> is a factory method used exclusively
     * by <code>ControllerImplFactory</code>.  Configurable properties of a new
     * controller created using this method are initialized using data in the
     * supplied properties object.  If the controller cannot be created, a
     * <code>ControllerCreationException</code> is thrown.</p>
     *
     * <p>This factory initializes the device port and collaborator.</p>
     *
     * @param ipConfig configurable property definitions
     * @return the created controller
     * @throws ControllerCreationException if an error occurred while creating the controller
     *
     *	TODO - implement lookup for topic names. 
     */
	public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
	{
	    WebMessageController vpController = Factory.create(WebMessageController.class);
	    return vpController;
	}
	
	
}
