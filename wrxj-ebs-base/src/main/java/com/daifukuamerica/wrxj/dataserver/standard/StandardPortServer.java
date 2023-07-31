package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;

//
//                 Daifuku America Corporation
//                     International Center
//                 5202 Douglas Corrigan Way
//              Salt Lake City, Utah  84116-3192
//                      (801) 359-9900
//
// This software is furnished under a license and may be used and copied only in
// accordance with the terms of such license.  This software or any other copies
// thereof in any form, may not be provided or otherwise made available, to any 
// other person or company without written consent from Daifuku America 
// Corporation.
//
// Daifuku America assumes no responsibility for the use or reliability of
// software which has been modified without approval.
//

import com.daifukuamerica.wrxj.dbadapter.data.Port;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.List;
import java.util.Map;

/**
 * A server that provides methods and transactions for use in port management.
 * Methods used to add, modify and delete ports are provided. Transactions are
 * wrapped around calls to the lower level data base objects.
 * 
 * @author avt
 * @version 1.0
 */
public class StandardPortServer extends StandardServer
{
  Port mpPort = Factory.create(Port.class);

  /**
   * Create default port server.
   */
  public StandardPortServer()
  {
    this(null);
  }

  /**
   * Create a port server with a name.
   * 
   * @param keyName Name to use in creation.
   */
  public StandardPortServer(String keyName)
  {
    super(keyName);
  }

  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardPortServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
  }
  /**
   * Method to delete a port.
   * 
   * @param portData Port to be deleted.
   * @exception DBException
   */
  @UnusedMethod
  public void deletePort(PortData portData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();

      mpPort.deletePort(portData.getPortName());
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, e.getMessage());
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to add a port.
   * 
   * @param ipPortData Filled in port data object.
   * @exception DBException
   */
  public void addPort(PortData ipPortData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpPort.addElement(ipPortData);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, e.getMessage());
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to get port information.
   * 
   * @param portName Port to look for.
   * @return PortData object containing port info. matching our search criteria.
   * @exception DBException
   */
  public PortData getPort(String portName) throws DBException
  {
    return mpPort.getPortData(portName);
  }

  /**
   * Method to get list of all ports.
   * 
   * @return List of portData objects.
   * @exception DBException
   */
  public List<Map> getPortlist() throws DBException
  {
    return mpPort.getPortDataList();
  }

  /**
   * Method to get the count of ports.
   * 
   * @return int count.
   * @exception DBException
   */
  @UnusedMethod
  public int getPortCount() throws DBException
  {
    return mpPort.getPortCount();
  }

  /**
   * Method to see if the port exists.
   * 
   * @param isName Port name.
   * @return boolean of <code>true</code> if it exists.
   * @exception DBException
   */
  public boolean exists(String isName) throws DBException
  {
    return mpPort.doesPortExist(isName);
  }

  /**
   * Method to update a port.
   * 
   * @param ipPortData Filled in port data object.
   * @exception DBException
   */
  public void updatePort(PortData ipPortData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      ipPortData.setKey(PortData.PORTNAME_NAME, ipPortData.getPortName());
      mpPort.modifyElement(ipPortData);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, e.getMessage());
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to delete a port.
   * 
   * @param isPortName Port to be deleted.
   * @exception DBException
   */
  public void deletePort(String isPortName) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpPort.deletePort(isPortName);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, e.getMessage());
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * A device's emulator port is defined as a port record with the deviceID and
   * Communication mode set to "server".
   */
  public List<PortData> getEmulatorPorts(String isDevice)
  {
    try
    {
      return mpPort.getEmulationPortsForDevice(isDevice);
    }
    catch (DBException e)
    {
      logException(e, e.getMessage());
      System.err.println("Error obtaining emulator ports for " + isDevice);
      return null;
    }
  }
}
