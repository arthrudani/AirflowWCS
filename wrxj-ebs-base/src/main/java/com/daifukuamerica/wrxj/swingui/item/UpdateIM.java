package com.daifukuamerica.wrxj.swingui.item;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.zone.RecommendedZoneComboBox;
import java.util.List;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating item masters. Item masters contain the
 * description of an item.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateIM extends DacInputFrame
{
  protected static final int MAX_DIGITS = 8;

  protected StandardInventoryServer mpInvServer = Factory.create(StandardInventoryServer.class);
  protected ItemMasterData mpDefaultItemMasterData = Factory.create(ItemMasterData.class);

  protected boolean ZONE_ENABLED = Factory.create(StandardLocationServer.class).hasRecommendedZonesDefined();

  protected SKDCTextField mpTxtItem;
  protected SKDCTextField mpTxtDescription;
  protected SKDCTranComboBox mpComboHoldType;
  protected SKDCIntegerField mpTxtPiecesPerUnit;
  protected SKDCDoubleField mpTxtLoadQty;
  protected SKDCDoubleField mpTextCCIPoint;
  protected SKDCDateField mpDateCCI;
  protected SKDCDateField mpDateModifyTime;
  protected SKDCComboBox mpComboWarehouse;
  protected RecommendedZoneComboBox mpComboZone;
  protected SKDCComboBox mpComboOrderRouteID;
  protected SKDCTranComboBox mpComboStorage;
  protected SKDCCheckBox mpCheckBoxDeleteZero;
  protected SKDCCheckBox mpCheckBoxExpirationRequired;

  protected boolean mzAdding = true;
  protected boolean mzReadOnly = false;

  /**
   *  Create item master screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateIM(String isTitle)
  {
    super(isTitle + " Item", "Item Information");
    try
    {
      initializeFields();
      buildScreen();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   *  Method to set screen for modifying.
   *
   *  @param isItemID Item to be modified.
   */
  public void setModify(String isItemID)
  {
    mzReadOnly = false;
    mzAdding = false;
    try
    {
      mpDefaultItemMasterData = mpInvServer.getItemMasterData(isItemID);
    }
    catch (DBException e2)
    {
      logAndDisplayException("Error reading Item Master", e2);
      return;
    }
    useModifyButtons();
  }

  /**
   *  Method to set screen for viewing.
   *
   *  @param isItemID Item to be viewed.
   */
  public void setView(String isItemID)
  {
    mzReadOnly = true;
    mzAdding = false;
    try
    {
      mpDefaultItemMasterData = mpInvServer.getItemMasterData(isItemID);
    }
    catch (DBException e2)
    {
      logAndDisplayException("Error reading Item Master", e2);
      return;
    }
    useReadOnlyButtons();
  }


  /**
   * Overridden method so we can set up frame for either an add or modify
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    if (!mzAdding)
    {
      mpTxtItem.setEnabled(false);
      mpDateCCI.setEnabled(true);

      if (mzReadOnly)
      {
        enablePanelComponents(mpInputPanel, false);
      }
      else
      {
        setTimeout(90);
      }
    }
    else
    {
      mpDateCCI.setEnabled(false);
      mpTxtItem.requestFocus();
    }
    setData(mpDefaultItemMasterData);
  }

  /**
   * Initialize the screen fields and add listeners as needed
   */
  protected void initializeFields()
  {
    mpTxtItem = new SKDCTextField(ItemMasterData.ITEM_NAME);
    mpTxtDescription = new SKDCTextField(ItemMasterData.DESCRIPTION_NAME);
    mpTxtPiecesPerUnit = new SKDCIntegerField(MAX_DIGITS);
    mpTxtLoadQty = new SKDCDoubleField(MAX_DIGITS);
    mpTextCCIPoint = new SKDCDoubleField(MAX_DIGITS);
    mpDateCCI = new SKDCDateField(true);
    mpDateModifyTime = new SKDCDateField(false);
    mpComboWarehouse = new SKDCComboBox();
    mpComboZone = new RecommendedZoneComboBox();
    mpComboOrderRouteID = new SKDCComboBox();
    mpCheckBoxDeleteZero = new SKDCCheckBox("Delete at Zero");
    mpCheckBoxExpirationRequired = new SKDCCheckBox("Expiration Required");

    try
    {
      mpComboHoldType = new SKDCTranComboBox(ItemMasterData.HOLDTYPE_NAME);
      mpComboStorage = new SKDCTranComboBox(ItemMasterData.STORAGEFLAG_NAME);
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }

    RecommendedWarehouseFill();
    RecommendedRouteFill();
  }

  /**
   *  This adds the components to the screen.
   */
  protected void buildScreen()
  {
    /*
     * Name & description -- first two (header) rows
     */
    addInput("Item ID", mpTxtItem);
    addInput("Description", mpTxtDescription);

    /*
     * Other rows
     */
    addInput("Hold Status", mpComboHoldType);
    addInput("Pieces Per Unit", mpTxtPiecesPerUnit);
    addInput("CCI Point", mpTextCCIPoint);
    addInput("Storage Flag", mpComboStorage);
    addInput("Storage Warehouse", mpComboWarehouse);
    if (ZONE_ENABLED)
    {
      addInput("Storage Zone", mpComboZone);
    }
    else
    {
      addInput("", new SKDCLabel(""));
    }
    addInput("", mpCheckBoxDeleteZero);
    addInput("", mpCheckBoxExpirationRequired);
    if (SKDCUserData.isSuperUser())
    {
      addInput("Default Load Quantity", mpTxtLoadQty);
      addInput("Simulation Order Route", mpComboOrderRouteID);
    }
    else
    {
      addInput("", new SKDCLabel(""));
      if (ZONE_ENABLED) addInput("", new SKDCLabel(""));
    }
    addInput("Last CCI Date", mpDateCCI);

    setHeaderRows(2);
    setInputColumns(2);

    useAddButtons();
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    mpInvServer.cleanUp();
    mpInvServer = null;
  }

  /**
   *  Method to populate the recommended warehouse combo box.
   */
  private void RecommendedWarehouseFill()
  {
    try
    {
      String[] recWarList = Factory.create(StandardLocationServer.class).getWarehouseChoices(false);
      mpComboWarehouse.setComboBoxData(recWarList, true);
    }
    catch (DBException ex)
    {
      logAndDisplayException(ex);
      return;
    }
  }

  /**
   *  Method to populate the recommended route combo box.
   */
  private void RecommendedRouteFill()
  {
    try
    {
      List recRouteList = mpInvServer.getItemRouteIDList("");
      mpComboOrderRouteID.setComboBoxData(recRouteList, true);
    }
    catch (DBException ex)
    {
      logAndDisplayException(ex);
      return;
    }
  }

  /**
   * Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(mpDefaultItemMasterData);
  }

  /**
   * Method to refresh screen fields.
   *
   * @param itemData Item master data to use in refreshing.
   */
  protected void setData(ItemMasterData itemData)
  {
    mpTxtItem.setText(itemData.getItem());
    mpTxtDescription.setText(itemData.getDescription());
    mpTxtPiecesPerUnit.setValue(itemData.getPiecesPerUnit());
    mpTextCCIPoint.setValue(itemData.getCCIPointQuantity());
    mpDateCCI.setDate(itemData.getLastCCIDate());

    mpComboWarehouse.setSelectedItem(itemData.getRecommendedWarehouse());
    mpComboZone.setSelectedItem(itemData.getRecommendedZone());
    mpComboOrderRouteID.setSelectedItem(itemData.getOrderRoute());
    mpTxtLoadQty.setValue(itemData.getDefaultLoadQuantity());

    mpCheckBoxDeleteZero.setSelected(itemData.getDeleteAtZeroQuantity());
    mpCheckBoxExpirationRequired.setSelected(itemData.getExpirationRequired());

    try
    {
      mpComboHoldType.setSelectedElement(itemData.getHoldType());
      mpComboStorage.setSelectedElement(itemData.getStorageFlag());
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      displayError("No Such Field: " + e2);
    }
  }

  /**
   * Action method to handle Close button.
   */
  @Override
  public void closeButtonPressed()
  {
    close();
  }

  /**
   * Action method to handle OK button. Verifies that entered data is valid,
   * then adds a new item master to the database.
   */
  @Override
  public void okButtonPressed()
  {
    // Check the data
    if (!checkScreenData())
    {
      return;
    }

    // Fill in IM data
    ItemMasterData vpIMData = Factory.create(ItemMasterData.class);
    if (!fillItemMasterDataWithScreenData(vpIMData))
    {
      return;
    }

    // Do the update
    try
    {
      if (mzAdding)
      {
        mpInvServer.addItemMaster(vpIMData);
        changed();
        displayInfoAutoTimeOut("Item " + mpTxtItem.getText() + " added");
      }
      else
      {
        mpInvServer.updateItemInfo(vpIMData);
        changed();
        displayInfoAutoTimeOut("Item " + mpTxtItem.getText() + " updated");
      }
    }
    catch (DBException e2)
    {
      logAndDisplayException("Error " + (mzAdding ? "adding" : "updating")
          + " item " + mpTxtItem.getText(), e2);
    }
    if (!mzAdding)
    {
      close();
    }
  }

  /**
   * Make sure the screen data is valid before adding/modifying
   *
   * @return
   */
  protected boolean checkScreenData()
  {
    boolean vzItemExists;
    try
    {
      vzItemExists = mpInvServer.itemMasterExists(mpTxtItem.getText());
    }
    catch (DBException e2)
    {
      logAndDisplayException("Error reading Item Master", e2);
      return false;
    }

    if (mzAdding && vzItemExists)
    {
      displayError("Item " + mpTxtItem.getText() + " already exists");
      return false;
    }

    if (!mzAdding && !vzItemExists)
    {
      displayError("Item " + mpTxtItem.getText() + " does not exist");
      return false;
    }

    return true;
  }

  /**
   * Fill an ItemMasterData object with the screen data.
   *
   * @param ipIMData
   * @return
   */
  protected boolean fillItemMasterDataWithScreenData(ItemMasterData ipIMData)
  {
    if (mzAdding)
    {
      ipIMData.setItem(mpTxtItem.getText());
      if (ipIMData.getItem().length() <= 0)  // required
      {
        displayError("Item name is required");
        return false;
      }
      try
      {
        // make sure item is not a synonym
        if (mpInvServer.existSynonym(mpTxtItem.getText()))
        {
          displayError("Item " + mpTxtItem.getText() + " exists as a synonym");
          return false;
        }
      }
      catch (DBException e2)
      {
        logAndDisplayException(e2);
        return false;
      }
    }

    ipIMData.setItem(mpTxtItem.getText());
    ipIMData.setDescription(mpTxtDescription.getText());
    ipIMData.setPiecesPerUnit(mpTxtPiecesPerUnit.getValue());
    ipIMData.setCCIPointQuantity(mpTextCCIPoint.getValue());
    ipIMData.setLastCCIDate(mpDateCCI.getDate());

    ipIMData.setRecommendedWarehouse(mpComboWarehouse.getText());
    ipIMData.setRecommendedZone(mpComboZone.getText());
    ipIMData.setOrderRoute(mpComboOrderRouteID.getText());
    ipIMData.setDefaultLoadQuantity(mpTxtLoadQty.getValue());

    try
    {
      ipIMData.setHoldType(mpComboHoldType.getIntegerValue());
      ipIMData.setStorageFlag(mpComboStorage.getIntegerValue());
    }
    catch (NoSuchFieldException e2)
    {
      logAndDisplayError("No Such Field: " + e2);
    }

    ipIMData.setDeleteAtZeroQuantity(mpCheckBoxDeleteZero.isSelectedYesNo());
    ipIMData.setExpirationRequired(mpCheckBoxExpirationRequired.isSelectedYesNo());

    return true;
  }
}

