package com.daifukuamerica.wrxj.swingui.dedication;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDedicationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;

/**
 * Description:<BR>
 *    Sets up the Dedication add/modify internal frame.  It fills in the 
 *    contents of a JPanel before adding it to the Internal frame.  This method
 *    then returns the Internal Frame reference to the caller so that it can be
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
public class DedicationUpdateFrame extends DacInputFrame
{
  private static final long serialVersionUID = 0L;
  
  private DedicatedLocationData mpCurrentDLData, mpNewDLData;
  private StandardDedicationServer mpDedServer;
  private boolean mbAddMode;

  private SKDCTextField     txtItem;
  private LocationPanel     lcPanel;
  private LocationPanel     moveLCPanel;
//  private SKDCTranComboBox  comboDedicatedType;
  private SKDCDoubleField   dblMinimum;
  private SKDCDoubleField   dblMaximum;
  private SKDCDoubleField   dblCurrent;
  private SKDCTranComboBox  comboReplenishNow;
  private SKDCTranComboBox  comboReplenishType;

  private String[]           mListWarehouse;

  /*========================================================================*/
  /*  Constructors                                                          */
  /*========================================================================*/
  
  /**
   * Universal constructor
   *  
   * @param mode 
   * @param pDL
   * @param warhse_list
   * @param pDLData
   */
  private DedicationUpdateFrame(String mode, StandardDedicationServer ipDedServer, 
      String[] pListWarehouse, DedicatedLocationData pDLData)
    throws NumberFormatException
  {
    super(mode + " Dedication", "Dedication Information");
    
    mpDedServer = ipDedServer;
    mListWarehouse = pListWarehouse;
    if (pDLData != null)
    {
      mpCurrentDLData = (DedicatedLocationData) pDLData.clone();
    }
    else
    {
      mpCurrentDLData = pDLData;
    }
    mbAddMode = (pDLData == null);
    
    mpNewDLData = Factory.create(DedicatedLocationData.class);
    
    try
    {
      buildScreen();
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
    }
    clearButtonPressed();
  }
  
  /**
   * Constructor for ADD
   * 
   * @param pDL
   * @param pListWarehouse
   * @throws NumberFormatException
   */
  public DedicationUpdateFrame(StandardDedicationServer pDedServer, String[] pListWarehouse)
    throws NumberFormatException
  {
    this("Add", pDedServer, pListWarehouse, null);
    useAddButtons();
  }
    
  /**
   * Constructor for MODIFY/MOVE
   * 
   * @param pDL
   * @param pListWarehouse
   * @param pDLData
   * @throws NumberFormatException
   */
  public DedicationUpdateFrame(StandardDedicationServer pDedServer, 
      String[] pListWarehouse, DedicatedLocationData pDLData)
    throws NumberFormatException
  {
    this("Modify", pDedServer, pListWarehouse, pDLData);
    useModifyButtons();
  }

  /*========================================================================*/
  /*  Methods for display formatting                                        */
  /*========================================================================*/
  /**
   * Builds the screen on the update form
   */
  private void buildScreen() throws NoSuchFieldException
  {
    txtItem = new SKDCTextField(DedicatedLocationData.ITEM_NAME);
    txtItem.setEnabled(mbAddMode);

    lcPanel = Factory.create(LocationPanel.class);
    lcPanel.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
    lcPanel.setEnabled(mbAddMode);

    moveLCPanel = Factory.create(LocationPanel.class);
    moveLCPanel.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);

    lcPanel.setWarehouseList(mListWarehouse);
    moveLCPanel.setWarehouseList(mListWarehouse);
    dblMinimum = new SKDCDoubleField("MinimumQuantity");
    dblMaximum = new SKDCDoubleField(DedicatedLocationData.MAXIMUMQUANTITY_NAME);
//    comboDedicatedType = new SKDCTranComboBox(dldata.getDedicatedTypeName());
    dblCurrent = new SKDCDoubleField(DedicatedLocationData.MAXIMUMQUANTITY_NAME);
    dblCurrent.setEnabled(false);
    comboReplenishNow = new SKDCTranComboBox(DedicatedLocationData.REPLENISHNOW_NAME);
    comboReplenishType = new SKDCTranComboBox(DedicatedLocationData.REPLENISHTYPE_NAME);

    addInput("Item:", txtItem);
    addInput("Location:", lcPanel);
    if (!mbAddMode)
    {
      addInput("New Location:", moveLCPanel);
    }
//    addInput("Dedication Type:", comboDedicatedType);
    addInput("Minimum Quantity:", dblMinimum);
    addInput("Maximum Quantity:", dblMaximum);
    if (!mbAddMode)
    {
      addInput("Current Quantity:", dblCurrent);
    }
    addInput("Replenish Flag:", comboReplenishNow);
    addInput("Replenish Type:", comboReplenishType);
  }

  /*========================================================================*/
  /*  Methods for event handling                                            */
  /*========================================================================*/
  /**
   *  Add/update the dedication
   */
  @Override
  protected void okButtonPressed()
  {
    String info;
    
    mpNewDLData.clear();               // Make sure everything is defaulted
                                       // to begin with.

    mpNewDLData.setItem(txtItem.getText());
    try
    {
      if (mbAddMode)
      {
        mpNewDLData.setWarehouse(lcPanel.getWarehouseString());
        mpNewDLData.setAddress(lcPanel.getAddressString());
      }
      else
      {
        mpNewDLData.setWarehouse(moveLCPanel.getWarehouseString());
        mpNewDLData.setAddress(moveLCPanel.getAddressString());
      }
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "Database Error");
    }

    try
    {
//      dldata.setDedicatedType(comboDedicatedType.getIntegerValue());
      mpNewDLData.setReplenishNow(comboReplenishNow.getIntegerValue());
      mpNewDLData.setReplenishType(comboReplenishType.getIntegerValue());
    }
    catch(NoSuchFieldException e)
    {
      displayError(e.getMessage(), "Translation Error");
    }

    mpNewDLData.setMinimumQuantity(dblMinimum.getValue());
    mpNewDLData.setMaximumQuantity(dblMaximum.getValue());
    
    DedicatedLocationData addData = (DedicatedLocationData)mpNewDLData.clone();

                                       // Call on the location server to add
    try                                // set of locations
    {
      if (mbAddMode)
      {
        info = mpDedServer.addDedication(addData);
      }
      else
      {
        if (mpCurrentDLData.equals(addData))
        {
          info = mpDedServer.updateDedication(addData,true);
        }
        else
        {
          boolean vzMoveNow = false;
          
          if ((mpCurrentDLData.getAddress().trim().length() > 0) &&
              (addData.getAddress().trim().length() > 0))
          {
            vzMoveNow = displayYesNoPrompt("Transfer inventory now");
          }
          
          info = mpDedServer.moveDedication(mpCurrentDLData, addData, vzMoveNow);
        }
      }
      displayInfoAutoTimeOut(info, "Rows Added");
      changed(null, addData);
      Thread.sleep(30);
      close();
    }
    catch(Exception exc)
    {
      logAndDisplayException("Error Adding Dedication", exc);
    }
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Update Location dialog.
   */
  @Override
  protected void clearButtonPressed()
  {                                    // Clear out the text box.
    if (mbAddMode)
    {
      txtItem.setText("");
      lcPanel.reset();
//      comboDedicatedType.resetDefaultSelection();
      comboReplenishNow.resetDefaultSelection();
      comboReplenishType.resetDefaultSelection();
      dblMinimum.setValue(0.0);
      dblMaximum.setValue(1.0);
    
      txtItem.requestFocus();
    }
    else
    {
      txtItem.setText(mpCurrentDLData.getItem());
      lcPanel.reset(mpCurrentDLData.getWarehouse(), mpCurrentDLData.getAddress());
      if (!mbAddMode)
      {
        moveLCPanel.reset(mpCurrentDLData.getWarehouse(), mpCurrentDLData.getAddress());
      }

      try
      {
//        comboDedicatedType.setSelectedElement(current_dldata.getDedicatedType());
        comboReplenishNow.setSelectedElement(mpCurrentDLData.getReplenishNow());
        comboReplenishType.setSelectedElement(mpCurrentDLData.getReplenishType());
      }
      catch(NoSuchFieldException e)
      {
        displayWarning(e.getMessage(), "Translation Error");
      }
      dblMinimum.setValue(mpCurrentDLData.getMinimumQuantity());
      dblMaximum.setValue(mpCurrentDLData.getMaximumQuantity());
      dblCurrent.setValue(mpCurrentDLData.getCurrentQuantity());
    }
    
    lcPanel.requestFocus();
  }

  /**
   *  Cancel Button handler.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }
}
