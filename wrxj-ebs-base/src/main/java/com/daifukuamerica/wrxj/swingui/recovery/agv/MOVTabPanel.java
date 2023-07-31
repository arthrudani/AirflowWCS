package com.daifukuamerica.wrxj.swingui.recovery.agv;

import com.daifukuamerica.wrxj.clc.database.DatabaseControllerTypeDefinition;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleMoveData;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Panel for Move Tab.
 *
 * @author A.D.
 * @since  09-Jul-2009
 */
public class MOVTabPanel extends AGVTabPanel
{
  protected final int REFRESH_TIME = 7000;
  protected StandardDeviceServer mpDevServ = Factory.create(StandardDeviceServer.class);
  protected DacTable mpTable = null;
  protected SKDCComboBox  mpCmbRoute = new SKDCComboBox();
  protected SKDCTextField mpTextLoad = new SKDCTextField(VehicleMoveData.LOADID_NAME);
  protected SKDCCheckBox  mpChkBoxRefreshTimer = new SKDCCheckBox("Auto-Refresh");
  protected Timer mpRefreshTimer;

  public MOVTabPanel()
  {
    initPanelComponents();
    setPreferredSize(new Dimension(746, 400));
    refreshTable();
    openTabOperations();
  }

  @Override
  public void openTabOperations()
  {
    if (mpRefreshTimer != null)
    {
      if (!mpRefreshTimer.isRunning())
        mpRefreshTimer.restart();
    }
    else
    {
      createRefreshTimer();
    }
  }

  @Override
  public void closeTabOperations()
  {
    if (mpRefreshTimer != null)
    {
      mpRefreshTimer.stop();
    }
  }

  @Override
  public void deleteCommand()
  {
    Map vpRowMap = mpTable.getSelectedRowData();
    String vsLoadID = (String)vpRowMap.get(VehicleMoveData.LOADID_NAME);
    try
    {
      String vsAsk = "Delete AGV Move for load " + vsLoadID + "?";
      int vnOption = JOptionPane.showConfirmDialog(null, vsAsk,
                                 "Select One", JOptionPane.YES_NO_OPTION);
      if (vnOption == JOptionPane.YES_OPTION)
      {
        mpDevServ.deleteAGVMoveRecordByLoadID(vsLoadID);
      }
    }
    catch(DBException ex)
    {
      JOptionPane.showMessageDialog(null, "Error deleting vehicle move for load " + vsLoadID,
                                    "Selection Error",JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void cancelCommand()
  {
    if (mpTable.getSelectedRowCount() == 0)
    {
      JOptionPane.showMessageDialog(null, "No rows selected for cancellation!",
                                    "Input Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    try
    {
      String[] vasSelectedLoad = mpTable.getSelectedColumnData(VehicleMoveData.LOADID_NAME);

      for(String vsLoadID : vasSelectedLoad)
      {
        switch(mpDevServ.getAGVMoveStatus(vsLoadID))
        {
          case DBConstants.AGV_MOVING:
          case DBConstants.AGV_MOVECOMPLETE:
            JOptionPane.showMessageDialog(null, "Load " + vsLoadID +
                         " has already been picked up! Move cannot be canceled.",
                         "Error", JOptionPane.ERROR_MESSAGE);
            break;

          case DBConstants.AGV_MOVECANCELED:
            JOptionPane.showMessageDialog(null, "Move for Load " +
                      mpTextLoad.getText() + " has already been canceled.", "Error",
                      JOptionPane.WARNING_MESSAGE);
            break;

          case DBConstants.AGV_MOVECANCELREQUEST:
          case DBConstants.AGV_MOVECANCELPENDING:
            int vnResp = JOptionPane.showConfirmDialog(null, "Move for Load " +
                    mpTextLoad.getText() + " has already been submitted for " +
                    "cancellation! Resubmit Request");
            if (vnResp == JOptionPane.YES_OPTION)
            {
              mpDevServ.updateAGVMoveStatus(mpTextLoad.getText(),
                                            DBConstants.AGV_MOVECANCELREQUEST);
              VehicleMoveData vpMoveData = mpDevServ.getAGVMoveRecord(vsLoadID);

              String vsMessageName = AGVMessageNameEnum.CAN_REQUEST.getValue();
              int vnSequence = vpMoveData.getSequenceNumber();
              ThreadSystemGateway.get().publishHostMesgReceiveEvent(vsMessageName,
                           vnSequence, DatabaseControllerTypeDefinition.AGV_TYPE);
              JOptionPane.showMessageDialog(null, "Move command submitted for cancellation.",
                                   "Confirmation", JOptionPane.INFORMATION_MESSAGE);
            }
            break;

          default:
            mpDevServ.updateAGVMoveStatus(vsLoadID, DBConstants.AGV_MOVECANCELREQUEST);
            VehicleMoveData vpMoveData = mpDevServ.getAGVMoveRecord(vsLoadID);

            String vsMessageName = AGVMessageNameEnum.CAN_REQUEST.getValue();
            int vnSequence = vpMoveData.getSequenceNumber();
                                       // Send message to AGV Controller.
            ThreadSystemGateway.get().publishHostMesgReceiveEvent(vsMessageName, vnSequence,
                                                           DatabaseControllerTypeDefinition.AGV_TYPE);
        }
      }
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, "Error adding AGV move cancel " +
                  "request! " + exc.getMessage(), "DB Error",
                  JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void execDataValidation()
  {
    if (mpTextLoad.getText().trim().length() == 0)
    {
      JOptionPane.showMessageDialog(null, "Load Input required!", "Input Error",
                                    JOptionPane.ERROR_MESSAGE);
    }
    else if (mpDevServ.agvLoadExists(mpTextLoad.getText()))
    {
      JOptionPane.showMessageDialog(null, "AGV Move already exists for this load!",
                                    "Input Error", JOptionPane.ERROR_MESSAGE);
    }
    else
    {
      try
      {
        addAGVMoveRequest();
        JOptionPane.showMessageDialog(null, "Move command successfully sent!",
                               "Confirmation", JOptionPane.INFORMATION_MESSAGE);
      }
      catch(DBException e)
      {
        JOptionPane.showMessageDialog(null, "Error adding AGV move request! " +
                    e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  @Override
  public void refreshTable()
  {
    try
    {
      List<Map> vpMoves = mpDevServ.getAllAGVMoveRecordsByStatus();
      mpTable.refreshData(vpMoves);
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, "Error finding AGV Move data " +
                  exc.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

 /**
  * Method to help recover AGV moves under various circumstances.
  */
  @Override
  public void errorCleanUp()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        int vnSelectCount = mpTable.getSelectedRowCount();
        if (vnSelectCount == 0 || vnSelectCount > 1)
        {
          String[] vasFromToStns = mpCmbRoute.getText().split("-");
          if (vasFromToStns != null && vasFromToStns.length > 0)
          {
            JOptionPane.showMessageDialog(null, "Select one row for recovery! ",
                                 "Selection Problem", JOptionPane.INFORMATION_MESSAGE);
          }
          return;
        }

        String vsAGVLoad = (String)mpTable.getCurrentRowDataField(VehicleMoveData.LOADID_NAME);
        VehicleMoveData vpAGVMoveData;
        try
        {
                                           // Make sure the're not playing with
                                           // stale data.
          StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
          vpAGVMoveData = vpDevServ.getAGVMoveRecord(vsAGVLoad);

          if (vpAGVMoveData == null)
          {
            JOptionPane.showMessageDialog(null, "Load " + vsAGVLoad +
                                          " is not an AGV Move anymore!",
                                          "Data Error", JOptionPane.ERROR_MESSAGE);
            return;
          }

          int vnOption;
          String vsAsk;
          switch(vpAGVMoveData.getAGVLoadStatus())
          {
            case DBConstants.AGV_MOVECOMPLETE:
              recoverCompletedMove(vpAGVMoveData, true);
              break;

            case DBConstants.AGV_MOVEERROR:
              vsAsk = "Is Load " + vpAGVMoveData.getLoadID() +
                             " physically at station " + vpAGVMoveData.getDestStation() +
                             " already?";
              vnOption = JOptionPane.showConfirmDialog(null, vsAsk,
                                         "Select One", JOptionPane.YES_NO_OPTION);
              if (vnOption == JOptionPane.YES_OPTION)
              {
                vpDevServ.updateAGVMoveStatus(vpAGVMoveData.getLoadID(),
                                              DBConstants.AGV_MOVECOMPLETE);
                recoverCompletedMove(vpAGVMoveData, false);
              }
              break;

            case DBConstants.AGV_MOVECANCELED:
            case DBConstants.AGV_RECOVERABLE:
              vsAsk = "Reschedule move?";
              vnOption = JOptionPane.showConfirmDialog(null, vsAsk,
                                         "Select One", JOptionPane.YES_NO_OPTION);
              if (vnOption == JOptionPane.YES_OPTION)
              {
                mpDevServ.updateAGVMoveStatus(vpAGVMoveData.getLoadID(), DBConstants.AGV_NOMOVE);
              }
              break;

            default:
              JOptionPane.showMessageDialog(null, "Recovery Operation. " +
                                            "This AGV move is not recoverable.",
                                            "Info. Message",
                                            JOptionPane.INFORMATION_MESSAGE);
          }
        }
        catch(DBException ex)
        {
          JOptionPane.showMessageDialog(null, "Error recovering AGV move. " +
                                        ex.getMessage(), "DB Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    });
  }

  protected void listenRefreshEvents()
  {
    mpChkBoxRefreshTimer.setSelected(true);
    mpChkBoxRefreshTimer.addItemListener(new CheckBoxEvtListener());
  }

  protected void initPanelComponents()
  {
    mpTable = new DacTable(new DacModel(new ArrayList<Map>(), "VehicleView"));

    GridBagConstraints vpGBConst = new GridBagConstraints();
    setLayout(new BorderLayout());
    JPanel vpMovInputPanel = new JPanel(new GridBagLayout());

    vpGBConst.gridwidth = 1;
    vpGBConst.gridx = 0;
    vpGBConst.gridy = 0;
    vpGBConst.anchor = GridBagConstraints.EAST;
    vpGBConst.insets = new Insets(5, 1, 5, 1);
    vpMovInputPanel.add(new SKDCLabel("AGV Routes:"), vpGBConst);
    vpGBConst.gridy = GridBagConstraints.RELATIVE;
    vpMovInputPanel.add(new SKDCLabel("Load ID:"), vpGBConst);

    vpGBConst.gridwidth = 2;
    vpGBConst.gridx = 1;
    vpGBConst.gridy = 0;
    vpGBConst.anchor = GridBagConstraints.WEST;
    vpMovInputPanel.add(mpCmbRoute, vpGBConst);
    vpGBConst.gridy = 1;
    vpMovInputPanel.add(mpTextLoad, vpGBConst);
    vpGBConst.gridx = GridBagConstraints.RELATIVE;
    vpMovInputPanel.add(Box.createHorizontalStrut(30), vpGBConst);
    vpMovInputPanel.add(mpChkBoxRefreshTimer, vpGBConst);
    listenRefreshEvents();
    add(vpMovInputPanel, BorderLayout.NORTH);
    add(mpTable.getScrollPane(), BorderLayout.CENTER);

    try
    {
      mpCmbRoute.setComboBoxData(mpDevServ.getAllAGVPathsForDisplay());
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, "Database Error finding AGV routes!",
              "Database Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  protected void recoverCompletedMove(VehicleMoveData ipMoveData,
                              boolean izPromptResendArrival) throws DBException
  {
    StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);
    LoadData vpLoadData = vpLoadServ.getLoad(ipMoveData.getLoadID());
    if (vpLoadData != null)
    {                              // Load does not exist. Publish message
                                   // if they want.
      if (izPromptResendArrival)
      {
        int vnOption = JOptionPane.showConfirmDialog(null, "Resend Arrival?",
                                   "Select One", JOptionPane.YES_NO_OPTION);
        if (vnOption == JOptionPane.YES_OPTION)
        {
          String vsDestStn = getDestinationStn(ipMoveData);
          sendSchedulerArrival(ipMoveData.getLoadID(), vsDestStn);
        }
      }
      else
      {
        String vsDestStn = getDestinationStn(ipMoveData);
        sendSchedulerArrival(ipMoveData.getLoadID(), vsDestStn);
      }
    }
//    else
//    {
//      JOptionPane.showMessageDialog(null, "Load data already exists for " +
//                                    ipMoveData.getLoadID() +
//              ".  No AGV Move recovery needed. " + SKDCConstants.EOL_CHAR +
//              "You may still need  to recover on the normal Wrx recovery screen.",
//              "Bad Data", JOptionPane.INFORMATION_MESSAGE);
//    }
  }

  protected void sendSchedulerArrival(String isLoadID, String isStation)
          throws DBException
  {
    StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);

    SystemGateway vpJMSGateway = ThreadSystemGateway.get();
    try
    {
      String vsScheduler = vpStnServ.getStationsScheduler(isStation);
      LoadEventDataFormat mpLEDF = Factory.create(LoadEventDataFormat.class, vsScheduler);
      String vsArrivalData = mpLEDF.createAGVLoadArrival(isStation, isLoadID);

      vpJMSGateway.publishLoadEvent(vsArrivalData, 0, vsScheduler);
    }
    catch(DBException exc)
    {
      throw new DBException("Failed to find scheduler name for message publishing!", exc);
    }
  }

  private void addAGVMoveRequest() throws DBException
  {
    VehicleMoveData vpVMData = new VehicleMoveData();
    vpVMData.setRequestID(mpDevServ.generateRequestID());
    String[] vasFromToStns = mpCmbRoute.getText().split("-");
    if (vasFromToStns != null && vasFromToStns.length > 0)
    {
      vpVMData.setCurrentStation(vasFromToStns[0].trim());
      vpVMData.setDestStation(vasFromToStns[1].trim());
    }
    vpVMData.setLoadID(mpTextLoad.getText());
    mpDevServ.addAGVRecord(vpVMData);
                                     // Get the assigned sequence number for
                                     // this record
    int vnRequestSequenceNum = vpVMData.getSequenceNumber();
    String vsMessageName = AGVMessageNameEnum.MOV_REQUEST.getValue();
    ThreadSystemGateway.get().publishHostMesgReceiveEvent(vsMessageName,
                                                          vnRequestSequenceNum,
                                                          DatabaseControllerTypeDefinition.AGV_TYPE);
    List<Map> vpMoves = mpDevServ.getAllAGVMoveRecordsByStatus();
    mpTable.refreshData(vpMoves);
  }

  private void createRefreshTimer()
  {
    mpRefreshTimer = new Timer(REFRESH_TIME, new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        refreshTable();
      }
    });

    mpRefreshTimer.start();
  }

  private class CheckBoxEvtListener implements ItemListener
  {
    @Override
    public void itemStateChanged(ItemEvent e)
    {
      if (e.getStateChange() == ItemEvent.SELECTED)
      {
        openTabOperations();
      }
      else
      {
        closeTabOperations();
      }
    }
  }

  protected String getDestinationStn(VehicleMoveData ipData)
  {
    return((ipData.getDestStation().trim().length() == 0) ? ipData.getCurrentStation() : ipData.getDestStation());
  }
}
