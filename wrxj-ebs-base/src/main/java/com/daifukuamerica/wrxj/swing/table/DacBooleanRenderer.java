/* ***************************************************************************
  $Workfile$
  $Date$
  
  Copyright (c) 2017 Daifuku North America Holding Company.  All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.swing.table;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;

/**
 * Class to render user specified boolean formats. This class will be used by
 * JTable if and only if the column being rendered is of class Boolean.
 * 
 * See also JTable.BooleanRenderer (this is the same, but adds row striping).
 */
public class DacBooleanRenderer extends JCheckBox
    implements TableCellRenderer, UIResource
{
  private static final long serialVersionUID = -4532159556957513819L;

  private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  protected DacRowForegroundColorChooser mpForegroundColorChooser = null;

  /**
   * Constructor
   */
  public DacBooleanRenderer()
  {
    setHorizontalAlignment(JLabel.CENTER);
    setBorderPainted(true);
  }

  /**
   * Render
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
  {
    if (isSelected)
    {
      setForeground(table.getSelectionForeground());
      super.setBackground(table.getSelectionBackground());
    }
    else
    {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }
    setSelected((value != null && ((Boolean)value).booleanValue()));

    if (hasFocus)
    {
      setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
    }
    else
    {
      setBorder(noFocusBorder);
    }
    setAlternatingColor(isSelected, row, this);
    return this;
  }

  /**
   * Set the foreground color chooser
   * 
   * @param ipDRFCC
   */
  public void setForeGroundColorChooser(DacRowForegroundColorChooser ipDRFCC)
  {
    mpForegroundColorChooser = ipDRFCC;
  }

  /**
   * Method sets all even rows to a light gray color.
   * 
   * @param izSelectedCell boolean to indicate if this is a selected cell.
   * @param inRow the current row.
   * @param ipCell a JComponent representing current cell.
   */
  protected void setAlternatingColor(boolean izSelectedCell, int inRow,
      JComponent ipCell)
  {
    if (!izSelectedCell)
    {
      if (inRow % 2 == 0)
        ipCell.setBackground(new Color(226, 226, 226));
      else ipCell.setBackground(Color.WHITE);

      if (mpForegroundColorChooser != null)
      {
        ipCell.setForeground(mpForegroundColorChooser.getColorForRow(inRow));
      }
    }
  }
}
