-----For US  1196---
-- Inventory By Flight handler
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostInventoryReqByFlightMessageHandler', 'Controller', 'ControllerType', 'HostInventoryReqByFlightMessageHandler', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostInventoryReqByFlightMessageHandler', 'Processor', 'InventoryReqByFlight', 'com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryreqbyflight.InventoryReqByFlightImpl', 1, NULL, NULL, NULL);
