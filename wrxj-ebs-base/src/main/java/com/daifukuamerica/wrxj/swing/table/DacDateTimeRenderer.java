package com.daifukuamerica.wrxj.swing.table;

import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

/**
 *  Class to render user specified date formats.  This class will be used
 *  by JTable if and only if the column being rendered is of class Date.
 * 
 *  @author A.D.
 *  @since 01-Dec-2008
 */
public class DacDateTimeRenderer extends DacTableCellRenderer
{
  private String misDateTimeFormat = null;
  private SimpleDateFormat mipDateFmt = new SimpleDateFormat();

  public DacDateTimeRenderer(String dateTimeFormat)
  {
    misDateTimeFormat = dateTimeFormat;
  }

  @Override
  public Component getTableCellRendererComponent(JTable ipTable, Object ipValue, 
          boolean izCellSelected, boolean izCellFocused, int inRow, int inColumn)
  {
    Component vpComponent = super.getTableCellRendererComponent(ipTable, ipValue,
                                                                izCellSelected,
                                                                izCellFocused,
                                                                inRow, inColumn);
    if (ipValue != null && ipValue.toString().trim().length() != 0)
    {
      mipDateFmt.applyPattern(SKDCConstants.DEF_SKTABLE_DATETIME_FORMAT);
      mipDateFmt.applyPattern(misDateTimeFormat);

      JLabel vpLabel = (JLabel)vpComponent;
      vpLabel.setText(mipDateFmt.format((Date)ipValue));
      vpLabel.setHorizontalAlignment(SwingConstants.CENTER);
      setAlternatingColor(izCellSelected, inRow, vpLabel);
    }

    return(vpComponent);
  }
}
