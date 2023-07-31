package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDialogPanel;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.util.SKDCConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Description:<BR>
 *    Panel to search for host messages by the message type.
 * @author       A.D.    11-Jul-05
 * @version      1.0
 */
public class HostMessageTypeSearchPanel extends SKDCDialogPanel
{
  private static final long serialVersionUID = 0L;
  
  private JCheckBox[]           mapJCheckBoxes;
  private HostDetailSearchFrame mpIFrame;
  protected SKDCComboBox        mpComboHostMesgNames;
  private HostProcessedMessage  mpHostProcessedMessage = new HostProcessedMessage();

  public HostMessageTypeSearchPanel(HostDetailSearchFrame ipFrame)
  {
    super(ipFrame);
    this.mpIFrame = ipFrame;
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

    vpDefs.put(new SKDCLabel("Message Name: "), mpComboHostMesgNames);
    for (int i = 0; i < mapJCheckBoxes.length; i++)
    {
      if (i == 0)
      {
        vpDefs.put(new SKDCLabel("Processed:"), mapJCheckBoxes[i]);
      }
      else
      {
        vpDefs.put(new SKDCLabel(""), mapJCheckBoxes[i]);
      }
    }
 
    return(vpDefs);
  }

 /**
  *  {@inheritDoc}
  */
  @Override
  protected void okButtonPressed()
  {
    // Find out how many Check Boxes of Processed are selected
    int vnCBoxSelected = mpHostProcessedMessage.countSelectedCheckBox(mapJCheckBoxes);
    List vpRtnList = new ArrayList();
    HostServerDelegate vpHostDelegate = mpIFrame.getHostServerDelegate();
    AbstractSKDCData   vpMesgData = mpIFrame.getDataObject();
    
                                       // Set the Host Name key.
    vpMesgData.setKey(HostToWrxData.HOSTNAME_NAME, mpIFrame.getHostName());
    
                                       // Set the Message Name key.
    String vsMessageName = mpComboHostMesgNames.getText();
    if (!vsMessageName.equals(SKDCConstants.ALL_STRING))
      vpMesgData.setKey("sMessageIdentifier", vsMessageName);
    
    if (vnCBoxSelected > 0)
    {                                  // Set the Message Processed key.
      Object[] vapProcessed = mpHostProcessedMessage.buildSelectedList(mapJCheckBoxes, vnCBoxSelected);
      vpMesgData.setInKey(WrxToHostData.MESSAGEPROCESSED_NAME, KeyObject.AND, vapProcessed);
    }

    try
    {
      vpMesgData.addOrderByColumn(WrxToHostData.MESSAGEADDTIME_NAME, true);
      vpMesgData.addOrderByColumn(WrxToHostData.MESSAGESEQUENCE_NAME, true);
      vpHostDelegate.setInfo(vpMesgData);
      vpRtnList = mpIFrame.mpHostServer.getDataQueueMessages(vpHostDelegate);
    }
    catch(DBException exc)
    {
      mpIFrame.displayError("Error finding data..." + exc.getMessage());
    }

    if (vpRtnList != null && !vpRtnList.isEmpty())
    {
      mpIFrame.changed(vpMesgData, vpRtnList);
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          mpIFrame.close();
        }
      });
    }
    else
    {
      mpIFrame.displayInfoAutoTimeOut("No data found");
    }
  }
  
/*==========================================================================
                  Private Methods go in this section.
  ==========================================================================*/
  private void defineFields()
  {
    mpComboHostMesgNames  = new SKDCComboBox();
    mapJCheckBoxes   = mpHostProcessedMessage.buildCheckBoxProcessed();
  }
}