package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.controller.status.StatusModel;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * <B>Description:</B> Display an image
 * 
 * <P>
 * Copyright (c) 2009 by Daifuku America Corporation
 * </P>
 * 
 * @author mandrus
 * @version 1.0
 */
public class GraphicButton extends JComponent implements EquipmentGraphic
{
  static final String RESOURCE = "Resource";
  
  /*
   * Parameters for GraphicParam 
   */
  protected String msDeviceName = null;
  protected GroupPanel mpParent;
  protected String mcController = null;
  protected String mosController = null;
  protected String msErrorCode = null;
  protected String msErrorClass = null;
  protected String msErrorSet = null;
  protected String msStationID = null;
  protected String msDeviceID = null;
  protected String msMOSID = null;

  protected Polygon mpPolygon;
  protected Rectangle mpRectangle;
  protected Color mpStatusColor = null;
  protected String msJVMNote = "";
  
  Image mpImage;
  
  /**
   * Constructor
   * 
   * @param ipEMF - Our Equipment Monitor Frame
   * @param inPanelIndex - The index of our panel
   * @param ipParent - Our panel
   * @param isGroupName - Our group name
   * @param isDeviceName - Our device name
   * @param ipPolygon - Our shape
   * @param ipProperties - Our properties
   * @param izCanTrack - Can we display tracking?
   * @param ipPermissions - the screen permissions
   */
  public GraphicButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super();

    mpParent = ipParent;
    mpPolygon = ipPolygon;
    msDeviceName = isDeviceName;
    
    setOpaque(true);

    mpRectangle = new Rectangle(mpPolygon.getBounds());
    mpRectangle.grow(1, 1);

    setBounds(mpRectangle);
    mpPolygon.translate(-mpRectangle.x, -mpRectangle.y);

    setProperties(ipProperties);

    setStatus(EquipmentGraphic.PRISTINE);
  }

  /**
   * Set the graphics properties
   * 
   * @param ipProperties
   */
  protected void setProperties(Map<String, String> ipProperties)
  {
    String vsProperty = ipProperties.get(StatusModel.DEVICE_ID);
    if (!vsProperty.equals(StatusModel.UNKNOWN) && 
        !vsProperty.equals(StatusModel.NO_VALUE))
    {
      setDeviceID(vsProperty);
    }

    vsProperty = ipProperties.get(StatusModel.STATION_ID);
    if (!vsProperty.equals(StatusModel.UNKNOWN) && 
        !vsProperty.equals(StatusModel.NO_VALUE))
    {
      setStationId(vsProperty);
    }

    vsProperty = ipProperties.get(StatusModel.MC_CONTROLLER);
    if (!vsProperty.equals(StatusModel.UNKNOWN) && 
        !vsProperty.equals(StatusModel.NO_VALUE))
    {
      setMCController(vsProperty);
    }

    vsProperty = ipProperties.get(StatusModel.MOS_CONTROLLER);
    if (!vsProperty.equals(StatusModel.UNKNOWN) && 
        !vsProperty.equals(StatusModel.NO_VALUE))
    {
      setMOSController(vsProperty);
    }

    vsProperty = ipProperties.get(StatusModel.ERROR_SET);
    if (!vsProperty.equals(StatusModel.UNKNOWN) && 
        !vsProperty.equals(StatusModel.NO_VALUE))
    {
      setErrorSet(vsProperty);
    }

    setErrorClass(ipProperties.get(PolygonButton.ERROR_CLASS));

    vsProperty = ipProperties.get(StatusModel.GRAPHIC_PARAMETER);
    if (!vsProperty.equals(StatusModel.UNKNOWN) && 
        !vsProperty.equals(StatusModel.NO_VALUE))
    {
      setGraphicParameter(vsProperty);
    }
  }
    
  /**
   * Get the name of the Graphic
   */
  public String getGraphicName()
  {
    return msDeviceName;
  }

  /**
   * Set the Device ID
   */
  public void setDeviceID(String isDeviceID)
  {
    msDeviceID = isDeviceID;
  }

  /**
   * Get the Device ID
   */
  public String getDeviceID()
  {
    return msStationID;
  }

  /**
   * The station ID is really the bank number
   */
  @Override
  public void setStationId(String s)
  {
    setStatusText(s);
  }

  /**
   * Get the Station ID
   */
  @Override
  public String getStationId()
  {
    return msStationID;
  }

  /**
   * Set the MOS ID
   */
  public void setMOSID(String s)
  {
    msMOSID = s;
  }

  /**
   * Get the MOS ID
   */
  public String getMOSID()
  {
    return msMOSID;
  }

  /**
   * Get this graphic's parent
   */
  public JComponent getGraphicParent()
  {
    return mpParent;
  }

  /**
   * Handle additional parameters, if any
   */
  public void setGraphicParameter(String isParameter)
  {
    if (!isParameter.equals(StatusModel.UNKNOWN))
    {
      try
      {
        StringTokenizer vpST = new StringTokenizer(isParameter, ",:");
        while (vpST.hasMoreTokens())
        {
          String vsParam = vpST.nextToken();
          String vsValue = vpST.nextToken();
          if (!assignParameter(vsParam, vsValue))
          {
            handleParameterError(isParameter + ": Error at " + vsParam, null);
          }
        }
      }
      catch (Exception e)
      {
        handleParameterError(isParameter, e);
      }
    }
  }
    
  /**
   * Assign a value to a parameter
   * 
   * @param isParameter
   * @param isValue
   * @return true if the parameter was assigned, false if invalid
   */
  protected boolean assignParameter(String isParameter, String isValue)
  {
    if (isParameter.equals(RESOURCE))
    {
      mpImage = new ImageIcon(getClass().getResource(isValue)).getImage();
      
      mpRectangle = new Rectangle(mpRectangle.x, mpRectangle.y,
          mpImage.getWidth(this), mpImage.getHeight(this));
      mpRectangle.grow(1, 1);

      setBounds(mpRectangle);
    }
    return true;
  }

  /**
   * Handle problems processing the graphic parameters
   * 
   * @param isParameter
   * @param ipException
   */
  protected void handleParameterError(String isParameter, Exception ipException)
  {
    String vsErrorMsg = "Invalid graphic parameter for " + getToolTipText()
    + ": " + isParameter;
    System.err.println(vsErrorMsg);
    if (ipException != null)
    {
      ipException.printStackTrace();
    }
  }

  /**
   * Is this graphic for future equipment?
   */
  @Override
  public boolean isFutureExpansion()
  {
    return false;
  }

  /**
   * Is this graphic for this JVM?
   */
  @Override
  public boolean isAssignedJVM()
  {
    return true;
  }

  /**
   * Allow us to adjust offsets for scale
   * 
   * @param inX
   * @param inY
   */
  public void setScale(int inX, int inY)
  {

  }

  /*--------------------------------------------------------------------------*/
  public void setErrorCode(String isErrorCode)
  {
    isErrorCode = isErrorCode.trim();
    if (isErrorCode.length() == 0)
    {
      isErrorCode = null;
    }
    msErrorCode = isErrorCode;
  }

  public String getErrorCode()
  {
    return msErrorCode;
  }

  /*--------------------------------------------------------------------------*/
  public void setErrorClass(String isErrorClass)
  {
    msErrorClass = isErrorClass;
  }

  public String getErrorClass()
  {
    return msErrorClass;
  }

  /*--------------------------------------------------------------------------*/
  public void setErrorSet(String isErrorSet)
  {
    msErrorSet = isErrorSet;
  }

  public String getErrorSet()
  {
    return msErrorSet;
  }

  /*--------------------------------------------------------------------------*/
  public void setStatusText(String isStatusText)
  {
    repaint();
  }

  /*--------------------------------------------------------------------------*/
  public void setMCController(String isMCController)
  {
    mcController = isMCController;
  }

  public String getMCController()
  {
    return mcController;
  }

  /*--------------------------------------------------------------------------*/
  public void setMOSController(String isMosController)
  {
    mosController = isMosController;
  }

  public String getMOSController()
  {
    return mosController;
  }

  @Override
  public void paintComponent(Graphics g)
  {
    g.drawImage(mpImage, 0, 0, this);
  }
    
  /**
   * Get the proper border color
   * 
   * @param ipBorderColor
   * @return
   */
  protected Color getBorderColor(Color ipBorderColor)
  {
    return ipBorderColor;
  }
    
  /**
   * Get the proper fill color
   * 
   * @param ipFillColor
   * @return
   */
  protected Color getFillColor(Color ipFillColor)
  {
    return ipFillColor;
  }
    
  /**
   * Get the proper border color (currently only changed for future equipment)
   * 
   * @param ipTextColor
   * @return
   */
  protected Color getTextColor(Color ipTextColor)
  {
    return ipTextColor;
  }

  /**
   * 
   */
  @Override
  public Dimension getPreferredSize()
  {
    return new Dimension(mpRectangle.width, mpRectangle.height);
  }

  /**
   * Refresh the status of the instantiated EquipmentGraphic object.
   * This method is to allow the equipment monitor to update based on local
   * WarehouseRx screen events.
   * 
   * @return status - the status
   */
  public void refreshStatus()
  {
    /*
     * At the moment, the baseline default behavior is to do nothing.
     */
  }

  /*========================================================================*/
  /*  Tracking-related methods                                              */
  /*========================================================================*/

  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic#setTracking(java.util.List)
   */
  @Override
  public void setTracking(List<String[]> ipTracking)
  {
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic#setTrackingVisible(boolean)
   */
  @Override
  public void setTrackingVisible(boolean izTrackingMode)
  {
  }

  /**
   * Get the count for the tracking
   * 
   * @return
   */
  protected String getTrackingCount()
  {
    return "";
  }

  /**
   * Display the tracking for only this graphic
   */
  protected void displayTracking()
  {
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic#clearTrackingFilter()
   */
  @Override
  public void clearTrackingFilter()
  {
  }

  /**
   * Currently no status.
   */
  @Override
  public void setStatus(int inStatus) {}
  
  /**
   * Currently no status.
   */
  @Override
  public void setStatusNoRepaint(int inStatus) {}
  
  /**
   * The image is always online for equipment monitor
   */
  public int getStatus()
  {
    return ONLINE;
  }

  /**
   * No tool tip.
   */
  @Override
  public void setToolTipText(String text) {}
}
