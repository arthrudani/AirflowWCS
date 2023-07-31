package com.daifukuamerica.wrxj.host.communication;

import java.util.logging.Formatter;

import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.logging.Level;


public class TCPIPBaseLoggerImpl implements TCPIPLogger
{
  private Logger mpBaseLogger = null;
  public TCPIPBaseLoggerImpl(Logger iplogger)
  {
    mpBaseLogger = iplogger;
    if (mpBaseLogger.getCommLogger() == null)
    {
      mpBaseLogger.addCommLogger();
    }
  }

  @Override
  public void logCommMessage(String isDirection, String isCommMessage)
  {
    byte[] vabMessage = isCommMessage.getBytes();
    if (isDirection.equals(MESG_DIR_RECV))
      mpBaseLogger.logRxByteCommunication(vabMessage, 0, vabMessage.length);
    else
      mpBaseLogger.logTxByteCommunication(vabMessage, 0, vabMessage.length);

  }

  @Override
  public void logDebugMessage(String isDebugMessage)
  {
    mpBaseLogger.logDebug(isDebugMessage);

  }

  @Override
  public void logErrorMessage(String isErrorMessage)
  {
    mpBaseLogger.logError(isErrorMessage);

  }

  @Override
  public <Type extends Throwable> void logErrorMessage(String isErrorMessage, Type ipExc)
  {
    mpBaseLogger.logException(isErrorMessage, ipExc);

  }

  @Override
  public void logHeartBeatMessage(String isDirection, String isHBMesg)
  {
    logCommMessage(isDirection, isHBMesg);
  }

  @Override
  public void setFormatter(Formatter ipFormatter)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setLoggingLevel(Level ipLevel)
  {
    // TODO Auto-generated method stub

  }
}
