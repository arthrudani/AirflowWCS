-- KR: LOCATiONS need to be changed 
-- for Rollback option
--DELETE FROM [asrs].[LOADLINEITEM];
--DELETE FROM [asrs].[LOAD];
--DELETE FROM [asrs].[LOCATION];
--DELETE FROM [asrs].[DEVICE];

SET IDENTITY_INSERT [asrs].[LOCATION] ON
INSERT INTO [asrs].[LOCATION]
([iID]
,[sWarehouse]
,[sAddress]
,[sShelfPosition]
,[sZone]
,[iLocationStatus]
,[iLocationType]
,[iEmptyFlag]
,[sDeviceID]
,[iHeight]
,[iAssignedLength]
,[iSearchOrder]
,[iAisleGroup]
,[iAllowDeletion]
,[iMoveSequence]
,[iLocationDepth]
,[sLinkedAddress]
,[iSwapZone]
,[dModifyTime]
,[sAddMethod]
,[sUpdateMethod]
,[iPrimarySearchOrder]
,[iSecondarySearchOrder])
VALUES

-- Storage Conveyor locatin South side

-- EBS South - Upper Lane 1A (Station : 6111)
( 1,'WHS','6111','000',null,29,10,21,'9001',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Upper Lane 2A (Station : 6121)
( 2,'WHS','6121','000',null,29,10,21,'9001',1,0,1,3,2,0,1,null,0,SYSDATETIME(),'sql','sql',5,null),
-- EBS South - Lower Lane 1A (Station : 6211)
( 3,'WHS','6211','000',null,29,10,21,'9002',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Lower Lane 2A (Station : 6221)
( 4,'WHS','6221','000',null,29,10,21,'9002',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),


-- EBS South - Upper Lane 3A (Station : 6131)
( 5,'WHS','6131','000',null,29,10,21,'9001',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Upper Lane 4A (Station : 6141)
( 6,'WHS','6141','000',null,29,10,21,'9001',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Lower Lane 3A (Station : 6231)
( 7,'WHS','6231','000',null,29,10,21,'9002',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Lower Lane 4A (Station : 6241)
( 8,'WHS','6241','000',null,29,10,21,'9002',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),


-- EBS South - Upper Lane 1B (Station : 6112)
( 9,'WHS','6112','000',null,29,10,21,'9001',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Upper Lane 2B (Station : 6122)
(10,'WHS','6122','000',null,29,10,21,'9001',1,0,1,3,2,0,1,null,0,SYSDATETIME(),'sql','sql',5,null),
-- EBS South - Lower Lane 1B (Station : 6212)
(11,'WHS','6212','000',null,29,10,21,'9002',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Lower Lane 2B (Station : 6222)
(12,'WHS','6222','000',null,29,10,21,'9002',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),


-- EBS South - Upper Lane 3B (Station : 6132)
(13,'WHS','6132','000',null,29,10,21,'9001',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Upper Lane 4B (Station : 6142)
(14,'WHS','6142','000',null,29,10,21,'9001',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Lower Lane 3B (Station : 6232)
(15,'WHS','6232','000',null,29,10,21,'S42',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS South - Lower Lane 4B (Station : 6242)
(16,'WHS','6242','000',null,29,10,21,'S42',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),


-- Storage Conveyor locatin North side

-- EBS North - Upper Lane 1A (Station : 6611)
(17,'WHN','6611','000',null,29,10,21,'9003',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS North - Upper Lane 2A (Station : 6621)
(18,'WHN','6621','000',null,29,10,21,'9003',1,0,1,3,2,0,1,null,0,SYSDATETIME(),'sql','sql',5,null),
-- EBS North - Lower Lane 1A (Station : 6711)
(19,'WHN','6711','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS North - Lower Lane 2A (Station : 6721)
(20,'WHN','6721','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),


-- EBS North - Upper Lane 3A (Station : 6631)
(21,'WHN','6631','000',null,29,10,21,'9003',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS North - Upper Lane 4A (Station : 6641)
(22,'WHN','6641','000',null,29,10,21,'9003',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS North - Lower Lane 3A (Station : 6731)
(23,'WHN','6731','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS North - Lower Lane 4A (Station : 6741)
(24,'WHN','6741','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),


-- EBS North - Upper Lane 1B (Station : 6612)
(25,'WHN','6612','000',null,29,10,21,'9003',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS North - Upper Lane 2B (Station : 6622)
(26,'WHN','6622','000',null,29,10,21,'9003',1,0,1,3,2,0,1,null,0,SYSDATETIME(),'sql','sql',5,null),
-- EBS North - Lower Lane 1B (Station : 6712)
(27,'WHN','6712','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS North - Lower Lane 2B (Station : 6722)
(28,'WHN','6722','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),


-- EBS North - Upper Lane 3B (Station : 6632)
(29,'WHN','6632','000',null,29,10,21,'9003',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- EBS North - Upper Lane 4B (Station : 6642)
(30,'WHN','6642','000',null,29,10,21,'9003',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
--EBS North - Lower Lane 3B (Station : 6732)
(31,'WHN','6732','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
--EBS North - Lower Lane 4B(Station : 6742)
(32,'WHN','6742','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),

-- OOG TRAY -EBS South - Upper Lane 5A (6151)
(33,'WHS','6151','000',null,29,10,21,'9003',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- OOG TRAY - EBS South - Upper Lane 5B(6152)
(34,'WHS','6152','000',null,29,10,21,'9003',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- OOG TRAY -EBS South - Lower Lane 5A (6251)
(35,'WHS','6251','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- OOG TRAY -EBS South - Lower Lane 5B (6152)
(36,'WHS','6152','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),


-- OOG TRAY - EBS North - Upper Lane 5A (6651)
(37,'WHN','6651','000',null,29,10,21,'9003',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- OOG TRAY - EBS North - Upper Lane 5B (6652)
(38,'WHN','6652','000',null,29,10,21,'9003',1,0,1,3,2,0,1,null,0,SYSDATETIME(),'sql','sql',5,null),
-- OOG TRAY - EBS North - Lower Lane 5A (Station : 6751)
(39,'WHN','6751','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null),
-- OOG TRAY - EBS North - Lower Lane 5B (Station : 6752)
(40,'WHN','6752','000',null,29,10,21,'9004',1,0,1,1,2,0,1,null,0,SYSDATETIME(),'sql','sql',1,null)
GO
SET IDENTITY_INSERT [asrs].[LOCATION] OFF