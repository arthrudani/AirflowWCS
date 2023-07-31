package com.daifukuamerica.wrxj.swingui.purchaseorder;

import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class POHLSearch
{
  public PurchaseOrderHeaderData podata = Factory.create(PurchaseOrderHeaderData.class);
  public PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);

  public POHLSearch()
  {
    podata.setOrderStatus(SKDCConstants.ALL_INT);
  }

  public void clear()
  {
    podata.clear();
    poldata.clear();
    podata.setOrderStatus(SKDCConstants.ALL_INT);
  }
}
