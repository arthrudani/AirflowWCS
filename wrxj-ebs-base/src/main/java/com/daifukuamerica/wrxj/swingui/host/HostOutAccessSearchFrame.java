package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dbadapter.data.HostOutAccessData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;

/**
 * Description:<BR>
 *    Host Out Access Search Frame
 *
 * @author       Y. Kang
 * @version      1.0
 * <BR>Created:  30-Mar-2009<BR>
 *     Copyright (c) 2009<BR>
 *     Company:  Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class HostOutAccessSearchFrame extends DacInputFrame
{
  private SKDCTextField mpHostNameText;
  private SKDCTextField mpMsgIdText;
  private SKDCTranComboBox mpEnabledComboBox;

  /**
   *  Create host out access search frame.
   *
   */
  public HostOutAccessSearchFrame()
  {
    super("Host Out Detail Search", "Host Out Detail Search Criteria");
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  private void jbInit() throws Exception
  {
    mpHostNameText = new SKDCTextField(HostOutAccessData.HOSTNAME_NAME);
    mpMsgIdText   = new SKDCTextField(HostOutAccessData.MESSAGEIDENTIFIER_NAME);
    try
    {
      mpEnabledComboBox = new SKDCTranComboBox(HostOutAccessData.ENABLED_NAME, true);
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }

    addInput("Host Name:",      mpHostNameText);
    addInput("Msg Identifier:", mpMsgIdText);
    addInput("Enabled:",        mpEnabledComboBox);
    
    useSearchButtons();
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
   *  Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpHostNameText.setText("");
    mpMsgIdText.setText("");
  }

  /**
   *  Action method to handle Search button. Method fires a property change
   *  event so parent frame can refresh its display.
   */
  @Override
  protected void okButtonPressed()
  {
    this.changed();
  }

  /**
   *  Method to get the entered search criteria as a ColumnObject.
   *
   *  @return ColumnObject containing criteria to use in search
   */
  public HostOutAccessData getSearchData()
  {
    HostOutAccessData vpHOAData = Factory.create(HostOutAccessData.class);
    if (mpHostNameText.getText().trim().length() > 0)
    {
      vpHOAData.setKey(HostOutAccessData.HOSTNAME_NAME, mpHostNameText.getText(), KeyObject.LIKE);
    }

    if (mpMsgIdText.getText().trim().length() > 0)
    {
      vpHOAData.setKey(HostOutAccessData.MESSAGEIDENTIFIER_NAME, mpMsgIdText.getText(), KeyObject.LIKE);
    }

    try
    {
      vpHOAData.setKey(HostOutAccessData.ENABLED_NAME, mpEnabledComboBox.getIntegerObject());
    }
    catch(NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      vpHOAData = null;
    }

    return(vpHOAData);
  }
}
