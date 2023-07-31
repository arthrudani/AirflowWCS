package com.daifukuamerica.wrxj.swingui.station;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;

/**
 * <B>Description:</B> Provide a combo list of stations with a default station 
 * pre-selected.<BR>
 * 
 * @author Michael Andrus
 * @version 1.0
 * 
 * <BR>Copyright (c) 2005-2008 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class StationComboBox extends SKDCComboBox
{
  public static final int[] manInputTypes = new int[] { DBConstants.USHAPE_IN,
                                                        DBConstants.PDSTAND,
                                                        DBConstants.REVERSIBLE,
                                                        DBConstants.INPUT };
  public static final int[] manOutputTypes = new int[] { DBConstants.USHAPE_OUT,
                                                         DBConstants.PDSTAND,
                                                         DBConstants.OUTPUT,
                                                         DBConstants.REVERSIBLE,
                                                         DBConstants.CONSOLIDATION,
                                                         DBConstants.SHIPPING };
  
  private String msOpType = "";

  /*========================================================================*/
  /*  Constructors                                                          */
  /*========================================================================*/
  public StationComboBox()
  {
    super();
  }
  
  public StationComboBox(String isOpType)
  {
    this();
    setOpType(isOpType);
  }

  public StationComboBox(Object[] iapItems)
  {
    this(iapItems, "");
  }

  public StationComboBox(Object[] iapItems, String isOpType)
  {
    super(iapItems);
    if (isOpType.isEmpty() == false)
    {
      setOpType(isOpType);
    }
    selectDefaultStation();
  }
  
  public StationComboBox(List ipItems)
  {
    this(ipItems.toArray());
  }
  
  public StationComboBox(List ipItems, String isOpType)
  {
    this(ipItems.toArray(), isOpType);
  }
  
  /*========================================================================*/
  /*  Overridden Methods                                                    */
  /*========================================================================*/

  /**
   * Populate the combo box.
   * @param iapItems
   * 
   * @see com.daifukuamerica.wrxj.swing.SKDCComboBox#setComboBoxData(java.lang.Object[])
   */
  @Override
  public void setComboBoxData(Object[] iapItems)
  {
    super.setComboBoxData(iapItems);
    selectDefaultStation();
  }
  
  /*========================================================================*/
  /*  Public Methods                                                        */
  /*========================================================================*/
  
  /**
   * Get the selected station
   * @return
   */
  public String getSelectedStation()
  {
    String vsStation = getText();
    if (vsStation.trim().length() > 0)
    {
      String[] vasStnAndDesc = vsStation.split(SKDCConstants.DESCRIPTION_SEPARATOR);
      if (vasStnAndDesc != null && vasStnAndDesc.length > 0)
      {
        vsStation = vasStnAndDesc[0].trim();
      }
    }

/*
    int vnStationLength = DBInfo.getFieldLength(StationData.STATIONNAME_NAME);
    if (vsStation.trim().length() > vnStationLength)
      vsStation = vsStation.substring(0, vnStationLength);
 */
    return vsStation;
  }
  
  /**
   * Set the selected station
   * @param isStation
   */
  public void setSelectedStation(String isStation)
  {
    if (isStation.trim().length() == 0)
      resetDefaultSelection();
    else
      selectItemBy(isStation);
  }

  /**
   * Fills an instance of this object with input stations. This method is JVM
   * dependent for split systems.
   * 
   * @param isPrepender prepender for the list (should be
   *          SKDCConstants.ALL_STRING, NONE_STRING, EMPTY_VALUE, or
   *          NO_PREPENDER)
   * @throws DBException
   */
  public void fillWithInputs(String isPrepender) throws DBException
  {
    fill(manInputTypes, isPrepender);
  }

  /**
   * Method gets all input stations in the system regardless of which JVM it
   * belongs to.
   * 
   * @param isPrepender prepender for the list (should be
   *          SKDCConstants.ALL_STRING, NONE_STRING, EMPTY_VALUE, or
   *          NO_PREPENDER)
   */
  public void fillWithAllInputs(String isPrepender) throws DBException
  {
    fillAll(manInputTypes, isPrepender);
  }

  /**
   * Fills the combo with output-able stations. This method is JVM dependent for
   * split systems.
   * 
   * @param isPrepender prepender for the list (should be
   *          SKDCConstants.ALL_STRING, NONE_STRING, EMPTY_VALUE, or
   *          NO_PREPENDER)
   * @throws DBException
   */
  public void fillWithOutputs(String isPrepender) throws DBException
  {
    fill(manOutputTypes, isPrepender);
  }

  /**
   * Method gets all output stations in the system regardless of which JVM it
   * belongs to.
   * 
   * @param isPrepender prepender for the list (should be
   *          SKDCConstants.ALL_STRING, NONE_STRING, EMPTY_VALUE, or
   *          NO_PREPENDER)
   */
  public void fillWithAllOutputs(String isPrepender) throws DBException
  {
    fillAll(manOutputTypes, isPrepender);
  }

  /**
   * Fills the combo with stations
   * 
   * @param ianStationTypes array of station types to lookup.
   * @param isPrepender prepender for the list (should be
   *          SKDCConstants.ALL_STRING, NONE_STRING, EMPTY_VALUE, or
   *          NO_PREPENDER)
   * @throws DBException
   */
  public void fill(int ianStationTypes[], String isPrepender) throws DBException
  {
    StandardStationServer vpStationServer = Factory.create(StandardStationServer.class);
    Map vpStationsMap = vpStationServer.getStationsByStationType(
        ianStationTypes, isPrepender);
    Object[] vapStations = vpStationsMap.keySet().toArray();
    setComboBoxData(vapStations);
  }

  /**
   * Fill the combo with stations from all available JVMs
   * 
   * @param ianStationTypes array of station types to lookup.
   * @param isPrepender prepender for the list (should be
   *          SKDCConstants.ALL_STRING, NONE_STRING, EMPTY_VALUE, or
   *          NO_PREPENDER)
   * @throws DBException
   */
  public void fillAll(int ianStationTypes[], String isPrepender)
      throws DBException
  {
    StandardStationServer vpStationServer = Factory.create(StandardStationServer.class);
    Map vpStationsMap = vpStationServer.getAllSystemStations(ianStationTypes,
        isPrepender);
    Object[] vapStations = vpStationsMap.keySet().toArray();
    setComboBoxData(vapStations);
  }

  /**
   * set Operation Type for the default station
   */
  protected void setOpType(String isOpType)
  {
    msOpType = isOpType;
  }

  /**
   * set the Operation Type to be Store.
   */
  public void setStoreOpType()
  {
    setOpType(SysConfig.OPTYPE_STORE);
  }

  /**
   * Set the Operation Type to be Pick.
   */
  public void setPickOpType()
  {
    setOpType(SysConfig.OPTYPE_PICK);
  }

  /**
   * Select the default station based the Operation Type.
   */
  public void selectDefaultStation()
  {
    selectDefaultStation(msOpType);
  }

  /**
   * Select the default station based upon:
   * <BR> 1. SysConfig
   * <BR> 2. LocalWorkStation
   */
  protected void selectDefaultStation(String isOpType)
  {
    try
    {
      // Check SysConfig first
      SysConfig mpSC = Factory.create(SysConfig.class);
      String vsStation = mpSC.getDefaultStation(SKDCUserData.getMachineName(),
          SKDCUserData.getIPAddress(), isOpType, SKDCUserData.isSuperUser());
      // There is no SysConfig entry--use LocalWorkStation
      if (vsStation.length() == 0)
      {
        vsStation = LocalWorkStation.getStation();
        addItemListener(new ItemListener()
          {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
              if (e.getStateChange() == ItemEvent.SELECTED)
              {
                LocalWorkStation.setStation(getSelectedStation());
              }
            }
          });
      }
      // If we have a station, try to select it
      if (vsStation != null)
      {
        setSelectedStation(vsStation);
      }
    }
    catch (DBException dbe)
    {
      /*
       * Should we display an error? If there is one, it's likely that the whole
       * system is down anyway.
       */
      Logger.getLogger().logException(dbe);
    }
  }
}
