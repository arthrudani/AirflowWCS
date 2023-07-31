package com.daifukuamerica.wrxj.dataserver.standard;

import java.util.List;
import java.util.Map;

/*
                    Daifuku America Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used and copied 
   only in accordance with the terms of such license.  This software or 
   any other copies thereof in any form, may not be provided or otherwise 
   made available, to any other person or company without written consent 
   from Daifuku America Corporation.

   Daifuku America Corporation assumes no responsibility for the use or 
   reliability of software which has been modified without approval.
*/

import com.daifukuamerica.wrxj.dbadapter.data.Carrier;
import com.daifukuamerica.wrxj.dbadapter.data.CarrierData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;

/**
 * Description:<BR>
 *   Server to handle Carrier Specific operations.
 * 
 * @author       R.M.
 * @version      1.0
 * <BR>Created: 29-Nov-04<BR>
 *     Copyright (c) 2004<BR>
 *     Company:  Daifuku America Corporation
 */
public class StandardCarrierServer extends StandardServer
{
  Carrier mpCarrier = Factory.create(Carrier.class);
  
  public StandardCarrierServer()
  {
    this(null);
  }

  public StandardCarrierServer(String keyName)
  {
    super(keyName);
    logDebug("StandardCarrierServer.createCarrierServer()");
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardCarrierServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo);
	  logDebug("StandardCarrierServer.createCarrierServer()");
  }

  /**
   *  Method adds Carrier to the database.
   *
   *  @param cadata <code>CarrierData</code> containing data to add.
   *
   *  @throws <code>DBException</code>
   */
  public void addCarrier(CarrierData cadata) throws DBException
  {
    if (mpCarrier.exists(cadata.getCarrierID()))
    {
      throw new DBException(
        "Duplicate data add error! Carrier '"
          + cadata.getCarrierID()
          + "' already exists!");
    }

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpCarrier.addElement(cadata);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method modifies Carrier in the database.
   *
   *  @param cadata <code>CarrierData</code> containing Key and
   *         data to modify.
   *
   *  @throws <code>DBException</code>
   */
  public String modifyCarrier(CarrierData cadata) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpCarrier.modifyElement(cadata);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Inside modifyCarrier");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }

    return ("Carrier modified successfully");
  }

  /**
   *  Method to delete Carriers.
   *
   *  @param carrierID <code>String</code> Carrier ID. to delete.
   *
   *  @return <code>int</code> value set to 1.
   *          -1 if the order doesn't exist.
   */
  public int deleteCarrier(String carrierID) throws DBException
  {
    CarrierData cadata = Factory.create(CarrierData.class);
    CarrierData carData = null;

    cadata.setKey(CarrierData.CARRIERID_NAME, carrierID);
    carData = mpCarrier.getElement(cadata, DBConstants.NOWRITELOCK);
    
    if (carData == null)               // If the Carrier doesn't even exist.
    {
      return (-1);
    }

    TransactionToken tt = null;
    int nextStatus = 1;
    try
    {
      tt = startTransaction();
      mpCarrier.deleteElement(cadata);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "deleteCarrier");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }

    return (nextStatus);
  }

  /**
   *  Gets a list of Carrier data based on keys passed in cadata.
   *  @param cadata <code>CarrierData</code> object containing search
   *         information.
   *  @return <code>List</code> of Carrier data in <code>Map</code> format.
   */
  public List<Map> getCarrierList(CarrierData cadata) throws DBException
  {
    if (cadata.getOrderByColumns().length == 0)
    {
      cadata.addOrderByColumn(CarrierData.CARRIERID_NAME);
    }
    return (mpCarrier.getAllElements(cadata));
  }

  /**
   *  Gets a Carrier record.
   */
  private CarrierData getCarrierRecord(CarrierData carData, int lockFlag)
         throws DBException
  {
    return (mpCarrier.getElement(carData, lockFlag));
  }

  /**
   *  Convenience method. Gets a Order Header with no Lock.
   */
  public CarrierData getCarrierRecord(CarrierData carData)
    throws DBException
  {
    return (getCarrierRecord(carData, DBConstants.NOWRITELOCK));
  }

  /**
   * 
   * @param carData
   * @return
   * @throws DBException
   */
  public boolean CarrierExists(CarrierData carData) throws DBException
  {
    return mpCarrier.exists(carData);
  }

  /**
   * Convenience method to check for carrier existence.
   * @param sCarrierID the carrier id.
   * @return true if the carrier exists, false otherwise.
   */
  public boolean exists(String sCarrierID)
  {
    CarrierData crdata = Factory.create(CarrierData.class);
    crdata.setKey(CarrierData.CARRIERID_NAME, sCarrierID);
    return mpCarrier.exists(crdata);
  }
  
  /*==========================================================================
            All types of Data gathering methods go in this section.
   ==========================================================================*/
  /**
   *  Gets list of all Carrier IDs in the system.
   */
  public String[] getCarrierChoices() throws DBException
  {
    String[] carlist = null;

    try
    {
      carlist = mpCarrier.getCarrierChoices();
    }
    catch (DBException exc)
    {
      logException(exc, "getCarrierChoices");
      throw exc;
    }

    return (carlist);
  }

  /**
   *  Get the station assigned to a carrier
   *
   *  @param isCarrierID
   *  @return <code>String</code> - Station Name
   *  @exception DBException
   */
  public String getCarrierStation(String isCarrierID) throws DBException
  {
    return mpCarrier.getCarrierStation(isCarrierID);
  }
  
  /**
   * Are there carriers defined in the system?
   * @return
   */
  public boolean hasCarriersDefined()
  {
    boolean vzHasCarriers = false;
   
    try
    {
      String[] vasCarriers = getCarrierChoices();
      vzHasCarriers = (vasCarriers.length != 0); // just blank
    }
    catch (DBException e)
    {
      logError(e.getMessage());
    }
    
    return vzHasCarriers;
  }
}
