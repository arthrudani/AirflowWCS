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

package com.daifukuamerica.wrxj.swingui.equipment;

import com.daifukuamerica.wrxj.swing.DacTranslator;
import com.daifukuamerica.wrxj.swing.LegendItem;
import com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton;
import com.daifukuamerica.wrxj.swingui.equipment.properties.EquipmentMonitorProperties;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * <B>Description:</B> The Legend Panel for the Equipment Monitor
 *
 * @author       Michael Andrus<BR>
 * @version      1.0
 *
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class EquipmentLegendPanel extends JPanel
{
  /**
   * Public constructor for default panel
   *
   * @param ipProperties
   */
  public EquipmentLegendPanel(EquipmentMonitorProperties ipProperties)
  {
    super();
    int vnWidth = ipProperties.getLegendWidth();
    int vnHeight = ipProperties.getLegendHeight();
    int vnRows = ipProperties.getLegendRows();
    int vnCols = ipProperties.getLegendColumns();

    setPreferredSize(new Dimension(vnWidth, vnHeight));

    TitledBorder vpActiveBord = BorderFactory.createTitledBorder
        (
          BorderFactory.createEtchedBorder(EquipmentGraphic.DAIFUKU_PURPLE,
                                           EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE)
        );
    vpActiveBord.setTitle(DacTranslator.getTranslation("Equipment Monitor Statuses"));
    setBorder(vpActiveBord);
    setLayout(new GridLayout(vnRows, vnCols));

    if (ipProperties.anyMOSConnections())
    {
      add(new LegendItem(PolygonButton.TRACKING_COLOR, "Displaying Tracking"));
    }

    add(new LegendItem(
        EquipmentGraphic.STATUS_COLORS[EquipmentGraphic.ONLINE],
        "Equipment Online"));
    add(new LegendItem(
        EquipmentGraphic.STATUS_COLORS[EquipmentGraphic.OFFLINE],
        "Equipment Stop/Offline"));
    add(new LegendItem(
        EquipmentGraphic.STATUS_COLORS[EquipmentGraphic.ERROR],
        "Equipment Error"));
    add(new LegendItem(
        EquipmentGraphic.STATUS_COLORS[EquipmentGraphic.DISCONNECTED],
        "Equipment Disconnected"));
    add(new LegendItem(
        EquipmentGraphic.STATUS_COLORS[EquipmentGraphic.UNKNOWN],
        "Equipment Not Initialized"));
    add(new LegendItem(
        EquipmentGraphic.STATUS_COLORS[EquipmentGraphic.PRISTINE],
        "Warehouse Rx Server Down"));
  }
}
