package com.daifukuamerica.wrxj.host.messages;

/****************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright ? 2004 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;

/* Description:<BR>
 *  Interface for all WRx-J message parsers.
 *
 * @author       A.D.
 * @version      1.0     02/09/05
 */
public interface MessageParser
{
 /**
  *  Method does the actual work of parsing a message, and adds records to the
  *  Wrx-J database as the parsing proceeds.
  *  @param  hiData  HostToWrxData object containing information about inbound
  *                  data.
  *  @throws java.text.ParseException when there is a parsing error.
  *  @throws com.daifukuamerica.wrxj.jdbc.DBException when there is a serious
  *          database error that prevents message operation (ADD, MODIFY or
  *          DELETE) from being performed.
  */
  public void parse(HostToWrxData hiData) throws InvalidHostDataException;
 /**
  * Method helps drop any DB connections specifically opened by any parser.
  */
  public void cleanUp();
}
