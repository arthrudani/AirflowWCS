package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

/**
 * Description:<BR>
 *   Class to handle PlcToWrx inbound data.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       D.K  21/10/2022
 * @version      1.0
 */
public class PlcToWrxData extends WrxToPlcData
{
  int iOriginalSequence = 0;
  public static String ORIGINALSQUENCE_NAME = "IORIGINALSEQUENCE";
  
  public PlcToWrxData()
  {
    super();
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    return(super.toString() + "\niOriginalSequence:" + iOriginalSequence);
  }
  
  /**
   *  Method to make a deep copy of this object.
   *
   *  @return copy of <code>HostToWrxData</code>.
   */
  @Override
  public PlcToWrxData clone()
  {
    PlcToWrxData vpClonedData = (PlcToWrxData)super.clone();
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
    if (absData == null || !(absData instanceof PlcToWrxData))
    {
      return(false);
    }
    PlcToWrxData podata = (PlcToWrxData)absData;
    return(podata.getPortName().equals(getPortName())                   &&
           podata.getMessageIdentifier().equals(getMessageIdentifier()) &&
           podata.getMessageProcessed() == getMessageProcessed()        &&
           podata.getMessageSequence() == getMessageSequence());
  }
  
/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Message Sequence value
   * @return MessageSequence value as integer
   */
  public int getOriginalMessageSequence()
  {
    return(iOriginalSequence);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets the original Host sent message sequence number.
   */
  public void setOriginalMessageSequence(int inOriginalSequence)
  {
    iOriginalSequence = inOriginalSequence;
    addColumnObject(new ColumnObject(ORIGINALSQUENCE_NAME,
        new Integer(inOriginalSequence)));
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
    
    if ((rtn = super.setField(colName, colValue)) == -1)
    {
      if (colName.equalsIgnoreCase(ORIGINALSQUENCE_NAME))
      {
        setOriginalMessageSequence(((Integer)colValue).intValue());
        rtn = 0;
      }
    }
    
    return(rtn);
  }
}
