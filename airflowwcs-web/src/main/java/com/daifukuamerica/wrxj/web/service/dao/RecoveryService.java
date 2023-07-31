package com.daifukuamerica.wrxj.web.service.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRecoveryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.core.messaging.JMSProducer;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;

/**
 * Base recovery logic rewritten for the web service layer and service layer
 *
 * Author: dystout
 * Created : Jun 12, 2018
 *
 */
public class RecoveryService
{
	/**
	 * Log4j logger: ContainerService
	 */
	private static final Logger logger = LoggerFactory.getLogger("FILE");
	private final String metaId = "Recovery";

	protected Location mpLoc = Factory.create(Location.class);

	protected LoadEventDataFormat mpLEDF  = Factory.create(LoadEventDataFormat.class, "Recovery");

	@Autowired
	private JMSProducer jmsProducer;

	/**
	 * Database retrieval of list containing load data for display on recovery screen implementations.
	 * (not in status NOMOVE, PICKED, STAGED, RECEIVED)
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		List<Map> data = getRecoveryData();
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : data)
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(data);

		return results;
	}

	public String getDeviceJmsTopic(String deviceId) throws DBException
	{
		String topic = null;
	    try {
	    	   SessionFactory factory = HibernateUtils.getSessionFactory();
	    	   Session session = factory.getCurrentSession();
	    	   if(!session.getTransaction().isActive())
	    		   session.getTransaction().begin();
	           topic =   (String) session.createQuery("select j.jmsTopic from Device d left join d.jvmConfig j  where d.id ='"+deviceId+"'").getSingleResult();

	           session.getTransaction().commit();
	           session.close();
	    }catch(Exception e){
	    	logger.error("Unable to get JMS Topic for device{} | ERROR: {}", deviceId, e.getMessage());
	    	e.printStackTrace(); //TODO remove
	    }
		return topic;
	}

	/**
	 * Retrieve load data records that are NOT in status of: NOMOVE, PICKED, STAGED, RECEIVED
	 *
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> getRecoveryData()
	{
		// See RecoveryListFrame.searchButtonPressed()
		List<KeyObject> vpSearchDataList = new ArrayList<KeyObject>();

		KeyObject vpStatusKey = new KeyObject(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(DBConstants.NOMOVE));
		vpStatusKey.setComparison(KeyObject.NOT_EQUAL);
		vpSearchDataList.add(vpStatusKey);

		KeyObject vpStatusKey2 = new KeyObject(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(DBConstants.PICKED));
		vpStatusKey2.setComparison(KeyObject.NOT_EQUAL);
		vpStatusKey2.setConjunction(KeyObject.AND);
		vpSearchDataList.add(vpStatusKey2);

		KeyObject vpStatusKey3 = new KeyObject(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(DBConstants.STAGED));
		vpStatusKey3.setComparison(KeyObject.NOT_EQUAL);
		vpStatusKey3.setConjunction(KeyObject.AND);
		vpSearchDataList.add(vpStatusKey3);

		KeyObject vpStatusKey4 = new KeyObject(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(DBConstants.RECEIVED));
		vpStatusKey4.setComparison(KeyObject.NOT_EQUAL);
		vpStatusKey4.setConjunction(KeyObject.AND);
		vpSearchDataList.add(vpStatusKey4);

		return getLoadList(KeyObject.toKeyArray(vpSearchDataList));
	}



	/*--------------------------------------------------------------------------*/
	/**
	 * Get the list to display in the Recovery screen
	 *
	 * @param iapColData
	 * @return
	 */
	public List<Map> getLoadList(KeyObject[] iapColData)
	{
		List vpList = null;
		try
		{
			StandardRecoveryServer mpRecoveryServer = Factory.create(StandardRecoveryServer.class);
			vpList = mpRecoveryServer.getRecoveryLoadDataList(iapColData);
		}
		catch (DBException e)
		{
			logger.error("Error getting recovery list data", e);
		}
		return vpList;
	}

	/*--------------------------------------------------------------------------*/
	/**
	 * Delete a load
	 *
	 * @param isLoadId
	 */
	public AjaxResponse deleteLoad(String isLoadId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			StandardInventoryServer mpInvServer = Factory.create(StandardInventoryServer.class);
			mpInvServer.deleteLoad(isLoadId, "");
		}
		catch (DBException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Failed to delete load \"" + isLoadId + "\" - "
					+ e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Deleted load " + isLoadId+ "!" );
		return ajaxResponse;
	}

	/*--------------------------------------------------------------------------*/
	/*--------------------------------------------------------------------------*/
	public AjaxResponse recoverArrival(String isLoadId, String isBarCode,
			String isStation, int iiLoadHeight)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		// Find out who is scheduling this station
		String scheduler = null;
		try
		{
			StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
			scheduler = mpStnServer.getStationsScheduler(isStation);
			// Send the scheduler event message
			String cmdstr = mpLEDF.processArrivalReport(
					AGCDeviceConstants.AGCDUMMYLOAD, isStation, iiLoadHeight, 1, isBarCode,
					"");
			ThreadSystemGateway.get().publishLoadEvent(cmdstr, 0, scheduler);
			logger.debug("Load \"{}\" \"Dummy Arrival\" Created", isLoadId);
		}
		catch(Exception ex)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error getting Scheduler attached to this station.");
		}


		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS,"Load \"" + isLoadId+ "\" \"Dummy Arrival\" Created");
		return ajaxResponse;
	}

	/***
	 * Test method for sending a string message to defaultTopic
	 * @param message
	 * @return
	 */
	public AjaxResponse testMessage(String message)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{
			jmsProducer.sendTestMessages(message);

		}catch(Exception e)
		{
			logger.error(e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
		}


		return ajaxResponse;

	}

	/* Custom for Ikea.  Send Custom Event message to LTW device handler.
	  * @param isLoadID
	  * @return
	*/
	public AjaxResponse sendEventToLTWDeviceHandler(String isDevice, String messageText, String jmsTopic)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{

			ajaxResponse = jmsProducer.publishCustomEvent(messageText, 0, isDevice,
											MessageEventConsts.CUSTOM_EVENT_TYPE,
											MessageEventConsts.CUSTOM_EVENT_TYPE_TEXT,
											jmsTopic);

		}catch(Exception e)
		{
			logger.error(e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
		}


		return ajaxResponse;

	}

	public LoadData getLoadData(String isLoadId)
	{
		StandardLoadServer loadServer = new StandardLoadServer();
		LoadData loadData = null;
		try {
			loadData = loadServer.getLoad1(isLoadId);
		} catch (DBException e) {
			logger.error("unable to find load data object for load: {}", isLoadId);
			e.printStackTrace();
		}
		return loadData;
	}

	/**
	 * TODO have you pushed work complete button YES/NO
	 * TODO get height at popup
	 *
	 * Recover an Arrival Pending load
	 *
	 * @param ipLoadData - load data object for load to recover
	 * @param height - height of the load given at popup request
	 * @throws DBException
	 */
	public AjaxResponse recoverArrivalPendingLoad(String loadId, int height)
			throws DBException
	{
		StandardLoadServer loadServer = new StandardLoadServer();
		LoadData ipLoadData = loadServer.getLoad(loadId);
		AjaxResponse ajaxResponse = new AjaxResponse();
		String vsLoadID = ipLoadData.getLoadID();
		StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
		StationData vsStationData = mpStnServer.getStation(ipLoadData.getAddress());

		if ((vsStationData.getStationType() == DBConstants.PDSTAND) ||
				(vsStationData.getStationType() == DBConstants.REVERSIBLE) ||
				(vsStationData.getStationType() == DBConstants.AGC_TRANSFER))
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Incompatible station type for Arrival Pending Load recovery" );
		}


		// find out who is scheduling this station
		String scheduler = mpStnServer.getStationsScheduler(vsStationData.getStationName());
		String topic = getDeviceJmsTopic(vsStationData.getDeviceID()); // get topic top publish to that is associated with the device id
		// send the scheduler event message
		try
		{
			String cmdstr = mpLEDF.processArrivalReport(AGCDeviceConstants.AGCDUMMYLOAD,
					vsStationData.getStationName(), height, 1, vsLoadID, "");
			ajaxResponse = jmsProducer.publishLoadEvent(cmdstr, 0, scheduler, topic);
			logger.debug("Load \"{}\" at station {}: re-sending Arrival", vsLoadID, vsStationData.getStationName());
		}
		catch (NullPointerException npe)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,"Null pointer: Arrival NOT sent for load \"" + vsLoadID + "\" | Message:" + npe.getMessage());
		}
		return ajaxResponse;


	}

	/**
	 * Given loadId, set it's parent load move status to setToMoveStatus.
	 *
	 * @param loadId - child loadId to look up parent load
	 * @param setToMoveStatus - move status to set parent load to
	 * @param message - message to append to move status change
	 *
	 * @return AjaxResponse - generic response object
	 */
	public AjaxResponse setParentLoadMoveStatus(String loadId, int setToMoveStatus, String message)
	{
		if(loadId==null)
			return new AjaxResponse(AjaxResponseCodes.FAILURE,"No load ID");
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{
			StandardLoadServer loadServer = new StandardLoadServer();
			loadServer.setParentLoadMoveStatus(loadId, setToMoveStatus, message);
		}catch(Exception e)
		{
			logger.error(e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
		}

		return ajaxResponse;

	}

	/**
	 * Uses pick server to auto-pick the load id from station.
	 *
	 * @param loadId
	 * @return ajax response
	 */
	public AjaxResponse autoPickLoad(String loadId)
	{
		if(loadId==null)
			return new AjaxResponse(AjaxResponseCodes.FAILURE,"No load ID");

		AjaxResponse ajaxResponse = new AjaxResponse();
		try{
			StandardPickServer pickServer = new StandardPickServer();
			if(pickServer.autoPickLoadFromStation(loadId))
				ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Auto-picked load: " +loadId );
			else
				ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to auto-pick load from station, check error logs");
		}catch(Exception e)
		{
			logger.error(e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
		}

		return ajaxResponse;
	}



	/**
	 * Recover a Moving load
	 * @param ipLoadData
	 * @throws DBException
	 */
	  protected AjaxResponse recoverMovingLoad(LoadData ipLoadData, boolean isRetrieved, boolean isCompleteMovement, boolean isRescheduleMovement) throws DBException
	  {
		AjaxResponse ajaxResponse = new AjaxResponse();
		StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
		try{

	 // A moving load might be a location-location load

		    if (mpStnServer.getStation(ipLoadData.getNextAddress()) == null)
		    {
		      ajaxResponse = recoverLocationToLocationLoad(ipLoadData, isRetrieved, isCompleteMovement);

		    }


	 // If it isn't at a station, or if the current station==next station,
	 // then this is a retrieving moving load

		    StationData vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
		    if (vpStationData == null ||
		        ipLoadData.getAddress().equals(ipLoadData.getNextAddress()))
		    {
		      ajaxResponse = recoverRetrieveMovingLoad(ipLoadData, isRetrieved, isRescheduleMovement);
		    }
		    else
		    {
		      ajaxResponse = recoverTransferMovingLoad(ipLoadData, isRetrieved, isRescheduleMovement);
		    }
		}catch(Exception e)
		{
			logger.error(e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to recover moving load | ERROR: "+e.getMessage());
		}


	    return ajaxResponse;
	  }

	/**
	 * TODO hasArrived- implement a YES/NO popup String vsDialogText = "Has load getLoadID() arrived at ipLoadData.getNextWarehouse(), ipLoadData.getNextAddress());

	     TODO reschedule retrieval(dependent on isInPrevLocation)- YES/NO Reschedule retrieval?

	 * Recovers a Moving load that is retrieving from a rack to a station
	 *
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected AjaxResponse recoverRetrieveMovingLoad(LoadData ipLoadData, boolean hasArrived, boolean isRescheduleRetrieval) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		StringBuilder sb = new StringBuilder();
		try{
			StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
			String vsLoadId = ipLoadData.getLoadID();
			StationData vpStationData = null;
			StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
			if(hasArrived)
			{
				//    find out who is scheduling this station
				vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
				if (vpStationData == null)
				{
					vpStationData = mpStnServer.getStation(ipLoadData.getNextAddress());
				}
				String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());

				//  send required completion message
				String cmdstr2 = mpLEDF.processArrivalReport(ipLoadData.getLoadID(),
						vpStationData.getStationName(), vpStationData.getHeight(), 1,
						ipLoadData.getBCRData(), "");
				ThreadSystemGateway.get().publishLoadEvent(cmdstr2,0,scheduler);

				logger.debug("Load \"{}\" Station: {} Re-Sent Arrival", vsLoadId, vpStationData.getStationName());
				sb.append("Load \"" + vsLoadId
						+ "\" Station: " + vpStationData.getStationName() + " Re-Sent Arrival");

			}
			if(isRescheduleRetrieval)
			{
				vpStationData = mpStnServer.getStation(ipLoadData.getNextAddress());

				//  find out who is scheduling this station
				if (vpStationData == null)
				{
					vpStationData = mpStnServer.getControllingStationFromLocation(ipLoadData.getWarehouse(),
							ipLoadData.getAddress());
				}
				String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());

				//  reset the load move status
				mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
						DBConstants.RETRIEVEPENDING, "Recovered by web console");

				//  send the scheduler event message
				String cmdstr = mpLEDF.processArrivalReport(AGCDeviceConstants.AGCDUMMYLOAD,
						vpStationData.getStationName(), vpStationData.getHeight(), 1, vsLoadId, "");
				ThreadSystemGateway.get().publishSchedulerEvent(cmdstr,0,scheduler);

				logger.debug("Load \"{}\" at station {}: re-scheduled Retrieval", vsLoadId, vpStationData.getStationName());
				sb.append("- Re-scheduled Retrieval for load \""
						+ vsLoadId + "\"");
			}
		}catch(Exception e){
			logger.error("ERROR recover retrieve moving load: {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Cannot retrieve moving load: " +e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, sb.toString());
		return ajaxResponse;
	}


	/**
	 * TODO - hasArrived condition ipLoadData.getNextWarehouse(), ipLoadData.getNextAddress());
	 * Recovers a Moving, Move Sent, or Move Error load that is moving station
	 * to station.
	 *
	 * TODO - reschedule movement condition - Do you want to re-schedule the movement of load
	 *
	 * This is nearly identical to recoverStoringLoad().  Perhaps the two should
	 * be combined.
	 *
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected AjaxResponse recoverTransferMovingLoad(LoadData ipLoadData, boolean hasArrived, boolean isRescheduleStore) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		StringBuilder successStringBuilder = new StringBuilder();

		try{
			StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
			StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
			StationData vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
			String isLoadId = ipLoadData.getLoadID();
			int vnLoadMoveStatus = ipLoadData.getLoadMoveStatus();
			LoadData vpTempLoadData = null;

			if (vnLoadMoveStatus == DBConstants.MOVEERROR ||
					vnLoadMoveStatus == DBConstants.MOVING)
			{
				//
				// First Check for older Move Error loads...if there are any,
				// make them recover them first
				//
				vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.MOVEERROR);
				if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
				{
					ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,"There Are \"Move Error\" loads... Recover them first.");
					return ajaxResponse;
				}
				else
				{
					// Then Check for older Moving loads...if there are any,
					// make them recover them first
					vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
							DBConstants.MOVING);
					if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
					{
						ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,"There are older \"Moving\" loads...\n" +
								"Either allow them to complete moving or recover them first");
						return ajaxResponse;
					}
				}
				if (hasArrived)
				{
					vpStationData = mpStnServer.getStation(ipLoadData.getNextAddress());
					// find out who is scheduling this station
					String scheduler = mpStnServer.getStationsScheduler(ipLoadData.getNextAddress());
					// send required completion messages
					String cmdstr;
					String vsRecoverType;
					if (vpStationData.getArrivalRequired() == DBConstants.YES)
					{
						cmdstr = mpLEDF.processArrivalReport(ipLoadData.getLoadID(),
								ipLoadData.getNextAddress(), ipLoadData.getHeight(), 0,
								ipLoadData.getLoadID(), "");
						vsRecoverType = "Arrival";
					}
					else
					{
						cmdstr = mpLEDF.processOperationCompletion(ipLoadData.getLoadID(),
								1, 0, ipLoadData.getAddress(), ipLoadData.getNextAddress(),
								ipLoadData.getNextAddress(), ipLoadData.getShelfPosition(),
								"000000000", ipLoadData.getNextShelfPosition(), 0, "", "", "");
						vsRecoverType = "Store Complete";
					}
					// send the load event message
					ThreadSystemGateway.get().publishLoadEvent(cmdstr,0,scheduler);
					logger.debug("Load \"{}\" at station {}: Re-Sent {}", isLoadId, ipLoadData.getAddress(), vsRecoverType);
					successStringBuilder.append(vsRecoverType + " sent for load \"" + isLoadId + " ");

				}
			}
			else if (vnLoadMoveStatus == DBConstants.MOVESENT)
			{
				vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.MOVEERROR);
				//
				// First Check for Move Error loads...if there are any,
				// make them recover them first
				//
				if(vpTempLoadData != null)
				{
					ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,"There are \"Move Error\" loads...\nRecover them first");
					return ajaxResponse;
				}
				vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.MOVING);
				//
				// First Check for Moving loads...if there are any, make them recover
				// them first (or wait till they are done moving)
				//
				if(vpTempLoadData != null)
				{
					ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,"There are Moving loads...\n" +
							"Either allow them to complete Moving or recover them first");
					return ajaxResponse;
				}
				else
				{
					// Then Check for OLDER Move sent loads...if there are any,
					// make them recover them first
					vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
							DBConstants.MOVESENT);
					if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
					{
						ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,"There Are older \"Move Sent\" loads...\nRecover them first");
						return ajaxResponse;
					}
				}
			}

			if (isRescheduleStore)
			{
				// find out who is scheduling this station
				String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());
				// reset the load move status
				mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
						DBConstants.MOVEPENDING, "Recovered by web console");
				// send the scheduler event message to wake up the scheduler
				String cmdstr = mpLEDF.moveLoadStationStation(isLoadId,
						vpStationData.getStationName(), vpStationData.getStationName(),
						null, isLoadId, "", ipLoadData.getHeight());
				ThreadSystemGateway.get().publishLoadEvent(cmdstr,0,scheduler);

				logger.debug("Load \"{}\": re-scheduled Store", isLoadId);
				successStringBuilder.append(" Load \"" + isLoadId + "\": re-scheduled Store");
			}

		}catch(Exception e)
		{
			logger.error(e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to recover Transfer Moving Load | "+e.getMessage());
		}

		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Re-scheduled Store for load " + successStringBuilder.toString());
		return ajaxResponse;
	}


	/**
	 * TODO  vsDialogText = "Has load \"" + ipLoadData.getLoadID()
		          + "\" been Retrieved from "
		          + mpLoc.describeLocation(ipLoadData.getWarehouse(), ipLoadData.getAddress())
		          + " to "
		          + mpLoc.describeLocation(ipLoadData.getNextWarehouse(), ipLoadData.getNextAddress());
		 TODO - isAutoMove -move load to next address in move record? ipLoadData.getLoadID() +
		                    " to " + ipLoadData.getNextAddress()

	 * Recover Retrieving, Retrieve Sent, and Retrieve Error loads
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected AjaxResponse recoverRetrievingLoad(LoadData ipLoadData, boolean isLoadRetrieved, boolean isAutoMove) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();

		try{
			StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
			StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
			StationData vpStationData = mpStnServer.getStation(ipLoadData.getNextAddress());

			/*
			 * If the station is null, this is hopefully a location-location move.
			 */
			if (vpStationData == null)
			{
				return recoverLocationToLocationLoad(ipLoadData, isLoadRetrieved, isAutoMove);
			}

			String vsLoadID = ipLoadData.getLoadID();
			int vnLoadMoveStatus = ipLoadData.getLoadMoveStatus();
			LoadData vpTempLoadData = null;

			if (vnLoadMoveStatus == DBConstants.RETRIEVEERROR ||
					vnLoadMoveStatus == DBConstants.RETRIEVING)
			{
				//
				// First Check for older retrieve error loads. If there are any,
				// make them recover them first (or wait till they are done retrieving)
				//
				vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.RETRIEVEERROR);
				if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(vsLoadID))
				{
					ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are \"Retrieve Error\" loads...\nRecover them first");
					return ajaxResponse;
				}
				else
				{
					// Then Check for OLDER retrieving loads. If there are any,
					// make them recover them first.
					vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
							DBConstants.RETRIEVING);
					if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(vsLoadID))
					{
						ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are older \"Retrieving\" loads...\n"
								+ "Either allow them to complete Retrieving or Recover them first");
						return ajaxResponse;
					}
				}

				if (isLoadRetrieved)
				{

					if (isAutoMove)
					{
						// find out who is scheduling this station
						String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());
						// send work complete to everyone
						String cmdstr = mpLEDF.processOperationCompletion(ipLoadData.getLoadID(),
								2,0,AGCDeviceConstants.RACKSTATION, vpStationData.getStationName(),
								ipLoadData.getAddress(), ipLoadData.getShelfPosition(), "",
								ipLoadData.getNextShelfPosition(), ipLoadData.getHeight(), "", "", "");

						ThreadSystemGateway.get().publishLoadEvent(cmdstr,0,scheduler);
						if(vpStationData.getArrivalRequired() == DBConstants.YES)
						{ // Send arrival only to stations that require it
							String cmdstr2 = mpLEDF.processArrivalReport(ipLoadData.getLoadID(),
									vpStationData.getStationName(), vpStationData.getHeight(), 1,
									ipLoadData.getBCRData(), "");
							ThreadSystemGateway.get().publishLoadEvent(cmdstr2,0,scheduler);
						}
						logger.debug("Load \"{}\" at station: {}: Re-Sent Arrival", vsLoadID, vpStationData.getStationName());
						ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Load \"" + vsLoadID
								+ "\" at station: " + vpStationData.getStationName()
								+ ": Re-Sent Arrival");
						return ajaxResponse;
					}


				}
			}
			else if (vnLoadMoveStatus == DBConstants.RETRIEVESENT)
			{
				vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.RETRIEVEERROR);
				//
				// First Check for retrieve error loads...if there are any,
				// make them recover them first
				//
				if(vpTempLoadData != null)
				{
					ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are \"Retrieve Error\" loads...\nRecover them first");
					return ajaxResponse;
				}
				vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.RETRIEVING);
				//
				// First Check for retrieving loads...if there are any, make them recover
				// them first (or wait till they are done retrieving)
				//
				if(vpTempLoadData != null)
				{
					ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are Retrieving loads...\n" +
							"Either allow them to complete Retrieving or Recover them first");
					return ajaxResponse;
				}
				else
				{
					//
					// Then Check for OLDER retrieve sent loads...if there are any,
					// make them recover them first
					//
					vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
							DBConstants.RETRIEVESENT);
					if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(vsLoadID))
					{
						ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are older \"Retrieve Sent\" loads...\n" +
								"Recover them first");
						return ajaxResponse;
					}
				}
			}


		}catch(Exception e){
			logger.error("Error in recovery: {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error in recovery: " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Recovery method completed" );
		return ajaxResponse;
	}

	/**
	 * Set a load back to retrieve pending and wake up the load's scheduler
	 *
	 * @param ipLoadData
	 * @param vpStationData
	 * @throws DBException
	 */
	protected AjaxResponse rescheduleRetrieve(LoadData ipLoadData,
			StationData vpStationData) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{
			StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
			StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
			String vsLoadID = ipLoadData.getLoadID();

			// find out who is scheduling this station
			String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());

			// reset the load move status
			mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
					DBConstants.RETRIEVEPENDING, "Rescheduled via web console");

			// send the scheduler event message
			AllocationMessageDataFormat vpAllocData = new AllocationMessageDataFormat();
			vpAllocData.setOutBoundLoad(vsLoadID);
			vpAllocData.setFromWarehouse(ipLoadData.getWarehouse());
			vpAllocData.setFromAddress(ipLoadData.getAddress());
			vpAllocData.setOutputStation(ipLoadData.getNextAddress());
			vpAllocData.createDataString();
			String cmdstr = vpAllocData.createStringToSend();
			ThreadSystemGateway.get().publishSchedulerEvent(cmdstr,0,scheduler);

			logger.debug("Load \"{}\" at station {}: re-scheduled Retrieval", vsLoadID, vpStationData.getStationName());
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Load \"" + vsLoadID
					+ "\" at station " + vpStationData.getStationName()
					+ ": re-scheduled Retrieval");
		}catch(Exception e)
		{
			logger.error("Error rescheduling retrieve | {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to reschedule retrieve | " +e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "Did not complete logic?"); // should not hit this if all is successfull

		return ajaxResponse;
	}

	/**
	 * Set a load back to retrieve pending and wake up the load's scheduler for
	 * location-to-location moves
	 *
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected AjaxResponse rescheduleRetrieve(LoadData ipLoadData) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{

			StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
			String vsLoadID = ipLoadData.getLoadID();

			// find out who is scheduling this load
			String vsScheduler = Factory.create(StandardDeviceServer.class)
					.getSchedulerName(ipLoadData.getDeviceID());

			// reset the load move status
			mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
					DBConstants.RETRIEVEPENDING, "Rescheduled via web console");

			// send the scheduler event message
			LoadEventDataFormat vpSwapMessage = Factory.create(
					LoadEventDataFormat.class, vsScheduler);
			String vsCommand = vpSwapMessage.moveLoadLocationLocation(
					ipLoadData.getParentLoadID(), ipLoadData.getAddress(),
					ipLoadData.getShelfPosition(), ipLoadData.getWarehouse(),
					ipLoadData.getNextAddress(), ipLoadData.getNextShelfPosition(),
					ipLoadData.getWarehouse(), ipLoadData.getHeight());

			ThreadSystemGateway.get().publishLoadEvent(vsCommand, 0, ipLoadData.getDeviceID());

			String vsMessage = ipLoadData.getNextAddress().trim().length() > 0 ?
					"Re-scheduled Loc-to-Loc Retrieval for load \""
					+ vsLoadID + "\" to " + ipLoadData.getNextAddress()
					: "Re-scheduled Retrieval for load \"" + vsLoadID + "\"";
					logger.debug(vsMessage);
					ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS,vsMessage);
		}catch(Exception e)
		{
			logger.error("Error scheduling retrieve: {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error scheduling retrieve: " + e.getMessage());
		}
		return ajaxResponse;
	}

	/**
	 *
	 * TODO isStoredInNext has load been stored at getnextwarehouse getnextaddress
	 * TODO "This load should not be Storing.\nCancel the Store?" isCancelStore
	     TODO "reschedule storage of load? y/n
	 * Recover Storing, Store Sent, and Store Error loads
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected AjaxResponse recoverStoringLoad(LoadData ipLoadData, boolean isStoredInNext, boolean isReschedulingStore) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{
			StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
			StationData vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
			StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
			if (vpStationData == null)
			{
				return new AjaxResponse(AjaxResponseCodes.PROMPT, "This load has no address data, do you want to cancel the store?");

			}
			String isLoadId = ipLoadData.getLoadID();
			int vnLoadMoveStatus = ipLoadData.getLoadMoveStatus();
			LoadData vpTempLoadData = null;

			if (vnLoadMoveStatus == DBConstants.STOREERROR ||
					vnLoadMoveStatus == DBConstants.STORING)
			{
				//
				// First Check for older Store error loads...if there are any,
				// make them recover them first
				//
				vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.STOREERROR);
				if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
				{
					ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are \"Store Error\" loads...\nRecover them first.");
					return ajaxResponse;
				}
				else
				{
					// Then Check for OLDER storing loads...if there are any,
					// make them recover them first
					vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
							DBConstants.STORING);
					if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
					{
						ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are older \"Storing\" loads...\n"
								+ "Either allow them to complete Storing or Recover them first");
						return ajaxResponse;
					}
				}

				if (isStoredInNext)
				{
					vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
					// find out who is scheduling this station
					String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());
					String jmsTopic = getDeviceJmsTopic(scheduler);
					// send required completion messages
					String cmdstr = mpLEDF.processOperationCompletion(ipLoadData.getLoadID(),
							1, 0, vpStationData.getStationName(), AGCDeviceConstants.RACKSTATION,
							ipLoadData.getNextAddress(), ipLoadData.getShelfPosition(),
							"000000000", ipLoadData.getNextShelfPosition(), 0, "", "", "");
					// send the load event message
					jmsProducer.publishLoadEvent(cmdstr, 0, scheduler, jmsTopic);
					logger.debug("Load \"{}\" at station {}: Re-Sent Store Complete", isLoadId, vpStationData.getStationName());
					ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Load \"" + isLoadId
							+ "\" at station " + vpStationData.getStationName()
							+ ": Re-Sent Store Complete");
					return ajaxResponse;
				}
			}
			else if (vnLoadMoveStatus == DBConstants.STORESENT)
			{
				vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.STOREERROR);
				//
				// First Check for Store error loads...if there are any,
				// make them recover them first
				//
				if(vpTempLoadData != null)
				{
					ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are \"Store Error\" loads...\nRecover them first");
					return ajaxResponse;
				}
				vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.STORING);
				//
				// First Check for storing loads...if there are any, make them recover
				// them first (or wait till they are done storing)
				//
				if(vpTempLoadData != null)
				{
					ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are Storing loads...\n"
							+ "Either allow them to complete Storing or Recover them first");
					return ajaxResponse;
				}
				else
				{
					// Then Check for OLDER Store sent loads...if there are any,
					// make them recover them first
					vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
							DBConstants.STORESENT);
					if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
					{
						ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There are older \"Store Sent\" loads...\nRecover them first");
						return ajaxResponse;
					}
				}
			}


			if (isReschedulingStore)
			{
				// find out who is scheduling this station
				String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());
				String jmsTopic = getDeviceJmsTopic(scheduler);
				// reset the load move status
				mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
						DBConstants.STOREPENDING, "rescheduling from web console");
				// send the scheduler event message to wake up the scheduler
				String cmdstr = mpLEDF.moveLoadStationStation(isLoadId,
						vpStationData.getStationName(), vpStationData.getStationName(),
						null, isLoadId, "", ipLoadData.getHeight());
				jmsProducer.publishLoadEvent(cmdstr, 0, scheduler, jmsTopic);

				logger.debug("Load \"{}\": re-scheduled Store", isLoadId);
				ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Re-scheduled Store for load \"" + isLoadId + "\"");
			}
		}catch(Exception e){
			logger.error("error");
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error");
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "Did not complete logic?"); // should not hit this if all is successfull

		return ajaxResponse;
	}

	/**
	 * TODO "This load should not be Storing.\nCancel the Store?" isCancelStore
	 *
	 * Recover a load that claims to be "Storing" but is in the rack.
	 *
	 * @param ipLoadData
	 */
	protected AjaxResponse recoverBadStoringLoad(LoadData ipLoadData, boolean isCancelStore) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{

			StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
			if (isCancelStore)
			{
				mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(), DBConstants.NOMOVE, "");
				ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Cancelled Store for load \"" + ipLoadData.getLoadID() + "\"");
			}
		}catch(Exception e){
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to cancel store load " + e.getMessage());
			logger.error("error with recover bad storing load {}", e.getMessage());
		}
		return ajaxResponse;
	}

	/**
	 *
	 * TODO isRetrieved "has load x been retrieved from y to nextwarehouse/address z" y/n
	 * TODO isCompleteMovement  automatically complete the movement bt setting the load to the next address y/n
	 *
	 * Recover a load that is moving from one rack location to another rack
	 * location (or at least not moving to a station).
	 *
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected AjaxResponse recoverLocationToLocationLoad(LoadData ipLoadData, boolean isRetrieved, boolean isCompleteMovement)
			throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{
			StandardSchedulerServer mpSchedServer = Factory.create(StandardSchedulerServer.class);
			if (ipLoadData.getNextAddress().trim().length() > 0 &&
					isRetrieved)
			{

				if (isCompleteMovement)
				{
					mpLEDF.setLoadID(ipLoadData.getLoadID());
					mpLEDF.setSourceLocation(ipLoadData.getNextAddress());
					mpLEDF.setDestinationLocation(ipLoadData.getNextAddress());

					mpSchedServer.updateLoadForShelfToShelfStoreComplete(mpLEDF);
					logger.debug("Recovered load \"{}\": moved to {}", ipLoadData.getLoadID(), mpLoc.describeLocation(ipLoadData.getNextWarehouse(),
							ipLoadData.getNextAddress()));
					ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Recovered load \"" + ipLoadData.getLoadID() + "\": moved to "
							+ mpLoc.describeLocation(ipLoadData.getNextWarehouse(),
									ipLoadData.getNextAddress()));
				}
			}
		}catch(Exception e)
		{
			logger.error("Unable to recover location to location {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to recover location to location " + e.getMessage());
		}

		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "Did not complete logic?"); // should not hit this if all is successfull

		return ajaxResponse;


	}

	/**
	 * Recover a Retrieve Pending load that is stuck because its order/move was
	 * forcibly deleted.
	 *
	 * @param ipLD - LoadData

	 * @throws DBException
	 */
	protected AjaxResponse recoverRetrievePendingLoad(LoadData ipLD) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{
			StandardMoveServer mpMoveServer = Factory.create(StandardMoveServer.class);
			StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
			/*
			 * If there are no moves for this load, and the AGC/SRC isn't going
			 * to move it, then cancel the move.
			 */
			MoveData vpMoveData = mpMoveServer.getNextMoveRecord(ipLD.getLoadID());
			if (vpMoveData == null)
			{

				mpLoadServer.setParentLoadMoveStatus(ipLD.getLoadID(), DBConstants.NOMOVE, "");
				ipLD.setLoadMoveStatus(DBConstants.NOMOVE);
			}
		}catch(Exception e)
		{
			logger.error(e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to recover retrieve pending load " +e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Set parent load status to no move" );
		return ajaxResponse;
	}

	/**
	 * Recover a Store Pending load that is stuck because the mode change stuff
	 * on the SRC is retarded and reports Retrieve when it is really in a post-
	 * pick store state.
	 *
	 * TODO isForceIntoStoreMode - Force Station into Store Mode in Warehouse Rx?
	 *
	 * @param ipLD - LoadData
	 * @return true if it was recovered, false if it doesn't need it.
	 */
	protected AjaxResponse recoverStorePendingLoad(LoadData ipLD, boolean isForceIntoStoreMode)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try{
			StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
			StationData vpSD = mpStnServer.getStation(ipLD.getAddress());
			if (vpSD != null && vpSD.getStationType() == DBConstants.REVERSIBLE)
			{
				if (vpSD.getBidirectionalStatus() != DBConstants.STOREMODE)
				{
					if (isForceIntoStoreMode)
					{
						mpStnServer.setBidirectionalMode(vpSD.getStationName(),
								DBConstants.STOREMODE);

						String cmdstr = mpLEDF.moveLoadStationStation(ipLD.getLoadID(),
								vpSD.getStationName(), vpSD.getStationName(),
								null, ipLD.getLoadID(), "", ipLD.getHeight());
						String vsScheduler;
						try
						{
							vsScheduler =  mpStnServer.getStationsScheduler(vpSD.getStationName());
						}
						catch(DBException e)
						{
							ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,"Scheduler not found attached to station " + vpSD.getStationName());
							return ajaxResponse;
						}

						ThreadSystemGateway.get().publishLoadEvent(cmdstr, 0, vsScheduler);

						logger.debug("Forced Store Mode for station {}", vpSD.getStationName());
						ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS,"Forced Store Mode for station " + vpSD.getStationName());
					}
				}
			}

		}catch(Exception e ){
			logger.error("Error with Store Pending load recovery | error: {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error with Store Pending load recovery | error: " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed" );
		return ajaxResponse;

	}

	/**
	 * Recover a load in ARRIVED status by auto-picking the load
	 * @param loadId
	 * @return
	 */
	protected AjaxResponse recoverArrivedLoad(String loadId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();

		try
		{
			StandardStationServer stationServer = new StandardStationServer();
			StandardLoadServer loadServer = new StandardLoadServer();
			StandardPickServer pickServer = new StandardPickServer();
			LoadData loadData = loadServer.getLoad1(loadId);
			if(loadData!=null)
			{
				 StationData vsStationData = stationServer.getStation(loadData.getAddress());
				    if (vsStationData == null)
				    {
				    	loadServer.setParentLoadMoveStatus(loadData.getLoadID(),
				          DBConstants.NOMOVE, "");
				    }
				    else if (vsStationData.getStationType() == DBConstants.OUTPUT  &&
				             vsStationData.getDeleteInventory() == DBConstants.YES &&
				             (vsStationData.getAutoLoadMovementType() == DBConstants.AUTOPICK ||
				              vsStationData.getAutoLoadMovementType() == DBConstants.BOTH))
				    {
				      pickServer.autoPickLoadFromStation(loadData.getLoadID());
				      ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully auto picked load" );
				    }
			}else{
				ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error with auto picking load  | error: Load Data does no longer exists!");
			}
		} catch (Exception e)
		{
			logger.error("Error with recovering ARRIVED load | error: {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error with recovering ARRIVED load  | error: " + e.getMessage());

		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed" );
		return ajaxResponse;
	}








}
