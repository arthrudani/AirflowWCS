package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.messageformat.MessageConstants;
import com.daifukuamerica.wrxj.messageformat.MessageDataFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;


/*
                       SKDC Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used
   and copied only in accordance with the terms of such license.
   This software or any other copies thereof in any form, may not be
   provided or otherwise made available, to any other person or company
   without written consent from SKDC Corporation.

   SKDC assumes no responsibility for the use or reliability of
   software which has been modified without approval.
*/


/**
 * Description:<BR>
 *  Class used to send and return data from the various Allocation Strategies.
 *
 * @author   A.D.
 * @version  1.0
 * @since    29-Jul-02
 */
public class AllocationMessageDataFormat extends MessageDataFormat
{
  public  static final int REPLENISH_ORDER = 110;
  public  static final int CYCLECOUNT_ORDER = 111;
  public  static final int NORMAL_ORDER     = 112;
  public  static final int ORDER_DIAGNOSIS  = 113;
  public  static final int ALLOCATE_SHIPPING  = 1;
  protected Map<Integer, String> mpAllocDataMap  = new LinkedHashMap<Integer, String>();
  protected String   DELIM          = ";";
  protected final Integer ALLOC_LOAD      = Integer.valueOf(0);
  protected final Integer ALLOC_WAREHOUSE = Integer.valueOf(1);
  protected final Integer ALLOC_ADDRESS   = Integer.valueOf(2);
  protected final Integer ALLOC_STATION   = Integer.valueOf(3);
  protected final Integer ALLOC_ORDERID   = Integer.valueOf(4);

  public AllocationMessageDataFormat()
  {
    this("AllocationController");
  }

  public AllocationMessageDataFormat(String senderName)
  {
    super(MessageConstants.SCHEDULERMESSAGETYPE, senderName);
  }

  public void clear()
  {
    mpAllocDataMap.clear();
  }

/*--------------------------------------------------------------------------
                        All set methods go here.
  --------------------------------------------------------------------------*/
  public void setDelimiter(String delim)
  {
    DELIM = delim;
  }

  public void setOutBoundLoad(String outBoundLoad)
  {
    mpAllocDataMap.put(ALLOC_LOAD, outBoundLoad.trim());
  }

  public void setFromWarehouse(String fromWarehouse)
  {
    mpAllocDataMap.put(ALLOC_WAREHOUSE, fromWarehouse.trim());
  }

  public void setFromAddress(String fromAddress)
  {
    mpAllocDataMap.put(ALLOC_ADDRESS, fromAddress.trim());
  }

  public void setOutputStation(String toStation)
  {
    mpAllocDataMap.put(ALLOC_STATION, toStation.trim());
  }

  public void setOrderID(String orderID)
  {
    mpAllocDataMap.put(ALLOC_ORDERID, orderID.trim());
  }

/*--------------------------------------------------------------------------
                        All get methods go here.
  --------------------------------------------------------------------------*/
  public String getOutBoundLoad()
  {
    String vsRtn = mpAllocDataMap.get(ALLOC_LOAD);
    return((vsRtn == null) ? "" : vsRtn);
  }

  public String getFromWarehouse()
  {
    String vsRtn = mpAllocDataMap.get(ALLOC_WAREHOUSE);
    return((vsRtn == null) ? "" : vsRtn);
  }

  public String getFromAddress()
  {
    String vsRtn = mpAllocDataMap.get(ALLOC_ADDRESS);
    return((vsRtn == null) ? "" : vsRtn);
  }

  public String getOutputStation()
  {
    String vsRtn = mpAllocDataMap.get(ALLOC_STATION);
    return((vsRtn == null) ? "" : vsRtn);
  }

  public String getOrderID()
  {
    String vsRtn = mpAllocDataMap.get(ALLOC_ORDERID);
    return((vsRtn == null) ? "" : vsRtn);
  }

  public boolean validOutputStation()
  {
    if(mpAllocDataMap.get(ALLOC_STATION).trim().length() == 0)
    {
        return false;
    }
    else if(mpAllocDataMap.get(ALLOC_STATION).trim() == null)
    {
        return false;
    }
    else
    {
        return true;
    }
  }

  /**
   *  Returns a DELIM character delimited string for a station scheduler.  The
   *  format of the string is: <code>load,location,station</code>
   */
  @Override
  public void createDataString()
  {
    StringBuffer vpData = new StringBuffer();

    vpData.append(getOutBoundLoad()).append(DELIM)
       .append(getFromWarehouse()).append(DELIM)
       .append(getFromAddress()).append(DELIM)
       .append(getOutputStation()).append(DELIM)
       .append(getOrderID()).append(DELIM);
    super.setDataString(vpData.toString());
  }

  /**
   *  Unpacks a string based on expected position of each token.  The ordering of
   *  data is expected to be Load ID, Warehouse, Address, Station, Order ID.
   */
  @Override
  public boolean decodeDataString()
  {
    boolean vzFoundData = false;
    String vsData = super.getDataString();
                                       // Make sure it's not a blank string!
    if (vsData != null && vsData.trim().length() != 0)
    {
      if (vsData.charAt(0) == ';') vsData = " " + vsData;
      Scanner vpScanner = new Scanner(vsData);
      vpScanner.useDelimiter(";");
      
      mpAllocDataMap.clear();
      vzFoundData = true;
      
      for(int vnCount = 0; vpScanner.hasNext(); vnCount++)
      {
        mpAllocDataMap.put(Integer.valueOf(vnCount), vpScanner.next());
      }
    }
    return(vzFoundData);
  }
}
