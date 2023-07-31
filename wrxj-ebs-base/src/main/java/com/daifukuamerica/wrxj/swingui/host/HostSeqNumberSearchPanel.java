package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.host.HostInDelegate;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDialogPanel;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.util.SKDCConstants;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

/**
 * Description:<BR>
 *    Panel to search for host messages by sequence number range.
 * @author       Y.Kang    23-Apr-10
 * @version      1.0
 */
@SuppressWarnings("serial")
public class HostSeqNumberSearchPanel extends SKDCDialogPanel
{
  protected SKDCComboBox          mpComboHostMesgNames;
  private   ButtonGroup           mpBtnSeqNumberGroup;
  private   JPanel                mpSeqNumberPanel;
  private   SKDCIntegerField      mpBegSeqField;
  private   SKDCIntegerField      mpEndSeqField;
  private   JRadioButton          mpRadWrxSeq;
  private   JRadioButton          mpRadHostSeq;
  private   JCheckBox[]           mapJCheckBoxes;
  private   HostDetailSearchFrame mpFrame;
  private   HostProcessedMessage  mpHostProcessedMessage = new HostProcessedMessage();
  private   HostServerDelegate    mpHostDelegate;
  private   HostOutDelegate       mpHostOutDelegate = new HostOutDelegate();
  private   HostInDelegate        mpHostInDelegate = new HostInDelegate();

  public HostSeqNumberSearchPanel(HostDetailSearchFrame ipFrame)
  {
    super(ipFrame);
    this.mpFrame = ipFrame;
    setInputAreaBorder(true);
    setButtonListener();
    buildPanel(mpHostProcessedMessage.COLNUM);
  }

 /**
  *  Method initializes display columns for this Panel.
  *
  *  @return <code>Map</code> of column data.
  */
  @Override
  protected Map<SKDCLabel, JComponent> initDisplayColumns()
  {
    Map<SKDCLabel, JComponent> vpDefs = new LinkedHashMap<SKDCLabel, JComponent>();
    defineFields();

    vpDefs.put(new SKDCLabel("Number Group:"), mpSeqNumberPanel);
    
    vpDefs.put(new SKDCLabel("Begin  Number:"), mpBegSeqField);
    vpDefs.put(new SKDCLabel("Ending Number:"), mpEndSeqField);
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

    String vsColumnName;
    if (mpRadHostSeq.isSelected())
    {
      vsColumnName = "iOriginalSequence";
    }
    else
    {
      vsColumnName = "iMessageSequence";
    }

    // Set the Beginning and Ending Sequence keys.
    int vnBegSeqNumber = mpBegSeqField.getValue();
    int vnEndSeqNumber = mpEndSeqField.getValue();

    if (vnBegSeqNumber > vnEndSeqNumber)
    {
      mpFrame.displayError("Start sequence must be smaller than ending sequence!");
      return;
    }
    else if (vnBegSeqNumber == vnEndSeqNumber)
    {
      vpMesgData.setKey(vsColumnName, Integer.valueOf(vnBegSeqNumber));
    }
    else
    {
      vpMesgData.setBetweenKey(vsColumnName, vnBegSeqNumber, vnEndSeqNumber);
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
  
  public void setHostSeqVisiable(boolean izVisible)
  {
    mpRadHostSeq.setVisible(izVisible);
    mpRadWrxSeq.setSelected(true);
    
    // When Host Sequence is visible, it means InBound message
    if (izVisible)
    {
      mpHostDelegate = mpHostInDelegate;
    }
    else
    {
      mpHostDelegate = mpHostOutDelegate;
    }
    fillMinMaxTextFields();
  }

/*==========================================================================
                  Private Methods go in this section.
  ==========================================================================*/
  private void defineFields()
  {
    mpBtnSeqNumberGroup = new ButtonGroup();
    mpRadWrxSeq         = new JRadioButton("WRx Sequence");
    mpRadHostSeq        = new JRadioButton("Host Sequence");
    mpRadWrxSeq.setSelected(true);
    
    mpBtnSeqNumberGroup.add(mpRadWrxSeq);
    mpBtnSeqNumberGroup.add(mpRadHostSeq);

    mpSeqNumberPanel = new JPanel();
    mpSeqNumberPanel.add(mpRadWrxSeq);
    mpSeqNumberPanel.add(mpRadHostSeq);

    mpBegSeqField        = new SKDCIntegerField(20);
    mpEndSeqField        = new SKDCIntegerField(20);
    mpComboHostMesgNames = new SKDCComboBox();
    mapJCheckBoxes       = mpHostProcessedMessage.buildCheckBoxProcessed();
    
    mpRadWrxSeq.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fillMinMaxTextFields();
      }
    });
    mpRadHostSeq.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fillMinMaxTextFields();
      }
    });
  }

  /**
   * Fill in the min/max search fields
   */
  private void fillMinMaxTextFields()
  {
    try
    {
      mpHostDelegate.setInfo(mpFrame.getHostName());
      int[] vanMinMax;
      if (mpRadWrxSeq.isSelected())
      {
        vanMinMax = mpFrame.mpHostServer.getMinMaxSequence(mpHostDelegate);
      }
      else
      {
        vanMinMax = mpFrame.mpHostServer.getHostMinMaxSequence(mpHostDelegate);
      }
      mpBegSeqField.setValue(vanMinMax[0]);
      mpEndSeqField.setValue(vanMinMax[1]);
    }
    catch (DBException exc)
    {
      mpFrame.displayError(exc.getMessage());
    }
  }
}
