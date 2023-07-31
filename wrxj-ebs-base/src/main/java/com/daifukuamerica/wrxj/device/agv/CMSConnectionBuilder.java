
package com.daifukuamerica.wrxj.device.agv;

import com.daifukuamerica.wrxj.device.agv.communication.TCPIPClientComms;
import com.daifukuamerica.TCPIPCommException;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * In the case that there are multiple IP addresses to which we could connect,
 * this class will help determine the valid connection.
 *
 * @author A.D.
 * @since  21-Oct-2009
 */
public class CMSConnectionBuilder
{
  protected static final String PRIMARY_CONN = "P";
  protected static final String SECONDARY_CONN = "S";

  private LinkedHashMap<String, TCPIPClientComms> mpConnMap;

  public CMSConnectionBuilder(LinkedHashMap<String, TCPIPClientComms> ipConnectionMap)
  {
    super();
    mpConnMap = ipConnectionMap;
  }

  public void createConnection(AGVLogger ipLogger) throws TCPIPCommException
  {
    boolean vzConnected = false;
    TCPIPClientComms vpTCPComm = null;

    for(Iterator<String> vpIter = mpConnMap.keySet().iterator(); vpIter.hasNext();)
    {
      String vsHostName = vpIter.next();
      vpTCPComm = mpConnMap.get(vsHostName);
      try
      {
        vpTCPComm.openConnection();
        vzConnected = true;
        break;
      }
      catch(TCPIPCommException ex)
      {
        ipLogger.logErrorMessage(ex.getMessage());
        vpTCPComm.closeConnection();
      }
    }

    if (!vzConnected)
      throw new TCPIPCommException("No connection to CMS available!");
    else
      swapOutPrimaryConnection(vpTCPComm);
  }

  private void swapOutPrimaryConnection(TCPIPClientComms ipNewPrimaryConn)
  {
    if (ipNewPrimaryConn != null)
    {
      TCPIPClientComms vpTCPComm1 = mpConnMap.get(PRIMARY_CONN);
      TCPIPClientComms vpTCPComm2 = mpConnMap.get(SECONDARY_CONN);

      if (vpTCPComm2 == ipNewPrimaryConn)
      {
        mpConnMap.put(PRIMARY_CONN, vpTCPComm2);
        mpConnMap.put(SECONDARY_CONN, vpTCPComm1);
      }
      else
      {
        mpConnMap.put(PRIMARY_CONN, vpTCPComm1);
        mpConnMap.put(SECONDARY_CONN, vpTCPComm2);
      }
    }
  }
}
