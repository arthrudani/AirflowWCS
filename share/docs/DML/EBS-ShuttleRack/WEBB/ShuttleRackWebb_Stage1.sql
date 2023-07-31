

Delete from [asrs].LOADLINEITEM
Delete from [asrs].LOAD

DELETE from  [asrs].[Location]
DELETE from  [asrs].[WAREHOUSE]
DELETE FROM [asrs].[STATION];
Delete from [asrs].[PORT];
Delete from  [asrs].[DEVICE];
delete from [asrs].ROUTE;
/*
//Need to drop the FK from [asrs].[DEDICATEDLOCATION] and  

ALTER TABLE [asrs].[LOAD] DROP CONSTRAINT [LOAD_FK_LOCATION];
ALTER TABLE [asrs].[DEDICATEDLOCATION] DROP CONSTRAINT [DEDICATEDLOCATION_FK_LOCATION];

//change the size

alter table [asrs].[LOAD] alter column [sAddress] [varchar](10) NOT NULL;
alter table [asrs].[DEDICATEDLOCATION] alter column [sAddress] [varchar](10) NOT NULL;
alter table [asrs].[Location] alter column [sAddress] [varchar](10) NOT NULL;
alter table [asrs].[STATION] alter column [sStationName] [varchar](10) NOT NULL;

//create FK again

ALTER TABLE [asrs].[LOAD]  WITH CHECK ADD  CONSTRAINT [LOAD_FK_LOCATION] FOREIGN KEY([sWarehouse], [sAddress])
REFERENCES [asrs].[LOCATION] ([sWarehouse], [sAddress])
GO
ALTER TABLE [asrs].[LOAD] CHECK CONSTRAINT [LOAD_FK_LOCATION];

ALTER TABLE [asrs].[DEDICATEDLOCATION]  WITH CHECK ADD  CONSTRAINT [DEDICATEDLOCATION_FK_LOCATION] FOREIGN KEY([sWarehouse], [sAddress])
REFERENCES [asrs].[LOCATION] ([sWarehouse], [sAddress])
GO

ALTER TABLE [asrs].[DEDICATEDLOCATION] CHECK CONSTRAINT [DEDICATEDLOCATION_FK_LOCATION]
*/
GO
DECLARE @DeviceId as varchar(4)
set @DeviceId = '9001'
-- Warehouse
INSERT INTO [asrs].[WAREHOUSE]
           (
		   [sSuperWarehouse]
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
	 ( 'EBS', 'EBS', 'EBS Warehouse', 64, 240, 1, 0, NULL, NULL, NULL);

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
		   (@DeviceId+'-Port',@DeviceId,103,0,182,'localhost',4501,5000,60000,70000,null,null,null);
          
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
	 (@DeviceId,182,1,null,186,233,196,@DeviceId+'-Port',NULL,NULL,null,235,'Scheduler','Allocator',NULL,NULL,NULL,'EBS',NULL,NULL,NULL,NULL,1);
	
	-- Stations
SET IDENTITY_INSERT [asrs].[STATION] ON

INSERT INTO [asrs].[STATION]
           ([iID]
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
           ,[sBCSDeviceID])
     VALUES

-- wearhouse stations ,   the 9001 is device id -> ACP
( 1,'0400199901','EBS','Revesable station',225,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,174,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),  
( 2,'0300299901','EBS','Output station',223,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
-- LIFT: Aisle 1 Inbound lifter Layer Pickup/Deposit Stations  type = 234
( 3,'0700199901','EBS','Lift transit input',234,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 4,'0700199902','EBS','Lift transit input',234,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 5,'0700199903','EBS','Lift transit input',234,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 6,'0700199904','EBS','Lift transit input',234,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 7,'0700199905','EBS','Lift transit input',234,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 8,'0700199906','EBS','Lift transit input',234,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
-- LIFT: Aisle 1 outbound Lifter Layer  Stations  type = 235 
( 9, '0500299901','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 10,'0500299902','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 11,'0500299903','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 12,'0500299904','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 13,'0500299905','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId),
( 14,'0500299906','EBS','Lift transit out',235,'BestFit',1,NULL,NULL,NULL,@DeviceId,1,2,5,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,0.000,2,36,0,0,@DeviceId);
-- SHUTTLE: 




-- Create location for Lift, aile in/out stations, shuttle and in/out stations 
--Lifts with location type = 18 -> Device Location
insert into [AirflowWCS].asrs.LOCATION ([sWarehouse],[sAddress],[sShelfPosition],[sZone],[iLocationStatus],[iLocationType],[iEmptyFlag],[sDeviceID] ,[iHeight] ,[iAssignedLength],[iSearchOrder],[iAisleGroup],[iAllowDeletion],[iMoveSequence],[iLocationDepth],[sLinkedAddress],[iSwapZone],[dModifyTime],[sAddMethod],[sUpdateMethod],[iPrimarySearchOrder],[iSecondarySearchOrder])  
values('EBS','0100199999','000',null,29,18,21,'9001',1,0,168,1,2,0,1,null,0,null,null,null,169,169),
	  ('EBS','0100299999','000',null,29,18,21,'9001',1,0,168,1,2,0,1,null,0,null,null,null,170,170);




-- Route : NOTE: for stage 1:  From all locations can go to outbound station (0300299901) and from inbound station (0400199901) can go all locations

INSERT INTO [asrs].[ROUTE]
           ([sRouteID]
           ,[sFromID]
           ,[sDestID]
           ,[iFromType]
           ,[iDestType]
           ,[iRouteOnOff]
           ,[dModifyTime]
           ,[sAddMethod]
           ,[sUpdateMethod])
     VALUES
		   ('0001','0400199901','20XXXYYYZZ',232,231,35,null,null,null),
           ('0002','20XXXYYYZZ','0300299901',231,232,35,null,null,null);
		 --TO CHECK  ('0003','20XXXYYYZZ','0400199901',231,232,35,null,null,null);-- Only if outbound stations is not available (0300299901)






