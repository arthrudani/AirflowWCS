package com.daifukuoc.wrxj.custom.ebs.dataserver;

import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveCommandServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ACPTableJoin;

public class ACPMoveCommandServer extends StandardMoveCommandServer{

	private ACPTableJoin mpTableJoin = null;
	/**
	 * Constructor for MoveCommand with no parameters
	 */
	public ACPMoveCommandServer() {
		super();
	}
	public void updateMoveCmdStatusById(String sDeviceId,String sLoadId,String fromLocation,String toLocation,int iStatus) 
	{
		 TransactionToken tt = null;
			try
			{
				  tt = startTransaction();
				  initializeTableJoin();
				  mpTableJoin.updateMoveCmdStatusById(sDeviceId, sLoadId,fromLocation,toLocation, iStatus);
				  commitTransaction(tt);
			}catch (DBException e)
			{
				String er = e.getMessage();
			}
		    finally
		    {
		      endTransaction(tt);
		    }
	}	
	 public void archiveCmds()
	 {
		 TransactionToken tt = null;
			try
			{
				  tt = startTransaction();
				  initializeTableJoin();
				  //Delete for now ....
				  mpTableJoin.archiveCMDsWithStatusDELETED();
				  commitTransaction(tt);
			}catch (DBException e)
			{
				String er = e.getMessage();
			}
		    finally
		    {
		      endTransaction(tt);
		    }
	 }
	 protected void initializeTableJoin() 
	 {
		if (mpTableJoin == null) {
		    mpTableJoin = Factory.create(ACPTableJoin.class);
		}
	 }

}
