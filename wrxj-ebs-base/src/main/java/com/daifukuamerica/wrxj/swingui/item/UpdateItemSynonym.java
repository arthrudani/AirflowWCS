package com.daifukuamerica.wrxj.swingui.item;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.SynonymData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating item synonyms.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateItemSynonym extends DacInputFrame
{
  StandardInventoryServer invtServer = Factory.create(StandardInventoryServer.class);
  SKDCTextField mpItemText = new SKDCTextField(SynonymData.ITEM_NAME);
  SKDCTextField mpSynonymText = new SKDCTextField(SynonymData.SYNONYM_NAME);

  SynonymData mpDefaultData = Factory.create(SynonymData.class); 
    
  String msItem = "";
  String msSynonym = "";

  boolean mzAdding = true;

  /**
   *  Create Item Synonym screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateItemSynonym(String isTitle)
  {
    super(isTitle, "Synonym Information");
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
   *  Create default Item Synonym screen class.
   */
  public UpdateItemSynonym()
  {
    this("");
  }

  /**
   *  Method to set screen for modifying.
   *
   *  @param isItemID Item ID.
   *  @param isSynonym Synonym.
   */
  public void setModify(String isItemID, String isSynonym)
  {
    msItem = isItemID;
    msSynonym = isSynonym;
    mzAdding = false;
    useModifyButtons();
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
      mpItemText.setEnabled(false);
      try
      {
        mpDefaultData = invtServer.getSynonymRecord(msItem, msSynonym);
        if (mpDefaultData == null)
        {
          displayError("Unable to get Synonym data");
          close();
        }
      }
      catch (DBException e2)
      {
        displayError("Error getting Synonym data");
        close();
      }
 
      this.setTimeout(90);
    }
    setData(mpDefaultData);
  }

  
  private void setData(SynonymData ipSynonymData)
  {
    mpItemText.setText(ipSynonymData.getItemID());
    mpSynonymText.setText(ipSynonymData.getSynonym());
  }
  
  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  void jbInit() throws Exception
  {
    addInput("Item ID:", mpItemText);
    addInput("Synonym:", mpSynonymText);
    
    useAddButtons();
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    invtServer.cleanUp();
    invtServer = null;
  }

  /**
   *  Action method to handle Clear button.
   */
  @Override
  public void clearButtonPressed()
  {
    setData(mpDefaultData);
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  public void closeButtonPressed()
  {
    close();
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new Item Synonym to the database.
   */
  @Override
  public void okButtonPressed()
  {
    SynonymData voSynonym = null;
    
    if (mpItemText.getText().length() <= 0)  // required
    {
      displayError("Item ID is required");
      return;
    }
    
    if (mpSynonymText.getText().length() <= 0)  // required
    {
      displayError("Synonym is required");
      return;
    }

    boolean vbSynonymExists = false;
    boolean vbItemExists = false;
    try
    {
      vbSynonymExists = invtServer.existSynonym(mpSynonymText.getText());
      vbItemExists = invtServer.itemMasterExists(mpItemText.getText());
    }
    catch (DBException e2)
    {
      displayError("Error getting Synonym data");
      return;
    }

    if (mzAdding)
    {
      // synonym can not exist
      if (vbSynonymExists)
      {
        displayError("Synonym " + mpSynonymText.getText() + " already exists.");
        return;
      }
      if (!vbItemExists)
      {
        displayError("Item " + mpItemText.getText() + " does not exist.");
        return;
      }
      try
      {
        // make sure synonym is not an item master
        if (invtServer.itemMasterExists(mpSynonymText.getText()))
        {
          displayError("Synonym " + mpSynonymText.getText() + " exists as an item");
          return;
        }
      }
      catch (DBException e2)
      {
        displayError("Unable to validate Item Master");
        return;
      }
    }
    else  // modifying
    {
      // make sure original is still there
      try
      {
        voSynonym = invtServer.getSynonymRecord(mpItemText.getText(), msSynonym);
      }
      catch (DBException e2)
      {
        displayError("Error getting Synonym data");
        return;
      }
      if (voSynonym == null)
      {
        displayError("Item synonym for " + mpItemText.getText() + ", " 
            + msSynonym + " does not exist.");
        return;
      }
      int changingSynonym = mpSynonymText.getText().compareTo(msSynonym);
      // we are changing the synonym
      if ((changingSynonym != 0) && vbSynonymExists)
      {
        displayError("Synonym " + mpSynonymText.getText() + " already exists.");
        mpSynonymText.setText(msSynonym);
        return;
      }
    }


    voSynonym = Factory.create(SynonymData.class);
    voSynonym.setItemID(mpItemText.getText());
    voSynonym.setSynonym(mpSynonymText.getText());

    try
    {
      if (mzAdding)
      {
        invtServer.addSynonym(voSynonym);
        
        this.changed();
        displayInfoAutoTimeOut("Item synonym for " + mpItemText.getText() +
          ", " + mpSynonymText.getText() + " added");
      }
      else
      {
        invtServer.modifySynonym(msSynonym, voSynonym);
        this.changed();
        displayInfoAutoTimeOut("Item synonym for " + mpItemText.getText() +
          ", " + mpSynonymText.getText() + " updated");
      }
    }
    catch (DBException e2)
    {
      if (mzAdding)
      {
        displayError(e2.getMessage());
      }
      else
      {
        displayError("Error updating Item Synonym " + mpItemText.getText() +
          ", " + mpSynonymText.getText());
      }
    }
    if (!mzAdding)
    {
      close();
    }
    else
    {
      mpItemText.setText("");
      mpSynonymText.setText("");
      mpItemText.requestFocus();
    }
  }

}
