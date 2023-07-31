package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class EmptyLoads extends PolygonButton
{
  
  protected StandardLoadServer mpLoadServer = new StandardLoadServer();
  protected String msToolTip;
  public EmptyLoads()
  {
    // TODO Auto-generated constructor stub
  }

  public EmptyLoads(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
    // TODO Auto-generated constructor stub
  }
  /**
   * Build this graphic's pop-up menu
   * 
   * @param ipEMF
   * @param inPanelIndex
   * @param isGroupName
   * @param izCanTrack
   * @param ipPermissions
   */
 
  /**
   * Set the graphics properties
   * 
   * @param ipProperties
   */
  @Override
  protected void setProperties(Map<String, String> ipProperties)
  {
      
    mnDefaultFontSize = 20;
    
    super.setProperties(ipProperties);
    
    refreshStatus();
    
    //  Since it'll be in a different thread the next time we need it, we'll
    //  set it to null and reinitialize it.
   
  }
  
  /**
   * Build this graphic's pop-up menu
   * 
   * @param ipEMF
   * @param inPanelIndex
   * @param isGroupName
   * @param izCanTrack
   * @param ipPermissions
   */
  @Override
  protected void buildPopup(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      String isGroupName, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    if (!mzIsFuture)
    {
      mpPopup = new SKDCPopupMenu();
      mpPopup.add("Refresh", "Refresh", new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
            refreshStatus();
          }
        });
    }
  }

 
  /**
   * Handle problems processing the graphic parameters
   * 
   * @param isParameter
   * @param ipException
   */
  @Override
  protected void handleParameterError(String isParameter, Exception ipException)
  {
    super.handleParameterError(isParameter, ipException);
    
   }

  /**
   * Set the controlling device of the locations to be monitored
   */
  @Override
  public void setMCController(String isMCController)
  {
    int vnTotalEmpties = 0;
    super.setMCController(isMCController);

    mpLoadServer = new StandardLoadServer();
    try
    {
         vnTotalEmpties = mpLoadServer.getLoadCount(null, null, 
                                             DBConstants.EMPTY,
                                             DBConstants.NOMOVE);
     }
    catch (DBException dbe)
    {
      System.err.println(dbe.getMessage());
    }
    setEmpties(vnTotalEmpties);
  }
  @Override
  public void setStatus(int inStatus){}
  
  protected void setEmpties(int inEmpties)
  {
      mpStatusColor = Color.GREEN;
      setStatusText("# Empty Loads " + inEmpties );
  }
  
  /**
   * Set the tool-tip text
   */
  @Override
  public void setToolTipText(String text)
  {
    if (text != null)
    {
      msToolTip = text;
      super.setToolTipText(text + "  ("
          + new SimpleDateFormat("HH:mm:ss").format(new Date()) + ")");
    }
  }
 
  /**
   * Refresh the status of the instantiated EquipmentGraphic object.
   * This method is to allow the equipment monitor to update based on local
   * WarehouseRx screen events.
   * 
   * @return status - the status
   */
  @Override
  public void refreshStatus()
  {
    int vnTotalEmpties = 0;
    try
    {
      vnTotalEmpties = mpLoadServer.getLoadCount( null, null, 
                                              DBConstants.EMPTY,
                                              DBConstants.NOMOVE); 
    }  
    catch (DBException dbe)
    {
      System.err.println(msDeviceName + ": " + dbe.getMessage());
    }
   
    setEmpties(vnTotalEmpties);
  }

}
