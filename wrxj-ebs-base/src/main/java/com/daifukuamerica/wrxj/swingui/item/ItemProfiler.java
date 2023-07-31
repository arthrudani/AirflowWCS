package com.daifukuamerica.wrxj.swingui.item;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

@SuppressWarnings("serial")
public class ItemProfiler extends DacInputFrame
{
  protected ItemNumberInput mpItemInput;
  protected SKDCTextField mpTxtDesc;
  protected ItemSizingPanel mpSizingPanel = new ItemSizingPanel();

  protected StandardInventoryServer mpInvServer;
  protected ItemMasterData  mpIMData;
  
  /**
   * Constructor
   */
  public ItemProfiler()
  {
    super("Item Profiler", "Item Information");
    
    mpInvServer = Factory.create(StandardInventoryServer.class);
    mpIMData = Factory.create(ItemMasterData.class);
    
    buildInputPanel();
  }

  /**
   *  Method to clean up as needed at closing.
   *
   */
  @Override
  public void cleanUpOnClose()
  {
    mpInvServer.cleanUp();
  }
  
  /**  
   * Build the Input Panel
   */
  private void buildInputPanel()
  {
    mpItemInput = new ItemNumberInput(mpInvServer, false, false);
    mpItemInput.addItemListener(new ItemListener()
        {
          public void itemStateChanged(ItemEvent arg0)
          {
            item_Selected(mpItemInput.getText());
          }
        });
    mpItemInput.addFocusListener(new FocusListener()
      {
        @Override
        public void focusGained(FocusEvent e) {}

        @Override
        public void focusLost(FocusEvent e)
        {
          item_Selected(mpItemInput.getText());
        }
      });
    mpTxtDesc = new SKDCTextField(ItemMasterData.DESCRIPTION_NAME);
    mpTxtDesc.setEnabled(false);
    mpItemInput.linkDescription(mpTxtDesc);

    addInput("Item:", mpItemInput);
    addInput("Description", mpTxtDesc);
    setHeaderRows(2);
    addInput("", mpSizingPanel);
    
    item_Selected(mpItemInput.getText());
    
    useModifyButtons();
  }
  
  
  /*========================================================================*/
  /*  Methods for processing button events                                  */
  /*========================================================================*/

  /**
   * Do this when an item is selected
   */
  protected void item_Selected(String isItem)
  {
    mpIMData.clear();
    if (isItem.trim().length() > 0)
    {
      try
      {
        mpIMData = mpInvServer.getItemMasterData(isItem);
      }
      catch (DBException dbe)
      {
        displayError(dbe.getMessage(), "Database Error");
      }
      if (mpIMData == null)
      {
        mpIMData = Factory.create(ItemMasterData.class);
      }
      else
      {
        setInfo("");
      }
    }
    mpSizingPanel.setEnabled(mpIMData.getItem().trim().length() != 0);
    mpSizingPanel.setSizingInfo(mpIMData);
  }

  /**
   *  Clear button pressed - reset changes
   */
  @Override
  protected void clearButtonPressed()
  {
    item_Selected(mpItemInput.getText());
  }
  
  /**
   * Submit button pressed - submit changes
   */
  @Override
  protected void okButtonPressed()
  {
    mpSizingPanel.getSizingInfo(mpIMData);
    try
    {
      mpInvServer.updateItemInfo(mpIMData);
      displayInfoAutoTimeOut("Updated item " + mpIMData.getItem());
      mpItemInput.setSelectedItem("");
      clearButtonPressed();
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage(), "Database Error");
    }
  }

  /**
   * Cancel button pressed - quit
   */
  @Override
  protected void closeButtonPressed()
  {
    close();    
  }
}
