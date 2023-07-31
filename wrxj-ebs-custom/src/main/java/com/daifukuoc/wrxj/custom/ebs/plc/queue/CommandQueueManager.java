package com.daifukuoc.wrxj.custom.ebs.plc.queue;

import java.util.List;

import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;

/**
 * 
 * CommandQueueManager for managing commands
 * 
 * @author MT
 *
 */

public interface CommandQueueManager {

	/**
	 * Method to delete all the move commands from queue for device
	 * 
	 * @param deviceId - device identifier
	 */
	public void clear(String deviceId);
	
	/**
	 * Method to delete all the move commands from queue
	 */
	public void clearAll();

	/**
	 * Method to remove specific move command of loadId from queue
	 * 
	 * @param loadId - unique tray id for each load
	 */
	public void delete(String loadId);

	/**
	 * Method to return oldest move command from queue
	 * 
	 * @param loadId - unique tray id for each load
	 * @return MoveCommandData - returns oldest MoveCommand for given loadId
	 */
	public MoveCommandData dequeue(String loadId);

	/**
	 * Method to add move command in queue
	 * 
	 * @param moveCommandData- MoveCommandData object
	 */
	public void enqueue(MoveCommandData moveCommandData);

	/**
	 * Method to fetch all the move command list with ready status for that device
	 * 
	 * @param deviceId - device identifier
	 * @return List<MoveCommandData> - list of moveCommandData
	 */

	public List<MoveCommandData> load(String deviceId);

	/**
	 * Method to fetch all the move command list for device
	 * 
	 * @param deviceId - device identifier
	 * @return List<MoveCommandData> - list of moveCommandData
	 */
	public List<MoveCommandData> toList(String deviceId);

	/**
	 * Method to update status of old move command
	 * 
	 * @param loadId - unique tray id for each load
	 * @param status
	 */
	public void update(String loadId, int status);

	
}
