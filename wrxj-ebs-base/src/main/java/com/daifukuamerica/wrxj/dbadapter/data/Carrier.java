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
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Description:<BR>
 *   Concrete implementation of CarrierInterface class for regular carriers.
 *   This Class will handle Carrier specific operations.
 *
 * @author       R.M.
 * @version      1.0   11/24/2004
 */
public class Carrier extends BaseDBInterface
{
  private CarrierData mpCarData;

  public Carrier()
  {
    super("Carrier");
    mpCarData = Factory.create(CarrierData.class);
  }

  /**
   * Method to get List of Carrier IDs..
   * @param izInsertBlank insert a blank into the results or not
   *
   * @return StringBuffer Carrier IDs.
   */
  public String[] getCarrierChoices() throws DBException
  {
    return getDistinctColumnValues(CarrierData.CARRIERID_NAME, "");
  }

  /**
   * Method to check for Carrier existence.
   *
   * @param carrierID <code>String</code> containing carrier ID.
   * @return boolean of true if it exists, false otherwise.
   */
  public boolean exists(String isCarrierID)
  {
    mpCarData.clear();
    mpCarData.setKey(CarrierData.CARRIERID_NAME, isCarrierID);
    return exists(mpCarData);
  }

  /**
   * Get the station assigned to a carrier
   * @param isCarrierID
   * @return
   * @throws DBException
   */
  public String getCarrierStation(String isCarrierID) throws DBException
  {
    mpCarData.clear();
    mpCarData.setKey(CarrierData.CARRIERID_NAME, isCarrierID);
    CarrierData vpData = getElement(mpCarData, DBConstants.NOWRITELOCK);
    if (vpData == null)
    {
      throw new DBException("Carrier " + isCarrierID + " not found.");
    }
    else
    {
      return vpData.getStation();
    }
  }
  
  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpCarData = null;
  }
}
