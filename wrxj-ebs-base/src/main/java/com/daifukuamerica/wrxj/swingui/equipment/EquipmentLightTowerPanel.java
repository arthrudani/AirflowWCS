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
import com.daifukuamerica.wrxj.swingui.equipment.properties.EquipmentMonitorProperties;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * <B>Description:</B> The Light Tower Panel for the Equipment Monitor
 *
 * @author       Michael Andrus<BR>
 * @version      1.0
 *
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class EquipmentLightTowerPanel extends JPanel
{
  public EquipmentLightTowerPanel(EquipmentMonitorProperties ipProperties)
  {
    super();
    int vnWidth = ipProperties.getLightTowerWidth();
    int vnHeight = ipProperties.getLightTowerHeight();
    int vnRows = ipProperties.getLightTowerRows();
    int vnCols = ipProperties.getLightTowerColumns();

    setPreferredSize(new Dimension(vnWidth, vnHeight));
    TitledBorder vpActiveBord = BorderFactory.createTitledBorder
        (
          BorderFactory.createEtchedBorder(EquipmentGraphic.DAIFUKU_PURPLE,
                                           EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE)
        );

    vpActiveBord.setTitle("Light Tower Statuses");
    setBorder(vpActiveBord);

    setLayout(new GridLayout(vnRows, vnCols));

    add(new LegendItem(Color.GREEN, "Equipment Online (SRM or P&D)"));
    add(new LegendItem(Color.BLUE, "Equipment Stop/Offline (SRM or P&D)"));
    add(new LegendItem(Color.RED, "Equipment Error (SRM or P&D)"));
    add(new LegendItem(Color.RED, "(Flashing) SRC Communication Error"));
  }
}
