package com.daifukuamerica.wrxj.swingui.pick;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.observer.DacObserver;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeallocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMaintenanceOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.swingui.store.StoreItemsMain;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class that handles End of Aisle (EOA) pick functionality. It picks
 * items from loads, picks complete loads, and releases loads back to storage.
 * An operator can skip picks within a load if necessary bby entering 0 for the
 * pick quantity and answering "NO" to the question if there is any remaining
 * picks; this is only applicable to multiple picks from a pick-to-load.
 * 
 * If the AllowUnderPicking parameter is set to true in the wrxj.properties file,
 * the operator can release the load without completing the full pick(s).  In
 * this case they will be given the opportunity to deallocate the order for 
 * whatever was not picked.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EOAPick extends SKDCInternalFrame
{
  protected static String CYCLE_COUNT = "Count Quantity:";
  private static String CYCLE_COUNT_2 = "Re-enter Count:";
  protected static String ITEM_PICK = "Pick Quantity:";
  
  protected static String PICK_CONFIRM_LABEL = "Confirm Pick";
  private static String CCI_CONFIRM_LABEL = "Confirm Count";
  
  protected LoadData mpLoadData = null;
  protected StationData mpStationData = null;
  protected MoveData mpMoveData = null;
  protected OrderHeaderData mpOrderData = null;
  private List<JComponent> mpCompList       = new ArrayList<JComponent>();
  private List<SKDCTextField> mpConfirmList = new ArrayList<SKDCTextField>();
  
  private MessageObserver messageObserver = new MessageObserver();
  private String subscribedScheduler = "";

  protected CycleCountQuantity cciEnteredQty = new CycleCountQuantity();

  protected boolean mzDisplayOrderInfo = false;
  
  protected boolean mzZeroLeft = false;
  protected boolean mzReallocate = false;
  protected boolean mzAllowUnderPick = Application.getBoolean("AllowUnderPicking", 
                                                            false);
  
  protected LoadEventDataFormat mpLEDF = null;

  protected int[] outputStations = {DBConstants.USHAPE_OUT, DBConstants.PDSTAND,
                          DBConstants.REVERSIBLE, DBConstants.OUTPUT};

  protected JPanel mpStationPanel;
  protected JPanel mpPickPanel;
  protected JPanel mpButtonPanel;

  protected StationComboBox mpStationCombo;
  
  protected SKDCButton mpStoreButton;
  protected SKDCButton mpPickButton;
  protected SKDCButton mpReleaseButton;
  protected SKDCButton mpReprintButton;
  protected SKDCButton mpAddCCIButton;
  
  protected static int MAX_DISPLAY_COLUMNS = 25;  
  
  protected SKDCLabel mpConfirmHeader = new SKDCLabel("Pick Confirmation");
  protected SKDCLabel mpLILabelFromLoad;
  
  protected SKDCLabel mpPILabelLoc;
  protected SKDCLabel mpPILabelItem;
  protected SKDCLabel mpPILabelDesc;
  protected SKDCLabel mpPILabelLot;
  protected SKDCLabel mpPILabelQty;
  protected SKDCLabel mpPILabelPosition;
  
  
  protected SKDCTextField mpOITxtOrderID;
  protected SKDCTextField mpOITxtType;
  protected SKDCTextField mpOITxtOHDesc;
  protected SKDCTextField mpOITxtOLDesc;
  
  protected SKDCTextField mpLITxtFromLoad;
  protected SKDCTextField mpLITxtLocation;
  protected SKDCTextField mpPITxtPick;
  protected SKDCTextField mpPITxtIMDesc;
  protected SKDCTextField mpPITxtLot;
  protected SKDCDoubleField mpPITxtQty;
  protected SKDCTextField mpPITxtPosition;
  
  protected SKDCTextField mpPCTxtConfirmLoc;
  protected SKDCTextField mpPCTxtConfirmLoad;
  protected SKDCTextField mpPCTxtConfirmItem;
  protected SKDCTextField mpPCTxtConfirmLot;
  protected SKDCTextField mpPCTxtConfirmPosition;
  protected SKDCDoubleField mpPCTxtConfirmQty;
  protected SKDCTextField mpPCTxtConfirmToLoad;
  
  protected Color mpConfirmForeground;
  protected Color mpConfirmBackground;
  protected Color mpConfirmSelectedText;
  protected Color mpConfirmSelection;

  protected SKDCLabel mpPCLabelToLoad;

  protected Component mpSpacer;
  
  protected StandardInventoryServer mpInvServer = Factory.create(StandardInventoryServer.class);
  protected StandardOrderServer mpMntOrderServ = Factory.create(StandardMaintenanceOrderServer.class);
  protected StandardMoveServer mpMoveServer = Factory.create(StandardMoveServer.class);
  protected StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
  protected StandardOrderServer mpOrdServer = Factory.create(StandardOrderServer.class);
  protected StandardPickServer mpPickServer = Factory.create(StandardPickServer.class);
  protected StandardMaintenanceOrderServer mpMaintServ = Factory.create(StandardMaintenanceOrderServer.class);
  protected StandardStationServer mpStationServer = Factory.create(StandardStationServer.class);
  protected StandardDeallocationServer mpDeallocServ = Factory.create(StandardDeallocationServer.class);

  /**
   * Create EOA Pick screen class.
   */
  public EOAPick()
  {
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Create EOA Pick screen class.
   * @param isInitialStation
   */
  public EOAPick(String isInitialStation)
  {
    this();
    mpStationCombo.setSelectedStation(isInitialStation);
  }

  /**
   * Refresh data upon activation in case another screen caused a change
   */
  @Override
  public void internalFrameActivated(InternalFrameEvent e)
  {
    super.internalFrameActivated(e);
    
    stationChange();
  }

  /**
   * Resize stuff so it matches
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent e)
  {
    super.internalFrameOpened(e);
    stationChange();
    
    mpConfirmHeader.setPreferredSize(mpPCTxtConfirmItem.getPreferredSize());
    mpConfirmHeader.setMinimumSize(mpPCTxtConfirmItem.getPreferredSize());
    mpConfirmHeader.setSize(mpPCTxtConfirmItem.getPreferredSize());
    mpSpacer.setPreferredSize(mpPCTxtConfirmLoad.getPreferredSize());
    mpSpacer.setMinimumSize(mpPCTxtConfirmLoad.getPreferredSize());
    mpSpacer.setSize(mpPCTxtConfirmLoad.getPreferredSize());
    pack();
  }

  /**
   *  Method to initialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  protected void jbInit() throws Exception
  {
    setTitle("Pick");

    mpStoreButton = new SKDCButton("Store Screen", "Bring up the screen for storing", 'S');
    mpPickButton = new SKDCButton(PICK_CONFIRM_LABEL, "Perform the pick", 'C');
    mpReleaseButton = new SKDCButton("Release Load", "Release this load to storage", 'R');
    mpReprintButton = new SKDCButton("Reprint", "Reprint last pick label");
    mpAddCCIButton = new SKDCButton("Add Item", "Add Cycle Count Item");
    
    /*
     * Build the station panel
     */
    mpStationCombo = new StationComboBox(SysConfig.OPTYPE_PICK);
    
    mpStationPanel = new JPanel();
    mpStationPanel.setBorder(BorderFactory.createEtchedBorder());
    mpStationPanel.add(new SKDCLabel("Station:"));
    mpStationPanel.add(mpStationCombo);
    
    /*
     * Build the pick panel.  The pick panel consists of 4 panels:
     *  Pick Information    Pick Confirmation
     *  Order Information   Load Information
     */
    buildPickPanel();
    
    /*
     * Add the panels
     */
    getContentPane().add(mpStationPanel, BorderLayout.NORTH);
    getContentPane().add(mpPickPanel, BorderLayout.CENTER);
    getContentPane().add(buildButtonPanel(),  BorderLayout.SOUTH);
    
    /*
     * Additional initialization
     */
    mpLEDF = Factory.create(LoadEventDataFormat.class, getTitle());

    setData();
    stationFill();
    
    mpConfirmBackground = mpPCTxtConfirmLoad.getBackground();
    mpConfirmForeground = mpPCTxtConfirmLoad.getForeground();
    mpConfirmSelectedText = mpPCTxtConfirmLoad.getSelectedTextColor();
    mpConfirmSelection = mpPCTxtConfirmLoad.getSelectionColor();
  }

  /**
   * Build the Pick Information Panel
   * @return
   */
  protected void buildPickPanel() throws Exception
  {
    int vnLocationLength = DBInfo.getFieldLength(LoadData.WAREHOUSE_NAME)
        + DBInfo.getFieldLength(LoadData.ADDRESS_NAME) + 1;
    mpPickPanel = new JPanel(new GridBagLayout());
    mpPickPanel.setBorder(BorderFactory.createEtchedBorder());
    
    /*
     * Initialize fields
     */
    mpPILabelLoc = new SKDCLabel("Location:");
    mpLILabelFromLoad = new SKDCLabel("Pick From Load:");
    mpPILabelDesc = new SKDCLabel("Item Description:");
    mpPILabelItem = new SKDCLabel("Item:");
    mpPILabelLot = new SKDCLabel("Lot:");
    mpPILabelPosition = new SKDCLabel("Sub Location:");
    mpPILabelQty = new SKDCLabel(ITEM_PICK);
    mpPCLabelToLoad = new SKDCLabel("To Load:");

    mpOITxtOrderID = new SKDCTextField(MAX_DISPLAY_COLUMNS);
    mpOITxtType = new SKDCTextField(MAX_DISPLAY_COLUMNS);
    mpOITxtOHDesc = new SKDCTextField(MAX_DISPLAY_COLUMNS);
    mpOITxtOLDesc = new SKDCTextField(MAX_DISPLAY_COLUMNS);
    mpLITxtLocation = new SKDCTextField(vnLocationLength);
    mpLITxtFromLoad = new SKDCTextField(LoadData.LOADID_NAME);
    mpPITxtPick = new SKDCTextField(MAX_DISPLAY_COLUMNS);
    mpPITxtIMDesc = new SKDCTextField(MAX_DISPLAY_COLUMNS);
    mpPITxtLot = new SKDCTextField(MAX_DISPLAY_COLUMNS);
    mpPITxtPosition = new SKDCTextField(LoadLineItemData.POSITIONID_NAME);
    mpPITxtQty = new SKDCDoubleField(10);
    mpSpacer = Box.createVerticalStrut(mpOITxtOLDesc.getPreferredSize().height);

    mpOITxtOrderID.setMaxColumns(DBInfo.getFieldLength(OrderHeaderData.ORDERID_NAME));
    mpOITxtOHDesc.setMaxColumns(DBInfo.getFieldLength(OrderHeaderData.DESCRIPTION_NAME));
    mpOITxtOLDesc.setMaxColumns(DBInfo.getFieldLength(OrderLineData.DESCRIPTION_NAME));
    mpPITxtIMDesc.setMaxColumns(DBInfo.getFieldLength(ItemMasterData.DESCRIPTION_NAME));
    mpPITxtPick.setMaxColumns(DBInfo.getFieldLength(LoadLineItemData.ITEM_NAME));
    mpPITxtLot.setMaxColumns(DBInfo.getFieldLength(LoadLineItemData.LOT_NAME));
    
    mpOITxtOrderID.setEnabled(false);
    mpOITxtType.setEnabled(false);
    mpOITxtOHDesc.setEnabled(false);
    mpOITxtOLDesc.setEnabled(false);
    mpLITxtLocation.setEnabled(false);
    mpLITxtFromLoad.setEnabled(false);
    mpPITxtPick.setEnabled(false);
    mpPITxtIMDesc.setEnabled(false);
    mpPITxtLot.setEnabled(false);
    mpPITxtPosition.setEnabled(false);
    mpPITxtQty.setEnabled(false);
    
    mpPCTxtConfirmLoc = new SKDCTextField(vnLocationLength);
    mpPCTxtConfirmLoad = new SKDCTextField(LoadData.LOADID_NAME);
    mpPCTxtConfirmItem = new SKDCTextField(MAX_DISPLAY_COLUMNS);
    mpPCTxtConfirmLot = new SKDCTextField(MAX_DISPLAY_COLUMNS);
    mpPCTxtConfirmPosition = new SKDCTextField(LoadLineItemData.POSITIONID_NAME);
    mpPCTxtConfirmQty = new SKDCDoubleField(10);
    mpPCTxtConfirmToLoad = new SKDCTextField(LoadData.LOADID_NAME);

    mpPCTxtConfirmItem.setMaxColumns(DBInfo.getFieldLength(LoadLineItemData.ITEM_NAME));
    mpPCTxtConfirmLot.setMaxColumns(DBInfo.getFieldLength(LoadLineItemData.LOT_NAME));

    /*
     * Add the fields in three columns: Label, Information, and Confirmation
     */
    GridBagConstraints vpGBC = new GridBagConstraints();
    setLabelColumnGridBagConstraints(vpGBC);

    mpPickPanel.add(new SKDCLabel(" "), vpGBC);
    mpPickPanel.add(new SKDCLabel("Order Number:"), vpGBC);
    if (mzDisplayOrderInfo)
    {
      mpPickPanel.add(new SKDCLabel("Order Type:"), vpGBC);
      mpPickPanel.add(new SKDCLabel("Order Description:"), vpGBC);
      mpPickPanel.add(new SKDCLabel("Line Item Description:"), vpGBC);
    }
    mpPickPanel.add(mpPILabelLoc, vpGBC);
    mpPickPanel.add(mpLILabelFromLoad, vpGBC);
    mpPickPanel.add(mpPILabelDesc, vpGBC);
    mpPickPanel.add(mpPILabelItem, vpGBC);
    mpPickPanel.add(mpPILabelLot, vpGBC);
    mpPickPanel.add(mpPILabelPosition, vpGBC);
    mpPickPanel.add(mpPILabelQty, vpGBC);
    mpPickPanel.add(mpSpacer, vpGBC);
    
    // Information Column
    setInputColumnGridBagConstraints(vpGBC);
    vpGBC.insets = getInnerLabelColumnInsets();
    mpPickPanel.add(new SKDCLabel("Pick Information"), vpGBC);
    mpPickPanel.add(mpOITxtOrderID, vpGBC);
    if (mzDisplayOrderInfo)
    {
      mpPickPanel.add(mpOITxtType, vpGBC);
      mpPickPanel.add(mpOITxtOHDesc, vpGBC);
      mpPickPanel.add(mpOITxtOLDesc, vpGBC);
    }
    mpPickPanel.add(mpLITxtLocation, vpGBC);
    mpPickPanel.add(mpLITxtFromLoad, vpGBC);
    mpPickPanel.add(mpPITxtIMDesc, vpGBC);
    mpPickPanel.add(mpPITxtPick, vpGBC);
    mpPickPanel.add(mpPITxtLot, vpGBC);
    mpPickPanel.add(mpPITxtPosition, vpGBC);
    mpPickPanel.add(mpPITxtQty, vpGBC);
    vpGBC.anchor = GridBagConstraints.EAST;
    mpPickPanel.add(mpPCLabelToLoad, vpGBC);

    // Confirmation Column
    setInputColumnGridBagConstraints(vpGBC);
    vpGBC.gridx = 2;
    vpGBC.gridy = 0;
    mpPickPanel.add(mpConfirmHeader, vpGBC);        vpGBC.gridy++;
    mpPickPanel.add(new SKDCLabel(" "), vpGBC);     vpGBC.gridy++;
    if (mzDisplayOrderInfo)
    {
      mpPickPanel.add(new SKDCLabel(" "), vpGBC);     vpGBC.gridy++;
      mpPickPanel.add(new SKDCLabel(" "), vpGBC);     vpGBC.gridy++;
      mpPickPanel.add(new SKDCLabel(" "), vpGBC);     vpGBC.gridy++;
    }
    mpPickPanel.add(mpPCTxtConfirmLoc, vpGBC);     vpGBC.gridy++;  // Confirm Location?
    mpPickPanel.add(mpPCTxtConfirmLoad, vpGBC);     vpGBC.gridy++;
    mpPickPanel.add(new SKDCLabel(" "), vpGBC);     vpGBC.gridy++;  // No Description Confirmation
    mpPickPanel.add(mpPCTxtConfirmItem, vpGBC);     vpGBC.gridy++;
    mpPickPanel.add(mpPCTxtConfirmLot, vpGBC);      vpGBC.gridy++;
    mpPickPanel.add(mpPCTxtConfirmPosition, vpGBC); vpGBC.gridy++;
    mpPickPanel.add(mpPCTxtConfirmQty, vpGBC);      vpGBC.gridy++;
    mpPickPanel.add(mpPCTxtConfirmToLoad, vpGBC);   vpGBC.gridy++;
    
    addConfirmationListeners();
  }

  /**
   * Add listeners to the confirmation boxes
   */
  protected void addConfirmationListeners()
  {
    mpPCTxtConfirmLoc.addActionListener(new CompleteConfirmation(mpPILabelLoc, mpLITxtLocation, mpPCTxtConfirmLoc));
    mpPCTxtConfirmLoad.addActionListener(new CompleteConfirmation(mpLILabelFromLoad, mpLITxtFromLoad, mpPCTxtConfirmLoad));
    mpPCTxtConfirmItem.addActionListener(new CompleteConfirmation(mpPILabelItem, mpPITxtPick, mpPCTxtConfirmItem));
    mpPCTxtConfirmLot.addActionListener(new CompleteConfirmation(mpPILabelLot, mpPITxtLot, mpPCTxtConfirmLot));
    mpPCTxtConfirmQty.addActionListener(new CompleteConfirmation());
    mpPCTxtConfirmToLoad.addActionListener(new CompleteConfirmation());
    
    mpConfirmList.add(mpPCTxtConfirmLoc);
    mpConfirmList.add(mpPCTxtConfirmLoad);
    mpConfirmList.add(mpPCTxtConfirmItem);
    mpConfirmList.add(mpPCTxtConfirmLot);
    mpConfirmList.add(mpPCTxtConfirmQty);
    mpConfirmList.add(mpPCTxtConfirmToLoad);
  }
  
  /**
   * Allow enter to advance to the next confirmation or to complete the pick on
   * the last confirmation field
   */
  protected class CompleteConfirmation implements ActionListener
  {
    SKDCLabel mpLabel;
    SKDCTextField mpField;
    SKDCTextField mpConfirm;
    boolean mzHasConfirmation = false;
    
    /**
     * Completion without confirmation
     */
    public CompleteConfirmation()
    {
      super();
    }
    
    /**
     * Completion with confirmation
     * 
     * @param ipLabel - Label to take text from
     * @param ipField - Source Field
     * @param ipConfirm - Confirmation Field
     */
    public CompleteConfirmation(SKDCLabel ipLabel, SKDCTextField ipField,
        SKDCTextField ipConfirm)
    {
      super();
      mpLabel = ipLabel;
      mpField = ipField;
      mpConfirm = ipConfirm;
      mzHasConfirmation = true;
    }
    
    /**
     * Do  confirmation (if any) and advance to the next field
     *  
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
      boolean vzFoundSource = false;
      boolean vzLastField = true;
      
      // Simple confirmation
      if (mzHasConfirmation)
      {
        if(mpConfirm == mpPCTxtConfirmItem)
        {
          mpConfirm.setText(mpInvServer.swapItemSynonymIfEntered(mpConfirm.getText()));
        }
        if (!mpField.getText().trim().equals(mpConfirm.getText().trim()))
        {
          confirmFailed(mpConfirm, mpLabel.getText() + " requires confirmation.");
          return;
        }
        else
        {
          resetConfirmColors(mpConfirm);
        }
      }
      
      SKDCTextField vpSource = (SKDCTextField)e.getSource();
      for (SKDCTextField t : mpConfirmList)
      {
        if (t.equals(vpSource))
        {
          vzFoundSource = true;
        }
        else if (vzFoundSource)
        {
          if (t.isVisible())
          {
            t.requestFocus();
            vzLastField = false;
            break;
          }
        }
      }
      
      if (vzLastField)
      {
        pickButtonPressed();
      }
    }
  }
  
  
  /**
   * Build the button panel
   */
  protected JPanel buildButtonPanel()
  {
    mpReprintButton.setEnabled(false);
    
    mpButtonPanel = getEmptyButtonPanel();
    mpButtonPanel.add(mpPickButton);
    mpButtonPanel.add(mpReleaseButton);
    mpButtonPanel.add(mpStoreButton);
    mpButtonPanel.add(mpReprintButton);
    mpButtonPanel.add(mpAddCCIButton);
    mpAddCCIButton.setVisible(false);
    
    mpPickButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        pickButtonPressed();
      }
    });
    mpReleaseButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        releaseButtonPressed();
      }
    });
    mpStoreButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        storeButtonPressed();
      }
    });
    mpReprintButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        reprintButtonPressed();
      }
    });
    mpAddCCIButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        addCCIButtonPressed();
      }
    });
    
    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(getInfoPanel(), BorderLayout.CENTER);
    vpSouthPanel.add(mpButtonPanel, BorderLayout.SOUTH);
    
    return vpSouthPanel;
  }

  /**
  *  Method to clean up as needed at when the frame shuts down.
  */
  @Override
  public void shutdownFrame()
  {
    getSystemGateway().deleteObserver(MessageEventConsts.LOAD_EVENT_TYPE, messageObserver);
    messageObserver = null;
    
    super.shutdownFrame();
  }

  /**
   *  Method to populate the station combo box.
   */
  protected void stationFill()
  {
    try
    {
      Map stationsMap = mpStationServer.getStationsByStationType(outputStations);
      mpStationCombo.setComboBoxData(stationsMap.keySet().toArray());
      mpStationCombo.addItemListener(new ItemListener()
      {
          public void itemStateChanged(ItemEvent e)
          {
            station_itemStateChanged(e);
          }
      });
    }
    catch (DBException e)
    {
      displayError("Unable to get Stations: " + e.getMessage());
    }
  }

  /**
   *  Action method for Pick button. Verifies the load still exists and is
   *  still at this station, validates the required confirmations then completes
   *  the pick.
   */
  protected void pickButtonPressed()
  {
    if(performPrePickChecks())
      performPickAction();
  }

  protected void addCCIButtonPressed()
  {
    buildLightWeightCompList();
    enableLightWeightComponents(mpCompList, false);
    
    AddLoadLineItem updateItemDetail = Factory.create(AddLoadLineItem.class,
        "Add Cycle-Count Item", mpLITxtFromLoad.getText(), null);
    addSKDCInternalFrame(updateItemDetail, new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent ipPCE)
      {
        String prop = ipPCE.getPropertyName();
        if (prop.equals(FRAME_CHANGE))
        {
          try
          {
            if (mpMoveData != null)
            {
              mpMaintServ.deleteMaintenanceOrderMove(mpMoveData.getMoveID());
              mpCompList.clear();
              mpMoveData.clear();
              mpMoveData = null;
              setData();
//              mpPickButton.setEnabled(false);
            }
          }
          catch(DBException exc)
          {
            displayError("Error completing move for cycle-count load " + 
                         mpMoveData.getLoadID());
          }
        }
        else if (prop.equals(FRAME_CLOSING))
        {
          enableLightWeightComponents(mpCompList, true);
          mpReleaseButton.setEnabled(true);
          mpAddCCIButton.setEnabled(true);
        }
      }
    });
  }
  
  
  /**
   * Reset all of the confirm colors to the default
   */
  protected void resetAllConfirmColors()
  {
    if (mpConfirmBackground == null)
      return;

    resetConfirmColors(mpPCTxtConfirmLoc);
    resetConfirmColors(mpPCTxtConfirmLoad);
    resetConfirmColors(mpPCTxtConfirmItem);
    resetConfirmColors(mpPCTxtConfirmLot);
    resetConfirmColors(mpPCTxtConfirmPosition);
    resetConfirmColors(mpPCTxtConfirmToLoad);
    resetConfirmColors(mpPCTxtConfirmQty);
  }

  /**
   * Reset all of the confirm colors to the default
   */
  private void resetConfirmColors(SKDCTextField ipField)
  {
    if (mpConfirmBackground == null)
      return;

    ipField.setBackground(mpConfirmBackground);
    ipField.setForeground(mpConfirmForeground);
    ipField.setSelectedTextColor(mpConfirmSelectedText);
    ipField.setSelectionColor(mpConfirmSelection);
  }

  /**
   * Check to see we we can actually confirm the pick
   * @return
   */
  protected boolean performPrePickChecks()
  {
    resetAllConfirmColors();
    
    boolean vzSuccess = true;
    if (mpLoadData.getLoadID().length() == 0)
    {
      displayError("Load ID is Required");
      return false;
    }
    try
    {
      // make sure load still exists and is still here
      mpLoadData = mpLoadServer.getLoad1(mpLoadData.getLoadID().toString());
    }
    catch (DBException e2)
    {
      displayError("Unable to get Load data: " + e2.getMessage());
      return false;
    }
    if (mpLoadData == null)
    {
      displayError("Load " + mpLoadData.getLoadID().toString() + " does not exist");
      return false;
    }

    // verify load is at this station
    if ((!mpLoadData.getAddress().equals(mpStationData.getStationName())) ||
        (!mpLoadData.getWarehouse().equals(mpStationData.getWarehouse())))
    {
      displayInfo("Load " + mpLoadData.getLoadID().toString()
          + " not at station " + mpStationData.getStationName());
      return false;
    }

    // verify that required confirmations are done
    if (mpPCTxtConfirmLoc.isVisible() &&
        (!mpLITxtLocation.getText().trim().equals(mpPCTxtConfirmLoc.getText().trim())))
    {
      return confirmFailed(mpPCTxtConfirmLoc, mpPILabelLoc.getText()
          + " requires confirmation");
    }
    
    // verify that required confirmations are done
    if (mpPCTxtConfirmLoad.isVisible() &&
        (!mpLITxtFromLoad.getText().trim().equals(mpPCTxtConfirmLoad.getText().trim())))
    {
      return confirmFailed(mpPCTxtConfirmLoad, mpLILabelFromLoad.getText()
          + " requires confirmation");
    }

    if (mpPCTxtConfirmItem.isVisible())
    {
      // do item / synonym switch if needed on Item Moves or Cycle Counts
      if (mpMoveData.getMoveType() == DBConstants.ITEMMOVE || 
          mpMoveData.getMoveType() == DBConstants.CYCLECOUNTMOVE)
      {
        mpPCTxtConfirmItem.setText(mpInvServer.swapItemSynonymIfEntered(mpPCTxtConfirmItem.getText()));
      }
      if (!mpPITxtPick.getText().trim().equals(mpPCTxtConfirmItem.getText().trim()))
      {
        return confirmFailed(mpPCTxtConfirmItem, mpPILabelItem.getText()
            + " requires confirmation");
      }
    }

    if (mpPCTxtConfirmLot.isVisible() &&
      (!mpPITxtLot.getText().trim().equals(mpPCTxtConfirmLot.getText().trim())))
    {
      return confirmFailed(mpPCTxtConfirmLot, mpPILabelLot.getText()
          + " requires confirmation");
    }

    if (mpPCTxtConfirmPosition.isVisible() &&
        (!mpPITxtPosition.getText().trim().equals(mpPCTxtConfirmPosition.getText().trim())))
      {
        return confirmFailed(mpPCTxtConfirmPosition, mpPILabelPosition.getText()
            + " requires confirmation");
      }

    if ((mpPCTxtConfirmToLoad.isVisible()) &&
        (mpStationData.getDeleteInventory() == DBConstants.NO) &&
        (mpPCTxtConfirmToLoad.getText().length() == 0) &&
        (mpPCTxtConfirmQty.getValue() > 0))
    {
      return confirmFailed(mpPCTxtConfirmToLoad, "To Load is required");
    }

    if (mpPCTxtConfirmQty.isVisible())
    {
      if (mpMoveData.getMoveType() != DBConstants.CYCLECOUNTMOVE)
      {
        // non-cycle count checks
        if (mpPCTxtConfirmQty.getValue() < 0)
        {
          return confirmFailed(mpPCTxtConfirmQty, mpPILabelQty.getText()
              + " cannot be less than 0");
        }
        if ((mpPITxtQty.getValue() < mpPCTxtConfirmQty.getValue()))
        {
          return confirmFailed(mpPCTxtConfirmQty, mpPILabelQty.getText()
              + " cannot be greater than requested quantity");
        }

        if (mpPCTxtConfirmQty.getValue() < mpPITxtQty.getValue())
        {
          vzSuccess = processUnderPick();
        }
        else
        {
          mzZeroLeft = false;
        }
      }
      else
      {
        // cycle count checks
        if (mpPCTxtConfirmQty.getValue() < 0)
        {
          return confirmFailed(mpPCTxtConfirmQty, mpPILabelQty.getText()
              + " cannot be less than 0");
        }
      }
    }
    return vzSuccess;
  }
  
  /**
   * Changes colors of and focuses on the confirmation field and prints the 
   * error message.
   * 
   * @param ipField
   * @param isMessage
   * @return <code>false</code>
   */
  protected boolean confirmFailed(SKDCTextField ipField, String isMessage)
  {
    displayInfo(isMessage);
    ipField.setBackground(Color.RED);
    ipField.setForeground(Color.WHITE);
    ipField.setSelectedTextColor(Color.RED);
    ipField.setSelectionColor(Color.WHITE);
    ipField.requestFocus();
    return false;
  }
  
  /**
   * I need a JavaDoc
   * @return
   */
  protected boolean processUnderPick()
  {
    boolean vzRtn = true;
    
    if (!displayYesNoPrompt("Is remaining quantity zero"))
    {
      if (!mzAllowUnderPick)
      {
        displayInfoAutoTimeOut("Pick the requested quantity");
        mpPCTxtConfirmQty.setValue(0);
        vzRtn = false;                 // Operator needs to do the pick
      }
    }
    else
    {
      mzZeroLeft = true;
      mzReallocate = displayYesNoPrompt("Reallocate this pick");
    }
    
    return vzRtn;
  }
  
  /**
   * I need a JavaDoc
   */
  private void performPickAction()
  {
     try
     {
       switch (mpMoveData.getMoveType())
       {
         case DBConstants.LOADMOVE:
           performLoadPick();
           findNextLoad();
           break;
           
         case DBConstants.ITEMMOVE:
           if (mpPCTxtConfirmQty.isVisible())
           {
             if (mpPCTxtConfirmQty.getValue() >= 0)
             {
               performItemPick(mpPCTxtConfirmQty.getValue());
               findNextLoad(mpMoveData.getMoveDate());
             }
             else                      // See if we can skip the pick.
             {
               findNextLoad(mpMoveData.getMoveDate());
             }
           }
           else                        // Pick Qty. confirmation is turned off.
           {                           // Pick whatever was on the move originally.
             performItemPick(mpMoveData.getPickQuantity());
             findNextLoad(mpMoveData.getMoveDate());
           }
           break;
           
         case DBConstants.CYCLECOUNTMOVE:
           performCycleCountPick();
           findNextLoad();
           break;
           
         default:
           displayError("Unexpected type of move.");
       }
     }
     catch (DBException e2)
     {
       logAndDisplayException("Unable to complete this pick (see log)", e2);
     }
  }
  
  /**
   * I need a JavaDoc
   * @throws DBException
   */
  protected void performLoadPick() throws DBException
  {
    mpPickServer.completeLoadPick(SKDCUserData.getLoginName(), mpMoveData,
        mpStationData.getCaptive() != DBConstants.NONCAPTIVE,
        mpStationData.getDeleteInventory() == DBConstants.YES,
        mpPCTxtConfirmToLoad.getText() );
    
    displayInfoAutoTimeOut("Picked " + mpMoveData.getLoadID());
  }
  
 /**
  * Method to perform the actual pick.  If the total item detail qty. on a load
  * goes below the Item master CCI point quantity in the item master, the user
  * will be asked to do a cycle-count for that item for that load.
  * @param idPickQty the pick quantity.
  * @return <code>true</code> if the pick was successful, ><code>false</code>
  *         otherwise.
  * @throws DBException database errors.
  */
  protected boolean performItemPick(double idPickQty) throws DBException
  {
    boolean vzRtn = true;
    
    String vsItem = mpMoveData.getItem();
    String vsLot = mpMoveData.getPickLot();
    String vsPositionID = mpMoveData.getPositionID();

    mpPickServer.completeItemPick(SKDCUserData.getLoginName(), mpMoveData,
                                  mpPCTxtConfirmToLoad.getText().trim(), 
                                  mpStationData.getDeleteInventory() == DBConstants.YES,
                                  idPickQty, mzZeroLeft, mzReallocate,
                                  mpStationData.getPrinter());

    displayInfoAutoTimeOut("Picked " + idPickQty + " of item " + vsItem);

    // check if we need to do a CCI
    if (mpInvServer.needsCCI(mpLoadData.getLoadID(),vsItem, vsLot, 
                             vsPositionID ) >= 0.0)
    {
       // we need to verify quantity
      doCCI(mpLoadData.getLoadID(), vsItem, vsLot, mpMoveData.getPositionID());
      vzRtn = false;
    }
    
    return vzRtn;
  }
  
  /**
   * I need a JavaDoc
   * @throws DBException
   */
  protected void performCycleCountPick() throws DBException
  {
    if (!cciEnteredQty.quantityIsAccepted(mpPCTxtConfirmQty.getValue()))
    {
      return;
    }
    mpPickServer.completeCycleCountMove(SKDCUserData.getLoginName(), mpMoveData,
                                        mpPCTxtConfirmQty.getValue() );

    displayInfoAutoTimeOut("Completed cycle count");
  }

  /**
   * Method brings up the Cycle-Count confirmation frame if CCI Point is reached
   * after pick.
   * @param isLoadID the Load being cycle counted.
   * @param isItem the item being counted.
   * @param isLot the lot being counted.
   * @param isPosition the position of the item within he load.
   */
  protected void doCCI(String isLoadID, String isItem, String isLot, 
                     String isPosition)
  {
    final boolean storeEnabled = mpStoreButton.isEnabled();
    final boolean pickEnabled = mpPickButton.isEnabled();
    final boolean releaseEnabled = mpReleaseButton.isEnabled();
    setResizable(false);
    setIconifiable(false);
    
    final CCIPointFrame vpCCIPointFrame = Factory.create(CCIPointFrame.class);
    vpCCIPointFrame.setData(isLoadID, isItem, isLot, isPosition);
    
    PropertyChangeListener vpListener = new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent e)
      {
        String prop = e.getPropertyName();
        if (prop.equals(FRAME_CLOSING))
        {
          // reset buttons back to there original states
          mpPickButton.setEnabled(pickEnabled);
          mpReleaseButton.setEnabled(releaseEnabled);
          mpStoreButton.setEnabled(storeEnabled);
          setResizable(true);
          setIconifiable(true);
          findNextLoad();
        }
      }
    };
    
    addSKDCInternalFrameModal(vpCCIPointFrame, new JPanel[] {mpButtonPanel, mpPickPanel},
                              vpListener);
  }

  /**
   *  Action method for Release load button.
   */
  protected void releaseButtonPressed()
  {
    mpAddCCIButton.setVisible(false);
    if (mpLoadData != null)
    {
      String vsLoadID = mpLoadData.getLoadID();
      
      try
      {
        if (loadHasAdditionalPicks(vsLoadID)) return;

        // if this is Semi Captive and there are no items left on the isLoadID
        // see if the operator is removing or returning the isLoadID.
        if (mpStationData.getCaptive() == DBConstants.SEMICAPTIVE &&
            mpInvServer.getLoadLineCount(vsLoadID) == 0)
        {
          if (displayYesNoPrompt("Remove " + vsLoadID, "Warning"))
          {
            mpInvServer.deleteLoadWithChecking(vsLoadID);
            return;
          }
        }
        
        if (setAmountFull(mpLoadData.getAmountFull()) == -1)
        {
          return;
        }

        String errMsg = mpPickServer.releaseLoad(vsLoadID, mpStationData);
        if (errMsg != null)
        {
          displayError(errMsg);
        }
        else
        {
          logger.logDebug("Load: " + vsLoadID + " released from: " +
                          mpStationCombo.getSelectedItem() + ".");
        }
      }
      catch (DBException e2)
      {
        displayError("Error releasing isLoadID " + vsLoadID + ":" + e2.getMessage());
      }
      
      findNextLoad();
    }
  }

  /**
   * Does this load have additional picks?
   * 
   * @param isLoadID
   * @return
   * @throws DBException
   */
  protected boolean loadHasAdditionalPicks(String isLoadID) throws DBException
  {
    boolean vzMorePicks = true;
                                       // Check if there Empty Container
                                       // requests or more picks.
    if (mpMoveServer.getMoveCount("", isLoadID, "") > 0)
    {
      if (mzAllowUnderPick && mpOrderData.getOrderType() == DBConstants.ITEMORDER)
      {
        String vsWarn = "Load " + isLoadID + " has additional picks for " +
                        SKDCConstants.EOL_CHAR +
                        "order " + mpOITxtOrderID.getText() + ". Deallocate " + 
                        "remaining picks " + SKDCConstants.EOL_CHAR +
                        "for this load and release load";
        if (displayYesNoPrompt(vsWarn))
        {
          mpDeallocServ.deallocPickStationLoad(mpOrderData.getOrderID(),
                                               isLoadID, true);
          vzMorePicks = false;
        }
      }
      else
      {
        displayInfo("Load has more picks to complete before it can be released.");
      }
    }
    else
    {
      vzMorePicks = false;
    }
    
    return(vzMorePicks);
  }
  
  /**
   *  Action method to station changed event.
   *
   *  @param e Item event.
   */
  protected void station_itemStateChanged(ItemEvent e)
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
    mpMoveData = null;
    mpLoadData = null;
    mpOrderData = null;

    String vsStation = mpStationCombo.getSelectedStation();
    mpStationData = mpStationServer.getStation(vsStation);
    if (mpStationData == null)
    {
      displayError("Unable to get station data");
      return;
    }
    
    mpPCLabelToLoad.setVisible(mpStationData.getDeleteInventory() == DBConstants.NO);
    mpPCTxtConfirmToLoad.setVisible(mpStationData.getDeleteInventory() == DBConstants.NO);
    
    mpReprintButton.setVisible(mpStationData.getPrinter().trim().length() > 0);
    
    checkSubscriptions();
    findNextLoad();
  }

  /**
   * Method to find the oldest load at the current station.
   */
  protected synchronized void findNextLoad()
  {
    findNextLoad(null);
  } 
  
  /**
   * Method to find the oldest load at the current station.
   */
  protected synchronized void findNextLoad(Date ipNextPickStartDate)
  {
    mpMoveData = null;
    mpLoadData = null;
    try
    {
      // check if there is a load with picks here
      mpLoadData = mpLoadServer.getOldestLoad(mpStationData.getWarehouse(),
                                              mpStationData.getStationName(),
                                              DBConstants.ARRIVED);
      if (mpLoadData != null)
      {
        if (ipNextPickStartDate != null && mpOrderData.getOrderType() == DBConstants.ITEMORDER)
          findNextMove(mpLoadData.getLoadID(), ipNextPickStartDate);
        else
          findNextMove();
        
        if (mpMoveData != null)
        {
          if (mpMoveData.getMoveType() == DBConstants.CYCLECOUNTMOVE)
          {
            mpPickButton.setText(CCI_CONFIRM_LABEL);
            mpAddCCIButton.setVisible(true);
            mpAddCCIButton.setEnabled(true);
          }
          else
          {
            mpPickButton.setText(PICK_CONFIRM_LABEL);
            mpAddCCIButton.setVisible(false);
          }
        }

        // if we are at a PD stand and the load status is storing, store pending, or arrive Pending,
        // or ID pending, bypass the release since it already has been released.
        if (((mpStationData.getStationType() == DBConstants.PDSTAND) ||
            (mpStationData.getStationType() == DBConstants.REVERSIBLE)) &&
            ((mpLoadData.getLoadMoveStatus() == DBConstants.STORING) ||
            (mpLoadData.getLoadMoveStatus() == DBConstants.STORESENT) ||
            (mpLoadData.getLoadMoveStatus() == DBConstants.ARRIVEPENDING) ||
            (mpLoadData.getLoadMoveStatus() == DBConstants.IDPENDING) ||
            (mpLoadData.getLoadMoveStatus() == DBConstants.STOREPENDING)))
        {
          mpLITxtFromLoad.setText("");
          mpLITxtLocation.setText("");
          mpReleaseButton.setEnabled(false);
        }
        else
        {
          mpReleaseButton.setEnabled(true);
        }
      }
      else
      {
        setData();
        mpPickButton.setEnabled(false);
        mpReleaseButton.setEnabled(false);
      }

    }
    catch (DBException e)
    {
      displayError("Unable to get data:" + e.getMessage());
    }
  }

  /**
   *  Method to find the next move for the load at the station.
   */
  protected synchronized void findNextMove()
  {
    mpMoveData = null;
    try
    {
      mpMoveData = mpMoveServer.getNextMoveRecord(mpLoadData.getLoadID());
    }
    catch (DBException e)
    {
      displayError("Unable to get move data:" + e.getMessage());
    }
    setData();
  }

  /**
   * Method to find the next item pick for a load that is greater than a given
   * date. This method is used to try and skip a pick if possible.
   * 
   * @param isLoadID the load being picked from.
   * @param ipPickStartDate the start search date for next pick. Try to find the
   *          next pick greater than this current date.
   */
  protected synchronized void findNextMove(String isLoadID, Date ipPickStartDate)
  {
    try
    {
      mpMoveData = mpPickServer.getNextItemPick(isLoadID, ipPickStartDate);
      if (mpMoveData == null)
        mpMoveData = mpPickServer.getPrevItemPick(isLoadID, ipPickStartDate);
    }
    catch(DBException e)
    {
      displayError("Unable to get move data:" + e.getMessage());
      mpMoveData = null;
    }
    setData();
  }

  /**
   *  Method to refresh screen fields based upon the load and move. Determines
   *  what data needs to be displayed on the screen. Also determines what
   *  fields need operator confirmation.
   */
  protected synchronized void setData()
  {
    resetAllConfirmColors();
    
    mpPCTxtConfirmToLoad.setText("");
    
    mpOrderData = null;
    //cciEnteredQty.clear();
    if (mpLoadData == null)
    {
      mpLITxtFromLoad.setText("");
      mpLITxtLocation.setText("");
    }
    else
    {
      mpLITxtFromLoad.setText(mpLoadData.getLoadID());
      mpLITxtLocation.setText(mpLoadData.getWarehouse() + "-" + mpLoadData.getAddress());
    }
    if (mpMoveData == null)
    {
      mpOITxtOrderID.setText("");
      mpOITxtType.setText("");
      mpOITxtOHDesc.setText("");
      mpOITxtOLDesc.setText("");
      mpPITxtPick.setText("");
      mpPITxtIMDesc.setText("");
      mpPITxtLot.setText("");
      mpPITxtPosition.setText("");
      mpPITxtQty.setValue(0);
      
      mpPCTxtConfirmLoc.setVisible(false);
      mpPCTxtConfirmLoad.setVisible(false);
      mpPCTxtConfirmItem.setVisible(false);
      mpPCTxtConfirmLot.setVisible(false);
      mpPCTxtConfirmPosition.setVisible(false);
      mpPCTxtConfirmQty.setVisible(false);
      mpPCLabelToLoad.setVisible(false);
      mpPCTxtConfirmToLoad.setVisible(false);
      
      mpReleaseButton.requestFocus();
      mpPickButton.setEnabled(false);
    }
    else
    {
      try
      {
        if (mpMoveData.getMoveType() != DBConstants.CYCLECOUNTMOVE)
        {
          OrderHeaderData ordata = Factory.create(OrderHeaderData.class);
          ordata.setKey(OrderHeaderData.ORDERID_NAME,mpMoveData.getOrderID());
          mpOrderData = mpOrdServer.getOrderHeaderRecord(ordata);
          if (mpOrderData == null)
          {
            displayError("Unable to find order for this pick.");
            return;
          }

          // get the extra data needed to display
          OrderLineData oldata = Factory.create(OrderLineData.class);
          oldata.setKey(OrderLineData.ORDERID_NAME,mpMoveData.getOrderID());
          if (mpMoveData.getMoveType() == DBConstants.ITEMMOVE)
          {
            oldata.setKey(OrderLineData.ITEM_NAME,mpMoveData.getItem());
            oldata.setKey(OrderLineData.ORDERLOT_NAME,mpMoveData.getOrderLot());
            oldata.setKey(OrderLineData.LINEID_NAME,mpMoveData.getLineID());
          }
          else
          {
            oldata.setKey(OrderLineData.LOADID_NAME,mpMoveData.getLoadID());
          }
          OrderLineData lidata = mpOrdServer.getOrderLineRecord(oldata);

          mpOITxtOrderID.setText(mpMoveData.getOrderID());
          try
          {
            mpOITxtType.setText(DBTrans.getStringValue(OrderHeaderData.ORDERTYPE_NAME, mpOrderData.getOrderType()));
          }
          catch (NoSuchFieldException e)
          {
          }
          mpOITxtOHDesc.setText(mpOrderData.getDescription());
          mpOITxtOLDesc.setText(lidata.getDescription());
        }
        else  // cycle count
        {
          mpOrderData = mpMntOrderServ.getOrderHeaderRecord(mpMoveData.getOrderID());
          if (mpOrderData == null)
          {
            displayError("Unable to find header for this cycle count.");
            return;
          }

          mpOITxtOrderID.setText(mpMoveData.getOrderID());
          try
          {
            mpOITxtType.setText(DBTrans.getStringValue(MoveData.MOVETYPE_NAME, mpMoveData.getMoveType()));
          }
          catch (NoSuchFieldException e)
          {
          }
          mpOITxtOHDesc.setText(mpOrderData.getDescription());
        }

        switch (mpMoveData.getMoveType())
        {
          case DBConstants.ITEMMOVE:
            mpPITxtPick.setText(mpMoveData.getItem());
            mpPITxtIMDesc.setText(mpInvServer.getItemMasterDescription(mpMoveData.getItem()));
            mpPITxtLot.setText(mpMoveData.getPickLot());
            mpPILabelQty.setText(ITEM_PICK);
            mpPITxtPosition.setText(mpMoveData.getPositionID());
            mpPITxtQty.setValue(mpMoveData.getPickQuantity());
            
            mpPCTxtConfirmLoc.setVisible(mpStationData.getConfirmLocation() == DBConstants.YES);
            mpPCTxtConfirmLoad.setVisible(mpStationData.getConfirmLoad() == DBConstants.YES);
            mpPCTxtConfirmItem.setVisible(mpStationData.getConfirmItem() == DBConstants.YES);
            mpPCTxtConfirmLot.setVisible(mpStationData.getConfirmLot() == DBConstants.YES);
            mpPCTxtConfirmQty.setVisible(mpStationData.getConfirmQty() == DBConstants.YES);
            
            mpPCLabelToLoad.setVisible(mpStationData.getDeleteInventory() == DBConstants.NO);
            mpPCTxtConfirmToLoad.setVisible(mpStationData.getDeleteInventory() == DBConstants.NO);
            mpLILabelFromLoad.setText("Pick From Load:");
            break;

          case DBConstants.LOADMOVE:
            mpPILabelQty.setText(ITEM_PICK);
            mpPITxtPick.setText(mpLoadData.getLoadID());
            
            mpPCTxtConfirmLoc.setVisible(mpStationData.getConfirmLocation() == DBConstants.YES);
            mpPCTxtConfirmLoad.setVisible(mpStationData.getConfirmLoad() == DBConstants.YES);
            mpPCTxtConfirmItem.setVisible(false);
            mpPCTxtConfirmLot.setVisible(false);
            mpPCTxtConfirmQty.setVisible(false);
            
            mpLILabelFromLoad.setText("Pick From Load:");
            switch (mpStationData.getCaptive())
            {
              case DBConstants.CAPTIVE:
                mpPCLabelToLoad.setVisible(true);
                mpPCTxtConfirmToLoad.setVisible(true);
                break;
                
              default:
                mpPCLabelToLoad.setVisible(false);
                mpPCTxtConfirmToLoad.setVisible(false);
            }
            break;
            
          case DBConstants.CYCLECOUNTMOVE:
            mpPILabelItem.setText("Item:");
            mpPITxtPick.setText(mpMoveData.getItem());
            mpPITxtIMDesc.setText(mpInvServer.getItemMasterDescription(mpMoveData.getItem()));
            mpPITxtLot.setText(mpMoveData.getPickLot());
            mpPITxtPosition.setText(mpMoveData.getPositionID());
            if(cciEnteredQty.getConfirmCount() == 0)
            {
            	mpPILabelQty.setText(CYCLE_COUNT);
            }
            mpPITxtQty.setValue(mpMoveData.getPickQuantity());
            mpPILabelItem.setText("Item:");
            
            mpPCTxtConfirmLoc.setVisible(mpStationData.getConfirmLocation() == DBConstants.YES);
            mpPCTxtConfirmLoad.setVisible(mpStationData.getConfirmLoad() == DBConstants.YES);
            mpPCTxtConfirmItem.setVisible(mpStationData.getConfirmItem() == DBConstants.YES);
            mpPCTxtConfirmLot.setVisible(mpStationData.getConfirmLot() == DBConstants.YES);
            if (!mpMoveData.getItem().isEmpty())
            {                          // Only show the Confirm Qty. field if 
                                       // there is an item to count.
              mpPCTxtConfirmQty.setVisible(true);
              mpPCTxtConfirmQty.setEnabled(true);
            }
            
            mpPCLabelToLoad.setVisible(false);
            mpPCTxtConfirmToLoad.setVisible(false);
            mpLILabelFromLoad.setText("Count Load:");
            break;
            
          default:
            displayError("Unexpected type of move.");
            return;
        }
        mpPickButton.setEnabled(true);
      }
      catch (DBException e2)
      {
        displayError("Unable to get screen data: " + e2.getMessage());
        return;
      }
    }
    mpPCTxtConfirmLoc.setText("");
    mpPCTxtConfirmLoad.setText("");
    mpPCTxtConfirmItem.setText("");
    mpPCTxtConfirmLot.setText("");
    mpPCTxtConfirmQty.setValue(0);

    if (mpPCTxtConfirmLoc.isVisible())         mpPCTxtConfirmLoc.requestFocus();
    else if (mpPCTxtConfirmLoad.isVisible())   mpPCTxtConfirmLoad.requestFocus();
    else if (mpPCTxtConfirmItem.isVisible())   mpPCTxtConfirmItem.requestFocus();
    else if (mpPCTxtConfirmLot.isVisible())    mpPCTxtConfirmLot.requestFocus();
    else if (mpPCTxtConfirmQty.isVisible())    mpPCTxtConfirmQty.requestFocus();
    else if (mpPCTxtConfirmToLoad.isVisible()) mpPCTxtConfirmToLoad.requestFocus();
    pack();
  }

  /**
   *  Action method for Store button.
   */
  protected void storeButtonPressed()
  {
    addSKDCInternalFrame(Factory.create(StoreItemsMain.class, mpStationCombo.getSelectedStation()));
  }

  /**
   * Reprint the last label
   */
  protected void reprintButtonPressed()
  {
    try
    {
      mpPickServer.reprintLastPickLabel(mpStationData.getPrinter());
    }
    catch (Exception e)
    {
      logAndDisplayException("Error reprinting pick label", e);
    }
  }
  
  /**
   *  Method to check message subscriptions. This screen needs to be notified
   *  of arrivals to this station and also load releases from the station. When
   *  the station we are working at changes we need to check if a different
   *  scheduler controls this station. If we change schedulers, we unsubscribe
   *  for messages from the old scheduler and subscribe to messages for the
   *  new scheduler.
   */
  private void checkSubscriptions()
  {
    if (mpStationData != null)
    {
      String stationsScheduler;
      try
      {
        stationsScheduler = mpStationServer.getStationsScheduler(mpStationData.getStationName());
      }
      catch(DBException exc)
      {
        displayError("Scheduler not found for station " + mpStationData.getStationName());
        return;
      }
      
      // if we have changed schedulers then
      // unsubscribe from old scheduler and
      // subscribe to new scheduler
      if (!subscribedScheduler.equals(stationsScheduler))
      {
        getSystemGateway().deleteObserver(MessageEventConsts.LOAD_EVENT_TYPE, messageObserver);
        //
        String selector = getSystemGateway().getLoadEventSelector(stationsScheduler);
        getSystemGateway().addObserver(
            MessageEventConsts.LOAD_EVENT_TYPE, selector, messageObserver);

        subscribedScheduler = stationsScheduler;
      }
    }
  }


  /**
   * An observer class needed for this screen to receive and process messages.
   */
  private class MessageObserver extends DacObserver
  {
    /**
     * Constructor
     */
    public MessageObserver()
    {
      super(logger);
    }

    /**
     * Method to process the arrivals and releases of loads for this station.  If
     * a load arrives at or leaves the station, then we recheck and reset the
     * screen with the correct information.
     *
     * @param o no information available
     * @param arg no information available
     */
    public void update(Observable o, Object arg)
    {
      ObservableControllerImpl observableData = (ObservableControllerImpl)o;
      String sText = observableData.getStringData();
      mpLEDF.decodeReceivedString(sText);

      if (((mpLEDF.getMessageID() == AGCDeviceConstants.GENERALLOADARRIVALATSTATION) &&
        (mpLEDF.getSourceStation().equals(mpStationData.getStationName())) &&
        (!mpLEDF.getLoadID().equals(mpLITxtFromLoad.getText()))) ||
        ((mpLEDF.getMessageID()== AGCDeviceConstants.GENERALSTORELOAD) &&
        (mpLEDF.getLoadID().equals(mpLITxtFromLoad.getText()))))
      {
        ensureDBConnection();
        findNextLoad();
      }
    }
  }

  /**
   * An cycle count class used to verify multiple entry of quantity.
   *
   */
  protected class CycleCountQuantity
  {
    private double savedQuantity;
    private int numberOfConfirmsRequired = 1;
    private int numberOfConfirmsPerformed;

    public CycleCountQuantity()
    {
      clear();
    }

    /**
     * Method to clear the cycle count quantities.
     *
     */
    public void clear()
    {
      savedQuantity = -1.0;
      numberOfConfirmsPerformed = 0;
    }
    
    /**
     * Method returns the number of times the count has been perormed
     * @return int
     */
    public int getConfirmCount()
    {
    	return numberOfConfirmsPerformed;
    }

    /**
     * Method to check if the quantity is accepted.
     *
     * @param countedQuantity Count quantity entered by the operator
     */
    public boolean quantityIsAccepted(double countedQuantity)
    {
      
      if (numberOfConfirmsPerformed == 0)
      {
        LoadLineItemData vpLoadLineData = Factory.create(LoadLineItemData.class);
        vpLoadLineData.setKey(LoadLineItemData.LOADID_NAME, mpMoveData.getLoadID());
        vpLoadLineData.setKey(LoadLineItemData.ITEM_NAME, mpMoveData.getItem());
        vpLoadLineData.setKey(LoadLineItemData.LOT_NAME, mpMoveData.getPickLot());
        vpLoadLineData.setKey(LoadLineItemData.POSITIONID_NAME, mpMoveData.getPositionID());
        try
        {
          vpLoadLineData = mpInvServer.getLoadLineItem(vpLoadLineData);
          if(vpLoadLineData != null && vpLoadLineData.getCurrentQuantity() == countedQuantity)
          {
            return true;
          }
        }
        catch (DBException dbe)
        {
          logAndDisplayError("Error getting LoadLine for LoadID: " + mpMoveData.getLoadID());
        }
      }

      if (numberOfConfirmsPerformed++ == numberOfConfirmsRequired)
      {
        if (savedQuantity == countedQuantity)
        {
          clear();
          return (true);
        }
        else
        {
          displayError("Entered counts do not match!\nPlease re-enter count.");
          clear();
          return false;
        }
      }
      mpPILabelQty.setText(CYCLE_COUNT_2);
      mpPCTxtConfirmQty.setValue(0.0);
      pack();
      // Make sure the confirm quantity is selected
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run()
        {
          mpPCTxtConfirmQty.selectAll();
        }});
      savedQuantity = countedQuantity;
      return (false);
    }
  }

  private void buildLightWeightCompList()
  {
    if (mpStationCombo.isEnabled())         mpCompList.add(mpStationCombo);
    if (mpPCTxtConfirmLoc.isEnabled())      mpCompList.add(mpPCTxtConfirmLoc);
    if (mpPCTxtConfirmLoad.isEnabled())     mpCompList.add(mpPCTxtConfirmLoad);
    if (mpPCTxtConfirmItem.isEnabled())     mpCompList.add(mpPCTxtConfirmItem);
    if (mpPCTxtConfirmLot.isEnabled())      mpCompList.add(mpPCTxtConfirmLot);
    if (mpPCTxtConfirmPosition.isEnabled()) mpCompList.add(mpPCTxtConfirmPosition);
    if (mpPCTxtConfirmQty.isEnabled())      mpCompList.add(mpPCTxtConfirmQty);
    if (mpPCTxtConfirmToLoad.isEnabled())   mpCompList.add(mpPCTxtConfirmToLoad);
    
    if (mpStoreButton.isEnabled())   mpCompList.add(mpStoreButton); 
    if (mpPickButton.isEnabled())    mpCompList.add(mpPickButton);
    if (mpReleaseButton.isEnabled()) mpCompList.add(mpReleaseButton);
    if (mpReprintButton.isEnabled()) mpCompList.add(mpReprintButton);
    if (mpAddCCIButton.isEnabled())  mpCompList.add(mpAddCCIButton);
  }
  
  /**
   * Method to choose and update the amount full.
   * 
   * @param inCurrAMountFull the current amount full.
   * @return -1 if the user clicks on the Cancel button, or there is some type
   *         of error.
   */
  protected int setAmountFull(int inCurrAMountFull)
  {
    int vnRtn = 0;
    String vsAmtFullSelect = null;
    try
    {
      String vsAmtFullTran = DBTrans.getStringValue(LoadData.AMOUNTFULL_NAME, 
                                                    inCurrAMountFull);
      String[] vasAmtFullTrans = DBTrans.getStringList(LoadData.AMOUNTFULL_NAME);
      vsAmtFullSelect = (String)JOptionPane.showInputDialog(this, 
                                                  "Choose amount full", "Input",
                                                   JOptionPane.QUESTION_MESSAGE,
                                                   null,
                                                   vasAmtFullTrans,
                                                   vsAmtFullTran);
      int vnSelectedAmtFull = -1;
      if (vsAmtFullSelect != null)
      {
        vnSelectedAmtFull = DBTrans.getIntegerValue(LoadData.AMOUNTFULL_NAME,
                                                    vsAmtFullSelect);
        if (vnSelectedAmtFull != inCurrAMountFull)
        {
          // set the load amount full
          mpLoadServer.setLoadAmountFull(mpLoadData.getLoadID(), vnSelectedAmtFull);
        }
      }
      else
      {
        vnRtn = -1;
      }
    }
    catch (NoSuchFieldException e)
    {
      logger.logException(e);
      vnRtn = -1;
    }
    catch (DBException e)
    {
      logger.logException(e);
      vnRtn = -1;
    }

    return vnRtn;
  }
}

