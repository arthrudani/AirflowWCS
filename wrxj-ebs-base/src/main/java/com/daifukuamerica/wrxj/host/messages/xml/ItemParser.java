package com.daifukuamerica.wrxj.host.messages.xml;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.io.InputStream;
import java.util.NoSuchElementException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Description:<BR>
 *  Class to parse XML formatted Item messages.
 *
 * @author       A.D.
 * @version      1.0   25-Mar-2005
 */
public class ItemParser extends DACXMLHandler implements MessageParser
{
  protected ItemMasterData          mpIMData;
  protected StandardInventoryServer mpInvServ;

  public ItemParser() throws DBException
  {
    super();
  }

 /**
  * {@inheritDoc} This method allows for dynamically changing which DTD
  * handler the parser will use.  This is particularly useful when we don't want
  * the producer of this XML message to know anything about what type of validations
  * occur on this side. <b>Note: </b> by the time this method is called the wrxj.dtd
  * reference in the DOCTYPE SYSTEM parameter is guaranteed to be set.
  * 
  * @param isPublicID {@inheritDoc} This parameter is not used in this implementation
  *        since it is not specified in the default DOCTYPE entity in use here.
  * @param isSystemID {@inheritDoc} This identifier will always be set to the
  *        default of "wrxj.dtd" when the message is retrieved from the inbound
  *        data queue (HostToWrx table for most implementations).  This makes
  *        this object (which is the only one that really cares) responsible for
  *        pointing to the real and correct DTD to use for parsing.
  * @return {@inheritDoc}
  */
  @Override
  public InputSource resolveEntity(String isPublicID, String isSystemID)
         throws SAXException
  {
    InputSource vpInputSrc = null;
    InputStream vpInpStrm = getClass().getResourceAsStream("/hostconfig/ItemMessage.dtd");
    if (vpInpStrm != null)
      vpInputSrc = new InputSource(vpInpStrm);
    else
      throw new SAXException("ItemMessage.dtd file not found. Parsing stopped.");

    return(vpInputSrc);
  }

  @Override
  public void startDocument() throws SAXException
  {
    mpPCData.setLength(0);
    mpIMData = Factory.create(ItemMasterData.class);
    mpInvServ = Factory.create(StandardInventoryServer.class, "ItemParser");
  }
  
  @Override
  public void startElement(String uri, String localName, String tagName,
                           Attributes attributes) throws SAXException
  {
    String attName = null, attValue = null;
    try
    {
      for(int idx = 0; idx < attributes.getLength(); idx++)
      {
        attName = attributes.getQName(idx);
        attValue = attributes.getValue(idx);
        Object valueObject;
        if (DBTrans.isTranslation(attName))
        {
            String newAttValue = attValue.replaceAll("_", " ");
            valueObject = DBTrans.getIntegerObject(attName, newAttValue);
            mpIMData.setField(attName.toUpperCase(), valueObject);
        }
        else if (attName.equals("action"))
        {
          setCurrentAction(attValue);
        }
        else                         // Must be an attribute that's not a
        {                            // translation.
          mpIMData.setField(attName.toUpperCase(), attValue);
        }
      }
    }
    catch(NoSuchFieldException nsf)
    {
      throw new SAXException("Invalid attribute \"" + attName + "\"", nsf);
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String tagName)
         throws SAXException
  {
    try
    {
      if (tagName.equals(msRootElement))
      {
        return;
      }
      else if (tagName.equalsIgnoreCase("ItemMaster"))
      {
        switch(getCurrentAction())
        {
          case DBConstants.ADD:
            mpInvServ.addItemMaster(mpIMData);
            break;

          case DBConstants.MODIFY:
            if (mpIMData.getItem().trim().length() == 0)
              throw new DBException("Item field must be filled in!  Item modify failed...");
              
            mpIMData.setKey(ItemMasterData.ITEM_NAME, mpIMData.getItem());
            mpInvServ.updateItemInfo(mpIMData);
            break;

          case DBConstants.DELETE:
            mpInvServ.deleteItemMaster(mpIMData.getItem());
            break;
        }
        mpIMData.clear();
      }
      else
      {
        MessageHelper.fillDataObject(mpIMData, tagName, mpPCData.toString());
      }
    }
    catch(NumberFormatException nfe)
    {
      InvalidHostDataException vpNFE = new InvalidHostDataException(
            "ItemMessage:: Error converting numeric value for " + tagName, nfe);
      vpNFE.setOriginalSequence(mpHIData.getOriginalMessageSequence());
      throw new SAXException(vpNFE);
    }
    catch(InvalidHostDataException pe)
    {
      String newErrorMessage = "ItemMessage:: Error in " + tagName + pe.getErrorMessage();
      pe.setOriginalSequence(mpHIData.getOriginalMessageSequence());
      pe.setErrorMessage(newErrorMessage);
      throw new SAXException(pe);
    }
    catch(DBException exc)
    {
      InvalidHostDataException invDataExc = new InvalidHostDataException(exc);
      invDataExc.setOriginalSequence(mpHIData.getOriginalMessageSequence());
      if (!exc.isDuplicate())
      {
        invDataExc.setErrorMessage("ItemMessage:: Error executing DB " +
                                   "operation for Item " + mpIMData.getItem());
        throw new SAXException(invDataExc);
      }
      else
      {
        invDataExc.setErrorCode(HostError.DUPLICATE_DATA);
        invDataExc.setErrorMessage("Item id . = " + mpIMData.getItem());
        throw new SAXException(invDataExc);
      }
    }
    catch(NoSuchElementException nse)
    {
      mpLogger.logDebug("ItemMessage:: Requested change to Item record for " +
                      mpIMData.getItem() + " did not work!");
      InvalidHostDataException invDataExc = new InvalidHostDataException(nse);
      invDataExc.setOriginalSequence(mpHIData.getOriginalMessageSequence());
      invDataExc.setErrorMessage("ItemMessage:: Requested change to Item record for " +
                                  mpIMData.getItem() + " did not work!");
      throw new SAXException(invDataExc);
    }
    finally
    {
      mpPCData.setLength(0);
    }
  }
  
 /**
  * {@inheritDoc}
  */
  public void cleanUp()
  {
    if (mpInvServ != null)
    {
      mpInvServ.cleanUp();
      mpInvServ = null;
    }
  }
}
