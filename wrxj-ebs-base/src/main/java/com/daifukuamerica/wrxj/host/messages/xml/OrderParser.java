package com.daifukuamerica.wrxj.host.messages.xml;

import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.jdbc.AmountFullTransMapper;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Description:<BR>
 *  Class to parse XML formatted Order messages.
 *
 * @author       A.D.
 * @version      1.0   04/04/2005
 */
public class OrderParser extends DACXMLHandler implements MessageParser 
{
  protected boolean               mzLoadOrder;
  protected boolean               mzEmptyContOrder;
  protected boolean             mzHeaderProvided, mzLineProvided;
  protected AbstractSKDCData    mpDataObject;
  protected OrderHeaderData     mpOHData;
  protected OrderLineData       mpOLData;
  protected List<Object>        mpOLList;
  protected StandardOrderServer mpOrderServer;
  private HashMap<String, AbstractSKDCData>   mpDataObjectMap;
  protected Map<String, AmountFullTransMapper>  mpPartialQtyMap;

 /**
  *  Default constructor.
  * @throws DBException if there is a database connection error.
  */
  public OrderParser() throws DBException
  {
    super();
  }

 /**
  * {@inheritDoc} This implementation sets up and calls a SAX parser.
  * @param ipHIData Data class containing the message received from host.
  * @throws InvalidHostDataException if the Host should be notified of the problem.
  * @throws DBRuntimeException if the exception is an internal exception.
  */
  @Override
  public void parse(HostToWrxData ipHIData) throws InvalidHostDataException,
                                                   DBRuntimeException
  {
    mzLoadOrder = ipHIData.getMessageIdentifier().equals("OrderLoadMessage");
    mzEmptyContOrder = ipHIData.getMessageIdentifier().equals("OrderEmptyMessage");
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
    
    if (mzLoadOrder)
      vpInpStrm = getClass().getResourceAsStream("/hostconfig/OrderLoadMessage.dtd");
    else if (mzEmptyContOrder)
      vpInpStrm = getClass().getResourceAsStream("/hostconfig/OrderEmptyMessage.dtd");
    else
      vpInpStrm = getClass().getResourceAsStream("/hostconfig/OrderItemMessage.dtd");
    
    if (vpInpStrm != null)
      vpInputSrc = new InputSource(vpInpStrm);
    else
      throw new SAXException("Order Message DTD file not found. Parsing stopped.");

    return(vpInputSrc);
  }
  
  @Override
  public void startDocument() throws SAXException
  {
    mpPCData.setLength(0);
    mpOHData = Factory.create(OrderHeaderData.class);
    mpOLData = Factory.create(OrderLineData.class);
    mpOLList = new LinkedList<Object>();
    mpOrderServer = Factory.create(StandardOrderServer.class, "OrderParser");
    mpPartialQtyMap = LoadData.getAmountFullDecimalMap();
    initDataMap();
  }
  
  @Override
  public void startElement(String isURI, String isLocalName, String isTagName,
                           Attributes ipAttributes) throws SAXException
  {
    String vsAttName = null, vsAttValue = null;
    for(int idx = 0; idx < ipAttributes.getLength(); idx++)
    {
      vsAttName = ipAttributes.getQName(idx);
      vsAttValue = ipAttributes.getValue(idx);
      if (vsAttName.equals("action"))
      {
        setCurrentAction(vsAttValue);
      }
      else
      {
        setAttributes(isTagName, vsAttName, vsAttValue);
      }
    }
    setDataObject(isTagName);
  }

 /**
  * {@inheritDoc} <b>Implementation</b> This is the main transactional method
  * for Order messages from the host.
  * @param isURI
  * @param isLocalName
  * @param isTagName 
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
      else if (isTagName.equals("Order"))
      {
        TransactionToken vpTok = null;
        try
        {
          vpTok = mpDBObj.startTransaction();
          switch(getCurrentAction())
          {
            case DBConstants.ADD:
              addOrder();
              mpOrderServer.setHostOrderStatus(mpOHData.getOrderID());
              break;
  
            case DBConstants.MODIFY:
              modifyOrder();
              break;
  
            case DBConstants.DELETE:
              deleteOrder();
              break;
          }
          mpDBObj.commitTransaction(vpTok);
        }
        finally
        {
          mpDBObj.endTransaction(vpTok);
          mpOHData.clear();
          mpOLList.clear();
        }
      }
      else if (isTagName.equals("OrderLine"))
      {
        if (mzLoadOrder)
          mpOLData.setOrderQuantity(1);
          
        mpOLList.add(mpOLData.clone());
        resetPrimaryKeyData(isTagName);
      }
      else
      {                                // This is the mpPCData.
        if (isTagName.equals("fOrderQuantity"))
        {
          String vsOrdQty = mpPCData.toString().trim();
          if (!isOrderQuantityValid(Double.parseDouble(vsOrdQty)))
          {
            throw new DBException("Order " + mpOHData.getOrderID() + " has " +
                                  "invalid Order Line Quantity of " + vsOrdQty);
          }
        }
        MessageHelper.fillDataObject(mpDataObject, isTagName, mpPCData.toString());
      }
    }
    catch(NumberFormatException nfe)
    {
      String vsErr = "OrderMessage:: Error converting numeric value for " + 
                     isTagName;
      throw new SAXException(new InvalidHostDataException(HostError.INVALID_DATA,vsErr, mpHIData.getOriginalMessageSequence(), nfe));
    }
    catch(InvalidHostDataException pe)
    {
      String vsNewErrorMessage = "OrderMessage:: Error with " + isTagName + 
                                 " Tag. " + pe.getErrorMessage() + " ";
      pe.setOriginalSequence(mpHIData.getOriginalMessageSequence());
      pe.setErrorMessage(vsNewErrorMessage);
      throw new SAXException(pe);
    }
    catch(DBException exc)
    {
      InvalidHostDataException invDataExc = new InvalidHostDataException(exc);
      invDataExc.setOriginalSequence(mpHIData.getOriginalMessageSequence());
      if (!exc.isDuplicate())
      {
        invDataExc.setErrorMessage("OrderMessage:: Error executing DB " +
                                   "operation for order " + getCachedOrderID() + ". ");
        invDataExc.setErrorCode(exc.getErrorCode());
        throw new SAXException(invDataExc);
      }
      else
      {
        invDataExc.setErrorCode(HostError.DUPLICATE_DATA);
        invDataExc.setErrorMessage("Order id . = " + getCachedOrderID());
        throw new SAXException(invDataExc);
      }
    }
    catch(NoSuchElementException nse)
    {
      mpLogger.logDebug("OrderMessage:: Requested change to Order record for " +
                      mpOLData.getOrderID() + " did not work!");

      InvalidHostDataException invDataExc = new InvalidHostDataException(nse);
      invDataExc.setOriginalSequence(mpHIData.getOriginalMessageSequence());
      invDataExc.setErrorMessage("OrderMessage:: Requested change to Order record for " +
                                 getCachedOrderID() + " did not work!");
      
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
    if (mpOrderServer != null)
    {
      try { mpDBObj.disconnect(); }
      catch(DBException e) {}
      mpOrderServer.cleanUp();
      mpOrderServer = null;
    }
  }

 /**
  *  Method to add an order from parsed data.
  */
  protected void addOrder() throws DBException
  {
    if (mpOLList.isEmpty())
      throw new DBException("No order lines found for order add operation!!");
    OrderLineData[] olArray = mpOLList.toArray(new OrderLineData[0]);

    if (mzLoadOrder)
      mpOHData.setOrderType(DBConstants.FULLLOADOUT);
    else if (mzEmptyContOrder)
      mpOHData.setOrderType(DBConstants.CONTAINER);
    mpOrderServer.buildOrder(mpOHData, olArray);
    
  }

 /**
  * Method modifies an order which includes a header element, a series of line
  * elements, order line notes, and order notes.  Which element(s) are modified
  * depends on what elements are provided for modification.
  * @throws DBException
  */
  protected void modifyOrder() throws DBException
  {
    if (mzLineProvided && !mpOLList.isEmpty())
    {
      int olLength = mpOLList.size();
      for(int idx = 0; idx < olLength; idx++)
      {
        mpOLData = (OrderLineData)mpOLList.get(idx);
        if (mpOrderServer.orderLineExists(mpOLData.getOrderID(), mpOLData.getItem(),
                                        mpOLData.getOrderLot(), mpOLData.getLineID()))
        {
          mpOrderServer.modifyOrderLine(mpOLData, true);
        }
        else
        {
          mpOrderServer.addOrderLine(mpOLData);
        }
      }
    }
    else
    {
      if (mpOHData.getNextStatus() == DBConstants.HOLD)
      {
        mpOrderServer.holdOrder(mpOHData.getOrderID());
      }
      else
        mpOrderServer.modifyOrderHeader(mpOHData);
    }
  }
  
 /**
  * Method will delete an Order completely if the header is provided, and
  * individual lines as needed if the order lines are provided.  We do not allow
  * for deletion of individual order notes and order line notes; these will be
  * deleted as their respective parents (Order header or Order lines) are deleted.
  * @throws DBException for data base update errors.
  */
  protected void deleteOrder() throws DBException
  {
    if (mzLineProvided && !mpOLList.isEmpty())
    {
      int olLength = mpOLList.size();
      for(int idx = 0; idx < olLength; idx++)
      {
        mpOLData = (OrderLineData)mpOLList.get(idx);
        mpOrderServer.deleteOrderLine(mpOLData.getOrderID(), mpOLData.getLineID());
      }
    }
    else
    {
      mpOrderServer.deleteOrder(mpOHData.getOrderID());
    }
  }
  
  protected void setAttributes(String isTagName, String isAttribName,
                             String isAttribValue) throws SAXException
  {
//    String vsUpperAttName = isAttribName.toUpperCase();
                                       // Compare attribute name passed-in in
                                       // a case sensitive manner since the parser
                                       // itself is picky about case.
    try
    {
      if (isTagName.equals("Order"))
      {
        if (isAttribName.equals("sOrderID"))
        {
          MessageHelper.fillDataObject(mpOLData, isAttribName, isAttribValue);
        }
        if (isAttribName.equals("iOrderStatus"))
        {                                // Always set the inital order status to HOLD.
                                         // Later the NextStatus field will determine
                                         // the real status.
          MessageHelper.fillDataObject(mpOHData, isAttribName, Integer.toString(DBConstants.HOLD));
          isAttribName = OrderHeaderData.NEXTSTATUS_NAME;
        }
        MessageHelper.fillDataObject(mpOHData, isAttribName, isAttribValue);
      }
      else if (isTagName.equals("OrderLine"))
      {
        MessageHelper.fillDataObject(mpOLData, isAttribName, isAttribValue);
      }
    }
    catch(InvalidHostDataException nsf)
    {
      throw new SAXException("Invalid attribute \"" + isAttribName + "\"", nsf);
    }
  }

  protected void resetPrimaryKeyData(String tagName)
  {
    if (tagName.equals("OrderLine"))
    {
      mpOLData.clear();
      mpOLData.setOrderID(mpOHData.getOrderID());
    }
  }
  
  private void setDataObject(String sDataObjectName)
  {
    if (mpDataObjectMap.containsKey(sDataObjectName))
    {
      AbstractSKDCData myDataObject = mpDataObjectMap.get(sDataObjectName);
      if (mpDataObject != myDataObject)
      {
        mpDataObject = myDataObject;
      }
      if (sDataObjectName.equals("OrderHeader")) mzHeaderProvided = true;
      if (sDataObjectName.equals("OrderLine"))   mzLineProvided = true;
    }
  }
  
  protected void initDataMap()
  {
    mpDataObjectMap = new HashMap<String, AbstractSKDCData>();
    mpDataObjectMap.put("OrderHeader", mpOHData);
    mpDataObjectMap.put("OrderLine", mpOLData);
  }
  
 /**
  * Method will try to return an order id. as known in the current active
  * data object.  this call is normally used in exception processing.  Depending
  * on whether the header failed or the line failed we can't always rely on 
  * the order id. being present in the stored order header.
  * @return String containing order id.
  */
  protected String getCachedOrderID()
  {
    String vsOrderID = (mpDataObject instanceof OrderHeaderData) ? mpOHData.getOrderID()
                                                                 : mpOLData.getOrderID();
    return(vsOrderID != null ? vsOrderID : "");
  }

 /**
  * Method to check basic order quantity boundaries.  If it's an Item or Empty
  * container Order, make sure the quantity is not zero, and does not exceed the
  * size of an Integer.  If it's an empty container order make sure that the qty.
  * is a valid empty 
  * 
  * @return <code>true</code> if quantity is valid.
  */
  protected boolean isOrderQuantityValid(double idOrderQty)
  {
    boolean vzRtn = false;
    
    if (idOrderQty < Integer.MAX_VALUE)
    {
      double vdOrderQty = SKDCUtility.getTruncatedDouble(idOrderQty);
      if (vdOrderQty > 0.00)
      {
        if (mzEmptyContOrder)
        {
          double vdFraction = 0;
          double vdFloor = Math.floor(vdOrderQty);
          if (vdFloor > 0.00)
            vdFraction = SKDCUtility.getTruncatedDouble(vdOrderQty%vdFloor);
          else
            vdFraction = vdOrderQty; // Order qty. is of 0.nn form.

          if (vdFraction > 0)
          {                          // Make sure the fractional quantity they
                                     // sent is a valid Empty container amount.
            Set<Map.Entry<String, AmountFullTransMapper>> vpSet = mpPartialQtyMap.entrySet();
            for(Map.Entry<String, AmountFullTransMapper> vpEntry : vpSet)
            {
              AmountFullTransMapper vpMapper = vpEntry.getValue();
              double vdPartialAmt = vpMapper.getPartialAmtFullDecimal();
              if (vdFraction == vdPartialAmt)
              {
                vzRtn = true;
                break;
              }
            }
          }
          else
          {
            vzRtn = true;
          }
        }
        else                           // This is a load or item order qty.
        {
          vzRtn = true;
        }
      }
    }
    
    return(vzRtn);
  }
}
