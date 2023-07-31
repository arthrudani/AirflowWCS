package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.Toolkit;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

 /**
 *   SK Daifuku Date text field component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */
@SuppressWarnings("serial")
public class SKDCDateTextField extends SKDCTextField
{
  private SimpleDateFormat sdf = new SimpleDateFormat();
  private String dFormat = SKDCConstants.DateFormatString;
  private boolean settingFormat = false;

  /**
   *  Create date text field.
   *
   */
  public SKDCDateTextField()
  {
//  super(columns);
    toolkit = Toolkit.getDefaultToolkit();
    sdf.applyPattern(dFormat);
    maxColumns = SKDCConstants.DateFormatString.length();  // match the pattern
    settingFormat = true;
    setText(SKDCConstants.DateFormatString);
    settingFormat = false;
  }

  /**
   *  Method to get the field value.
   *
   *  @return Date containing current value from field
   */
  public Date getDate() {
    Date retDate;

    try
    {
      retDate = null;
      String str = new String (super.getText());
      if (str.length() > 0)
      {
        boolean formatOnly = true;
        for (int i=0; i < str.length() && formatOnly; i++)
        {
          if ((str.charAt(i) != '-') && (str.charAt(i) != ':')
              && (str.charAt(i) != ' '))
          {
            formatOnly = false;
          }
        }
        if (!formatOnly)
        {
          retDate = sdf.parse(str);
        }
      }
    }
    catch(ParseException e)
    {
      System.out.println("Error " + e + " Getting Date field");
      retDate = null;
    }
    return(retDate);
  }

  /**
   *  Method to set field current value.
   *
   *  @param date contains date to use in field.
   */
  public void setDate(Date date)
  {
    if (date != null)
    {
      setText(sdf.format(date));
    }
  }

  /**
   *  Method to create the default model for the field.
   *
   *  @return New DateDocument.
   */
  @Override
  protected Document createDefaultModel() {
    return new DateDocument();
  }

  /**
   * SK Daifuku Date text document class. This class provides the required
   * insertString method that performs validation.
   */
  protected class DateDocument extends PlainDocument
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
      char ch;
      int j = 0;

      if (timingOn)
      {
        restartTimer();
      }

      for (int i = 0; i < result.length; i++)
      {
        ch = checkChar(source[i], offs + i);
        if (ch != 0)
          result[j++] = ch;
        else
          toolkit.beep();
      }
      if (offs + j > maxColumns)
      {
        toolkit.beep();
      }
      else if ((super.getLength() == 0) && (offs + j <= maxColumns))
      { // first time setting up format
        super.insertString(offs, new String(result, 0, j), a);
      }
      else if ((super.getLength() <= maxColumns) &&
          (offs + j <= maxColumns))
      {
        super.remove(offs,j);
        super.insertString(offs, new String(result, 0, j), a);
      }
      else
      {
        toolkit.beep();
      }
    }
  }

  /**
   *  Method to validate the entry.
   *
   *  @param c Character to be checked.
   *  @param offset Location of character
   *  @return boolean of <code>true</code> if character is valid.
   */
  char checkChar(char c, int offset)
  {
    char ch = 0;

    // if a formatting char at this position, replace with the format
    // if char matches format HH,MM,mm, etc, replace with space
    if (offset >= maxColumns)
      return ch;
    if ((dFormat.charAt(offset) == '-') || (dFormat.charAt(offset) == ':')
        || dFormat.charAt(offset) == ' ')
      ch = dFormat.charAt(offset);
    else if (settingFormat && (dFormat.charAt(offset) == c))
      ch = ' ';
    else if ((dFormat.charAt(offset) == 'H') && (Character.isDigit(c)))
      ch = c;
    else if ((dFormat.charAt(offset) == 'm') && (Character.isDigit(c)))
      ch = c;
    else if ((dFormat.charAt(offset) == 's') && (Character.isDigit(c)))
      ch = c;
    else if ((dFormat.charAt(offset) == 'M') && (Character.isDigit(c)))
      ch = c;
    else if ((dFormat.charAt(offset) == 'd') && (Character.isDigit(c)))
      ch = c;
    else if ((dFormat.charAt(offset) == 'y') && (Character.isDigit(c)))
      ch = c;

    return ch;
  }

}
