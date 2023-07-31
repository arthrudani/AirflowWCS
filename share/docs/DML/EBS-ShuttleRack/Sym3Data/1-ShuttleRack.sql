Use AirFlowWCS

-- run this clear data 

delete from  [asrs].WRXTOHOST;
delete from  [asrs].HOSTTOWRX;
delete from [asrs].[TRANSACTIONHISTORY];
delete from [asrs].PURCHASEORDERLINE
delete from [asrs].PURCHASEORDERHEADER
delete from [asrs].ORDERLINE
delete from [asrs].ORDERHEADER
delete from [asrs].LOADLINEITEM
delete from [asrs].LOAD
delete from [asrs].LOCATION
delete from [asrs].STATION
delete from [asrs].DEVICE
delete from [asrs].PORT
delete from [asrs].MOVE

-- Warehouse 

INSERT INTO [asrs].[WAREHOUSE]
           ([sSuperWarehouse]
           ,[sWarehouse]
           ,[sDescription]
           ,[iWarehouseType]
           ,[iWarehouseStatus]
           ,[iOneLoadPerLoc]
           ,[sEquipWarehouse]
           ,[dModifyTime]
           ,[sAddMethod]
           ,[sUpdateMethod])

     VALUES 
	 (NULL, 'EBS', 'EBS Warehouse', 64, 240, 1, 0, NULL, NULL, NULL);
	
	 -- port
INSERT INTO [asrs].[PORT]
           ([sPortName]
           ,[sDeviceID]
           ,[iDirection]
           ,[iLastSequence]
           ,[iCommunicationMode]
           ,[sServerName]
           ,[sSocketNumber]
           ,[iRetryInterval]
           ,[iSndKeepAliveInterval]
           ,[iRcvKeepAliveInterval]
           ,[dModifyTime]
           ,[sAddMethod]
           ,[sUpdateMethod])
     VALUES
	('9001-Port','9001',103,0,182,'localhost',4501,5000,60000,70000,null,null,null),
	('9002-Port','9002',103,0,182,'localhost',4502,5000,60000,70000,null,null,null),
	('9003-Port','9003',103,0,182,'localhost',4503,5000,60000,70000,null,null,null),
	('9004-Port','9004',103,0,182,'localhost',4504,5000,60000,70000,null,null,null),
	('9005-Port','9005',103,0,182,'localhost',4505,5000,60000,70000,null,null,null),
	('9006-Port','9006',103,0,182,'localhost',4506,5000,60000,70000,null,null,null),
	('9007-Port','9007',103,0,182,'localhost',4507,5000,60000,70000,null,null,null),
	('9008-Port','9008',103,0,182,'localhost',4508,5000,60000,70000,null,null,null),
	('9009-Port','9009',103,0,182,'localhost',4509,5000,60000,70000,null,null,null),
	('9010-Port','9010',103,0,182,'localhost',4510,5000,60000,70000,null,null,null);

-- device
INSERT INTO [asrs].[DEVICE]
           ([sDeviceID]
           ,[iDeviceType]
           ,[iAisleGroup]
           ,[sCommDevice]
           ,[iOperationalStatus]
           ,[iPhysicalStatus]
           ,[iEmulationMode]
           ,[sCommSendPort]
           ,[sCommReadPort]
           ,[sErrorCode]
           ,[sNextDevice]
           ,[iDeviceToken]
           ,[sSchedulerName]
           ,[sAllocatorName]
           ,[sStationName]
           ,[sUserID]
           ,[sPrinter]
           ,[sWarehouse]
           ,[sJVMIdentifier]
           ,[dModifyTime]
           ,[sAddMethod]
           ,[sUpdateMethod]
           ,[iLocSeqMethod])
     VALUES
	 ('9001',182,1,null,186,233,196,'9001-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1),
	 ('9002',182,2,null,186,233,196,'9002-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1),
	 ('9003',182,3,null,186,233,196,'9003-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1),
	 ('9004',182,4,null,186,233,196,'9004-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1),
	 ('9005',182,5,null,186,233,196,'9005-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1),
	 ('9006',182,6,null,186,233,196,'9006-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1),
	 ('9007',182,7,null,186,233,196,'9007-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1),
	 ('9008',182,8,null,186,233,196,'9008-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1),
	 ('9009',182,9,null,186,233,196,'9009-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1),
	 ('9010',182,10,null,186,233,196,'9010-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1);

	 --inbound & outbound stations

	  INSERT INTO [asrs].[STATION] ([sStationName] ,[sWarehouse],[sDescription],[iStationType] ,[sAllocationType],[iDeleteInventory] ,[sDeviceID]
           ,[iArrivalRequired],[iMaxAllowedEnroute],[iMaxAllowedStaged],[iAutoLoadMovementType] ,[iAutoOrderType] ,[iAllocationEnabled],[iStatus] ,[iBidirectionalStatus]
           ,[iCaptive] ,[iConfirmLot] ,[iConfirmLocation],[iConfirmLoad],[iConfirmItem],[iConfirmQty] ,[sContainerType],[iPhysicalStatus] ,[iOrderStatus] ,[iPoReceiveAll] ,[iCCIAllowed] ,[fWeight]
           ,[iHeight]  ,[iAmountFull],[iPriorityCategory]  ,[iReInputFlag]  ,[iRetrieveCommandDetail],[fOrderQuantity] ,[iAllowRoundRobin] ,[iSimulate] ,[iSimInterval] ,[iCustomAction] ,[sBCSDeviceID]) VALUES
( '0300199901','EBS','Output station',223,'BestFit',1,'9001',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9001'),
( '0200299901','EBS','input station',224,'BestFit',1,'9001',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9001'),
( '0300399901','EBS','Output station',223,'BestFit',1,'9002',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9002'),
( '0200499901','EBS','input station',224,'BestFit',1,'9002',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9002'),
( '0300599901','EBS','Output station',223,'BestFit',1,'9003',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9003'),
( '0200699901','EBS','input station',224,'BestFit',1,'9003',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9003'),
( '0300799901','EBS','Output station',223,'BestFit',1,'9004',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9004'),
( '0200899901','EBS','input station',224,'BestFit',1,'9004',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9004'),
( '0300999901','EBS','Output station',223,'BestFit',1,'9005',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9005'),
( '0201099901','EBS','input station',224,'BestFit',1,'9005',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9005'),
( '0301199901','EBS','Output station',223,'BestFit',1,'9006',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9006'),
( '0201299901','EBS','input station',224,'BestFit',1,'9006',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9006'),
( '0301399901','EBS','Output station',223,'BestFit',1,'9007',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9007'),
( '0201499901','EBS','input station',224,'BestFit',1,'9007',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9007'),
( '0301599901','EBS','Output station',223,'BestFit',1,'9008',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9008'),
( '0201699901','EBS','input station',224,'BestFit',1,'9008',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9008'),
( '0301799901','EBS','Output station',223,'BestFit',1,'9009',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9009'),
( '0201899901','EBS','input station',224,'BestFit',1,'9009',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9009'),
( '0301999901','EBS','Output station',223,'BestFit',1,'9010',1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9010'),
( '0202099901','EBS','input station',224,'BestFit',1,'9010',1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,'9010');


--Lifts
insert into [AirflowWCS].asrs.LOCATION ([sWarehouse],[sAddress],[sShelfPosition],[sZone],[iLocationStatus],[iLocationType],[iEmptyFlag],[sDeviceID] ,[iHeight] 
                 ,[iAssignedLength],[iSearchOrder],[iAisleGroup],[iAllowDeletion],[iMoveSequence],[iLocationDepth],[sLinkedAddress],[iSwapZone],[dModifyTime],[sAddMethod],[sUpdateMethod],[iPrimarySearchOrder],[iSecondarySearchOrder]) values 
('EBS','0100199999','000',null,29,20,21,'9001',1,0,1,1,2,0,1,null,0,null,null,null,1,1),
('EBS','0100299999','000',null,29,20,21,'9001',1,0,338,1,2,0,1,null,0,null,null,null,338,338),
('EBS','0100399999','000',null,29,20,21,'9002',1,0,675,2,2,0,1,null,0,null,null,null,675,675),
('EBS','0100499999','000',null,29,20,21,'9002',1,0,1012,2,2,0,1,null,0,null,null,null,1012,1012),
('EBS','0100599999','000',null,29,20,21,'9003',1,0,1349,3,2,0,1,null,0,null,null,null,1349,1349),
('EBS','0100699999','000',null,29,20,21,'9003',1,0,1686,3,2,0,1,null,0,null,null,null,1686,1686),
('EBS','0100799999','000',null,29,20,21,'9004',1,0,2023,4,2,0,1,null,0,null,null,null,2023,2023),
('EBS','0100899999','000',null,29,20,21,'9004',1,0,2360,4,2,0,1,null,0,null,null,null,2360,2360),
('EBS','0100999999','000',null,29,20,21,'9005',1,0,2697,5,2,0,1,null,0,null,null,null,2697,2697),
('EBS','0101099999','000',null,29,20,21,'9005',1,0,3034,5,2,0,1,null,0,null,null,null,3034,3034),
('EBS','0101199999','000',null,29,20,21,'9006',1,0,3371,6,2,0,1,null,0,null,null,null,3371,3371),
('EBS','0101299999','000',null,29,20,21,'9006',1,0,3708,6,2,0,1,null,0,null,null,null,3708,3708),
('EBS','0101399999','000',null,29,20,21,'9007',1,0,4045,7,2,0,1,null,0,null,null,null,4045,4045),
('EBS','0101499999','000',null,29,20,21,'9007',1,0,4382,7,2,0,1,null,0,null,null,null,4382,4382),
('EBS','0101599999','000',null,29,20,21,'9008',1,0,4719,8,2,0,1,null,0,null,null,null,4719,4719),
('EBS','0101699999','000',null,29,20,21,'9008',1,0,5056,8,2,0,1,null,0,null,null,null,5056,5056),
('EBS','0101799999','000',null,29,20,21,'9009',1,0,5393,9,2,0,1,null,0,null,null,null,5393,5393),
('EBS','0101899999','000',null,29,20,21,'9009',1,0,5730,9,2,0,1,null,0,null,null,null,5730,5730),
('EBS','0101999999','000',null,29,20,21,'9010',1,0,6067,10,2,0,1,null,0,null,null,null,6067,6067),
('EBS','0102099999','000',null,29,20,21,'9010',1,0,6404,10,2,0,1,null,0,null,null,null,6404,6404);

-- transfer stations in separate file (2-Transfer Stations.sql)
-- Locations in separate file (3-Locations.sql)
-- Shuttles in separte file (4-Shuttles.sql)









