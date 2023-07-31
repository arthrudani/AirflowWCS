package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCUtility;


/**
 * <B>Description:</B> Timed event to update the status of the fullness for 
 * captive devices for the Equipment Monitor.  This class prevents 15 individual 
 * Equipment Monitors from beating the hell out of our poor database.
 * 
 * <P>Sample equipment configuration (must remove parenthetical notes):
 * <pre>
 *  EquipmentName   SRC-5:Fullness
 *  Description     FullnessMonitor
 *  Category        Equipment           (pretend this is monitoring equipment)
 *  ErrorSet        AS21
 *  MCController    Fullness            (FULLNESS_MONITOR_NAME)
 *  MOSController   *NONE*
 *  AisleGroup      *NONE*
 *  HostID          *NONE*
 *  DeviceID        *NONE*
 *  StationID       *NONE*
 *  MCID            SRC5                (device)
 *  MOSID           SRC5                (device)
 *  GraphicClass    FullnessMonitorButton
 * </pre></P>
 * 
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class CaptiveFullnessMonitor extends FullnessMonitor
{
  StandardLoadServer mpLoadServer = null;
  
  /**
   * Constructor
   * 
   * @param isName
   */
  public CaptiveFullnessMonitor(String isName)
  {
    super(isName);
  }

  /*========================================================================*/
  /* Overridden methods                                                     */
  /*========================================================================*/
  
  /**
   * @see com.daifukuamerica.wrxj.scheduler.event.FullnessMonitor#run()
   */
  @Override
  public void run()
  {
    if (mpLocServer == null)  // Reconfigure on first time through
    {
      mpLoadServer = Factory.create(StandardLoadServer.class);
      mpLocServer = Factory.create(StandardLocationServer.class);
      initializeLocationTotals();
    }
    
    try
    {
      // For each monitored device
      for (String vsDevice : masDevices)
      {
        int vnTotalLocations = mpLocationTotals.get(vsDevice);
        
        /*
         * Figure out the fullness. Note that this is for a captive rack. We
         * aren't counting empty locations (ie, those without loads) because
         * there shouldn't be any. A captive rack with no loads is thus 100%
         * "Full" since there are no empty containers.
         */
        double vdFullness = mpLoadServer.getTotalLoadFullnessByDevice(vsDevice);
        int vnPercentFull = SKDCUtility.convertDoubleToInt(100 * vdFullness
            / vnTotalLocations);

        // Send the message
        sendStatus(vsDevice, vnPercentFull);
      }
    }
    catch (DBException dbe)
    {
      mpLogger.logException(dbe);
    }
  }
}
