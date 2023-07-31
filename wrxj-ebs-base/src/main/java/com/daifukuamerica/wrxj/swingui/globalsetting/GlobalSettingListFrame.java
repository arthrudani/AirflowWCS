package com.daifukuamerica.wrxj.swingui.globalsetting;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardGlobalSettingServer;
import com.daifukuamerica.wrxj.dbadapter.data.aed.GlobalSettingData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;

/**
 * A screen class for displaying global settings
 * 
   INSERT INTO ROLEOPTION (SROLE, SCATEGORY, SOPTION, SICONNAME, SCLASSNAME, IBUTTONBAR, IADDALLOWED, IMODIFYALLOWED, IDELETEALLOWED, IVIEWALLOWED, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD) values ('Master', 'Tools', 'Global Settings', '/graphics/global.png', 'globalsetting.GlobalSettingListFrame', 2, 1, 1, 1, 1, null, null, null);
   INSERT INTO ROLEOPTION (SROLE, SCATEGORY, SOPTION, SICONNAME, SCLASSNAME, IBUTTONBAR, IADDALLOWED, IMODIFYALLOWED, IDELETEALLOWED, IVIEWALLOWED, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD) values ('SKDaifuku', 'Tools', 'Global Settings', '/graphics/global.png', 'globalsetting.GlobalSettingListFrame', 2, 1, 1, 1, 1, null, null, null);
   
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'AREA', 'Area', 'N', 3, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'DESCRIPTION', 'Description', 'N', 5, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'GS_CACHED_VALUE_KEY', 'Cached', 'N', 13, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'ID', 'ID', 'N', -1, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'INSTANCEID', 'Instance ID', 'N', -1, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'ISEDITABLE', 'Editable?', 'N', -1, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'MAXVALUE', 'Max', 'N', -1, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'MINVALUE', 'Min', 'N', -1, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'NAME', 'Name', 'N', 4, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'PRODUCTID', 'Product ID', 'N', -1, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'RECOMENDEDVALUE', 'Recommended', 'N', -1, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'REGEX', 'RegEx', 'N', -1, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'TYPEID', 'Type', 'Y', 6, NULL, NULL, NULL);
   INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('GlobalSetting', 'VALUE', 'Value', 'N', 8, NULL, NULL, NULL);
 *
 * @author mandrus
 * @version 1.0
 */
@SuppressWarnings("serial")
public class GlobalSettingListFrame extends SKDCListFrame
{
  private String msSearchName = null;
  
  /**
   *  Create frame.
   */
  public GlobalSettingListFrame()
  {
    super("GlobalSetting");
    setSearchData("Name", GlobalSettingData.NAME_LEN);
    setDisplaySearchCount(true, "global setting");
    refreshTable();
    setDetailSearchVisible(false);
    setAddButtonVisible(false);
    setDeleteButtonVisible(false);
    modifyButton.setText("Refresh Cache");
  }

  /**
   * Refresh the display.
   */
  public void refreshTable()
  {
    try
    {
      refreshTable(Factory.create(StandardGlobalSettingServer.class).getList(msSearchName));
    }
    catch (DBException e)
    {
      logger.logException(e);
      displayError("Database Error: " + e);
    }
  }

  @Override
  protected void refreshButtonPressed()
  {
    
    refreshTable();
  }
  
  @Override
  protected void searchButtonPressed()
  {
    msSearchName = searchField.getText();
    refreshTable();
  }
  
  @Override
  protected void modifyButtonPressed()
  {
    Application.refreshPropertiesLayers();
  }
  
  /**
   * Get the class name that will be used in the RoleOptions table.  This 
   * method facilitates the getting of permissions when setCategoryAndOption()
   * is not called and the implemented class is different from the baseline
   * class.
   * 
   * @return <code>Class</code>
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Class getRoleOptionsClass()
  {
    return GlobalSettingListFrame.class;
  }
}