package com.daifukuamerica.wrxj.swingui.pick;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryAdjustServer;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;

/**
 * Screen to make operator enter ACI Quantity.
 * @author    A.D.
 * @version   1.0
 * @since:    21-Oct-08
 */
@SuppressWarnings("serial")
public class CCIPointFrame extends DacInputFrame
{
  private SKDCTextField   mpLoadTextField;
  private SKDCTextField   mpItemTextField;
  private SKDCTextField   mpPickLotTextField;
  private SKDCTextField   mpSubLocnTextField;
  private SKDCDoubleField mdVerifyQtyField;
  private double          mdPreviousEntry = -1;
  private StandardInventoryAdjustServer mpInvServ;
  
  
  public CCIPointFrame()
  {
    super("CCI Point Frame", "Verify Count");
    mpInvServ = Factory.create(StandardInventoryAdjustServer.class);
    initSwingComponents();
  }

 /**
  * Method to set display only data.
  * @param isPickFromLoad the pick from load
  * @param isItem the pick item
  * @param isPickLot the pick lot
  * @param isSubLocn the sub-location in the pick-from load.
  */  
  public void setData(final String isPickFromLoad, final String isItem, 
                      final String isPickLot, final String isSubLocn)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        mpLoadTextField.setText(isPickFromLoad);
        mpItemTextField.setText(isItem);
        mpPickLotTextField.setText(isPickLot);
        mpSubLocnTextField.setText(isSubLocn);
      }
    });
  }
  
  /**
   * Builds fields to display.
   */
  protected void initSwingComponents() 
  {
    mpLoadTextField    = new SKDCTextField(MoveData.LOADID_NAME);           
    mpItemTextField    = new SKDCTextField(MoveData.ITEM_NAME);             
    mpPickLotTextField = new SKDCTextField(MoveData.PICKLOT_NAME);       
    mpSubLocnTextField = new SKDCTextField(MoveData.POSITIONID_NAME);    
    mdVerifyQtyField   = new SKDCDoubleField(MoveData.PICKQUANTITY_NAME);

    addInput("Pick From Load", mpLoadTextField);
    addInput("Item", mpItemTextField);
    addInput("Lot", mpPickLotTextField);
    addInput("Sub-Location", mpSubLocnTextField);
    addInput("Verify Quantity", mdVerifyQtyField);

    setInputEnabled(mpLoadTextField, false);
    setInputEnabled(mpItemTextField, false);
    setInputEnabled(mpPickLotTextField, false);
    setInputEnabled(mpSubLocnTextField, false);
    
    mdVerifyQtyField.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyTyped(KeyEvent ke)
      {
        if (ke.getKeyChar() == KeyEvent.VK_ENTER)
        {
          compareEntries();
        }
      }
    });
    
    mpButtonPanel.setVisible(false);
  }

  /**
   * Handle the submit button
   */
  protected void compareEntries()
  {
    if (mdPreviousEntry == mdVerifyQtyField.getValue())
    {
      // we have 2 successive entries for the quantity
      // if the quantity has changed, make adjustments to inventory if needed
      try
      {
        mpInvServ.adjustLoadLineItemIfNeeded(SKDCUserData.getLoginName(),
                                             mpLoadTextField.getText(), 
                                             mpItemTextField.getText(),
                                             mpPickLotTextField.getText(), "",
                                             "", "", mpSubLocnTextField.getText(), 
                                             mdVerifyQtyField.getValue(), "C");
      }
      catch (DBException e)
      {
        displayError("Failed to complete cycle count: " + e.getMessage());
      }
      close();
    }
    mdPreviousEntry = mdVerifyQtyField.getValue();
    updateInputTitle(mdVerifyQtyField, "Re-enter quantity");
    mdVerifyQtyField.setText("");
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Add Carrier Frame.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpLoadTextField.setText("");
    mpItemTextField.setText("");
    mpPickLotTextField.setText("");
    mpSubLocnTextField.setText(""); 
    mdVerifyQtyField.setText("");

    mdVerifyQtyField.requestFocus();
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
