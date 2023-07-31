package com.daifukuamerica.wrxj.swingui.purchaseorder;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCUserData;

/**
 * <B>Description:</B> Abstract base class for single- and multi-line 
 * expected receipts or purchase orders or whatever the kids call them nowadays.
 * <BR>
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2008 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public abstract class AbstractPOFrame extends DacInputFrame
{
  protected PurchaseOrderHeaderData mpPOHData;

  protected SKDCTextField mpOrderID;

  protected boolean mzAdding = false;
  protected boolean mzDisplayOnly = false;
  
  protected SKDCScreenPermissions ePerms;
  protected SKDCUserData userData;

  /**
   * @param isFrameTitle
   * @param isInputTitle
   */
  public AbstractPOFrame(String isFrameTitle, String isInputTitle)
  {
    super(isFrameTitle, isInputTitle);
    
    mpOrderID = new SKDCTextField(PurchaseOrderHeaderData.ORDERID_NAME);

    userData = new SKDCUserData();
    ePerms = userData.getOptionPermissionsByClass(AbstractPOFrame.class);
  }
  
  /**
   * Create an "ER" order ID
   * @return
   */
  protected String createOrderIDByDateTime()
  {
    return DBHelper.createOrderIDByDateTime("ER");
  }

  protected abstract void buildScreen();
  protected abstract void correctFieldEnabledValues();
  protected abstract void setAddMode();
  protected abstract void setDisplayMode(PurchaseOrderHeaderData ipPOHData);
  protected abstract void setModifyMode(PurchaseOrderHeaderData ipPOHData);
}
