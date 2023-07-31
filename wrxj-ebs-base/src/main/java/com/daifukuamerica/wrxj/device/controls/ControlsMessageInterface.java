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

package com.daifukuamerica.wrxj.device.controls;

public interface ControlsMessageInterface
{
  /**
   * Parse and interpret the passed-in message text into fields defined for that
   * message type.
   *
   * @param sMessageString the message to decode
   */
  public void toDataValues(String isMessageString);
  
  /**
   * Convert the current command to a string for transmission
   */
  public String getMessageAsString();

  /**
   * Get the parsed message string
   */
  public String getParsedMessageString();

  /**
   * Is this a valid message?
   * @return
   */
  public boolean getValidMessage();
  
  /**
   * Get the description of why the message is invalid
   * @return String if message is invalid, null if message is valid
   */
  public String getInvalidMessageDescription();

  /**
   * Set the equipment status
   * @param isStatus
   */
  public void setEquipmentStatus(String isStatus);
  
  /**
   * Get the equipment status report
   * @return
   */
  public String getEquipmentStatusReport();

}
