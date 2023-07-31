package com.daifukuamerica.wrxj.dbadapter;

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Description:<BR>
 *  Base class for the Database View Interface classes.  
 *  This class differs from BaseDBInterface in that we are dealing 
 *  with DB views instead of actual tables. While views may be 
 *  mutable, we wish to keep them read-only and avoid problems with 
 *  adding, modifying and deleting.
 *
 * @author       R.G.
 * @version      1.0    04/05/05
 */
public class ViewDBInterface extends BaseDBInterface
{
  public ViewDBInterface()
  {
    super();
  }

  public ViewDBInterface(String isViewName)
  {
    super(isViewName);
  }

  /**
   * Retrieves one record using unique key.
   *
   * @param eskdata <code>AbstractSKDCData</code> object.  This object should
   *        have Key information in it already.
   * @param withLock integer whose value is WRITELOCK or NOWRITELOCK.
   *        This value must be NOWRITELOCK, as a view will not be mutable.
   *
   * @return reference to object containing current record. <code>null</code> 
   *         reference if no records found.
   * @exception DBException
   */
  @Override
  public AbstractSKDCData getElement(AbstractSKDCData eskdata, int withLock)
      throws DBException
  {
    if (withLock == DBConstants.WRITELOCK)
    {                                  // If this is a write lock.
      throw new DBException("Write locks are not allowed in views");
    }
    
    return super.getElement(eskdata, withLock);
  }

  /**
   * Method to add a row of data.
   * 
   * NOT ALLOWED WITH VIEWS... Automatically throws an exception...
   * 
   * @param eskdata <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   * @exception DBException if Database Add error.
   */
  @Override
  public void addElement(AbstractSKDCData eskdata) throws DBException
  {
    throw new DBException("Views are Read-Only, Cannot add a view");
  }

  /**
   * Method to modify a row of data.
   * 
   * NOT ALLOWED WITH VIEWS... Automatically throws an exception...
   * 
   * @param eskdata <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   * @exception DBException
   */
  @Override
  public void modifyElement(AbstractSKDCData eskdata) throws DBException
  {
    throw new DBException("Views are Read-Only, Cannot modify a view");
  }

  /**
   * Method to delete a row of data.
   * 
   * NOT ALLOWED WITH VIEWS... Automatically throws an exception...
   * 
   * @param eskdata <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   * @return number of rows deleted.
   * @exception DBException
   */
  @Override
  public void deleteElement(AbstractSKDCData eskdata) throws DBException
  {
    throw new DBException("Views are Read-Only, Cannot delete a view");
  }
}
