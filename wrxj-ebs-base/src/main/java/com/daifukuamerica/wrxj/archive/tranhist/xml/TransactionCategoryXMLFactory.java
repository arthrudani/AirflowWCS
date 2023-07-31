package com.daifukuamerica.wrxj.archive.tranhist.xml;
/*
                     SK Daifuku Corporation
                     International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3152
                        (801) 359-9900

   This software is furnished under a license and may be used
   and copied only in accordance with the terms of such license.
   This software or any other copies thereof in any form, may not be 
   provided or otherwise made available, to any other person or company 
   without written consent from SK Daifuku Corporation. 

   SK Daifuku assumes no responsibility for the use or reliability of
   software which has been modified without approval.
*/

import com.daifukuamerica.wrxj.archive.ExportFormat;
import com.daifukuamerica.wrxj.archive.Exporter;
import com.daifukuamerica.wrxj.jdbc.DBConstants;

/**
 * Description:<BR>
 *   Factory for producing different Transaction Category XML writers.
 *
 * @author       A.D.
 * @version      1.0
 */
public class TransactionCategoryXMLFactory implements ExportFormat
{
 /**
  *  Returns a writer or exporter class based on criteria.
  *  @param criteria <code>Object</code> containing criteria to get writer.
  *
  *  @return object implementing the Exporter interface.
  */
  public Exporter getDataWriter(Object category)
  {
    if (category instanceof Integer)
    {
      Integer tranCatObject = (Integer)category;
      switch(tranCatObject.intValue())
      {
        case DBConstants.LOAD_TRAN:
          return(new ExportLoadXML());
      
        case DBConstants.ORDER_TRAN:
          return(new ExportOrderXML());
        
        case DBConstants.USER_TRAN:
          return(new ExportUserXML());

        case DBConstants.INVENTORY_TRAN:
          return(new ExportInventoryXML());

        case DBConstants.SYSTEM_TRAN:
          return(new ExportSystemXML());
          
        default:
          System.out.println("\n********* Invalid Transaction Category.....");
      }
    }
    else
    {
      System.out.println("\n********* Transaction category is not of Integer type.....");
    }

    return(null);
  }
}
