package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.sequencer.LocationSequencer;
import com.daifukuamerica.wrxj.sequencer.RackBHLSequencer;
import com.daifukuamerica.wrxj.sequencer.RackRandomSequencer;
import com.daifukuamerica.wrxj.sequencer.RackTORSequencer;
import com.daifukuamerica.wrxj.sequencer.ResetSequencer;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import java.awt.Component;
import java.awt.FlowLayout;
import java.lang.reflect.Constructor;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * Description:<BR>
 *    Sets up the Location add internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author       Ed Askew.
 * @version      1.0
 * <BR>Created: 11-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class LocationOrderFrame extends DacInputFrame
{
  private LocationData     locationData   = Factory.create(LocationData.class);
  private StandardLocationServer   locationServer = null;
  protected SKDCComboBox warhseCombo  = null;
  private SKDCComboBox mpSequencerCombo = null;
  private SKDCIntegerField txtLocationOrder = new SKDCIntegerField(LocationData.SEARCHORDER_NAME);
  private SKDCIntegerField txtBeginBank  = null;
  private SKDCIntegerField txtBeginBay   = null;
  private SKDCIntegerField txtBeginTier  = null;
  private SKDCIntegerField txtEndBank    = null;
  private SKDCIntegerField txtEndBay     = null;
  private SKDCIntegerField txtEndTier    = null;
  private String[]     warhseList  = null;

  private class LocationSequencerRenderer extends JLabel implements ListCellRenderer
  {
    public LocationSequencerRenderer()
    {
      setOpaque(true);
    }
    public Component getListCellRendererComponent(JList ipList, Object ipSelection, 
                                                  int inIndex, boolean izSelected, 
                                                  boolean izHasFocus)
    {
      LocationSequencer vpSequencer = (LocationSequencer)ipSelection;
      if (izSelected)
      {
        setBackground(ipList.getSelectionBackground());
        setForeground(ipList.getSelectionForeground());
      }
      else
      {
        setBackground(ipList.getBackground());
        setForeground(ipList.getForeground());
      }
      setText(vpSequencer.getDescription());
      return this;
    }
  }
  
  /**
   * Constructor
   * @param ipLocServer
   * @param iasWarehouseList
   */
  public LocationOrderFrame(StandardLocationServer ipLocServer, String[] iasWarehouseList)
  {
    super("Location Order", "Location Ordering");

    try
    {
      txtBeginBank = new SKDCIntegerField("001", DBConstants.LNBANK);
      txtBeginBay  = new SKDCIntegerField("001", DBConstants.LNBAY);
      txtBeginTier = new SKDCIntegerField("001", DBConstants.LNTIER);
      txtEndBank   = new SKDCIntegerField("001", DBConstants.LNBANK);
      txtEndBay    = new SKDCIntegerField("001", DBConstants.LNBAY);
      txtEndTier   = new SKDCIntegerField("001", DBConstants.LNTIER);
    }
    catch(NumberFormatException e)
    {
      displayError("The programmer screwed up!\n" + e.getMessage() 
          + "\nInvalid value passed to constructor!", "SKDCIntegerField Error");
    }

    locationServer = ipLocServer;
    warhseList = iasWarehouseList;

    buildScreen(getSequencers());
  }

  /**
   * Get the list of available sequencers
   * @return
   */
  protected Vector<LocationSequencer> getSequencers()
  {
    Vector vpSequencers = new Vector();
    
    // Always add the reset sequencer.
    vpSequencers.add(new ResetSequencer());
    
    String vsSequencers = Application.getString("LocationSequencers");
    if (vsSequencers != null && vsSequencers.length() > 0)
    {
      String[] vasSequencers = vsSequencers.split(",");
      for (int vnIndex = 0; vnIndex < vasSequencers.length; vnIndex++)
      {
        String vsClassName = LocationSequencer.class.getPackage().getName() 
            + '.' + vasSequencers[vnIndex];
        try
        {
          Class vpClass = Class.forName(vsClassName); // Get class definition.
          Constructor vpConstructor = vpClass.getConstructor(new Class[] {});
          vpSequencers.add(vpConstructor.newInstance(new Object[] {})); // Make a new one.
        }
        catch (Exception vpException)
        {
          displayError("Unable to create sequencer: " + vsClassName);
        }
      }
    }
    else
    {
      vpSequencers.add(new RackBHLSequencer());    // If there are not any defined
      vpSequencers.add(new RackTORSequencer());    // sequencers then add these for
      vpSequencers.add(new RackRandomSequencer()); // backwards compatibility.
    }
    vpSequencers.trimToSize();
    
    return vpSequencers;
  }
  
  /**
   * Constructor
   * @param ipLocServer
   * @param iasWarehouseList
   * @throws NumberFormatException
   */
  public LocationOrderFrame(StandardLocationServer ipLocServer,
      String[] iasWarehouses, String isWarehouse)
  {
    this(ipLocServer, iasWarehouses);
    warhseCombo.selectItemBy(isWarehouse);
  }
  
/*===========================================================================
              Methods for display formatting go in this section.
===========================================================================*/
  private void buildScreen(Vector ipSequencers)
  {
    warhseCombo = new SKDCComboBox(warhseList);
    mpSequencerCombo = new SKDCComboBox(ipSequencers.toArray()); 
    mpSequencerCombo.setRenderer(new LocationSequencerRenderer());
    
    addInput("Warehouse:", warhseCombo);
    addInput("Beginning Address:", begAddressPanel());
    addInput("Ending Address:", endAddressPanel());
    addInput("Location Order Strategy:", mpSequencerCombo);
    addInput("Starting Search Order Value:", txtLocationOrder);
  }

  /**
   *  Builds Begin Address panel
   */
  private JPanel begAddressPanel()
  {
    JPanel addrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    addrPanel.add(txtBeginBank);
    addrPanel.add(new SKDCLabel(" - "));
    addrPanel.add(txtBeginBay);
    addrPanel.add(new SKDCLabel(" - "));
    addrPanel.add(txtBeginTier);
                                       // Add entry verifiers for Bank, Bay
                                       // and Tier fields. Makes sure the entry
                                       // is three characters long each.
    txtBeginBank.setInputVerifier(new BankBayTierVerifier(true));
    txtBeginBay.setInputVerifier(new BankBayTierVerifier(true));
    txtBeginTier.setInputVerifier(new BankBayTierVerifier(true));

    return(addrPanel);
  }

  /**
      Builds Ending Address panel.
   */
  private JPanel endAddressPanel()
  {
    JPanel addrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    addrPanel.add(txtEndBank);
    addrPanel.add(new SKDCLabel(" - "));
    addrPanel.add(txtEndBay);
    addrPanel.add(new SKDCLabel(" - "));
    addrPanel.add(txtEndTier);
                                       // Add entry verifiers for Bank, Bay
                                       // and Tier fields. Makes sure the entry
                                       // is three characters long each.
    txtEndBank.setInputVerifier(new BankBayTierVerifier(true));
    txtEndBay.setInputVerifier(new BankBayTierVerifier(true));
    txtEndTier.setInputVerifier(new BankBayTierVerifier(true));

    return(addrPanel);
  }

  private boolean checkForValidLocRange()
  {
    int compareValue;
    compareValue = txtEndBank.getText().compareTo(txtBeginBank.getText());
    if(compareValue < 0)
    {   // the ending bank is less than the beginning bank error
      displayError( "Location Range is Invalid beginning bank larger than ending");
      return false;
    }
    else
    {
      compareValue = txtEndBay.getText().compareTo(txtBeginBay.getText());
      if(compareValue < 0)
      {   // the ending bay is less than the beginning bay error
        displayError( "Location Range is Invalid Beginning Bay Larger than Ending");
        return false;
      }
      else if(compareValue > 0)
      {   // the ending bay is greater than the beginning bay so rest don't matter
        return true;
      }
      else
      {
        compareValue = txtEndTier.getText().compareTo(txtBeginTier.getText());
        if(compareValue < 0)
        {   // the ending tier is less than the beginning tier error
          displayError( "Location Range is Invalid Beginning Tier Larger than Ending");
           return false;
        }
        else
        {   // the ending tier is greater than the beginning tier so rest don't matter
          return true;
        }
      }
    }
  }

/*===========================================================================
              Methods for event handling go in this section.
===========================================================================*/
  /**
   *  Processes Location add request.  This method stuffs column objects into
   *  a LocationData instance container.
   */
  @Override
  protected void okButtonPressed()
  {
    if(checkForValidLocRange() == false)
    {
      return ;
    }
    locationData.clear();                    // Make sure everything is defaulted
                                       // to begin with.

                                       // Get the beginning Address.
    String sBeginAddr = txtBeginBank.getText();
    sBeginAddr += txtBeginBay.getText();
    sBeginAddr += txtBeginTier.getText();
                                       // Get the Ending Address.
    String sEndingAddr = txtEndBank.getText();
    sEndingAddr += txtEndBay.getText();
    sEndingAddr += txtEndTier.getText();

    locationData.setWarehouse(warhseCombo.getSelectedItem().toString());
    locationData.setAddress(sBeginAddr);
    locationData.setEndingAddress(sEndingAddr);
                                       // Set the Search Order.
    if (txtLocationOrder.getText().trim().length() != 0)
    {
      locationData.setSearchOrder(txtLocationOrder.getValue());
    }

    try                                // set of locations
    {
      String[] vasLocations = ((LocationSequencer)mpSequencerCombo.getSelectedItem()).sequenceLocations(locationData);
      if (vasLocations != null)
      {
        String info = locationServer.changeSearchOrder(vasLocations, locationData);
        displayInfo(info);
      }
      changed();                       // Fire frame (data) change event.
      setVisible(false);
      close();
    }
    catch(Exception vpException)
    {
      logAndDisplayException("Location Sequencing Error", vpException);
    }
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Add Location dialog.
   */
  @Override
  protected void clearButtonPressed()
  {                                    // Clear out the text box.
    warhseCombo.setSelectedIndex(0);
    txtBeginBank.setText("001");
    txtBeginBay.setText("001");
    txtBeginTier.setText("001");
    txtEndBank.setText("001");
    txtEndBay.setText("001");
    txtEndTier.setText("001");
                                       // Uncheck the check box.
    warhseCombo.requestFocus();
  }
}
