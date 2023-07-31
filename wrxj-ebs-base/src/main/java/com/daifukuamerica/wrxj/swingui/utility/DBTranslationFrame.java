/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class DBTranslationFrame extends DacInputFrame
{
  SKDCComboBox mpTransFields;
  
  public DBTranslationFrame()
  {
    super("DB Translations", "Database Translation Field Information");
    buildScreen();
    setResizable(true);
  }
  
  private void buildScreen()
  {
    mpTransFields = new SKDCComboBox(DBTrans.getTranslationFields().toArray());
    addInput("Translation Field", mpTransFields);
    showTableWithDefaultButtons("TranslationFields");
    mpBtnAddLine.setVisible(false);
    mpBtnModLine.setVisible(false);
    mpBtnDelLine.setVisible(false);
  }

  /**
   * List the valid translations
   */
  @Override
  protected void okButtonPressed()
  {
    int[] vanValues = DBTrans.getIntegerList(mpTransFields.getText());
    List<Map> vpTransList = new ArrayList<Map>();
    for (int i : vanValues)
    {
      String vsTrans = "Something is horribly wrong"; 
      try
      {
        vsTrans = DBTrans.getStringValue(mpTransFields.getText(), i);
      }
      catch (Exception e)
      {
        displayError(e.getMessage());
        e.printStackTrace();
      }
      Map m = new TreeMap();
      m.put("INT", i);
      m.put("STRING", vsTrans);
      vpTransList.add(m);
    }
    refreshTable(vpTransList);
  }

  /**
   * Clear
   */
  @Override
  protected void clearButtonPressed()
  {
    refreshTable(new ArrayList<Map>());
  }
}
