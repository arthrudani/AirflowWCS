/*==========================================================================*/
/* Add WRxJ-Web Tables to a MICROSOFT SQL SERVER database 					*/
/*==========================================================================*/
USE [WRxJ]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding WEBAUTHGROUP                +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
CREATE TABLE [asrs].[WEBAUTHGROUP](
	[ID] [int] NULL,
	[NAME] [nvarchar](26) NULL,
	[DESCRIPTION] [nvarchar](120) NULL
) ON [WRxJData]
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding WEBNAVGROUP                 +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
CREATE TABLE [asrs].[WEBNAVGROUP](
	[ID] [bigint] IDENTITY(1,1) NOT NULL,
	[NAME] [nvarchar](26) NULL,
	[ICON] [nvarchar](100) NULL,
	[STYLE] [nvarchar](100) NULL,
	[NAVORDER] [int] CONSTRAINT [DF_WEBNAVGROUP_NAVORDER] DEFAULT ((0)) NOT NULL,
	
	CONSTRAINT [PK_WEBNAVGROUP_ID] PRIMARY KEY CLUSTERED ([ID] ASC) ON [WRxJIndex],
	CONSTRAINT [UK_WEBNAVGROUP_NAME] UNIQUE NONCLUSTERED ([NAME] ASC) ON [WRxJIndex]
) ON [WRxJData]
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding WEBNAVOPTION                +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
CREATE TABLE [asrs].[WEBNAVOPTION](
	[ID] [bigint] IDENTITY(1,1) NOT NULL,
	[AUTHGROUPNAME] [nvarchar](26) NULL,
	[NAVGROUPNAME] [nvarchar](26) NULL,
	[NAME] [nvarchar](50) NULL,
	[LINK] [nvarchar](200) NULL,
	[DESCRIPTION] [nvarchar](100) NULL,
	[ICON] [nvarchar](100) NULL,
	[FAVORITE] [int] CONSTRAINT [DF_WEBNAVOPTION_FAVORITE]  DEFAULT ((0)) NOT NULL,
	[ORDERNO] [int] CONSTRAINT [DF_WEBNAVOPTION_ORDERNO]  DEFAULT ((0)) NOT NULL,
	
	CONSTRAINT [PK_WEBNAVOPTION_ID] PRIMARY KEY CLUSTERED ([ID] ASC) ON [WRxJIndex],
	CONSTRAINT [UK_WEBNAVOPTION_NAME] UNIQUE NONCLUSTERED ([NAME] ASC) ON [WRxJIndex]
) ON [WRxJData]
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding WEBUSERAUTHGROUP            +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
CREATE TABLE [asrs].[WEBUSERAUTHGROUP](
	[AUTH_USER_GROUP_ID] [int] NULL,
	[USERNAME] [nvarchar](26) NULL,
	[AUTH_GROUP] [nvarchar](26) NULL
) ON [WRxJData]
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding WEBUSERPREF                 +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
CREATE TABLE [asrs].[WEBUSERPREF](
	[ID] [int] NULL,
	[USER_ID] [nvarchar](26) NULL,
	[PREF_KEY] [nvarchar](56) NULL,
	[PREF_VALUE] [nvarchar](56) NULL,
	[PREF_DESC] [nvarchar](56) NULL
) ON [WRxJData]
GO
