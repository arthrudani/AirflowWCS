/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.swingui.aesystem;

import com.daifukuamerica.wrxj.dataserver.standard.StandardAeSystemServer;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunications;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunicationsData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.wynright.wrxj.app.Wynsoft;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

/*
  INSERT INTO ROLEOPTION (SROLE, SCATEGORY, SOPTION, SICONNAME, SCLASSNAME, IBUTTONBAR, IADDALLOWED, IMODIFYALLOWED, IDELETEALLOWED, IVIEWALLOWED, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD) 
    values ('SKDaifuku', 'Developer', 'AE Instance Comms', '/graphics/AEConfig.png', 'aesystem.InstanceCommsListFrame', 2, 1, 1, 1, 1, null, null, null);
    
  INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('InstanceCommsListFrame', 'COMM_INTERFACE', 'Comm Interface', 'N', 6, NULL, NULL, NULL);
  INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('InstanceCommsListFrame', 'COMM_TYPE_NAME', 'Comm Type Name', 'N', 5, NULL, NULL, NULL);
  INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('InstanceCommsListFrame', 'COMMUNICATION_TYPE_ID', 'CommType ID', 'N', 4, NULL, NULL, NULL);
  INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('InstanceCommsListFrame', 'RECEIVER_ID', 'Receiver ID', 'N', 2, NULL, NULL, NULL);
  INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('InstanceCommsListFrame', 'RECEIVER_NAME', 'Receiver Name', 'N', 3, NULL, NULL, NULL);
  INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('InstanceCommsListFrame', 'SENDER_ID', 'Sender ID', 'N', 0, NULL, NULL, NULL);
  INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder, dModifyTime, sAddMethod, sUpdateMethod) VALUES ('InstanceCommsListFrame', 'SENDER_NAME', 'Sender Name', 'N', 1, NULL, NULL, NULL);
 */

/**
 * A screen class for displaying a list of AE System Instance Communications
 * definitions to aid in system setup.
 *
 * @author mandrus
 * @version 1.0
 */
@SuppressWarnings("serial")
public class InstanceCommsListFrame extends SKDCListFrame
{
  private StandardAeSystemServer mpAeSysServer = Factory.create(StandardAeSystemServer.class);

  /**
   * Create PecPlantBasedRouteListFrame.
   */
  public InstanceCommsListFrame()
  {
    super("InstanceCommsListFrame");
    setDisplaySearchCount(true, "communications definition", "communications definitions", true);
    setSearchData("Instance ID:", 5);
    searchButtonPressed();
    refreshButton.setVisible(false);
    modifyButton.setVisible(false);
  }

  /**
   * Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    refreshTable(searchField.getText());
  }

  /**
   * Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    InstanceCommunicationsInput vpAddFrame = Factory.create(
        InstanceCommunicationsInput.class, "Add Instance Communication");
    addSKDCInternalFrameModal(vpAddFrame,
        new JPanel[] { buttonPanel, searchPanel },
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent e)
          {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshTable(searchField.getText());
            }
          }
        });
  }
  
  /**
   * Action method to handle Delete button.
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected void deleteButtonPressed()
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to delete", "Selection Error");
      return;
    }

    if (displayYesNoPrompt("Do you really want to delete\nall selected Items",
        "Delete Confirmation "))
    {
      // Get selected list
      List<Map> delList = sktable.getSelectedRowDataArray();

      int delCount = 0;
      int[] deleteIndices = sktable.getSelectedRows();
      for (int row = 0; row < totalSelected; row++)
      {
        try
        {
          int vnSend = (Integer)delList.get(row).get(InstanceCommunicationsData.SENDER_ID_NAME);
          int vnComp = (Integer)delList.get(row).get(InstanceCommunicationsData.SENDER_COMPONENT_ID_NAME);
          int vnRecv = (Integer)delList.get(row).get(InstanceCommunicationsData.RECEIVER_ID_NAME);
          int vnComm = (Integer)delList.get(row).get(InstanceCommunicationsData.COMMUNICATION_TYPE_ID_NAME);
          mpAeSysServer.deleteInstanceCommunication(vnSend, vnComp, vnRecv, vnComm);
          delCount++;
        }
        catch (DBException exc)
        {
          displayError(exc.getMessage(), "Delete Error");
          // De-Select the troubling row!
          sktable.deselectRow(deleteIndices[row]);
        }
      }
      if (delCount != totalSelected)
      {
        displayInfo(
            "Deleted " + delCount + " of " + totalSelected + " selected rows",
            "Delete Result");
      }
      else
      {
        displayInfoAutoTimeOut(
            "Deleted " + delCount + " of " + totalSelected + " selected rows",
            "Delete Result");
      }
      sktable.deleteSelectedRows(); // Update the display.
    }
  }

  /**
   * Method to filter by instance ID. Refreshes display.
   *
   * @param isID to search for.
   */
  protected void refreshTable(String isID)
  {
    try
    {
      int vnInstanceId = SKDCUtility.isBlank(isID) ? 
          Wynsoft.getInstanceId() : Integer.parseInt(isID);
      refreshTable(Factory.create(InstanceCommunications.class).getList(vnInstanceId));
    }
    catch (Exception e)
    {
      displayError("Error: " + e);
      logger.logException(e);
    }
  }

  /**
   * Get the class name that will be used in the RoleOptions table. This method
   * facilitates the getting of permissions when setCategoryAndOption() is not
   * called and the implemented class is different from the baseline class.
   * 
   * @return <code>Class</code>
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Class getRoleOptionsClass()
  {
    return InstanceCommsListFrame.class;
  }
}
