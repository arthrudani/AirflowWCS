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
 *   SK Daifuku Double field component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */
@SuppressWarnings("serial")
public class SKDCDoubleField extends SKDCTextField
{
  private NumberFormat numberFormatter;
  private boolean mzNegativeIntegerAllowed = false;

  /**
   * Create double field with initial value and maximum length.
   * 
   * @param idValue Initial value.
   * @param inColumns Maximum length.
   */
  public SKDCDoubleField(double idValue, int inColumns)
  {
    super(inColumns);
    maxColumns = inColumns;
    toolkit = Toolkit.getDefaultToolkit();
    numberFormatter = NumberFormat.getNumberInstance(Locale.US);
    setValue(idValue);
  }

  /**
   * Create double field with maximum length.
   * 
   * @param inColumns Maximum length.
   */
  public SKDCDoubleField(int inColumns)
  {
    this(0.0, inColumns);
  }

  /**
   * Create double field with maximum length.
   * 
   * @param isDBField - the database field that this represents
   */
  public SKDCDoubleField(String isDBField)
  {
    this(DBInfo.getFieldLength(isDBField));
  }

  /**
   * Method to get the field value.
   * 
   * @return double containing current value
   */
  public double getValue()
  {
    double retDoub = 0.0;
    try
    {
      retDoub = numberFormatter.parse(getText()).doubleValue();
    }
    catch (ParseException e)
    {
      // This should never happen because insertString allows
      // only properly formatted data to get in the field.
      toolkit.beep();
    }
    return (retDoub);
  }

  /**
   *  Method to set field current value.
   *
   *  @param value Double value.
   */
  public void setValue(double value)
  {
    setText(numberFormatter.format(value));
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
   *  @return New DoubleNumberDocument.
   */
  @Override
  protected Document createDefaultModel()
  {
    return new DoubleNumberDocument();
  }

  /**
   *   SK Daifuku Double number document class. This class provides the
   *   required insertString method that performs validation.
   *
   */
  protected class DoubleNumberDocument extends PlainDocument
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

      for (int i = 0; i < result.length; i++) {
        if (Character.isDigit(source[i]) || (source[i] == '.'))
          result[j++] = source[i];
        else if (mzNegativeIntegerAllowed && source[i] == '-')
          result[j++] = source[i];
//      else {
//      toolkit.beep();
////    System.err.println("insertString: " + source[i]);
//      }
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
