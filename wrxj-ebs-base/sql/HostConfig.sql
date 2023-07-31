SET DEFINE OFF;
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'Formatter', 'DelimitedFormatter', 'com.daifukuamerica.wrxj.host.messages.delimited.DelimitedFormatter', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'Formatter', 'FixedLengthFormatter', 'com.daifukuamerica.wrxj.host.messages.fixedlength.FixedLengthFormatter', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'Formatter', 'XMLFormatter', 'com.daifukuamerica.wrxj.host.messages.xml.XMLFormatter', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'Formatter', 'SendEmptyFields', 'false', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'Error', 'com.daifukuamerica.wrxj.host.messages.HostError', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'ExpectedReceiptComplete', 'com.daifukuamerica.wrxj.host.messages.ExpectedReceiptComplete', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'InventoryAdjustment', 'com.daifukuamerica.wrxj.host.messages.InventoryAdjustment', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'InventoryStatus', 'com.daifukuamerica.wrxj.host.messages.InventoryStatus', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'InventoryUpload', 'com.daifukuamerica.wrxj.host.messages.InventoryUpload', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'LoadArrival', 'com.daifukuamerica.wrxj.host.messages.LoadArrival', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'LocationArrival', 'com.daifukuamerica.wrxj.host.messages.LocationArrival', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'OrderComplete', 'com.daifukuamerica.wrxj.host.messages.OrderComplete', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'OrderStatus', 'com.daifukuamerica.wrxj.host.messages.OrderStatus', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'PickComplete', 'com.daifukuamerica.wrxj.host.messages.PickComplete', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'StoreComplete', 'com.daifukuamerica.wrxj.host.messages.StoreComplete', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController-1', 'Controller', 'Collaborator', 'HostMessageIntegrator', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController-1', 'Controller', 'CommType', 'JDBC-DB2-AS400Host', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController-1', 'Controller', 'CommType', 'JDBC-ORACLEHost', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController-1', 'Controller', 'ControllerType', 'HostController-1', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController-1', 'Controller', 'HostInCheckInterval', '10', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController-1', 'Controller', 'HostOutCheckInterval', '30', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController-1', 'Controller', 'PublishStatus', 'true', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'Controller', 'ControllerType', 'HostMessageIntegrator', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'DelimitedParser', 'Carrier', 'com.daifukuamerica.wrxj.host.messages.delimited.CarrierParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'DelimitedParser', 'Customer', 'com.daifukuamerica.wrxj.host.messages.delimited.CustomerParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'DelimitedParser', 'ExpectedReceiptHeader', 'com.daifukuamerica.wrxj.host.messages.delimited.ExpectedReceiptHeaderParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'DelimitedParser', 'ExpectedReceiptLine', 'com.daifukuamerica.wrxj.host.messages.delimited.ExpectedReceiptLineParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'DelimitedParser', 'InventoryHold', 'com.daifukuamerica.wrxj.host.messages.delimited.InventoryHoldParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'DelimitedParser', 'InventoryRequest', 'com.daifukuamerica.wrxj.host.messages.delimited.InventoryRequestParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'DelimitedParser', 'ItemMaster', 'com.daifukuamerica.wrxj.host.messages.delimited.ItemMasterParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'DelimitedParser', 'OrderHeader', 'com.daifukuamerica.wrxj.host.messages.delimited.OrderHeaderParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'DelimitedParser', 'OrderLine', 'com.daifukuamerica.wrxj.host.messages.delimited.OrderLineParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'FixedLengthParser', 'Carrier', 'com.daifukuamerica.wrxj.host.messages.fixedlength.FixedLengthCarrierParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'FixedLengthParser', 'Customer', 'com.daifukuamerica.wrxj.host.messages.fixedlength.FixedLengthCustomerParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'FixedLengthParser', 'ExpectedReceiptHeader', 'com.daifukuamerica.wrxj.host.messages.fixedlength.FixedLengthExpectedReceiptHeaderParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'FixedLengthParser', 'ExpectedReceiptLine', 'com.daifukuamerica.wrxj.host.messages.fixedlength.FixedLengthExpectedReceiptLineParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'FixedLengthParser', 'ItemMaster', 'com.daifukuamerica.wrxj.host.messages.fixedlength.FixedLengthItemMasterParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'FixedLengthParser', 'OrderHeader', 'com.daifukuamerica.wrxj.host.messages.fixedlength.FixedLengthOrderHeaderParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'FixedLengthParser', 'OrderLine', 'com.daifukuamerica.wrxj.host.messages.fixedlength.FixedLengthOrderLineParser', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'Host', 'DataType', 'Delimited', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'Host', 'DataType', 'XML', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'Host', 'DataType', 'FixedLength', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'CarrierMessage', 'com.daifukuamerica.wrxj.host.messages.xml.CarrierParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'CustomerMessage', 'com.daifukuamerica.wrxj.host.messages.xml.CustomerParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'ExpectedLoadMessage', 'com.daifukuamerica.wrxj.host.messages.xml.ExpectedReceiptParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'ExpectedReceiptMessage', 'com.daifukuamerica.wrxj.host.messages.xml.ExpectedReceiptParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'InventoryHoldMessage', 'com.daifukuamerica.wrxj.host.messages.xml.InventoryHoldParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'InventoryRequestMessage', 'com.daifukuamerica.wrxj.host.messages.xml.InventoryRequestParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'ItemMessage', 'com.daifukuamerica.wrxj.host.messages.xml.ItemParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'OrderEmptyMessage', 'com.daifukuamerica.wrxj.host.messages.xml.OrderParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'OrderItemMessage', 'com.daifukuamerica.wrxj.host.messages.xml.OrderParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostMessageIntegrator', 'XMLParser', 'OrderLoadMessage', 'com.daifukuamerica.wrxj.host.messages.xml.OrderParser', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-ORACLEHost', 'Transport', 'HostName', 'localhost', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-ORACLEHost', 'Transport', 'LogHostMessages', 'true ', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-ORACLEHost', 'Transport', 'class', 'com.daifukuamerica.wrxj.host.communication.JDBCTransporter', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-ORACLEHost', 'Transport', 'driver', 'oracle.jdbc.driver.OracleDriver', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-ORACLEHost', 'Transport', 'maximum', '5', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-ORACLEHost', 'Transport', 'password', 'asrs', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-ORACLEHost', 'Transport', 'url', 'jdbc:oracle:thin:@localhost:1521:wrxj', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-ORACLEHost', 'Transport', 'user', 'wrxjhost', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-ORACLEHost', 'Transport', 'ErrorCodes', 'com.daifukuamerica.wrxj.jdbc.oracle.OracleErrorCodes', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'ErrorCodes', 'com.daifukuamerica.wrxj.jdbc.db2.DB2ErrorCodes', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'HostName', 'aplus', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'HostToWRxTableName', 'HostData.HostToWRx', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'LogHostMessages', 'false', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'WRxToHostTableName', 'HostData.WRxToHost', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'class', 'com.daifukuamerica.wrxj.host.communication.JDBCTransporter', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'driver', 'com.ibm.as400.access.AS400JDBCDriver', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'maximum', '5', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'password', 'asrs', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'url', 'jdbc:as400://10.1.5.80', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('JDBC-DB2-AS400Host', 'Transport', 'user', 'wrxjhost', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController-1', 'Controller', 'CommType', 'TCPIPHost', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('HostController', 'OutboundMessage', 'DeviceStatus', 'com.daifukuamerica.wrxj.host.messages.DeviceStatus', 1);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'ClientRetryInterval', '12', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'HeartBeatInterval', '10', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'HeartBeatString', 'HeartBeat', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'HostName', '10.16.1.105', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'ListenPort', '8421', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'LogPath', 'logs', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'MessagePrefix', '0x02', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'MessageSuffix', '0x03', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'SocketType', 'Client', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'UseAcks', 'false', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'UseHeartBeats', 'true', 2);
Insert into HOSTCONFIG
   (SDATAHANDLER, SGROUP, SPARAMETERNAME, SPARAMETERVALUE, IACTIVECONFIG)
 Values
   ('TCPIPHost', 'Transport', 'class', 'com.daifukuamerica.wrxj.host.communication.TCPClientTransport', 2);
COMMIT;
