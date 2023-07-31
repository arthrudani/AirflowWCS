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
 *   Class to handle XML output for User Transaction History. This implementation
 *   is JDOM specific.
 *
 * @author    A.D.
 * @version   1.0
 * @since     03-Jul-03
 */
public class ExportUserXML extends JDOMHelper implements Exporter
{
  private String documentName = "UserTransaction";

  public ExportUserXML()
  {
    super();
  }

  @Override
  public void writeData(List dataList, String isArchivePath)
  {
    Element rootElement = buildTranRootElement(documentName,
                                               DBConstants.USER_TRAN);
    Document doc = new Document(rootElement);

    for(int idx = 0; idx < dataList.size(); idx++)
    {
      Map columnMap = (Map)dataList.get(idx);
      Element tranTypeElement = buildTranTypeElement(columnMap);
      Element dateElement = buildTranDateElement(columnMap);
      Element userElement = buildUserElement(columnMap);
      Element roleElement = buildRoleElement(columnMap);
      Element deviceElement = buildDeviceElement(columnMap);
      Element stationElement = buildStationElement(columnMap);

      rootElement.addContent(tranTypeElement);
      tranTypeElement.addContent(dateElement);
      tranTypeElement.addContent(userElement);
      tranTypeElement.addContent(roleElement);
      tranTypeElement.addContent(deviceElement);
      tranTypeElement.addContent(stationElement);
    }

    createFile(documentName, doc, isArchivePath);
  }

  private Element buildUserElement(Map columnMap)
  {
    Element user = new Element(TransactionHistoryData.USERID_NAME);
    String sUser = DBHelper.getStringField(columnMap, TransactionHistoryData.USERID_NAME);
    user.setText(sUser);

    return(user);
  }

  private Element buildRoleElement(Map columnMap)
  {
    Element role = new Element(TransactionHistoryData.ROLE_NAME);
    String sRole = DBHelper.getStringField(columnMap, TransactionHistoryData.ROLE_NAME);
    role.setText(sRole);

    return(role);
  }

  private Element buildDeviceElement(Map columnMap)
  {
    Element device = new Element(TransactionHistoryData.DEVICEID_NAME);
    String sDevice = DBHelper.getStringField(columnMap, TransactionHistoryData.DEVICEID_NAME);
    device.setText(sDevice);

    return(device);
  }

  private Element buildStationElement(Map columnMap)
  {
    Element station = new Element(TransactionHistoryData.STATION_NAME);
    String sStation = DBHelper.getStringField(columnMap, TransactionHistoryData.STATION_NAME);
    station.setText(sStation);

    return(station);
  }
}
