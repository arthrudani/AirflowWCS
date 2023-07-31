package com.daifukuamerica.wrxj.swingui.equipment;

import com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;

/**
 * <B>Description:</B> Panel for a tab on the equipment monitor
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class EquipmentTabPanel extends JPanel
{
  /**
   * Constructor
   * 
   * @param isTabName
   */
  public EquipmentTabPanel(String isTabName)
  {
    super(null);
    setName(isTabName);
  }

  /**
   * Get the color that reflects the overall status of this tab
   * @return
   */
  public Color getTabStatusColor()
  {
    Color vpTabColor = EquipmentGraphic.TEXT_STATUS_COLORS[EquipmentGraphic.NO_STATUS];
    for (int j = 0; j < getComponentCount(); j++)
    {
      if (getComponent(j) instanceof JPanel)
      {
        JPanel vpPanel = (JPanel)getComponent(j);
        for (int k = 0; k < vpPanel.getComponentCount(); k++)
        {
          if (vpPanel.getComponent(k) instanceof JPanel)
          {
            JPanel vpInnerPanel = (JPanel)vpPanel.getComponent(k);
            vpTabColor = getPanelStatusColor(vpTabColor, vpInnerPanel.getComponents());
          }
        }
      }
    }
    return vpTabColor;
  }
  
  /**
   * Get the color of the tab text (reflect errors, etc)
   * 
   * @param ipCurrentColor
   * @param vpComponents
   * @return
   */
  private Color getPanelStatusColor(Color ipCurrentColor,
      Component[] vpComponents)
  {
    int vnPriorityStatus = colorToStatus(ipCurrentColor);
    ArrayList<Integer> vpStatuses = new ArrayList<Integer>(vpComponents.length);

    for (int k = 0; k < vpComponents.length; k++)
    {
      if (vpComponents[k] instanceof PolygonButton)
      {
        Integer vnTmp = Integer.valueOf(((PolygonButton)vpComponents[k]).getStatus());
        if (!vpStatuses.contains(vnTmp))
        {
          vpStatuses.add(vnTmp);
        }
      }
    }
    vpStatuses.trimToSize();
    Iterator<Integer> vpIterator = vpStatuses.iterator();
    while (vpIterator.hasNext())
    {
      int vnStat = vpIterator.next().intValue();
      if (vnStat < vnPriorityStatus)
      {
        vnPriorityStatus = vnStat;
      }
    }
    Color vpColor = EquipmentGraphic.TEXT_STATUS_COLORS[vnPriorityStatus];
    return vpColor;
  }

  /**
   * Does this tab have active buttons?
   * 
   * @return false if all buttons are future or for a different JVM, else true
   */
  public boolean isActive()
  {
    return isPanelActive(this);
  }
  
  /**
   * Does this panel have active buttons?
   *  
   * @param ipPanel
   * @return
   */
  private boolean isPanelActive(JPanel ipPanel)
  {
    for (int j = 0; j < ipPanel.getComponentCount(); j++)
    {
      Component vpComp = ipPanel.getComponent(j);
      if (vpComp instanceof JPanel)
      {
        if (isPanelActive((JPanel)vpComp))
        {
          return true;
        }
      }
      else if (vpComp instanceof EquipmentGraphic)
      {
        EquipmentGraphic vpEG = (EquipmentGraphic)vpComp;
        if (!vpEG.isFutureExpansion() && vpEG.isAssignedJVM())
        {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Converts a color to a status
   * 
   * @param ipColor
   * @return status, or EquipmentGraphic.UNKNOWN if there is no mapping
   */
  private int colorToStatus(Color ipColor)
  {
    for (int i = 0; i < EquipmentGraphic.STATUS_COLORS.length; i++)
    {
      if (ipColor == EquipmentGraphic.TEXT_STATUS_COLORS[i])
      {
        return i;
      }
    }
    return EquipmentGraphic.UNKNOWN;
  }
}
