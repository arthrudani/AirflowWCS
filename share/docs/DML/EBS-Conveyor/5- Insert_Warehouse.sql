USE [EBSWRXJ]
GO

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
	 (NULL, 'WHS', 'EBS South Warehouse', 65, 240, 1, 0, NULL, NULL, NULL),
	 (NULL, 'WHN', 'EBS North Warehouse', 65, 240, 1, 0, NULL, NULL, NULL)
GO	


