package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfig;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;

import java.awt.Toolkit;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class UpdateControllerConfig extends DacInputFrame
{
  private static final long serialVersionUID = 0L;
  private int mnValueLength = DBInfo.getFieldLength(SysConfigData.PARAMETERVALUE_NAME);
  private int mnDescLength = DBInfo.getFieldLength(ControllerConfigData.PROPERTYDESC_NAME);

  protected SKDCTextField mpTxtController;
  protected SKDCTextField mpTxtName;
  protected JTextArea     mpTxtValueArea;
  protected JTextArea     mpTextDescArea;
  protected SKDCCheckBox  mpChkBxEnabled;
  protected SKDCCheckBox  mpChkBxChangeAllowed;
  
  protected boolean       mzSuperUser = false;
  protected boolean       mzAdding = true;
  protected DBObject      mpDBObject = null;
  
  protected ControllerConfig     mpCC = Factory.create(ControllerConfig.class);
  protected ControllerConfigData mpCCD = Factory.create(ControllerConfigData.class);

  public UpdateControllerConfig()
  {
    this("");
  }
  
  public UpdateControllerConfig(String isFrameTitle)
  {
    this(isFrameTitle, isFrameTitle);
  }
  
  public UpdateControllerConfig(String isFrameTitle, String isInputTitle)
  {
    super(isFrameTitle, isInputTitle);
    setResizable(true);
    ensureDBConnection();
    jbInit();
    pack();
  }

  /**
   *  Method to set screen for modifying.
   *
   *  @param ipCCD <code>ControllerConfigData</code> Object of ControllerCOnfig to be modified
   *  @param izSuperUser <code>boolean</code> user's role
   */
  public void setModify(ControllerConfigData ipCCD, boolean izSuperUser)
  {
    mzSuperUser = izSuperUser;
    mpCCD = ipCCD;
    setData(mpCCD);
    
    useModifyButtons();
    mzAdding = false;
  }

  @Override
  public void internalFrameActivated(InternalFrameEvent e)
  {
    super.internalFrameActivated(e);
    
    setData(mpCCD);
  }

  /**
   * Method to get data from screen.
   */
  protected ControllerConfigData getDataFromInputFields()
  {
      // get data from screen fields
    ControllerConfigData vpCCD = Factory.create(ControllerConfigData.class);
    vpCCD.setController(mpTxtController.getText().trim());
    vpCCD.setPropertyName(mpTxtName.getText().trim());
    vpCCD.setPropertyValue(mpTxtValueArea.getText().trim());
    vpCCD.setPropertyDesc(mpTextDescArea.getText().trim());
    vpCCD.setEnabled(mpChkBxEnabled.isSelectedYesNo());
    vpCCD.setScreenChangeAllowed(mpChkBxChangeAllowed.isSelectedYesNo());
    return vpCCD;
  }

  /**
   * Method for the Submit button.
   */
  @Override
  protected void okButtonPressed()
  {
      // get data from screen fields
    ControllerConfigData vpCCD = getDataFromInputFields();
    
    try
    {
      updateDatabase(vpCCD);
      changed();
      
      if (mzAdding)
      {
        displayInfoAutoTimeOut("ControllerConfig record added");
        clearButtonPressed();
      }
      else
      {
        displayInfoAutoTimeOut("ControllerCOnfig record modified");
        close();
      }
    }
    catch(DBException ex)
    {
      displayError(ex.getMessage(), "Database Error");
      setupInputFields();
    }
  }

  /**
   * Method for the Submit button.
   */
  protected void updateDatabase(ControllerConfigData ipCCD) throws DBException
  {
      // set keys
    ipCCD.setKey(ControllerConfigData.CONTROLLER_NAME, ipCCD.getController());
    ipCCD.setKey(ControllerConfigData.PROPERTYNAME_NAME, ipCCD.getPropertyName());

    TransactionToken vpTT = null;
    
    vpTT = mpDBObject.startTransaction();

    if (mzAdding)
    {
      mpCC.addElement(ipCCD);
    }
    else
    {
      mpCC.modifyElement(ipCCD);
    }
    
    mpDBObject.commitTransaction(vpTT);
  }

  /**
   * Method for the Clear button.
   */  
  @Override
  protected void clearButtonPressed()
  {
    setData(mpCCD);
  }
  
  /**
   * Build the screen
   */
  protected void jbInit()
  {
    mpTxtController = new SKDCTextField(ControllerConfigData.CONTROLLER_NAME);
    mpTxtName = new SKDCTextField(ControllerConfigData.PROPERTYNAME_NAME);
    mpTxtValueArea = new JTextArea(2, 50);
    mpTxtValueArea.setLineWrap(true);
    mpTxtValueArea.setDocument(new PlainDocument()
    {
      @Override
      public void insertString(int inOffset, String isInputStr, AttributeSet ipAttributes)
             throws BadLocationException
      {
        if (isInputStr.equals("\t") || isInputStr.equals("\n"))
        {
          mpTextDescArea.requestFocusInWindow();
        }
        else if (getLength() < mnValueLength)
        {
          super.insertString(inOffset, isInputStr, ipAttributes);
        }
        else
        {
          Toolkit.getDefaultToolkit().beep();
        }
      }
    });
    mpTextDescArea = new JTextArea(4, 50);
    mpTextDescArea.setLineWrap(true);
    mpTextDescArea.setDocument(new PlainDocument()
    {
      @Override
      public void insertString(int inOffset, String isInputStr, AttributeSet ipAttributes)
             throws BadLocationException
      {
        if (getLength() < mnDescLength)
        {
          super.insertString(inOffset, isInputStr, ipAttributes);
        }
        else
        {
          Toolkit.getDefaultToolkit().beep();
        }
      }
    });
    mpChkBxEnabled = new SKDCCheckBox();
    mpChkBxChangeAllowed = new SKDCCheckBox();
    
    buildDataPanel();
  }
  
  /**
   * Build the screen
   */
  protected void buildDataPanel()
  {
    /*
     * Build the data panel
     */
    addInput("Controller:", mpTxtController);
    addInput("Property Name:", mpTxtName);
    addInput("Property Value:", new JScrollPane(mpTxtValueArea));
    addInput("Property Description:", new JScrollPane(mpTextDescArea));
    addInput("Enabled:", mpChkBxEnabled);
    addInput("Screen Change Allowed:", mpChkBxChangeAllowed);
    
    useAddButtons();
  }
   
  /**
   *  Method simply ensures database connectivity.  This is useful
   */
  protected void ensureDBConnection()
  {
    if (mpDBObject == null || !mpDBObject.checkConnected())
    {
      mpDBObject = new DBObjectTL().getDBObject();
      try { mpDBObject.connect(); }
      catch(DBException e) { return; }
    }
  }
  
  /**
   * Method to set data
   * @param ipCCD <code>ControllerConfigData</code> of object
   */
  protected void setData(ControllerConfigData ipCCD)
  {
    mpTxtController.setText(ipCCD.getController());
    mpTxtName.setText(ipCCD.getPropertyName());
    mpTxtValueArea.setText(ipCCD.getPropertyValue());
    mpTextDescArea.setText(ipCCD.getPropertyDesc());
    mpChkBxEnabled.setSelected(ipCCD.getEnabled() == DBConstants.YES);
    mpChkBxChangeAllowed.setSelected(ipCCD.getScreenChangeAllowed() == DBConstants.YES);
    
    setupInputFields();
  }
  
  protected void setupInputFields()
  {
    if (mzAdding == true)
    {
      mpTxtController.requestFocusInWindow();
      mpTxtController.selectAll();
    }
    else
    {
      mpTxtController.setEditable(false);
      mpTxtName.setEditable(false);
      mpTxtValueArea.requestFocusInWindow();
      mpTxtValueArea.selectAll();
    }
  }
}
