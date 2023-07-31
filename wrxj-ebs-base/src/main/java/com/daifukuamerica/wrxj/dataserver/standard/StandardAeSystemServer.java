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
package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.aed.Instance;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunications;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunicationsData;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceData;
import com.daifukuamerica.wrxj.dbadapter.data.aed.WcfService;
import com.daifukuamerica.wrxj.dbadapter.data.aed.WcfServiceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.WrxjVersion;
import com.wynright.wrxj.app.Wynsoft;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 * A server for interacting with Wynsoft AE System
 */
public class StandardAeSystemServer extends StandardServer
{
  /*====================================================================*/
  /*  Constructors                                                      */
  /*====================================================================*/

  /**
   * Constructor
   */
  public StandardAeSystemServer()
  {
    this(null);
  }

  /**
   * Constructor
   *
   * @param keyName
   */
  public StandardAeSystemServer(String keyName)
  {
    super(keyName);
    logDebug("Creating " + getClass().getSimpleName());
  }

//  /*====================================================================*/
//  /*  Overridden methods                                                */
//  /*====================================================================*/
//
//  /**
//   * Shuts down this controller by cancelling any timers and shutting down the
//   * Equipment.
//   */
//  @Override
//  public void cleanUp()
//  {
//    super.cleanUp();
//  }
  
  /*====================================================================*/
  /* Methods                                                            */
  /*====================================================================*/

  /*====================================================================*/
  /* Public Methods - Server Instance Update                            */
  /*====================================================================*/
  /**
   * Update the AE system instance table when a server starts
   */
  public void updateInstanceForServerStart()
  {
    InstanceData vpInstData = Factory.create(InstanceData.class);
    vpInstData.setKey(InstanceData.ID_NAME, Wynsoft.getInstanceId());
    
    // Host/IP
    try
    {
      InetAddress vpSvrAddr = InetAddress.getLocalHost();
      vpInstData.setIpAddress(vpSvrAddr.getHostAddress());
      vpInstData.setComputerName(vpSvrAddr.getHostName());
    }
    catch (UnknownHostException e) {}

    // PID
    try
    {
      String vsPID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
      vpInstData.setProcessId(Integer.parseInt(vsPID));
    }
    catch (Exception e) {}

    // Version
    vpInstData.setVersion(WrxjVersion.getBuildTime());
    // Process Name
    vpInstData.setProcessName(System.getProperty("sun.java.command"));
    // User
    vpInstData.setUserName(System.getProperty("user.name"));
    // Run Path
    vpInstData.setRunPath(Paths.get("").toAbsolutePath().toString());
    // Time
    vpInstData.setTimeRegistered(new Date());
    
    // These are not implemented for now.
    vpInstData.setTotalMemory(0);
    vpInstData.setTotalDiskSpace(0);
    vpInstData.setStatsMeasureTime(0);

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(Instance.class).update(vpInstData);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException("Unable to update AE Instance", e);
    }
    catch (NoSuchElementException e)
    {
      logError("AE instance [" + Wynsoft.getInstanceId() + "] is undefined.");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /*====================================================================*/
  /* Public Methods - Instance Communications                           */
  /*====================================================================*/

  /**
   * Add an AE System Instance Communications definition
   * 
   * @param ipInstCommData
   * @throws DBException
   */
  public void addInstanceCommunication(InstanceCommunicationsData ipInstCommData)
      throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      getICHandler().addElement(ipInstCommData);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException("Error adding AE System Instance Communications definition",
          e);
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Delete an AE System Instance Communications definition
   * 
   * @param inSender
   * @param inCompId
   * @param inReceiver
   * @param inCommType
   * @throws DBException
   */
  public void deleteInstanceCommunication(int inSender, int inCompId,
      int inReceiver, int inCommType) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      getICHandler().delete(inSender, inCompId, inReceiver, inCommType);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(
          "Error deleting AE System Instance Communications definition", e);
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Get an InstanceCommunications DB Handler
   * @return
   */
  private InstanceCommunications getICHandler()
  {
    return Factory.create(InstanceCommunications.class);
  }
  
  /*====================================================================*/
  /* Public Methods - Web Service                                       */
  /*====================================================================*/
  
  /**
   * Get the endpoint for a Wynsoft WCF Web Service
   * @param isServiceName
   * @return
   * @throws DBException
   */
  public String getEndpoint(String isServiceName) throws DBException
  {
    // Get the service definition
    WcfService vpSrvHandler = Factory.create(WcfService.class);
    WcfServiceData vpSrvData = vpSrvHandler.getData(isServiceName);
    if (vpSrvData == null)
    {
      throw new DBException(String.format(
          "Service definition not found for ClassName=[%1$s]. Check [%2$s].",
          isServiceName, vpSrvHandler.getReadTableName()));
    }
    
    // Get the instance definition
    InstanceData vpInstData = Factory.create(Instance.class).getData(vpSrvData.getHostingInstanceId());
    if (vpInstData == null)
    {
      throw new DBException(String.format(
          "Cannot get enpoint for [%1$s]--hosting Instance=[%2$d] not found!",
          isServiceName, vpSrvData.getHostingInstanceId()));
    }
    
    // Assemble the endpoint
    return vpInstData.getIpAddress() + ":" + vpInstData.getPort() + "/"
        + isServiceName + ".svc?wsdl";
  }
}
