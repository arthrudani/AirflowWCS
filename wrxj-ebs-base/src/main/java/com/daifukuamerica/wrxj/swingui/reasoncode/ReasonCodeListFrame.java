package com.daifukuamerica.wrxj.swingui.reasoncode;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCodeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of Reason Codes.
 *
 * @author jan
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ReasonCodeListFrame extends SKDCListFrame
{
  protected SKDCTranComboBox cmbReasonCategory;
  protected KeyObject[] filterData;
  
  /*  Create ReasonCodes list frame.
  *
  */
  public ReasonCodeListFrame()
  {
    super("ReasonCode");
    
    try
    {
      cmbReasonCategory = new SKDCTranComboBox(ReasonCodeData.REASONCATEGORY_NAME);
      cmbReasonCategory.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
            reasonCategory_changed();
          }
        });
    }
    catch(NoSuchFieldException e)
    {
      logger.logError("Unable to initialize reason category combo box");
    }

    searchPanel = new JPanel(new FlowLayout());
    searchPanel.setBorder(BorderFactory.createEtchedBorder());
    searchPanel.add(new SKDCLabel("Reason Category:"));
    searchPanel.add(cmbReasonCategory);
    getContentPane().add(searchPanel, BorderLayout.NORTH);
    
    setDisplaySearchCount(true, "reason code");
  }

  /**
   * Sets screen permissions.
   *
   * <p><b>Details:</b> <code>internalFrameOpened</code> augments the
   * supermethod by setting the screen permissions.</p>
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    refreshTable();
  }

  /**
   * Method to set the search criteria filter.
   * 
   * @param iiReasonCategory containing Category to use in search.
   */
  public void setReasonCategoryFilter(String isCategory)
  {
    int iCategory = 0;
    try
    {
      iCategory = DBTrans.getIntegerValue(ReasonCodeData.REASONCATEGORY_NAME,
          isCategory);
    }
    catch (NoSuchFieldException exp)
    {
      exp.printStackTrace();
      return;
    }
    filterData = new KeyObject[1];
    filterData[0] = new KeyObject(ReasonCodeData.REASONCATEGORY_NAME,
      Integer.valueOf(iCategory));
  }

  /**
   * Method to filter by extended search. Refreshes display.
   */
  protected void refreshTable()
  {
    try
    {
      String sCategory = cmbReasonCategory.getSelectedItem().toString();
      setReasonCategoryFilter(sCategory);
      
      StandardInventoryServer invtServer = Factory.create(StandardInventoryServer.class);
      List aList = invtServer.getReasonCodeDataList(filterData);
      refreshTable(aList);
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
    catch (NoSuchElementException e){}
  }

  /**
   *  Action method to handle search button.
   */
  protected void reasonCategory_changed()
  {
    refreshTable();
  }

  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    try
    {
      UpdateReasonCode vpAddReasonCode = Factory.create(UpdateReasonCode.class,
          "Add Reason Code", cmbReasonCategory.getIntegerValue());
      addSKDCInternalFrameModal(vpAddReasonCode, buttonPanel,
        new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE) || prop.equals(FRAME_CLOSING))
            {
                refreshTable();
            }
        }
      });
    }
    catch(NoSuchFieldException e)
    {
      displayWarning(e.getMessage(), "Translation Warning");
    }
  }

  /**
   *  Action method to handle Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    if (isSelectionValidForDelete(true))
    {
      int totalSelected = sktable.getSelectedRowCount();
      if (displayYesNoPrompt("Do you really want to Delete\n"
          + "all selected Reason Codes", "Delete Confirmation"))
      {
        StandardInventoryServer invtServer = Factory.create(StandardInventoryServer.class);
        int delCount = 0;
        int[] deleteIndices = sktable.getSelectedRows();
        for (int row = 0; row < totalSelected; row++)
        {
          try
          {
            ReasonCodeData rcdata = Factory.create(ReasonCodeData.class);
            rcdata.dataToSKDCData(sktable.getRowData(deleteIndices[row]));
            invtServer.deleteReasonCode(rcdata.getReasonCategory(), rcdata.getReasonCode());

            delCount++;
          }
          catch (DBException exc)
          {
            displayError(exc.getMessage(), "Delete Error");
            sktable.deselectRow(deleteIndices[row]);
          }
        }
        if (delCount != totalSelected)
        {
          displayInfo("Deleted " + delCount + " of " + totalSelected
              + " selected rows", "Delete Result");
        }
        else
        {
          displayInfoAutoTimeOut("Deleted " + delCount + " of " + totalSelected
              + " selected rows", "Delete Result");
        }
        refreshTable();
      }
    }
  }

  /**
   * Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {
    if (isSelectionValidForModify(false))
    {
      Integer vnCategory = (Integer)sktable.getCurrentRowDataField(ReasonCodeData.REASONCATEGORY_NAME);
      String vsCode = sktable.getCurrentRowDataField(ReasonCodeData.REASONCODE_NAME).toString();
      String vsDescription = sktable.getCurrentRowDataField(ReasonCodeData.DESCRIPTION_NAME).toString();
      
      UpdateReasonCode updateReasonCode = Factory.create(UpdateReasonCode.class, 
          "Modify Reason Code");
      updateReasonCode.setModify(vnCategory.intValue(), vsCode, vsDescription);
      addSKDCInternalFrameModal(updateReasonCode, buttonPanel,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e)
            {
              String prop = e.getPropertyName();
              if (prop.equals(FRAME_CHANGE) || prop.equals(FRAME_CLOSING))
              {
                refreshTable();
              }
            }
          });
    }
  }
  
  /**
   * Get the class name that will be used in the RoleOptions table.  This 
   * method facilitates the getting of permissions when setCategoryAndOption()
   * is not called and the implemented class is different from the baseline
   * class.
   * 
   * @return <code>Class</code>
   */
  @Override
  protected Class getRoleOptionsClass()
  {
    return ReasonCodeListFrame.class;
  }
}