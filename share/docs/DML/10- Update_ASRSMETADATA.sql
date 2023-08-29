Update asrs.ASRSMETADATA set sDataViewName='WorkMaintenanceHidden' where sDataViewName='WorkMaintenance' and sFullName='Modified Date';
Update asrs.ASRSMETADATA set sDataViewName='WorkMaintenanceHidden' where sDataViewName='WorkMaintenance' and sFullName='Final Sort Location Id';

-- Update ASRSMETADATA and [VIEW_LOADSCREEN] to make US 875 work.

Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='SRECOMMENDEDZONEHide' where sDataViewName='Load' and sFullName='Zone'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='SROUTEIDHide' where sDataViewName='Load' and sFullName='Route ID'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='SNEXTWAREHOUSEHide' where sDataViewName='Load' and sFullName='Next Warehouse'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='SFINALWAREHOUSEHide' where sDataViewName='Load' and sFullName='Final Warehouse'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='SNEXTADDRESSHide' where sDataViewName='Load' and sFullName='Next Address'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='SFINALADDRESSHide' where sDataViewName='Load' and sFullName='Final Address'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='SPARENTLOADHide' where sDataViewName='Load' and sFullName='Parent Load'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='SLOADMESSAGEHide' where sDataViewName='Load' and sFullName='Message'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='ILOADPRESENCECHECKHide' where sDataViewName='Load' and sFullName='LP Check'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='IHEIGHTHide' where sDataViewName='Load' and sFullName='Height'; 
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden',sColumnName='SBCRDATAHide' where sDataViewName='Load' and sFullName='Bar Code'; 
Update asrs.ASRSMETADATA set sColumnName='SLINEID' where sDataViewName='Load' and sFullName='BagID';

-- change IAMOUNTFULL to SITEM to same place for load
Update asrs.ASRSMETADATA set sColumnName='SITEM',sFullName='Item Type' where sDataViewName='Load' and sFullName='Amount Full';
--Transaction History View changes US906
Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Adjusted Qty'; 
Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Previous Qty'; 
Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Picked Qty'; 
Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Change Description'; 
Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Order Type'; 

---For US 932

Insert into asrs.WEBNAVOPTION([AUTHGROUPNAME],[NAVGROUPNAME],[NAME],[LINK],[DESCRIPTION],[ICON],[FAVORITE],[ORDERNO]) values 
  ('ROLE_USER','Inventory','Work Maintenance','/work/view','Manage Work','fa-tasks',1,12);

Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','SLOADID','Tray Id', 'N',0);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','SLINEID','Bag Id', 'N',1);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','SLOT','Flight#', 'N',2);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','SDEVICEID','Device Id', 'N',3);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','SFROM','From', 'N',4);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','STODEST','To', 'N',5);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','ISTATUS','Status', 'N',6);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','SCURRENTADDRESS','Current Location', 'N',7);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','IMOVETYPE','Move Type', 'N',8);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','DMOVEDATE','Load Date', 'N',9);
--- New Queries--
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','SFLIGHTNUM','Flight Number', 'N',13);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','SFLIGHTSTD','Flight Std', 'N',11);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','FINALSORTLOCATIONID','Final Sort Location Id', 'N',12);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','IORDERTYPE','Order Type', 'N',10);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','DCREATEDDT','Created Date', 'N',14);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('WorkMaintenance','DLASTMODIFYDT','Modified Date', 'N',15);
Update asrs.ASRSMETADATA set sColumnName='ORDERID',sFullName='Order Id' where sDataViewName='WorkMaintenance' and sFullName='Load Date';
Update asrs.ASRSMETADATA set sColumnName='SGLOBALID',sFullName='Global Id' where sDataViewName='WorkMaintenance' and sFullName='Current Location';
Update asrs.ASRSMETADATA set sColumnName='SITEMID',sFullName='Item Id' where sDataViewName='WorkMaintenance' and sFullName='Flight#';
Update asrs.ASRSMETADATA set sColumnName='SCOMMAND',sFullName='Command' where sDataViewName='WorkMaintenance' and sFullName='Bag Id';
--- End -- 
Update asrs.ASRSMETADATA set sIsTranslation='Y',sColumnName='ICMDMOVETYPE' where sDataViewName='WorkMaintenance' and sFullName='Move Type';
Update asrs.ASRSMETADATA set sIsTranslation='Y',sColumnName='ICMDORDERTYPE' where sDataViewName='WorkMaintenance' and sFullName='Order Type';
Update asrs.ASRSMETADATA set sIsTranslation='Y',sColumnName='ICMDSTATUS' where sDataViewName='WorkMaintenance' and sFullName='Status';
Update asrs.ASRSMETADATA set iDisplayOrder=1 where sDataViewName='WorkMaintenance' and sFullName='Global Id';
Update asrs.ASRSMETADATA set iDisplayOrder=2 where sDataViewName='WorkMaintenance' and sFullName='Item Id	';
Update asrs.ASRSMETADATA set iDisplayOrder=3 where sDataViewName='WorkMaintenance' and sFullName='Flight Number';
Update asrs.ASRSMETADATA set iDisplayOrder=4 where sDataViewName='WorkMaintenance' and sFullName='Order Id';
Update asrs.ASRSMETADATA set iDisplayOrder=5 where sDataViewName='WorkMaintenance' and sFullName='From';
Update asrs.ASRSMETADATA set iDisplayOrder=6 where sDataViewName='WorkMaintenance' and sFullName='To';
Update asrs.ASRSMETADATA set iDisplayOrder=7 where sDataViewName='WorkMaintenance' and sFullName='Flight Std';
Update asrs.ASRSMETADATA set iDisplayOrder=8 where sDataViewName='WorkMaintenance' and sFullName='Order Type';
Update asrs.ASRSMETADATA set iDisplayOrder=9 where sDataViewName='WorkMaintenance' and sFullName='Move Type';

Update asrs.ASRSMETADATA set sColumnName='SORDERID' where sDataViewName='WorkMaintenance' and sFullName='Order Id';
Update asrs.ASRSMETADATA set sColumnName='DCREATEDDATE' where sDataViewName='WorkMaintenance' and sFullName='Created Date';
Update asrs.ASRSMETADATA set sColumnName='DLASTMODIFYDATE' where sDataViewName='WorkMaintenance' and sFullName='Modified Date';
Update asrs.ASRSMETADATA set sColumnName='SFINALSORTLOCATIONID' where sDataViewName='WorkMaintenance' and sFullName='Final Sort Location Id';
Update asrs.ASRSMETADATA set sColumnName='DFLIGHTSTD',sDataViewName='WorkMaintenanceHidden' where sDataViewName='WorkMaintenance' and sFullName='Flight Std';

Update asrs.ASRSMETADATA set sDataViewName='WorkMaintenanceHidden' where sDataViewName='WorkMaintenance' and sFullName='Command';
--Solved bug of load screen

Update asrs.ASRSMETADATA set sIsTranslation='N' where sDataViewName='Load' and sFullName='Item Type';
-- Solved bug of bagId US 1084
Update asrs.ASRSMETADATA set sDataViewName='ItemDetailHidden' where sDataViewName='ItemDetail' and sFullName='Line ID';
Update asrs.ASRSMETADATA set sColumnName='SLINEID', sFullName='Item Id' where sDataViewName='ItemDetail' and sFullName='BagID';
Update asrs.ASRSMETADATA set sColumnName='SLINEID', sFullName='Item Id' where sDataViewName='FlightDetails' and sFullName='BagID';
Update asrs.ASRSMETADATA set sColumnName='SLINEID', sFullName='Item Id' where sDataViewName='PurchaseOrderHeader' and sFullName='BagID';

Update asrs.ASRSMETADATA set iDisplayOrder= 4 where sDataViewName='Load' and sFullName='Move Status';
Update asrs.ASRSMETADATA set iDisplayOrder= 5 where sDataViewName='Load' and sFullName='Address';
Update asrs.ASRSMETADATA set iDisplayOrder= 6 where sDataViewName='Load' and sFullName='Device ID';
Update asrs.ASRSMETADATA set iDisplayOrder= 7 where sDataViewName='Load' and sFullName='Container';
Update asrs.ASRSMETADATA set iDisplayOrder= 8 where sDataViewName='Load' and sFullName='Load Date';
Update asrs.ASRSMETADATA set iDisplayOrder= 9 where sDataViewName='Load' and sFullName='Item Type';
Update asrs.ASRSMETADATA set iDisplayOrder= 10 where sDataViewName='Load' and sFullName='Current Location';
Update asrs.ASRSMETADATA set sDataViewName='LoadHidden' where sDataViewName='Load' and sFullName='Weight';

--for US 1119
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Flight','FLIGHTSTD','Flight STD', 'N',2);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Flight','FLIGHTETD','Flight ETD', 'N',3);

---For US 1121---
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Transaction_All','SLINEID','Line Id', 'N',6);
 Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Transaction_All','SSTATION','Station', 'N',10);
 Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Transaction_All','STOSTATION','To-Station', 'N',11);
 Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Transaction_All','SDEVICEID','Device Id', 'N',14);

 Update asrs.ASRSMETADATA set iDisplayOrder= 3 where sDataViewName='Transaction_All' and sFullName='Order ID';
 Update asrs.ASRSMETADATA set iDisplayOrder= 4 where sDataViewName='Transaction_All' and sFullName='Lot';
 Update asrs.ASRSMETADATA set iDisplayOrder= 5 where sDataViewName='Transaction_All' and sFullName='TrayID';
 Update asrs.ASRSMETADATA set iDisplayOrder= 7 where sDataViewName='Transaction_All' and sFullName='Item ID';
 Update asrs.ASRSMETADATA set iDisplayOrder= 8 where sDataViewName='Transaction_All' and sFullName='Location';
 Update asrs.ASRSMETADATA set iDisplayOrder= 9 where sDataViewName='Transaction_All' and sFullName='To-Location';
 Update asrs.ASRSMETADATA set iDisplayOrder= 12 where sDataViewName='Transaction_All' and sFullName='Code';
 Update asrs.ASRSMETADATA set iDisplayOrder= 13 where sDataViewName='Transaction_All' and sFullName='Change Description';

 Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='To-Load'; 
 Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Order Type'; 
 Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Previous Qty'; 
 Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Adjusted Qty'; 
 Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Picked Qty'; 
 Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Hold Type'; 
 Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='User ID'; 
 Update asrs.ASRSMETADATA set sDataViewName='Transaction_AllHidden' where sDataViewName='Transaction_All' and sFullName='Role'; 

-----For US 1137----
Update asrs.ASRSMETADATA set sDataViewName='LocationHidden' where sDataViewName='Location' and sFullName='Height'; 

----For US 1183----
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','IAVAILABLECOUNT','Available Count', 'N',0);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','IOCCUPIEDCOUNT','Occupied Count', 'N',1);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','IUNAVAILABLECOUNT','Unavailable Count', 'N',2);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','ISTDEMPTYTRAYCOUNT','Std Empty Tray Count', 'N',3);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','IOOGEMPTYTRAYCOUNT','OOG Empty Tray Count', 'N',4);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','ISTDBAGONTRAYCOUNT','Std Bag on Tray Count', 'N',5);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','IOOGBAGONTRAYCOUNT','OOG Bag on Tray Count', 'N',6);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','ISTDTRAYSTACKCOUNT','Std Tray Stack Count', 'N',7);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','IOOGTRAYSTACKCOUNT','OOg Tray Stack Count', 'N',8);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','IOTHERCONTAINERTYPECOUNT','Other Count', 'N',9);
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Occupancy','DLASTMOVEMENTTIME','Last Movement', 'N',10);

----For US 1387---
Insert into asrs.ASRSMETADATA ([sDataViewName],[sColumnName],[sFullName],[sIsTranslation],[iDisplayOrder]) values ('Load','SSHELFPOSITION','Bag Position', 'N',6);
Update asrs.ASRSMETADATA set iDisplayOrder= 7 where sDataViewName='Load' and sFullName='Device ID';
Update asrs.ASRSMETADATA set iDisplayOrder= 8 where sDataViewName='Load' and sFullName='Container';
Update asrs.ASRSMETADATA set iDisplayOrder= 9 where sDataViewName='Load' and sFullName='Load Date';
Update asrs.ASRSMETADATA set iDisplayOrder= 10 where sDataViewName='Load' and sFullName='Item Type';
Update asrs.ASRSMETADATA set iDisplayOrder= 11 where sDataViewName='Load' and sFullName='Current Location';



---For Load transaction history screen
INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SLOADID', 'Load id','0');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SCONTAINERTYPE', 'Container type','1');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SBARCODE', 'Barcode','2');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SCARRIER', 'Carrier','3');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SGLOBALID', 'Global Id','4');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SFLIGHTNUM', 'Flight num','5');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DFLIGHTSTD', 'Flight STD','6');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DLASTEXPIRYDATE', 'Last expiry date','7');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DEXPECTEDRECEIPTDATE', 'Expected receipt data','8');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SARRIVALAISLEID', 'Arrival aisle id','9');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SSTORAGELOCATIONID', 'Storage location id','10');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SSTORAGELIFTERID', 'Storage lifter id','11');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DSTORAGELOADATEBSDATE', 'Storage load atebs date','12');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DSTORAGELOADPICKEDBYLIFTERDATE', 'Storage load picked by lifter date','13');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DSTORAGELOADDROPPEDBYLIFTERDATE', 'Storage load dropped by lifter date','14');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SSTORAGESHUTTLEID', 'Storage sguttle id','15');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DSTORAGELOADPICKEDBYSHUTTLEDATE', 'Storage load picked by shuttle date','16');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DSTORAGELOADDROPPEDBYSHUTTLEDATE', 'Storage load dropped by shuttle date','17');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DRETRIEVALORDERDATE', 'Retrival order date','18');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DRETRIEVALSHUTTLEID', 'Retrival shuttle id','19');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DRETRIEVALLOADPICKEDBYSHUTTLEDATE', 'Retrival load picked by shuttle date','20');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DRETRIEVALLOADDROPPEDBYSHUTTLEDATE', 'Retrival load dropped by shuttle date','21');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SRETRIEVALLIFTERID', 'Retrival lifter id','22');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DRETRIEVALLOADPICKEDBYLIFFTERDATE', 'Retrival load picked by lifter date','23');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DRETRIEVALLOADDROPPEDBYLIFFTERDATE', 'Retrival load dropped by lifter date','24');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'SRETRIEVALLOCATIONID', 'Retrival location id','25');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'ISTORAGEDURATION', 'Storage duration','26');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'IDWELVETIME', 'Dwelve time','27');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'IRETRIEVALDURATION', 'Retrival duration','28');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'ISTORAGELIFTERWAITINGTIME', 'Storage lifter waiting time','29');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'ISTORAGESHUTTLEWAITINGTIME', 'Storage shuttle waiting time','30');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'IRETRIEVALLIFTERWAITINGTIME', 'Retrival lifter waiting time','31');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'IRETRIEVALSHUTTLEWAITINGTIME', 'Retrival shuttle waiting time','32');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'IISCOMPLETED', 'Is completed','33');INSERT INTO [AirflowWCS].[asrs].[ASRSMETADATA] (sDataViewName, sColumnName, sFullName,iDisplayOrder) VALUES ('LoadTransactionHistory', 'DERTIMEOUTDATE', 'Er timeout date','34');