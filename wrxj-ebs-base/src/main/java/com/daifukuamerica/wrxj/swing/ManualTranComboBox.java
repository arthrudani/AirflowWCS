/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2017 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manual translation combo box.
 */
public class ManualTranComboBox<K> extends SKDCComboBox
{
  private static final long serialVersionUID = -8744051780059674087L;
  
  private Map<K, String> mpKeyToDisplay;
  private Map<String, K> mpDisplayToKey;

  /**
   * Constructor
   * 
   * @param ipTranslations map of key=displayable value
   */
  public ManualTranComboBox(Map<K, String> ipTranslations)
  {
    super();
    
    mpKeyToDisplay = ipTranslations;
    mpDisplayToKey = mpKeyToDisplay.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    resetComboBoxData();
  }

  /**
   * Add an element to the combo box
   * 
   * @param ipKey
   * @param isValue
   */
  public void addElement(K ipKey, String isValue)
  {
    mpKeyToDisplay.put(ipKey, isValue);
    mpDisplayToKey.put(isValue, ipKey);
    resetComboBoxData();
  }
  
  /**
   * Rebuild the combo box list based upon the current translation mapping
   */
  private void resetComboBoxData()
  {
    List<String> mpDisplayed = new ArrayList<>();
    mpDisplayed.addAll(mpKeyToDisplay.values());
    mpDisplayed.sort(new Comparator<String>() {
      @Override
      public int compare(String o1, String o2)
      {
        if (o1.equals(SKDCConstants.ALL_STRING)) return -1;
        if (o2.equals(SKDCConstants.ALL_STRING)) return 1;
        return o1.compareTo(o2);
      }});
    setComboBoxData(mpDisplayed);
  }
  
  /**
   * Select combo-box element by key
   */
  public void setSelectedKey(K ipKey)
         throws NoSuchFieldException
  {
    String vsDisplayedValue = mpKeyToDisplay.get(ipKey);
    setSelectedItem(vsDisplayedValue);
  }
  
  /**
   * Get the key value of the selected item
   * @return
   */
  public K getSelectedKey()
  {
    return mpDisplayToKey.get(getSelectedItem());
  }
}
