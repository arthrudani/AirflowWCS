
package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.VehicleSystemCmdEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data class for VehicleSystemCmd Table.
 * @author A.D.
 * @since  15-Jun-2009
 */
public class VehicleSystemCmdData extends AbstractSKDCData
{
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /*========================================================================*/
  /*  Column names                                                          */
  /*========================================================================*/
  public static final String COMMANDSTATUS_NAME    = COMMANDSTATUS.getName();
  public static final String COMMANDVALUE_NAME     = COMMANDVALUE.getName();
  public static final String STATUSCHANGETIME_NAME = STATUSCHANGETIME.getName();
  public static final String SEQUENCENUMBER_NAME   = SEQUENCENUMBER.getName();
  public static final String SYSTEMMESSAGEID_NAME  = SYSTEMMESSAGEID.getName();

  /*========================================================================*/
  /*  Table Data                                                            */
  /*========================================================================*/
  private int    iSequenceNumber  = 0;
  private int    iCommandStatus   = DBConstants.AGV_SYSCMD_REQUEST;
  private String sCommandValue    = "";
  private String sSystemMessageID = "";
  private Date   dStatusChangeTime = new Date();

  public VehicleSystemCmdData()
  {
    super();
    initColumnMap(mpColumnMap, VehicleSystemCmdEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s =  "iSequenceNumber: " + iSequenceNumber + SKDCConstants.EOL_CHAR;
           s += "sSystemMessageID: " + sSystemMessageID  + SKDCConstants.EOL_CHAR;
           s += "sCommandValue: "  + sCommandValue  + SKDCConstants.EOL_CHAR;
           s += "dStatusChangeTime:" +  dStatusChangeTime + SKDCConstants.EOL_CHAR;

    try
    {
      s += "iCommandStatus:" + 
           DBTrans.getStringValue(COMMANDSTATUS_NAME, iCommandStatus);
    }
    catch(NoSuchFieldException e)
    {
      s += "0";
    }

    return(s + super.toString());
  }

  @Override
  public boolean equals(AbstractSKDCData ipVSCmd)
  {
    if (ipVSCmd == null) return(false);
    VehicleSystemCmdData vpCMD = (VehicleSystemCmdData)ipVSCmd;
    return(vpCMD.getSequenceNumber() == iSequenceNumber);
  }

  /**
   * Method to make a deep copy of this object.
   *
   *  @return copy of this object.
   */
  @Override
  public VehicleSystemCmdData clone()
  {
    VehicleSystemCmdData vpClonedData = (VehicleSystemCmdData)super.clone();
    vpClonedData.dStatusChangeTime = (Date)dStatusChangeTime.clone();

    return(vpClonedData);
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in the default behaviour

    iSequenceNumber = 0;
    iCommandStatus  = DBConstants.AGV_SYSCMD_REQUEST;
    sSystemMessageID  = "";
    sCommandValue = "";
    dStatusChangeTime.setTime(System.currentTimeMillis());
  }

  public Date getStatusChangeTime()
  {
    return(dStatusChangeTime);
  }

  public int getCommandStatus()
  {
    return iCommandStatus;
  }

  public int getSequenceNumber()
  {
    return iSequenceNumber;
  }

  public String getCommandValue()
  {
    return sCommandValue;
  }

  public String getSystemMessageID()
  {
    return sSystemMessageID;
  }

/*----------------------------------------------------------------------------
                             Setters Section
  ----------------------------------------------------------------------------*/
  public void setStatusChangeTime(Date ipMessageAddTime)
  {
    dStatusChangeTime = ipMessageAddTime;
    addColumnObject(new ColumnObject(STATUSCHANGETIME_NAME, ipMessageAddTime));
  }

  public void setCommandStatus(int inCommandStatus)
  {
    iCommandStatus = inCommandStatus;
    addColumnObject(new ColumnObject(COMMANDSTATUS_NAME, inCommandStatus));
  }

  public void setSequenceNumber(int inSequenceNumber)
  {
    iSequenceNumber = inSequenceNumber;
    addColumnObject(new ColumnObject(SEQUENCENUMBER_NAME, inSequenceNumber));
  }

  public void setCommandValue(String isCommandValue)
  {
    sCommandValue = isCommandValue;
    addColumnObject(new ColumnObject(COMMANDVALUE_NAME, isCommandValue));
  }

  public void setSystemMessageID(String isSystemMessageID)
  {
    sSystemMessageID = isSystemMessageID;
    addColumnObject(new ColumnObject(SYSTEMMESSAGEID_NAME, isSystemMessageID));
  }

  /**
   *  Required set field method.  This method figures out what column was
   *  passed to it and sets the value.  This allows us to have a generic
   *  method for all DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return super.setField(isColName, ipColValue);
    }

    switch((VehicleSystemCmdEnum)vpEnum)
    {
      case COMMANDSTATUS:
        setCommandStatus((Integer)ipColValue);
        break;

      case SEQUENCENUMBER:
        setSequenceNumber((Integer)ipColValue);
        break;

      case SYSTEMMESSAGEID:
        setSystemMessageID((String)ipColValue);
        break;

      case COMMANDVALUE:
        setCommandValue((String)ipColValue);
        break;

      case STATUSCHANGETIME:
        setStatusChangeTime((Date)ipColValue);
    }

    return(0);
  }
}
