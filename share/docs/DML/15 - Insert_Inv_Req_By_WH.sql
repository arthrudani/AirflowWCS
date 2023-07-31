-- US - 1269 - SAC - Inventory Request by Warehouse
-- Inventory request by warehouse handler
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostInvReqByWarehouseMessageHandler', 'Controller', 'ControllerType', 'HostInvReqByWarehouseMessageHandler', 1, NULL, NULL, NULL);
INSERT INTO [asrs].[HOSTCONFIG] VALUES('HostInvReqByWarehouseMessageHandler', 'Processor', 'InvReqByWarehouseProcessor', 'com.daifukuoc.wrxj.custom.ebs.host.processor.invreqbywarehouse.InvReqByWarehouseProcessorImpl', 1, NULL, NULL, NULL);
