package com.daifukuamerica.wrxj.swing.table;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Class to center or left-justify displayed column values for Integer, Double 
 * and String objects.
 *
 * @author A.D.
 * @since
 */
public class DacCellContentAlignmentRenderer extends DacTableCellRenderer
{
  int mnCellJustification;

  public DacCellContentAlignmentRenderer()
  {
    super();
  }
  
  public DacCellContentAlignmentRenderer(int inJustification)
  {
    mnCellJustification = inJustification;
  }

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
      JLabel vpLabel = (JLabel)vpComponent;
      vpLabel.setHorizontalAlignment(mnCellJustification);
      if (ipTable.isCellEditable(inRow, inColumn))
        vpLabel.setBackground(new Color(245, 245, 224));
      else
        setAlternatingColor(izCellSelected, inRow, vpLabel);
    }
    return(vpComponent);
  }
}
