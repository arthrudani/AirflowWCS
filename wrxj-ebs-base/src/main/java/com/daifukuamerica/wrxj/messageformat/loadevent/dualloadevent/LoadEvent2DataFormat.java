package com.daifukuamerica.wrxj.messageformat.loadevent.dualloadevent;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import java.util.StringTokenizer;


public class LoadEvent2DataFormat  extends LoadEventDataFormat
{
//Data passed between the Station Device and the Scheduler
  private LoadEventDataFormat loadMessage2; // this is the second command message

  /**
   * Constructor
   * 
   * @param name
   */
  public LoadEvent2DataFormat(String name)
  {
    super(name);
    loadMessage2 = Factory.create(LoadEventDataFormat.class, name);
  }

  /**
   * Constructor
   * 
   * @param name
   * @param loadEvent1
   * @param loadEvent2
   */
  public LoadEvent2DataFormat(String name, LoadEventDataFormat loadEvent1,
      LoadEventDataFormat loadEvent2)
  {
    super(name, loadEvent1);
    loadMessage2 = Factory.create(LoadEventDataFormat.class, name, loadEvent2);
  }

  /**
   * Clears the values of all data in this class
   */
  @Override
  public void clearAllData()
  {
    super.clearAllData();
    loadMessage2.clearAllData();
  }

  /**
   * Puts all data of this class into a string with delimeters of ":"
   * 
   * @return String of all data in supper class and this class
   */
  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer();
    myString.append(super.toString());
    myString.append(loadMessage2.toString());
    return myString.toString();
  }

  /**
   * Formats the supper class data then formats the second command data
   * 
   * @return String of formatted data for both commands
   */
  @Override
  public StringBuffer formatDataString(StringBuffer myString)
  {
    myString = super.formatDataString(myString);
    myString = loadMessage2.formatDataString(myString);
    return myString;
  }

  /**
   * Parse the data string of the super classes data then parse then remaining
   * data into the second commands data loadMessage2
   * 
   * @param String of data to parse
   * @return stringTokinizer after data way taken out an stored in loadMessage2
   */
  @Override
  public StringTokenizer parseDataString(StringTokenizer parser ) throws Exception 
  {
    setMessageID(Integer.parseInt(parser.nextToken()));
    setLoadID(parser.nextToken());
    setSourceStation(parser.nextToken());
    setDestinationStation(parser.nextToken());
    setSourceLocation(parser.nextToken());
    setDestinationLocation(parser.nextToken());
    setResults(Integer.parseInt(parser.nextToken()));
    setStatus(Integer.parseInt(parser.nextToken()));
    setDimensionInfo(Integer.parseInt(parser.nextToken()));
    setBarCode(parser.nextToken());
    setInformation(parser.nextToken());
    setErrorCode(parser.nextToken()); 
//    super.parseDataString(parser);
    loadMessage2.clearAllData();
    loadMessage2.setMessageID(Integer.parseInt(parser.nextToken()));
    loadMessage2.setLoadID(parser.nextToken());
    loadMessage2.setSourceStation(parser.nextToken());
    loadMessage2.setDestinationStation(parser.nextToken());
    loadMessage2.setSourceLocation(parser.nextToken());
    loadMessage2.setDestinationLocation(parser.nextToken());
    loadMessage2.setResults(Integer.parseInt(parser.nextToken()));
    loadMessage2.setStatus(Integer.parseInt(parser.nextToken()));
    loadMessage2.setDimensionInfo(Integer.parseInt(parser.nextToken()));
    loadMessage2.setBarCode(parser.nextToken());
    loadMessage2.setInformation(parser.nextToken());
    loadMessage2.setErrorCode(parser.nextToken());
    return parser;
  }

  /**
   * Stores the second loadEventDataFormat into loadMessage2
   * 
   * @param loadEventMessage to store
   */
  public void setLoadEventMessage2(LoadEventDataFormat secondLoadEventMessage)
  {
    loadMessage2 = secondLoadEventMessage;
  }

  /**
   * Get the second loadEventDataFormat message which contains the second
   * command
   * 
   * @return loadMessage2
   */
  public LoadEventDataFormat getLoadEventMessage2()
  {
    return loadMessage2;
  }

  /**
   * Get the first loadEventDataFormat message which contains the first command
   * 
   * @return loadMessage1
   */
  public LoadEventDataFormat getLoadEventMessage1()
  {
    LoadEventDataFormat message1 = Factory.create(LoadEventDataFormat.class,
        super.getSendersName());
    message1.setLoadID( getLoadID());
    message1.setSourceStation(getSourceStation());
    message1.setDestinationStation(getDestinationStation());
    message1.setSourceLocation(getSourceLocation());
    message1.setDestinationLocation(getDestinationLocation());
    message1.setResults(getResults());
    message1.setStatus(getStatus());
    message1.setDimensionInfo(getDimensionInfo());
    message1.setBarCode(getBarCode());
    message1.setInformation(getInformation());
    message1.setErrorCode(getErrorCode());
    return(message1);
  }

  /**
   * Sets the sMessageID to AGCDEVICEARRIVALREPORT and creates the datastring
   * 
   * @param loadID LoadId of load command to cancel
   * @param station where load arrived at
   * @param height of the load
   * @param results results of the response
   * @param barCode of the load
   * @param info Control information of the load
   * @return LoadEvent string to transmit
   */
  public String processDualArrivalReport(String loadID, String station,
      int height, int results, String barCode, String info, String loadID2,
      String station2, int height2, int results2, String barCode2, String info2)
  {
    clearAllData();
    processArrivalReport(loadID, station, height, results, barCode, info);
    setMessageID(AGCDeviceConstants.AGCDEVICEDUALARRIVALREPORT); // 429
    loadMessage2.processArrivalReport(loadID2, station2, height2, results2,
        barCode2, info2);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to STOREDUALLOADSTATIONLOCATION and creates the
   * datastring
   * 
   * @param loadID LoadId of load to store
   * @param sourceStationName station where load is currently at.
   * @param destinationLocation location where load is to be moved to
   * @return LoadEvent string to transmit
   */
  public String storeDualLoadStationLocation()
  {
    setMessageID(AGCDeviceConstants.STOREDUALLOADSTATIONLOCATION);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to RETRIEVEDUALLOADSTATIONLOCATION and creates the
   * datastring
   * 
   * @param loadID LoadId of load to store
   * @param sourceStationName station where load is currently at.
   * @param destinationLocation location where load is to be moved to
   * @return LoadEvent string to transmit
   */
  public String retrieveDualLoadStationLocation()
  {
    setMessageID(AGCDeviceConstants.RETRIEVEDUALLOADSTATIONLOCATION);
    return createStringToSend();
  }
}