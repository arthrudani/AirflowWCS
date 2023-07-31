package com.daifukuamerica.wrxj.swingui.route;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating routes.
 *
 * @author avt
 * @version 1.0
 */
public class UpdateRoute extends DacInputFrame
{
  private static final long serialVersionUID = 0L;

  SKDCTextField    mpTextRouteID  = new SKDCTextField(RouteData.ROUTEID_NAME);
  SKDCTranComboBox mpTranFromType = new SKDCTranComboBox();
  SKDCTranComboBox mpTranDestType = new SKDCTranComboBox();
  SKDCComboBox     mpComboFromID  = new SKDCComboBox();
  SKDCComboBox     mpComboDestID  = new SKDCComboBox();
  SKDCCheckBox     mpChkOnOff     = new SKDCCheckBox();

  StandardRouteServer   mpRouteServ    = Factory.create(StandardRouteServer.class);
  StandardStationServer mpStationServ  = Factory.create(StandardStationServer.class);
  StandardDeviceServer  mpDeviceServer = Factory.create(StandardDeviceServer.class);

  RouteData mpdefRouteData = Factory.create(RouteData.class);

  String msRouteID = "";
  String msRouteFromID = "";
  String msRouteDestID = "";

  boolean mzAdding = true;
  
  /**
   *  Create route screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateRoute( String isTitle)
  {
    super(isTitle, "Route Information");

    try
    {
      jbInit();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   *  Create default route screen class.
   */
  public UpdateRoute()
  {
    this("");
  }

  /**
   *  Method to set screen for modifying.
   *
   *  @param rtName Route name to be modified.
   *  @param rtCurrentStation Route current station to be modified.
   *  @param rtToStation
   */
  public void setModify(String rtName, String rtCurrentStation, String rtToStation)
  {
    msRouteID = rtName;
    msRouteFromID = rtCurrentStation.trim();
    msRouteDestID = rtToStation.trim();
    mzAdding = false;
    useModifyButtons();
  }

  /**
   * Overridden method so we can set up frame for either an add or modify
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    if (!mzAdding)
    {
      mpTextRouteID.setText(msRouteID);
      mpTextRouteID.setEnabled(false);
      mpTranFromType.setEnabled(true);
      mpComboFromID.setEnabled(true);
      mpTranDestType.setEnabled(true);
      mpComboDestID.setEnabled(true);

      try
      {
        RouteData rtSearchData = Factory.create(RouteData.class);
        if (mpTextRouteID.toString().trim().length() > 0)
        {
          rtSearchData.setKey(RouteData.ROUTEID_NAME, mpTextRouteID.getText());
          rtSearchData.setKey(RouteData.FROMID_NAME, msRouteFromID);
          rtSearchData.setKey(RouteData.DESTID_NAME, msRouteDestID);
        }
        mpdefRouteData = mpRouteServ.getRouteRecord(rtSearchData);
        if (mpdefRouteData == null)
        {
          displayError("Unable to get Route data");
          return;
        }
      }
      catch (DBException e2)
      {
        displayError("Unable to get Route data");
        return;
      }

      mpComboFromID.requestFocus();
      this.setTimeout(90);
    }
    setData(mpdefRouteData);

    // Setting up Item Listener here for the combo boxes so 
    // that initialization SELECTS will not cause a reset of
    // combo box.
    mpTranFromType.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          FromIDFill(e.getItem().toString());
        }
      }
    });

    mpTranDestType.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          DestIDFill(e.getItem().toString());
        }
      }
    });
    
  }

  /**
   *  Method to initialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  private void jbInit() throws Exception
  {
    try
    {
      mpTranFromType     = new SKDCTranComboBox(RouteData.FROMTYPE_NAME);
      mpTranDestType     = new SKDCTranComboBox(RouteData.DESTTYPE_NAME);
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }

    try
    {
      buildScreen();      // Set up all elements of Label column
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /*========================================================================*/
  /*  Methods for display formatting                                        */
  /*========================================================================*/
  /**
   * Builds the label column on the update form
   * @param ipJPanel
   * @throws NoSuchFieldException
   */
  private void buildScreen() throws NoSuchFieldException
  {
    addInput("Name:", mpTextRouteID);
    addInput("From Type:", mpTranFromType);
    addInput("From ID:", mpComboFromID);
    addInput("Destination Type:", mpTranDestType);
    addInput("Destination ID:", mpComboDestID);
    addInput("Route On:", mpChkOnOff);
    
    useAddButtons();
  }
  
  
  /*========================================================================*/
  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    mpDeviceServer.cleanUp();
    mpRouteServ.cleanUp();
    mpStationServ.cleanUp();
  }

  /**
   *  Method to populate the From ID combo box.
   *
   *  @param isFromType
   */
  void FromIDFill(String isFromType)
   {
     try
     {
       List<String> vpList = null;
       int vnFromType = 0;

       try
       {
         vnFromType = DBTrans.getIntegerValue(RouteData.FROMTYPE_NAME, isFromType);
       }
       catch(NoSuchFieldException e)
       {
         vnFromType = 0;
       }
       
       switch(vnFromType)
       {
         case DBConstants.STATION:
           vpList = mpStationServ.getStationNameList();
           vpList.add(0, "--Stations--");
           break;
         case DBConstants.EQUIPMENT:
           vpList = mpDeviceServer.getDeviceNameList();
           vpList.add(0, "--Devices--");
           break;
       }

       mpComboFromID.setComboBoxData(vpList);
     }
     catch (DBException e)
     {
       displayError("Unable to get Stations and Devices");
     }
   }

   /**
    *  Method to populate the Destination ID combo box.
    *
    * @param inDestType
    */
    void DestIDFill(String isDestType)
    {
      try
      {
        List<String> vpList = null;
        int vnDestType = 0;

        try
        {
          vnDestType = DBTrans.getIntegerValue(RouteData.FROMTYPE_NAME, isDestType);
        }
        catch(NoSuchFieldException e)
        {
          vnDestType = 0;
        }

        switch(vnDestType)
        {
          case DBConstants.STATION:
            vpList = mpStationServ.getStationNameList();         
            vpList.add(0, "--Stations--");
            break;
          case DBConstants.EQUIPMENT:
            vpList = mpDeviceServer.getDeviceNameList();
            vpList.add(0, "--Devices--");
            break;
        }

        vpList.add(null);
        mpComboDestID.setComboBoxData(vpList);
      }
      catch (DBException e)
      {
        displayError("Unable to get Roles");
      }
    }
   
   
  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new route to the database.
   *  glm The COMBINATION of the fields routeid, fromid, and destid must be unique
   */
  @Override
  protected void okButtonPressed()
  {
    RouteData vpRouteSearch = Factory.create(RouteData.class);
    RouteData vpRouteData = null;
 
    String vsNewRouteID = mpTextRouteID.getText().trim();
    String vsNewFromID = mpComboFromID.getText().trim();
    String vsNewDestID = mpComboDestID.getText().trim();

    if (vsNewFromID.length() > DBInfo.getFieldLength(RouteData.FROMID_NAME))
    {
      displayWarning("You must select a from ID.");
      return;
    }
    if (vsNewDestID.length() > DBInfo.getFieldLength(RouteData.DESTID_NAME))
    {
      displayWarning("You must select a destination ID.");
      return;
    }
    
    if (mzAdding)
    {
      vpRouteSearch.setKey(RouteData.ROUTEID_NAME, vsNewRouteID);
      vpRouteSearch.setKey(RouteData.FROMID_NAME, vsNewFromID);
      vpRouteSearch.setKey(RouteData.DESTID_NAME, vsNewDestID);
    }
    else
    {
      vpRouteSearch.setKey(RouteData.ROUTEID_NAME, msRouteID);
      vpRouteSearch.setKey(RouteData.FROMID_NAME, msRouteFromID);
      vpRouteSearch.setKey(RouteData.DESTID_NAME, msRouteDestID);

      /*
       * These are used for Modify
       */
      vpRouteSearch.setRouteID(msRouteID);
      vpRouteSearch.setFromID(msRouteFromID);
      vpRouteSearch.setDestID(msRouteDestID);
   }

    try
    {
      vpRouteData = mpRouteServ.getRouteRecord(vpRouteSearch);
    }
    catch (DBException e2)
    {
      displayError("Unable to get Route data");
      return;
    }

    if (mzAdding && (vpRouteData != null))
    {
      displayError(mpRouteServ.describeRouteSegment(vsNewRouteID, 
          vsNewFromID, vsNewDestID) + " already exists.");
      return;
    }

    if (!mzAdding && (vpRouteData == null))
    {
      displayError(mpRouteServ.describeRouteSegment(msRouteID, msRouteFromID,
          msRouteDestID) + " does not exist.");
      return;
    }

    /*
     * If we're modifying to or from IDs, make sure we don't cause a duplicate
     */
    if ((!mzAdding) && 
        ((!msRouteFromID.equals(vsNewFromID)) || (!msRouteDestID.equals(vsNewDestID))))
    {
      RouteData vpCheckRoute = Factory.create(RouteData.class);
      
      vpCheckRoute.setKey(RouteData.ROUTEID_NAME, vsNewRouteID);
      vpCheckRoute.setKey(RouteData.FROMID_NAME, vsNewFromID);
      vpCheckRoute.setKey(RouteData.DESTID_NAME, vsNewDestID);
      
      try
      {
        vpCheckRoute = mpRouteServ.getRouteRecord(vpCheckRoute);
        if (vpCheckRoute != null)
        {
          displayError(mpRouteServ.describeRouteSegment(vsNewRouteID, 
              vsNewFromID, vsNewDestID) + " already exists.");
          return;
        }
      }
      catch (DBException e2)
      {
        displayError("Unable to get Route data");
        return;
      }
    }

    // fill in Route data
    if (mzAdding)
    {
      vpRouteData = Factory.create(RouteData.class);
      if (vsNewRouteID.length() <= 0)  // required
      {
        displayError("Route name is required");
        return;
      }
      vpRouteData.setFromID(vsNewFromID);
    }
    vpRouteData.setRouteID(vsNewRouteID);
    vpRouteData.setFromID(vsNewFromID);
    vpRouteData.setDestID(vsNewDestID);
    if ((vpRouteData.getDestID() == null) || (vpRouteData.getDestID().trim().length() == 0))
    {
        displayError("To station is required");
        return;
    }
    if (vsNewFromID.equalsIgnoreCase(vsNewDestID))
    {
        displayError("To station cannot be same as current station");
        return;
    }
    if (mpChkOnOff.isSelected())
    {
      vpRouteData.setRouteOnOff(DBConstants.ON);
    }
    else
    {
      vpRouteData.setRouteOnOff(DBConstants.OFF);
    }

    try
    {
      vpRouteData.setFromType(mpTranFromType.getIntegerValue());
      vpRouteData.setDestType(mpTranDestType.getIntegerValue());
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      displayError("No Such Field: " + e2);
    }

    try
    {
      if (mzAdding)
      {
        mpRouteServ.addRoute(vpRouteData);
        changed();
        displayInfoAutoTimeOut("Route " + vsNewRouteID + " added");
      }
      else
      {
        mpRouteServ.modifyRoute(vpRouteSearch, vpRouteData);
        changed();
        displayInfoAutoTimeOut("Route " + vsNewRouteID + " updated");
      }
    }
    catch (DBException e2)
    {
      if (mzAdding)
      {
        displayError("Error adding route " + vsNewRouteID + ". " + SKDCConstants.EOL_CHAR +
                     e2.getMessage());
      }
      else
      {
        displayError("Error updating route " + vsNewRouteID + ". " + SKDCConstants.EOL_CHAR +
                     e2.getMessage());
      }
    }
    if (!mzAdding)
    {
      cleanUpOnClose();
      close();
    }
  }

  /**
   *  Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(mpdefRouteData);
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param rt Route data to use in refreshing.
   */
  void setData(RouteData rt)
  {
    String vsFromType = null;
    String vsDestType = null;
    
    mpTextRouteID.setText(rt.getRouteID());
        
    //Fill the Type Combo Fields
    try
    {
      vsFromType = DBTrans.getStringValue("iFromType", rt.getFromType());
      vsDestType = DBTrans.getStringValue("iDestType", rt.getDestType());
    }
    catch(NoSuchFieldException nsfe)
    {
      nsfe.printStackTrace(System.out);
      displayError("No Such Field: " + nsfe);
    }

    FromIDFill(vsFromType);
    mpComboFromID.setSelectedItem(rt.getFromID());

    DestIDFill(vsDestType);
    mpComboDestID.setSelectedItem(rt.getDestID());

    try
    {
      mpTranFromType.setSelectedElement(rt.getFromType());
      mpTranDestType.setSelectedElement(rt.getDestType());
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      displayError("No Such Field: " + e2);
    }
    
    mpChkOnOff.setSelected(rt.getRouteOnOff() == DBConstants.ON);
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    cleanUpOnClose();
    close();
  }
}