package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* Description:<BR>
 *  Factory for producing a Wrx-J message parser.
 *
 * @author       A.D.
 * @version      1.0     02/09/05
 */
public class MessageParserFactory
{
  private static HostConfig mpHostCfg;
  private static Map<String, Class<? extends MessageParser>> mpParserCache =
                      new ConcurrentHashMap<String, Class<? extends MessageParser>>();
  
  private MessageParserFactory()
  {
    // Don't let them instantiate this class directly
  }

 /**
   * Gets message parser based on a message identifier.
   * @param isControllerName the name of the Controller calling this Parser.  <i>This
   *        must be the same as what is specified in the HostConfig. table.</i>
   * @param isMessageIdentifier String of any one of the following:
   * <i><ul><li>OrderMessage (XML based)</li>
   * <li>OrderHeader (Delimiter based)</li>
   * <li>OrderLine  (Delimiter based)</li>
   * <li>ExpectedReceiptMessage (XML based)</li>
   * <li>ExpectedReceiptHeader (Delimiter based)</li>
   * <li>ExpectedReceiptLine (Delimiter based)</li>
   * <li>ItemMessage (XML based)</li>
   * <li>ItemMaster (Delimiter based)</li>
   * <li>InventoryRequestMessage (XML based)</li>
   * <li>InventoryRequest (Delimiter based)</li>
   * </ul>
   * </i>
   * 
   * @return MessageParser object if found or else a <code>null</code> object.
   * @throws DBRuntimeException if a Parser can't be found or created.
   */
  public static synchronized MessageParser getParser(String isControllerName, String isMessageIdentifier)
         throws DBRuntimeException
  {
    MessageParser vpMesgParserObject = null;
    
    if (mpParserCache.containsKey(isMessageIdentifier))
    {
      Class<? extends MessageParser> vpClass = mpParserCache.get(isMessageIdentifier);
      try
      {
        vpMesgParserObject = vpClass.newInstance();
      }
      catch(InstantiationException ex)
      {
        throw new DBRuntimeException("Reflection error creating " +
                           "Parser object " + vpClass.getSimpleName(), ex);
      }
      catch(IllegalAccessException ex)
      {
        throw new DBRuntimeException("Reflection security error " +
                                             "creating Parser object " +
                                             vpClass.getSimpleName(), ex);
      }
    }
    else                               // Get it from the Host Config. table.
    {
      vpMesgParserObject = MessageParserFactory.create(isControllerName, isMessageIdentifier);
    }
    
    return(vpMesgParserObject);
  }

 /**
  * Method creates a Parser Object by using the information in the HostConfig.
  * table.
  * @param isControllerName the name of the Controller calling this Parser.  <i>This
  *        must be the same as what is specified in the HostConfig. table.</i>
  * @param isMessageIdentifier String of any one of the following:
  * @return MessageParser object.
  * @throws DBRuntimeException if a Parser can't be found or created.
  */
  private static MessageParser create(String isControllerName, String isMessageIdentifier)
          throws DBRuntimeException
  {
    MessageParser vpMesgParserObject = null;
    Logger       vpErrorLog = Logger.getLogger();

    try
    {
      mpHostCfg = Factory.create(HostConfig.class);
      String vsClassName = mpHostCfg.getParserClassName(isControllerName, isMessageIdentifier);
      if (vsClassName == null || vsClassName.trim().length() == 0)
      {
        throw new ClassNotFoundException(isMessageIdentifier + " not found " +
                                         "in configuration! Invalid " +
                                         "identifier found in message...");
      }
      Class<? extends MessageParser> vpClassMetaData = Class.forName(vsClassName)
                                                            .asSubclass(MessageParser.class);
      vpMesgParserObject = vpClassMetaData.newInstance();
      mpParserCache.put(isMessageIdentifier, vpClassMetaData);
    }
    catch(DBException exc)
    {
      vpErrorLog.logException(exc, "HostConfig error trying to instantiate " +
                              "parser... ");
      throw new DBRuntimeException("DB Error with HostConfig trying to instantiate " +
                                   "parser " + isMessageIdentifier +
                                   " in MessageParserFactory!", exc);
    }
    catch(ClassNotFoundException e)
    {
      throw new DBRuntimeException("Failed to initialise MessageParser class " +
                                   isMessageIdentifier + " in MessageParserFactory!", e);
    }
    catch(InstantiationException e)
    {
      throw new DBRuntimeException("Failed to build MessageParser " + isMessageIdentifier +
                                   " in MessageParserFactory!", e);
    }
    catch(IllegalAccessException e)
    {
      throw new DBRuntimeException("Failed to build MessageParser " + isMessageIdentifier +
                                   " in MessageParserFactory!", e);
    }
    
    return(vpMesgParserObject);
  }
}

