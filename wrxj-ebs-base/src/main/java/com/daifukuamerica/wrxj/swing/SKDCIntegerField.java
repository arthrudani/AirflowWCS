package com.daifukuamerica.wrxj.swing;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.jdbc.DBInfo;
import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 *   SK Daifuku Integer field component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */
@SuppressWarnings("serial")
public class SKDCIntegerField extends SKDCTextField
{
  private NumberFormat integerFormatter;
  // Default value.
  private boolean mzNegativeIntegerAllowed = false;

  /**
   *  Create integer field with initial value and maximum length.
   *
   *  @param value Initial value.
   *  @param columns Maximum length.
   */
  public SKDCIntegerField(int value, int columns)
  {
    super(columns);
    maxColumns = columns;
    toolkit = Toolkit.getDefaultToolkit();
    integerFormatter = NumberFormat.getNumberInstance(Locale.US);
    integerFormatter.setParseIntegerOnly(true);
    setValue(value);
  }

  /**
   *  Create integer field with initial value and maximum length.
   *
   *  @param value Initial value.
   *  @param columns Maximum length.
   */
  public SKDCIntegerField(String value, int columns) throws NumberFormatException
  {
    super(columns);
    Integer.parseInt(value);           // Will throw format exception if value
    // is not integer.
    maxColumns = columns;
    toolkit = Toolkit.getDefaultToolkit();
    integerFormatter = NumberFormat.getNumberInstance(Locale.US);
    integerFormatter.setParseIntegerOnly(true);
    setText(value);
  }

  /**
   *  Create integer field with initial value and maximum length.
   *
   *  @param columns Maximum length.
   */
  public SKDCIntegerField(int columns)
  {
    super(columns);
    maxColumns = columns;
    toolkit = Toolkit.getDefaultToolkit();
    integerFormatter = NumberFormat.getNumberInstance(Locale.US);
    integerFormatter.setParseIntegerOnly(true);
  }

  /**
   * Create integer field for a database column.
   * 
   * @param columns Maximum length.
   */
  public SKDCIntegerField(String isDBColumnName)
  {
    this(DBInfo.getFieldLength(isDBColumnName));
  }

  /**
   *  Method to get the field value.
   *
   *  @return int containing current value
   */
  public int getValue()
  {
    int retVal = 0;
    try
    {
      retVal = integerFormatter.parse(getText()).intValue();
    }
    catch (ParseException e)
    {
      // This should never happen because insertString allows
      // only properly formatted data to get in the field.
      toolkit.beep();
    }
    return retVal;
  }

  /**
   *  Method to set field current value.
   *
   *  @param value Integer value.
   */
  public void setValue(int value)
  {
    //setText(integerFormatter.format(value));
    setText(value + "");
  }

  /**
   * Set whether or not we allow negative values
   * @param izNegativeIntegerAllowed
   */
  public void allowNegativeValues(boolean izNegativeIntegerAllowed)
  {
    mzNegativeIntegerAllowed = izNegativeIntegerAllowed;
  }

  /**
   *  Method to create the default model for the field.
   *
   *  @return New WholeNumberDocument.
   */
  @Override
  protected Document createDefaultModel()
  {
    return new WholeNumberDocument();
  }

  /**
   *   SK Daifuku Whole number document class. This class provides the
   *   required insertString method that performs validation.
   *
   */
  protected class WholeNumberDocument extends PlainDocument
  {
    /**
     *  Method to perform needed validation on entry. Performs the extra
     *  checking that we need, beeps on any bad input, then invokes the
     *  super.insertString().
     *
     *  @param offs Offset into field.
     *  @param str Text that was entered.
     *  @param a Attribute set.
     *  @exception BadLocationException
     */
    @Override
    public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException
    {
      char[] source = str.toCharArray();
      char[] result = new char[source.length];
      int j = 0;

      if (timingOn)
      {
        restartTimer();
      }

      for (int i = 0; i < result.length; i++)
      {
        if (Character.isDigit(source[i]))
          result[j++] = source[i];
        else if (mzNegativeIntegerAllowed && source[i] == '-')
          result[j++] = source[i];
        else
          toolkit.beep();
      }
      if ((super.getLength() < maxColumns) &&
          (offs + j <= maxColumns))
      {
        super.insertString(offs, new String(result, 0, j), a);
      }
      else
      {
        toolkit.beep();
      }
    }
  }
}
