package com.daifukuamerica.wrxj.device.agv;

import com.daifukuamerica.wrxj.device.agv.messages.AGVData;

/**
 * Interface class to decouple message handling objects from database related
 * objects.
 * @author A.D.
 * @since  29-May-2009
 */
public interface AGVDBInterface
{
  /**
   * If the received sequence number and the last one have a difference greater
   * than one.  This may indicate a lost message.
   */
  int RECEIVED_SKIPPED_SEQ = -1;
  /**
   * If there is a Database update error.
   */
  int UPDATE_ERROR = -2;
  /**
   * If the data could not be found for some reason.
   */
  int DATA_NOT_FOUND = -3;
  /**
   * If there is a DB connect error.
   */
  int DB_CONNECT_ERROR = -4;
  /**
   * General database error.
   */
  int DB_ERROR = -5;

  /**
   * Load was dropped off at destination indicator.
   */
  int LOAD_AT_LOCATION = 10;
  /**
   * Load not picked up yet.
   */
  int LOAD_NOT_PICKED_UP = 11;
  /**
   * Load is confirmed to have been picked up.
   */
  int LOAD_PICKED_UP = 12;
  /**
   * Load move command sent to CMS awaiting ACK/NAK
   */
  int LOAD_MOVE_SENT = 13;
  /**
   * We've sucessfully sent the CMS a command to dispatch an AGV (it's been Ack'ed
   * or at worst been Nak'ed with a skipped sequence warning).
   */
  int LOAD_MOVE_PENDING = 14;
  /**
   * Move was aborted for some reason (we received MAB).  We mark the load as
   * in error and make them manually complete the move and tell us when it is
   * complete.  The CMS no longer has tracking for this move hence the manual
   * completion requirement.
   */
  int LOAD_MOVE_ERROR = 15;
  /**
   * Load move should be recovered.
   */
  int LOAD_MOVE_RECOVER = 16;
  /**
   * Cancel command requested by user form. Command still not sent to CMS yet.
   */
  int LOAD_MOVE_CANCEL_REQUEST = 17;
  /**
   * We received an MRC (move request canceled) response. This status can only happen
   * <u>before</u> the {@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#LOAD_PICKED_UP LOAD_PICKED_UP}
   * status (meaning the load has actually not been picked up yet). In this case
   * they will need to recover the load -- which means resend dispatch request.
   */
  int LOAD_MOVE_CANCELED = 18;
  /**
   * A move cancellation request was submitted to the CMS.
   */
  int LOAD_MOVE_CANCEL_PENDING = 19;
  /**
   * Cancel command was rejected for some reason.  Normally this state is from
   * a NAK message from CMS.
   */
  int LOAD_MOVE_CANCEL_ERROR = 20;
  /**
   * Cancel message sent awaiting ACK/NAK.
   */
  int LOAD_MOVE_CANCEL_SENT = 21;

  /**
   * AGV command complete.
   */
  int SYSCMD_COMPLETE = 30;
  /**
   * AGV Command sent awaiting ACK/NAK
   */
  int SYSCMD_SENT = 31;
  /**
   * AGV command successfully submitted to CMS, awaiting completion.
   */
  int SYSCMD_PENDING = 32;
  /**
   * AGV command rejected.
   */
  int SYSCMD_ERROR = 33;
  /**
   * System command requested.
   */
  int SYSCMD_REQUEST = 34;

 /**
  * Method to connect to the database.
  * @return 0 if the connection is successful, else
  * {@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#DB_CONNECT_ERROR DB_CONNECT_ERROR}.
  */
  public int connectToDatabase();

 /**
  * Method to close database connection.
  */
  public void closeDatabaseConnection();

 /**
  * Method to increment sequence number for each message we receive.
  * @throws AGVException if there is a databse update error.  Excepton will have
  * one of these possible error codes:
  * <ul>
  *  <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#DB_CONNECT_ERROR DB_CONNECT_ERROR}</li>
  *  <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#DATA_NOT_FOUND DATA_NOT_FOUND}</li>
  * </ul>
  */
  public void incrementInboundSequence() throws AGVException;
  
 /**
  * Method to get a sequence number to be used in formatting a new message.
  * @return a new sequence number.
  * @throws AGVException if there is a Database error. Possible exception error code:
  *  {@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#DATA_NOT_FOUND DATA_NOT_FOUND}
  */
  public int getFormatSequenceNumber() throws AGVException;

  /**
   * Method to get Heart Beat sequence number.  It does not make sense for the
   * Heart Beat message to have a sequence number, but that's the way the
   * Webb AGV spec. is written.  This method exists to make sure this sequence
   * number does not interfere with the normal outbound message sequence.
   * @return a new sequence number.
   * @throws AGVException if there is a Database error.
   */
  public int getHeartBeatSequenceNumber() throws AGVException;

 /**
  * Method checks if an inbound sequence was skipped.  If the sequence is skipped,
  * we simply log a message for troubleshooting purposes and continue (eg.
  * message(s) may have been skipped).
  * @param inReceivedSeq the sequence received from CMS.
  * @throws AGVException if there is a database error or skipped message sequence.
  */
  public void checkSkippedInboundSequence(int inReceivedSeq) throws AGVException;

 /**
  * Method to update inbound sequence number from the host.
  * @param inCMSSequence the CMS specified sequence.
  * @throws AGVException if there is a database error.
  */
  public void resyncInboundSequence(int inCMSSequence) throws AGVException;

 /**
  * Method deletes all AGV moves of given status(s).
  * @param ianStatus Move statuses by which to delete.
  * @throws AGVException if there is a deletion error.
  */
  public void deleteAGVMoves(int... ianStatus) throws AGVException;
  
 /**
  * Delete AGV move.
  * @param isLoadID the loadf ID of the move to delete.
  * 
  * @throws AGVException if there is a deletion error.
  */
  public void deleteAGVMove(String isLoadID) throws AGVException;

 /**
  * Method to update the vehicle id. of moving load.
  * @param isLoad the load being moved.
  * @param isVehicleID the vehicle id.
  * @throws AGVException if there is a databse update error.  Excepton will have
  * one of these possible error codes:
  * <ul>
  *  <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#DB_CONNECT_ERROR DB_CONNECT_ERROR}</li>
  *  <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#UPDATE_ERROR UPDATE_ERROR}</li>
  * </ul>
  */
  public void updateVehicleID(String isLoad, String isVehicleID)
         throws AGVException;

 /**
  * Method to update an AGV Move's status by load id.
  * @param isLoad the load id. of the move being updated.
  * @param inLoadStatus the load movement status.  The following values are
  *        acceptable:
  * <ul>
  *  <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#LOAD_AT_LOCATION LOAD_AT_LOCATION}</li>
  *  <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#LOAD_PICKED_UP LOAD_PICKED_UP}</li>
  *  <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#LOAD_MOVESENT_STATUS LOAD_MOVESENT_STATUS}</li>
  *  <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#LOAD_MOVE_PENDING LOAD_MOVE_PENDING}</li>
  * </ul>
  *
  * <ul>
  *  <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#DATA_NOT_FOUND DATA_NOT_FOUND}</li>
  * </ul>
  * @throws AGVException
  */
  public void updateAGVMoveStatus(String isLoad, int inLoadStatus)
         throws AGVException;

 /**
  * Method to update an AGV Move's status by sequence number.
  * @param inSequence the sequence number of the move.
  * @param inLoadStatus method to update the AGV move status by sequence number.
  * @throws AGVException if there is a database update error.
  */
  public void updateAGVMoveStatus(int inSequence, int inLoadStatus)
         throws AGVException;

 /**
  * Method to update an AGV command status.
  * @param inSequence the sequence key.
  * @param inCmdStatus the new status.
  * @throws  AGVException if there is a database update error.
  */
  public void updateAGVSystemCmdStatus(int inSequence, int inCmdStatus)
         throws AGVException;
 /**
  * Method to update AGV command status' for one or more records.
  * @param sSystemMessageID The message id of the command.
  * @param isCommandValue the command value.  This may be passed in as an empty
  *        string or a {@code null} if it shouldn't be used in the lookup.
  * @param inCmdStatus the new status.
  * @throws  AGVException if there is a database update error.
  */
  public void updateAGVSystemCmdStatus(String sSystemMessageID, 
                                       String isCommandValue, int inCmdStatus)
         throws AGVException;

 /**
  * Method deletes AGV system command.
  * @param sSystemMessageID The message id of the command.
  * @param ianCmdStatus System Command status' by which to delete.
  * @throws AGVException if there is a database error.
  */
  public void deleteAGVSystemCmd(String sSystemMessageID, int... ianCmdStatus)
         throws AGVException;

 /**
  * Method to update load location.
  * @param isLoad the load being updated.
  * @param isPickupLocation the pick up location.
  * @throws AGVException containing one of the following error codes.
  *  <ul>
  *    <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#DB_CONNECT_ERROR DB_CONNECT_ERROR}</li>
  *    <li>{@link com.daifukuamerica.wrxj.device.agv.AGVDBInterface#DATA_NOT_FOUND DATA_NOT_FOUND}</li>
  *  </ul>
  */
  public void updateLoadLocation(String isLoad, String isPickupLocation)
         throws AGVException;

 /**
  * Method to update the AGV station status.  This method is normally called
  * when we receive an SSR station to tell us if the station is online or offline.
  * @param isStationName the station name.
  * @param isStationStatus The station status value from CMS. Valid parameters
  *        are {@code AGVMessageConstants.STATION_ENABLED} or
  *            {@code AGVMessageConstants.STATION_DISABLED}
  * @throws AGVException
  * @see com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants#STATION_ENABLED
  * @see com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants#STATION_DISABLED
  */
  public void updateAGVStationStatus(String isStationName,
                                     String isStationStatus) throws AGVException;

 /**
  * Method checks if AGV station is online.
  * @param isAGVStation the AGV station
  * @return {@code true} if station is online.
  */
  public boolean isStationOnline(String isAGVStation);

 /**
  * Convenience method.  If the caller does not know the message category, use
  * this method to find what type of message to respond to by potentially searching
  * both tables (the sequence number will be unique between the two tables).
  * @param inMessageSequence the current message sequence.
  * @return reference to AGVData object. {@code null} if no record exists.
  * @throws AGVException if there is a database error.
  */
  public AGVData getData(int inMessageSequence) throws AGVException;

 /**
  * Method to get AGV data from the persistence layer.
  *
  * @param inMessageCat message category.  This is either MOVE_CATEGORY, or
  *        SYSTEM_CHANGE_CATEGORY.
  * @param inMessageSequence the current message sequence.
  * @return reference to AGVData object. {@code null} if no record exists.
  * @throws AGVException if there is a database error.
  * @see com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants#SYSTEM_CHANGE_CATEGORY
  * @see com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants#MOVE_CATEGORY
  */
  public AGVData getData(int inMessageCat, int inMessageSequence) throws AGVException;
}
