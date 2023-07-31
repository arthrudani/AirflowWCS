package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.controller.NamedThread;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.InternalFrameEvent;


/**
 * Monitors runtime resources.
 *
 * <p><b>Details:</b> <code>JavaRuntimeMonitorFrame</code> is a frame that displays
 * useful information about the state of the Java runtime.  Presently, this user
 * interface monitors the thread count of the main thread group and memory
 * usage.  It also features a button to invoke the garbage collector.  This user
 * interface is intended primarily for developer trouble-<wbr>shooting and is
 * not intended for the end user.</p>
 *
 * @author Sharky
 */
public final class JavaRuntimeMonitorFrame
  extends
    SKDCInternalFrame
  implements
    Runnable,
    ActionListener
{
  private static final long serialVersionUID = 0L;

  /**
   * Polling interval.
   *
   * <p><b>Details:</b> <code>UPDATE_INTERVAL</code> is the time, in
   * milliseconds, between runtime polls and user interface updates.  Because
   * polling the runtime for resource information is not cheap, don't set this
   * value too low.</p>
   */
  private static final int UPDATE_INTERVAL = 1000;

  private final JTextField mpTxtThreads = new JTextField();

  private final JTextField mpTxtUsedMemory = new JTextField();

  private final JTextField mpTxtFreeMemory = new JTextField();

  private final JTextField mpTxtTotalMemory = new JTextField();

  private final JTextField mpTxtMaxMemory = new JTextField();

  private final SKDCButton mpButCollectGarbage = new SKDCButton("Collect Garbage");

  private final ThreadTableModel mpThreadTableModel = new ThreadTableModel();

  private Thread mpThread;

  /**
   * Default constructor.
   *
   * <p><b>Details:</b> This constructor sets up the user interface but does not
   * create or start the monitor thread.</p>
   */
  public JavaRuntimeMonitorFrame()
  {
    // Initialize components:
    final SKDCLabel vpLblThreads = new SKDCLabel("Threads:");
    vpLblThreads.setHorizontalAlignment(SwingConstants.RIGHT);
    mpTxtThreads.setHorizontalAlignment(JTextField.TRAILING);
    final SKDCLabel vpLblUsedMemory = new SKDCLabel("Used Memory:");
    vpLblUsedMemory.setHorizontalAlignment(SwingConstants.RIGHT);
    mpTxtUsedMemory.setHorizontalAlignment(SwingConstants.RIGHT);
    final SKDCLabel vpLblFreeMemory = new SKDCLabel("Free Memory:");
    vpLblFreeMemory.setHorizontalAlignment(SwingConstants.RIGHT);
    mpTxtFreeMemory.setHorizontalAlignment(SwingConstants.RIGHT);
    final SKDCLabel vpLblTotalMemory = new SKDCLabel("Total Memory:");
    vpLblTotalMemory.setHorizontalAlignment(SwingConstants.RIGHT);
    mpTxtTotalMemory.setHorizontalAlignment(SwingConstants.RIGHT);
    final SKDCLabel vpLblMaxMemory = new SKDCLabel("Max Memory:");
    vpLblMaxMemory.setHorizontalAlignment(SwingConstants.RIGHT);
    mpTxtMaxMemory.setHorizontalAlignment(SwingConstants.RIGHT);
    final JTable vpTblThreads = new JTable(mpThreadTableModel);
    final JScrollPane vpScrThreads = new JScrollPane(vpTblThreads);
    // Attach components:
    final JPanel vpPanel = new JPanel();
    vpPanel.setLayout(new GridLayout(5, 2));
    vpPanel.add(vpLblThreads, null);
    vpPanel.add(mpTxtThreads, null);
    vpPanel.add(vpLblUsedMemory, null);
    vpPanel.add(mpTxtUsedMemory, null);
    vpPanel.add(vpLblFreeMemory, null);
    vpPanel.add(mpTxtFreeMemory, null);
    vpPanel.add(vpLblTotalMemory, null);
    vpPanel.add(mpTxtTotalMemory, null);
    vpPanel.add(vpLblMaxMemory, null);
    vpPanel.add(mpTxtMaxMemory, null);
    final Box vpBox = Box.createVerticalBox();
    mpButCollectGarbage.setAlignmentX(Box.CENTER_ALIGNMENT);
    vpBox.add(mpButCollectGarbage, null);
    vpBox.add(vpScrThreads);
    getContentPane().add(vpPanel, BorderLayout.NORTH);
    getContentPane().add(vpBox, BorderLayout.CENTER);
    setTitle("Java Monitor");
    // Set up events:
    mpButCollectGarbage.addActionListener(this);
  }

  /**
   * Indicates no system gateway is needed.
   *
   * <p><b>Details:</b> <code>getSystemGatewayNeeded</code> returns
   * <code>false</code> to indicate that this frame requires no system
   * gateway.</p>
   *
   * @return false
   */
  @Override
  protected boolean getSystemGatewayNeeded() {return false;}

  /**
   * Starts monitor thread.
   *
   * <p><b>Details:</b> <code>internalFrameOpened</code> is a callback method
   * and should not be directly invoked by client code.</p>
   *
   * <p>This implementation invokes the supermethod, and then it creates and
   * starts the monitor thread.</p>
   *
   * @param ipEvent passed to supermethod
   */
  @Override
  public void internalFrameOpened(final InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);
    mpThread = new NamedThread(this, "Java Runtime Monitor");
    mpThread.start();
  }

  /**
   * Stops monitor thread.
   *
   * <p><b>Details:</b> <code>internalFrameClosed</code> is a callback method
   * and should not be directly invoked by client code.</p>
   *
   * <p>This implementation invokes the supermethod and then interrupts the
   * monitor thread.</p>
   *
   * @param ipEvent passed to supermethod
   */
  @Override
  public void internalFrameClosed(final InternalFrameEvent ipEvent)
  {
    super.internalFrameClosed(ipEvent);
    mpThread.interrupt();
  }

  private int mnMaxThreads = 0;
  
  private int mnMinThreads = Integer.MAX_VALUE;
  
  /**
   * Thread procedure.
   *
   * <p><b>Details:</b> <code>run</code> is a callback method and should not be
   * directly invoked by client code.</p>
   *
   * <p>This implementation polls runtime properties and updates the user
   * interface display in continuous loop.</p>
   */
  public void run()
  {
    while(! Thread.interrupted())
    {
      int vnThreads = Thread.currentThread().getThreadGroup().activeCount();
      mnMinThreads = Math.min(mnMinThreads, vnThreads);
      mnMaxThreads = Math.max(mnMaxThreads, vnThreads);
      String msTxtThreads = mnMinThreads + " < " + vnThreads + " < " + mnMaxThreads; 
      mpTxtThreads.setText(msTxtThreads);
      final Runtime vpRuntime = Runtime.getRuntime();
      final long vlFreeMemory = vpRuntime.freeMemory();
      final long vlTotalMemory = vpRuntime.totalMemory();
      mpTxtUsedMemory.setText(formatMemory(vlTotalMemory - vlFreeMemory));
      mpTxtFreeMemory.setText(formatMemory(vlFreeMemory));
      mpTxtTotalMemory.setText(formatMemory(vlTotalMemory));
      mpTxtMaxMemory.setText(formatMemory(vpRuntime.maxMemory()));
      mpThreadTableModel.poll();
      try
      {
        Thread.sleep(UPDATE_INTERVAL);
      }
      catch (final InterruptedException ve)
      {
        break;
      }
    }
    mpThread = null;
  }

  /**
   * Formats memory measurement.
   *
   * <p><b>Details:</b> <code>formatMemory</code> formats the supplied long
   * as a count of bytes by inserting commas for thousands separators and
   * appending "&nbsp;bytes" to the resulting string.</p>
   *
   * @param ilMem the number of bytes
   * @return the formatted string
   */
  private static String formatMemory(final long ilMem)
  {
    final StringBuffer vpBuff = new StringBuffer();
    vpBuff.append(ilMem);
    for (int vnI = vpBuff.length() - 3; vnI > 0; vnI -= 3)
      vpBuff.insert(vnI, ',');
    vpBuff.append(" bytes");
    return vpBuff.toString();
  }

  /**
   * Invokes garbage collector.
   *
   * <p><b>Details:</b> <code>actionPerformed</code> is a callback method and
   * should not be directly invoked by client code.</p>
   *
   * <p>This implementation invokes the garbage collector.</p>
   *
   * @param vpEvent ignored
   */
  public void actionPerformed(final ActionEvent vpEvent)
  {
    new Thread("JavaRuntimeMonitorFrame-System.gc()")
    {
      @Override
      public void run()
      {
        System.gc();
      }
    }.start();
  }

}

