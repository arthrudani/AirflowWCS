/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.swingui.aesystem;

import com.daifukuamerica.wrxj.dataserver.standard.StandardAeSystemServer;
import com.daifukuamerica.wrxj.dbadapter.data.aed.CommunicationType;
import com.daifukuamerica.wrxj.dbadapter.data.aed.Instance;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunicationsData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.ManualTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import java.util.HashMap;
import java.util.Map;

/**
 * A screen class for updating containers.
 * 
 * <p>Note that this screen is provided as a developer convenience.  This screen
 * should have a counterpart in Visibility.<p>
 * 
 * @version 1.0
 */
public class InstanceCommunicationsInput extends DacInputFrame
{
  private static final long serialVersionUID = 634774270860201243L;
  
  private InstanceCommunicationsData mpDefaultData;
  
  private ManualTranComboBox<Integer> mpSender;
  private SKDCIntegerField mpComponentId;
  private ManualTranComboBox<Integer> mpReceiver;
  private ManualTranComboBox<Integer> mpCommType;
  
  /**
   *  Create container screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public InstanceCommunicationsInput(String isTitle)
  {
    super(isTitle, "AE System Instance Communications");
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

  /*========================================================================*/
  /*  Methods for display formatting                                        */
  /*========================================================================*/
  /**
   * Builds the update form
   * 
   * @throws NoSuchFieldException
   */
  protected void buildScreen() throws NoSuchFieldException
  {
    Map<Integer, String> vpInstanceMap = new HashMap<>();
    Map<Integer, String> vpCommMap = new HashMap<>();
    try
    {
      vpInstanceMap = Factory.create(Instance.class).getTranslationMap();
      vpCommMap = Factory.create(CommunicationType.class).getTranslationMap();
    }
    catch (Exception e)
    {
      logAndDisplayException("Error initialing combos", e);
    }
    mpSender = new ManualTranComboBox<>(vpInstanceMap);
    mpComponentId = new SKDCIntegerField(5);
    mpComponentId.setValue(0);
    mpReceiver = new ManualTranComboBox<>(vpInstanceMap);
    mpCommType = new ManualTranComboBox<>(vpCommMap);

    addInput("Sender:", mpSender);
    addInput("Component:", mpComponentId);
    addInput("Receiver:", mpReceiver);
    addInput("Comm Type:", mpCommType);

    useAddButtons();
  }

  /*========================================================================*/
  /*  Action methods                                                        */
  /*========================================================================*/

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new container to the database.
   */
  @Override
  protected void okButtonPressed()
  {
    Integer vnSenderId = mpSender.getSelectedKey();
    Integer vnReceiverId = mpReceiver.getSelectedKey();
    Integer vnCommTypeId = mpCommType.getSelectedKey();

    InstanceCommunicationsData vpData = Factory.create(InstanceCommunicationsData.class);
    vpData.setSenderId(vnSenderId);
    vpData.setSenderComponentId(mpComponentId.getValue());
    vpData.setReceiverId(vnReceiverId);
    vpData.setCommunicationTypeId(vnCommTypeId);
    
    try
    {
      Factory.create(StandardAeSystemServer.class).addInstanceCommunication(vpData);
      this.changed();
      displayInfoAutoTimeOut("Added");
    }
    catch (Exception e2)
    {
      e2.printStackTrace(System.out);
      displayError("Error adding definition");
    }
  }

  /**
   *  Action method to handle Clear button..
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(mpDefaultData);
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param ipPecLSData data to use in refreshing.
   */
  protected void setData(InstanceCommunicationsData ipPecLSData)
  {
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