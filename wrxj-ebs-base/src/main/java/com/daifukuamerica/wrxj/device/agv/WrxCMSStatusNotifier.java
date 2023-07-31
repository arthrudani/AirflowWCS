package com.daifukuamerica.wrxj.device.agv;

import com.daifukuamerica.wrxj.clc.database.DatabaseControllerTypeDefinition;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;

/**
 * Warehouse Rx impl. of MOS Notifier of CMS status.
 *
 * @author A.D.
 * @since  30-Jun-2009
 */
public class WrxCMSStatusNotifier implements CMSStatusNotifier
{
  private String[] masStatusCollab;

  /**
   * Constructor with collaborator initialisation
   * @param iasStatusCollaborators array of collaborators for status messages.
   */
  public WrxCMSStatusNotifier(String[] iasStatusCollaborators)
  {
    masStatusCollab = iasStatusCollaborators;
  }

  @Override
  public void notifyCMSAvailable(boolean izAvailable)
  {
    if (masStatusCollab != null && masStatusCollab.length > 0)
    {
      SystemGateway vpJMSGateway = ThreadSystemGateway.get();
      LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
                                     DatabaseControllerTypeDefinition.AGV_TYPE);
      String isErrCode = (izAvailable) ? CMSStatusNotifier.CMS_ERROR_RESET :
                                         CMSStatusNotifier.CMS_IN_ERROR;
      String isText = vpLEDF.createDataMessage(isErrCode);

      for(int vnIdx = 0; vnIdx < masStatusCollab.length; vnIdx++)
      {
        vpJMSGateway.publishLoadEvent(isText, 0, masStatusCollab[vnIdx]);
      }
    }
  }

  @Override
  public void notifyLoadArrival(String isDestStation, String isLoadID) throws AGVException
  {
    StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);

    SystemGateway vpJMSGateway = ThreadSystemGateway.get();
    try
    {
      String vsScheduler = vpStnServ.getStationsScheduler(isDestStation);
      LoadEventDataFormat mpLEDF = Factory.create(LoadEventDataFormat.class, vsScheduler);
      String vsArrivalData = mpLEDF.createAGVLoadArrival(isDestStation, isLoadID);

      vpJMSGateway.publishLoadEvent(vsArrivalData, 0, vsScheduler);
    }
    catch(DBException exc)
    {
      throw new AGVException("Failed to find scheduler name for message publishing!", exc);
    }
  }

  @Override
  public void notifyLoadPickupComplete(String isLoadID) throws AGVException
  {
    StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);
    StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);

    SystemGateway vpJMSGateway = ThreadSystemGateway.get();
    try
    {
      String vsLoadId = isLoadID.trim();
      LoadData vpLoadData = vpLoadServ.getLoad(vsLoadId);
      if (vpLoadData != null && vpStnServ.exists(vpLoadData.getAddress()))
      {
        String vsScheduler = vpStnServ.getStationsScheduler(vpLoadData.getAddress());
        LoadEventDataFormat mpLEDF = Factory.create(LoadEventDataFormat.class,
                                                    vsScheduler);
        String vsArrivalData = mpLEDF.createAGVLoadPickupComplete(isLoadID);

        vpJMSGateway.publishLoadEvent(vsArrivalData, 0, vsScheduler);
      }
      else
      {
        throw new AGVException("Load " + isLoadID + " not found!  LPC not processed...");
      }
    }
    catch(DBException exc)
    {
      throw new AGVException("Failed to find scheduler name for message publishing!", exc);
    }
  }
}
