package com.daifukuamerica.wrxj.swingui.carrier;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCarrierServer;
import com.daifukuamerica.wrxj.dbadapter.data.CarrierData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.NoSuchElementException;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of carrier Stations.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class CarrierStationListFrame extends SKDCListFrame
{
  StandardCarrierServer mpCarServer = Factory.create(StandardCarrierServer.class);
  CarrierData mpSearchData = Factory.create(CarrierData.class);

  /**
   *  Create carrier station list frame.
   */
  public CarrierStationListFrame()
  {
    super("CarrierStation");
    setSearchData("Carrier", DBInfo.getFieldLength(CarrierData.CARRIERID_NAME));
    setDetailSearchVisible(false);
    setDisplaySearchCount(true, "Carrier Station");
    refreshButton.setVisible(false);
    addButton.setVisible(false);
    deleteButton.setVisible(false);
  }

  /**
   * Sets screen permissions.
   *
   * <p><b>Details:</b> <code>internalFrameOpened</code> augments the
   * supermethod by setting the screen permissions.</p>
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);
    if (ePerms == null)
      return;
    popupMenu.setAuthorization("Modify", ePerms.iModifyAllowed);

    searchButtonPressed();
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    mpCarServer.cleanUp();
    mpCarServer = null;
  }

  /**
   *  Method to filter by extended search. Refreshes display.
   */
  public void refreshTable()
  {
    try
    {
      mpSearchData.clear();
      if (getEnteredSearchText().trim().length() > 0)
      {
        mpSearchData.setKey(CarrierData.CARRIERID_NAME, getEnteredSearchText(), 
          KeyObject.LIKE);
      }
      refreshTable(mpCarServer.getCarrierList(mpSearchData));
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
    catch (NoSuchElementException e){}
  }

  /**
   *  Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    refreshTable();
  }

  /**
   *  Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {
    if (isSelectionValidForModify(false))
    {
      Object cObjCarrier = sktable.getCurrentRowDataField(CarrierData.CARRIERID_NAME);
      Object cObjStation = sktable.getCurrentRowDataField(CarrierData.STATIONNAME_NAME);
      if (cObjCarrier != null)
      {
        UpdateCarrierStation vpUpdater = Factory.create(UpdateCarrierStation.class, "Modify Carrier Station");
        vpUpdater.setModify(cObjCarrier.toString(),cObjStation.toString());
        addSKDCInternalFrameModal(vpUpdater, buttonPanel,
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent e)
              {
                String prop = e.getPropertyName();
                if (prop.equals(FRAME_CHANGE))
                {
                  refreshTable();
                }
              }
            });
      }
    }
  }

  /**
   * Mouse listener for the table 
   */
  @Override
  protected void setTableMouseListener()
  {
    super.setTableMouseListener();
    popupMenu.remove("Add");
    popupMenu.remove("Delete");
  }
  
  /**
   * Get the class name that will be used in the RoleOptions table.  This 
   * method facilitates the getting of permissions when setCategoryAndOption()
   * is not called and the implemented class is different from the baseline
   * class.
   * 
   * @return <code>Class</code>
   */
  @Override
  protected Class getRoleOptionsClass()
  {
    return CarrierStationListFrame.class;
  }
}