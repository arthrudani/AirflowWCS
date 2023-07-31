/*
-- NOTE : please run the following statements (commented out) first before running the main sql 

ALTER TABLE [asrs].[LOCATION] DROP CONSTRAINT [LOCATION_CH_iLocationType]
ALTER TABLE [asrs].[LOCATION]  WITH CHECK ADD  CONSTRAINT [LOCATION_CH_iLocationType] CHECK  (([iLocationType]>=(10) AND [iLocationType]<=(21)))
ALTER TABLE [asrs].[LOCATION] CHECK CONSTRAINT [LOCATION_CH_iLocationType]

ALTER TABLE [asrs].[STATION] DROP CONSTRAINT [STATION_CH_iStationType]
ALTER TABLE [asrs].[STATION]  WITH CHECK ADD  CONSTRAINT [STATION_CH_iStationType] CHECK  (([iStationType]>=(220) AND [iStationType]<=(239)))
ALTER TABLE [asrs].[STATION] CHECK CONSTRAINT [STATION_CH_iStationType]
*/

-- NOTE Please run the above sql first
-- Please check if your deviceid is 9001 in the device table othewise change the follwoing var
DECLARE @DeviceId as varchar(4)
set @DeviceId = '9001'   --- Change this var if your device name is ACP1

-- Delete and insert again
delete from asrs.STATION where iStationType in (234,235,236,237,238,239) and sDeviceID = @DeviceId

--delete shuttle from locations 
delete from asrs.location where sAddress like '11%' and iLocationType = 21
--delete from [asrs].[ROUTE]

-- update the lift location type and change from 18 to 20
update asrs.LOCATION set iLocationType=20 where sAddress like '01%' and iLocationType =18
--update the bidirectional station to input station
update asrs.STATION set sStationName ='0400199901', iStationType =225, sDescription ='Input Station' where sStationName ='0200199901' and iStationType = 224 


SET IDENTITY_INSERT [asrs].[STATION] ON

INSERT INTO [asrs].[STATION]  ([iID]
		   ,[sStationName]
           ,[sWarehouse]
           ,[sDescription]
           ,[iStationType]
           ,[sAllocationType]
           ,[iDeleteInventory]
           ,[sDefaultRoute]
           ,[sLinkRoute]
           ,[sRejectRoute]
           ,[sDeviceID]
           ,[iArrivalRequired]
           ,[iMaxAllowedEnroute]
           ,[iMaxAllowedStaged]
           ,[iAutoLoadMovementType]
           ,[iAutoOrderType]
           ,[iAllocationEnabled]
           ,[iStatus]
           ,[iBidirectionalStatus]
           ,[iCaptive]
           ,[iConfirmLot]
           ,[iConfirmLocation]
           ,[iConfirmLoad]
           ,[iConfirmItem]
           ,[iConfirmQty]
           ,[sContainerType]
           ,[iPhysicalStatus]
           ,[iOrderStatus]
           ,[iPoReceiveAll]
           ,[iCCIAllowed]
           ,[fWeight]
           ,[iHeight]
           ,[iAmountFull]
           ,[iPriorityCategory]
           ,[iReInputFlag]
           ,[iRetrieveCommandDetail]
           ,[fOrderQuantity]
           ,[iAllowRoundRobin]
           ,[iSimulate]
           ,[iSimInterval]
           ,[iCustomAction]
           ,[sBCSDeviceID]) VALUES

-- wearhouse stations ,   the 9001 is device id -> ACP

-- LIFT: Aisle 1 Inbound/Deposit lifter Layer Stations  type = 238 Reversible
( 3,'0700199901','EBS','Lift transit Reversible',238,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 4,'0700199902','EBS','Lift transit Reversible',238,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 5,'0700199903','EBS','Lift transit Reversible',238,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 6,'0700199904','EBS','Lift transit Reversible',238,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 7,'0700199905','EBS','Lift transit Reversible',238,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 8,'0700199906','EBS','Lift transit Reversible',238,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
-- LIFT: Aisle 1 outbound/Pickup Lifter Layer  Stations  type = 235 
( 9, '0500299901','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 10,'0500299902','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 11,'0500299903','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 12,'0500299904','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 13,'0500299905','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 14,'0500299906','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),


-- SHUTTLE - inbound/pickup Stations type = 236 Revesible
( 15,'1400100001','EBS','Shuttle transit Reversible',239,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 16,'1400100002','EBS','Shuttle transit Reversible',239,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 17,'1400100003','EBS','Shuttle transit Reversible',239,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 18,'1400100004','EBS','Shuttle transit Reversible',239,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 19,'1400100005','EBS','Shuttle transit Reversible',239,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 20,'1400100006','EBS','Shuttle transit Reversible',239,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,400,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),

-- SHUTTLE - outbound/deposit Stations type = 237
( 21,'1300200001','EBS','Shuttle transit out',237,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 22,'1300200002','EBS','Shuttle transit out',237,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 23,'1300200003','EBS','Shuttle transit out',237,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 24,'1300200004','EBS','Shuttle transit out',237,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 25,'1300200005','EBS','Shuttle transit out',237,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 26,'1300200006','EBS','Shuttle transit out',237,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId);

-- SHUTTLE: insert shuttles into location table
insert into [AirflowWCS].asrs.LOCATION ([sWarehouse],[sAddress],[sShelfPosition],[iLocationStatus],[iLocationType],[iEmptyFlag],[sDeviceID] ,[iHeight] ,[iAssignedLength],[iSearchOrder],[iAisleGroup],[iAllowDeletion],[iMoveSequence],[iLocationDepth],[sLinkedAddress],[iSwapZone],[iPrimarySearchOrder],[iSecondarySearchOrder])  
values('EBS','1100199901','000',29,21,21,@DeviceId,1,0,200,1,2,0,1,null,0,171,171),
	  ('EBS','1100199902','000',29,21,21,@DeviceId,1,0,200,1,2,0,1,null,0,172,172),
	  ('EBS','1100199903','000',29,21,21,@DeviceId,1,0,200,1,2,0,1,null,0,172,172),
	  ('EBS','1100199904','000',29,21,21,@DeviceId,1,0,200,1,2,0,1,null,0,172,172),
	  ('EBS','1100199905','000',29,21,21,@DeviceId,1,0,200,1,2,0,1,null,0,172,172),
	  ('EBS','1100199906','000',29,21,21,@DeviceId,1,0,200,1,2,0,1,null,0,172,172);







