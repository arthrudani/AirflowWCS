package com.daifukuamerica.wrxj.swingui.move;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

/**
 * A screen class for collecting detailed search criteria for a list of moves.
 * 
 * @author avt
 * @version 1.0
 */
public class MoveDetailedSearchFrame extends DacInputFrame
{
  private String[] masDeviceNames;
  protected GridBagConstraints mpGBC = new GridBagConstraints();
  protected JPanel mpPanelInput  = new JPanel(new GridBagLayout());
  protected JPanel mpPanelButton = new JPanel();
  
  SKDCButton mpBtnSearch = new SKDCButton();
  SKDCButton mpBtnClear  = new SKDCButton();
  SKDCButton mpBtnClose  = new SKDCButton();

  SKDCTextField    mpTextLoadID;
  SKDCTextField    mpTextOrderID;
  SKDCTextField    mpTextItemID;
  SKDCComboBox     mpDeviceCombo;
  SKDCTranComboBox mpTComboType;
  SKDCTranComboBox mpTComboStatus;
  private LocationPanel mpRLPLocation;


  /**
   * Create move search frame.
   */
  public MoveDetailedSearchFrame()
  {
    super("Move Search", "Move Search Criteria");
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Method to initialize screen components. This adds the components to the
   * screen and adds listeners as needed.
   * 
   * @exception Exception
   */
  private void jbInit() throws Exception
  {
    try
    {
      StandardDeviceServer mpDeviceServ = Factory.create(StandardDeviceServer.class);      
      masDeviceNames = mpDeviceServ.getRackStorageDeviceNames();
      buildScreen();
    }
    catch(DBException dbe)
    {
      displayError("Database error finding Device names.");
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }
  }

  /**
   * Builds the label column on the update form
   * 
   * @param ipJPanel The panel to build
   */
  protected void buildScreen() throws NoSuchFieldException
  {
    mpTextLoadID   = new SKDCTextField(MoveData.LOADID_NAME);
    mpTextOrderID  = new SKDCTextField(MoveData.ORDERID_NAME);
    mpTextItemID   = new SKDCTextField(MoveData.ITEM_NAME);
    mpTComboType   = new SKDCTranComboBox(MoveData.MOVETYPE_NAME, true);
    mpTComboStatus = new SKDCTranComboBox(MoveData.MOVESTATUS_NAME, true);
    mpRLPLocation  = Factory.create(LocationPanel.class);
    mpRLPLocation.setWarehouseList(LocationPanel.WTYPE_REGULAR, true);
    mpDeviceCombo  = new SKDCComboBox();
    mpDeviceCombo.setComboBoxData(masDeviceNames, "");

    addInput("Load:", mpTextLoadID);
    addInput("Order:", mpTextOrderID);
    addInput("Item:", mpTextItemID);
    addInput("Type:", mpTComboType);
    addInput("Status:", mpTComboStatus);
    addInput("Location:", mpRLPLocation);
    addInput("Device:", mpDeviceCombo);
    
    useSearchButtons();
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
    mpTextLoadID.setText("");
    mpTextItemID.setText("");
    mpTextOrderID.setText("");
    try
    {
      mpTComboType.setSelectedElement(DBConstants.LOADMOVE);
      mpTComboStatus.setSelectedElement(DBConstants.AVAILABLE);
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }
    mpRLPLocation.reset();
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
   * Method to get the entered search criteria as a ColumnObject.
   * 
   * @return ColumnObject containing criteria to use in search
   */
  public KeyObject[] getSearchData ()
  {
    List vpSearchData = new LinkedList();
    if (mpTextOrderID.getText().toString().trim().length() > 0)
    {
      vpSearchData.add(new KeyObject(MoveData.ORDERID_NAME, mpTextOrderID.getText()));
    }
    
    if (mpTextItemID.getText().toString().trim().length() > 0)
    {
      vpSearchData.add(new KeyObject(MoveData.ITEM_NAME, mpTextItemID.getText()));
    }
    
    if (mpTextLoadID.getText().toString().trim().length() > 0)
    {
      vpSearchData.add(new KeyObject(MoveData.LOADID_NAME, mpTextLoadID.getText()));
    }
    
    if (!mpDeviceCombo.getText().isEmpty())
    {
      vpSearchData.add(new KeyObject(MoveData.DEVICEID_NAME, mpDeviceCombo.getText()));
    }
    
    try
    {
      vpSearchData.add(new KeyObject(mpTComboStatus.getTranslationName(), 
          mpTComboStatus.getIntegerObject()));
      vpSearchData.add(new KeyObject(mpTComboType.getTranslationName(), 
          mpTComboType.getIntegerObject()));

    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }
    
    /*
     * Current warehouse/address
     */
    try
    {
      String vsWarehouse = mpRLPLocation.getWarehouseString().trim();
      if (vsWarehouse.length() > 0)
      {
        vpSearchData.add(new KeyObject(MoveData.WAREHOUSE_NAME, vsWarehouse));
        
        String vsAddress = mpRLPLocation.getAddressString().trim();
        if (vsAddress.length() > 0)
        {
          vpSearchData.add(new KeyObject(MoveData.ADDRESS_NAME, vsAddress));
        }
      }
    }
    catch (DBException e3)
    {
      e3.printStackTrace(System.out);
    }

    return KeyObject.toKeyArray(vpSearchData);
  }
}