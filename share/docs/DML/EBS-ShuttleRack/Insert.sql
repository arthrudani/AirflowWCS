-- Expected receipt handler
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostExpectedReceiptMessageHandler', 'Controller', 'ControllerType', 'HostExpectedReceiptMessageHandler', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostExpectedReceiptMessageHandler', 'Processor', 'EmptyLocationFinder', 'com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.AisleBasedEmptyLocationFinderImpl', 1, NULL, NULL, NULL);

-- New parsers used by HostMessageIntegrator
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostMessageIntegrator', 'DelimitedParser', 'FlightDataUpdateMessage', 'com.daifukuoc.wrxj.custom.ebs.host.messages.delimited.FlightDataUpdateParser', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostMessageIntegrator', 'DelimitedParser', 'STOREDCOMPLETEACKMESSAGE', 'com.daifukuoc.wrxj.custom.ebs.host.messages.delimited.StoredCompleteParser', 1, NULL, NULL, NULL);

-- Flight data update handler
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostFlightDataUpdateMessageHandler', 'Controller', 'ControllerType', 'HostFlightDataUpdateMessageHandler', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostFlightDataUpdateMessageHandler', 'Processor', 'FlightDataUpdater', 'com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.DefaultFlightDataUpdaterImpl', 1, NULL, NULL, NULL);

-- A new outbound message for flight data update ack 
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostController', 'OutboundMessage', 'FlightDataUpdateMessage', 'com.daifukuoc.wrxj.custom.ebs.host.messages.FlightDataUpdateResponseMessage', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTOUTACCESS] VALUES('SAC', 'FlightDataUpdateMessage', 1, NULL, NULL, NULL);

-- Retrieval order handler
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostRetrievalOrderMessageHandler', 'Controller', 'ControllerType', 'HostRetrievalOrderMessageHandler', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostRetrievalOrderMessageHandler', 'Processor', 'LoadRetriever', 'com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.AisleBasedFlightLoadRetrieverImpl', 1, NULL, NULL, NULL);

-- Insert KeepAliveTimeout
Insert into [asrs].[HOSTCONFIG] VALUES('TCPIPHost', 'Transport', 'KeepAliveTimeout', 60, 1, NULL, NULL, NULL);

-- Insert AckTimeout and AckMaxRetry
Insert into [asrs].[HOSTCONFIG] VALUES('TCPIPHost', 'Transport', 'AckTimeout', 60, 1, NULL, NULL, NULL);
Insert into [asrs].[HOSTCONFIG] VALUES('TCPIPHost', 'Transport', 'AckMaxRetry', 3, 1, NULL, NULL, NULL);

-- Change from PLCPort to ACPPort
UPDATE [asrs].[CONTROLLERCONFIG] SET sPropertyValue='com.daifukuoc.wrxj.custom.ebs.plc.acp.ACPPort' WHERE sController='PLCPort' AND sPropertyName='class';

-- SACPort configurations
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostPort', 'Controller', 'ControllerType', 'HostPort', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostPort', 'Controller', 'CommType', 'SACHost', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostPort', 'Formatter', 'DelimitedFormatter', 'com.daifukuamerica.wrxj.host.messages.delimited.DelimitedFormatter', 1, NULL, NULL, NULL);

-- SACHost configuration used by SACPort
INSERT INTO [asrs].[HOSTCONFIG] VALUES('SACHost', 'Transport', 'PortName', 'HostPort', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('SACHost', 'Transport', 'IntegratorName', 'HostIntegrator', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('SACHost', 'Transport', 'IPAddress', 'localhost', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('SACHost', 'Transport', 'PortNumber', '8421', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('SACHost', 'Transport', 'KeepAliveInterval', '20', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('SACHost', 'Transport', 'RetryInterval', '5', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('SACHost', 'Transport', 'AckTimeout', '20', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('SACHost', 'Transport', 'AckMaxRetry', '2', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('SACHost', 'Transport', 'UseStxEtx', 'true', 1, NULL, NULL, NULL);

-- SACIntegrator configurations
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostIntegrator', 'Controller', 'ControllerType', 'HostIntegrator', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostIntegrator', 'Host', 'DataType', 'Delimited', 1, NULL, NULL, NULL);

-- Disable existing entries used by HostController and HostMessageIntegrator
UPDATE [asrs].[HOSTCONFIG] SET iActiveConfig=2 WHERE sDataHandler='TCPIPHost';
UPDATE [asrs].[HOSTCONFIG] SET iActiveConfig=2 WHERE sDataHandler like 'HostController%';
UPDATE [asrs].[HOSTCONFIG] SET iActiveConfig=2 WHERE sDataHandler like 'HostMessageIntegrator%';

-- Register a new controller - HostPlcIntegrator
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostPlcIntegrator', 'Controller', 'ControllerType', 'HostPlcIntegrator', 1, NULL, NULL, NULL);

-- Register Task.CmdAllocation.class
INSERT INTO [asrs].[CONTROLLERCONFIG]
           ([sController]
           ,[sPropertyName]
           ,[sPropertyValue]
           ,[sPropertyDesc]
           ,[iScreenChangeAllowed]
           ,[iEnabled])
     VALUES
           ('TimedEventScheduler','Task.CmdAllocation.class','com.daifukuoc.wrxj.custom.ebs.scheduler.event.CmdAllocationTask','Task to check for stations needing CMD allocating',2,1),
		   ('TimedEventScheduler','Task.CmdAllocation.interval','10','interval in seconds',2,1);

-- US - 803 SAC Interface - Inventory Update
-- Inventory update handler
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostInventoryUpdateMessageHandler', 'Controller', 'ControllerType', 'HostInventoryUpdateMessageHandler', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostInventoryUpdateMessageHandler', 'Processor', 'InventoryUpdater', 'com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate.InventoryUpdaterImpl', 1, NULL, NULL, NULL);

-- A new outbound message for inventory update ack 
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostController', 'OutboundMessage', 'InventoryUpdateMessage', 'com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryUpdateResponseMessage', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTOUTACCESS] VALUES('SAC', 'InventoryUpdateMessage', 1, NULL, NULL, NULL);

INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostMessageIntegrator', 'DelimitedParser', 'InventoryUpdateMessage', 'com.daifukuoc.wrxj.custom.ebs.host.messages.delimited.InventoryUpdateParser', 1, NULL, NULL, NULL);
