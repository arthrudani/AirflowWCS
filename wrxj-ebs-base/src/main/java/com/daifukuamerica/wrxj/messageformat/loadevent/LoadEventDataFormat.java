package com.daifukuamerica.wrxj.messageformat.loadevent;

import com.daifukuamerica.wrxj.dbadapter.ParameterNameConstants;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.messageformat.MessageConstants;
import com.daifukuamerica.wrxj.messageformat.MessageDataFormat;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.StringTokenizer;

/**
 * 
 * <B>Description:</B> Load Event message class
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 */
public class LoadEventDataFormat extends MessageDataFormat
{
  int LNLOADID = DBInfo.getFieldLength(LoadData.LOADID_NAME);
  int LNSTATION = DBInfo.getFieldLength(StationData.STATIONNAME_NAME);
  
  // Data passed between the Station Device and the Scheduler
  protected int mnMessageID;
  protected int mnResults;
  protected int mnStatus;
  protected int mnDimensionInfo;
  protected int mnRetrievalPriority = MessageConstants.PLANNED_RETRIEVAL;
  protected int mnRetvCmdDetail = MessageConstants.UNIT_RETRIEVAL;
  protected int mnReinputFlag = MessageConstants.NO_REINPUT;
  protected int mnGroupNo = 0;
  protected String msLoadID;
  protected String msSourceStation;
  protected String msDestinationStation;
  protected String msSourceLocation;
  protected String msSourceLocnShelfPosition;
  protected String msSourceEquipWarehouse;
  protected String msDestinationLocation;
  protected String msDestLocnShelfPosition;
  protected String msDestinationEquipWarehouse;
  protected String msBarCode;
  protected String msInformation;
  protected String msErrorCode;
  protected String msSubID;
  protected String msMsgData;

  /**
   * Constructor
   * 
   * @param isSendersName
   */
  public LoadEventDataFormat(String isSendersName)
  {
    super(MessageConstants.AGCMESSAGETYPE, isSendersName);
  }

  /**
   * Constructor
   *
   * @param isSendersName
   * @param ipLEDF - <code>LoadEventDataFormat</code> containing initial values
   */
  public LoadEventDataFormat(String isSendersName, LoadEventDataFormat ipLEDF)
  {
    super(MessageConstants.AGCMESSAGETYPE, isSendersName);
    copyIntoMyData(ipLEDF);
  }

  /**
   * Copy method
   *
   * @param ipNewData
   */
  public void copyIntoMyData(LoadEventDataFormat ipNewData)
  {
    setLoadID(ipNewData.getLoadID());
    setSourceStation(ipNewData.getSourceStation());
    setDestinationStation(ipNewData.getDestinationStation());
    setSourceLocation(ipNewData.getSourceLocation());
    setSourceEquipWarehouse(ipNewData.getSourceEquipWarehouse());
    setDestinationLocation(ipNewData.getDestinationLocation());
    setDestinationEquipWarehouse(ipNewData.getDestinationEquipWarehouse());
    setResults(ipNewData.getResults());
    setStatus(ipNewData.getStatus());
    setDimensionInfo(ipNewData.getDimensionInfo());
    setGroupNumber(ipNewData.getGroupNumber());
    setReinputFlag(ipNewData.getReinputFlag());
    setPriorityCategory(ipNewData.getPriorityCategory());
    setRetrievalCommandDetail(ipNewData.getRetrievalCommandDetail());
    setBarCode(ipNewData.getBarCode());
    setInformation(ipNewData.getInformation());
    setErrorCode(ipNewData.getErrorCode());
    setSubID(ipNewData.getSubID());
    setMsgData(ipNewData.getMsgData());
  }

  /**
   * Clears the values of all data in this class
   */
  @Override
  public void clearAllData()
  {
    super.clearAllData();
    setLoadID("");
    setSourceStation("");
    setDestinationStation("");
    setSourceLocation("");
    setSourceLocnShelfPosition("000");
    setSourceEquipWarehouse(" ");
    setDestinationLocation(" ");
    setDestinationLocnShelfPosition("000");
    setDestinationEquipWarehouse("");
    setResults();
    setStatus();
    setDimensionInfo();
    setGroupNumber(0);
                             // Use default values.
    setPriorityCategory(MessageConstants.PLANNED_RETRIEVAL);
    setReinputFlag(MessageConstants.NO_REINPUT);
    setRetrievalCommandDetail(MessageConstants.UNIT_RETRIEVAL);
    setBarCode();
    setInformation("");
    setErrorCode(" ");
    setSubID("");
    setMsgData("");
  }

  /**
   * Puts all data of this class into a string with delimiters of ":"
   * 
   * @return myString super.toString and all of this classes data appended to it
   */
  @Override
  public String toString()
  {
    StringBuffer vpMyString = new StringBuffer();
    vpMyString.append(super.toString());
    vpMyString.append(": mnMessageID = " + mnMessageID);
    vpMyString.append(": msLoadID  = " + msLoadID);
    vpMyString.append(": msSourceStation = " + msSourceStation);
    vpMyString.append(": msDestinationStation = " + msDestinationStation);
    vpMyString.append(": msSourceLocation =: " + msSourceLocation);
    vpMyString.append(": msSourceLocnShelfPosition =: " + msSourceLocnShelfPosition);
    vpMyString.append(": msSourceEquipWarehouse =: " + msSourceEquipWarehouse);
    vpMyString.append(": msDestinationLocation =: " + msDestinationLocation);
    vpMyString.append(": msDestLocnShelfPosition =: " + msDestLocnShelfPosition);
    vpMyString.append(": msDestinationEquipWarehouse =: " + msDestinationEquipWarehouse);
    vpMyString.append(": mnResults =: " + mnResults);
    vpMyString.append(": mnStatus =: " + mnStatus);
    vpMyString.append(": mnDimensionInfo =: " + mnDimensionInfo);
    vpMyString.append(": mnGroupNo =: " + mnGroupNo);
    vpMyString.append(": mnRetrievalPriority =: " + mnRetrievalPriority);
    vpMyString.append(": mnReinputFlag =: " + mnReinputFlag);
    vpMyString.append(": mnRetvCmdDetail =: " + mnRetvCmdDetail);
    vpMyString.append(": msBarCode =: " + msBarCode);
    vpMyString.append(": msInformation =: " + msInformation);
    vpMyString.append(": msErrorCode = " + msErrorCode);
    vpMyString.append(": msSubID = "+ msSubID);
    vpMyString.append(": msMsgData = ").append(msMsgData);
    return vpMyString.toString();
  }

  /**
   * Formats the data string to be published
   * 
   * @param ipStringBuffer <code>StringBuffer</code> to append data to
   * @return the original <code>StringBuffer</code> with appended data
   */
  public StringBuffer formatDataString(StringBuffer ipStringBuffer)
  {
    ipStringBuffer.append(mnMessageID).append(";")
                  .append(msLoadID).append(";")
                  .append(msSourceStation).append(";")
                  .append(msDestinationStation).append(";")
                  .append(msSourceLocation).append(";")
                  .append(msSourceLocnShelfPosition).append(";")
                  .append(msSourceEquipWarehouse).append(";")
                  .append(msDestinationLocation).append(";")
                  .append(msDestLocnShelfPosition).append(";")
                  .append(msDestinationEquipWarehouse).append(";")
                  .append(mnResults).append(";")
                  .append(mnStatus).append(";")
                  .append(mnDimensionInfo).append(";")
                  .append(mnRetrievalPriority).append(";")
                  .append(mnReinputFlag).append(";")
                  .append(mnRetvCmdDetail).append(";")
                  .append(msBarCode).append(";")
                  .append(msInformation).append(";")
                  .append(msErrorCode).append(";")
                  .append(msSubID).append(";")
                  .append(msMsgData).append(";")
                  .append(mnGroupNo).append(";");
    return ipStringBuffer;
  }
  
  /**
   * This method takes all data values and make them into one string with
   * delimiters of ";" and stores that string into the base class dataString
   */
  @Override
  public void createDataString()
  {
    StringBuffer myString = new StringBuffer();
    myString = formatDataString(myString);
    setDataString(myString.toString());
  }

  /**
   * parses the current string and stores the data
   * 
   * @param parser string to continue parsing
   * @return parser string that I just parsed what data I needed
   * @throws Exception error parsing the data
   */
  public StringTokenizer parseDataString(StringTokenizer parser ) throws Exception 
  {
    setMessageID(Integer.parseInt(parser.nextToken()));
    setLoadID(parser.nextToken());
    setSourceStation(parser.nextToken());
    setDestinationStation(parser.nextToken());
    setSourceLocation(parser.nextToken());
    setSourceLocnShelfPosition(parser.nextToken());
    setSourceEquipWarehouse(parser.nextToken());
    setDestinationLocation(parser.nextToken());
    setDestinationLocnShelfPosition(parser.nextToken());
    setDestinationEquipWarehouse(parser.nextToken());
    setResults(Integer.parseInt(parser.nextToken()));
    setStatus(Integer.parseInt(parser.nextToken()));
    setDimensionInfo(Integer.parseInt(parser.nextToken()));
    setPriorityCategory(Integer.parseInt(parser.nextToken()));
    setReinputFlag(Integer.parseInt(parser.nextToken()));
    setRetrievalCommandDetail(Integer.parseInt(parser.nextToken()));
    setBarCode(parser.nextToken());
    setInformation(parser.nextToken());
    setErrorCode(parser.nextToken());
    setSubID(parser.nextToken());
    setMsgData(parser.nextToken());
    setGroupNumber(Integer.parseInt(parser.nextToken()));
    return parser;
  }
  
  /**
   * This method takes the get data string of the base class and decodes it into
   * the individual data members of this class.
   * 
   * @return True is a valid message was decoded else returns false
   */
  @Override
  public boolean decodeDataString()
  {
    StringTokenizer parser = new StringTokenizer(getDataString(), ";");
    boolean validMessage = true;
    try
    {
      clearAllData();
      parseDataString(parser);
    }
    catch(Exception e)
    {
      validMessage = false;
    }
    return validMessage;
  }

  /**
   * This method stores messageID into imessageID field
   * 
   * @param messageID some constant value inside the interface {@link com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants}
   *        in the AGCScheuler-to-AGCStationDevice section or vice-versa.
   */
  public void setMessageID(int messageID)
  {
    mnMessageID = messageID;
  }

  /**
   * Returns the mnMessageID string
   * 
   * @return String value of mnMessageID
   */
  public int getMessageID()
  {
    return mnMessageID ;
  }

  /**
   * Stores the loadID value into msLoadID
   * 
   * @param loadID the value to set the msLoadID to
   */
  public void setLoadID(String loadID)
  {
    String vsLoadID = (loadID == null) ? "" : loadID.trim();

    if (vsLoadID.length() > LNLOADID)
      msLoadID = vsLoadID.substring(0, LNLOADID);
    else
      msLoadID = SKDCUtility.spaceFillTrailing(vsLoadID, LNLOADID);
  }

  /**
   * Trims iLoadID and the returns it.
   * 
   * @return msLoadID the current LoadID value
   */
  public String getLoadID()
  {
    return msLoadID.trim() ;
  }

  /**
   * Sets the msSourceStation with sourceStation.
   * @param  isSourceStation the value to set the msSourceStation to
   */
  public void setSourceStation(String isSourceStation)
  {
    String vsSourceStn = (isSourceStation == null) ? "" : isSourceStation.trim();
    
    if (vsSourceStn.length() > LNSTATION)
      msSourceStation = vsSourceStn.substring(0, LNSTATION);
    else
      msSourceStation = SKDCUtility.spaceFillTrailing(vsSourceStn, LNSTATION);
  }

  /**
   * Trims msSourceStation and then returns it.
   * 
   * @return msSourceStation the current SourceStation value
   */
  public String getSourceStation()
  {
    return msSourceStation.trim();
  }

  /**
   * Sets msDestinationStation to destinationStation
   * 
   * @param destinationStation the value to set the msDestinationStation to
   */
  public void setDestinationStation(String isDestStation)
  {
    String vsDestStn = (isDestStation == null) ? "" : isDestStation.trim();
    
    if (vsDestStn.length() > LNSTATION)
      msDestinationStation = vsDestStn.substring(0, LNSTATION);
    else
      msDestinationStation = SKDCUtility.spaceFillTrailing(vsDestStn, LNSTATION);
  }

  /**
   * Trims msDestinationStation and then returns it.
   * 
   * @return msDestinationStation the current DestinationStation value
   */
  public String getDestinationStation()
  {
    return msDestinationStation.trim();
  }

  /**
   * Sets the msSourceLocation to location
   * 
   * @param isSourceStation the value to set the sLocation to
   */
  public void setSourceLocation(String isSourceLocn)
  {
    String vsSrcLocn = (isSourceLocn == null) ? "" : isSourceLocn.trim();
    
    if (vsSrcLocn.length() > AGCDeviceConstants.LNAGCLOCATION)
      msSourceLocation = vsSrcLocn.substring(0, AGCDeviceConstants.LNAGCLOCATION);
    else
      msSourceLocation = SKDCUtility.spaceFillTrailing(vsSrcLocn,
                                              AGCDeviceConstants.LNAGCLOCATION);
  }

  /**
   * Trims the msSourceLocation and then returns it
   * 
   * @return sLocation the current SourceLocation value
   */
  public String getSourceLocation()
  {
    return msSourceLocation.trim();
  }

  public void setSourceLocnShelfPosition(String isSourceLocnShelfPos)
  {
    if (SKDCUtility.isBlank(isSourceLocnShelfPos))
    {
      msSourceLocnShelfPosition = LoadData.DEFAULT_POSITION_VALUE;
    }
    else
    {
      msSourceLocnShelfPosition = isSourceLocnShelfPos;
    }
  }

  public String getSourceLocnShelfPosition()
  {
    return(msSourceLocnShelfPosition);
  }

  /**
   * Sets the msSourceEquipWarehouse to sourceEquipWarehouse
   * 
   * @param isSourceEquipWarehouse the value to set the sEquipWarehouse to
   */
  public void setSourceEquipWarehouse(String isSourceEquipWarehouse)
  {
    String vsSrcLocn = (isSourceEquipWarehouse == null) ? ""
        : isSourceEquipWarehouse.trim();

    if (vsSrcLocn.length() > AGCDeviceConstants.LNAGCEQUIPWAREHOUSE)
      msSourceEquipWarehouse = vsSrcLocn.substring(0,
          AGCDeviceConstants.LNAGCEQUIPWAREHOUSE);
    else
      msSourceEquipWarehouse = SKDCUtility.spaceFillTrailing(vsSrcLocn,
          AGCDeviceConstants.LNAGCEQUIPWAREHOUSE);
  }

  /**
   * Trims the msSourceLocation and then returns it
   * 
   * @return sLocation the current SourceLocation value
   */
  public String getSourceEquipWarehouse()
  {
    return msSourceEquipWarehouse.trim();
  }

  /**
   * Sets the msDestinationLocation equal to location
   * 
   * @param isDestLocn the value to set the msDestinationLocation to
   */
  public void setDestinationLocation(String isDestLocn)
  {
    String vsDestLocn = (isDestLocn == null) ? "" : isDestLocn.trim();
    
    if (vsDestLocn.length() > AGCDeviceConstants.LNAGCLOCATION)
      msDestinationLocation = vsDestLocn.substring(0,
                                              AGCDeviceConstants.LNAGCLOCATION);
    else
      msDestinationLocation = SKDCUtility.spaceFillTrailing(vsDestLocn,
                                              AGCDeviceConstants.LNAGCLOCATION);
  }

  /** 
   * Trims the msDestinationLocation and then returns it.
   * @return sLocation the current DestinationLocation value
   */
  public String getDestinationLocation()
  {
    return msDestinationLocation.trim();
  }

  public void setDestinationLocnShelfPosition(String isDestLocnShelfPosition)
  {
    if (SKDCUtility.isBlank(isDestLocnShelfPosition))
    {
      msDestLocnShelfPosition = LoadData.DEFAULT_POSITION_VALUE;
    }
    else
    {
      msDestLocnShelfPosition = isDestLocnShelfPosition;
    }
  }

  public String getDestinationLocnShelfPosition()
  {
    return msDestLocnShelfPosition.trim();
  }
  
  /**
   * Sets the msDestinationEquipWarehouse to destinationEquipWarehouse
   * @param  isDestinationEquipWarehouse the value to set the sEquipWarehouse to
   */
  public void setDestinationEquipWarehouse(String isDestinationEquipWarehouse)
  {
    String vsSrcLocn = (isDestinationEquipWarehouse == null) ? ""
        : isDestinationEquipWarehouse.trim();

    if (vsSrcLocn.length() > AGCDeviceConstants.LNAGCEQUIPWAREHOUSE)
      msDestinationEquipWarehouse = vsSrcLocn.substring(0,
          AGCDeviceConstants.LNAGCEQUIPWAREHOUSE);
    else
      msDestinationEquipWarehouse = SKDCUtility.spaceFillTrailing(vsSrcLocn,
          AGCDeviceConstants.LNAGCEQUIPWAREHOUSE);
  }
  
 
  /**
   * sets the group number
   * @param inGroupNo
   */
  public void setGroupNumber(int inGroupNo)
  {
    mnGroupNo = inGroupNo;
  }
  
  /**
   * Return the iGroupNo
   * @return int value of group
   */
  public int getGroupNumber()
  {
    return mnGroupNo;
  }

  /**
   * Trims the msSourceLocation and then returns it
   * 
   * @return sLocation the current SourceLocation value
   */
  public String getDestinationEquipWarehouse()
  {
    return msDestinationEquipWarehouse.trim();
  }
  
  /**
   * This method stores results into mnResults
   * @param  inResults
   */
  public void setResults(int inResults)
  {
    mnResults = inResults;
  }

  /**
   * This method initializes mnResults to 0
   */
  private void setResults()
  {
    mnResults = 0;
  }
  
  /**
   * Returns the mnResults string
   * 
   * @return String value of mnResults
   */
  public int getResults()
  {
    return mnResults ;
  }

  /**
   * This method stores status into mnStatus
   * 
   * @param inStatus
   */
  public void setStatus(int inStatus)
  {
    mnStatus = inStatus;
  }

  /**
   * This method initializes mnStatus to 0
   */
  private void setStatus()
  {
    mnStatus = 0;
  }

  /**
   * Returns the mnStatus string
   * 
   * @return String value of mnStatus
   */
  public int getStatus()
  {
    return mnStatus;
  }

  /**
   * This method stores height into mnDimensionInfo
   * 
   * @param inDimensionInfo the load dimension information.
   */
  public void setDimensionInfo(int inDimensionInfo)
  {
    mnDimensionInfo = inDimensionInfo;
  }

  /**
   * This method initializes mnDimensionInfo to 0
   */
  private void setDimensionInfo()
  {
    mnDimensionInfo = 0;
  }

  /**
   * Returns the mnDimensionInfo string
   * 
   * @return String value of mnDimensionInfo
   */
  public int getDimensionInfo()
  {
    return mnDimensionInfo ;
  }

  /**
   * Sets the msBarCode equal to barCode
   * 
   * @param isBarCode the value to set the msBarCode to
   */
  public void setBarCode(String isBarCode)
  {
    if( isBarCode == null || isBarCode.trim().length() == 0)
    {
      setBarCode();
    }
    else
    {
      msBarCode = isBarCode;
    }
  }

  /**
   * Initializes the msBarCode to blanks
   */
  private void setBarCode()
  {
    int i;
    StringBuffer barCode = new StringBuffer();
    for(i=0; i<DBInfo.getFieldLength(ParameterNameConstants.LOADID); i++ )
    {
      barCode.append(" ");
    }
    msBarCode = barCode.toString();
  }

  /**
   * Trims the msBarCode and then returns it (max LNLOADID characters).
   * 
   * @return msBarCode the current BarCode value
   */
  public String getBarCode()
  {
    int vnBarCodeLength = DBInfo.getFieldLength(ParameterNameConstants.LOADID);
    
    if (msBarCode.length() > vnBarCodeLength)
    {
      return msBarCode.substring(0, vnBarCodeLength).trim();
    }
    else
    {
      return msBarCode.trim();
    }
  }
  
  /**
   * Trims the msBarCode and then returns it.
   * 
   * @return msBarCode the current BarCode value
   */
  public String getFullBarCode()
  {
    return msBarCode.trim();
  }

  public void setPriorityCategory(int inRetrievalPriority)
  {
    mnRetrievalPriority = inRetrievalPriority;
  }

  public int getPriorityCategory()
  {
    return(mnRetrievalPriority);
  }

  public void setReinputFlag(int inReinputFlag)
  {
    mnReinputFlag = inReinputFlag;
  }

  public int getReinputFlag()
  {
    return(mnReinputFlag);
  }

  public void setRetrievalCommandDetail(int inRetvCmdDetail)
  {
    mnRetvCmdDetail = inRetvCmdDetail;
  }

  public int getRetrievalCommandDetail()
  {
    return(mnRetvCmdDetail);
  }
  
  /**
   * Sets the Control Information
   * 
   * @param isInformation the value to set the msInformation to
   */
  public void setInformation(String isInformation)
  {
    String vsInfo = (isInformation == null) ? "" : isInformation.trim();
    
    if (vsInfo.length() > MessageConstants.LNINFORMATION)
      msInformation = vsInfo.substring(0, MessageConstants.LNINFORMATION);
    else
      msInformation = SKDCUtility.spaceFillTrailing(vsInfo,
                                                MessageConstants.LNINFORMATION);
  }

  /**
   * Get the Control Information
   * 
   * @return msInformation the current Information value
   */
  public String getInformation()
  {
    return msInformation;
  }

  /**
   * Set the Sub ID
   * 
   * @param isSubID
   */
  protected void setSubID(String isSubID)
  {
    msSubID = SKDCUtility.spaceFillTrailing(isSubID, 2);
  }

  /**
   * Get the Sub ID
   * 
   * @return
   */
  public String getSubID()
  {
    return msSubID.trim();
  }
  
  /**
   * Set the message data
   * 
   * @param isMsgData
   */
  protected void setMsgData(String isMsgData)    
  {
    msMsgData = isMsgData;
    if (msMsgData.length() == 0) msMsgData = " ";
  }
  
  /**
   * Get the message data
   * 
   * @return
   */
  public String getMsgData()
  {
    return msMsgData.trim();
  }
  
  /**
   * Sets msErrorCode equal to errorCode.
   * 
   * @param isErrorCode the tokenized string of codes that are delimited by "."
   */
  public void setErrorCode(String isErrorCode)
  {
    if (isErrorCode == null)
    {
      msErrorCode = " ";
    }
    else
    {
      msErrorCode = isErrorCode;
    }
  }

  /**
   * Returns msErrorCode a string that is delimited by "."
   * 
   * @return msErrorCode the current ErrorCode value
   */
  public String getErrorCode()
  {
    return msErrorCode ;
  }

  /**
   * Sets the sMessageID to STORELOADSTATIONLOCATION and creates the data string
   * 
   * @param isLoadID LoadId of load to store
   * @param isSourceStationName station where load is currently at.
   * @param isDestinationLocation location where load is to be moved to
   * @param isDestLocnPos The position within the destination location at which
   *        the load will be stored.
   * @param isDestinationEquipWarehouse
   * @param isBarCode
   * @param isControl - control information. null on most systems, may be
   *          populated for custom projects.
   * @param inDimensionInfo - the dimension information for the load
   * @return LoadEvent string to transmit
   */
  public String storeLoadStationLocation(String isLoadID, 
                                         String isSourceStationName,
                                         String isDestinationLocation,
                                         String isDestLocnPos,
                                         String isDestinationEquipWarehouse,
                                         String isBarCode, String isControl,
                                         int inDimensionInfo)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.STORELOADSTATIONLOCATION);
    setSourceStation(isSourceStationName);
    setDestinationLocation(isDestinationLocation);
    setDestinationLocnShelfPosition(isDestLocnPos);
    setDestinationEquipWarehouse(isDestinationEquipWarehouse);
    setLoadID(isLoadID);
    setBarCode(isBarCode);
    setInformation(isControl);
    setDimensionInfo(inDimensionInfo);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to RETRIEVELOADLOCATIONSTATION and creates the data string with the group number
   * @param isLoadID
   * @param isSourceLocn
   * @param isSourceLocnPos
   * @param isSourceEquipWarehouse
   * @param isDestinationStationName
   * @param inPriorityCategory
   * @param inReInputFlag
   * @param inRetrieveCommandDetail
   * @param isBarCode
   * @param isControl
   * @param inDimensionInfo
   * @param inGroupNo
   * @return
   */
  public String retrieveLoadLocationStation(String isLoadID,String isSourceLocn,
      String isSourceLocnPos,
      String isSourceEquipWarehouse,
      String isDestinationStationName,
      int inPriorityCategory,
      int inReInputFlag,
      int inRetrieveCommandDetail,
      String isBarCode, String isControl,
      int inDimensionInfo, int inGroupNo)
 {
    
    clearAllData();
    setMessageID(AGCDeviceConstants.RETRIEVELOADLOCATIONSTATION);
    setSourceLocation(isSourceLocn);
    setSourceLocnShelfPosition(isSourceLocnPos);
    setSourceEquipWarehouse(isSourceEquipWarehouse);
    setDestinationStation(isDestinationStationName);
    setLoadID(isLoadID);
    setBarCode(isBarCode);
    setPriorityCategory(inPriorityCategory);
    setReinputFlag(inReInputFlag);
    setRetrievalCommandDetail(inRetrieveCommandDetail);
    setInformation(isControl);
    setDimensionInfo(inDimensionInfo);
    setGroupNumber(inGroupNo);
    return createStringToSend();
}
  

  /**
   * Sets the sMessageID to MOVELOADSTATIONSTATION and creates the data string
   * 
   * @param isLoadID LoadId of load to move
   * @param isSourceStationName station where load is currently at.
   * @param isDestinationStationName station where load is to be moved to
   * @param isBarCode
   * @param isInformation control information
   * @param inHeight load height information
   * @return LoadEvent string to transmit
   */
  public String moveLoadStationStation(String isLoadID,
      String isSourceStationName, String isDestinationStationName,
      String isDestinationEquipWarehouse, String isBarCode, String isInformation,
      int inHeight )
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.MOVELOADSTATIONSTATION);
    setSourceStation(isSourceStationName);
    setDestinationStation(isDestinationStationName);
    setDestinationEquipWarehouse(isDestinationEquipWarehouse);
    setLoadID(isLoadID);
    setDimensionInfo(inHeight);
    setBarCode(isBarCode);
    setInformation(isInformation);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to MOVELOADLOCATIONLOCATION and creates the data string
   * 
   * @param isLoadID LoadId of load to move
   * @param isSourceLocn location where load is currently at.
   * @param isSourceLocnPosition The position within the source location from which
   *                             load will be retrieved.
   * @param isSourceEquipWarehouse
   * @param isDestLocn location where load is to be moved to
   * @param isDestLocnPosition The position within the destination location at which
   *        the load will be stored.
   * @param isDestEquipWarehouse
   * @param inHeight
   * @return LoadEvent string to transmit
   */
  public String moveLoadLocationLocation(String isLoadID, String isSourceLocn,
                                         String isSourceLocnPosition,
                                         String isSourceEquipWarehouse,
                                         String isDestLocn, String isDestLocnPosition,
                                         String isDestEquipWarehouse,
                                         int inHeight)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.MOVELOADLOCATIONLOCATION);
    setSourceLocation(isSourceLocn);
    setSourceLocnShelfPosition(isSourceLocnPosition);
    setSourceEquipWarehouse(isSourceEquipWarehouse);
    setDestinationLocation(isDestLocn);
    setDestinationLocnShelfPosition(isDestLocnPosition);
    setDestinationEquipWarehouse(isDestEquipWarehouse);
    setLoadID(isLoadID);
    setDimensionInfo(inHeight);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to DESTSTATIONCHANGE and creates the data string
   * 
   * @param isLoadID LoadId of load to move
   * @param isDestinationLocation location where load is to be moved to
   * @param isDestinationStation the reject station.
   * @param isInfo
   * @return LoadEvent string to transmit
   */
  public String rejectLoad(String isLoadID, String isDestinationLocation,
      String isDestinationStation, String isInfo)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.DESTSTATIONCHANGE);
    setDestinationLocation(isDestinationLocation);
    setDestinationStation(isDestinationStation);
    setLoadID(isLoadID);
    setInformation(isInfo);
    return createStringToSend();
  }

   /**
   * Method to format a message 50 as defined baseline AS21 specification.
   * 
   * @param isMsgData the message Data to be passed
   * @return LoadEvent string to transmit
   */
  public String createDataMessage(String isMsgData)
  {
    setMessageID(AGCDeviceConstants.SEND_DATA_MESSAGE);
    setMsgData(isMsgData);

    return createStringToSend();
  }
  
  /**
   * Method to format a message 50 as defined baseline AS21 specification.
   * 
   * @param isMsgData the message Data to be passed
   * @return LoadEvent string to transmit
   */
  public String createResonseToRetrievalTrigger(String isStation, int idResults)
  {
    setMessageID(AGCDeviceConstants.RESPONSETORETRIEVALTRIGGER);
    setSourceStation(isStation);
    setResults(idResults);

    return createStringToSend();
  }
  
  /**
   * Method to format a message 54.  It is used to control the light tower.
   * @param isStation
   * @param isLamp
   * @param inStatus
   * @return
   */
  public String createDOOutputInstruction(String isStation, String isLamp, int inStatus)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.DOOUTPUTINSTRUCTION);
    setSourceStation(isStation);
    setMsgData(isLamp);
    setStatus(inStatus);
    return createStringToSend();
  }

  public String processmsg70Report( String isStation,
          int inRequestType)
  {
	  clearAllData();
	  setMessageID(AGCDeviceConstants.AGCRTSAGCMESSAGEDATA);
	  setSourceStation(isStation);
	  setDimensionInfo(inRequestType);
	  return createStringToSend();
  }
  
  public String processmsg66Report( String isStation)
  {
	  clearAllData();
	  setMessageID(AGCDeviceConstants.AGCDEVICERETRIEVALTRIGGER);
	  setSourceStation(isStation);
	  return createStringToSend();
  }

 /**
  * Format a simple Arrival message for AGCScheduler to process when a load
  * arrives at an AGV station.
  * @param isDestStation the destination AGV station.
  * @param isLoadID the load that arrived at destination.
  * @return formatted string for JMS.
  */
  public String createAGVLoadArrival(String isDestStation, String isLoadID)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGV_LOAD_ARRIVAL);
    setLoadID(isLoadID);
    setSourceStation(isDestStation);

    return createStringToSend();
  }
  
  /**
   * Format a simple Arrival message for AGCScheduler to process when a load
   * arrives at an AGV station.
   * @param isDestStation the destination AGV station.
   * @param isLoadID the load that arrived at destination.
   * @return formatted string for JMS.
   */
   public String createAGVLoadPickupComplete(String isLoadID)
   {
     clearAllData();
     setMessageID(AGCDeviceConstants.AGV_LOAD_PICKUP_COMPLETE);
     setLoadID(isLoadID);

     return createStringToSend();
   }

  /**
   * Sets the sMessageID to GENERALSTORELOAD and creates the data string
   * <P>
   * This message is published when the store or pick screen or whoever is done
   * with the load and we are ready to wait for the equipment to take control of
   * it.
   * </P>
   * 
   * @param isLoadID LoadId of load to move
   * @param isSourceStation station where load is currently at.
   * @return LoadEvent string to transmit
   */
  public String screenLoadRelease(String isLoadID, String isSourceStation)
  {
    clearAllData();
    setLoadID(isLoadID);
    setSourceStation(isSourceStation);
    setMessageID(AGCDeviceConstants.GENERALSTORELOAD);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to RTSAGCOPERCOMP (ID 45) and creates the data string.
   * <P>
   * This message is published when the store or pick screen or whoever is done
   * with the load and we are ready to wait for the equipment to take control of
   * it.
   * </P>
   * 
   * @param isLoadID LoadId of load to move
   * @param isSourceStation station where load is currently at.
   * @return LoadEvent string to transmit
   */
  public String screenOperationComplete(String isLoadID, String isSourceStation)
  {
    clearAllData();
    setLoadID(isLoadID);
    setSourceStation(isSourceStation);
    setMessageID(AGCDeviceConstants.RTSAGCOPERCOMP);  // ID 45
    return createStringToSend();
  }
   
  /**
   * Sets the sMessageID to GENERALLOADARRIVALATSTATION and creates the data
   * string.
   * <P>
   * This message is published to tell the pick screen or whoever that a load
   * has just arrived at a station.
   * </P>
   * 
   * @param isLoadID LoadId of load that arrived at station
   * @param isSourceStation station where load arrived at.
   * @return LoadEvent string to publish
   */
  public String screenLoadArrivedAtStation(String isLoadID,
                                           String isSourceStation)
  {
//    clearAllData();
    setLoadID(isLoadID);
    setSourceStation(isSourceStation);
    setMessageID(AGCDeviceConstants.GENERALLOADARRIVALATSTATION);

    return createStringToSend();
  }

  /**
   * Sets the sMessageID to BINFULLNEWLOC and creates the data string.
   * <P>
   * This message is used when the destination of a load is occupied and we want
   * to send it to a different location.
   * </P>
   * 
   * @param isLoadID LoadId of load needs new location
   * @param isDestLocn alternate location to store load at.
   * @param isShelfPosition The position within location in which to store load.
   * @param isDestEquipWarehouse
   * @param inDimensionInfo
   * @return LoadEvent string to transmit
   */
  public String binFullNewLocation(String isLoadID, String isDestLocn,
                                   String isShelfPosition,
                                   String isDestEquipWarehouse, int inDimensionInfo)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.BINFULLNEWLOC);
    setDestinationLocation(isDestLocn);
    setDestinationLocnShelfPosition(isShelfPosition);
    setDestinationEquipWarehouse(isDestEquipWarehouse);
    setLoadID(isLoadID);
    setDimensionInfo(inDimensionInfo);
    setSourceStation(AGCDeviceConstants.RACKSTATION);
    return createStringToSend();
  }
  
  /**
   * Sets the sMessageID to BINFULLNONEWLOC and creates the data string
   * <P>
   * This message is used when the destination of a load is occupied and we
   * don't want to send a different location
   * </P>
   * 
   * @param isLoadID
   */
  public String binFullNoNewLocation(String isLoadID)
  {
      clearAllData();
      setMessageID(AGCDeviceConstants.BINFULLNONEWLOC);
      setLoadID(isLoadID);
      return createStringToSend();
  }
  
  /**
   * Sets the sMessageID to DIMENSIONWRONGNEWLOC and creates the data string
   * <P>
   * This message is used when the load dimension changes and we want to send it to
   * a different location.
   * </P>
   * 
   * @param isLoadID LoadId of load needs new location
   * @param isDestLocn salternate location to store load at.
   * @param isDestLocnPos The position within location in which to store load.
   * @param inMismatchedDimen the mismatched load dimension (assumed to be 
   *        height and/or length for baseline) for which we are finding an
   *        alternate location.
   * @return LoadEvent string to transmit
   */
  public String binDimensionMismatchNewLocation(String isLoadID, String isDestLocn,
                                                String isDestLocnPos,
                                                int inMismatchedDimen)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.DIMENSIONWRONGNEWLOC);
    setDestinationLocation(isDestLocn);
    setDestinationLocnShelfPosition(isDestLocnPos);
    setLoadID(isLoadID);
    setDimensionInfo(inMismatchedDimen);
    setSourceStation(AGCDeviceConstants.RACKSTATION);
    return createStringToSend();
  }
   
  /**
   * Sets the sMessageID to BINFULLMOVESTATION and creates the data string.
   * <P>
   * This message is published when the destination of a load is occupied and we
   * want to just send the load to a reject station and figure out there what to
   * do with it.
   * </P>
   * 
   * @param isLoadID LoadId of load needs to move to reject station
   * @param isRejectStation reject station to move load to.
   * @param inLoadDimen the load dimension (assumed to be height and/or length
   *        for baseline) for which we are finding an alternate location.
   * @return LoadEvent string to transmit
   */
  public String binFullMoveStation(String isLoadID, String isRejectStation,
                                   int inLoadDimen)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.BINFULLMOVESTATION);
    setDestinationStation(isRejectStation);
    setDimensionInfo(inLoadDimen);
    setLoadID(isLoadID);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to BINFULLMOVESTATION and creates the data string.
   * <P>
   * This message is published when the destination of a load is occupied and we
   * want to just send the load to a reject station and figure out there what to
   * do with it.
   * </P>
   * 
   * @param isLoadID LoadId of load needs to move to reject station
   * @param isRejectStation reject station to move load to.
   * @param inMisMatchDimen the mismatched dimension of the load being moved.
   * @return LoadEvent string to transmit
   */
  public String binDimensionMismatchMoveStation(String isLoadID,
                                             String isRejectStation, int inMisMatchDimen)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.DIMENSIONWRONGMOVESTATION);
    setDestinationStation(isRejectStation);
    setDimensionInfo(inMisMatchDimen);
    setLoadID(isLoadID);
    return createStringToSend();
  }
   
  /**
   * Sets the sMessageID to BINEMPTYCANCEL and creates the data string.
   * <P>
   * This message is used when we requested a load from a location and the load
   * isn't there. So we just cancel then command to get the load.
   * </P>
   * 
   * @param isLoadID LoadId of load command to cancel
   * @param isAltRetrieveLocation alternate location from which to retrieve a load.
   * @param isAltLocnPos position within alternate location from which to retrieve load.
   * @param inHeight the location height.
   * @return LoadEvent string to transmit
   */
  public String binEmptyCancel(String isLoadID, String isEquipWarehouse,
      String isAltRetrieveLocation, String isAltLocnPos, int inHeight)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.BINEMPTYCANCEL);
    setLoadID(isLoadID);
    setDestinationEquipWarehouse(isEquipWarehouse);
    setDestinationLocation(isAltRetrieveLocation);
    setDestinationLocnShelfPosition(isAltLocnPos);
    setDimensionInfo(inHeight);
    setSourceStation(AGCDeviceConstants.RACKSTATION);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICERESPTRANSPORTDATACANCEL and creates the
   * data string
   * 
   * @param isLoadID LoadId of load command to cancel
   * @param inResults results of the response
   * @return formatted string to send.
   */
  public String responseTransportDataCancel(String isLoadID, int inResults)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICERESPTRANSPORTDATACANCEL);
    setLoadID(isLoadID);
    setResults(inResults);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICERESPTRANSPORTCOMMAND and creates the data
   * string
   * 
   * @param isLoadID LoadId of load command to cancel
   * @param inResults results of the response
   * @return LoadEvent string to transmit
   */
  public String responseToTransportCommand(String isLoadID, int inResults)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICERESPTRANSPORTCOMMAND);
    setLoadID(isLoadID);
    setResults(inResults);
    return createStringToSend();
  }
  

  /**
   * Sets the sMessageID to AGCDEVICEARRIVALREPORT and returns the data string
   *
   * @param isLoadID LoadId of load that has arrived '99999999' dummy arrival if
   *          any other number other than '00000000' it is the final arrival
   * @param isStation where load arrived at
   * @param inDimensionInfo of the load
   * @param inLoadInfo Load presence info. 0 = no load presence, 1 = load presence.
   * @param isBarCode of the load
   * @param isInfo Control information of the load
   * @return LoadEvent string to transmit
   */
  public String processArrivalReport(String isLoadID, String isStation,
                                     int inDimensionInfo, int inLoadInfo,
                                     String isBarCode, String isInfo)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICEARRIVALREPORT);
    setLoadID(isLoadID);
    setSourceStation(isStation);
    setDimensionInfo(inDimensionInfo);
    setResults(inLoadInfo);
    setBarCode(isBarCode);
    setInformation(isInfo);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICEREQDESTSTATCHG and creates the data string
   * 
   * @param isLoadID LoadId of load command to change station
   * @param isLocation location of the load.
   * @param isSourceLocnShelfPos Position on shelf that load is being retrieved from
   *        (provided this is a location to station move -- if this is a
   *        station-to-station move rejection, this field is likely blank.)
   * @param isStation station where load now needs to go
   * @param isInfo information of the load
   * @return LoadEvent string to transmit
   */
  public String requestDestinationStationChg(String isLoadID, String isLocation,
                                             String isSourceLocnShelfPos,
                                             String isStation, String isInfo)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICEREQDESTSTATCHG);
    setLoadID(isLoadID);
    setDestinationStation(isStation);
    setSourceLocation(isLocation);
    setSourceLocnShelfPosition(isSourceLocnShelfPos);
    setInformation(isInfo);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICERESPDESTSTATCHG and creates the data string
   * 
   * @param isLoadID LoadId of load command to change station
   * @param inResults indicates if the destination station change request was
   *        successful.  A zero indicates new station chosen by Wrx was accepted.
   * @param isStation station where load now needs to go
   * @param isLocation location of the load
   * @param isSourceLocnShelfPos Position on shelf that load is being retrieved from
   *        (provided this is a location to station move -- if this is a
   *        station-to-station move rejection, this field is likely blank.)
   * @param inDimension information of the load
   * @return LoadEvent string to transmit
   */
  public String responseDestinationStationChg(String isLoadID, int inResults,
                                              String isLocation,
                                              String isSourceLocnShelfPos,
                                              String isStation, int inDimension)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICERESPDESTSTATCHG);
    setLoadID(isLoadID);
    setResults(inResults);
    setDestinationStation(isStation);
    setSourceLocation(isLocation);
    setSourceLocnShelfPosition(isSourceLocnShelfPos);
    setDimensionInfo(inDimension);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICEALTLOCCOMMANDRESP and creates the
   * data string
   * 
   * @param isLoadID LoadId of load command to change locationn
   * @param inResults results of the response
   * @return LoadEvent string to transmit
   */
  public String processAlternateLocation(String isLoadID, int inResults)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICEALTLOCCOMMANDRESP);
    setLoadID(isLoadID);
    setResults(inResults);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICERESPRETRIEVECMD and creates the data string
   * 
   * @param isLoadID LoadId of load command to retrieve
   * @param inResults results of the response
   * @return LoadEvent string to transmit
   */
  public String processRetrieveCommandResponse(String isLoadID, int inResults)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICERESPRETRIEVECMD);
    setLoadID(isLoadID);
    setResults(inResults);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICEOPERATIONCOMPLETION and creates the
   * data string
   * 
   * @param isLoadID LoadId that has completed operation
   * @param inTransClass the Transportation classification
   * @param inCompletionClass results of the operation
   * @param isSourceStation source station of the load if a store
   * @param isDestStation destination station of load if a retrieve
   * @param isLocation location of load if a retrieve or store
   * @param isSourceLocnShelfPos
   * @param inDimension dimension of the load.  This is relevant only if
   * <code>inCompletionClass</code> is set to 3 by the SRC (which normally tells
   * us of a location size mismatch for this load).
   * @param isShelfLocation destination/from location for location-to-location move.
   * @param isBarCode barcode of the load
   * @param isDestLocnShelfPos
   * @param isWorkNumber worknumber of the load
   * @param isInfo information of the load
   * @return LoadEvent string to transmit
   */
  public String processOperationCompletion(String isLoadID, int inTransClass,
                                           int inCompletionClass,
                                           String isSourceStation,
                                           String isDestStation, String isLocation,
                                           String isSourceLocnShelfPos,
                                           String isShelfLocation,
                                           String isDestLocnShelfPos, int inDimension,
                                           String isBarCode, String isWorkNumber,
                                           String isInfo)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICEOPERATIONCOMPLETION);
    setLoadID(isLoadID);
    setStatus(inTransClass);
    setResults(inCompletionClass);
    setSourceStation(isSourceStation);
    setDestinationStation(isDestStation);
    setSourceLocation(isLocation);
    setSourceLocnShelfPosition(isSourceLocnShelfPos);
    setDestinationLocation(isShelfLocation);
    setDestinationLocnShelfPosition(isDestLocnShelfPos);
    setDimensionInfo(inDimension);
    setBarCode(isBarCode);
    setErrorCode(isWorkNumber);
    setInformation(isInfo);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICETRANDATADELREPORT and creates the data
   * string
   * 
   * @param isLoadID LoadId of transport command
   * @param inResults the cause of deletion
   * @param isStation destination of the load
   * @param isInfo information of the load
   * @return LoadEvent string to transmit
   */
  public String processTransportDeletion(String isLoadID, int inResults,
                                         String isStation, String isInfo)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICETRANDATADELREPORT);
    setLoadID(isLoadID);
    setResults(inResults);
    setDestinationStation(isStation);
    setInformation(isInfo);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICEPICKCOMPREPORT and creates the data string
   * 
   * @param isLoadID LoadId that was moved
   * @param isStation station of the load
   * @return LoadEvent string to transmit
   */
  public String processPickupCompletionReport(String isLoadID, String isStation)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICEPICKCOMPREPORT);
    setLoadID(isLoadID);
    setSourceStation(isStation);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICEPICKCOMPREPORT and creates the data string
   * 
   * @param isStation station that request retrieval
   * @return LoadEvent string to transmit
   */
  public String processRetrievalTrigger(String isStation)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICERETRIEVALTRIGGER);
    setSourceStation(isStation);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to AGCDEVICETRANDATADELREPORT and creates the data
   * string
   * 
   * @param isLoadID LoadId of transport/Retrieval command
   * @param isStation destination of the load
   * @param isInfo information of the load
   * @return LoadEvent string to transmit
   */
  public String processTriggerOfOperationIndication(String isLoadID,
                                                String isStation, String isInfo)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICETRIGGEROPERINDICAT);
    setLoadID(isLoadID);
    setSourceStation(isStation);
    setDestinationStation(isStation);
    setInformation(isInfo);
    return createStringToSend();
  }

  /**
   * Sets the sMessageID to SEND_DATA_MESSAGE and creates the data string
   * 
   * @param isStationName
   * @return
   */
  public String sendDataMessage(String isMessage)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.SEND_DATA_MESSAGE);
    setMsgData(isMessage);
    return createStringToSend();
  }

  /**
   * Tests the loadID data to see if it is equal to AGCDUMMYLOAD
   * 
   * @return true if the loadID data is equal to AGCDUMMYLOAD
   */
  public boolean isLoadDummyLoad()
  {
    if(getLoadID().equals(AGCDeviceConstants.AGCDUMMYLOAD))
    {
      return true;  // this is a dummy load
    }
    else
    {
      return false;   // This is a valid loadid
    }
  }

  /**
   * Tests the BCR data to see if it is equal to ZEROBCRFIELD or is it is blank
   * 
   * @return true if the BCR data is equal a valid BCR ie not zero filled or
   *         blank
   */
  public boolean isBCRValid()
  {
    if( (getBarCode().equals(AGCDeviceConstants.ZEROBCRFIELD)) ||
          (getBarCode().length() == 0))
    {
      return false;  // this is not a valid BCR
    }
    else
    {
      return true;   // This is a valid BCR
    }
  }

  /**
   * Tests the BCR data to see if it is a bad read or a no read
   * 
   * @return true if the BCR data is a bad read or a no read
   */
  public boolean isBCRGoodRead()
  {
    String vsBarcode = getBarCode();
    if ((vsBarcode.equalsIgnoreCase(AGCDeviceConstants.BR_BARCODE)) ||
        (vsBarcode.equalsIgnoreCase(AGCDeviceConstants.NOREAD_BARCODE)) ||
        (vsBarcode.equalsIgnoreCase(AGCDeviceConstants.NR_BARCODE)))
    {
      return false;  // this is A Bad Read
    }
    else
    {
      return true;   // This is a Good Read
    }
  }

  /**
   * This takes a final arrival message and changes the bar code to the load id
   * and sets the load id to '99999999' to make a dummy arrival
   */
  public void changeFinalArrivalToStoreArrival()
  {
    setBarCode(getLoadID());
    setLoadID(AGCDeviceConstants.AGCDUMMYLOAD);
  }
}