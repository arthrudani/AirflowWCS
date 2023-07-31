package com.daifukuoc.wrxj.custom.ebs.plc.messages.util;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.regex.Pattern;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

public class PLCMessageUtil {

	private final static Pattern patternNumeric = Pattern.compile("-?\\d+(\\.\\d+)?");
	
	public static byte[] buildMessageHeader( short mwMsgType, int mnMsgLength, int equimpmentId, short mnSeqNum , short mwMsgVer) {

	  	 // Build Message Header
		byte[] mabMsg = new byte[PLCConstants.MSG_HEADER_LEN];	
	    ByteBuffer buffer = ByteBuffer.wrap(mabMsg);		
	    
    	 // Message length Header Field1
	    short vwTotalMsgSize = (short) (mnMsgLength + PLCConstants.MSG_HEADER_LEN);
		buffer.putShort(vwTotalMsgSize);
		
		// Sequence # Header Field2
	    buffer.putShort(mnSeqNum);
	    
	     // MsgType Header Field3
	    buffer.putShort(mwMsgType);
	    
	    // Equipment ID Header Field4
	    buffer.putInt(equimpmentId);
	   
		 // Get local time
		 LocalTime localTime = LocalTime.now();
		 short vwHours = (short) localTime.getHour();
		 short vwMinutes = (short) localTime.getMinute();
		 short vwMilliseconds = (short) (localTime.getSecond() * 1000) ; //KR:TODO: need to add Milliseconds   ((seconds x 1000)+ milliseconds)
		 
	     //  Hours Header Field5
		 buffer.put((byte)vwHours);
	     //  Minutes Header Field6
		 buffer.put((byte)vwMinutes); 
		 
	     // Milliseconds Header Field7
		 buffer.putShort(vwMilliseconds);

	     //Message version number Field8	
		 buffer.putShort(mwMsgVer);
	     return(mabMsg);
	  }


	public static String getStringFromBuffer(byte[] receivedBuffer,int iStartPosition,int iStringLen)
	 {
		 byte[] tmpBuf = new byte[iStringLen];
		 System.arraycopy(receivedBuffer,iStartPosition, tmpBuf, 0, iStringLen );
		 
	     String vsItem =new String(tmpBuf);
	     return vsItem.trim();
	 }

	
	 /**
	  * Converts provided string to fixed size char array. 
	  * @param st
	  * @param requiredSize
	  * @return Char array
	  */
	 public static char[] convertSringToFixSizeCharArray(String st,int requiredSize)
	 {
		 
		 if(st.isEmpty() || requiredSize == 0)
		 {
			 return null;
		 }
		 int stringLen = st.length();
		 if(stringLen == requiredSize) {
			return st.toCharArray();
		 }else if(stringLen > requiredSize)
		 {
			 //truncate to required size 
			return st.substring(0, requiredSize).toCharArray();
			 
		 }else if(stringLen < requiredSize)
		 {
			 //add SPACE value to end of string
			 int diff =requiredSize - stringLen;
			 for(int i=0; i < diff;i++)
			 {
				 st += " ";
			 }
			 return st.toCharArray();
		 }
		 return null;
	 }
	 
	 public static boolean isNumeric(String strNum)
	 {
		 if (strNum == null) {
		        return false; 
		    }
		 return patternNumeric.matcher(strNum).matches();
	 }
	 
	 public static String formatThisDate(Date date)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return dateFormat.format(date);
	}
}
