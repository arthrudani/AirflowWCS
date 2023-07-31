package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.Timer;
import javax.swing.event.InternalFrameEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

/**
 * <B>Description:</B> Simple screen to check the JBoss message queue size
 * 
 * <P>Copyright (c) 2010 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class JBossMonitorFrame extends DacInputFrame implements ActionListener
{
  public static final String JBOSS_DB = "JBossDB";
  public static final String JBOSS_QUERY_DEST = "SELECT DESTINATION, COUNT(*) AS \"MESSAGE_COUNT\" FROM JMS_MESSAGES GROUP BY DESTINATION";
  public static final String DESTINATION_NAME = "DESTINATION";
  public static final String RESULT_NAME = "MESSAGE_COUNT";
  
  protected static final int POLL_INTERVAL = 1000;
  private static final int RESULTS_TO_DISPLAY = 600;
  private static final String TOTAL_SERIES_NAME = "Total Message Count";
    
  protected DBObject mpJBossDBObj;
  
  protected SKDCTextField mpTxtDriver;
  protected SKDCTextField mpTxtURL;
  protected SKDCTextField mpTxtUser;
  protected JPasswordField mpTxtPassword;
  private JCheckBox mpCBShowDest;
  private ChartPanel mpHistoryGraph;
  private DefaultCategoryDataset mpChartData;
  private Integer mpPollCount = 0;

  protected Timer mpTimer; 
  
  /**
   * Constructor
   */
  public JBossMonitorFrame()
  {
    super("JBoss Monitor", "JBoss connection information");
    buildScreen();
  }
  
  /*========================================================================*/
  /* PRIVATE METHODS                                                        */
  /*========================================================================*/
  
  /**
   * Build the screen
   */
  private void buildScreen()
  {
    mpTxtDriver = new SKDCTextField(30);
    mpTxtDriver.setMaxColumns(150);
    mpTxtURL = new SKDCTextField(30);
    mpTxtURL.setMaxColumns(150);
    mpTxtUser = new SKDCTextField(30);
    mpTxtPassword = new JPasswordField(30);
    mpCBShowDest = new JCheckBox();

    mpChartData = new DefaultCategoryDataset();
    JFreeChart vpChart = ChartFactory.createLineChart("Queued JBoss Messages",
        null, null, mpChartData, PlotOrientation.VERTICAL, true, true,
        false);
    vpChart.getPlot().setBackgroundPaint(Color.WHITE);
    ((CategoryPlot)vpChart.getPlot()).setRangeGridlinePaint(Color.LIGHT_GRAY);
    ((CategoryPlot)vpChart.getPlot()).setAxisOffset(new RectangleInsets(0,0,0,0));
    ((CategoryPlot)vpChart.getPlot()).setInsets(new RectangleInsets(2,2,8,8));
    CategoryAxis vpCA = new CategoryAxis();
    vpCA.setLowerMargin(0.0);
    vpCA.setUpperMargin(0.0);
    vpCA.setCategoryMargin(0.0);
    vpCA.setVisible(false);
    ((CategoryPlot)vpChart.getPlot()).setDomainAxis(vpCA);
    vpChart.setAntiAlias(true);
    vpChart.setBackgroundPaint(new JPanel().getBackground());
    clearChartData();

    mpHistoryGraph = new ChartPanel(vpChart);
    mpHistoryGraph.setPreferredSize(new Dimension(600,250));
    mpHistoryGraph.setBorder(BorderFactory.createEtchedBorder());

    addInput("DB Driver", mpTxtDriver);
    addInput("DB URL", mpTxtURL);
    addInput("DB User Name", mpTxtUser);
    addInput("DB Password", mpTxtPassword);
    addInput("Show Destination", mpCBShowDest);
    
    mpCenterPanel.add(mpHistoryGraph, BorderLayout.CENTER);
    
    mpBtnSubmit.setText("Start");
    mpBtnClear.setText("Stop");
    mpBtnClear.setEnabled(false);
  }

  /**
   * Clear the chart data
   */
  protected void clearChartData()
  {
    mpChartData.clear();
    mpPollCount = 0;
    for (int i = 0; i < RESULTS_TO_DISPLAY; i++)
    {
      mpChartData.addValue(0, TOTAL_SERIES_NAME, mpPollCount++);
    }
  }
  
  /**
   * Start the JBoss monitor
   */
  private void startMonitor()
  {
    if (mpTxtDriver.isEnabled() &&
        !displayYesNoPrompt("Save this configuration "
            + "(once saved, you must restart Warehouse Rx to change)"))
    {
      return;
    }
    
    // Multiple calls to Application.set* do not over-write--they just add to
    // the properties map.
    mpTxtDriver.setEnabled(false);
    mpTxtURL.setEnabled(false);
    mpTxtUser.setEnabled(false);
    mpTxtPassword.setEnabled(false);

    Application.setString(JBOSS_DB + ".driver", mpTxtDriver.getText());
    Application.setString(JBOSS_DB + ".url", mpTxtURL.getText());
    Application.setString(JBOSS_DB + ".user", mpTxtUser.getText());
    Application.setString(JBOSS_DB + ".password", new String(mpTxtPassword.getPassword()));
    Application.setString(JBOSS_DB + ".maximum", "3");
    mpJBossDBObj = new DBObjectTL().getDBObject(JBOSS_DB);
    clearChartData();
    
    try
    {
      mpJBossDBObj.connect();
      mpTimer = new Timer(POLL_INTERVAL, this);
      mpTimer.setInitialDelay(0);
      mpTimer.start();
      
      mpBtnSubmit.setEnabled(false);
      mpBtnClear.setEnabled(true);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }
  
  /**
   * Stop the JBoss monitor
   */
  private void stopMonitor()
  {
    mpBtnSubmit.setEnabled(true);
    mpBtnClear.setEnabled(false);
    
    try
    {
      if (mpTimer != null)
      {
        mpTimer.stop();
        mpTimer = null;
        if (mpJBossDBObj.isConnectionActive())
        {
          mpJBossDBObj.disconnect();
        }
      }
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }

  
  /*========================================================================*/
  /* OVERRIDDEN METHODS - ActionListener                                    */
  /*========================================================================*/
  
  /**
   * Refresh the message count
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e)
  {
    try
    {
      List<Map> vpResults = mpJBossDBObj.execute(JBOSS_QUERY_DEST).getRows();
      
      int vnTotalCount = 0;
      for (Map m : vpResults)
      {
        String vsDest = m.get(DESTINATION_NAME).toString();
        int vnDCount = Integer.parseInt(m.get(RESULT_NAME).toString());
        vnTotalCount += vnDCount;

        if (mpCBShowDest.isSelected())
        {
          mpChartData.addValue(vnDCount, vsDest, mpPollCount);
        }
      }
      mpChartData.addValue(vnTotalCount, TOTAL_SERIES_NAME, mpPollCount++);
      mpChartData.removeColumn(0);
      
      displayInfoAutoTimeOut(new Date().toString() + ": " + vnTotalCount + " messages");
    }
    catch (DBException dbe)
    {
      stopMonitor();
      logAndDisplayException(dbe);
    }
  }
  
  
  /*========================================================================*/
  /* OVERRIDDEN METHODS - SKDCInternalFrame                                 */
  /*========================================================================*/
  
  /**
   * Stuff to do when the frame is opened
   * 
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#internalFrameOpened(javax.swing.event.InternalFrameEvent)
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent e)
  {
    super.internalFrameOpened(e);

    mpTxtDriver.setText(Application.getString(JBOSS_DB + ".driver", "oracle.jdbc.driver.OracleDriver"));
    mpTxtURL.setText(Application.getString(JBOSS_DB + ".url", "jdbc:oracle:thin:@localhost:1521:JBossJMS"));
    mpTxtUser.setText(Application.getString(JBOSS_DB + ".user", "JBoss"));
    mpTxtPassword.setText(Application.getString(JBOSS_DB + ".password", "JBoss"));
    
    if (Application.getString(JBOSS_DB + ".url") != null)
    {
      mpTxtDriver.setEnabled(false);
      mpTxtURL.setEnabled(false);
      mpTxtUser.setEnabled(false);
      mpTxtPassword.setEnabled(false);
      displayInfoAutoTimeOut("Configuration Loaded");
    }
    else
    {
      displayInfoAutoTimeOut("Guessed JBoss configuration");
    }
    setMinimumSize(getPreferredSize());
    setResizable(true);
  }
  
  /**
   * Make sure the monitor is stopped before shutting down
   * 
   * @see com.daifukuamerica.wrxj.swing.SKDCInternalFrame#shutdownFrame()
   */
  @Override
  protected void shutdownFrame()
  {
    stopMonitor();
    super.shutdownFrame();
  }
  
  
  /*========================================================================*/
  /* OVERRIDDEN METHODS - DacInputFrame                                     */
  /*========================================================================*/

  /**
   * 
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#okButtonPressed()
   */
  @Override
  protected void okButtonPressed()
  {
    startMonitor();
  }
  
  /**
   * 
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#clearButtonPressed()
   */
  @Override
  protected void clearButtonPressed()
  {
    stopMonitor();
  }
}
