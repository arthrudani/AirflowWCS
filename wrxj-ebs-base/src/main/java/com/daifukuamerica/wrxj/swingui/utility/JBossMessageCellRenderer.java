package com.daifukuamerica.wrxj.swingui.utility;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Left-align long fields, used fixed-width font
 * 
 * @author mandrus
 */
public class JBossMessageCellRenderer extends DefaultTableCellRenderer
{
  private final Font MONOSPACED = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  
  /**
   * Constructor
   */
  public JBossMessageCellRenderer()
  {
    super();
  }
  
  /**
   * Get the component for the cell
   * 
   * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent(JTable ipTable, Object ipCellValue,
      boolean izCellSelected, boolean izCellFocused,  int inRow, int inColumn)
  {
    Component vpComponent = super.getTableCellRendererComponent(ipTable,
        ipCellValue, izCellSelected, izCellFocused, inRow, inColumn);
    if (ipCellValue != null)
    {
      JLabel vpLabel = (JLabel)vpComponent;
      vpLabel.setHorizontalAlignment(vpLabel.getText().length() > 20 ? SwingConstants.LEFT
          : SwingConstants.CENTER);
      if (!izCellSelected)
      {
        if (inRow % 2 == 1)
        {
          vpComponent.setBackground(new Color(226, 226, 226));
        }
        else
        {
          vpComponent.setBackground(Color.WHITE);
        }
      }
      vpComponent.setFont(MONOSPACED);
    }
    return (vpComponent);
  }
}
