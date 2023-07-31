package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import com.daifukuamerica.wrxj.swingui.location.LocationUtilizationFrame;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <B>Description:</B> Display a Rack Fullness indicator.  One is needed 
 * per non-captive device.
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class FullnessButton extends PolygonButton
{
  protected final int DEFAULT_WARNING_LEVEL = 91;
  protected final int DEFAULT_ERROR_LEVEL = 96;
  
  protected final String WARNING_LEVEL = "WarningPercent"; 
  protected final String ERROR_LEVEL = "ErrorPercent"; 
  
  protected StandardLocationServer mpLocServer;
  protected int mnTotalLocations;
  protected int mnWarningLevel;
  protected int mnErrorLevel;
  protected String msToolTip;
  
  EquipmentMonitorFrame mpEMF;
  
  /**
   * Public constructor for Factory
   * 
   * @param ipEMF - Our Equipment Monitor Frame
   * @param inPanelIndex - The index of our panel
   * @param ipParent - Our panel
   * @param isGroupName - Our group name
   * @param isDeviceName - Our device name
   * @param ipPolygon - Our shape
   * @param ipProperties - Our properties
   * @param izCanTrack - Can we display tracking?
   * @param ipPermissions
   */
  public FullnessButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
    
    mpEMF = ipEMF;
  }
  
  /**
   * Set the graphics properties
   * 
   * @param ipProperties
   */
  @Override
  protected void setProperties(Map<String, String> ipProperties)
  {
    mnTotalLocations = -1;
    mnWarningLevel = DEFAULT_WARNING_LEVEL;
    mnErrorLevel = DEFAULT_ERROR_LEVEL;

    mnDefaultFontSize = 18;
    
    super.setProperties(ipProperties);
    mzCanTrack = false;

    refreshStatus();
    
    //  Since it'll be in a different thread the next time we need it, we'll
    //  set it to null and reinitialize it.
    mpLocServer = null;
  }
  
  /**
   * Build this graphic's pop-up menu
   * 
   * @param ipEMF
   * @param inPanelIndex
   * @param isGroupName
   * @param izCanTrack
   * @param ipPermissions
   */
  @Override
  protected void buildPopup(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      String isGroupName, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    if (!mzIsFuture && mzIsAssignedJVM)
    {
      mpPopup = new SKDCPopupMenu();
      mpPopup.add("Refresh", "Refresh", new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
            refreshStatus();
          }
        });
    }
  }

  /**
   * Handle additional parameters, if any
   * 
   * For <code>FullnessButton</code>, the graphic parameter should be in the
   * format of xx,yy where xx is the warning percent and yy is the error 
   * percent.  Default is 90,95  
   */
  @Override
  public void setGraphicParameter(String isParameter)
  {
    super.setGraphicParameter(isParameter);
    
    /*
     * Validate the parameters after they are all processed
     */
    if (mnErrorLevel <= mnWarningLevel || mnWarningLevel <= 0)
    {
      handleParameterError(isParameter, null);
    }
  }

  /**
   * Assign a value to a parameter
   * 
   * @param isParameter
   * @param isValue
   * @return true if the parameter was assigned, false if invalid
   */
  @Override
  protected boolean assignParameter(String isParameter, String isValue)
  {
    if (isParameter.equals(WARNING_LEVEL))
    {
      mnWarningLevel = Integer.parseInt(isValue);
    }
    else if (isParameter.equals(ERROR_LEVEL))
    {
      mnErrorLevel = Integer.parseInt(isValue);
    }
    else
    {
      return super.assignParameter(isParameter, isValue);
    }
    return true;
  }
  
  
  /**
   * Handle problems processing the graphic parameters
   * 
   * @param isParameter
   * @param ipException
   */
  @Override
  protected void handleParameterError(String isParameter, Exception ipException)
  {
    super.handleParameterError(isParameter, ipException);
    
    mnWarningLevel = DEFAULT_WARNING_LEVEL;
    mnErrorLevel = DEFAULT_ERROR_LEVEL;
  }

  /**
   * Set the controlling device of the locations to be monitored
   */
  @Override
  public void setMCController(String isMCController)
  {
    super.setMCController(isMCController);

    mpLocServer = Factory.create(StandardLocationServer.class);
    try
    {
    	int vnTotalLoc = mpLocServer.getLocationCount(null, getMCController(), 
          0, DBConstants.LCASRS, 0, 0, -1);
    	int vnProhibitedLoc = mpLocServer.getLocationCount(null, getMCController(), 
          0, DBConstants.LCASRS, DBConstants.LCPROHIBIT, 0, -1);
      mnTotalLocations = vnTotalLoc - vnProhibitedLoc;
    }
    catch (DBException dbe)
    {
      System.err.println(dbe.getMessage());
    }
  }
  
  /*--------------------------------------------------------------------------*/
  @Override
  public void setStatus(int inStatus){}

  protected void setFullness(int inFullness)
  {
    if (mzIsFuture)
    {
      mpStatusColor = Color.LIGHT_GRAY;
      setStatusText("Future");
    }
    else if (!mzIsAssignedJVM)
    {
      mpStatusColor = Color.LIGHT_GRAY;
      setStatusText(msJVMNote);
    }
    else
    {
      mpStatusColor = Color.ORANGE;
      if (mnTotalLocations > 0)
      {
        if (inFullness >= mnErrorLevel)
          mpStatusColor = Color.RED;
        else if (inFullness >= mnWarningLevel)
          mpStatusColor = Color.YELLOW;
        else if (inFullness >= 0)
          mpStatusColor = Color.GREEN;
      }
      if (mpStatusColor == Color.ORANGE)
        setStatusText("?");
      else
        setStatusText(inFullness + "%");
    }

//    repaint();  Don't need this because setStatusText() repaints
  }
  
  /**
   * Set the tool-tip text
   */
  @Override
  public void setToolTipText(String text)
  {
    if (text != null)
    {
      msToolTip = text;
      super.setToolTipText(text + "  ("
          + new SimpleDateFormat("HH:mm:ss").format(new Date()) + ")");
    }
  }

  /**
   * Get the status for this graphic
   */
  @Override
  public int getStatus()
  {
    // Future and non-JVM are always "no status"
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      return NO_STATUS;
    }

    int vnStatus = EquipmentGraphic.UNKNOWN;
    if (mpStatusColor == Color.YELLOW)
    {
      vnStatus = EquipmentGraphic.ALARM;
    }
    else if (mpStatusColor == Color.RED)
    {
      vnStatus = EquipmentGraphic.ERROR;
    }
    else if (mpStatusColor == Color.GREEN)
    {
      vnStatus = EquipmentGraphic.ONLINE;
    }
    return vnStatus;
  }

  /**
   * This method is called on a double-click
   */
  @Override
  protected void displayTracking()
  {
    LocationUtilizationFrame vpLUF = Factory.create(
        LocationUtilizationFrame.class, getMCController());
    mpEMF.addSKDCInternalFrame(vpLUF);
  }
  
  /**
   * Refresh the status of the instantiated EquipmentGraphic object.
   * This method is to allow the equipment monitor to update based on local
   * WarehouseRx screen events.
   * 
   * @return status - the status
   */
  @Override
  public void refreshStatus()
  {
    int vnPercentFull = -1;
    // vnPercentFull = (int)(Math.random() * 100);
    if (mnTotalLocations > 0)
    {
      try
      {
        if (mpLocServer == null)
        {
          mpLocServer = Factory.create(StandardLocationServer.class);
        }
        int vnEmpties = mpLocServer.getLocationCount(null, getMCController(),
            0, DBConstants.LCASRS, DBConstants.LCAVAIL, DBConstants.UNOCCUPIED,
            -1);
        vnPercentFull = (mnTotalLocations - vnEmpties) * 100 / mnTotalLocations;
      }
      catch (DBException dbe)
      {
        System.err.println(msDeviceName + ": " + dbe.getMessage());
        mpLocServer = null;
      }
    }
    
    setFullness(vnPercentFull);
    setToolTipText(msToolTip);
  }
}
