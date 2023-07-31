/*==========================================================================*/
/* Add WRxJ-Web table data to a MICROSOFT SQL SERVER database 				*/
/*==========================================================================*/
/*
truncate table asrs.WEBNAVGROUP;
truncate table asrs.WEBNAVOPTION;
truncate table asrs.EQUIPMENTMONITORSTATUSTYPE;
*/

/* Authorization groups for Spring Security */
INSERT into asrs.WEBAUTHGROUP (ID, NAME, DESCRIPTION) VALUES (1, N'ROLE_ADMIN', N'Administrator role.');
INSERT into asrs.WEBAUTHGROUP (ID, NAME, DESCRIPTION) VALUES (2, N'ROLE_MASTER', N'Daifuku Wynright developer & support role.');
INSERT into asrs.WEBAUTHGROUP (ID, NAME, DESCRIPTION) VALUES (3, N'ROLE_USER', N'Basic user role.');
INSERT into asrs.WEBAUTHGROUP (ID, NAME, DESCRIPTION) VALUES (4, N'ROLE_READONLY', N'Read only role.');
INSERT into asrs.WEBAUTHGROUP (ID, NAME, DESCRIPTION) VALUES (5, N'ROLE_ELEVATED', N'Role with elevated privileges for recovery.');
GO

/* Navigation Panel Configuration - Menu Groups */
insert into asrs.WEBNAVGROUP (NAME, ICON, STYLE, NAVORDER) values ('Facility','fa-building',null,1);
insert into asrs.WEBNAVGROUP (NAME, ICON, STYLE, NAVORDER) values ('Inventory','fa-cubes',null,2);
insert into asrs.WEBNAVGROUP (NAME, ICON, STYLE, NAVORDER) values ('Orders','fa-cart-arrow-down',null,3);
insert into asrs.WEBNAVGROUP (NAME, ICON, STYLE, NAVORDER) values ('Reports','fa-hdd-o',null,4);
insert into asrs.WEBNAVGROUP (NAME, ICON, STYLE, NAVORDER) values ('Tools','fa-wrench',null,5);
insert into asrs.WEBNAVGROUP (NAME, ICON, STYLE, NAVORDER) values ('Users','fa-users',null,6);
insert into asrs.WEBNAVGROUP (NAME, ICON, STYLE, NAVORDER) values ('Work','fa-briefcase',null,7);
insert into asrs.WEBNAVGROUP (NAME, ICON, STYLE, NAVORDER) values ('Server Master','fa-user-secret','color:red !important',8);
insert into asrs.WEBNAVGROUP (NAME, ICON, STYLE, NAVORDER) values ('Help','fa-question-circle',null,9);

/* Navigation Panel Configuration - Menu Options */
SET IDENTITY_INSERT asrs.WEBNAVOPTION ON 
GO
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (1, 'ROLE_ADMIN', 'Facility', 'Containers', '/container/view', 'Manage Container Types', 'fa-box', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (2, 'ROLE_ADMIN', 'Facility', 'Devices', '/device/view', 'Manage Devices', 'fa-hdd-o', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (3, 'ROLE_ADMIN', 'Facility', 'Locations', '/location/view', 'Manage Locations', 'fa-table', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (5, 'ROLE_ADMIN', 'Facility', 'Routes', '/route/view', 'Manage Routes', 'fa-arrow-circle-right', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (6, 'ROLE_ADMIN', 'Facility', 'Warehouse', '/warehouse/view', 'Manage Warehouses', 'fa-warehouse', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (7, 'ROLE_ADMIN', 'Facility', 'Zones', '/zone/view', 'Manage Location Zones', 'fa-globe', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (8, 'ROLE_ADMIN', 'Facility', 'Zone Groups', '/zone/viewGroup', 'Manage Recommended Zone Groups', 'fa-hand-point-right', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (35, 'ROLE_USER', 'Facility', 'Flights', '/flights/view', 'View bags per flight', 'fa-plane', 1, 9);

INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (4, 'ROLE_USER', 'Inventory', 'Item Details', '/itemdetail/view', 'Manage Item Details', 'fa-cubes', 1, 8);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (10, 'ROLE_ADMIN', 'Inventory', 'Item Master', '/item/view', 'Manage Item Masters', 'fa-cube', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (11, 'ROLE_USER', 'Inventory', 'Loads', '/load/view', 'Manage Loads', 'fa-briefcase', 1, 1);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (13, 'ROLE_USER', 'Inventory', 'Moves', '/move/view', 'Manage Moves', 'fa-arrows-alt', 0, 0);

INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (14, 'ROLE_USER', 'Orders', 'Incoming Loads', '/expected/view', 'Manage Incoming Loads', 'fa-download', 1, 2);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (15, 'ROLE_USER', 'Orders', 'Order Maintenance', '/order/view', 'Manage Orders', 'fa-upload', 1, 3);

INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (17, 'ROLE_USER', 'Tools', 'Equipment Monitor', '/equipment/view', 'Monitor Equipment Status', 'fa-desktop', 1, 4);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (30, 'ROLE_USER', 'Tools', 'Log Viewer', '/logview/view', 'View Logs', 'fa-eye', 1, 6);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (31, 'ROLE_USER', 'Tools', 'Load Recovery', '/recovery2/view', 'Recovery & Load Movement', 'fa-wrench', 1, 7);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (19, 'ROLE_ADMIN', 'Tools', 'System Configuration', '/sysconfig/view', 'Manage System Configuration', 'fa-cogs', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (20, 'ROLE_USER', 'Tools', 'Transaction History', '/history/view', 'View Transaction History', 'fa-book', 1, 5);

INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (21, 'ROLE_ADMIN', 'Users', 'Users', '/users/view', 'Manage User Accounts', 'fa-users', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (22, 'ROLE_ADMIN', 'Users', 'User Permissions', '/userpermission/view', 'Manage User Permissions', 'fa-users-cog', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (23, 'ROLE_ADMIN', 'Users', 'User Sessions', '/usersession/view', 'View User Sessions', 'fa-sitemap', 0, 0);

INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (24, 'ROLE_USER', 'Work', 'Pick', '/pick/view', 'Pick', 'fa-hand-lizard-o', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (25, 'ROLE_USER', 'Work', 'Store', '/store/view', 'Store', 'fa-box-open', 0, 0);

INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (26, 'ROLE_MASTER', 'Server Master', 'Application Managment', '/webmanagement/view', 'Application Managment', 'fa-puzzle-piece', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (27, 'ROLE_MASTER', 'Server Master', 'Developer Playground', '/playground/view', 'Developer Test UI', 'fa-tools', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (28, 'ROLE_MASTER', 'Server Master', 'JMS Messages', '/message/view', NULL, 'fa-envelope', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (29, 'ROLE_MASTER', 'Server Master', 'Google', 'http://www.google.com', 'Google', 'fa-google', 0, 0);

INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (33, 'ROLE_USER', 'Help', 'Support', '/help/support', 'Support Information', 'fa-book-medical', 0, 0);
INSERT into asrs.WEBNAVOPTION (ID, AUTHGROUPNAME, NAVGROUPNAME, NAME, LINK, DESCRIPTION, ICON, FAVORITE, ORDERNO) VALUES (34, 'ROLE_USER', 'Help', 'User Manual', '/docs/wynsoftusr-base.pdf', 'User Manual', 'fa-book-medical', 0, 0);
GO
SET IDENTITY_INSERT asrs.WEBNAVOPTION OFF
GO

/* Web User Authorization (users need to exist in Employee table) */
-- Super-User
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('su', 'ROLE_ADMIN');
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('su', 'ROLE_ELEVATED');
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('su', 'ROLE_MASTER');
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('su', 'ROLE_USER');
-- Administrator
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('Administrator', 'ROLE_ADMIN');
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('Administrator', 'ROLE_ELEVATED');
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('Administrator', 'ROLE_USER');
-- Operator
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('Operator', 'ROLE_USER');
-- Recovery/Lead
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('Recovery', 'ROLE_ELEVATED');
INSERT INTO asrs.WEBUSERAUTHGROUP (USERNAME, AUTH_GROUP) VALUES ('Recovery', 'ROLE_USER');
GO

/* Equipment Monitor Status Types */
INSERT INTO asrs.EQUIPMENTMONITORSTATUSTYPE (sEMStatusID, sEMStatusDescription, iEMDisplayPriority, sEMBackground, sEMForeground) VALUES ('DISCONNECTED', 'Disconnected', 7, '#ffff00', '#000000');
INSERT INTO asrs.EQUIPMENTMONITORSTATUSTYPE (sEMStatusID, sEMStatusDescription, iEMDisplayPriority, sEMBackground, sEMForeground) VALUES ('ENERGY', 'Online (energy saving)', 2, '#009900', '#ffffff');
INSERT INTO asrs.EQUIPMENTMONITORSTATUSTYPE (sEMStatusID, sEMStatusDescription, iEMDisplayPriority, sEMBackground, sEMForeground) VALUES ('ERROR', 'Error', 8, '#ff0000', '#ffffff');
INSERT INTO asrs.EQUIPMENTMONITORSTATUSTYPE (sEMStatusID, sEMStatusDescription, iEMDisplayPriority, sEMBackground, sEMForeground) VALUES ('LOC-LOC', 'Double-deep swap', 1, '#00ffff', '#000000');
INSERT INTO asrs.EQUIPMENTMONITORSTATUSTYPE (sEMStatusID, sEMStatusDescription, iEMDisplayPriority, sEMBackground, sEMForeground) VALUES ('OFFLINE', 'Offline', 6, '#0000ff', '#ffffff');
INSERT INTO asrs.EQUIPMENTMONITORSTATUSTYPE (sEMStatusID, sEMStatusDescription, iEMDisplayPriority, sEMBackground, sEMForeground) VALUES ('ONLINE', 'Online', 3, '#00ff00', '#000000');
INSERT INTO asrs.EQUIPMENTMONITORSTATUSTYPE (sEMStatusID, sEMStatusDescription, iEMDisplayPriority, sEMBackground, sEMForeground) VALUES ('RUNNING', 'Online', 4, '#00ff00', '#000000');
INSERT INTO asrs.EQUIPMENTMONITORSTATUSTYPE (sEMStatusID, sEMStatusDescription, iEMDisplayPriority, sEMBackground, sEMForeground) VALUES ('STOPPED', 'Offline', 5, '#0000ff', '#ffffff');
INSERT INTO asrs.EQUIPMENTMONITORSTATUSTYPE (sEMStatusID, sEMStatusDescription, iEMDisplayPriority, sEMBackground, sEMForeground) VALUES ('UNKNOWN', 'Unknown', 0, '#888888', '#000000');
GO

/* Equipment Monitor Status (Web) */
INSERT INTO ASRSMETADATA (sDataViewName  ,sColumnName ,sFullName ,sIsTranslation ,iDisplayOrder) VALUES ('EquipmentDetail', 'sEMGraphicID', 'Equipment ID', 'N', 0);
INSERT INTO ASRSMETADATA (sDataViewName  ,sColumnName ,sFullName ,sIsTranslation ,iDisplayOrder) VALUES ('EquipmentDetail', 'sEMStatusText', 'Description', 'N', 1);
INSERT INTO ASRSMETADATA (sDataViewName  ,sColumnName ,sFullName ,sIsTranslation ,iDisplayOrder) VALUES ('EquipmentDetail', 'sEMStatusID', 'Status', 'N', 2);
INSERT INTO ASRSMETADATA (sDataViewName  ,sColumnName ,sFullName ,sIsTranslation ,iDisplayOrder) VALUES ('EquipmentDetail', 'sEMStatusText2', 'Status Detail', 'N', 3);
INSERT INTO ASRSMETADATA (sDataViewName  ,sColumnName ,sFullName ,sIsTranslation ,iDisplayOrder) VALUES ('EquipmentDetail', 'sEMErrorCode', 'Error Code', 'N', 4);
INSERT INTO ASRSMETADATA (sDataViewName  ,sColumnName ,sFullName ,sIsTranslation ,iDisplayOrder) VALUES ('EquipmentDetail', 'sEMErrorText', 'Error Detail', 'N', 5);

/* Equipment Monitor Tracking (Web) */
INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder)  VALUES ('EquipmentTracking', 'SEMGRAPHICID', 'Graphic ID', 'N', 0);
INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder)  VALUES ('EquipmentTracking', 'SEMDEVICEID', 'Device ID', 'N', 1);
INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder)  VALUES ('EquipmentTracking', 'SEMTRACKINGID', 'Tracking ID', 'N', 2);
INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder)  VALUES ('EquipmentTracking', 'SEMSTATUS', 'Status', 'N', 4);
INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder)  VALUES ('EquipmentTracking', 'SEMORIGIN', 'Origin', 'N', 5);
INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder)  VALUES ('EquipmentTracking', 'SEMDESTINATION', 'Destination', 'N', 6);
INSERT INTO ASRSMETADATA (sDataViewName, sColumnName, sFullName, sIsTranslation, iDisplayOrder)  VALUES ('EquipmentTracking', 'SEMSIZE', 'Size', 'N', 7);
GO
