package com.daifukuamerica.wrxj.host.messages.xml;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.io.InputStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Description:<BR>
 *  Class to parse XML formatted InventoryHold messages.  This message is
 *  unique in that there is no corresponding table to add this data to.  As a
 *  result, the message will be responded to by this object.
 *
 * @author       A.D.
 * @version      1.0   03/28/2005
 */
public class InventoryHoldParser extends DACXMLHandler implements MessageParser 
{
  private LoadLineItemData         mpIDData;
  private StandardInventoryServer  mpInvtServ;

 /**
  *  Default constructor.
  * @throws DBException if there is a database connection error.
  */
  public InventoryHoldParser() throws DBException
  {
    super();
  }
  
 /**
  * {@inheritDoc} This method allows for dynamically changing which DTD
  * handler the parser will use.  This is particularly useful when we don't want
  * the producer of this XML message to know anything about what type of validations
  * occur on this side.
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
    InputSource vpInputSource = null;
    InputStream vpInpStrm = getClass().getResourceAsStream("/hostconfig/InventoryHoldMessage.dtd");
    if (vpInpStrm != null)
      vpInputSource = new InputSource(vpInpStrm);
    else
      throw new SAXException("Inventory Hold Message DTD find error.");

    return(vpInputSource);
  }
  
  @Override
  public void startDocument() throws SAXException
  {
    mpPCData.setLength(0);
    mpIDData = Factory.create(LoadLineItemData.class);
    mpInvtServ = Factory.create(StandardInventoryServer.class);
  }
  
 /**
  *  {@inheritDoc}  This is the method that carries out all database transactions
  *  for a message since this signifies the end of an Inventory Request Message.
  *  @param isURI {@inheritDoc}
  *  @param isLocalName {@inheritDoc}
  *  @param isTagName {@inheritDoc}
  *  @throws SAXException Parser Exception
  */
  @Override
  public void endElement(String isURI, String isLocalName, String isTagName)
         throws SAXException
  {
    try
    {
      if (isTagName.equals(msRootElement))
      {
        return;
      }
      else if (isTagName.equalsIgnoreCase("InventoryHold"))
      {
        int ithold;
        ithold = (mpIDData.getHoldReason().trim().length() == 0) ? DBConstants.ITMAVAIL
                                                               : DBConstants.ITMHOLD;
        mpInvtServ.setItemHoldValue(mpIDData.getItem(), mpIDData.getLot(),
                                    mpIDData.getHoldReason(), ithold);
      }
      else
      {                                // These are the PCDATA fields.
        MessageHelper.fillDataObject(mpIDData, isTagName, mpPCData.toString());
      }
    }
    catch(NumberFormatException nfe)
    {
      throw new SAXException("Error converting numeric value for " + isTagName, nfe);
    }
    catch(InvalidHostDataException pe)
    {
      String newErrorMessage = "InventoryHoldMessage:: Error in " + isTagName + pe.getErrorMessage();
      pe.setErrorMessage(newErrorMessage);
      throw new SAXException(pe);
    }
    catch(DBException exc)
    {
      InvalidHostDataException invDataExc = new InvalidHostDataException(exc);
      if (!exc.isDuplicate())
      {
        invDataExc.setErrorMessage("InventoryHoldMessage:: Error executing DB " +
                                   "operation for item " + mpIDData.getItem());
        throw new SAXException(invDataExc);
      }
      else
      {
        invDataExc.setErrorCode(HostError.DUPLICATE_DATA);
        invDataExc.setErrorMessage("Host Sequence number: " + mpHIData.getOriginalMessageSequence() + 
                                   "item . = " + mpIDData.getItem());
        throw new SAXException(invDataExc);
      }
    }
    finally
    {
      mpPCData.setLength(0);
    }
  }
  
 /**
  * {@inheritDoc}
  */
  @Override
  public void cleanUp()
  {
    if (mpInvtServ != null)
    {
      mpInvtServ.cleanUp();
      mpInvtServ = null;
    }
  }
}
