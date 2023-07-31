package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.jdbc.DBInfo;

/**
 * Daifuku release to code field component for screens.
 * 
 * @author RKM
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ReleaseToCodeField extends SKDCTextField
{
  /**
   *  Create text field.
   */
  public ReleaseToCodeField()
  {
    super(DBInfo.getFieldLength(OrderHeaderData.RELEASETOCODE_NAME));
  }

  /**
   * Determine whether or not we are using release-to codes
   * @return
   */
  public static boolean useReleaseToCode()
  {
    return Application.getBoolean("UseReleaseToCode", false);
  }
}
