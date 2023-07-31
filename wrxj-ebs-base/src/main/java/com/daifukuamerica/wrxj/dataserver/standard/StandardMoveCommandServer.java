package com.daifukuamerica.wrxj.dataserver.standard;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.data.MoveCommand;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;

public class StandardMoveCommandServer extends StandardServer {

	private MoveCommand mpMoveCommand = Factory.create(MoveCommand.class);

	/**
	 * Constructor for MoveCommand with no parameters
	 */
	public StandardMoveCommandServer() {
		super();
	}

	/**
	 * Constructor for load with name of who is creating it and the scheduler name
	 *
	 * @param isKeyName name of creator
	 */
	public StandardMoveCommandServer(String isKeyName) {
		super(isKeyName);
	}

	/**
	 * Web application constructor for per user connection pooling
	 * 
	 * @param keyName
	 * @param dbo
	 */
	public StandardMoveCommandServer(String keyName, DBObject dbo) {
		super(keyName, dbo);
	}

	/**
	 * Add a new move command
	 * 
	 * @param moveCommandData The move command data to be persisted
	 * @throws DBException When anything goes wrong
	 */
	public void addMoveCommand(MoveCommandData moveCommandData) throws DBException {
		TransactionToken transactionToken = null;
		try {
			transactionToken = startTransaction();

			mpMoveCommand.addMoveCommand(moveCommandData);

			commitTransaction(transactionToken);
		} finally {
			endTransaction(transactionToken);
		}
	}

	/**
	 * Method to update status of move command as per current status
	 * 
	 * @param loadId - uniquely identify the move command
	 * @param status - status which to be updated
	 * @throws DBException When anything goes wrong
	 */
	public void updateMoveCommand(String loadId, int status) throws DBException {
		TransactionToken transactionToken = null;
		try {
			transactionToken = startTransaction();
			switch (status) {
			case DBConstants.CMD_PROCCESSING:
				mpMoveCommand.updateMoveCommandStatusValue(loadId, status, DBConstants.CMD_COMMANDED);
				break;
			case DBConstants.CMD_COMPLETED:
				mpMoveCommand.updateMoveCommandStatusValue(loadId, status, DBConstants.CMD_PROCCESSING);
				break;
			case DBConstants.CMD_DELETED:
				mpMoveCommand.updateMoveCommandStatusValue(loadId, status, DBConstants.CMD_COMPLETED);
				break;
			default:
				break;
			}

			commitTransaction(transactionToken);
		} finally {
			endTransaction(transactionToken);
		}
	}

	/**
	 * Fetch all the move command from queue for device
	 * 
	 * @param deviceId - device identifier
	 * @return list of MoveCommandData
	 * @throws DBException When anything goes wrong
	 */
	public List<MoveCommandData> getAllMoveCommandList(String deviceId) throws DBException {
		
		return mpMoveCommand.getAllMoveCommandData(deviceId);
	}

	/**
	 * Method to delete specific move command for load Id
	 * 
	 * @param loadId - uniquely identify the move command
	 * @throws DBException When anything goes wrong
	 */
	public void deleteMoveCommand(String loadId) throws DBException {
		TransactionToken transactionToken = null;
		try {
			transactionToken = startTransaction();
			mpMoveCommand.deleteMoveCommand(loadId);
			commitTransaction(transactionToken);
		} finally {
			endTransaction(transactionToken);
		}

	}
	
	/**
	 * Method to delete specific move command for load Id 
	 * @param id
	 * @throws DBException
	 */
	public void deleteMoveCommandForId(String id) throws DBException {
		TransactionToken transactionToken = null;
		try {
			transactionToken = startTransaction();
			mpMoveCommand.deleteMoveCommandForId(id);
			commitTransaction(transactionToken);
		} finally {
			endTransaction(transactionToken);
		}

	}

	/**
	 * Method to clear all the move commands from queue
	 * 
	 * @throws DBException
	 */
	public void deleteAllMoveCommand() throws DBException {
		TransactionToken transactionToken = null;
		try {
			transactionToken = startTransaction();
			mpMoveCommand.deleteAllMoveCommand();
			commitTransaction(transactionToken);
		} finally {
			endTransaction(transactionToken);
		}
	}

	/**
	 * Method to fetch oldest move command from given loadId
	 * @param loadId - uniquely identify the move command
	 * @return MoveCommand
	 * @throws DBException
	 */
	public MoveCommandData getOldestMoveCommandForLoadId(String loadId) throws DBException {
		TransactionToken transactionToken = null;
		MoveCommandData moveCommandData = null;
		try {
			transactionToken = startTransaction();
			moveCommandData = mpMoveCommand.getOldestMoveCommandForLoadId(loadId);
			commitTransaction(transactionToken);
			return moveCommandData;
		} finally {
			endTransaction(transactionToken);
		}
	}

	
	/**
	 * Fetch all the move command from queue for device with status ready
	 * 
	 * @param deviceId - device identifier
	 * @return list of MoveCommandData
	 * @throws DBException When anything goes wrong
	 */
	public List<MoveCommandData> getAllMoveCommandListForDeviceWithReadyStatus(String deviceId) throws DBException {
		return mpMoveCommand.getAllMoveCommandDataWithDeviceIdAndStatus(deviceId, DBConstants.CMD_READY);
	}

	/**
	 * Delete all the move command from queue for device
	 * @param deviceId - device identifier
	 * @throws DBException 
	 */
	public void deleteAllMoveCommandForDevice(String deviceId) throws DBException {
		TransactionToken transactionToken = null;
		try {
			transactionToken = startTransaction();
			mpMoveCommand.deleteAllMoveCommandForDevice(deviceId);
			commitTransaction(transactionToken);
		} finally {
			endTransaction(transactionToken);
		}
	}
	
	/**
	 * Gets move command status for ready or unknown
	 * @param id
	 * @return
	 * @throws DBException
	 */
	public int getStatusForId(String id) throws DBException {
		return mpMoveCommand.getMoveCommandStatusForId(id);

	}

	public MoveCommandData getMoveCommnadById(String id) throws DBException {
		TransactionToken transactionToken = null;
		MoveCommandData moveCommandData = null;
		try {
			transactionToken = startTransaction();
			moveCommandData = mpMoveCommand.getMoveCommnadById(id);
			commitTransaction(transactionToken);
			return moveCommandData;
		} finally {
			endTransaction(transactionToken);
		}
	}
	
	public void updateMoveCommandData(int id, int status, String from, String to) throws DBException {
		TransactionToken transactionToken = null;
		try {
			transactionToken = startTransaction();
			mpMoveCommand.updateMoveCommandData(id, status, from,to);
			commitTransaction(transactionToken);
		} finally {
			endTransaction(transactionToken);
		}
	}

}
