INSERT INTO [asrs].[SYSCONFIG]
           ([sGroup]
           ,[sParameterName]
           ,[sParameterValue]
           ,[sDescription]
           ,[iScreenChangeAllowed]
           ,[iEnabled])
     VALUES
           ('ConveyorWH','ReleaseWindowPeriodInMin','120','ConveyorWH Release Win Period in Minutes',2,1),
		   ('ConveyorWH','StoreNonOverlappingBags','true','Not store = false, Store = true',2,1);