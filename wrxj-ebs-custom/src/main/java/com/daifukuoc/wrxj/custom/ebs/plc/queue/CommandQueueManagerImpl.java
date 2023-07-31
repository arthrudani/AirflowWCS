package com.daifukuoc.wrxj.custom.ebs.plc.queue;

import java.util.List;

import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveCommandServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Implementation class for CommandQueueManager
 * 
 * @author MT
 *
 */
public class CommandQueueManagerImpl implements CommandQueueManager {

	protected StandardMoveCommandServer standardMoveCommandServer = Factory.create(StandardMoveCommandServer.class);

	@Override
	public void enqueue(MoveCommandData moveCommandData) {
		try {
			standardMoveCommandServer.addMoveCommand(moveCommandData);
		} catch (DBException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(String loadId, int status) {
		try {
			standardMoveCommandServer.updateMoveCommand(loadId, status);
		} catch (DBException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(String loadId) {
		try {
			standardMoveCommandServer.deleteMoveCommand(loadId);
		} catch (DBException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<MoveCommandData> toList(String deviceId) {
		try {
			return standardMoveCommandServer.getAllMoveCommandList(deviceId);
		} catch (DBException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void clearAll() {
		try {
			standardMoveCommandServer.deleteAllMoveCommand();
		} catch (DBException e) {
			e.printStackTrace();

		}

	}
	
	@Override
	public void clear(String deviceId) {
		try {
			standardMoveCommandServer.deleteAllMoveCommandForDevice(deviceId);
		} catch (DBException e) {
			e.printStackTrace();

		}

	}

	@Override
	public MoveCommandData dequeue(String loadId) {
		try {
			return standardMoveCommandServer.getOldestMoveCommandForLoadId(loadId);
		} catch (DBException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<MoveCommandData> load(String deviceId) {
		try {
			return standardMoveCommandServer.getAllMoveCommandListForDeviceWithReadyStatus(deviceId);
		} catch (DBException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	

}
