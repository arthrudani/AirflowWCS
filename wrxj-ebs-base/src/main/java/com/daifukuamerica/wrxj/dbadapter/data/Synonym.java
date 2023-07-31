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
 * A lower level data base object that interfaces to the synonym table.
 *
 * @author jan
 * @version 1.0
 * @created 02/16/05
 * @file    Synonym.java
 * 
 *   sItem              VARCHAR(20)    NOT NULL,
 *   sSynonym           VARCHAR(30)    NULL,
 *   iUPCFlag           INTEGER        DEFAULT 2
 * 
 */
public class Synonym extends BaseDBInterface
{
  private SynonymData mpSynData;

  public Synonym()
  {
    super("Synonyms", "Synonyms", Factory.create(SynonymData.class));
    mpSynData = Factory.create(SynonymData.class);
  }

  /**
   *  Method to get a synonym data for specified synonym.
   *
   *  @param synonymName synonym number.
   *  @return syData object containing synonym info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public SynonymData getSynonymData(String sisynonym) throws DBException
  {
    mpSynData.clear();
    mpSynData.setKey(SynonymData.SYNONYM_NAME, sisynonym);
    return (getElement(mpSynData, DBConstants.NOWRITELOCK));
  }

  /**
   *  Method to delete a synonym.
   *
   *  @param sisynonym synonym.
   *  @exception DBException
   */
  public void deleteSynonym(String siSynonym) throws DBException
  {
    if (existSynonym(siSynonym))
    {
      mpSynData.clear();
      mpSynData.setKey(SynonymData.SYNONYM_NAME, siSynonym);
      deleteElement(mpSynData);
    }
    return;
  }

  /**
   *  Method to add a synonym.
   *
   *  @param rold Filled in synonym data object.
   *  @exception DBException
   */
  public void addSynonym(SynonymData sydata) throws DBException
  {
    addElement(sydata);
    return;
  }

  /**
   *  Method to see if the specified synonym exists.
   *
   *  @param synonymName synonym name.
   *  @return boolean of <code>true</code> if it exists.
   *  @exception DBException
   */
  public boolean existSynonym(String synonymName) throws DBException
  {
    mpSynData.clear();
    mpSynData.setKey(SynonymData.SYNONYM_NAME, synonymName);
    return (exists(mpSynData));
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpSynData = null;
  }
}
