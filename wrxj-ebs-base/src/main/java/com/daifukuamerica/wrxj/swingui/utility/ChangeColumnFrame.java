package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

/**
 * Description:<BR>
 *    Sets up a frame to change asrs meta data columns (Add or Modify).
 *
 * @author       A.D.
 * @version      1.0
 *     Copyright (c) 2004<BR>
 *     Company:  SKDC Corporation
 */
public class ChangeColumnFrame extends DacInputFrame 
{
  private static final long serialVersionUID = 0L;

  private AsrsMetaDataData   mpOriginalData;
  private AsrsMetaDataData   mpModifiedData;
  private String             msChangeAction;

  private AsrsMetaDataData  mpMetaDataData;
  private AsrsMetaData      mpMetaData;
  private DBObject          dbobj = new DBObjectTL().getDBObject();

  // Define JComponents
  private SKDCTextField    txtMetaViewName;
  private SKDCTextField    txtMetaColumnDesc;
  private SKDCIntegerField txtMetaColumnOrder;
  private SKDCComboBox     metaColumnNameCombo;  
  private JCheckBox        isTranslationCheckBox;

  boolean mzAdding = true;

  /**
   * Constructor
   */
  public ChangeColumnFrame()
  {
    super("Meta-Data Change", "Meta-Data Information");

    mpMetaDataData = Factory.create(AsrsMetaDataData.class);
    mpMetaData     = Factory.create(AsrsMetaData.class);

    buildScreen();
  }

  /**
   * Set to Add or Modify
   * @param isChangeAction
   */
  public void setChangeAction(String isChangeAction)
  {
    msChangeAction = isChangeAction;
    if (isChangeAction.equals(SKDCGUIConstants.ADD_BTN))
    {
      mzAdding = true;
      setTitle("Add Meta-Data Column");
      useAddButtons();
    }
    else
    {
      mzAdding = false;
      setTitle("Modify Meta-Data Column");
      useModifyButtons();
    }
  }

  /**
   * Build the screen
   */
  private void buildScreen()
  {
    txtMetaViewName = new SKDCTextField(AsrsMetaDataData.DATAVIEWNAME_NAME);
    metaColumnNameCombo = new SKDCComboBox();
    txtMetaColumnDesc = new SKDCTextField(AsrsMetaDataData.FULLNAME_NAME);
    txtMetaColumnOrder = new SKDCIntegerField(0, 5);
    txtMetaColumnOrder.allowNegativeValues(true);
    isTranslationCheckBox = new JCheckBox("Yes");

    addInput("Data View Name:", txtMetaViewName);
    addInput("DB Column Name:", metaColumnNameCombo);
    addInput("Column Description:", txtMetaColumnDesc);
    addInput("Display Ordering:", txtMetaColumnOrder);
    addInput("Translation:", isTranslationCheckBox);

    useAddButtons();
  }

  /**
   *  Method to set the current data values as it appears in the SKDCTable row
   *  selection.
   *  
   *  @param metaData <code>AsrsMetaDataData</code> containing selected row data.
   */
  public void setCurrentData(AsrsMetaDataData ipCurrentData)
  {                                    // Save off original data.
    mpOriginalData = (AsrsMetaDataData)ipCurrentData.clone();
    fillMetaColumnNameCombo();

    txtMetaViewName.setText(mpOriginalData.getDataViewName());
    metaColumnNameCombo.setSelectedItem(mpOriginalData.getColumnName());
    txtMetaColumnDesc.setText(mpOriginalData.getFullName());
    txtMetaColumnOrder.setValue(mpOriginalData.getDisplayOrder());
    isTranslationCheckBox.setSelected((mpOriginalData.getIsTranslation().equals("Y")));
    disableFields();
  }

  /**
   * Do this on closing
   */
  @Override
  public void internalFrameClosing(javax.swing.event.InternalFrameEvent e)
  {
    mpModifiedData = (AsrsMetaDataData)mpMetaDataData.clone();
    if (mpModifiedData.getDataViewName().trim().length() == 0)
    {
      mpModifiedData.setDataViewName(mpOriginalData.getDataViewName());
    }
    super.internalFrameClosing(e);
  }

  /**
   * Do this on close
   */
  @Override
  public void internalFrameClosed(javax.swing.event.InternalFrameEvent e)
  {
    super.internalFrameClosed(e);
    firePropertyChange(msChangeAction, mpOriginalData, mpModifiedData);
    try { Thread.sleep(30); } catch(InterruptedException ie) {}
  }

  /**
   * Disable non-user-modifiable fields
   */
  private void disableFields()
  {
    txtMetaViewName.setEnabled(false);
    if (mzAdding)
    {
      metaColumnNameCombo.requestFocus();
    }
    else
    {
      metaColumnNameCombo.setEnabled(false);
      txtMetaColumnDesc.requestFocus();
    }
  }

  /**
   * Fill the combo box
   */
  private void fillMetaColumnNameCombo()
  {
    if (!dbobj.checkConnected())
    {
      try { dbobj.connect(); } catch(DBException e) {};
    }

    try
    {
      if (!mzAdding)
      {
        String[] displayColumns = mpMetaData.getOrderedColumns(mpOriginalData.getDataViewName(), false);    
        metaColumnNameCombo.setComboBoxData(displayColumns);
      }
      else
      {                                  // Only want those columns that can be added.
        String[] complementList = mpMetaData.getNonDisplayColumns(mpOriginalData.getDataViewName());
        if (complementList.length == 0)
          metaColumnNameCombo.setComboBoxData(new String[] { "No Columns" });
        else
          metaColumnNameCombo.setComboBoxData(complementList);
      }
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, exc.getMessage(), "Validation Error",
          JOptionPane.ERROR_MESSAGE);
    }

  }

  /**
   *  Method to execute the modify request.  This method does all necessary
   *  validations.
   */
  @Override
  protected void okButtonPressed()
  {
    mpMetaDataData.clear();
    mpMetaDataData.setDataViewName(txtMetaViewName.getText());

    if (metaColumnNameCombo.getText().trim().length() > 0)
    {
      mpMetaDataData.setColumnName((String)metaColumnNameCombo.getSelectedItem());
    }
    else
    {
      JOptionPane.showMessageDialog(null, "Column name may not be blank",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;                                    
    }
    mpMetaDataData.setFullName(txtMetaColumnDesc.getText());

    if (txtMetaColumnOrder.getValue() >= -1)
    {
      mpMetaDataData.setDisplayOrder(txtMetaColumnOrder.getValue());
    }
    else
    {
      JOptionPane.showMessageDialog(null, "Column Ordering cannot be less than -1",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    mpMetaDataData.setIsTranslation((isTranslationCheckBox.isSelected()) ? "Y" : "N");
    processChangeRequest();            // Either do the Add or Modify as requested.
    closeButtonPressed();
  }

  /**
   *  Default method to clear all input fields, and set combo-boxes to default
   *  values.
   */
  @Override
  protected void clearButtonPressed()
  {
    metaColumnNameCombo.setSelectedItem(mpOriginalData.getColumnName());
    txtMetaColumnDesc.setText(mpOriginalData.getFullName());
    txtMetaColumnOrder.setValue(mpOriginalData.getDisplayOrder());
    isTranslationCheckBox.setSelected((mpOriginalData.getIsTranslation().equals("Y")));

    if (mzAdding)
    {
      metaColumnNameCombo.requestFocus();
    }
    else
    {
      txtMetaColumnDesc.requestFocus();
    }
  }

  /**
   * Update the database
   */
  private void processChangeRequest()
  {
    TransactionToken tt = null;
    try
    {
      tt = dbobj.startTransaction();
      if (mzAdding)
      {
        mpMetaData.addElement(mpMetaDataData);
      }
      else
      {
        mpMetaDataData.setKey(AsrsMetaDataData.DATAVIEWNAME_NAME, txtMetaViewName.getText());
        mpMetaDataData.setKey(AsrsMetaDataData.COLUMNNAME_NAME, mpMetaDataData.getColumnName());
        mpMetaData.modifyElement(mpMetaDataData);
      }
      dbobj.commitTransaction(tt);
    }
    catch(DBException exc)
    {
      JOptionPane.showMessageDialog(null, "Database update failed..." + exc.getMessage(),
          "Change Error", JOptionPane.ERROR_MESSAGE);
    }
    finally
    {
      dbobj.endTransaction(tt);
    }
  }
}
