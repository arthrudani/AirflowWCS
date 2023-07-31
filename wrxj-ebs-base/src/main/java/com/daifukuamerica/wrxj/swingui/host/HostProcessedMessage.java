package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Arrays;
import java.util.Map;
import javax.swing.JCheckBox;

public class HostProcessedMessage
{
  // Number of Columns per row
  final   int             COLNUM = 3;
  
  private String[]        mpStringList = null;
  private int[]           mpIntegerList = null;
  
  
  /**
   * The method returns an array of JCheckBox objects for each defined MessageProcessed value
   * @return an array of JCheckBox objects for each defined MessageProcessed value.
   */
  protected JCheckBox[] buildCheckBoxProcessed()
  {
    JCheckBox [] vpJCheckBox = null;
    
    initialize();
  
    vpJCheckBox = new JCheckBox[mpStringList.length];
    
    for (int i = 0; i < mpStringList.length; i++)
    {
      vpJCheckBox[i] = new JCheckBox(mpStringList[i]);
    }

    return vpJCheckBox;
  }
  
  /**
   * The method returns an array of Objects that contain the values associated with the checked JCheckBox 
   * @param ipCheckboxProcessed the array of JCheckBox
   * @param inSize the number of selected JCheckBox
   * @return an array of Object that contains values associated with selected JCheckBox
   */
  protected Object[] buildSelectedList(JCheckBox[] ipCheckboxProcessed, int inSize)
  {
    Object [] vapProcessed = new Object[inSize];
    int       idx = 0;

    for (int i = 0; i < ipCheckboxProcessed.length; i++)
    {
      if (ipCheckboxProcessed[i].isSelected())
      {
        vapProcessed[idx++] = getIntegerValue(ipCheckboxProcessed[i].getText());
      }
    }

    return vapProcessed;
  }
  
  /**
   * The method returns the number of CheckBox have been selected.
   * @param ipCheckboxProcessed the array of JCheckBox
   * @return the number of CHeckBox have been selected.
   */
  protected int countSelectedCheckBox(JCheckBox[] ipCheckboxProcessed)
  {
    int cnt = 0;
    for (int i = 0; i < ipCheckboxProcessed.length; i++)
    {
      if (ipCheckboxProcessed[i].isSelected()) cnt++;
    }

    return cnt;
  }
  
  /**
   * The method returns the integer value of the next element in the list. If the current
   * element is the last in the list, the first element in the list will be the next element.
   * @param ipDataMap the Data map
   * @return the integer value of the next element to the passed in element.
   */
  protected int getIntegerOfNextInList(Map ipDataMap)
  {
    initialize();

    int currentValue = DBHelper.getIntegerField(ipDataMap, WrxToHostData.MESSAGEPROCESSED_NAME);
    for (int i = 0; i < mpIntegerList.length; i++)
    {
      if (mpIntegerList[i] == currentValue)
      {
        return mpIntegerList[((i + 1) % mpIntegerList.length)];
      }
    }
    
    return 0;
  }

  /*-------------------------------------------------------------------------------------
   * Private Methods
  -------------------------------------------------------------------------------------*/

  /**
   * The method initializes lists.
   */
  private void initialize()
  {
    if (mpIntegerList == null)
    {
      mpIntegerList = DBTrans.getIntegerList(WrxToHostData.MESSAGEPROCESSED_NAME);

      if (mpIntegerList.length > 0)
      {
        // Let sort Integer values
        Arrays.sort(mpIntegerList);

        // Get the String list
        try
        {
          mpStringList = DBTrans.getStringList(WrxToHostData.MESSAGEPROCESSED_NAME, mpIntegerList);
        }
        catch (NoSuchFieldException e) { };
      }
    }
  }
  
  /**
   * The method returns the translated integer value of string
   * @param isName the target string
   * @return the translated integer value of the given string or zero if the string is not found
   */
  private int getIntegerValue(String isName)
  {
    try
    {
      return DBTrans.getIntegerValue(WrxToHostData.MESSAGEPROCESSED_NAME, isName);
    }
    catch (NoSuchFieldException e){};

    return 0;
  }
}
