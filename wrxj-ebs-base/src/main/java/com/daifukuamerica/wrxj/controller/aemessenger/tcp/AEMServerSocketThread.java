package com.daifukuamerica.wrxj.controller.aemessenger.tcp;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPConnectionEvent;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.impl.TCPIPServerComms;
import java.util.Properties;

public class AEMServerSocketThread extends Thread
{
  private TCPIPServerComms mpServComm;
  private TCPIPLogger mpCommLogger;
  private String msPort = null;
  
  public AEMServerSocketThread(Properties ipProperties, TCPIPLogger ipCommLogger,
      TCPIPConnectionEvent ipConnectionHandler)
  {
    mpCommLogger = ipCommLogger;
    msPort = ipProperties.getProperty(TCPIPConstants.LISTEN_PORT);
    mpServComm = new TCPIPServerComms(ipProperties, mpCommLogger);
    mpServComm.registerConnectionEvents(ipConnectionHandler);
  }

  @Override
  public void run()
  {
    setName(getClass().getSimpleName() + ":" + msPort);
    try
    {
      mpServComm.connectionWait();
    }
    catch (TCPIPCommException e)
    {
      mpCommLogger.logErrorMessage("Error on server thread. Shutting down port " + msPort, e);
      mpServComm.stopServer();
    }
  }

  @Override
  public void interrupt()
  {
    mpServComm.stopServer();
    super.interrupt();
  }
}
