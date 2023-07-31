package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Class to read/manipulate JVM Config table.
 * 
 * @author A.D.
 * @since  10-Feb-2009
 */
public class JVMConfig extends BaseDBInterface
{
  protected JVMConfigData mpJVMData;

  public JVMConfig()
  {
    super("JVMConfig");
    mpJVMData = Factory.create(JVMConfigData.class);
  }

 /**
  * Method checks if any JVM is configured in the system.
  * @return <code>true</code> if there any JVM is configured.
  * @throws DBException  if there is a DB access error.
  */
  public boolean isAnyJVMConfigured() throws DBException
  {
    mpJVMData.clear();
    return(getCount(mpJVMData) > 0);
  }

 /**
  * Method tests if a given JVM Identifier represents the primary JVM.
  * @param isJVMId the JVM Identifier.
  * @return <code>true</code> if JVM Identifier represents the primary JVM;
  * @throws DBException for database errors.
  */
  public boolean isPrimaryJVM(String isJVMId) throws DBException
  {
    mpJVMData.clear();
    mpJVMData.setKey(JVMConfigData.JVMIDENTIFIER_NAME, isJVMId);
    mpJVMData.setJVMType(0);
    List<Map> vpList = getSelectedColumnElements(mpJVMData);

    int vnJVMType = 0;
    if (!vpList.isEmpty())
    {
      vnJVMType = DBHelper.getIntegerField(vpList.get(0),
                                           JVMConfigData.JVMTYPE_NAME);
    }

    return(vnJVMType == DBConstants.PRIMARY_JVM);
  }

 /**
  * Method gets the JVM identifier associated with this instance of the JVM.
  * @param isJMSTopic the JMS topic.
  * @return the JVM Identifier.
  * @throws DBException if there is a DB access error.
  */
  public JVMConfigData getJVMRecord(String isJMSTopic) throws DBException
  {
    mpJVMData.clear();
    mpJVMData.setKey(JVMConfigData.JMSTOPIC_NAME, isJMSTopic);

    return(getElement(mpJVMData, DBConstants.NOWRITELOCK));
  }

 /**
  * Method to get an array of JVMId's under a particular super warehouse.
  * @param isSuperWhs the super warehouse
  * @return array of JVM IDs.  Empty array if nothing found.
  * @throws DBException if there is a database error.
  */
  public String[] getJVMIDsPerSuperWarehouse(String isSuperWhs)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT jvm.sJVMIdentifier FROM JVMConfig jvm, ")
               .append("Device dv, Warehouse wh WHERE ")
               .append("wh.iWarehouseType = ").append(DBConstants.REGULAR).append(" AND ")
               .append("wh.sSuperWarehouse = ? AND ")
               .append("wh.sWarehouse = dv.sWarehouse AND ")
               .append("jvm.sJVMIdentifier = dv.sJVMIdentifier");
    List<Map> vpList = fetchRecords(vpSql.toString(), isSuperWhs);

    return(SKDCUtility.toStringArray(vpList, JVMConfigData.JVMIDENTIFIER_NAME));
  }

 /**
  * Method to reserve a JVM for use. <b>This method must be called within a
  * transaction!</b>
  *
  * @param isJVMId the JVM Identifier that is in current use.
  * @return record containing JVM config info.  <code>null</code> if no suitable
  * entry is found.
  * @throws DBException if there is a DB access/update error.
  */
  public JVMConfigData reserveJVM(String isJVMId) throws DBException
  {
    mpJVMData.clear();
    mpJVMData.setKey(JVMConfigData.JVMIDENTIFIER_NAME, isJVMId);
//    mpJVMData.setKey(JVMConfigData.JVMSTATUS_NAME, DBConstants.JVM_UNUSED);
    JVMConfigData vpJVMData = getElement(mpJVMData, DBConstants.WRITELOCK);

    if (vpJVMData != null)
    {
      setJVMStatus(vpJVMData.getJVMIdentifier(), DBConstants.JVM_INUSE);
      vpJVMData.setJVMStatus(DBConstants.JVM_INUSE);
    }

    return(vpJVMData);
  }

  /**
  * Method to unreserve a JVM.  <b>This method must be called from within a
  * transaction.</b>
  * @param isJVMId the JVM Identifier that is in current use.
  * @throws DBException if there is a database access/modify error.
  */
  public void unreserveJVM(String isJVMId) throws DBException
  {
    mpJVMData.clear();
    mpJVMData.setKey(JVMConfigData.JVMIDENTIFIER_NAME, isJVMId);
    mpJVMData.setJVMStatus(DBConstants.JVM_UNUSED);
    modifyElement(mpJVMData);
  }

 /**
  * Checks if this JVM id. exists.
  * @param isJVMIdentifier the jvm identifier.
  * @return <code>true</code> if JVM record exists.
  * @throws DBException if there is an DB access error.
  */
  public boolean exists(String isJVMIdentifier) throws DBException
  {
    mpJVMData.clear();
    mpJVMData.setKey(JVMConfigData.JVMIDENTIFIER_NAME, isJVMIdentifier);
    return(exists(mpJVMData));
  }
  
 /**
  * Sets the JVM identifier record status.
  * @param isJVMIdentifier the jvm identifier.
  * @param inNewStatus the new status.
  * @throws DBException if there is a database error, or the record was not
  *         updated for some reason.
  */
  public void setJVMStatus(String isJVMIdentifier, int inNewStatus)
         throws DBException
  {
    mpJVMData.clear();
    mpJVMData.setKey(JVMConfigData.JVMIDENTIFIER_NAME, isJVMIdentifier);
    mpJVMData.setJVMStatus(inNewStatus);

    try
    {
      modifyElement(mpJVMData);
    }
    catch(NoSuchElementException nse)
    {
      throw new DBException("JVM ID not updated!", nse);
    }
  }
}
