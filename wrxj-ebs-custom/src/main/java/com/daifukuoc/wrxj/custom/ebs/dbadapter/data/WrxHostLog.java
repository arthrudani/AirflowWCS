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
package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Class to support database operations on the WRXLOG table
 */
public class WrxHostLog extends BaseDBInterface
{
	  private EBSTableJoin mpTableJoin = new EBSTableJoin();
	  
  public WrxHostLog()
  {
    super("WRXHOSTLOG", "WRXHOSTLOG", Factory.create(WrxHostLogData.class));
  }

  public List<Map> getDataQueueMessagesForWeb( WrxHostLogData ipKey )
	      throws DBException
	  {

		    List<Map> rtnList = null;
		    rtnList = mpTableJoin.getDataQueueMessagesForWeb( );
		    return rtnList;
	  }
}
