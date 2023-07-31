package com.daifukuamerica.wrxj.device.agv;

import com.daifukuamerica.wrxj.log.Logger;

/**
 * Logs messages using WarehouseRx logger.
 * 
 * @author A.D.
 * @since  10-Jun-2009
 */
public class AGVLoggerWrxjImpl implements AGVLogger
{
  private Logger mpBaseLogger = null;

  public AGVLoggerWrxjImpl(String isLoggerName)
  {
    mpBaseLogger = Logger.getLogger(isLoggerName + "-Port");
    if (mpBaseLogger.getCommLogger() == null)
    {
      mpBaseLogger.addCommLogger();
    }
  }

  @Override
  public void logErrorMessage(String isErrorMessage)
  {
    mpBaseLogger.logError(isErrorMessage);
  }

  @Override
  public void logCommMessage(int inDirection, String isCommMessage)
  {
    byte[] vabMessage = isCommMessage.getBytes();
    if (inDirection == AGVLogger.INBOUND_MESG)
      mpBaseLogger.logRxByteCommunication(vabMessage, 0, vabMessage.length);
    else
      mpBaseLogger.logTxByteCommunication(vabMessage, 0, vabMessage.length);
  }

  @Override
  public void logDebugMessage(String isDebugMessage)
  {
    mpBaseLogger.logDebug(isDebugMessage);
  }
}
