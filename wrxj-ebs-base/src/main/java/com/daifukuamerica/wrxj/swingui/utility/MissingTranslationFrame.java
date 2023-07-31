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

import com.daifukuamerica.wrxj.swing.DacTranslator;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;

@SuppressWarnings("serial")
public class MissingTranslationFrame extends SKDCListFrame
{
  SKDCComboBox mpLanguage = new SKDCComboBox(DacTranslator.getLanguages());
  
  public MissingTranslationFrame()
  {
    super("MissingTranslations");
    setDisplaySearchCount(true, "missing translation");
    setSearchData("Language", mpLanguage);
    getButtonPanel().setVisible(false);
    sktable.leftJustifyStrings();
  }
  
  @Override
  protected void searchButtonPressed()
  {
    refreshTable(DacTranslator.listMissingTranslations(mpLanguage.getText()));
  }
  
  @Override
  protected Class getRoleOptionsClass()
  {
    return MissingTranslationFrame.class;
  }
}
