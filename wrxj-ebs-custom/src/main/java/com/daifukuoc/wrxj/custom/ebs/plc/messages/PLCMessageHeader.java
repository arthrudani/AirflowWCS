package com.daifukuoc.wrxj.custom.ebs.plc.messages;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;

  public class PLCMessageHeader extends AbstractSKDCData
  {
    
	  private int iMsgType = 0;
	  private String sEquipmentID = "";
	  private int iSeqNo = 0;
	  private int iHours = 0;
	  private int iMinutes = 0;
	  private int iMilliSeconds = 0;
	  private int iMsgLength = 0;
	  private int iMsgVersion = 0;
	  private String sSeparater = ","; 

    /**
     * Default constructor. This constructor finds the correct message formatter
     * to use for this message.
     */
    public PLCMessageHeader()
    {
      
    }
   
    /**
     * This helps in debugging when we want to print the whole structure.
     */
    @Override
    public String toString()
    {
    	 String s =iMsgType + sSeparater + sEquipmentID   + sSeparater + iSeqNo + sSeparater 
       		  + iHours + sSeparater + iMinutes + sSeparater + iMilliSeconds + sSeparater + iMsgLength + sSeparater + iMsgVersion;
      return(s);
    }
    /**
     * Resets the data in this class to the default.
     */
    public void clear()
    {                    // Pull in default behaviour.

       iMsgType = 0;
   	   sEquipmentID = "";
   	   iSeqNo = 0;
   	   iHours = 0;
   	   iMinutes = 0;
   	   iMilliSeconds = 0;
   	   iMsgLength = 0;
   	   iMsgVersion = 0;
    }
    
    /*---------------------------------------------------------------------------
     ******** Column getter methods go here. ********
---------------------------------------------------------------------------*/
    public int getMsgType()
    {
      return iMsgType;
    }

    public String getEquipmentID()
    {
      return sEquipmentID;
    }
    
    public int getSeqNo()
    {
      return iSeqNo;
    }
    
    public int getHours()
    {
      return iHours;
    }
    
    public int getMinutes()
    {
      return iMinutes;
    }
    
    public int getMilliSeconds()
    {
      return iMilliSeconds;
    }
    
    public int getMsgLength()
    {
      return iMsgLength;
    }
    public int getMsgVersion()
    {
    	return iMsgVersion;
    }

  /*---------------------------------------------------------------------------
                 ******** Column Setting methods go here. ********
    ---------------------------------------------------------------------------*/
    
    public void setEquipmentId(String isEquipmentID)
    {
    	sEquipmentID = checkForNull(isEquipmentID);
    } 
    
    public void setMsgType(int inMsgType)
    {
    	iMsgType = inMsgType;
    } 
    
    public void setSeqNo(int inSeqNo)
    {
    	iSeqNo = inSeqNo;
    }  
    
    public void setHours(int inHours)
    {
    	iHours = inHours;
    }  
    
    public void setMinutes(int inMinutes)
    {
    	iMinutes = inMinutes;
    }  
    
    public void setMilliSeconds(int inMilliSeconds)
    {
    	iMilliSeconds = inMilliSeconds;
    }  
    public void setMsgLength(int inMsgLength)
    {
    	iMsgLength = inMsgLength;
    }
    public void setMsgVersion(int inMsgVersion)
    {
    	iMsgVersion = inMsgVersion;
    }

	@Override
	public boolean equals(AbstractSKDCData eskdata) {
		// TODO Auto-generated method stub
		return false;
	} 
    
  }

