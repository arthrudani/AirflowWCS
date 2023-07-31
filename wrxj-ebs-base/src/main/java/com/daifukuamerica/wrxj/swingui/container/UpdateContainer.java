package com.daifukuamerica.wrxj.swingui.container;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.ContainerTypeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating containers.
 *
 * @author avt
 * @version 1.0
 */
public class UpdateContainer extends DacInputFrame
{
  private static final long serialVersionUID = 1L;
  
  String containerType = "";
  String defaultContainerType = "";

  protected SKDCTextField mpContainerType;
  protected SKDCDoubleField mpContLength;
  protected SKDCDoubleField mpContWidth;
  protected SKDCDoubleField mpContHeight;
  protected SKDCDoubleField mpContWeight;
  protected SKDCDoubleField mpMaxWeight;

  protected StandardInventoryServer invtServ = Factory.create(StandardInventoryServer.class);
  StandardLocationServer mpLocServer = Factory.create(StandardLocationServer.class);
  ContainerTypeData defaultContainerData = Factory.create(ContainerTypeData.class);

  protected boolean mzAdding = true;
  
  /**
   *  Create container screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateContainer(String isTitle)
  {
    super(isTitle, "Container Information");
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
   *  @param s Container type to be modified.
   */
  public void setModify(String s)
  {
    containerType = s;
    useModifyButtons();
    mzAdding = false;
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

    if (!mzAdding) // we are modifying
    {
      try
      {
        defaultContainerData = invtServ.getContainer(containerType);
      }
      catch (DBException e2)
      {
        displayError("Unable to get Container data");
        return;
      }

      mpContainerType.setEnabled(false);
      this.setTimeout(90);
    }
    else
    {
      if (defaultContainerType.length() > 0) // we have a default name
      {
        mpContainerType.setText(defaultContainerType);
      }
    }
    setData(defaultContainerData);
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
    mpContainerType = new SKDCTextField(ContainerTypeData.CONTAINERTYPE_NAME);
    mpContLength    = new SKDCDoubleField(ContainerTypeData.CONTLENGTH_NAME);
    mpContWidth     = new SKDCDoubleField(ContainerTypeData.CONTWIDTH_NAME);
    mpContHeight    = new SKDCDoubleField(ContainerTypeData.CONTHEIGHT_NAME);
    mpContWeight    = new SKDCDoubleField(ContainerTypeData.WEIGHT_NAME);
    mpMaxWeight     = new SKDCDoubleField(ContainerTypeData.MAXWEIGHT_NAME);

    addInput("Type:", mpContainerType);
    addInput("Container Length:", mpContLength);
    addInput("Container Width:", mpContWidth);
    addInput("Container Height:", mpContHeight);
    addInput("Container Weight:", mpContWeight);
    addInput("Maximum Weight:", mpMaxWeight);

    useAddButtons();
  }
  
  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new container to the database.
   */
  @Override
  protected void okButtonPressed()
  {
    boolean alreadyExists;

    try
    {
      alreadyExists = invtServ.containerExists(mpContainerType.getText().trim());
    }
    catch (DBException e2)
    {
      displayError("Unable to get Container data");
      return;
    }

    if(mzAdding)
    {
        if(mpContainerType.getText().trim().length() < 1)
        {
            displayError("Container Cannot Be Blank");
            mpContainerType.requestFocus();
            return;
        }
    }
    if (mzAdding && alreadyExists)
    {
      displayError("Container " + mpContainerType.getText().trim() + " already exists");
      mpContainerType.requestFocus();
      return;
    }

    if (!mzAdding && !alreadyExists)
    {
      displayError("Container " + mpContainerType.getText().trim() + " does not exist");
      mpContainerType.requestFocus();
    }

    // fill in Container data
    ContainerTypeData vpCTData;
    if (mzAdding)
    {
      vpCTData = Factory.create(ContainerTypeData.class);
    }
    else
    {
      try
      {
        vpCTData = invtServ.getContainer(mpContainerType.getText().trim());
      }
      catch (DBException e2)
      {
        displayError("Unable to get Container data");
        return;
      }
    }

    try
    {
      if (mzAdding)
      {
        vpCTData.setContainer(mpContainerType.getText().trim());
      }

      vpCTData.setContLength(mpContLength.getValue());
      vpCTData.setContWidth(mpContWidth.getValue());
      vpCTData.setContHeight(mpContHeight.getValue());
      vpCTData.setWeight(mpContWeight.getValue());
      vpCTData.setMaxWeight(mpMaxWeight.getValue());

      if (mzAdding)
      {
        invtServ.addContainer(vpCTData);
        this.changed();
        displayInfoAutoTimeOut("Container " + mpContainerType.getText().trim() + " added");
      }
      else
      {
        invtServ.updateContainer(vpCTData);
        this.changed();
        displayInfoAutoTimeOut("Container " + mpContainerType.getText().trim() + " updated");
      }
    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);
      if (mzAdding)
      {
        displayError("Error adding Container " + mpContainerType.getText().trim());
      }
      else
      {
        displayError("Error updating Container " + mpContainerType.getText().trim());
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
    setData(defaultContainerData);
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param ipCTData Container data to use in refreshing.
   */
  protected void setData(ContainerTypeData ipCTData)
  {
    mpContainerType.setText(ipCTData.getContainer());
    mpContLength.setValue(ipCTData.getContLength());
    mpContWidth.setValue(ipCTData.getContWidth());
    mpContHeight.setValue(ipCTData.getContHeight());
    mpContWeight.setValue(ipCTData.getWeight());
    mpMaxWeight.setValue(ipCTData.getMaxWeight());
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Method to set default container for creating
   *
   *  @param s Container type to be added.
   */
  public void setDefaultContainer(String s)
  {
    defaultContainerType = s;
  }


}