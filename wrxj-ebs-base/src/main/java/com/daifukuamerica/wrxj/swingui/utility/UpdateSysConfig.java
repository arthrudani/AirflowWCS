package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
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

public class UpdateSysConfig extends DacInputFrame
{
  private static final long serialVersionUID = 0L;
  private int mnDescLength = DBInfo.getFieldLength(SysConfigData.DESCRIPTION_NAME);
  private int mnValueLength = DBInfo.getFieldLength(SysConfigData.PARAMETERVALUE_NAME);

  protected SKDCTextField mpTxtGroup;
  protected SKDCTextField mpTxtName;
  protected JTextArea     mpTxtValueArea;
  protected JTextArea     mpTxtDescArea;
  protected SKDCCheckBox  mpChkBxEnabled;
  protected SKDCCheckBox  mpChkBxChangeAllowed;
  
  protected boolean       mzSuperUser = false;
  protected boolean       mzAdding = true;
  protected DBObject      mpDBObject = null;
  
  protected SysConfig     mpSC = Factory.create(SysConfig.class);
  protected SysConfigData mpSCD = Factory.create(SysConfigData.class);
  protected SysConfigData mpCurSCD;

  public UpdateSysConfig()
  {
    this("");
  }
  
  public UpdateSysConfig(String isFrameTitle)
  {
    this(isFrameTitle, isFrameTitle);
  }
  
  public UpdateSysConfig(String isFrameTitle, String isInputTitle)
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
   *  @param isStationName Name of Default Station to be modified
   *  @param isIPAddress IP Address for Default Station being modified
   */
  public void setModify(SysConfigData ipSCD, boolean izSuperUser)
  {
    mzSuperUser = izSuperUser;
    mpSCD = ipSCD;
    
    useModifyButtons();
    mzAdding = false;
  }

  @Override
  public void internalFrameActivated(InternalFrameEvent e)
  {
    super.internalFrameActivated(e);
    
    setData(mpSCD);
    mpCurSCD = (SysConfigData)mpSCD.clone();
  }

  /**
   * Method to get data from screen.
   */
  protected SysConfigData getDataFromInputFields()
  {
    SysConfigData vpSCD = Factory.create(SysConfigData.class);
    vpSCD.setGroup(mpTxtGroup.getText().trim());
    vpSCD.setParameterName(mpTxtName.getText().trim());
    vpSCD.setParameterValue(mpTxtValueArea.getText().trim());
    vpSCD.setDescription(mpTxtDescArea.getText().trim());
    vpSCD.setEnabled(mpChkBxEnabled.isSelectedYesNo());
    vpSCD.setScreenChangeAllowed(mpChkBxChangeAllowed.isSelectedYesNo());
    return vpSCD;
  }

  /**
   * Method for the Submit button.
   */
  @Override
  protected void okButtonPressed()
  {
    SysConfigData vpSCD = getDataFromInputFields();
    
    try
    {
      updateDatabase(vpSCD);
      changed();
      
      if (mzAdding)
      {
        displayInfoAutoTimeOut("SysConfig record added");
        clearButtonPressed();
      }
      else
      {
        displayInfoAutoTimeOut("SysConfig record modified");
        close();
      }
    }
    catch(DBException ex)
    {
      displayError(ex.getMessage(), "Database Error");
      mpTxtGroup.requestFocusInWindow();
      mpTxtGroup.selectAll();
    }
  }

  /**
   * Method for update database records.
   * @param ipSCD <code>SysConfigData</code> Object of SysConfig record 
   * to be added/updated.
   */
  protected void updateDatabase(SysConfigData ipSCD) throws DBException
  {
      // set keys
    ipSCD.setKey(SysConfigData.GROUP_NAME, mpCurSCD.getGroup());
    ipSCD.setKey(SysConfigData.PARAMETERNAME_NAME, mpCurSCD.getParameterName());

    TransactionToken vpTT = null;
    
    vpTT = mpDBObject.startTransaction();
    if (mzAdding)
    {
      mpSC.addElement(ipSCD);
    }
    else
    {
      mpSC.modifyElement(ipSCD);
    }
    mpDBObject.commitTransaction(vpTT);
  }

  /**
   * Method for the Clear button.
   */  
  @Override
  protected void clearButtonPressed()
  {
    setData(mpSCD);
    if (mzSuperUser == false)
    {
      
    }
  }
  
  /**
   * Build the screen
   */
  protected void jbInit()
  {
    mpTxtGroup = new SKDCTextField(50);
    mpTxtName = new SKDCTextField(50);
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
          mpTxtDescArea.requestFocusInWindow();
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

    mpTxtDescArea = new JTextArea(4, 50);
    mpTxtDescArea.setLineWrap(true);
    mpTxtDescArea.setDocument(new PlainDocument()
    {
      @Override
      public void insertString(int inOffset, String isInputStr, AttributeSet ipAttributes)
             throws BadLocationException
      {
        if (isInputStr.equals("\t") || isInputStr.equals("\n"))
        {
          mpChkBxEnabled.requestFocusInWindow();
        }
        else if (getLength() < mnDescLength)
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
    addInput("Group:", mpTxtGroup);
    addInput("Parameter Name:", mpTxtName);
    addInput("Parameter Value:",  new JScrollPane(mpTxtValueArea));
    addInput("Parameter Description:", new JScrollPane(mpTxtDescArea));
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
   * @param ipSCD <code>SysConfigData</code> of object
   */
  protected void setData(SysConfigData ipSCD)
  {
    mpTxtGroup.setText(ipSCD.getGroup());
    mpTxtName.setText(ipSCD.getParameterName());
    mpTxtValueArea.setText(ipSCD.getParameterValue());
    mpTxtDescArea.setText(ipSCD.getDescription());
    mpChkBxEnabled.setSelected(ipSCD.getEnabled() == DBConstants.YES);
    mpChkBxChangeAllowed.setSelected(ipSCD.getScreenChangeAllowed() == DBConstants.YES);

    setupInputFields();
  }
  
  /**
   * Method to enable/disable input fields base on the user's role and action.
   */
  protected void setupInputFields()
  {
    if (mzAdding == true)
    {
      mpTxtGroup.requestFocusInWindow();
      mpTxtGroup.selectAll();
    }
    else
    {
      mpTxtGroup.setEditable(false);
      mpTxtName.setEditable(false);
      mpTxtValueArea.requestFocusInWindow();
    }
  }
}
