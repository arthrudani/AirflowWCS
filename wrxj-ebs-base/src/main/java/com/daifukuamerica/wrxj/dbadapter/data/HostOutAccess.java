package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.List;
import java.util.Map;

/**
 * Class to handle HostOutAccess table functions.  This table controls what
 * messages are sent to the host(s).  <b>Note:</b> the HostConfig table enables
 * or disables a message, however, it does not do this by host name.  So if we
 * have multiple hosts this table allows us to enable a message to one host and
 * at the same time disable it for another.
 *
 * @author A.D.
 * @since  19-Nov-2008
 */
public class HostOutAccess extends BaseDBInterface 
{
  private HostOutAccessData hadata;

  public HostOutAccess()
  {
    super("HostOutAccess");
    hadata = Factory.create(HostOutAccessData.class);
  }
  
  /**
   * Method changes the host name in hostoutaccess
   * @param isOrigHostName <code>String</code> The original host name.
   * @param isNewHostName <code>String</code> The new host name.
   * @throws DBException
   */
  public void changeHostName(String isOrigHostName, String isNewHostName) throws DBException
  {
    hadata.clear();
    hadata.setKey(HostOutAccessData.HOSTNAME_NAME, isOrigHostName);
    hadata.setField(HostOutAccessData.HOSTNAME_NAME, isNewHostName);
    modifyElement(hadata);
  }
  
  /**
   *  Method gets a list of currently enabled host messages to the host.
   *  @param isHostName <code>String</code> containing order id. of the order being
   *         checked.
   *
   *  @return <code>String[]</code> array of messages that a given host can receive.
   */
  public String[] getMessageList(String isHostName, boolean izEnabled) throws DBException
  {
    int enabled = (izEnabled) ? DBConstants.YES : DBConstants.NO;
    StringBuilder vpSql = new StringBuilder("SELECT sMessageIdentifier FROM ").append(getWriteTableName())
             .append(" WHERE sHostName = '").append(isHostName).append("' AND ")
             .append("iEnabled = ").append(enabled);

    List<Map> vpList = fetchRecords(vpSql.toString());
    return(SKDCUtility.toStringArray(vpList, HostOutAccessData.MESSAGEIDENTIFIER_NAME));
  }
  
  /**
   * Method validates that a passed in message is okay to send to a host.
   * @param isHostName The Host's name.
   * @param isMessageIdentifier The message identifier or name.
   * @return true if the message is defined in atleast one host.
   * @throws DBException 
   */
  public boolean isLegalMessage(String isHostName, String isMessageIdentifier)
         throws DBException
  {
    String vsNewIdentifier = getProperMessageIdentifier(isMessageIdentifier);
    hadata.clear();
    hadata.setKey(HostOutAccessData.HOSTNAME_NAME, isHostName);
    hadata.setKey(HostOutAccessData.MESSAGEIDENTIFIER_NAME, vsNewIdentifier);
    hadata.setKey(HostOutAccessData.ENABLED_NAME, Integer.valueOf(DBConstants.YES));
    
    return(getCount(hadata) > 0);
  }

 /**
  * Checks for the existence of HostOutAccess message definition.
  * @param isHostName the Host Name.
  * @param isMessageIdentifier the Message identifier.
  * @return <code>true</code> if the message definition exists.
  * @throws DBException if there is a database access error.
  */
  public boolean exists(String isHostName, String isMessageIdentifier)
         throws DBException
  {
    String vsNewIdentifier = getProperMessageIdentifier(isMessageIdentifier);
    hadata.clear();
    hadata.setKey(HostOutAccessData.HOSTNAME_NAME, isHostName);
    hadata.setKey(HostOutAccessData.MESSAGEIDENTIFIER_NAME, vsNewIdentifier);

    return(exists(hadata));
  }

  /**
   *  Gets a list of order host out access data based on keys passed in inHOAData.
   *  @param ipHOAData <code>HostOutAccessData</code> object containing search
   *         information.
   *  @return <code>List</code> of host out access data.
   *  @exception DBException when DB exception is detected
   */
  public List<Map> getHostOutAccessData(HostOutAccessData ipHOAData)
         throws DBException
  {
    return getAllElements(ipHOAData);
  }

  /**
   *  Method retrieves a host out access record using host name and message ID as keys.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *  @param isMsgId <code>String</code> containing the message ID.
   *  @param withLock <code>int</code> flag indicating if record should be locked.
   *
   *  @return <code>HostOutAccessData</code> object. <code>null</code> if no record found.
   *  @exception DBException when DB exception is detected
   */
  public HostOutAccessData getHostOutAccessData(String isHostName, String isMsgId, int withLock)
         throws DBException
  {
    hadata.clear();
    hadata.setKey(HostOutAccessData.HOSTNAME_NAME, isHostName);
    hadata.setKey(HostOutAccessData.MESSAGEIDENTIFIER_NAME, isMsgId);

    return getElement(hadata, withLock);
  }

  /**
   *  Method adds host out access record.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *  @param isMsgId <code>String</code> containing the message ID.
   *  @param inEnabled <code>int</code> containing the enabled flag.
   *
   *  @exception DBException when DB exception is detected
   */
  public void addHostOutAccess(String isHostName, String isMsgId, int inEnabled) throws DBException
  {
    hadata.clear();
    hadata.setHostName(isHostName);
    hadata.setMessageIdentifier(isMsgId);
    hadata.setEnabled(inEnabled);
    
    addElement(hadata);
  }

  /**
   *  Method modifies the enabled field of a given host name and message ID.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *  @param isMsgId <code>String</code> containing the message ID.
   *  @param inEnabled <code>int</code> containing the enabled flag.
   *
   *  @exception DBException when DB exception is detected
   */
  public void modifyEnabledFlag(String isHostName, String isMsgId, int inEnabled) throws DBException
  {
    hadata.clear();
    hadata.setKey(HostOutAccessData.HOSTNAME_NAME, isHostName);
    hadata.setKey(HostOutAccessData.MESSAGEIDENTIFIER_NAME, isMsgId);
    hadata.setEnabled(inEnabled);

    modifyElement(hadata);
  }

  /**
   *  Method deletes a host out access record using host name and message ID as keys.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *  @param isMsgId <code>String</code> containing the message ID.
   *
   *  @exception DBException when DB exception is detected
   */
  public void deleteHostOutAccess(String isHostName, String isMsgId) throws DBException
  {
    hadata.clear();
    hadata.setKey(HostOutAccessData.HOSTNAME_NAME, isHostName);
    hadata.setKey(HostOutAccessData.MESSAGEIDENTIFIER_NAME, isMsgId);

    deleteElement(hadata);
  }

 /**
  * Make sure the message identifier that is passed is the unqualified name.
  * For XML based messages the message identifer will always be "some name" + "Message"
  * so that messages can be nested.  For delimited and fixed messages the message
  * identifier will not have the "Message" suffix in the string.  The HostOutAccess
  * table will always have the plain message name as part of the lookup.
  * @param isMessageID
  * @return
  */
  private String getProperMessageIdentifier(String isMessageID)
  {
    String vsNewIdentifier = isMessageID;
//    if (isMessageID.endsWith("Message"))
//    {
//      int vnSuffixOffset = isMessageID.lastIndexOf("Message");
//      vsNewIdentifier = isMessageID.substring(0, vnSuffixOffset);
//    }

    return(vsNewIdentifier);
  }
}