package com.daifukuamerica.wrxj.messageformat.stationevent;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.messageformat.MessageConstants;
import com.daifukuamerica.wrxj.messageformat.MessageDataFormat;
import java.util.StringTokenizer;

public class StationEventDataFormat  extends MessageDataFormat
{
//Data passed between the Station Device and the Scheduler
  private int iMessageID;
  private String sStation;
  private int iResults;
  private String sErrorCode;          // delimiter "."

  public StationEventDataFormat(String name)
  {
    super(MessageConstants.AGCMESSAGETYPE, name);
  }


/**
* Clears the values of all data in this class
*/

  @Override
  protected void clearAllData()
  {
    super.clearAllData();
    setStation();
    setResults();
    setErrorCode(null);
  }

/**
* Puts all data of this class into a string with delimeters of ":"
*/

  @Override
  public String toString()
  {
    String s = super.toString();
    s = s + ": iMessageID = " + getMessageID() +
            ": sStation = " + getStation() +
            ": iResults =: " + getResults() +
            ": iErrorCode = " + getErrorCode();
    return(s);
  }

/**
* This method takes all data values and make them into one string with delimiters
* of ";" and stores that string into the base class dataString
*/

  @Override
  public void createDataString()
  {
    String s = getMessageID() + ";" +
            getStation() + ";" +
            getResults() + ";" +
            getErrorCode();
    setDataString(s);
  }

/**
* This method takes the statString of the base clase and decodes it into the
* individule data members of this class.
*/

  @Override
  public boolean decodeDataString()
  {
    StringTokenizer parser = new StringTokenizer(getDataString(), ";");
    boolean validMessage = true;
    try
    {
      clearAllData();
      setMessageID(Integer.parseInt(parser.nextToken()));
      setStation(parser.nextToken());
      setResults(Integer.parseInt(parser.nextToken()));
      setErrorCode(parser.nextToken());
    }
    catch (Exception e)
    {
      validMessage = false;
    }
    return validMessage;
  }

/**
* This method stores messageID into imessageID
* @param  messageID
*/

  public void setMessageID(int messageID)
  {
    iMessageID = messageID;

  }

/**
* Returns the iMessageID string
* @return String value of iMessageID
*/

  public int getMessageID()
  {
    return iMessageID ;

  }

/**
* Sets the sStation with station.
* @param  station the value to set the sStation to
*/

  public void setStation(String station)
  {
    int i;

    if( station == null )
    {
      setStation();
    }
    else
    {
      if( station.length() > AGCDeviceConstants.LNAGCSTATION)
      {
        sStation = station.substring(0, AGCDeviceConstants.LNAGCSTATION);
      }
      else
      {
        StringBuffer myStation = new StringBuffer();
        for(i=0; i<AGCDeviceConstants.LNAGCSTATION; i++ )
        {
          myStation.append(" ");
        }
        myStation.insert(0, station.substring(0, station.length()));
        sStation = myStation.toString();
      }
    }
  }

  /**
  * Initalizes the sSourceStation to blanks
  */

  public void setStation()
  {
    int i;
    StringBuffer station = new StringBuffer();
    for(i=0; i<AGCDeviceConstants.LNAGCSTATION; i++ )
    {
      station.append(" ");
    }
    sStation = station.toString();
  }

/**
* Trims sSourceStation and then returns it.
* @return sSourceStation the current SourceStation value
*/

  public String getStation()
  {
    return sStation.trim();

  }

/**
* This method stores results into iResults
* @param  results
*/

  public void setResults(int results)
  {
    iResults = results;

  }

/**
* This method initializes iResults to -1
*/

  private void setResults()
  {
    iResults = -1;

  }

/**
* Returns the iResults string
* @return String value of iResults
*/

  public int getResults()
  {
    return iResults ;

  }

  /**
  * Sets sErrorCode equal to errorCode.
  * @param  errorCode the tokenized string of codes that are delimited by "."
  */

  public void setErrorCode(String errorCode)
  {
    if( errorCode == null )
    {
      sErrorCode = errorCode;
    }
    else
    {
      sErrorCode = errorCode;
    }
  }

/**
* Returns sErrorCode a string that is delimited by "."
* @return sErrorCode the current ErrorCode value
*/

  public String getErrorCode()
  {
    return sErrorCode ;

  }

  /**
  * Sets the sMessageID to AGCDEVICERESPOPERMODECHGCMD and creates the datastring
  * @param  station that the response is for
  * @return LoadEvent string to transmit
  */

  public String processOperationModeChangeCmd(String station, int results)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICERESPOPERMODECHGCMD);
    setStation(station);
    setResults(results);
    return createStringToSend();
  }
  
  /**
   * Sets the sMessageID to AGCDEVICEOPERMODECHGREQ and creates the datastring for the
   * message
   * @param isStation the station that requested mode change
   * @param inResults  the mode requested
   * @return StationEvent String to transmit
   */
  
  public String processOperationModeChangeRequest(String isStation, int inResults)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICEOPERMODECHGREQ);
    setStation(isStation);
    setResults(inResults);
    return createStringToSend();
  }
  
  /**
   * Sets the sMessageID to RTSAGCMODECHGRESP and creates the datastring fro the 
   * message
   * @param isStation the station that requested mode change
   * @param inResults the response to mode change
   * @return StationEvent String to transmit
   */
  public String processOperationModeChangeReqResponse(String isStation, int inResults)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.RTSAGCMODECHGRESP);
    setStation(isStation);
    setResults(inResults);
    return createStringToSend();
    
  }

  /**
  * Sets the sMessageID to AGCDEVICEOPERMODECHGCOMPREPORT and creates the datastring
  * @param  station that the response is for
  * @return LoadEvent string to transmit
  */

  public String processOperationModeChgReport(String station, int results)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.AGCDEVICEOPERMODECHGCOMPREPORT);
    setStation(station);
    setResults(results);
    return createStringToSend();
  }

  public String screenChangeStationMode(String station)
  {
    clearAllData();
    setMessageID(AGCDeviceConstants.GENERALCHANGESTATIONMODE);
    setStation(station);
    return createStringToSend();
  }

}