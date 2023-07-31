package com.daifukuamerica.wrxj.dbadapter;

/*
                    Daifuku America Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used and copied only 
   in accordance with the terms of such license. This software or any other 
   copies thereof in any form, may not be provided or otherwise made available, 
   to any other person or company without written consent from Daifuku America 
   Corporation.

   Daifuku America Corporation assumes no responsibility for the use or 
   reliability of software which has been modified without approval.
*/

import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *  Interface class for all Data classes.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 05-Jul-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
public interface ModelInterface
{
/**
 ** Retrieves an List of data from the database.
 **/
  public List<Map> getAllElements(AbstractSKDCData eskdata) throws DBException;

/**
 ** Gets a specific record from the database.
 **/
  public <Type extends AbstractSKDCData> Type getElement(Type eskdata,
                                      int lockFlag) throws DBException;

/**
 ** Adds a row of Data. This method normally calls 'addData' to do it's job,
 ** but the user needs to implement this in case there are more steps that may
 ** need to be taken in the future before adding a row (more than what is in
 ** addData).
 **/
  public void addElement(AbstractSKDCData eskdata) throws DBException;

/**
 ** Modifies a row of Data. This method normally calls 'modifyData' to do it's
 ** job, but the user needs to implement this in case there are more steps that
 ** may need to be taken in the future before modifying a row (more than what is
 ** in modifyData).
 **/
  public void modifyElement(AbstractSKDCData eskdata) throws DBException;

/**
 ** Deletes a row of Data. This method normally calls 'deleteData' to do it's
 ** job, but the user needs to implement this in case there are more steps that
 ** may need to be taken in the future before deleting a row (more than what is
 ** in deleteData).
 **/
  public void deleteElement(AbstractSKDCData eskdata) throws DBException;

/**
 ** Gets a Record Count based on information provided in eskdata.
 **/
  public int getCount(AbstractSKDCData eskdata) throws DBException;

/**
 ** Checks for existence of record(s) matching criteria in eskdata.
 **/
  public boolean exists(AbstractSKDCData eskdata);

/**
 ** Method to help clean up references.  Method should set all objects it used
 ** to <code>null</code> to help out the JVM garbage collector.
 **/
  public void cleanUp();
}