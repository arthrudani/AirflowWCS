package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocation;
import com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.Warehouse;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

@SuppressWarnings("serial")
public class DBTableDiagnostic extends SKDCInternalFrame
{
  private final String RUN_DIAG = "RUN_DIAG";
  private final String CLEAR = "CLEAR_SELECTIONS";
  private final String SELECTALL = "SELECT ALL";

  protected JPanel northPanel;
  protected SKDCButton btnRunDiagnostic;
  protected SKDCButton btnClear;
  protected SKDCButton btnSelectAll;
  protected ActionListener btnEventListener;
  protected JCheckBox checkBoxDedicatedLocation;
  protected JCheckBox checkBoxItemMaster;
  protected JCheckBox checkBoxLoad;
  protected JCheckBox checkBoxLoadLineItem;
  protected JCheckBox checkBoxLocation;
  protected JCheckBox checkBoxMove;
  protected JCheckBox checkBoxOrderHeader;
  protected JCheckBox checkBoxOrderLine;
  protected JCheckBox checkBoxPurchaseOrderHeader;
  protected JCheckBox checkBoxPurchaseOrderLine;
  protected JCheckBox checkBoxStation;
  protected SKDCLabel LoadHoursLabel;
  protected SKDCIntegerField LoadHoursText;
  protected SKDCLabel POHoursLabel;
  protected SKDCIntegerField POHoursText;
  protected JTextPane mpTextArea;
  
  protected String msResults;

  /**
   * Constructor
   */
  public DBTableDiagnostic()
  {
    super("DB Table Diagnostic");
    setMaximizable(true);
    
    initInputArea();
    defineLabels();
    defineButtons();
    Container cp = getContentPane();
    cp.add(buildInputPanel(), BorderLayout.NORTH);
    cp.add(new JScrollPane(mpTextArea), BorderLayout.CENTER);
    cp.add(buildButtonPanel(), BorderLayout.SOUTH);

  }

  /**
   * getPreferredSize
   */
  @Override
  public Dimension getPreferredSize()
  {
    return (new Dimension(750, 420));
  }

  /*===========================================================================
   ****** Event Listeners go here ******
   ===========================================================================*/
  /**
   *  Defines all buttons on the main Host frame view, and adds listeners
   *  to them.
   */
  private void setButtonListeners()
  {
    btnEventListener = new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        String which_button = e.getActionCommand();
        if (which_button.equals(RUN_DIAG))
        {
          runDiagnostic();
        }
        if (which_button.equals(CLEAR))
        {
          runClear();
        }
        if (which_button.equals(SELECTALL))
        {
          runSelectAll();
        }
      }
    };
    // Attach listeners.
    btnRunDiagnostic.addEvent(RUN_DIAG, btnEventListener);
    btnClear.addEvent(CLEAR, btnEventListener);
    btnSelectAll.addEvent(SELECTALL, btnEventListener);
  }

  /*===========================================================================
   ****** Button pressed action methods go here ******
   ===========================================================================*/
  public void runClear()
  {

    String sText = "";
    mpTextArea.setText(sText);

    checkBoxDedicatedLocation.setSelected(false);
    checkBoxItemMaster.setSelected(false);
    checkBoxLoad.setSelected(false);
    checkBoxLoadLineItem.setSelected(false);
    checkBoxLocation.setSelected(false);
    checkBoxStation.setSelected(false);
    checkBoxOrderHeader.setSelected(false);
    checkBoxOrderLine.setSelected(false);
    checkBoxPurchaseOrderHeader.setSelected(false);
    checkBoxPurchaseOrderLine.setSelected(false);
    checkBoxMove.setSelected(false);

    LoadHoursText.setEnabled(false);
    POHoursText.setEnabled(false);
  }

  public void runSelectAll()
  {

    String sText = "";
    mpTextArea.setText(sText);

    checkBoxDedicatedLocation.setSelected(true);
    checkBoxItemMaster.setSelected(true);
    checkBoxLoad.setSelected(true);
    checkBoxLoadLineItem.setSelected(true);
    checkBoxLocation.setSelected(true);
    checkBoxStation.setSelected(true);
    checkBoxOrderHeader.setSelected(true);
    checkBoxOrderLine.setSelected(true);
    checkBoxPurchaseOrderHeader.setSelected(true);
    checkBoxPurchaseOrderLine.setSelected(true);
    checkBoxMove.setSelected(true);

    LoadHoursText.setEnabled(true);
    POHoursText.setEnabled(true);
  }

  public void runDiagnostic()
  {
    initializeTextArea();

    if (checkBoxDedicatedLocation.isSelected())
    {
      addHeader("Dedicated Locations");
      checkDedicatedLocations();
    }
    if (checkBoxItemMaster.isSelected())
    {
      addHeader("Item Masters");
      checkItemMaster();
    }
    if (checkBoxLoad.isSelected())
    {
      addHeader("Loads");
      checkLoad();
    }
    if (checkBoxLoadLineItem.isSelected())
    {
      addHeader("Item Details");
      checkLoadLineItem();
    }
    if (checkBoxLocation.isSelected())
    {
      addHeader("Locations");
      checkLocation();
    }
    if (checkBoxMove.isSelected())
    {
      addHeader("Moves");
      checkMove();
    }
    if (checkBoxOrderHeader.isSelected())
    {
      addHeader("Order Headers");
      checkOrderHeaders();
    }
    if (checkBoxOrderLine.isSelected())
    {
      addHeader("Order Lines");
      checkOrderLines();
    }
    if (checkBoxPurchaseOrderHeader.isSelected())
    {
      addHeader("PO Headers");
      checkPOHeaders();
    }
    if (checkBoxPurchaseOrderLine.isSelected())
    {
      addHeader("PO Lines");
      checkPOLines();
    }
    if (checkBoxStation.isSelected())
    {
      addHeader("Stations");
      checkStation();
    }
    
    finalizeTextArea();
  }

  /**
   * Initialize the text area
   */
  protected void initializeTextArea()
  {
    mpTextArea.setText("");
    
    msResults = "<html>";
  }
  
  /**
   * Add a header
   * 
   * @param isHeader
   */
  protected void addHeader(String isHeader)
  {
    completeErrorList();
    addLineFeed();
    msResults += "<B><FONT SIZE=+1>" + isHeader + "</FONT></B>";
    addLineFeed();
  }
  
  /**
   * Add a sub-header
   * 
   * @param isSubHeader
   */
  protected void addSubHeader(String isSubHeader)
  {
    completeErrorList();
    msResults += " &nbsp; &nbsp; <B>" + isSubHeader + "</B>";
    addLineFeed();
  }

  /**
   * Add a note
   * 
   * @param isNote
   */
  protected void addNote(String isNote)
  {
    completeErrorList();
    msResults += " &nbsp; &nbsp; " + isNote;
    addLineFeed();
  }

  protected boolean mzNeedsListStart = true;
  protected boolean mzNeedsListEnd = false;
  
  /**
   * Add a error
   * 
   * @param isError
   */
  protected void addError(String isError)
  {
    if (mzNeedsListStart)
    {
      msResults += "\n<UL>";
      mzNeedsListStart = false;
      mzNeedsListEnd = true;
    }
    msResults += "\n<LI><B><FONT FACE='COURIER NEW' COLOR=RED>" + isError + ".</FONT></B></LI>";
  }
  
  /**
   * Add a error
   * 
   * @param e
   */
  protected void addError(Exception e)
  {
    if (mzNeedsListStart)
    {
      msResults += "\n<UL>";
      mzNeedsListStart = false;
      mzNeedsListEnd = true;
    }
    msResults += "\n<LI><FONT COLOR=RED>" + e.getMessage() + ".</FONT></LI>";
    logger.logException(e);
  }

  /**
   * Complete the error list
   */
  protected void completeErrorList()
  {
    if (mzNeedsListEnd)
    {
      msResults += "\n</UL>";
      mzNeedsListStart = true;
      mzNeedsListEnd = false;
    }
  }

  /**
   * Add a line feed.
   */
  protected void addLineFeed()
  {
    completeErrorList();
    msResults += "\n<BR>";
  }
  
  /**
   * Add any footers to the text area and display
   */
  protected void finalizeTextArea()
  {
    msResults += "<BR>Finished Diagnostic Check.</html>";
    mpTextArea.setText(msResults);
    mpTextArea.setCaretPosition(0);
  }
  
  /*===========================================================================
   ****** All other private methods go here ******
   ===========================================================================*/
  private void defineButtons()
  {
    btnSelectAll = new SKDCButton("Select All", "Select All", 'R');

    btnClear = new SKDCButton("Clear Selections", "Clear Selections", 'R');

    btnRunDiagnostic = new SKDCButton("Run Diagnostic",
        "Execute the diagnostic tool.", 'R');
    setButtonListeners();
  }

  private void defineLabels()
  {
    LoadHoursLabel = new SKDCLabel("          Hours Load Is Active Check:");
    POHoursLabel = new SKDCLabel("          Hours PO Exists Check:");
  }

  private void setCheckBoxListeners()
  {
    checkBoxLoad.addActionListener(new LoadCheckBoxListener());
    checkBoxPurchaseOrderHeader.addActionListener(new POCheckBoxListener());
  }

  protected void initInputArea()
  {
    mpTextArea = new JTextPane();
    mpTextArea.setContentType("text/html");
    mpTextArea.setEditable(false);

    checkBoxDedicatedLocation = new JCheckBox("Check Dedicated Locations",
        false);
    checkBoxItemMaster = new JCheckBox("Check Item Masters", false);
    checkBoxLoad = new JCheckBox("Check Loads ", false);
    checkBoxLoadLineItem = new JCheckBox("Check Items", false);
    checkBoxLocation = new JCheckBox("Check Locations", false);
    checkBoxMove = new JCheckBox("Check Moves", false);
    checkBoxOrderHeader = new JCheckBox("Check Order Headers", false);
    checkBoxOrderLine = new JCheckBox("Check Order Lines", false);
    checkBoxPurchaseOrderHeader = new JCheckBox("Check PO Headers", false);
    checkBoxPurchaseOrderLine = new JCheckBox("Check PO Lines", false);
    checkBoxStation = new JCheckBox("Check Stations", false);

    LoadHoursText = new SKDCIntegerField(6);
    POHoursText = new SKDCIntegerField(6);
    // Default to hours
    LoadHoursText.setValue(2);
    POHoursText.setValue(24);

    //HoursLabel.setEnabled(false);
    LoadHoursText.setEnabled(false);
    POHoursText.setEnabled(false);

    //call to setjcheckBoxlisteners
    setCheckBoxListeners();
  }

  /**
   * Define input panel components.
   * @return Built JPanel with input text boxes.
   */
  private JPanel buildInputPanel()
  {
    northPanel = new JPanel();
    northPanel = getEmptyInputPanel("");
    // Create Grid bag constraints.
    GridBagConstraints gbconst = new GridBagConstraints();

    gbconst.insets = new Insets(1, 1, 5, 1);
    gbconst.gridy = 1;
    gbconst.gridwidth = 1;
    gbconst.weighty = 0.5;
    gbconst.weightx = 5;
    gbconst.gridx = GridBagConstraints.RELATIVE;
    gbconst.anchor = GridBagConstraints.WEST;
    northPanel.add(checkBoxLoad, gbconst);
    gbconst.anchor = GridBagConstraints.EAST;
    northPanel.add(LoadHoursLabel, gbconst);
    gbconst.anchor = GridBagConstraints.WEST;
    northPanel.add(LoadHoursText, gbconst);
    northPanel.add(checkBoxLocation, gbconst);

    gbconst.gridy = 2;
    northPanel.add(checkBoxPurchaseOrderHeader, gbconst);
    gbconst.anchor = GridBagConstraints.EAST;
    northPanel.add(POHoursLabel, gbconst);
    gbconst.anchor = GridBagConstraints.WEST;
    northPanel.add(POHoursText, gbconst);
    northPanel.add(checkBoxPurchaseOrderLine, gbconst);

    gbconst.gridy = 3;
    northPanel.add(checkBoxOrderHeader, gbconst);
    northPanel.add(checkBoxOrderLine, gbconst);
    northPanel.add(checkBoxMove, gbconst);

    gbconst.gridy = 4;
    northPanel.add(checkBoxItemMaster, gbconst);
    northPanel.add(checkBoxLoadLineItem, gbconst);
    northPanel.add(checkBoxDedicatedLocation, gbconst);
    northPanel.add(checkBoxStation, gbconst);

    return (northPanel);
  }

  /**
   * Define the button panel at the bottom of the screen.
   * 
   * @return JPanel with buttons in place.
   */
  private JPanel buildButtonPanel()
  {
    JPanel buttonPanel = getEmptyButtonPanel();

    buttonPanel.add(btnSelectAll);
    buttonPanel.add(btnClear);
    buttonPanel.add(btnRunDiagnostic);

    return (buttonPanel);
  }

  /*========================================================================*/
  /* Database check methods                                                 */
  /*========================================================================*/
  
  /**
   * Check PO Headers
   */
  protected void checkPOHeaders()
  {
    PurchaseOrderHeader poh = Factory.create(PurchaseOrderHeader.class);

    try
    {
      addSubHeader("Checking PO Headers for INCORRECT line count.");

      List poList = poh.getPOsWithInvalidLinecount();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String vsOrderID = DBHelper.getStringField(theMap, PurchaseOrderHeaderData.ORDERID_NAME);
          String vsHostLineCount = DBHelper.getStringField(theMap, PurchaseOrderHeaderData.HOSTLINECOUNT_NAME);

          addError("PO Header " + vsOrderID + " has an INCORRECT line count of "
              + vsHostLineCount);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      Date ipDate = new Date();
      Date ipmoveDate = new Date(ipDate.getTime()
          - (POHoursText.getValue() * 3600000));

      addSubHeader("Checking for OLD PO Headers.");

      List poList = poh.getPOsExistingToLong(ipmoveDate);
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sOrderID = DBHelper.getStringField(theMap, PurchaseOrderHeaderData.ORDERID_NAME);
          Date dMoveDate = DBHelper.getDateField(theMap, PurchaseOrderHeaderData.EXPECTEDDATE_NAME);

          addError("PO Header " + sOrderID + " has EXISTED since " + dMoveDate);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }
  }

  /**
   * Check PO Lines
   */
  protected void checkPOLines()
  {
    PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);

    try
    {
      addSubHeader("Checking PO Lines for INVALID Items.");

      List poList = pol.getPOLinesWithInvalidItemMaster();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sOrderID = DBHelper.getStringField(theMap, PurchaseOrderLineData.ORDERID_NAME);
          String sItem = DBHelper.getStringField(theMap, PurchaseOrderLineData.ITEM_NAME);

          addError("PO Line " + sOrderID + " has an INVALID item " + sItem);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }
  }

  /**
   * Check Order Lines
   */
  protected void checkOrderLines()
  {
    OrderLine ol = Factory.create(OrderLine.class);

    try
    {
      addSubHeader("Checking Order Lines for INVALID items.");

      List poList = ol.getOrderLinesWithInvalidItemMaster();

      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sOrderID = DBHelper.getStringField(theMap, OrderLineData.ORDERID_NAME);
          String sItem = DBHelper.getStringField(theMap, OrderLineData.ITEM_NAME);

          addError("Order Line " + sOrderID + " has an INVALID item " + sItem);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }
  }

  /**
   * Check Order Headers
   */
  protected void checkOrderHeaders()
  {
    OrderHeader oh = Factory.create(OrderHeader.class);

    try
    {
      addSubHeader("Checking Order Headers for INCORRECT line count.");

      List poList = oh.getOrderHeaderWithInvalidLinecount();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String vsOrderID = DBHelper.getStringField(theMap, OrderHeaderData.ORDERID_NAME);
          String vsHostLineCount = DBHelper.getStringField(theMap, OrderHeaderData.HOSTLINECOUNT_NAME);

          addError("Order Header " + vsOrderID
              + " has an INCORRECT line count of " + vsHostLineCount);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      addSubHeader("Checking Order Headers for INVALID Customer.");

      List poList = oh.getOrderHeaderWithInvalidShipCustomer();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sOrderID = DBHelper.getStringField(theMap, OrderHeaderData.ORDERID_NAME);
          String sShipCustomer = DBHelper.getStringField(theMap, OrderHeaderData.SHIPCUSTOMER_NAME);

          addError("Order Header " + sOrderID + " has an INVALID Customer of "
              + sShipCustomer);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      addSubHeader("Checking Order Headers for INVALID destination.");

      List poList = oh.getOrderHeaderWithInvalidDestination();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sOrderID = DBHelper.getStringField(theMap, OrderHeaderData.ORDERID_NAME);
          String sDestWarehouse = DBHelper.getStringField(theMap, OrderHeaderData.DESTWAREHOUSE_NAME);
          String sDestAddress = DBHelper.getStringField(theMap, OrderHeaderData.DESTADDRESS_NAME);

          addError("Order Header " + sOrderID
              + " has an INVALID destination of " + sDestWarehouse + "-"
              + sDestAddress);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }
  }

  /**
   * Check Dedicated Locations
   */
  protected void checkDedicatedLocations()
  {
    DedicatedLocation dl = Factory.create(DedicatedLocation.class);

    try
    {
      addSubHeader("Checking Dedicated Locations for INVALID items.");

      List poList = dl.getDedicatedLocationsWithInvalidItemMaster();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sItem = DBHelper.getStringField(theMap, DedicatedLocationData.ITEM_NAME);
          String sWarehouse = DBHelper.getStringField(theMap, DedicatedLocationData.WAREHOUSE_NAME);
          String sAddress = DBHelper.getStringField(theMap, DedicatedLocationData.ADDRESS_NAME);

          addError("\tDedicated Location " + sWarehouse + "-" + sAddress
              + " has an INVALID Item of " + sItem + ".\n");
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      addSubHeader("Checking Dedicated Locations for INVALID locations.");

      List poList = dl.getDedicatedLocationsWithInvalidLocation();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sItem = DBHelper.getStringField(theMap, DedicatedLocationData.ITEM_NAME);
          String sWarehouse = DBHelper.getStringField(theMap, DedicatedLocationData.WAREHOUSE_NAME);
          String sAddress = DBHelper.getStringField(theMap, DedicatedLocationData.ADDRESS_NAME);

          addError("Dedicated Location " + sItem
              + " has an INVALID Location of " + sWarehouse + "-" + sAddress);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }
  }

  /**
   * Check stations
   */
  protected void checkStation()
  {
    addNote("No Station checks currently defined.");
  }

  /**
   * Check locations
   */
  protected void checkLocation()
  {
    Location lc = Factory.create(Location.class);
    Warehouse wh = Factory.create(Warehouse.class);

    try
    {
      addSubHeader("Checking EMPTY AS/RS Locations.");

      List poList = lc.getAsrsLocationsEmptyWithLoads();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);

          String sWarehouse = DBHelper.getStringField(theMap, LocationData.WAREHOUSE_NAME);
          String sAddress = DBHelper.getStringField(theMap, LocationData.ADDRESS_NAME);
          String sLoadid = DBHelper.getStringField(theMap, LoadData.LOADID_NAME);

          addError("Location " + sWarehouse + "-" + sAddress
              + " is EMPTY but has Load " + sLoadid);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      String[] whList = wh.getRegularWarehouseChoices("");
      int whidx = 0;

      addSubHeader("Checking OCCUPIED Locations.");

      while (whidx < whList.length)
      {
        try
        {
          List poList = lc.getAsrsLocationsFullWithoutLoads(whList[whidx]);
          int listLength = poList.size();
          if (listLength != 0)
          {
            for (int idx = 0; idx < listLength; idx++)
            {
              Map theMap = (Map) poList.get(idx);

              String sAddress = DBHelper.getStringField(theMap, LocationData.ADDRESS_NAME);

              addError("Location " + whList[whidx] + "-" + sAddress
                  + " is OCCUPIED but has NO LOAD");
            }
          }
        }
        catch (DBException e)
        {
          addError(e);
        }
        whidx++;
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      String[] whList = wh.getRegularWarehouseChoices("");

      addSubHeader("Checking RESERVED Locations.");

      int whidx = 0;

      while (whidx < whList.length)
      {

        try
        {
          List poList = lc.getAsrsLocationsReservedWithoutLoads(whList[whidx]);
          int listLength = poList.size();
          if (listLength != 0)
          {
            for (int idx = 0; idx < listLength; idx++)
            {
              Map theMap = (Map) poList.get(idx);

              String sAddress = DBHelper.getStringField(theMap, LocationData.ADDRESS_NAME);

              addError("Location " + whList[whidx] + "-" + sAddress
                  + " is RESERVED but has NO LOAD en route");
            }
          }
        }
        catch (DBException e)
        {
          addError(e);
        }
        whidx++;
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    // Unavailable empty location check
    try
    {
      String[] whList = wh.getRegularWarehouseChoices("");

      addSubHeader("Checking UNAVAILABLE Empty Locations.");

      int whidx = 0;

      while (whidx < whList.length)
      {

        try
        {
          List poList = lc.getAsrsLocationsUnavailWithoutLoads(whList[whidx]);
          int listLength = poList.size();
          if (listLength != 0)
          {
            for (int idx = 0; idx < listLength; idx++)
            {
              Map theMap = (Map) poList.get(idx);

              String sAddress = DBHelper.getStringField(theMap, LocationData.ADDRESS_NAME);

              addError("Location " + whList[whidx] + "-" + sAddress
                  + " is UNAVAILABLE with NO LOAD");
            }
          }
        }
        catch (DBException e)
        {
          addError(e);
        }
        whidx++;
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    // Unavailable occupied location check
    try
    {
      String[] whList = wh.getRegularWarehouseChoices("");

      addSubHeader("Checking UNAVAILABLE Occupied Locations.");

      int whidx = 0;

      while (whidx < whList.length)
      {
        try
        {
          List poList = lc.getAsrsLocationsUnavailWithLoads(whList[whidx]);
          int listLength = poList.size();
          if (listLength != 0)
          {
            for (int idx = 0; idx < listLength; idx++)
            {
              Map theMap = (Map) poList.get(idx);

              String sAddress = DBHelper.getStringField(theMap, LocationData.ADDRESS_NAME);
              String sLoadid = DBHelper.getStringField(theMap, LoadData.LOADID_NAME);

              addError("Location " + whList[whidx] + "-" + sAddress
                  + " is UNAVAILABLE but has Load " + sLoadid);
            }
          }
        }
        catch (DBException e)
        {
          addError(e);
        }
        whidx++;
      }
    }
    catch (DBException e)
    {
      addError(e);
    }
  }

  /**
   * Check item masters
   */
  protected void checkItemMaster()
  {
    addNote("No Item Master checks currently defined.");
  }

  /**
   * Check moves
   */
  protected void checkMove()
  {
    Move mv = Factory.create(Move.class);

    try
    {
      addSubHeader("Checking Moves for INVALID Loads.");

      List poList = mv.getMovesWithInvalidLoadIDs();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);

          String sLoadid = DBHelper.getStringField(theMap, MoveData.LOADID_NAME);
          String sItem = DBHelper.getStringField(theMap, MoveData.ITEM_NAME);
          String sOrderID = DBHelper.getStringField(theMap, MoveData.ORDERID_NAME);
          String sRouteID = DBHelper.getStringField(theMap, MoveData.ROUTEID_NAME);

          addError("Move has INVALID LoadID " + sLoadid + ", item " + sItem
              + ", Order " + sOrderID + ", Route " + sRouteID);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }
  }

  /**
   * Check loads
   */
  protected void checkLoad()
  {
    Load ld = Factory.create(Load.class);

    try
    {
      addSubHeader("Checking Loads with NO MATCHING Locations.");

      List poList = ld.getLoadsWithIncorrectDeviceID();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sLoadid = DBHelper.getStringField(theMap, LoadData.LOADID_NAME);
          String sWarehouse = DBHelper.getStringField(theMap, LoadData.WAREHOUSE_NAME);
          String sAddress = DBHelper.getStringField(theMap, LoadData.ADDRESS_NAME);
          String sDeviceID = DBHelper.getStringField(theMap, LoadData.DEVICEID_NAME);

          addError("Load " + sLoadid + " at " + sWarehouse + "-" + sAddress
              + " has device " + sDeviceID + " NOT MATCHING location device.");
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      Date ipDate = new Date();
      Date ipmoveDate = new Date(ipDate.getTime()
          - (LoadHoursText.getValue() * 3600000));

      addSubHeader("Checking Loads that have been ACTIVE too long.");

      List poList = ld.getLoadsMovingTooLong(ipmoveDate);
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sLoadid = DBHelper.getStringField(theMap, LoadData.LOADID_NAME);
          String sWarehouse = DBHelper.getStringField(theMap, LoadData.WAREHOUSE_NAME);
          String sAddress = DBHelper.getStringField(theMap, LoadData.ADDRESS_NAME);
          int iLoadMoveStatus = DBHelper.getIntegerField(theMap, LoadData.LOADMOVESTATUS_NAME);
          Date dMoveDate = DBHelper.getDateField(theMap, LoadData.MOVEDATE_NAME);
          String loadMoveStatusText = null;
          try
          {
            loadMoveStatusText = DBTrans.getStringValue(
                LoadData.LOADMOVESTATUS_NAME, iLoadMoveStatus);
          }
          catch (Exception eTrans)
          {
            addError(eTrans);
          }

          addError("Load " + sLoadid + " at " + sWarehouse + "-" + sAddress
              + " with status " + loadMoveStatusText + " SINCE " + dMoveDate);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      addSubHeader("Checking NOMOVE Loads with a Next Address.");

      List poList = ld.getLoadsNoMoveWithNextLoc();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sLoadid = DBHelper.getStringField(theMap, LoadData.LOADID_NAME);
          String sWarehouse = DBHelper.getStringField(theMap, LoadData.WAREHOUSE_NAME);
          String sAddress = DBHelper.getStringField(theMap, LoadData.ADDRESS_NAME);
          String sNextAddress = DBHelper.getStringField(theMap, LoadData.NEXTADDRESS_NAME);

          addError("Load " + sLoadid + " at " + sWarehouse + "-" + sAddress
              + " is NOMOVE with NextAdd " + sNextAddress);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }
  }

  /**
   * Check item details or load line items or whatever they're called
   */
  protected void checkLoadLineItem()
  {
    LoadLineItem lli = Factory.create(LoadLineItem.class);

    try
    {
      addSubHeader("Checking Items with INVALID Allocated Qtys.");

      List poList = lli.getLoadLineItemWithBadAlcQty();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sLoadID = DBHelper.getStringField(theMap, LoadLineItemData.LOADID_NAME);
          String sItem = DBHelper.getStringField(theMap, LoadLineItemData.ITEM_NAME);
          String sAlcQty = DBHelper.getStringField(theMap,
              LoadLineItemData.ALLOCATEDQUANTITY_NAME);

          addError("Load Line Item " + sLoadID + "-" + sItem
              + " has an INVALID Allocated Qty " + sAlcQty);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      addSubHeader("Checking Items for NEGATIVE Qtys.");

      List poList = lli.getLoadLineItemWithNegCurQty();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sLoadID = DBHelper.getStringField(theMap, LoadLineItemData.LOADID_NAME);
          String sItem = DBHelper.getStringField(theMap, LoadLineItemData.ITEM_NAME);
          String sCurQty = DBHelper.getStringField(theMap, LoadLineItemData.CURRENTQUANTITY_NAME);

          addError("Load Line Item " + sLoadID + "-" + sItem
              + " has NEGATIVE Current Qty " + sCurQty);
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }

    try
    {
      addSubHeader("Checking ALLOCATED Items without Moves.");

      List poList = lli.getLoadLineItemAlcQtyWithOutMove();
      int listLength = poList.size();
      if (listLength != 0)
      {
        for (int idx = 0; idx < listLength; idx++)
        {
          Map theMap = (Map) poList.get(idx);
          String sLoadID = DBHelper.getStringField(theMap, LoadLineItemData.LOADID_NAME);
          String sItem = DBHelper.getStringField(theMap, LoadLineItemData.ITEM_NAME);
          String sAlcQty = DBHelper.getStringField(theMap, LoadLineItemData.ALLOCATEDQUANTITY_NAME);

          addError("Load Line Item " + sLoadID + "-" + sItem
              + " has an Allocated Qty " + sAlcQty + " WITHOUT a Move.");
        }
      }
    }
    catch (DBException e)
    {
      addError(e);
    }
  }

  /*========================================================================*/
  /* Check box listeners                                                    */
  /*========================================================================*/
  
  /**
   * LoadCheckBoxListener
   */
  protected class LoadCheckBoxListener extends AbstractAction
  {
    public LoadCheckBoxListener()
    {
    }

    public void actionPerformed(ActionEvent e)
    {
      JCheckBox checkBox = (JCheckBox) e.getSource();
      if (checkBox.isSelected())
      {
        LoadHoursText.setEnabled(true);
      }
      else
      {
        LoadHoursText.setEnabled(false);
      }
    }
  }

  /**
   * POCheckBoxListener
   */
  protected class POCheckBoxListener extends AbstractAction
  {
    public POCheckBoxListener()
    {
    }

    public void actionPerformed(ActionEvent e)
    {
      JCheckBox checkBox = (JCheckBox) e.getSource();
      if (checkBox.isSelected())
      {
        POHoursText.setEnabled(true);
      }
      else
      {
        POHoursText.setEnabled(false);
      }
    }
  }
}
