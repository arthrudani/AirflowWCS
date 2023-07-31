package com.daifukuoc.wrxj.custom.ebs.plc.messages;

import java.util.List;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.device.controls.ControlsMessageInterface;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusInfo;

public class PLCMessageData implements ControlsMessageInterface
{
	// Internal fields
	private String  msMessageText = null; //message
	private boolean mzIsValid = false;
	private String  msInvalidReason = null;
  
	// Message header data fields
	private String msTelegramNumber = " "; //msg header
	private String msLength    = " ";   
	private String msSource = " ";	
	private String msDestination = " ";	
	private String msDeviceId="";
  
	//Message body data fields
	private String msData = " ";				// Message Body
	private String msMsgType = " ";				// Message ID
	  // Fake equipment status
	private StatusEventDataFormat mpSEDF = new StatusEventDataFormat(getClass().getSimpleName());

  /**
   * public constructor for Factory
   */
  public PLCMessageData()
  {
  }
  
	@Override
	public void toDataValues(String isMessageString) {

	 try
	    {
	      mzIsValid = true;
	      msInvalidReason = null;
	      msMessageText = isMessageString;
	      
	      // Parse the message
	      msMsgType    = isMessageString.substring(0, 2);
	    }
	    catch (Exception e)
	    {
	      mzIsValid = false;
	      msInvalidReason = e.getMessage() + " parsing " + isMessageString;
	    }
	      
		
	}
	
	
	
	@Override
	public String getMessageAsString() {		 
		 return msMessageText;
	}
	
	
	
	@Override
	public String getParsedMessageString() {
		String s = "";
		switch(msMsgType )
		{
			case PLCConstants.PLC_ITEM_STORED_MSG_TYPE:
				s += " PLC Storage Complete ";
				break;
			case PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE:
				s += " PLC Flush Request ";
				break;
			case PLCConstants.PLC_ITEM_PICKEDUP_MSG_TYPE:
				s += " PLC Item Picked Up ";
				break;
			case PLCConstants.PLC_LOCATION_STATUS_MSG_TYPE:
				s += " PLC Location Status ";
				break;
			case PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE:
				s += " PLC Item Arrived ";
				break;
			case PLCConstants.PLC_ITEM_RELEASED_MSG_TYPE:
				s += " PLC Item Released ";
				break;
			case PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE:
				s += " PLC Move Order Request ";
				break;
			case PLCConstants.PLC_BAG_DATA_UPDATE_MSG_TYPE:
				s += " PLC Bag Data Update ";
				break;
				
			default:
				s += "UNKNOWN:" + msMsgType + " ";
				break;
				
		}
	    return s;
	}
	
	
	
	@Override
	public boolean getValidMessage() {
		 return mzIsValid;
	}
	
	
	
	@Override
	public String getInvalidMessageDescription() {
		return msInvalidReason;
	}
	
	
	
	@Override
	public String getEquipmentStatusReport() {
		
	    mpSEDF.setType(ControllerConsts.EQUIPMENT_STATUS);
	    return mpSEDF.createStringToSend();
	}
	
	
	@Override
	public void setEquipmentStatus(String isStatus) {
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
  
	public String getMessageId()
	{
		return msMsgType;
	}
	
	public String getDeviceId() {
		return msDeviceId;
	}
	public void setDeviceId(String sDeviceId)
	{
		msDeviceId = sDeviceId;
	}
}
