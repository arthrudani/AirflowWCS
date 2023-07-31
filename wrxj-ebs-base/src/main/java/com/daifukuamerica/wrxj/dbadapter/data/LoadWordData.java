package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description:<BR>
 *   Class to handle Load Words.
 *
 * @author       Michael Andrus
 * @version      1.0
 * <BR>Created:  15-Nov-04<BR>
 *     Copyright (c) Daifuku America Corporation 2004<BR>
 */
public class LoadWordData extends AbstractSKDCData
{
/*---------------------------------------------------------------------------
                   Database fields for LoadWord table.
  ---------------------------------------------------------------------------*/
  private String sLoadWord  = "";
  private int iWordSequence = 0;

  public LoadWordData()
  {
    super();
  }

 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    StringBuffer s = new StringBuffer();
    s.append("iWordSequence:'").append(Integer.toString(iWordSequence))
     .append(SKDCConstants.EOL_CHAR)
     .append("sLoadWord:'").append(sLoadWord)
     .append(SKDCConstants.EOL_CHAR);

                                       // Throw in the Key and Column info.
    s.append(SKDCConstants.EOL_CHAR);

    return(s.toString() + super.toString());
  }

  @Override
  public boolean equals(AbstractSKDCData absLW)
  {
    if (absLW == null) return(false);
    LoadWordData lw = (LoadWordData)absLW;
    return(lw.getLoadWord().equals(getLoadWord()));
  }

 /**
  * Resets the data in this class to the default.
  */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in the default behaviour

    sLoadWord     = "";
	iWordSequence = 0;
  }

/*---------------------------------------------------------------------------
   Methods to return column names.  These methods facilitate changing column
   names within the database and having least impact on the Controller, and
   View classes.
 ---------------------------------------------------------------------------*/
  public String getLoadWordName()     { return("sLoadWord");      }
  public String getWordSequenceName() { return("iWordSequence");  }

/*---------------------------------------------------------------------------
      Column value get methods go here. These methods do some basic checking
      in most cases to return the default value in case something is not set
      correctly.
  ---------------------------------------------------------------------------*/

  /**
   * Fetches Load Word
   * @return Load Word as string
   */
  public String getLoadWord()
  {
    return(sLoadWord);
  }

  /**
   * Fetches Word Sequence Type
   * @return Word Sequence Type as integer.
   */
  public int getWordSequence()
  {
    return(iWordSequence);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/

  /**
   * Sets LoadWord value.
   */
  public void setLoadWord(String isLoadWord)
  {
    sLoadWord = checkForNull(isLoadWord);
    addColumnObject(new ColumnObject(getLoadWordName(), isLoadWord));
  }

  /**
   * Sets LoadWord type value
   */
  public void setWordSequence(int inWordSequence)
  {
    iWordSequence = inWordSequence;
    addColumnObject(new ColumnObject(getWordSequenceName(), Integer.valueOf(inWordSequence)));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String colName, Object colValue)
  {
    int rtn = 0;
                                       // Set the Super LoadWord.
    if (colName.equalsIgnoreCase(getLoadWordName()))
    {
      setLoadWord(colValue.toString());
    }                                  // Set the LoadWord Description.
    else if (colName.equalsIgnoreCase(getWordSequenceName()))
    {
      setWordSequence(((Integer)colValue).intValue());
    }
    else
    {
      rtn = super.setField(colName, colValue);
    }

    return(rtn);
  }
}
