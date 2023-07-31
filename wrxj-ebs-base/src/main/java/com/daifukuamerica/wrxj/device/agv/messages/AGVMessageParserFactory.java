package com.daifukuamerica.wrxj.device.agv.messages;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.ACKParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.ALMParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.ENDParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.ERRParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.LALParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.LPCParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.LSSParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.MABParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.MRCParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.NAKParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.QMRParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.SSRParserImpl;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.VSRParserImpl;

/**
 * Singleton class for creating a parser factory.
 * @author A.D.
 * @since
 */
public class AGVMessageParserFactory
{
  private static final ReentrantLock mpParseLock = new ReentrantLock();
  private static HashMap<AGVMessageNameEnum,
  Class<? extends AbstractMessageParser>> mpParserMap = null;
  private static volatile AGVMessageParserFactory mpFactory = null;

  private AGVMessageParserFactory()
  {
    // There must be only one instance of this class.
  }

  /**
   * Method to get a single instance of the AGV Message Parser, and initialise
   * list of available parsers.
   * @return instance of AGVMessageParserFactory.
   */
  public static AGVMessageParserFactory getInstance()
  {
    if (mpFactory == null)
    {
      try
      {
        mpParseLock.lock();
        if (mpFactory == null)
        {
          mpParserMap = new HashMap<AGVMessageNameEnum, Class<? extends AbstractMessageParser>>(11);
          mpParserMap.put(AGVMessageNameEnum.ACK_REQUEST_RESPONSE, ACKParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.ALM_RESPONSE, ALMParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.NAK_REQUEST_RESPONSE, NAKParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.END_RESPONSE, ENDParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.ERR_REQUEST_RESPONSE, ERRParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.LAL_RESPONSE, LALParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.LPC_RESPONSE, LPCParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.LSS_REQUEST_RESPONSE, LSSParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.MAB_RESPONSE, MABParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.MRC_RESPONSE, MRCParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.QMR_RESPONSE, QMRParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.SSR_RESPONSE, SSRParserImpl.class);
          mpParserMap.put(AGVMessageNameEnum.VSR_RESPONSE, VSRParserImpl.class);
          mpFactory = new AGVMessageParserFactory();
        }
      }
      finally
      {
        mpParseLock.unlock();
      }
    }

    return(mpFactory);
  }

  /**
   * Method to get the Parser instance.
   * @param isMessage message received from CMS.
   * @return reference to {@link AbstractMessageParser AbstractMessageParser} object.
   * @throws AGVMessageFactoryException if there is an error retrieving a parser.
   */
  public synchronized AbstractMessageParser getParser(String isMessage)
  throws AGVMessageFactoryException
  {
    String vsMessageID;
    try
    {
      int vnStart = AGVMessageConstants.MESSAGE_LENGTH_LEN +
      AGVMessageConstants.SERIAL_NUMBER_LEN;
      int vnEnd = vnStart + AGVMessageConstants.MESSAGEID_LEN;
      vsMessageID = isMessage.substring(vnStart, vnEnd);
    }
    catch(StringIndexOutOfBoundsException iob)
    {
      throw new AGVMessageFactoryException("Error getting Message Identifier " +
      "from message!");
    }
    return(getParser(AGVMessageNameEnum.getEnumObject(vsMessageID)));
  }

  /**
   * Method to get the Parser instance.
   * @param ipMessageName Parser Name.
   * @return Message Parser instance.
   * @throws AGVMessageFactoryException
   */
  private AbstractMessageParser getParser(AGVMessageNameEnum ipMessageName)
  throws AGVMessageFactoryException
  {
    if (ipMessageName == null)
    {
      throw new AGVMessageFactoryException("Parser name not found! " +
      "Factory retrieval failed...");
    }

    AbstractMessageParser vpParser = null;
    if (!mpParserMap.isEmpty())
    {
      if (mpParserMap.containsKey(ipMessageName))
      {
        Class<? extends AbstractMessageParser> vpClass = mpParserMap.get(ipMessageName);
        try
        {
          vpParser = vpClass.newInstance();
        }
        catch(InstantiationException ex)
        {
          throw new AGVMessageFactoryException("Reflection error creating " +
              "Parser object " + vpClass.getSimpleName(), ex);
        }
        catch(IllegalAccessException ex)
        {
          throw new AGVMessageFactoryException("Reflection security error " +
              "creating Parser object " +
              vpClass.getSimpleName(), ex);
        }
      }
      else
      {
        throw new AGVMessageFactoryException("Unknown Parser object " +
            ipMessageName.getValue() + "! Object creation failed.");
      }
    }
    else
    {
      throw new AGVMessageFactoryException("Parser objects have not been " +
      "initialised!  Factory retrieval failed...");
    }

    return(vpParser);
  }
}
