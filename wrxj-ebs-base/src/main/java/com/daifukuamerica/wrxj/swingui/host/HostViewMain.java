package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrx;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHost;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostInDelegate;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.DacLargeListFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class HostViewMain extends DacLargeListFrame
{
  protected String[]             masHostNameList;
  protected SKDCComboBox         mpCmbHostName;
  protected JCheckBox            mpChkMessageIn;
  protected SKDCButton           mpBtnToggleProcessed;
  protected SKDCButton           mpBtnViewMessage;
  protected SKDCButton           mpBtnReset;
  protected SKDCInternalFrame    mpSearchFrame;
  protected SKDCInternalFrame    mpViewFrame;
  protected DBObject             mpDbObj  = null;
  protected StandardHostServer   mpHostServer;
  protected HostOutDelegate      mpHostOutDelegate;
  protected HostInDelegate       mpHostInDelegate;
  protected HostServerDelegate   mpHostDelegate;
  protected HostToWrxData        mpMesgInData;
  protected WrxToHostData        mpMesgOutData;
  protected String               msSearchType;
  protected String               msSearchResults;
  protected AbstractSKDCData     mpHostData;
  protected HostToWrx            mpHostToWrx = Factory.create(HostToWrx.class);
  protected WrxToHost            mpWrxToHost = Factory.create(WrxToHost.class);

  private static final String TOGGLE_PROCESSED_BTN = "MESSAGE_PROCESSED";
  private static final String VIEW_MESSAGE_BTN     = "VIEW_BUTTON";

  public HostViewMain()
  {
    super("HostDataView");
    setSearchVisible(true);
    setDetailSearchVisible(true);

    dbConnect();

    mpMesgInData = Factory.create(HostToWrxData.class);
    mpMesgOutData = Factory.create(WrxToHostData.class);
    mpHostServer = Factory.create(StandardHostServer.class, "HostDataView");
    mpHostInDelegate = new HostInDelegate();
    mpHostOutDelegate = new HostOutDelegate();

    initSearchInput();
    setGlobalHostDataObjects();
    fillMinMaxTextFields((String)mpCmbHostName.getItemAt(0));
  }

  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(895, 420));
  }

  /**
   *  Method to initialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  @Override
  protected void buildScreen() throws Exception
  {
    listPanel.setLayout(new BorderLayout());

    // build the search panel
    searchPanel = createSearchPanel();

    createButtonPanel();

    setSearchVisible(false);

    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(getInfoPanel(), BorderLayout.CENTER);
    vpSouthPanel.add(buttonPanel, BorderLayout.SOUTH);

    sktable = new DacTable(new DacModel(dataList, dataName));

    getContentPane().add(searchPanel, BorderLayout.NORTH);
    getContentPane().add(sktable.getScrollPane(), BorderLayout.CENTER);
    getContentPane().add(vpSouthPanel, BorderLayout.SOUTH);
  }

  /**
   * Build search panel
   * @return <code>JPanel</code> the Search panel
   */
  @Override
  protected JPanel createSearchPanel()
  {
    mpCmbHostName = new SKDCComboBox();
    mpChkMessageIn = new JCheckBox("Inbound Messages", true);
    searchButton = new SKDCButton("Search", "Search", 'S');
    detailedSearchButton = new SKDCButton("Detailed Search", "Detailed Search", 't');
    refreshButton = new SKDCButton("Refresh", "Refresh", 'R');
    mpBtnReset = new SKDCButton("Reset Search", "Reset to default search criteria", 'e');

    searchLabel.setText("Search");
    setTitleFromDataName();

    JPanel vpPanel = getEmptyListSearchPanel();

    vpPanel.add(mpCmbHostName, null);
    vpPanel.add(mpChkMessageIn, null);
    vpPanel.add(searchButton, null);
    vpPanel.add(detailedSearchButton, null);
    vpPanel.add(refreshButton, null);
    vpPanel.add(mpBtnReset, null);

    return vpPanel;
  }

  @Override
  protected void createButtonPanel()
  {
    mpBtnToggleProcessed = new SKDCButton("Change Processed",
            "Toggle message processed flag on selected rows", 'C');
    mpBtnViewMessage = new SKDCButton("View Selected Message(s)",
            "View complete host message.", 'V');

    buttonPanel.add(mpBtnToggleProcessed, null);
    buttonPanel.add(mpBtnViewMessage, null);

    addButtonListeners();
  }

  /**
   *  Method used to set the view button visible.
   *
   *  @param visible True or false.
   *
   */
  @Override
  protected void setViewButtonVisible(boolean visible)
  {
    mpBtnViewMessage.setVisible(visible);
  }

  /**
   *  Method used to set the view button visible.
   *
   *  @param visible True or false.
   *
   */
  @Override
  protected void setCloseButtonVisible(boolean visible)
  {
    // There is no Close button
  }

  /**
   * Set up the permission of buttons
   */
  @Override
  protected void setupButtonPermission()
  {
  }

  /**
   * Make sure we're connected to the database
   */
  private void dbConnect()
  {
    mpDbObj = new DBObjectTL().getDBObject();

    try
    {
      if (mpDbObj != null && !mpDbObj.checkConnected()) mpDbObj.connect();
    }
    catch (DBException e)
    {
      displayError("Error connecting to Database...", "Connection Error");
    }
  }

  /**
   * Initialize the search fields
   */
  private void initSearchInput()
  {
    try
    {
      masHostNameList = mpHostServer.getHostNames();
      if (masHostNameList.length == 0)
      {
        masHostNameList = new String[] {""};
        displayWarning("No host is defined.");
      }
      mpCmbHostName.setComboBoxData(masHostNameList);
    }
    catch(DBException e)
    {
      displayError("Error getting host names...Combo-box not built!" +
                   e.getMessage());
    }
    setHostNameComboListener();
    mpCmbHostName.setSelectedIndex(0);
  }

  /**
   * Set the min/max when the host name changes
   */
  private void setHostNameComboListener()
  {
    mpCmbHostName.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent ipEvent)
      {
        if (ipEvent.getStateChange() == ItemEvent.SELECTED ||
                ipEvent.getStateChange() == ItemEvent.DESELECTED)
        {
          setGlobalHostDataObjects();
        }
        fillMinMaxTextFields((String)ipEvent.getItem());
      }
    });
  }

  /**
   *  Method sets an AbstractSKDCData reference to WrxToHostData or HostToWrxData
   *  and a HostServerDelegate reference to HostInDelgate, or HostOutDelegate.
   *  The appropriate reference is set based on whether or not the Message In
   *  check box is selected.
   */
  private void setGlobalHostDataObjects()
  {
    if (mpChkMessageIn.isSelected())
    {
      mpDBInt = mpHostToWrx;
      mpHostDelegate = mpHostInDelegate;
      mpHostData = mpMesgInData;
    }
    else
    {
      mpDBInt = mpWrxToHost;
      mpHostDelegate = mpHostOutDelegate;
      mpHostData = mpMesgOutData;
    }
    mpHostData.clearKeysColumns();
    mpHostData.clearOrderByColumns();
    mpHostData.addOrderByColumn(HostToWrxData.MESSAGEADDTIME_NAME, true);
    mpHostData.addOrderByColumn(HostToWrxData.MESSAGESEQUENCE_NAME);
  }

  /**
   * Fill in the min/max search fields
   * @param isHostName
   */
  private void fillMinMaxTextFields(String isHostName)
  {
    try
    {
      mpHostDelegate.setInfo(isHostName);
    }
    catch (DBException exc)
    {
      super.displayError(exc.getMessage());
    }
  }

  /**
   * Set the min/max when the in/out check box changes
   */
  private void setMessageInCheckboxListener()
  {
    mpChkMessageIn.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent ipEvent)
      {
        if (ipEvent.getStateChange() == ItemEvent.SELECTED)
        {
          mpHostDelegate = mpHostInDelegate;
        }
        else
        {
          mpHostDelegate = mpHostOutDelegate;
        }
        fillMinMaxTextFields((String)HostViewMain.this.mpCmbHostName.getSelectedItem());
      }
    });
  }

  /*===========================================================================
                    ****** Event Listeners go here ******
    ===========================================================================*/
  /**
   *  Defines all buttons on the main Host frame view, and adds listeners
   *  to them.
   */
  private void addButtonListeners()
  {
    ActionListener vpBtnEventListener = getDefaultListener();

    mpBtnToggleProcessed.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        toggleProcessedButton();
      }
    });
    mpBtnViewMessage.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        viewMessageButtonPressed();
      }
    });
    mpBtnReset.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        resetButtonPressed();
      }
    });
                                       // Attach listeners.
    mpBtnToggleProcessed.addEvent(TOGGLE_PROCESSED_BTN, vpBtnEventListener);
    mpBtnViewMessage.addEvent(VIEW_MESSAGE_BTN, vpBtnEventListener);
    searchButton.addEvent(SKDCGUIConstants.SEARCH_BTN, vpBtnEventListener);
    detailedSearchButton.addEvent(SKDCGUIConstants.DETSEARCH_BTN, vpBtnEventListener);
    refreshButton.addEvent(SKDCGUIConstants.REFRESH_BTN, vpBtnEventListener);
    mpBtnReset.addEvent(SKDCGUIConstants.RESET_BTN, vpBtnEventListener);

    setMessageInCheckboxListener();
  }

  /**
   * Custom mouse pop-up for the table
   */
  @Override
  protected void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
      @Override
      public SKDCPopupMenu definePopup()
      {
        popupMenu.add("Change Processed Flag", TOGGLE_PROCESSED_BTN, getDefaultListener());
        popupMenu.add("View Full Message", VIEW_MESSAGE_BTN, getDefaultListener());

        return(popupMenu);
      }

      @Override
      public void displayDetail()
      {
        viewMessageButtonPressed();
      }
    });
  }

  /*===========================================================================
              ****** Button pressed action methods go here ******
    ===========================================================================*/
  /**
   *  Method allows a message(s) to be marked as processed or unprocessed.
   */
  private void toggleProcessedButton()
  {
    int vnUpdateCount = 0, vnMessageProcessed = 0;
    setGlobalHostDataObjects();
    int[] vpSelectedRows = sktable.getSelectedRows();

    for(int vnLoopIdx = 0; vnLoopIdx < vpSelectedRows.length; vnLoopIdx++)
    {
      Map vpDataMap = sktable.getRowData(vpSelectedRows[vnLoopIdx]);
      vnMessageProcessed = DBHelper.getIntegerField(vpDataMap, WrxToHostData.MESSAGEPROCESSED_NAME);
      vnMessageProcessed = (vnMessageProcessed == DBConstants.NO) ? DBConstants.YES
                                                                   : DBConstants.NO;
      mpHostData.dataToSKDCData(vpDataMap);
      mpHostData.setField(WrxToHostData.MESSAGEPROCESSED_NAME, Integer.valueOf(vnMessageProcessed));
      try
      {
        mpHostDelegate.setInfo(mpHostData);
        mpHostServer.toggleProcessedFlag(mpHostDelegate);
                                       // Update the selected row.
        sktable.modifyRow(vpSelectedRows[vnLoopIdx], mpHostData);
        vnUpdateCount++;
      }
      catch(DBException exc)
      {
      }
    }

    displayInfoAutoTimeOut("Updated " + vnUpdateCount + " out of " +
                           vpSelectedRows.length + " rows.");

    if (vnUpdateCount > 0 && vnMessageProcessed == DBConstants.NO)
    {
      getSystemGateway().publishHostMesgReceiveEvent("", 0, "HostMessageIntegrator");
    }

  }

  /**
   *  Method to view complete host message.
   */
  protected void viewMessageButtonPressed()
  {
    int[] vanSelection = sktable.getSelectedRows();
    if (vanSelection.length == 0)
    {
      displayInfoAutoTimeOut("No row selected to View", "Selection Error");
      return;
    }
    else if (vanSelection.length >= 1)
    {
      mpViewFrame = new XMLHostDataViewFrame(mpHostDelegate, sktable.getSelectedRowDataArray());
    }
    addSKDCInternalFrameModal(mpViewFrame, new JPanel[] { searchPanel, buttonPanel });
  }

  /**
   * Do the search
   */
  @Override
  protected void searchButtonPressed()
  {
    setGlobalHostDataObjects();

//    String vsMessageSequenceName = WrxToHostData.MESSAGESEQUENCE_NAME;

    mpHostData.setKey(WrxToHostData.HOSTNAME_NAME, mpCmbHostName.getText());
    mpSearchFrame = null;
    searchForData();
  }

  /**
   * Method to refresh the data list. If searchFram is not null, it means
   * the Detail Search has been accessed and we need to get the data based on
   * the last accessed frame to generate the data list.
   */
  @Override
  protected void refreshButtonPressed()
  {
    fillMinMaxTextFields((String)HostViewMain.this.mpCmbHostName.getSelectedItem());
    if (mpSearchFrame == null)
    {
       searchButtonPressed();
    }
    else if (mpHostData != null)    // Use search frame criteria to refresh.
    {
      searchForData();
    }
    else
    {
      displayInfoAutoTimeOut(msSearchResults, msSearchType);
    }
  }

  /**
   * Method to reset min/max sequence number.
   */
  private void resetButtonPressed()
  {
    fillMinMaxTextFields((String)mpCmbHostName.getItemAt(0));
    searchButtonPressed();
  }

  /**
   * Detailed Search
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    msSearchType = "Detail Search Result";
    mpSearchFrame = new HostDetailSearchFrame(masHostNameList,
                                              mpCmbHostName.getText(),
                                              mpChkMessageIn.isSelected());
    addSKDCInternalFrameModal(mpSearchFrame, new JPanel[] {searchPanel, buttonPanel},
                              new HostDetailSearchFrameHandler());
  }

/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/
  /**
   * Search for host messages
   */
  private void searchForData()
  {
    startSearch(mpDBInt, mpHostData, Application.getInt("RowsPerPage",
            DEFAULT_ROWS_PER_PAGE));
  }

/*===========================================================================
              ****** All Listener classes go here ******
  ===========================================================================*/
  /**
   *   Property Change event listener for Search frame.
   */
  private class HostDetailSearchFrameHandler implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent ipPCEvent)
    {
      String vsSearchType = "Detail Search Result";
      String vsPropName = ipPCEvent.getPropertyName();
      if (vsPropName.equals(FRAME_CHANGE))
      {
                                       // This is the search crtiteria used in
                                       // the last search panel.
        mpHostData = (AbstractSKDCData)ipPCEvent.getOldValue();

        Object vpNewData = ipPCEvent.getNewValue();
        if (vpNewData instanceof List)
        {
          refreshButtonPressed();
        }
        else if (vpNewData instanceof AbstractSKDCData)
        {
          sktable.clearTable();
          sktable.appendRow((AbstractSKDCData)vpNewData);
          mpChkMessageIn.setSelected(vpNewData instanceof HostToWrxData);
        }
        // Update information
        mpCmbHostName.setSelectedItem(((HostDetailSearchFrame)mpSearchFrame).getHostName());
        mpChkMessageIn.setSelected(((HostDetailSearchFrame)mpSearchFrame).getInboundCheckBox());

        // Prepare message to be displayed
        int vnCount = sktable.getRowCount();
        if (vnCount == 0)
        {
          msSearchResults = "No data found";
        }
        else
        {
          msSearchResults = vnCount + " Message" + (vnCount == 1 ? "" : "s") + " Found";
        }
        displayInfoAutoTimeOut(msSearchResults, vsSearchType);
      }
    }
  }

  @Override
  protected Class getRoleOptionsClass()
  {
    return HostViewMain.class;
  }

}
