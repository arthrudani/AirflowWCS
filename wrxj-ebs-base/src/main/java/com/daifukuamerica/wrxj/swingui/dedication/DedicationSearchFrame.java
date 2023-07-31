package com.daifukuamerica.wrxj.swingui.dedication;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDedicationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.List;

/**
 * Description:<BR>
 *    Sets up the Dedication Search internal frame.  It fills in the contents
 *    of a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * <B>NOTE:</B> As Dedicated Type is not yet implemented, all references to it 
 * have been commented out.
 *
 * @author       mandrus<BR>
 * @version      1.0
 * <BR>Created: Feb 18, 2005<BR>
 *     Copyright (c) 2005<BR>
 *     Company:  Daifuku America Corporation
 */
public class DedicationSearchFrame extends DacInputFrame
{
  private static final long serialVersionUID = 0L;
  
  private StandardDedicationServer mpDedServer;
  private DedicatedLocationData dldata;
  private DedicatedLocationData searchCriteria;
  
  private List               datalist;
  private String[]           whs_all_list;
  private LocationPanel      lcPanel;
  private SKDCTextField      txtItem;
//  private SKDCTranComboBox   comboDedicatedType;
  private SKDCTranComboBox   comboReplenishNow;

  public DedicationSearchFrame(StandardDedicationServer ipDedServer, String[] whs_list)
  {
    super("Dedication Search", "Dedication Information");

    mpDedServer = ipDedServer;
    whs_all_list = whs_list;

    dldata = Factory.create(DedicatedLocationData.class);
    
    try
    {
      buildScreen();
    }
    catch(NoSuchFieldException err)
    {
      displayWarning(err.getMessage(), "Translation Warning");
    }
  }

  /**
   *  Builds the screen
   */
  private void buildScreen() throws NoSuchFieldException
  {
    lcPanel = new LocationPanel();
    lcPanel.setWarehouseList(whs_all_list);
    txtItem = new SKDCTextField(DedicatedLocationData.ITEM_NAME);
    //comboDedicatedType = new SKDCTranComboBox("iDedicatedType", true);
    comboReplenishNow = new SKDCTranComboBox(DedicatedLocationData.REPLENISHNOW_NAME, true);
    
    addInput("Search Location:", lcPanel);
    addInput("Item ID:"        , txtItem);
    //addInput("Dedication Type:", comboDedicatedType);
    addInput("Replenish Flag:" , comboReplenishNow);
    
    useSearchButtons();
  }

  public DedicatedLocationData getSearchCriteria()
  {
    return(searchCriteria);
  }

  /**
   * Processes Search Location request.
   */
  @Override
  protected void okButtonPressed()
  {
    //  Make sure everything is defaulted to begin with
    dldata.clear();

    if (!lcPanel.getWarehouseString().equals(SKDCConstants.ALL_STRING))
    {
      // Store Warehouse entry.  Warehouse is a mandatory field.
      dldata.setKey(DedicatedLocationData.WAREHOUSE_NAME, lcPanel.getWarehouseString());
    }

    // Store Address entry from Bank, Bay, and Tier.
    String sAddress = "";
    try
    {
      sAddress = lcPanel.getAddressString();
    }
    catch(DBException e)
    {
      displayError("Error retrieving LocationAddress string", "Data Error");
      return;
    }

    if (sAddress.trim().length() != 0)
    {
      dldata.setKey(DedicatedLocationData.ADDRESS_NAME, sAddress, KeyObject.LIKE);
    }

    // Set up Item key.
    if (txtItem.getText().trim().length() != 0)
    {
      dldata.setKey(DedicatedLocationData.ITEM_NAME, txtItem.getText().trim(), KeyObject.LIKE);
    }

    try
    {
      // Set up Dedicated Type key.
      // dldata.setKey(dldata.getDedicatedTypeName(), comboDedicatedType.getIntegerObject());
      
       // Set up Replenish Now key.
      dldata.setKey(DedicatedLocationData.REPLENISHNOW_NAME, comboReplenishNow.getIntegerObject());
    }
    catch(NoSuchFieldException e)
    {
      displayWarning(e.getMessage(), "Translation Error");
    }
    
    //  Save off the search criteria.
    searchCriteria = (DedicatedLocationData)dldata.clone();

    // Call the location server and fetch the data based on the search criteria.
    // If data comes back, notify main screen of change.
    try
    {
      datalist = mpDedServer.getDedications(dldata);
      if (datalist != null && datalist.size() != 0)
      {
        changed(null, datalist);
        close();
      }
      else
      {
        displayInfoAutoTimeOut("No data found.", "Search Information");
        clearButtonPressed();
      }
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "Search Error");
    }
  }

  /**
   * Cancel Button handler.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    lcPanel.reset();
    txtItem.setText("");
    //comboDedicatedType.resetDefaultSelection();
    comboReplenishNow.resetDefaultSelection();
  }
}
