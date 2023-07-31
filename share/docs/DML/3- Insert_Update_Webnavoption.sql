USE [EBSWRXJ]

-- Update the data with order no Where FAVORITE is on
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 1 WHERE [ID] = 43;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 2 WHERE [ID] = 11;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 3 WHERE [ID] = 14;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 4 WHERE [ID] = 15;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 5 WHERE [ID] = 17;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 6 WHERE [ID] = 20;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 7 WHERE [ID] = 30;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 8 WHERE [ID] = 31;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 9 WHERE [ID] = 38;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 10 WHERE [ID] = 41;
UPDATE [asrs].[WEBNAVOPTION] SET [ORDERNO] = 11 WHERE [ID] = 4;

-- update the icon and text in the dash board
UPDATE [asrs].[WEBNAVOPTION] SET [ICON] = 'fa-suitcase' WHERE [ID] = 11;
UPDATE [asrs].[WEBNAVOPTION] SET [NAME] = 'Incoming Loads', [DESCRIPTION] = 'Manage Incoming Loads', [ICON] = 'fa-download' WHERE [ID] = 14;
UPDATE [asrs].[WEBNAVOPTION] SET [ICON] = 'fa-upload' WHERE [ID] = 15;

--insert new widget to the Database
INSERT INTO [asrs].[WEBNAVOPTION] ([AUTHGROUPNAME], [NAVGROUPNAME], [NAME], [LINK], [DESCRIPTION], [ICON], [FAVORITE], [ORDERNO]) VALUES ('ROLE_USER', 'Facility', 'Flights', '/flight/view', 'View bags per flight', 'fa-plane', 1, 1);
-- insert new widget for Time Slots Config
INSERT INTO [asrs].[WEBNAVOPTION] ([AUTHGROUPNAME], [NAVGROUPNAME], [NAME], [LINK], [DESCRIPTION], [ICON], [FAVORITE], [ORDERNO]) VALUES  ('ROLE_ADMIN', 'Inventory', 'Time Slots Config', '/timeslot/view', 'Manage Time Slot Configuration', 'fa-clock-o', 0, 0);