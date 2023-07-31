package com.daifukuamerica.wrxj.archive.tranhist.xml;

import com.daifukuamerica.wrxj.archive.Exporter;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Description:<BR>
 *   Class to handle XML output for Inventory Transaction History. This
 *   implementation is JDOM specific.
 *
 * @author       A.D.
 * @version      1.0
 * @since        07-Jul-03
 */
public class ExportInventoryXML extends JDOMHelper implements Exporter
{
  private String documentName = "InventoryTransaction";

  public ExportInventoryXML()
  {
    super();
  }

  @Override
  public void writeData(List dataList, String isArchivePath)
  {
    Element rootElement = buildTranRootElement(documentName,
                                               DBConstants.INVENTORY_TRAN);
    Document doc = new Document(rootElement);

    for(int idx = 0; idx < dataList.size(); idx++)
    {
      Map columnMap = (Map)dataList.get(idx);
      Element tranTypeElement = buildTranTypeElement(columnMap);
      Element dateElement = buildTranDateElement(columnMap);
      Element locationElement = buildLocationElement(columnMap);
      Element loadElement = buildLoadElement(columnMap);
      Element toLoadElement = buildToLoadElement(columnMap);
      Element orderElement = buildOrderElement(columnMap);
      Element itemElement = buildItemElement(columnMap);

      rootElement.addContent(tranTypeElement);
      tranTypeElement.addContent(dateElement);
      tranTypeElement.addContent(locationElement);
      tranTypeElement.addContent(orderElement);
      tranTypeElement.addContent(loadElement);
      tranTypeElement.addContent(itemElement);
      tranTypeElement.addContent(toLoadElement);
    }

    createFile(documentName, doc, isArchivePath);
  }

  private Element buildLocationElement(Map columnMap)
  {
    Element location = new Element(TransactionHistoryData.LOCATION_NAME);
    String sLocation = DBHelper.getStringField(columnMap, TransactionHistoryData.LOCATION_NAME);
    location.setText(sLocation);

    return(location);
  }

  private Element buildLoadElement(Map columnMap)
  {
    Element loadID = new Element(TransactionHistoryData.LOADID_NAME);
    String sLoadID = DBHelper.getStringField(columnMap, TransactionHistoryData.LOADID_NAME);
    loadID.setText(sLoadID);

    return(loadID);
  }

  private Element buildToLoadElement(Map columnMap)
  {
    Element toLoad = new Element(TransactionHistoryData.TOLOAD_NAME);
    String sToLoadID = DBHelper.getStringField(columnMap, TransactionHistoryData.TOLOAD_NAME);
    toLoad.setText(sToLoadID);

    return(toLoad);
  }

  private Element buildOrderElement(Map columnMap)
  {
    Element order = new Element(TransactionHistoryData.ORDERID_NAME);
    String sOrder = DBHelper.getStringField(columnMap, TransactionHistoryData.ORDERID_NAME);
    order.setText(sOrder);

    return(order);
  }

  private Element buildItemElement(Map columnMap)
  {
    Element item = new Element(TransactionHistoryData.ITEM_NAME);
    String sItem = DBHelper.getStringField(columnMap, TransactionHistoryData.ITEM_NAME);
    item.setText(sItem);

    String sLot = DBHelper.getStringField(columnMap, TransactionHistoryData.LOT_NAME);
    item.setAttribute(TransactionHistoryData.LOT_NAME, sLot);

    String currQty = DBHelper.getStringField(columnMap,
                                        TransactionHistoryData.CURRENTQUANTITY_NAME);
    item.setAttribute(TransactionHistoryData.CURRENTQUANTITY_NAME, currQty);

    String adjQty = DBHelper.getStringField(columnMap,
                                        TransactionHistoryData.ADJUSTEDQUANTITY_NAME);
    item.setAttribute(TransactionHistoryData.ADJUSTEDQUANTITY_NAME, adjQty);

    String pickQty = DBHelper.getStringField(columnMap,
                                        TransactionHistoryData.PICKQUANTITY_NAME);
    item.setAttribute(TransactionHistoryData.PICKQUANTITY_NAME, pickQty);

    return(item);
  }
}
