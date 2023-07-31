package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

 /**
 *   SK Daifuku Limited length text field component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */
@SuppressWarnings("serial")
public class SKDCTextField extends JTextField
        implements SKDCGUIConstants
{
  Toolkit toolkit;
  int maxColumns = 0;
  protected boolean timingOn = false;
  private boolean illegalCharacterChecking = true;
  private boolean forceCapitalization = false;
  private char[] illegalChars = { '`','\"','\'','%',';' };

  /**
   *  Create text field with no maximum length.
   */
  public SKDCTextField()
  {
    super();
    timingOn = false;
    toolkit = Toolkit.getDefaultToolkit();
    forceCapitalization = Application.getBoolean("UseCapitalizedInput", false);
    addMouseListener(new SKDCTextFieldListener(this));
    addFocusListener(new java.awt.event.FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent fe)
      {
//        System.out.println("---- focusGained" + fe.getClass().getName());
        if (timingOn)
        {
          restartTimer();
        }

        // try to set the field contents to selected
        selectAll();
      }
    });
  }

  /**
   *  Create text field with maximum length.
   *
   *  @param columns Maximum length.
   */
  public SKDCTextField(int columns)
  {
    this();
    setColumns(columns);
  }

  /**
   * Create a new SKDCTextField with a number of columns to accommodate a DB
   * field
   * 
   * @param isDBField
   */
  public SKDCTextField(String isDBField)
  {
    this();
    setColumns(DBInfo.getFieldLength(isDBField));
  }

  /**
   * Create a new SKDCTextField with a number of columns to accommodate a DB
   * field
   * 
   *  @param columns Maximum length.
   * @param isDBField
   */
  public SKDCTextField(int columns, String isDBField)
  {
    this();
    setColumns(columns);
    setMaxColumns(DBInfo.getFieldLength(isDBField));
  }

  /**
   * Create a new SKDCTextField with a number of columns to accommodate a DB
   * field
   * 
   * @param columns    length of the field.
   * @param maxColumns Maximum length of the data
   */
  public SKDCTextField(int columns, int maxColumns)
  {
    this();
    setColumns(columns);
    setMaxColumns(maxColumns);
  }
  
  /**
   *  Method to set the fields display length.
   *
   *  @param columns display length.
   */
  @Override
  public void setColumns(int columns)
  {
    super.setColumns(columns > 5 ? columns : columns + 1);
    maxColumns = columns;
  }

  /**
   *  Method to set the fields maximum length.
   *
   *  @param columns maximum length.
   */
  public void setMaxColumns(int columns)
  {
    maxColumns = columns;
  }

  /**
   * No more field collapsing!
   */
  @Override
  public Dimension getMinimumSize()
  {
     return getPreferredSize();
  }
  
  /**
   *  Method to enable / disable the component time out.
   *
   *  @param on Enable value.
   */
  public void enableTimer(boolean on)
  {
    timingOn = on;
  }

  /**
   *  Method to enable / disable illegal character checking.
   *
   *  @param on Enable value.
   */
  public void setIllegalCharacterChecking(boolean on)
  {
    illegalCharacterChecking = on;
  }

  /**
   *  Method to enable / disable forcing of capitalization.
   *
   *  @param on Enable value.
   */
  public void setForceCapitalization(boolean on)
  {
    forceCapitalization = on;
  }

  /**
   *  Method to restart the component time out.
   *
   */
  public void restartTimer ()
  {
//    System.out.println("Timer restarted SKDCTextField");
      firePropertyChange(FRAME_TIMER_RESTART,"Old","New");
  }

  /**
   *  Method to create the default model for the field.
   *
   *  @return New LimitedLengthTextDocument.
   */
  @Override
  protected Document createDefaultModel()
  {
    return new LimitedLengthTextDocument();
  }

  /**
   *   SK Daifuku Limited length text document class. This class provides the
   *   required insertString method that performs validation.
   *
   */
  protected class LimitedLengthTextDocument extends PlainDocument
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
        if (checkEntry(source[i]))
          result[j++] = forceCapitalization ? 
              Character.toUpperCase(source[i]) : source[i];
        else
        {
          toolkit.beep();
        }
      }

      if ((super.getLength() < maxColumns)
          && (offs + source.length <= maxColumns))
      {
        super.insertString(offs, new String(result, 0, j), a);
      }
      else
      {
        toolkit.beep();
      }
    }
  }

  /**
   * Returns the trimmed text contained in this <code>TextComponent</code>.
   *
   * @return the trimmed text
   */
  @Override
  public String getText()
  {
    return super.getText().trim();
  }

  /**
   *  Method to determine if a field is empty.
   * @return <code>true</code> if field is empty, <code>false</code> otherwise.
   */
  public boolean isEmpty()
  {
    return(getText().trim().length() == 0);
  }
  
  /**
   *  Method to validate the entry.
   *
   *  @param c Character to be checked.
   *  @return boolean of <code>true</code> if character is valid.
   */
  boolean checkEntry(char c)
  {
    if (c < 31) return false;

    if (illegalCharacterChecking)
    {
//    if ((c >= 33) && (c <= 47)) return false;
//    if ((c >= 58) && (c <= 64)) return false;
//    if ((c >= 91) && (c <= 64)) return false;
      for (int i = 0; i < illegalChars.length; i++)
      {
        if (c == illegalChars[i])
           return false;
      }
    }

    return true;
  }
}
