/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.RecoveryAjaxResponse;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load Service to handle any CRUD actions or business logic for Loads.
 * Validation should happen as much as it can at the Model Validator level with
 * JSR 303 annotations on the models uses for form input, however, if all else
 * fails validation at this level can be accepted.
 *
 * This is an example of the most functional controller to date. Chances are
 * this will become the standard which most controller Services will be built.
 *
 * Author: dystout Created : May 4, 2017
 *
 */
public class Recovery2Service
{
	/**
	 * Log4j logger: LoadService
	 */
	private static final Logger logger = LoggerFactory.getLogger("RECOVERY");
	private static final String metaId = "Recovery";

	private static final String NO_RECOVERY_NEEDED = "Load [%s] does not need recovery";
	private static final String NO_RECOVERY_PENDING = "Load [%s] is [%s]"
						+ "\nbecause the scheduler hasn't scheduled it yet."
						+ "\n\nIf the move status does not change automatically,"
						+ "\nplease check the Error Log.";
	private static final String NO_RECOVERY_PERFORMED = "No recovery performed.";
	private static final String NO_RECOVERY_ON_DEVICE = "Cannot reschedule on device--no recovery performed.";

	private static final String ACTION_AUTOPICK = "autopick";
	private static final String ACTION_CANCEL = "cancel";
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_RESCHEDULE = "reschedule";
	private static final String ACTION_SEND_ARRIVAL = "sendarrival";
	private static final String ACTION_SEND_COMPLETION = "sendcompletion";
	private static final String ACTION_STORE_MODE = "storemode";

	/**
	 * List all non-stationary loads
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		return listSearch(Factory.create(LoadData.class));
	}

	/**
	 * List non-stationary loads by search criteria
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(LoadData searchLDData) throws DBException, NoSuchFieldException
	{
		// See RecoveryListFrame.searchButtonPressed()
		// Add keys to exclude stationary loads

		KeyObject vpStatusKey = new KeyObject(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(DBConstants.NOMOVE));
		vpStatusKey.setComparison(KeyObject.NOT_EQUAL);
		vpStatusKey.setConjunction(KeyObject.AND);
		searchLDData.addKeyObject(vpStatusKey);

		KeyObject vpStatusKey2 = new KeyObject(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(DBConstants.PICKED));
		vpStatusKey2.setComparison(KeyObject.NOT_EQUAL);
		vpStatusKey2.setConjunction(KeyObject.AND);
		searchLDData.addKeyObject(vpStatusKey2);

		KeyObject vpStatusKey3 = new KeyObject(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(DBConstants.STAGED));
		vpStatusKey3.setComparison(KeyObject.NOT_EQUAL);
		vpStatusKey3.setConjunction(KeyObject.AND);
		searchLDData.addKeyObject(vpStatusKey3);

		KeyObject vpStatusKey4 = new KeyObject(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(DBConstants.RECEIVED));
		vpStatusKey4.setComparison(KeyObject.NOT_EQUAL);
		vpStatusKey4.setConjunction(KeyObject.AND);
		searchLDData.addKeyObject(vpStatusKey4);

		List<Map> utLoadData = Factory.create(Load.class).getAllElements(searchLDData);
		AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(utLoadData, metaId);
		TableDataModel results = new TableDataModel(utLoadData);
		return results;
	}

	/*==============================================================*/
	/* Recovery Flow 												*/
	/*==============================================================*/

	/**
	 * Get a logic tree that can be executed by the UI to gather info and take the
	 * appropriate recovery steps.
	 *
	 * @param isLoadId
	 * @param ipUser
	 * @return
	 */
	public RecoveryAjaxResponse getRecoveryFlow(String isLoadId, User ipUser)
	{
		/*
		 * Lots of logic here because the WRx LoadRecovery class is tightly coupled with
		 * Swing and basically had to be reproduced.
		 *
		 * The alternative to returning a logic tree would be to make an Ajax call after
		 * every question, and doing so would require a lot of extra UI work and
		 * redundant database calls.
		 */
		try
		{
			// Get the load
			LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadId);
			if (vpLoadData == null)
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.FAILURE, "Load [" + isLoadId + "] not found!");
			}

			String vsLoadStatus = DBTrans.getStringValueNoExc(LoadData.LOADMOVESTATUS_NAME,
					vpLoadData.getLoadMoveStatus());
			logger.info(String.format("User [%s]: Initiating recovery for load [%s] with status [%s]",
					ipUser.getUserId(), isLoadId, vsLoadStatus));

			switch (vpLoadData.getLoadMoveStatus())
			{
			case DBConstants.ARRIVEPENDING:
				return recoverArrivalPendingLoad(vpLoadData);

			case DBConstants.ARRIVED:
				return recoverArrivedLoad(vpLoadData);

			case DBConstants.IDPENDING:
				return recoverIDPendingLoad(vpLoadData);

			case DBConstants.MOVEPENDING:
				return recoverMovePendingLoad(vpLoadData, vsLoadStatus);

			case DBConstants.MOVEERROR:
			case DBConstants.MOVING:
			case DBConstants.MOVESENT:
				return recoverMovingLoad(vpLoadData);

			case DBConstants.RETRIEVEPENDING:
				return recoverRetrievePendingLoad(vpLoadData, vsLoadStatus);

			case DBConstants.RETRIEVEERROR:
			case DBConstants.RETRIEVING:
			case DBConstants.RETRIEVESENT:
				return recoverRetrievingLoad(vpLoadData, vsLoadStatus);

			case DBConstants.STOREPENDING:
				return recoverStorePendingLoad(vpLoadData, vsLoadStatus);

			case DBConstants.STOREERROR:
			case DBConstants.STORING:
			case DBConstants.STORESENT:
				return recoverStoringLoad(vpLoadData, vsLoadStatus);

			default:
				String vsMessage = "Recovery not implemented for status=[" + vsLoadStatus + "]";
				logger.warn(vsMessage);
				return new RecoveryAjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
			}
		}
		catch (Exception e)
		{
			String vsMessage = "Error getting recovery info for [" + isLoadId + "]";
			logger.error(vsMessage, e);
			return new RecoveryAjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
		}
	}

	/*--------------------------------------------------------------*/
	/* Recovery Flow - Arrival Pending 								*/
	/*--------------------------------------------------------------*/

	/**
	 * Recover an Arrival Pending load
	 *
	 * @param isLoadId
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverArrivalPendingLoad(LoadData ipLoadData) throws DBException
	{
		RecoveryAjaxResponse vpReturn = new RecoveryAjaxResponse();
		RecoveryAjaxResponse vpCurrent = vpReturn;

		StationData vpStationData = Factory.create(StandardStationServer.class).getStation(ipLoadData.getAddress());
		if (vpStationData == null)
		{
			String vsPosition = SKDCUtility.isBlank(ipLoadData.getShelfPosition())
					|| ipLoadData.getShelfPosition().equals(LoadData.DEFAULT_POSITION_VALUE) ? ""
							: ipLoadData.getShelfPosition();
			String vsMessage = String.format("Unable to recover load [%s] at [%s-%s%s]", ipLoadData.getLoadID(),
					ipLoadData.getWarehouse(), ipLoadData.getAddress(), vsPosition);
			return new RecoveryAjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
		}

		if ((vpStationData.getStationType() != DBConstants.PDSTAND)
				&& (vpStationData.getStationType() != DBConstants.REVERSIBLE)
				&& (vpStationData.getStationType() != DBConstants.AGC_TRANSFER))
		{
			vpCurrent.setResponseCode(AjaxResponseCodes.PROMPT);
			vpCurrent.setResponseMessage("Have you pressed the Work Complete button?");
			vpCurrent.setYes(new RecoveryAjaxResponse());
			vpCurrent.setNo(new RecoveryAjaxResponse(AjaxResponseCodes.INFO, "Please press the Work Complete button."));
			vpCurrent = vpCurrent.getYes();
		}

		vpCurrent.setResponseCode(AjaxResponseCodes.PROMPT);
		vpCurrent.setResponseMessage("Do you want to resend the Arrival for load [" + ipLoadData.getLoadID() + "]?");
		vpCurrent.setYes(new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_SEND_ARRIVAL));
		vpCurrent.setNo(new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));

		return vpReturn;
	}

	/*--------------------------------------------------------------*/
	/* Recovery Flow - Arrived		 								*/
	/*--------------------------------------------------------------*/

	/**
	 * Recover Arrived loads
	 *
	 * @param loadData
	 */
	protected RecoveryAjaxResponse recoverArrivedLoad(LoadData loadData)
	{
		StationData vsStationData = Factory.create(StandardStationServer.class).getStation(loadData.getAddress());
		if (vsStationData == null)
		{
			Factory.create(StandardLoadServer.class).setParentLoadMoveStatus(loadData.getLoadID(), DBConstants.NOMOVE,
					"");
			return new RecoveryAjaxResponse(AjaxResponseCodes.SUCCESS, "Recovered load [" + loadData.getLoadID() + "]");
		}
		else if (vsStationData.getStationType() == DBConstants.OUTPUT
				&& vsStationData.getDeleteInventory() == DBConstants.YES
				&& (vsStationData.getAutoLoadMovementType() == DBConstants.AUTOPICK
						|| vsStationData.getAutoLoadMovementType() == DBConstants.BOTH))
		{
			return new RecoveryAjaxResponse("Auto-Pick load [" + loadData.getLoadID() + "]?",
					new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_AUTOPICK),
					new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));
		}
		return new RecoveryAjaxResponse(AjaxResponseCodes.INFO,
				String.format(NO_RECOVERY_NEEDED, loadData.getLoadID()));
	}

	/*--------------------------------------------------------------*/
	/* Recovery Flow - ID Pending	 								*/
	/*--------------------------------------------------------------*/

	/**
	 * Recover (delete) ID Pending loads
	 * @param isLoadID
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverIDPendingLoad(LoadData loadData) throws DBException
	{
		return new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_DELETE);
	}

	/*--------------------------------------------------------------*/
	/* Recovery Flow - Move Error, Move Sent, Moving				*/
	/*--------------------------------------------------------------*/

	/**
	 * Recover a Moving load
	 *
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverMovingLoad(LoadData ipLoadData) throws DBException
	{
		// Special handling for a load on a device
		LocationData vpLocData = Factory.create(Location.class).getLocation(ipLoadData.getWarehouse(), ipLoadData.getAddress());
		if (vpLocData.getLocationType() == DBConstants.LCDEVICE)
		{
			return recoverLoadOnDevice(ipLoadData);
		}

		StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);

		/*
		 * A moving load might be a location-location load
		 */
		if (vpStnServer.getStation(ipLoadData.getNextAddress()) == null)
		{
			return recoverLocationToLocationLoad(ipLoadData);
		}

		/*
		 * If it isn't at a station, or if the current station==next station, then this
		 * is a retrieving moving load
		 */
		StationData vpStationData = vpStnServer.getStation(ipLoadData.getAddress());
		if (vpStationData == null || ipLoadData.getAddress().equals(ipLoadData.getNextAddress()))
		{
			return recoverRetrieveMovingLoad(ipLoadData);
		}
		else
		{
			return recoverTransferMovingLoad(ipLoadData);
		}
	}

	/**
	 * Recovers a Moving load that is retrieving from a rack to a station
	 *
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverRetrieveMovingLoad(LoadData ipLoadData) throws DBException
	{
		RecoveryAjaxResponse vpReturn = new RecoveryAjaxResponse(
				"Has load [" + ipLoadData.getLoadID() + "] arrived at [" + describeNextLocation(ipLoadData) + "]?",
				new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_SEND_COMPLETION),
				new RecoveryAjaxResponse());
		RecoveryAjaxResponse vpCurrent = vpReturn.getNo();

		if (canRescheduleLoad(ipLoadData))
		{
			vpCurrent.setResponse(AjaxResponseCodes.PROMPT,
					"Is load [" + ipLoadData.getLoadID() + "] still at [" + describeLocation(ipLoadData) + "]?");
			vpCurrent.setYes(new RecoveryAjaxResponse(
					"Do you want to re-schedule the retrieval of load [" + ipLoadData.getLoadID() + "]?",
					new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_RESCHEDULE),
					new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED)));
			vpCurrent.setNo(new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));
		}
		else
		{
			vpCurrent.setResponse(AjaxResponseCodes.WARNING, NO_RECOVERY_ON_DEVICE);
		}
		return vpReturn;
	}

	/**
	 * Recovers a Moving, Move Sent, or Move Error load that is moving station to
	 * station.
	 *
	 * This is nearly identical to recoverStoringLoad(). Perhaps the two should be
	 * combined.
	 *
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverTransferMovingLoad(LoadData ipLoadData) throws DBException
	{
		StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
		StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);

		StationData vpStationData = vpStnServer.getStation(ipLoadData.getAddress());
		int vnLoadMoveStatus = ipLoadData.getLoadMoveStatus();
		LoadData vpTempLoadData = null;

		RecoveryAjaxResponse vpReturn = new RecoveryAjaxResponse();
		RecoveryAjaxResponse vpCurrent = vpReturn;

		if (vnLoadMoveStatus == DBConstants.MOVEERROR || vnLoadMoveStatus == DBConstants.MOVING)
		{
			// First Check for older Move Error loads...if there are any,
			// make them recover them first
			vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.MOVEERROR);
			if (vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(ipLoadData.getLoadID()))
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
						"There are [Move Error] loads...\nRecover them first.");
			}
			else
			{
				// Then Check for older Moving loads...if there are any,
				// make them recover them first
				vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.MOVING);
				if (vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(ipLoadData.getLoadID()))
				{
					return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING, "There are older [Moving] loads...\n"
							+ "Either allow them to complete or recover them first.");
				}
			}

			vpCurrent.setResponse(AjaxResponseCodes.PROMPT,
					"Has load [" + ipLoadData.getLoadID() + "] arrived at [" + describeNextLocation(ipLoadData) + "]");
			vpCurrent.setYes(new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_SEND_COMPLETION));
			vpCurrent.setNo(new RecoveryAjaxResponse());
			vpCurrent = vpCurrent.getNo();
		}
		else if (vnLoadMoveStatus == DBConstants.MOVESENT)
		{
			// First Check for Move Error loads...if there are any,
			// make them recover them first
			vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.MOVEERROR);
			if (vpTempLoadData != null)
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
						"There are [Move Error] loads...\nRecover them first.");
			}

			// Then Check for Moving loads...if there are any, make them recover
			// them first (or wait till they are done moving)
			vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.MOVING);
			if (vpTempLoadData != null)
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING, "There are [Moving] loads...\n"
						+ "Either allow them to complete moving or recover them first.");
			}
			else
			{
				// Then Check for OLDER Move sent loads...if there are any,
				// make them recover them first
				vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.MOVESENT);
				if (vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(ipLoadData.getLoadID()))
				{
					return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
							"There are older [Move Sent] loads...\nRecover them first.");
				}
			}
		}

		if (canRescheduleLoad(ipLoadData))
		{
			vpCurrent.setResponse(AjaxResponseCodes.PROMPT, "Do you want to reschedule the movement?");
			vpCurrent.setYes(new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_RESCHEDULE));
			vpCurrent.setNo(new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));
		}
		else
		{
			vpCurrent.setResponse(AjaxResponseCodes.WARNING, NO_RECOVERY_ON_DEVICE);
		}

		return vpReturn;
	}

	/**
	 * Recover Retrieving, Retrieve Sent, and Retrieve Error loads
	 *
	 * @param ipLoadData
	 * @param isLoadStatus
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverRetrievingLoad(LoadData ipLoadData, String isLoadStatus) throws DBException
	{
		StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
		StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);

		// Special handling for a load on a device
		LocationData vpLocData = Factory.create(Location.class).getLocation(ipLoadData.getWarehouse(), ipLoadData.getAddress());
		if (vpLocData.getLocationType() == DBConstants.LCDEVICE)
		{
			return recoverLoadOnDevice(ipLoadData);
		}

		/*
		 * If the station is null, this is hopefully a location-location move.
		 */
		StationData vpStationData = vpStnServer.getStation(ipLoadData.getNextAddress());
		if (vpStationData == null)
		{
			return recoverLocationToLocationLoad(ipLoadData);
		}

		/*
		 * If we get here, then this should be a regular retrieve
		 */
		RecoveryAjaxResponse vpReturn = new RecoveryAjaxResponse();
		RecoveryAjaxResponse vpCurrent = vpReturn;

		int vnLoadMoveStatus = ipLoadData.getLoadMoveStatus();
		LoadData vpTempLoadData = null;

		if (vnLoadMoveStatus == DBConstants.RETRIEVEERROR || vnLoadMoveStatus == DBConstants.RETRIEVING)
		{
			// First Check for older retrieve error loads. If there are any,
			// make them recover them first (or wait till they are done retrieving)
			vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.RETRIEVEERROR);
			if (vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(ipLoadData.getLoadID()))
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
						"There are [Retrieve Error] loads...\nRecover them first.");
			}
			else
			{
				// Then Check for OLDER retrieving loads. If there are any,
				// make them recover them first.
				vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.RETRIEVING);
				if (vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(ipLoadData.getLoadID()))
				{
					return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING, "There are older [Retrieving] loads...\n"
							+ "Either allow them to complete or recover them first.");
				}
			}

			vpCurrent.setResponse(AjaxResponseCodes.PROMPT,
					String.format("Has load [%s] been retrieved from [%s] to [%s]", ipLoadData.getLoadID(),
							describeLocation(ipLoadData), describeNextLocation(ipLoadData)));
			vpCurrent.setYes(new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_SEND_COMPLETION));
			vpCurrent.setNo(new RecoveryAjaxResponse());
			vpCurrent = vpCurrent.getNo();
		}
		else if (vnLoadMoveStatus == DBConstants.RETRIEVESENT)
		{
			// First Check for retrieve error loads...if there are any,
			// make them recover them first
			vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.RETRIEVEERROR);
			if (vpTempLoadData != null)
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
						"There are [Retrieve Error] loads...\nRecover them first.");
			}

			// Then Check for retrieving loads...if there are any, make them recover
			// them first (or wait till they are done retrieving)
			vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.RETRIEVING);
			if (vpTempLoadData != null)
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
						"There are [Retrieving] loads...\n" + "Either allow them to complete or recover them first");
			}
			else
			{
				// Then Check for OLDER retrieve sent loads...if there are any,
				// make them recover them first
				vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(),
						DBConstants.RETRIEVESENT);
				if (vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(ipLoadData.getLoadID()))
				{
					return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
							"There are older [Retrieve Sent] loads...\nRecover them first.");
				}
			}
		}

		if (canRescheduleLoad(ipLoadData))
		{
			vpCurrent.setResponse(AjaxResponseCodes.PROMPT, "Do you want to reschedule the retrieval?");
			vpCurrent.setYes(new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_RESCHEDULE));
			vpCurrent.setNo(new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));
		}
		else
		{
			vpCurrent.setResponse(AjaxResponseCodes.WARNING, NO_RECOVERY_ON_DEVICE);
		}

		return vpReturn;
	}


	/**
	 * Recover Storing, Store Sent, and Store Error loads
	 *
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverStoringLoad(LoadData ipLoadData, String isLoadStatus) throws DBException
	{
		StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
		StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);

		// Special handling for a load on a device
		LocationData vpLocData = Factory.create(Location.class).getLocation(ipLoadData.getWarehouse(), ipLoadData.getAddress());
		if (vpLocData.getLocationType() == DBConstants.LCDEVICE)
		{
			return recoverLoadOnDevice(ipLoadData);
		}

		// Storing in the rack?
		if (vpLocData.getLocationType() == DBConstants.LCASRS)
		{
			return recoverBadStoringLoad(ipLoadData);
		}

		RecoveryAjaxResponse vpReturn = new RecoveryAjaxResponse();
		RecoveryAjaxResponse vpCurrent = vpReturn;

		String vsLoadId = ipLoadData.getLoadID();
		int vnLoadMoveStatus = ipLoadData.getLoadMoveStatus();
		LoadData vpTempLoadData = null;

		StationData vpStationData = vpStnServer.getStation(ipLoadData.getAddress());
		if (vnLoadMoveStatus == DBConstants.STOREERROR || vnLoadMoveStatus == DBConstants.STORING)
		{
			//
			// First Check for older Store error loads...if there are any,
			// make them recover them first
			//
			vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.STOREERROR);
			if (vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(vsLoadId))
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
						"There are [Store Error] loads...\nRecover them first.");
			}
			else
			{
				// Then Check for OLDER storing loads...if there are any,
				// make them recover them first
				vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.STORING);
				if (vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(vsLoadId))
				{
					return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING, "There are older [Storing] loads...\n"
							+ "Either allow them to complete or recover them first");
				}
			}

			vpCurrent.setResponse(AjaxResponseCodes.PROMPT,
					"Has load [" + ipLoadData.getLoadID() + "] arrived at [" + describeNextLocation(ipLoadData) + "]");
			vpCurrent.setYes(new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_SEND_COMPLETION));
			vpCurrent.setNo(new RecoveryAjaxResponse());
			vpCurrent = vpCurrent.getNo();
		}
		else if (vnLoadMoveStatus == DBConstants.STORESENT)
		{
			// First check for Store Error loads...if there are any,
			// make them recover them first
			vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.STOREERROR);
			if (vpTempLoadData != null)
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
						"There are [Store Error] loads...\nRecover them first.");
			}

			// Then check for storing loads...if there are any, make them recover
			// them first (or wait till they are done storing)
			vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.STORING);
			if (vpTempLoadData != null)
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
						"There are [Storing] loads...\n" + "Either allow them to complete or recover them first");
			}
			else
			{
				// Then Check for OLDER Store sent loads...if there are any,
				// make them recover them first
				vpTempLoadData = vpLoadServer.getOldestLoadData(vpStationData.getStationName(), DBConstants.STORESENT);
				if (vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(vsLoadId))
				{
					return new RecoveryAjaxResponse(AjaxResponseCodes.WARNING,
							"There are older [Store Sent] loads...\nRecover them first");
				}
			}
		}

		if (canRescheduleLoad(ipLoadData))
		{
			vpCurrent.setResponse(AjaxResponseCodes.PROMPT, "Do you want to reschedule the storage?");
			vpCurrent.setYes(new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_RESCHEDULE));
			vpCurrent.setNo(new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));
		}
		else
		{
			vpCurrent.setResponse(AjaxResponseCodes.WARNING, NO_RECOVERY_ON_DEVICE);
		}

		return vpReturn;
	}

	/**
	 * Recover a load that claims to be "Storing" but is in the rack.
	 *
	 * @param ipLoadData
	 */
	protected RecoveryAjaxResponse recoverBadStoringLoad(LoadData ipLoadData) throws DBException
	{
		return new RecoveryAjaxResponse("This load should not be Storing.\nCancel the Store?",
				new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_CANCEL),
				new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));
	}

	/**
	 * Recover a load that is on a device
	 *
	 * @param ipLoadData
	 * @return
	 */
	protected RecoveryAjaxResponse recoverLoadOnDevice(LoadData ipLoadData)
	{
		// If the current location is a device, this is probably a WCS4 system and the
		// only option is to complete the movement.
		return new RecoveryAjaxResponse("Has load [" + ipLoadData.getLoadID() + "] arrived at [" + describeNextLocation(ipLoadData) + "]?",
				new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_SEND_COMPLETION),
				new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));
	}

	/**
	 * Recover a load that is moving from one rack location to another rack location
	 * (or at least not moving to a station).
	 *
	 * @param ipLoadData
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverLocationToLocationLoad(LoadData ipLoadData) throws DBException
	{
		String vsDialogText = String.format("Has load [%s] been retrieved from [%s] to [%s]?", ipLoadData.getLoadID(),
				describeLocation(ipLoadData), describeNextLocation(ipLoadData));

		RecoveryAjaxResponse vpYes = new RecoveryAjaxResponse("Do you want to complete the movement?",
				new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_SEND_COMPLETION),
				new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));

		RecoveryAjaxResponse vpNo;
		if (canRescheduleLoad(ipLoadData))
		{
			vpNo = new RecoveryAjaxResponse("Do you want to reschedule the movement?",
					new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_RESCHEDULE),
					new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));
		}
		else
		{
			vpNo = new RecoveryAjaxResponse(AjaxResponseCodes.WARNING, NO_RECOVERY_ON_DEVICE);
		}

		return new RecoveryAjaxResponse(vsDialogText, vpYes, vpNo);
	}

	/*==============================================================*/
	/* Move/Retrieve/Store Pending Loads							*/
	/*==============================================================*/

	/**
	 * Recover a Move Pending load that is stuck.
	 *
	 * @param ipLD - LoadData
	 * @param isLoadStatus - human readable status for return message
	 * @return
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverMovePendingLoad(LoadData ipLD, String isLoadStatus)
	{
		return new RecoveryAjaxResponse(AjaxResponseCodes.INFO,
				String.format(NO_RECOVERY_PENDING, ipLD.getLoadID(), isLoadStatus));
	}

	/**
	 * Recover a Retrieve Pending load that is stuck because its order/move was
	 * forcibly deleted.
	 *
	 * @param ipLD - LoadData
	 * @param isLoadStatus - human readable status for return message
	 * @return
	 * @throws DBException
	 */
	protected RecoveryAjaxResponse recoverRetrievePendingLoad(LoadData ipLD, String isLoadStatus) throws DBException
	{
		/*
		 * If there are no moves for this load, and the AGC/SRC isn't going to move it,
		 * then cancel the move.
		 */
		if (!hasMoves(ipLD.getLoadID()))
		{
			Factory.create(StandardLoadServer.class).setParentLoadMoveStatus(ipLD.getLoadID(), DBConstants.NOMOVE, "");
			ipLD.setLoadMoveStatus(DBConstants.NOMOVE);
			return new RecoveryAjaxResponse(AjaxResponseCodes.SUCCESS, "Movement canceled (no longer required).");
		}
		return new RecoveryAjaxResponse(AjaxResponseCodes.INFO,
				String.format(NO_RECOVERY_PENDING, ipLD.getLoadID(), isLoadStatus));
	}

	/**
	 * Recover a Store Pending load that is stuck because the mode change stuff on
	 * the SRC has some unfortunate behavior and reports Retrieve when it is really
	 * in a post- pick store state.
	 *
	 * @param ipLD         - LoadData
	 * @param isLoadStatus - human readable status for return message
	 * @return
	 */
	protected RecoveryAjaxResponse recoverStorePendingLoad(LoadData ipLD, String isLoadStatus)
	{
		StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);
		StationData vpSD = vpStnServer.getStation(ipLD.getAddress());
		if (vpSD == null)
		{
			// Should not ever happen
			logger.error("Load [{} has status [{}] at {}!", ipLD.getLoadID(), isLoadStatus, describeLocation(ipLD));
			Factory.create(StandardLoadServer.class).setParentLoadMoveStatus(ipLD.getLoadID(), DBConstants.NOMOVE, "");
			ipLD.setLoadMoveStatus(DBConstants.NOMOVE);
			return new RecoveryAjaxResponse(AjaxResponseCodes.SUCCESS, "Movement canceled (not possible).");
		}
		if (vpSD.getStationType() == DBConstants.REVERSIBLE && vpSD.getBidirectionalStatus() != DBConstants.STOREMODE)
		{
			return new RecoveryAjaxResponse("Force station " + vpSD.getStationName() + " into Store Mode?",
					new RecoveryAjaxResponse(AjaxResponseCodes.ALTPROMPT, ACTION_STORE_MODE),
					new RecoveryAjaxResponse(AjaxResponseCodes.INFO, NO_RECOVERY_PERFORMED));
		}
		return new RecoveryAjaxResponse(AjaxResponseCodes.INFO,
				String.format(NO_RECOVERY_PENDING, ipLD.getLoadID(), isLoadStatus));
	}

	/*==============================================================*/
	/* Recovery Methods												*/
	/*==============================================================*/

	/**
	 * Recovery -> Auto-pick
	 *
	 * @param isLoadId
	 * @param ipUser
	 * @return
	 */
	public AjaxResponse autoPick(String isLoadId, User ipUser)
	{
		try
		{
			checkUser(ipUser);

			LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadId);
			if (vpLoadData == null)
			{
				return new AjaxResponse(AjaxResponseCodes.FAILURE, "Load [" + isLoadId + "] not found!");
			}
			if (vpLoadData.getLoadMoveStatus() != DBConstants.ARRIVED)
			{
				String vsLoadStatus = DBTrans.getStringValueNoExc(LoadData.LOADMOVESTATUS_NAME,
						vpLoadData.getLoadMoveStatus());
				String vsMessage = "Unable to auto-pick. Load [" + isLoadId + "] now has status [" + vsLoadStatus
						+ "] at [" + describeLocation(vpLoadData) + "].";
				logger.warn(vsMessage);
				return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
			}

			Factory.create(StandardPickServer.class).autoPickLoadFromStation(isLoadId);

			String vsMessage = "Auto-picked load [" + isLoadId + "] at station [" + vpLoadData.getAddress() + "]";
			logger.warn("User [{}]: {}", ipUser.getUserId(), vsMessage);
			logger.warn(vsMessage);
			return new AjaxResponse(AjaxResponseCodes.SUCCESS, vsMessage);
		}
		catch (Exception e)
		{
			String vsMessage = "Unable to auto-pick load [" + isLoadId + "]";
			logger.error(vsMessage, e);
			return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
		}
	}

	/**
	 * Recovery -> Cancel
	 *
	 * @param isLoadId
	 * @param ipUser
	 * @return
	 */
	public AjaxResponse cancel(String isLoadId, User ipUser)
	{
		try
		{
			checkUser(ipUser);

			LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadId);
			if (vpLoadData == null)
			{
				return new AjaxResponse(AjaxResponseCodes.FAILURE, "Load [" + isLoadId + "] not found!");
			}
			if (vpLoadData.getLoadMoveStatus() != DBConstants.ARRIVEPENDING)
			{
				String vsLoadStatus = DBTrans.getStringValueNoExc(LoadData.LOADMOVESTATUS_NAME,
						vpLoadData.getLoadMoveStatus());
				String vsMessage = "Unable to send arrival. Load [" + isLoadId + "] now has status [" + vsLoadStatus + "].";
				logger.warn(vsMessage);
				return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
			}

			Factory.create(StandardLoadServer.class).setParentLoadMoveStatus(isLoadId, DBConstants.NOMOVE, "");

			String vsMessage = "Movement canceled for load [" + isLoadId + "]";
			logger.warn("User [{}]: {}", ipUser.getUserId(), vsMessage);
			return new AjaxResponse(AjaxResponseCodes.SUCCESS, vsMessage);
		}
		catch (Exception e)
		{
			String vsMessage = "Unable to cancel movement for load [" + isLoadId + "]";
			logger.error(vsMessage, e);
			return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
		}
	}

	/**
	 * Recovery -> Reschedule load movement
	 *
	 * @param isLoadId
	 * @param ipUser
	 * @return
	 */
	public AjaxResponse reschedule(String isLoadId, User ipUser)
	{
		try
		{
			AjaxResponse vpReturn = null;

			checkUser(ipUser);

			// Get the load
			LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadId);
			if (vpLoadData == null)
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.FAILURE, "Load [" + isLoadId + "] not found!");
			}
			String vsLoadStatus = DBTrans.getStringValueNoExc(LoadData.LOADMOVESTATUS_NAME,
					vpLoadData.getLoadMoveStatus());

			switch (vpLoadData.getLoadMoveStatus())
			{
			case DBConstants.MOVEERROR:
			case DBConstants.MOVING:
			case DBConstants.MOVESENT:
				rescheduleMoving(vpLoadData, ipUser);
				break;

			case DBConstants.RETRIEVEERROR:
			case DBConstants.RETRIEVING:
			case DBConstants.RETRIEVESENT:
				vpReturn = rescheduleRetrieving(vpLoadData, ipUser);
				break;

			case DBConstants.STOREERROR:
			case DBConstants.STORING:
			case DBConstants.STORESENT:
				rescheduleStoring(vpLoadData, ipUser);
				break;

			default:
				String vsMessage = "Unable to reschedule [" + vsLoadStatus + "] load [" + isLoadId + "]";
				logger.error(vsMessage);
				return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
			}

			if (vpReturn == null)
			{
				String vsMessage = String.format("Rescheduled [%s] load [%s] at [%s]",
						vsLoadStatus, isLoadId, describeLocation(vpLoadData));
				logger.warn("User [{}]: {}", ipUser.getUserId(), vsMessage);
				vpReturn = new AjaxResponse(AjaxResponseCodes.SUCCESS, vsMessage);
			}
			return vpReturn;
		}
		catch (Exception e)
		{
			String vsMessage = "Unable to reschedule load [" + isLoadId + "]";
			logger.error(vsMessage, e);
			return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
		}
	}

	/**
	 * Recovery -> Reschedule a Moving load
	 *
	 * @param ipLoadData
	 * @param ipUser
	 * @throws Exception
	 */
	protected void rescheduleMoving(LoadData ipLoadData, User ipUser) throws Exception
	{
		StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
		StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);

		StationData vpNextStnData = vpStnServer.getStation(ipLoadData.getNextAddress());
		if (vpNextStnData == null)
		{
			// Moving for location-location transfer
			rescheduleRetrieving(ipLoadData, ipUser);
		}
		else
		{
			StationData vpCurrStnData = vpStnServer.getStation(ipLoadData.getAddress());
			if (vpCurrStnData == null || ipLoadData.getAddress().equals(ipLoadData.getNextAddress()))
			{
				// Moving for retrieve

				// find out who is scheduling this station
				if (vpCurrStnData == null)
				{
					vpCurrStnData = vpStnServer.getControllingStationFromLocation(ipLoadData.getWarehouse(),
							ipLoadData.getAddress());
				}
				String scheduler = vpStnServer.getStationsScheduler(vpCurrStnData.getStationName());

				// reset the load move status
				vpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(), DBConstants.RETRIEVEPENDING,
						getRecoveryNote(ipUser));

				// send the scheduler event message
				String cmdstr = getLoadEvent().processArrivalReport(AGCDeviceConstants.AGCDUMMYLOAD,
						vpCurrStnData.getStationName(), vpCurrStnData.getHeight(), 1, ipLoadData.getLoadID(), "");
				publishSchedulerEvent(cmdstr, 0, scheduler);
			}
			else
			{
				// Moving for station-station transfer

				// find out who is scheduling this station
				String scheduler = vpStnServer.getStationsScheduler(vpCurrStnData.getStationName());

				// reset the load move status
				vpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(), DBConstants.MOVEPENDING,
						getRecoveryNote(ipUser));

				// send the scheduler event message to wake up the scheduler
				String cmdstr = getLoadEvent().moveLoadStationStation(ipLoadData.getLoadID(),
						vpCurrStnData.getStationName(), vpCurrStnData.getStationName(), null, ipLoadData.getLoadID(),
						"", ipLoadData.getHeight());
				publishLoadEvent(cmdstr, 0, scheduler);
			}
		}
	}

	/**
	 * Recovery -> Reschedule a Retrieving load
	 *
	 * @param ipLoadData
	 * @param ipUser
	 * @return AjaxResponse if the movement was canceled instead of rescheduled, null otherwise
	 * @throws Exception
	 */
	protected AjaxResponse rescheduleRetrieving(LoadData ipLoadData, User ipUser) throws Exception
	{
		StationData vpNextStnData = Factory.create(StandardStationServer.class).getStation(ipLoadData.getNextAddress());
		if (vpNextStnData == null)
		{
			// location-location
			rescheduleLocToLocRetrieve(ipLoadData, ipUser);
			return null;
		}
		else
		{
			// location-station
			return rescheduleLocToStnRetrieve(ipLoadData, vpNextStnData, ipUser);
		}
	}

	/**
	 * Set a load back to retrieve pending and wake up the load's scheduler for
	 * location-to-location moves
	 *
	 * @param ipLoadData
	 * @param ipUser
	 * @throws Exception
	 */
	protected void rescheduleLocToLocRetrieve(LoadData ipLoadData, User ipUser) throws Exception
	{
		// find out who is scheduling this load
		String vsScheduler = Factory.create(StandardDeviceServer.class).getSchedulerName(ipLoadData.getDeviceID());

		// reset the load move status
		Factory.create(StandardLoadServer.class).setParentLoadMoveStatus(ipLoadData.getLoadID(),
				DBConstants.RETRIEVEPENDING, getRecoveryNote(ipUser));

		// send the scheduler event message
		LoadEventDataFormat vpSwapMessage = Factory.create(LoadEventDataFormat.class, vsScheduler);
		String vsCommand = vpSwapMessage.moveLoadLocationLocation(ipLoadData.getParentLoadID(), ipLoadData.getAddress(),
				ipLoadData.getShelfPosition(), ipLoadData.getWarehouse(), ipLoadData.getNextAddress(),
				ipLoadData.getNextShelfPosition(), ipLoadData.getWarehouse(), ipLoadData.getHeight());

		publishLoadEvent(vsCommand, 0, ipLoadData.getDeviceID());
	}

	/**
	 * Set a load back to retrieve pending and wake up the load's scheduler
	 *
	 * @param ipLoadData
	 * @param vpStationData
	 * @param ipUser
	 * @return AjaxResponse if the movement was canceled instead of rescheduled,
	 *         null otherwise
	 * @throws Exception
	 */
	protected AjaxResponse rescheduleLocToStnRetrieve(LoadData ipLoadData, StationData vpStationData, User ipUser)
			throws Exception
	{
		if (hasMoves(ipLoadData.getLoadID()))
		{
			// find out who is scheduling this station
			String scheduler = Factory.create(StandardStationServer.class)
					.getStationsScheduler(vpStationData.getStationName());

			// reset the load move status
			Factory.create(StandardLoadServer.class).setParentLoadMoveStatus(ipLoadData.getLoadID(),
					DBConstants.RETRIEVEPENDING, getRecoveryNote(ipUser));

			// send the scheduler event message
			AllocationMessageDataFormat vpAllocData = Factory.create(AllocationMessageDataFormat.class);
			vpAllocData.setOutBoundLoad(ipLoadData.getLoadID());
			vpAllocData.setFromWarehouse(ipLoadData.getWarehouse());
			vpAllocData.setFromAddress(ipLoadData.getAddress());
			vpAllocData.setOutputStation(ipLoadData.getNextAddress());
			vpAllocData.createDataString();
			String cmdstr = vpAllocData.createStringToSend();
			publishSchedulerEvent(cmdstr, 0, scheduler);
			return null;
		}
		else
		{
			String vsLoadStatus = DBTrans.getStringValueNoExc(LoadData.LOADMOVESTATUS_NAME,
					ipLoadData.getLoadMoveStatus());
			return recoverRetrievePendingLoad(ipLoadData, vsLoadStatus);
		}
	}

	/**
	 * Recovery -> Reschedule a Storing load
	 *
	 * @param ipLoadData
	 * @param ipUser
	 * @throws Exception
	 */
	protected void rescheduleStoring(LoadData ipLoadData, User ipUser) throws Exception
	{
		String vsStation = ipLoadData.getAddress();

		// find out who is scheduling this station
		String scheduler = Factory.create(StandardStationServer.class).getStationsScheduler(vsStation);

		// reset the load move status
		Factory.create(StandardLoadServer.class).setParentLoadMoveStatus(ipLoadData.getLoadID(),
				DBConstants.STOREPENDING, getRecoveryNote(ipUser));

		// send the scheduler event message to wake up the scheduler
		String cmdstr = getLoadEvent().moveLoadStationStation(ipLoadData.getLoadID(), vsStation, vsStation, null,
				ipLoadData.getLoadID(), "", ipLoadData.getHeight());
		publishLoadEvent(cmdstr, 0, scheduler);
	}

	/**
	 * Recovery -> Resend an arrival
	 *
	 * @param isLoadId
	 * @param ipHeight
	 * @param ipUser
	 * @return
	 */
	public AjaxResponse sendArrival(String isLoadId, Integer ipHeight, User ipUser)
	{
		try
		{
			checkUser(ipUser);

			LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadId);
			if (vpLoadData == null)
			{
				return new AjaxResponse(AjaxResponseCodes.FAILURE, "Load [" + isLoadId + "] not found!");
			}
			if (vpLoadData.getLoadMoveStatus() != DBConstants.ARRIVEPENDING)
			{
				String vsLoadStatus = DBTrans.getStringValueNoExc(LoadData.LOADMOVESTATUS_NAME,
						vpLoadData.getLoadMoveStatus());
				String vsMessage = "Unable to send arrival. Load [" + isLoadId + "] now has status [" + vsLoadStatus + "].";
				logger.warn(vsMessage);
				return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
			}

			if (ipHeight == null)
			{
				ipHeight = vpLoadData.getHeight();
			}

			// find out who is scheduling this station
			String scheduler = Factory.create(StandardStationServer.class)
					.getStationsScheduler(vpLoadData.getAddress());

			// send the scheduler event message
			String cmdstr = getLoadEvent().processArrivalReport(
					AGCDeviceConstants.AGCDUMMYLOAD, vpLoadData.getAddress(), ipHeight, 1, isLoadId, "");
			publishLoadEvent(cmdstr, 0, scheduler);

			String vsMessage = "Resent arrival for load [" + isLoadId + "] at station [" + vpLoadData.getAddress()
					+ "]";
			logger.warn("User [{}]: {}", ipUser.getUserId(), vsMessage);
			return new AjaxResponse(AjaxResponseCodes.SUCCESS, vsMessage);
		}
		catch (Exception e)
		{
			String vsMessage = "Unable to send arrival for load [" + isLoadId + "]";
			logger.error(vsMessage, e);
			return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
		}
	}

	/**
	 * Recovery -> send completion
	 *
	 * @param isLoadId
	 * @param ipUser
	 * @return
	 */
	public AjaxResponse sendCompletion(String isLoadId, User ipUser)
	{
		try
		{
			checkUser(ipUser);

			// Get the load
			LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadId);
			if (vpLoadData == null)
			{
				return new RecoveryAjaxResponse(AjaxResponseCodes.FAILURE, "Load [" + isLoadId + "] not found!");
			}
			String vsLoadStatus = DBTrans.getStringValueNoExc(LoadData.LOADMOVESTATUS_NAME,
					vpLoadData.getLoadMoveStatus());

			switch (vpLoadData.getLoadMoveStatus())
			{
			case DBConstants.MOVEERROR:
			case DBConstants.MOVING:
			case DBConstants.MOVESENT:
				sendCompletionMoving(vpLoadData, ipUser);
				break;

			case DBConstants.RETRIEVEERROR:
			case DBConstants.RETRIEVING:
			case DBConstants.RETRIEVESENT:
				sendCompletionRetrieving(vpLoadData, ipUser);
				break;

			case DBConstants.STOREERROR:
			case DBConstants.STORING:
			case DBConstants.STORESENT:
				sendCompletionStoring(vpLoadData, ipUser);
				break;

			default:
				String vsMessage = "Unable to complete [" + vsLoadStatus + "] load [" + isLoadId + "]";
				logger.error(vsMessage);
				return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
			}

			String vsMessage = "Re-sent completion message for [" + vsLoadStatus + "] load [" + isLoadId + "]";
			logger.warn("User [{}]: {}", ipUser.getUserId(), vsMessage);
			return new AjaxResponse(AjaxResponseCodes.SUCCESS, vsMessage);
		}
		catch (Exception e)
		{
			String vsMessage = "Unable to complete load [" + isLoadId + "]";
			logger.error(vsMessage, e);
			return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
		}
	}

	/**
	 * Recovery -> send completion for a Moving load
	 *
	 * @param ipLoadData
	 * @param ipUser
	 * @return
	 */
	protected void sendCompletionMoving(LoadData ipLoadData, User ipUser) throws Exception
	{
		StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);

		StationData vpNextStnData = vpStnServer.getStation(ipLoadData.getNextAddress());
		if (vpNextStnData == null)
		{
			// Moving for location-location transfer
			sendCompletionRetrieving(ipLoadData, ipUser);
		}
		else
		{
			StationData vpCurrStnData = vpStnServer.getStation(ipLoadData.getAddress());
			DeviceData vpCurrDevData = Factory.create(StandardDeviceServer.class).getDeviceData(ipLoadData.getAddress());
			if ((vpCurrStnData == null && vpCurrDevData == null) || ipLoadData.getAddress().equals(ipLoadData.getNextAddress()))
			{
				// Moving for retrieve

				// find out who is scheduling this station
				StationData vpStationData = vpCurrStnData;
				if (vpStationData == null)
				{
					vpStationData = vpNextStnData;
				}
				String scheduler = vpStnServer.getStationsScheduler(vpStationData.getStationName());

				// send required completion message
				if (vpStationData.getArrivalRequired() == DBConstants.YES)
				{
					// If an arrival is required, send the arrival
					String cmdstr2 = getLoadEvent().processArrivalReport(ipLoadData.getLoadID(),
							vpStationData.getStationName(), vpStationData.getHeight(), 1, ipLoadData.getBCRData(), "");
					publishLoadEvent(cmdstr2, 0, scheduler);
				}
				else
				{
					// If an arrival is not required, send the work complete
					String cmdstr2 = getLoadEvent().processOperationCompletion(ipLoadData.getLoadID(), 0, // Normal
							2, // Retrieval
							"", vpStationData.getStationName(), ipLoadData.getAddress(), ipLoadData.getShelfPosition(),
							"", "", ipLoadData.getHeight(), ipLoadData.getBCRData(), "", "");
					publishLoadEvent(cmdstr2, 0, scheduler);
				}
			}
			else
			{
				// Moving for station-station transfer

				// find out who is scheduling this station
				String scheduler = vpStnServer.getStationsScheduler(vpNextStnData.getStationName());

				// send required completion messages
				String cmdstr;
				if (vpNextStnData.getArrivalRequired() == DBConstants.YES)
				{
					cmdstr = getLoadEvent().processArrivalReport(ipLoadData.getLoadID(), ipLoadData.getNextAddress(),
							ipLoadData.getHeight(), 0, ipLoadData.getLoadID(), "");
				}
				else
				{
					cmdstr = getLoadEvent().processOperationCompletion(ipLoadData.getLoadID(), 1, 0,
							ipLoadData.getAddress(), ipLoadData.getNextAddress(), ipLoadData.getNextAddress(),
							ipLoadData.getShelfPosition(), "000000000", ipLoadData.getNextShelfPosition(), 0, "", "",
							"");
				}
				// send the load event message
				publishLoadEvent(cmdstr, 0, scheduler);
			}
		}
	}

	/**
	 * Recovery -> send completion for a Retrieving load
	 *
	 * @param ipLoadData
	 * @param ipUser
	 * @return
	 */
	protected void sendCompletionRetrieving(LoadData ipLoadData, User ipUser) throws Exception
	{
		StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);
		StationData vpStationData = vpStnServer.getStation(ipLoadData.getNextAddress());

		// find out who is scheduling this station
		String scheduler = vpStnServer.getStationsScheduler(vpStationData.getStationName());

		// send work complete to everyone
		String cmdstr = getLoadEvent().processOperationCompletion(ipLoadData.getLoadID(), 2, 0,
				AGCDeviceConstants.RACKSTATION, vpStationData.getStationName(), ipLoadData.getAddress(),
				ipLoadData.getShelfPosition(), "", ipLoadData.getNextShelfPosition(), ipLoadData.getHeight(), "", "",
				"");

		publishLoadEvent(cmdstr, 0, scheduler);

		if (vpStationData.getArrivalRequired() == DBConstants.YES)
		{ // Send arrival only to stations that require it
			String cmdstr2 = getLoadEvent().processArrivalReport(ipLoadData.getLoadID(), vpStationData.getStationName(),
					vpStationData.getHeight(), 1, ipLoadData.getBCRData(), "");
			publishLoadEvent(cmdstr2, 0, scheduler);
		}
	}

	/**
	 * Recovery -> send completion for a Storing load
	 *
	 * @param ipLoadData
	 * @param ipUser
	 * @return
	 */
	protected void sendCompletionStoring(LoadData ipLoadData, User ipUser) throws Exception
	{
		// find out who is scheduling this station
		String vsScheduler = Factory.create(StandardStationServer.class).getStationsScheduler(ipLoadData.getAddress());
		if (SKDCUtility.isBlank(vsScheduler))
			vsScheduler = Factory.create(StandardDeviceServer.class).getSchedulerName(ipLoadData.getDeviceID());

		// send required completion messages
		String cmdstr = getLoadEvent().processOperationCompletion(ipLoadData.getLoadID(), 1, 0,
				ipLoadData.getAddress(), AGCDeviceConstants.RACKSTATION, ipLoadData.getNextAddress(),
				ipLoadData.getNextShelfPosition(), "000000000", "000", 0, "", "", "");

		// send the load event message
		publishLoadEvent(cmdstr, 0, vsScheduler);
	}

	/**
	 * Recovery (Store Pending) -> Force Store Mode
	 *
	 * @param isLoadId
	 * @param ipUser
	 * @return
	 */
	public AjaxResponse storeMode(String isLoadId, User ipUser)
	{
		try
		{
			checkUser(ipUser);

			LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadId);
			if (vpLoadData == null)
			{
				return new AjaxResponse(AjaxResponseCodes.FAILURE, "Load [" + isLoadId + "] not found!");
			}
			if (vpLoadData.getLoadMoveStatus() != DBConstants.STOREPENDING)
			{
				String vsLoadStatus = DBTrans.getStringValueNoExc(LoadData.LOADMOVESTATUS_NAME,
						vpLoadData.getLoadMoveStatus());
				String vsMessage = "Unable to change store mode. Load [" + isLoadId + "] now has status ["
						+ vsLoadStatus + "].";
				logger.warn(vsMessage);
				return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
			}

			StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);
			StationData vpSD = vpStnServer.getStation(vpLoadData.getAddress());
			if (vpSD == null)
			{
				return new AjaxResponse(AjaxResponseCodes.FAILURE, "Load [" + isLoadId + "] is not at a station!");
			}

			vpStnServer.setBidirectionalMode(vpSD.getStationName(), DBConstants.STOREMODE);

			String cmdstr = getLoadEvent().moveLoadStationStation(vpLoadData.getLoadID(), vpSD.getStationName(),
					vpSD.getStationName(), null, vpLoadData.getLoadID(), "", vpLoadData.getHeight());
			String vsScheduler = vpStnServer.getStationsScheduler(vpSD.getStationName());

			publishLoadEvent(cmdstr, 0, vsScheduler);

			String vsMessage = "Forced Store Mode for station [" + vpSD.getStationName() + "]";
			logger.warn("User [{}]: {}", ipUser.getUserId(), vsMessage);
			return new AjaxResponse(AjaxResponseCodes.SUCCESS, vsMessage);
		}
		catch (Exception e)
		{
			String vsMessage = "Unable to change the station's Store Mode for load [" + isLoadId + "]";
			logger.error(vsMessage, e);
			return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
		}
	}

	/*==============================================================*/
	/* Template														*/
	/*==============================================================*/

	/**
	 * Recovery -> xxx
	 *
	 * @param isLoadId
	 * @param ipUser
	 * @return
	 */
	public AjaxResponse xxx(String isLoadId, User ipUser)
	{
		try
		{
			checkUser(ipUser);

			LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadId);
			if (vpLoadData == null)
			{
				return new AjaxResponse(AjaxResponseCodes.FAILURE, "Load [" + isLoadId + "] not found!");
			}
//			if (vpLoadData.getLoadMoveStatus() != DBConstants.ARRIVEPENDING)
//			{
//				String vsLoadStatus = DBTrans.getStringValueNoExc(LoadData.LOADMOVESTATUS_NAME,
//						vpLoadData.getLoadMoveStatus());
//				String vsMessage = "Unable to send arrival. Load [" + isLoadId + "] now has status [" + vsLoadStatus + "].";
//				logger.warn(vsMessage);
//				return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
//			}

			String vsMessage = "XXX for load [" + isLoadId + "]";
			logger.warn("User [{}]: {}", ipUser.getUserId(), vsMessage);
			return new AjaxResponse(AjaxResponseCodes.SUCCESS, vsMessage);
		}
		catch (Exception e)
		{
			String vsMessage = "Unable to XXX for load [" + isLoadId + "]";
			logger.error(vsMessage, e);
			return new AjaxResponse(AjaxResponseCodes.FAILURE, vsMessage);
		}
	}

	/*==============================================================*/
	/* Helper														*/
	/*==============================================================*/
	/**
	 * Can this movement be rescheduled?
	 *
	 * @param ipLoadData
	 * @return
	 */
	protected boolean canRescheduleLoad(LoadData ipLoadData)
	{
		// Movement cannot be rescheduled if the current location is a device
		return Factory.create(StandardDeviceServer.class).getDeviceData(ipLoadData.getAddress()) == null;
	}

	/**
	 * Throw an exception if user data is obviously bad
	 * @param ipUser
	 */
	private void checkUser(User ipUser)
	{
		if (ipUser == null || SKDCUtility.isBlank(ipUser.getUserId()))
		{
			throw new IllegalArgumentException("User information is unavailable!");
		}
	}

	// TODO: find a better location for the describeLocation() methods

	/**
	 * Describe a location
	 *
	 * @param isWarehouse
	 * @param isAddress
	 * @param isPosition
	 * @return
	 */
	private String describeLocation(LoadData ipLoadData)
	{
		return describeLocation(ipLoadData.getWarehouse(), ipLoadData.getAddress(), ipLoadData.getShelfPosition());
	}

	/**
	 * Describe a location
	 *
	 * @param isWarehouse
	 * @param isAddress
	 * @param isPosition
	 * @return
	 */
	private String describeLocation(String isWarehouse, String isAddress, String isPosition)
	{
		String vsPosition = SKDCUtility.isBlank(isPosition) || isPosition.equals(LoadData.DEFAULT_POSITION_VALUE) ? ""
				: isPosition;
		return isWarehouse + "-" + isAddress + vsPosition;
	}

	/**
	 * Describe a location
	 *
	 * @param isWarehouse
	 * @param isAddress
	 * @param isPosition
	 * @return
	 */
	private String describeNextLocation(LoadData ipLoadData)
	{
		return describeLocation(ipLoadData.getNextWarehouse(), ipLoadData.getNextAddress(),
				ipLoadData.getNextShelfPosition());
	}

	/**
	 * Get a new LoadEventDataFormat
	 * @return
	 */
	private LoadEventDataFormat getLoadEvent()
	{
		return Factory.create(LoadEventDataFormat.class, "Recovery");
	}

	/**
	 *
	 * @param ipUser
	 * @return
	 */
	private String getRecoveryNote(User ipUser)
	{
		return "Recovered by " + ipUser.getUserId();
	}

	/**
	 * Does the load have moves?
	 *
	 * @param isLoadID
	 * @return
	 * @throws DBException
	 */
	private boolean hasMoves(String isLoadID) throws DBException
	{
		MoveData vpMoveData = Factory.create(StandardMoveServer.class).getNextMoveRecord(isLoadID);
		return vpMoveData != null;
	}

	/*==============================================================*/
	/* System Gateway												*/
	/*==============================================================*/

	/**
	 * Send a load event
	 *
	 * @param sEvent
	 * @param iEvent
	 * @param sCKN
	 */
	private void publishLoadEvent(String sEvent, int iEvent, String sCKN) throws Exception
	{
		logger.info(String.format("Send load event. Event Text=[%1$s], Event Int=[%2$d], Controller=[%3$s]", sEvent,
				iEvent, sCKN));

		SystemGateway sg = null;
		try
		{
			sg = SystemGateway.create(com.daifukuamerica.wrxj.log.Logger.getLogger());
			sg.publishLoadEvent(sEvent, iEvent, sCKN);
		}
		finally
		{
			if (sg != null)
				SystemGateway.destroy(sg);
		}
	}

	/**
	 * Send a scheduler event
	 *
	 * @param sEvent
	 * @param iEvent
	 * @param sCKN
	 */
	private void publishSchedulerEvent(String sEvent, int iEvent, String sCKN) throws Exception
	{
		logger.trace(String.format("Send load event. Event Text=[%1$s], Event Int=[%2$d], Controller=[%3$s]", sEvent,
				iEvent, sCKN));

		SystemGateway sg = null;
		try
		{
			sg = SystemGateway.create(com.daifukuamerica.wrxj.log.Logger.getLogger());
			sg.publishSchedulerEvent(sEvent, iEvent, sCKN);
		}
		finally
		{
			if (sg != null)
				SystemGateway.destroy(sg);
		}
	}
}
