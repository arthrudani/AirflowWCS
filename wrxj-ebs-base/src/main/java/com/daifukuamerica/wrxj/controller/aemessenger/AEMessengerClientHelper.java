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
package com.daifukuamerica.wrxj.controller.aemessenger;

import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.impl.ConnectionType;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.wrxj.controller.aemessenger.tcp.AEMTcpipReaderWriter;
import com.daifukuamerica.wrxj.dbadapter.data.aed.Instance;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.communication.TCPIPBaseLoggerImpl;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.Properties;

/**
 * Small class to aid in AEMessenger testing and/or emulation
 * 
 * @author mandrus
 */
public class AEMessengerClientHelper
{
  private static final String EXC_NO_INSTANCE = "Instance [%s] not found!";
  
  /**
   * Constructor
   */
  private AEMessengerClientHelper() {}

  /**
   * Get client properties by instance names
   * 
   * @param isOriginName
   * @param isDestinationName
   * @throws DBException
   */
  public static Properties getProperties(String isOriginName, String isDestinationName)
      throws DBException
  {
    Instance vpAEInstHandler = Factory.create(Instance.class);
    InstanceData vpSrcData = vpAEInstHandler.getData(isOriginName);
    if (vpSrcData == null)
    {
      throw new IllegalArgumentException(String.format(EXC_NO_INSTANCE, isOriginName));
    }
    InstanceData vpDestData = vpAEInstHandler.getData(isDestinationName);
    if (vpDestData == null)
    {
      throw new IllegalArgumentException(String.format(EXC_NO_INSTANCE, isDestinationName));
    }

    String vsHost = SKDCUtility.isBlank(vpDestData.getComputerName())
        ? vpDestData.getIpAddress() : vpDestData.getComputerName();
    
    return getProperties(vsHost, vpDestData.getPort(), vpSrcData.getId());
  }

  /**
   * Get client properties
   * 
   * @param isHost
   * @param inPort
   * @param inSourceID
   */
  public static Properties getProperties(String isHost, int inPort, int inSourceID)
  {
    Properties mpConnectionProperties = new Properties();
    mpConnectionProperties.setProperty(TCPIPConstants.SERVER_IP, isHost);
    mpConnectionProperties.setProperty(TCPIPConstants.LISTEN_PORT, Integer.toString(inPort));
    mpConnectionProperties.setProperty(TCPIPConstants.SOCKET_TYPE, ConnectionType.CLIENT.getValue());
    mpConnectionProperties.setProperty(AEMTcpipReaderWriter.MSG_SOURCE, Integer.toString(inSourceID));
    return mpConnectionProperties;
  }
  
  /**
   * Get a logger
   * 
   * @return
   */
  public static TCPIPLogger getLogger()
  {
    return new TCPIPBaseLoggerImpl(Logger.getLogger());
  }
  
  /**
   * Get a logger
   * 
   * @param isName
   * @return
   */
  public static TCPIPLogger getLogger(String isName)
  {
    return new TCPIPBaseLoggerImpl(Logger.getLogger(isName));
  }
}
