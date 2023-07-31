package com.daifukuamerica.wrxj.messageformat.schedulerevent;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */
import com.daifukuamerica.wrxj.messageformat.MessageConstants;
import com.daifukuamerica.wrxj.messageformat.MessageDataFormat;
import java.util.StringTokenizer;

public class SchedulerEventDataFormat  extends MessageDataFormat
{
//Data passed between the Station Device and the Scheduler
  private String sStation;

  public SchedulerEventDataFormat(String schedulerName)
  {
    super(MessageConstants.SCHEDULERMESSAGETYPE, schedulerName);
  }

  @Override
  protected void clearAllData()
  {
    super.clearAllData();
    setStation();
  }

  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer();
    myString.append(super.toString());
    myString.append(": sStation = " + getStation());
    return myString.toString();
  }

  @Override
  public void createDataString()
  {
    StringBuffer myString = new StringBuffer();
    myString.append(getStation());
    setDataString(myString.toString());
  }

  @Override
  public boolean decodeDataString()
  {
    StringTokenizer parser = new StringTokenizer(getDataString(), ";");
    boolean validMessage = true;
    try
    {
      clearAllData();
      setStation(parser.nextToken());
    }
    catch (Exception e)
    {
      validMessage = false;
    }
    return validMessage;
  }

  public void setStation(String station)
  {
    clearAllData();
    if( station == null )
    {
      setStation();
    }
    else
    {
      sStation = station;
    }
  }

  public void setStation()
  {
      sStation = null;
  }

  public String getStation()
  {
    return sStation;

  }

  public String getScreenStation()
  {
    String myStation = getStation();
    return myStation;
  }

}