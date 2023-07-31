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
package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import java.util.Date;

/**
 * Class to support database operations on the WRXLOG table
 */
public class WrxLog extends BaseDBInterface
{
  public WrxLog()
  {
    super("WRXLOG", "WRXLOG", Factory.create(WrxLogData.class));
  }

  /**
   * Delete a particular WrxLogError record
   * 
   * @param ipEarliestDateTime
   * @throws DBException
   * @return number of records deleted
   */
  public int purge(Date ipEarliestDateTime) throws DBException
  {
    WrxLogData vpKey = Factory.create(WrxLogData.class);
    vpKey.setKey(WrxLogData.DATE_TIME_NAME, ipEarliestDateTime, KeyObject.LESS_THAN);
    int vnCount = getCount(vpKey);
    if (vnCount > 0)
    {
      setMaxRows(Integer.MAX_VALUE);
      deleteElement(vpKey);
      setMaxRows();
    }
    return vnCount;
  }
}
