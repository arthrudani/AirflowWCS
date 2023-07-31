-----For US  1196---
-- Retrieval order handler
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostRetrievalItemMessageHandler', 'Controller', 'ControllerType', 'HostRetrievalItemMessageHandler', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostRetrievalItemMessageHandler', 'Processor', 'LoadItemRetriever', 'com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalitemorder.AisleBasedFlightItemRetrieverImpl', 1, NULL, NULL, NULL);
