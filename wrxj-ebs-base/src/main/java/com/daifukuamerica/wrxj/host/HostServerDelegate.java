package com.daifukuamerica.wrxj.host;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *  Delegate to {@link com.daifukuamerica.wrxj.dataserver.HostServer HostServer}
 *  This class has calls redirected from the Host server.  This class provides for
 *  a cleaner delineation between a transaction mediator, which primarily performs
 *  transactions, and the rules of operating on the data.
 *
 * @author       A.D.
 * @version      1.0   02/18/2005
 */
public interface HostServerDelegate 
{
 /**
  * Sets the data content for a given operation.
  * @param theData Message information.  This may not be <code>null</code>.
  * @throws DBException if the delegate doesn't have
  *         appropriate info.
  */
  public void setInfo(Object ... theData) throws DBException;

 /**
  * Method tells delegate to lock a record when reading it.
  * @param readWithLock boolean value indicating if record should be locked.
  */
  public void setReadLockInfo(boolean readWithLock);
  
 /**
  * Adds an message to a host data queue.
  * @throws DBException if the delegate doesn't have
  *         appropriate info., or if a database error occurs.
  */
  public void addToDataQueue() throws DBException;
  
  /**
   * Adds an message to a host data queue.
   * @throws DBException if the delegate doesn't have
   *         appropriate info., or if a database error occurs.
   */
   public void addToDataQueue(int originalMessageSequenceNumber) throws DBException;
  
  /**
   * Adds an message to a host data queue.
   * @throws DBException if the delegate doesn't have
   *         appropriate info., or if a database error occurs.
   */
   public void addToDataQueue(int inRequestId, String isLoadId) throws DBException;
  
 /**
  * Deletes a processed message from the data queue. 
  * @throws com.daifukuamerica.wrxj.common.jdbc.DBException if the delegate doesn't have
  *         appropriate info., or if a database error occurs.
  */
  public void deleteMessage() throws DBException;

 /**
  * Deletes all processed inbound/outbound message from the data queue. 
  * @throws com.daifukuamerica.wrxj.common.jdbc.DBException if the delegate doesn't have
  *         appropriate info., or if a database error occurs.
  */
  public void deleteProcessedMessages() throws DBException;
  
 /**
  *  Method marks a message as processed.
  */
  public void markMessageProcessed() throws DBException;
  
 /**
  *  Method to toggle current processed flag.  <i>The delegate is expected to 
  *  know the Host name as well as the sequence number. In addition it must know
  *  what the value of the processed
  *  flag should be set to</i>
  */
  public void toggleProcessedFlag() throws DBException;

 /**
  * Reads messages from the Host data queue tables.
  * @return java.util.List of records.
  * @throws DBException if the delegate doesn't have
  *         appropriate info., or if a database error occurs.
  */
  public List<Map> getMessages() throws DBException;

 /**
  * Reads a message from the Host data queue tables.
  * @return {@link com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData#AbstractSKDCData AbstractSKDCData}
  *         if record is found, otherwise <code>null</code> is returned.
  * @throws DBException if the delegate doesn't have
  *         appropriate info., or if a database error occurs.
  */
  public AbstractSKDCData getMessage() throws DBException;

 /**
  * Reads oldest unprocessed message from a data queue.
  * @return {@link com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData#AbstractSKDCData AbstractSKDCData}
  *         if record is found, otherwise <code>null</code> is returned.
  * @throws DBException if the delegate doesn't have
  *         appropriate info., or if a database error occurs.
  */
  public AbstractSKDCData getOldestUnprocessedMessage() throws DBException;

 /**
  * Reads newest message from a data queue. <i>The delegate is
  * expected to know the host name, and whether the host record should be read
  * with a lock (default is read with no lock).</i>
  * @throws DBException if there are connectivity errors.
  */
  public AbstractSKDCData getNewestMessage() throws DBException;
  
 /**
  *  Checks if delegate has preliminary understanding of the info. it received.
  *  When the operation request is carried out, the actual information
  *  validation is performed.
  *  @return true if delegate understands its information.
  */
  public boolean isInfoUnderstood();

 /**
  *  Method gets the minimum, and maximum sequence numbers in the appropriate 
  *  host tables.
  *
  *  @return <code>int[]</code> array containing sequence number boundaries
  *                             (min/max).
  */
  public int[] getSequenceBoundaries() throws DBException;

  /**
   *  Method gets the minimum, and maximum sequence numbers in the HostIn 
   *  host tables.
   *
   *  @return <code>int[]</code> array containing sequence number boundaries
   *                             (min/max).
   */
   public int[] getHostSequenceBoundaries() throws DBException;

 /**
  *  Gets a list of all inbound/outbound messages defined in the system.
  *  @return String array of message names.
  *  @throws DBException for database access errors.
  */
  public String[] getMessageNames() throws DBException;
  
 /**
  * Checks if there are messages available for processing.
  * @return <code>true</code> if unprocessed messages exist, <code>false</code>
  *        otherwise.
  * @throws DBException if the delegate doesn't have
  *         appropriate info., or if a database error occurs.
  */
  public boolean unprocessedMessageAvailable() throws DBException;
}
