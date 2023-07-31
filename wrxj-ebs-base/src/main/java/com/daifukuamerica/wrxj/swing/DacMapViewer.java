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

package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public class DacMapViewer extends DacInputFrame
{
  private static final int MAXWIDTH = 20;
  
  /**
   * Constructor
   * 
   * @param isFrameTitle
   * @param isInputTitle
   * @param ipMapToView
   * @param isViewName
   */
  public DacMapViewer(String isFrameTitle, String isInputTitle, 
      Map ipMapToView, String isViewName)
  {
    super(isFrameTitle, isInputTitle);
    buildScreen(ipMapToView, isViewName);
  }

  /**
   * Build the screen
   * 
   * @param ipMapToView
   * @param isViewName
   */
  private void buildScreen(Map ipMapToView, String isViewName)
  {
    List<DataEntry> vpDataList = getDisplayData(ipMapToView, isViewName);
    Collections.sort(vpDataList);
    
    for (DataEntry d : vpDataList)
    {
      int vnLength = DBInfo.getFieldLength(d.msKey);
      int vnDisplayLength = d.getDisplayValue().length();
      vnLength = Math.max(vnLength, vnDisplayLength);
      vnLength = Math.min(vnLength, MAXWIDTH); 
      SKDCTextField vpTxt = new SKDCTextField(vnLength);
      if (vnDisplayLength > MAXWIDTH)
      {
        vpTxt.setMaxColumns(vnDisplayLength);
      }
      vpTxt.setEnabled(false);
      vpTxt.setText(d.getDisplayValue());
      if (vnDisplayLength > MAXWIDTH)
      {
        vpTxt.setCaretPosition(0);
      }
      
      addInput(d.msDisplayKey, vpTxt);
    }
    
    setInputColumns((vpDataList.size() + 14)/15);
    useReadOnlyButtons();
  }
  
  /**
   * Turn the map into more human-friendly data
   * 
   * @param ipMapToView
   * @param isViewName
   * @return
   */
  private List<DataEntry> getDisplayData(Map ipMapToView, String isViewName)
  {
    AsrsMetaData vpAMD = Factory.create(AsrsMetaData.class);
    AsrsMetaDataData vpAMDKey = Factory.create(AsrsMetaDataData.class);
    AsrsMetaDataData vpAMDData;
    List<DataEntry> vpDataList = new ArrayList<DataEntry>();
    
    Set<String> vasKeys = ipMapToView.keySet();
    for (String s : vasKeys)
    {
      DataEntry d = new DataEntry();
      d.msKey = s;
      d.mpValue = ipMapToView.get(s);
      
      vpAMDKey.clear();
      vpAMDKey.setKey(AsrsMetaDataData.DATAVIEWNAME_NAME, isViewName);
      vpAMDKey.setKey(AsrsMetaDataData.COLUMNNAME_NAME, s.toUpperCase());
      try
      {
        vpAMDData = vpAMD.getElement(vpAMDKey, DBConstants.NOWRITELOCK);
        d.mnDisplayOrder = vpAMDData.getDisplayOrder();
        d.msDisplayKey = vpAMDData.getFullName();
        d.mzIsTranslation = vpAMDData.getIsTranslation().equals("Y");
      }
      catch (Exception e)
      {
        // Don't bother logging the Exception.
        d.mnDisplayOrder = -2;
        d.msDisplayKey = s;
        d.mzIsTranslation = true;  // It may not be, but we can try.
        if (!SKDCUserData.isSuperUser()) continue;
      }

      vpDataList.add(d);
    }
    
    return vpDataList;
  }
  
  /**
   * <B>Description:</B> An entry to be displayed on the Viewer
   *
   * @author       mandrus
   * @version      1.0
   * 
   * <BR>Copyright (c) 2007 by Daifuku America Corporation
   */
  private class DataEntry implements Comparable
  {
    int mnDisplayOrder;
    String msKey;
    Object mpValue;
    String msDisplayKey;
    boolean mzIsTranslation;

    /**
     * Get the displayable value
     * @return
     */
    public String getDisplayValue()
    {
      if (mpValue == null)
      {
        return "null";
      }
      if (mpValue instanceof Date)
      {
        SimpleDateFormat vpSDF = new SimpleDateFormat();
        vpSDF.applyPattern(SKDCConstants.DATETIME_FORMAT2);
        return vpSDF.format((Date)mpValue);
      }
      else if (mzIsTranslation && (mpValue instanceof Integer))
      {
        try
        {
          String vsReturn = DBTrans.getStringValue(msKey, (Integer)mpValue);
          if (SKDCUserData.isSuperUser())
          {
            vsReturn += " (" + mpValue.toString() + ")";
          }
          return vsReturn;
        }
        catch (NoSuchFieldException e){}
      }
      String vsValue = mpValue.toString();
      vsValue = vsValue.replace("\n", " - ");
      return vsValue;
    }

    /**
     * Ordering with everything on the screen
     * @return
     */
    private int getDisplayOrder()
    {
      if (mnDisplayOrder < 0)
      {
        return mnDisplayOrder * -100;
      }
      else return mnDisplayOrder; 
    }

    /**
     * To make this sortable
     */
    public int compareTo(Object o)
    {
      if (o instanceof DataEntry)
      {
        DataEntry d = (DataEntry)o;
        if (d.getDisplayOrder() != getDisplayOrder())
        {
          return (getDisplayOrder() - d.getDisplayOrder());
        }
        else
        {
          return msDisplayKey.compareTo(d.msDisplayKey);
        }
      }
      return 0;
    }
  }
}
