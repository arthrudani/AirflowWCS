package com.daifukuamerica.wrxj.swingui.rackusage;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;

/**
 * <B>Description:</B> Class for a bank of locations for the RackUsage screen
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class RackUsageBankTier extends JComponent
{
  public static final String ADDRESS_CLICKED = "ADDRESS_CLICKED";
  public static final String LOAD_CLICKED = "LOAD_CLICKED";
  
  protected final int BANK_START_X = 25;
  protected int mnBankEndX = 700;
  protected int[] manY;
  
  protected  String msWarehouse = "";
  protected String msBank = "";
  protected int mnBays = 0;
  protected String msTier = "";
  protected Polygon mpBank;
  protected Polygon[] mapBays;
  protected int[] manStatuses;
  protected int[] manSpeeds;
  protected int mnMinSearchOrder = -1;
  protected int mnMaxSearchOrder = -1;
  protected boolean mzShowBank = false;
  protected boolean mzShowTier = false;
  protected RackInfoCombo mpDisplaySelector;
  
  protected StandardLocationServer mpLocServer;
  SKDCInternalFrame mpParentFrame;  // For launching new screens

  /**
   * Constructor
   * 
   * @param x
   * @param y
   * @param width
   * @param height
   * @param izShowBank - if true, draw the bank number
   * @param izShowTier - if true and izShowBank is false, draw the tier number
   * @param ipPCL - Property change listener to handle ADDRESS_CLICKED event
   * @param ipDisplaySelector - specify what to display
   */
  public RackUsageBankTier(int x, int y, int width, int height,
      boolean izShowBank, boolean izShowTier, PropertyChangeListener ipPCL,
      RackInfoCombo ipDisplaySelector)
  {
    super();
    setBounds(x, y, width, height);
    mnBankEndX = width - BANK_START_X;
    manY = new int[] {0, 0, height-1, height-1};
    mpBank = new Polygon(
        new int[] { BANK_START_X, mnBankEndX, mnBankEndX, BANK_START_X }, 
        manY, 4);
    mpLocServer = Factory.create(StandardLocationServer.class);
    
    mzShowBank = izShowBank;
    mzShowTier = izShowTier;
    
    mpDisplaySelector = ipDisplaySelector;
    
    addMouseListener(new RackUsageMouseListener());
    addMouseMotionListener(new RackUsageMouseListener());
    addPropertyChangeListener(ipPCL);
  }
  
  /**
   * @see java.awt.Component#paint(java.awt.Graphics)
   */
  @Override
  public void paint(Graphics g)
  {
    g.setColor(EquipmentGraphic.DAIFUKU_LIGHT_PURPLE);
    g.fillPolygon(mpBank);
    
    g.setColor(Color.BLACK);
    g.drawPolygon(mpBank);
    
    if (mzShowBank && msBank != null)
    {
      g.drawString(msBank, 0, getHeight()/2 + 5);
      g.drawString(msBank, getWidth()-20, getHeight()/2 + 5);
    }
    else if (mzShowTier && msTier != null)
    {
      g.drawString(msTier, 0, getHeight()/2 + 5);
      g.drawString(msTier, getWidth()-20, getHeight()/2 + 5);
    }
    
    if (mapBays != null)
    {
      if (mpDisplaySelector.isSpeedSelected())
      {
        for (int i = 0; i < mapBays.length; i++)
        {
          g.setColor(RackSpeedLegend.getSpeedColor(manSpeeds[i],
              mnMinSearchOrder, mnMaxSearchOrder));
          g.fillPolygon(mapBays[i]);
          
          g.setColor(Color.BLACK);
          g.drawPolygon(mapBays[i]);
        }
      }
      else
      {
        for (int i = 0; i < mapBays.length; i++)
        {
          switch (manStatuses[i])
          {
            case DBConstants.ERROR:
              g.setColor(RackUsageLegend.RU_ERROR);
              break;
            case DBConstants.OCCUPIED:
              g.setColor(RackUsageLegend.RU_OCCUPIED);
              break;
            case DBConstants.UNOCCUPIED:
              g.setColor(RackUsageLegend.RU_UNOCCUPIED);
              break;
            case DBConstants.LC_SWAP:
              g.setColor(RackUsageLegend.RU_SWAP);
              break;
            case DBConstants.LC_DDMOVE:
            case DBConstants.LCRESERVED:
              g.setColor(RackUsageLegend.RU_DDMOVE_RESERVED);
              break;
            case DBConstants.LCPROHIBIT:
              g.setColor(RackUsageLegend.RU_PROHIBIT);
              break;
            case DBConstants.LCUNAVAIL:
              g.setColor(RackUsageLegend.RU_UNAVAILABLE);
              break;
            default:
              g.setColor(RackUsageLegend.RU_UNKNOWN);
          }
          g.fillPolygon(mapBays[i]);
          
          g.setColor(Color.BLACK);
          g.drawPolygon(mapBays[i]);
        }
      }
    }
  }

  /**
   * Clear the data
   */
  public void clearData()
  {
    mapBays = null;
    msWarehouse = null;
    msBank = null;
    msTier = null;
  }
  
  /**
   * Set the bank and bay information
   * 
   * @param isWarehouse
   * @param inBank
   * @param inBays
   */
  public void setData(String isWarehouse, int inBank, int inBays)
  {
    msWarehouse = isWarehouse;
    msBank = zeroPad(inBank);

    mnBays = inBays;
    double vnBayWidth = (double)(mnBankEndX - BANK_START_X) / (double)mnBays;
    
    double vdX = BANK_START_X;
    // Draw the bays
    mapBays = new Polygon[mnBays];
    manStatuses = new int[mnBays];
    manSpeeds = new int[mnBays];
    for (int i = 0; i < mnBays; i++)
    {
      double vdEndX = vdX + vnBayWidth;
      mapBays[i] = new Polygon(
          new int[] {(int)vdX, (int)vdEndX, (int)vdEndX, (int)vdX}, manY, 4);
      vdX = vdEndX;
    }

    // Re-draw the bank
    mpBank = new Polygon(new int[] { BANK_START_X, (int)vdX, (int)vdX,
        BANK_START_X }, manY, 4);
  }
  
  /**
   * Get actual statuses for a given tier
   * 
   * @param inTier
   */
  public void getTierInfo(int inTier)
  {
    msTier = zeroPad(inTier);

    mnMinSearchOrder = -1;
    for (int i = 0; i < mnBays; i++)
    {
      String vsAddress = msBank + zeroPad(i+1) + msTier;
      try
      {
        LocationData vpLocData = mpLocServer.getLocationRecord(msWarehouse,
            vsAddress);
        if (vpLocData == null)
        {
          Logger.getLogger().logError(
              msWarehouse + "-" + vsAddress + " does not exist!");
          manStatuses[i] = DBConstants.UNKNOWN;
          manSpeeds[i] = -1;
        }
        else
        {
          if (mnMinSearchOrder < 0)
          {
            getSearchOrderMinMax(vpLocData.getDeviceID());
          }

          if (vpLocData.getLocationStatus() == DBConstants.LCAVAIL)
          {
            manStatuses[i] = vpLocData.getEmptyFlag();
            manSpeeds[i] = vpLocData.getSearchOrder();
          }
          else
          {
            manStatuses[i] = vpLocData.getLocationStatus();
            manSpeeds[i] = -vpLocData.getLocationStatus();
          }
        }
      }
      catch (DBException dbe)
      {
        manStatuses[i] = DBConstants.ERROR;
        manSpeeds[i] = -1;
      }
    }
    repaint();
  }

  /**
   * Get the min/max search order for a device
   * 
   * @param isDeviceID
   */
  protected void getSearchOrderMinMax(String isDeviceID)
  {
    try
    {
      LocationData vpLocKey = Factory.create(LocationData.class);
      vpLocKey.setKey(LocationData.DEVICEID_NAME, isDeviceID);
      vpLocKey.setKey(LocationData.LOCATIONTYPE_NAME, DBConstants.LCASRS);
      vpLocKey.addOrderByColumn(LocationData.SEARCHORDER_NAME);
      List<Map> vpResultList = mpLocServer.getLocationData(vpLocKey);
      mnMinSearchOrder = (Integer)vpResultList.get(0).get(LocationData.SEARCHORDER_NAME);

      vpLocKey.clearOrderByColumns();
      vpLocKey.addOrderByColumn(LocationData.SEARCHORDER_NAME, true);
      vpResultList = mpLocServer.getLocationData(vpLocKey);
      mnMaxSearchOrder = (Integer)vpResultList.get(0).get(LocationData.SEARCHORDER_NAME);
    }
    catch (DBException dbe)
    {
      mnMinSearchOrder = 0;
      mnMaxSearchOrder = 100;
      dbe.printStackTrace();
    }
  }
  
  /**
   * Zero-pad a bank, bay or tier
   * 
   * @param inBankBayOrTier
   * @return
   */
  protected String zeroPad(int inBankBayOrTier)
  {
    if (inBankBayOrTier < 10)
    {
      return "00" + inBankBayOrTier;
    }
    else if (inBankBayOrTier < 100)
    {
      return "0" + inBankBayOrTier;
    }
    return "" + inBankBayOrTier;
  }
  
  /**
   * <B>Description:</B> Mouse listener for the tier
   *
   * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
   *
   * @author       mandrus
   * @version      1.0
   */
  protected class RackUsageMouseListener extends MouseAdapter
  {
    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
      mouseClickedEvent( e);
    }
   
    /**
     * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e)
    {
      mouseMovedEvent(e);
    } 
  }
  /*
   * created a separate method so it could be extended
   */
  protected  void mouseClickedEvent(MouseEvent e)
  {
    if (e.getClickCount() == 2 && mapBays != null)
    {
      for (int i = 0; i < mapBays.length; i++)
      {
        if (mapBays[i].contains(e.getPoint()))
        {
          String vsAddress = msBank + zeroPad(i + 1) + msTier;
          firePropertyChange(ADDRESS_CLICKED, null, msWarehouse + "-"
              + vsAddress);
          break;
        }
      }
    }
  }
  /*
   * created a separate method so it could be extended
   */
  
  protected void mouseMovedEvent(MouseEvent e)
  {
    boolean vzInvalidLocation = true;
    if (mapBays != null)
    {
      for (int i = 0; i < mapBays.length; i++)
      {
        if (mapBays[i].contains(e.getPoint()))
        {
          String vsAddress = msBank + zeroPad(i + 1) + msTier;
          setToolTipText(msWarehouse + "-" + vsAddress 
              + ((mpDisplaySelector.isSpeedSelected()) ? 
                  " (#" + manSpeeds[i] + ")" : ""));
          vzInvalidLocation = false;
          break;
        }
      }
    }
    if (vzInvalidLocation)
    {
      setToolTipText(null);
    }
  }
}
