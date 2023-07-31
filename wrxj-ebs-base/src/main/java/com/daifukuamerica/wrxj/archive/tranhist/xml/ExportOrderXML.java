package com.daifukuamerica.wrxj.archive.tranhist.xml;

import com.daifukuamerica.wrxj.archive.Exporter;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Description:<BR>
 *   Class to handle XML output for Order Transaction History. This
 *   implementation is JDOM specific.
 *
 * @author      A.D.
 * @version     1.0
 * @since       03-Jul-03
 */
public class ExportOrderXML extends JDOMHelper implements Exporter
{
  private String documentName = "OrderTransaction";

  public ExportOrderXML()
  {
    super();
  }

  @Override
  public void writeData(List dataList, String isArchivePath)
  {
    Element rootElement = buildTranRootElement(documentName,
                                               DBConstants.ORDER_TRAN);
    Document doc = new Document(rootElement);

    for(int idx = 0; idx < dataList.size(); idx++)
    {
      Map columnMap = (Map)dataList.get(idx);
      Element tranTypeElement = buildTranTypeElement(columnMap);
      Element dateElement = buildTranDateElement(columnMap);
      Element orderElement = buildOrderElement(columnMap);
      Element stationElement = buildToStationElement(columnMap);

      rootElement.addContent(tranTypeElement);
      tranTypeElement.addContent(dateElement);
      tranTypeElement.addContent(orderElement);
      tranTypeElement.addContent(stationElement);
    }

    createFile(documentName, doc, isArchivePath);
  }

  private Element buildOrderElement(Map columnMap)
  {
    Element order = new Element(TransactionHistoryData.ORDERID_NAME);
    String sOrder = DBHelper.getStringField(columnMap, TransactionHistoryData.ORDERID_NAME);
    order.setText(sOrder);

    int iOrderType = DBHelper.getIntegerField(columnMap, TransactionHistoryData.ORDERTYPE_NAME);
    try
    {
      String sOrderType = DBTrans.getStringValue(TransactionHistoryData.ORDERTYPE_NAME,
                                                 iOrderType);
      order.setAttribute(TransactionHistoryData.ORDERTYPE_NAME, sOrderType);
    }
    catch(NoSuchFieldException e)
    {
//      logMessage(e.getMessage());
    }

    return(order);
  }

  private Element buildToStationElement(Map columnMap)
  {
    Element station = new Element(TransactionHistoryData.TOSTATION_NAME);
    String sStation = DBHelper.getStringField(columnMap, TransactionHistoryData.TOSTATION_NAME);
    station.setText(sStation);

    return(station);
  }
}
