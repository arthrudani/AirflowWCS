package com.daifukuoc.wrxj.custom.ebs.dataserver;

import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSDevice;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeallocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.Device;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;

public class EBSDeviceServer extends StandardDeviceServer
{

	  protected EBSDevice mpEBSDevice = Factory.create(EBSDevice.class);
	
	public EBSDeviceServer()
	  {
	    super();
	  }

	  public EBSDeviceServer(String keyName)
	  {
	    super(keyName);
	  }
	  
	  /**
	   * Web application constructor for per user connection pooling
	   * @param keyName
	   * @param dbo
	   */
	  public EBSDeviceServer(String keyName, DBObject dbo)
	  {
		  super(keyName, dbo); 
	  }
	
	
	/**
	   * Method get the device that has the token then changes that devices token to
	   * false and sets the next devices token to true.
	   *
	   * @param aisleGroup Group <code>Integer</code> of the token to get.
	   *
	   * @return <code>String</code> containing device ID with token that is true.
	   */
	  public String getAndUpdateDeviceToken(String sWarhse )
	  {
	    String currentDevice = null;
	    TransactionToken tt = null;
	    try
	    {
	      tt = startTransaction();
	      currentDevice = mpEBSDevice.getAndUpdateDeviceToken(sWarhse);
	      commitTransaction(tt);
	    }
	    catch (DBException e)
	    {
	      logException(e, "Exception Getting Device Token for Warehouse \""
	          + sWarhse + "\"  - StandardDeviceServer.getDeviceToken");
	      currentDevice = null;
	    }
	    finally
	    {
	      endTransaction(tt);
	    }

	    return(currentDevice);
	  }
	
	  
	  /**
	   *  Modifies a Device Record based on DeviceData passed in, and the unique device id
	   *  key.
	   * @param dvdata Data class containing key and column modify settings.
	   * @throws com.daifukuamerica.wrxj.jdbc.DBException for database access errors.
	   * @return  Message indicating success or failure (useful for GUI mainly).
	   */
	  public String modifyDevice(DeviceData dvdata) throws DBException
	  {
	    /*
	     * TODO: Conventional devices REQUIRE a location for the device. This
	     * requirement, however, can break RTS retro-fits. We need to find a way to
	     * keep both happy.
	     */

	    TransactionToken tt = null;
	    try
	    {
	      tt = startTransaction();

	      DeviceData vpOldData = getDeviceData(dvdata.getDeviceID());
	      logModifyTransaction(vpOldData, dvdata);

	      mpDevice.modifyElement(dvdata);
	      // MCM, EBS
	      // don't deallocate if setting to INOP, the secondary crane can still retrieve 
//	      if(dvdata.getOperationalStatus() == DBConstants.INOP)
//	      {
//	          StandardDeallocationServer dealServ =
//	            Factory.create(StandardDeallocationServer.class, getClass().getSimpleName());
//	          dealServ.deallocateMovesForDevice(dvdata.getDeviceID());
//	          dealServ.cleanUp();
//	      }
	      commitTransaction(tt);
	    }
	    catch(DBException exc)
	    {
	      logException(exc, "StandardDeviceServer - modifyDevice");
	      throw exc;
	    }
	    finally
	    {
	      endTransaction(tt);
	    }
	    String vsMesg = "Device " + dvdata.getDeviceID() + " modified successfully.";

	    return(vsMesg);
	  }
	  
	  /**
	    * Method to return a the Secondary DeviceID for an  aisle
	    *
	    * @throws DBException for DB access errors.
	    */
	   public String getSecondaryDeviceID( String isDeviceID )
	       throws DBException
	   {
		  return mpEBSDevice.getSecondaryDeviceID(isDeviceID);
	  }
	   
	   /**
	    * Method to return a the LocSeqmethod
	    *
	    * @throws DBException for DB access errors.
	    */
	   public int getLocSeqMethodForDeviceID( String isDeviceID )
	       throws DBException
	       {
	         int status = 0;
	         if (isDeviceID.length() != 0)
	         {
	           try
	           {
	             status = mpEBSDevice.getLocSeqMethodForDeviceID(isDeviceID);
	           }
	           catch (Exception e)
	           {
	             logException(e, "EBSDeviceServer - Device \"" +
	            		 isDeviceID + "\" getLocSeqMethodForDeviceID()");
	           }
	         }
	         else
	         {
	           logError("EBSDeviceServer - Device \"" + isDeviceID
	               + "\" NOT Found - getLocSeqMethodForDeviceID()");
	         }

	         return status;
	       }
	  
}
