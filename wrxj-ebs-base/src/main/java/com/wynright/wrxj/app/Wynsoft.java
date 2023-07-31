package com.wynright.wrxj.app;

import com.daifukuamerica.wrxj.application.Application;

public class Wynsoft
{
  private Wynsoft() {}

  private static final String INTEGRATION = "Wynsoft.Integration";
  private static final String PRODUCT_ID  = "Wynsoft.ProductId";
  private static final String INSTANCE_ID = "Wynsoft.InstanceId";
  private static final String UPDATE_AE   = "Wynsoft.UpdateAE";
  
  /**
   * Is this instance of WRx integrated with other Wynsoft products?  If so then
   * there should be a database synonym called S_AED_AES_SYS_GLOBAL_SETTINGS
   * connected the WRx DB to the global settings data in the AED database.
   * @return
   */
  public static boolean isIntegrated()
  {
    return Application.getBoolean(INTEGRATION, false);
  }

  /**
   * See xxxx_AED.AES_SYS_PRODUCTS.  This matches an enum in the Wynsoft code,
   * so it probably won't change often (if ever).
   * @return
   */
  public static int getProductId()
  {
    return Application.getInt(PRODUCT_ID, 28);
  }

  /**
   * See xxxx_AED.AES_SYS_INSTANCES.  This is unique per Warehouse Rx instance.
   * @return
   */
  public static int getInstanceId()
  {
    return Application.getInt(INSTANCE_ID, 2801);
  }
  
  /**
   * Update the AE Instance table with server run information
   * @return true to update, false to not update
   */
  public static boolean updateAESystem()
  {
    return Application.getBoolean(UPDATE_AE, true);
  }
}
