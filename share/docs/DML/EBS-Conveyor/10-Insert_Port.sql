Delete from [asrs].[PORT];

INSERT INTO [asrs].[PORT]
           ([sPortName]
           ,[sDeviceID]
           ,[iDirection]
           ,[iLastSequence]
           ,[iCommunicationMode]
           ,[sServerName]
           ,[sSocketNumber]
           ,[iRetryInterval]
           ,[iSndKeepAliveInterval]
           ,[iRcvKeepAliveInterval]
           ,[iEnableWrapping])
     VALUES
           ('9003-Port','9003',103,0,182,'localhost',4503,5000,60000,70000,1),
		   ('9004-Port','9004',103,0,182,'localhost',4504,5000,60000,70000,1),
		   ('9001-Port','9001',103,0,182,'localhost',4501,5000,60000,70000,1),
		   ('9002-Port','9002',103,0,182,'localhost',4502,5000,60000,70000,1)
          
GO


