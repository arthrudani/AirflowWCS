package com.daifukuamerica.wrxj.swingui.emulation;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
* Swing user interface for simulation of various stations.
*
* @author  Kip Armstrong
* @version 1.0
*/
public class StationSimulation extends SKDCInternalFrame
{
  private static final long serialVersionUID = 0L;
  
//  private Map<String,String> mpStationsMap = null;
  
  JPanel mpSimPanel = new JPanel();
  protected JTabbedPane mpTabPane = new JTabbedPane();
  StandardStationServer mpStationServ = null;
//  SKDCComboBox mpStnComboBox = new SKDCComboBox();
  SKDCIntegerField mpSimTime = new SKDCIntegerField(7);
  SKDCTranComboBox mpAutoReceiveType;
  SKDCTranComboBox mpSimulateComboBox;
  SKDCTranComboBox mpModeComboBox;
  SKDCButton mpUpdateButton;
  
  DefaultListModel mpOffModel = new DefaultListModel();
  DefaultListModel mpOnModel = new DefaultListModel();
  JList mpOffList = new JList(mpOffModel);
  JList mpOnList = new JList(mpOnModel);
  JScrollPane mpOffPane = new JScrollPane(mpOffList);
  JScrollPane mpOnPane = new JScrollPane(mpOnList);
  
  public StationSimulation()
  {
    try
    {
      mpAutoReceiveType = new SKDCTranComboBox(StationData.AUTOLOADMOVEMENTTYPE_NAME);
      mpSimulateComboBox = new SKDCTranComboBox(StationData.SIMULATE_NAME);
      mpModeComboBox = new SKDCTranComboBox(StationData.BIDIRECTIONALSTATUS_NAME);
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Build the screen
   * 
   * @throws Exception
   */
  protected void jbInit() throws Exception
  {
    GridBagConstraints vpGBC = new GridBagConstraints();
    JPanel vpDataPanel = getEmptyInputPanel("Station Simulation Details");
    JPanel vpListPanel = getEmptyInputPanel("");
    JPanel vpButtonPanel = getEmptyButtonPanel();

    mpTabPane = new JTabbedPane();
    mpTabPane.add(mpSimPanel, "Station Simulation");
    mpTabPane.add(Factory.create(OrderSimulation.class), "Order Simulation");
    
    this.setTitle("Station Simulation");
    this.getContentPane().add(mpTabPane, BorderLayout.CENTER);

    /*
     * Build the data panel label column
     */
    setLabelColumnGridBagConstraints(vpGBC);

//    vpDataPanel.add(new SKDCLabel("Station:"), vpGBC);
    vpDataPanel.add(new SKDCLabel("Sim Time:"), vpGBC);
    vpDataPanel.add(new SKDCLabel("Auto Work:"), vpGBC);
    vpDataPanel.add(new SKDCLabel("Simulation:"), vpGBC);
    vpDataPanel.add(new SKDCLabel("Mode:"), vpGBC);

    /*
     * Build the data panel input column
     */
    setInputColumnGridBagConstraints(vpGBC);

    mpStationServ = Factory.create(StandardStationServer.class);
//    stationFill();
    setStationListListeners();
//    if (mpStnComboBox.getItemCount() > 0)
//    {
//      String stationName = mpStationsMap.get(mpStnComboBox.getItemAt(0));
//      processStationSelectEvent(stationName);
//    }
//    
//    vpDataPanel.add(mpStnComboBox, vpGBC);
    vpDataPanel.add(mpSimTime, vpGBC);
    vpDataPanel.add(mpAutoReceiveType, vpGBC);
    vpDataPanel.add(mpSimulateComboBox, vpGBC);
    vpDataPanel.add(mpModeComboBox, vpGBC);

    /*
     * Build the List panel
     */
    mpOffList.setVisibleRowCount(9);
    mpOffPane.setPreferredSize(new Dimension(100, 148));
    mpOnList.setVisibleRowCount(9);
    mpOnPane.setPreferredSize(new Dimension(100, 148));
    refreshStationLists(); 
    vpListPanel.add(new SKDCLabel("ON "));
    vpListPanel.add(mpOnPane);
    vpListPanel.add(mpOffPane);
    vpListPanel.add(new SKDCLabel(" OFF"));
    
    /*
     * Build the button panel
     */
    mpUpdateButton = new SKDCButton("Update");
    mpUpdateButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        updateButton_actionPerformed();
      }
    });
    vpButtonPanel.add(mpUpdateButton, null);

    /*
     * Add the panels
     */
    mpSimPanel.setLayout(new BorderLayout());
    mpSimPanel.add(vpDataPanel, BorderLayout.NORTH);
    mpSimPanel.add(vpButtonPanel, BorderLayout.CENTER);
    mpSimPanel.add(vpListPanel, BorderLayout.SOUTH);
    mpTabPane.setSelectedComponent(mpSimPanel);
  }
  
//  void stationFill()
//  {
//    try
//    {
//      int[] vanStations = DBTrans.getIntegerList("iStationType");
//      mpStationsMap = mpStationServ.getStationsByStationType(vanStations);
//      Object[] stationsArray = mpStationsMap.keySet().toArray();
//      mpStnComboBox.setComboBoxData(stationsArray);
//    }
//    catch (DBException e)
//    {
//      displayError(e.getMessage(), "Unable to get Stations");
//    }
//  }
  
  @Override
  public void cleanUpOnClose()
  {
    mpStationServ.cleanUp();
    mpStationServ = null;
  }
  
  void updateButton_actionPerformed()
  {
//    String vsStation = mpStationsMap.get(mpStnComboBox.getSelectedItem());
    Object[] vasStns = mpOnList.getSelectedValues();
    if (vasStns.length < 1)
      vasStns = mpOffList.getSelectedValues();
    if (vasStns.length < 1)
      return;
    
    String vsAutoRec = (String)mpAutoReceiveType.getSelectedItem();
    String vsSimulate = (String)mpSimulateComboBox.getSelectedItem();
    String vsMode = (String)mpModeComboBox.getSelectedItem();
    int vnTime = mpSimTime.getValue();
    try
    {
      int vnUpdateCount = 0;
      for (Object vpObj : vasStns)
      {
        String vsStation = (String)vpObj;
        StationData vpSD = Factory.create(StationData.class);
        vpSD.setStationName(vsStation);
        vpSD.setAutoLoadMovementType(DBTrans.getIntegerValue(StationData.AUTOLOADMOVEMENTTYPE_NAME, vsAutoRec));
        vpSD.setSimulate(DBTrans.getIntegerValue(StationData.SIMULATE_NAME, vsSimulate));
        vpSD.setSimInterval(vnTime*1000);
        vpSD.setBidirectionalStatus(DBTrans.getIntegerValue(StationData.BIDIRECTIONALSTATUS_NAME, vsMode));
        mpStationServ.modifyStationRecord(vpSD);
        
        // update simulation controllers to reflect change
        vpSD = mpStationServ.getStation(vsStation);
        mpStationServ.alertSimulatorOfStationUpdate(vpSD);
        vnUpdateCount++;
      }
      refreshStationLists();
      displayInfoAutoTimeOut(vnUpdateCount + " stations updated.");
    }
    catch(Exception ex)
    {
      displayError(ex.getMessage(), "Error updating stations.");
    }
  }

  private void setStationListListeners()
  {
    mpOffList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        if (mpOffList.getSelectedValue() != null)
        {
          // TODO: these are circular!!!
          processStationSelectEvent((String)mpOffList.getSelectedValue());
          mpOnList.clearSelection();
        }
      }
    });
    
    mpOnList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        if (mpOnList.getSelectedValue() != null)
        {
          processStationSelectEvent((String)mpOnList.getSelectedValue());
          mpOffList.clearSelection();
        }
      }
    });
  }
  
  /**
   *  Method builds the appropriate display for given Station selection.
   *
   *  @param isStationName <code>String</code> containing station name.
   */
   private void processStationSelectEvent(String isStationName)
   {
     StationData mpSD = Factory.create(StationData.class);
     mpSD.setKey(StationData.STATIONNAME_NAME, isStationName);
     mpSD = mpStationServ.getStation(isStationName);
     if (mpSD == null)
     {
       displayInfoAutoTimeOut("No data found for station " + isStationName, "Search Result");
     }
     else
     {
       refreshStationInfo(mpSD);
     }
   }
   
   /**
    * Redisplay station info for selected station represented by <code>
    * ipSD</code>
    * 
    * @param ipSD <code>StationData</code> containing station information.
    */
   private void refreshStationInfo(StationData ipSD)
   {
     try
     {
       mpSimTime.setValue(ipSD.getSimInterval()/1000);
       mpSimulateComboBox.setSelectedElement(ipSD.getSimulate());
       mpAutoReceiveType.setSelectedElement(ipSD.getAutoLoadMovementType());
       int vnType = ipSD.getStationType();
       boolean vzEnable = (vnType == DBConstants.PDSTAND || vnType == DBConstants.REVERSIBLE);
       mpModeComboBox.setEnabled(vzEnable);
       mpModeComboBox.setSelectedElement(ipSD.getBidirectionalStatus());
     }
     catch(NoSuchFieldException ex)
     {
       displayError(ex.getMessage(), "Error loading data.");
     }
   }
   
   /**
    * Redisplay the lists of which stations have simulation on and which are off.
    *
    */
   private void refreshStationLists()
   {
     try
     {
       mpOnModel.clear();
       mpOffModel.clear();
       for (String vsStn : mpStationServ.getStationNameList())
       {
         mpOffModel.addElement(vsStn);
       }
       
       List<StationData> vpSimStns = mpStationServ.getSimStationsForDevice("");
       for (StationData vpSD : vpSimStns)
       {
         String vsStn = vpSD.getStationName();
         mpOnModel.addElement(vsStn);
         mpOffModel.removeElement(vsStn);
       }
     }
     catch (DBException ex)
     {
       displayError(ex.getMessage(), "Error refreshing data.");
     }
   }
}
