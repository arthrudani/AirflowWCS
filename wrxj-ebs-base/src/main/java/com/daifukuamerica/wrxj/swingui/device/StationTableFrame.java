package com.daifukuamerica.wrxj.swingui.device;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * Description:<BR>
 *    Sets up the internal frame containing Station information for a device.
 *
 * @author       A.D.
 * @version      1.0
 * @since        14-Mar-2002
 */
@SuppressWarnings("serial")
public class StationTableFrame extends SKDCInternalFrame
       implements SKDCGUIConstants
{
  private StandardDeviceServer mpDevServer    = null;
  private DacTable    eskTable     = null;
  private SKDCButton   btnClose    = null;
  private JPanel       btnpanel    = null;
  private List<Map>    mpDataList  = null;

  public StationTableFrame(StandardDeviceServer ipDevServer)
  {                                    // This internal frame will be
                                       // resizable, but not closable.
    super("Station Data", true, true);
    mpDevServer = ipDevServer;
  }

  /**
   *  Fills in the Staton table frame.
   * @param deviceID 
   * @throws DBException
   */
  public void setTableData(String deviceID) throws DBException
  {
                                       // Get the Station Data for this Device
                                       // from the server.
    mpDataList = mpDevServer.getDeviceStationData(deviceID);
    if (mpDataList.size() == 0)
    {
      throw new DBException("No Rows found!");
    }

    eskTable = new DacTable(new DacModel(mpDataList, "DeviceStation"));
                                       //Add the scroll pane to this frame.
    this.getContentPane().add(eskTable.getScrollPane(), BorderLayout.CENTER);
                                       // Add the button panel.
    this.getContentPane().add(button_panel(), BorderLayout.SOUTH);
  }

  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(750, 275));
  }

  /**
   *  Handles close frame notification.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
      Adds Buttons to a panel.
   */
  private JPanel button_panel()
  {
    btnpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

    btnpanel.setBorder(new EtchedBorder());
    btnClose = new SKDCButton("    Close    ", "Close Window", 'C');
    btnClose.addEvent(CLOSE_BTN, new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (e.getActionCommand().equals(CLOSE_BTN))
        {
          closeButtonPressed();
        }
      }
    });

    btnpanel.add(btnClose);            // Add the buttons to the panel

    return(btnpanel);
  }
}
