package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;

public class NudgeEquipmentMonitorTask extends TimedEventTask
{
  StatusEventDataFormat mpSEDF;
  
  public NudgeEquipmentMonitorTask(String isName)
  {
    super(isName);
    mpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    mpSEDF.setType(ControllerConsts.NUDGE_EQUIPMENT_MONITOR);
  }

  /**
   *  Periodic task for "nudging" the Equipment monitor
   */
  @Override
  public String initTask()
  {
    mnInitialInterval = 60000;
    int vnSecs = getConfigValue(INTERVAL);
    msIntervalString = vnSecs + " seconds ";
    mnInterval = vnSecs * 1000;
    if (vnSecs < 1)
      return "INVALID NudgeEquipmentMonitorTask interval - " + vnSecs
          + " NudgeEquipmentMonitorTask will not be started.";
    return null;
  }

  /**
   * Nudge the equipment monitor
   */
  @Override
  public void run()
  {
    ThreadSystemGateway.get().publishUpdateEvent(mpSEDF.createStringToSend());
  }
}
