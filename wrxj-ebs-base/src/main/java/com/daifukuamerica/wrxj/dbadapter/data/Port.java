package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
 $Workfile$
 $Revision$
 $Date$

 Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

 THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
 NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
 REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
 COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
 CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS WORK 
 WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
 LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 * Title: Class to handle Port Object. Description : Handles all reading and
 * writing database for port
 * 
 * @author REA
 * @author A.D.
 * @version 1.0 04/25/02
 * @version 2.0 10/11/04
 */
public class Port extends BaseDBInterface
{
  private PortData mpPortData;

  public Port()
  {
    super("Port");
    mpPortData = Factory.create(PortData.class);
  }

  /**
   * Method returns a particular port record in the system.
   * 
   * @return <code>PortData</code> containing record data.
   */
  public PortData getPortData(String portName) throws DBException
  {
    mpPortData.clear();
    if (portName.endsWith(SKDCConstants.EMULATION_SUFFIX))
    {
      /*
       * Emulator ports don't really exist--they're just converted versions of
       * the real port.
       */
      mpPortData.setKey(PortData.PORTNAME_NAME, portName.replace(
          SKDCConstants.EMULATION_SUFFIX, ""));
      PortData vpPort = getElement(mpPortData, DBConstants.NOWRITELOCK);
      if (vpPort != null)
        convertPortToEmulationPort(vpPort);
      return vpPort;
    }
    else
    {
      mpPortData.setKey(PortData.PORTNAME_NAME, portName);
      return getElement(mpPortData, DBConstants.NOWRITELOCK);
    }
  }

  /**
   * Method returns all port records in the system.
   * 
   * @return <code>List</code> of port records.
   */
  public List<Map> getPortDataList() throws DBException
  {
    mpPortData.clear();
    mpPortData.addOrderByColumn(PortData.PORTNAME_NAME);
    return (getAllElements(mpPortData));
  }

  /**
   * Delete a port by name
   * 
   * @param portName
   * @throws DBException
   */
  public void deletePort(String portName) throws DBException
  {
    if (doesPortExist(portName))
    {
      mpPortData.clear();
      mpPortData.setKey(PortData.PORTNAME_NAME, portName);
      deleteElement(mpPortData);
    }
  }

  /**
   * Test for port existence.
   * 
   * @param portName <code>String</code> containing port name to test.
   * @return <code>boolean</code> of <code>true</code> if the port exists.
   * @throws DBException if there is a database error.
   */
  public boolean doesPortExist(String portName) throws DBException
  {
    mpPortData.clear();
    mpPortData.setKey(PortData.PORTNAME_NAME, portName);
    return (getCount(mpPortData) > 0);
  }

  /**
   * Get total count of all ports in the system.
   */
  @UnusedMethod // Only called by an UnusedMethod
  public int getPortCount() throws DBException
  {
    mpPortData.clear();
    return (getCount(mpPortData));
  }

  /**
   * Builds a list of emulation ports based upon the real ports
   * 
   * @param isDeviceID
   * @return
   */
  public List<PortData> getEmulationPortsForDevice(String isDeviceID)
      throws DBException
  {
    mpPortData.clear();
    mpPortData.setKey(PortData.DEVICEID_NAME, isDeviceID);
    List<PortData> vpPorts = DBHelper.convertData(getAllElements(mpPortData),
                                                  PortData.class);
    for (PortData p : vpPorts)
    {
      convertPortToEmulationPort(p);
    }
    return vpPorts;
  }

  /**
   * Convert the PortData to an emulation PortData
   * 
   * @param ipPortData
   */
  public void convertPortToEmulationPort(PortData ipPortData)
  {
    if (!ipPortData.getPortName().endsWith(SKDCConstants.EMULATION_SUFFIX))
    {
      ipPortData.setPortName(ipPortData.getPortName()
          + SKDCConstants.EMULATION_SUFFIX);

      if (ipPortData.getCommunicationMode() == DBConstants.MASTER)
        ipPortData.setCommunicationMode(DBConstants.SLAVE);
      else
        ipPortData.setCommunicationMode(DBConstants.MASTER);

      if (ipPortData.getDirection() == DBConstants.INBOUND)
        ipPortData.setDirection(DBConstants.OUTBOUND);
      else if (ipPortData.getDirection() == DBConstants.OUTBOUND)
        ipPortData.setDirection(DBConstants.INBOUND);
    }
  }
}