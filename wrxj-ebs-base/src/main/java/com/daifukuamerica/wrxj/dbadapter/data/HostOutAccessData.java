package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.HostOutAccessEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to handle Host Access Data table.
 * @author       A.D.  01-Jun-2005
 * @version      1.0
 */
public class HostOutAccessData extends AbstractSKDCData 
{
  public static final String ENABLED_NAME           = ENABLED.getName();          
  public static final String HOSTNAME_NAME          = HOSTNAME.getName();         
  public static final String MESSAGEIDENTIFIER_NAME = MESSAGEIDENTIFIER.getName();

  private String sHostName          = "";
  private String sMessageIdentifier = "";
  private int    iEnabled           = DBConstants.YES;
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();
  
  public HostOutAccessData()
  {
    super();
    initColumnMap(mpColumnMap, HostOutAccessEnum.class);
  }
  
 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    String s = "sHostName:" + sHostName +
               "\nsMessageIdentifier:" + sMessageIdentifier + "\n";
    try
    {
      s = s + "iEnabled:" + DBTrans.getStringValue(ENABLED_NAME, iEnabled) + "\n";
    }
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }
    s += super.toString();

    return(s);
  }

  /**
   * Defines equality between two HostOutAccessData objects.
   * 
   * @param absOH <code>AbstractSKDCData</code> reference whose runtime type
   *            is expected to be <code>OrderHeaderData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absOH)
  {
    HostOutAccessData ha = (HostOutAccessData)absOH;
    return(getHostName().equals(ha.getHostName()) &&
           getMessageIdentifier().equals(ha.getMessageIdentifier()));
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.
    sHostName          = "";
    sMessageIdentifier = "";
    iEnabled           = DBConstants.YES;
  }

/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches host name.
   * @return HostName value as string
   */
  public String getHostName()
  {
    return(sHostName);
  }

  /**
   * Fetches host Message Identifier.
   * @return MessageIdentifier as string
   */
  public String getMessageIdentifier()
  {
    return(sMessageIdentifier);
  }

  /**
   * Fetches Enabled flag.
   * @return iEnabled as int
   */
  public int getEnabled()
  {
    return(iEnabled);
  }
  
/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Host Name
   */
  public void setHostName(String isHostName)
  {
    sHostName = checkForNull(isHostName);
    addColumnObject(new ColumnObject(HOSTNAME_NAME, isHostName));
  }

  /**
   * Sets Message Identifier value.
   */
  public void setMessageIdentifier(String isMessageIdentifier)
  {
    sMessageIdentifier = checkForNull(isMessageIdentifier);
    addColumnObject(new ColumnObject(MESSAGEIDENTIFIER_NAME, isMessageIdentifier));
  }

  /**
   * Set Enabled
   * @param inEnabled
   */
  public void setEnabled(int inEnabled)
  {
    try
    {
      DBTrans.getStringValue("iEnabled", inEnabled);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inEnabled = DBConstants.YES;
    }
    iEnabled = inEnabled;
    addColumnObject(new ColumnObject(ENABLED_NAME, Integer.valueOf(inEnabled)));
  }
  
  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return super.setField(isColName, ipColValue);
    }

    switch((HostOutAccessEnum)vpEnum)
    {
      case ENABLED:
        setEnabled(((Integer)ipColValue).intValue());
        break;

      case HOSTNAME:
        setHostName((String)ipColValue);
        break;

      case MESSAGEIDENTIFIER:
        setMessageIdentifier((String)ipColValue);
    }
    
    return(0);
  }
}
