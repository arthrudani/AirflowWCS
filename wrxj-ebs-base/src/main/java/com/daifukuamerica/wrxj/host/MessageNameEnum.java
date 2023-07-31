package com.daifukuamerica.wrxj.host;

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

/**
 * Interface for outbound Host Message name enumerations.  This allows us have
 * customized enumerations without directly affecting baseline code.
 *
 * @author       A.D.
 * @version      1.0   06/16/2006
 */
public interface MessageNameEnum
{
 /**
  * Method gets the value of an enumeration as a string.
  * @return enum. value as a string.
  */
  public String getValue();
 /**
  *  Method gets the fully qualified message name as defined in the Host
  *  Message Specification.
  *  
  *  @return returns String containing qualified message name.
  */
  public String getQualifiedName();
}
