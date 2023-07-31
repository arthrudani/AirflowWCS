package com.daifukuamerica.wrxj.device.agv.messages;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.ACKFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.CANFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.CHGFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.ERRFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.HBTFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.HLDFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.LSSFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.MOVFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.MV2Formatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.NAKFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.PICFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.RESFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.RSUFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.XMTFormatter;

/**
 * Singleton Factory class that produces a message formatter object.
 * 
 * @author A.D.
 * @since  12-May-2009
 */
public class AGVMessageFormatterFactory
{
  private static HashMap<AGVMessageNameEnum,
  Class<? extends AbstractMessageFormatter>> mpFormatterMap = null;
  private static volatile AGVMessageFormatterFactory mpFactory = null;

  private static final ReentrantLock mpFormatLock = new ReentrantLock();

  private AGVMessageFormatterFactory()
  {
    // There must be only one instance of this class.
  }

  /**
   * Method to get a single instance of the AGV Message Formatter, and initialise
   * list of available formatters.
   * @return instance of AGVMessageFormatterFactory.
   */
  public static AGVMessageFormatterFactory getInstance()
  {
    if (mpFactory == null)
    {
      try
      {
        mpFormatLock.lock();
        if (mpFactory == null)
        {
          mpFactory = new AGVMessageFormatterFactory();

          mpFormatterMap = new HashMap<AGVMessageNameEnum, Class<? extends AbstractMessageFormatter>>(14);
          mpFormatterMap.put(AGVMessageNameEnum.ACK_REQUEST_RESPONSE, ACKFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.CAN_REQUEST, CANFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.CHG_REQUEST, CHGFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.ERR_REQUEST_RESPONSE, ERRFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.HBT_REQUEST, HBTFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.HLD_REQUEST, HLDFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.LSS_REQUEST_RESPONSE, LSSFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.MOV_REQUEST, MOVFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.MV2_REQUEST, MV2Formatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.NAK_REQUEST_RESPONSE, NAKFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.PIC_REQUEST, PICFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.RES_REQUEST, RESFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.RSU_REQUEST, RSUFormatter.class);
          mpFormatterMap.put(AGVMessageNameEnum.XMT_REQUEST, XMTFormatter.class);
        }
      }
      finally
      {
        mpFormatLock.unlock();
      }
    }

    return(mpFactory);
  }

  /**
   * Method to get the Formatter instance.
   * @param ipMessageName
   * @param ipInterface
   * @return
   * @throws AGVMessageFactoryException
   */
  public synchronized <Type extends AbstractMessageFormatter> Type getFormatter(
      AGVMessageNameEnum ipMessageName)
  throws AGVMessageFactoryException
  {
    Type vpFormatter = null;
    if (!mpFormatterMap.isEmpty())
    {
      if (mpFormatterMap.containsKey(ipMessageName))
      {
        Class<? extends AbstractMessageFormatter> vpClass =
          (mpFormatterMap.get(ipMessageName));
        try
        {
          vpFormatter = (Type)vpClass.newInstance();
        }
        catch(InstantiationException ex)
        {
          throw new AGVMessageFactoryException("Reflection error creating " +
              "Formatter object " + vpClass.getSimpleName(), ex);
        }
        catch(IllegalAccessException ex)
        {
          throw new AGVMessageFactoryException("Reflection security error " +
              "creating Formatter object " +
              vpClass.getSimpleName(), ex);
        }
      }
      else
      {
        throw new AGVMessageFactoryException("Unknown formatter object " +
            ipMessageName.getValue() + "! Object creation failed.");
      }
    }
    else
    {
      throw new AGVMessageFactoryException("Formatter objects have not been " +
      "initialised!  Factory retrieval failed...");
    }

    return(vpFormatter);
  }
}
