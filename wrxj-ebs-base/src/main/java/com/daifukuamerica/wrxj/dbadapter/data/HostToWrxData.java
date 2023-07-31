package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

/**
 * Description:<BR>
 *   Class to handle HostToWrx inbound data.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.D.  02/08/2005
 * @version      1.0
 */
public class HostToWrxData extends WrxToHostData
{
  public HostToWrxData()
  {
    super();
  }
  
  /**
   *  Method to make a deep copy of this object.
   *
   *  @return copy of <code>HostToWrxData</code>.
   */
  @Override
  public HostToWrxData clone()
  {
    HostToWrxData vpClonedData = (HostToWrxData)super.clone();
    return vpClonedData;
  }

  /**
   * Defines equality between two HostToWrxData objects.
   *
   * @param  absData <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>HostToWrxData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absData)
  {
    if (absData == null || !(absData instanceof HostToWrxData))
    {
      return(false);
    }
    HostToWrxData hodata = (HostToWrxData)absData;
    return(hodata.getHostName().equals(getHostName())                   &&
           hodata.getMessageIdentifier().equals(getMessageIdentifier()) &&
           hodata.getMessageProcessed() == getMessageProcessed()        &&
           hodata.getMessageSequence() == getMessageSequence());
  }
}
