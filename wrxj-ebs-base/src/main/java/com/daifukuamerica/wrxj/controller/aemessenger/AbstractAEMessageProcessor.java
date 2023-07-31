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
package com.daifukuamerica.wrxj.controller.aemessenger;

import com.daifukuamerica.wrxj.log.Logger;

/**
 * 
 * @author mandrus
 */
public abstract class AbstractAEMessageProcessor implements AEMessageProcessor
{
  protected Logger mpLogger;
  
  /**
   * Constructor
   * 
   * @param isProduct
   */
  public AbstractAEMessageProcessor(String isProduct)
  {
    mpLogger = AEMessenger.getAEMessageEqLogger(isProduct);
  }
}
