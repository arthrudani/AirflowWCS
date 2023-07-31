package com.daifukuoc.wrxj.custom.ebs.jdbc;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.device.controls.ControlsMessageInterface;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusInfo;

import java.util.List;

public class BCSMessage implements ControlsMessageInterface
{
	
// Message types
public static final String BCS_TRAY_RELEASE_REQUEST				    = "10";  // WRx <- BCS, Tray Release Request
public static final String BCS_TRAY_RELEASE_RESPONSE				= "10";  // WRx -> BCS, Tray Release Response
public static final String BCS_TRAY_ARRIVAL_CONFIRMATION			= "16";  // WRx <- BCS, Tray Arrival Confirmation
public static final String BCS_STORAGE_STATION_STATUS				= "17";  // WRx -> BCS, Storage Station Status
public static final String BCS_AISLE_REQUEST						= "18";  // WRx <- BCS, Aisle Request
public static final String BCS_AISLE_RESPONSE						= "18";  // WRx -> BCS, Aisle Response
public static final String BCS_TRAY_DESTINATION						= "20";  // WRx -> BCS, Tray destination

public static final String BCS_OSS_REQUEST						= "55";  // WRx <- BCS, OutputStationStatus Request
public static final String BCS_OSS_RESPONSE						= "55";  // WRx -> BCS, OutputStationStatus Response
  
//Message types
public static final int BCS_TRAY_RELEASE_REQUEST_INT				= 10;  // WRx <- BCS, Tray Release Request
public static final int BCS_TRAY_RELEASE_RESPONSE_INT			    = 10;  // WRx -> BCS, Tray Release Response
public static final int BCS_TRAY_ARRIVAL_CONFIRMATION_INT			= 16;  // WRx <- BCS, Tray Arrival Confirmation
public static final int BCS_STORAGE_STATION_STATUS_INT			    = 17;  // WRx -> BCS, Storage Station Status
public static final int BCS_AISLE_REQUEST_INT						= 18;  // WRx <- BCS, Aisle Request
public static final int BCS_AISLE_RESPONSE_INT						= 18;  // WRx -> BCS, Aisle Response
public static final int BCS_TRAY_DESTINATION_INT					= 20;  // WRx -> BCS, Tray destination
public static final int BCS_OSS_REQUEST_INT							= 55;  // WRx <- BCS, OutputStationStatus Request
public static final int BCS_OSS_RESPONSE_INT						= 55;  // WRx -> BCS, OutputStationStatus Response

//Message Length, minus Header length
public static final int BCS_TRAY_ARRIVAL_CONFIRMATION_LENGTH			= 20;  // WRx <- BCS, Tray Arrival Confirmation
public static final int BCS_AISLE_REQUEST_LENGTH						= 44;  // WRx <- BCS, Aisle Request
public static final int BCS_TRAY_RELEASE_REQUEST_LENGTH					= 18;  // WRx <- BCS, Tray Release Request
public static final int BCS_TRAY_RELEASE_RESPONSE_LENGTH				= 20;  // WRx -> BCS, Tray Release Response
public static final int BCS_STORAGE_STATION_STATUS_LENGTH				= 20;  // WRx -> BCS, Tray Release Response
public static final int BCS_AISLE_RESPONSE_LENGTH						= 24;  // WRx -> BCS, Aisle Response
public static final int BCS_TRAY_DESTINATION_LENGTH					    = 34;  // WRx -> BCS, Tray destination 
public static final int BCS_OSS_REQUEST_LENGTH					    	= 16;  // WRx -> BCS, OutputStationStatus
public static final int BCS_OSS_RESPONSE_LENGTH					   		= 16;  // WRx -> BCS, OutputStationStatus
  
  // Internal fields
  private String  msMessageText = null;
  private boolean mzIsValid = false;
  private String  msInvalidReason = null;
  
  // Message header data fields
  private String msTelegramNumber = " ";
  private String msLength    = " ";   
  private String msSource = " ";	
  private String msDestination = " ";	
  
 //Message body data fields
 private String msData = " ";				// Message Body
 private String msMsgTyp = " ";				// Message ID
 
  
  
  // Fake equipment status
  private StatusEventDataFormat mpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
  
  /**
   * public constructor for Factory
   */
  public BCSMessage()
  {
  }
  
  /**
   * Parse and interpret the passed-in message text into fields defined for that
   * message type.
   *
   * @param isMessageString the message to decode
   */
//  @Override
  public void toDataValues(String isMessageString)
  {
    try
    {
      mzIsValid = true;
      msInvalidReason = null;
      msMessageText = isMessageString;
      
      // Parse the message
      msMsgTyp    = isMessageString.substring(0, 2);
    }
    catch (Exception e)
    {
      mzIsValid = false;
      msInvalidReason = e.getMessage() + " parsing " + isMessageString;
    }
  }


 

  /**
   * Convert the current command to a string for transmission
   */
//  @Override
  public String getMessageAsString()
  {
    msMessageText = msTelegramNumber + msLength + msSource + msDestination + msMsgTyp + msData; 
    return msMessageText;
  }

  /**
   * Get the parsed message string
   */
//  @Override
  public String getParsedMessageString()
  {
    String s = "";

    if (msMsgTyp.equals(BCS_TRAY_RELEASE_REQUEST))   s += " BCS Tray Release Reqeust ";
    else if (msMsgTyp.equals(BCS_TRAY_ARRIVAL_CONFIRMATION))   s += " BCS Tray Arrival Confirmation ";
    else if (msMsgTyp.equals(BCS_AISLE_REQUEST))   s += " BCS Aisle Request ";
    else s += "UNKNOWN:" + msMsgTyp + " ";
    
    return s;
  }
  
  /**
   * Is this a valid message?
   * @return
   */
//  @Override
  public boolean getValidMessage()
  {
    return mzIsValid;
  }
  
  /**
   * Get the description of why the message is invalid
   * @return String if message is invalid, null if message is valid
   */
//  @Override
  public String getInvalidMessageDescription()
  {
    return msInvalidReason;
  }

  /**
   * Initialize Equipment Statuses
   * @param iasStations
   */
  public void initializeEquipmentStatus(String isDevice)
  {
    mpSEDF.addEquipmentStatus(isDevice, "Conveyor", isDevice, "Unknown",
          "Unknown", "*NONE*", "Now");
  }
  
  /**
   * Set the equipment status
   * @param isStatus
   */
//  @Override
  public void setEquipmentStatus(String isStatus)
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    List<StatusInfo> vpStatuses = mpSEDF.getStatusList();
    for (StatusInfo vpSI : vpStatuses)
    {
      vpSEDF.addEquipmentStatus(vpSI.getMachineID(), vpSI.getMachineType(), 
          vpSI.getMachineNo(), vpSI.getMachineStat(), isStatus, 
          vpSI.getMachineError(), "NOW");
    }
    mpSEDF = vpSEDF;
  }
  
  /**
   * Get the equipment status report
   * @return
   */
  public String getEquipmentStatusReport()
  {
    mpSEDF.setType(ControllerConsts.EQUIPMENT_STATUS);
    return mpSEDF.createStringToSend();
  }

  /*========================================================================*/
  /*  Message Field Getters                                                 */
  /*========================================================================*/
  public String getMessageID() 			{ return msMsgTyp; }
  public String getTelegramNumber()     { return msTelegramNumber; }
  public String getLength()     		{ return msLength; }
  public String getSource()   			{ return msSource; }
  public String getDestination()    	{ return msDestination; }
  public String getMsgTyp()    			{ return msMsgTyp; }
  public String getData()      			{ return msData; }

  //-----------------------
  	 
//Create and send the message as a delimited string, the BCSDevicePort will parse send
public String getTrayArrivalConfirmationCommand(String isLoadid, String isStoreStation, int iTrayStatus  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_TRAY_ARRIVAL_CONFIRMATION;
	  msg =  vsTelegramType + "," + isLoadid + "," + isStoreStation + "," + iTrayStatus;
	  
	  return msg;
}
 
public String getAisleRequestCommand(  String sRequestID, String sLoadid, int iTrayType, int iTrayStatus, 
  		String sGlobalID, String sItem, int iHeight  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_AISLE_REQUEST;
	  msg =  vsTelegramType + "," + sRequestID + "," + sLoadid + "," + iTrayType + 
			  "," + iTrayStatus + "," + sGlobalID + "," + sItem + "," + iHeight ;
	  
	  return msg;
}
 
 
public String getTrayReleaseRequestCommand( int iQty, int iDestinationID, String sRequestID  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_TRAY_RELEASE_REQUEST;
	  msg =  vsTelegramType + "," + iQty + "," + iDestinationID + "," + sRequestID;
	  
	  return msg;
}



public String getOutputStationStatusRequestCommand( int iDestinationID, int iStatus  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_OSS_REQUEST;
	  msg =  vsTelegramType +  "," + iDestinationID + "," + iStatus;
	  
	  return msg;
}


public String getOutputStationStatusResponseCommand( int iDestinationID, int iStatus  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_OSS_RESPONSE;
	  msg =  vsTelegramType +  "," + iDestinationID + "," + iStatus;
	  
	  return msg;
}


public String getTrayReleaseResponseCommand( int iDestinationID, int iReqQty, int iSentQty, String sRequestID  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_TRAY_RELEASE_RESPONSE;
	  msg =  vsTelegramType + "," + iDestinationID + "," + iReqQty + "," + iSentQty + "," + sRequestID;
	  
	  return msg;
}


public String getOSSResponseCommand( int iDestinationID, int iStatus  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_OSS_RESPONSE;
	  msg =  vsTelegramType + "," + iDestinationID + "," + iStatus;
	  
	  return msg;
}

public String getStorageStationStatusCommand( String sStationID, String sStatus, String sCount, String sTrayStatus    ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_STORAGE_STATION_STATUS;
	  msg =  vsTelegramType + "," + sStationID + "," + sStatus + "," + sCount + "," + sTrayStatus;
	  
	  return msg;
}

public String getAisleResponseCommand( String sRequestID, String sTrayID, String sStorageInputId, String sAltStorageID, String sErrorCode ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_AISLE_RESPONSE;
	  msg =  vsTelegramType + "," + sRequestID + "," + sTrayID + "," + sStorageInputId + "," + sAltStorageID + "," + sErrorCode;
	  
	  return msg;
}

public String getTrayDestinationCommand(  String sTrayID, String sDestID, String sItem, String sTrayStatus, String sOutputStation ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_TRAY_DESTINATION;
	  msg =  vsTelegramType + "," + sTrayID + "," + sDestID + "," + sItem + "," + sTrayStatus + "," + sOutputStation;
	  
	  return msg;
}

 //-----------------------
 
  //Create and send the message as a delimited string, the BCSDevicePort will parse and send

public String getTrayArrivalConfirmationLogString( String isLoadid, String isStoreStation, int iTrayStatus  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_TRAY_ARRIVAL_CONFIRMATION;
	  msg = vsTelegramType + " TRAY_ARRIVAL_CONFIRMATION, TrayID " + isLoadid + ", StoreStation =" + isStoreStation + ", TrayStatus=" + iTrayStatus;
	  
	  return msg;
}

public String getAisleRequestLogString(  String sRequestID, String sLoadid, int iTrayType, int iTrayStatus, 
  		String sGlobalID, String sItem, int iHeight  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_AISLE_REQUEST;
	  msg = vsTelegramType + " AISLE_REQUEST, RequestID =" + sRequestID + ", TrayID =" + sLoadid + ", TrayType=" + iTrayType +
			  ", TrayStatus =" + iTrayStatus + ", GlobalID =" + sGlobalID + ", Item =" + sItem + ", Height =" + iHeight ;
	  
	  return msg;
}
  
public String getTrayReleaseRequestLogString( int iQty, int iDestinationID, String sRequestID  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_TRAY_RELEASE_REQUEST;
	  msg = vsTelegramType + " TRAY_RELEASE_REQUEST, ReqQty =" + iQty + ", DestinationID =" + iDestinationID + ", RequestID =" + sRequestID;
	  
	  return msg;
}

public String getOutputStationStatusRequestLogString(  int iDestinationID, int iStatus  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_OSS_REQUEST;
	  msg = vsTelegramType + " OUTPUT_STATION_STATUS,  DestinationID =" + iDestinationID + ", Status =" + iStatus;
	  
	  return msg;
}

public String getOutputStationStatusResponseLogString(  int iDestinationID, int iStatus  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_OSS_RESPONSE;
	  msg = vsTelegramType + " OUTPUT_STATION_STATUS,  DestinationID =" + iDestinationID + ", Status =" + iStatus;
	  
	  return msg;
}


public String getTrayReleaseResponseLogString( int iDestinationID, int iReqQty, int iSentQty, String sRequestID   ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_TRAY_RELEASE_RESPONSE;
	  msg = vsTelegramType + " TRAY_RELEASE_RESPONSE, iDestinationID =" + iDestinationID + ", ReqQty =" + iReqQty + ", SentQty =" + iSentQty + ", RequestID =" + sRequestID;
	  
	  return msg;
}


public String getOSSResponseLogString( int iDestinationID, int iStatus ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_OSS_RESPONSE;
	  msg = vsTelegramType + " OUTPUT_STATION_STATUS_RESPONSE, iDestinationID =" + iDestinationID + ", Status =" + iStatus;
	  
	  return msg;
}

public String getStorageStationStatusLogString( String sStationID, String sStatus, String sCount, String sTrayStatus  ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_STORAGE_STATION_STATUS;
	  msg = vsTelegramType + " STORAGE_STATION_STATUS, stationID =" + sStationID + ", Status =" + sStatus + ", Count=" + sCount + ", TrayStatus=" + sTrayStatus;
	  
	  return msg;
}

public String getAisleResponseLogString( String sRequestID, String sTrayID, String sStorageInputId, String sAltStorageID, String sErrorCode   ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_AISLE_RESPONSE;
	  msg = vsTelegramType + " AISLE_RESPONSE, RequestID =" + sRequestID + ", TrayID =" + sTrayID + ", StorageInputId =" + sStorageInputId + ", AltStorageID =" + sAltStorageID + ", ErrorCode =" + sErrorCode;
	  
	  return msg;
}


public String getTrayDestinationLogString(  String sTrayID, String sDestID, String sItem, String sTrayStatus, String sOutputStation   ) 
{
	  String msg = "";
	  String vsTelegramType = BCS_TRAY_DESTINATION;
	  msg = vsTelegramType + " BCS_TRAY_DESTINATION, TrayID =" + sTrayID   + ", DestID =" + sDestID + ", OutputStation =" + sOutputStation  + ", BagID =" + sItem  + ", TrayStatus =" + sTrayStatus;
	  
	  return msg;
}

}
