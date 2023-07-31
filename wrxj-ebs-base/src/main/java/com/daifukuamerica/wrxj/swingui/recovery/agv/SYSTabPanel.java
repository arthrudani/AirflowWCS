package com.daifukuamerica.wrxj.swingui.recovery.agv;

import com.daifukuamerica.wrxj.clc.database.DatabaseControllerTypeDefinition;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleSystemCmdData;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCRadioButton;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

/**
 * System Command panel
 * @author A.D.
 * @since  13-Jul-2009
 */
public class SYSTabPanel extends AGVTabPanel
{
  private JRadioButton    mpPICRadioBtn;

  private SKDCRadioButton  mpXMTRadBtn;
  private SKDCRadioButton  mpPICRadBtn;
  private SKDCRadioButton  mpMiscRadBtn;
  private JPanel           mpPICCmdPanel;
  private JPanel           mpXMTCmdPanel;
  private JPanel           mpMiscCmdPanel;
  private SKDCComboBox     mpCmbReportIndicators;
  private StandardDeviceServer mpDevServ = Factory.create(StandardDeviceServer.class);

  public SYSTabPanel()
  {
    SYSCmdRadioListener vpRadioListener = new SYSCmdRadioListener();

    mpXMTRadBtn  = new SKDCRadioButton("XMT Command", 'X', true);
    mpXMTRadBtn.addActionListener(vpRadioListener);
    mpPICRadBtn  = new SKDCRadioButton("PIC Command", 'P', false);
    mpPICRadBtn.addActionListener(vpRadioListener);
    mpMiscRadBtn = new SKDCRadioButton("Misc. Commands", 'M', false);
    mpMiscRadBtn.addActionListener(vpRadioListener);

    ButtonGroup vpGroup = new ButtonGroup();
    vpGroup.add(mpXMTRadBtn);
    vpGroup.add(mpPICRadBtn);
    vpGroup.add(mpMiscRadBtn);

    mpXMTCmdPanel = buildXMTCmdPanel();
    mpPICCmdPanel = buildPICCmdPanel();
    mpMiscCmdPanel = buildMISCPanel();

    setLayout(new GridBagLayout());
    GridBagConstraints vpGBConst = new GridBagConstraints();
    
    vpGBConst.gridwidth = 1;
    vpGBConst.gridx = 0;
    vpGBConst.gridy = 0;
    vpGBConst.anchor = GridBagConstraints.EAST;
    vpGBConst.insets = new Insets(5, 1, 5, 1);
    add(mpXMTCmdPanel, vpGBConst);
    vpGBConst.gridy = GridBagConstraints.RELATIVE;

    add(mpPICCmdPanel, vpGBConst);
    add(mpMiscCmdPanel, vpGBConst);

    vpGBConst.gridwidth = 2;
    vpGBConst.gridx = 1;
    vpGBConst.gridy = 0;
    vpGBConst.anchor = GridBagConstraints.WEST;
    add(mpXMTRadBtn, vpGBConst);
    vpGBConst.gridy = GridBagConstraints.RELATIVE;

    add(mpPICRadBtn, vpGBConst);
    add(mpMiscRadBtn, vpGBConst);
  }

  @Override
  public void execDataValidation()
  {
    if (mpXMTRadBtn.isSelected())
    {
      submitXMTCommand();
    }
    else if (mpPICRadBtn.isSelected())
    {
      submitPICCommand();
    }
  }

  private JPanel buildXMTCmdPanel()
  {
    GridBagConstraints vpGBConst = new GridBagConstraints();
    JPanel vpXMTPanel = new JPanel(new GridBagLayout());
    vpXMTPanel.setBorder(BorderFactory.createEtchedBorder());

    vpGBConst.gridwidth = 1;
    vpGBConst.gridx = 0;
    vpGBConst.gridy = 0;
    vpGBConst.anchor = GridBagConstraints.EAST;
    vpGBConst.insets = new Insets(5, 1, 5, 1);
    vpXMTPanel.add(new SKDCLabel("Report Indicator:"), vpGBConst);

    vpGBConst.gridwidth = 2;
    vpGBConst.gridx = 1;
    vpGBConst.anchor = GridBagConstraints.WEST;
    mpCmbReportIndicators = new SKDCComboBox(new String[] {"SSR", "ALM", "QMR", "VSR"});
    vpXMTPanel.add(mpCmbReportIndicators, vpGBConst);

    firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, vpXMTPanel, Boolean.TRUE);
    
    return(vpXMTPanel);
  }

  private JPanel buildPICCmdPanel()
  {
    GridBagConstraints vpGBConst = new GridBagConstraints();
    final JPanel vpPICPanel = new JPanel(new GridBagLayout());
    vpPICPanel.setBorder(BorderFactory.createEtchedBorder());

    vpGBConst.gridwidth = 1;
    vpGBConst.gridx = 0;
    vpGBConst.gridy = 0;
    vpGBConst.anchor = GridBagConstraints.EAST;
    vpGBConst.insets = new Insets(5, 1, 5, 1);
    vpPICPanel.add(new SKDCLabel("Pickup Status:"), vpGBConst);

    vpGBConst.gridwidth = 2;
    vpGBConst.gridx = 1;
    vpGBConst.anchor = GridBagConstraints.WEST;
    mpPICRadioBtn = new JRadioButton("Enabled", false);
    vpPICPanel.add(mpPICRadioBtn, vpGBConst);

    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, vpPICPanel, Boolean.FALSE);
      }
    });

    return(vpPICPanel);
  }

  private JPanel buildMISCPanel()
  {
    final SKDCButton vpHLDBtn = new SKDCButton("Send HLD", "HLD Command to hold all vehicles.");
    final SKDCButton vpRESBtn = new SKDCButton("Send RES", "RES Command to clear CMS queue of non-started move requests.");
    final SKDCButton vpRSUBtn = new SKDCButton("Send RSU", "RSU Command to release all vehicles.");

    final JPanel vpMISCPanel = new JPanel(new GridBagLayout());
    vpMISCPanel.setBorder(BorderFactory.createEtchedBorder());

    GridBagConstraints vpConst = new GridBagConstraints();
    vpConst.insets = new Insets(10, 30, 10, 30);
    vpConst.gridwidth = 2;
    vpConst.weightx = 0.2;
    vpConst.weighty = 0.5;

    vpConst.anchor = GridBagConstraints.CENTER;
    vpConst.gridx = 0;
    vpConst.gridy = 0;

    vpMISCPanel.add(vpHLDBtn, vpConst);
    vpConst.gridy = GridBagConstraints.RELATIVE;

    vpMISCPanel.add(vpRESBtn, vpConst);
    vpMISCPanel.add(vpRSUBtn, vpConst);

    ActionListener vpMiscButtonListener = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        SKDCButton vpPressed = (SKDCButton)e.getSource();
        if (vpPressed == vpHLDBtn)
        {
          submitHLDCommand();
        }
        else if (vpPressed == vpRESBtn)
        {
          submitRESCommand();
        }
        else if (vpPressed == vpRSUBtn)
        {
          submitRSUCommand();
        }
        else
        {
          JOptionPane.showMessageDialog(null, "Unknown command button pressed!",
                                        "DB Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    };

    vpHLDBtn.addActionListener(vpMiscButtonListener);
    vpRESBtn.addActionListener(vpMiscButtonListener);
    vpRSUBtn.addActionListener(vpMiscButtonListener);

    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, vpMISCPanel, Boolean.FALSE);
      }
    });

    return(vpMISCPanel);
  }

  private void submitXMTCommand()
  {
    String vsReportIndicator = mpCmbReportIndicators.getText();
    if (!vsReportIndicator.equals(AGVMessageNameEnum.SSR_RESPONSE.getValue()))
    {
      JOptionPane.showMessageDialog(null, vsReportIndicator + " not implemented yet.",
                                 "Info.", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    String vsMessageName = AGVMessageNameEnum.XMT_REQUEST.getValue();

    VehicleSystemCmdData vpVSData = Factory.create(VehicleSystemCmdData.class);
    vpVSData.setSystemMessageID(vsMessageName);
    vpVSData.setCommandValue(vsReportIndicator);

    try
    {
      mpDevServ.addAGVCommandRecord(vpVSData);
                                     // Get the assigned sequence number for
                                     // this record
      int vnRequestSequenceNum = vpVSData.getSequenceNumber();
      ThreadSystemGateway.get().publishHostMesgReceiveEvent(vsMessageName,
                                                     vnRequestSequenceNum,
                                                     DatabaseControllerTypeDefinition.AGV_TYPE);
      JOptionPane.showMessageDialog(null, "XMT command successfully sent!",
                               "Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, "Error adding XMT command. " +
                    exc.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void submitPICCommand()
  {
    String vsPickStatus = (mpPICRadioBtn.isSelected())        ?
                          AGVMessageConstants.STATION_ENABLED :
                          AGVMessageConstants.STATION_DISABLED;

    String vsMessageName = AGVMessageNameEnum.PIC_REQUEST.getValue();
    VehicleSystemCmdData vpVSData = Factory.create(VehicleSystemCmdData.class);
    vpVSData.setSystemMessageID(vsMessageName);
    vpVSData.setCommandValue(vsPickStatus);

    try
    {
      mpDevServ.addAGVCommandRecord(vpVSData);
                                     // Get the assigned sequence number for
                                     // this record
      int vnRequestSequenceNum = vpVSData.getSequenceNumber();
      ThreadSystemGateway.get().publishHostMesgReceiveEvent(vsMessageName,
                                                     vnRequestSequenceNum,
                                                     DatabaseControllerTypeDefinition.AGV_TYPE);
      JOptionPane.showMessageDialog(null, "PIC command successfully sent!",
                               "Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, "Error adding PIC command. " +
                    exc.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void submitHLDCommand()
  {
    String vsMessageName = AGVMessageNameEnum.HLD_REQUEST.getValue();
    VehicleSystemCmdData vpVSData = Factory.create(VehicleSystemCmdData.class);
    vpVSData.setSystemMessageID(vsMessageName);

    try
    {
      mpDevServ.addAGVCommandRecord(vpVSData);
                                     // Get the assigned sequence number for
                                     // this record
      int vnRequestSequenceNum = vpVSData.getSequenceNumber();
      ThreadSystemGateway.get().publishHostMesgReceiveEvent(vsMessageName,
                                                     vnRequestSequenceNum,
                                                     DatabaseControllerTypeDefinition.AGV_TYPE);
      JOptionPane.showMessageDialog(null, "HLD command successfully sent!",
                               "Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, "Error adding HLD command. " +
                    exc.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void submitRSUCommand()
  {
    String vsMessageName = AGVMessageNameEnum.RSU_REQUEST.getValue();
    VehicleSystemCmdData vpVSData = Factory.create(VehicleSystemCmdData.class);
    vpVSData.setSystemMessageID(vsMessageName);

    try
    {
      mpDevServ.addAGVCommandRecord(vpVSData);
                                     // Get the assigned sequence number for
                                     // this record
      int vnRequestSequenceNum = vpVSData.getSequenceNumber();
      ThreadSystemGateway.get().publishHostMesgReceiveEvent(vsMessageName,
                                                     vnRequestSequenceNum,
                                                     DatabaseControllerTypeDefinition.AGV_TYPE);
      JOptionPane.showMessageDialog(null, "RSU command successfully sent!",
                               "Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, "Error adding RSU command. " +
                    exc.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void submitRESCommand()
  {
    String vsMessageName = AGVMessageNameEnum.RES_REQUEST.getValue();
    VehicleSystemCmdData vpVSData = Factory.create(VehicleSystemCmdData.class);
    vpVSData.setSystemMessageID(vsMessageName);

    try
    {
      mpDevServ.addAGVCommandRecord(vpVSData);
                                     // Get the assigned sequence number for
                                     // this record
      int vnRequestSequenceNum = vpVSData.getSequenceNumber();
      ThreadSystemGateway.get().publishHostMesgReceiveEvent(vsMessageName,
                                                     vnRequestSequenceNum,
                                                     DatabaseControllerTypeDefinition.AGV_TYPE);
      JOptionPane.showMessageDialog(null, "RES command successfully sent!",
                               "Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, "Error adding RES command. " +
                    exc.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private class SYSCmdRadioListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      SKDCRadioButton vpSelected = (SKDCRadioButton)e.getSource();
      if (vpSelected == mpXMTRadBtn)
      {
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, mpXMTCmdPanel, Boolean.TRUE);
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, mpPICCmdPanel, Boolean.FALSE);
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, mpMiscCmdPanel, Boolean.FALSE);
      }
      else if (vpSelected == mpPICRadBtn)
      {
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, mpXMTCmdPanel, Boolean.FALSE);
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, mpPICCmdPanel, Boolean.TRUE);
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, mpMiscCmdPanel, Boolean.FALSE);
      }
      else if (vpSelected == mpMiscRadBtn)
      {
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, mpXMTCmdPanel, Boolean.FALSE);
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, mpPICCmdPanel, Boolean.FALSE);
        firePropertyChange(AGVTabPanel.SYS_PANEL_EVT, mpMiscCmdPanel, Boolean.TRUE);
      }
    }
  }
}
