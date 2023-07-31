package com.daifukuamerica.wrxj.dbadapter;

import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Class to help support tables in other Wynsoft databases
 * 
 * NOTE: Don't go to the other databases directly as they may require another
 * connection to another server or may even move.  Use synonyms with a 
 * predictable prefix.
 * 
 * @author mandrus
 */
public class WynsoftDBInterface extends BaseDBInterface
{
  /**
   * Get the alias prefix for a foreign Wynsoft table synonym
   * 
   * @param isIdentityName
   * @return
   */
  protected static String getSchema(String isIdentityName)
  {
    return "S_" + isIdentityName + "_";
  }

  /**
   * Constructor
   * 
   * @param isIdentityName
   * @param isWriteTableName
   * @param isReadViewName
   * @param ipData
   */
  public WynsoftDBInterface(String isIdentityName, String isWriteTableName,
      String isReadViewName, AbstractSKDCData ipData)
  {
    super(getSchema(isIdentityName) + isWriteTableName,
        getSchema(isIdentityName) + isReadViewName, ipData);
  }
  
  /**
   * Method to modify a row of data. Normally used if there are multiple columns
   * that need to be changed.
   * 
   * <p>The WRx identity column usage conflicts with foreign schemas.</p>
   *
   * @param ipModData <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   * @exception DBException
   */
  @Override
  public void modifyElement(AbstractSKDCData ipModData) throws DBException
  {
    modifyData(ipModData.getColumnArray(), ipModData.getKeyArray(),
        getWriteTableName());
  }
}
