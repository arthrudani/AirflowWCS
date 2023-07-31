package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.VehicleMoveEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Class to keep CMS command data.
 *
 *  @author A.D.
 *  @since  13-May-2009
 */
public class VehicleMoveData extends AbstractSKDCData
{
  public static final String AGVLOADSTATUS_NAME    = AGVLOADSTATUS.getName();
  public static final String CURRSTATION_NAME      = CURRSTATION.getName();
  public static final String DESTSTATION_NAME      = DESTSTATION.getName();
  public static final String DUALLOADMOVESEQ_NAME  = DUALLOADMOVESEQ.getName();
  public static final String LOADID_NAME           = LOADID.getName();
  public static final String STATUSCHANGETIME_NAME = STATUSCHANGETIME.getName();
  public static final String NOTIFYHOST_NAME       = NOTIFYHOST.getName();
  public static final String REQUESTID_NAME        = REQUESTID.getName();
  public static final String SEQUENCENUMBER_NAME   = SEQUENCENUMBER.getName();
  public static final String VEHICLEID_NAME        = VEHICLEID.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  private int    iAGVLoadStatus  = DBConstants.AGV_NOMOVE;
  private int    iNotifyHost     = 0;
  private int    iSequenceNumber = 0;
  private Date   dStatusChangeTime = new Date();
  private String sLoadID         = "";
  private String sVehicleID      = "";
  private String sRequestID      = "";
  private String sCurrStation    = "";
  private String sDestStation    = "";
  private String sDualLoadMoveSeq= "";

 /**
  *  Default Constructor.
  */
  public VehicleMoveData()
  {
    super();
    initColumnMap(mpColumnMap, VehicleMoveEnum.class);
  }

 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    String vsStr = "\ndStatusChangeTime:" +  dStatusChangeTime +
                   "\nsLoadID:"         +  sLoadID         +
                   "\niSequenceNumber:" +  iSequenceNumber +
                   "\nsVehicleID:"      +  sVehicleID      +
                   "\nsRequestID:"      +  sRequestID      +
                   "\nsSourceStation:"  +  sCurrStation    +
                   "\nsDestStation:"    +  sDestStation    +
                   "\nsDualLoadMoveSeq:"+  sDualLoadMoveSeq;
    try
    {
      vsStr = vsStr + "\niAGVLoadStatus:"  +
              DBTrans.getStringValue(AGVLOADSTATUS_NAME, iAGVLoadStatus);
      vsStr = vsStr + "\niNotifyHost:"  +
              DBTrans.getStringValue(NOTIFYHOST_NAME, iNotifyHost);
    }
    catch(NoSuchFieldException e)
    {
      vsStr = vsStr + "0";
    }

    return(vsStr);
  }

 /**
  * Defines equality between two HostToWrxData objects.
  *
  * @param  ipData <code>AbstractSKDCData</code> reference whose runtime type
  *         is expected to be <code>HostToWrxData</code>
  * @return
  */
  @Override
  public boolean equals(AbstractSKDCData ipData)
  {
    if (ipData == null || !(ipData instanceof VehicleMoveData))
    {
      return(false);
    }

    VehicleMoveData vpVHData = (VehicleMoveData)ipData;
    return(vpVHData.iAGVLoadStatus == iAGVLoadStatus        &&
           vpVHData.iSequenceNumber == iSequenceNumber      &&
           vpVHData.sLoadID.equals("sLoadID")               &&
           vpVHData.sVehicleID.equals("sVehicleID")         &&
           vpVHData.sRequestID.equals("sRequestID")         &&
           vpVHData.sCurrStation.equals("sCurrStation")     &&
           vpVHData.sDestStation.equals("sDestStation")); 
  }

  /**
   * Method to make a deep copy of this object.
   *
   *  @return copy of this object.
   */
  @Override
  public VehicleMoveData clone()
  {
    VehicleMoveData vpClonedData = (VehicleMoveData)super.clone();
    vpClonedData.dStatusChangeTime = (Date)dStatusChangeTime.clone();

    return(vpClonedData);
  }

 /**
  * Resets the data in this class to the default.
  */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    iAGVLoadStatus  = DBConstants.AGV_NOMOVE;
    iNotifyHost     = 0;
    iSequenceNumber = 0;
    dStatusChangeTime.setTime(System.currentTimeMillis());
    sLoadID         = "";
    sVehicleID      = "";
    sRequestID      = "";
    sCurrStation    = "";
    sDestStation    = "";
    sDualLoadMoveSeq = "";
  }

/*----------------------------------------------------------------------------
                             Getters Section
  ----------------------------------------------------------------------------*/
  public Date getStatusChangeTime()
  {
    return(dStatusChangeTime);
  }

  public int getAGVLoadStatus()
  {
    return(iAGVLoadStatus);
  }

  public int getNotifyHost()
  {
    return(iNotifyHost);
  }

  public int getSequenceNumber()
  {
    return(iSequenceNumber);
  }

  public String getDestStation()
  {
    return(sDestStation);
  }

  public String getLoadID()
  {
    return(sLoadID);
  }

  public String getRequestID()
  {
    return(sRequestID);
  }

  public String getCurrentStation()
  {
    return(sCurrStation);
  }

  public String getVehicleID()
  {
    return(sVehicleID);
  }

  public String getDualLoadMoveSeq()
  {
    return(sDualLoadMoveSeq);
  }

/*----------------------------------------------------------------------------
                             Setters Section
  ----------------------------------------------------------------------------*/
  public void setStatusChangeTime(Date ipStatusChangeTime)
  {
    dStatusChangeTime = ipStatusChangeTime;
    addColumnObject(new ColumnObject(STATUSCHANGETIME_NAME, ipStatusChangeTime));
  }

  public void setAGVLoadStatus(int inAGVLoadStatus)
  {
    iAGVLoadStatus = inAGVLoadStatus;
    addColumnObject(new ColumnObject(AGVLOADSTATUS_NAME, iAGVLoadStatus));
  }

  public void setNotifyHost(int inNotifyHost)
  {
    iNotifyHost = inNotifyHost;
    addColumnObject(new ColumnObject(NOTIFYHOST_NAME, iNotifyHost));
  }

  public void setSequenceNumber(int inSequenceNumber)
  {
    iSequenceNumber = inSequenceNumber;
    addColumnObject(new ColumnObject(SEQUENCENUMBER_NAME, iSequenceNumber));
  }

  public void setDestStation(String isDestStation)
  {
    sDestStation = isDestStation;
    addColumnObject(new ColumnObject(DESTSTATION_NAME, isDestStation));
  }

  public void setLoadID(String isLoadID)
  {
    sLoadID = isLoadID;
    addColumnObject(new ColumnObject(LOADID_NAME, isLoadID));
  }

  public void setRequestID(String isRequestID)
  {
    sRequestID = isRequestID;
    addColumnObject(new ColumnObject(REQUESTID_NAME, isRequestID));
  }

  public void setCurrentStation(String isCurrStation)
  {
    sCurrStation = isCurrStation;
    addColumnObject(new ColumnObject(CURRSTATION_NAME, isCurrStation));
  }

  public void setVehicleID(String isVehicleID)
  {
    sVehicleID = isVehicleID;
    addColumnObject(new ColumnObject(VEHICLEID_NAME, isVehicleID));
  }

  public void setDualLoadMoveSeq(String isDualLoadMoveSeq)
  {
    sDualLoadMoveSeq = isDualLoadMoveSeq;
    addColumnObject(new ColumnObject(DUALLOADMOVESEQ_NAME, isDualLoadMoveSeq));
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

    switch((VehicleMoveEnum)vpEnum)
    {
      case AGVLOADSTATUS:
        setAGVLoadStatus((Integer)ipColValue);
        break;

      case DESTSTATION:
        setDestStation((String)ipColValue);
        break;

      case DUALLOADMOVESEQ:
        setDualLoadMoveSeq((String)ipColValue);
        break;
        
      case LOADID:
        setLoadID((String)ipColValue);
        break;

      case STATUSCHANGETIME:
        setStatusChangeTime((Date)ipColValue);
        break;

      case NOTIFYHOST:
        setNotifyHost((Integer)ipColValue);
        break;

      case SEQUENCENUMBER:
        setSequenceNumber((Integer)ipColValue);
        break;

      case REQUESTID:
        setRequestID((String)ipColValue);
        break;

      case CURRSTATION:
        setCurrentStation((String)ipColValue);
        break;

      case VEHICLEID:
        setVehicleID((String)ipColValue);
        break;
    }
    
    return(0);
  }
}
