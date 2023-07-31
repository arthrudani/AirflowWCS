package com.daifukuamerica.wrxj.device.agv.messages;

import java.util.Date;

/**
 * A generic data class for getting information from a persistence layer.
 *
 * @author A.D.
 * @since  15-Jun-2009
 */
public class AGVData
{
  private AGVMessageNameEnum mpMessageName = null;
  private int    mnSequenceNumber     = 0;
  private int    mnAGVLoadStatus      = 0;
  private int    mnNotifyHost         = 0;
  private int    mnPickLocationHeight = 0;
  private int    mnPickLocationDepth  = 0;
  private int    mnDropLocationHeight = 0;
  private int    mnDropLocationDepth  = 0;
  private Date   mpMessageAddTime  = new Date();
  private String msLoadID          = "";
  private String msVehicleID       = "";
  private String msRequestID       = "";
  private String msCurrStation     = "";
  private String msDestStation     = "";
  private String msDualLoadMoveSeq = "";

  private int    mnCommandStatus   = 0;
  private String msCommandValue    = "";

  public AGVData()
  {
    super();
  }

/*----------------------------------------------------------------------------
                               Setter Methods.
  ----------------------------------------------------------------------------*/
  public void setSequenceNumber(int inSequenceNumber)
  {
    mnSequenceNumber = inSequenceNumber;
  }

  public void setAGVLoadStatus(int inAGVLoadStatus)
  {
    mnAGVLoadStatus = inAGVLoadStatus;
  }

  public void setCommandStatus(int inCommandStatus)
  {
    mnCommandStatus = inCommandStatus;
  }

  public void setCommandValue(String isCommandValue)
  {
    msCommandValue = isCommandValue;
  }

  public void setNotifyHost(int inNotifyHost)
  {
    mnNotifyHost = inNotifyHost;
  }

  public void setMessageAddTime(Date ipMessageAddTime)
  {
    mpMessageAddTime = ipMessageAddTime;
  }

  public void setMessageName(AGVMessageNameEnum ipMessageName)
  {
    mpMessageName = ipMessageName;
  }

  public void setCurrStation(String isCurrStation)
  {
    msCurrStation = isCurrStation;
  }

  public void setDestStation(String isDestStation)
  {
    msDestStation = isDestStation;
  }

  public void setDualLoadMoveSeq(String isDualLoadMoveSeq)
  {
    msDualLoadMoveSeq = isDualLoadMoveSeq;
  }

  public void setLoadID(String isLoadID)
  {
    msLoadID = isLoadID;
  }

  public void setRequestID(String isRequestID)
  {
    msRequestID = isRequestID;
  }

  public void setVehicleID(String isVehicleID)
  {
    msVehicleID = isVehicleID;
  }

  public void setPickLocationDepth(int inPickLocationDepth)
  {
    mnPickLocationDepth = inPickLocationDepth;
  }

  public void setPickLocationHeight(int inPickLocationHeight)
  {
    mnPickLocationHeight = inPickLocationHeight;
  }

  public void setDropLocationDepth(int inDropLocationDepth)
  {
    mnDropLocationDepth = inDropLocationDepth;
  }

  public void setDropLocationHeight(int inDropLocationHeight)
  {
    mnDropLocationHeight = inDropLocationHeight;
  }

/*----------------------------------------------------------------------------
                               Getter Methods.
  ----------------------------------------------------------------------------*/
  public int getSequenceNumber()
  {
    return(mnSequenceNumber);
  }

  public int getAGVLoadStatus()
  {
    return(mnAGVLoadStatus);
  }

  public int getCommandStatus()
  {
    return(mnCommandStatus);
  }
  
  public int getNotifyHost()
  {
    return(mnNotifyHost);
  }

  public Date getMessageAddTime()
  {
    return(mpMessageAddTime);
  }

  public AGVMessageNameEnum getMessageName()
  {
    return(mpMessageName);
  }

  public String getCurrStation()
  {
    return(msCurrStation);
  }

  public String getDestStation()
  {
    return(msDestStation);
  }

  public String getDualLoadMoveSeq()
  {
    return(msDualLoadMoveSeq);
  }

  public String getLoadID()
  {
    return(msLoadID);
  }

  public String getCommandValue()
  {
    return(msCommandValue);
  }

  public String getRequestID()
  {
    return(msRequestID);
  }

  public String getVehicleID()
  {
    return(msVehicleID);
  }

  public int getPickLocationDepth()
  {
    return(mnPickLocationDepth);
  }

  public int getPickLocationHeight()
  {
    return(mnPickLocationHeight);
  }

  public int getDropLocationDepth()
  {
    return(mnDropLocationDepth);
  }

  public int getDropLocationHeight()
  {
    return(mnDropLocationHeight);
  }
}
