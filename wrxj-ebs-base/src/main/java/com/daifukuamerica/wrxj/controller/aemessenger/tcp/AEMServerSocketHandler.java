/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.controller.aemessenger.tcp;

import com.daifukuamerica.TCPIPConnectionEvent;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.wrxj.controller.aemessenger.AEMessage;
import com.daifukuamerica.wrxj.controller.aemessenger.AEMessageProcessor;
import com.daifukuamerica.wrxj.controller.aemessenger.process.DefaultMessageProcessor;
import com.daifukuamerica.wrxj.dbadapter.data.aed.Instance;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.communication.TCPIPBaseLoggerImpl;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Properties;

/**
 * Server Socket Handler for use outside of AE Messenger
 * @author mandrus
 *
 */
public class AEMServerSocketHandler
    implements AEMReadEvent, AEMSocketCloseEvent, TCPIPConnectionEvent
{
  private Properties mpProps;
  private Map<Long, AEMessageProcessor> mpProcessorMap;
  
  private Logger logger;
  private TCPIPLogger mpCommLogger;
  
  // External event handling
  private AEMSocketConnectionEvent mpConnectHandler;
  private AEMSocketCloseEvent mpCloseHandler;
  
  /**
   * Constructor
   * 
   * @param isConnectionName
   * @param ipProps
   * @param ipProcMap
   * @param ipConnectHandler
   * @param ipReadHandler
   * @param ipCloseHandler
   */
  public AEMServerSocketHandler(String isConnectionName, Properties ipProps,
      Map<Long, AEMessageProcessor> ipProcMap,
      AEMSocketConnectionEvent ipConnectHandler, AEMSocketCloseEvent ipCloseHandler)
  {
    mpProps = ipProps;
    mpProcessorMap = ipProcMap;
    
    logger = Logger.getLogger(isConnectionName);
    mpCommLogger = new TCPIPBaseLoggerImpl(logger);
    
    mpConnectHandler = ipConnectHandler;
    mpCloseHandler = ipCloseHandler;
  }
  
  /**
   * connectionHandler() - what to do when someone connects to us
   */
  @Override
  public void connectionHandler(SocketChannel ipClientChannel)
  {
    AEMTcpipReaderWriter vpConnection = null;
    try
    {
      vpConnection = new AEMTcpipReaderWriter(mpProps, ipClientChannel, mpCommLogger);
      vpConnection.registerReadEvent(this);
      vpConnection.registerCloseEvent(this);
      vpConnection.start();
      
      // External event handling
      if (mpConnectHandler != null)
      {
        mpConnectHandler.onSocketConnection(vpConnection);
      }
    }
    catch (Exception exc)
    {
      mpCommLogger.logErrorMessage(
          "Server Connection dropped due to Exception!", exc);
      if (vpConnection != null)
      {
        vpConnection.stopThread();
        vpConnection = null;
      }
    }
  }

  /**
   * receivedData() - process a message
   * 
   * @param ipChannel
   * @param ipMsg
   */
  @Override
  public void receivedData(AEMTcpipReaderWriter ipChannel, AEMessage ipMsg)
  {
    try
    {
      logger.logRxEquipmentMessage(ipMsg.getMessageDataAsString(), ipMsg.toString());
      
      // Pick the correct message processor
      AEMessageProcessor vpProcessor = mpProcessorMap.get(ipMsg.getSource());
      if (vpProcessor == null)
      {
        vpProcessor = new DefaultMessageProcessor(getProduct(ipMsg.getSource()));
      }
      
      // Process the message
      byte[] vabResponse = vpProcessor.process(ipMsg);
      if (vabResponse != null)
      {
        // Send the response if there is one
        ipChannel.sendMessage(ipMsg.getTransactionID(), vabResponse);
      }
    }
    catch (Exception e)
    {
      logger.logException("Error processing message", e);
    }
  }

  /**
   * 
   * @param inInstanceId
   * @return
   */
  private String getProduct(long inInstanceId)
  {
    InstanceData vpPpeInstData;
    try
    {
      vpPpeInstData = Factory.create(Instance.class).getData((int)inInstanceId);
      if (vpPpeInstData != null)
      {
        return vpPpeInstData.getIdentityName();
      }
    }
    catch (DBException e)
    {
      logger.logException("Error getting product for ID=[" + inInstanceId + "]",
          e);
    }
    return "" + inInstanceId;
  }
  
  /**
   * socketCloseEvent() - what to do when a socket closes
   */
  @Override
  public void onSocketClose(AEMTcpipReaderWriter ipReader)
  {
    // External event handling
    if (mpCloseHandler != null)
    {
      mpCloseHandler.onSocketClose(ipReader);
    }
  }
}
