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
 * <B>Description:</B>
 *   Class to handle XML output for System Transaction History. This
 *   implementation is JDOM specific.
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author      mandrus
 * @version     1.0
 * @since       2009-Jun-05       
 */
public class ExportSystemXML extends JDOMHelper implements Exporter
{
  private String documentName = "SystemTransaction";

  /**
   * Constructor
   */
  public ExportSystemXML()
  {
    super();
  }

  /**
   * This method specifies the Document Type Definition and Element definitions
   * for XML output, and writes the contents to a file.
   * 
   * @param ipDataList <code>List</code> containing data stream to convert to XML.
   */
  @Override
  public void writeData(List ipDataList, String isArchivePath)
  {
    Element vpRootElement = buildTranRootElement(documentName,
        DBConstants.LOAD_TRAN);
    
    Document vpDoc = new Document(vpRootElement);

    for (int idx = 0; idx < ipDataList.size(); idx++)
    {
      Map vpColumnMap = (Map)ipDataList.get(idx);
      
      Element vpTranTypeElement = buildTranTypeElement(vpColumnMap);
      Element vpDateElement = buildTranDateElement(vpColumnMap);
      Element vpDeviceElement = buildElement(vpColumnMap,
          TransactionHistoryData.DEVICEID_NAME);
      Element vpLocationElement = buildElement(vpColumnMap,
          TransactionHistoryData.LOCATION_NAME);
      Element vpRouteElement = buildElement(vpColumnMap,
          TransactionHistoryData.ROUTEID_NAME);
      Element vpStationElement = buildElement(vpColumnMap,
          TransactionHistoryData.STATION_NAME);
      Element vpActionElement = buildElement(vpColumnMap,
          TransactionHistoryData.ACTIONDESCRIPTION_NAME);

      vpRootElement.addContent(vpTranTypeElement);
      vpTranTypeElement.addContent(vpDateElement);
      vpTranTypeElement.addContent(vpDeviceElement);
      vpTranTypeElement.addContent(vpLocationElement);
      vpTranTypeElement.addContent(vpStationElement);
      vpTranTypeElement.addContent(vpRouteElement);
      vpTranTypeElement.addContent(vpActionElement);
    }

    createFile(documentName, vpDoc, isArchivePath);
  }

  /**
   * Build an Element
   * 
   * @param ipColumnMap
   * @param isField
   * @return
   */
  private Element buildElement(Map ipColumnMap, String isField)
  {
    Element vpElement = new Element(isField);
    vpElement.setText(DBHelper.getStringField(ipColumnMap, isField));

    return vpElement;
  }
}
