package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.factory.FactoryException;
import com.daifukuamerica.wrxj.host.MessageNameEnum;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;


/* Description:<BR>
 *  Factory to retrieve correct host message object.
 *
 * @author       A.D.
 * @version      1.0     02/19/05
 */
public class MessageOutFactory
{
  public MessageOutFactory()
  {
    // Don't let them instantiate this class directly
  }

 /**
  *  Gets a message Object based on passed in class name.  If this factory is
  *  used, code will be extensible for projects with minimal change.  We
  *  simply define a project specific class string in the HostConfig table and
  *  this factory will automatically find it.
  *  @param ipMessageEnum Any of the following outbound enumeration name constants:
  *  <i><ul><li>EXPECTED_RECEIPT_COMPLETE</li>
  *         <li>HOST_ERROR</li>
  *         <li>LOAD_ARRIVAL</li>
  *         <li>INVENTORY_ADJUST</li>
  *         <li>INVENTORY_STATUS</li>
  *         <li>INVENTORY_UPLOAD</li>
  *         <li>ORDER_COMPLETE</li>
  *         <li>ORDER_STATUS</li>
  *         <li>STORE_COMPLETE</li>
  *         <li>SHIP_COMPLETE</li>
  *         <li>PICK_COMPLETE</li>
  *     </ul>
  *  </i>
  * 
  * @return {@link MessageOut} object if found or else a <code>null</code> object.
  */
  public static synchronized <Type extends MessageOut> Type getInstance(MessageNameEnum ipMessageEnum)
         throws DBRuntimeException
  {
    String vsMessageClassName = ipMessageEnum.getValue();
    Type vpOutObject = null;
    
    HostConfig vpHostCfg = Factory.create(HostConfig.class);

    Class<? extends MessageOut> vpClassMetaData = null;      
    try
    {
      String vsClassName = vpHostCfg.getMessageOutClassName(vsMessageClassName);
      if (vsClassName.trim().length() != 0)
      {
        vpClassMetaData = Class.forName(vsClassName).asSubclass(MessageOut.class);
        vpOutObject = (Type)vpClassMetaData.newInstance();
      }
      else
      {
        throw new DBException("Outbound message name not found in HostConfig " +
                              "Table.  Message object not created!");
      }
    }
    catch(DBException e)
    {
      throw new DBRuntimeException("Failed to initialise " + ipMessageEnum.getValue() +
                                   " class in MessageOutFactory!", e);
    }
    catch(ClassNotFoundException e)
    {
      throw new DBRuntimeException("Failed to initialise " + ipMessageEnum.getValue() +
                                   " class in MessageOutFactory!", e);
    }
    catch(FactoryException fe)
    {
      throw new DBRuntimeException("Failed to initialise " + ipMessageEnum.getValue() +
                                   " class in MessageOutFactory!", fe);
    }
    catch (InstantiationException e)
    {
      throw new DBRuntimeException("Failed to initialise " + ipMessageEnum.getValue() +
                                   " class in MessageOutFactory!", e);
    }
    catch (IllegalAccessException e)
    {
      throw new DBRuntimeException("Failed to initialise " + ipMessageEnum.getValue() +
                                   " class in MessageOutFactory!", e);
    }
    

    return(vpOutObject);
  }
}
