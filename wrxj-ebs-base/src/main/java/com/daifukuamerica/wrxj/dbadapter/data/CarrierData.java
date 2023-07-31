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

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

/**
 * Description:<BR>
 *   Class to handle Carrier Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       R.M.
 * @version      1.0
 * <BR>Created: 24-Nov-04<BR>
 *     Copyright (c) 2004<BR>
 *     Company:  Daifuku America Corporation
 */
public class CarrierData extends AbstractSKDCData
{
  public static final String CARRIERID_NAME      = "SCARRIERID";
  public static final String CARRIERNAME_NAME    = "SCARRIERNAME";
  public static final String CARRIERCONTACT_NAME = "SCARRIERCONTACT";
  public static final String CARRIERPHONE_NAME   = "SCARRIERPHONE";
  public static final String STATIONNAME_NAME    = "SSTATIONNAME";
  
/*---------------------------------------------------------------------------
                 Database fields for OrderHeader table.
  ---------------------------------------------------------------------------*/
  private String sCarrierID          = "";
  private String sCarrierName        = "";
  private String sCarrierContact     = "";
  private String sCarrierPhone       = "";
  private String sStationName        = "";

  
  public CarrierData()
  {
    super();
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sCarrierID:" + sCarrierID +
               "\nsCarrierName:" + sCarrierName +
               "\nsCarrierContact:" + sCarrierContact +
               "\nsCarrierPhone:" + sCarrierPhone + "\n" +
               "\nsStation:" + sStationName + "\n" +
               "\n\n";
    s += super.toString();
    
    return(s);
  }

  /**
   *  Method to make a deep copy of this object.
   *
   *  @return copy of <code>CarrierData</code>.
   */
  @Override
  public CarrierData clone()
  {
    return (CarrierData)super.clone();
  }

  /**
   * Defines equality between two OrderHeaderData objects.
   *
   * @param  absOH <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>OrderHeaderData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absOH)
  {
    CarrierData ca = (CarrierData)absOH;
    return(getCarrierID().equals(ca.getCarrierID()));
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sCarrierID = "";
    sCarrierName = "";
    sCarrierContact = "";
    sCarrierPhone = "";
    sStationName = "";
  }

/*---------------------------------------------------------------------------
                       Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Carrier ID
   * @return CarrierID as string
   */
  public String getCarrierID()
  {
    return(sCarrierID);
  }

  /**
   * Fetches Carrier Name
   * @return CarrierName as string
   */
  public String getCarrierName()
  {
    return(sCarrierName);
  }

  /**
   * Fetches Carrier Contact
   * @return CarrierContact as string
   */
  public String getCarrierContact()
  {
    return(sCarrierContact);
  }

  /**
   * Fetches Carrier Phone for the order.
   * @return CarrierPhone as string
   */
  public String getCarrierPhone()
  {
    return(sCarrierPhone);
  }

  /**
   * Fetches assigned Station for the carrier.
   * @return Station as string
   */
  public String getStation()
  {
    return(sStationName);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Order ID value.
   */
  public void setCarrierID(String isCarrierID)
  {
    sCarrierID = checkForNull(isCarrierID);
    addColumnObject(new ColumnObject(CARRIERID_NAME, isCarrierID));
  }

  /**
   * Sets Carrier Name value.
   */
  public void setCarrierName(String isCarrierName)
  {
    sCarrierName = checkForNull(isCarrierName);
    addColumnObject(new ColumnObject(CARRIERNAME_NAME, isCarrierName));
  }

  /**
   * Sets Carrier Contact value.
   */
  public void setCarrierContact(String isCarrierContact)
  {
    sCarrierContact = checkForNull(isCarrierContact);
    addColumnObject(new ColumnObject(CARRIERCONTACT_NAME, isCarrierContact));
  }

  /**
   * Sets Carrier Phone value.
   */
  public void setCarrierPhone(String isCarrierPhone)
  {
    sCarrierPhone = checkForNull(isCarrierPhone);
    addColumnObject(new ColumnObject(CARRIERPHONE_NAME, isCarrierPhone));
  }

  /**
   * Sets Carrier Phone value.
   */
  public void setStation(String isStation)
  {
    sStationName = checkForNull(isStation);
    addColumnObject(new ColumnObject(STATIONNAME_NAME, isStation));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String colName, Object colValue)
  {
    int rtn = 0;

    if (colName.equalsIgnoreCase(CARRIERID_NAME))
    {
      setCarrierID(colValue.toString());
    }
    else if (colName.equalsIgnoreCase(CARRIERNAME_NAME))
    {
      setCarrierName(colValue.toString());
    }
    else if (colName.equalsIgnoreCase(CARRIERCONTACT_NAME))
    {
      setCarrierContact(colValue.toString());
    }
    else if (colName.equalsIgnoreCase(CARRIERPHONE_NAME))
    {
      setCarrierPhone(colValue.toString());
    }
    else if (colName.equalsIgnoreCase(STATIONNAME_NAME))
    {
      setStation(colValue.toString());
    }
    else
    {
      rtn = super.setField(colName, colValue);
    }

    return rtn;
  }
}
