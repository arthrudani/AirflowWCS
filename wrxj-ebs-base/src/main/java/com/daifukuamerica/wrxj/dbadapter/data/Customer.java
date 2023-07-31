package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description:<BR>
 *   Class for handling Customer table interactions.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 01-Sep-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
public class Customer extends BaseDBInterface
{
  protected CustomerData mpCustData;

  public Customer()
  {
    super("Customer");
    mpCustData = Factory.create(CustomerData.class);
  }

  /**
   * Method to get List of Customers..
   *  @param izAllOrNone <code>boolean</code> containing indicator of string to start list
   *         with.  The values are SKDCConstants.ALL_STRING or
   *         SKDCConstants.NONE_STRING
   *
   * @return StringBuffer Customers.
   */
  public String[] getCustomerChoices(boolean izAllOrNone) throws DBException
  {
    return getDistinctColumnValues(CustomerData.CUSTOMER_NAME, 
                   (izAllOrNone) ? SKDCConstants.ALL_STRING : "");
  }

  /**
   * Method to check if short orders are allowed for a given customer.
   *
   * @return boolean of true if short orders allowed, false otherwise.
   */
  public boolean allowsShortOrders(String custid) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iProcessShort AS \"iProcessShort\" FROM Customer ")
        .append("WHERE sCustomer = '").append(custid).append("'");

    return getIntegerColumn("iProcessShort", vpSql.toString()) == DBConstants.YES;
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpCustData    = null;
  }
}
