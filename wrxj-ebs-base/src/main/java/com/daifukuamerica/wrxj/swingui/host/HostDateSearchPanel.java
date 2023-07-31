package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCDialogPanel;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Description:<BR>
 *    Panel to search for host messages by date range.
 * @author       A.D.    11-Jul-05
 * @version      1.0
 */
@SuppressWarnings("serial")
public class HostDateSearchPanel extends SKDCDialogPanel
{
  protected SKDCComboBox  mpComboHostMesgNames;
  private   SKDCDateField mpBeginDateField;
  private   SKDCDateField mpEndDateField;
  private   JCheckBox[]  mapJCheckBoxes;
  private   HostDetailSearchFrame mpFrame;
  private HostProcessedMessage  mpHostProcessedMessage = new HostProcessedMessage();

  public HostDateSearchPanel(HostDetailSearchFrame ipFrame)
  {
    super(ipFrame);
    this.mpFrame = ipFrame;
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

    vpDefs.put(new SKDCLabel("Begin Date:"),   mpBeginDateField);
    vpDefs.put(new SKDCLabel("End Date:"),     mpEndDateField);
    vpDefs.put(new SKDCLabel("Message Name:"), mpComboHostMesgNames);
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
  * {@inheritDoc}
  */
  @Override
  protected void okButtonPressed()
  {    
    // Find out how many Check Boxes of Processed are selected
    int vnCBoxSelected = mpHostProcessedMessage.countSelectedCheckBox(mapJCheckBoxes);
    List vpRtnList = new ArrayList();

    HostServerDelegate vpHostDelegate = mpFrame.getHostServerDelegate();
    AbstractSKDCData vpMesgData = mpFrame.getDataObject();

                                       // Set the Beginning and Ending Date keys.
    Date vdBeginDate = mpBeginDateField.getDate();
    Date vdEndDate = mpEndDateField.getDate();
    Calendar vpBeginCalDate = Calendar.getInstance();
    vpBeginCalDate.setTime(vdBeginDate);
    vpBeginCalDate.clear(Calendar.MILLISECOND);
    
    Calendar vpEndCalDate = Calendar.getInstance();
    vpEndCalDate.setTime(vdEndDate);
    vpEndCalDate.clear(Calendar.MILLISECOND);
    
    switch(vpBeginCalDate.compareTo(vpEndCalDate))
    {
      case 0:
        vpEndCalDate.add(Calendar.MILLISECOND, 999);

      case -1:
        vpMesgData.setBetweenKey("dMessageAddTime", vpBeginCalDate.getTime(),
                               vpEndCalDate.getTime());
        break;
        
      default:
        vpMesgData.setBetweenKey("dMessageAddTime", vpEndCalDate.getTime(),
                               vpBeginCalDate.getTime());
    }
                                       // Set the Host Name key.
    vpMesgData.setKey("sHostName", mpFrame.getHostName());
                                       // Set the Message Name key.
    String vsMessageName = mpComboHostMesgNames.getText();
    if (!vsMessageName.equals(SKDCConstants.ALL_STRING))
      vpMesgData.setKey("sMessageIdentifier", vsMessageName);
    
    if (vnCBoxSelected > 0)
    {                                  // Set the Message Processed key.
      Object[] vapProcessed = mpHostProcessedMessage.buildSelectedList(mapJCheckBoxes, 
                                                                     vnCBoxSelected);
      vpMesgData.setInKey(WrxToHostData.MESSAGEPROCESSED_NAME, KeyObject.AND, vapProcessed);
    }
    
    try
    {
      if (vpMesgData.getKeyCount() > 0)
      {
        vpMesgData.addOrderByColumn(WrxToHostData.MESSAGEADDTIME_NAME, true);
        vpMesgData.addOrderByColumn(WrxToHostData.MESSAGESEQUENCE_NAME, true);
        vpHostDelegate.setInfo(vpMesgData);
        vpRtnList = mpFrame.mpHostServer.getDataQueueMessages(vpHostDelegate);
      }
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
    mpBeginDateField      = new SKDCDateField(false);
    mpEndDateField        = new SKDCDateField(false);
    mpComboHostMesgNames  = new SKDCComboBox();
    mapJCheckBoxes   = mpHostProcessedMessage.buildCheckBoxProcessed();

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    mpBeginDateField.setDate(cal.getTime());
  }
}