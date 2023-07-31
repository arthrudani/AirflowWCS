/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.store;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.InvalidDataException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * <B>Description:</B> Simple class for loading a captive rack.
 * <BR>Since I'm writing it, I get to specify the load naming convention!
 * <BR>BWAHAHAHAHA!
 *
 * @author       mandrus<BR>
 * @version      1.0
 *
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class LoadCaptiveRack extends DacInputFrame
{
  protected StandardLoadServer     mpLoadServer;
  protected StandardLocationServer mpLocServer;
  protected StandardPickServer     mpPickServer;
  protected StandardStationServer  mpStationServer;
  protected StandardDeviceServer   mpDeviceServer;

  protected StationComboBox mpCBStation;
  protected SKDCTextField   mpLoadID;

  public LoadCaptiveRack(String isTitle)
  {
    super(isTitle, "Store Information");
    buildScreen();
  }

  public LoadCaptiveRack()
  {
    this("");
  }

  protected void buildScreen()
  {
    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpLocServer = Factory.create(StandardLocationServer.class);
    mpPickServer = Factory.create(StandardPickServer.class);
    mpStationServer = Factory.create(StandardStationServer.class);
    mpDeviceServer = Factory.create(StandardDeviceServer.class);

    try
    {
      int[] vanInputStations = { DBConstants.USHAPE_OUT, DBConstants.PDSTAND,
          DBConstants.REVERSIBLE, DBConstants.INPUT };
      Map vpStations = mpStationServer.getStationsByStationType(vanInputStations);
      Object[] vapStations = vpStations.keySet().toArray();
      mpCBStation = new StationComboBox(vapStations);
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
      displayError(dbe.getMessage());
    }

    mpLoadID = new SKDCTextField(LoadData.LOADID_NAME);
    mpLoadID.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          okButtonPressed();
        }
      });

    addInput("Station", mpCBStation);
    addInput("Load ID", mpLoadID);

    mpBtnClear.setVisible(false);
  }

  /**
   * This creates an Arrival Pending load and guesses the to-location based
   * upon the station and load ID.
   */
  @Override
  protected void okButtonPressed()
  {
    /*
     * Get the Station, and make sure it's a captive insert
     */
    StationData vpSD = mpStationServer.getStation(mpCBStation.getSelectedStation());
    if (vpSD == null)
    {
      displayError("Something REALLY bad happened");
      return;
    }
    if ((vpSD.getCaptive() != DBConstants.CAPTIVE ||
         vpSD.getCaptive() != DBConstants.SEMICAPTIVE) &&
        vpSD.getStatus() != DBConstants.CAPTIVEINSERT)
    {
      displayError("Station is not configured for captive insert.");
      return;
    }

    /*
     * Make sure the load does not exist
     */
    String vsLoadID = mpLoadID.getText();
    LoadData vpLD = mpLoadServer.getLoad(vsLoadID);
    if (vpLD != null)
    {
      displayError("Load already exists!");
      return;
    }

    /*
     * Build location address using BarCodeIsLocation property in the
     * wrxj.properties file.
     */
    String vsNextAddress;
    try
    {
      int vnAisleGroup = mpDeviceServer.getDeviceAisleGroup(vpSD.getDeviceID());
      vsNextAddress = StandardLocationServer.getAddressFromLoadID(vsLoadID, vnAisleGroup);
      if (vsNextAddress.isEmpty())
      {
        displayInfo("Invalid Captive Load ID. Check " + SKDCConstants.EOL_CHAR +
                    "BarCodeIsLocationProperty in wrxj.properties");
        return;
      }

      LocationData vpLC = mpLocServer.getLocationRecord(vpSD.getWarehouse(), vsNextAddress);
      if (vpLC == null)
      {
        displayInfoAutoTimeOut("Location " + vpSD.getWarehouse() + "-"
            + vsNextAddress + " does not exist.");
        return;
      }
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
      displayError(dbe.getMessage());
      return;
    }
    catch(InvalidDataException eID)
    {
      logger.logException(eID);
      displayError(eID.getMessage());
      return;
    }

    try
    {
      createCaptiveLoad(vsLoadID, vsNextAddress, vpSD);
      String errMsg = mpPickServer.releaseLoad(vsLoadID, vpSD);
      if(errMsg != null)
      {
        displayError(errMsg);
      }
      else
      {
        displayInfoAutoTimeOut("Released " + vsLoadID + " to "
            + vpSD.getWarehouse() + "-" + vsNextAddress);
      }
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
      displayError(dbe.getMessage());
      return;
    }

    mpLoadID.setText("");
    mpLoadID.requestFocus();
  }

  protected void createCaptiveLoad(String isLoadID, String isNextAddress, StationData ipStnData)
            throws DBException
  {
    LoadData vpLDData = Factory.create(LoadData.class);
    vpLDData.setLoadID(isLoadID);
    vpLDData.setParentLoadID(isLoadID);
    vpLDData.setContainerType(ipStnData.getContainerType());
    vpLDData.setWarehouse(ipStnData.getWarehouse());
    vpLDData.setAddress(ipStnData.getStationName());
    vpLDData.setNextWarehouse(ipStnData.getWarehouse());
    vpLDData.setNextAddress(isNextAddress);
    vpLDData.setFinalWarehouse(ipStnData.getWarehouse());
    vpLDData.setFinalAddress(isNextAddress);
    vpLDData.setDeviceID(ipStnData.getDeviceID());
    vpLDData.setAmountFull(DBConstants.EMPTY);
    vpLDData.setLoadMoveStatus(DBConstants.ARRIVED);
    vpLDData.setHeight(ipStnData.getHeight());

    mpLoadServer.addLoad(vpLDData);
  }
}
