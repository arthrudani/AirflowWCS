USE [EBSWRXJ]

-- Remove work record from WEBNAVOPTION, it will reflect on the navigation menue
DELETE FROM [asrs].[WEBNAVOPTION] WHERE NAVGROUPNAME='Work';
GO

-- Remove work record from WEBNAVOPTION, it will reflect on the navigation menue
DELETE FROM [asrs].[WEBNAVOPTION] WHERE NAVGROUPNAME='Server Master' AND NAME ='Application Managment';
GO

-- Remove Flush Aisle from WEBNAVOPTION, it will reflect on the navigation menue
DELETE FROM [asrs].[WEBNAVOPTION] WHERE NAVGROUPNAME='Tools' AND NAME ='Flush Aisle';
GO
