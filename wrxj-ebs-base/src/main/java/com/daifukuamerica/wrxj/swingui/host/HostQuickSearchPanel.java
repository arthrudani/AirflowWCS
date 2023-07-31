package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCDialogPanel;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

/**
 * Description:<BR>
 *    Panel to search for host messages by date range.
 * @author       A.D.    11-Jul-05
 * @version      1.0
 */
@SuppressWarnings("serial")
public class HostQuickSearchPanel extends SKDCDialogPanel
{
  private ButtonGroup   mpBtnQuickSearchGroup;
  private JRadioButton  mpRadMessagesToday;
  private JRadioButton  mpRadLastMessage;
  private JRadioButton  mpRadOldestMessage;
  private HostDetailSearchFrame mpFrame;

  public HostQuickSearchPanel(HostDetailSearchFrame ipFrame)
  {
    super(ipFrame);
    
    this.mpFrame = ipFrame;
    setInputAreaBorder(true);
    setButtonListener();
    buildPanel();
  }
  
 /**
  *  Method initialises display columns for this Panel.
  *
  *  @return <code>Map</code> of column data.
  */
  @Override
  protected Map<SKDCLabel, JRadioButton> initDisplayColumns()
  {
    Map<SKDCLabel, JRadioButton> vpColumnDefs = new LinkedHashMap<SKDCLabel, JRadioButton>();
    defineFields();

    vpColumnDefs.put(new SKDCLabel("Messages Today: "), mpRadMessagesToday);
    vpColumnDefs.put(new SKDCLabel("Last Message: "),   mpRadLastMessage);
    vpColumnDefs.put(new SKDCLabel("Oldest Unprocessed Message: "), mpRadOldestMessage);
  
    return(vpColumnDefs);
  }

  @Override
  protected void okButtonPressed()
  {
    Object vpRtnData;
    HostServerDelegate vpHostDelegate = mpFrame.getHostServerDelegate();
    AbstractSKDCData   vpMesgData = mpFrame.getDataObject();
    
    try
    {
      if (mpRadMessagesToday.isSelected())
      {
        vpMesgData.setKey("sHostName", mpFrame.getHostName());
    
        Calendar vcCurrentDate = Calendar.getInstance(TimeZone.getDefault());
        vcCurrentDate.set(Calendar.HOUR_OF_DAY, 0);
        vcCurrentDate.clear(Calendar.MINUTE);
        vcCurrentDate.clear(Calendar.SECOND);
        vcCurrentDate.clear(Calendar.MILLISECOND);
        vpMesgData.setKey("dMessageAddTime", vcCurrentDate.getTime(), KeyObject.GREATER_THAN_INCLUSIVE);
        vpMesgData.addOrderByColumn(WrxToHostData.MESSAGEADDTIME_NAME, true);
        vpMesgData.addOrderByColumn(WrxToHostData.MESSAGESEQUENCE_NAME, true);
        vpHostDelegate.setInfo(vpMesgData);
        vpRtnData = mpFrame.mpHostServer.getDataQueueMessages(vpHostDelegate);
      }
      else if (mpRadLastMessage.isSelected())
      {
        vpMesgData = null;
        vpHostDelegate.setInfo(mpFrame.getHostName());
        vpRtnData = mpFrame.mpHostServer.getNewestDataQueueMessage(vpHostDelegate);
      }
      else                             // Get oldest unprocessed message.
      {
        vpMesgData = null;
        vpHostDelegate.setInfo(mpFrame.getHostName());
        vpRtnData = mpFrame.mpHostServer.getOldestDataQueueMessage(vpHostDelegate);
      }
    }
    catch(DBException e)
    {
      vpRtnData = null;
      mpFrame.displayError("Error finding data..." + e.getMessage());
    }
    
    if (vpRtnData != null)
    {
//      mpFrame.setCurrentPanelFrame(this);
      mpFrame.changed(vpMesgData, vpRtnData);
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
    mpBtnQuickSearchGroup = new ButtonGroup();
    mpRadMessagesToday = new JRadioButton();
    mpRadLastMessage = new JRadioButton();
    mpRadOldestMessage = new JRadioButton();
    mpRadMessagesToday.setSelected(true);
    
    mpBtnQuickSearchGroup.add(mpRadMessagesToday);
    mpBtnQuickSearchGroup.add(mpRadLastMessage);
    mpBtnQuickSearchGroup.add(mpRadOldestMessage);
  }
}