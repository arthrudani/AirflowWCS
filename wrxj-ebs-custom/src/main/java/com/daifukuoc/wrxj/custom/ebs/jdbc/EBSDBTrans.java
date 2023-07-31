package com.daifukuoc.wrxj.custom.ebs.jdbc;

import com.daifukuamerica.wrxj.jdbc.DBTrandef;
import com.daifukuamerica.wrxj.jdbc.DBTrans;

/**
 * Translation objects.
 *
 * @author A.D.
 * @since  13-Apr-2017
 */
public class EBSDBTrans extends DBTrans
{
 /**
  *  Initializes Translation objects, and stores them for retrieval later.
  */
  public static void init()
  {
    if (tm.size() == 0)
    {

        // Object array for "iLocSeqMethod"
        DBTrandef[] iLocSeqMethod = new DBTrandef[2];
        iLocSeqMethod[0] = new DBTrandef(EBSDBConstants.PRIMARYSEQ, "Primary Sequence");
        iLocSeqMethod[1] = new DBTrandef(EBSDBConstants.SECONDARYSEQ, "Secondary Sequence");

        tm.put("ILOCSEQMETHOD", iLocSeqMethod);
 
         
    }
  }
} /*** End of class BCSDBTrans ****/
