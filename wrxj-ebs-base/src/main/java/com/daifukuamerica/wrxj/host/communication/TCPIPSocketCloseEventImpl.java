package com.daifukuamerica.wrxj.host.communication;

import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.TCPIPSocketCloseEvent;
import com.daifukuamerica.impl.TCPIPReaderWriter;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;

/**
 * Socket Close event handler.
 *
 * @author A.D.
 * @since  17-Jul-2013
 */
public class TCPIPSocketCloseEventImpl implements TCPIPSocketCloseEvent
{
  protected String msCommGrp = null;
  protected Controller mpSysGateWay = null;
  protected TCPIPLogger mpLogger;

  public TCPIPSocketCloseEventImpl(String isCommGroup, Controller ipGateway, TCPIPLogger ipLogger)
  {
    msCommGrp = isCommGroup;
    mpSysGateWay = ipGateway;
    mpLogger = ipLogger;
  }

  /**
   * {@inheritDoc} This Event will cause a status message to be sent to all
   * Equipment Monitor Screens.
   *
   * @param ipCommThread
   */
  @Override
  public void socketCloseEvent(TCPIPReaderWriter ipCommThread)
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    vpSEDF.setType(ControllerConsts.EQUIPMENT_STATUS);
    vpSEDF.addEquipmentStatus(msCommGrp, "Host", msCommGrp, StatusEventDataFormat.STATUS_OFFLINE,
                              StatusEventDataFormat.STATUS_OFFLINE,
                              StatusEventDataFormat.NONE, "NOW");
    mpSysGateWay.publishStatusEvent(vpSEDF.createStringToSend());
  }
}