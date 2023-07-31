package com.daifukuamerica.wrxj.host.messages.xml;

import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.InventoryUpload;
import com.daifukuamerica.wrxj.host.messages.MessageOutFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 * Class to create message to host on a separate thread,
 * so the host system does not have to wait on the inventory
 * upload.
 */
public class UploadMessageCreator extends NamedThread
{
  protected static final int MAX_ROW_COUNT = 1000;
  protected InventoryUpload mpIUMesg;
  protected HostToWrxData mpHostInData;
  protected StandardInventoryServer mpInvServer;
  protected StandardHostServer mpHostServer;
  protected Logger mpLogger;
  protected DBObject mpDBObj;
  protected String msReqWarhse;
  protected String msReqItem;
  protected String msReqLot;

  public UploadMessageCreator(HostToWrxData ipHostInData, String isWarehouse,
                              String isItem, String isLot)
  {
    mpHostInData = ipHostInData;
    msReqWarhse = isWarehouse;
    msReqItem = isItem;
    msReqLot = isLot;
  }
  
  /**
   * Method initializes all of the class variables of the thread
   */
  protected void initialize()
  {
    mpInvServer = Factory.create(StandardInventoryServer.class);
    mpHostServer = Factory.create(StandardHostServer.class);
    mpLogger = Logger.getLogger();
    mpDBObj = new DBObjectTL().getDBObject();
    mpDBObj.setMaxRows(MAX_ROW_COUNT);
    mpIUMesg = MessageOutFactory.getInstance(MessageOutNames.INVENTORY_UPLOAD);
  }

  /**
   * Method processes the message from the parser
   * @throws DBException
   * @throws InvalidHostDataException
   */
  protected void processMessage() throws DBException, InvalidHostDataException
  {
    List vpInventoryList = mpInvServer.getLoadLineItemTotals(msReqWarhse,
                                                             msReqItem, msReqLot);
    int vnListLength = vpInventoryList.size();

    if (vnListLength == 0)
    {
      InvalidHostDataException invData = new InvalidHostDataException(
          "No available inventory on system...");
      invData.setErrorCode(HostError.NO_DATA_FOUND);
      invData.setOriginalSequence(mpHostInData.getOriginalMessageSequence());
      throw invData;
    }

    if (vnListLength == MAX_ROW_COUNT)
    {
      boolean vzMoreMessages = false;
      int vnLength = findLastCompleteItem(vpInventoryList);
      do
      {
        createMessage(vpInventoryList, vnLength, false);
        Map vpTempMap = (Map) vpInventoryList.get(vnLength - 1);
        String vsItem = DBHelper.getStringField(vpTempMap,
            LoadLineItemData.ITEM_NAME);
        vpInventoryList = mpInvServer.getItemTotalsGreaterThanItem(msReqWarhse, vsItem);
        vnListLength = vpInventoryList.size();
        if (vnListLength == 0)
        {
          vzMoreMessages = false;
        }
        else if(vnListLength > 0 && vnListLength < MAX_ROW_COUNT)
        {
          createMessage(vpInventoryList, vnListLength, true);
          vzMoreMessages = false;
        }
        else
        {
          vnLength = findLastCompleteItem(vpInventoryList);
          vzMoreMessages = true;
        }
      } while (vzMoreMessages);
    }
    else
    {
      createMessage(vpInventoryList, vnListLength, true);
    }
  }

  /**
   * Method creates the message and sends it to the host
   * @param ipInvList the inventory list
   * @param inListLength the list length
   * @param izLastUpLoad flag indicating if this is going to be the last message
   * to upload.
   * @throws DBException
   */
  protected void createMessage(List ipInvList, int inListLength, boolean izLastUpLoad)
      throws DBException
  {
    for (int idx = 0; idx < inListLength; idx++)
    {
      Map theMap = (Map) ipInvList.get(idx);
      String vsWarhse = DBHelper.getStringField(theMap, "SWAREHOUSE");
      String vsHoldReason = DBHelper.getStringField(theMap,
          LoadLineItemData.HOLDREASON_NAME);
      String vsItem = DBHelper.getStringField(theMap, LoadLineItemData.ITEM_NAME);
      String vsLot = DBHelper.getStringField(theMap, LoadLineItemData.LOT_NAME);
      double vfAvailQty = DBHelper.getDoubleField(theMap, "FAVAILQUANTITY");

      mpIUMesg.setHoldReason(vsHoldReason);
      mpIUMesg.setItem(vsItem);
      mpIUMesg.setLot(vsLot);
      mpIUMesg.setQuantity(vfAvailQty);
      mpIUMesg.setWarehouse(vsWarhse);
      if (izLastUpLoad && idx == inListLength - 1)
      {
        mpIUMesg.setLastRecordFlag();
        formatMesg();
      }
      else
      {
        formatMesg();
      }
    }
    
    mpHostServer.addToDataQueue(new HostOutDelegate(mpIUMesg));
  }

  protected void formatMesg()
  {
    try
    {
      mpIUMesg.format();
    }
    catch (Exception e)
    {
      mpLogger.logError(e.getMessage());
    }
    catch (Throwable e)
    {
      mpLogger.logError(e.getMessage());
    }
  }

  /**
   * Method finds the last complete item in the inventory list.
   * @param isInvList the list of items to search
   * @return vnLength the last complete item in message.
   */
  protected int findLastCompleteItem(List isInvList)
  {
    int vnLength = isInvList.size();
    
    //Prevents array out of bounds errors.  This should never happen.
    if(vnLength < 2)
      return vnLength;
    
    Map vpTempMap = (Map) isInvList.get(vnLength-1);
    String vsItem = DBHelper.getStringField(vpTempMap,
        LoadLineItemData.ITEM_NAME);
    for (int mnIndex = vnLength - 2; mnIndex >= 0; mnIndex--)
    {
      vpTempMap = (Map) isInvList.get(mnIndex);
      String vsListItem = DBHelper.getStringField(vpTempMap,
          LoadLineItemData.ITEM_NAME);
      if (!vsListItem.equals(vsItem))
      {
        vnLength = mnIndex + 1;
        break;
      }
    }
    return vnLength;
  }

  @Override
  public void run()
  {
    initialize();
    try
    {
      processMessage();

    }
    catch (DBException exc)
    {
      InvalidHostDataException invDataExc = new InvalidHostDataException(exc);
      if (!exc.isDuplicate())
      {
        invDataExc.setErrorMessage("InventoryHoldMessage:: Error executing DB "
            + "operation for item " + msReqItem);
        invDataExc.setOriginalSequence(mpHostInData.getOriginalMessageSequence());
        notifyHost(invDataExc);
      }
      else
      {
        invDataExc.setErrorCode(HostError.DUPLICATE_DATA);
        invDataExc.setErrorMessage("Host Sequence number: "
            + mpHostInData.getOriginalMessageSequence() + "item . = "
            + msReqItem);
        notifyHost(invDataExc);
      }
    }
    catch (InvalidHostDataException exc)
    {
      notifyHost(exc);
    }
    cleanUp();
  }
  
  public void cleanUp()
  {
    if(mpHostServer != null)
    {
      mpHostServer.cleanUp();
      mpHostServer = null;
    }
    if(mpInvServer != null)
    {
      mpInvServer.cleanUp();
      mpInvServer = null;
    }
 
  }

  /**
   * Method marks the host message in error and logs and exception.
   */
  private void errorMessageHandler(Exception ipExc)
  {
    try
    {
      mpHostServer.markMessageInError(mpHostInData);
    }
    catch (DBException exc)
    {
      mpLogger.logException(exc, "InventoryRequestParser-->CreateMessage.run()");
    }
    finally
    {
      mpLogger.logException(ipExc,
          "InventoryRequestParser-->CreateMessage.run():: "
              + "Message Sequence = " + mpHostInData.getMessageSequence()
              + ", Message Identifier = \""
              + mpHostInData.getMessageIdentifier() + "\".");
    }
  }

  /**
   * Method to notify host of an error processing a message.
   * @param ipDataException reference to an InvalidHostDataException.  This exception
   *        will contain info. that will be sent to the host, such as the error code,
   *        and the error message, and optionally the specific host the message is
   *        directed at.
   */
  private void notifyHost(InvalidHostDataException ipDataException)
  {
    errorMessageHandler(ipDataException);
    int vnErrorCode = (ipDataException.getErrorCode() == 0) ? HostError.INVALID_DATA
        : ipDataException.getErrorCode();
    int vnOrigSeqNumber = ipDataException.getOriginalSequence();
    String vsHostName = ipDataException.getHostName();
    String vsErrorMessage = formCompleteMessage(ipDataException);

    try
    {
      mpHostServer.writeHostError(vnErrorCode, vnOrigSeqNumber, vsHostName,
          vsErrorMessage);
    }
    catch (DBException exc)
    {
      mpLogger.logException(exc,
          "HostMessageIntegrator-->processIPCReceivedMessage. "
              + "Host error message failed to add to data queue for sending...");
    }
  }

  private String formCompleteMessage(Throwable ipExcep)
  {
    String vsRtnMsg = ipExcep.getMessage();

    if (ipExcep.getCause() != null)
    {
      boolean vzDone = false;
      while (!vzDone)
      {
        Throwable vpExcep = ipExcep.getCause();
        ipExcep = vpExcep;
        if (vpExcep == null)
        {
          vzDone = true;
          continue;
        }
        vsRtnMsg += (vpExcep.getMessage() + ". ");
      }
    }

    return (vsRtnMsg);
  }

}
