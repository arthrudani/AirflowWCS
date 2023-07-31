package com.daifukuoc.wrxj.custom.ebs.plc.commandallocation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.ACPMoveCommandServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ACPTableJoin;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCMoveOrderMessage;

/**
 * Handles Shuttle rack CM allocation system
 * @author KR
 *
 */
public class ACPCommandAllocator extends StandardCommandAllocator {  

	private Integer iMaxCmd = 2 ;// Default TODO: load from configuration	

	protected ACPTableJoin mpTableJoin = null;
	
	public ACPCommandAllocator(String deviceID)
	{
		super( deviceID );
	}
	
	@Override
	public void allocate(String stationID) {
		initializeTableJoin();
		mpLogger.logDebug("ACPCommandAllocator.allocateAll is called for device: "+ sDeviceID + " and station:"+stationID );
		
	}

	@Override
	public void allocateAll() {
		
		initializeTableJoin();
		
		mpLogger.logDebug("ACPCommandAllocator.allocateAll is called for device: "+ sDeviceID );
		
		try {
			//start output process
			processOutput();
			
		} catch (DBException e) {
			mpLogger.logError( "Error loading data for sDevice: "+ sDeviceID + " - Er:"+e.getMessage() );
		}
		
		try {
			//start input process
			processInput();
			
		} catch (DBException e) {
			mpLogger.logError( "Error loading data for sDevice: "+ sDeviceID + " - Er:"+e.getMessage() );
		}
	}

	
	private PLCMoveOrderMessage constructMoveOrderData(MoveCommandData mvCmdData)
	{
		PLCMoveOrderMessage moveOrder = Factory.create(PLCMoveOrderMessage.class);
       // moveOrder.setSerialNum("0"); // This will be allocated later in ACPport before sending out
        moveOrder.setOrderId(mvCmdData.getOrderid());
        moveOrder.setLoadId(mvCmdData.getLoadID());
        moveOrder.setGlobalId(mvCmdData.getGlobalID());
        moveOrder.setLineId(mvCmdData.getItemID());
        moveOrder.setLot(mvCmdData.getFlightNum());
        moveOrder.setFlightSchduledDateTime( mvCmdData.getFlightSTD());
        moveOrder.setFinalSortLocation(mvCmdData.getFinalSortLocationID());
        moveOrder.setFromLocation(mvCmdData.getFrom());
        moveOrder.setToLocation(mvCmdData.getToDest());
        moveOrder.setDeviceId(mvCmdData.getDeviceID());
        moveOrder.setMoveType(String.valueOf(mvCmdData.getCmdMoveType()) );
        return moveOrder;
	}
	
	private void processInput() throws DBException
	{
		Integer iCmdReady = 0;
		Integer	iCmdWorking = 0;
		String sDestination = null;
		List<Map> commands = null;
		Integer iNumOfCmd = 1;
		
		//star by moving trays from stations types 12/14 to ASRS locations
		List<Map> cmdCounts =  mpTableJoin.getCMDCountForInputASRSLocations(sDeviceID );
		if(cmdCounts != null && cmdCounts.size() > 0 )
		{
			for(Map m: cmdCounts)
			{
				iCmdReady = (  m.get(CMD_READY) != null) ? (int) m.get(CMD_READY) : 0;
				iCmdWorking = ( m.get(CMD_WORKING) != null) ? (int) m.get(CMD_WORKING) : 0;
				sDestination = ( m.get(CMD_DEST_STATION) != null) ? (String) m.get(CMD_DEST_STATION) : null;
				mpLogger.logDebug("sDeviceID:"+ sDeviceID + " - OutputLocationtype20 CmdReady:"
						+ iCmdReady + " - CmdWorking:"+ iCmdWorking + " - sDestination:"+sDestination);
				
				//if there is a command to process && number of in-progress items zero as we only allocate one item to these station at the time
				//this allows the process to allocate input (storage) item the same time to move in
				if(iCmdReady > 0  && iCmdWorking == 0)
				{
					 iNumOfCmd = 1;
					 commands = mpTableJoin.getCMDFor(sDeviceID,iNumOfCmd,sDestination); 
					 processCommands(commands);	
				}
			}
		}
		
		
		//process stations type 12 and 14  (Shuttle pickup transfer stations) 
		//this allows the items move in as soon as possible and clear the transfer stations
		//get count ...
		cmdCounts =  mpTableJoin.getCMDCountForInputTransferStations(sDeviceID );
		if(cmdCounts != null && cmdCounts.size() > 0 )
		{
			for(Map m: cmdCounts)
			{
				iCmdReady = (  m.get(CMD_READY) != null) ? (int) m.get(CMD_READY) : 0;
				iCmdWorking = ( m.get(CMD_WORKING) != null) ? (int) m.get(CMD_WORKING) : 0;
				sDestination = ( m.get(CMD_DEST_STATION) != null) ? (String) m.get(CMD_DEST_STATION) : null;
				mpLogger.logDebug("sDeviceID:"+ sDeviceID + " - OutputstationType12/14 CmdReady:"
						+ iCmdReady + " - CmdWorking:"+ iCmdWorking + " - sDestination:"+sDestination);
				if(iCmdReady > 0  && iCmdWorking < iMaxCmd)
				{
					iNumOfCmd = iMaxCmd - iCmdWorking;
					//Get the commands for  
					commands = mpTableJoin.getCMDFor(sDeviceID,iNumOfCmd,sDestination); 
					processCommands(commands);	
					
				}
			}
		}
		
		
	}
	private void processOutput() throws DBException
	{
		Integer iCmdReady = 0;
		Integer	iCmdWorking = 0;
		String sDestination = null;
		List<Map> commands = null;
		Integer iNumOfCmd = 1;
		//start with 03 types stations (out-bound) 
		//get count ...
		List<Map> cmdCounts =  mpTableJoin.getCMDCountForOutboundStations(sDeviceID);
		if(cmdCounts != null && cmdCounts.size() > 0 )
		{
			//we usually have only one output (out-bound station type =03) however we might get more in the future...
			for(Map m: cmdCounts)
			{		
				iCmdReady = (  m.get(CMD_READY) != null) ? (int) m.get(CMD_READY) : 0;
				iCmdWorking = ( m.get(CMD_WORKING) != null) ? (int) m.get(CMD_WORKING) : 0;
				sDestination = ( m.get(CMD_DEST_STATION) != null) ? (String) m.get(CMD_DEST_STATION) : null;
				
				mpLogger.logDebug("sDeviceID:"+ sDeviceID + " - OutputstationType03 CmdReady:"
						+ iCmdReady + " - CmdWorking:"+ iCmdWorking + " - sDestination:"+sDestination);
				//if there is a command to process && numb of in-progress items are less than MaxCmd 
				if(iCmdReady > 0  && iCmdWorking < iMaxCmd)
				{
					iNumOfCmd = iMaxCmd - iCmdWorking;
					//Get the commands with status= ready for this destination 
					commands = mpTableJoin.getCMDFor(sDeviceID,iNumOfCmd,sDestination); 
					processCommands(commands);	
					
				}
			}
		}
		
		//Doing the same for 05/07 Lift pickup transfer out stations
		cmdCounts =  mpTableJoin.getCMDCountForLiftPickupTransferStations(sDeviceID );
		if(cmdCounts != null && cmdCounts.size() > 0 )
		{
			for(Map m: cmdCounts)
			{
				iCmdReady = (  m.get(CMD_READY) != null) ? (int) m.get(CMD_READY) : 0;
				iCmdWorking = ( m.get(CMD_WORKING) != null) ? (int) m.get(CMD_WORKING) : 0;
				sDestination = ( m.get(CMD_DEST_STATION) != null) ? (String) m.get(CMD_DEST_STATION) : null;
				mpLogger.logDebug("sDeviceID:"+ sDeviceID + " - OutputstationType05/07 CmdReady:"
						+ iCmdReady + " - CmdWorking:"+ iCmdWorking + " - sDestination:"+sDestination);
				
				//if there is a command to process && number of in-progress items zero as we only allocate one item to these station at the time
				//this allows the process to allocate input (storage) item the same time to move in
				if(iCmdReady > 0  && iCmdWorking == 0)
				{
					 iNumOfCmd = 1;
					//Get the commands with status= ready for this destination 
					 commands = mpTableJoin.getCMDFor(sDeviceID,iNumOfCmd,sDestination); 
					 processCommands(commands);	
				}
			}
		}
	}
	private void processCommands(List<Map> commands)
	{
		if(commands != null && commands.size() > 0)
		{
			List<MoveCommandData> moveCommads = commands.stream().map(row -> {
				MoveCommandData liToReturn = Factory.create(MoveCommandData.class);
	            liToReturn.dataToSKDCData(row);
	            return liToReturn;
	        }).collect(Collectors.toList());
			
			if(moveCommads != null && moveCommads.size() > 0 )
			{
				for(MoveCommandData mv:  moveCommads)
				{
					PLCMoveOrderMessage moveOrder = constructMoveOrderData(mv);
					moveOrder.sendMessageToPlc();
					//update status
					ACPMoveCommandServer mvServer = Factory.create(ACPMoveCommandServer.class);
					mvServer.updateMoveCmdStatusById(mv.getDeviceID(), mv.getLoadID(),mv.getFrom(),mv.getToDest(), DBConstants.CMD_COMMANDED);
				}
			}
		}
	}
	protected void initializeTableJoin() {
        if (mpTableJoin == null) {
            mpTableJoin = Factory.create(ACPTableJoin.class);
        }
    }

}
