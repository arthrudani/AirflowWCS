/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2004 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.zone;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroup;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroupData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.event.InternalFrameEvent;

@SuppressWarnings("serial")
public class UpdateZoneGroup extends DacInputFrame
{
  private SKDCTextField    mpZoneGroup;
  private SKDCIntegerField mpPriority;
  private SKDCComboBox     mpZone;
  
  String msZoneGroup = "";
  int    mnPriority = 0;

  StandardLocationServer mpLocServer = Factory.create(StandardLocationServer.class);
  ZoneGroupData mpDefaultZGD = Factory.create(ZoneGroupData.class);

  boolean mzAdding = true;

  /**
   *  Create Location Zone screen class.
   *
   *  @param title Title to be displayed.
   */
  public UpdateZoneGroup(String isTitle)
  {
    super(isTitle, "Recommended Zone Information");

    try
    {
      buildScreen();
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
      displayError(dbe.getMessage());
    }
  }
  
  /**
   *  Method to set screen for modifing.
   *
   *  @param isZoneGroup Zone type to be modified.
   */
  public void setModify(String isZoneGroup, int inPriority)
  {
    msZoneGroup = isZoneGroup;
    mnPriority  = inPriority;
    try
    {
      ZoneGroupData vpZGData = mpLocServer.getZoneGroupMember(isZoneGroup, inPriority);
      String[] vasZoneList = mpLocServer.getZonesNotInGroup(isZoneGroup);
      String[] vasModZoneList = new String[vasZoneList.length + 1];
      vasModZoneList[0] = vpZGData.getZone();
      for (int i = 0; i < vasZoneList.length; i++)
      {
        vasModZoneList[i+1] = vasZoneList[i];
      }
      mpZone.setComboBoxData(vasModZoneList);
    }
    catch (DBException dbe)
    {
      dbe.printStackTrace();
      logger.logException(dbe);
    }
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
         mpDefaultZGD = mpLocServer.getZoneGroupMember(msZoneGroup, mnPriority);
       }
       catch (DBException e2)
       {
         displayError("Unable to get Zone data");
         return;
       }

       mpZoneGroup.setEnabled(false);
       mpPriority.setEnabled(false);
       setTimeout(90);
     }
     setData(mpDefaultZGD);
  }


  /*========================================================================*/
  /*  Methods for display formatting                                        */
  /*========================================================================*/
  /**
   * Builds the update form
   * 
   * @throws NoSuchFieldException
   */
  private void buildScreen() throws NoSuchFieldException, DBException
  {
    mpZoneGroup = new SKDCTextField(ZoneGroupData.ZONEGROUP_NAME);
    mpPriority  = new SKDCIntegerField(ZoneGroupData.PRIORITY_NAME);
    mpZone      = new SKDCComboBox(mpLocServer.getZonesNotInGroup(msZoneGroup));
    mpZone.setPrototypeDisplayLength(DBInfo.getFieldLength(ZoneGroupData.ZONE_NAME));

    mpZoneGroup.addFocusListener(new FocusListener()
      {
        public void focusGained(FocusEvent arg0)
        {
          updateForNewZoneGroup(mpZoneGroup.getText());
        }

        public void focusLost(FocusEvent arg0)
        {
          updateForNewZoneGroup(mpZoneGroup.getText());
        }
      });

    addInput("Recommended Zone:", mpZoneGroup);
    addInput("Priority:", mpPriority);
    addInput("Location Zone:", mpZone);
    
    useAddButtons();
  }

  /**
   * Update priority and zone list based upon a new zone group
   * 
   * @param isZoneGroup
   */
  private void updateForNewZoneGroup(String isZoneGroup)
  {
    try
    {
      mpPriority.setValue(mpLocServer.getNextZoneGroupPriority(isZoneGroup));
      mpZone.setComboBoxData(mpLocServer.getZonesNotInGroup(isZoneGroup));
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
      displayError(dbe.getMessage());
    }
  }
  
  
  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new Location Zone to the database.
   */
  @Override
  protected void okButtonPressed()
  {
    String vsZoneGroup = mpZoneGroup.getText().trim();
    int    vnPriority  = mpPriority.getValue();
    String vsZone      = mpZone.getText().trim();
    String vsZGDescription = ZoneGroup.describeZoneGroupMember(vsZoneGroup, vnPriority);

    boolean vzAlreadyExists;
    try
    {
      vzAlreadyExists = (mpLocServer.getZoneGroupMember(vsZoneGroup, vnPriority) != null);
    }
    catch (DBException e2)
    {
      displayError("Unable to get Zone Group data");
      return;
    }

    if(mzAdding)
    {
      if(vsZoneGroup.length() < 1)
      {
        displayError("Zone Group Cannot Be Blank");
        mpZoneGroup.requestFocus();
        return;
      }
      if(vsZone.length() < 1)
      {
        displayError("Zone Cannot Be Blank");
        mpZone.requestFocus();
        return;
      }
    }
    if (mzAdding && vzAlreadyExists)
    {
      displayError(vsZGDescription + " already exists");
      mpZoneGroup.requestFocus();
      return;
    }

    if (!mzAdding && !vzAlreadyExists)
    {
      displayError(vsZGDescription + " does not exist");
      mpZoneGroup.requestFocus();
    }

    // fill in Zone Group data
    ZoneGroupData vpZoneData;
    if (mzAdding)
    {
      vpZoneData = Factory.create(ZoneGroupData.class);
    }
    else
    {
      try
      {
        vpZoneData = mpLocServer.getZoneGroupMember(vsZoneGroup, vnPriority);
      }
      catch (DBException e2)
      {
        displayError("Unable to get Zone Group data");
        return;
      }
    }

    try
    {
      if (mzAdding)
      {
        vpZoneData.setZoneGroup(vsZoneGroup);
        vpZoneData.setPriority(vnPriority);
      }
      vpZoneData.setZone(vsZone);

      if (mzAdding)
      {
        mpLocServer.addZoneGroupMember(vpZoneData);
        changed();
        displayInfoAutoTimeOut(vsZGDescription + " added");
      }
      else
      {
        mpLocServer.modifyZoneGroupMember(vpZoneData);
        changed();
        displayInfoAutoTimeOut(vsZGDescription + " updated");
      }
    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);
      if (mzAdding)
      {
        displayError(e2.getMessage(), "Error adding " + vsZGDescription);
      }
      else
      {
        displayError(e2.getMessage(), "Error updating " + vsZGDescription);
      }
    }

    if (!mzAdding)
    {
      close();
    }
    else
    {
      try
      {
        mpPriority.setValue(vnPriority+1);
        mpZone.setComboBoxData(mpLocServer.getZonesNotInGroup(vsZoneGroup));
      }
      catch (DBException dbe)
      {
        dbe.printStackTrace();
      }
    }
  }

  /**
   *  Action method to handle Clear button..
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(mpDefaultZGD);
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param ipZGData - Zone Group data to use in refreshing.
   *
   */
  private void setData(ZoneGroupData ipZGData)
  {
    mpZoneGroup.setText(ipZGData.getZoneGroup());
    mpPriority.setValue(ipZGData.getPriority());
    mpZone.setSelectedItem(ipZGData.getZone());
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
