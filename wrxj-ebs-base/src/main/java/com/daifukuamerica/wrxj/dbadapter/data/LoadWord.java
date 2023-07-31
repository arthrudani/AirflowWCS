package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Description:<BR>
 *   Class to handle Load Words.
 *
 * @author       Michael Andrus
 * @version      1.0
 * <BR>Created:  15-Nov-04<BR>
 *     Copyright (c) Daifuku America Corporation 2004<BR>
 */
public class LoadWord extends BaseDBInterface
{
  private LoadWordData mpLWData;

  public LoadWord()
  {
    super("LoadWord");
    mpLWData = Factory.create(LoadWordData.class);
  }

  /**
   * Gets a load word
   * 
   * @param index
   * @return
   */
  public String getLoadWord(int vnLWIndex) throws DBException, ArrayIndexOutOfBoundsException
  {
    mpLWData.clear();
    mpLWData.setKey(mpLWData.getWordSequenceName(), Integer.valueOf(vnLWIndex));
    LoadWordData vpLWData = getElement(mpLWData, DBConstants.NOWRITELOCK);
    if (vpLWData == null)
    {
      throw new ArrayIndexOutOfBoundsException("No Load Word for index: " + vnLWIndex);
    }
    return vpLWData.getLoadWord();
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpLWData = null;
  }
}
