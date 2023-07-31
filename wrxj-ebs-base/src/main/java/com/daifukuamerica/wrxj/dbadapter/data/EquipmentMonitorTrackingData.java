/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorTrackingEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold EQUIPMENTMONITORTRACKING data
 */
public class EquipmentMonitorTrackingData extends AbstractSKDCData
{
  /*------------------------------------------------------------------------*/
  /* Column names                                                           */
  /*------------------------------------------------------------------------*/
  public static final String BARCODE_NAME           = BARCODE.getName();
  public static final String DESTINATION_NAME       = DESTINATION.getName();
  public static final String DEVICEID_NAME          = DEVICEID.getName();
  public static final String GRAPHICID_NAME         = GRAPHICID.getName();
  public static final String ORIGIN_NAME            = ORIGIN.getName();
  public static final String SIZE_NAME              = SIZE.getName();
  public static final String STATUS_NAME            = STATUS.getName();
  public static final String TRACKINGID_NAME        = TRACKINGID.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /*------------------------------------------------------------------------*/
  /* Table column data                                                      */
  /*------------------------------------------------------------------------*/
  private String msEMBarcode;
  private String msEMDestination;
  private String msEMDeviceID;
  private String msEMGraphicID;
  private String msEMOrigin;
  private String msEMSize;
  private String msEMStatus;
  private String msEMTrackingID;

  /*------------------------------------------------------------------------*/
  /* Default column values                                                  */
  /*------------------------------------------------------------------------*/
  
  /**
   * Constructor
   */
  public EquipmentMonitorTrackingData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, EquipmentMonitorTrackingEnum.class);
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
  public boolean equals(AbstractSKDCData absOther)
  {
    EquipmentMonitorTrackingData other = (EquipmentMonitorTrackingData)absOther;
    return other.getGraphicID().equals(getGraphicID())
        && other.getTrackingID().equals(getTrackingID());
  }


  /*------------------------------------------------------------------------*/
  /* Getters                                                                */
  /*------------------------------------------------------------------------*/
  public String getBarcode()           {  return msEMBarcode;             }
  public String getDestination()       {  return msEMDestination;         }
  public String getDeviceID()          {  return msEMDeviceID;            }
  public String getGraphicID()         {  return msEMGraphicID;           }
  public String getOrigin()            {  return msEMOrigin;              }
  public String getSize()              {  return msEMSize;                }
  public String getStatus()            {  return msEMStatus;              }
  public String getTrackingID()        {  return msEMTrackingID;          }


  /*------------------------------------------------------------------------*/
  /* Setters                                                                */
  /*------------------------------------------------------------------------*/
  public void setBarcode(String isEMBarcode)
  {
    msEMBarcode = isEMBarcode;
    addColumnObject(new ColumnObject(BARCODE_NAME, msEMBarcode));
  }
  public void setDestination(String isEMDestination)
  {
    msEMDestination = isEMDestination;
    addColumnObject(new ColumnObject(DESTINATION_NAME, msEMDestination));
  }
  public void setDeviceID(String isEMDeviceID)
  {
    msEMDeviceID = isEMDeviceID;
    addColumnObject(new ColumnObject(DEVICEID_NAME, msEMDeviceID));
  }
  public void setGraphicID(String isEMGraphicID)
  {
    msEMGraphicID = isEMGraphicID;
    addColumnObject(new ColumnObject(GRAPHICID_NAME, msEMGraphicID));
  }
  public void setOrigin(String isEMOrigin)
  {
    msEMOrigin = isEMOrigin;
    addColumnObject(new ColumnObject(ORIGIN_NAME, msEMOrigin));
  }
  public void setSize(String isEMSize)
  {
    msEMSize = isEMSize;
    addColumnObject(new ColumnObject(SIZE_NAME, msEMSize));
  }
  public void setStatus(String isEMStatus)
  {
    msEMStatus = isEMStatus;
    addColumnObject(new ColumnObject(STATUS_NAME, msEMStatus));
  }
  public void setTrackingID(String isEMTrackingID)
  {
    msEMTrackingID = isEMTrackingID;
    addColumnObject(new ColumnObject(TRACKINGID_NAME, msEMTrackingID));
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
    
    switch ((EquipmentMonitorTrackingEnum)vpEnum)
    {
      case BARCODE:
        setBarcode((String)ipColValue);
        break;
      case DESTINATION:
        setDestination((String)ipColValue);
        break;
      case DEVICEID:
        setDeviceID((String)ipColValue);
        break;
      case GRAPHICID:
        setGraphicID((String)ipColValue);
        break;
      case ORIGIN:
        setOrigin((String)ipColValue);
        break;
      case SIZE:
        setSize((String)ipColValue);
        break;
      case STATUS:
        setStatus((String)ipColValue);
        break;
      case TRACKINGID:
        setTrackingID((String)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
