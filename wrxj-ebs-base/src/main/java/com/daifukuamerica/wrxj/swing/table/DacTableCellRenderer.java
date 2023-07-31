package com.daifukuamerica.wrxj.swing.table;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Dac. table cell renderer.
 * @author A.D.
 * @since  01-Dec-2008
 */
public class DacTableCellRenderer extends DefaultTableCellRenderer
{
  protected DacRowForegroundColorChooser mpForegroundColorChooser = null;

  public DacTableCellRenderer()
  {
    super();
  }

  /**
   * Set the foreground color chooser
   * @param ipDRFCC
   */
  public void setForeGroundColorChooser(DacRowForegroundColorChooser ipDRFCC)
  {
    mpForegroundColorChooser = ipDRFCC;
  }
  
 /**
  * Method sets all even rows to a light gray color.
  * @param izSelectedCell boolean to indicate if this is a selected cell.
  * @param inRow the current row.
  * @param ipLabel a label representing current cell.
  */
  protected void setAlternatingColor(boolean izSelectedCell, int inRow,
                                     JLabel ipLabel)
  {
    if (!izSelectedCell)
    {
      if (inRow%2 == 0)
        ipLabel.setBackground(new Color(226, 226, 226));
      else
        ipLabel.setBackground(Color.WHITE);
      
      if (mpForegroundColorChooser != null)
      {
        ipLabel.setForeground(mpForegroundColorChooser.getColorForRow(inRow));
      }
    }
  }
}
