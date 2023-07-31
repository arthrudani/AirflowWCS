package com.daifukuamerica.wrxj.swingui.warehouse;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;

/**
 * Description:<BR>
 *    Sets up the Super Warehouse add internal frame.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 02-Apr-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class LinkSuperWarehouse extends DacInputFrame
{
  private static final long serialVersionUID = 0L;
  
  private WarehouseData  mpWarehouseData    = Factory.create(WarehouseData.class);
  private WarehouseData  mpOldWarehouseData = null;
  private SKDCTextField  txtSuperWarehouse  = new SKDCTextField(WarehouseData.SUPERWAREHOUSE_NAME);
  private SKDCTextField  txtWarehouse       = new SKDCTextField(WarehouseData.WAREHOUSE_NAME);
  private StandardLocationServer mpLocServer        = null;

  public LinkSuperWarehouse(StandardLocationServer lc_Server, 
      WarehouseData old_wtdata)
  {
                                       // This internal frame will be
                                       // resizable, but not closable.
    super("Link Super Warehouse", "Warehouse Link");
    mpLocServer = lc_Server;
    mpOldWarehouseData = old_wtdata;

    buildScreen();
    insertData();
  }

/*===========================================================================
              Methods for display formatting go in this section.
===========================================================================*/
  private void buildScreen()
  {
    txtWarehouse.setEnabled(false);

    addInput("Warehouse:", txtWarehouse);
    addInput("Parent Warehouse:", txtSuperWarehouse);
  }

/*===========================================================================
              Methods for event handling go in this section.
===========================================================================*/
  /**
   * Cancel button event handler.  This method destroys this frame and fires a
   * FRAME_CLOSE event.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Add Location dialog.
   */
  @Override
  protected void clearButtonPressed()
  {
    txtSuperWarehouse.setText("");
    txtSuperWarehouse.requestFocus();
    insertData();
  }

  /**
   *  Processes Super Warehouse linkage request.  This method stuffs column
   *  objects into a WarehouseData instance container and calls the
   *  Warehouse Server.
   */
  @Override
  protected void okButtonPressed()
  {
    int countBeforeMod = 0, countAfterMod = 0;
    String vsSuperWarehouse = txtSuperWarehouse.getText().trim();
    

    mpWarehouseData.clear();
                                       // Take snapshot count before we make
                                       // change.
    try { countBeforeMod = mpLocServer.getWarehouseCount(mpWarehouseData); }
    catch(DBException e) { displayError(e.getMessage()); }

    if (vsSuperWarehouse.length() == 0)
    {
      displayError("Super Warehouse required", "Entry Error");
      txtSuperWarehouse.requestFocus();
      return;
    }
                                       // Before linking super and child warehouse
                                       // see if the super warehouse really
                                       // exists.
    mpWarehouseData.setKey(WarehouseData.WAREHOUSE_NAME, vsSuperWarehouse);
    if (mpLocServer.exists(mpWarehouseData) == true)
    {
      mpWarehouseData.setKey(WarehouseData.WAREHOUSETYPE_NAME, Integer.valueOf(DBConstants.REGULAR));

      if (mpLocServer.exists(mpWarehouseData) == true)
      {
        displayError(vsSuperWarehouse + " is not a Super Warehouse", "Entry Error");
        txtSuperWarehouse.requestFocus();
        return;
      }
    }
    else
    {
      if (!displayYesNoPrompt(vsSuperWarehouse + " does not exist.  Add it", "Add warehouse?"))
      {
        return;
      }
    }
    
    // Put in parent warehouse.
    mpWarehouseData.setSuperWarehouse(vsSuperWarehouse);
                                       // Put in Warehouse.
    mpWarehouseData.setWarehouse(txtWarehouse.getText());

    String mesg = null;
    try
    {
      mesg = mpLocServer.modifySuperLink(mpOldWarehouseData.getSuperWarehouse(),
                                        mpOldWarehouseData.getWarehouse(),
                                        mpWarehouseData.getSuperWarehouse());
      displayInfo(mesg, "Confirmation");
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "DB Error");
      return;
    }

    mpWarehouseData.clear();
                                       // Take snapshot count after our changes.
                                       // If there are more records than before
                                       // the mod. refresh dataset.
    try { countAfterMod = mpLocServer.getWarehouseCount(mpWarehouseData); }
    catch(DBException e) { displayError(e.getMessage()); }

    if (countBeforeMod == countAfterMod)
    {
      changed(null, vsSuperWarehouse);
    }
    else
    {
      this.firePropertyChange(REFRESH_NOTIFY, false, true);
    }
    closeButtonPressed();

    return;
  }

  /**
   *   Inserts selected row data into modify Warehouse Screen.
   */
  public void insertData()
  {
    txtWarehouse.setText(mpOldWarehouseData.getWarehouse());
    txtSuperWarehouse.setText(mpOldWarehouseData.getSuperWarehouse());

    return;
  }
}
