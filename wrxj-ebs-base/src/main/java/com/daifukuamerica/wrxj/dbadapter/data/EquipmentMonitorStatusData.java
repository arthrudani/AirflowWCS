/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorStatusEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold EQUIPMENTMONITORSTATUS data
 */
public class EquipmentMonitorStatusData extends AbstractSKDCData
{
  public static final String ALTGRAPHICID_NAME      = ALTGRAPHICID.getName();
  public static final String BEHAVIOR_NAME          = BEHAVIOR.getName();
  public static final String CANTRACK_NAME          = CANTRACK.getName();
  public static final String DESCRIPTION_NAME       = DESCRIPTION.getName();
  public static final String DEVICEID_NAME          = DEVICEID.getName();
  public static final String ERRORCODE_NAME         = ERRORCODE.getName();
  public static final String ERRORSET_NAME          = ERRORSET.getName();
  public static final String ERRORTEXT_NAME         = ERRORTEXT.getName();
  public static final String GRAPHICID_NAME         = GRAPHICID.getName();
  public static final String MCCONTROLLER_NAME      = MCCONTROLLER.getName();
  public static final String MCID_NAME              = MCID.getName();
  public static final String MOSCONTROLLER_NAME     = MOSCONTROLLER.getName();
  public static final String MOSID_NAME             = MOSID.getName();
  public static final String STATIONID_NAME         = STATIONID.getName();
  public static final String STATUSID_NAME          = STATUSID.getName();
  public static final String STATUSTEXT1_NAME       = STATUSTEXT1.getName();
  public static final String STATUSTEXT2_NAME       = STATUSTEXT2.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  // ------------------- EquipmentMonitorShape table data -----------------------------
  private String msAltGraphicID;
  private String msBehavior;
  private int    miCanTrack;
  private String msDescription;
  private String msDeviceID;
  private String msErrorCode;
  private String msErrorSet;
  private String msErrorText;
  private String msGraphicID;
  private String msMCController;
  private String msMCID;
  private String msMOSController;
  private String msMOSID;
  private String msStationID;
  private String msStatusID;
  private String msStatusText1;
  private String msStatusText2;

  //-------------------- EquipmentMonitorShape default data ---------------------------
  public EquipmentMonitorStatusData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, EquipmentMonitorStatusEnum.class);
  }

  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer(getClass().getCanonicalName()).append("\n");
    String[] vasKeys = mpColumnMap.keySet().toArray(new String[0]);
    Arrays.sort(vasKeys);
    for (String sKey : vasKeys) {
      ColumnObject vpVal = getColumnObject(sKey);
      String vsVal = vpVal == null ? null : 
        vpVal.getColumnValue() == null ? null : vpVal.getColumnValue().toString();
      myString.append(" * ").append(sKey).append(" = ").append(vsVal).append(";\n");
    }
    return myString.toString();
  }

  @Override
  public void clear()
  {
    msAltGraphicID = "";
    msBehavior = "";
    miCanTrack = 0;
    msDescription = "";
    msDeviceID = "";
    msErrorCode = "";
    msErrorSet = "";
    msErrorText = "";
    msGraphicID = "";
    msMCController = "";
    msMCID = "";
    msMOSController = "";
    msMOSID = "";
    msStationID = "";
    msStatusID = "";
    msStatusText1 = "";
    msStatusText2 = "";

    super.clear();
  }
  
  @Override
  public boolean equals(AbstractSKDCData absOther)
  {
    EquipmentMonitorStatusData other = (EquipmentMonitorStatusData)absOther;
    return other.getGraphicID().equals(getGraphicID());
  }


/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public String getAltGraphicID()      {  return msAltGraphicID;        }
  public String getBehavior()          {  return msBehavior;            }
  public int    getCanTrack()          {  return miCanTrack;            }
  public String getDescription()       {  return msDescription;         }
  public String getDeviceID()          {  return msDeviceID;            }
  public String getErrorCode()         {  return msErrorCode;           }
  public String getErrorSet()          {  return msErrorSet;            }
  public String getErrorText()         {  return msErrorText;           }
  public String getGraphicID()         {  return msGraphicID;           }
  public String getMCController()      {  return msMCController;        }
  public String getMCID()              {  return msMCID;                }
  public String getMOSController()     {  return msMOSController;       }
  public String getMOSID()             {  return msMOSID;               }
  public String getStationID()         {  return msStationID;           }
  public String getStatusID()          {  return msStatusID;            }
  public String getStatusText1()       {  return msStatusText1;         }
  public String getStatusText2()       {  return msStatusText2;         }


/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setAltGraphicID(String isAltGraphicID)
  {
    msAltGraphicID = isAltGraphicID;
    addColumnObject(new ColumnObject(ALTGRAPHICID_NAME, msAltGraphicID));
  }
  public void setBehavior(String isBehavior)
  {
    msBehavior = isBehavior;
    addColumnObject(new ColumnObject(BEHAVIOR_NAME, msBehavior));
  }
  public void setCanTrack(int inCanTrack)
  {
    miCanTrack = inCanTrack;
    addColumnObject(new ColumnObject(CANTRACK_NAME, miCanTrack));
  }
  public void setDescription(String isDescription)
  {
    msDescription = isDescription;
    addColumnObject(new ColumnObject(DESCRIPTION_NAME, msDescription));
  }
  public void setDeviceID(String isDeviceID)
  {
    msDeviceID = isDeviceID;
    addColumnObject(new ColumnObject(DEVICEID_NAME, msDeviceID));
  }
  public void setErrorCode(String isErrorCode)
  {
    msErrorCode = isErrorCode;
    addColumnObject(new ColumnObject(ERRORCODE_NAME, msErrorCode));
  }
  public void setErrorSet(String isErrorSet)
  {
    msErrorSet = isErrorSet;
    addColumnObject(new ColumnObject(ERRORSET_NAME, msErrorSet));
  }
  public void setErrorText(String isErrorText)
  {
    msErrorText = isErrorText;
    addColumnObject(new ColumnObject(ERRORTEXT_NAME, msErrorText));
  }
  public void setGraphicID(String isGraphicID)
  {
    msGraphicID = isGraphicID;
    addColumnObject(new ColumnObject(GRAPHICID_NAME, msGraphicID));
  }
  public void setMCController(String isMCController)
  {
    msMCController = isMCController;
    addColumnObject(new ColumnObject(MCCONTROLLER_NAME, msMCController));
  }
  public void setMCID(String isMCID)
  {
    msMCID = isMCID;
    addColumnObject(new ColumnObject(MCID_NAME, msMCID));
  }
  public void setMOSController(String isMOSController)
  {
    msMOSController = isMOSController;
    addColumnObject(new ColumnObject(MOSCONTROLLER_NAME, msMOSController));
  }
  public void setMOSID(String isMOSID)
  {
    msMOSID = isMOSID;
    addColumnObject(new ColumnObject(MOSID_NAME, msMOSID));
  }
  public void setStationID(String isStationID)
  {
    msStationID = isStationID;
    addColumnObject(new ColumnObject(STATIONID_NAME, msStationID));
  }
  public void setStatusID(String isStatusID)
  {
    msStatusID = isStatusID;
    addColumnObject(new ColumnObject(STATUSID_NAME, msStatusID));
  }
  public void setStatusText1(String isStatusText1)
  {
    msStatusText1 = isStatusText1;
    addColumnObject(new ColumnObject(STATUSTEXT1_NAME, msStatusText1));
  }
  public void setStatusText2(String isStatusText2)
  {
    msStatusText2 = isStatusText2;
    addColumnObject(new ColumnObject(STATUSTEXT2_NAME, msStatusText2));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null) 
    {
      return super.setField(isColName, ipColValue);
    }
    
    switch ((EquipmentMonitorStatusEnum)vpEnum)
    {
      case ALTGRAPHICID:
        setAltGraphicID((String)ipColValue);
        break;
      case BEHAVIOR:
        setBehavior((String)ipColValue);
        break;
      case CANTRACK:
        setCanTrack((Integer)ipColValue);
        break;
      case DESCRIPTION:
        setDescription((String)ipColValue);
        break;
      case DEVICEID:
        setDeviceID((String)ipColValue);
        break;
      case ERRORCODE:
        setErrorCode((String)ipColValue);
        break;
      case ERRORSET:
        setErrorSet((String)ipColValue);
        break;
      case ERRORTEXT:
        setErrorText((String)ipColValue);
        break;
      case GRAPHICID:
        setGraphicID((String)ipColValue);
        break;
      case MCCONTROLLER:
        setMCController((String)ipColValue);
        break;
      case MCID:
        setMCID((String)ipColValue);
        break;
      case MOSCONTROLLER:
        setMOSController((String)ipColValue);
        break;
      case MOSID:
        setMOSID((String)ipColValue);
        break;
      case STATIONID:
        setStationID((String)ipColValue);
        break;
      case STATUSID:
        setStatusID((String)ipColValue);
        break;
      case STATUSTEXT1:
        setStatusText1((String)ipColValue);
        break;
      case STATUSTEXT2:
        setStatusText2((String)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
