package com.daifukuamerica.wrxj.swingui.purchaseorder;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * <B>Description:</B> Screen for manual addition of expected loads
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 * 
 * @author       mandrus
 * @version      1.0
 */
@SuppressWarnings("serial")
public class ExpectedLoadPOFrame extends AbstractPOFrame
{
  protected StationComboBox  mpStation;
  protected SKDCTextField    mpLoadID;
  protected JScrollPane      mpLoadScroller;
  protected JList            mpLoadList;
  protected DefaultListModel mpLoadModel;

  protected PurchaseOrderLineData mpPOLData;
  protected StandardPoReceivingServer mpPOServer;

  private boolean mzAutoClearOnAdd = true;

  /**
   * @param isFrameTitle
   * @param isInputTitle
   */
  public ExpectedLoadPOFrame()
  {
    super("Expected Load", "Expected Load Information");
    
    mpPOHData = Factory.create(PurchaseOrderHeaderData.class);
    mpPOLData = Factory.create(PurchaseOrderLineData.class);
    mpPOServer = Factory.create(StandardPoReceivingServer.class);

    mpLoadModel = new DefaultListModel();
    
    buildScreen();
    setAddMode();
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#buildScreen()
   */
  @Override
  protected void buildScreen()
  {
    mpStation = new StationComboBox();
    try
    {
      mpStation.fill(new int[] { DBConstants.USHAPE_IN, DBConstants.PDSTAND,
          DBConstants.INPUT, DBConstants.REVERSIBLE },
          SKDCConstants.NO_PREPENDER);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
    mpLoadID = new SKDCTextField(PurchaseOrderLineData.LOADID_NAME);
    mpLoadList = new JList(mpLoadModel);
    mpLoadScroller = new JScrollPane(mpLoadList);
    Dimension vpScrollerSize = mpLoadScroller.getPreferredSize();
    vpScrollerSize.width = vpScrollerSize.width /2;
    vpScrollerSize.height = (int)(vpScrollerSize.height /1.5);
    mpLoadScroller.setPreferredSize(vpScrollerSize);

    addInput("Expected Receipt ID:", mpOrderID);
    addInput("Station:", mpStation);
    addInput("Load ID:", mpLoadID);
    addInput("Loads", mpLoadScroller);
    
    setInputVisible(mpLoadScroller, false);
    
    useAddButtons();
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#correctFieldEnabledValues()
   */
  @Override
  protected void correctFieldEnabledValues()
  {
    mpOrderID.setEnabled(mzAdding);
    mpStation.setEnabled(!mzDisplayOnly);
    mpLoadID.setEnabled(mzAdding);
    mpLoadList.setEnabled(false);
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#setAddMode()
   */
  @Override
  protected void setAddMode()
  {
    setTitle("Add Expected Load");
    setInputVisible(mpLoadID, true);
    setInputVisible(mpLoadScroller, false);
    mzAdding = true;
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#setDisplayMode(com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData)
   */
  @Override
  protected void setDisplayMode(PurchaseOrderHeaderData ipPOHData)
  {
    setTitle("Display Expected Receipt");
    mzAdding = false;
    mzDisplayOnly = true;
    mpPOHData = ipPOHData.clone();
    mpOrderID.setText(mpPOHData.getOrderID());
    
    setInputVisible(mpLoadID, false);
    setInputVisible(mpLoadScroller, true);
    
    mpBtnSubmit.setVisible(false);
    mpBtnClear.setVisible(false);
    
    getPOLData();

    correctFieldEnabledValues();
    clearButtonPressed();
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#setModifyMode(com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData)
   */
  @Override
  protected void setModifyMode(PurchaseOrderHeaderData ipPOHData)
  {
    setTitle("Modify Expected Receipt");
    mzAdding = false;
    mpPOHData = ipPOHData.clone();
    mpOrderID.setText(mpPOHData.getOrderID());

    setInputVisible(mpLoadID, false);
    setInputVisible(mpLoadScroller, true);

    useModifyButtons();
    
    getPOLData();

    correctFieldEnabledValues();
    clearButtonPressed();
  }

  /**
   * Clean up and close
   */
  @Override
  protected void closeButtonPressed()
  {
    mpPOServer.cleanUp();
    close();
  }
  
  /**
   * Reset to defaults
   */
  @Override
  protected void clearButtonPressed()
  {
    if (mzAdding)
    {
      mpPOHData.clear();
    }
    mpStation.setSelectedStation(mpPOHData.getStoreStation());
    mpLoadID.setText(mpPOLData.getLoadID());
    if (mzAdding)
    {
      mpOrderID.setText(createOrderIDByDateTime());
      mpOrderID.requestFocus();
    }
  }

  /**
   * Create the order
   */
  @Override
  protected void okButtonPressed()
  {
    if (mzAdding)
      addSingleLinePO();
    else
      updateSingleLinePO();
  } 

  /**
   * New order
   */
  protected void addComplete()
  {
    if (mzAutoClearOnAdd)
    {
      clearButtonPressed();
    }
    else
    {
      mpOrderID.setText(createOrderIDByDateTime());
      mpOrderID.requestFocus();
    }
  }

  /**
   * Create the purchase order or expected receipt or whatever we're calling it
   * nowadays.
   */
  protected void addSingleLinePO()
  {
    String vsOrderID = mpOrderID.getText();
    String vsStation = mpStation.getSelectedStation();
    String vsLoadID  = mpLoadID.getText();
    
    /*
     * Validation
     */
    if (vsOrderID.length() == 0)
    {
      mpOrderID.setText(createOrderIDByDateTime());
      vsOrderID = mpOrderID.getText();
    }

    if (vsStation.trim().length() == 0)
    {
      displayInfoAutoTimeOut("Station can not be blank.", "Validation Error");
      mpStation.requestFocus();
      return;
    }
    
    if (vsLoadID.length() == 0)
    {
      displayInfoAutoTimeOut("Load can not be blank.", "Validation Error");
      mpLoadID.requestFocus();
      return;
    }
    
    /*
     * Add the purchase order
     */
    PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
    vpPOHData.setOrderID(vsOrderID);
    vpPOHData.setOrderStatus(DBConstants.EREXPECTED);
    vpPOHData.setStoreStation(vsStation);
    vpPOHData.setExpectedDate(new Date());
    vpPOHData.setLastActivityTime(new Date());

    PurchaseOrderLineData vpPOLData = Factory.create(PurchaseOrderLineData.class);
    vpPOLData.clear();
    vpPOLData.setOrderID(vsOrderID);
    vpPOLData.setRouteID(vsStation);
    vpPOLData.setLineID("1");
    vpPOLData.setLoadID(vsLoadID);
    vpPOLData.setExpectedQuantity(1.0);

    List<PurchaseOrderLineData> vpPOLList = new ArrayList();
    vpPOLList.add(vpPOLData);
    try
    {
      mpPOServer.buildExpectedLoad(vpPOHData, vpPOLList);
      displayInfoAutoTimeOut("Expected Receipt " + vsOrderID + " added successfully.");
      changed(null, vpPOHData);
      addComplete();
//      close();
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }

  /**
   * Create the order
   */
  protected void updateSingleLinePO()
  {
    /*
     * Currently, the only thing they can change is the store station...
     */
    if (mpStation.getSelectedStation().equals(mpPOHData.getStoreStation()))
    {
      displayInfoAutoTimeOut("Nothing changed.");
      return;
    }
    
    try
    {
      PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
      vpPOHData.setKey(PurchaseOrderLineData.ORDERID_NAME, mpPOHData.getOrderID());
      vpPOHData.setStoreStation(mpStation.getSelectedStation());
      
      mpPOServer.modifyPOHead(vpPOHData);
      changed(null, mpPOHData);
      close();
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error updating Expected Receipt", dbe);
    }
  }

  /**
   * Get the PO Line Data for modify/display
   */
  protected void getPOLData()
  {
    mpOrderID.setText(mpPOHData.getOrderID());
    try
    {
      List<Map> vpOLList= mpPOServer.getPurchaseOrderLines(mpPOHData.getOrderID());
      mpPOLData.dataToSKDCData(vpOLList.get(0));
      if (vpOLList.size() > 1)
      {
        mpPOLData.setLoadID("MULTIPLE");
      }
      
      String[] vasLoads = new String[vpOLList.size()]; 
      for (int i = 0; i < vpOLList.size(); i++)
      {
        vasLoads[i] = vpOLList.get(i).get(PurchaseOrderLineData.LOADID_NAME).toString();
      }
      Arrays.sort(vasLoads);
      
      mpLoadModel.clear();
      for (String s : vasLoads)
      {
        mpLoadModel.addElement(s);
      }
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }
}
