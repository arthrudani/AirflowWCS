package com.daifukuamerica.wrxj.device.agc;

/**
 * Title:        WRx 8.xx (Java)
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corporation
 */

import com.daifukuamerica.wrxj.time.SkDateTime;
import java.util.Random;

/**
 * AgcMessage is a common core of data and methods for all message sets used to
 * communicate with a Daifuku AGC.
 *
 * @author       Stephen Kendorski
 * @version 1.0
 */
public class AGCMessage
{
  public static final String MINI_PARSE_DIVIDER = 
    "                                                                     \n\n";
  protected static final String NO_ERROR = "0000000";

  private int COMMUNICATION_TEST_TEXT_LENGTH = 488;
 /**
  * Semaphore to show if a decoded message is correct.
  */
  protected boolean validMessage = false;
  /**
   * Text describing why a message is not correct.
   */
  protected String invalidMessageDescription = "";
  /**
   * The message encoded as a block of text.
   */
  protected String messageAsString = null;
  private String parsedMessageString = "";
  /**
   *  A String of data particular to each message type (without the header
   *  common to all messages).
   */
  protected String messageData = "";

  protected static final String[] ASCII_DIGITS =
     {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
  /**
   * The array containing text descriptions of all equipment types used in
   * the message sets.
   */
  protected static String[] machineTypes = new String[100];
  protected static String[] machineTypesFL = new String[100];
  //
  // Data Values common to ALL AGC messages.
  //
  protected int id = 0;
  protected String transmissionTime = "000000";
  protected SkDateTime msgDateTime = new SkDateTime("HHmmss");
  protected boolean responseMessage = false;
  //
  // Data Values for:  1: RequestToStartOperation
  //                   2: DateTimeData
  //
  protected String dataDateTimeString = "";
  protected SkDateTime dataDateTime = new SkDateTime("yyyyMMddHHmmss");

  protected String communicationTestTextRequest = null;
  protected String communicationTestTextResponse = null;

  static
  {
    setMachineTypes();
  }
  public AGCMessage()
  {
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected static void setMachineTypes()
  {
    for (int i = 0; i < machineTypes.length; i++)
    {
      machineTypes[i] = "*UNKNOWN-" + i + "*";
    }
    machineTypes[01] = "MC";
    machineTypes[02] = "MOS";
    machineTypes[03] = "AGC";
    machineTypes[04] = "CELL";
    machineTypes[11] = "S/R-Crane";
    machineTypes[15] = "MSS";
    machineTypes[16] = "TRV";
    machineTypes[17] = "MA";
    machineTypes[18] = "IDR";
    machineTypes[20] = "ROBOT";
    machineTypes[21] = "Conveyor";
    machineTypes[22] = "LFT";
    machineTypes[28] = "PT-Mate";
    machineTypes[31] = "Magic-Vehicle";
    machineTypes[32] = "R/LC";
    machineTypes[33] = "Charger";
    machineTypes[37] = "MV-Maint-Stn";
    machineTypes[39] = "MV-Dummy-Stn";
    machineTypes[44] = "SPCS";
    machineTypes[45] = "SPCL";
    machineTypes[47] = "SPC-Maint-Stn";
    machineTypes[48] = "SPC-Stb-SW";
    machineTypes[49] = "SPC-Dummy-Stn";
    machineTypes[54] = "STV-Shuttle";
    machineTypes[55] = "STV-Loop";
    machineTypes[57] = "STV-Maint-Stn";
    machineTypes[58] = "SPC-Traverser";
    machineTypes[59] = "STV-Dummy-Stn";
    machineTypes[61] = "DPR";
    machineTypes[91] = "Selector SW";
    machineTypes[92] = "BCR";
    machineTypes[93] = "Indicator";
    machineTypes[94] = "Labeler";
    machineTypes[95] = "C-Box";
    machineTypes[96] = "Weigh-Scale";
    machineTypes[99] = "Supervisor";


    for (int i = 0; i < 10; i++)
    {
      machineTypesFL[i] = "*UNKNOWN-0" + i + "* ";
    }
    for (int i = 10; i < machineTypesFL.length; i++)
    {
      machineTypesFL[i] = "*UNKNOWN-" + i + "* ";
    }
    machineTypesFL[01] = "MC           ";
    machineTypesFL[02] = "MOS          ";
    machineTypesFL[03] = "AGC          ";
    machineTypesFL[04] = "CELL         ";
    machineTypesFL[11] = "S/R-Crane    ";
    machineTypesFL[15] = "MSS          ";
    machineTypesFL[16] = "TRV          ";
    machineTypesFL[17] = "MA           ";
    machineTypesFL[18] = "IDR          ";
    machineTypesFL[20] = "ROBOT  ";
    machineTypesFL[21] = "Conveyor     ";
    machineTypesFL[22] = "LFT          ";
    machineTypesFL[28] = "PT-Mate      ";
    machineTypesFL[31] = "Magic-Vehicle";
    machineTypesFL[32] = "R/LC         ";
    machineTypesFL[33] = "Charger      ";
    machineTypesFL[37] = "MV-Maint-Stn ";
    machineTypesFL[39] = "MV-Dummy-Stn ";
    machineTypesFL[44] = "SPCS         ";
    machineTypesFL[45] = "SPCL         ";
    machineTypesFL[47] = "SPC-Maint-Stn";
    machineTypesFL[48] = "SPC-Stb-SW   ";
    machineTypesFL[49] = "SPC-Dummy-Stn";
    machineTypesFL[54] = "STV-Shuttle  ";
    machineTypesFL[55] = "STV-Loop     ";
    machineTypesFL[57] = "STV-Maint-Stn";
    machineTypesFL[58] = "SPC-Traverser";
    machineTypesFL[59] = "STV-Dummy-Stn";
    machineTypesFL[61] = "DPR          ";
    machineTypesFL[91] = "Selector SW  ";
    machineTypesFL[92] = "BCR          ";
    machineTypesFL[93] = "Indicator    ";
    machineTypesFL[94] = "Labeler      ";
    machineTypesFL[95] = "C-Box        ";
    machineTypesFL[96] = "Weigh-Scale  ";
    machineTypesFL[99] = "Supervisor   ";
  }

  public boolean getValidMessage()
  {
    return(validMessage);
  }

  public String getInvalidMessageDescription()
  {
    return(invalidMessageDescription);
  }

  public void setID(int inID)
  {
    id = inID;
  }

  public int getID()
  {
    return(id);
  }

  public String getMessageAsString()
  {
    return(messageAsString);
  }

  public String getParsedMessageString()
  {
    return(parsedMessageString);
  }
  
  protected void setParsedMessageString(String isParsedMessageString)
  {
    parsedMessageString = isParsedMessageString;
  }

  protected void setCommTestMessageLength(int inLength)
  {
    COMMUNICATION_TEST_TEXT_LENGTH = inLength;
  }

  /*--------------------------------------------------------------------------*/
  public void setCommunicationTestRandomTextRequest()
  {
    communicationTestTextRequest = getCommunicationTestRandomText();
  }

  /*--------------------------------------------------------------------------*/
  public void setCommunicationTestTextRequest(String isCommunicationTestTextRequest)
  {
    communicationTestTextRequest = isCommunicationTestTextRequest;
  }
  /*--------------------------------------------------------------------------*/
  public String getCommunicationTestTextRequest()
  {
    return communicationTestTextRequest;
  }

  /*--------------------------------------------------------------------------*/
  public void setCommunicationTestRandomTextResponse()
  {
    communicationTestTextResponse = getCommunicationTestRandomText();
  }

  /*--------------------------------------------------------------------------*/
  public void setCommunicationTestTextResponse(String isCommunicationTestTextResponse)
  {
    communicationTestTextResponse = isCommunicationTestTextResponse;
  }
  /*--------------------------------------------------------------------------*/
  public String getCommunicationTestTextResponse()
  {
    return communicationTestTextResponse;
  }

  public boolean getCommunicationTestResult()
  {
    return (communicationTestTextResponse.equals(communicationTestTextRequest));
  }

  /*--------------------------------------------------------------------------*/
  protected String getCommunicationTestRandomText()
  {
    Random random = new Random();
    char[] testText = new char[COMMUNICATION_TEST_TEXT_LENGTH];
    for (int i = 0; i < COMMUNICATION_TEST_TEXT_LENGTH; i++)
    {
      int value = '"';
      while (value == '"')
      {
        value = random.nextInt(0x7f-0x22);
        value += 0x21;  // get value in ASCII displayable range 0x21 - 0x7e
      }
      testText[i] = (char)value;
    }
    testText[0] = '[';
    testText[COMMUNICATION_TEST_TEXT_LENGTH-1] = ']';
    String result = new String(testText);
    return (result);
  }

  protected String setTransmissionTime()
  {
    transmissionTime = msgDateTime.getCurrentDateTimeAsString();
    return (transmissionTime);
  }

  protected void setDateTimeString()
  {
    dataDateTimeString = dataDateTime.getCurrentDateTimeAsString();
  }

  protected void setDateTimeString(String isDataDateTimeString)
  {
    dataDateTimeString = isDataDateTimeString;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected String getSubString(String sString, int iOffset, int iLen)
  {
    try
    {
      return sString.substring(iOffset, iOffset + iLen);
    }
    catch (Exception e)
    {
      if (getValidMessage())
      {
        validMessage = false;
        invalidMessageDescription = "#####  IndexOutOfBoundsException -- Offset: " +
                                     iOffset + "  Length: " + iLen + "  \"" +
                                     sString + "\"  #####";
      }
      return "";
    }
  }

  /*--------------------------------------------------------------------------*/
  protected String getTwoAsciiDigits(int iValue)
  {
    String sResult = ASCII_DIGITS[iValue / 10] +
                     ASCII_DIGITS[iValue % 10];
    return sResult;
  }

  protected String getThreeAsciiDigits(int iValue)
  {
    String sResult = ASCII_DIGITS[iValue / 100] +
                     ASCII_DIGITS[(iValue % 100) / 10] +
                     ASCII_DIGITS[iValue % 10];
    return sResult;
  }
  protected String getFourAsciiDigits(int iValue)
  {
    String sResult = ASCII_DIGITS[iValue / 1000] +
                     ASCII_DIGITS[(iValue % 1000) / 100] +
                     ASCII_DIGITS[(iValue % 100) / 10] +
                     ASCII_DIGITS[iValue % 10];
    return sResult;
  }
  protected String getSixAsciiDigits(int iValue)
  {
    String sResult = ASCII_DIGITS[iValue / 10000] +
                     ASCII_DIGITS[(iValue % 10000) / 1000] +
                     ASCII_DIGITS[iValue / 1000] +
                     ASCII_DIGITS[(iValue % 1000) / 100] +
                     ASCII_DIGITS[(iValue % 100) / 10] +
                     ASCII_DIGITS[iValue % 10];
    return sResult;
  }
  protected String getSevenAsciiDigits(int iValue)
  {
    String sResult = ASCII_DIGITS[(iValue / 100000) / 10000] +
                     ASCII_DIGITS[iValue / 10000] +
                     ASCII_DIGITS[(iValue % 10000) / 1000] +
                     ASCII_DIGITS[iValue / 1000] +
                     ASCII_DIGITS[(iValue % 1000) / 100] +
                     ASCII_DIGITS[(iValue % 100) / 10] +
                     ASCII_DIGITS[iValue % 10];
    return sResult;
  }

  /*--------------------------------------------------------------------------*/
  protected int getIntDigit(String sString, int iValue)
  {
    try
    {
      int iDigit = sString.charAt(iValue);
      if ((iDigit >= 0x30) && (iDigit <= 0x39))
      {
        iDigit -= 0x30;
        return iDigit;
      }
      else
      {
        if (getValidMessage() )
        {
          validMessage = false;
          invalidMessageDescription = ("#####  NON 0-9 ASCII Digit -- Offset: " + iValue + "\"  #####");
        }
        return 0;
      }
    }
    catch (IndexOutOfBoundsException e)
    {
      if (getValidMessage() )
      {
        validMessage = false;
        invalidMessageDescription = ("##### Index OUT-OF-BOUNDS -- Offset: " + iValue + "\"  #####");
      }
      return 0;
    }
  }

  protected int getIntFromOneAsciiDigit(String msgStr, int iOffset)
  {
    int i0 = getIntDigit(msgStr, iOffset);
    return (i0);
  }

  protected int getIntFromTwoAsciiDigits(String msgStr, int iOffset)
  {
    int i0 = getIntDigit(msgStr, iOffset);
    int i1 = getIntDigit(msgStr, iOffset+1);
    return ((i0 * 10) + i1);
  }

  protected int getIntFromThreeAsciiDigits(String msgStr, int iOffset)
  {
    int i0 = getIntDigit(msgStr, iOffset);
    int i1 = getIntDigit(msgStr, iOffset+1);
    int i2 = getIntDigit(msgStr, iOffset+2);
    return ((i0 * 100) + (i1 * 10) + i2);
  }

  protected int getIntFromFourAsciiDigits(String msgStr, int iOffset)
  {
    int i0 = getIntDigit(msgStr, iOffset);
    int i1 = getIntDigit(msgStr, iOffset+1);
    int i2 = getIntDigit(msgStr, iOffset+2);
    int i3 = getIntDigit(msgStr, iOffset+3);
    return ((i0 * 1000) + (i1 * 100) + (i2 * 10) + i3);
  }

  protected int getIntFromSixAsciiDigits(String msgStr, int iOffset)
  {
    int i0 = getIntDigit(msgStr, iOffset);
    int i1 = getIntDigit(msgStr, iOffset+1);
    int i2 = getIntDigit(msgStr, iOffset+2);
    int i3 = getIntDigit(msgStr, iOffset+3);
    int i4 = getIntDigit(msgStr, iOffset+4);
    int i5 = getIntDigit(msgStr, iOffset+5);
    return ((i0 * 100000) + (i1 * 10000) + (i2 * 1000) + (i3 * 100) + (i4 * 10) + i5);
  }

  protected int getIntFromSevenAsciiDigits(String msgStr, int iOffset)
  {
    int i0 = getIntDigit(msgStr, iOffset);
    int i1 = getIntDigit(msgStr, iOffset+1);
    int i2 = getIntDigit(msgStr, iOffset+2);
    int i3 = getIntDigit(msgStr, iOffset+3);
    int i4 = getIntDigit(msgStr, iOffset+4);
    int i5 = getIntDigit(msgStr, iOffset+5);
    int i6= getIntDigit(msgStr, iOffset+6);
    return ((i0 * 1000000) +(i1 * 100000) + (i2 * 10000) + (i3 * 1000) + (i4 * 100) + (i5 * 10) + i6);
  }
}