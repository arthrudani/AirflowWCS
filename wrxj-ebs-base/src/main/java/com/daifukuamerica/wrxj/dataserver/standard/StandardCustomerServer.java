package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.Customer;
import com.daifukuamerica.wrxj.dbadapter.data.CustomerData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Description:<BR>
 *   Server to handle Customer Specific operations.
 * 
 * @author       A.D./R.M.
 * @version      1.0
 * <BR>Created: 29-Nov-04<BR>
 *     Copyright (c) 2004<BR>
 *     Company:  Daifuku America Corporation
 */
public class StandardCustomerServer extends StandardServer
{
  public StandardCustomerServer()
  {
    this(null);
  }

  public StandardCustomerServer(String keyName)
  {
    super(keyName);
    logDebug("StandardCustomerServer.createCustomerServer()");
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardCustomerServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
  }

  /**
   *  Method adds Customer to the database.
   *
   *  @param cidata <code>CustomerData</code> containing data to add.
   *
   *  @throws <code>DBException</code>
   */
  public void addCustomer(CustomerData cidata) throws DBException
  {
    Customer customer = Factory.create(Customer.class);
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      customer.addElement(cidata);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException("StandardCustomerServer.addCustomer", e);
      if (e.isDuplicate())
        throw new DBException("Customer " + cidata.getCustomer() + " already exists!", true);
      else
        throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method modifies Customer in the database.
   *
   *  @param cidata <code>CustomerData</code> containing Key and
   *         data to modify.
   *
   *  @throws <code>DBException</code>
   */
  public String modifyCustomer(CustomerData cidata) throws DBException
  {
    Customer customer = Factory.create(Customer.class);

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      customer.modifyElement(cidata);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Inside modifyCustomer");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }

    return ("Customer modified successfully");
  }

  /**
   *  Method to delete Customer.
   *
   *  @param customerID <code>String</code> Customer ID. to delete.
   *  @return <code>int</code> value set to 1.
   *          -1 if the order doesn't exist.
   */
  public void deleteCustomer(String customerID) throws DBException, DBRuntimeException
  {
    Customer customer = Factory.create(Customer.class);
    CustomerData cidata = Factory.create(CustomerData.class);
    TransactionToken tt = null;
    try
    {
      cidata.setKey(CustomerData.CUSTOMER_NAME, customerID);
      tt = startTransaction();
      customer.deleteElement(cidata);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "deleteCustomer");
      throw e;
    }
    catch(NoSuchElementException ne)
    {
      throw new DBRuntimeException("Customer record not deleted...", ne);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Gets a list of Customer data based on field name and value.
   *
   *  @param fieldName <code>String</code> containing DB Column name.
   *  @param fieldValue <code>int</code> containing Column value.
   *  @return <code>List</code> of Customer data.
   */
  public List<Map> getCustomerData(String fieldName, int fieldValue)
    throws DBException
  {
    CustomerData cidata = Factory.create(CustomerData.class);
    cidata.setKey(fieldName, Integer.valueOf(fieldValue));

    return (getCustomerData(cidata));
  }

  /**
   *  Gets a list of Customer data based on field name and value.  It is
   *  assumed that the fieldValue is not necessarily exact but rather a pattern.
   *
   *  @param fieldName <code>String</code> containing DB Column name.
   *  @param fieldValue <code>String</code> containing Column value.
   *
   *  @return <code>List</code> of Customer data.
   */
  public List<Map> getCustomerData(String fieldName, String fieldValue)
    throws DBException
  {
    CustomerData cidata = Factory.create(CustomerData.class);
    if (fieldValue != null && fieldValue.trim().length() != 0)
    {
      cidata.setKey(fieldName, fieldValue, KeyObject.LIKE);
    }

    return (getCustomerData(cidata));
  }

  /**
   *  Gets a list of Customer data based on keys passed in cidata.
   *  @param cidata <code>CustomerData</code> object containing search
   *         information.
   *  @return <code>List</code> of Customer data.
   */
  public List<Map> getCustomerData(CustomerData cidata) throws DBException
  {
    Customer ci = Factory.create(Customer.class);
    return(ci.getAllElements(cidata));
  }

  /**
   *  Gets a Customer record.
   */
  public CustomerData getCustomerRecord(CustomerData cusData, int lockFlag)
         throws DBException
  {
    Customer ci = Factory.create(Customer.class);
    return(ci.getElement(cusData, lockFlag));
  }

  /**
   *  Convenience method. Gets a Customer Record with no Lock.
   */
  public CustomerData getCustomerRecord(CustomerData cusData)
         throws DBException
  {
    return (this.getCustomerRecord(cusData, DBConstants.NOWRITELOCK));
  }

  /**
   *  Convenience method. Gets a Customer with no Lock, and only the
   *  Customer ID.
   *  @param customerID <code>String</code> containing Customer ID of record being
   *         read.
   */
  public CustomerData getCustomerRecord(String customerID) throws DBException
  {
    CustomerData cusData = Factory.create(CustomerData.class);
    cusData.setKey(CustomerData.CUSTOMER_NAME, customerID);
    return (getCustomerRecord(cusData));
  }

 /**
  *  Convenience method. Gets a Customer with lock flag specified.
  *  @param customerID <code>String</code> containing Customer ID of record being
  *         read.
  *  @param writeLockFlag <code>int</code> specifying if record is locked.
  */
  public CustomerData getCustomerRecord(String customerID, int writeLockFlag)
         throws DBException
  {
    CustomerData cusData = Factory.create(CustomerData.class);
    cusData.setKey(CustomerData.CUSTOMER_NAME, customerID);
    return (getCustomerRecord(cusData, writeLockFlag));
  }

  public boolean CustomerExists(CustomerData cusData) throws DBException
  {
    return Factory.create(Customer.class).exists(cusData);
  }

  /*==========================================================================
            All types of Data gathering methods go in this section.
   ==========================================================================*/
 /**
  *  Gets list of all Customer IDs in the system.
  */
  public String[] getCustomerChoices(boolean insertAll) throws DBException
  {
    String[] cuslist = null;

    try
    {
      cuslist = Factory.create(Customer.class).getCustomerChoices(insertAll);
    }
    catch (DBException exc)
    {
      logException(exc, "getCustomerChoices");
      throw exc;
    }

    return (cuslist);
  }
}
