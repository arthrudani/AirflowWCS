--for US 1119
delete from asrs.WEBNAVOPTION where NAME='Equipment Monitor';
delete from asrs.WEBNAVOPTION where NAME='Item Details';
delete from asrs.WEBNAVOPTION where NAME='Load Recovery';

--for US 1138--
Insert into asrs.WEBNAVOPTION([AUTHGROUPNAME],[NAVGROUPNAME],[NAME],[LINK],[DESCRIPTION],[ICON],[FAVORITE],[ORDERNO]) values 
  ('ROLE_ADMIN','Facility','Equipments','/equipments/view','Manage Equipments','fa-table',0,3);
  
Update asrs.WEBNAVOPTION set ORDERNO = 1 where NAVGROUPNAME = 'Facility' and NAME = 'Devices';
Update asrs.WEBNAVOPTION set ORDERNO = 2 where NAVGROUPNAME = 'Facility' and NAME = 'Locations';
Update asrs.WEBNAVOPTION set ORDERNO = 4 where NAVGROUPNAME = 'Facility' and NAME = 'Routes';
Update asrs.WEBNAVOPTION set ORDERNO = 5 where NAVGROUPNAME = 'Facility' and NAME = 'Warehouse';
Update asrs.WEBNAVOPTION set ORDERNO = 6 where NAVGROUPNAME = 'Facility' and NAME = 'Zones';
Update asrs.WEBNAVOPTION set ORDERNO = 7 where NAVGROUPNAME = 'Facility' and NAME = 'Zone Groups';

--for US 1183--
Insert into asrs.WEBNAVOPTION([AUTHGROUPNAME],[NAVGROUPNAME],[NAME],[LINK],[DESCRIPTION],[ICON],[FAVORITE],[ORDERNO]) values 
  ('ROLE_ADMIN','Reports','Occupancy','/occupancy/view','Manage Occupancy','fa-table',0,0);
  
-- for US 1308--
---for Disabling work maintenance---
Update asrs.WEBNAVOPTION set NAVGROUPNAME = 'InventoryHide' where NAVGROUPNAME = 'Inventory' and NAME = 'Work Maintenance';
---for Enabling work maintenance again---
Update asrs.WEBNAVOPTION set NAVGROUPNAME = 'Inventory' where NAVGROUPNAME = 'InventoryHide' and NAME = 'Work Maintenance';

-- for US 1305--
---for Disabling Time Slots Config---
Update asrs.WEBNAVOPTION set NAVGROUPNAME = 'InventoryHide' where NAVGROUPNAME = 'Inventory' and NAME = 'Time Slots Config';
---for Enabling Time Slots Config again---
Update asrs.WEBNAVOPTION set NAVGROUPNAME = 'Inventory' where NAVGROUPNAME = 'InventoryHide' and NAME = 'Time Slots Config';
