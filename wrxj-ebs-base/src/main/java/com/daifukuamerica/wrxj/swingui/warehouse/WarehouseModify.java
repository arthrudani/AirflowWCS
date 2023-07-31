package com.daifukuamerica.wrxj.swingui.warehouse;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;

/**
 * Description:<BR>
 *    Sets up the Warehouse modify internal frame.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 04-Apr-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class WarehouseModify extends DacInputFrame
{
  protected static final long serialVersionUID = 0;

  private WarehouseData old_wtdata = null;
  private WarehouseData new_wtdata = Factory.create(WarehouseData.class);

  private SKDCTextField    txtWarehouse;
  private SKDCTextField    txtDescription;
  private SKDCTranComboBox comboWarehouseType;
  private SKDCTranComboBox comboWarehouseStatus;
  private SKDCTranComboBox comboOneLoadPerLoc;
  private SKDCTextField    mpTxtEquipWarehouse;
  
  private StandardLocationServer     mpLocServer = null;

  public WarehouseModify(StandardLocationServer ipLocServer, 
      WarehouseData ipOldWarData)
  {
    super("Modify Warehouse", "Warehouse Information");
    mpLocServer = ipLocServer;
    old_wtdata = ipOldWarData;

    try
    {
      buildScreen();
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
    }
    
    insertData(ipOldWarData);
    txtDescription.requestFocus();
  }


  /*========================================================================*/
  /*  Methods for display formatting                                        */
  /*========================================================================*/
  /**
   * Builds the update form
   * 
   * @throws NoSuchFieldException
   */
  private void buildScreen() throws NoSuchFieldException
  {
    txtWarehouse         = new SKDCTextField(WarehouseData.WAREHOUSE_NAME);
    txtDescription       = new SKDCTextField(WarehouseData.DESCRIPTION_NAME);
    comboWarehouseType   = new SKDCTranComboBox(WarehouseData.WAREHOUSETYPE_NAME);
    comboWarehouseStatus = new SKDCTranComboBox(WarehouseData.WAREHOUSESTATUS_NAME);
    comboOneLoadPerLoc   = new SKDCTranComboBox(WarehouseData.ONELOADPERLOC_NAME);
    mpTxtEquipWarehouse  = new SKDCTextField(WarehouseData.EQUIPWAREHOUSE_NAME);
    mpTxtEquipWarehouse.setText("0");

    /*
     * You can't change the following for super-warehouses
     */
    if (old_wtdata.getWarehouseType() == DBConstants.SUPER)
    {
      comboOneLoadPerLoc.setEnabled(false);
    }
    
    addInput("Warehouse:", txtWarehouse);
    addInput("Description:", txtDescription);
    addInput("Warehouse Status:", comboWarehouseStatus);
    addInput("One Load per Location:", comboOneLoadPerLoc);
    if (SKDCUserData.isSuperUser())
    {
      addInput("Equipment Warehouse:", mpTxtEquipWarehouse);
    }

    useModifyButtons();
  }

/*===========================================================================
              Methods for event handling go in this section.
===========================================================================*/
  /**
   * Cancel button event handler.  This method destroys this frame, and fires a
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
    insertData(old_wtdata);            // Reload old data into screen fields.
    txtDescription.requestFocus();
  }

  /**
   *  Processes Warehouse add request.  This method stuffs column objects into
   *  a WarehouseData instance container and calls the Warehouse Server.
   */
  @Override
  protected void okButtonPressed()
  {                                    // Make sure everything is defaulted
    new_wtdata.clear();                // to begin with.

                                       // Put in description.
    new_wtdata.setDescription(txtDescription.getText());
    
    try
    {                                  // Put in Warehouse status
      new_wtdata.setWarehouseType(comboWarehouseType.getIntegerValue());
      new_wtdata.setWarehouseStatus(comboWarehouseStatus.getIntegerValue());
      new_wtdata.setOneLoadPerLoc(comboOneLoadPerLoc.getIntegerValue());
    }
    catch(NoSuchFieldException e)
    {
      displayError(e.getMessage(), "Translation Error");
      return;
    }
    
    new_wtdata.setEquipWarehouse(mpTxtEquipWarehouse.getText());

    String confirm_mesg = null;
    try
    {
      new_wtdata.setKey(WarehouseData.SUPERWAREHOUSE_NAME,
                        old_wtdata.getSuperWarehouse());
      new_wtdata.setKey(WarehouseData.WAREHOUSE_NAME, txtWarehouse.getText());

      confirm_mesg = mpLocServer.modifyWarehouse(new_wtdata);
      changed(null, new_wtdata);
      displayInfoAutoTimeOut(confirm_mesg, "Confirmation");
    }
    catch(Exception e)
    {
      displayError(e.getMessage(), "DB Error");
      return;
    }

    closeButtonPressed();            // We are done.

    return;
  }

  /**
   *   Inserts selected row data into modify Warehouse Screen.
   */
  public void insertData(WarehouseData wtdata)
  {
    txtWarehouse.setText(wtdata.getWarehouse());
    txtDescription.setText(wtdata.getDescription());
    mpTxtEquipWarehouse.setText(wtdata.getEquipWarehouse());
    
    try
    {
      comboWarehouseType.setSelectedElement(wtdata.getWarehouseType());
      comboWarehouseStatus.setSelectedElement(wtdata.getWarehouseStatus());
      comboOneLoadPerLoc.setSelectedElement(wtdata.getOneLoadPerLoc());
    }
    catch(NoSuchFieldException e)
    {
      displayError(e.getMessage(), "Translation Error");
    }

    return;
  }
}
