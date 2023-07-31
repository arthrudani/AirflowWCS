package com.daifukuamerica.wrxj.swingui.load;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.InKeyObject;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.swingui.zone.RecommendedZoneComboBox;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A screen class for collecting detailed search criteria for a list of loads.
 * 
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class LoadDetailedSearchFrame extends DacInputFrame
{
  private SKDCTextField textLoadID;
  private SKDCTextField textParentLoadID;
  private SKDCTranComboBox statusComboBox;
  private SKDCComboBox containerComboBox;
  private SKDCTranComboBox fullnessComboBox;
  private RecommendedZoneComboBox zoneComboBox;
  private SKDCComboBox deviceComboBox;
  private LocationPanel location;
  private LocationPanel destination;
  private SKDCTextField textOrder;
  private SKDCTextField textItem;

  private StandardInventoryServer mpInvServer = Factory.create(StandardInventoryServer.class);

  /**
   * Create load search frame.
   */
  public LoadDetailedSearchFrame()
  {
    super("Load Search", "Load Information");
    try
    {
      buildScreen();
    }
    catch (Exception exc)
    {
      logger.logException("LoadDetailedSearchFrame", exc);
    }
  }

  /**
   * Method to initialize screen components. This adds the components to the
   * screen and adds listeners as needed.
   * 
   * @exception Exception
   */
  private void buildScreen() throws Exception
  {
    location = Factory.create(LocationPanel.class);
    destination = Factory.create(LocationPanel.class);
    location.setWarehouseList(LocationPanel.WTYPE_REGULAR, true);
    destination.setWarehouseList(LocationPanel.WTYPE_REGULAR, true);
    textLoadID = new SKDCTextField(LoadData.LOADID_NAME);
    textParentLoadID = new SKDCTextField(LoadData.PARENTLOAD_NAME);
    textOrder = new SKDCTextField(LoadLineItemData.ORDERID_NAME);
    textItem = new SKDCTextField(LoadLineItemData.ITEM_NAME);
    
    zoneComboBox = new RecommendedZoneComboBox();
    containerComboBox = new SKDCComboBox();
    deviceComboBox = new SKDCComboBox();

    try
    {
      statusComboBox = new SKDCTranComboBox(LoadData.LOADMOVESTATUS_NAME, true);
      fullnessComboBox = new SKDCTranComboBox(LoadData.AMOUNTFULL_NAME, true);
    }
    catch (NoSuchFieldException exc)
    {
      logger.logException("LoadDetailedSearchFrame", exc);
    }

    addInput("Load:", textLoadID);
    addInput("Parent Load:", textParentLoadID);
    addInput("Move Status:", statusComboBox);
    addInput("Container:", containerComboBox);
    addInput("Amount Full:", fullnessComboBox);
    if (Factory.create(StandardLocationServer.class).hasRecommendedZonesDefined())
      addInput("Zone:", zoneComboBox);
    addInput("Device:", deviceComboBox);
    addInput("Location:", location);
    addInput("Destination:", destination);
    addInput("Order:", textOrder);
    addInput("Item:", textItem);

    useSearchButtons();

    fillContainerCombo();
    fillDeviceCombo();
  }

  /**
   * Method to populate the container type combo box.
   */
  private void fillContainerCombo()
  {
    try
    {
      List containerList = mpInvServer.getContainerTypeList();
      containerComboBox.setDisplayAllEnabled(true);
      containerComboBox.setComboBoxData(containerList);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }

  /**
   * Method to populate the container type combo box.
   */
  private void fillDeviceCombo()
  {
    StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
    deviceComboBox.setDisplayAllEnabled(true);
    try
    {
      String[] vpDeviceList = vpLocServer.getDeviceIDList(false);
      deviceComboBox.setComboBoxData(vpDeviceList);
    }
    catch (DBException exc)
    {
      logger.logException("LoadDetailedSearchFrame", exc);
    }
    vpLocServer.cleanUp();
  }

  /**
   * Method to clean up as needed at closing.
   * 
   */
  @Override
  public void cleanUpOnClose()
  {
    // invtServ.cleanUp();
    // invtServ = null;
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
   * Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    deviceComboBox.getEditor().setItem("");
    zoneComboBox.setSelectedItem("");
    containerComboBox.getEditor().setItem("");
    textLoadID.setText("");
    textParentLoadID.setText("");
    textOrder.setText("");
    textItem.setText("");
    destination.reset();
    location.reset();
  }

  /**
   * Action method to handle Search button. Method fires a property change event
   * so parent frame can refresh its display.
   */
  @Override
  protected void okButtonPressed()
  {
    changed();
  }

  /**
   * Method to get the entered search criteria as a LoadData.
   * 
   * @return LoadData containing criteria to use in search
   */
  public LoadData getSearchData()
  {
    LoadData vpLoadData = Factory.create(LoadData.class);

    if (textLoadID.getText().length() > 0)
    {
      KeyObject loadKey = new KeyObject(LoadData.LOADID_NAME,
          textLoadID.getText());
      // Accept partial load searches.
      loadKey.setComparison(KeyObject.LIKE);
      vpLoadData.addKeyObject(loadKey);
    }
    if (textParentLoadID.getText().length() != 0)
    {
      vpLoadData.addKeyObject(new KeyObject(LoadData.PARENTLOAD_NAME,
          textParentLoadID.getText()));
    }
    String vsDeviceID = deviceComboBox.getSelectedItem().toString().trim();
    if (vsDeviceID.length() > 0)
    {
      vpLoadData.addKeyObject(new KeyObject(LoadData.DEVICEID_NAME, vsDeviceID));
    }
    String vsZone = zoneComboBox.getSelectedItem().toString().trim();
    if (vsZone.length() > 0)
    {
      vpLoadData.addKeyObject(new KeyObject(LoadData.RECOMMENDEDZONE_NAME,
          vsZone));
    }
    String vsContainer = containerComboBox.getSelectedItem().toString().trim();
    if (vsContainer.length() > 0)
    {
      vpLoadData.addKeyObject(new KeyObject(LoadData.CONTAINERTYPE_NAME,
          vsContainer));
    }
    try
    {
      vpLoadData.addKeyObject(new KeyObject(statusComboBox.getTranslationName(),
          statusComboBox.getIntegerObject()));
      vpLoadData.addKeyObject(new KeyObject(fullnessComboBox.getTranslationName(),
          fullnessComboBox.getIntegerObject()));
      if (location.getWarehouseString().length() > 0)
      {
        vpLoadData.addKeyObject(new KeyObject(LoadData.WAREHOUSE_NAME,
            location.getWarehouseString().trim()));
        if (location.getAddressString().length() > 0)
        {
          KeyObject addressKey = new KeyObject(LoadData.ADDRESS_NAME,
              location.getAddressString().trim());
          addressKey.setComparison(KeyObject.LIKE);
          vpLoadData.addKeyObject(addressKey);
        }
      }
      if (destination.getWarehouseString().length() > 0)
      {
        vpLoadData.addKeyObject(new KeyObject(LoadData.FINALWAREHOUSE_NAME,
            destination.getWarehouseString().trim()));
        if (destination.getAddressString().length() > 0)
        {
          KeyObject destAddrKey = new KeyObject(LoadData.FINALADDRESS_NAME,
              destination.getAddressString().trim());
          destAddrKey.setComparison(KeyObject.LIKE);
          vpLoadData.addKeyObject(destAddrKey);
        }
      }
    }
    catch (Exception exc)
    {
      logAndDisplayException(exc);
    }

    try
    {
      // Build the key for stuff not in the load
      LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
      String vsOrder = textOrder.getText();
      if (vsOrder.length() > 0)
      {
        vpLLIData.addKeyObject(new KeyObject(LoadLineItemData.ORDERID_NAME,
            vsOrder));
      }
      String vsItem = textItem.getText();
      if (vsItem.length() > 0)
      {
        vpLLIData.addKeyObject(new KeyObject(LoadLineItemData.ITEM_NAME, vsItem));
      }
      // If the key is not empty, get matching loads
      if (vpLLIData.getKeyArray().length > 0)
      {
        List<Map> vpLLIList = mpInvServer.getLoadLineItemDataList(vpLLIData);
        if (vpLLIList.size() == 0)
        {
          // No matches
          vpLoadData.addKeyObject(new KeyObject(LoadData.LOADID_NAME, ""));
        }
        else
        {
          // Matches, but may not be unique if multiple items per load
          // Note: Oracle croaks if there are > 1000 loads in the set
          Set<String> vpLoadSet = new TreeSet<String>(); 
          for (Map m : vpLLIList)
          {
            vpLLIData.clear();
            vpLLIData.dataToSKDCData(m);
            vpLoadSet.add(vpLLIData.getLoadID());
          }
          vpLoadData.addKeyObject(new InKeyObject(LoadData.LOADID_NAME,
              vpLoadSet.toArray()));
        }
      }
    }
    catch (DBException exc)
    {
      logAndDisplayException(exc);
    }
    
    return vpLoadData;
  }

}