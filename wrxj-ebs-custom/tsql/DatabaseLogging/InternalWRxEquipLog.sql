/*=======================================================================*/
/* Database Equipment logging within the WRx database for standalone installations */
/*=======================================================================*/
CREATE TABLE [asrs].[WRXEQUIPLOG](
	[IDENTITY_NAME] [varchar](60) NULL,
	[DATE_TIME] [datetime2] NOT NULL,
	[DEVICEID] [varchar](60) NULL,
	[IDIRECTION] [int] NULL,
	[COUNT] [int] NULL,
	[DATA] [varchar](3000) NULL
) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_WRXEQUIPLOG_DATETIME] ON [asrs].[WRXEQUIPLOG]
(
	[DATE_TIME] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO




/* ASRS MetaData required */

INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (705, N'WRxEquipLog', N'COUNT', N'Count', N'N', 4, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (702, N'WRxEquipLog', N'DATA', N'Message', N'N', 5, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (701, N'WRxEquipLog', N'DATE_TIME', N'Timestamp', N'N', 1, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (703, N'WRxEquipLog', N'DEVICEID', N'DeviceID', N'N', 2, NULL, NULL, NULL)
GO
INSERT [asrs].[ASRSMETADATA] ([iID], [sDataViewName], [sColumnName], [sFullName], [sIsTranslation], [iDisplayOrder], [dModifyTime], [sAddMethod], [sUpdateMethod]) VALUES (704, N'WRxEquipLog', N'IDIRECTION', N'Direction', N'Y', 3, NULL, NULL, NULL)