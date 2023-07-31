/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2005 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.transfer;

import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeallocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCode;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//import com.skdaifuku.tool.wrxj.swingui.load.UpdateLoad;

/**
 * Description:<BR>
 *    Primary frame for Consolidation maintenance.
 *
 * @author       R.M.
 * @version      1.0
 * <BR>Created: 16-Feb-05<BR>
 *     Copyright (c) 2005<BR>
 *     Company:  Daifuku America Corporation
 */
public class TransferMain extends SKDCInternalFrame 
{
  private static final long serialVersionUID = 0L;
  

  
  private JPanel            ipanel;
  private JPanel            xferpanel;
  private JPanel            excpanel;
  private JPanel            incpanel;
  private JPanel            inoutpanel;

  private JPanel            lbtnpanel;
  private JPanel            rbtnpanel;
  private JPanel            fromHeaderPanel;
  private JPanel            fromHeaderInputPanel;
  private JPanel            fromHeaderLabelPanel;
  private JPanel            toHeaderPanel;
  private JPanel            toHeaderLabelPanel;
  private JPanel            toHeaderInputPanel;
  private List<Map>              mpLeftList        = new ArrayList<Map>();
  private List<Map>              mpRightList        = new ArrayList<Map>();
  private SKDCButton        btnTransferLeft;
  private SKDCButton        btnTransferRight;
  private SKDCButton        btnReleaseRight; 
  private SKDCButton        btnReleaseLeft;
  private SKDCTextField     txtLeftLoadID = new SKDCTextField(LoadData.LOADID_NAME);
  private StationComboBox   stationLeftComboBox;
  private StationComboBox   stationRightComboBox;

  //  private SKDCLabel         txtLeftWarehouse = new SKDCLabel("   ");
//  private SKDCLabel         txtLeftAddress  = new SKDCLabel("        ");
//  private SKDCTextField     txtRightWarehouse  = new SKDCTextField(DBInfo.getFieldLength("warehouse"));
//  private SKDCTextField     txtRightAddress  = new SKDCTextField(DBInfo.getFieldLength("address"));
//  private SKDCTextField     txtLeftOrderID  = new SKDCTextField(DBInfo.getFieldLength("orderid"));
 
  private SKDCTextField     txtRightLoadID = new SKDCTextField(LoadData.LOADID_NAME);
 // private SKDCTextField     txtRightLocation  = new SKDCTextField(DBInfo.getFieldLength("warehouse")+DBInfo.getFieldLength("address"));
//  private SKDCTextField     txtRightOrderID  = new SKDCTextField(DBInfo.getFieldLength("orderid"));
//  private JTextArea         txtRightLoadMsg  = new JTextArea("   ");
  private SKDCDoubleField   txtQuantity  = new SKDCDoubleField(LoadLineItemData.CURRENTQUANTITY_NAME);
  private SKDCLabel         txtLeftSuperLoad  = new SKDCLabel("       ");
  private SKDCLabel         txtRightSuperLoad  = new SKDCLabel("        ");
  private DacTable         mpRightTable;
  private DacTable         mpLeftTable;
  protected DacModel       mpRightModel = new DacModel(mpRightList, "Transfer");
  protected DacModel       mpLeftModel = new DacModel(mpLeftList, "Transfer");
  protected SKDCTextField mpLoadText = new SKDCTextField(LoadData.LOADID_NAME);
  protected MessageObserver messageObserver = new MessageObserver();

  protected LoadEventDataFormat mpLEDF = null;
  protected String subscribedScheduler = "";
  
  private SKDCPopupMenu     excPopupMenu    = new SKDCPopupMenu();
  private SKDCPopupMenu     incPopupMenu    = new SKDCPopupMenu();
  protected StandardInventoryServer   mpInvServer;
  private StandardLoadServer        mpLoadServer;
  private StandardLocationServer    mpLocationServer;
  protected StandardPickServer        mpPickServer;
  protected StandardDeallocationServer mpDelServer; 
  private LoadLineItemData  mpLLIData       = Factory.create(LoadLineItemData.class);
  private String            selectedLoad = "";
  private LoadData          mpRightLoadData = Factory.create(LoadData.class);
  private LoadData          mpLeftLoadData = Factory.create(LoadData.class);
  private boolean           mzValidLeftLoad = false;
  private boolean           mzValidRightLoad   = false;
  private boolean           mzClosing = false;
  protected LoadData ld = null;
  protected StandardStationServer mpStationServer = null;
  protected StationData stationRightData;
  protected StationData stationLeftData;

  
  /**
   * Constructor
   */
  public TransferMain() throws Exception
  {
    this(false);
  }

  /**
   * Constructor8
   */
  public TransferMain(boolean DEBUGMODE) throws Exception
  {
    super("Transfer");
//    this.DEBUGMODE = DEBUGMODE;
//
//   if (!this.DEBUGMODE)
//    {
//      userData = new SKDCUserData();
//    }
//    else
//    {
//      GlobalServerFactory.set(new com.skdaifuku.fada.dataserver.standard.StandardServerFactory(null));
//    }
    logger.logDebug("ConsolidateMain.createDeviceServer()");
    mpInvServer = Factory.create(StandardInventoryServer .class);
    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpLocationServer = Factory.create(StandardLocationServer.class);
    mpStationServer = Factory.create(StandardStationServer.class);
    mpPickServer = Factory.create(StandardPickServer.class);
    mpDelServer = Factory.create(StandardDeallocationServer.class);

    mpRightTable = new DacTable(mpRightModel);
    mpLeftTable = new DacTable(mpLeftModel);
    setTableIncMouseListener();        // Set up mouse listeners for our tables.
    setTableExcMouseListener();
    defineButtons();

//    stationRightData = mpStationServer.getStation("1122");
//    stationLeftData = mpStationServer.getStation("1111");
                                       // Get content pane of this Internal
                                       // Frame, and add the panels, and table
    Container cp = this.getContentPane();
    cp.add(buildInputPanel(), BorderLayout.NORTH);
    cp.add(buildTableSetPanel(), BorderLayout.CENTER);
    
    mpLEDF = Factory.create(LoadEventDataFormat.class, this.getTitle());
    checkSubscriptions();
    stationFill();
}

  /**
   * Preferred frame size
   */
  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(950, 500));
  }

  /**
   * Clean up
   */
  @Override
  public void cleanUpOnClose()
  {
    mzClosing = true;
    mpInvServer.cleanUp();
    mpInvServer = null;
    mpLocationServer.cleanUp();
    mpLocationServer = null;
    mpLoadServer.cleanUp();
    mpLoadServer = null;
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  /**
   * Populate the Left station list (by load)
   */
  void displayLeftLoad()
  {
    if (mzClosing)
    {
      return;
    }

	
//	 List <Map> loadList;
    
	mpLeftLoadData = null;
	txtLeftLoadID.setText("        ");
	mpLeftList.clear();
 
    if (stationLeftData != null)
    {
      try
      {
         mpLeftLoadData = mpLoadServer.getOldestLoad(stationLeftData.getWarehouse(), stationLeftData.getStationName());    
      }
      catch(DBException exc)
      {
          displayError(exc.getMessage(), " DB Error");
          return;
      }
    }
    if (mpLeftLoadData != null && mpLeftLoadData.getLoadMoveStatus() == DBConstants.ARRIVED)
    {
      txtLeftLoadID.setText(mpLeftLoadData.getParentLoadID());
      mzValidLeftLoad = true;
    }
    else
    {
      txtLeftSuperLoad.setText("        ");
      mpLeftList.clear();
      mpLeftTable.refreshData(mpLeftList);
      btnTransferRight.setEnabled(false);
      btnTransferLeft.setEnabled(false);
      mzValidLeftLoad = false;
      return;
    }
     
    try
    {                                  // Get data from the Inventory server.
      mpLeftList = mpInvServer.getLoadLineItemDataListByLoadID(mpLeftLoadData.getLoadID());		
    }
    catch(DBException exc)
    {
      displayError(exc.getMessage(), "DB Error");
      return;
    }
    if (mpLeftList.size() == 0)
    {
       displayInfoAutoTimeOut("No Right Load Detail found", "Search Results");
    }   
    mpLeftTable.refreshData(mpLeftList);
    enableTransferButtons();
	txtQuantity.setValue(0);  
    txtQuantity.setEnabled(true);   
  }


  /**
   * Populate the from-load list (by load)
   */
  void displayRightLoad()
  {
    if (mzClosing)
    {
      return;
    }

    mpRightLoadData = null;
  	txtRightLoadID.setText("        ");
    mpRightList.clear();

    if (stationRightData != null)
    {
      try
      {
        mpRightLoadData = mpLoadServer.getOldestLoad(stationRightData.getWarehouse(), stationRightData.getStationName());    
      }
      catch(DBException exc)
      {
        displayError(exc.getMessage(), " DB Error");
        return;
      }
    }
    if (mpRightLoadData != null &&
        mpRightLoadData.getLoadMoveStatus() == DBConstants.ARRIVED)
    {
      mzValidRightLoad = true;
      txtRightLoadID.setText(mpRightLoadData.getParentLoadID());
    }
    else
    {
      txtRightSuperLoad.setText("        ");
      mpRightList.clear();
      mpRightTable.refreshData(mpRightList);
      btnTransferRight.setEnabled(false);
	  btnTransferLeft.setEnabled(false);
      mzValidRightLoad = false;
      return;
    }
    
    try
    {                                  // Get data from the Inventory server.
  	  mpRightList = mpInvServer.getLoadLineItemDataListByLoadID(txtRightLoadID.getText());
    }
    catch(DBException exc)
    {
      displayError(exc.getMessage(), "DB Error");
      return;
    }
    
    if (mpRightList.size() == 0)
    {
       displayInfoAutoTimeOut("No Right Load Detail found", "Search Results");
    }
            
    mpRightTable.refreshData(mpRightList);

    enableTransferButtons(); 
  	txtQuantity.setValue(0);       
	  txtQuantity.setEnabled(true);   
  }
 
  /**
   * Move selected items from the from-load to the to-load
   */
  void transferLeft()
  {
    ArrayList vapSelectedData = new ArrayList();
    String vsRightLoad = txtRightLoadID.getText();
    String vsLeftLoadID = txtLeftLoadID.getText();
    
    vapSelectedData = (ArrayList)mpRightTable.getSelectedRowDataArray();
    
    mpLeftLoadData = mpLoadServer.getLoad(vsLeftLoadID);
    if (mpLeftLoadData == null)
    {
      displayError("TO Load cannot be blank");
      return;
    }
    if (vsLeftLoadID.trim().equals(""))
    {
      displayError("TO Load cannot be blank");
      return;
    }
 
    for (int i=0; i<vapSelectedData.size(); i++)
    {
      Map m;
      
      m = (Map)vapSelectedData.get(i);
      mpLLIData.dataToSKDCData(m);            // convert map to load line item detail
 
      vsRightLoad = mpLLIData.getLoadID();
      if (vsRightLoad.equals(vsLeftLoadID))
      {
         displayError("Item is already in this load.  Loads are the same.");
      }
      else
      {
        if (vapSelectedData.size() == 1)
        {
           mpLLIData.setCurrentQuantity(txtQuantity.getValue());
		}
        try
        {
	         mpInvServer.transferLoadLineItem(mpLLIData, vsLeftLoadID,
	             ReasonCode.getItemLoadTransferReasonCode());
        }
        catch (DBException e)
        {
          displayError(e.getMessage(), "Could not update load line item");
          return;
        }
      }
	  
      displayRightLoad();
      displayLeftLoad();
    }
}
 
  /**
   * Move selected items from the from-load to the to-load
   */
  void transferRight()
  {
    ArrayList vapSelectedData = new ArrayList();
    String vsLeftLoad = txtLeftLoadID.getText();
    String vsRightLoadID = txtRightLoadID.getText();
    
    vapSelectedData = (ArrayList)mpLeftTable.getSelectedRowDataArray();
    
    mpRightLoadData = mpLoadServer.getLoad(vsRightLoadID);
    if (mpRightLoadData == null)
    {
      displayError("TO Load cannot be blank");
      return;
    }
    if (vsRightLoadID.trim().equals(""))
    {
      displayError("TO Load cannot be blank");
      return;
    }
 
    for (int i=0; i<vapSelectedData.size(); i++)
    {
      Map m;
      
      m = (Map)vapSelectedData.get(i);
      mpLLIData.dataToSKDCData(m);            // convert map to load line item detail
 
      vsLeftLoad = mpLLIData.getLoadID();
      if (vsLeftLoad.equals(vsRightLoadID))
      {
         displayError("Item is already in this load.  Loads are the same.");
      }
      else
      {
        if (vapSelectedData.size() == 1)
        { 
           mpLLIData.setCurrentQuantity(txtQuantity.getValue());
        }
        try
        {
          mpInvServer.transferLoadLineItem(mpLLIData, vsRightLoadID,
                              ReasonCode.getItemLoadTransferReasonCode());
        }
        catch (DBException e)
        {
          displayError(e.getMessage(), "Could not update load line item");
          return;
        }
      }
      displayLeftLoad();
      displayRightLoad();
    }
}
  /**
   * Enable/disable the transfer buttons depending on screen state
   */
  void enableTransferButtons()
  {
    if (mzValidLeftLoad && mzValidRightLoad)
    {
      btnTransferLeft.setEnabled(true);
      btnTransferRight.setEnabled(true);
    }
    else
    {
      btnTransferLeft.setEnabled(false);
      btnTransferRight.setEnabled(false);
    }
  }
/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/
  /**
   *  Defines popup menu items for <code>SKDCTable</code>, and adds listeners
   *  to them.
   */
  private void setTableExcMouseListener()
  {
    mpLeftTable.addMouseListener(new DacTableMouseListener(mpLeftTable)
    {
      @Override
      public SKDCPopupMenu definePopup()
      {
        excPopupMenu.add("Set as TO Load", ADD_BTN, new PopUpButtonListener(true));
        excPopupMenu.add("Set as FROM Load", MODIFY_BTN, new PopUpButtonListener(true));
        
        return(excPopupMenu);
      }
     
      @Override
      public void displayDetail()
      {
      }
    });
  }

  /**
   *  Defines popup menu items for <code>SKDCTable</code>, and adds listeners
   *  to them.
   */
  private void setTableIncMouseListener()
  {
    mpRightTable.addMouseListener(new DacTableMouseListener(mpRightTable)
    {
      @Override
      public SKDCPopupMenu definePopup()
      {
        incPopupMenu.add("Set as TO Load", ADD_BTN, new PopUpButtonListener(false));
        incPopupMenu.add("Set as FROM Load", MODIFY_BTN, new PopUpButtonListener(false));
        
        return(incPopupMenu);
      }
     
      @Override
      public void displayDetail()
      {
      }
    });
  }

  /**
   * Builds the to-load panel
   * 
   * @return
   * @throws DBException
   */
  private JPanel buildInputPanel()
  {
    ipanel = new JPanel(new GridBagLayout());
                                       // Create Grid bag constraints.
    GridBagConstraints gbconst = new GridBagConstraints();
        
                                       // Put in the Header label relative to
    gbconst.gridx = 0;                 // column 0, and make it occupy both
    gbconst.gridy = 0;                 // columns.
        
                                       // Put 10 pixels between components and
                                       // bottom boundary.
    gbconst.insets = new Insets(20, 1, 10, 1);
        
    gbconst.gridwidth = GridBagConstraints.RELATIVE;
    gbconst.anchor = GridBagConstraints.CENTER;
    gbconst.weightx = 0.9;
    
                                       // Add the Search Device Text Field.
    ipanel.add(buildLeftPanel(), gbconst);
    gbconst.gridx = GridBagConstraints.RELATIVE;
    ipanel.add(buildRightPanel(), gbconst);                                    

    gbconst.anchor = GridBagConstraints.WEST;

                                       // Put 8 pixels between left boundary
                                       // and component, and 10 pixels between
                                       // bottom boundary and component.
    gbconst.insets = new Insets(1, 8, 10, 1);

    return(ipanel);
  }

  /**
   * Builds the from-load panel
   * 
   * @return
   * @throws DBException
   */
  private JPanel buildLeftPanel()
  {
    fromHeaderPanel = new JPanel(new GridBagLayout());
                                       // Create Grid bag constraints.
    GridBagConstraints gbconst = new GridBagConstraints();

                                       // Put in the Header label relative to
    gbconst.gridx = 0;                 // column 0, and make it occupy both
    gbconst.gridy = 0;                 // columns.

                                       // Put 10 pixels between components and
                                       // bottom boundary.
    gbconst.insets = new Insets(1, 1, 10, 1);

    gbconst.gridwidth = GridBagConstraints.RELATIVE;
    gbconst.anchor = GridBagConstraints.EAST;
    gbconst.weightx = 0.9;
                                       // Add the Search Device Text Field.
    fromHeaderPanel.add(buildLeftLabelPanel(), gbconst);
    gbconst.gridx = GridBagConstraints.RELATIVE;
    fromHeaderPanel.add(buildLeftInputPanel(), gbconst);                                    
//    displayLeftLoad();
    return(fromHeaderPanel);
  }

  /**
   * Builds the from-load label panel
   * 
   * @return
   * @throws DBException
   */
  private JPanel buildLeftLabelPanel()
  {
    fromHeaderLabelPanel = new JPanel(new GridBagLayout());
                                       // Create Grid bag constraints.
    GridBagConstraints gbconst = new GridBagConstraints();

                                       // Put in the Header label relative to
    gbconst.gridx = 0;                 // column 0, and make it occupy both
    gbconst.gridy = 0;                 // columns.
    gbconst.gridwidth = GridBagConstraints.REMAINDER;

                                       // Put 10 pixels between components and
                                       // bottom boundary.
    gbconst.insets = new Insets(5, 1, 10, 1);
    gbconst.anchor = GridBagConstraints.EAST;
    gbconst.weighty = 0.8;
                                      // Add the Search Device Text Field.
    fromHeaderLabelPanel.add(new SKDCLabel("Station:"), gbconst);
    gbconst.gridy = GridBagConstraints.RELATIVE;
    gbconst.insets = new Insets(1, 1, 10, 1);
    fromHeaderLabelPanel.add(new SKDCLabel("Load:"), gbconst);
    return(fromHeaderLabelPanel);
  }

  /**
   * Builds the from-load input panel
   * 
   * @return
   * @throws DBException
   */
  private JPanel buildLeftInputPanel()
  {
    fromHeaderInputPanel = new JPanel(new GridBagLayout());
                                       // Create Grid bag constraints.
    GridBagConstraints gbconst = new GridBagConstraints();
                                       // Put in the Header label relative to
    gbconst.gridx = 0;                 // column 0, and make it occupy both
    gbconst.gridy = 0;                 // columns.
    gbconst.gridwidth = GridBagConstraints.REMAINDER;
                                       // Put 10 pixels between components and
                                       // bottom boundary.
    gbconst.insets = new Insets(1, 1, 4, 1);
    gbconst.anchor = GridBagConstraints.WEST;
    gbconst.weighty = 0.8;
                                       // Add the Search Device Text Field.
    gbconst.insets = new Insets(1, 1, 1, 1);
    stationLeftComboBox = new StationComboBox();
    fromHeaderInputPanel.add(stationLeftComboBox, gbconst);
//    fromHeaderInputPanel.add(new SKDCLabel("1122"), gbconst);
    gbconst.gridy = GridBagConstraints.RELATIVE;
    fromHeaderInputPanel.add(txtLeftLoadID, gbconst);
    txtLeftLoadID.setEnabled(false);
    return(fromHeaderInputPanel);
  }

  /**
   * Builds the to-load label panel
   * 
   * @return
   * @throws DBException
   */
  private JPanel buildRightLabelPanel()
  {
  toHeaderLabelPanel = new JPanel(new GridBagLayout());
  // Create Grid bag constraints.
  GridBagConstraints gbconst = new GridBagConstraints();

                                     // Put in the Header label relative to
  gbconst.gridx = 0;                 // column 0, and make it occupy both
  gbconst.gridy = 0;                 // columns.
  gbconst.gridwidth = GridBagConstraints.REMAINDER;

                                     // Put 10 pixels between components and
                                     // bottom boundary.
  gbconst.insets = new Insets(5, 1, 10, 1);
  gbconst.anchor = GridBagConstraints.EAST;
  gbconst.weighty = 0.8;
                                    // Add the Search Device Text Field.
  toHeaderLabelPanel.add(new SKDCLabel("Station:"), gbconst);
  gbconst.gridy = GridBagConstraints.RELATIVE;
  gbconst.insets = new Insets(1, 1, 10, 1);
  toHeaderLabelPanel.add(new SKDCLabel("Load:"), gbconst);
  gbconst.gridy = GridBagConstraints.RELATIVE;
  return(toHeaderLabelPanel);

  }

  /**
   * Builds the to-load input panel
   * 
   * @return
   * @throws DBException
   */
  private JPanel buildRightInputPanel()
  {
  toHeaderInputPanel = new JPanel(new GridBagLayout());
  // Create Grid bag constraints.
  GridBagConstraints gbconst = new GridBagConstraints();
                                     // Put in the Header label relative to
  gbconst.gridx = 0;                 // column 0, and make it occupy both
  gbconst.gridy = 0;                 // columns.
  gbconst.gridwidth = GridBagConstraints.REMAINDER;
                                     // Put 10 pixels between components and
                                     // bottom boundary.
  gbconst.insets = new Insets(1, 1, 4, 1);
  gbconst.anchor = GridBagConstraints.WEST;
  gbconst.weighty = 0.8;
                                     // Add the Search Device Text Field.
  gbconst.insets = new Insets(1, 1, 1, 1);
  stationRightComboBox = new StationComboBox();
  toHeaderInputPanel.add(stationRightComboBox, gbconst);
 //  toHeaderInputPanel.add(new SKDCLabel("1111"), gbconst);
  gbconst.gridy = GridBagConstraints.RELATIVE;
  toHeaderInputPanel.add(txtRightLoadID, gbconst);
  txtRightLoadID.setEnabled(false);
  gbconst.insets = new Insets(1, 1, 8, 1);
//  displayRightLoad();

  return(toHeaderInputPanel);

  }

  /**
   * Builds the tol-load panel
   * 
   * @return
   * @throws DBException
   */
  private JPanel buildRightPanel() 
  {
    toHeaderPanel = new JPanel(new GridBagLayout());
                                       // Create Grid bag constraints.
    GridBagConstraints gbconst = new GridBagConstraints();
    
                                       // Put in the Header label relative to
    gbconst.gridx = 0;                 // column 0, and make it occupy both
    gbconst.gridy = 0;                 // columns.
    
                                       // Put 10 pixels between components and
                                       // bottom boundary.
    gbconst.insets = new Insets(1, 1, 10, 1);
    
    gbconst.gridwidth = GridBagConstraints.RELATIVE;
    gbconst.anchor = GridBagConstraints.EAST;
    gbconst.weightx = 0.8;

                                       // Add the Search Device Text Field.
    toHeaderPanel.add(buildRightLabelPanel(), gbconst);
    gbconst.gridx = GridBagConstraints.RELATIVE;
    toHeaderPanel.add(buildRightInputPanel(), gbconst);                                    
//    displayRightLoad();
    return(toHeaderPanel);
  }

  private JPanel buildTableSetPanel() 
  {
    xferpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    excpanel = new JPanel(new BorderLayout());
    incpanel = new JPanel(new BorderLayout());
    inoutpanel = new JPanel(new GridBagLayout());
    lbtnpanel = new JPanel();
    rbtnpanel = new JPanel();
   
    excpanel.add(mpLeftTable.getScrollPane(), BorderLayout.CENTER);
    mpLeftTable.getScrollPane().setBorder(new BevelBorder(BevelBorder.LOWERED));
    incpanel.add(mpRightTable.getScrollPane(), BorderLayout.CENTER);

    lbtnpanel.add(btnReleaseLeft);
    rbtnpanel.add(btnReleaseRight);
    mpRightTable.getScrollPane().setBorder(new BevelBorder(BevelBorder.LOWERED));
    mpRightTable.setPreferredScrollableViewportSize(new Dimension(400,200));
    mpLeftTable.setPreferredScrollableViewportSize(new Dimension(400,200));

    ListSelectionModel exelsm = mpLeftTable.getSelectionModel();
    exelsm.addListSelectionListener(new SelectItemListener(true));

    ListSelectionModel inclsm = mpRightTable.getSelectionModel();
    inclsm.addListSelectionListener(new SelectItemListener(false));

    xferpanel.add(excpanel);
    xferpanel.add(inoutpanel);
    xferpanel.add(incpanel);
    incpanel.add(lbtnpanel, BorderLayout.SOUTH);    
    excpanel.add(rbtnpanel, BorderLayout.SOUTH);    
                           // Create Grid bag constraints.
    GridBagConstraints gbconst = new GridBagConstraints();
    gbconst.insets = new Insets(1, 1, 15, 1);

                                       // Put in the Header label relative to
    gbconst.gridx = 0;                 // column 0, and make it occupy both
    gbconst.gridy = 0;                 // columns.
    gbconst.gridwidth = GridBagConstraints.REMAINDER;
    gbconst.anchor = GridBagConstraints.CENTER;

                                       // Put 10 pixels between components and
                                       // bottom boundary.
    gbconst.insets = new Insets(1, 1, 10, 1);

    gbconst.gridy = GridBagConstraints.RELATIVE;
    gbconst.weighty = 0.8;
                                       // Add the Search Device Text Field.
    inoutpanel.add(new SKDCLabel("Item Quantity"), gbconst);
    inoutpanel.add(txtQuantity, gbconst);
    txtQuantity.setEnabled(false);
    inoutpanel.add(btnTransferRight, gbconst);
    inoutpanel.add(btnTransferLeft, gbconst);
    btnTransferRight.setEnabled(false);
    btnTransferLeft.setEnabled(false);

    gbconst.anchor = GridBagConstraints.WEST;

                                       // Put 8 pixels between left boundary
                                       // and component, and 10 pixels between
                                       // bottom boundary and component.
    gbconst.insets = new Insets(1, 8, 10, 1);
//    checkSubscriptions();
    return(xferpanel);
  }

  /**
   *  Defines all buttons on the main Location Panels, and adds listeners
   *  to them.
   */
  private void defineButtons()
  {
    btnTransferRight   = new SKDCButton(">", "Transfer to Right Load", 'I');
    btnTransferLeft    = new SKDCButton("<", "Transfer to Left Load", 'E');
    btnReleaseLeft     = new SKDCButton("Release Load", "Release Load", 'A');
    btnReleaseRight    = new SKDCButton("Release Load", "Release Load", 'B');
                                      // Attach listeners.
    btnTransferRight.addEvent(ADD_BTN, new DVButtonListener());
    btnTransferLeft.addEvent(DELETE_BTN, new DVButtonListener());
    btnReleaseRight.addEvent(RESET_BTN, new DVButtonListener());
    btnReleaseLeft.addEvent(RESTART_BTN, new DVButtonListener());

 }

/*===========================================================================
              ****** All Listener classes go here ******
  ===========================================================================*/

  /**
   *  Left Right Button Listener class.
   */
  private class DVButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(ADD_BTN))
      {
        transferRight();
      }
      else if (which_button.equals(DELETE_BTN))
      {
        transferLeft();
      }
      else if (which_button.equals(RESTART_BTN))
      {
        releaseLoad(mpRightLoadData, stationRightData);
      }
      else if (which_button.equals(RESET_BTN))
      {
        releaseLoad(mpLeftLoadData, stationLeftData);
      }
    }
  }

  /**
   * Listener for pop-up menu
   */
  private class PopUpButtonListener implements ActionListener
  {
    private boolean pExcludeSide;
    
    public PopUpButtonListener(boolean iExcludeSide)
    {
      pExcludeSide = iExcludeSide;
    }
    
    
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (pExcludeSide)
      {
        if (mpLeftTable.getSelectedRowCount() == 0)
        {
          displayError("No Row Selected", "Error");
          return;
        }
      }
      else
      {
        if (mpRightTable.getSelectedRowCount() == 0)
        {
          displayError("No Row Selected", "Error");
          return;
        }
      }
 
      if (which_button.equals(ADD_BTN))
      {
        txtRightLoadID.setText(selectedLoad);
      }
      else if (which_button.equals(MODIFY_BTN))
      {
        txtLeftLoadID.setText(selectedLoad);
      }
      else if (which_button.equals(MODIFY_BTN))
      {
        txtLeftLoadID.setText(selectedLoad);
      }
    }
  }

  /**
   * Listener for selections
   */
  private class SelectItemListener implements ListSelectionListener
  {
    private boolean pExcludeSide;
    
    public SelectItemListener(boolean iExcludeSide)
    {
      pExcludeSide = iExcludeSide;
    }
    
    public void valueChanged(ListSelectionEvent e)
    {
      ListSelectionModel vLSMinner = (ListSelectionModel)e.getSource();
        
      if (!vLSMinner.isSelectionEmpty())
      {
        if (pExcludeSide)
        {
          mpRightTable.clearSelection();
        }
        else
        {
          mpLeftTable.clearSelection();
        } 
        
        int vMin = vLSMinner.getMinSelectionIndex();
        int vMax = vLSMinner.getMaxSelectionIndex();
        if (vMax-vMin == 0)
        {
          for (int i=vMin; i<=vMax; i++)
          {
            if (vLSMinner.isSelectedIndex(i))
            {
              txtQuantity.setEnabled(true);
              enableTransferButtons();
              selectedLoad = mpLLIData.getLoadID();   // save load ID for popup use
            }
          }
        }
        else
        {
          txtQuantity.setValue(0.0);
          txtQuantity.setEnabled(false);
          selectedLoad = "";
        }
          
      }           
    }
  }


  /**
   * Method to check message subscriptions. This screen needs to be notified of
   * arrivals to this station and also load releases from the station. When the
   * station we are working at changes we need to check if a different scheduler
   * controls this station. If we change schedulers, we unsubscribe for messages
   * from the old scheduler and subscribe to messages for the new scheduler.
   */
  protected void checkSubscriptions()
  {
    if(stationLeftData != null)
    {
      String stationsScheduler;
      try
      {
        stationsScheduler = mpStationServer.getStationsScheduler(stationLeftData.getStationName());
      }
      catch(DBException exc)
      {
        displayError("Scheduler not found for station " + stationLeftData.getStationName());
        return;
      }

      // if we have changed schedulers then
      // unsubscribe from old scheduler and
      // subscribe to new scheduler
      if(!subscribedScheduler.equals(stationsScheduler))
      {
        getSystemGateway().deleteObserver(MessageEventConsts.LOAD_EVENT_TYPE, messageObserver);
        //
        String selector = getSystemGateway().getLoadEventSelector(stationsScheduler);
        getSystemGateway().addObserver(MessageEventConsts.LOAD_EVENT_TYPE, selector, messageObserver);

        subscribedScheduler = stationsScheduler;
      }
    }
  }
  /**
   * An observer class needed for this screen to receive and process messages.
   * 
   */
  protected class MessageObserver implements Observer
  {
    public MessageObserver()
    {
    }

    /**
     * Method to process the arrivals and releases of loads for this station. If
     * a load leaves the station and it is the load displayed, then we clear the
     * screen. If a load arrives at our station then we will set the screen with
     * the correct information for the load.
     * 
     * @param o
     *          no information available
     * @param arg
     *          no information available
     */
    public void update(Observable o, Object arg)
    {
      ObservableControllerImpl observableData = (ObservableControllerImpl) o;
      String sText = observableData.getStringData();
      try
      {
        mpLEDF.decodeReceivedString(sText);

        if((mpLEDF.getMessageID() == AGCDeviceConstants.GENERALSTORELOAD)
            && (mpLEDF.getLoadID().equals(mpLoadText.getText())))
        {
          stationChange();
        }
        if((mpLEDF.getMessageID() == AGCDeviceConstants.GENERALLOADARRIVALATSTATION)
            && (mpLoadText.getText().trim().length() == 0))
        {
          stationChange();
        }
      }
      catch(Exception e)
      {
        String vsStationName = null;
        if(stationLeftData != null)
        {
          vsStationName = stationLeftData.getStationName();
        }
        logger.logException(e, "update() - sText \"" + sText + "\"\nmpLoadText \"" + mpLoadText.getText()
            + "\"\nStation " + vsStationName);
      }
    }
  }
  /**
   * Method to release the load. Verifies that load can be released. If picks or
   * empty containers requests exist for the load, the operator is given a
   * notification of the condition. The operator can still decide to release the
   * load.
   * 
   * @param load
   *          Load ID to be released.
   */
  protected void releaseLoad(LoadData mpLoad, StationData station)
  {
    List<Map>   mpItemList = new ArrayList<Map>();
    try
    {
         
      if(setAmountFull(mpLoad))
      {
        mpDelServer.deallocateMovesForLoad(mpLoad.getLoadID());
        mpItemList = mpInvServer.getLoadLineItemDataListByLoadID(mpLoad.getLoadID());
        LoadLineItemData vpLLD = new LoadLineItemData();
        for (Map vpMap : mpItemList)
        {
          vpLLD.dataToSKDCData(vpMap);
          mpDelServer.deallocateLoadLineItem(vpLLD);
        }
        
        
        String errMsg = mpPickServer.releaseLoad(mpLoad.getLoadID(), station);
        
        if(errMsg != null)
        {
          displayError(errMsg);
        }
     }
 
    }
    catch(DBException e2)
    {
      displayError("Error releasing Load " + mpLoad.getLoadID() + " - " + e2.getMessage());
      return;
    }
    stationChange();
  }
  /**
   * Method to choose and update the amount full.
   * 
   */
  protected boolean setAmountFull(LoadData mpLoad)
  {
    boolean vbResult = false;
    int amountFull = mpLoad.getAmountFull();
    try
    {
      String selectedAmount = (String)JOptionPane.showInputDialog(this,
          "Choose amount full", "Input", JOptionPane.QUESTION_MESSAGE, null,
          DBTrans.getStringList(LoadData.AMOUNTFULL_NAME),
          DBTrans.getStringValue(LoadData.AMOUNTFULL_NAME, amountFull));
      if (selectedAmount != null)
      {
        amountFull = DBTrans.getIntegerValue(LoadData.AMOUNTFULL_NAME,
            selectedAmount);
        vbResult = true;
      }
      if (amountFull != mpLoad.getAmountFull())
      {
        // set the load amount full
        mpLoadServer.setLoadAmountFull(mpLoad.getLoadID(), amountFull);
      }
    }
    catch(NoSuchFieldException e)
    {
      logger.logException(e);
    }
    catch(DBException e)
    {
      logger.logException(e);
    }
    return vbResult;
  }
  /**
   *  Method to populate the station combo box.
   */
  private void stationFill()
  {
    Map stationsMap = null;
    int[] outputStations = {DBConstants.USHAPE_OUT, DBConstants.PDSTAND,
        DBConstants.REVERSIBLE};
    try
    {
      stationsMap = mpStationServer.getStationsByStationType(outputStations);
    }
    catch (Exception e)
    {
      displayError("Unable to get Stations: " + e.getMessage()); 
    }
    if (stationsMap.size() == 1)
    {
      displayError("No Stations Available for Transfer Function");
      return;
    }
    stationLeftComboBox.setComboBoxData(stationsMap.keySet().toArray());
    stationLeftComboBox.addItemListener(new java.awt.event.ItemListener()
    {
        public void itemStateChanged(ItemEvent e)
        {
          station_left_itemStateChanged(e);
        }
    });
    
    try
    {
      stationsMap = mpStationServer.getStationsByStationType(outputStations);
      stationRightComboBox.setComboBoxData(stationsMap.keySet().toArray());
      stationRightComboBox.setSelectedIndex(1);
      stationRightComboBox.addItemListener(new java.awt.event.ItemListener()
      {
          public void itemStateChanged(ItemEvent e)
          {
            station_right_itemStateChanged(e);
          }
      });
    }
    catch (DBException e)
    {
      displayError("Unable to get Stations: " + e.getMessage());
    }
    stationChange();
  }
  private void station_left_itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      stationChange();
    }
  }
  private void station_right_itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      stationChange();
    }
  }

  /**
   *  Method to handle a station change. When a station change occurs it
   *  rechecks the subscriptions and rechecks loads at station.
   */
  protected void stationChange()
  {
    String vsStationLeft = stationLeftComboBox.getSelectedStation();
    stationLeftData = mpStationServer.getStation(vsStationLeft);
    if (stationLeftData == null)
    {
      displayError("Unable to get station data");
      return;
    }
    String vsStationRight = stationRightComboBox.getSelectedStation();
 
    stationRightData = mpStationServer.getStation(vsStationRight);
    if (stationRightData == null)
    {
      displayError("Unable to get station data");
      return;
    }
    if (stationLeftData.getStationName().equalsIgnoreCase(stationRightData.getStationName()))
    {
      displayError("Station number can not be the same");
      return;
    }
    checkSubscriptions();
  
    displayRightLoad();
    displayLeftLoad();
  }

}
