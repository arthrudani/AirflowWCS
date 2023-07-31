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
 *   Class to handle XML output for Load Transaction History. This
 *   implementation is JDOM specific.
 *
 * @author     A.D.
 * @version    1.0
 * @since      03-Jul-03
 */
public class ExportLoadXML extends JDOMHelper implements Exporter
{
  private String documentName = "LoadTransaction";

  public ExportLoadXML()
  {
    super();
  }

 /**
  *  This method specifies the Document Type Definition and Element
  *  definitions for XML output, and writes the contents to a file.
  *
  *  @param dataList <code>List</code> containing data stream to convert to XML.
  */
  @Override
  public void writeData(List dataList, String isArchivePath)
  {
    Element rootElement = buildTranRootElement(documentName,
                                               DBConstants.LOAD_TRAN);
    Document doc = new Document(rootElement);

    for(int idx = 0; idx < dataList.size(); idx++)
    {
      Map columnMap = (Map)dataList.get(idx);
      Element tranTypeElement = buildTranTypeElement(columnMap);
      Element dateElement = buildTranDateElement(columnMap);
      Element loadElement = buildLoadElement(columnMap);
      Element locationElement = buildLocationElement(columnMap);
      Element toLocationElement = buildToLocationElement(columnMap);
      Element routeElement = buildRouteElement(columnMap);

      rootElement.addContent(tranTypeElement);
      tranTypeElement.addContent(dateElement);
      tranTypeElement.addContent(loadElement);
      tranTypeElement.addContent(locationElement);
      tranTypeElement.addContent(toLocationElement);
      tranTypeElement.addContent(routeElement);
    }

    createFile(documentName, doc, isArchivePath);
  }

  private Element buildLoadElement(Map columnMap)
  {
    Element loadID = new Element(TransactionHistoryData.LOADID_NAME);
    String sLoadID = DBHelper.getStringField(columnMap, TransactionHistoryData.LOADID_NAME);
    loadID.setText(sLoadID);

    return(loadID);
  }

  private Element buildLocationElement(Map columnMap)
  {
    Element location = new Element(TransactionHistoryData.LOCATION_NAME);
    String sLocation = DBHelper.getStringField(columnMap, TransactionHistoryData.LOCATION_NAME);
    location.setText(sLocation);

    return(location);
  }

  private Element buildToLocationElement(Map columnMap)
  {
    Element toLocation = new Element("STOLOCATION");
    String sToLocation = DBHelper.getStringField(columnMap, "STOLOCATION");
    toLocation.setText(sToLocation);

    return(toLocation);
  }

  private Element buildRouteElement(Map columnMap)
  {
    Element routeID = new Element(TransactionHistoryData.ROUTEID_NAME);
    String sRouteID = DBHelper.getStringField(columnMap, TransactionHistoryData.ROUTEID_NAME);
    routeID.setText(sRouteID);

    return(routeID);
  }
}
