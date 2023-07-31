package com.daifukuamerica.wrxj.swingui.station;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

@SuppressWarnings("serial")
public class StationParameterMain extends DacInputFrame 
{
  protected StationData mpCurrentStationData;
  protected boolean mzIsStoringStation = false;

  // Screen fields
  protected StationComboBox mpStationCombo;
  protected DacComboPanel mpStationMode;
  protected DacComboPanel mpStationOrderStatus;
  protected DacComboPanel mpAllocStrategy;
  protected DacComboPanel mpBidirectionalMode;
  protected DacComboPanel mpAutoOrder;
  protected DacComboPanel mpAutoStore;
  protected DacComboPanel mpCCIAllowed;
  protected ItemNumberInput mpItemInput;
  protected SKDCTextField mpItemDesc;
  protected SKDCDoubleField mpTxtOrderQty;
  protected SKDCComboBox mpContainerTypeCombo;
  protected SKDCTranComboBox mpAmountFullCombo;
  protected SKDCTextField mpPrinter;

  // Servers
  protected StandardStationServer   mpStnServer;
  protected StandardInventoryServer mpInvServer;
  
  protected int[] captiveStationChoice = { DBConstants.CAPTIVEINSERT,
                                         DBConstants.STORERETRIEVE,
                                         DBConstants.STNOFFLINE };
  protected int[] nonCaptiveStationChoice = { DBConstants.STORERETRIEVE,
                                            DBConstants.STNOFFLINE };
  
  /**
   * Constructor
   * @throws Exception
   */
  public StationParameterMain()
  {
    super("Station Parameters", "Modify Station Parameters");

    /*
     * This keeps the panel from changing size when different options are
     * selected, but the pieces still move around.  I think I'd like it better
     * if we enabled/disabled inputs instead. 
     */
//    mpInputPanel.setPreferredSize(new Dimension(678,325));
    
    mpStnServer = Factory.create(StandardStationServer.class, "StationParameterPanel");
    mpInvServer = Factory.create(StandardInventoryServer.class, "StationParameterPanel");

    initDisplayColumns();

    showItemTypeFields(false);
    showContainerTypeFields(false);
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
    initStationSelectEvent();
  }

  /**
   *  Method to execute the modify request.  This method does all necessary
   *  validations.
   */
  @Override
  protected void okButtonPressed()
  {
    StationData vpSD = Factory.create(StationData.class);

    try
    {
      vpSD = getStationData();
    }
    catch(NoSuchFieldException noField)
    {
      JOptionPane.showMessageDialog(null, noField.getMessage(), 
          "Translation Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, exc.getMessage(), "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    // No columns specified for update!  Just return.
    if (vpSD.getColumnCount() == 0)
    {
      displayInfoAutoTimeOut("No Station selected for modification");
      return;
    }
    String sStationName = mpStationCombo.getSelectedStation();
    vpSD.setKey(StationData.STATIONNAME_NAME, sStationName);
    try
    {
      mpStnServer.modifyStationRecord(vpSD);
      displayInfoAutoTimeOut("Station " + sStationName + " modified");

      /*
       * If this is a reversible station, update the SRC.
       * 
       * Actually, due to weird logic, the SRC reports retrieve mode when the
       * station has a captive load to store after a retrieval.  So if there 
       * is a Store Pending or Move Pending load at the station, just leave it
       * in store mode.
       */
      if (vpSD.getStationType() == DBConstants.REVERSIBLE ||
          vpSD.getStationType() == DBConstants.PDSTAND)
      {
        StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
        LoadData vpSPLoad = vpLoadServer.getOldestLoadData(
            vpSD.getStationName(), DBConstants.STOREPENDING);
        LoadData vpMPLoad = vpLoadServer.getOldestLoadData(
            vpSD.getStationName(), DBConstants.MOVEPENDING);

        if (vpSD.getBidirectionalStatus() != DBConstants.STOREMODE ||
            (vpSPLoad == null && vpMPLoad == null))
        {
          mpStnServer.sendBiDirectionalChangeCommand(vpSD);
        }
      }
    }
    catch(DBException modExc)
    {
      JOptionPane.showMessageDialog(null, modExc.getMessage(), "Update Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Get the StationData with updates from the screen
   * 
   * @return
   * @throws DBException
   * @throws NoSuchFieldException
   */
  protected StationData getStationData() throws DBException, NoSuchFieldException
  {
    if (mpStationMode.isSelected())
      mpCurrentStationData.setStatus(mpStationMode.getIntegerValue());

    if (mpStationOrderStatus.isSelected())
      mpCurrentStationData.setOrderStatus(mpStationOrderStatus.getIntegerValue());

    if (mpAllocStrategy.isSelected())
      mpCurrentStationData.setAllocationType(mpAllocStrategy.getText());

    if (mpBidirectionalMode.isSelected())
      mpCurrentStationData.setBidirectionalStatus(mpBidirectionalMode.getIntegerValue());

    int vnType = -1;
    if (mpAutoOrder.isSelected())
    {
      vnType = mpAutoOrder.getIntegerValue();
      mpCurrentStationData.setAutoOrderType(vnType);
    }
    if (mpAutoStore.isSelected())
    {
      vnType = mpAutoStore.getIntegerValue();
      mpCurrentStationData.setAutoLoadMovementType(vnType);
    }

    if (mpCCIAllowed.isSelected())
        mpCurrentStationData.setCCIAllowed(mpCCIAllowed.getIntegerValue());

    
    mpCurrentStationData.setPrinter(mpPrinter.getText());
    
    switch(vnType)
    {
      case DBConstants.AUTORECEIVE_LOAD:
      case DBConstants.EMPTY_CONTAINER_ORDER:
        mpCurrentStationData.setContainerType(mpContainerTypeCombo.getText());
        mpCurrentStationData.setAmountFull(mpAmountFullCombo.getIntegerValue());
        break;

      case DBConstants.AUTORECEIVE_ITEM:
      case DBConstants.ITEM_ORDER:
        String sItem = mpItemInput.getText();
        double fOrderQuantity = mpTxtOrderQty.getValue();
        if (sItem.length() > 0)
        {
          /*
           * If they typed in an item, the item should exist, and the order qty.
           * better be greater than 0.
           */
          if (!mpInvServer.itemMasterExists(sItem))
          {
            throw new DBException("Item " + sItem + " does not exist");
          }
          else if (mpTxtOrderQty.getValue() == 0.0)
          {
            throw new DBException("The order quantity must be greater than 0");
          }
        }
        mpCurrentStationData.setItem(sItem);
        mpCurrentStationData.setOrderQuantity(fOrderQuantity);
        break;
    }

    return mpCurrentStationData;
  }

  /**
   * Method initializes display columns for this Panel.
   * 
   * @return <code>Map</code> of column data.
   */
  protected void initDisplayColumns()
  {
    defineFields();

    addInput("Station:", mpStationCombo);
    addInput("Station Mode:", mpStationMode);
    addInput("Station Order Activation:", mpStationOrderStatus);
    addInput("Allocation Strategy:", mpAllocStrategy);
    addInput("Bidirectional Mode:", mpBidirectionalMode);
    addInput("Auto-Order Type:", mpAutoOrder);
    addInput("Auto-Store Type:", mpAutoStore);
    addInput("Fixed Item:", mpItemInput);
    addInput("Item Description:", mpItemDesc);
    addInput("Fixed Quantity:", mpTxtOrderQty);
    addInput("Container Type:", mpContainerTypeCombo);
    addInput("Amount Full:", mpAmountFullCombo);
    addInput("Cycle Count Allowed:", mpCCIAllowed);
    addInput("Printer:", mpPrinter);

    setStationListeners();
    setAutoOrderListeners();
    setAutoStoreListeners();
    setBiDirectionalListeners();
  }

  /**
   * Method to initialize station selection. One of the peculiarities of
   * JComboBox is that it will not fire an initial Item Selected event when it's
   * first populated. As a result any screen field that depends on this event
   * for populating themselves will not work on the first try when the screen is
   * brought up! This method piggy-backs off the internal frame open event to
   * simulate an item change event.
   */
  protected void initStationSelectEvent()
  {
    String vsStationName = mpStationCombo.getSelectedStation();
    processStationSelectEvent(vsStationName);
  }

  /**
   * Initialize screen fields
   */
  protected void defineFields()
  {
    try
    {
      int [] vanStns = DBTrans.getIntegerList(StationData.STATIONTYPE_NAME);
      Map<String,String> vpStationMap = mpStnServer.getStationsByStationType(vanStns);
      mpStationCombo = new StationComboBox(vpStationMap.keySet().toArray());
      mpContainerTypeCombo = new SKDCComboBox(mpInvServer.getContainerTypeList());
      mpAmountFullCombo = new SKDCTranComboBox(StationData.AMOUNTFULL_NAME, false);
      mpAmountFullCombo.removeItem("Full");

      mpStationMode = new DacComboPanel(StationData.STATUS_NAME, nonCaptiveStationChoice);
      mpStationOrderStatus = new DacComboPanel(StationData.ORDERSTATUS_NAME);
      mpAllocStrategy = new DacComboPanel(mpStnServer.getAllocationTypes());
      mpBidirectionalMode = new DacComboPanel(
          StationData.BIDIRECTIONALSTATUS_NAME, 
          new int[] { DBConstants.RETRIEVEMODE, DBConstants.STOREMODE });
      mpAutoOrder = new DacComboPanel(StationData.AUTOORDERTYPE_NAME);
      mpAutoStore = new DacComboPanel(StationData.AUTOLOADMOVEMENTTYPE_NAME);
      mpCCIAllowed = new DacComboPanel( StationData.CCIALLOWED_NAME, 
    		  new int[] { DBConstants.YES, DBConstants.NO } );
    }
    catch(NoSuchFieldException ne)
    {
      JOptionPane.showMessageDialog(null, ne.getMessage(), "Choice List Error",
          JOptionPane.ERROR_MESSAGE);
    }
    catch(DBException dbexc)
    {
      JOptionPane.showMessageDialog(null, dbexc.getMessage(), "Choice List Error",
          JOptionPane.ERROR_MESSAGE);
    }
    mpItemInput = new ItemNumberInput(mpInvServer, false, false);
    mpItemDesc = new SKDCTextField(ItemMasterData.DESCRIPTION_NAME);
    mpItemDesc.setEnabled(false);
    mpItemInput.linkDescription(mpItemDesc);
    mpTxtOrderQty = new SKDCDoubleField(0.0, DBInfo.getFieldLength(OrderLineData.ORDERQUANTITY_NAME));
    mpPrinter = new SKDCTextField(StationData.PRINTER_NAME);
  }

  /*==========================================================================
    Private Methods go in this section.
  ==========================================================================*/
    
  /**
   * Show item order related fields
   * 
   * @param izShowIt
   */
  protected void showItemTypeFields(boolean izShowIt)
  {
    setInputVisible(mpItemInput, izShowIt);
    setInputVisible(mpItemDesc, izShowIt);
    setInputVisible(mpTxtOrderQty, izShowIt);
    
    if (izShowIt)
    {
      mpItemInput.setSelectedItem(mpCurrentStationData.getItem());
      mpTxtOrderQty.setValue(mpCurrentStationData.getOrderQuantity());
    }
  }
    
  /**
   * Show empty container order related fields
   * 
   * @param izShowIt
   */
  protected void showContainerTypeFields(boolean izShowIt)
  {
    setInputVisible(mpContainerTypeCombo, izShowIt);
    setInputVisible(mpAmountFullCombo, izShowIt);
    if (izShowIt)
    {
      mpContainerTypeCombo.setSelectedItem(mpCurrentStationData.getContainerType());
      try
      {
        mpAmountFullCombo.setSelectedElement(mpCurrentStationData.getAmountFull());
      }
      catch (NoSuchFieldException nfe)
      {
        nfe.printStackTrace();
      }
    }
  }

  /**
   * Enable/disable auto-ordering fields
   * 
   * @param izEnableIt
   */
  protected void enableAutoOrderStoreExpandedSet(boolean izEnableIt)
  {
    setInputEnabled(mpItemInput, false);
    setInputEnabled(mpItemDesc, false);
    setInputEnabled(mpTxtOrderQty, false);
    
    setInputEnabled(mpContainerTypeCombo, false);
    setInputEnabled(mpAmountFullCombo, false);
    
    if (mpCurrentStationData != null)
    {
      int vnSelection = 0;
      if (!mzIsStoringStation)
      {
        if (mpCurrentStationData.getAutoOrderType() == DBConstants.AUTO_ORDER_OFF)
          try {vnSelection = mpAutoOrder.getIntegerValue(); }
        catch(NoSuchFieldException nfe){}
        else
          vnSelection = mpCurrentStationData.getAutoOrderType();
      }
      else
      {
        try {vnSelection = mpAutoStore.getIntegerValue(); }
        catch(NoSuchFieldException nfe){}
      }

      switch(vnSelection)
      {
        case DBConstants.AUTORECEIVE_ITEM:
        case DBConstants.ITEM_ORDER:
          setInputEnabled(mpItemInput, izEnableIt);
          setInputEnabled(mpItemDesc, izEnableIt);
          mpItemDesc.setEnabled(false); // description field is always disabled
          setInputEnabled(mpTxtOrderQty, izEnableIt);
          break;

        case DBConstants.AUTORECEIVE_LOAD:
        case DBConstants.EMPTY_CONTAINER_ORDER:
          setInputEnabled(mpContainerTypeCombo, izEnableIt);
          setInputEnabled(mpAmountFullCombo, izEnableIt);
          break;
      }
    }
  }
    
  /*========================================================================*/
  /* EVENT LISTENERS                                                        */
  /*========================================================================*/
  
  /**
   * Add extra listeners to the StationCombo field
   */
  protected void setStationListeners()
  {
    mpStationCombo.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          if (e.getStateChange() == ItemEvent.SELECTED)
          {
            processStationSelectEvent(mpStationCombo.getSelectedStation());
          }
        }
      });    
  }

  /**
   * Add extra listeners to the AutoOrder field
   */
  protected void setAutoOrderListeners()
  {
    mpAutoOrder.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          if (e.getStateChange() == ItemEvent.SELECTED)
          {
            try
            {
              String vsType = (String)e.getItem();
              int vnAutoType = DBTrans.getIntegerValue(
                StationData.AUTOORDERTYPE_NAME, vsType);
              processAutoOrderStoreSelectEvent(vnAutoType);
            }
            catch (NoSuchFieldException ne)
            {
              ne.printStackTrace();
              return;
            }
          }
        }
      });
    mpAutoOrder.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          enableAutoOrderStoreExpandedSet(mpAutoOrder.isSelected());
        }
      });
  }
    
  /**
   * Add extra listeners to the AutoStore field
   */
  protected void setAutoStoreListeners()
  {
    mpAutoStore.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          if (e.getStateChange() == ItemEvent.SELECTED)
          {
            try
            {
              String vsType = (String)e.getItem();
              int vnAutoType = DBTrans.getIntegerValue(StationData.AUTOLOADMOVEMENTTYPE_NAME,
                  vsType);
              processAutoOrderStoreSelectEvent(vnAutoType);
            }
            catch(NoSuchFieldException ne)
            {
              ne.printStackTrace();
              return;
            }
          }
        }
      });
    
    mpAutoStore.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        enableAutoOrderStoreExpandedSet(mpAutoStore.isSelected());
      }
    });
  }
    
  /**
   * Method to update the screen when a different bidirectional mode is
   * selected. When store mode is selected, it should display the auto-store
   * fields. When retrieve mode is chosen, it should display the
   * auto-load-movement fields.
   */
  protected void setBiDirectionalListeners()
  {
    mpBidirectionalMode.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          if (e.getStateChange() == ItemEvent.SELECTED)
          {
            try
            {
              String vsMode = (String)e.getItem();
              int vnMode = DBTrans.getIntegerValue(
                StationData.BIDIRECTIONALSTATUS_NAME, vsMode);
              mpCurrentStationData.setBidirectionalStatus(vnMode);
              if (vnMode == DBConstants.STOREMODE)
              {
                mzIsStoringStation = true;
              }
              if (mzIsStoringStation)
              {
                processAutoOrderStoreSelectEvent(mpCurrentStationData.getAutoLoadMovementType());
              }
              else
              {
                processAutoOrderStoreSelectEvent(mpCurrentStationData.getAutoOrderType());
              }
            }
            catch(NoSuchFieldException ne)
            {
              ne.printStackTrace();
              return;
            }
          }
        }
      });
  }

  /**
   *  Method populates the Station mode combo box with the correct
   *  data depending on if the selected station is a Captive station.
   */
  private void processStationSelectEvent(String selectedStation)
  {
    mpCurrentStationData = mpStnServer.getStation(selectedStation);
    if (mpCurrentStationData == null)
    {
      JOptionPane.showInternalMessageDialog(this, "No Stations are defined!",
          "Data Dependency", JOptionPane.ERROR_MESSAGE);
      return;                                            
    }

    try
    {
      updateComboBoxes();
      if (mzIsStoringStation)
      {
        processAutoOrderStoreSelectEvent(mpCurrentStationData.getAutoLoadMovementType());
      }
      else
      {
        processAutoOrderStoreSelectEvent(mpCurrentStationData.getAutoOrderType());
      }
    }
    catch(NoSuchFieldException nf)
    {
      String errMesg = nf.getMessage() + "::: Selected " + selectedStation;
      JOptionPane.showInternalMessageDialog(this, errMesg, "Translation",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Update combo boxes
   *  
   * @throws NoSuchFieldException
   */
  protected void updateComboBoxes() throws NoSuchFieldException
  {
    // inform listeners that changes may be occurring
    int[] theTranCombo = ((mpCurrentStationData.getCaptive() == DBConstants.CAPTIVE ||
        mpCurrentStationData.getCaptive() == DBConstants.SEMICAPTIVE)) ?
            captiveStationChoice : nonCaptiveStationChoice;
    mpStationMode.setComboBoxData(StationData.STATUS_NAME, theTranCombo);
    mpStationMode.setSelectedElement(mpCurrentStationData.getStatus());
    mpPrinter.setText(mpCurrentStationData.getPrinter());

    // Hide everything then only show the ones we need
    hideAll();
    mzIsStoringStation = false;
    int vnType = mpCurrentStationData.getStationType();
    switch(vnType)
    {
      case DBConstants.REVERSIBLE:
        setInputVisible(mpBidirectionalMode, true);
        mpBidirectionalMode.setSelectedElement(mpCurrentStationData.getBidirectionalStatus());
        if (mpCurrentStationData.getBidirectionalStatus() == DBConstants.STOREMODE)
        {
          mzIsStoringStation = true;
        }
      case DBConstants.PDSTAND:
      case DBConstants.OUTPUT:
      case DBConstants.USHAPE_OUT:
      case DBConstants.TRANSFER_STATION:
        setInputVisible(mpStationOrderStatus, true);
        setInputVisible(mpAllocStrategy, true);
        setInputVisible(mpAutoOrder, true);
        setInputVisible(mpCCIAllowed, true);
        setInputVisible(mpPrinter, true);
        mpStationOrderStatus.setSelectedElement(mpCurrentStationData.getOrderStatus());
        mpAllocStrategy.setSelectedItem(mpCurrentStationData.getAllocationType());
        mpAutoOrder.setSelectedElement(mpCurrentStationData.getAutoOrderType());
        mpCCIAllowed.setSelectedElement(mpCurrentStationData.getCCIAllowed());
        break;
      case DBConstants.INPUT:
        mzIsStoringStation = true;
        break;
    }

    if (mzIsStoringStation)
    {
      setInputVisible(mpAutoStore, true);
      setInputVisible(mpAutoOrder, false);
      setInputVisible(mpPrinter, false);
      mpAutoStore.setSelectedElement(mpCurrentStationData.getAutoLoadMovementType());
    }

    enableAutoOrderStoreExpandedSet(mpAutoOrder.isSelected() ||
        mpAutoStore.isSelected());
  }

  /**
   * Hide all optional inputs
   */
  protected void hideAll()
  {
    setInputVisible(mpStationOrderStatus, false);
    setInputVisible(mpAllocStrategy, false);
    setInputVisible(mpBidirectionalMode, false);
    setInputVisible(mpAutoOrder, false);
    setInputVisible(mpAutoStore, false);
    setInputVisible(mpCCIAllowed, false);
    setInputVisible(mpPrinter, false);
  }

  /**
   * Method determines which pair of fields to show or hide depending on the
   * AutoOrderType selection. The two pairs of fields are Item and Quantity if
   * the auto-order type is Item, or else Container and Amount Full if the
   * auto-order type is Container Type.
   */
  private void processAutoOrderStoreSelectEvent(int inAutoType)
  {
    switch (inAutoType)
    {
      case DBConstants.AUTORECEIVE_ITEM:
      case DBConstants.ITEM_ORDER:
        showItemTypeFields(true);
        showContainerTypeFields(false);
        break;

      case DBConstants.AUTORECEIVE_LOAD:
      case DBConstants.EMPTY_CONTAINER_ORDER:
        showItemTypeFields(false);
        showContainerTypeFields(true);
        break;

      default:
        showItemTypeFields(false);
        showContainerTypeFields(false);
    }
    // Only the checkbox being checked will allow mods. to these elements.
    enableAutoOrderStoreExpandedSet(mpAutoOrder.isSelected()
        || mpAutoStore.isSelected());
  }

  /*========================================================================*/
  
  /**
   * <P><B>Description:</B> Combines a ComboBox and a Modify CheckBox</P>
   * 
   * TODO: If this can be used anywhere else, separate it out and put it in the swing package
   *
   * @author       mandrus<BR>
   * @version      1.0
   * 
   * <BR>Copyright (c) 2008 by Daifuku America Corporation
   */
  public class DacComboPanel extends JPanel
  {
    SKDCComboBox mpComboBox;
    JCheckBox mpCheckBox;

    /**
     * Constructor for Translation ComboBox
     * @param isTranslation
     */
    public DacComboPanel(String isTranslation)
    {
      super();
      
      try
      {
        mpComboBox = new SKDCTranComboBox(isTranslation);
        buildPanel();
      }
      catch (NoSuchFieldException nsfe)
      {
        System.out.println("Silly programmer, \"" + isTranslation
            + "\" isn't a valid translation!");
      }
    }

    /**
     * Constructor for Translation ComboBox
     * 
     * @param isTranslation
     * @param ianTranslations
     */
    public DacComboPanel(String isTranslation, int[] ianTranslations)
    {
      super();
      
      try
      {
        mpComboBox = new SKDCTranComboBox(isTranslation, ianTranslations, false);
        buildPanel();
      }
      catch (NoSuchFieldException nsfe)
      {
        System.out.println("Silly programmer, \"" + isTranslation
            + "\" isn't a valid translation!");
      }
    }

    /**
     * Constructor
     * @param ipChoices
     */
    public DacComboPanel(String[] iasChoices)
    {
      super();
      
      mpComboBox = new SKDCComboBox(iasChoices);
      buildPanel();
    }

    /**
     * 
     */
    private void buildPanel()
    {
      setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      mpCheckBox = new JCheckBox("Modify");
      mpCheckBox.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            mpComboBox.setEnabled(mpCheckBox.isSelected());
            if (mpCheckBox.isSelected())
            {
              mpComboBox.requestFocus();
            }
          }
        });
      
      mpComboBox.setEnabled(false);
      
      add(mpComboBox);
      add(Box.createHorizontalStrut(10));
      add(mpCheckBox);
    }
    
    /**
     * 
     * @return
     */
    public boolean isSelected()
    {
      return mpCheckBox.isSelected();
    }
    
    /**
     * 
     * @return
     * @throws NoSuchFieldException
     */
    public int getIntegerValue() throws NoSuchFieldException
    {
      if (mpComboBox instanceof SKDCTranComboBox)
      {
        return ((SKDCTranComboBox)mpComboBox).getIntegerValue();
      }
      throw new NoSuchFieldException("Not a translation combo box"); 
    }
    
    /**
     * 
     * @return
     */
    public String getText()
    {
      return mpComboBox.getText();
    }
    
    /**
     * Add an ActionListener to the CheckBox
     * @param ipListener
     */
    public void addActionListener(ActionListener ipListener)
    {
      mpCheckBox.addActionListener(ipListener);
    }
    
    /**
     * Add an ItemListener to the ComboBox
     * @param ipListener
     */
    public void addItemListener(ItemListener ipListener)
    {
      mpComboBox.addItemListener(ipListener);
    }
    
    /**
     * 
     * @param inSelected
     * @throws NoSuchFieldException
     */
    public void setSelectedElement(int inSelected) throws NoSuchFieldException
    {
      if (mpComboBox instanceof SKDCTranComboBox)
      {
        ((SKDCTranComboBox)mpComboBox).setSelectedElement(inSelected);
      }
      else
      {
        throw new NoSuchFieldException("Not a translation combo box");
      }
    }
    
    /**
     * 
     * @param ipSelected
     */
    public void setSelectedItem(Object ipSelected)
    {
      mpComboBox.setSelectedItem(ipSelected);
    }
    
    /**
     * 
     * @param isTranslation
     * @param ianTranslations
     * @throws NoSuchFieldException
     */
    public void setComboBoxData(String isTranslation, int[] ianTranslations)
        throws NoSuchFieldException
    {
      if (mpComboBox instanceof SKDCTranComboBox)
      {
        ((SKDCTranComboBox)mpComboBox).setComboBoxData(isTranslation, ianTranslations);
      }
      else
      {
        throw new NoSuchFieldException("Not a translation combo box");  
      }
    }
  }
}
