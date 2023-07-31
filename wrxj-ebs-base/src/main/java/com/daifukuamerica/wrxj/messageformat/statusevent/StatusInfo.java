/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.messageformat.statusevent;

import java.util.StringTokenizer;

/**
 * <B>Description:</B> This class was created as part of the conversion from
 * hard-coded strings & tokenizers into a more maintainable form.
 * 
 * <BR>TODO: This class should probably be broken up into an abstract class and one child class per status message type.
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
public class StatusInfo
{
  private String[] masParams;
  
  private static final String DELIMITER = "\t";

  /*========================================================================*/
  /*  Constructors                                                          */
  /*========================================================================*/
  public StatusInfo(String... iasParams)
  {
    masParams = new String[iasParams.length];
    System.arraycopy(iasParams, 0, masParams, 0, iasParams.length);
  }

  public StatusInfo(String isParseMe)
  {
    StringTokenizer vpST = new StringTokenizer(isParseMe, "\t");
    masParams = new String[vpST.countTokens()];
    for (int i = 0; i < masParams.length; i++)
    {
      masParams[i] = vpST.nextToken();
    }
  }

  /*========================================================================*/
  /*  Getter methods                                                        */
  /*========================================================================*/
  // Bidirectional status messages
  public String getBidirStation()    {   return masParams[0];   }
  public String getBidirStatus()     {   return masParams[1];   }
  
  // Controller status messages
  public String getControlName()     {   return masParams[0];   }
  public String getControlType()     {   return masParams[1];   }
  public String getControlStatus()   {   return masParams[2];   }
  public String getControlDetail()   {   return masParams[3];   }
  public String getControlTime()     {   return masParams[4];   }
  
  // Controller Update status messages
  public String getControlUName()     {   return masParams[0];   }
  public String getControlUOpMode()   {   return masParams[1];   }
  public String getControlUOnStat()   {   return masParams[2];   }
  public String getControlUStatus()   {   return masParams[3];   }
  public String getControlUDetail()   {   return masParams[4];   }
  public String getControlUHBeat()    {   return masParams[5];   }
  public String getControlUError()    {   return masParams[6];   }
  public String getControlUTime()     {   return masParams[7];   }
  
  // Equipment status messages
  public String getMachineID()       {   return masParams[0];   }
  public String getMachineType()     {   return masParams[1];   }
  public String getMachineNo()       {   return masParams[2];   }
  public String getMachineStat()     {   return masParams[3];   }
  public String getMachineDesc()     {   return masParams[4];   }
  public String getMachineError()    {   return masParams[5];   }
  public String getMachineTime()     {   return masParams[6];   }
  
  // Tracking status messages
  public String getTrackName()       {   return masParams[0];   }
  public String getTrackMachine()    {   return masParams[1];   }
  public String getTrackKey()        {   return masParams[2];   }
  public String getTrackBCR()        {   return masParams[3];   }
  public String getTrackType()       {   return masParams[4];   }
  public String getTrackSrc()        {   return masParams[5];   }
  public String getTrackDest()       {   return masParams[6];   }
  public String getTrackSize()       {   return masParams[7];   }
  public String getTrackTime()       {   return masParams[8];   }
  
  // Update status messages
  public String getUpdateMachine()   {   return masParams[0];   }
  public String getUpdateDesc()      {   return masParams[1];   }
  public String getUpdateError()     {   return masParams[2];   }
  public String getUpdateErrSet()    {   return masParams[3];   }
  public String getUpdateTime()      {   return masParams[4];   }
  
  
  @Override
  public String toString()
  {
    StringBuffer vpString = new StringBuffer();
   
    for (int i = 0; i < masParams.length-1; i++)
    {
      vpString.append(masParams[i]).append(DELIMITER);
    }
    vpString.append(masParams[masParams.length-1]).append("\n");
    return vpString.toString();
  }
}
