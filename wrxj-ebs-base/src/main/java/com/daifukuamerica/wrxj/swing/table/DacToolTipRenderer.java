package com.daifukuamerica.wrxj.swing.table;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;

/**
 * Renderer for Tool Tip display on a JTable row.
 * 
 * @author A.D.
 * @since  01-Dec-2008
 */
public class DacToolTipRenderer extends DacCellContentAlignmentRenderer
{
  @Override
  public Component getTableCellRendererComponent(JTable ipTable, Object ipCellValue,
                     boolean izCellSelected, boolean izCellFocused,  int inRow, int inColumn)
  {
    Component vpComponent = super.getTableCellRendererComponent(ipTable, ipCellValue,
                                                                izCellSelected,
                                                                izCellFocused,
                                                                inRow, inColumn);
    if (ipCellValue != null)
    {
      JComponent vpCell = (JComponent)vpComponent;
      vpCell.setToolTipText(ipCellValue.toString());
    }

    return(vpComponent);
  }
}
