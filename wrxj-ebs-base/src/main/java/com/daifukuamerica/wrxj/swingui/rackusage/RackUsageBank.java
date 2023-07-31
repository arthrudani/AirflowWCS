package com.daifukuamerica.wrxj.swingui.rackusage;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;

/**
 * <B>Description:</B> Class for a bank of locations for the RackUsage screen
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class RackUsageBank extends JComponent
{
  private final int BANK_START_X = 25;
  
  String msWarehouse = "";
  String msBank = "";
  int mnBays = 0;
  int mnTiers = 0;
  Polygon mpBank;
  RackUsageBankTier[] mapTiers;
  
  StandardLocationServer mpLocServer;
  PropertyChangeListener mpPCL;
  RackInfoCombo mpDisplaySelector;

  /**
   * Constructor
   * 
   * @param x
   * @param y
   * @param width
   * @param height
   * @param ipPCL - Property change listener for Tiers
   * @param ipDisplaySelector
   */
  public RackUsageBank(int x, int y, int width, int height,
      PropertyChangeListener ipPCL, RackInfoCombo ipDisplaySelector)
  {
    super();
    setBounds(x, y, width, height);
    setLayout(null);
    
    mpBank = new Polygon(
        new int[] { BANK_START_X, width - BANK_START_X, width - BANK_START_X, BANK_START_X }, 
        new int[] {0, 0, height-1, height-1}, 4);
    
    mpLocServer = Factory.create(StandardLocationServer.class);
    
    mpPCL = ipPCL;
    
    mpDisplaySelector = ipDisplaySelector;
  }
  
  /**
   * @see java.awt.Component#paint(java.awt.Graphics)
   */
  @Override
  public void paint(Graphics g)
  {
    if (mapTiers == null)
    {
      g.setColor(EquipmentGraphic.DAIFUKU_LIGHT_PURPLE);
      g.fillPolygon(mpBank);
      
      g.setColor(Color.BLACK);
      g.drawPolygon(mpBank);
    }
    else
    {
      super.paint(g);
    }
  }

  /**
   * Set the bank and bay information
   * 
   * @param isWarehouse
   * @param inBank
   * @param inBays
   */
  public void setData(String isWarehouse, int inBays, int inTiers)
  {
    msWarehouse = isWarehouse;
    mnBays = inBays;
    mnTiers = inTiers;
  }
  
  /**
   * Get actual statuses for a given tier
   * 
   * @param inTier
   */
  public void getBankInfo(int inBank)
  {
       removeAll();
    
    if (mnTiers > 0)
    {
      msBank = zeroPad(inBank);
      
      // Add the tiers
      double vdHeight = ((double)getHeight())/((double)mnTiers);
      double vdY = 0;
      mapTiers = new RackUsageBankTier[mnTiers];
      for (int i = mnTiers; i > 0; i--)
      {
        int vnArrayIdx = i - 1;
        int vnHeight = (int)(vdY + vdHeight + 1) - (int)vdY;
        if (vnHeight + (int)vdY > getHeight())
        {
          vnHeight -= 1;
        }
        mapTiers[vnArrayIdx] = Factory.create(RackUsageBankTier.class, 0, (int)vdY, getWidth(),
            vnHeight, false, true, mpPCL, mpDisplaySelector);
        mapTiers[vnArrayIdx].setData(msWarehouse, inBank, mnBays);
        mapTiers[vnArrayIdx].getTierInfo(i);
        add(mapTiers[vnArrayIdx]);
        
        vdY += vdHeight;
      }
    }
    repaint();
  }
  
  /**
   * Zero-pad a bank, bay or tier
   * 
   * @param inBankBayOrTier
   * @return
   */
  private String zeroPad(int inBankBayOrTier)
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
}
