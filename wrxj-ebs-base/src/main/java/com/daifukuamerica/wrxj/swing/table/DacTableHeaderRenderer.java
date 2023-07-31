package com.daifukuamerica.wrxj.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Header renderer to use for DAC tables.
 * 
 * @author A.D.
 * @since  13-Mar-2008
 */
@SuppressWarnings("serial")
public class DacTableHeaderRenderer extends DefaultTableCellRenderer
{
  private Color mpTableHeaderColor;
  
 /**
  * Default constructor to use whatever color settings are in the current
  * Look-And-Feel manager.
  */
  public DacTableHeaderRenderer()
  {
    UIDefaults vpLFDef = UIManager.getLookAndFeel().getDefaults();
    mpTableHeaderColor = vpLFDef.getColor("TabbedPane.selected");
  }
  
 /**
  * Constructor to allow caller to set header color.
  * @param ipTableHeaderColor the header color to use.
  */
  public DacTableHeaderRenderer(Color ipTableHeaderColor)
  {
    mpTableHeaderColor = ipTableHeaderColor;
  }
  
  @Override
  public Component getTableCellRendererComponent(JTable ipTable, Object ipValue,
                   boolean izCellSelected, boolean izCellHasFocus, int inRow, int inColumn)
  {
    Component vpComponent = super.getTableCellRendererComponent(ipTable, ipValue,
                                                                izCellSelected,
                                                                izCellHasFocus, 
                                                                inRow, inColumn);
    if (ipValue != null)
    {
      JLabel vpLabel = (JLabel)vpComponent;
      vpLabel.setBorder(BorderFactory.createRaisedBevelBorder());
      vpLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
      vpLabel.setText(ipValue.toString());
      vpLabel.setHorizontalAlignment(SwingConstants.CENTER);
      vpLabel.setBackground(mpTableHeaderColor);
    }

    return(vpComponent);
  }
}
