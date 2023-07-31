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
package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.status.StatusModel;
import com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorStatus;
import com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorStatusData;
import com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorTracking;
import com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorTrackingData;
import com.daifukuamerica.wrxj.errorcodes.api.ErrorDescriptions;
import com.daifukuamerica.wrxj.errorcodes.api.ErrorGuide;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.swingui.equipment.properties.EquipmentMonitorProperties;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.wynright.wrxj.app.Wynsoft;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * <B>Description:</B> Server for persisting equipment status updates for
 * use by the web Equipment Monitor
 *
 * <P>Copyright (c) 2018 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class StandardStatusServer extends StandardServer
{
  private static final boolean PERSIST_STATUS = Application.getBoolean("StatusModel.PersistStatus", false);
  private static final boolean PERSIST_TRACKING = Application.getBoolean("StatusModel.PersistTracking", false);

  protected static final Map<String,ErrorDescriptions> mpErrorDescriptions = new HashMap<>();

  protected EquipmentMonitorStatus mpShapeHandler = Factory.create(EquipmentMonitorStatus.class);
  protected EquipmentMonitorTracking mpTrackHandler = Factory.create(EquipmentMonitorTracking.class);

  /**
   * Constructor
   */
  public StandardStatusServer()
  {
  }

  /**
   * Constructor
   *
   * @param isKeyName
   */
  public StandardStatusServer(String isKeyName)
  {
    super(isKeyName);
  }

  /**
   * Persist StatusModel information to the database
   *
   * @param isGraphicId
   * @param isMCID
   * @param isMCController
   * @param isMOSID
   * @param isMOSController
   * @param isErrorSet
   * @param isStationID
   * @param isDeviceID
   * @param isGraphicClass
   * @param isDescription
   * @param isVisibilityBehavior
   */
  public void addEquipment(String isGraphicId, String isMCID,
      String isMCController, String isMOSID, String isMOSController,
      String isErrorSet, String isStationID, String isDeviceID,
      String isGraphicClass, String isDescription, String isVisibilityBehavior)
  {
    if (PERSIST_STATUS)
    {
      TransactionToken tt = null;
      try
      {
        // No need to re-add
        if (mpShapeHandler.exists(isGraphicId))
        {
          // We can't reset the status to UNKNOWN here because some controllers
          // might request a copy of the status model (which results in this
          // getting called again) and some devices don't periodically publish
          // status (which results in that equipment getting stuck in UNKNOWN).
          return;
        }

        // This hasn't been persisted yet
        tt= startTransaction();

        EquipmentMonitorStatusData vpData = Factory.create(EquipmentMonitorStatusData.class);
        // Static Data
        vpData.setGraphicID(isGraphicId);
        vpData.setAltGraphicID(toAltGraphicId(isGraphicId));
        vpData.setDescription(isDescription.replaceAll("_", " "));
        vpData.setMCID(noneToNull(isMCID));
        vpData.setMCController(noneToNull(isMCController));
        vpData.setMOSID(noneToNull(isMOSID));
        vpData.setMOSController(noneToNull(isMOSController));
        vpData.setErrorSet(noneToNull(isErrorSet));
        vpData.setStationID(noneToNull(isStationID));
        vpData.setDeviceID(noneToNull(isDeviceID));
        if (SKDCUtility.isNotBlank(vpData.getMOSController())
            && !isGraphicClass.contains("Fullness")
            && !isGraphicClass.contains("Swap"))
        {
          vpData.setCanTrack(DBConstants.YES);
        }
        else
        {
          vpData.setCanTrack(DBConstants.NO);
        }
        String vsBehavior = noneToNull(isVisibilityBehavior);
        if (vsBehavior == null && !Wynsoft.isIntegrated())
        {
          vsBehavior = noneToNull(isGraphicClass);
        }
        vpData.setBehavior(vsBehavior);
        // Initialize Dynamic Data
        vpData.setStatusID(StatusEventDataFormat.STATUS_UNKNOWN.toUpperCase());
        vpData.setAddMethod("addEquipment");
        vpData.setModifyTime(new Date());


        mpShapeHandler.addElement(vpData);

        commitTransaction(tt);
      }
      catch (Exception e)
      {
        logException("Error adding [" + isGraphicId + "]", e);
      }
      finally
      {
        endTransaction(tt);
      }
    }
  }

  /**
   * Create an alternate, web-friendly Graphic ID
   *
   * @param isGraphicId
   * @return
   */
  protected String toAltGraphicId(String isGraphicId)
  {
    return isGraphicId.replaceAll("[~|:|\\\\|/]", "-");
  }

  /**
   * Convert the web alt graphic ID to an equipment status
   *
   * @param isAltGraphicId
   * @return
   * @throws DBException
   * @throws NoSuchElementException - if no record found
   */
  public EquipmentMonitorStatusData altToStatusData(String isAltGraphicId) throws DBException, NoSuchElementException
  {
      EquipmentMonitorStatusData vpKey = Factory.create(EquipmentMonitorStatusData.class);
      vpKey.setKey(EquipmentMonitorStatusData.ALTGRAPHICID_NAME, isAltGraphicId);

      EquipmentMonitorStatus vpHandler = Factory.create(EquipmentMonitorStatus.class);
      EquipmentMonitorStatusData vpData = vpHandler.getElement(vpKey, DBConstants.NOWRITELOCK);
      if (vpData == null)
      {
          throw new NoSuchElementException("No matching data found for [" + isAltGraphicId + "]");
      }
      return vpData;
  }

  /**
   * Reinitialize a graphic
   *
   * @param isGraphicId
   */
  protected void reinitialize(String isGraphicId)
  {
    if (PERSIST_STATUS)
    {
      TransactionToken tt = null;
      try
      {
        tt= startTransaction();

        EquipmentMonitorStatusData vpData = Factory.create(EquipmentMonitorStatusData.class);
        vpData.setStatusID(StatusEventDataFormat.STATUS_UNKNOWN.toUpperCase());
        vpData.setStatusText2(null);
        vpData.setErrorCode(null);
        vpData.setErrorText(null);
        vpData.setUpdMethod("reinitialize");
        vpData.setModifyTime(new Date());

        commitTransaction(tt);
      }
      catch (Exception e)
      {
        logException("Error", e);
      }
      finally
      {
        endTransaction(tt);
      }
    }
  }

  /**
   * Update for data coming from the MC port
   *
   * @param isMCController
   * @param isMCID
   * @param isStatus
   * @param isError
   */
  public void updateMCStatus(String isMCController, String isMCID,
      String isStatus, String isError)
  {
    updateStatus(EquipmentMonitorStatusData.MCCONTROLLER_NAME, isMCController,
        EquipmentMonitorStatusData.MCID_NAME, isMCID, isStatus, isError);
  }

  /**
   * Update for data coming from the MOS port
   *
   * @param isMOSController
   * @param isMOSID
   * @param isStatus
   * @param isError
   */
  public void updateMOSStatus(String isMOSController, String isMOSID,
      String isStatus, String isError)
  {
    updateStatus(EquipmentMonitorStatusData.MOSCONTROLLER_NAME, isMOSController,
        EquipmentMonitorStatusData.MOSID_NAME, isMOSID, isStatus, isError);
  }

  /**
   * Update for data coming from the MC port
   *
   * @param isKey1Name
   * @param isKey1Value
   * @param isKey2Name
   * @param isKey2Value
   * @param isStatus
   * @param isError
   */
  protected void updateStatus(String isKey1Name, String isKey1Value,
      String isKey2Name, String isKey2Value, String isStatus, String isError)
  {
    if (PERSIST_STATUS)
    {
      TransactionToken tt = null;
      try
      {
        tt= startTransaction();

        String[] vasStatusText = isStatus.split("\\|");
        String vsError = noneToNull(isError);

        EquipmentMonitorStatusData vpKey = Factory.create(EquipmentMonitorStatusData.class);
        vpKey.setKey(isKey1Name, isKey1Value);
        vpKey.setKey(isKey2Name, isKey2Value);

        vpKey.setErrorCode(vsError);
        if (vsError == null)
        {
          vpKey.setErrorText(null);
        }
        else
        {
          EquipmentMonitorStatusData vpData = mpShapeHandler.getElement(vpKey,
              DBConstants.NOWRITELOCK);
          if (vpData == null)
          {
            throw new DBException("No data found!");
          }
          vpKey.setErrorText(getErrorDescription(vpData.getErrorSet(), vsError));
        }
        vpKey.setStatusID(vasStatusText[0].toUpperCase());
        vpKey.setStatusText2(vasStatusText.length > 1 ? vasStatusText[1] : null);
        vpKey.setUpdMethod("updateMCStatus");
        vpKey.setModifyTime(new Date());
        mpShapeHandler.modifyElement(vpKey);

        commitTransaction(tt);
      }
      catch(NoSuchElementException nse)
      {
        // Don't propagate this
      }
      catch (Exception e)
      {
        logException("Error updating shape " + isKey1Name + "=[" + isKey1Value
            + "], " + isKey2Name + "=[" + isKey2Value + "]", e);
      }
      finally
      {
        endTransaction(tt);
      }
    }
  }

  /**
   * Get the error description
   *
   * @param isErrorSet
   * @param isErrorCode
   * @return
   */
  public String getErrorDescription(String isErrorSet, String isErrorCode)
  {
    initializeErrorSet(isErrorSet);
    String vsDesc = null;
    ErrorDescriptions vpDesc = mpErrorDescriptions.get(isErrorSet);
    if (vpDesc != null)
    {
      vsDesc = vpDesc.getDescription(isErrorCode);
    }
    if (SKDCUtility.isBlank(vsDesc))
    {
      return "Unknown " + isErrorSet + " error (" + isErrorCode + ")";
    }
    return vsDesc;
  }

  /**
   * Get the error description
   *
   * @param isErrorSet
   * @param isErrorCode
   * @return
   */
  public String getErrorGuidance(String isErrorSet, String isErrorCode)
  {
    initializeErrorSet(isErrorSet);
    String vsErrorGuide = null;
    ErrorDescriptions vpDesc = mpErrorDescriptions.get(isErrorSet);
    if (vpDesc != null)
    {
      if (vpDesc instanceof ErrorGuide)
      {
        URL vpGuideUrl = ((ErrorGuide) vpDesc).getGuide(isErrorCode);
        if (vpGuideUrl != null)
        {
          try (Scanner vpScanner = new Scanner(vpGuideUrl.openStream(),
              StandardCharsets.UTF_8.toString()))
          {
              vpScanner.useDelimiter("\\A");
              vsErrorGuide = vpScanner.hasNext() ? vpScanner.next() : "";
          }
          catch (IOException ioe)
          {
            ioe.printStackTrace();
            return "Unknown " + isErrorSet + " error (" + isErrorCode + ")";
          }
        }
      }
      else
        vsErrorGuide = vpDesc.getDescription(isErrorCode);
    }
    else
    {
      return "Unknown error set (" + isErrorSet + ")";
    }
    if (SKDCUtility.isBlank(vsErrorGuide))
    {
      return "Unknown " + isErrorSet + " error (" + isErrorCode + ")";
    }
    return vsErrorGuide;
  }

  /**
   * Convert *NONE* to null
   *
   * @param isVal
   * @return
   */
  protected String noneToNull(String isVal)
  {
    if (isVal == null || isVal.equals(StatusModel.NO_VALUE)
        || isVal.equals(StatusModel.UNKNOWN))
    {
      return null;
    }
    return isVal;
  }

  /**
   * Initialize an error set
   * @param isErrorSet
   */
  protected void initializeErrorSet(String isErrorSet)
  {
    if (isErrorSet != null)
    {
      if (!mpErrorDescriptions.containsKey(isErrorSet))
      {
        try
        {
          EquipmentMonitorProperties mpEquipProperties = new EquipmentMonitorProperties(mpLogger);
          String vsErrorClass = mpEquipProperties.getErrorClass(isErrorSet);
          ErrorDescriptions vpErrorDescriptions =
            (ErrorDescriptions)Class.forName(vsErrorClass).getDeclaredConstructor().newInstance();
          mpErrorDescriptions.put(isErrorSet, vpErrorDescriptions);
        }
        catch (Exception e)
        {
          logException("No Error Set for " + isErrorSet, e);
        }
      }
    }
  }

  /**
   * Delete Tracking
   *
   * @param isGraphicId
   */
  public void deleteTracking(String isGraphicId)
  {
    if (PERSIST_TRACKING)
    {
      TransactionToken tt = null;
      try
      {
        tt= startTransaction();
        mpTrackHandler.delete(isGraphicId);
        commitTransaction(tt);
      }
      catch (Exception e)
      {
        logException("Error", e);
      }
      finally
      {
        endTransaction(tt);
      }
    }
  }

  /**
   * Delete All Tracking for a Controller
   *
   * @param isControllerID
   */
  public void deleteAllTracking(String isControllerID)
  {
    if (PERSIST_TRACKING)
    {
      TransactionToken tt = null;
      try
      {
        tt= startTransaction();
        mpTrackHandler.deleteAll(isControllerID);
        commitTransaction(tt);
      }
      catch (Exception e)
      {
        logException("Error", e);
      }
      finally
      {
        endTransaction(tt);
      }
    }
  }

  /**
   * Add Tracking
   *
   * @param isGraphicId
   * @param isDeviceId
   * @param isTrackingId
   * @param isBarcode
   * @param isStatus
   * @param isOrigin
   * @param isDestination
   * @param isSize
   */
  public void addTracking(String isGraphicId, String isDeviceId,
      String isTrackingId, String isBarcode, String isStatus, String isOrigin,
      String isDestination, String isSize)
  {
    if (PERSIST_TRACKING)
    {
      TransactionToken tt = null;
      try
      {
        tt= startTransaction();
        EquipmentMonitorTrackingData vpData = Factory.create(EquipmentMonitorTrackingData.class);
        vpData.setGraphicID(isGraphicId);
        vpData.setDeviceID(isDeviceId);
        vpData.setTrackingID(isTrackingId);
        vpData.setBarcode(isBarcode);
        vpData.setStatus(isStatus);
        vpData.setOrigin(isOrigin);
        vpData.setDestination(isDestination);
        vpData.setSize(isSize);
        mpTrackHandler.addElement(vpData);
        commitTransaction(tt);
      }
      catch (Exception e)
      {
        logException("Error", e);
      }
      finally
      {
        endTransaction(tt);
      }
    }
  }

  /**
   * Convert the tracking ID to tracking data
   *
   * @param isDeviceId
   * @param isTrackingId
   * @return
   * @throws DBException
   * @throws NoSuchElementException - if no record found
   */
  public EquipmentMonitorTrackingData toTrackingData(String isDeviceId,
      String isTrackingId) throws DBException, NoSuchElementException
  {
    EquipmentMonitorTrackingData vpKey = Factory.create(EquipmentMonitorTrackingData.class);
    vpKey.setKey(EquipmentMonitorTrackingData.GRAPHICID_NAME, isDeviceId);
    vpKey.setKey(EquipmentMonitorTrackingData.TRACKINGID_NAME, isTrackingId);

    EquipmentMonitorTracking vpTrackHandler = Factory.create(EquipmentMonitorTracking.class);
    EquipmentMonitorTrackingData vpData = vpTrackHandler.getElement(vpKey, DBConstants.NOWRITELOCK);
    if (vpData == null)
    {
      throw new NoSuchElementException(
          "No matching data found for [" + isTrackingId + "]");
    }
    return vpData;
  }

//  public void xxx()
//  {
//    TransactionToken tt = null;
//    try
//    {
//      tt= startTransaction();
//
//      commitTransaction(tt);
//    }
//    catch (Exception e)
//    {
//      logException("Error", e);
//    }
//    finally
//    {
//      endTransaction(tt);
//    }
//  }
}
