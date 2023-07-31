package com.daifukuamerica.wrxj.dbadapter.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class MoveCommand extends BaseDBInterface {

	protected MoveCommandData mpMoveCommandData;

	public MoveCommand() {
		super("MoveCommand");
		mpMoveCommandData = Factory.create(MoveCommandData.class);
	}

	/**
	 * Add a move command
	 *
	 * @param MoveCommandData
	 * @throws DBException When anything goes wrong
	 */
	public void addMoveCommand(MoveCommandData moveCommandData) throws DBException {
		addElement(moveCommandData);
	}

	/**
	 * Method to set the Move Command Status.
	 * @param loadId - uniquely identify the move command
	 * @param currentStatus - contains move command status to be updated
	 * @param prevStatus - expected previous status of move command
	 * @throws DBException When anything goes wrong
	 */
	public void updateMoveCommandStatusValue(String loadId, int currentStatus, int prevStatus) throws DBException {
		mpMoveCommandData.clear();
		mpMoveCommandData.setKey(MoveCommandData.LOADID_NAME, loadId);
		mpMoveCommandData.setKey(MoveCommandData.STATUS_NAME, prevStatus);
		mpMoveCommandData.setCmdStatus(currentStatus);
		modifyElement(mpMoveCommandData);
	}

	
	/**
	 * Method to fetch all the move command for device and order by created date time
	 * @param deviceId - device identifier
	 * @return List<MoveCommandData> - which contains all the move command object as map
	 * @throws DBException
	 */
	public List<MoveCommandData> getAllMoveCommandData(String deviceId) throws DBException {
		mpMoveCommandData.clear();
		mpMoveCommandData.addOrderByColumn(MoveCommandData.CREATEDDATE_NAME);
		mpMoveCommandData.setKey(MoveCommandData.DEVICEID_NAME, deviceId);
		return DBHelper.convertData(getAllElements(mpMoveCommandData), MoveCommandData.class);
	}

	/**
	 * Method to fetch all latest the move command for load
	 * @param loadId - uniquely identify the move command
	 * @return MoveCommandData - finds the latest move command data
	 * @throws DBException When anything goes wrong
	 */
	public List<Map> getLatestMoveCommandForLoadId(String loadId) throws DBException {
		mpMoveCommandData.clear();
		StringBuilder vpSql = new StringBuilder("SELECT Top 1 * FROM MoveCommand WHERE ").append("sLoadID = '").append(loadId)
				.append("'").append(" and iCmdStatus < ").append(DBConstants.CMD_COMPLETED)
				.append(" order by dCreatedDate desc");

		List<Map> arrList = fetchRecords(vpSql.toString());
		if (!arrList.isEmpty()) {
			return arrList;
		}
		return null;
	}

	/**
	 * Method to delete all the move commands for load
	 * @param loadId - uniquely identify the move command
	 * @throws DBException When anything goes wrong
	 */
	public void deleteMoveCommand(String loadId){
		try {
			this.execute("delete from MoveCommand where sLoadID = ? ", loadId);
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}

	/**
	 * Method to delete the move commands for Id
	 * @param loadId - uniquely identify the move command
	 * @throws DBException When anything goes wrong
	 */
	public void deleteMoveCommandForId(String id) throws DBException {
		this.execute("delete from MoveCommand where iID = ? ", id);
	}
	
	/**
	 * Method to delete all the move commands from queue
	 * @throws DBException When anything goes wrong
	 */
	public void deleteAllMoveCommand() throws DBException {
		this.execute("delete from MoveCommand");
	}

	/**
	 * Method to fetch the oldest move command for load
	 * @param loadId - uniquely identify the move command
	 * @return MoveCommandData
	 * @throws DBException When anything goes wrong

	 */
	public MoveCommandData getOldestMoveCommandForLoadId(String loadId) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT * FROM MoveCommand WHERE ").append("sLoadID = '").append(loadId)
				.append("'").append("iCmdStatus < '").append(DBConstants.CMD_COMPLETED).append("'")
				.append("order by dCreatedDate asc");

		List<Map> arrList = fetchRecords(vpSql.toString());
		if (!arrList.isEmpty()) {
			List<MoveCommandData> moveCommandList = DBHelper.convertData(arrList, MoveCommandData.class);
			return moveCommandList.get(0);
		}
		return null;
		
	}

	/**
	 * Method to fetch all the move command for device with status
	 * @param deviceId - device identifier
	 * @param status - expected status of move command
	 * @return List of MoveCommandData
	 * @throws DBException When anything goes wrong
	 */
	public List<MoveCommandData> getAllMoveCommandDataWithDeviceIdAndStatus(String deviceId, int status) throws DBException {
		mpMoveCommandData.clear();
		mpMoveCommandData.setKey(MoveCommandData.DEVICEID_NAME, deviceId);
		mpMoveCommandData.setKey(MoveCommandData.STATUS_NAME, status);
		mpMoveCommandData.addOrderByColumn(MoveCommandData.CREATEDDATE_NAME);
		return DBHelper.convertData(getAllElements(mpMoveCommandData), MoveCommandData.class);
	}

	/**
	 * Method to delete all the move commands for load
	 * @param deviceId - device identifier
	 * @throws DBException When anything goes wrong
	 */
	public void deleteAllMoveCommandForDevice(String deviceId) throws DBException {
		this.execute("delete from MoveCommand where sDeviceID = ? ", deviceId);
	}

	/**
	 * Method to update all the move command status to deleted for given load
	 * @param loadId - uniquely identify the move command
	 * @param status - set new status
	 */
	public void updateMoveCommandStatusByLoadId(String sLoadId, int status) {
		try {
			this.execute("update MOVECOMMAND  set iCmdStatus = ? WHERE sLoadID = ? ", status, sLoadId);
		} catch (Exception e) {
			getLogger().logDebug(e.getMessage());
		}
		
	}
	
	public int getMoveCommandStatusForId(String id) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT * FROM MoveCommand WHERE ").append("iID = '").append(id)
				.append("' AND ").append("iCmdStatus < ").append(DBConstants.CMD_COMMANDED);

		List<Map> arrList = fetchRecords(vpSql.toString());
		if (!arrList.isEmpty()) {
			List<MoveCommandData> moveCommandList = DBHelper.convertData(arrList, MoveCommandData.class);
			return moveCommandList.get(0).getCmdStatus();
		}
		return -1;
	}

	public MoveCommandData getMoveCommnadById(String id) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT * FROM MoveCommand WHERE ").append("iID = ").append(id);
				
		List<Map> arrList = fetchRecords(vpSql.toString());
		if (!arrList.isEmpty()) {
			List<MoveCommandData> moveCommandList = DBHelper.convertData(arrList, MoveCommandData.class);
			return moveCommandList.get(0);
		}
		return null;
	}

	public void updateMoveCommandData(int id, int status, String from, String to) {
		try {
			this.execute("update MOVECOMMAND  set iCmdStatus = ?, sFrom = ?, sTodest = ? WHERE iId = ? ", status, from, to, id);
		} catch (Exception e) {
			getLogger().logDebug(e.getMessage());
		}
	}
	
	/**
	 * Method to fetch all the move command for load
	 * @param loadId - uniquely identify the move command
	 * @return MoveCommandData - finds the move command data
	 * @throws DBException When anything goes wrong
	 */
	public List<MoveCommandData> getAllMoveCommandsForLoadId(String loadId) throws DBException {
		mpMoveCommandData.clear();
		mpMoveCommandData.setKey(MoveCommandData.LOADID_NAME, loadId);
		if(!getAllElements(mpMoveCommandData).isEmpty()) {
			return DBHelper.convertData(getAllElements(mpMoveCommandData), MoveCommandData.class);
		}
		return new ArrayList<MoveCommandData>();
	}
}
