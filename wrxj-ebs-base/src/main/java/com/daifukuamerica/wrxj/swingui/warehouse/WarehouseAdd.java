package com.daifukuamerica.wrxj.swingui.warehouse;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import java.awt.event.ActionEvent;

/**
 * Description:<BR>
 *    Sets up the Warehouse add internal frame.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 02-Apr-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class WarehouseAdd extends DacInputFrame
{
  private static final long serialVersionUID = 1L;

  private WarehouseData wtdata = Factory.create(WarehouseData.class);
  
  private SKDCTextField    txtWarehouse;
  private SKDCTextField    txtDescription;
  private SKDCTranComboBox comboWarehouseType;
  private SKDCTranComboBox comboWarehouseStatus;
  private SKDCTranComboBox comboOneLoadPerLoc;
  private SKDCTextField    mpTxtEquipWarehouse;

  private StandardLocationServer     mpLocServer = null;

  public WarehouseAdd(StandardLocationServer ipLocServer)
  {
    super("Add Warehouse", "Warehouse Information");
    mpLocServer = ipLocServer;

    try
    {
      buildScreen();
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
    }
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

    comboWarehouseType.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          try
          {
            boolean vpIsRegWarehouse = (comboWarehouseType.getIntegerValue() == DBConstants.REGULAR);
            
//            if (!vpIsRegWarehouse)
//            {
//              comboOneLoadPerLoc.setSelectedElement(DBConstants.NO);
//            }
            comboOneLoadPerLoc.setEnabled(vpIsRegWarehouse);
          }
          catch (NoSuchFieldException nsfe)
          {
            displayError(nsfe.getMessage(), "Translation Error");
          }
        }
      });

    addInput("Warehouse:", txtWarehouse);
    addInput("Description:", txtDescription);
    addInput("Warehouse Type:", comboWarehouseType);
    addInput("Warehouse Status:", comboWarehouseStatus);
    addInput("One Load per Location:", comboOneLoadPerLoc);
    if (SKDCUserData.isSuperUser())
    {
      addInput("Equipment Warehouse:", mpTxtEquipWarehouse);
    }
    
    clearButtonPressed();
    
    useAddButtons();
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
    wtdata.clear();
    
    txtWarehouse.setText("");
    txtDescription.setText("");
    mpTxtEquipWarehouse.setText("0");
                                       // Reset Combo-Boxes to orig. selection
    try
    {
      comboWarehouseStatus.setSelectedElement(wtdata.getWarehouseStatus());
      comboWarehouseType.setSelectedElement(wtdata.getWarehouseType());
      comboOneLoadPerLoc.setSelectedElement(wtdata.getOneLoadPerLoc());
    }
    catch (NoSuchFieldException nsfe)
    {
      displayError(nsfe.getMessage());
    }
    
    txtWarehouse.requestFocus();
  }

  /**
   *  Processes Warehouse add request.  This method stuffs column objects into
   *  a WarehouseData instance container and calls the Warehouse Server.
   */
  @Override
  protected void okButtonPressed()
  {                                    // Make sure everything is defaulted
    wtdata.clear();                    // to begin with.

    if (txtWarehouse.getText().trim().length() == 0)
    {
      displayInfoAutoTimeOut("Warehouse required", "Data Entry Error");
      txtWarehouse.requestFocus();
      return;
    }
		
    if(mpLocServer.doesWarehouseExist(txtWarehouse.getText().trim()))
    {
      displayInfoAutoTimeOut("Warehouse already exists", "Data Entry Error");
      txtWarehouse.requestFocus();
      return;
    }

                                       // Put in Warehouse.
    wtdata.setWarehouse(txtWarehouse.getText().trim());
                                       // Put in description.
    wtdata.setDescription(txtDescription.getText());

    try
    {
      wtdata.setWarehouseType(comboWarehouseType.getIntegerValue());
      wtdata.setWarehouseStatus(comboWarehouseStatus.getIntegerValue());
      wtdata.setOneLoadPerLoc(comboOneLoadPerLoc.getIntegerValue());
    }
    catch(NoSuchFieldException e)
    {
      displayError(e.getMessage(), "Translation Error");
      return;
    }

    wtdata.setEquipWarehouse(mpTxtEquipWarehouse.getText());
    
    String confirm_mesg = null;
    try
    {
      confirm_mesg = mpLocServer.addWarehouse(wtdata);
/*---------------------------------------------------------------------------
  Add a blank super warehouse field to make sure display is updated properly.
  (When the ColumnObject array is built from this wtdata structure to add the
  row to the display, it needs to reflect properly that the Super warehouse is
  blank.)
---------------------------------------------------------------------------*/
      wtdata.setSuperWarehouse("");
      this.firePropertyChange(FRAME_CHANGE, null, wtdata);
      displayInfoAutoTimeOut(confirm_mesg, "Confirmation");
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "DB Error");
      return;
    }

    clearButtonPressed();

    return;
  }
}
