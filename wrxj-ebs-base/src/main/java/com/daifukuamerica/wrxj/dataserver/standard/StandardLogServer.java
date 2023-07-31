/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.WrxEquipLog;
import com.daifukuamerica.wrxj.dbadapter.data.WrxLog;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <B>Description:</B> Server for maintaining database logs
 *
 * <P>Copyright (c) 2018 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class StandardLogServer extends StandardServer
{
  protected WrxLog mpLogHandler = Factory.create(WrxLog.class);
  protected WrxEquipLog mpEquipLogHandler = Factory.create(WrxEquipLog.class);
  
  /**
   * Constructor
   */
  public StandardLogServer()
  {
  }

  /**
   * Constructor
   * 
   * @param isKeyName
   */
  public StandardLogServer(String isKeyName)
  {
    super(isKeyName);
  }

  /**
   * Purge logs
   * 
   * @param inDaysToKeep
   */
  public void purge(int inDaysToKeep)
  {
    TransactionToken tt = null;
    try
    {
      tt= startTransaction();
      
      Calendar c = Calendar.getInstance();
      c.add(Calendar.DAY_OF_YEAR, -inDaysToKeep);
      
      Date vpCutOff = c.getTime();
      mpLogger.logOperation(LogConsts.OPR_LOG,
          "Deleting all database logs before date: "
              + new SimpleDateFormat(SKDCConstants.DATETIME_FORMAT2).format(
                  vpCutOff));
      int vnCount = mpLogHandler.purge(vpCutOff);
      mpLogger.logOperation(LogConsts.OPR_LOG,
          "Deleted " + vnCount + " database logs");
      
      commitTransaction(tt);
    }
    catch (Exception e)
    {
      logException("Error purging logs", e);
    }
    finally
    {
      endTransaction(tt);
    }
  }
  
  /**
   * Purge logs
   * 
   * @param inDaysToKeep
   */
  public void purgeEquipLogs(int inDaysToKeep)
  {
    TransactionToken tt = null;
    try
    {
      tt= startTransaction();
      
      Calendar c = Calendar.getInstance();
      c.add(Calendar.DAY_OF_YEAR, -inDaysToKeep);
      
      Date vpCutOff = c.getTime();
      mpLogger.logOperation(LogConsts.OPR_LOG,
          "Deleting all database Equipment logs before date: "
              + new SimpleDateFormat(SKDCConstants.DATETIME_FORMAT2).format(
                  vpCutOff));
      int vnCount = mpEquipLogHandler.purge(vpCutOff);
      mpLogger.logOperation(LogConsts.OPR_LOG,
          "Deleted " + vnCount + " database logs");
      
      commitTransaction(tt);
    }
    catch (Exception e)
    {
      logException("Error purging logs", e);
    }
    finally
    {
      endTransaction(tt);
    }
  }
}
