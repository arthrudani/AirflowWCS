package com.daifukuamerica.wrxj.swingui.equipment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * <B>Description:</B> Panel for an equipment group
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
@SuppressWarnings("serial")
public class GroupPanel extends JPanel
{
  JPanel mpInternalPanel;
  Map<String, EquipmentGraphic> mpGraphics;
  TrackingPanel mpTrackingPanel = null;

  /**
   * Constructor
   *
   * @param isGroupName
   * @param inWidth
   * @param inHeight
   * @param inX
   * @param inY
   */
  public GroupPanel(String isGroupName, int inWidth, int inHeight, int inX,
      int inY)
  {
    super(new GridLayout(1,1));
    setName(isGroupName);

    // Size/location
    Dimension vpSize = new Dimension(inWidth, inHeight);
    setPreferredSize(vpSize);
    setBounds(new Rectangle(vpSize));
    setLocation(inX, inY);

    TitledBorder vpInactiveBord = BorderFactory.createTitledBorder
          (
          BorderFactory.createEtchedBorder(EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE,
                                           EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE)
          );
    // Border
    vpInactiveBord.setTitle(getName());
    setBorder(vpInactiveBord);
    mpInternalPanel = new JPanel(null);
    super.add(mpInternalPanel);

    mpGraphics = new TreeMap<String, EquipmentGraphic>();
  }

  /**
   * This panel is attached to an active tracking panel
   *
   * @param ipTrackingPanel
   */
  public void activate(TrackingPanel ipTrackingPanel)
  {
    // Link the TrackingPanel
    mpTrackingPanel = ipTrackingPanel;
    TitledBorder vpActiveBord = BorderFactory.createTitledBorder
        (
          BorderFactory.createEtchedBorder(EquipmentGraphic.DAIFUKU_PURPLE,
                                           EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE)
        );
    // Update the borders
    vpActiveBord.setTitle(getName());
    setBorder(vpActiveBord);

    // Update the graphics
    for (EquipmentGraphic vpEG : mpGraphics.values())
    {
      vpEG.setTrackingVisible(true);
    }
  }

  /**
   * This panel is not attached to an active tracking panel
   */
  public void deactivate()
  {
    // Disassociate the TrackingPanel
    mpTrackingPanel = null;
    TitledBorder vpInactiveBord = BorderFactory.createTitledBorder
          (
          BorderFactory.createEtchedBorder(EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE,
                                           EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE)
          );

    // Update the borders
    vpInactiveBord.setTitle(getName());
    setBorder(vpInactiveBord);

    // Update the graphics
    for (EquipmentGraphic vpEG : mpGraphics.values())
    {
      vpEG.setTrackingVisible(false);
    }
  }

  /**
   * @return the panel to which we will add the graphics
   */
  public JPanel getGraphicPanel()
  {
    return mpInternalPanel;
  }

  /**
   * @see java.awt.Container#add(java.awt.Component)
   */
  public void add(EquipmentGraphic ipGraphic)
  {
    mpInternalPanel.add((JComponent)ipGraphic);
    mpGraphics.put(ipGraphic.getGraphicName(), ipGraphic);

    // If this isn't future expansion, make the title black
    if (!ipGraphic.isFutureExpansion() && ipGraphic.isAssignedJVM())
    {
      ((TitledBorder)getBorder()).setTitleColor(Color.BLACK);
    }
  }

  /**
   * Set the Tracking Data for this group, passing individual tracking to this
   * group's graphics.
   *
   * @param ipTrackingData
   */
  public void setTrackingData(List<String[]> ipTrackingData)
  {
    // Sort out the tracking
    Map<String, List<String[]>> vpTrackingMap = new TreeMap<String, List<String[]>>();
    for (String[] vasTracking : ipTrackingData)
    {
      String vsGraphic = vasTracking[LoadStatusScrollPane.EQUIP_COLUMN];
      List<String[]> vpTrackingList = vpTrackingMap.remove(vsGraphic);
      if (vpTrackingList == null)
      {
        vpTrackingList = new ArrayList<String[]>();
      }
      vpTrackingList.add(vasTracking);
      vpTrackingMap.put(vsGraphic, vpTrackingList);
    }

    // Assign the tracking to the graphics
    for (EquipmentGraphic vpEG : mpGraphics.values())
    {
      vpEG.setTracking(vpTrackingMap.remove(vpEG.getGraphicName()));
    }
  }

  /**
   * Clear tracking filters
   */
  public void clearTrackingFilters()
  {
    for (EquipmentGraphic vpEG : mpGraphics.values())
    {
      vpEG.clearTrackingFilter();
    }
    mpTrackingPanel.setFilter("");
  }

  /**
   * Limit the tracking to a single graphic
   *
   * @param isFilter
   */
  public void setTrackingFilter(String isFilter)
  {
    if (mpTrackingPanel != null)
    {
      mpTrackingPanel.setFilter(isFilter);
    }
  }
}
