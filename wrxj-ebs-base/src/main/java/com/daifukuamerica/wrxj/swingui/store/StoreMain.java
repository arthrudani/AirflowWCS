package com.daifukuamerica.wrxj.swingui.store;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Receive screen to receive items into the system at an input station.
 *
 * @author A.D.
 * @since  02-Apr-2008
 */
@SuppressWarnings("serial")
public class StoreMain extends DacInputFrame implements Observer
{
                                       // Transaction Servers
  private StandardLoadServer        mpLoadServ;
  private StandardInventoryServer   mpInvServ;
  private StandardSchedulerServer   mpSchedServer;
  private StandardStationServer     mpStationServ;
  private StandardPickServer        mpPickServ;
  private StandardMoveServer        mpMoveServ;
  private StandardPoReceivingServer mpPOServ;
                                       // GUI Components.
  private StationComboBox mpStationCombo;
  private SKDCTextField   mpStoreLoadTextField;
  private SKDCComboBox    mpContainerCombo;
  private SKDCTextField   mpERTextField;
  private SKDCButton      mpBtnInsertID;
  private SKDCButton      mpBtnReceiveLines;
                                       // Misc. declarations.
  private volatile boolean mzObserverStationCheck = false;
  private boolean mzPORequiredField;
  private List<JComponent> mpCompList = new ArrayList<JComponent>();

  public StoreMain()
  {
    super("Store", "");
    mpLoadServ    = Factory.create(StandardLoadServer.class);
    mpInvServ     = Factory.create(StandardInventoryServer.class);
    mpSchedServer = Factory.create(StandardSchedulerServer.class);
    mpStationServ = Factory.create(StandardStationServer.class);
    mpPickServ    = Factory.create(StandardPickServer.class);
    mpMoveServ    = Factory.create(StandardMoveServer.class);
    mpPOServ      = Factory.create(StandardPoReceivingServer.class);
    mzPORequiredField = Application.getBoolean("PORequired", false);

                                       // Setup observable to get Load events.
    String vsSelector = getSystemGateway().getLoadEventSelector("");
    getSystemGateway().addObserver(MessageEventConsts.LOAD_EVENT_TYPE, vsSelector + "%", this);

    setResizable(true);
    buildScreen();
                                       // Populate Station combo box and attach
    refreshStationList();              // listener.
    setStationComboListener();

    refreshContainerData();
                                       // If there is only one container type the
    if (mpContainerCombo.isEnabled())  // ComboBox will be disabled.
      setContainerComboListener();
                                       // Text field key listeners.
    setLDFocusListener();
    setLDTextKeyListener();
    setERFocusListener();
    setERTextKeyListener();
                                       // Build table and display data.
    showTableWithDefaultButtons("StoreMain");
    mpTable.setEditableColumn(PurchaseOrderLineData.ACCEPTQUANTITY_NAME);
    buildButtonPanel();
  }

  @Override
  public Dimension getPreferredSize()
  {
    return(new Dimension(960, 480));
  }

  @Override
  public void update(Observable ipObservable, Object ipObj)
  {
    ObservableControllerImpl vpReceivedData = (ObservableControllerImpl)ipObservable;
    String vsReceivedData = vpReceivedData.getStringData();
    String vsCurrentStn = mpStationCombo.getSelectedStation();
    if (vsReceivedData.contains(";" + vsCurrentStn))
    {
      if (mpStoreLoadTextField.getText().trim().length() > 0)
      {                                // If there is a load being processed at
        return;                        // current station, ignore LoadEvent.
      }
      else if (!mzObserverStationCheck)
      {                                // Set "semaphore" that station is being
        mzObserverStationCheck = true; // checked already
        checkForLoadAtStation(vsCurrentStn);
        mzObserverStationCheck = false;
      }
    }
  }

 /**
  *  Method to clean up as needed at closing.
  */
  @Override
  public void cleanUpOnClose()
  {
    getSystemGateway().deleteObserver(MessageEventConsts.LOAD_EVENT_TYPE, this);
    mpLoadServ.cleanUp();
    mpInvServ.cleanUp();
    mpSchedServer.cleanUp();
    mpStationServ.cleanUp();
    mpPickServ.cleanUp();
    mpMoveServ.cleanUp();
    mpPOServ.cleanUp();
  }

  protected void buildScreen()
  {
    mpStationCombo = new StationComboBox();
    mpContainerCombo = new SKDCComboBox();
    mpStoreLoadTextField = new SKDCTextField(DBInfo.getFieldLength(LoadData.LOADID_NAME));
    mpERTextField = new SKDCTextField(DBInfo.getFieldLength(PurchaseOrderLineData.ORDERID_NAME));

    addInput(new SKDCLabel("Station:"), mpStationCombo);
    addInput(new SKDCLabel("Store Load:"), mpStoreLoadTextField);
    addInput(new SKDCLabel("Container Type:"), mpContainerCombo);
    addInput(new SKDCLabel("Expected Receipt ID:"), mpERTextField);
  }

  protected void buildButtonPanel()
  {
    mpButtonPanel.removeAll();

    mpBtnInsertID = new SKDCButton("Add Line", "Add line to receive", 'A');
    mpBtnReceiveLines = new SKDCButton("Receive Lines",
                                       "Receive all lines as shown above.",
                                       'R');
    final SKDCButton vpBtnReleaseLoad = new SKDCButton("Release Load",
                                                       "Release load to be stored.",
                                                        'e');
    final SKDCButton vpBtnReset = new SKDCButton("Reset",
                                                 "Reset to default display.",
                                                 's');
    final SKDCButton vpBtnChangeER = new SKDCButton("Change E.R.",
                                                 "Receive against another Expected Receipt.",
                                                 'C');

    ActionListener vpButtonListener = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        Object vpPressedButton = e.getSource();
        if (vpPressedButton == vpBtnReleaseLoad)
        {
          releaseButtonPressed();
        }
        else if (vpPressedButton == vpBtnReset)
        {
          resetButtonPressed(true);
        }
        else if (vpPressedButton == mpBtnInsertID)
        {
          insertLineButtonPressed();
        }
        else if (vpPressedButton == mpBtnReceiveLines)
        {
          receiveLinesButtonPressed();
        }
        else if (vpPressedButton == vpBtnChangeER)
        {
          newERButtonPressed();
        }
      }
    };

    vpBtnReleaseLoad.addActionListener(vpButtonListener);
    vpBtnChangeER.addActionListener(vpButtonListener);
    vpBtnReset.addActionListener(vpButtonListener);
    mpBtnInsertID.addActionListener(vpButtonListener);
    mpBtnReceiveLines.addActionListener(vpButtonListener);

    mpButtonPanel.add(mpBtnReceiveLines);
    mpButtonPanel.add(vpBtnChangeER);
    mpButtonPanel.add(vpBtnReleaseLoad);
    mpButtonPanel.add(vpBtnReset);
    if (!mzPORequiredField)
      mpButtonPanel.add(mpBtnInsertID);
  }

 /**
  * Method to setup amount full dialog data.  This method exists for extensibility
  * of amount full translations for various projects.
  *
  * @param ipAmtFullList empty List of Strings.  This list is filled in with the
  *        appropriate amount full list potentially varying by project.
  * @return String containing default selection to be used for amount full choice
  *         list.
  * @throws NoSuchFieldException if there is a translation error.
  */
  protected String setupAmountFullDialogData(List<String> ipAmtFullList)
            throws NoSuchFieldException
  {
    LoadData vpLoadData = Factory.create(LoadData.class);
    String vsDefaultSelection = DBTrans.getStringValue(LoadData.AMOUNTFULL_NAME,
                                                       vpLoadData.getAmountFullnessTrans());
    String[] vasAmountFull = DBTrans.getStringList(LoadData.AMOUNTFULL_NAME);
    for(int vnIdx = 0; vnIdx < vasAmountFull.length; vnIdx++)
    {
      ipAmtFullList.add(vasAmountFull[vnIdx]);
    }

    return(vsDefaultSelection);
  }

/*----------------------------------------------------------------------------
                         Button Press handlers
  ----------------------------------------------------------------------------*/
  private void receiveLinesButtonPressed()
  {
    final String vsStoreLoad = mpStoreLoadTextField.getText();
    final int vnDisplayRowCount = mpTable.getRowCount();

    if (vnDisplayRowCount == 0)
    {
      return;
    }
    else if (mpStoreLoadTextField.getText().trim().length() == 0)
    {
      displayError("No Store load provided!");
      mpStoreLoadTextField.requestFocus();
      return;
    }

    new NamedThread(getClass().getSimpleName() + ".receiveLinesButtonPressed")
    {
      @Override
      public void run()
      {
        StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);
        try
        {
          SwingUtilities.invokeAndWait(new Runnable()
          {
            public void run()
            {
              boolean vzRtn = receiveItems(vsStoreLoad, vnDisplayRowCount);
              if (vzRtn)
              {
                refreshIDData();
                appendERData();
              }
            }
          });
        }
        catch(InterruptedException ie) {}
        catch(InvocationTargetException ite) {}
        finally
        {
          vpLoadServ.cleanUp();
        }
      }
    }.start();
  }

  private void newERButtonPressed()
  {
    refreshIDData();
    resetERField();
  }

  private void releaseButtonPressed()
  {
    boolean vzErrorCondition = false;
    String vsStoreLoad = mpStoreLoadTextField.getText();
    try
    {
      if (vsStoreLoad.isEmpty() || !mpLoadServ.loadExists(vsStoreLoad))
      {
        displayInfoAutoTimeOut("No load to release.");
        vzErrorCondition = true;
      }
      else if (mpMoveServ.getMoveCount("", vsStoreLoad, "") > 0)
      {
                                       // if any picks, they must be completed
        displayInfo("Load has more Picks. Use Pick screen to complete work before releasing");
        vzErrorCondition = true;
      }
      else if (isAnyItemNotAccepted())
      {          // displayYesNoPrompt returns true if user presses "YES" button
        vzErrorCondition = !displayYesNoPrompt("There are acceptable items "   +
                                               "that have" + SKDCConstants.EOL_CHAR +
                                               "not been received! Continue");
      }

      if (!vzErrorCondition)
      {
        List<String> vpAmtFullList = new ArrayList<String>();
        String vsDefaultValue = setupAmountFullDialogData(vpAmtFullList);
        String vsAmtFullSelect = (String)JOptionPane.showInputDialog(this,
                                           "Select Amount Full",
                                           "Input", JOptionPane.NO_OPTION, null,
                                           vpAmtFullList.toArray(),
                                           vsDefaultValue);
        if (vsAmtFullSelect != null)
        {
          int vnAmtFullSelect = DBTrans.getIntegerValue(LoadData.AMOUNTFULL_NAME,
                                                        vsAmtFullSelect);
          mpLoadServ.setLoadAmountFull(vsStoreLoad, vnAmtFullSelect);

          StationData vpStationData = mpStationServ.getStation(mpStationCombo.getSelectedStation());
          String vsErrorMsg = mpPickServ.releaseLoad(vsStoreLoad, vpStationData);
          if(vsErrorMsg != null)
          {
            displayError(vsErrorMsg);
          }
          else
          {
            logger.logDebug("Load \"" + vsStoreLoad + "\" released from Station " +
                            mpStationCombo.getText());
            prepareForNextLoad();
            checkForLoadAtStation(vpStationData.getStationName());
          }
        }
      }
    }
    catch(NoSuchFieldException nsf)
    {
      displayError(nsf.getMessage());
    }
    catch(DBException e2)
    {
      displayError("Error releasing Load " + vsStoreLoad + " - " + e2.getMessage());
    }
  }

  private void resetButtonPressed(boolean izResetStation)
  {
    if (izResetStation)
      mpStationCombo.setSelectedIndex(0);

//    setStoreLoadFieldAttr("", true);
    mpERTextField.setText("");
    mpERTextField.setEnabled(true);
    mpTable.clearTable();
    checkForLoadAtStation(mpStationCombo.getSelectedStation());
  }

  private void insertLineButtonPressed()
  {
    AddItemDetail vpAddIDFrame = Factory.create(AddItemDetail.class, mpStoreLoadTextField.getText());

    buildLightWeightCompList();
    enableLightWeightComponents(mpCompList, false);
/*---------------------------------------------------------------------------
   We don't want to use Modal here because it does not respect components
   that are already disabled.  When the Modal frame closes in this case it
   simply enables everything!
  ---------------------------------------------------------------------------*/
    addSKDCInternalFrame(vpAddIDFrame, new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent ipEvt)
      {
        String vsProp = ipEvt.getPropertyName();
        if (vsProp.equals(FRAME_CHANGE))
        {
          Object vpNewObj = ipEvt.getNewValue();
          if (vpNewObj != null && vpNewObj instanceof LoadLineItemData)
          {
            mergeDisplayedList(getDisplayItemDetails(), (LoadLineItemData)vpNewObj);
          }
        }
        else if (vsProp.equals(FRAME_CLOSING))
        {
          enableLightWeightComponents(mpCompList, true);
          mpCompList.clear();
        }
      }
    });
  }

/*----------------------------------------------------------------------------
                          Various listeners.
  ----------------------------------------------------------------------------*/
  private void setStationComboListener()
  {
    mpStationCombo.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          resetButtonPressed(false);
          checkForLoadAtStation(mpStationCombo.getSelectedStation());
        }
      }
    });
  }

  private void setContainerComboListener()
  {
    mpContainerCombo.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent ipItmEvt)
      {
        if (ipItmEvt.getStateChange() == ItemEvent.SELECTED &&
            !mpStoreLoadTextField.getText().trim().isEmpty())
        {
          changeLoadContainer((String)ipItmEvt.getItem());
        }
      }
    });
  }

  private void setERFocusListener()
  {
    mpERTextField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent fe)
      {
        Component vpComp = fe.getOppositeComponent();
        if (vpComp == mpBtnReceiveLines)
        {
          handleERTextFieldData();
        }
      }
    });
  }

  private void setERTextKeyListener()
  {
    mpERTextField.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyTyped(KeyEvent ke)
      {
        if (ke.getKeyChar() == KeyEvent.VK_ENTER)
        {
          handleERTextFieldData();
        }
      }
    });
  }

  private void setLDFocusListener()
  {
    mpStoreLoadTextField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent fe)
      {
        if (mpStoreLoadTextField.isEnabled())
        {
          Component vpComp = fe.getOppositeComponent();
          if (vpComp == mpERTextField || vpComp == mpBtnAddLine)
          {
            handleLoadTextFieldData();
          }
        }
      }
    });
  }

  private void setLDTextKeyListener()
  {
    mpStoreLoadTextField.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyTyped(KeyEvent ke)
      {
        if (ke.getKeyChar() == KeyEvent.VK_ENTER)
        {
          handleLoadTextFieldData();
        }
      }
    });
  }

/*----------------------------------------------------------------------------
                          Misc. Private Methods
  ----------------------------------------------------------------------------*/
  private void handleERTextFieldData()
  {
    String vsStoreLoad = mpStoreLoadTextField.getText();
    String vsStation = mpStationCombo.getSelectedStation();
    if (!mpLoadServ.isLoadAtStation(vsStoreLoad, vsStation))
    {
      displayError("Load " + vsStoreLoad + " is not at this station!");
      return;
    }

    String vsER = mpERTextField.getText().trim();
    if (!vsER.isEmpty())
    {
      if (vsStoreLoad.trim().isEmpty())
      {
        displayError("Store Load must be entered first!");
        mpStoreLoadTextField.requestFocus();
      }
      else if (!mpPOServ.exists(vsER))
      {                          // If they entered an E.R. it better exist.
        displayError("Expected Receipt does not exist!");
      }
      else
      {
        appendERData();
        mpERTextField.setEnabled(false);
      }
    }
    else if (mzPORequiredField)
    {
      displayError("Expected Receipt is required to store items!");
      mpERTextField.requestFocus();
    }
  }

  private void handleLoadTextFieldData()
  {
    try
    {
      String vsStoreLoad = mpStoreLoadTextField.getText();
      String vsStation = mpStationCombo.getSelectedStation();

      if (vsStoreLoad.isEmpty()) return;

      if (!mpLoadServ.loadExists(vsStoreLoad))
      {                                // The following method only adds a load at
                                       // this station if its settings allow it.
        boolean vzRtn = mpSchedServer.createNewLoadAtStation(vsStoreLoad,
            vsStation, true);
        if (!vzRtn)
        {
          displayError("Load " + vsStoreLoad + " not created at " + SKDCConstants.EOL_CHAR +
                       "station " + vsStation + ". Check station parameters " + SKDCConstants.EOL_CHAR +
                       "for incorrect settings.");
        }
        else
        {
          showLoadContainerType(vsStoreLoad);
          setStoreLoadFieldAttr(vsStoreLoad, false);
          mpERTextField.requestFocus();
        }
      }
      else if (!mpLoadServ.isLoadAtStation(vsStoreLoad, vsStation))
      {
        displayError("Load " + vsStoreLoad + " is not at this station!");
      }
      else
      {
        refreshIDData();
        setStoreLoadFieldAttr(vsStoreLoad, false);
        mpERTextField.requestFocus();
      }
    }
    catch(DBException exc)
    {
    }
  }

 /**
  * Method to populate the station combo box.
  */
  private void refreshStationList()
  {
    new NamedThread(getClass().getSimpleName() + ".refreshStationList")
    {
      @Override
      public void run()
      {
        StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);
        int[] vanInputStns = new int[] { DBConstants.USHAPE_OUT,
                                         DBConstants.PDSTAND,
                                         DBConstants.REVERSIBLE,
                                         DBConstants.INPUT };
        try
        {
          final Map vpStnToDescMap = vpStnServ.getStationsByStationType(vanInputStns);
          vpStnServ.cleanUp();

          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              mpStationCombo.setStoreOpType();
              mpStationCombo.setComboBoxData(vpStnToDescMap.keySet().toArray());
            }
          });
        }
        catch(DBException e)
        {
          displayError("Unable to get Stations");
        }
      }
    }.start();
  }

 /**
  * Method to populate the Container combo box.
  */
  private void refreshContainerData()
  {
    new NamedThread(getClass().getSimpleName() + ".refreshContainerData")
    {
      @Override
      public void run()
      {
        try
        {
          StandardInventoryServer vpInvServ = Factory.create(StandardInventoryServer.class);
          StandardStationServer vpStationServ = Factory.create(StandardStationServer.class);
          final StationData stationData = vpStationServ.getStation(mpStationCombo.getSelectedStation());
          final List<String> vpContList = vpInvServ.getContainerTypeList();
          vpInvServ.cleanUp();

          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {

              mpContainerCombo.setComboBoxData(vpContList);
              if (vpContList.size() == 1)
                mpContainerCombo.setEnabled(false);
              else if (stationData != null)
                mpContainerCombo.setSelectedItem(stationData.getContainerType());
            }
          });
        }
        catch (DBException dbe)
        {
          logAndDisplayException(dbe);
        }
      }
    }.start();
  }

  private void refreshIDData()
  {
    new NamedThread(getClass().getSimpleName() + ".refreshIDData")
    {
      @Override
      public void run()
      {
        StandardInventoryServer vpInvServ = Factory.create(StandardInventoryServer.class);
        final List<Map> vpList = vpInvServ.getStoreScreenDataList(mpStoreLoadTextField.getText());
        vpInvServ.cleanUp();
        if (!vpList.isEmpty())
        {
          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              mpTable.refreshData(vpList);
              if (mzPORequiredField)
              {                        // Limit what can be edited to ER rows.
                mpTable.setNonEditableRowsByColumnPattern(LoadLineItemData.DATATYPE_NAME,
                                                          LoadLineItemData.SCREEN_DATA_MNEMONIC);
              }
              mpTable.clearSelection();
            }
          });
        }
      }
    }.start();
  }

  private void appendERData()
  {
    new NamedThread(getClass().getSimpleName() + ".appendERData")
    {
      @Override
      public void run()
      {
        StandardPoReceivingServer vpPOServ = Factory.create(StandardPoReceivingServer.class);
        final List<Map> vpELList = vpPOServ.getReceivablePurchaseOrderLines(mpERTextField.getText());
        vpPOServ.cleanUp();
        if (vpELList.isEmpty())
        {
          resetERField();
        }
        else
        {
          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              mpERTextField.setEnabled(false);
              for(Map vpMap : vpELList)
              {
                mpTable.appendRow(vpMap);
              }
            }
          });
        }
      }
    }.start();
  }

  private void changeLoadContainer(final String isContainerType)
  {
    new NamedThread(getClass().getSimpleName() + ".changeLoadContainer")
    {
      @Override
      public void run()
      {
        StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);
        try
        {
          vpLoadServ.updateLoadContainerType(mpStoreLoadTextField.getText(),
                                             isContainerType);
        }
        catch(DBException e)
        {
          displayError("Container Type unchanged. " + e.getMessage());
        }
        finally
        {
          vpLoadServ.cleanUp();
        }
      }
    }.start();
  }

  private void showLoadContainerType(String isLoadID) throws DBException
  {
    if (mpContainerCombo.getComponentCount() > 1)
    {
      String vsContType = mpLoadServ.getLoadContainerType(isLoadID);
      if (!vsContType.isEmpty())
      {
        mpContainerCombo.selectItemBy(vsContType);
      }
    }
  }

  private void prepareForNextLoad()
  {
    mpERTextField.setText("");
    mpERTextField.setEnabled(true);
    mpTable.clearTable();
  }

 /**
  * Gets a list of currently displayed item details.
  * @return List of displayed item details.
  */
  private List<Map> getDisplayItemDetails()
  {
    List<Map> vpTableList = mpTable.getTableData();
    List<Map> vpDispIDList = new ArrayList<Map>();

    for(Map vpMap : vpTableList)
    {
      String vsDataType = DBHelper.getStringField(vpMap, LoadLineItemData.DATATYPE_NAME);
      if (vsDataType.equals(LoadLineItemData.SCREEN_DATA_MNEMONIC))
      {
        vpDispIDList.add(vpMap);
      }
    }

    return(vpDispIDList);
  }

 /**
  * Merges any existing lines in the display with the new item detail.
  * @param ipOrigList the list of displayed data.
  * @param ipLLIData the newly added item detail.
  */
  private void mergeDisplayedList(List<Map> ipOrigList, LoadLineItemData ipLLIData)
  {
    boolean vzMergeOccurred = false;

    for(ListIterator<Map> vpListIter = ipOrigList.listIterator(); !vzMergeOccurred && vpListIter.hasNext();)
    {
      Map vpMap = vpListIter.next();
      String vsOrigItem = DBHelper.getStringField(vpMap, PurchaseOrderLineData.ITEM_NAME).trim();
      String vsOrigLot = DBHelper.getStringField(vpMap, PurchaseOrderLineData.LOT_NAME).trim();
      String vsOrigPosition = DBHelper.getStringField(vpMap, LoadLineItemData.POSITIONID_NAME).trim();

      String vsIDItem = ipLLIData.getItem();
      String vsIDLot = ipLLIData.getLot();
      String vsIDPosition = ipLLIData.getPositionID();

      if (vsOrigItem.equals(vsIDItem) && vsOrigLot.equals(vsIDLot) &&
          vsOrigPosition.equals(vsIDPosition))
      {
        double vdIDCurrQty = ipLLIData.getCurrentQuantity();
        double vdOrigRcvdQty = DBHelper.getDoubleField(vpMap, PurchaseOrderLineData.RECEIVEDQUANTITY_NAME);
        double vdOrigAcceptQty = DBHelper.getDoubleField(vpMap, PurchaseOrderLineData.ACCEPTQUANTITY_NAME);

        vpMap.put(PurchaseOrderLineData.RECEIVEDQUANTITY_NAME, Double.valueOf(vdOrigRcvdQty));
        vpMap.put(PurchaseOrderLineData.ACCEPTQUANTITY_NAME, Double.valueOf(vdOrigAcceptQty + vdIDCurrQty));
        vzMergeOccurred = true;
      }
    }

    if (!vzMergeOccurred)
    {
      Map<String, Object> vpMap = new TreeMap<String, Object>();
      vpMap.put(LoadLineItemData.DATATYPE_NAME, LoadLineItemData.SCREEN_DATA_MNEMONIC);
      vpMap.put(LoadLineItemData.ITEM_NAME, ipLLIData.getItem());
      vpMap.put(LoadLineItemData.LOT_NAME, ipLLIData.getLot());
      vpMap.put(LoadLineItemData.POSITIONID_NAME, ipLLIData.getPositionID());
      vpMap.put(PurchaseOrderLineData.EXPECTEDQUANTITY_NAME, Double.valueOf(0.0));
      vpMap.put(PurchaseOrderLineData.RECEIVEDQUANTITY_NAME, Double.valueOf(0.0));
      vpMap.put(LoadLineItemData.ACCEPTQUANTITY_NAME, Double.valueOf(ipLLIData.getCurrentQuantity()));
      mpTable.appendRow(vpMap);
    }
    else
    {
      mpTable.refreshTable();
    }
  }

  private void buildLightWeightCompList()
  {
    if (mpStationCombo.isEnabled()) mpCompList.add(mpStationCombo);
    if (mpContainerCombo.isEnabled()) mpCompList.add(mpContainerCombo);
    if (mpERTextField.isEnabled()) mpCompList.add(mpERTextField);
    mpCompList.add(mpTable);
    mpCompList.add(mpButtonPanel);
  }

  private String checkForLoadAtStation(String isStation)
  {
    StandardLoadServer vpLoadServ;
    StandardPoReceivingServer vpPOServ;
    String vsStationLoad = null;
    String vsStnWhs = mpStationServ.getStationWarehouse(isStation);

    try
    {
      int vnStationType = mpStationServ.getStationType(isStation);
      if (vnStationType == DBConstants.USHAPE_OUT ||
          vnStationType == DBConstants.PDSTAND    ||
          vnStationType == DBConstants.REVERSIBLE ||
          vnStationType == DBConstants.INPUT)
      {
        vpLoadServ = Factory.create(StandardLoadServer.class);
        vpPOServ = Factory.create(StandardPoReceivingServer.class);

        LoadData vpLoadData = vpLoadServ.getOldestLoad(vsStnWhs, isStation, DBConstants.ARRIVED);
        if (vpLoadData != null)
        {
          vsStationLoad = vpLoadData.getLoadID();
          setStoreLoadFieldAttr(vsStationLoad, false);
          showLoadContainerType(vsStationLoad);

          if (mpInvServ.getLoadLineCount(vsStationLoad) > 0)
          {
            refreshIDData();

          }
          if (vpPOServ.exists(vsStationLoad))
          {
            mpERTextField.setText(vsStationLoad);
            appendERData();
          }
        }
        else
        {
          setStoreLoadFieldAttr("", true);
          mpStoreLoadTextField.requestFocus();
        }
      }
    }
    catch(DBException exc)
    {
      displayError("Error finding load at location " + vsStnWhs + "-" + isStation);
    }

    return(vsStationLoad);
  }

 /**
  * Method does the work of receiving items into a load.
  *
  * @param isStoreLoad the store load.
  * @param vnLineCount the number of lines to receive.
  */
  private boolean receiveItems(String isStoreLoad, int vnLineCount)
  {
    boolean vzItemsReceived = false;
    double vdAcceptQty = 0, vdRecvdQty = 0, vdExpdQty = 0;
    StandardPoReceivingServer vpPOServ = Factory.create(StandardPoReceivingServer.class);
    StandardInventoryServer vpInvServ = Factory.create(StandardInventoryServer.class);
    LoadLineItemData vpLineData = Factory.create(LoadLineItemData.class);

    stopReceipt:
    for(int vnIdx = 0; vnIdx < vnLineCount; vnIdx++)
    {
      mpTable.commitEdit(vnIdx, PurchaseOrderLineData.ACCEPTQUANTITY_NAME);

      String vsItem = "";
      try
      {
        Map<String, Object> vpRowMap = mpTable.getRowData(vnIdx);
//        String vsExpectedReceipt = mpERTextField.getText();
        String vsExpectedReceipt = DBHelper.getStringField(vpRowMap, PurchaseOrderLineData.ORDERID_NAME);
        String vsPOLot = DBHelper.getStringField(vpRowMap, PurchaseOrderLineData.LOT_NAME);
        String vsPosition = DBHelper.getStringField(vpRowMap, LoadLineItemData.POSITIONID_NAME);
        String vsDataType = DBHelper.getStringField(vpRowMap, PurchaseOrderLineData.DATATYPE_NAME);
        vsItem = DBHelper.getStringField(vpRowMap, PurchaseOrderLineData.ITEM_NAME);
        vdExpdQty = DBHelper.getDoubleField(vpRowMap,
                                            PurchaseOrderLineData.EXPECTEDQUANTITY_NAME);
        vdRecvdQty = DBHelper.getDoubleField(vpRowMap,
                                             PurchaseOrderLineData.RECEIVEDQUANTITY_NAME);
                                       // Qty. that can still be received.
        vdAcceptQty = DBHelper.getDoubleField(vpRowMap,
                                             PurchaseOrderLineData.ACCEPTQUANTITY_NAME);
        if (vdAcceptQty > 0)
        {
          if (vsDataType.equals(PurchaseOrderLineData.SCREEN_DATA_MNEMONIC))
          {
            if (vdAcceptQty > (vdExpdQty - vdRecvdQty))
            {
              vdAcceptQty = vdExpdQty - vdRecvdQty;
              displayWarning("Store Quantity cannot exceed Acceptable Quantity." + SKDCConstants.EOL_CHAR +
                             "Item receipt for " + vsItem + " adjusted to " + vdAcceptQty);
              if (vdAcceptQty == 0) break stopReceipt;
            }
            vpPOServ.receivePOLine(vsExpectedReceipt, isStoreLoad, vsItem, vsPOLot,
                                   "", vdAcceptQty, new Date(), new Date());
            vzItemsReceived = true;
          }
          else
          {                            // Make sure Item Master exists.
            if (!vpInvServ.itemMasterExists(vsItem))
            {
              displayInfoAutoTimeOut("Item master for \"" + vsItem + "\" added.");
              vpInvServ.addDefaultItem(vsItem);
            }
            vpLineData.clear();
            vpLineData.setLoadID(isStoreLoad);
            vpLineData.setItem(vsItem);
            vpLineData.setLot(vsPOLot);
            vpLineData.setPositionID(vsPosition);
            vpLineData.setCurrentQuantity(vdAcceptQty);
            vpInvServ.addLoadLIWithValidation(vpLineData, MessageOutNames.STORE_COMPLETE);
            vzItemsReceived = true;
          }
        }
      }
      catch(DBException ex)
      {
        displayError("Item " + vsItem + " not received! " + ex.getMessage());
      }
    } // End for-loop
    vpPOServ.cleanUp();

    return(vzItemsReceived);
  }

  private void setStoreLoadFieldAttr(String isStoreFieldText, boolean izEnabled)
  {
    mpStoreLoadTextField.setText(isStoreFieldText);
    mpStoreLoadTextField.setEnabled(izEnabled);
    mpBtnInsertID.setEnabled(!izEnabled);
  }

 /**
  * Method checks if there are any displayed items that have acceptable quantities
  * that have not been received.
  * @return <code>true</code> if there are any rows with quantites that have
  *         <b>not</b> been accepted (received). <code>false</code> otherwise.
  */
  private boolean isAnyItemNotAccepted()
  {
    boolean vzRtn = false;
    List<Map> vpList = mpTable.getTableData();
    for(Map vpRowMap : vpList)
    {
      double vdAcceptQty = DBHelper.getDoubleField(vpRowMap,
                                     PurchaseOrderLineData.ACCEPTQUANTITY_NAME);
      if (vdAcceptQty > 0)
      {
        vzRtn = true;
        break;
      }
    }

    return(vzRtn);
  }

  private void resetERField()
  {
    mpERTextField.setText("");
    mpERTextField.setEnabled(true);
    mpERTextField.requestFocus();
  }
}
