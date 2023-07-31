/*=======================================================================*/
/* Database logging within the WRx database for standalone installations */
/*=======================================================================*/
CREATE TABLE [asrs].[WRXLOG](
	[IDENTITY_NAME] [varchar](60) NULL,
	[DATE_TIME] [datetime2] NOT NULL,
	[SOURCE] [varchar](60) NULL,
	[AREA] [varchar](60) NULL,
	[POSITION] [smallint] NULL,
	[DESCRIPTION] [varchar](3000) NULL,
	[SUBJECT] [varchar](60) NULL,
	[INFOWARNFATAL] [varchar](1) NULL
) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_WRXLOG_DATETIME] ON [asrs].[WRXLOG]
(
	[DATE_TIME] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO

CREATE SYNONYM [asrs].[S_AED_WRX_LOG_ERR] FOR [asrs].[WRXLOG]
GO

CREATE SYNONYM [asrs].[S_AED_WRX_LOG_INF] FOR [asrs].[WRXLOG]
GO

/* CONTROLLERCONFIG entries for the Log Cleanup task */
INSERT asrs.CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled, dModifyTime, sAddMethod, sUpdateMethod)
  VALUES ('TimedEventScheduler', 'Task.LogCleanup.class', 'LogCleanupTask', 'Task to clean up old logs', 1, 1, NULL, NULL, NULL)
INSERT asrs.CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled, dModifyTime, sAddMethod, sUpdateMethod)
  VALUES ('TimedEventScheduler', 'Task.LogCleanup.DaysToKeep', '7', 'Number of days to keep logs', 1, 1, NULL, NULL, NULL)
INSERT asrs.CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled, dModifyTime, sAddMethod, sUpdateMethod)
  VALUES ('TimedEventScheduler', 'Task.LogCleanup.interval', '1', 'Interval in hours', 1, 1, NULL, NULL, NULL)

  
  
 /* ASRSMetaData required */
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (693, N'WRxLog', N'AREA', N'Area', N'N', -1, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (694, N'WRxLog', N'DATE_TIME', N'Timestamp', N'N', 1, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (695, N'WRxLog', N'DESCRIPTION', N'Message', N'N', 10, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (696, N'WRxLog', N'IDENTITY_NAME', N'Product', N'N', -1, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (697, N'WRxLog', N'INFOWARNFATAL', N'Severity', N'N', -1, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (698, N'WRxLog', N'POSITION', N'Position', N'N', -1, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (699, N'WRxLog', N'SOURCE', N'Source', N'N', 6, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (700, N'WRxLog', N'SUBJECT', N'Subject', N'N', 7, NULL, NULL, NULL)