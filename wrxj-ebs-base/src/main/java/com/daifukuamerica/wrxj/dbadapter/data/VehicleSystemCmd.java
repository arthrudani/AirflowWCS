
package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;

/**
 * Vehicle System command table interface.
 * @author A.D.
 * @since  15-Jun-2009
 */
public class VehicleSystemCmd extends BaseDBInterface
{
  protected VehicleSystemCmdData mpVSCmdData;

  public VehicleSystemCmd()
  {
    super("VehicleSystemCmd");
    mpVSCmdData = Factory.create(VehicleSystemCmdData.class);
  }
}
