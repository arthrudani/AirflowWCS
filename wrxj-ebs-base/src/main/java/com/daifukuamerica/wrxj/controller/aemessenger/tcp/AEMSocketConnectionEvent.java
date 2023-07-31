/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.controller.aemessenger.tcp;

/**
 * Event handler for Client Socket Connection notifications.
 */
public interface AEMSocketConnectionEvent
{
  /**
   * Method to handle client socket connection operations
   *
   * @param ipThread the client thread that is closing.
   */
  public void onSocketConnection(AEMTcpipReaderWriter ipThread);
}
