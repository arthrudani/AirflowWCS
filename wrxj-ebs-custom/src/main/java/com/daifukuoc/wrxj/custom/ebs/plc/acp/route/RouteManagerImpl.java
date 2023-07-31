package com.daifukuoc.wrxj.custom.ebs.plc.acp.route;

import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

public class RouteManagerImpl implements RouteManager{
	
	protected EBSTableJoin ebsTJ = Factory.create(EBSTableJoin.class);
	
	private String moveType = "";

	public String getMoveType() {
		return moveType;
	}

	public void setMoveType(String moveType) {
		this.moveType = moveType;
	}

	@Override
	public String findNextDestination(LoadData load, String currentAddress) throws RouteManagerFailureException, DBException{
		// Get Current address of the load.
		String nextAddress = null;
		if(Objects.isNull(load)) {
			throw new RouteManagerFailureException("Load data can not be null while finding next destination address");	
		}
		
		if(Objects.isNull(currentAddress) || currentAddress.equalsIgnoreCase("")) {
			throw new RouteManagerFailureException("Current address can not be null while finding next destination address");
		}
		
		EBSAddress currentEBSAddress = new EBSAddress(currentAddress);
		EBSAddress loadEBSAddress = new EBSAddress(load.getAddress());
		
		switch(load.getLoadMoveStatus()) {
		case DBConstants.ARRIVEPENDING:
		case DBConstants.STOREPENDING:
		case DBConstants.STORING:
			nextAddress = handleNextDestinationForInbound(load, currentEBSAddress, loadEBSAddress);
			break;
		case DBConstants.RETRIEVEPENDING:
		case DBConstants.RETRIEVING:
		case DBConstants.NOMOVE:
			nextAddress = handleNextDestinationForOutbound(load, currentEBSAddress, loadEBSAddress);
			break;
		case DBConstants.TRANSFERRING_OUT:
			nextAddress = handleNextDestinationForTransferringOut(load, currentEBSAddress);
			break;
		}
		
		return nextAddress;
	}
	
	private String handleNextDestinationForTransferringOut(LoadData load, EBSAddress currentEBSAddress) throws DBException {
		clearMoveType();
		EBSAddress nextAddress = new EBSAddress();
		switch (currentEBSAddress.getType()) {
		case RouteManagerConstants.LCSHUTTLE_PICKUP_STATION_TYPE:
		case RouteManagerConstants.LCSHUTTLE_BIDIRECTIONAL_STATION_TYPE:
			String searchAddress = RouteManagerConstants.LOCATION_TYPE_CONSTANT + currentEBSAddress.getBank() + RouteManagerConstants.LIFT_BAY_CONSTANT + RouteManagerConstants.TRANSFERRING_OUT_DEFAULT_LELVEL;
			nextAddress = new EBSAddress(ebsTJ.getDestinationIdFromRoute(searchAddress));
			// set move type is station to station
			setMoveType(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.DIRECT));
			break;
		case RouteManagerConstants.LCLIFTER_LAYER_PICKUP_STATION_TYPE:
			nextAddress = new EBSAddress(ebsTJ.getDestinationIdFromRoute(currentEBSAddress.getAddress()));
			// set move type is station to station
			setMoveType(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.DIRECT));
			break;
		case RouteManagerConstants.LCLIFTER_AISLE_PICKUP_STATION_TYPE:
		case RouteManagerConstants.LCLIFTER_AISLE_BIDIRECTIONAL_STATION_TYPE:
			Integer stationType = Integer.valueOf(currentEBSAddress.getType()) + 10;
			nextAddress = new EBSAddress(stationType.toString(), currentEBSAddress.getBank(),
					RouteManagerConstants.LCSHUTTLE_BAY, RouteManagerConstants.TRANSFERRING_OUT_DEFAULT_LELVEL);
			// set move type is station to station
			setMoveType(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.DIRECT));
			break;
		}
		return nextAddress.getAddress();
	}

	private String handleNextDestinationForOutbound(LoadData load, EBSAddress currentEBSAddress, EBSAddress loadEBSAddresss) throws DBException {
		clearMoveType();
		EBSAddress nextAddress = new EBSAddress();
		String destinationName = null;
		switch(currentEBSAddress.getType()) {
		case RouteManagerConstants.LCSTORAGE_LOCATION_TYPE:
			String searchStr = RouteManagerConstants.LOCATION_TYPE_CONSTANT + currentEBSAddress.getBank() + RouteManagerConstants.LIFT_BAY_CONSTANT + loadEBSAddresss.getLevel();
			destinationName = ebsTJ.getDestinationIdFromRoute(searchStr);
			nextAddress = new EBSAddress(destinationName);
			// set move type is location to station
			setMoveType(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.RETRIEVAL));
			break;
		case RouteManagerConstants.LCLIFTER_LAYER_PICKUP_STATION_TYPE:
			destinationName  = ebsTJ.getDestinationIdFromRoute(currentEBSAddress.getAddress());
			nextAddress = new EBSAddress(destinationName);
			// set move type is station to station
			setMoveType(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.DIRECT));
			break;	
		}
		
		return nextAddress.getAddress();
	}

	private String handleNextDestinationForInbound(LoadData load, EBSAddress currentEBSAddress,
			EBSAddress loadEBSAddresss) throws DBException {
		clearMoveType();
		EBSAddress nextAddress = new EBSAddress();
		switch (currentEBSAddress.getType()) {
		case RouteManagerConstants.LCSHUTTLE_PICKUP_STATION_TYPE:
		case RouteManagerConstants.LCSHUTTLE_BIDIRECTIONAL_STATION_TYPE:
			nextAddress = loadEBSAddresss;
			// set move type is station to location
			setMoveType(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.STORAGE));
			break;
		case RouteManagerConstants.LCLIFTER_AISLE_PICKUP_STATION_TYPE:
		case RouteManagerConstants.LCLIFTER_AISLE_BIDIRECTIONAL_STATION_TYPE:
			Integer stationType = Integer.valueOf(currentEBSAddress.getType()) + 10;
			nextAddress.setType(stationType.toString());
			nextAddress.setBank(currentEBSAddress.getBank());
			nextAddress.setBay(RouteManagerConstants.LCSHUTTLE_BAY);
			nextAddress.setLevel(loadEBSAddresss.getLevel());
			// set move type is station to station
			setMoveType(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.DIRECT));
			break;
		}
		return nextAddress.getAddress();
	}

	

	private void clearMoveType() {
		this.moveType = "";
	}
	
	
}
