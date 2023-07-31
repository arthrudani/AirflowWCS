package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostInDelegate;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

/**
 * Description:<BR>
 *    Frame to search for host messages.  This frame manages three search panels.
 * @author       A.D.    11-Jul-05
 * @version      1.0
 */
@SuppressWarnings("serial")
public class HostDetailSearchFrame extends SKDCInternalFrame
{
  private   String[]             masHostNameList;
  private   String[]             masInboundMessageNames;
  private   String[]             masOutboundMessageNames;
  private   JCheckBox            mpInboundCheckBox;
  private   HostDateSearchPanel  mpDateSearchPanel;
  private   HostQuickSearchPanel mpQuickSearchPanel;
  private   HostMessageTypeSearchPanel mpMesgTypeSearchPanel;
  private   HostDataSearchPanel  mpDataSearchPanel;
  private   HostSeqNumberSearchPanel  mpSeqNumSearchPanel;
  private   SKDCComboBox         mpComboHostName;
  private   HostInDelegate       mpHostInDelegate;
  private   HostOutDelegate      mpHostOutDelegate;
  protected StandardHostServer   mpHostServer;

  public HostDetailSearchFrame(String[] iasHostNameList, String isHostName, boolean izSelected)
  {
    this(iasHostNameList);
    if (isHostName != null && isHostName.length() > 0)
    {
      mpComboHostName.selectItemBy(isHostName);
    }
    mpInboundCheckBox.setSelected(izSelected);
  }
  
  public HostDetailSearchFrame(String[] iasHostNameList)
  {
    super("Detailed Message Search");
    masHostNameList = iasHostNameList;
    initDataComponents();
    initSwingComponents();
    
    JTabbedPane vpTabbedPane = new JTabbedPane();
    mpDateSearchPanel     = new HostDateSearchPanel(this);
    mpQuickSearchPanel    = new HostQuickSearchPanel(this);
    mpMesgTypeSearchPanel = new HostMessageTypeSearchPanel(this);
    mpDataSearchPanel     = new HostDataSearchPanel(this);
    mpSeqNumSearchPanel   = new HostSeqNumberSearchPanel(this);
    
    vpTabbedPane.addTab("Search By Data Value(s)", mpDataSearchPanel);
    vpTabbedPane.addTab("Search By Date", mpDateSearchPanel);
    vpTabbedPane.addTab("Search By Seq. Numbers", mpSeqNumSearchPanel);
    vpTabbedPane.addTab("Quick Search", mpQuickSearchPanel);
    vpTabbedPane.addTab("Search By Message Type", mpMesgTypeSearchPanel);

    Container contentPane = getContentPane();
    contentPane.add(vpTabbedPane, BorderLayout.CENTER);
    
    JPanel vpWestPanel = buildWestPanel();
    contentPane.add(vpWestPanel, BorderLayout.WEST);
    setItemEventListener();
    
    mpDateSearchPanel.mpComboHostMesgNames.setComboBoxData(masInboundMessageNames, SKDCConstants.ALL_STRING);
    mpMesgTypeSearchPanel.mpComboHostMesgNames.setComboBoxData(masInboundMessageNames);
    mpDataSearchPanel.mpComboHostMesgNames.setComboBoxData(masInboundMessageNames, SKDCConstants.ALL_STRING);
    mpSeqNumSearchPanel.mpComboHostMesgNames.setComboBoxData(masInboundMessageNames, SKDCConstants.ALL_STRING);
    mpSeqNumSearchPanel.setHostSeqVisiable(mpInboundCheckBox.isSelected());

    setPreferredSize(resetSize(vpTabbedPane, vpWestPanel));
  }
  
  public HostServerDelegate getHostServerDelegate()
  {
    return((mpInboundCheckBox.isSelected()) ? mpHostInDelegate : mpHostOutDelegate);
  }
  
  public boolean getInboundCheckBox()
  {
    return(mpInboundCheckBox.isSelected());
  }
  
  public String getSelectedHostName()
  {
    return(mpComboHostName.getText());
  }

  public AbstractSKDCData getDataObject()
  {
    AbstractSKDCData mesgData;
    if (mpInboundCheckBox.isSelected())
      mesgData = Factory.create(HostToWrxData.class);
    else
      mesgData = Factory.create(WrxToHostData.class);
    
    return(mesgData);
  }
  
  public String getHostName()
  {
    return(mpComboHostName.getText());
  }
  
  private void initDataComponents()
  {
    mpHostServer = Factory.create(StandardHostServer.class);
    mpHostOutDelegate = new HostOutDelegate();
    mpHostInDelegate = new HostInDelegate();
    masInboundMessageNames = getMessageNames(mpHostInDelegate);
    masOutboundMessageNames = getMessageNames(mpHostOutDelegate);
  }

  private void initSwingComponents()
  {
    mpComboHostName = new SKDCComboBox(masHostNameList);
    mpComboHostName.requestFocus();
    mpInboundCheckBox = new JCheckBox();
    mpInboundCheckBox.setSelected(true);
  }

  private void setItemEventListener()
  {
    mpInboundCheckBox.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          mpDateSearchPanel.mpComboHostMesgNames.setComboBoxData(masInboundMessageNames, SKDCConstants.ALL_STRING);
          mpMesgTypeSearchPanel.mpComboHostMesgNames.setComboBoxData(masInboundMessageNames);
          mpDataSearchPanel.mpComboHostMesgNames.setComboBoxData(masInboundMessageNames, SKDCConstants.ALL_STRING);
          mpSeqNumSearchPanel.setHostSeqVisiable(true);
          mpSeqNumSearchPanel.mpComboHostMesgNames.setComboBoxData(masInboundMessageNames, SKDCConstants.ALL_STRING);
        }
        else
        {
          mpDateSearchPanel.mpComboHostMesgNames.setComboBoxData(masOutboundMessageNames, SKDCConstants.ALL_STRING);
          mpMesgTypeSearchPanel.mpComboHostMesgNames.setComboBoxData(masOutboundMessageNames);
          mpDataSearchPanel.mpComboHostMesgNames.setComboBoxData(masOutboundMessageNames, SKDCConstants.ALL_STRING);
          mpSeqNumSearchPanel.setHostSeqVisiable(false);
          mpSeqNumSearchPanel.mpComboHostMesgNames.setComboBoxData(masOutboundMessageNames, SKDCConstants.ALL_STRING);
        }
      }
    });
  }
  
  private String[] getMessageNames(HostServerDelegate hostServDelegate)
  {
    String[] messageNames = new String[0];
    try
    {
      messageNames = mpHostServer.getMessageNames(hostServDelegate);
    }
    catch (DBException e)
    {
      displayError("DB Error finding host message names..." + e.getMessage());
    }
    return(messageNames);
  }
  
  private JPanel buildWestPanel()
  {
    JPanel commonPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbagConst = new GridBagConstraints();
    gbagConst.gridwidth = 1;
    gbagConst.gridx = 0;
    gbagConst.gridy = 0;

    gbagConst.anchor = GridBagConstraints.EAST;
    gbagConst.insets = new Insets(1, 1, 5, 3);
    commonPanel.add(new SKDCLabel("Host Names:"), gbagConst);

    gbagConst.gridx = GridBagConstraints.RELATIVE;
    gbagConst.anchor = GridBagConstraints.WEST;
    gbagConst.insets = new Insets(1, 2, 5, 1);
    commonPanel.add(mpComboHostName, gbagConst);
    
    gbagConst.gridx = 0;
    gbagConst.gridy = 1;
    gbagConst.anchor = GridBagConstraints.EAST;
    gbagConst.insets = new Insets(1, 1, 5, 3);
    commonPanel.add(new SKDCLabel("Inbound Messages:"), gbagConst);

    gbagConst.gridx = GridBagConstraints.RELATIVE;
    gbagConst.anchor = GridBagConstraints.WEST;
    gbagConst.insets = new Insets(1, 2, 5, 1);
    commonPanel.add(mpInboundCheckBox, gbagConst);
    
    TitledBorder titledBorder = BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "Common Options");
    commonPanel.setBorder(titledBorder);
    
    JPanel westPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbconst = new GridBagConstraints();
    gbconst.insets = new Insets(1, 1, 30, 1);

    gbconst.gridwidth = 1;
    gbconst.gridx = 0;
    gbconst.gridy = 0;
    gbconst.anchor = GridBagConstraints.CENTER;
    westPanel.add(commonPanel, gbconst);

    return(westPanel);
  }
  
  private Dimension resetSize(JComponent...ipComponent)
  {
    int vnWidth = 0;
    int vnHeight = (int)(getPreferredSize().getHeight());
    
    for(JComponent vpComp : ipComponent)
    {
      vnWidth += (int)(vpComp.getPreferredSize().getWidth());
    }
  
    return(new Dimension(vnWidth + 40, vnHeight));
  }
}
