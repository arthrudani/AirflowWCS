package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Title:        SKDC combo Box screen
 * Description:<BR>
 * @author       A.T.
 *  Modified:    A.D.  Added ComboBoxModel stuff to refresh data.
 * @version      1.0
 * <BR>Created: 04-Mar-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */

@SuppressWarnings("serial")
public class SKDCComboBox extends JComboBox
        implements SKDCGUIConstants
{
  protected boolean timingOn = false;
  protected String  msDefaultSelectTran = null;
  protected boolean mzDisplayAllOption = false;


 /**
  *  Create combo box without data.
  *
  */
  public SKDCComboBox()
  {
    super();
    addTimeout();
    setUIOptions(); 
  }

  /**
   *  Convenience method to auto-fill the combo box with array contents.
   */
  public SKDCComboBox(Object[] items)
  {
    super(items);
    addTimeout();
    setUIOptions(); 
  }
  
  /**
   * Create a combo box with a limited width
   */
  public SKDCComboBox(int limit)
  {
    this();
    JTextField vpText = (JTextField)this.getEditor().getEditorComponent();
    vpText.setDocument(new SKDCTextField(limit).new LimitedLengthTextDocument());
//    vpText = new SKDCTextField(limit);
  }

  /**
   *  Convenience method to auto-fill the combo box with List contents.
   */
  public SKDCComboBox(List items)
  {
    this(items.toArray());
  }

  /**
   *  Convenience method to auto-fill the combo box with array contents.
   */
  public SKDCComboBox(Object[] items, boolean allDisplay)
  {
    this(items);
    mzDisplayAllOption = allDisplay;
    if(mzDisplayAllOption == true)
    {
      refreshComboModel(items);
    }
  }

  /**
   *  Convenience method to auto-fill the combo box with List contents.
   */
  public SKDCComboBox(List items, boolean allDisplay)
  {
    this(items.toArray(), allDisplay);
  }
  
  /*--------------------------------------------------------------------------*/

  /**
   * Make all SKDCComboBoxes look the same
   */
  private void setUIOptions()
  {
    /*
     * These look REALLY bad in the default Java Look&Feel, so don't do them
     * unless we change the L&F again.
     */ 
//    setForeground(Color.black);
//    setBackground(Color.white);
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   *  Refresh method.
   *  @param items      - The list for the combo box
   *  @param izAddBlank - Add a blank to the beginning of the list
   */
  public void setComboBoxData(Object[] items, boolean izAddBlank)
  {
    refreshComboModel(items);
    if (izAddBlank)
      insertBlank();
  }

  /**
   *  Refresh method.
   *  <BR>Convenience method for setComboBoxData(Object[], boolean)
   */
  public void setComboBoxData(Object[] items)
  {
    setComboBoxData(items, false);
  }
  
  /**
   *  Refresh method.
   *  <BR>Convenience method for setComboBoxData(Object[], boolean)
   */
  public void setComboBoxData(List items)
  {
    setComboBoxData(items.toArray(), false);
  }

  /**
   *  Refresh method.
   *  <BR>Convenience method for setComboBoxData(Object[], boolean)
   */
  public void setComboBoxData(List items, boolean izAddBlank)
  {
    setComboBoxData(items.toArray(), izAddBlank);
  }
  
 /**
  * Refresh method that allows a user specified string to be pre-pended to the
  * displayed list.
  * @param iapEntries the array of entries to display.
  * @param isFirstEntry the string that will become the first entry on the list.
  */
  public void setComboBoxData(Object[] iapEntries, String isFirstEntry)
  {
    int vnOrigLength = iapEntries.length;
    Object[] vapNewEntries = null;

    if (isFirstEntry.equals(SKDCConstants.NO_PREPENDER))
    {
      vapNewEntries = iapEntries;
    }
    else
    {
      vapNewEntries = new Object[vnOrigLength + 1];
      vapNewEntries[0] = isFirstEntry;

      for(int vnIdx = 1; vnIdx < vnOrigLength+1; vnIdx++)
      {
        vapNewEntries[vnIdx] = iapEntries[vnIdx-1];
      }
    }
    
    refreshComboModel(vapNewEntries);
  }
  
  /**
   * Set the prototype display length
   * @param inLength
   */
  public void setPrototypeDisplayLength(int inLength)
  {
     String s = "";
     for (int i = 0; i < inLength; i++)
     {
       s = s + "W";
     }
     setPrototypeDisplayValue(s);
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   *  Since the getIndexOf method does not appear to work correctly for
   *  String literals, this is a method to get the zero based index of a
   *  selected item. It is assumed all elements of a ComboBox are strings.
   */
//  private int getSelectedItemIndex()
//  {
//    return getSelectedIndex();
//  }

 /**
  *  Method to restart timer when the selected item changes.
  */
  @Override
  public void selectedItemChanged()
  {
    if (timingOn)
    {
      restartTimer();
    }
    super.selectedItemChanged();
  }

 /**
  *  Method to get selected item text.
  *
  *  @return String containing text of selected item.
  */
  public String getText()
  {
    if (getSelectedIndex() >=0)
    {
      return (getSelectedItem().toString());
    }
    else
    {
      return (getEditor().getItem().toString());
    }
  }

 /**
  * Method sets the selection to the <i>first</i> match of the passed partial
  * string.
  * @param isPartialString the partial string to match in the combo box.
  * @return the index of the partial match.
  */
  public int selectItemBy(String isPartialString)
  {
    int vnMatchedIndex = 0;
    CharSequence vpSearchSequence = isPartialString.subSequence(0, isPartialString.length());
    for (int i = 0; i < getItemCount(); i++)
    {
      if (((String)getItemAt(i)).contains(vpSearchSequence))
      {
        vnMatchedIndex = i;
        setSelectedIndex(vnMatchedIndex);
        break;
      }
    }
    
    return(vnMatchedIndex);
  }

  /*--------------------------------------------------------------------------*/
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
  *  Method to restart the component time out.
  */
  public void restartTimer()
  {
    firePropertyChange(FRAME_TIMER_RESTART, "Old" , "New");
  }
  /**
   *  Allows set of option to display the "ALL" translation
   *  .
   */
  public void setDisplayAllEnabled(boolean allDisplay)
  {
    mzDisplayAllOption = allDisplay;
    refreshComboModel();
  }

          // Put here to be used with either the skdccombo or the skdctrancombo
          // so that the interface to both is similar
  public void resetDefaultSelection()
  {
    setSelectedItem(this.msDefaultSelectTran);
  }
/*--------------------------------------------------------------------------
             ******** All private methods go here. ********
  --------------------------------------------------------------------------*/
  private void addTimeout()
  {
    this.addFocusListener(new java.awt.event.FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent fe)
      {
        if (timingOn)
        {
          restartTimer();
        }
      }
    });
  }

  // This refreshes the model with an object array passed in
  protected void refreshComboModel(Object[] items)
  {
    ((DefaultComboBoxModel)dataModel).removeAllElements();
    if (mzDisplayAllOption == true)
    {
      ((DefaultComboBoxModel)dataModel).addElement(SKDCConstants.ALL_STRING);
      this.msDefaultSelectTran = SKDCConstants.ALL_STRING;
      setSelectedItem(this.msDefaultSelectTran);
    }
    for (int idx = 0; idx < items.length; idx++)
    {
      ((DefaultComboBoxModel)dataModel).addElement(items[idx]);
    }
  }

  /**
   * This refreshes the model by reading its own elements and
   * re-adding them either with 'ALL' or without depending on the
   * display_all_option
   */
  protected void refreshComboModel()
  {
    DefaultComboBoxModel items = new DefaultComboBoxModel();
    if (mzDisplayAllOption == true)
    {
      items.addElement(SKDCConstants.ALL_STRING);
      this.msDefaultSelectTran = SKDCConstants.ALL_STRING;
      setSelectedItem(this.msDefaultSelectTran);
    }
    int idx;
    int count = getItemCount();
    for (idx = 0; idx < count; idx++)
    {
      Object o = getItemAt(idx);
      String st = o.toString();
      if (!st.equals(SKDCConstants.ALL_STRING))
      {
        items.addElement(o);
      }
    }
    setModel(items);
  }
  
  /**
   * This refreshes the model by reading its own elements and
   * re-adding them either with 'ALL' or without depending on the
   * display_all_option
   */
  protected void insertBlank()
  {
    DefaultComboBoxModel items = new DefaultComboBoxModel();
    items.addElement("");
    msDefaultSelectTran = "";
    setSelectedItem("");

    int idx;
    int count = getItemCount();
    for (idx = 0; idx < count; idx++)
    {
      Object o = getItemAt(idx);
      String st = o.toString();
      if (!st.equals(""))
      {
        items.addElement(o);
      }
    }
    setModel(items);
  }

}