package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.RackLocationParser;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.FlowLayout;
import javax.swing.JPanel;

/**
 * <B>Description:</B> Simple address panel for consistency.
 * <BR>TODO: We may want to incorporate this into LocationPanel
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2006 by Daifuku America Corporation
 */
public class AddressPanel extends JPanel
{
  private static final long serialVersionUID = 0L;
  
  private SKDCIntegerField mpBank;
  private SKDCIntegerField mpBay;
  private SKDCIntegerField mpTier;
  private SKDCTextField    mpAddress;
  private SKDCLabel        mpDash1;
  private SKDCLabel        mpDash2;
  
  private boolean mzRackMode = true;

  private static String DASH = " - "; 
  
  public AddressPanel()
  {
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

    /*
     * Initialize the components
     */
    mpAddress = new SKDCTextField(LocationData.ADDRESS_NAME);
    mpDash1 = new SKDCLabel(DASH);
    mpDash2 = new SKDCLabel(DASH);
    try
    {
      mpBank = new SKDCIntegerField("001", DBConstants.LNBANK);
      mpBay  = new SKDCIntegerField("001", DBConstants.LNBAY);
      mpTier = new SKDCIntegerField("001", DBConstants.LNTIER);
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
      throw e;
    }
    
    mpBank.setInputVerifier(new BankBayTierVerifier(true));
    mpBay.setInputVerifier(new BankBayTierVerifier(true));
    mpTier.setInputVerifier(new BankBayTierVerifier(true));

    /*
     * Add them to the panel
     */
    add(mpBank);
    add(mpDash1);
    add(mpBay);
    add(mpDash2);
    add(mpTier);
    
    mpAddress.setVisible(false);
    mpAddress.setLocation(mpBank.getLocation());
    add(mpAddress);
  }

  /**
   * Set the rack vs. non-rack display
   * @param izRackMode
   */
  public void setRackMode(boolean izRackMode)
  {
    mzRackMode = izRackMode;
    
    mpBank.setVisible(mzRackMode);
    mpDash1.setVisible(mzRackMode);
    mpBay.setVisible(mzRackMode);
    mpDash2.setVisible(mzRackMode);
    mpTier.setVisible(mzRackMode);
    
    mpAddress.setVisible(!mzRackMode);
  }
  
  /**
   * Set the rack vs. non-rack display
   * @param izEnabled
   */
  @Override
  public void setEnabled(boolean izEnabled)
  {
    mpBank.setEnabled(izEnabled);
    mpBay.setEnabled(izEnabled);
    mpTier.setEnabled(izEnabled);
    
    mpAddress.setEnabled(izEnabled);
  }

  /**
   * Get the address
   * @return
   */
  public String getAddress()
  {
    String vsAddress;
    if (mzRackMode)
    {
      vsAddress = mpBank.getText();
      vsAddress += mpBay.getText();
      vsAddress += mpTier.getText();
    }
    else
    {
      vsAddress = mpAddress.getText();
    }

    return vsAddress;
  }
  
  /**
   * Get the bank
   * @return
   */
  public String getBank()
  {
    return mpBank.getText();
  }

  /**
   * Get the bank
   * @return
   */
  public String getBay()
  {
    return mpBay.getText();
  }

  /**
   * Get the bank
   * @return
   */
  public String getTier()
  {
    return mpTier.getText();
  }

  /**
   * Set the address
   * @param
   */
  public void setAddress(String isAddress)
  {
    mpAddress.setText(isAddress);

    try
    {
      Integer.parseInt(isAddress);
      
      RackLocationParser vpRackLocParser = RackLocationParser.parse(isAddress, true);
      mpBank.setText(vpRackLocParser.getBankString());
      mpBay.setText(vpRackLocParser.getBayString());
      mpTier.setText(vpRackLocParser.getTierString());
    }
    catch (Exception e)
    {
      setRackMode(false);
    }
  }
  
  /**
   * Clear to default values
   */
  public void reset()
  {
    mpBank.setText("001");
    mpBay.setText("001");
    mpTier.setText("001");
    mpAddress.setText("");
  }
}
