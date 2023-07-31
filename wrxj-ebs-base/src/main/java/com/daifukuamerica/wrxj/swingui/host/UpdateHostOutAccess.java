package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostOutAccessData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import javax.swing.event.InternalFrameEvent;

@SuppressWarnings("serial")
public class UpdateHostOutAccess extends DacInputFrame
{
  protected String msHostName;
  protected String msMsgId;
  protected int    mnEnabled;

  protected SKDCTextField    mpTxtHostName;
  protected SKDCTextField    mpTxtMsgId;
  protected SKDCTranComboBox mpTCbxEnabled;
  
  protected String  msErrorTitle = "";

  protected StandardConfigurationServer mpConfigServer;
  protected HostOutAccessData defaultHostOutAccessData;

  /**
   *  Create load line host screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateHostOutAccess(String isTitle)
  {
    super(isTitle, "Host Out Access Detail Information");
    try
    {
      initialization();
      buildScreen();
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
    }
    catch (Exception e)
    {
      logger.logException(e);
      displayError(e.getMessage());
    }
  }

  /**
   *  Method to set screen for adding.
   *
   */
  public void setAdd(String isHost)
  {
    msErrorTitle = "Add Failure";
    msHostName = isHost;
    msMsgId = "";
    mnEnabled = DBConstants.YES;
    useAddButtons();
    mpTxtHostName.setEnabled(true);
    mpTxtMsgId.setEnabled(true);
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
    
    setData(defaultHostOutAccessData);
  }

  /**
   * Method to initialize global variables
   */
  private void initialization()
  {
    mpConfigServer = Factory.create(StandardConfigurationServer.class);
    defaultHostOutAccessData = Factory.create(HostOutAccessData.class);
  }

  /**
   *  Method to initialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  protected void buildScreen() throws Exception
  {
    mpTxtHostName = new SKDCTextField(HostOutAccessData.HOSTNAME_NAME);
    mpTxtMsgId = new SKDCTextField(HostOutAccessData.MESSAGEIDENTIFIER_NAME);
    mpTCbxEnabled = new SKDCTranComboBox(HostOutAccessData.ENABLED_NAME, false);
    
    msErrorTitle = "Add Failure";
    mpTxtHostName.setEnabled(false);
    mpTxtMsgId.setEnabled(false);
    
    addInput("Host Name:", mpTxtHostName);
    addInput("Message Identifier:", mpTxtMsgId);
    addInput("Enabled:", mpTCbxEnabled);
    
    useAddButtons();
  }

  /**
   *  Action method to handle Clear button Mouse Event.
   */
  @Override
  public void clearButtonPressed()
  {
    setData(defaultHostOutAccessData);
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param data Load line host data to use in refreshing.
   */
  protected void setData(HostOutAccessData data)
  {
    try
    {
      mpTxtHostName.setText(data.getHostName());
      mpTxtMsgId.setText(data.getMessageIdentifier());
      mpTCbxEnabled.setSelectedElement(data.getEnabled());
    }
    catch( NoSuchFieldException nsfe )
    {
      logger.logException( nsfe );
      displayError( nsfe.getMessage(), msErrorTitle );
    }
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
   *  then adds a new load line host to the database.
   */
  @Override
  protected void okButtonPressed()
  {
    try
    {
      // Get data from screen fields
      msHostName = mpTxtHostName.getText();
      msMsgId = mpTxtMsgId.getText();
      mnEnabled = mpTCbxEnabled.getIntegerValue();
      
      mpConfigServer.addHostOutAccess(msHostName, msMsgId, mnEnabled);
      displayInfoAutoTimeOut("Record (" + msHostName + "," + msMsgId + ") has been added.");
      mpTxtMsgId.setText("");
      mpTxtMsgId.requestFocus();
    }
    catch( NoSuchFieldException nsfe )
    {
      logger.logException( nsfe );
      displayError( nsfe.getMessage(), msErrorTitle );
    }
    catch( DBException dbe )
    {
      logger.logException( dbe );
      displayError( dbe.getMessage(), msErrorTitle );
    }
  }

  /**
   * Clean up
   */
  @Override
  public void cleanUpOnClose()
  {
    mpConfigServer.cleanUp();
    super.cleanUpOnClose(); 
  }
}
