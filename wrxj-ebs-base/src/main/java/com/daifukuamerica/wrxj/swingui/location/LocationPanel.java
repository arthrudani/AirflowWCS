package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCRadioButton;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Description:<BR>
 *    Builds a semi-intelligent Location panel. Allows for rack and non-rack
 *    location formats, can toggle between formats.
 *
 * @author       A.T.
 * @version      1.0
 */
public class LocationPanel extends JPanel
{
  protected static final long serialVersionUID = 0;
  
  public static int INPUT_REQUIRED     = 1;
  public static int INPUT_NOT_REQUIRED = 2;
  
  public static int WTYPE_REGULAR      = 3;
  public static int WTYPE_SUPER        = 4;
  public static int WTYPE_ALL          = 5;

  protected SKDCComboBox     whs_combo  = null;
  protected SKDCTextField    txtWarehouse = new SKDCTextField(LocationData.WAREHOUSE_NAME);
  
  protected SKDCIntegerField txtBank    = new SKDCIntegerField(DBConstants.LNBANK);
  protected SKDCIntegerField txtBay     = new SKDCIntegerField(DBConstants.LNBAY);
  protected SKDCIntegerField txtTier    = new SKDCIntegerField(DBConstants.LNTIER);
  
  protected SKDCTextField    txtAddress = new SKDCTextField(LocationData.ADDRESS_NAME);
  
  private String[]         masWhsList = null;
  private SKDCLabel        dash1      = new SKDCLabel(DASH);
  private SKDCLabel        dash2      = new SKDCLabel(DASH);
  private SKDCLabel        dash3      = new SKDCLabel(DASH);
  protected SKDCRadioButton  rackFormat = new SKDCRadioButton();
  protected int              mnInputRequirement;
  
  private boolean          mzAutoSelectFormatOnReset = false;
  
  protected static final String DASH = " - ";

  /**
   * Constructor for rack locations.
   * 
   * @param bank               String containing Location Bank.
   * @param bay                String containing Location Bay.
   * @param tier               String containing Location Tier.
   * @param position           String containing Location Shelf Position.
   * @param inInputRequirement integer specifying input requirement. If value is
   *          INPUT_REQUIRED then input is required for bank, bay and tier.
   */
  protected LocationPanel(String bank, String bay, String tier, String position,
      int inInputRequirement)
  {
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

    mnInputRequirement = inInputRequirement;
    
    whs_combo = new SKDCComboBox();   // Build empty combo-box until they fill
    // it in with a call to setWarehouseList.
    add(whs_combo);

    txtWarehouse.setVisible(false);
    txtWarehouse.setLocation(whs_combo.getLocation());
    add(txtWarehouse);

    txtBank.setVisible(false);
    txtBay.setVisible(false);
    txtTier.setVisible(false);
    dash2.setVisible(false);
    dash3.setVisible(false);
    
    add(dash1);
    add(txtBank);
    add(dash2);
    add(txtBay);
    add(dash3);
    add(txtTier);

    txtAddress.setVisible(true);
    txtAddress.setLocation(txtBank.getLocation());
    add(txtAddress);

    rackFormat.setSelected(false);
//    removed the rack radio button 
//    rackFormat.setToolTipText("Switch location format");
//    rackFormat.addItemListener(new java.awt.event.ItemListener()
//    {
//      @Override
//      public void itemStateChanged(ItemEvent e)
//      {
//        if ((e.getStateChange() == ItemEvent.SELECTED) ||
//            (e.getStateChange() == ItemEvent.DESELECTED))
//        {
//          setRackFormat(e.getStateChange() == ItemEvent.SELECTED);
//        }
//      }
//    });
//    add(rackFormat);

    // Add entry verifiers for Bank, Bay
    // and Tier fields. Makes sure the entry
    // is three characters long each.
    boolean input_req = (mnInputRequirement == LocationPanel.INPUT_REQUIRED);

    txtBank.setInputVerifier(new BankBayTierVerifier(input_req));
    txtBay.setInputVerifier(new BankBayTierVerifier(input_req));
    txtTier.setInputVerifier(new BankBayTierVerifier(input_req));

    reset(bank, bay, tier, position);
  }

  /**
   * Default constructor. Defaults everything.
   */
  public LocationPanel()
  {
    this("", "", "", "", INPUT_NOT_REQUIRED);
  }

  /**
   * Show or not show the warehouse
   * @param izVisible
   */
  public void setWarehouseVisible(boolean izVisible)
  {
    whs_combo.setVisible(izVisible);
    dash1.setVisible(izVisible);
  }
  
  /**
   * Show or not show the format radio button
   * @param izVisible
   */
  public void setFormatVisible(boolean izVisible)
  {
    rackFormat.setVisible(izVisible);
    if (!izVisible)
    {
      setAutoSelectFormatOnReset(true);
    }
  }

  /**
   * Set AutoSelectFormatOnReset
   * @param izValue
   */
  public void setAutoSelectFormatOnReset(boolean izValue)
  {
    mzAutoSelectFormatOnReset = izValue;
    if (mzAutoSelectFormatOnReset)
    {
      reset(getWarehouseString(), txtAddress.getText().trim());
    }
  }
  
  /**
   * Add an ActionListener to the location panel
   * @param ipAL
   */
  public void addActionListener(ActionListener ipAL)
  {
//    whs_combo.addActionListener(ipAL);
    txtAddress.addActionListener(ipAL);
    txtBay.addActionListener(ipAL);
    txtBank.addActionListener(ipAL);
    txtTier.addActionListener(ipAL);
  }
  
  /**
   * Add an Event to the location panel
   * @param isAC - String - ActionCommand
   * @param ipAL - ActionListener
   */
  public void addEvent(String isAC, ActionListener ipAL)
  {
    if (isAC == null || isAC.length() == 0)
    {
      JOptionPane.showMessageDialog(null, "Invalid actionID passed.",
          "Event Add Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

//    whs_combo.setActionCommand(isAC);
    txtAddress.setActionCommand(isAC);
    txtBay.setActionCommand(isAC);
    txtBank.setActionCommand(isAC);
    txtTier.setActionCommand(isAC);

//    whs_combo.addActionListener(ipAL);
    txtAddress.addActionListener(ipAL);
    txtBay.addActionListener(ipAL);
    txtBank.addActionListener(ipAL);
    txtTier.addActionListener(ipAL);
  }
  
  /**
   * Method to build a panel out of the bank, bay, tier fields.
   */
  public JPanel getAddressAsPanel()
  {
    JPanel rtnComponent = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    if (rackFormat.isSelected())
    {
      rtnComponent.add(txtBank);
      rtnComponent.add(new SKDCLabel(DASH));
      rtnComponent.add(txtBay);
      rtnComponent.add(new SKDCLabel(DASH));
      rtnComponent.add(txtTier);
    }
    else
    {
      rtnComponent.add(txtAddress);
    }

    return (rtnComponent);
  }

  /**
   * Reset the bank-bay-tier fields to whatever is passed in. If a blank is
   * passed in for any of the fields it will be defaulted to 1's.
   * 
   * @param bank Bank number.
   * @param bay Bay number.
   * @param tier Tier number.
   * @param position Shelf Position (not used in baseline)
   */
  public void reset(String bank, String bay, String tier, String position)
  {
    if (whs_combo != null && masWhsList != null)
    {
      whs_combo.setSelectedIndex(0);
      txtWarehouse.setText(whs_combo.getSelectedItem().toString());
    }
    checkBankBayTier(bank, DBConstants.LNBANK, txtBank);
    checkBankBayTier(bay, DBConstants.LNBAY, txtBay);
    checkBankBayTier(tier, DBConstants.LNTIER, txtTier);
    txtAddress.setText("");
  }

  /**
   * Reset the panel with just a warehouse
   * 
   * @param war Warehouse.
   */
  public void reset(String war)
  {
    reset();
    if (whs_combo != null && masWhsList != null && war != null)
    {
      whs_combo.setSelectedItem(war);
      txtWarehouse.setText(whs_combo.getSelectedItem().toString());
    }
  }
  
  /**
   * Reset the bank-bay-tier/address fields to whatever is passed in.
   * 
   * @param war Warehouse.
   * @param address Address in warehouse.
   */
  public void reset(String war, String address)
  {
    if (whs_combo != null && masWhsList == null && war != null && war.length() > 0)
    {
      setWarehouseList(new String[] { war });
    }
    if (whs_combo != null && masWhsList != null && war != null)
    {
      whs_combo.setSelectedItem(war);
      txtWarehouse.setText(whs_combo.getSelectedItem().toString());
    }
    try
    {
      if (address != null)
      {
        txtAddress.setText(address);
        if (address.length() > 0)
        {
          checkBankBayTier(address.substring(0,DBConstants.LNBANK),
            DBConstants.LNBANK, txtBank);
        }
        else
        {
          txtBank.setText("");
        }
        if (address.length() > DBConstants.LNBANK)
        {
          checkBankBayTier(address.substring(DBConstants.LNBANK,
            DBConstants.LNBANK+DBConstants.LNBAY), DBConstants.LNBAY, txtBay);
        }
        else
        {
          txtBay.setText("");
        }
        if (address.length() > DBConstants.LNBANK + DBConstants.LNBAY)
        {
          checkBankBayTier(address.substring(DBConstants.LNBANK+DBConstants.LNBAY,
            DBConstants.LNBANK+DBConstants.LNBAY+DBConstants.LNTIER),
            DBConstants.LNTIER, txtTier);
        }
        else
        {
          txtTier.setText("");
        }
        if (mzAutoSelectFormatOnReset)
        {
          rackFormat.setSelected(true);
        }
      }
    }
    catch(NumberFormatException e)
    {
      if (rackFormat.isSelected())
      {
        setRackFormat(false);
      }
      txtAddress.setText(address);
    }
    catch(IndexOutOfBoundsException e)
    {
      if (rackFormat.isSelected())
      {
        setRackFormat(false);
      }
      txtAddress.setText(address);
    }
  }

  /**
   * Reset the bank-bay-tier/address fields to whatever is passed in.
   * 
   * @param war Warehouse.
   * @param address Address in warehouse.
   */
  public void reset(String war, String address, String position)
  {
    reset(war, address);
  }
  
  /**
   * Reset the bank-bay-tier fields to blanks, provided input is NOT required.
   */
  public void reset()
  {
    if (mnInputRequirement == INPUT_NOT_REQUIRED)
    {
      reset("", "", "", "");
    }
    else
    {
      reset("1", "1", "1", "1");
    }
  }

  /**
   * Set input requirement on for the Bank, Bay, and Tier fields. The initial
   * values will be set to "001"
   */
  public void setInputRequired()
  {
    mnInputRequirement = INPUT_REQUIRED;
    reset("1", "1", "1", "1");
    txtBank.setInputVerifier(new BankBayTierVerifier(true));
    txtBay.setInputVerifier(new BankBayTierVerifier(true));
    txtTier.setInputVerifier(new BankBayTierVerifier(true));
  }

  /**
   * Returns the Warehouse, Address as one string.
   * 
   * @return String containing full location.
   */
  public String getLocationString() throws DBException
  {
    return (getWarehouseString() + getAddressString());
  }

  /**
   * Returns the Address as a string.
   * 
   * @return String containing address.
   */
  public String getAddressString() throws DBException
  {
    StringBuffer locn_string = new StringBuffer(
        DBInfo.getFieldLength(LocationData.ADDRESS_NAME) + 1);
    if (rackFormat.isSelected())
    {
      checkBankBayTier(txtBank.getText(), DBConstants.LNBANK, txtBank);
      checkBankBayTier(txtBay.getText() , DBConstants.LNBAY , txtBay);
      checkBankBayTier(txtTier.getText(), DBConstants.LNTIER, txtTier);
      
                                       // If bank, bay, or tier is entered,
                                       // it must be in the right order.
      if (checkBankBayTier(txtBank.getText(), txtBay.getText(),
                           txtTier.getText()) == -1)
      {
        throw new DBException("Invalid Entry");
      }

      locn_string.append(txtBank.getText())
                 .append(txtBay.getText())
                 .append(txtTier.getText());
    }
    else
    {
      locn_string.append(txtAddress.getText().trim());
    }
                                       // If bank, bay, or tier is entered,
                                       // it must be in the right order.
    if (checkBankBayTier(txtBank.getText(), txtBay.getText(),
                         txtTier.getText()) == -1)
    {
      throw new DBException("Invalid Entry");
    }

    return(locn_string.toString());
  }

  /**
   * Returns the selected Warehouse String.
   * 
   * @return String containing selected warehouse.
   */
  public String getWarehouseString()
  {
    try
    {
      return (whs_combo.getSelectedItem().toString());
    }
    catch (NullPointerException npe)
    {
      return "";
    }
  }

  /**
   * Returns a reference to the Warehouse List being used here.
   */
  public String[] getWarehouseList()
  {
    return (masWhsList);
  }

  /**
   * Sets the current warehouse combo box to use the passed in list.
   */
  public void setWarehouseList(String[] whs_list)
  {
    masWhsList = whs_list;
    whs_combo.setComboBoxData(masWhsList);
  }

  /**
   * Builds the warehouse list based on passed in parameters and populates the
   * Combo-Box.
   * 
   * @param wtype Combo-Box will contain these warehouse types. Choices are
   *          WTYPE_SUPER, WTYPE_REGULAR, and WTYPE_ALL.
   * @param include_all_string boolean parameter. If false the "ALL" string will
   *          not be included in the combo-box.
   */
  public void setWarehouseList(int wtype, boolean include_all_string)
  {
    try
    {
      buildWarehouseList(wtype, include_all_string);
      whs_combo.setComboBoxData(masWhsList);
    }
    catch(DBException e)
    {
      JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Enables or disables the combo-box, and Bank, Bay, Tier fields.
   */
  @Override
  public void setEnabled(boolean enabled)
  {
    txtWarehouse.setEnabled(enabled);
    whs_combo.setEnabled(enabled);
    txtBank.setEnabled(enabled);
    txtBay.setEnabled(enabled);
    txtTier.setEnabled(enabled);
    txtAddress.setEnabled(enabled);
    
    if (!enabled)
    {
      txtWarehouse.setVisible(true);
      whs_combo.setVisible(false);
    }
  }

  /**
   * Method to check that the bank, bay and tier is valid.
   * 
   * @param sBank Bank number.
   * @param sBay Bay number.
   * @param sTier Tier number.
   * @return int value of 0 if okay, -1 if not
   */
  private int checkBankBayTier(String sBank, String sBay, String sTier)
  {
                                       // If they filled in the Tier but not
                                       // the Bank and Bay, return error.
    if (!sTier.equals("") && (sBank.equals("") || sBay.equals("")))
    {
      JOptionPane.showMessageDialog(this, "Bank and Bay must be filled in, too",
                                    "Entry Error", JOptionPane.ERROR_MESSAGE);
      txtBank.requestFocus();
      return(-1);
    }                                  // If they filled in the Bay but not
                                       // the Bank return error.
    else if (!sBay.equals("") && sBank.equals(""))
    {
      JOptionPane.showMessageDialog(this, "Bank must be filled in, too",
                                    "Entry Error", JOptionPane.ERROR_MESSAGE);
      txtBank.requestFocus();
      return(-1);
    }

    return(0);
  }

  /**
   * Checks passed string "inputString" to make sure it is set to the right
   * value. It is set to 1 otherwise.
   * 
   */
  protected void checkBankBayTier(String inputString, int inputLength,
                                SKDCIntegerField component)
  {
                                       // Default field if it's a 0 or
    try                                // non-integer
    {
      if (mnInputRequirement == INPUT_REQUIRED)
      {                                // If they pass in blanks or zero.
        if (inputString.trim().equals("") || Integer.parseInt(inputString) == 0)
          component.setText(SKDCUtility.preZeroFill("1", inputLength));
        else
          component.setText(SKDCUtility.preZeroFill(inputString, inputLength));
      }
      else
      {
        if (inputString.trim().equals(""))
          component.setText(inputString);
        else
          component.setText(SKDCUtility.preZeroFill(inputString, inputLength));
      }
    }
    catch(NumberFormatException e)
    {
      if (mzAutoSelectFormatOnReset)
      {
        throw e;
      }
      else
      {
        component.setText(SKDCUtility.preZeroFill("1", inputLength));
      }
    }
  }

  /**
   * Adds a blank to the warehouse list to enable blanking out warehouse
   */
  public void addBlankWarehouse()
  {
    int vnLength = masWhsList.length;
    String[] vasWhsList = new String[vnLength + 1];
    System.arraycopy(masWhsList, 0, vasWhsList, 1, vnLength);
    vasWhsList[0] = "";
    masWhsList = vasWhsList;
    whs_combo.setComboBoxData(vasWhsList);
  }

  /**
   * Builds a warehouse-combo box based on what type of warehouse is set.
   */
  private void buildWarehouseList(int warehouse_type, boolean izAllPrepend)
          throws DBException
  {
    StandardLocationServer lc_Server = Factory.create(StandardLocationServer.class);
    String isPrepend = (izAllPrepend) ? SKDCConstants.ALL_STRING :
                                        SKDCConstants.NO_PREPENDER;
    if (warehouse_type == LocationPanel.WTYPE_REGULAR)
      masWhsList = lc_Server.getRegularWarehouseChoices(isPrepend);

    else if (warehouse_type == LocationPanel.WTYPE_SUPER)
      masWhsList = lc_Server.getSuperWarehouseChoices(izAllPrepend);

    else if (warehouse_type == LocationPanel.WTYPE_ALL)
      masWhsList = lc_Server.getWarehouseChoices(izAllPrepend);

    else
      masWhsList = lc_Server.getRegularWarehouseChoices(isPrepend);
  }

  /**
   * Method to switch between rack and non-rack location format.
   * 
   * @param isRack True if setting to rack format.
   */
  public void setRackFormat(boolean isRack)
  {
    /*
     * These line make it so stuff doesn't usually shift around
     * (it still does once if the LocationPanel starts in non-rack format)
     */
    if (isValid() && txtBank.isVisible())
    {
      setPreferredSize(getSize());
    }
    
    rackFormat.setSelected(isRack);
    txtBank.setVisible(rackFormat.isSelected());
    txtBay.setVisible(rackFormat.isSelected());
    txtTier.setVisible(rackFormat.isSelected());
    dash2.setVisible(rackFormat.isSelected());
    dash3.setVisible(rackFormat.isSelected());
    txtAddress.setVisible(!rackFormat.isSelected());
    if (rackFormat.isSelected())
    {
      reset(getWarehouseString(), txtAddress.getText().trim());
    }
    else
    {
      StringBuffer sb = new StringBuffer();
      sb.append(txtBank.getText()).append(txtBay.getText()).append(txtTier.getText());
      txtAddress.setText(sb.toString());
    }
  }
}
