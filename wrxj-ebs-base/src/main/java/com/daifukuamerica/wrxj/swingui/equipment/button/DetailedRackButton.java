/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2008 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.RackLocationParser;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class DetailedRackButton extends PolygonButton
{
  private static final String WAREHOUSE = "Warehouse";
  private static final String BANK = "Bank";
  private static final String STARTING_CORNER = "Layout";
  
  private static final String NWE = "NW->E";
  private static final String NES = "NE->S";
  private static final String SEW = "SE->W";
  private static final String SWN = "SW->N";

  private static final String NWS = "NW->S";
  private static final String NEW = "NE->W";
  private static final String SEN = "SE->N";
  private static final String SWE = "SW->E";

  private String msWarehouse;
  private String msBank;
  private String msLayout;
  protected int mnBays;
  protected int mnTiers;
  
  protected DetailedRackLocation[][] mapRackLocations;

  protected LocationData mpLCKey;
  protected Location mpLoc;
  
  protected String msToolTipText;
  
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
  public DetailedRackButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
    msToolTipText = isDeviceName;
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
      String isGroupName, boolean izCanTrack, SKDCScreenPermissions ipPermissions)
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
   * Assign a value to a parameter
   * 
   * @param isParameter
   * @param isValue
   * @return true if the parameter was assigned, false if invalid
   */
  @Override
  protected boolean assignParameter(String isParameter, String isValue)
  {
    if (isParameter.equals(WAREHOUSE))
    {
      msWarehouse = isValue;
    }
    else if (isParameter.equals(BANK))
    {
      msBank = isValue;
    }
    else if (isParameter.equals(STARTING_CORNER))
    {
      msLayout = isValue;
    }
    else
    {
      return super.assignParameter(isParameter, isValue);
    }
    
    if (msWarehouse != null && msBank != null && msLayout != null)
    {
      initialize();
      refreshStatus();
    }
    
    return true;
  }
  
  /**
   * Refresh the status of the instantiated EquipmentGraphic object.
   * This method is to allow the equipment monitor to update based on local
   * WarehouseRx screen events.
   */
  @Override
  public void refreshStatus()
  {
    try
    {
      List<Map> vpLocs = mpLoc.getAllElements(mpLCKey);
      LocationData vpLCData = Factory.create(LocationData.class);
      int vnLocIndex = 0;
      for (int vnBay = 0; vnBay < mnBays; vnBay++)
      {
        for (int vnTier = 0; vnTier < mnTiers; vnTier++)
        {
          vpLCData.dataToSKDCData(vpLocs.get(vnLocIndex++));
          mapRackLocations[vnBay][vnTier].setStatus(
              vpLCData.getLocationStatus(), vpLCData.getEmptyFlag());
        }
      }
    }
    catch (DBException dbe)
    {
      dbe.printStackTrace();
    }
    setToolTipText(msToolTipText + "  ("
        + new SimpleDateFormat("HH:mm:ss").format(new Date()) + ")");
    repaint();
  }
  
  /**
   * Initialize the button
   */
  private void initialize()
  {
    mpLoc = Factory.create(Location.class);
    mpLCKey = Factory.create(LocationData.class);
    mpLCKey.setKey(LocationData.WAREHOUSE_NAME, msWarehouse);
    mpLCKey.setKey(LocationData.ADDRESS_NAME, msBank, KeyObject.LIKE);
    mpLCKey.addOrderByColumn(LocationData.ADDRESS_NAME);
    try
    {
      List<Map> vpLocs = mpLoc.getAllElements(mpLCKey);
      if (vpLocs.size() == 0)
      {
        setStatusText("ERROR");
        System.out.println(msDeviceName
            + ": Configuration contains no locations!");
        return;
      }
      //For now, we'll assume that we start at msBank-001-001
      LocationData vpLCData = Factory.create(LocationData.class);
      vpLCData.dataToSKDCData(vpLocs.get(vpLocs.size()-1));
      RackLocationParser vpRLP = RackLocationParser.parse(vpLCData.getAddress(), true);
      mnBays = vpRLP.getBayInteger();
      mnTiers = vpRLP.getTierInteger();
      mapRackLocations = new DetailedRackLocation[mnBays][mnTiers];
      
      // Figure out the drawing direction
      // Defaults are NWS
      int vnStartX = 0;
      int vnStartY = 0;
      int vnIncX = 1;
      int vnIncY = 1;
      boolean vzXIsBay = true;
      
      if (msLayout.equals(NES))
      {
        vnStartX = mnBays-1;
        vnIncX = -1;
      }
      else if (msLayout.equals(SWN))
      {
        vnStartY = mnTiers-1;
        vnIncY = -1;
      }
      else if (msLayout.equals(SEN))
      {
        vnStartX = mnBays-1;
        vnStartY = mnTiers-1;
        vnIncX = -1;
        vnIncY = -1;
      }
      else if (!msLayout.equals(NWS))
      {
        // Default is NWE
        vzXIsBay = false;
        if (msLayout.equals(NWE))
        {
          vnStartX = mnTiers-1;
          vnIncX = -1;
        }
        else if (msLayout.equals(SWE))
        {
          vnStartY = mnBays-1;
          vnIncY = -1;
        }
        else if (msLayout.equals(SEW))
        {
          vnStartX = mnTiers-1;
          vnStartY = mnBays-1;
          vnIncX = -1;
          vnIncY = -1;
        }
        else if (!msLayout.equals(NEW))
        {
          System.out.println("Invalid Layout: " + msLayout);
        }
      }
      
      int vnLocWidth = vzXIsBay ? mpRectangle.width / mnBays : 
                                  mpRectangle.width / mnTiers;
      int vnLocHeight = vzXIsBay ? mpRectangle.height / mnTiers : 
                                   mpRectangle.height / mnBays;
      
      if (vzXIsBay)
      {
        mpPolygon = new Polygon(
            new int[] {0,vnLocWidth*mnBays+1,vnLocWidth*mnBays+1,0}, 
            new int[] {0,0,vnLocHeight*mnTiers+1,vnLocHeight*mnTiers+1}, 4);
      }
      else
      {
        mpPolygon = new Polygon(
            new int[] {0,vnLocWidth*mnTiers+1,vnLocWidth*mnTiers+1,0}, 
            new int[] {0,0,vnLocHeight*mnBays+1,vnLocHeight*mnBays+1}, 4);
      }
      mpRectangle = mpPolygon.getBounds();
      
      // Build the RackLocations
      int vnLocIndex = 0;

      int x = vnStartX;
      int y = vnStartY;
      for (int vnBay = 0; vnBay < mnBays; vnBay++)
      {
        if (vzXIsBay)
          y = vnStartY;
        else
          x = vnStartX;

        for (int vnTier = 0; vnTier < mnTiers; vnTier++)
        {
          vpLCData.dataToSKDCData(vpLocs.get(vnLocIndex++));
          mapRackLocations[vnBay][vnTier] = getRackLocation(x * vnLocWidth, 
              y * vnLocHeight, vnLocWidth, vnLocHeight, vpLCData);
          if (vzXIsBay)
            y += vnIncY;
          else
            x += vnIncX;
        }
        if (vzXIsBay)
          x += vnIncX;
        else
          y += vnIncY;
      }
    }
    catch (DBException dbe)
    {
      dbe.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Get the Detailed Rack Location.  Provided for extensibility. 
   * 
   * @param inX
   * @param inY
   * @param inW
   * @param inH
   * @param inLocData
   * @return
   */
  protected DetailedRackLocation getRackLocation(int inX, int inY, int inW,
      int inH, LocationData inLocData)
  {
    DetailedRackLocation vpDRL = new DetailedRackLocation(inX, inY, inW, inH);
    vpDRL.setStatus(inLocData.getLocationStatus(), inLocData.getEmptyFlag());
    return vpDRL;
  }
  
  /**
   * Draw the actual shape
   * @param g
   */
  @Override
  protected void drawPolygon(Graphics g)
  {
    synchronized (g)
    {
      for (int vnBay = 0; vnBay < mnBays; vnBay++)
      {
        for (int vnTier = 0; vnTier < mnTiers; vnTier++)
        {
          // Color
          g.setColor(mapRackLocations[vnBay][vnTier].getStatusFillColor());
          g.fillPolygon(mapRackLocations[vnBay][vnTier]);
          
          // Text
          g.setColor(mapRackLocations[vnBay][vnTier].getStatusTextColor());
          drawTextInRectangle(g, mapRackLocations[vnBay][vnTier].getBounds(), 
              mapRackLocations[vnBay][vnTier].getStatusText());

          // Border
          g.setColor(BORDER_COLOR);
          g.drawPolygon(mapRackLocations[vnBay][vnTier]);
        }
      }
    }
  }
  
  /**
   * Racks are always online for equipment monitor
   */
  @Override
  public int getStatus()
  {
    // Future and non-JVM are always "no status"
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      return NO_STATUS;
    }
    
    // Everything else is "online"
    return ONLINE;
  }
}
