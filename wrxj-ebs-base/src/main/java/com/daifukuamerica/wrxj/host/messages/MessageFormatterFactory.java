package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.factory.FactoryException;
import com.daifukuamerica.wrxj.host.messages.delimited.DelimitedFormatter;
import com.daifukuamerica.wrxj.host.messages.fixedlength.FixedLengthFormatter;
import com.daifukuamerica.wrxj.host.messages.xml.XMLFormatter;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.log.Logger;


/**
 *  Class to create a formatter to be used in Host Message formatting.
 *  
 *  @author       A.D.  10-Feb-05
 *  @version      1.0     
 */
public class MessageFormatterFactory
{
  private static HostConfig mpHostCfg = null;
  private static String msFormatterClassName;
  private static Logger mpLog;
  static
  {
    mpLog = Logger.getLogger();
    mpHostCfg = Factory.create(HostConfig.class);
    try
    {
      msFormatterClassName = mpHostCfg.getFormatterClassName();
    }
    catch(DBException e)
    {
      mpLog.logError("Error determining expected Host data format.... " +
                      "Database access error for HostConfig table to read " +
                      "\"DataType\"" + e.getMessage());
    }
  }

  private MessageFormatterFactory()
  {
    // Don't let them instantiate this class.
  }

  /**
   * Gets a message formatter object.
   * 
   * @return {@link MessageFormatter}
   * @throws DBRuntimeException if something bad happens
   */
  public static MessageFormatter getInstance() throws DBRuntimeException
  {
    Class vpClassMetaData = null;
    try
    {
      // See if there is a customized one defined in the database.
      if (msFormatterClassName.trim().length() != 0)
      {
        vpClassMetaData = Class.forName(msFormatterClassName);
      }
      else
      {
        // Try to get the standard formatter class object.
        vpClassMetaData = getStandardFormatterClassObject();
        if (vpClassMetaData == null)
          throw new DBRuntimeException("Standard Formatter Class not defined "
              + "or unknown format defined.!");
      }
      
      // Following will try to find a customized class using Factory.properties.
      // If none found, it looks in the standard class path.
      return (MessageFormatter)Factory.create(vpClassMetaData);
    }
    catch (ClassNotFoundException e)
    {
      throw new DBRuntimeException("Unable to create MessageFormatter in "
          + "MessageFormatterFactory!", e);
    }
    catch (FactoryException e)
    {
      throw new DBRuntimeException("Unable to create MessageFormatter in "
          + "MessageFormatterFactory!", e);
    }
    catch (NullPointerException npe)
    {
      throw new DBRuntimeException("Unable to create MessageFormatter in "
          + "MessageFormatterFactory!", npe);
    }
  }
  
  private static Class getStandardFormatterClassObject()
  {
    Class vpClass = null;
    switch(Application.getInt(HostConfigData.ACTIVE_DATA_TYPE))
    {
      case DBConstants.XML:
        vpClass = XMLFormatter.class;
        break;

      case DBConstants.DELIMITED:
        vpClass = DelimitedFormatter.class;
        break;

      case DBConstants.FIXEDLENGTH:
        vpClass = FixedLengthFormatter.class;
    }

    return(vpClass);
  }
}
