package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDialogPanel;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Class searches for Host Messages using data fields in the CLOB.  Currently,
 * three generic fields are provided for the search.
 *
 * @author A.D.
 * @since 13-Jun-2007
 */
@SuppressWarnings("serial")
public class HostDataSearchPanel extends SKDCDialogPanel
{
  protected SKDCComboBox        mpComboHostMesgNames;
  private SKDCTextField         mpSearchField_1;
  private SKDCTextField         mpSearchField_2;
  private SKDCTextField         mpSearchField_3;
  private JCheckBox []          mpCheckboxProcessed;
  private HostDetailSearchFrame mpFrame;
  private HostProcessedMessage  mpHostProcessedMessage = new HostProcessedMessage();
  
  private final int GENERIC_FIELD_SIZE = 35;
  
  public HostDataSearchPanel(HostDetailSearchFrame ipFrame)
  {
    super(ipFrame);
    mpFrame = ipFrame;
    setInputAreaBorder(true);
    setButtonListener();
    buildPanel(mpHostProcessedMessage.COLNUM);
  }

 /**
  *  Method initialises display columns for this Panel.
  *
  *  @return <code>Map</code> of column data.
  */
  @Override
  protected Map<SKDCLabel, JComponent> initDisplayColumns()
  {
    Map<SKDCLabel, JComponent> vpDefs = new LinkedHashMap<SKDCLabel, JComponent>();
    defineFields();

    vpDefs.put(new SKDCLabel("Message Name:"), mpComboHostMesgNames);
    for (int i = 0; i < mpCheckboxProcessed.length; i++)
    {
      if (i == 0)
      {
        vpDefs.put(new SKDCLabel("Processed:"), mpCheckboxProcessed[i]);
      }
      else
      {
        vpDefs.put(new SKDCLabel(""), mpCheckboxProcessed[i]);
      }
    }
    vpDefs.put(new SKDCLabel("Search Field #1:"), mpSearchField_1);
    vpDefs.put(new SKDCLabel("Search Field #2:"), mpSearchField_2);
    vpDefs.put(new SKDCLabel("Search Field #3:"), mpSearchField_3);

    return(vpDefs);
  }

 /**
  * {@inheritDoc}
  */
  @Override
  protected void okButtonPressed()
  {
    // Find out how many Check Boxes of Processed are selected
    int vnSelected = mpHostProcessedMessage.countSelectedCheckBox(mpCheckboxProcessed);
    List vpRtnList = new ArrayList();

    HostServerDelegate vpHostDelegate = mpFrame.getHostServerDelegate();
    AbstractSKDCData vpMesgData = mpFrame.getDataObject();
    
                                       // Set the Host Name key.
    vpMesgData.setKey(WrxToHostData.HOSTNAME_NAME, mpFrame.getHostName());
    
                                       // Set the Message Name key.
    String vsMessageName = mpComboHostMesgNames.getText();
    if (!vsMessageName.equals(SKDCConstants.ALL_STRING))
      vpMesgData.setKey(WrxToHostData.MESSAGEIDENTIFIER_NAME, vsMessageName);
    
                                       // Set the Message Processed key if it's
    if (vnSelected > 0)                // used.
    {
      Object[] vapProcessed = mpHostProcessedMessage.buildSelectedList(mpCheckboxProcessed, vnSelected);
      vpMesgData.setInKey(WrxToHostData.MESSAGEPROCESSED_NAME, KeyObject.AND, vapProcessed);
    }
    
                                       // Set up the search field options.
    if (!mpSearchField_1.isEmpty())
      vpMesgData.setWildcardKey(WrxToHostData.MESSAGE_NAME, mpSearchField_1.getText(), true);

    if (!mpSearchField_2.isEmpty())
      vpMesgData.setWildcardKey(WrxToHostData.MESSAGE_NAME, mpSearchField_2.getText(), true);

    if (!mpSearchField_3.isEmpty())
      vpMesgData.setWildcardKey(WrxToHostData.MESSAGE_NAME, mpSearchField_3.getText(), true);

    try
    {
      vpMesgData.addOrderByColumn(WrxToHostData.MESSAGEADDTIME_NAME, true);
      vpMesgData.addOrderByColumn(WrxToHostData.MESSAGESEQUENCE_NAME, true);
      vpHostDelegate.setInfo(vpMesgData);
      vpRtnList = mpFrame.mpHostServer.getDataQueueMessages(vpHostDelegate);
    }
    catch(DBException exc)
    {
      mpFrame.displayError("Error finding data..." + exc.getMessage());
    }

    if (vpRtnList != null && !vpRtnList.isEmpty())
    {
      mpFrame.changed(vpMesgData, vpRtnList);
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          mpFrame.close();
        }
      });
    }
    else
    {
      mpFrame.displayInfoAutoTimeOut("No data found");
    }
  }

/*==========================================================================
                  Private Methods go in this section.
  ==========================================================================*/
  private void defineFields()
  {
    mpComboHostMesgNames  = new SKDCComboBox();
    mpCheckboxProcessed = mpHostProcessedMessage.buildCheckBoxProcessed();
    mpSearchField_1       = new SKDCTextField(GENERIC_FIELD_SIZE);
    mpSearchField_2       = new SKDCTextField(GENERIC_FIELD_SIZE);
    mpSearchField_3       = new SKDCTextField(GENERIC_FIELD_SIZE);
  }
}
