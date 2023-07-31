package com.daifukuamerica.wrxj.device.agv;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleMove;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleMoveData;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleSystemCmdData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxSequencer;
import com.daifukuamerica.wrxj.device.agv.messages.AGVData;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBCommException;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Warehouse Rx database update interface for AGVs.
 * 
 * @author A.D.
 * @since 29-May-2009
 */
public class AGVDBUpdateWrxjImpl implements AGVDBInterface
{
  private WrxSequencer mpSequencer;
  private VehicleMoveData mpVehicleData;
  private StandardDeviceServer mpDeviceServ;
  private StandardStationServer mpStnServ;

  public AGVDBUpdateWrxjImpl()
  {
    mpDeviceServ = Factory.create(StandardDeviceServer.class);
    mpStnServ = Factory.create(StandardStationServer.class);
    mpVehicleData = Factory.create(VehicleMoveData.class);
    mpSequencer = Factory.create(WrxSequencer.class);
  }

 /**
  * {@inheritDoc} This method is unnecessary for WarehouseRx since we are using 
  * a ThreadLocal
  * based connection pool.
  * @return 0
  */
  @Override
  public int connectToDatabase()
  {
    return(0);
  }

  @Override
  public void closeDatabaseConnection()
  {
    mpDeviceServ.cleanUp();
    mpStnServ.cleanUp();
  }

  @Override
  public int getFormatSequenceNumber() throws AGVException
  {
    int vnSeq = -1;
    try
    {
      vnSeq =mpDeviceServ.generateAGVMessageSequence(VehicleMove.OUTBOUND_SEQUENCER);
    }
    catch(DBException ex)
    {
      throw new AGVException("Error getting formatting sequence number.",
                             AGVDBInterface.UPDATE_ERROR, ex);
    }

    return(vnSeq);
  }

  @Override
  public int getHeartBeatSequenceNumber() throws AGVException
  {
    int vnSeq = -1;
    try
    {
      vnSeq = mpDeviceServ.generateAGVMessageSequence(VehicleMove.HEARTBEAT_SEQUENCER);
    }
    catch(DBException ex)
    {
      throw new AGVException("Error getting Heart Beat sequence number.",
                             AGVDBInterface.UPDATE_ERROR, ex);
    }

    return(vnSeq);
  }

  @Override
  public void deleteAGVMoves(int... ianStatus) throws AGVException
  {
    try
    {
      if (ianStatus.length > 0)
      {
        int[] vanStatuses = new int[ianStatus.length];
        for(int vnIdx = 0; vnIdx < vanStatuses.length; vnIdx++)
        {
          vanStatuses[vnIdx] = portableMoveStatusToWRxValue(ianStatus[vnIdx]);
        }
        mpDeviceServ.deleteAGVMoveRecordsByStatus(vanStatuses);
      }
      else
      {
        mpDeviceServ.deleteAGVMoveRecordsByStatus();
      }
    }
    catch(DBException ex)
    {
      throw new AGVException("Error deleting AGV Move records!", AGVDBInterface.DB_ERROR);
    }
    catch(NoSuchElementException nse)
    {
      // Don't propagate this!
    }
  }

 /**
  * Delete AGV move.
  * @param isLoadID the loadf ID of the move to delete.
  *
  * @throws AGVException if there is a deletion error.
  */
  @Override
  public void deleteAGVMove(String isLoadID) throws AGVException
  {
    try
    {
      mpDeviceServ.deleteAGVMoveRecordByLoadID(isLoadID);
    }
    catch(DBException ex)
    {
      throw new AGVException("Vehicle Move record for Load " + isLoadID +
                             " failed to delete!");
    }
  }

  @Override
  public void updateAGVMoveStatus(int inSequence, int inLoadStatus)
         throws AGVException
  {
    try
    {
      int vnWrxLoadStatus = portableMoveStatusToWRxValue(inLoadStatus);
      if (!mpDeviceServ.agvMoveSequenceExists(inSequence))
      {
        throw new AGVException("AGV Move record does not exist for " +
                               "sequence number " + inSequence,
                               AGVDBInterface.DATA_NOT_FOUND);
      }
      mpDeviceServ.updateAGVMoveStatus(inSequence, vnWrxLoadStatus);
    }
    catch(NoSuchElementException nse)
    {
      throw new AGVException("Vehicle Move record for sequence " + inSequence +
                             " failed to update! ");
    }
    catch(DBException exc)
    {
      throw new AGVException("Database error when updating AGV Move " +
                             "for sequence " + inSequence,
                             AGVDBInterface.UPDATE_ERROR, exc);
    }
    catch(DBCommException exc)
    {
      throw new AGVException("Database connection error when updating " +
                             "AGV Move for sequence " + inSequence,
                             AGVDBInterface.DB_CONNECT_ERROR, exc);
    }
  }

  @Override
  public void updateAGVMoveStatus(String isLoad, int inLoadStatus)
         throws AGVException
  {
    String vsLoadID = isLoad.trim();
    try
    {
      int vnWrxLoadStatus = portableMoveStatusToWRxValue(inLoadStatus);
      mpDeviceServ.updateAGVMoveStatus(vsLoadID, vnWrxLoadStatus);
    }
    catch(NoSuchElementException nse)
    {
      String vsErr = "Load " + vsLoadID +  " failed to update! ";
      if (!mpDeviceServ.agvLoadExists(vsLoadID))
      {
        throw new AGVException(vsErr + "Vehicle Move does not exist any more!!",
                               AGVDBInterface.DATA_NOT_FOUND);
      }
    }
    catch(DBException exc)
    {
      throw new AGVException("Database error when updating Load " +
                             vsLoadID, AGVDBInterface.UPDATE_ERROR, exc);
    }
    catch(DBCommException exc)
    {
      throw new AGVException("Database connection error when updating Load " +
                             vsLoadID, AGVDBInterface.DB_CONNECT_ERROR, exc);
    }
  }

  @Override
  public void updateAGVSystemCmdStatus(int inSequence, int inCmdStatus)
         throws AGVException
  {
    try
    {
      int vnCmdStatus = portableCmdStatusToWrxValue(inCmdStatus);
      if (!mpDeviceServ.agvCommandSequenceExists(inSequence))
      {
        throw new AGVException("AGV Command record does not exist for " +
                               "sequence number " + inSequence,
                               AGVDBInterface.DATA_NOT_FOUND);
      }
      mpDeviceServ.updateAGVCommandStatus(inSequence, vnCmdStatus);
    }
    catch(NoSuchElementException nse)
    {
      throw new AGVException("Vehicle Command record for sequence " + inSequence +
                             " failed to update! ");
    }
    catch(DBException exc)
    {
      throw new AGVException("Database error when updating AGV Command " +
                             "for sequence " + inSequence,
                             AGVDBInterface.UPDATE_ERROR, exc);
    }
    catch(DBCommException exc)
    {
      throw new AGVException("Database connection error when updating " +
                             "AGV command for sequence " + inSequence,
                             AGVDBInterface.DB_CONNECT_ERROR, exc);
    }
  }

  @Override
  public void updateAGVSystemCmdStatus(String isSystemMessageID,
                                       String isCommandValue, int inCmdStatus)
         throws AGVException
  {
    try
    {
      List<VehicleSystemCmdData> vpList = mpDeviceServ.getAGVCommandRecords(
                                             isSystemMessageID, isCommandValue);
      if (!vpList.isEmpty())
      {
        for(VehicleSystemCmdData vpData : vpList)
        {
          if (vpData.getCommandStatus() != DBConstants.AGV_SYSCMD_COMPLETE)
          {
            updateAGVSystemCmdStatus(vpData.getSequenceNumber(), inCmdStatus);
          }
        }
      }
    }
    catch(NoSuchElementException nse)
    {
      throw new AGVException("Vehicle Command record for command " +
                             isSystemMessageID + " failed to update! ");
    }
    catch(DBException exc)
    {
      throw new AGVException("Database error when updating AGV Command " +
                           isSystemMessageID, AGVDBInterface.UPDATE_ERROR, exc);
    }
    catch(DBCommException exc)
    {
      throw new AGVException("Database connection error when updating " +
                             "AGV command " + isSystemMessageID,
                             AGVDBInterface.DB_CONNECT_ERROR, exc);
    }
  }

  public void deleteAGVSystemCmd(String isMessageID, int... ianCommandStatus)
         throws AGVException
  {
    try
    {
      if (ianCommandStatus.length > 0)
      {
        int[] vanStatuses = new int[ianCommandStatus.length];
        for(int vnIdx = 0; vnIdx < vanStatuses.length; vnIdx++)
        {
          vanStatuses[vnIdx] = portableMoveStatusToWRxValue(ianCommandStatus[vnIdx]);
        }
        mpDeviceServ.deleteAGVCommandRecordsByStatus(vanStatuses);
      }
      else
      {
        mpDeviceServ.deleteAGVCommandRecordsByStatus();
      }
    }
    catch(DBException ex)
    {
      throw new AGVException("Error deleting AGV System Command records!",
                             AGVDBInterface.DB_ERROR);
    }
    catch(NoSuchElementException nse)
    {
      // Don't propagate this!
    }
  }

  @Override
  public void updateVehicleID(String isLoad, String isVehicleID)
         throws AGVException
  {
    String vsLoadID = isLoad.trim();

    mpVehicleData.clear();
    mpVehicleData.setKey(VehicleMoveData.LOADID_NAME, vsLoadID);
    mpVehicleData.setVehicleID(isVehicleID);
    try
    {
      mpDeviceServ.updateAGVRecord(mpVehicleData);
    }
    catch(DBException exc)
    {
      throw new AGVException("Database error updating AGV vehicle id. for load " +
                             vsLoadID + ". Vehicle id. = " + isVehicleID,
                             AGVDBInterface.UPDATE_ERROR, exc);
    }
  }

 /**
  * {@inheritDoc} <b>Warehouse Rx Specific:</b> update the load's current
  * location.  We know about this location once we receive the Expected
  * Receipt from the host that has the pickup and destination station.  This
  * method is normally called when we receive the LAL response.  Update the 
  * load's location to the destination station.
  * 
  * @param isLoad the load being updated.
  * @param isCurrentLocation the current location/station of the load.
  * @throws AGVException
  */
  @Override
  public void updateLoadLocation(String isLoad, String isCurrentLocation)
         throws AGVException
  {
    String vsLoadID = isLoad.trim();

    try
    {
      mpDeviceServ.updateCurrAGVLoadLocn(vsLoadID, isCurrentLocation);
    }
    catch(DBException exc)
    {
      throw new AGVException("Database error updating AGV record for load " +
                             vsLoadID, AGVDBInterface.UPDATE_ERROR, exc);
    }
    catch(DBCommException exc)
    {
      throw new AGVException("Database connection error when updating Load " +
                             vsLoadID, AGVDBInterface.DB_CONNECT_ERROR, exc);
    }
    catch(NoSuchElementException nse)
    {
      String vsErr = "Load " + vsLoadID +  " failed to update! ";
      if (!mpDeviceServ.agvLoadExists(vsLoadID))
      {                                // If it disappeared we do care!
        throw new AGVException(vsErr + "Load does not exist any more!!",
                               AGVDBInterface.DATA_NOT_FOUND);
      }
    }
  }

  @Override
  public void updateAGVStationStatus(String isStationName,
                                    String isPhysicalStatus) throws AGVException
  {
    DBObject vpDBObj = new DBObjectTL().getDBObject();

    if (!mpStnServ.exists(isStationName))
    {
      throw new AGVException("Station " + isStationName + " received from " +
                             "CMS does not exist!", AGVDBInterface.DATA_NOT_FOUND);
    }

    TransactionToken vpTok = null;

    try
    {
      int vnStationStatus = -1;
      if (isPhysicalStatus.equals(AGVMessageConstants.STATION_ENABLED))
        vnStationStatus = DBConstants.ONLINE;
      else
        vnStationStatus = DBConstants.OFFLINE;

      vpTok = vpDBObj.startTransaction();
      mpStnServ.setPhysicalStatus(isStationName, vnStationStatus);
      vpDBObj.commitTransaction(vpTok);
    }
    catch(DBException exc)
    {
      throw new AGVException("Error updating AGV station status! " +
                             exc.getMessage(), AGVDBInterface.UPDATE_ERROR);
    }
    catch(DBCommException exc)
    {
      throw new AGVException("Database connection error when updating station " +
                             isStationName, AGVDBInterface.DB_CONNECT_ERROR, exc);
    }
    finally
    {
      vpDBObj.endTransaction(vpTok);
    }
  }

  @Override
  public boolean isStationOnline(String isAGVStation)
  {
    int vnPhysicalStatus = mpStnServ.getPhysicalStatus(isAGVStation);

    return(vnPhysicalStatus == DBConstants.ONLINE);
  }

  @Override
  public void incrementInboundSequence() throws AGVException
  {
    try
    {
      mpDeviceServ.generateAGVMessageSequence(VehicleMove.INBOUND_SEQUENCER);
    }
    catch(DBException exc)
    {
      throw new AGVException("Error generating sequence number.",
                             AGVDBInterface.DATA_NOT_FOUND);
    }
    catch(DBCommException exc)
    {
      throw new AGVException("Database connection error when incrementing " +
                    "inbound sequence.", AGVDBInterface.DB_CONNECT_ERROR, exc);
    }
  }
  
  @Override
  public AGVData getData(int inMessageSequence) throws AGVException
  {
    AGVData vpData = null;

    vpData = getData(AGVMessageConstants.MOVE_CATEGORY, inMessageSequence);
    if (vpData == null)
    {
      vpData = getData(AGVMessageConstants.SYSTEM_CHANGE_CATEGORY, inMessageSequence);
    }

    return(vpData);
  }

  @Override
  public AGVData getData(int inCategory, int inMessageSequence) throws AGVException
  {
    AGVData vpData = null;

    try
    {
      if (inCategory == AGVMessageConstants.MOVE_CATEGORY)
      {
        VehicleMoveData vpMovData = mpDeviceServ.getAGVMoveRecord(inMessageSequence);
        if (vpMovData != null)
        {
          int vnPortableMoveStatus = wrxMoveStatusToPortableValue(vpMovData.getAGVLoadStatus());
          
          AGVMessageNameEnum vpName = AGVMessageNameEnum.MOV_REQUEST;
          if (vpMovData.getAGVLoadStatus() == DBConstants.AGV_MOVECANCELREQUEST)
          {                            // This is a cancel request really.
            vpName = AGVMessageNameEnum.CAN_REQUEST;
          }
          vpData = new AGVData();
          vpData.setMessageName(vpName);
          vpData.setLoadID(vpMovData.getLoadID());
          vpData.setSequenceNumber(vpMovData.getSequenceNumber());
          vpData.setAGVLoadStatus(vnPortableMoveStatus);
          vpData.setMessageAddTime(vpMovData.getStatusChangeTime());
          vpData.setVehicleID(vpMovData.getVehicleID());
          vpData.setRequestID(vpMovData.getRequestID());
          vpData.setCurrStation(vpMovData.getCurrentStation());
          vpData.setDestStation(vpMovData.getDestStation());
          vpData.setDualLoadMoveSeq(vpMovData.getDualLoadMoveSeq());
          vpData.setNotifyHost(vpMovData.getNotifyHost());
        }
      }
      else
      {
        VehicleSystemCmdData vpCmdData = mpDeviceServ.getAGVCommandRecord(inMessageSequence);
        if (vpCmdData != null)
        {
          int vnPortableCmdStatus = wrxCmdStatusToPortableValue(
                                                 vpCmdData.getCommandStatus());

          vpData = new AGVData();
          vpData.setMessageName(AGVMessageNameEnum.getEnumObject(vpCmdData.getSystemMessageID()));
          vpData.setSequenceNumber(vpCmdData.getSequenceNumber());
          vpData.setCommandValue(vpCmdData.getCommandValue());
          vpData.setCommandStatus(vnPortableCmdStatus);
        }
      }
    }
    catch(DBException exc)
    {
      throw new AGVException("Database error when getting AGV record for " +
                             "sequence number " + inMessageSequence,
                             AGVDBInterface.DB_ERROR, exc);
    }
    catch(DBCommException exc)
    {
      throw new AGVException("Database connection error when getting AGV " +
                             "record for sequence number " + inMessageSequence,
                             AGVDBInterface.DB_CONNECT_ERROR, exc);
    }

    return(vpData);
  }

  @Override
  public void checkSkippedInboundSequence(int inReceivedSeq) throws AGVException
  {
    int vnCurrSeq;
    TransactionToken vpTok = null;
    DBObject vpDBObj = new DBObjectTL().getDBObject();
    
    try
    {
      /*
       * Start/end tran needed to release DB lock.
       */
      vpTok = vpDBObj.startTransaction();
      vnCurrSeq = mpSequencer.getCurrentSequenceNumber("AGVInboundSeq",
                "AGVController", DBConstants.DEVICE_SEQ, DBConstants.WRITELOCK);
      vpDBObj.commitTransaction(vpTok);
      if (inReceivedSeq - vnCurrSeq > 1)
      {
        throw new AGVException("Received skipped sequence number from CMS! " +
                               "Expected " + vnCurrSeq + " but received " +
                               inReceivedSeq, AGVDBInterface.RECEIVED_SKIPPED_SEQ);
      }
    }
    catch(DBException exc)
    {
      throw new AGVException("Error getting current inbound sequence number.",
                             AGVDBInterface.DATA_NOT_FOUND, exc);
    }
    finally
    {
      vpDBObj.endTransaction(vpTok);
    }
  }

  @Override
  public void resyncInboundSequence(int inCMSSequence) throws AGVException
  {
    TransactionToken vpTok = null;
    DBObject vpDBObj = new DBObjectTL().getDBObject();
    try
    {
      vpTok = vpDBObj.startTransaction();
      mpSequencer.setNewSequenceValue("AGVInboundSeq", "AGVController",
                                      DBConstants.DEVICE_SEQ, inCMSSequence);
    /*
     * When they send us a zero sequence for resyncing it means they are starting
     * with a clean system (e.g. if they "cold start" their CMS controller). So mark
     * all incomplete moves in our system to an error status and have them manually
     * recover those loads (force them to deliver them manually and complete
     * move on WRx recovery screens).
     */
      if (inCMSSequence == 0)
      {
        mpDeviceServ.markAGVMovesForRecovery();
      }
      vpDBObj.commitTransaction(vpTok);
    }
    catch(DBException exc)
    {
      throw new AGVException("Error updating inbound sequence number, " +
                             "or AGV Move records to a recovery state.",
                             AGVDBInterface.UPDATE_ERROR, exc);
    }
    finally
    {
      vpDBObj.endTransaction(vpTok);
    }
  }

/*============================================================================
 *               Private Methods go in this section
 *============================================================================*/

 /**
  * Converts the portable Load move status' to what this implementation needs.
  * Other implementations will need to write their own converter since they
  * may not translate to integers in the database but rather string values.
  *
  * @param inPortableTranslation the generic translation number.
  * @return translation integer known to Warehouse Rx.
  */
  private int portableMoveStatusToWRxValue(int inPortableTranslation)
  {
    int vnAGVLoadStatus;

    switch(inPortableTranslation)
    {
      case AGVDBInterface.LOAD_NOT_PICKED_UP:
        vnAGVLoadStatus = DBConstants.AGV_NOMOVE;
        break;
        
      case AGVDBInterface.LOAD_AT_LOCATION:
        vnAGVLoadStatus = DBConstants.AGV_MOVECOMPLETE;
        break;

      case AGVDBInterface.LOAD_PICKED_UP:
        vnAGVLoadStatus = DBConstants.AGV_MOVING;
        break;

      case AGVDBInterface.LOAD_MOVE_ERROR:
        vnAGVLoadStatus = DBConstants.AGV_MOVEERROR;
        break;

      case AGVDBInterface.LOAD_MOVE_SENT:
        vnAGVLoadStatus = DBConstants.AGV_MOVESENT;
        break;

      case AGVDBInterface.LOAD_MOVE_PENDING:
        vnAGVLoadStatus = DBConstants.AGV_MOVEPENDING;
        break;

      case AGVDBInterface.LOAD_MOVE_RECOVER:
        vnAGVLoadStatus = DBConstants.AGV_RECOVERABLE;
        break;

      case AGVDBInterface.LOAD_MOVE_CANCELED:
        vnAGVLoadStatus = DBConstants.AGV_MOVECANCELED;
        break;

      case AGVDBInterface.LOAD_MOVE_CANCEL_PENDING:
        vnAGVLoadStatus = DBConstants.AGV_MOVECANCELPENDING;
        break;

      case AGVDBInterface.LOAD_MOVE_CANCEL_ERROR:
        vnAGVLoadStatus = DBConstants.AGV_MOVECANCELERROR;
        break;

      case AGVDBInterface.LOAD_MOVE_CANCEL_SENT:
        vnAGVLoadStatus = DBConstants.AGV_MOVECANCELSENT;
        break;

      default:
        vnAGVLoadStatus = 0;
    }

    return(vnAGVLoadStatus);
  }

  private int wrxMoveStatusToPortableValue(int inWrxConstant)
  {
    int vnAGVLoadStatus;

    switch(inWrxConstant)
    {
      case DBConstants.AGV_NOMOVE:
        vnAGVLoadStatus = AGVDBInterface.LOAD_NOT_PICKED_UP;
        break;

      case DBConstants.AGV_MOVECOMPLETE:
        vnAGVLoadStatus = AGVDBInterface.LOAD_AT_LOCATION;
        break;

      case DBConstants.AGV_MOVING:
        vnAGVLoadStatus = AGVDBInterface.LOAD_PICKED_UP;
        break;

      case DBConstants.AGV_MOVEERROR:
        vnAGVLoadStatus = AGVDBInterface.LOAD_MOVE_ERROR;
        break;

      case DBConstants.AGV_MOVESENT:
        vnAGVLoadStatus = AGVDBInterface.LOAD_MOVE_SENT;
        break;

      case DBConstants.AGV_MOVEPENDING:
        vnAGVLoadStatus = AGVDBInterface.LOAD_MOVE_PENDING;
        break;

      case DBConstants.AGV_RECOVERABLE:
        vnAGVLoadStatus = AGVDBInterface.LOAD_MOVE_RECOVER;
        break;

      case DBConstants.AGV_MOVECANCELREQUEST:
        vnAGVLoadStatus = AGVDBInterface.LOAD_MOVE_CANCEL_REQUEST;
        break;

      case DBConstants.AGV_MOVECANCELED:
        vnAGVLoadStatus = AGVDBInterface.LOAD_MOVE_CANCELED;
        break;

      case DBConstants.AGV_MOVECANCELPENDING:
        vnAGVLoadStatus = AGVDBInterface.LOAD_MOVE_CANCEL_PENDING;
        break;

      case DBConstants.AGV_MOVECANCELERROR:
        vnAGVLoadStatus = AGVDBInterface.LOAD_MOVE_CANCEL_ERROR;
        break;

      case DBConstants.AGV_MOVECANCELSENT:
        vnAGVLoadStatus = AGVDBInterface.LOAD_MOVE_CANCEL_SENT;
        break;

      default:
        vnAGVLoadStatus = 0;
    }

    return(vnAGVLoadStatus);
  }

  private int portableCmdStatusToWrxValue(int inPortableTranslation)
  {
    int vnCmdStatus;
    switch(inPortableTranslation)
    {
      case AGVDBInterface.SYSCMD_COMPLETE:
        vnCmdStatus = DBConstants.AGV_SYSCMD_COMPLETE;
        break;

      case AGVDBInterface.SYSCMD_SENT:
        vnCmdStatus = DBConstants.AGV_SYSCMD_SENT;
        break;

      case AGVDBInterface.SYSCMD_PENDING:
        vnCmdStatus = DBConstants.AGV_SYSCMD_PENDING;
        break;

      case AGVDBInterface.SYSCMD_ERROR:
        vnCmdStatus = DBConstants.AGV_SYSCMD_ERROR;
        break;

      case AGVDBInterface.SYSCMD_REQUEST:
        vnCmdStatus = DBConstants.AGV_SYSCMD_REQUEST;
        break;
        
      default:
        vnCmdStatus = 0;
    }
    return(vnCmdStatus);
  }

  private int wrxCmdStatusToPortableValue(int inPortableTranslation)
  {
    int vnCmdStatus;
    switch(inPortableTranslation)
    {
      case DBConstants.AGV_SYSCMD_COMPLETE:
        vnCmdStatus = AGVDBInterface.SYSCMD_COMPLETE;
        break;

      case DBConstants.AGV_SYSCMD_PENDING:
        vnCmdStatus = AGVDBInterface.SYSCMD_PENDING;
        break;

      case DBConstants.AGV_SYSCMD_SENT:
        vnCmdStatus = AGVDBInterface.SYSCMD_SENT;
        break;

      case DBConstants.AGV_SYSCMD_ERROR:
        vnCmdStatus = AGVDBInterface.SYSCMD_ERROR;
        break;

      case DBConstants.AGV_SYSCMD_REQUEST:
        vnCmdStatus = AGVDBInterface.SYSCMD_REQUEST;
        break;

      default:
        vnCmdStatus = 0;
    }
    return(vnCmdStatus);
  }
}
