package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.PortEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Title:  Class to handle PortData Object.
 *  Description : Handles all data for port
 *  @since 04-Jan-02
 */
public class PortData extends AbstractSKDCData
{
  public static final String COMMUNICATIONMODE_NAME    = COMMUNICATIONMODE.getName();
  public static final String DEVICEID_NAME             = DEVICEID.getName();
  public static final String DIRECTION_NAME            = DIRECTION.getName();
  public static final String LASTSEQUENCE_NAME         = LASTSEQUENCE.getName();
  public static final String PORTNAME_NAME             = PORTNAME.getName();
  public static final String RCVKEEPALIVEINTERVAL_NAME = RCVKEEPALIVEINTERVAL.getName();
  public static final String RETRYINTERVAL_NAME        = RETRYINTERVAL.getName();
  public static final String SERVERNAME_NAME           = SERVERNAME.getName();
  public static final String SNDKEEPALIVEINTERVAL_NAME = SNDKEEPALIVEINTERVAL.getName();
  public static final String SOCKETNUMBER_NAME         = SOCKETNUMBER.getName();
  public static final String ENABLEWRAPPING_NAME	   = ENABLEWRAPPING.getName();

// -------------------Port Table data -----------------------------
  private String sPortName = "";
  private String sDeviceID = "";
  private String sServerName = "";
  private String sSocketNumber = "";
  private int    iDirection;
  private int    iLastSequence;
  private int    iCommunicationMode;
  private int    iRetryInterval;
  private int    iSndKeepAliveInterval;
  private int    iRcvKeepAliveInterval;
  private int	 iEnableWrapping;
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  //-----------------------  default data ---------------------------------
  private String DEFAULT_PORTNAME = "";
  private String DEFAULT_DEVICEID = "";
  private String DEFAULT_SERVERNAME = "";
  private String DEFAULT_SOCKETNUMBER = "";
  private int    DEFAULT_DIRECTION = DBConstants.BIDIRECT;
  private int    DEFAULT_LASTSEQUENCE = 0;
  private int    DEFAULT_COMMUNICATIONMODE = DBConstants.MASTER;
  private int    DEFAULT_RETRYINTERVAL = 5000;
  private int    DEFAULT_SKA_INTERVAL = 60000;
  private int    DEFAULT_RKA_INTERVAL = 70000;
  public static int MINIMUM_INTERVAL = 1000;
  private int 	 DEAFULT_ENABLEWRAPPING = 1;

  public PortData()
  {
    clear();     // set all valuse to default
    initColumnMap(mpColumnMap, PortEnum.class);
  }

  /**
   * Reset to defaults
   */
  @Override
  public void clear()
  {
    super.clear();

    sPortName =             DEFAULT_PORTNAME;
    sDeviceID =             DEFAULT_DEVICEID;
    sServerName =           DEFAULT_SERVERNAME;
    sSocketNumber =         DEFAULT_SOCKETNUMBER;
    iDirection =            DEFAULT_DIRECTION;
    iLastSequence =         DEFAULT_LASTSEQUENCE;
    iCommunicationMode =    DEFAULT_COMMUNICATIONMODE;
    iRetryInterval =        DEFAULT_RETRYINTERVAL;
    iSndKeepAliveInterval = DEFAULT_SKA_INTERVAL;
    iRcvKeepAliveInterval = DEFAULT_RKA_INTERVAL;
    iEnableWrapping =		DEAFULT_ENABLEWRAPPING;
  }

  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer();
    myString.append(": sPortName = ").append(sPortName)
            .append(": sDeviceID = ").append(sDeviceID)
            .append(": iDirection = ").append(iDirection)
            .append(": iLastSequence = ").append(iLastSequence)
            .append(": iCommunicationMode = ").append(iCommunicationMode)
            .append(": sServerName = ").append(sServerName)
            .append(": sSocketNumber = ").append(sSocketNumber)
            .append(": iRetryInterval = ").append(iRetryInterval)
            .append(": iSndKeepAliveInterval = ").append(iSndKeepAliveInterval)
            .append(": iRcvKeepAliveInterval = ").append(iRcvKeepAliveInterval)
            .append(": iEnableWrapping = ").append(iEnableWrapping);

    return myString.toString() + super.toString();
  }

  @Override
  public boolean equals(AbstractSKDCData absPT)
  {
    PortData pt = (PortData)absPT;
    return(pt.getPortName().equals(getPortName()));
  }

  public void setPortName(String portName)
  {
    sPortName = checkForNull(portName);
    addColumnObject(new ColumnObject(PORTNAME_NAME, sPortName));
  }

  public String getPortName()
  {
    return sPortName;
  }

  public void setDeviceID(String deviceID)
  {
    sDeviceID = checkForNull(deviceID);
    addColumnObject(new ColumnObject(DEVICEID_NAME, sDeviceID));
  }

  public String getDeviceID()
  {
    return sDeviceID;
  }

  public void setDirection(int direction)
  {
    iDirection = direction;
    addColumnObject(new ColumnObject(DIRECTION_NAME, Integer.valueOf(iDirection)));
  }

  public int getDirection()
  {
    return iDirection;
  }

  public void setLastSequence(int lastSequence)
  {
    iLastSequence = lastSequence;
    addColumnObject(new ColumnObject(LASTSEQUENCE_NAME, Integer.valueOf(iLastSequence)));
  }

  public int getLastSequence()
  {
    return iLastSequence;
  }

  public void setCommunicationMode(int communicationMode)
  {
    iCommunicationMode = communicationMode;
    addColumnObject(new ColumnObject(COMMUNICATIONMODE_NAME, Integer.valueOf(iCommunicationMode)));
  }

  public int getCommunicationMode()
  {
    return iCommunicationMode;
  }

  public void setServerName(String serverName)
  {
    sServerName = checkForNull(serverName);
    addColumnObject(new ColumnObject(SERVERNAME_NAME, sServerName));
  }

  public String getServerName()
  {
    return sServerName;
  }

  public void setSocketNumber(String socketNumber)
  {
    sSocketNumber = checkForNull(socketNumber);
    addColumnObject(new ColumnObject(SOCKETNUMBER_NAME, sSocketNumber));
  }

  public String getSocketNumber()
  {
    return sSocketNumber;
  }

  public void setRetryInterval(int inRetryInterval)
  {
    iRetryInterval = inRetryInterval;
    if (iRetryInterval < MINIMUM_INTERVAL)
    {
      iRetryInterval = MINIMUM_INTERVAL;
    }
    addColumnObject(new ColumnObject(RETRYINTERVAL_NAME, iRetryInterval));
  }

  public int getRetryInterval()
  {
    return iRetryInterval;
  }

  public void setSndKeepAliveInterval(int inSndKeepAliveInterval)
  {
    iSndKeepAliveInterval = inSndKeepAliveInterval;
    if (iSndKeepAliveInterval < MINIMUM_INTERVAL && iSndKeepAliveInterval > 0)
    {
      iSndKeepAliveInterval = MINIMUM_INTERVAL;
    }
    addColumnObject(new ColumnObject(SNDKEEPALIVEINTERVAL_NAME, iSndKeepAliveInterval));
  }

  public int getSndKeepAliveInterval()
  {
    return iSndKeepAliveInterval;
  }

  public void setRcvKeepAliveInterval(int inRcvKeepAliveInterval)
  {
    iRcvKeepAliveInterval = inRcvKeepAliveInterval;
    if (iRcvKeepAliveInterval < MINIMUM_INTERVAL && inRcvKeepAliveInterval > 0)
    {
      iRcvKeepAliveInterval = MINIMUM_INTERVAL;
    }
    addColumnObject(new ColumnObject(RCVKEEPALIVEINTERVAL_NAME, iRcvKeepAliveInterval));
  }

  public int getRcvKeepAliveInterval()
  {
    return iRcvKeepAliveInterval;
  }
	public boolean getEnableWrapping() {
		return iEnableWrapping == DEAFULT_ENABLEWRAPPING;
	}

	public void setEnableWrapping(int iEnableWrapping) {
		this.iEnableWrapping = iEnableWrapping;
		addColumnObject(new ColumnObject(ENABLEWRAPPING_NAME, Integer.valueOf(iEnableWrapping)));
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

    switch((PortEnum)vpEnum)
    {
      case COMMUNICATIONMODE:
        setCommunicationMode((Integer)ipColValue);
        break;

      case DEVICEID:
        setDeviceID((String)ipColValue);
        break;

      case DIRECTION:
        setDirection((Integer)ipColValue);
        break;

      case LASTSEQUENCE:
        setLastSequence((Integer)ipColValue);
        break;

      case PORTNAME:
        setPortName((String)ipColValue);
        break;

      case RCVKEEPALIVEINTERVAL:
        setRcvKeepAliveInterval((Integer)ipColValue);
        break;

      case RETRYINTERVAL:
        setRetryInterval((Integer)ipColValue);
        break;

      case SERVERNAME:
        setServerName((String)ipColValue);
        break;

      case SNDKEEPALIVEINTERVAL:
        setSndKeepAliveInterval((Integer)ipColValue);
        break;

      case ENABLEWRAPPING:
        setEnableWrapping((Integer)ipColValue);
        break;
          
      case SOCKETNUMBER:
        setSocketNumber((String)ipColValue);
    }

    return(0);
  }
}
