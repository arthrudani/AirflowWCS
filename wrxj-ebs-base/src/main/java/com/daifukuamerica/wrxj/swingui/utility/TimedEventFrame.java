package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfig;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameEvent;

@SuppressWarnings("serial")
public class TimedEventFrame extends SKDCListFrame
{
  private DBObject mpDBObject = null;

  public TimedEventFrame()
  {
    super("TimedEvents");
    setDetailSearchVisible(false);
    setSearchVisible(false);
    setDisplaySearchCount(true, "Timed Event");
    sktable.leftJustifyStrings();
    
    mpDBObject = new DBObjectTL().getDBObject();
    try { mpDBObject.connect(); }
    catch(DBException e) { }
  }
  
  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(900, 700));
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
    sktable.setToolTipColumns(ControllerConfigData.PROPERTYDESC_NAME);
  }

  void refreshTable()
  {
    try
    {
      ControllerConfig vpCC = Factory.create(ControllerConfig.class);
      ControllerConfigData vpCCD = Factory.create(ControllerConfigData.class);
      vpCCD.setWildcardKey(ControllerConfigData.CONTROLLER_NAME, "TimedEventScheduler", false);
      vpCCD.setWildcardKey(ControllerConfigData.PROPERTYNAME_NAME, "Task", false);
      vpCCD.addOrderByColumn(ControllerConfigData.PROPERTYNAME_NAME);
      refreshTable(vpCC.getAllElements(vpCCD));
      
      if (sktable.getRowCount() == 0)
      {
        displayInfoAutoTimeOut("No data found");
      }
    }
    catch (DBException e)
    {
      e.printStackTrace(System.err);
      displayError("Database Error: " + e);
    }
  }
  
  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   *
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateTimedEvent updateEvent = Factory.create(UpdateTimedEvent.class, "Add Event Parameter");
    addSKDCInternalFrameModal(updateEvent, new JPanel[] {buttonPanel},
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshTable();
            }
          }
        });
  }
   
   /**
    *  Action method to handle Modify button. Brings up screen to do the update.
    */
  @Override
  protected void modifyButtonPressed()
  {
    /*
     * Make sure only one row is selected
     */
    if (sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row can be Modified at a time", "Selection Error");
      return;
    }
    else if (sktable.getSelectedRowCount() == 0)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }

    ControllerConfigData vpCCD = Factory.create(ControllerConfigData.class);  
    vpCCD.dataToSKDCData(sktable.getSelectedRowData());
    
    /*
     * Do the update
     */
    UpdateTimedEvent updateEvent = Factory.create(UpdateTimedEvent.class,
        "Modify Event Parameter");
    updateEvent.setModify(vpCCD, true);

    addSKDCInternalFrameModal(updateEvent, new JPanel[] {buttonPanel},
      new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent e)
        {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
            refreshTable();
          }
        }
      });
  }
    
  
  /**
   * Delete button
   */
  @Override
  protected void deleteButtonPressed()
  {
    ControllerConfigData vpCCD = Factory.create(ControllerConfigData.class);
    ControllerConfig vpCC = Factory.create(ControllerConfig.class);
    
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    
    if (!displayYesNoPrompt("Are you sure?"))
      return;
    
    int delCount = 0;
    int[] deleteIndices = sktable.getSelectedRows();
    for(int row = 0; row < totalSelected; row++)
    {
      vpCCD.dataToSKDCData(sktable.getSelectedRowData());
      String vsController = vpCCD.getController();
      String vsName = vpCCD.getPropertyName();
      String vsValue = vpCCD.getPropertyValue();
     
      TransactionToken vpTT = null;
      try
      {
        vpCCD.setKey(ControllerConfigData.CONTROLLER_NAME, vsController);
        vpCCD.setKey(ControllerConfigData.PROPERTYNAME_NAME, vsName);
        vpCCD.setKey(ControllerConfigData.PROPERTYVALUE_NAME, vsValue);
        vpTT = mpDBObject.startTransaction();
        vpCC.deleteElement(vpCCD);
        mpDBObject.commitTransaction(vpTT);
        sktable.deselectRow(deleteIndices[row]);
        delCount++;
      }
      catch(DBException exc)
      {
        displayError(exc.getMessage(), "Delete Error");
                                     // De-Select the troubling row!
        sktable.deselectRow(deleteIndices[row]);
      }
      finally
      {
        mpDBObject.endTransaction(vpTT);
      }
    }
    if (delCount > 0)
    {
      displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                             " selected Parameters", "Delete Result");
      refreshTable();
    }
  }
  
  protected JPanel getButtonPanel()
  {
    return southPanel;
  }
  
  protected JPanel getScrollPanel()
  {
    return searchPanel;
  }
  
  protected DacTable getSktable()
  {
    return sktable;
  }
  
  protected JScrollPane getSearchPanel()
  {
    return sktable.getScrollPane();
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
    return TimedEventFrame.class;
  }
}
