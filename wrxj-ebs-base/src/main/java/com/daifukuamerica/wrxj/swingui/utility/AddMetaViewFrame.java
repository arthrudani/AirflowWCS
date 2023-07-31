package com.daifukuamerica.wrxj.swingui.utility;


import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.JDBCMetaData;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

/**
 * Description:<BR>
 *    Sets up a frame to add one order line to an existing order.
 *
 * @author       A.D.
 * @version      1.0
 *     Copyright (c) 2004<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class AddMetaViewFrame extends SKDCInternalFrame
{
  private final String       FETCH_COLUMNS_BTN = "FETCHCOLUMNS";
  private final String       MOVE_SELECTION_BTN = "MOVESELECTION";
  private final String       MOVE_ALL_BTN = "MOVEALL";
  private final String       REMOVE_SELECTION_BTN = "REMOVESELECTION";
  private final String       REMOVE_ALL_BTN = "REMOVEALL";
  private final String       MOVE_UP_BTN = "MOVEUP";
  private final String       MOVE_DOWN_BTN = "MOVEDOWN";
  private AsrsMetaDataData   mddata;
  private AsrsMetaData       asrsMeta;
  private DBObject           dbobj;
  private SKDCButton         btnOK;
  private SKDCButton         btnCancel;
  private SKDCButton         btnFetchColumns;
  private SKDCButton         btnMoveSelection;
  private SKDCButton         btnMoveAll;
  private SKDCButton         btnRemoveAll;
  private SKDCButton         btnRemoveSelection;
  private SKDCButton         btnMoveUp;
  private SKDCButton         btnMoveDown;
  private SKDCTextField      txtDataView;
  private SKDCLabel          labelDataView;
  private boolean            addedNewView = false;
  private JList              tablesList;
  private JList              tableColumnsList;
  private JTable             selectionTable;
  private JPanel             northPanel;
  private JPanel             centralPanel;
  private JPanel             buttonPanel;
  private ActionListener     evtListener;
  private DefaultListModel   tableColumnsListModel;
  private URL                moveAllRightURL;
  private URL                removeAllURL;
  private URL                removeSelectionURL;
  private URL                moveSelectionURL;
  private URL                moveUpURL;
  private URL                moveDownURL;
  private String[]           tableHeaderData =  {"Column",
                                                 "Column Description",
                                                 "Translation"};
  public AddMetaViewFrame()
  {
    super("Add Meta-Data View", true, true);
                                       // Get content pane for this internal
                                       // frame, and modify the panel to it.
    defineButtons();
    Container cp = this.getContentPane();
    northPanel = buildNorthPanel();
    centralPanel = buildSelectionPanel();
    buttonPanel = buildButtonPanel();
    cp.add(northPanel, BorderLayout.NORTH);
    cp.add(centralPanel, BorderLayout.CENTER);
    cp.add(buttonPanel, BorderLayout.SOUTH);

    enablePanelComponents(northPanel, true);
    enablePanelComponents(centralPanel, false);
    enablePanelComponents(buttonPanel, false);
    
    dbobj = new DBObjectTL().getDBObject();    
    mddata = Factory.create(AsrsMetaDataData.class);
    asrsMeta = Factory.create(AsrsMetaData.class);
    
//    setResizable(false);
  }

//  @Override
//  public Dimension getPreferredSize()
//  {                                    // Search frame Width and Height.
//    return(new Dimension(750, 480));
//  }

  @Override
  public void internalFrameClosed(javax.swing.event.InternalFrameEvent e)
  {
    super.internalFrameClosed(e);
    if (addedNewView) changed();
    try { Thread.sleep(30); } catch(InterruptedException ie) {}
  }

/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/
  private JPanel buildNorthPanel()
  {
    JPanel vpViewNamePanel = new JPanel();
    labelDataView = new SKDCLabel("View Name: ");
    txtDataView = new SKDCTextField(AsrsMetaDataData.DATAVIEWNAME_NAME);
    vpViewNamePanel.add(labelDataView);
    vpViewNamePanel.add(txtDataView);
    
    JPanel vpFetchPanel = new JPanel();
    vpFetchPanel.add(btnFetchColumns);
    
    JPanel newPanel = new JPanel(new GridLayout(2,1,10,10));
    newPanel.add(vpViewNamePanel);
    newPanel.add(vpFetchPanel);
    
    Object[] availableTables = JDBCMetaData.getTableNames().keySet().toArray();
    tablesList = new JList(availableTables);
    tablesList.setVisibleRowCount(5);
    
    JPanel theNorthPanel = new JPanel(new FlowLayout());
    theNorthPanel.setBorder(setBorderAttributes("Initial View Info."));
    theNorthPanel.add(new JScrollPane(tablesList));
    theNorthPanel.add(Box.createHorizontalStrut(25));
    theNorthPanel.add(newPanel);
    
    return(theNorthPanel);
  }

  private JPanel buildSelectionPanel()
  {
    JPanel theCentralPanel = new JPanel();
    theCentralPanel.setBorder(setBorderAttributes("Column Selection"));

    tableColumnsListModel = new DefaultListModel();
    tableColumnsList = new JList(tableColumnsListModel);
    tableColumnsList.setVisibleRowCount(15);

                                       // Set up table of selected columns.
    selectionTable = new JTable(new DefaultTableModel()
    {
      @Override
      public boolean isCellEditable(int row, int column)
      {
        return(column == 1);
      }
    });
    selectionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    selectionTable.getTableHeader().setReorderingAllowed(false);

    ((DefaultTableModel)selectionTable.getModel()).setColumnIdentifiers(tableHeaderData);

    JScrollPane vpScroll1 = new JScrollPane(tableColumnsList);
    JScrollPane vpScroll2 = new JScrollPane(selectionTable);
    vpScroll1.setPreferredSize(new Dimension(150,300));
    vpScroll2.setPreferredSize(new Dimension(400,300));
    
    theCentralPanel.add(vpScroll1);
    theCentralPanel.add(moveButtonsPanel());
    theCentralPanel.add(vpScroll2);
    theCentralPanel.add(reorderPanel());

    return(theCentralPanel);
  }

  private JPanel buildButtonPanel()
  {
    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout());
    btnPanel.setBorder(setBorderAttributes(""));

    btnPanel.add(btnOK);
    btnPanel.add(btnCancel);
    
    return(btnPanel);
  }

  private JPanel moveButtonsPanel()
  {
    JPanel moveButtonPanel = new JPanel();
    moveButtonPanel.setLayout(new BoxLayout(moveButtonPanel, BoxLayout.Y_AXIS));

    moveButtonPanel.add(btnMoveAll);
    moveButtonPanel.add(Box.createVerticalStrut(10));
    moveButtonPanel.add(btnRemoveAll);
    moveButtonPanel.add(Box.createVerticalStrut(10));
    moveButtonPanel.add(btnMoveSelection);
    moveButtonPanel.add(Box.createVerticalStrut(10));
    moveButtonPanel.add(btnRemoveSelection);
    
    return(moveButtonPanel);
  }

  private JPanel reorderPanel()
  {
    JPanel reorderingPanel = new JPanel();
    reorderingPanel.setLayout(new BoxLayout(reorderingPanel, BoxLayout.Y_AXIS));

    reorderingPanel.add(btnMoveUp);
    reorderingPanel.add(Box.createVerticalStrut(20));
    reorderingPanel.add(btnMoveDown);
    
    return(reorderingPanel);
  }

  private void defineButtons()
  {
    btnOK = new SKDCButton("    OK   ", "Submit request.", 'O');
    btnCancel = new SKDCButton("    Cancel   ", "Cancel Add.", 'C');
    btnFetchColumns = new SKDCButton("Fetch Columns", "Fetch columns for selected tables", 'F');

    moveSelectionURL= SKDCButton.class.getResource("/graphics/MoveSelectionRight.png");
    btnMoveSelection = new SKDCButton(new ImageIcon(moveSelectionURL), "Move only selected items");

    removeSelectionURL= SKDCButton.class.getResource("/graphics/MoveSelectionLeft.png");
    btnRemoveSelection = new SKDCButton(new ImageIcon(removeSelectionURL), "Remove selected columns");

    moveAllRightURL= SKDCButton.class.getResource("/graphics/MoveAllRight.png");
    btnMoveAll = new SKDCButton(new ImageIcon(moveAllRightURL), "Move all columns");

    removeAllURL= SKDCButton.class.getResource("/graphics/MoveAllLeft.png");
    btnRemoveAll = new SKDCButton(new ImageIcon(removeAllURL), "Remove all columns");

    moveUpURL = SKDCButton.class.getResource("/graphics/MoveUp.png");
    btnMoveUp = new SKDCButton(new ImageIcon(moveUpURL), "Move selection up");

    moveDownURL = SKDCButton.class.getResource("/graphics/MoveDown.png");
    btnMoveDown = new SKDCButton(new ImageIcon(moveDownURL), "Move selection Down");

    setButtonListeners();
  }

  private Border setBorderAttributes(String isTitle)
  {
    Border theBorder = BorderFactory.createEtchedBorder();
    Border borderType;

    if (isTitle.trim().length() == 0)
    {
      borderType = theBorder;
    }
    else
    {
      borderType = BorderFactory.createTitledBorder(theBorder, isTitle);
    }

    return(borderType);
  }
  
  /*===========================================================================
    Methods for event handling go in this section.
  ===========================================================================*/
  /**
   *  Submit request to add a view.
   */
   @Override
   protected void okButtonPressed()
   {
     DefaultTableModel tableModel = (DefaultTableModel)selectionTable.getModel();
     Vector tblVect = tableModel.getDataVector();
     Enumeration vpEnumer = tblVect.elements();
     String sViewName = txtDataView.getText();
     if(!exists(sViewName.trim()))
     {
       int rowCount = 0;
       TransactionToken tt = null;
       try
       {
         tt = dbobj.startTransaction();
         while(vpEnumer.hasMoreElements())
         {
           mddata.clear();
           mddata.setDataViewName(sViewName);
           Vector rowVect = (Vector)vpEnumer.nextElement();
         
           mddata.setColumnName(((String)rowVect.get(0)).toUpperCase());
           mddata.setFullName((String)rowVect.get(1));
           mddata.setIsTranslation(((String)rowVect.get(2)).substring(0, 1));
           mddata.setDisplayOrder(rowCount++);
           asrsMeta.addElement(mddata);
         }
                                        // Pick up any columns that are not to
                                        // be displayed also.
         for(int listIdx = 0; listIdx < tableColumnsListModel.getSize(); listIdx++)
         {
           String listEntry = (String)tableColumnsListModel.get(listIdx);
           mddata.clear();
           mddata.setDataViewName(sViewName);
           mddata.setColumnName(listEntry.toUpperCase());
           mddata.setFullName(listEntry.substring(1));
           mddata.setIsTranslation(DBTrans.isTranslation(listEntry) ? "Y" : "N");
           mddata.setDisplayOrder(-1);
           asrsMeta.addElement(mddata);
         }
         dbobj.commitTransaction(tt);
         displayInfoAutoTimeOut("View " + sViewName + " added successfully",
                              "View Change");
         addedNewView = true;
       }
       catch(DBException exc)
       {
         JOptionPane.showMessageDialog(this, "Database Add failed..." + exc.getMessage(),
                                     "Add Error", JOptionPane.ERROR_MESSAGE);
       }
       finally
       {
         dbobj.endTransaction(tt);
       }
     }
     else
     {
       JOptionPane.showMessageDialog(this, "View already Exists",
           "Add Error", JOptionPane.ERROR_MESSAGE);
     }

     cancelButtonPressed();
   }

 /**
  *  Cancel add operation and return.
  */
  private void cancelButtonPressed()
  {
    tableColumnsListModel.clear();
    DefaultTableModel tableModel = (DefaultTableModel)selectionTable.getModel();
    Vector tv = tableModel.getDataVector();
    tv.clear();
    tableModel.fireTableRowsDeleted(0, tableModel.getRowCount());
    enablePanelComponents(northPanel, true);
    enablePanelComponents(centralPanel, false);
    enablePanelComponents(buttonPanel, false);
    txtDataView.setText("");
    txtDataView.requestFocus();
  }

 /**
  *  Fetches all columns for a set of selected tables.
  */
  private void fetchColumnsButtonPressed()
  {
                                       // Make sure View name is entered.
    String sViewName = txtDataView.getText();
    if (sViewName.trim().length() == 0)
    {
      JOptionPane.showMessageDialog(null, "View Name is required!",
                                    "Data Error", JOptionPane.ERROR_MESSAGE);
      txtDataView.requestFocus();
      return;
    }

    Object[] selectedTableNames = tablesList.getSelectedValues();
    if (selectedTableNames.length > 0)
    {
      Map columnNames = new TreeMap();
      for(int tidx = 0; tidx < selectedTableNames.length; tidx++)
      {
        Map tempColumnNames = JDBCMetaData.getColumnNames((String)selectedTableNames[tidx]);
        Iterator itr = tempColumnNames.keySet().iterator();
        while(itr.hasNext())
        {
          Object vpKeyName = itr.next();
          if (!columnNames.containsKey(vpKeyName))
          {
            columnNames.put(vpKeyName, vpKeyName);
          }
        }
      }
                                       // Now we have a sorted list of columns!
                                       // Add it to the list.
      Iterator sortedIter = columnNames.keySet().iterator();
      while(sortedIter.hasNext())
        tableColumnsListModel.addElement(sortedIter.next());
        
      enablePanelComponents(northPanel, false);
      enablePanelComponents(centralPanel, true);
      enablePanelComponents(buttonPanel, true);
    }
    else
    {
      JOptionPane.showMessageDialog(null, "No table(s) selected!",
                                    "Selection Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void moveColumnSelectionPressed()
  {
    Object[] selectedColumnNames = tableColumnsList.getSelectedValues();
    DefaultTableModel tableModel = (DefaultTableModel)selectionTable.getModel();
    
    String isTranslation;
    for(int idx = 0; idx < selectedColumnNames.length; idx++)
    {
      String listEntry = (String)selectedColumnNames[idx];    
      isTranslation = (DBTrans.isTranslation(listEntry)) ? "Yes" : "No";
      String vsTempDesc = guessDescriptionFromColumnName(listEntry);
      
      Object[] metaDataRow = new Object[] { listEntry, vsTempDesc, isTranslation };
      tableModel.addRow(metaDataRow);
      tableColumnsListModel.removeElement(selectedColumnNames[idx]);
    }
  }

  private void moveAllPressed()
  {
    Object[] allColumns = tableColumnsListModel.toArray();
    DefaultTableModel tableModel = (DefaultTableModel)selectionTable.getModel();
    
    String isTranslation;    
    for(int idx = 0; idx < allColumns.length; idx++)
    {
      isTranslation = DBTrans.isTranslation((String)allColumns[idx]) ? "Yes" : "No";
      Object[] metaDataRow = new Object[] { allColumns[idx],
                                            guessDescriptionFromColumnName((String)allColumns[idx]),
                                            isTranslation
                                          };
      tableModel.addRow(metaDataRow);
    }
    tableColumnsListModel.clear();
  }

  /**
   * Guess a column description based upon the name
   * @param isColumName
   * @return
   */
  private String guessDescriptionFromColumnName(String isColumName)
  {
    String vsTempDesc = isColumName.charAt(1) + isColumName.substring(2).toLowerCase();
    if (vsTempDesc.endsWith("id"))
    {
      vsTempDesc = vsTempDesc.substring(0,vsTempDesc.length()-2) + " ID";
    }
    else if (vsTempDesc.endsWith("date"))
    {
      vsTempDesc = vsTempDesc.substring(0,vsTempDesc.length()-4) + " Date";
    }
    else if (vsTempDesc.endsWith("status"))
    {
      vsTempDesc = vsTempDesc.substring(0,vsTempDesc.length()-6) + " Status";
    }
    else if (vsTempDesc.endsWith("number"))
    {
      vsTempDesc = vsTempDesc.substring(0,vsTempDesc.length()-6) + " Number";
    }
    return vsTempDesc;
  }
  
  private void removeSelectionPressed()
  {
    int[] selectedRows = selectionTable.getSelectedRows();
    DefaultTableModel tableModel = (DefaultTableModel)selectionTable.getModel();
    for(int k = 0; k < selectedRows.length; k++)
    {
      tableColumnsListModel.addElement(selectionTable.getValueAt(selectedRows[k], 0));
      tableModel.removeRow(selectedRows[k]);
      for(int j = k + 1; j < selectedRows.length; j++) selectedRows[j] -= 1;
    }
  }
  
  private void removeAllPressed()
  {
    DefaultTableModel tableModel = (DefaultTableModel)selectionTable.getModel();
    Vector tblVect = tableModel.getDataVector();
    Enumeration vpEnumer = tblVect.elements();
    while(vpEnumer.hasMoreElements())
    {
      Vector rowVect = (Vector)vpEnumer.nextElement();
      tableColumnsListModel.addElement(rowVect.firstElement());
    }
    tblVect.clear();
    tableModel.fireTableRowsDeleted(0, tableModel.getRowCount());
  }
  
  private void moveSelectionUp()
  {
    DefaultTableModel tableModel = (DefaultTableModel)selectionTable.getModel();
    int[] selectedRows = selectionTable.getSelectedRows();
    int numberSelected = selectedRows.length;

    if (numberSelected == 0)
    {
      return;
    }
    else if (numberSelected > 1)
    {
      JOptionPane.showMessageDialog(null, "Only one row can be reordered at a time!",
                                    "Selection Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (selectedRows[0]-1 >= 0)
    {
      tableModel.moveRow(selectedRows[0], selectedRows[0], selectedRows[0]-1);
      DefaultListSelectionModel defSelection = (DefaultListSelectionModel)selectionTable.getSelectionModel();
      defSelection.removeSelectionInterval(selectedRows[0], selectedRows[0]);
      defSelection.addSelectionInterval(selectedRows[0]-1, selectedRows[0]-1);
    }
  }
  
  /**
   * Method returns true if view already exists
   * @param <code>String</code> The new view name
   * @return <code>boolean</code>
   *
   */
  private boolean exists(String isViewName)
  {
    boolean vzExists = false;
    String[] vasTemp = null;
    AsrsMetaData vpMetaData = Factory.create(AsrsMetaData.class);
    try
    {
      vasTemp= vpMetaData.getAsrsMetaDataChoices(false);
    }
    catch(DBException exc)
    {
      displayError("Database error " + exc.getMessage());
      logger.logException("AddMetaViewFrame error getting views", exc);
      return true;
    }
    for(String vsView: vasTemp)
    {
      if(vsView.equals(isViewName))
      {
        vzExists = true;
        break;
      }
    }
    return vzExists;
  }  

  private void moveSelectionDown()
  {
    DefaultTableModel tableModel = (DefaultTableModel)selectionTable.getModel();
    int[] selectedRows = selectionTable.getSelectedRows();
    int numberSelected = selectedRows.length;

    if (numberSelected == 0)
    {
      return;
    }
    else if (numberSelected > 1)
    {
      JOptionPane.showMessageDialog(null, "Only one row can be reordered at a time!",
                                    "Selection Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (selectedRows[0]+1 < selectionTable.getRowCount())
    {
      tableModel.moveRow(selectedRows[0], selectedRows[0], selectedRows[0]+1);
      DefaultListSelectionModel defSelection = (DefaultListSelectionModel)selectionTable.getSelectionModel();
      defSelection.removeSelectionInterval(selectedRows[0], selectedRows[0]);
      defSelection.addSelectionInterval(selectedRows[0]+1, selectedRows[0]+1);
    }
  }

/*===========================================================================
              ****** All Listener methods go here ******
  ===========================================================================*/
  /**
   *  Defines all buttons on the add view dialog screen.
   */
  private void setButtonListeners()
  {
    evtListener = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String which_button = e.getActionCommand();
        if (which_button.equals(SKDCGUIConstants.OK_BTN))
        {
          okButtonPressed();
        }
        else if (which_button.equals(SKDCGUIConstants.CANCEL_BTN))
        {
          cancelButtonPressed();
        }
        else if (which_button.equals(FETCH_COLUMNS_BTN))
        {
          fetchColumnsButtonPressed();
        }
        else if (which_button.equals(MOVE_SELECTION_BTN))
        {
          moveColumnSelectionPressed();
        }
        else if (which_button.equals(MOVE_ALL_BTN))
        {
          moveAllPressed();
        }
        else if (which_button.equals(REMOVE_SELECTION_BTN))
        {
          removeSelectionPressed();
        }
        else if (which_button.equals(REMOVE_ALL_BTN))
        {
          removeAllPressed();
        }
        else if (which_button.equals(MOVE_UP_BTN))
        {
          moveSelectionUp();
        }
        else if (which_button.equals(MOVE_DOWN_BTN))
        {
          moveSelectionDown();
        }
      }
    };
                                       // Attach listeners.
    btnOK.addEvent(SKDCGUIConstants.OK_BTN, evtListener);
    btnCancel.addEvent(SKDCGUIConstants.CANCEL_BTN, evtListener);
    btnFetchColumns.addEvent(FETCH_COLUMNS_BTN, evtListener);
    btnMoveSelection.addEvent(MOVE_SELECTION_BTN, evtListener);
    btnMoveAll.addEvent(MOVE_ALL_BTN, evtListener);
    btnRemoveAll.addEvent(REMOVE_ALL_BTN, evtListener);
    btnRemoveSelection.addEvent(REMOVE_SELECTION_BTN, evtListener);
    btnMoveUp.addEvent(MOVE_UP_BTN, evtListener);
    btnMoveDown.addEvent(MOVE_DOWN_BTN, evtListener);
  }
}
