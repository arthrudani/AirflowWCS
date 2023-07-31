package com.daifukuamerica.wrxj.swingui.item;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import javax.swing.event.InternalFrameEvent;

/**
 * Title:        RTS JAVA GUI
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Eskay
 * @author
 * @version 1.0
 */

@SuppressWarnings("serial")
public class ChangeItemName extends DacInputFrame
{
  private SKDCTextField oldName = new SKDCTextField(ItemMasterData.ITEM_NAME);
  protected SKDCTextField newName = new SKDCTextField(ItemMasterData.ITEM_NAME);

  private StandardInventoryServer inventoryServer;
  protected String unchangeableItems[];

  /**
   *  Create change item screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public ChangeItemName(String isTitle)
  {
    super(isTitle, "Item Information");
    try
    {
      jbInit();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   *  Create default change item screen class.
   *
   */
  public ChangeItemName()
  {
    this("");
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
    inventoryServer = Factory.create(StandardInventoryServer.class);
    unchangeableItems = new String []{inventoryServer.getBinEmptyItemName(),
    		inventoryServer.getBinFullItemName(), inventoryServer.getDefaultItemName(),
    		inventoryServer.getBinHeightItemName()};
    

  }

 /**
  *  Method to intialize screen components. This adds the components to the
  *  screen and adds listeners as needed.
  *
  *  @exception Exception
  */
  protected void jbInit() throws Exception
  {
    addInput("Old Item ID:", oldName);
    addInput("New Item ID:", newName);
  }

 /**
  *  Method to clean up as needed at closing.
  *
  */
  @Override
  public void cleanUpOnClose()
  {
    inventoryServer.cleanUp();
  }

 /**
  *  Method to determine if an item name is valid.
  *
  */
  public boolean isValidItemName(String item)
  {
    for (int i = 0; i < unchangeableItems.length; i++)
    {
      if (item.equalsIgnoreCase(unchangeableItems[i]))
      {
        return false;
      }
    }
    return true;
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new route to the database.
   */
  @Override
  protected void okButtonPressed()
  {
    if (oldName.getText().trim().length() == 0)
    {
      displayError("Old item ID is required.");
      return;
    }

    if (newName.getText().trim().length() == 0)
    {
      displayError("New item ID is required.");
      return;
    }

    if (!isValidItemName(oldName.getText()))
    {
      displayError("Old item ID is restricted.");
      return;
    }

    if (!isValidItemName(newName.getText()))
    {
      displayError("New item ID is restricted.");
      return;
    }

    try
    {
    	if(!inventoryServer.itemMasterExists(oldName.getText()))
    	{
      	displayError("Old item ID does not Exist");
      	return;
      }
      if (inventoryServer.itemMasterExists(newName.getText()))
      {
        displayError("New item ID already exists.");
        return;
      }
      inventoryServer.changeItemMasterName(oldName.getText(), newName.getText(), true);
      newName.setText("");
      oldName.setText("");
    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);
      displayError("Error changing item ID: " + e2.getMessage());
    }
  }

  /**
   *  Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    newName.setText("");
    oldName.setText("");
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    cleanUpOnClose();
    close();
  }

}