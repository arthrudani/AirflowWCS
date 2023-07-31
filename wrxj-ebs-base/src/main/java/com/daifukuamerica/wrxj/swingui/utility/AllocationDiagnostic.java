package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Description:<BR>
 *    Primary frame for troubleshooting allocation failure.
 *
 * @author       A.D.
 * @version      1.0      12-Jul-05
 */
@SuppressWarnings("serial")
public class AllocationDiagnostic extends DacInputFrame
{
  private SKDCTextField mpTxtOrderID;
  private JTextArea mpTextArea;
  private StandardOrderServer mpOrdServer;
  private DiagnosticObserver mpMessageObserver;
  
  /**
   * Constructor
   */
  public AllocationDiagnostic()
  {
    super("Allocation Diagnostic", "");

    mpOrdServer = Factory.create(StandardOrderServer.class);
    
    buildScreen();

    subscribeAllocationProbeResults();
  }

  /**
   * Constructor
   * 
   * @param isOrderID
   */
  public AllocationDiagnostic(String isOrderID)
  {
    this();
    mpTxtOrderID.setText(isOrderID);
  }
  
  /**
   * Force a preferred size 
   */
  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(750, 420));
  }

  /**
   * Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    getSystemGateway().deleteObserver(
        MessageEventConsts.ALLOCATION_PROBE_EVENT_TYPE, mpMessageObserver);
    mpMessageObserver = null;
  }

  /**
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#clearButtonPressed()
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTxtOrderID.setText("");
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#okButtonPressed()
   */
  @Override
  protected void okButtonPressed()
  {
    runDiagnostic();
  }
  
  /**
   * Run the diagnostic
   */
  public void runDiagnostic()
  {
    String vsAllocator = "";
    String sOrderID = mpTxtOrderID.getText();
    if (sOrderID.trim().length() == 0)
    {
      displayError("Order ID must be entered!");
      return;
    }
    try
    {
      vsAllocator = mpOrdServer.getAllocatorForOrder(sOrderID);
    }
    catch (DBException ex)
    {
      displayError("Error getting allocator: " + ex.getMessage());
    }
    getSystemGateway().publishAllocationProbeEvent(sOrderID, AllocationMessageDataFormat.ORDER_DIAGNOSIS, vsAllocator);
  }

  /**
   * Build the screen
   */
  private void buildScreen()
  {
    mpTextArea = new JTextArea(5, 10);
    mpTextArea.setLineWrap(true);
    mpTextArea.setFont(new Font("Monospaced", Font.BOLD, 12));
    mpTextArea.setForeground(Color.RED);
    mpTextArea.setEditable(false);
    
    mpTxtOrderID = new SKDCTextField(OrderHeaderData.ORDERID_NAME);
    addInput("Order ID:", mpTxtOrderID);

    mpBtnSubmit.setText("Run Diagnostic");
    mpBtnSubmit.setToolTipText("Execute the diagnostic tool.");
    mpBtnSubmit.setMnemonic('R');
    
    Container cp = getContentPane();
    cp.add(mpInputPanel, BorderLayout.NORTH);
    cp.add(new JScrollPane(mpTextArea), BorderLayout.CENTER);
    cp.add(mpButtonPanel,BorderLayout.SOUTH);
  }

  /**
   * Subscribe to the allocation probe results messages
   */
  private void subscribeAllocationProbeResults()
  {
    mpMessageObserver = new DiagnosticObserver();
    String selector = getSystemGateway().getAllocationProbeEventSelector();
    getSystemGateway().addObserver(
        MessageEventConsts.ALLOCATION_PROBE_EVENT_TYPE, selector + "%",
        mpMessageObserver);
  }
  
  /**
   * An observer class needed for this screen to receive and process messages.
   */
  private class DiagnosticObserver implements Observer
  {
    /**
     * Method to process the arrivals and releases of loads for this station. If
     * a load arrives at or leaves the station, then we recheck and reset the
     * screen with the correct information.
     * 
     * @param o no information available
     * @param arg no information available
     */
    public void update(Observable o, Object arg)
    {
      ObservableControllerImpl observableData = (ObservableControllerImpl)o;
      String sText = observableData.getStringData();
      mpTextArea.setText(sText);
    }
  }
}
