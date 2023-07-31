package com.daifukuamerica.wrxj.swingui.port;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPortServer;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating ports.
 *
 * @author avt
 * @version 1.0
 */
public class UpdatePort extends DacInputFrame
{
  private static final long serialVersionUID = 0L;
  
  StringBuffer portName = new StringBuffer("");

  StandardPortServer portServ = Factory.create(StandardPortServer.class);
  PortData defaultPortData = Factory.create(PortData.class);

  SKDCTextField deviceText;
  SKDCTranComboBox direction;
  SKDCTextField portNameText;
  SKDCTextField serverText;
  SKDCTranComboBox commMode;
  SKDCIntegerField lastSeq;
  SKDCTextField socketText;
  SKDCIntegerField mpRetryInterval;
  SKDCIntegerField mpSKeepAliveInterval;
  SKDCIntegerField mpRKeepAliveInterval;
  
  boolean mzAdding = true;

 /**
  *  Create port screen class.
  *
  *  @param isTitle Title to be displayed.
  */
  public UpdatePort(String isTitle)
  {
    super(isTitle, "Port Information");
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
   *  Create default port screen class.
   *
   */
  public UpdatePort()
  {
    this("");
  }

  /**
   *  Method to set screen for modifing.
   *
   *  @param port Port to be modified.
   */
  public void setModify(String port)
  {
    portName.setLength(0);
    portName.append(port);
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

    if (!mzAdding) // we are modifying
    {
      portNameText.setText(portName.toString());

      try
      {
        defaultPortData = portServ.getPort(portName.toString());
      }
      catch (DBException e2)
      {
        displayError("Unable to get Port data");
        return;
      }

      portNameText.setEnabled(false);
      deviceText.requestFocus();

      this.setTimeout(90);
    }
    setData(defaultPortData);
  }

 /**
  *  Method to intialize screen components. This adds the components to the
  *  screen and adds listeners as needed.
  *
  *  @exception Exception
  */
  void jbInit() throws Exception
  {
    portNameText = new SKDCTextField(PortData.PORTNAME_NAME);
    deviceText = new SKDCTextField(PortData.DEVICEID_NAME);
    serverText = new SKDCTextField(PortData.SERVERNAME_NAME);
    socketText = new SKDCTextField(PortData.SOCKETNUMBER_NAME);
    lastSeq = new SKDCIntegerField(4);
    mpRetryInterval = new SKDCIntegerField(6);
    mpSKeepAliveInterval = new SKDCIntegerField(6);
    mpRKeepAliveInterval = new SKDCIntegerField(6);

    commMode = new SKDCTranComboBox();
    
    try
    {
      direction = new SKDCTranComboBox(PortData.DIRECTION_NAME);
      commMode = new SKDCTranComboBox(PortData.COMMUNICATIONMODE_NAME);
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }
    lastSeq.setEditable(false);

    addInput("Name:",               portNameText); 
    addInput("Device ID:",          deviceText);
    addInput("Direction:",          direction);
    addInput("Server Name:",        serverText); 
    addInput("Socket Number:",      socketText);
    addInput("Communication Mode:", commMode);
    addInput("Retry Interval (ms)", mpRetryInterval);
    addInput("WRx->Device Keep-Alive Interval (ms)", mpSKeepAliveInterval);
    addInput("Device->Wrx Keep-Alive Interval (ms)", mpRKeepAliveInterval);
    addInput("Last Sequence:",      lastSeq);
    
    useAddButtons();
  }

  /**
   *  Method to clean up as needed at closing.
   *
   */
  @Override
  public void cleanUpOnClose()
  {
    portServ.cleanUp();
  }

  /**
   *  Action method to handle Clear button..
   *
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(defaultPortData);
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param data Port data to use in refreshing.
   */
  void setData(PortData data)
  {
    portNameText.setText(data.getPortName());
    serverText.setText(data.getServerName());
    socketText.setText(data.getSocketNumber());
    try
    {
      direction.setSelectedElement(data.getDirection());
      commMode.setSelectedElement(data.getCommunicationMode());
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      displayError("No Such Field: " + e2);
    }

    lastSeq.setValue(data.getLastSequence());
    deviceText.setText(data.getDeviceID());
    mpRetryInterval.setValue(data.getRetryInterval());
    mpSKeepAliveInterval.setValue(data.getSndKeepAliveInterval());
    mpRKeepAliveInterval.setValue(data.getRcvKeepAliveInterval());
  }

  /**
   *  Action method to handle Close button.
   *
   */
  @Override
  public void closeButtonPressed()
  {
    close();
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new port to the database.
   *
   */
  @Override
  public void okButtonPressed()
  {
    boolean alreadyExists;
    try
    {
      alreadyExists = portServ.exists(portNameText.getText().trim());
    }
    catch (DBException e2)
    {
      displayError("Unable to get Port data");
      return;
    }

    if (mzAdding && alreadyExists)
    {
      displayError("Port " + portNameText.getText().trim() + " already exists");
    }

    if (!mzAdding && !alreadyExists)
    {
      displayError("Port " + portNameText.getText().trim() + " does not exist");
    }

    PortData vpPortData = Factory.create(PortData.class);
    // fill in Port data

    vpPortData.setPortName(portNameText.getText().trim());
    if (mzAdding)
    {
      if (vpPortData.getPortName().length() <= 0)  // required
      {
        displayError("Port name is required");
        return;
      }
    }

    if (deviceText.getText().trim().length() <= 0)  // required
    {
      displayError("Device ID is required");
      return;
    }

//    if(!devServ.exists(deviceText.getText().trim()))
//    {
//        displayError("Device '" + deviceText.getText().trim() +
//                     "' Does Not Exist");
//        return;
//    }

    if (serverText.getText().length() <= 0)  // required
    {
      displayError("Server is required");
      return;
    }

    if (socketText.getText().length() <= 0)  // required
    {
      displayError("Socket is required");
      return;
    }

    if (mpRetryInterval.getValue() < PortData.MINIMUM_INTERVAL)
    {
      displayError("Invalid Retry Interval (minimum " + PortData.MINIMUM_INTERVAL + ")");
      return;
    }

    if (mpSKeepAliveInterval.getValue() < PortData.MINIMUM_INTERVAL &&
        mpSKeepAliveInterval.getValue() > 0)
    {
      displayError("Invalid Keep-Alive Interval (minimum "
          + PortData.MINIMUM_INTERVAL + " or ZERO)");
      mpSKeepAliveInterval.requestFocus();
      return;
    }

    if (mpRKeepAliveInterval.getValue() < PortData.MINIMUM_INTERVAL &&
        mpRKeepAliveInterval.getValue() > 0)
    {
      displayError("Invalid Keep-Alive Interval (minimum "
          + PortData.MINIMUM_INTERVAL + " or ZERO)");
      mpRKeepAliveInterval.requestFocus();
      return;
    }
    
    vpPortData.setDeviceID(deviceText.getText().trim());
    vpPortData.setServerName(serverText.getText().trim());
    vpPortData.setSocketNumber(socketText.getText().trim());
    vpPortData.setRetryInterval(mpRetryInterval.getValue());
    vpPortData.setSndKeepAliveInterval(mpSKeepAliveInterval.getValue());
    vpPortData.setRcvKeepAliveInterval(mpRKeepAliveInterval.getValue());
    vpPortData.setLastSequence(lastSeq.getValue());

    try
    {
      vpPortData.setDirection(direction.getIntegerValue());
      vpPortData.setCommunicationMode(commMode.getIntegerValue());
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
        portServ.addPort(vpPortData);
        changed();
        displayInfoAutoTimeOut("Port " + portNameText.getText().trim() + " added");
      }
      else
      {
        portServ.updatePort(vpPortData);
        changed();
        displayInfoAutoTimeOut("Port " + portNameText.getText() + " updated");
      }
    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);
      if (mzAdding)
      {
        displayError("Error adding port " + portNameText.getText());
      }
      else
      {
        displayError("Error updating port " + portNameText.getText());
      }
    }
    if (!mzAdding)
    {
      close();
    }
  }
}
