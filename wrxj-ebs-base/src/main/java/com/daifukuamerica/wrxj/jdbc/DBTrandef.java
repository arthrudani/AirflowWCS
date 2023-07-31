package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.swing.DacTranslator;

/**
 * Title: DBTrandef
 * Description: Class defines what makes a Translation object.  This class <BR>
 *              is meant to be used directly only inside this package (its <BR>
 *              methods are however exposed to classes outside this package <BR>
 *              that sub-class it).
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 30-Aug-01<BR>
 *     Copyright (c) 2001<BR>
 *     Company:  Eskay Corporation
 */
public class DBTrandef
{
  private int    mnTranInteger;
  private String msTranString;

  public DBTrandef()
  {
    super();
    mnTranInteger = 0;
    msTranString = "";
  }

  public DBTrandef(int inTranInteger, String inTranString)
  {
    this();
    mnTranInteger = inTranInteger;
    msTranString = inTranString;
  }

  /*
   *  Returns the String representation of a translation object.
   */
  public String getTranString()
  {
    return DacTranslator.getTranslation(msTranString);
  }

  /*
   *  Returns the Integer representation of the translation object.
   */
  public int getTranInteger()
  {
    return mnTranInteger;
  }
}

