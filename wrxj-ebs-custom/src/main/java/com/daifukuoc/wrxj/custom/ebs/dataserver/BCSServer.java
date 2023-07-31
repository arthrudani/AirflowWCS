package com.daifukuoc.wrxj.custom.ebs.dataserver;


import java.util.List;
import java.util.Map;

import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSPurchaseOrderLineData;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSStationData;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class BCSServer extends StandardServer
{

	  protected EBSInventoryServer mpEBSInvServer = null;
	  protected StandardLoadServer mpLoadServer = null;
	  protected StandardStationServer mpStationServer = null;
	  protected EBSDeviceServer mpEBSDeviceServer = null;
	  protected EBSPoReceivingServer mpEBSPOServer = null;
	  protected EBSSchedulerServer mpEBSSchedulerServer = null;
	  protected EBSOrderServer mpOrderServer = null;
	  protected StandardLocationServer mpLocServer = null;
	  
	  protected String msMyClass = null;
	  private EBSTableJoin mpTableJoin = new EBSTableJoin();
	  private EBSStationData mpEBSSTData = Factory.create(EBSStationData.class);

  /**
   * Constructor w/o key name
   */
  public BCSServer()
  {
    this(null);
  }

  /**
   * Constructor with key name
   *
   * @param isKeyName
   */
  public BCSServer(String isKeyName)
  {
    super(isKeyName);
  }

  /**
   * Shuts down this controller by canceling any timers and shutting down the
   * Equipment.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
  }

  protected void initializeEBSInventoryServer()
  {
    if (mpEBSInvServer == null)
    {
      mpEBSInvServer = Factory.create(EBSInventoryServer.class, msMyClass);
    }
  }
  
  protected void initializeEBSScheduleServer()
  {
    if (mpEBSSchedulerServer == null)
    {
    	mpEBSSchedulerServer = Factory.create(EBSSchedulerServer.class, msMyClass);
    }
  }
  
  protected void initializeLoadServer()
  {
    if (mpLoadServer == null)
    {
    	mpLoadServer = Factory.create(StandardLoadServer.class, msMyClass);
    }
  }

  protected void initializeEBSDeviceServer()
  {
    if (mpEBSDeviceServer == null)
    {
    	mpEBSDeviceServer = Factory.create(EBSDeviceServer.class, msMyClass);
    }
  }

  protected void initializeOrderServer()
  {
    if (mpOrderServer == null)
    {
    	mpOrderServer = Factory.create(EBSOrderServer.class, msMyClass);
    }
  }
  
  protected void initializeStationServer()
  {
    if (mpStationServer == null)
    {
    	mpStationServer = Factory.create(StandardStationServer.class, msMyClass);
    }
  }
  
  protected void initializeEBSPOServer()
  {
    if (mpEBSPOServer == null)
    {
    	mpEBSPOServer = Factory.create(EBSPoReceivingServer.class, msMyClass);
    }
  }
  
  protected void initializeLocationServer()
  {
    if (mpLocServer == null)
    {
    	mpLocServer = Factory.create(StandardLocationServer.class, msMyClass);
    }
  }
  
  	/*
	 * Custom for EBS Send Custom Event message a BCS device handler.
	 *
	 * @param isLoadID
	 *
	 * @return
	 */
  public void processTrayArrival(String sTrayID, String sDestinationID, int iErrorCode)
  {
	  initializeEBSInventoryServer();
	  initializeEBSPOServer();
	  initializeEBSScheduleServer();
	  initializeLoadServer();
	  initializeOrderServer();
	  
	  if( sDestinationID.length() == 3 )
	  {
		  sDestinationID = "0" + sDestinationID;
	  }
	  

	  try 
	  {
		  // check for BR and reject to upper output
		  if( (iErrorCode == EBSConstants.TRAY_STATUS_UNKNOWN) || 
			  (sTrayID.equals(EBSConstants.LOAD_ARRIVAL_BR)) )
		  {
			  // create empty load at station and store for reject
			  String vsLoadID = null;
			    while(true)
			    {
			      vsLoadID = mpLoadServer.createRandomLoadID(SKDCConstants.BR_LOAD_PREFIX);
			      if(mpLoadServer.getLoad(vsLoadID) == null)
			      {
			        break;
			      }
			    } 
			  addRejectLoadForTrayArrival(vsLoadID, sDestinationID);
			  mpEBSInvServer.addBadReadItem(vsLoadID);
			  mpEBSSchedulerServer.checkToJoinIDPendingLoad(sDestinationID);
			  logError("Rejecting BadRead Tray " + vsLoadID + " at " + sDestinationID );
			  return;
		  }
		  
		  // check if tray already exists in the system, if so reject to upper output
		  if( mpLoadServer.loadExists(sTrayID) )
		  {
			  // create empty load at station and store for reject
			  String vsLoadID = null;
			    while(true)
			    {
			      vsLoadID = mpLoadServer.createRandomLoadID(SKDCConstants.DL_LOAD_PREFIX);
			      if(mpLoadServer.getLoad(vsLoadID) == null)
			      {
			        break;
			      }
			    } 
			  addRejectLoadForTrayArrival(vsLoadID, sDestinationID);
			  mpEBSInvServer.addDuplicateTrayItem(vsLoadID);
			  mpEBSSchedulerServer.checkToJoinIDPendingLoad(sDestinationID);
			  LoadData dupLoad = mpLoadServer.getLoad(sTrayID);
			  if( dupLoad != null )
			  {
				  logError("Rejecting Duplicate Tray " + vsLoadID + " at " + sDestinationID + 
						   ".  Tray = " + sTrayID + " at " + dupLoad.getAddress() );			 
			  }
			  else
			  {
			      logError("Rejecting Duplicate Tray " + vsLoadID + " at " + sDestinationID );
			  }
			  
			  // retrieve initial tray to destination 4 Manual coding
			  mpOrderServer.addDuplicateRetrievalOrder( EBSConstants.REJECT_STATION, sTrayID, EBSConstants.EBS_DUPLICATE_TRAY);
			  return;
		  }
		  
		  
		  
		  
		  
		  // Does ER exist for this trayID?
		  if( !mpEBSPOServer.expectedLoadExists(sTrayID) )
		  {
			  if( iErrorCode == EBSConstants.TRAY_STATUS_EMPTYTRAYSTACK) 
			  {
				// create unexpected empty tray stack at station and store
				  addLoadForTrayArrival(sTrayID, sDestinationID, EBSConstants.EMPTY_TRAY_STACK_HEIGHT);
				  mpEBSInvServer.addEmptyTrayStackItem(sTrayID);
				  logError("Storing UnExpected Emtpy Tray Stack " + sTrayID + " at " + sDestinationID );
			  }
			  else
			  {
				  // MCM, 21Oct2020
				  // Reject unknown bags
				 // create empty load at station and store
				 // addLoadForTrayArrival(sTrayID, sDestinationID, EBSConstants.UNKNOWN_ITEM_HEIGHT);
				 // logError("Storing Unknow Tray " + sTrayID + " at " + sDestinationID );
					
				  addRejectLoadForTrayArrival(sTrayID, sDestinationID);
					
				  mpEBSInvServer.addUnknownItem(sTrayID);
				  logError("Rejecting Unknown Bag on Tray " + sTrayID + " at " + sDestinationID );
			  }
			  mpEBSSchedulerServer.checkToJoinIDPendingLoad(sDestinationID);
			  return;
		  }
		 
		  // get POLine
		  EBSPurchaseOrderLineData oldata = Factory.create(EBSPurchaseOrderLineData.class);
		  oldata.clear();
		  oldata.setKey(PurchaseOrderLineData.LOADID_NAME, sTrayID);

		  EBSPurchaseOrderLineData elData;
	
		  elData = (EBSPurchaseOrderLineData) mpEBSPOServer.getPoLineRecord(oldata);
		  
		  if( elData != null )
	      {
			  // check if bagid already exists in the system, if so reject to upper output
			 String vsBagid = elData.getItem();
			 LoadLineItemData vpLLI = mpEBSInvServer.getLoadLineItem("", vsBagid, "", "", "","","");
		      if (vpLLI != null)
		      {
				  // create empty load at station and store for reject
				  String vsLoadID = null;
				    while(true)
				    {
				      vsLoadID = mpLoadServer.createRandomLoadID(SKDCConstants.DL_LOAD_PREFIX);
				      if(mpLoadServer.getLoad(vsLoadID) == null)
				      {
				        break;
				      }
				    } 
				  addRejectLoadForTrayArrival(vsLoadID, sDestinationID);
				  mpEBSInvServer.addDuplicateBagIdItem(vsLoadID);
				  mpEBSSchedulerServer.checkToJoinIDPendingLoad(sDestinationID);
				  LoadData dupLoad = mpLoadServer.getLoad(sTrayID);
				  if( dupLoad != null )
				  {
					  logError("Rejecting Duplicate Bagid " + vsBagid + " on TrayID " + vsLoadID + " at " + sDestinationID + 
							   ".  Tray = " + sTrayID + " at " + dupLoad.getAddress() );			 
				  }
				  else
				  {
				      logError("Rejecting Duplicate Bagid " + vsLoadID + " at " + sDestinationID );
				  }
				  
				  // retrieve initial bagid to destination 4 Manual coding
				  mpOrderServer.addDuplicateRetrievalOrder( EBSConstants.REJECT_STATION, vpLLI.getLoadID(), EBSConstants.EBS_DUPLICATE_BAGID);
				  return;
			  }
			  
			  
			  
			  
			  // Create load at destination 
			  // update the load height from ER
			  addLoadForTrayArrival(sTrayID, sDestinationID, elData.getHeight());
			  
			  // Add the appropriate inventory from the ER
			  mpEBSPOServer.receiveEntirePO( elData.getOrderID(), sTrayID );
			  
			  mpEBSSchedulerServer.checkToJoinIDPendingLoad(sDestinationID);
		  }
		  else
		  {
			  // create empty load at station and store
			  addLoadForTrayArrival(sTrayID, sDestinationID, EBSConstants.UNKNOWN_ITEM_HEIGHT);
			  mpEBSInvServer.addUnknownItem(sTrayID);
			  mpEBSSchedulerServer.checkToJoinIDPendingLoad(sDestinationID);
			  logError("Error reading ExpectedReceipt, Storing Unknow Tray " + sTrayID + " at " + sDestinationID );
			  return;
		  }
	  } 
	  catch (DBException e) 
	  {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
      
  }
  

  public void addLoadForTrayArrival(String sLoadid, String sStation, int iHeight )
  {

	   LoadData ld = null;
	   StationData stationData = null;
	   
	   initializeLoadServer();
	   initializeStationServer();
	   
	   // add the load before we add any items or store it
      try
      {
        ld = Factory.create(LoadData.class);
        stationData = mpStationServer.getStation(sStation);
        if (stationData == null)
        {
        	
        }
          ld.setWarehouse(stationData.getWarehouse());
          ld.setAddress(stationData.getStationName());
          ld.setLoadID(sLoadid);
          ld.setParentLoadID(sLoadid);
          ld.setHeight(iHeight);
          ld.setContainerType(stationData.getContainerType());
          ld.setDeviceID(stationData.getDeviceID());
          ld.setAmountFull(DBConstants.EMPTY);
          ld.setLoadMoveStatus(DBConstants.ARRIVEPENDING);
          mpLoadServer.addLoad(ld);
      }
      catch(DBException e2)
      {
        logException(e2, "Error adding Load " + sLoadid + " - " + e2.getMessage());
      }
  }
  
  

  public void addRejectLoadForTrayArrival(String sLoadid, String sStation )
  {

	   LoadData ld = null;
	   StationData stationData = null;
	   
	   initializeLoadServer();
	   initializeStationServer();
	   
	   // add the load before we add any items or store it
      try
      {
        ld = Factory.create(LoadData.class);
        stationData = mpStationServer.getStation(sStation);
        if (stationData == null)
        {
        	
        }
          ld.setWarehouse(stationData.getWarehouse());
          ld.setAddress(stationData.getStationName());
          ld.setLoadID(sLoadid);
          ld.setParentLoadID(sLoadid);
          ld.setContainerType(stationData.getContainerType());
          ld.setDeviceID(stationData.getDeviceID());
          ld.setAmountFull(DBConstants.EMPTY);
          
          ld.setNextWarehouse(stationData.getWarehouse());
          ld.setNextAddress(stationData.getRejectRoute());
          
          ld.setLoadMoveStatus(DBConstants.ARRIVEPENDING);
          mpLoadServer.addLoad(ld);
      }
      catch(DBException e2)
      {
        logException(e2, "Error adding Load " + sLoadid + " - " + e2.getMessage());
      }
  }

  
  public void addRejectDuplicateLoadForTrayArrival(String sLoadid, String sStation )
  {

	   LoadData ld = null;
	   StationData stationData = null;
	   
	   initializeLoadServer();
	   initializeStationServer();
	   
	   // add the load before we add any items or store it
      try
      {
        ld = Factory.create(LoadData.class);
        stationData = mpStationServer.getStation(sStation);
        if (stationData == null)
        {
        	
        }
          ld.setWarehouse(stationData.getWarehouse());
          ld.setAddress(stationData.getStationName());
          ld.setLoadID(sLoadid);
          ld.setParentLoadID(sLoadid);
          ld.setContainerType(stationData.getContainerType());
          ld.setDeviceID(stationData.getDeviceID());
          ld.setAmountFull(DBConstants.EMPTY);
          
          ld.setNextWarehouse(stationData.getWarehouse());
          ld.setNextAddress(stationData.getRejectRoute());
          
          ld.setLoadMoveStatus(DBConstants.ARRIVEPENDING);
          mpLoadServer.addLoad(ld);
      }
      catch(DBException e2)
      {
        logException(e2, "Error adding Load " + sLoadid + " - " + e2.getMessage());
      }
  }
  
  
  
  	/*
	 * Custom for EBS Send Custom Event message a BCS device handler.
	 *
	 * @param isLoadID
	 *
	 * @return
	 */
  public void sendEventToBCSDeviceHandler(String isDevice, String isCmdStr)
  {
	  // 	Send the BCS device handler a custom event message
	  ThreadSystemGateway.get().publishCustomEvent(isCmdStr, 0, isDevice,
                                               MessageEventConsts.CUSTOM_EVENT_TYPE,
                                               MessageEventConsts.CUSTOM_EVENT_TYPE_TEXT);
  }

  
  /**
   * Find  aisle for Load Storage (round robin aisles with available empty
   * locaitons, if matching assigned location not found)
   *
   * @param inAisleGroup - the LTW aisleGroup
   * @param isPalletType - the pallet type of the load we will be storing
   * @param isItem - the item on the load we will be storing
   * @param idAgingDate - the ageing date of the item on the load we will be
   * storing
   * @return IkeaStationData - destination Station or <code>null</code>
   * @throws DBException if there is one
   */
  public StationData findAisleForLoadStorage(int inHeight, String isItem, String isLot) throws DBException
  {

    String vsDeviceID = "";
    String vsInputStation = "";
    EBSStationData vpSD = null;
    List<Map> rtnList = null;
    Map currRow = null;

    initializeStationServer();
    initializeEBSDeviceServer();
	initializeLocationServer();

    // get a list of devices in balanced order based on the item/lot being stored
    if (vsDeviceID.isEmpty())
    {
    	 rtnList = mpTableJoin.getDeviceListForBalancedRoundRobin(isItem, isLot);
    }

    if ( rtnList != null && !rtnList.isEmpty() )
    {
    	String sDeviceIDToCheck = "";
    	boolean izLocFound = false;
    	for(int ldIdx = 0; ldIdx < rtnList.size() && !izLocFound; ldIdx++)
	   	{
	   		currRow = rtnList.get(ldIdx);
	   		sDeviceIDToCheck = DBHelper.getStringField(currRow, DeviceData.DEVICEID_NAME);
	        int vnAisleGroup = mpEBSDeviceServer.getDeviceAisleGroup(sDeviceIDToCheck);
	        
	   		vsInputStation = mpTableJoin.getInputStationByDevice(sDeviceIDToCheck);
	        vpSD = (EBSStationData) mpStationServer.getStation(vsInputStation);
	        if( vpSD == null )
	        {
	        	logError("BCSServer.findAisleForLoadStorage(), Error finding input station - " + vsInputStation);
	        	return null;
	        }
	        
	   		// Get enroute already to deviceid
	   		int iEnrCount = mpTableJoin.getEnrouteCountToAisleFromER( vsInputStation, 0);
	   		
	   		// Does current enroute exceed input station maxenr?
	   		if( iEnrCount >= vpSD.getMaxAllowedEnroute() )
	   		{
	   			logDebug("BCSServer.findAisleForLoadStorage(), Trays destined to " + vsInputStation + 
	   						" Equals maxEnr = " + vpSD.getMaxAllowedEnroute() );
	   			continue;
	   		}

	   		// Get available locations for deviceid for this height
	   		int iAvailLocsCount = mpLocServer.getLocationCount(vpSD.getWarehouse(), sDeviceIDToCheck, vnAisleGroup,
	   	          DBConstants.LCASRS, DBConstants.LCAVAIL, DBConstants.UNOCCUPIED, inHeight);
	   		
	   		if( mpEBSDeviceServer.isDeviceInoperable(vpSD.getSecondaryDeviceID()) )
	   		{
	   			// If the Secondary device is inoperable, include it's count of empties
	   			iAvailLocsCount += mpLocServer.getLocationCount(vpSD.getWarehouse(), vpSD.getSecondaryDeviceID(), vnAisleGroup,
	  	   	          DBConstants.LCASRS, DBConstants.LCAVAIL, DBConstants.UNOCCUPIED, inHeight);
	   		}
	   		
	   		// Get enroute for height already to deviceid
	   		iEnrCount = mpTableJoin.getEnrouteCountToAisleFromER( vsInputStation, inHeight);
	   		
	   		
	   		// determine if deviceid has locations available
	   		if( iAvailLocsCount > iEnrCount )
	   		{
	   			izLocFound = true;
	   			vsDeviceID = sDeviceIDToCheck;
	   		}   		
	   	}
    }

    // Get input station for this device
    if (!vsDeviceID.isEmpty())
    {
      vsInputStation = mpTableJoin.getInputStationByDevice(vsDeviceID);
      vpSD = (EBSStationData) mpStationServer.getStation(vsInputStation);
    }
    else
    {
    	logError("BCSServer.findAisleForLoadStorage(), No EMPTY Locations Available");
    }

    return vpSD;
  }
  
  
  
  public String getStationsBCSDevice( String isStation )
  {
	    initializeStationServer();
	    mpEBSSTData = (EBSStationData) mpStationServer.getStation(isStation);
	    return mpEBSSTData.getBCSDeviceID();
  }
  

  public String getLocationsBCSDevice( String isWarehouse, String isAddress ) throws DBException
  {
	    initializeStationServer();
	    initializeLocationServer();

	    String  vsOutputStation = mpTableJoin.getOutputStationByDevice(mpLocServer.getLocationDeviceId(isWarehouse, isAddress));
	    mpEBSSTData = (EBSStationData) mpStationServer.getStation(vsOutputStation);
	    return mpEBSSTData.getBCSDeviceID();
  }

//KR: new methods for KIX project below
/**
 * Finds the storage location for the Tray/Bag/Item in either south or north ASRS based on given parameters
 * @param isfinalSortLocation
 * @param inItemType
 * @return
 */
  public StationData findStorageInputStationByFinalSortLocation(String isfinalSortLocation,int inItemType)
  {
	    initializeStationServer(); 
	  /*Logic:
	   *  - Find ASRS close to finalSortLocation
	   *  - Check item type standard/Over sized to identify the storage type needed
	   *  
	   *  TODO: create a new table (FinalSortLocation/MakeupStations) which has relation ship with Warehouse table  WH one->Many NewTable 
	   *  this how we can identify which WH to choose when we receive a ExpctedReceipt message and need to decide which WH to try first ,,,,  
	  */
	  //1 - Identify which WH (south or north) to go
	  
	  //2 - Based on the item type(Standard or Over size) find out which storage need to go
	  
	  //3 - Get the INPUT station number of the storage  
	  
	  String vsInputStation = "2202"; // test only - S41
	  
	  StationData inputStation = mpStationServer.getStation(vsInputStation);
	  
	  return inputStation;
  }
}
