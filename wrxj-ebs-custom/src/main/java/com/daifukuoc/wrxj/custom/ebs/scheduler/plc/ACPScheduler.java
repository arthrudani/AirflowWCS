package com.daifukuoc.wrxj.custom.ebs.scheduler.plc;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.scheduler.agc.AGCScheduler;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants.MoveOrder.MOVE_TYPE;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived.ItemArrivedContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCMoveOrderMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.queue.CommandQueueManagerImpl;


public class ACPScheduler extends AGCScheduler {

	private EBSTableJoin mEBSTableJoin = Factory.create(EBSTableJoin.class);

	public ACPScheduler(String name) {
		super(name);

	}

	@Override
	protected void processStationEvent(String receivedString) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processLoadEvent(String receivedString) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processSchedulerEvent(String receivedString) {

		//  Create a CMD and add to queue

		AllocationMessageDataFormat vpAMDF = Factory.create(AllocationMessageDataFormat.class, getSchedulerName());
		vpAMDF.decodeReceivedString(receivedString);
		logger.logDebug("LoadId \"" + vpAMDF.getOutBoundLoad() + "\" Retrieve Pending to Station "
				+ vpAMDF.getOutputStation() + " - " + getClass().getSimpleName() + ".processSchedulerEvent()");
		
		// Get the Order and Order line item
		LoadData vpLoadData = mpLoadServer.getLoad(vpAMDF.getOutBoundLoad());
		List<Map> vpIDList = null;
		try {

			vpIDList = mEBSTableJoin.getLoadLineItemsForThisLoad(vpAMDF.getOutBoundLoad());

		} catch (DBException e) {
			logger.logError(
					getClass().getSimpleName() + " Failed to get LoadLineItem data for:" + vpAMDF.getOutBoundLoad());
		}
		if (vpLoadData != null && vpIDList != null && vpIDList.size() > 0) {
			
			
			LoadLineItemData mpIDData = Factory.create(LoadLineItemData.class);

			CommandQueueManagerImpl cmdQueueManager = Factory.create(CommandQueueManagerImpl.class);

			for (Iterator<Map> vpIter = vpIDList.iterator(); vpIter.hasNext();) {
				mpIDData.dataToSKDCData(vpIter.next());				
				
				MoveCommandData moveCommandData = constructMoveCommandData(vpLoadData, mpIDData,String.valueOf(MOVE_TYPE.RETRIEVAL),
						vpAMDF.getOutputStation(), vpAMDF.getFromAddress(),vpAMDF.getOrderID());
		        cmdQueueManager.enqueue(moveCommandData);
							
			}


		} else {
			logger.logError(getClass().getSimpleName() + " Failed to get LOAD data for:" + vpAMDF.getOutBoundLoad());
		}

	}

	@Override
	public void startup() {
		super.startup();
		logger.logDebug(getClass().getSimpleName() + ".startup() - KR:Start " + getSchedulerName());
	}

	@Override
	public void shutdown() {
		super.shutdown();
	}

	/**
	 * Look for retrieve pending loads for a station and send messages to get those
	 * loads to retrieve. Also send a message to the allocator to check to see if we
	 * need more loads staged.
	 *
	 * @param ipSD
	 * @throws DBException
	 */
	protected void retrieveAndStageForStation(StationData ipSD) throws DBException {
	
	}

	/**
	 * See if there are any loads to schedule a store from this station
	 *
	 * @param isStationName the station to check
	 */
	protected void checkIfStationHasLoadToStore(String isStationName) {
	
	}

	/**
	 * load has just arrived at the output station Update the load and publish a
	 * message to the pick screen of load arrival.
	 *
	 * @param ipLEDF decoded LoadEventDataFormat message
	 */
	public void processFinalArrival(LoadEventDataFormat ipLEDF) {
	
	}

	/**
	 * Send a loadEvent to who is subscribing to my move Commands.
	 *
	 * @param moveCommand the command to be published
	 */
	protected void publishLoadEventMove(String moveCommand, String vsDevice) {
		transmitLoadEvent(moveCommand, vsDevice);
	}

	private MoveCommandData constructMoveCommandData(LoadData loadData, LoadLineItemData loadLineData, String moveType,
				String toStationId, String fromStationId,String OrderId) {
	    	MoveCommandData moveCommandData = Factory.create(MoveCommandData.class);
	        moveCommandData.setCreatedDate(new Date(System.currentTimeMillis()));
	        moveCommandData.setLoadID(loadData.getLoadID());
	        moveCommandData.setFinalSortLocationID(loadData.getFinalSortLocationID());
	        moveCommandData.setCmdStatus(DBConstants.CMD_READY);
	        moveCommandData.setGlobalID(loadLineData.getGlobalID());
	        moveCommandData.setOrderid(OrderId);	        
	        moveCommandData.setCmdMoveType(Integer.valueOf(moveType));
	        moveCommandData.setFrom(fromStationId);
	        moveCommandData.setToDest(toStationId);
	        moveCommandData.setCmdOrderType(DBConstants.CMD_RETRIEVAL);
	        moveCommandData.setItemID(loadLineData.getLineID());
	        moveCommandData.setFlightNum(loadLineData.getLot());
	        moveCommandData.setFlightSTD(loadLineData.getExpectedDate());
	        moveCommandData.setDeviceID(loadData.getDeviceID());
			return moveCommandData;

		}

}
