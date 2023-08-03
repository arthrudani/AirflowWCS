package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description:<BR>
 *    SKDC translation combo box.  This combo box remembers its original
 *    selected value, so that it's easy to set the display back to defaults.
 *    There are three constructors provided.  One uses a translation string
 *    as the initial selected display, the second one uses a translation
 *    integer as its initial selected display, and the third defaults to the
 *    first natural selection in the list.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 11-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class SKDCTranComboBox extends SKDCComboBox
{
  private String   msTransName = "";
  private double      mnDefaultSelectTran;
  private boolean  mzRedoList;
  private String[] masUserList;


  public SKDCTranComboBox()
  {
    super();
  }

//  public SKDCTranComboBox(String isTransName, String isSelectedTran)
//         throws NoSuchFieldException
//  {
//    this();
//    msTransName = isTransName;
//    msDefaultSelectTran = isSelectedTran;
//    addEntries(false);
//  }

  public SKDCTranComboBox(String isTransName, int inSelectedTran)
         throws NoSuchFieldException
  {
    this();
    msTransName = isTransName;
    mnDefaultSelectTran = inSelectedTran;
    msDefaultSelectTran = null;
    addEntries(false);
  }

  public SKDCTranComboBox(String isTransName) throws NoSuchFieldException
  {
    this(isTransName, 0);
  }

//  public SKDCTranComboBox(String isTransName, String isSelectedTran,
//                            boolean all_display) throws NoSuchFieldException
//  {
//      this();
//      mzDisplayAllOption = all_display;
//      msTransName = isTransName;
//      if(mzDisplayAllOption == true)
//      {
//          msDefaultSelectTran = SKDCConstants.ALL_STRING;
//          mnDefaultSelectTran = SKDCConstants.ALL_INT;
//          setSelectedItem(msDefaultSelectTran);
//      }
//      else
//      {
//          msDefaultSelectTran = isSelectedTran;
//      }
//
//      addEntries(false);
//  }

  public SKDCTranComboBox(String isTransName, int inSelectedTran,
                          boolean all_display) throws NoSuchFieldException
  {
      this();
      msTransName = isTransName;
      mzDisplayAllOption = all_display;

      msTransName = isTransName;

      if(mzDisplayAllOption == true)
      {
          mnDefaultSelectTran = SKDCConstants.ALL_INT;
          msDefaultSelectTran = SKDCConstants.ALL_STRING;
          setSelectedItem(msDefaultSelectTran);
      }
      else
      {
          mnDefaultSelectTran = inSelectedTran;
          msDefaultSelectTran = null;
      }

      addEntries(false);
  }

  public SKDCTranComboBox(String isTransName,
                         boolean all_display) throws NoSuchFieldException
  {
    this(isTransName, 0, all_display);
  }

 /**
  *  Constructor for building a user specified translation combo-box.
  *
  *  @param isTransName <code>String</code> containing the name of the translation.
  *  @param iTranList <code>int[]</code> array containing translation values to build
  *         list with.
  *  @param all_display <code>boolean</code> flag to indicate if the ALL string
  *         should be part of the display.
  */
  public SKDCTranComboBox(String isTransName, int[] iTranList, boolean all_display)
         throws NoSuchFieldException
  {
    msTransName = isTransName;
    masUserList = new String[iTranList.length];
    mzDisplayAllOption = all_display;
    
    for(int i = 0; i < iTranList.length; i++)
      masUserList[i] = DBTrans.getStringValue(isTransName, iTranList[i]);
    addEntries(true);
  }

 /**
  *  This method is meant to be used for refreshing the current entries displayed.
  */
  public void setComboBoxData(String isTransName, int[] iTranList)
         throws NoSuchFieldException  
  {
    masUserList = new String[iTranList.length];
    for(int i = 0; i < iTranList.length; i++)
      masUserList[i] = DBTrans.getStringValue(isTransName, iTranList[i]);
    refreshComboModel(masUserList);
  }
  
  /**
   *  Allows selection of combo-box element by Translation integer
   *  representation.
   */
  public void setSelectedElement(double d)
         throws NoSuchFieldException
  {
                                   // If it's a valid translation, great!
                                   // If not, throw exception.
      mnDefaultSelectTran = d;
      msDefaultSelectTran = DBTrans.getStringValue(msTransName,
                                                   d);
      setSelectedItem(msDefaultSelectTran);
  }

  /**
   *  Allows selection of combo-box element by Translation String
   *  representation.
   */
  public void setSelectedElement(String isSelectedTran)
         throws NoSuchFieldException
  {
                                       // If it's a valid translation, great!
                                       // If not, throw exception.
      msDefaultSelectTran = isSelectedTran;
      mnDefaultSelectTran = DBTrans.getIntegerValue(msTransName,
                                                   isSelectedTran);
      setSelectedItem(msDefaultSelectTran);
  }

  /**
   *  Allows set of option to display the "ALL" translation (ALL_STRING)which
   *  would be set to the integer -33 (ALL_INT)....default is to have the
   *  flag set to false
   *  .
   */
  @Override
  public void setDisplayAllEnabled(boolean all_display)
  {
    if(all_display == true)
    {
      mzDisplayAllOption = true;
    }
    else
    {
      mzDisplayAllOption = false;
      mnDefaultSelectTran = 0;
      msDefaultSelectTran = null;
    }
    mzRedoList = true;
    try
    {
      addEntries(false);
    }
    catch(NoSuchFieldException e)
    {
      ;
    }
  }

  private void addEntries(boolean userDefinedList) throws NoSuchFieldException
  {
    if(mzRedoList == true)
    {
        removeAllItems();
        mzRedoList = false;
    }

    String[] cmb_entries;
    if (userDefinedList)
      cmb_entries = masUserList;
    else
      cmb_entries = DBTrans.getStringList(msTransName);

    if(mzDisplayAllOption == true)
    {
        addItem(SKDCConstants.ALL_STRING);
        msDefaultSelectTran = SKDCConstants.ALL_STRING;
        mnDefaultSelectTran = SKDCConstants.ALL_INT;
        setSelectedItem(msDefaultSelectTran);
    }
                                       // Add all entries to the list.
    for(int idx = 0; idx < cmb_entries.length; idx++)
    {
        addItem(cmb_entries[idx]);
    }

    if(mzDisplayAllOption == false)
    {
                                       // Set the default values for future
                                       // reference.
        if (msDefaultSelectTran != null || mnDefaultSelectTran != 0)
        {
          if (msDefaultSelectTran == null)
          {                                // Use the translation integer to get
                                           // the translation string.
            msDefaultSelectTran = DBTrans.getStringValue(msTransName,
                                                         mnDefaultSelectTran);
            setSelectedItem(msDefaultSelectTran);
          }
          else if (mnDefaultSelectTran == 0)
          {                                // Use the translation string to get
                                           // the translation integer.
            mnDefaultSelectTran = DBTrans.getIntegerValue(msTransName,
                                                       msDefaultSelectTran);
          }
        }
        else
        {
          msDefaultSelectTran = getItemAt(0).toString();
          mnDefaultSelectTran = DBTrans.getIntegerValue(msTransName,
                                                       msDefaultSelectTran);
        }
    }

    resetDefaultSelection();           // After above block, the translation
                                       // string will always be filled in.
  }

  public int getIntegerValue() throws NoSuchFieldException
  {
    String isSelectedTran = getSelectedItem().toString();
    if(isSelectedTran.equals(SKDCConstants.ALL_STRING))
    {
        return(SKDCConstants.ALL_INT);
    }
    return(DBTrans.getIntegerValue(msTransName, isSelectedTran));
  }

  public Integer getIntegerObject() throws NoSuchFieldException
  {
    String isSelectedTran = getSelectedItem().toString();
    if(isSelectedTran.equals(SKDCConstants.ALL_STRING))
    {
        return(Integer.valueOf(SKDCConstants.ALL_INT));
    }

    return(DBTrans.getIntegerObject(msTransName, isSelectedTran));
  }

  public String getTranslationName()
  {
    return(msTransName);
  }


}
