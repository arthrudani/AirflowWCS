package com.daifukuamerica.wrxj.host.messages.xml;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Description:<BR>
 *  Class to parse XML formatted Expected Receipt messages.
 *
 * @author       A.D.
 * @version      1.0   04/11/2005
 */
public class ExpectedReceiptParser extends DACXMLHandler implements MessageParser 
{
  private boolean                           mzHeaderProvided;
  protected boolean mzLineProvided;
  protected boolean                         mzExpectedLoad;
  protected List<PurchaseOrderLineData>     mpELList;
  protected StandardPoReceivingServer       mpPOServ;
  protected HashMap<String, AbstractSKDCData> mpDataObjectMap;
  protected AbstractSKDCData                mpDataObject;
  protected PurchaseOrderHeaderData         mpEHData;
  protected PurchaseOrderLineData           mpELData;
  boolean inExpectedDate, inExpirationDate;

 /**
  *  Default constructor.
  *  @throws DBException if there is a database connection error.
  */
  public ExpectedReceiptParser() throws DBException
  {
    super();
  }

 /**
  *  {@inheritDoc}
  * @param ipHIData {@inheritDoc}
  * @throws InvalidHostDataException {@inheritDoc}
  * @throws DBRuntimeException
  */
  @Override
  public void parse(HostToWrxData ipHIData) throws InvalidHostDataException, 
                                                   DBRuntimeException
  {
    mzExpectedLoad = ipHIData.getMessageIdentifier().equals("ExpectedLoadMessage");
    super.parse(ipHIData);
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
    InputSource vpInputSrc = null;
    InputStream vpInpStrm;
    
    if (mzExpectedLoad)
      vpInpStrm = getClass().getResourceAsStream("/hostconfig/ExpectedLoadMessage.dtd");
    else
      vpInpStrm = getClass().getResourceAsStream("/hostconfig/ExpectedReceiptMessage.dtd");
    
    if (vpInpStrm != null)
      vpInputSrc = new InputSource(vpInpStrm);
    else
      throw new SAXException("Expected Receipt Message DTD find error.");
      
    return(vpInputSrc);
  }
  
  @Override
  public void startDocument() throws SAXException
  {
    mpPCData.setLength(0);
    mpEHData = Factory.create(PurchaseOrderHeaderData.class);
    mpELData = Factory.create(PurchaseOrderLineData.class);
    mpELList = new LinkedList<PurchaseOrderLineData>();
    mpPOServ = Factory.create(StandardPoReceivingServer.class, "ExpectedReceiptParser");
    initDataMap();
  }
  
  @Override
  public void startElement(String isURI, String isLocalName, String isTagName,
                           Attributes ipAttributes) throws SAXException
  {
    String vsAttName = null, vsAttValue = null;
    try
    {
      for(int vnIdx = 0; vnIdx < ipAttributes.getLength(); vnIdx++)
      {
        vsAttName = ipAttributes.getQName(vnIdx);
        vsAttValue = ipAttributes.getValue(vnIdx);
        Object valueObject;
        if (DBTrans.isTranslation(vsAttName))
        {
          String newAttValue = vsAttValue.replaceAll("_", " ");
          valueObject = DBTrans.getIntegerObject(vsAttName, newAttValue);
          setAttributes(isTagName, vsAttName, valueObject);        
        }
        else if (vsAttName.equals("action"))
        {
          setCurrentAction(vsAttValue);
        }
        else                         // Must be an attribute that's not a
        {                            // translation.
          setAttributes(isTagName, vsAttName, vsAttValue);        
        }
      }
      setDataObject(isTagName);
    }
    catch(NoSuchFieldException nsf)
    {
      throw new SAXException("Invalid attribute \"" + vsAttName + "\"", nsf);
    }
  }

  @Override
  public void endElement(String isUri, String isLocalName, String isTagName)
         throws SAXException
  {
    String vsCurrentOrder = "";
    
    try
    {
      if (isTagName.equals(msRootElement))
      {
        return;
      }
      else if (isTagName.equals("ExpectedReceipt") ||
               isTagName.equals("ExpectedLoad"))
      {
        TransactionToken vpTok = null;
        try
        {
          vpTok = mpDBObj.startTransaction();
          switch(getCurrentAction())
          {
            case DBConstants.ADD:
              if (mpELList.isEmpty())
                throw new DBException("No order lines found for order add " + 
                                      "operation!!");
              mpEHData.setOrderStatus(DBConstants.EREXPECTED);
              
              if (mzExpectedLoad)
                mpPOServ.buildExpectedLoad(mpEHData, mpELList);
              else
                mpPOServ.buildPO(mpEHData, mpELList);
              break;
  
            case DBConstants.MODIFY:
              modifyExpectedReceipt();
              break;
  
            case DBConstants.DELETE:
              deleteExpectedReceipt();
              break;
          }
          mpDBObj.commitTransaction(vpTok);
        }
        finally
        {
          mpDBObj.endTransaction(vpTok);
                                       // Save off current order in case there
                                       // is an error.
          vsCurrentOrder = mpEHData.getOrderID();
          mpEHData.clear();
          mpELList.clear();
        }
      }
      else if (isTagName.equals("ExpectedReceiptLine") ||
               isTagName.equals("ExpectedLoadLine"))
      {
        if (mzExpectedLoad) mpELData.setExpectedQuantity(1.0);
        mpELList.add((PurchaseOrderLineData)mpELData.clone());
        resetPrimaryKeyData(isTagName);
      }
      else
      {                                // This is the PCData.
        MessageHelper.fillDataObject(mpDataObject, isTagName, mpPCData.toString());
      }
    }
    catch(NumberFormatException nfe)
    {
      InvalidHostDataException vpExc = new InvalidHostDataException(
                               "ExpectedReceiptMessage:: Error converting " +
                               "numeric value for " + isTagName, nfe);
      vpExc.setOriginalSequence(mpHIData.getOriginalMessageSequence());
      throw new SAXException(vpExc);
    }
    catch(InvalidHostDataException pe)
    {
      String newErrorMessage = "ExpectedReceiptMessage:: Error in " + 
                               isTagName + pe.getErrorMessage();
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
        invDataExc.setErrorMessage("ExpectedReceiptMessage:: Error executing " +
                                   "DB operation for order " + vsCurrentOrder +
                                   " Original Host Sequence Number = " +
                                   mpHIData.getOriginalMessageSequence());
        throw new SAXException(invDataExc);
      }
      else
      {
        invDataExc.setErrorCode(HostError.DUPLICATE_DATA);
        invDataExc.setErrorMessage("Host Sequence number: " + 
                                   mpHIData.getOriginalMessageSequence() + 
                                   "Order id . = " + vsCurrentOrder);
        throw new SAXException(invDataExc);
      }
    }
    catch(NoSuchElementException nse)
    {
      throw new SAXException("ExpectedReceiptMessage:: Requested change to " + 
                             "Order record for " + vsCurrentOrder + 
                             " did not work!", nse);                      
    }
    finally
    {
      mpPCData.setLength(0);
    }
  }
  
  protected void setAttributes(String isTagName, String isAttName, Object ipValueObj)
  {
    String vsUpperAttName = isAttName.toUpperCase();
                                       // Note: XML is case sensitive, and so we
                                       // must be too!
    if (isTagName.equals("ExpectedReceipt") || isTagName.equals("ExpectedLoad"))
    {
      if (isAttName.equals("sOrderID"))
      {
        mpELData.setField(vsUpperAttName, ipValueObj);
      }
      mpEHData.setField(vsUpperAttName, ipValueObj);
    }
    else if (isTagName.equals("ExpectedReceiptLine") ||
             isTagName.equals("ExpectedLoadLine"))
    {
      mpELData.setField(vsUpperAttName, ipValueObj);
    }
  }
  
  protected void resetPrimaryKeyData(String tagName)
  {
    if (tagName.equals("ExpectedReceiptLine") ||
        tagName.equals("ExpectedLoadLine"))
    {
      mpELData.clear();
      mpELData.setField(PurchaseOrderHeaderData.ORDERID_NAME, mpEHData.getOrderID());
    }
  }
  
 /**
  * Method modifies an expected receipt header and any accompanying lines.
  * @throws DBException when there is a modify error.
  */
  protected void modifyExpectedReceipt() throws DBException
  {
    if (mzHeaderProvided)
    {
      mpPOServ.modifyPOHead(mpEHData);
    }
    
    if (mzLineProvided && !mpELList.isEmpty())
    {
      int elLength = mpELList.size();
      for(int idx = 0; idx < elLength; idx++)
      {
        mpELData = mpELList.get(idx);
        if (mpPOServ.exists(mpELData.getOrderID(), mpELData.getItem(),
                            mpELData.getLot(), mpELData.getLineID()))
        {
          mpELData.setKey(PurchaseOrderLineData.ORDERID_NAME, mpELData.getOrderID());
          mpELData.setKey(PurchaseOrderLineData.ITEM_NAME, mpELData.getItem());
          mpELData.setKey(PurchaseOrderLineData.LOT_NAME, mpELData.getLot());
          if (mpELData.getLineID().trim().length() != 0)
            mpELData.setKey(PurchaseOrderLineData.LINEID_NAME, mpELData.getLineID());
          mpPOServ.modifyPOLine(mpELData);
        }
        else
        {
          mpPOServ.addPOLine(mpELData);
        }
      }
    }
  }

 /**
  * Method will delete an Expected Receipt or the line items only.  If they provide
  * an Expected Receipt Header, then the expected receipt as a whole will be deleted.
  * If they only provide an Expected Receipt Line, then that line item will be
  * deleted. 
  * @throws DBException if there is a database exception.
  */
  protected void deleteExpectedReceipt() throws DBException
  {
    if (mzLineProvided && !mpELList.isEmpty())
    {
      int elLength = mpELList.size();
      for(int idx = 0; idx < elLength; idx++)
      {
        mpELData = mpELList.get(idx);
        mpPOServ.deletePOLine(mpELData.getOrderID(), mpELData.getItem(),
                              mpELData.getLot(), mpELData.getLineID());
      }
    }
    else
    {
      mpPOServ.deletePO(mpEHData.getOrderID());
    }
  }
  
  private void setDataObject(String isDataObjectName)
  {
    if (mpDataObjectMap.containsKey(isDataObjectName))
    {
      AbstractSKDCData myDataObject = mpDataObjectMap.get(isDataObjectName);
      if (mpDataObject != myDataObject)
      {
        mpDataObject = myDataObject;
      }
      mzHeaderProvided = (isDataObjectName.equals("ExpectedReceiptHeader") || 
                          isDataObjectName.equals("ExpectedLoadHeader"));
      mzLineProvided = (isDataObjectName.equals("ExpectedReceiptLine") || 
                        isDataObjectName.equals("ExpectedLoadLine"));
    }
  }
  
  protected void initDataMap()
  {
    mpDataObjectMap = new HashMap<String, AbstractSKDCData>();
    mpDataObjectMap.put("ExpectedReceiptHeader", mpEHData);
    mpDataObjectMap.put("ExpectedLoadHeader", mpEHData);
    mpDataObjectMap.put("ExpectedReceiptLine", mpELData);
    mpDataObjectMap.put("ExpectedLoadLine", mpELData);
  }

 /**
  * {@inheritDoc}
  */
  @Override
  public void cleanUp()
  {
    if (mpPOServ != null)
    {
      mpPOServ.cleanUp();
      mpPOServ = null;
    }
  }
}
