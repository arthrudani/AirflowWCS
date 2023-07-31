package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.status.StatusModel;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import com.daifukuamerica.wrxj.swingui.equipment.popup.EquipmentGraphicPopupMenu;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * <B>Description:</B> Default graphic class for the Equipment Monitor
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
@SuppressWarnings("serial")
public class PolygonButton extends JComponent 
    implements MouseListener, MouseMotionListener, EquipmentGraphic
{
  /*
   * Class for error display
   */
  public static String ERROR_CLASS = "ERROR_CLASS";
  
  /*
   * Colors
   */
  public static Color BORDER_COLOR = Color.BLACK;
  public static Color TRACKING_COLOR = Color.WHITE;

  // Note for future equipment
  public static String FUTURE_NOTE = " (FUTURE EXPANSION)";
  
  /*
   * Parameters for GraphicParam 
   */
  protected static final String PADDING="Padding";
  protected static final String X_OFFSET="X_Offset";
  protected static final String Y_OFFSET="Y_Offset";
  protected static final String FUTURE="Future";
  protected static final String JVMID="JVMID";
  protected static final String ROTATE_TEXT="RotateText";
  
  protected String msStatusText = null;
  protected String msStatusText2 = null;
  protected int mnCenterX = 0;
  protected int mnCenterY = 0;
  protected int mnTextPadding = 2;
  protected int mnXOffset = 0;
  protected int mnYOffset = 0;
  
  protected String msDeviceName = null;
  protected GroupPanel mpParent;
  protected SKDCPopupMenu mpPopup = null;
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
  protected boolean mzIsActive;
  protected Color mpStatusColor = null;
  protected boolean mzIsFuture = false;
  protected boolean mzIsAssignedJVM = true;
  protected String msJVMNote = "";
  protected boolean mzRotateText = false;
  
  protected static PolygonButton mpCurrentButton = null;
  protected int mnDefaultFontSize = 12;

  // Tracking display
  protected boolean mzCanTrack;
  protected boolean mzShowTracking = false;
  private List<String[]> mpTrackingData = null;
  private boolean mzIsFiltering = false;
  
  /**
   * Empty constructor for graphic w/o properties
   */
  public PolygonButton() {}
  
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
  public PolygonButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super();
    mpParent = ipParent;
    mpPolygon = ipPolygon;
    msDeviceName = isDeviceName;

    setToolTipText(isDeviceName);
    setOpaque(false);

    mpParent.addMouseMotionListener(this);
    addMouseListener(this);

    mpRectangle = new Rectangle(mpPolygon.getBounds());
    mpRectangle.grow(1, 1);

    setBounds(mpRectangle);
    mpPolygon.translate(-mpRectangle.x, -mpRectangle.y);
    //
    mnCenterX = mpRectangle.width/2;
    mnCenterY = mpRectangle.height/2;
    
    setProperties(ipProperties);

    setStatus(EquipmentGraphic.PRISTINE);
    
    buildPopup(ipEMF, inPanelIndex, isGroupName, izCanTrack, ipPermissions);
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
    
    setErrorClass(ipProperties.get(ERROR_CLASS));
    
    vsProperty = ipProperties.get(StatusModel.GRAPHIC_PARAMETER);
    if (!vsProperty.equals(StatusModel.UNKNOWN) && 
        !vsProperty.equals(StatusModel.NO_VALUE))
    {
      setGraphicParameter(vsProperty);
    }
    
    mzCanTrack = false;
    vsProperty = ipProperties.get(StatusModel.MOS_ID);
    if (!vsProperty.equals(StatusModel.UNKNOWN) && 
        !vsProperty.equals(StatusModel.NO_VALUE))
    {
      setMOSID(vsProperty);
      
      // Tracking - these don't have it
      if (!(vsProperty.contains(":101") || vsProperty.contains(":103") ||
            vsProperty.contains(":104") || vsProperty.contains(":192") || 
            vsProperty.contains(":199")))
      {
        mzCanTrack = true;
      }
    }
    
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
  protected void buildPopup(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      String isGroupName, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    if (!mzIsFuture && mzIsAssignedJVM)
    {
      mpPopup = Factory.create(EquipmentGraphicPopupMenu.class, ipEMF,
          inPanelIndex, isGroupName, this, izCanTrack, ipPermissions);
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
    return msDeviceID;
  }

  /**
   * Set the Station ID
   */
  public void setStationId(String s)
  {
    msStationID = s;
    setToolTipText(msDeviceName + "  (" + msStationID + ")");
  }

  /**
   * Get the Station ID
   */
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
    
    if (mzIsFuture)
    {
      setToolTipText(getToolTipText() + FUTURE_NOTE);
      mpPopup = null;
    }
    else if (!mzIsAssignedJVM)
    {
      setToolTipText(getToolTipText() + msJVMNote);
      mpPopup = null;
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
    if (isParameter.equals(PADDING))
    {
      mnTextPadding = Integer.parseInt(isValue);
    }
    else if (isParameter.equals(X_OFFSET))
    {
      mnXOffset = Integer.parseInt(isValue);
    }
    else if (isParameter.equals(Y_OFFSET))
    {
      mnYOffset = Integer.parseInt(isValue);
    }
    else if (isParameter.equals(FUTURE))
    {
      mzIsFuture = Boolean.parseBoolean(isValue);
    }
    else if (isParameter.equals(ROTATE_TEXT))
    {
      mzRotateText = Boolean.parseBoolean(isValue);
    }
    else if (isParameter.equals(JVMID))
    {
      msJVMNote = isValue;
      mzIsAssignedJVM = isValue.equals(
          Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY));
    }
    else
    {
      return false;
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
    return mzIsFuture;
  }
  
  /**
   * Is this graphic for this JVM?
   */
  @Override
  public boolean isAssignedJVM()
  {
    return mzIsAssignedJVM;
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
  
  /**
   * Handle future equipment
   */
  @Override
  public void setToolTipText(String text)
  {
    if (mzIsFuture)
    {
      if (!text.endsWith(FUTURE_NOTE))
        text += FUTURE_NOTE;
    }
    else if (!mzIsAssignedJVM)
    {
      if (!text.contains(msJVMNote))
        text += " (" + msJVMNote + ")";
    }
    super.setToolTipText(text);
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
    isStatusText = isStatusText.trim();
    if (isStatusText.length() == 0)
    {
      isStatusText = null;
      msStatusText2 = null;
    }
    else
    {
      int newLine = isStatusText.indexOf('|');
      if (newLine != -1)
      {
        msStatusText2 = isStatusText.substring(newLine + 1);
        isStatusText = isStatusText.substring(0, newLine);
      }
    }
    msStatusText = isStatusText;
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

  /*--------------------------------------------------------------------------*/
  public void setStatus(int inStatus)
  {
    Color vpColor = STATUS_COLORS[ALARM];
    
    try
    {
      vpColor = STATUS_COLORS[inStatus];
    }
    catch (ArrayIndexOutOfBoundsException aioobe) {}
    
    if (mpStatusColor != vpColor)
    {
      mpStatusColor = vpColor;
      repaint();
    }
  }
  public void setStatusNoRepaint(int inStatus)
  {
    Color vpColor = STATUS_COLORS[ALARM];
    
    try
    {
      vpColor = STATUS_COLORS[inStatus];
    }
    catch (ArrayIndexOutOfBoundsException aioobe) {}
    
    if (mpStatusColor != vpColor)
    {
      mpStatusColor = vpColor;
    }
  }

  /*--------------------------------------------------------------------------*/
  public int getStatus()
  {
    // Future and non-JVM are always "no status"
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      return NO_STATUS;
    }

    // Everything else is by color
    for (int i = 0; i < STATUS_COLORS.length; i++)
    {
      if (mpStatusColor == STATUS_COLORS[i])
      {
        return i;
      }
    }
    
    // Something Bad (tm) happened
    return UNKNOWN;
  }

  /*--------------------------------------------------------------------------*/
  public void mouseMoved(MouseEvent e)
  {
    e = SwingUtilities.convertMouseEvent((java.awt.Component)e.getSource(), e, this);
    int x = e.getX();// - m_rc.x;
    int y = e.getY();// - m_rc.y;
    boolean vzIsActive = mpPolygon.contains(x, y);

    if (mzIsActive != vzIsActive)
      setState(vzIsActive);
    if (mzIsActive)
      e.consume();
  }

  public void mouseDragged(MouseEvent e) {}

  protected void setState(boolean izIsActive)
  {
    if (izIsActive)
    {
      if ((mpCurrentButton != null) && (mpCurrentButton != this))
        mpCurrentButton.setState(false); // recursive!
      mpCurrentButton = this;
    }
    else
    {
      mpCurrentButton = null;
    }
    mzIsActive = izIsActive;
  }

  public void mouseClicked(MouseEvent e)
  {
  }

  public void mousePressed(MouseEvent e)
  {
    e = SwingUtilities.convertMouseEvent((java.awt.Component)e.getSource(), e, this);
    if (e.isConsumed() || e.getID() == MouseEvent.MOUSE_PRESSED)
    {
      return;
    }
    int x = e.getX();// - m_rc.x;
    int y = e.getY();// - m_rc.y;
    boolean vzActive = mpPolygon.contains(x, y);
    if ((vzActive) && (e.isPopupTrigger()) && (mpPopup != null))
    {
      mpPopup.show(e.getComponent(), e.getX(), e.getY());
    }
    else if (vzActive && SwingUtilities.isLeftMouseButton(e))
    {
      if (e.getClickCount() % 2 == 0)
      {
        displayTracking();
      }
    }
  }

  public void mouseReleased(MouseEvent e)
  {
    mousePressed(e);
  }

  public void mouseExited(MouseEvent e)
  {
    mouseMoved(e);
  }

  public void mouseEntered(MouseEvent e)
  {
    mouseMoved(e);
  }

  @Override
  public void paintComponent(Graphics g)
  {
    drawPolygon(g);
    if (mzShowTracking)
    {
      drawText(g, "" + getTrackingCount(), null);
    }
    else if (mzRotateText)
    {
      if (msStatusText != null && msStatusText2 != null)
      {
        drawVerticalText(g, msStatusText + " " + msStatusText2);
      }
      else
      {
        drawVerticalText(g, msStatusText);
      }
    }
    else
    {
      if (msStatusText2 != null && mpRectangle.getHeight() < 25)
      {
        drawText(g, msStatusText + " " + msStatusText2, null);
      }
      else
      {
        drawText(g, msStatusText, msStatusText2);
      }
    }
  }
  
  /**
   * Get the proper border color
   * 
   * @param ipBorderColor
   * @return
   */
  protected Color getBorderColor(Color ipBorderColor)
  {
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      return Color.LIGHT_GRAY;
    }
    else
    {
      return ipBorderColor;
    }
  }
  
  /**
   * Get the proper fill color
   * 
   * @param ipFillColor
   * @return
   */
  protected Color getFillColor(Color ipFillColor)
  {
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      return getBackground();
    }
    else if (mzShowTracking)
    {
      return TRACKING_COLOR;
    }
    else
    {
      return ipFillColor;
    }
  }
  
  /**
   * Get the proper border color (currently only changed for future equipment)
   * 
   * @param ipTextColor
   * @return
   */
  protected Color getTextColor(Color ipTextColor)
  {
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      return Color.LIGHT_GRAY;
    }
    else if (mzShowTracking)
    {
      return Color.BLACK;
    }
    else
    {
      if (mpStatusColor == Color.BLUE ||
          mpStatusColor == Color.DARK_GRAY ||
          mpStatusColor == Color.RED ||
          mpStatusColor == DAIFUKU_PURPLE)
      {
        return Color.WHITE;
      }
    }
    return ipTextColor;
  }
  
  /**
   * Draw the actual shape
   * @param g
   */
  protected void drawPolygon(Graphics g)
  {
    // Color
    g.setColor(getFillColor(mpStatusColor));
    g.fillPolygon(mpPolygon);

    // Border
    g.setColor(getBorderColor(BORDER_COLOR));
    g.drawPolygon(mpPolygon);
  }
  
  /**
   * Draw the text in the shape
   * @param g
   */
  protected void drawText(Graphics g, String isText1, String isText2)
  {
    g.setColor(getTextColor(BORDER_COLOR));
    
    Font font = g.getFont();
    int fStyle = font.getStyle();
    int fSize = mnDefaultFontSize;
    font = font.deriveFont(fStyle, fSize);
    g.setFont(font);
    int statusTextWidth = 0;
    FontMetrics fontMetrics = null;
    int statusTextWidthCenter = 0;
    int statusTextHeightCenter = 0;
    int statusY = 0;
    if (isText1 != null)
    {
      while (true)
      {
        fontMetrics = g.getFontMetrics();
        Rectangle2D vpRectangle2D =  fontMetrics.getStringBounds(isText1, g);
        statusTextWidthCenter = (int)vpRectangle2D.getCenterX()-1;
        statusTextHeightCenter = (int)vpRectangle2D.getCenterY()-1;
        statusTextWidth = fontMetrics.stringWidth(isText1);
        if ((statusTextWidth < (mpRectangle.width-mnTextPadding)) || (fSize <= 3))
        {
          break;
        }
        font = g.getFont();
        fStyle = font.getStyle();
        fSize = font.getSize();
        fSize--;
        font = font.deriveFont(fStyle, fSize);
        g.setFont(font);
      }
      statusY = mnCenterY;
      if (isText2 != null) statusTextHeightCenter = 2;
      
      g.drawString(isText1, mnCenterX - statusTextWidthCenter + mnXOffset,
          mnCenterY - statusTextHeightCenter + mnYOffset);

      if (isText2 != null)
      {
        int lineHeight = fontMetrics.getHeight();
        
        font = g.getFont();
        fStyle = font.getStyle();
        font = font.deriveFont(fStyle, mnDefaultFontSize);
        g.setFont(font);
        //
        statusTextWidth = 0;
        fSize = mnDefaultFontSize;
        fontMetrics = null;
        while (true)
        {
          fontMetrics = g.getFontMetrics();
          Rectangle2D vpRectangle2D =  fontMetrics.getStringBounds(isText2, g);
          statusTextWidthCenter = (int)vpRectangle2D.getCenterX();
          statusTextHeightCenter = (int)vpRectangle2D.getCenterY();
          statusTextWidth = fontMetrics.stringWidth(isText2);
          if ((statusTextWidth < (mpRectangle.width-mnTextPadding)) || (fSize <= 3))
          {
            break;
          }
          font = g.getFont();
          fStyle = font.getStyle();
          fSize = font.getSize();
          fSize--;
          font = font.deriveFont(fStyle, fSize);
          g.setFont(font);
        }
        g.drawString(isText2, mnCenterX - statusTextWidthCenter
            + mnXOffset, statusY + lineHeight - 3 + mnYOffset);
      }
    }
  }

  /**
   * Draw the text vertically in the shape.
   * 
   * <P><I>Note: drawVerticalText() does not render the second line of status 
   * text.</I></P>
   * 
   * @param g
   */
  protected void drawVerticalText(Graphics g, String isText1)
  {
    g.setColor(getTextColor(BORDER_COLOR));
    
    Font font = g.getFont();
    int fStyle = font.getStyle();
    int fSize = mnDefaultFontSize;
    font = font.deriveFont(fStyle, fSize);
    g.setFont(font);
    int statusTextWidth = 0;
    FontMetrics fontMetrics = null;
    int statusTextWidthCenter = 0;
    int statusTextHeightCenter = 0;
    if (isText1 != null)
    {
      while (true)
      {
        fontMetrics = g.getFontMetrics();
        Rectangle2D vpRectangle2D =  fontMetrics.getStringBounds(isText1, g);
        statusTextWidthCenter = (int)vpRectangle2D.getCenterX()-1;
        statusTextHeightCenter = (int)vpRectangle2D.getCenterY()-1;
        statusTextWidth = fontMetrics.stringWidth(isText1);
        if ((statusTextWidth < (mpRectangle.height-mnTextPadding)) || (fSize <= 3))
        {
          break;
        }
        font = g.getFont();
        fStyle = font.getStyle();
        fSize = font.getSize();
        fSize--;
        font = font.deriveFont(fStyle, fSize);
        g.setFont(font);
      }
      
      int vnX = mnCenterX - statusTextHeightCenter + mnXOffset;
      int vnY = mnCenterY - statusTextWidthCenter - mnYOffset;
      ((Graphics2D)g).translate(vnX, mpRectangle.height - vnY);
      ((Graphics2D)g).rotate( -Math.PI / 2 );
      g.drawString(isText1, 0, 0);
     }
  }

  
  /**
   * Draw rack labels
   * 
   * @param g - how to draw
   * @param r - where to draw
   * @param s - what to draw
   */
  protected void drawTextInRectangle(Graphics g, Rectangle r, String s)
  {
    Font font = g.getFont();
    int fStyle = font.getStyle();
    int fSize = mnDefaultFontSize;
    font = font.deriveFont(fStyle, fSize);
    g.setFont(font);
    
    int statusTextWidth = 0;
    FontMetrics fontMetrics = null;
    int vnTextWidthCenter = 0;
    int vnTextHeightCenter = 0;
    if (s != null)
    {
      while (true)
      {
        fontMetrics = g.getFontMetrics();
        Rectangle2D vpRectangle2D =  fontMetrics.getStringBounds(s, g);
        vnTextWidthCenter = (int)vpRectangle2D.getCenterX();
        vnTextHeightCenter = (int)vpRectangle2D.getCenterY()-1;
        statusTextWidth = fontMetrics.stringWidth(s);
        if ((statusTextWidth < (r.width-2)) || (fSize <= 3))
        {
          break;
        }
        font = g.getFont();
        fStyle = font.getStyle();
        fSize = font.getSize();
        fSize--;
        font = font.deriveFont(fStyle, fSize);
        g.setFont(font);
      }
      int vnX = r.x + (r.width-2)/2 - vnTextWidthCenter;
      if (vnX <= r.x+1) vnX = r.x+2;  // Try to keep it off of the edge
      g.drawString(s, vnX, r.y + r.height/2 - vnTextHeightCenter);
    }
  }

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
    mpTrackingData = ipTracking;
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic#setTrackingVisible(boolean)
   */
  @Override
  public void setTrackingVisible(boolean izTrackingMode)
  {
    if (mzCanTrack)
    {
      mzShowTracking = izTrackingMode;
      mzIsFiltering = false;
    }
  }
  
  /**
   * Get the count for the tracking
   * 
   * @return
   */
  protected String getTrackingCount()
  {
    if (mzIsFiltering)
      return "View";
    else if (mpTrackingData != null)
      return "" + mpTrackingData.size();
    else
      return "0";
  }
 
  /**
   * Display the tracking for only this graphic
   */
  protected void displayTracking()
  {
    if (mzShowTracking)
    {
      if (mzIsFiltering)
      {
        // Stop filtering
        mpParent.clearTrackingFilters();
      }
      else 
      {
        // Start filtering
        mpParent.clearTrackingFilters();
        mpParent.setTrackingFilter(msDeviceName);
        mzIsFiltering = true;
      }
    }
    repaint();
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic#clearTrackingFilter()
   */
  @Override
  public void clearTrackingFilter()
  {
    if (mzIsFiltering)
    {
      mzIsFiltering = false;
      repaint();
    }
  }
}