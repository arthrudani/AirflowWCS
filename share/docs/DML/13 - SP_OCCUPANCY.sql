CREATE OR ALTER PROC asrs.SP_OCCUPANCY
AS
BEGIN
	INSERT INTO [asrs].[OCCUPANCY]
			   ([dLastMovementTime]
			   ,[iAvailableCount]
			   ,[iOccupiedCount]
			   ,[iUnavailableCount]
			   ,[iStdEmptyTrayCount]
			   ,[iOOGEmptyTrayCount]
			   ,[iStdBagOnTrayCount]
			   ,[iOOGBagOnTrayCount]
			   ,[iStdTrayStackCount]
			   ,[iOOGTrayStackCount]
			   ,[iOtherContainerTypeCount])
	SELECT getdate() LastUpdateDT
		   ,SUM(CASE WHEN lc.iLocationStatus = 29 THEN 1 ELSE 0 END ) AvailableCount 
		   ,SUM(CASE WHEN lc.iEmptyFlag = 22 THEN 1 ELSE 0 END) OccupiedCount
		   ,SUM(CASE WHEN lc.iLocationStatus > 29 THEN 1 ELSE 0 END ) UnavailableCount
		   ,SUM(CASE WHEN ldl.sItem = 'Empty_Tray_Stack' THEN 1 ELSE 0 END ) * Sum(ldl.fCurrentQuantity) StdEmptyTrayCount
		   ,SUM(CASE WHEN ldl.sItem = 'OOG_Empty_Tray_Stack' THEN 1 ELSE 0 END ) * SUM(ldl.fCurrentQuantity)  OOGEmptyTrayCount
		   ,SUM(CASE WHEN ldl.sItem = 'Bag_On_Tray' THEN 1 ELSE 0 END )  StdBagOnTrayCount
		   ,SUM(CASE WHEN ldl.sItem = 'OOG_Bag_On_Tray' THEN 1 ELSE 0 END ) OOGBagOnTrayCount
		   ,SUM(CASE WHEN ldl.sItem = 'Empty_Tray_Stack' THEN 1 ELSE 0 END ) StdTrayStackCount
		   ,SUM(CASE WHEN ldl.sItem = 'OOG_Empty_Tray_Stack' THEN 1 ELSE 0 END ) OOGTrayStackCount
		   ,SUM(CASE WHEN ( ldl.sItem not in('Empty_Tray_Stack','OOG_Empty_Tray_Stack', 'Bag_On_Tray','OOG_Bag_On_Tray') ) THEN 1 ELSE 0 END ) OtherContainerTypeCount

	from LOCATION lc left join  LOAD ld on lc.sAddress = ld.sAddress left join LOADLINEITEM ldl on ld.sLoadID = ldl.sLoadID 
	where lc.iLocationType = 10 

END