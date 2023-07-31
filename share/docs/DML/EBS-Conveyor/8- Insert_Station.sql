 /* 
<iStationType, int>					-- 225 : Reversible
<sAllocationType, varchar(30)>		-- best fit for now
<sDefaultRoute, varchar(12)>	    -- Need to update based on after the Route Table update 
<sRejectRoute, varchar(12)>		    -- Need to update based on after the Route Table update 				
<iAutoLoadMovementType, int>		-- 174 : AUTO_MOVE_OFF and 179 : AUTOPICK (go with 179)
<iAutoOrderType>					-- 340 : AUTO_ORDER_OFF
<iStatus, int>						-- 202 : STORERETRIEVE
<iBidirectionalStatus>				-- 404 : RETRIEVEMODE
<iCaptive>							-- 149 : NONCAPTIVE
<iConfirmLot>						-- 1 : YES and 2 : NO	
<sContainerType>					-- Tray  or OOGTray (FROM CONTAINERTYPE TABLE)
<iPhysicalStatus>					-- 233 : ONLINE AND 234 : OFFLINE
<iAmountFull>						-- 234 : EMPTY , 238 : FULL

*/


USE [EBSWRXJ]
GO

-- for remove exisitng option
DELETE FROM [asrs].[STATION]
GO

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
           ,[sStationScale]
           ,[sLoadPrefix]
           ,[sOrderPrefix]
           ,[iArrivalRequired]
           ,[iMaxAllowedEnroute]
           ,[iMaxAllowedStaged]
           ,[sPrinter]
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
           ,[sItem]
           ,[sLot]
           ,[fOrderQuantity]
           ,[iAllowRoundRobin]
           ,[sReplenishSources]
           ,[iSimulate]
           ,[iSimInterval]
           ,[iCustomAction]
           ,[sRecommendedZone]
           ,[sReprStationName]
           ,[sBCSDeviceID]
           ,[dModifyTime]
           ,[sAddMethod] 
           ,[sUpdateMethod]
           ,[sSecondaryDeviceID])
     VALUES

-- storage Conveyor south wearhouse stations
( 1,'6111','WHS','EBS South - Upper Lane 1A',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,174,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),           
( 2,'6121','WHS','EBS South - Upper Lane 2A',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),
( 3,'6211','WHS','EBS South - Lower Lane 1A',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),
( 4,'6221','WHS','EBS South - Lower Lane 2A',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,174,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),           
( 5,'6131','WHS','EBS South - Upper Lane 3A',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),
( 6,'6141','WHS','EBS South - Upper Lane 4A',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),
( 7,'6231','WHS','EBS South - Lower Lane 3A',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),
( 8,'6241','WHS','EBS South - Lower Lane 4A',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),
( 9,'6112','WHS','EBS South - Upper Lane 1B',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,174,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),           
(10,'6122','WHS','EBS South - Upper Lane 2B',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),
(11,'6212','WHS','EBS South - Lower Lane 1B',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),
(12,'6222','WHS','EBS South - Lower Lane 2B',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,174,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),           
(13,'6132','WHS','EBS South - Upper Lane 3B',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),
(14,'6142','WHS','EBS South - Upper Lane 4B',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),
(15,'6232','WHS','EBS South - Lower Lane 3B',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),
(16,'6242','WHS','EBS South - Lower Lane 4B',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),

(17,'6151','WHS','EBS South - Upper Lane 5A',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'OOGTray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),
(18,'6251','WHS','EBS South - Lower Lane 5A',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'OOGTray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S41',NULL,NULL,NULL,NULL),
(19,'6152','WHS','EBS South - Upper Lane 5B',225,'BestFit',1,NULL,NULL,NULL,'S41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'OOGTray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),
(20,'6252','WHS','EBS South - Lower Lane 5B',225,'BestFit',1,NULL,NULL,NULL,'S42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'OOGTray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'S42',NULL,NULL,NULL,NULL),

-- storage Conveyor north wearhouse stations
(21,'6611','WHN','EBS North - Upper Lane 1A',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,174,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),           
(22,'6621','WHN','EBS North - Upper Lane 2A',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),
(23,'6711','WHN','EBS North - Lower Lane 1A',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),
(24,'6721','WHN','EBS North - Lower Lane 2A',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,174,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),           
(25,'6631','WHN','EBS North - Upper Lane 3A',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL),
(26,'6641','WHN','EBS North - Upper Lane 4A',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL),
(27,'6731','WHN','EBS North - Lower Lane 3A',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL),
(28,'6741','WHN','EBS North - Lower Lane 4A',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL),
(29,'6612','WHN','EBS North - Upper Lane 1B',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,174,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),           
(30,'6622','WHN','EBS North - Upper Lane 2B',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),
(31,'6712','WHN','EBS North - Lower Lane 1B',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),
(32,'6722','WHN','EBS North - Lower Lane 1B',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,174,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),           
(33,'6632','WHN','EBS North - Upper Lane 3B',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL),
(34,'6642','WHN','EBS North - Upper Lane 4B',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL),
(35,'6732','WHN','EBS North - Lower Lane 3B',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL),
(36,'6742','WHN','EBS North - Lower Lane 4B',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'Tray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL),

(37,'6651','WHN','EBS North - Upper Lane 5A',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'OOGTray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),
(38,'6751','WHN','EBS North - Lower Lane 5A',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'OOGTray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N41',NULL,NULL,NULL,NULL),
(39,'6652','WHN','EBS North - Upper Lane 5B',225,'BestFit',1,NULL,NULL,NULL,'N41',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'OOGTray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL),
(40,'6752','WHN','EBS North - Lower Lane 5B',225,'BestFit',1,NULL,NULL,NULL,'N42',NULL,NULL,NULL,1,2,5,NULL,179,340,1,202,404,179,2, 2, 2,2,1,'OOGTray',233,233,1,2,0.000,1, 234,2,0,1,NULL,NULL,0.000,2,NULL,36,0,0,NULL,NULL,'N42',NULL,NULL,NULL,NULL);

GO
SET IDENTITY_INSERT [asrs].[STATION] OFF

Select * from [asrs].[STATION]