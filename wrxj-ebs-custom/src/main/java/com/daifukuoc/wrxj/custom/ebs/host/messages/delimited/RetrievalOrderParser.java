/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.host.messages.delimited;

import org.apache.commons.lang3.StringUtils;

import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.log.Logger;

/**
 * Class to handle the parsing of Retrieval Order data from a host system. The message details defined in
 * <p>
 * http://softwaredoc.daifukuoc.com/airflowwcs/technical-guide/messages/sac-messages/retrieval-order-2.html
 * </p>
 * The processed message will be in comma separated format. for example (Message header + Message body) :
 * <p>
 * 5,OrderID001, QFA 1234A,20221201134500,0
 * </p>
 * 
 * @author NHC 26-Aug-2022
 */

public class RetrievalOrderParser implements MessageParser {

    protected Logger mpLogger = Logger.getLogger();
    private SystemGateway gateway = ThreadSystemGateway.get();

    /*
     * A constructor
     */
    public RetrievalOrderParser() {
    }

    /**
     * Method to do delimited parsing for an Retrieval Order.
     * 
     * @param hostToWrxData The data from the HostToWrx data queue (table).
     * @throws InvalidHostDataException when there are parsing errors due to malformed messages, or problems with
     *         message content validation.
     * @throws DBRuntimeException If there is an internal error that only the system needs to resolve.
     */
    @Override
    public void parse(HostToWrxData hostToWrxData) throws InvalidHostDataException {

        if (hostToWrxData == null) {
            throw new InvalidHostDataException(
                    "The given host to wrx data is null");
        }
        if (hostToWrxData.getMessage() == null) {
            throw new InvalidHostDataException(
                    "The received retrieval order message is null");
        }
        if (StringUtils.isBlank(hostToWrxData.getMessage())) {
            throw new InvalidHostDataException(
                    "The received retrieval order message is empty or blank only");
        }
        gateway.publishHostRetrievalOrderEvent(hostToWrxData.getMessage());
    }

    @Override
    public void cleanUp() {
    }

// Commented out for now because 1. Implementation is not even completed 2. Originally written for flat conveyer based storage system like Kansai airport
//  /*
//   * This method will handle the List of Retrieval Order Request get the load
//   * details by load id and move status (no moves #224)
//   * 
//   * @return a list of Devices id(PLC) and associated target station id
//   */
//  private Hashtable<String, String> processRetrivealOrderListRequest() throws DBException {
//      String stationId = "";
//      String deviceId = "";
//      data_dict = new Hashtable<String, String>();
//
//      // get Load details for order, flight and No of Bags
//      mpLogger.logDebug("get Load details for order :" + mpRetrievalOrderData.getOrderID());
//
//      // get the load details from the loop
//      List<RetrievalOrderItemList> listOrderData = mpRetrievalOrderData.listOrderData;
//      mpLogger.logDebug("RetrivealOrderListRequest :" + listOrderData.size());
//
//      for (RetrievalOrderItemList vpLoadMap : listOrderData) {
//          List<Map> loadList = getLoadDataById(vpLoadMap.getLoadId());
//          if( loadList != null)
//          {
//              for (Map vpLoad : loadList) {
//                  stationId = mpEBSLocationServer
//                          .getGateIdForLocationAddress(DBHelper.getStringField(vpLoad, LoadData.ADDRESS_NAME));
//                  deviceId = DBHelper.getStringField(vpLoad, LoadData.DEVICEID_NAME);
//  
//                  if (!data_dict.containsKey(deviceId)) {
//                      // create a map with device and Station
//                      data_dict.put(deviceId, stationId);
//                      mpLogger.logDebug("adding Flush data to Hashtable, key:" + deviceId + ", value : " + stationId);                }
//              }
//          }
//      }
//      mpLogger.logDebug(
//              "Flush hash table data: " + data_dict.size() + " for OrderId: " + mpRetrievalOrderData.getOrderID());
//      return data_dict;
//  }
//
//  /*
//   * This method will handle the single Retrieval Order Request get the load
//   * details by lot Id and No of bags and fill and return flush data Hash table
//   * 
//   * @return Hash table with flush related data
//   */
//  private Hashtable<String, String> processRetrivealOrderRequest() throws DBException {
//      String stationId = "";
//      String deviceId = "";
//      data_dict = new Hashtable<String, String>();
//
//      mpLogger.logDebug("get Load details for order :" + mpRetrievalOrderData.getOrderID() + " flight:"
//              + mpRetrievalOrderData.getLot() + " and No of Bags:" + mpRetrievalOrderData.getsNumberOfBags());
//
//      // get Load details for order, flight and No of Bags
//      List<Map> loadList = getLoadDetails(mpRetrievalOrderData.getLot(), mpRetrievalOrderData.getsNumberOfBags());
//      mpLogger.logDebug("RetrivealOrderRequest :" + loadList.size());
//
//      if (loadList != null && loadList.size() > 0) {
//
//          // this for testing
//          // listofMissingBags.add(DBHelper.getStringField(vpLoadMap,
//          // LoadLineItemData.LOT_NAME));
//          listofMissingBags.add("Test 123"); // this section is testing for missing bags
//
//          for (Map vpLoadMap : loadList) {
//              // get the Station no by passing address data to the method this is the happy
//              // path
//              stationId = mpEBSLocationServer
//                      .getGateIdForLocationAddress(DBHelper.getStringField(vpLoadMap, LoadData.ADDRESS_NAME));
//              deviceId = DBHelper.getStringField(vpLoadMap, LoadData.DEVICEID_NAME);
//
//              if (!data_dict.containsKey(deviceId)) {
//                  // create a map with device and Station
//                  data_dict.put(deviceId, stationId);
//                  mpLogger.logDebug("adding Flush data to Hashtable, key:" + deviceId + ", value : " + stationId);
//              }
//          }
//      } else {
//          mpLogger.logError("No load information found for flight" + mpRetrievalOrderData.getLot()
//                  + " and for No of Bags" + mpRetrievalOrderData.getsNumberOfBags());
//      }
//
//      mpLogger.logDebug(
//              "Flush Hashtable data: " + data_dict.size() + " for OrderId: " + mpRetrievalOrderData.getOrderID());
//      return data_dict;
//  }
//
//  /*
//   * Check the load is exist, if so get the load data and return
//   */
//  private List<Map> getLoadDataById(String loadId) {
//
//      // check load is exist
//      List<Map> loadData = null;
//      try {
//          if (mpEBSLoadServer.loadExists(loadId)) {
//
//              mpLoadData.clear();
//              mpLoadData.setKey(LoadData.LOADID_NAME, loadId);
//              mpLoadData.setKey(LoadData.LOADMOVESTATUS_NAME, DBConstants.NOMOVE);
//              loadData = mpEBSLoadServer.getLoadDataList(mpLoadData.getKeyArray());
//
//              mpLogger.logDebug("LoadId " + loadId + " is Exists in the database, load data :" + loadData.size());
//          }
//      } catch (DBException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//      }
//      return loadData;
//  }
//
//  private List<Map> getLoadDetails(String flightNo, int noOfBags) {
//      List<Map> loadData = null;
//      try {
//
//          // if the no of bag count is specify in the message get the exact data from the
//          // database
//          // else get all the data by flight no
//          if (noOfBags != 0) {
//              loadData = mpEBSLoadServer.getLoadbyFlightNo(flightNo, noOfBags);
//          } else {
//              loadData = mpEBSLoadServer.getLoadbyFlightNo(flightNo);
//          }
//          mpLogger.logDebug("Load count " + loadData.size() + " for order Retrieval, flight " + flightNo
//                  + "and No of Bags" + noOfBags);
//
//      } catch (DBException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//      }
//      return loadData;
//  }
//

//
//  /**
//   * This method inserts the Retrieval Order response message in the WRX to host
//   * 
//   * @param orderNo
//   * @param sListOfMissingBags array list of missing bag id`s
//   * 
//   */
//  public void sendRetrievalOrderResponseMsg(String orderNo, ArrayList<String> sListOfMissingBags, int responseMsgType) {
//
//      String strListMissingBags = "";
//      StringBuilder sb = new StringBuilder();
//      RetrievalOrderResponseMessage mpResponseMsg = Factory.create(RetrievalOrderResponseMessage.class);
//
//      mpResponseMsg.setResponseMsgType(responseMsgType);
//      mpResponseMsg.setOrderID(orderNo);
//      mpResponseMsg.setStatus(SACControlMessage.RetrievalOrderNotify.STATUS.SUCCESS);
//      mpResponseMsg.setArrayLength(sListOfMissingBags.size());
//
//      if (sListOfMissingBags != null && sListOfMissingBags.size() > 0) {
//          for (int i = 0; i < sListOfMissingBags.size(); i++) {
//              String var = sListOfMissingBags.get(i);
//              sb.append(var).append(",");
//          }
//          // Removing the last character of a string
//          sb.deleteCharAt(sb.toString().length() - 1);
//          strListMissingBags = sb.toString();
//          mpResponseMsg.setArrayOfMissingBags(strListMissingBags);
//      } else {
//          mpResponseMsg.setArrayOfMissingBags(strListMissingBags);
//      }
//
//      try {
//          mpEBSHostServer.sendRetrievalOrderResponseToHost(mpResponseMsg);
//      } catch (DBException e) {
//          throw new InvalidHostDataException(HostError.ADD_ERROR, "Unable to generate the expected receipt response."
//                  + ", Exception details:" + e.getMessage() + ",mpResponseMsg" + mpResponseMsg);
//      }
//  }
}
