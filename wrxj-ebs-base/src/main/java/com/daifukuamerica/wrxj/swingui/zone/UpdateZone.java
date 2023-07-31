package com.daifukuamerica.wrxj.swingui.zone;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2005
 * Company:      Daifuku America Corp.
 */

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating Location Zones.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateZone extends DacInputFrame
{
  String msZone = "";

  private SKDCTextField mpZone;
  private SKDCTextField mpDescription;
  
  StandardLocationServer mpLocServer = Factory.create(StandardLocationServer.class);
  ZoneData mpDefaultZoneData = Factory.create(ZoneData.class);
  
  boolean mzAdding = true;

  /**
   *  Create Location Zone screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateZone(String isTitle)
  {
    super(isTitle, "Location Zone Information");

    try
    {
      buildScreen();
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
    }
  }

  /**
   *  Method to set screen for modifing.
   *
   *  @param s Zone type to be modified.
   */
  public void setModify(String s)
  {
    msZone = s;
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
      try
      {
        mpDefaultZoneData = mpLocServer.getZone(msZone);
      }
      catch (DBException e2)
      {
        displayError("Unable to get Zone data");
        return;
      }
      mpZone.setEnabled(false);
      this.setTimeout(90);
    }
    setData(mpDefaultZoneData);
  }


  /*========================================================================*/
  /*  Methods for display formatting                                        */
  /*========================================================================*/
  /**
   * Builds the update form
   * 
   * @throws NoSuchFieldException
   */
  private void buildScreen() throws NoSuchFieldException
  {
    mpZone        = new SKDCTextField(ZoneData.ZONE_NAME);
    mpDescription = new SKDCTextField(ZoneData.DESCRIPTION_NAME);

    addInput("Location Zone:", mpZone);
    addInput("Description:", mpDescription);
    
    useAddButtons();
  }

  
  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new Location Zone to the database.
   */
  @Override
  protected void okButtonPressed()
  {
    boolean alreadyExists;

    try
    {
      alreadyExists = (mpLocServer.getZone(mpZone.getText().trim()) != null);
    }
    catch (DBException e2)
    {
      displayError("Unable to get Zone data");
      return;
    }

    if(mzAdding)
    {
        if(mpZone.getText().trim().length() < 1)
        {
            displayError("Zone Cannot Be Blank");
            mpZone.requestFocus();
            return;
        }
    }
    if (mzAdding && alreadyExists)
    {
      displayError("Zone " + mpZone.getText().trim() + " already exists");
      mpZone.requestFocus();
      return;
    }

    if (!mzAdding && !alreadyExists)
    {
      displayError("Zone " + mpZone.getText().trim() + " does not exist");
      mpZone.requestFocus();
    }

    // fill in Zone data
    ZoneData vpZoneData;
    if (mzAdding)
    {
      vpZoneData = Factory.create(ZoneData.class);
    }
    else
    {
      try
      {
        vpZoneData = mpLocServer.getZone(mpZone.getText().trim());
      }
      catch (DBException e2)
      {
        displayError("Unable to get Zone data");
        return;
      }
    }

    try
    {
      if (mzAdding)
      {
        vpZoneData.setZone(mpZone.getText().trim());
      }
      vpZoneData.setDescription(mpDescription.getText().trim());

      if (mzAdding)
      {

        mpLocServer.addZone(vpZoneData);
        changed();
        displayInfoAutoTimeOut("Zone " + mpZone.getText().trim() + " added");
      }
      else
      {
        mpLocServer.modifyZone(vpZoneData);
        changed();
        displayInfoAutoTimeOut("Zone " + mpZone.getText().trim() + " updated");
      }
    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);
      if (mzAdding)
      {
        displayError(e2.getMessage(), "Error adding Zone " + mpZone.getText().trim());
      }
      else
      {
        displayError(e2.getMessage(), "Error updating Zone " + mpZone.getText().trim());
      }
    }

    if (!mzAdding)
    {
      close();
    }
  }

  /**
   *  Action method to handle Clear button..
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(mpDefaultZoneData);
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param ipZoneData - Zone data to use in refreshing.
   */
  private void setData(ZoneData ipZoneData)
  {
    mpZone.setText(ipZoneData.getZone());
    mpDescription.setText(ipZoneData.getDescription());
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }
}