package com.daifukuamerica.wrxj.swingui.equipment;

import com.daifukuamerica.wrxj.controller.status.StatusModel;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRecoveryServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.properties.EquipmentMonitorProperties;
import com.daifukuamerica.wrxj.timer.RestartableTimer;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * <B>Description:</B> Panel for the tracking
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
@SuppressWarnings("serial")
public class TrackingPanel extends JPanel
{
  private static final String INACTIVE_TITLE = "Equipment Load Tracking Data";
  private static final String ACTIVE_TITLE = " Load Tracking Data";

  // Tracking recovery pop-up
  private static final String POPUP_DELETE_DB = "DelDb";
  private static final String POPUP_DELETE_EQ = "DelEq";
  private static final String POPUP_RECOVER = "Rcvr";

  // Timed updates
  private RestartableTimer timers = null;
  private LoadStatusTimeout loadStatusTimeout = new LoadStatusTimeout();
  private DBObject mpDBObject;
  private StandardRecoveryServer mpRcvrServer = null;

  StatusModel mpStatusModel;
  Logger logger;
  EquipmentMonitorFrame mpEMF;
  GroupPanel mpActivePanel;
  String msActiveGroup;
  int mnActivePanelIndex;

  private int mnSelectedRow = -1;
  SKDCPopupMenu mpTrackingPopupMenu;
  LoadStatusScrollPane mpLSSP = null;

  /**
   * Constructor
   *
   * @param ipEMF
   * @param isGroup
   * @param ipProperties
   * @param ipPermissions
   * @param ipSM
   * @param ipLogger
   */
  public TrackingPanel(EquipmentMonitorFrame ipEMF, String isGroup,
      EquipmentMonitorProperties ipProperties,
      SKDCScreenPermissions ipPermissions, StatusModel ipSM, Logger ipLogger)
  {
    // Panel
    super(new GridLayout(1,1));
    setMinimumSize(new Dimension(500, ipProperties.getTrackingPanelHeight()));
    setPreferredSize(new Dimension(ipProperties.getTrackingPanelWidth(),
        ipProperties.getTrackingPanelHeight()));

    mpEMF = ipEMF;
    mpStatusModel = ipSM;
    logger = ipLogger;

    // ScrollPane
    mpLSSP = new LoadStatusScrollPane(ipProperties.useLongLoadIDs());
    JScrollPane vpScrollPane = mpLSSP.initialize(ipEMF);
    vpScrollPane.setSize(100, 120);
    vpScrollPane.setPreferredSize(new Dimension(mpLSSP.getTableWidth(),
        ipProperties.getTrackingPanelHeight() - 33));

    // Pop-up menu
    mpTrackingPopupMenu = defineLoadStatusViewPopup(
        ipProperties.allowWRxRecovery(), ipPermissions);
    JTable vpTable = mpLSSP.getTable();
    vpTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    vpTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          showPopUpMenu(e); // if this is a popupTrigger
        }
        @Override
        public void mouseClicked(MouseEvent e)
        {
          showPopUpMenu(e); // if this is a popupTrigger
        }
        @Override
        public void mouseReleased(MouseEvent e)
        {
          showPopUpMenu(e); // if this is a popupTrigger
        }
      });

    // Timer
    timers = new RestartableTimer("timers-" + isGroup);

    // Start deactivated
    deactivate();

    add(vpScrollPane);
  }

  /**
   * Return the ScrollPane
   *
   * @return
   */
  public LoadStatusScrollPane getLoadStatusScrollPane()
  {
    return mpLSSP;
  }

  /**
   * Listener for tracking pop-up
   */
  private class LoadStatusViewPopupMenuListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      /*
       * Get the selected data
       */
      String vsLoadID = (String) mpLSSP.getValueAt(mnSelectedRow,
          LoadStatusScrollPane.MCKEY_COLUMN);
      String vsBarCode = (String) mpLSSP.getValueAt(mnSelectedRow,
          LoadStatusScrollPane.BAR_CODE_COLUMN);
      String vsFromStation = (String) mpLSSP.getValueAt(mnSelectedRow,
          LoadStatusScrollPane.FROM_COLUMN);
      StringTokenizer vpST = new StringTokenizer(vsFromStation);
      if (vpST.hasMoreTokens())
      {
        vsFromStation = vpST.nextToken();
      }
      Integer vnHeight = Integer.valueOf((String)mpLSSP.getValueAt(mnSelectedRow,
        LoadStatusScrollPane.LOAD_SIZE_COLUMN));
      String vsMachineID = (String) mpLSSP.getValueAt(mnSelectedRow,
          LoadStatusScrollPane.MACHINEID_COLUMN);
      String vsEquipStatus = (String) mpLSSP.getValueAt(mnSelectedRow,
          LoadStatusScrollPane.SRC_STATUS_COLUMN);

      /*
       * Do the action
       */
      String vsAction = e.getActionCommand();
      if (vsAction.equals(POPUP_RECOVER))
      {
        mpEMF.recoverDBLoad(vsLoadID, vsBarCode, vsFromStation, vnHeight);
      }
      else if (vsAction.equals(POPUP_DELETE_DB))
      {
        mpEMF.deleteDBLoad(vsLoadID);
      }
      else if (vsAction.equals(POPUP_DELETE_EQ))
      {
        mpEMF.deleteTracking(vsLoadID, vsMachineID, vsEquipStatus);
      }
    }
  }

  /**
   * Build the tracking pop-up
   *
   * @param izAllowWrxRecovery
   * @param ipPermissions
   * @return
   */
  private SKDCPopupMenu defineLoadStatusViewPopup(boolean izAllowWrxRecovery,
      SKDCScreenPermissions ipPermissions)
  {
    SKDCPopupMenu vpPopupMenu = new SKDCPopupMenu();
    ActionListener vpActionListener = new LoadStatusViewPopupMenuListener();

    vpPopupMenu.add("Delete from SRC", POPUP_DELETE_EQ, vpActionListener);
    vpPopupMenu.setAuthorization("Delete From SRC",
        ipPermissions.iDeleteAllowed);

    if (izAllowWrxRecovery)
    {
      vpPopupMenu.addSeparator();

      vpPopupMenu.add("Recover In Warehouse Rx", POPUP_RECOVER,
          vpActionListener);
      vpPopupMenu.add("Delete From Warehouse Rx", POPUP_DELETE_DB,
          vpActionListener);

      vpPopupMenu.setAuthorization("Recover In Warehouse Rx",
          ipPermissions.iModifyAllowed);
      vpPopupMenu.setAuthorization("Delete From Warehouse Rx",
          ipPermissions.iDeleteAllowed);
    }

    return vpPopupMenu;
  }

  /**
   * Show the pop-up menu
   *
   * @param e
   */
  private void showPopUpMenu(MouseEvent e)
  {
    if (e.isPopupTrigger())
    {
      Point origin = e.getPoint();
      mnSelectedRow = ((JTable)e.getSource()).rowAtPoint(origin);
      mpTrackingPopupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  /**
   * Activate this tracking panel
   *
   * @param isGroupName
   * @param ipGroupPanel
   * @param inPanelIndex
   */
  public void activate(String isGroupName, JComponent ipGroupPanel,
      int inPanelIndex)
  {
    // Remember our linked panel
    msActiveGroup = isGroupName;
    mpActivePanel = (GroupPanel)ipGroupPanel;
    mnActivePanelIndex = inPanelIndex;

    TitledBorder vpActiveBord = BorderFactory.createTitledBorder
        (
          BorderFactory.createEtchedBorder(EquipmentGraphic.DAIFUKU_PURPLE,
                                           EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE)
        );
    // Update the borders
    vpActiveBord.setTitle(getName());

    vpActiveBord.setTitle(isGroupName + ACTIVE_TITLE);
    setBorder(vpActiveBord);
    mpActivePanel.activate(this);

    // Start the update
    mpLSSP.setActiveGroupName(msActiveGroup);
    timers.setPeriodicTimerEvent(loadStatusTimeout, 100);
  }

  /**
   * Deactivate this tracking panel
   */
  public void deactivate()
  {
    TitledBorder vpInactiveBord = BorderFactory.createTitledBorder
      (
      BorderFactory.createEtchedBorder(EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE,
                                       EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE)
      );

    // Update the borders
    vpInactiveBord.setTitle(INACTIVE_TITLE);
    setBorder(vpInactiveBord);
    if (mpActivePanel != null)
    {
      mpActivePanel.deactivate();
    }

    // Clear the scroll pane
    mpLSSP.setCurrentDBLoads(new HashMap());
    mpLSSP.clearTable();

    // Stop the update
    mpLSSP.setActiveGroupName(LoadStatusScrollPane.NONE_ACTIVE);
    timers.cancel(loadStatusTimeout);
  }

  /**
   * Shut down this tracking panel
   */
  public void shutdown()
  {
    deactivate();
    loadStatusTimeout = null;
    if (timers != null)
    {
      timers.cancel();
      timers = null;
    }
    mpLSSP.cleanUpOnClose();
  }

  /**
   * Task for getting tracking updates
   */
  private class LoadStatusTimeout extends RestartableTimerTask
  {
    @Override
    public void run()
    {
      loadStatusTimeoutRun(mnActivePanelIndex);
      timers.setPeriodicTimerEvent(loadStatusTimeout, 3500);
    }
  }

  /**
   * Update tracking data
   *
   * @param inPanelIndex
   */
  protected void loadStatusTimeoutRun(int inPanelIndex)
  {
    synchronized(mpLSSP)
    {
      ensureDBConnection();
      updateActiveDevicesLoads(inPanelIndex);
    }
  }

  /**
   * Method simply ensures database connectivity.
   */
  protected void ensureDBConnection()
  {
    if (mpDBObject == null || !mpDBObject.checkConnected())
    {
      mpDBObject = new DBObjectTL().getDBObject();
      try { mpDBObject.connect(); }
      catch(DBException e) { return; }
    }
  }

  /**
   * Load Warehouse Rx tracking data
   *
   * @param inPanelIndex
   * @return
   */
  protected void updateActiveDevicesLoads(int inPanelIndex)
  {
    SimpleDateFormat vpSDF = new SimpleDateFormat(
        SKDCConstants.STATUS_DATE_FORMAT);

    String vsCraneName = msActiveGroup + ":Crane";
    String vsDeviceID = mpStatusModel.get(vsCraneName, StatusModel.DEVICE_ID);
    String vsMOSController = mpStatusModel.get(vsCraneName,
        StatusModel.MOS_CONTROLLER);
    List<Map> mpLoadsForDevice = null;
    Map<String, Map> newDBLoads = new HashMap<String, Map>();
    try
    {
      if (mpRcvrServer == null)
      {
        mpRcvrServer = Factory.create(StandardRecoveryServer.class);
      }
      mpLoadsForDevice = mpRcvrServer.getDevicesRecoveryLoadDataList(vsDeviceID);
    }
    catch (Exception eDB)
    {
      logger.logException(eDB, "Reading loads for device " + vsDeviceID);
    }
    if ((mpLoadsForDevice != null) && (!mpLoadsForDevice.isEmpty()))
    {
      LoadData vpLD = Factory.create(LoadData.class);
      for (Map m : mpLoadsForDevice)
      {
        vpLD.clear();
        vpLD.dataToSKDCData(m);

        String vsLoadID   = vpLD.getLoadID();
        String vsMCKey    = vpLD.getMCKey();
        String vsMoveTime = vpSDF.format(vpLD.getMoveDate());
        String vsLoadSize = "" + vpLD.getHeight();
        String vsFromLoc  = vpLD.getAddress();
        String vsNextLoc  = vpLD.getNextAddress();
        String vsBCRData  = vpLD.getBCRData();
        String vsFromStn  = getStation(vsFromLoc, inPanelIndex, vsMOSController);
        String vsNextStn  = getStation(vsNextLoc, inPanelIndex, vsMOSController);

        int iStatus = vpLD.getLoadMoveStatus();
        String vsStatus = "";
        try
        {
          vsStatus = DBTrans.getStringValue(LoadData.LOADMOVESTATUS_NAME,
              iStatus);
        }
        catch (Exception eTrans)
        {
          logger.logError("DBTrans CANNOT find " + LoadData.LOADMOVESTATUS_NAME
              + "=" + iStatus);
        }
        if (vsStatus == null)
        {
          vsStatus = "" + iStatus;
        }

        Map<String, String> loadInfo = new HashMap<String, String>();
        loadInfo.put(LoadStatusScrollPane.DB_MCKEY,   vsMCKey);
        loadInfo.put(LoadStatusScrollPane.DB_DEVICE,  msActiveGroup);
        loadInfo.put(LoadStatusScrollPane.DB_TIME,    vsMoveTime);
        loadInfo.put(LoadStatusScrollPane.DB_SIZE,    vsLoadSize);
        loadInfo.put(LoadStatusScrollPane.DB_BCRDATA, vsBCRData);
        loadInfo.put(LoadStatusScrollPane.DB_FROM,    vsFromStn);
        loadInfo.put(LoadStatusScrollPane.DB_TO,      vsNextStn);
        loadInfo.put(LoadStatusScrollPane.DB_STATUS,  vsStatus);
        newDBLoads.put(vsLoadID, loadInfo);
      }
    }
    mpLSSP.setCurrentDBLoads(newDBLoads);
  }

  /**
   * Get the station/description for a given location string
   *
   * @param isLocation
   * @param inPanelIndex
   * @param isMOSController
   * @return
   */
  private String getStation(String isLocation, int inPanelIndex,
      String isMOSController)
  {
    String vsStation = mpStatusModel.get(
        isMOSController + ":" + isLocation, StatusModel.DESCRIPTION);
    if (vsStation.equals(StatusModel.UNKNOWN))
    {
      try
      {
        vsStation = isLocation.substring(0,1) + "-" +
                    isLocation.substring(1,3) + "-" +
                    isLocation.substring(3,6) + "-" +
                    isLocation.substring(6,9) + "  (Bin)";
      }
      catch (Exception e)
      {
        vsStation = isLocation;
      }
    }
    else
    {
      vsStation =  isLocation + "  (" + vsStation + ")";
    }
    return vsStation;
  }

  /**
   * Update the equipment tracking data
   *
   * @param o
   * @param arg
   */
  public void update(Observable o, Object arg)
  {
    if (mpActivePanel != null)
    {
      mpLSSP.update(o, arg);
      mpActivePanel.setTrackingData(mpLSSP.getAllTrackingData());
    }
  }

  /**
   * Set the graphic name filter
   *
   * @param isFilter
   */
  public void setFilter(String isFilter)
  {
    if (isFilter.length() == 0)
    {
      setBorder(new TitledBorder(new LineBorder(Color.black, 2), msActiveGroup
          + ACTIVE_TITLE));
    }
    else
    {
      setBorder(new TitledBorder(new LineBorder(Color.black, 2), isFilter
          + ACTIVE_TITLE));
    }

    mpLSSP.setGraphicFilter(isFilter);
  }

  public void resizeScrollPane(Dimension ipSize)
  {
    setPreferredSize(ipSize);
    setBounds(getBounds().x, getBounds().y, ipSize.width, ipSize.height);
  }
}
