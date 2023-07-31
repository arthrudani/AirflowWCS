package com.daifukuoc.wrxj.custom.ebs.scheduler.plc;



import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;

import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.scheduler.agc.AGCScheduler;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ConveyorTableJoin;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCFlushRequestMessage;



public class ConveyorScheduler extends AGCScheduler {
	ConveyorTableJoin mpTableJoin = null;

	public ConveyorScheduler(String name) {
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

		//KR-> TODO: Create a CMD and add to queue
		AllocationMessageDataFormat vpAMDF = Factory.create(AllocationMessageDataFormat.class, getSchedulerName());
		vpAMDF.decodeReceivedString(receivedString);
		logger.logDebug("LoadId \"" + vpAMDF.getOutBoundLoad() + "\" Retrieve Pending to Station "
				+ vpAMDF.getOutputStation() + " - " + getClass().getSimpleName() + ".processSchedulerEvent()");

		/* KR: to be deleted
		 * This is the values assigned to Allocation Message in the ConveyorPieceAllocation  
		 *   newAllocData.setOutBoundLoad(mpOLData.getOrderLot()); // flight number  
 	         newAllocData.setFromWarehouse(vpLoadData.getDeviceID()); //device id
 	         newAllocData.setFromAddress(String.valueOf( itemPosition)); // position of the tray/bag on conveyor 
			 newAllocData.setOutputStation(vpLoadData.getAddress()); //land ID
			 newAllocData.setOrderID(mpOLData.getOrderID());
		 */
		
		//get the Conveyor which need to be flushed
		String outPutstation = vpAMDF.getOutputStation();
		if(outPutstation.isBlank()|| outPutstation.isEmpty())
		{
			logger.logError("processSchedulerEvent->Error - the outPutstation station (conveyorID) is null in receivedString:" + receivedString);
			return;
		}
		PLCFlushRequestMessage flushRequest = constructFlushRequest(vpAMDF);
		if( flushRequest != null)
		{
			flushRequest.sendMessageToPlc();
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
	
	
	private PLCFlushRequestMessage constructFlushRequest(AllocationMessageDataFormat vpAMDF)
	{
		PLCFlushRequestMessage moveOrder = Factory.create(PLCFlushRequestMessage.class);
       // moveOrder.setSerialNum("0"); // This will be allocated later in ACPport before sending out
        moveOrder.setOrderId(vpAMDF.getOrderID());
        moveOrder.setDeviceId(vpAMDF.getFromWarehouse());  //Device ID
        moveOrder.setLaneId(vpAMDF.getOutputStation()); // Land Id
        Integer qty  = calculateQty(vpAMDF.getFromWarehouse(),vpAMDF.getOutputStation(),vpAMDF.getFromAddress() );

        moveOrder.setQuantity(qty);
        moveOrder.setRequestType(PLCFlushRequestMessage.REQUEST_TYPE_PROCESS);
        return moveOrder;
	}
	private Integer calculateQty(String sDeviceID,String sLaneID, String sTrayPosition  )
	{
		Integer qty = 0;
		//get the max position at the moment for this lane
		inializeTableJoin();
		try {
			String sMaxPosition = mpTableJoin.getMaxPositionByDevice(sDeviceID, sLaneID);
			
			Integer MaxPos = ((sMaxPosition.isBlank() || sMaxPosition.isEmpty() ) ? 0 : Integer.parseInt(sMaxPosition));
			Integer iTrayPos = ((sTrayPosition.isBlank() || sTrayPosition.isEmpty() ) ? 0 : Integer.parseInt(sTrayPosition));
			//Formula to calculate the qty is  Qty = MaxPosition - ( TrayPosition - 1);
			qty = MaxPos - (iTrayPos -1 );

		} catch (DBException e) {
		
			this.logger.logError("calculateQty failed to get the MaxPosition " + e.getMessage());
		}
		
		return qty;
	}
	
	private void inializeTableJoin()
	{
		if(mpTableJoin == null )
		{
			mpTableJoin = Factory.create(ConveyorTableJoin.class);
		}
	}
}
