package com.daifukuamerica.wrxj.swingui.emulation;

import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.emulation.station.OrderSimulator;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class OrderSimulation extends JPanel
{
  private static final long serialVersionUID = 0L;
  
  private static final String MOVE_LEFT = "MOVELEFT";
  private static final String MOVE_RIGHT = "MOVERIGHT";
  private static final String MOVE_ALL_LEFT = "MOVEALLLEFT";
  private static final String MOVE_ALL_RIGHT = "MOVEALLRIGHT";

//  private SKDCButton mpUpdateButton;
  private SKDCIntegerField mpSimTime = new SKDCIntegerField(7);
  private SKDCComboBox mpFullQtyBox = new SKDCComboBox();
  private SKDCComboBox mpUseItemLotBox = new SKDCComboBox();
  private SKDCComboBox mpAllowMultBox = new SKDCComboBox();
  
  protected DefaultListModel mpOffModel = new DefaultListModel();
  protected DefaultListModel mpOnModel = new DefaultListModel();
  private JList mpOffList = new JList(mpOffModel);
  private JList mpOnList = new JList(mpOnModel);
  JScrollPane mpOffPane = new JScrollPane(mpOffList);
  JScrollPane mpOnPane = new JScrollPane(mpOnList);
  
  private JPanel mpStnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
  private JPanel mpOnPanel = new JPanel();
  private JPanel mpOffPanel = new JPanel();
  private JPanel mpControlPanel = new JPanel(new GridBagLayout());
  private SKDCButton mpBtnLeft = new SKDCButton("<", "Simulate orders for this route");
  private SKDCButton mpBtnRight = new SKDCButton(">", "Don't simulate orders for this route");
  private SKDCButton mpAllBtnLeft = new SKDCButton("<<", "Simulate orders for all routes");
  private SKDCButton mpAllBtnRight = new SKDCButton(">>", "Don't simulate orders for any routes");
  
  public OrderSimulation()
  {
    jbinit();
  }
  
  /**
   * Create and intialize all the Swing components
   */
  private void jbinit()
  {
    GridBagConstraints vpGBC;
    JPanel mpInputPanel = new JPanel();

    this.setLayout(new BorderLayout());
    TitledBorder vpTitledBorder = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Order Simulation Details");

//    mpUpdateButton = new SKDCButton();
//    mpUpdateButton.setText("Update");
//    mpUpdateButton.addActionListener(new ActionListener()
//    {
//      public void actionPerformed(ActionEvent e)
//      {
//        updateButtonPressed();
//      }
//    });

    mpInputPanel.setLayout(new GridBagLayout());
    mpInputPanel.setBorder(vpTitledBorder);
    vpGBC = new GridBagConstraints();
    vpGBC.insets = new Insets(4, 4, 4, 4);

    vpGBC.gridx = 0;
    vpGBC.gridy = 0;
    vpGBC.gridwidth = 1;
    vpGBC.weightx = 0.2;
    vpGBC.weighty = 0.8;
    
    vpGBC.anchor = GridBagConstraints.EAST;
    mpInputPanel.add(new SKDCLabel("Use station item/lot:"), vpGBC); 
    vpGBC.gridx++;
    vpGBC.anchor = GridBagConstraints.WEST;
    mpInputPanel.add(mpUseItemLotBox, vpGBC);
    vpGBC.gridx++;
    vpGBC.anchor = GridBagConstraints.EAST;
    mpInputPanel.add(new SKDCLabel("Multiple Order Lines:"), vpGBC);
    vpGBC.gridx++;
    vpGBC.anchor = GridBagConstraints.WEST;
    mpInputPanel.add(mpAllowMultBox, vpGBC);
    
    vpGBC.gridx = 0;
    vpGBC.gridy = 1;
    vpGBC.anchor = GridBagConstraints.EAST;
    mpInputPanel.add(new SKDCLabel("Order Full Quantity:"), vpGBC);
    vpGBC.gridx++;
    vpGBC.anchor = GridBagConstraints.WEST;
    mpInputPanel.add(mpFullQtyBox, vpGBC);
    vpGBC.gridx++;
    vpGBC.anchor = GridBagConstraints.EAST;
    mpInputPanel.add(new SKDCLabel("Order Interval:"), vpGBC);
    vpGBC.gridx++;
    vpGBC.anchor = GridBagConstraints.WEST;
    mpInputPanel.add(mpSimTime, vpGBC);
    
    vpGBC.gridx = 0;
    vpGBC.gridy = 2;
    vpGBC.gridwidth = 4;
    vpGBC.anchor = GridBagConstraints.SOUTH;
//    mpInputPanel.add(mpUpdateButton, vpGBC);
    
    this.add(mpInputPanel,  BorderLayout.NORTH);
    
    mpOffList.setVisibleRowCount(9);
    mpOffPane.setPreferredSize(new Dimension(100, 148));
    mpOnList.setVisibleRowCount(9);
    mpOnPane.setPreferredSize(new Dimension(100, 148));
    
    GridBagConstraints gbconst = new GridBagConstraints();
    gbconst.insets = new Insets(1, 1, 10, 1);
    
    gbconst.gridx = 0;
    gbconst.gridy = 0;
    gbconst.anchor = GridBagConstraints.EAST;
    gbconst.gridwidth = 1;
    gbconst.gridheight = 1;
    gbconst.weightx = 0.2;
    gbconst.weighty = 0;
    gbconst.ipadx = 100;
    gbconst.ipady = 40;
    gbconst.fill = GridBagConstraints.VERTICAL;
    
    mpOffPanel.add(new SKDCLabel("Order Simulation OFF"));
    mpOnPanel.add(new SKDCLabel("Order Simulation ON"));
    
    mpOffPanel.add(mpOffPane);
    mpOnPanel.add(mpOnPane);
    mpOffPanel.setPreferredSize(new Dimension(110,172));
    mpOnPanel.setPreferredSize(new Dimension(110,172));
    mpControlPanel.setPreferredSize(new Dimension(50,150));
    GridBagConstraints vpGBConst = new GridBagConstraints();
    vpGBConst.insets = new Insets(1, 1, 10, 1);
    vpGBConst.gridwidth = GridBagConstraints.REMAINDER;
    vpGBConst.anchor = GridBagConstraints.CENTER;
    vpGBConst.gridy = GridBagConstraints.RELATIVE;
    
    mpControlPanel.add(mpAllBtnLeft, vpGBConst);
    mpControlPanel.add(mpBtnLeft, vpGBConst);
    mpControlPanel.add(mpBtnRight, vpGBConst);
    mpControlPanel.add(mpAllBtnRight, vpGBConst);
    mpStnPanel.add(mpOnPanel, BorderLayout.WEST);
    mpStnPanel.add(mpControlPanel, BorderLayout.CENTER);
    mpStnPanel.add(mpOffPanel, BorderLayout.EAST);
    this.add(mpStnPanel, BorderLayout.CENTER);
    
    setButtonListeners();
    initScreen();
  }

//  private void updateButtonPressed()
//  {
//    boolean vzFull = (mpFullQtyBox.getSelectedItem().equals("Yes"));
//    boolean vzItemLot = (mpUseItemLotBox.getSelectedItem().equals("Yes"));
//    boolean vzMult = (mpAllowMultBox.getSelectedItem().equals("Yes"));
//    int vnTime = mpSimTime.getValue();
//    OrderSimulator.updateValues(mpSimTime.getValue(), vzFull, vzItemLot, vzMult);
//  }
  
  /**
   * Refresh the data in all tables and fields.
   */
  private void initScreen()
  {
    String[] vasYesNo = {"Yes", "No"};
    mpSimTime.setValue(0);
    
    mpAllowMultBox.setComboBoxData(vasYesNo);
    mpAllowMultBox.setSelectedItem("No");

    mpUseItemLotBox.setComboBoxData(vasYesNo);
    mpUseItemLotBox.setSelectedItem("No");
    
    mpFullQtyBox.setComboBoxData(vasYesNo);
    mpFullQtyBox.setSelectedItem("Yes");

    setItemLotBoxListener();
    try
    {     
      stationFill();
    }
    catch(DBException ex)
    {
      JOptionPane.showMessageDialog(this, ex.getMessage(),
          "Unable to get Stations",
          JOptionPane.ERROR_MESSAGE);
    }
  }
  
  /**
   * Fill the on and off lists with stations.  
   * @param iasStns Data to place in station lists
   */
  protected void stationFill() throws DBException
  {
    StandardRouteServer vpRouteServ = Factory.create(StandardRouteServer.class);
    List<String> vpRoutes = vpRouteServ.getRouteNameList("", DBConstants.EQUIPMENT, DBConstants.STATION);
    for(String vsRoute : OrderSimulator.getRoutes())
    {
      mpOnModel.addElement(vsRoute);
      vpRoutes.remove(vsRoute);
    }
    for (String vsRoute : vpRoutes)
    {
      mpOffModel.addElement(vsRoute);
    }
  }
  
  private void moveRight()
  {
    Object[] vapStns = mpOnList.getSelectedValues();
    for(int i=0; i<vapStns.length; i++)
    {
      mpOnModel.removeElement(vapStns[i]);
      mpOffModel.addElement(vapStns[i]);
      OrderSimulator.stopOrders((String)vapStns[i]);
    }
  }
  
  private void moveLeft()
  {
    boolean vzFull = (mpFullQtyBox.getSelectedItem().equals("Yes"));
    boolean vzItemLot = (mpUseItemLotBox.getSelectedItem().equals("Yes"));
    boolean vzMult = (mpAllowMultBox.getSelectedItem().equals("Yes"));
    int vnTime = mpSimTime.getValue();
    Object[] vapStns = mpOffList.getSelectedValues();
    if (vnTime < 1)
    {
      JOptionPane.showMessageDialog(this,
          "Interval must be positive.", "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    for(int i=0; i<vapStns.length; i++)
    {
      mpOffModel.removeElement(vapStns[i]);
      mpOnModel.addElement(vapStns[i]);
      OrderSimulator.updateValues((String)vapStns[i], vnTime, vzFull, vzItemLot, vzMult);
    }
  }
  
  private void moveAllRight()
  {
    for(int i=0; i<mpOnModel.size(); i++)
    {
      mpOffModel.addElement(mpOnModel.elementAt(i));
      OrderSimulator.stopOrders((String)mpOnModel.elementAt(i));
    }
    mpOnModel.removeAllElements();
    
  }
  
  private void moveAllLeft()
  {
    for(int i=0; i<mpOffModel.size(); i++)
    {
      mpOnModel.addElement(mpOffModel.elementAt(i));
      boolean vzFull = (mpFullQtyBox.getSelectedItem().equals("Yes"));
      boolean vzItemLot = (mpUseItemLotBox.getSelectedItem().equals("Yes"));
      boolean vzMult = (mpAllowMultBox.getSelectedItem().equals("Yes"));
      int vnTime = mpSimTime.getValue();
      if (vnTime < 1)
      {
        JOptionPane.showMessageDialog(this,
            "Interval must be positive.", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
      OrderSimulator.updateValues((String)mpOffModel.elementAt(i), vnTime, vzFull, vzItemLot, vzMult);
    }
    mpOffModel.removeAllElements();
    
  }
  
  /*===========================================================================
   ****** All Listener methods go here ******
  ===========================================================================*/
  /**
  *  Defines all buttons on the add view dialog screen.
  */
  private void setButtonListeners()
  {
    ActionListener mpEvtListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        String vsButton = e.getActionCommand();
      if (vsButton.equals(MOVE_LEFT))
      {
        moveLeft();
      }
      else if (vsButton.equals(MOVE_RIGHT))
      {
        moveRight();
      }
      else if (vsButton.equals(MOVE_ALL_LEFT))
      {
        moveAllLeft();
      }
      else if (vsButton.equals(MOVE_ALL_RIGHT))
      {
        moveAllRight();
      }
    }
  };
                              // Attach listeners.
    mpBtnLeft.addEvent(MOVE_LEFT, mpEvtListener);
    mpBtnRight.addEvent(MOVE_RIGHT, mpEvtListener);
    mpAllBtnLeft.addEvent(MOVE_ALL_LEFT, mpEvtListener);
    mpAllBtnRight.addEvent(MOVE_ALL_RIGHT, mpEvtListener);
  }
  
  /**
   * Listener for use item/lot combo box
   *
   */
  private void setItemLotBoxListener()
  {
    mpUseItemLotBox.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          boolean vzEnable = e.getItem().equals("No");
          mpAllowMultBox.setEnabled(vzEnable);
          mpFullQtyBox.setEnabled(vzEnable);
        }
      }
    });    
  }
}
