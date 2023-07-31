package com.daifukuamerica.wrxj.dbadapter.data;

/*
                    Daifuku America Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used and copied only 
   in accordance with the terms of such license. This software or any other 
   copies thereof in any form, may not be provided or otherwise made available, 
   to any other person or company without written consent from Daifuku America 
   Corporation.

   Daifuku America Corporation assumes no responsibility for the use or 
   reliability of software which has been modified without approval.
*/

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import java.io.IOException;
import java.io.StringReader;

/**
 * Class to parse a location address into Bank, Bay, and Tier
 * representation.  This class provides methods to retrieve the
 * string as well as integer representation.
 *
 * @author  A.D.  Original Version. 20-Dec-02<br>
 * @author  A.D.  Added ability to split complete rack location string.
 * @version      2.0
 *     <BR>Copyright (c) 2004<BR>
 *     Company:  Daifuku America Corporation
 */
public final class RackLocationParser
{
  private String sWarehouse;
  private String sAddress;
  private String sBank;
  private String sBay;
  private String sTier;

  private RackLocationParser(String locationPart, boolean addressOnly)
          throws IOException
  {
    if (addressOnly)
    {
      this.sAddress = locationPart;
      splitRackAddress();
    }
    else
    {
      String[] splitValue = Location.parseLocation(locationPart);
      this.sWarehouse = splitValue[0];
      this.sAddress = splitValue[1];
      splitRackAddress();
    }
  }

 /**
  *  Method to parse Rack location string.  The passed in string may be the
  *  location address or the full location (warehouse + address). If the full
  *  location is passed, the following are examples of valid location strings:<br>
  *  <b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>&quot;A&nbsp;&nbsp;001001001&quot;</code> or<br>
  *  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>&quot;A&nbsp;&nbsp;-001001001&quot;</code> or<br>
  *  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>&quot;A-001001001&quot;</code></b>
  * 
  *  @param locationPart <code>String</code> containing location part that will
  *         be parsed.
  *  @param addressOnly <code>boolean</code> indicating if the first
  *         parameter is a location address only, or full location.
  * 
  *  @return <code>RackLocationParser</code> object to use for retrieving
  *          various parts of a Rack location.
  */
  public static RackLocationParser parse(String locationPart,
                                         boolean addressOnly) throws IOException
  {
    return(new RackLocationParser(locationPart, addressOnly));
  }

 /**
  * Gets warehouse string if <code>RackLocationParser.{@link #parse parse}</code>
  * was initially called.
  *
  * @return <code>String</code> containing warehouse if full location is parsed.
  *         <code>null</code> object otherwise.
  */
  public String getWarehouse()
  {
    return(this.sWarehouse);
  }

 /**
  * Gets Location address string.
  */
  public String getAddress()
  {
    return(this.sAddress);
  }

 /**
  * Gets Bank portion of Location address.
  */
  public String getBankString()
  {
    return(this.sBank);
  }

 /**
  * Gets Bay portion of Location address.
  */
  public String getBayString()
  {
    return(this.sBay);
  }

 /**
  * Gets Tier portion of Location address.
  */
  public String getTierString()
  {
    return(this.sTier);
  }

 /**
  * Gets Bank portion of Location address as an integer.
  */
  public int getBankInteger() throws NumberFormatException
  {
    return(Integer.parseInt(this.sBank));
  }

 /**
  * Gets Bay portion of Location address as an integer.
  */
  public int getBayInteger() throws NumberFormatException
  {
    return(Integer.parseInt(this.sBay));
  }

 /**
  * Gets Tier portion of Location address as an integer.
  */
  public int getTierInteger() throws NumberFormatException
  {
    return(Integer.parseInt(this.sTier));
  }

/*===========================================================================
                     ****** Private Methods ******
  ===========================================================================*/
 /**
  *  Method that does the work of splitting an address into bank-bay-tier.
  */
  private void splitRackAddress() throws IOException
  {
    if (this.sAddress == null || this.sAddress.length() == 0)
    {
      throw new IOException("Invalid address " + sAddress + " provided!");
    }

    char[] cBank = new char[DBConstants.LNBANK];
    char[] cBay  = new char[DBConstants.LNBAY];
    char[] cTier = new char[DBConstants.LNTIER];
    StringReader addr_strm = new StringReader(sAddress);

    try
    {
      addr_strm.read(cBank, 0, DBConstants.LNBANK);
      addr_strm.read(cBay,  0, DBConstants.LNBAY);
      addr_strm.read(cTier, 0, DBConstants.LNTIER);
      addr_strm.close();
    }
    catch(IOException e)
    {
      throw new IOException("Invalid Bank, Bay, or Tier format!" + e.getMessage());
    }

    this.sBank = new String(cBank);
    this.sBay  = new String(cBay);
    this.sTier = new String(cTier);
  }
}
