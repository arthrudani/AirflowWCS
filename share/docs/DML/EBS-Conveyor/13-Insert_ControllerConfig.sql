USE [EBSWRXJ]
GO

INSERT INTO [asrs].[CONTROLLERCONFIG]
           ([sController]
           ,[sPropertyName]
           ,[sPropertyValue]
           ,[sPropertyDesc]
           ,[iScreenChangeAllowed]
           ,[iEnabled]
           ,[dModifyTime]
           ,[sAddMethod]
           ,[sUpdateMethod])
     VALUES
           ('TimedEventScheduler', 'Task.EBSExpiredBagTask.class','com.daifukuoc.wrxj.custom.ebs.scheduler.event.EBSExpiredBagTask', NULL, 2, 1, GETDATE() ,NULL ,NULL),
		   ('TimedEventScheduler', 'Task.EBSExpiredBagTask.interval','10', 'Number of seconds between runs.', 2, 1, GETDATE() ,NULL ,NULL);

GO

-- Rolle back query
--DROP FROM [asrs].[CONTROLLERCONFIG] WHERE sController ='TimedEventScheduler' AND ([sPropertyName] = 'Task.EBSExpiredBagTask.class' OR [sPropertyName] = 'Task.EBSExpiredBagTask.interval');
