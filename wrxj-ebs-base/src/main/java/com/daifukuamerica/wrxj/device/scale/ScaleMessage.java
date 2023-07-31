package com.daifukuamerica.wrxj.device.scale;

import com.daifukuamerica.wrxj.util.SKDCUtility;

public class ScaleMessage
{
  public static final int WEIGHT = 0;
  public static final int MOVEMENT = 1;
  public static final int SYNTAX_ERROR = 2;
  public static final int TRANSMISSION_ERROR = 3;
  public static final int LOGICAL_ERROR = 4;
  public static final int UNKNOWN = 5;
  public static final int BUFFER_CLEAR = 6;
  public static final int WEIGHT_LENGTH = 10;
  
 
 
  public String getWeightCommand()
  {
    String vsWeight = "S";
    return vsWeight;
  }

  public String getBufferClearCommand()
  {
    String vsClear = "@";
    return vsClear;
  }
  
  public String getBufferClearResponse()
  {
    return "l4 A \"\"";
  }

  public int getMessageType(String isReceivedText)
  {
    try
    {
      String vsType = isReceivedText.substring(0, 2);
      if (vsType.equals("S "))
      {
        String vsSubType = isReceivedText.substring(2, 3);
        if (vsSubType.equals("S"))
        {
          return WEIGHT;
        }
        else
        {
          return MOVEMENT;
        }
      }
      else
        if (vsType.equals("ES"))
        {
          return SYNTAX_ERROR;
        }
        else
          if (vsType.equals("ET"))
          {
            return TRANSMISSION_ERROR;
          }
          else
            if (vsType.equals("EL"))
            {
              return LOGICAL_ERROR;
            }
            else
            {
              return UNKNOWN;
            }
    }
    catch (IndexOutOfBoundsException e)
    {
      return UNKNOWN;
    }
  }
  
  public int getEmulatorMessageType(String isReceivedText)
  {
    if(isReceivedText.equals("@"))
    {
      return BUFFER_CLEAR;
    }
    else if(isReceivedText.equals("S"))
    {
      return WEIGHT;
    }
    return UNKNOWN;
       
  }
	
	
  /**
   * Get a Weight Message
   * @param mdWeight
   * @return String message
   */
  public String getWeightDataMessage(String isWeight)
  {
  	String vsWeight = SKDCUtility.spaceFillLeading(isWeight, ScaleMessage.WEIGHT_LENGTH);
  	return "S S " + vsWeight + " lb";
  }
	
	
  /**
   * This method tries to parse the weight from the message
   * @param isReceivedText
   * @return Weight value or -1 in an error condition
   */
  public double getWeight(String isReceivedText)
  {
    try
    {
      String vsMessageWeight = isReceivedText.substring(4, 14);
      String vsTemp = vsMessageWeight.trim();
      double vdWeight = Double.parseDouble(vsTemp);
      return vdWeight;
    }
    catch(Exception e)
    {
      return -1;
    }
  }
}
