package com.daifukuamerica.wrxj.swingui.store;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swingui.itemdetail.StoreLoadLineItem;
import com.daifukuamerica.wrxj.swingui.pick.EOAPick;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class that handles End of Aisle (EOA) store functionality. It
 * inserts empty containers, stores items onto loads at the EOA, stores loads,
 * and releases loads back to storage.
 * 
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class StoreItemsMain extends SKDCInternalFrame
{
  public final static String ADDITEMTEXT = "Add Item";
  public final static String ADDPOITEMTEXT = "Receive ER";

  protected BorderLayout borderLayout1 = new BorderLayout();
  protected LoadData ld = null;
  protected String loadID = "";

  protected MessageObserver messageObserver = new MessageObserver();
  protected LoadEventDataFormat mpLEDF = null;
  protected String subscribedScheduler = "";
  protected boolean firstTimeForLoad = true;
  protected boolean mzPOEntered = false;

  protected DacTable sktable = null;
  protected JPanel mpPanelButtons = getEmptyButtonPanel();
  protected SKDCButton mpBtnAdd = new SKDCButton(ADDITEMTEXT, "Add an item to the load", 'A');
  protected SKDCButton mpBtnAddAll = new SKDCButton(ADDPOITEMTEXT, "Add all Expected Receipt items to the load", 'E');
  protected SKDCButton mpBtnClose = new SKDCButton("Close", "Leave screen");
  protected SKDCButton mpBtnPick = new SKDCButton("Pick Screen", "Bring up the screen for picking", 'P');
  protected SKDCButton mpBtnRelease = new SKDCButton("Release Load", "Release this load to storage", 'R');

  protected StationComboBox mpStationComboBox;
  protected SKDCComboBox mpContainer = new SKDCComboBox();
  protected SKDCTextField mpLoadText = new SKDCTextField(LoadData.LOADID_NAME);
  protected SKDCTextField mpPOText = new SKDCTextField(PurchaseOrderHeaderData.ORDERID_NAME);
  
  protected boolean listDisplayed = false;
  protected StationData stationData;
  protected StationData linkStationData;
  protected MoveData moveData = null;
  protected SKDCLabel infoDisplay = new SKDCLabel();
  //
  protected StandardInventoryServer mpInventoryServer = null;
  protected StandardLoadServer mpLoadServer = null;
  protected StandardMoveServer mpMoveServer = null;
  protected StandardPickServer mpPickServer = null;
  protected StandardPoReceivingServer mpPoReceivingServer = null;
  protected StandardRouteServer mpRouteServer = null;
  protected StandardStationServer mpStationServer = null;

  /**
   * Create EOA Store screen class.
   * 
   */
  public StoreItemsMain()
  {
    mpInventoryServer = Factory.create(StandardInventoryServer.class);
    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpMoveServer = Factory.create(StandardMoveServer.class);
    mpPickServer = Factory.create(StandardPickServer.class);
    mpPoReceivingServer = Factory.create(StandardPoReceivingServer.class);
    mpRouteServer = Factory.create(StandardRouteServer.class);
    mpStationServer = Factory.create(StandardStationServer.class);

    try
    {
      jbInit();
      pack();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Create EOA Store screen class.
   * @param isInitialStation
   */
  public StoreItemsMain(String isInitialStation)
  {
    this();
    mpStationComboBox.setSelectedStation(isInitialStation);
  }

  /**
   * Method to initialize screen components. This adds the components to the
   * screen and adds listeners as needed.
   * 
   * @exception Exception
   */
  protected void jbInit() throws Exception
  {
    stationFill();
    containerFill();

    setTitle("Store");
    JPanel mpStoreItemsPanel = new JPanel();
    getContentPane().add(mpStoreItemsPanel, BorderLayout.CENTER);

    GridBagConstraints vpConstraints = new GridBagConstraints();
    setLabelColumnGridBagConstraints(vpConstraints);
    
    JPanel vpPanelInput = getEmptyInputPanel("");
    vpPanelInput.add(new SKDCLabel("Station:"), vpConstraints);
    vpPanelInput.add(new SKDCLabel("Container Type:"), vpConstraints);
    vpPanelInput.add(new SKDCLabel("Load ID:"), vpConstraints);
    vpPanelInput.add(new SKDCLabel("Expected Receipt:"), vpConstraints);

    setInputColumnGridBagConstraints(vpConstraints);

    vpPanelInput.add(mpStationComboBox, vpConstraints);
    vpPanelInput.add(mpContainer, vpConstraints);
    mpLoadText.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        load_actionPerformed(e);
      }
    });
    vpPanelInput.add(mpLoadText, vpConstraints);
    mpPOText.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
        poKeyReleased();
      }
    });
    vpPanelInput.add(mpPOText, vpConstraints);

    vpConstraints.gridx = 0;
    vpConstraints.gridwidth = 2;
    vpConstraints.anchor = GridBagConstraints.CENTER;

    vpPanelInput.add(infoDisplay, vpConstraints);

    mpBtnAdd.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        addButtonPressed();
      }
    });
    mpBtnAddAll.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        addAllButtonPressed();
      }
    });
    mpBtnAddAll.setEnabled(false);
    mpBtnClose.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        closeButtonPressed();
      }
    });
    mpBtnPick.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        pickButtonPressed();
      }
    });
    mpBtnRelease.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        releaseButtonPressed();
      }
    });
    mpPanelButtons.add(mpBtnAdd);
    mpPanelButtons.add(mpBtnAddAll);
    mpPanelButtons.add(mpBtnRelease);
    mpPanelButtons.add(mpBtnPick);
//    buttonPanel.add(mpBtnClose);

    sktable = new DacTable(new DacModel(new ArrayList(), "Store Items"));
    sktable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    sktable.setDateDisplayFormat(SKDCConstants.DATE_FORMAT2);
    
    JPanel vpTablePanel = new JPanel(new GridLayout(1,1));
    vpTablePanel.setPreferredSize(new Dimension(0, 200));
    vpTablePanel.add(sktable.getScrollPane());

    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(getInfoPanel(), BorderLayout.CENTER);
    vpSouthPanel.add(mpPanelButtons, BorderLayout.SOUTH);

    mpStoreItemsPanel.setLayout(new BorderLayout());
    mpStoreItemsPanel.add(vpPanelInput, BorderLayout.NORTH);
    mpStoreItemsPanel.add(vpTablePanel, BorderLayout.CENTER);
    mpStoreItemsPanel.add(vpSouthPanel, BorderLayout.SOUTH);

    mpLEDF = Factory.create(LoadEventDataFormat.class, this.getTitle());
    
    setPreferredSize(new Dimension(800,500));
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
   * Overridden method so we can set up frame for messaging and for the default
   * work station.
   * 
   * @param e
   *          no information available
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent e)
  {
    super.internalFrameOpened(e);
    checkSubscriptions();
    stationChange();
  }

  /**
   * Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    getSystemGateway().deleteObserver(MessageEventConsts.LOAD_EVENT_TYPE, messageObserver);
    messageObserver = null;
    //
    mpStationServer.cleanUp();
    mpLoadServer.cleanUp();
    mpInventoryServer.cleanUp();
    mpRouteServer.cleanUp();
    //
    mpPickServer.cleanUp();
    mpPoReceivingServer.cleanUp();
    super.cleanUpOnClose();
  }

  /**
   * Method to populate the station combo box.
   */
  protected void stationFill()
  {
    try
    {
      int[] inputStations =
      { DBConstants.USHAPE_OUT, DBConstants.PDSTAND, DBConstants.REVERSIBLE, DBConstants.INPUT };
      Map stationsMap = mpStationServer.getStationsByStationType(inputStations);
      mpStationComboBox = new StationComboBox(stationsMap.keySet().toArray(), SysConfig.OPTYPE_STORE);
      mpStationComboBox.addItemListener(new java.awt.event.ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          station_itemStateChanged(e);
        }
      });
    }
    catch(DBException e)
    {
      displayError("Unable to get Stations");
    }
  }

  /**
   * Method to populate the container type combo box.
   */
  protected void containerFill()
  {
    try
    {
      List containerList = mpInventoryServer.getContainerTypeList();
      mpContainer.setComboBoxData(containerList);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }

  /**
   * Method to filter by name. Refreshes display.
   * 
   * @param s
   *          Load to search for.
   */
  public void refreshTable(String s)
  {
    try
    {
      sktable.refreshData(mpInventoryServer.getLoadLineItemDataListByLoadID(s));
      listDisplayed = true;
    }
    catch (DBException e)
    {
      logAndDisplayException(e);
    }
    catch (NoSuchElementException e)
    {
    }
  }

  /**
   * Method to validate this load. If the load exists it displays existing
   * information for the load. If it does not exist we may check to see if a new
   * load can be inserted here. We may also add the load to the database.
   * 
   * @param addLoad
   *          True if we are to add the load to the database.
   * @param checkInsertMode
   *          True if we need to check station modes.
   * 
   */
  protected boolean checkLoad(boolean addLoad, boolean checkInsertMode)
  {
    boolean result = false;
    // Auto generate Load ID if blank
    if(mpLoadText.getText().trim().length() == 0)
    {
      mpLoadText.setText(mpLoadServer.createRandomLoadID());
    }

    if(mpLoadText.getText().equals(loadID))
    {
      return true;
    }
    try
    {
      firstTimeForLoad = true;
      ld = mpLoadServer.getLoad1(mpLoadText.getText());
    }
    catch(DBException e2)
    {
      displayError("Unable to get Load data - " + e2.getMessage());
      return false;
    }
    if (ld == null)
    {
      if(checkInsertMode)
      {
        try
        {
          String vsStationName = stationData.getStationName();
          if(vsStationName != null)
          {
            mpStationServer.canStationInsertALoad(vsStationName);
          }
          else
          {
            displayInfo("Station must be selected");
          }
        }
        catch(DBException dbe)
        {
          displayInfo(dbe.getMessage());
          return false;
        }
      }

      // add the load before we add any items or store it
      try
      {
        ld = Factory.create(LoadData.class);
        mpContainer.setEnabled(true);

        if(addLoad)
        {
          ld.setWarehouse(stationData.getWarehouse());
          ld.setAddress(stationData.getStationName());
          ld.setLoadID(mpLoadText.getText());
          ld.setParentLoadID(mpLoadText.getText());
          ld.setContainerType(mpContainer.getText());
          // ld.setRouteID(stationData.getStationName());
          ld.setDeviceID(stationData.getDeviceID());
          ld.setAmountFull(DBConstants.EMPTY);
          ld.setLoadMoveStatus(DBConstants.ARRIVED);
          mpLoadServer.addLoad(ld);
          loadID = ld.getLoadID();
          mpLoadText.setText(loadID);
          mpContainer.setEnabled(false);
        }
        mpLoadText.setEnabled(false);
        mpLoadText.setEditable(false);
        result = true;
      }
      catch(DBException e2)
      {
        displayError("Error adding Load " + mpLoadText.getText() + " - " + e2.getMessage());
      }
    }
    else
    {

      mpContainer.setEnabled(false);
      // verify load is at this station
      if((!ld.getAddress().equals(stationData.getStationName()))
          || (!ld.getWarehouse().equals(stationData.getWarehouse())))
      // if
      // (!ld.getAddress().trim().equals(stationData.getStationName().trim()))
      {
        displayInfo("Load " + mpLoadText.getText() + " not at Station " + stationData.getStationName());
        return false;
      }

      loadID = ld.getLoadID();

      // set container type based on load data
      this.mpContainer.setSelectedItem(ld.getContainerType());
      mpLoadText.setText(loadID);
      mpLoadText.setEnabled(false);
      mpLoadText.setEditable(false);

      result = true;
    }
    return result;
  }

  /**
   * Action method to handle Add button. Brings up screen to do the add.
   */
  protected void addButtonPressed()
  {
    if (checkLoad(true, true))
    {
      if (!listDisplayed)
      {
        refreshTable(mpLoadText.getText());
      }

      mpStationComboBox.setEnabled(false);
      String vsER = null;
      if (mpPOText.getText().length() > 0 || 
          Application.getBoolean("PORequired", true))
      {
        vsER = mpPOText.getText();
      }
      StoreLoadLineItem vpStoreItemDetail = Factory.create(StoreLoadLineItem.class,
          "Add Item Detail", mpLoadText.getText(), null, vsER);
      if (vpStoreItemDetail.validateER())
      {
        addSKDCInternalFrameModal(vpStoreItemDetail, mpPanelButtons,
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ipPCE)
              {
                String prop = ipPCE.getPropertyName();
                if (prop.equals(FRAME_CHANGE))
                {
                  refreshTable(mpLoadText.getText());
                }

              }
            });
      }
      else
      {
        vpStoreItemDetail.close();
      }
    }
    else
    {
      mpLoadText.setEditable(true);
      mpLoadText.setEnabled(true);
      mpLoadText.requestFocus();
    }
  }

  /**
   * Action method to handle Add button. Brings up screen to do the add.
   */
  protected void addAllButtonPressed()
  {
    if (checkLoad(true, true))
    {
      if (mzPOEntered)
      {
        // If there is something in the Purchase Order field then the add
        // button should add items that are attached to the PO
        if (mpPoReceivingServer.receiveEntirePO(mpPOText.getText(), 
            mpLoadText.getText()))
        {
          mpPOText.setText("");
          // get the update load information
          try
          {
            ld = mpLoadServer.getLoad1(mpLoadText.getText());
          }
          catch (DBException e2)
          {
            displayError("Unable to get Load data - " + e2.getMessage());
            return;
          }
          refreshTable(mpLoadText.getText());
        }
        else
        {
          displayError("Unable to add items for Expected Receipt \""
              + mpPOText.getText() + "\" to Load");
        }
      }
    }
    else
    {
      mpLoadText.setEditable(true);
      mpLoadText.setEnabled(true);
      mpLoadText.requestFocus();
    }
  }

  /**
   * Action method to handle a load action event.
   * 
   * @param e
   *          Action event.
   */
  protected void load_actionPerformed(ActionEvent e)
  {
    if(checkLoad(false, false))
    {
      refreshTable(mpLoadText.getText());
      mpContainer.requestFocus();
    }
    else
    {
      mpLoadText.setText("");
    }
  }

  /**
   * Action method to handle a purchase order event.
   */
  protected void poKeyReleased()
  {
    mzPOEntered = mpPOText.getText().length() > 0;
    mpBtnAddAll.setEnabled(mzPOEntered);
  }


  /**
   * Method to reset screen fields to defaults.
   */
  protected void clearScreen()
  {
    infoDisplay.setText("");
    infoDisplay.setIcon(null);
    mpLoadText.setEditable(true);
    mpLoadText.setEnabled(true);
    mpLoadText.setText("");
    mpContainer.setEnabled(true);
    mpStationComboBox.setEnabled(true);
    listDisplayed = false;
    sktable.clearTable();
    loadID = "";
  }

  /**
   * Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   * Action method to station changed event.
   * 
   * @param e
   *          Item event.
   */
  protected void station_itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      stationChange();
    }
  }

  /**
   * Method to handle a station change. When a station change occurs it tries to
   * find the load at the new station as well as any empty container requests
   * for the load.
   */
  protected void stationChange()
  {
    try
    {
      clearScreen();
      //
      // Get stationId from Description
      //
      String vsStation = mpStationComboBox.getSelectedStation();
      StandardStationServer vpStationServer = Factory.create(StandardStationServer.class);
      stationData = vpStationServer.getStation(vsStation);
      if (stationData != null)
      {
        firstTimeForLoad = true;
        StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
        ld = vpLoadServer.getOldestLoad(stationData.getWarehouse(), stationData.getStationName(), DBConstants.ARRIVED);
        if (ld != null)
        {
          mpLoadText.setText(ld.getLoadID());
          mpLoadText.setEnabled(false);

          // set container type based on load data
          mpContainer.setSelectedItem(ld.getContainerType());

          refreshTable(ld.getLoadID());
        }
        else
        {
          mpContainer.setSelectedItem(stationData.getContainerType());
          mpLoadText.setText("");
          infoDisplay.setText("");
          mpLoadText.requestFocus();
        }
        checkSubscriptions();
      }
    }
    catch(DBException e2)
    {
      logAndDisplayException(e2);
      return;
    }
  }

  /**
   * Action method for Pick button.
   */
  protected void pickButtonPressed()
  {
    displayPickScreen();
  }

  /**
   * Method to put up the pick screen.
   */
  protected void displayPickScreen()
  {
    addSKDCInternalFrame(Factory.create(EOAPick.class, mpStationComboBox.getSelectedStation()));
  }

  /**
   * Action method for Release load button.
   */
  protected void releaseButtonPressed()
  {
    if(checkLoad(true, true))
    {
      releaseLoad(mpLoadText.getText().trim());
    }
    else
    {
      mpLoadText.setEditable(true);
      mpLoadText.setEnabled(true);
      mpLoadText.requestFocus();
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
  protected void releaseLoad(String load)
  {
    try
    {
      // check if there Empty Container requests or more picks
      if (mpMoveServer.getMoveCount("", load, "") > 0)
      {
        // if more picks, they must be completed
        displayInfo("Load has more picks.  Use Pick screen to complete work before releasing.");
        return;
      }

      if(setAmountFull(ld.getAmountFull()))
      {
        String errMsg = mpPickServer.releaseLoad(load, stationData);
        if(errMsg != null)
        {
          displayError(errMsg);
        }
        else
        {
          logger.logDebug("Load \"" + load + "\" released from Station " + mpStationComboBox.getText());
        }
      }
    }
    catch(DBException e2)
    {
      displayError("Error releasing Load " + load + " - " + e2.getMessage());
      return;
    }
    stationChange();
  }

  /**
   * Method to choose and update the amount full.
   * 
   */
  protected boolean setAmountFull(int currentAmountFull)
  {
    boolean vbResult = false;
    int amountFull = currentAmountFull;
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
      if (amountFull != currentAmountFull)
      {
        // set the load amount full
        mpLoadServer.setLoadAmountFull(ld.getLoadID(), amountFull);
      }
    }
    catch(NoSuchFieldException e)
    {
      logAndDisplayException(e);
    }
    catch(DBException e)
    {
      logAndDisplayException(e);
    }
    return vbResult;
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
    if(stationData != null)
    {
      String stationsScheduler;
      try
      {
        stationsScheduler = mpStationServer.getStationsScheduler(stationData.getStationName());
      }
      catch(DBException exc)
      {
        displayError("Scheduler not found for station " + stationData.getStationName());
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
    @Override
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
            && (mpLEDF.getSourceStation().equals(stationData.getStationName()))
            && (mpLoadText.getText().trim().length() == 0))
        {
          stationChange();
        }
      }
      catch(Exception e)
      {
        String vsStationName = null;
        if(stationData != null)
        {
          vsStationName = stationData.getStationName();
        }
        logger.logException(e, "update() - sText \"" + sText + "\"\nmpLoadText \"" + mpLoadText.getText()
            + "\"\nStation " + vsStationName);
      }
    }
  }
}
