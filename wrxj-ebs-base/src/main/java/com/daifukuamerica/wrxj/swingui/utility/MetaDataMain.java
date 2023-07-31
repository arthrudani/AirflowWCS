package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.JDBCMetaData;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

/**
 * Description:<BR>
 *    Primary frame for ASRS Metadata Maintenance
 *
 * @author       A.D.
 * @version      1.0
 */
public class MetaDataMain extends SKDCInternalFrame
{
  private static final long serialVersionUID = 0L;
  
  private final String      ADD_VIEW_BTN = "ADDVIEW";
  private final String      DELETE_VIEW_BTN = "DELETEVIEW";
  private JPanel            btnPanel;
  private JPanel            inputPanel;
  private AsrsMetaData      amd;
  private AsrsMetaDataData  mddata;
  private SKDCComboBox      availMetaViewCombo = new SKDCComboBox();
  private SKDCButton        btnAdd;
  private SKDCButton        btnAddView;
  private SKDCButton        btnDeleteView;
  private SKDCButton        btnModify;
  private SKDCButton        btnDelete;
  private ActionListener    btnEventListener;
  private SKDCPopupMenu     popupMenu = new SKDCPopupMenu();
  private DacTable         sktable;
  private List              arrayList = new ArrayList();
  private String            currentSelectedView;
  private DBObject          dbobj    = null;
  //private Logging           logger;
  
  public MetaDataMain() throws Exception
  {
    super("ASRS MetaData Main");
    JDBCMetaData.init();
    dbobj = new DBObjectTL().getDBObject();
    amd = Factory.create(AsrsMetaData.class);
    mddata = Factory.create(AsrsMetaDataData.class);
                                       // Set up meta-data view combo.
    initMetaViewCombo();
    setMetaViewComboListener();

    defineButtons();
    sktable = new DacTable(new DacModel(arrayList, "AsrsMetaData"));
    setTableMouseListener();
   
    Container cp = getContentPane();
    inputPanel = buildInputPanel();
    btnPanel = buildButtonPanel();
    cp.add(inputPanel, BorderLayout.NORTH);
    cp.add(sktable.getScrollPane(), BorderLayout.CENTER);
    cp.add(btnPanel, BorderLayout.SOUTH);
  }
  
 /**
  * Overridden method so we can fake initial comboBox event for station.
  *
  * @param ipEvent ignored
  */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);
    initAsrsMetadataSelectEvent();
  }
  
  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(895, 420));
  }

/*===========================================================================
                  ****** Event Listeners go here ******
  ===========================================================================*/
 /**
  * Property Change event listener for Search frame for the Add view frame.
  */
  private class AddMetaViewFrameDataHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String newMetaDataViewName = pcevt.getPropertyName();
      if (newMetaDataViewName.equals(FRAME_CHANGE))
      {
        initMetaViewCombo();
      }
    }
  }
  
  /**
   *   Property Change event listener for Add/Modify frame.
   */
  private class ColumnChangeFrameDataHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(SKDCGUIConstants.MODIFY_BTN))
      {
        AsrsMetaDataData old_mddata = (AsrsMetaDataData)pcevt.getOldValue();
        AsrsMetaDataData new_mddata = (AsrsMetaDataData)pcevt.getNewValue();
        if (!old_mddata.equals(new_mddata)) sktable.modifySelectedRow(new_mddata);
      }
      else if (prop_name.equals(SKDCGUIConstants.ADD_BTN))
      {
        AsrsMetaDataData old_mddata = (AsrsMetaDataData)pcevt.getOldValue();
        AsrsMetaDataData new_mddata = (AsrsMetaDataData)pcevt.getNewValue();
        if (!old_mddata.equals(new_mddata)) sktable.appendRow(new_mddata);
      }
    }
  }

  private void setMetaViewComboListener()
  {
    availMetaViewCombo.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          processMetaDataSelectEvent((String)e.getItem());
        }
      }
    });    
  }
  
  /**
   *  Defines all buttons on the main ASRS Meta-Data Panels, and adds listeners
   *  to them.
   */
  private void setButtonListeners()
  {
    btnEventListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        String which_button = e.getActionCommand();
        if (which_button.equals(ADD_VIEW_BTN))
        {
          addViewButtonPressed();
        }
        else if (which_button.equals(DELETE_VIEW_BTN))
        {
          deleteViewButtonPressed();
        }
        else if (which_button.equals(SKDCGUIConstants.ADD_BTN))
        {
          addButtonPressed();
        }
        else if (which_button.equals(SKDCGUIConstants.MODIFY_BTN))
        {
          modifyButtonPressed();
        }
        else if (which_button.equals(SKDCGUIConstants.DELETE_BTN))
        {
          deleteButtonPressed();
        }
      }
    };
                                       // Attach listeners.
    btnAddView.addEvent(ADD_VIEW_BTN, btnEventListener);
    btnDeleteView.addEvent(DELETE_VIEW_BTN, btnEventListener);
    btnAdd.addEvent(SKDCGUIConstants.ADD_BTN, btnEventListener);
    btnModify.addEvent(SKDCGUIConstants.MODIFY_BTN, btnEventListener);
    btnDelete.addEvent(SKDCGUIConstants.DELETE_BTN, btnEventListener);
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
 /**
  *  Method allows a meta-data view to be added.
  */
  private void addViewButtonPressed()
  {
    addSKDCInternalFrameModal(Factory.create(AddMetaViewFrame.class),
                              new JPanel[] { inputPanel, btnPanel },
                              new AddMetaViewFrameDataHandler());
  }

 /**
  *  Method allows for the deletion of a complete meta-data view.
  *
  *  @return <code></code>
  */
  private void deleteViewButtonPressed()
  {
    mddata.clear();
    mddata.setKey(AsrsMetaDataData.DATAVIEWNAME_NAME, currentSelectedView);
    TransactionToken tt = null;
    try
    {
      if (displayYesNoPrompt("Delete View " + currentSelectedView, "Delete Confirmation"))
      {
        tt = dbobj.startTransaction();
        amd.deleteElement(mddata);
        dbobj.commitTransaction(tt);
        displayInfoAutoTimeOut("View " + currentSelectedView + " Deleted",
                               "Delete Result");
        availMetaViewCombo.removeItemAt(availMetaViewCombo.getSelectedIndex());
        currentSelectedView = "";
        sktable.clearTable();
      }
    }
    catch(DBException exc)
    {
      displayError(exc.getMessage(), "Delete Error");
      sktable.clearTable();
    }
    finally
    {
      dbobj.endTransaction(tt);
    }
  }

 /**
  *  Method allows for adding a column to an existing meta data column.
  */
  private void addButtonPressed()
  {
    mddata.clear();
    mddata.setDataViewName((String)availMetaViewCombo.getSelectedItem());

    ChangeColumnFrame addFrame = Factory.create(ChangeColumnFrame.class);
    addFrame.setChangeAction(SKDCGUIConstants.ADD_BTN);
    addFrame.setCurrentData(mddata);
    addSKDCInternalFrameModal(addFrame, new JPanel[] { inputPanel, btnPanel },
                              new ColumnChangeFrameDataHandler());
  }

 /**
  *  Method allows for modifying a meta-data column.
  */
  private void modifyButtonPressed()
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to Modify", "Selection Error");
      return;
    }
    else if (totalSelected > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to Modify at a time", "Selection Error");
      return;
    }

    mddata.dataToSKDCData(sktable.getSelectedRowData());
    ChangeColumnFrame modFrame = Factory.create(ChangeColumnFrame.class);
    modFrame.setChangeAction(SKDCGUIConstants.MODIFY_BTN);
    modFrame.setCurrentData(mddata);
    addSKDCInternalFrameModal(modFrame, new JPanel[] { inputPanel, btnPanel },
                              new ColumnChangeFrameDataHandler());
  }

 /**
  *  Method deletes metadata column definition.
  */
  private void deleteButtonPressed()
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to Delete", "Selection Error");
      return;
    }

    boolean deleteSelected;
    deleteSelected = displayYesNoPrompt("Do you really want to Delete\nall selected rows", "Delete Confirmation");
    if (deleteSelected)
    {
      String[] deletionList = sktable.getSelectedColumnData(AsrsMetaDataData.COLUMNNAME_NAME);
      int[] deleteIndices = sktable.getSelectedRows();
      
      int delCount = 0;
      TransactionToken tt = null;
      for(int row = 0; row < totalSelected; row++)
      {
        try
        {
          mddata.clear();
          mddata.setKey(AsrsMetaDataData.DATAVIEWNAME_NAME, currentSelectedView);
          mddata.setKey(AsrsMetaDataData.COLUMNNAME_NAME, deletionList[row]);
          tt = dbobj.startTransaction();  
          amd.deleteElement(mddata);
          dbobj.commitTransaction(tt);
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
          dbobj.endTransaction(tt);
        }
      }
      if (delCount != totalSelected)
      {
        displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                    " selected rows",
                    "Delete Result");
      }
      else
      {
        displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                               " selected rows", "Delete Result");
      }
      sktable.deleteSelectedRows();    // Update the display.
    }
  }
 
 /**
  *  Method builds the appropriate display for given Metadata view selection.
  *
  *  @param sDataViewName <code>String</code> containing meta-data view name.
  */
  private void processMetaDataSelectEvent(String sDataViewName)
  {
    currentSelectedView = sDataViewName;
    mddata.clear();
    mddata.setKey(AsrsMetaDataData.DATAVIEWNAME_NAME, sDataViewName);
    try
    {
      List alist = amd.getAllElements(mddata);
      if (alist.isEmpty())
      {
        sktable.clearTable();
        displayInfoAutoTimeOut("No data found", "Search Result");
      }
      else
      {
        sktable.refreshData(alist);
        displayInfoAutoTimeOut(alist.size() + " Column" +
            (alist.size() == 1 ? "" : "s") + " found");
      }
    }
    catch(DBException e)
    {
      displayError(e.getMessage());
    }
  }

 /**
  *  The following method is so that we can simulate the initial select event
  *  then the combo-box is first populated.
  */
  private void initAsrsMetadataSelectEvent()
  {
    if (availMetaViewCombo.getItemCount() > 0)
    {
      String metaDataViewName = (String)availMetaViewCombo.getItemAt(0);
      processMetaDataSelectEvent(metaDataViewName);
    }
  }

/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/
  private void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
     /**
      *  Defines popup menu items for <code>SKDCTable</code>, and adds listeners
      *  to them.
      */
      @Override
      public SKDCPopupMenu definePopup()
      {
        popupMenu.add("Add Meta Column", SKDCGUIConstants.ADD_BTN, btnEventListener);
        popupMenu.add("Modify Meta Column", SKDCGUIConstants.MODIFY_BTN, btnEventListener);
        popupMenu.add("Delete Meta Column", SKDCGUIConstants.DELETE_BTN, btnEventListener);
        
        return(popupMenu);
      }
     /**
      *  Display the detail screen.
      */
      @Override
      public void displayDetail()
      {
      }
    });
  }

  private void initMetaViewCombo()
  {
    try
    {
      availMetaViewCombo.setComboBoxData(amd.getAsrsMetaDataChoices(false));
    }
    catch(DBException exc)
    {
      displayError(exc.getMessage());
      return;
    }
  }

  private void defineButtons()
  {
    btnAddView = new SKDCButton(" Add View ", "Add a ASRS Meta-Data View.", 'd');
    btnDeleteView = new SKDCButton(" Delete View ",
                                   "Delete a given ASRS Meta-Data View", 'e');
    btnAdd     = new SKDCButton("Add Meta Column", "Add Meta Data Column.", 'A');
    btnModify  = new SKDCButton("Modify Meta Column", "Modify Selected Column definition.", 'M');
    btnDelete  = new SKDCButton("Delete Meta Column", "Delete Selected Row(s).", 'D');
    setButtonListeners();
  }

  private JPanel buildInputPanel() throws DBException
  {
    JPanel northPanel = getEmptyButtonPanel();

    northPanel.add(new SKDCLabel("Meta Data Views:"));
    northPanel.add(availMetaViewCombo);
    northPanel.add(btnAddView);
    northPanel.add(btnDeleteView);

    return(northPanel);
  }

  private JPanel buildButtonPanel()
  {
    JPanel buttonPanel = getEmptyButtonPanel();

    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(getInfoPanel(), BorderLayout.CENTER);
    vpSouthPanel.add(buttonPanel, BorderLayout.SOUTH);

    buttonPanel.add(btnAdd);           // Disable these buttons initially.
    buttonPanel.add(btnModify);        // Add the buttons to the panel
    buttonPanel.add(btnDelete);
    
    return(vpSouthPanel);
  }
}
