
-- Update the Destintation Station column size
ALTER TABLE  [asrs].[ORDERHEADER] 
ALTER COLUMN sDestinationStation varchar(10);

-- add new column to Load table for Final Sort Location 

ALTER TABLE  [asrs].[LOAD] 
add sFinalSortLocationID varchar(10);

ALTER TABLE  [asrs].[PURCHASEORDERHEADER]
add sFinalSortLocationID varchar(10);

-- Add new Column into WEBNAVOPTION to arrange the wifgert item in the Dashboard
ALTER TABLE [asrs].[WEBNAVOPTION]
ADD [ORDERNO] [int] NOT NULL
CONSTRAINT [DF_WEBNAVOPTION_ORDERNO] DEFAULT (0)
WITH VALUES;

--Update the sDestID length from 4 to 12
ALTER TABLE  [asrs].[ROUTE] 
ALTER COLUMN sDestID varchar(12);

--Update the sFromID length from 4 to 12
ALTER TABLE  [asrs].[ROUTE] 
ALTER COLUMN sFromID varchar(12);


-- Drop if requierd
-- DROP TABLE [asrs].[WAREHOUSE_FINALSORTLOCATION]

-- Create new WAREHOUSE_FINALSORTLOCATION table in the DB : Script Date: 23/05/2022 4:22:45 pm
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [asrs].[WAREHOUSE_FINALSORTLOCATION](
[iID] [bigint] IDENTITY(1,1) NOT NULL,
[sWarehouse] [varchar](3) NOT NULL,
[sLocationID] [varchar](10) NULL,
 CONSTRAINT [WAREHOUSE_FINALSORTLOCATION_PK] PRIMARY KEY CLUSTERED
(
[iID] ASC
))


/****** Object:  Table [asrs].[TIMESLOTSCHEMADEF]    Script Date: 31/05/2022 4:14:22 PM ******/

-- Drop if requierd
-- Drop table [asrs].[TIMESLOTSCHEMADEF]
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [asrs].[TIMESLOTSCHEMADEF](
	[iSchemaID] [int] IDENTITY(1,1) NOT NULL,
	[sName] [nvarchar](50) NULL,
	[dCreatedDT] [datetime] NULL,
	[sCreatedBy] [nvarchar](50) NULL,
 CONSTRAINT [PK_TimeslotSchemaDef] PRIMARY KEY CLUSTERED 
(
	[iSchemaID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [EBSWRXJDATA]
) ON [EBSWRXJDATA]
GO

INSERT INTO [asrs].[TIMESLOTSCHEMADEF] ([sName],[dCreatedDT],[sCreatedBy])
 VALUES
 ( 'Schema-1', GETDATE(), 'InitialData'),
 ( 'Schema-2', GETDATE(), 'InitialData'),
 ( 'Schema-3', GETDATE(), 'InitialData');


/****** Object:  Table [asrs].[TIMESLOTCONFIG]    Script Date: 31/05/2022 4:12:59 PM ******/
--Drop table [asrs].[TIMESLOTCONFIG]
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [asrs].[TIMESLOTCONFIG](
	[iID] [bigint] IDENTITY(1,1) NOT NULL,
	[iTimeslotID] [int] NOT NULL,
	[iSchemaID] [int] NOT NULL,
	[sName] [nvarchar](50) NULL,
	[sStartTime] [nvarchar](5) NOT NULL,
 CONSTRAINT [PK_TimeslotConfig] PRIMARY KEY CLUSTERED 
(
	[iID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [EBSWRXJDATA]
) ON [EBSWRXJDATA]
GO

ALTER TABLE [asrs].[TIMESLOTCONFIG]  WITH CHECK ADD  CONSTRAINT [FK_TimeslotConfig_iSchemaId] FOREIGN KEY([iSchemaID])
REFERENCES [asrs].[TIMESLOTSCHEMADEF] ([iSchemaID])
GO

ALTER TABLE [asrs].[TIMESLOTCONFIG] CHECK CONSTRAINT [FK_TimeslotConfig_iSchemaId]
GO


INSERT INTO [asrs].[TIMESLOTCONFIG] ([iTimeslotID],[iSchemaID],[sName], [sStartTime])
 VALUES
 ( 1, 1, 'TIMESLOT_01:00', '01:00'),
 ( 2, 1, 'TIMESLOT_02:00', '02:00'),
 ( 3, 2, 'TIMESLOT_03:00', '03:00'),
 ( 4, 2, 'TIMESLOT_03:30', '03:30'),
 ( 5, 3, 'TIMESLOT_04:00', '04:00');
 
 
 /****** Object:  Table [asrs].[[TIMESLOT_LINK_LOCATIONS]]    Script Date: 31/05/2022 4:12:59 PM ******/
--Drop table [asrs].[TIMESLOT_LINK_LOCATIONS]
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO
 
 CREATE TABLE [asrs].[TIMESLOT_LINK_LOCATIONS](
[iID] [bigint] IDENTITY(1,1) NOT NULL,
[iTimeslotID] [int] NOT NULL,
[sWarehouse] [varchar](3) NOT NULL,
[sLocationID] [varchar](10) NULL,
CONSTRAINT [TIMESLOT_LINK_LOCATIONS_PK] PRIMARY KEY CLUSTERED
(
[iID] ASC
))


/***** Update PURCHASEORDERHEADER coloumnn sStoreStation length from 4 to 12  ******/
ALTER TABLE  [asrs].[PURCHASEORDERHEADER] 
ALTER COLUMN sStoreStation varchar(12);

/***** Drop and re create the CONSTRAINT iLocationDepth in Location Table  ******/
ALTER TABLE [asrs].[LOCATION] DROP CONSTRAINT [LOCATION_CH_iLocationDepth] 
GO

ALTER TABLE [asrs].[LOCATION]  WITH CHECK ADD  CONSTRAINT [LOCATION_CH_iLocationDepth] CHECK  (([iLocationDepth]>=(1) AND [iLocationDepth]<=(30)))
GO

ALTER TABLE [asrs].[LOCATION] CHECK CONSTRAINT [LOCATION_CH_iLocationDepth]
GO


ALTER TABLE  [asrs].[LOADLINEITEM] add sGlobalID varchar(30);
ALTER TABLE [asrs].[PURCHASEORDERLINE] ADD sGlobalID varchar(30);

ALTER TABLE [asrs].[LOAD] add sCurrentAddress varchar(10);

-- Increase the length of columns of TRANSACTIONHISTORY table
-- 4 --> 10
ALTER TABLE [asrs].[TRANSACTIONHISTORY] ALTER COLUMN sStation varchar(10) NULL;
ALTER TABLE [asrs].[TRANSACTIONHISTORY] ALTER COLUMN sToStation varchar(10) NULL;
-- 13 --> 17(Warehouse 3 digits + '-' + Address 10 digits + Shelf position 3 digit)
ALTER TABLE [asrs].[TRANSACTIONHISTORY] ALTER COLUMN sLocation varchar(17) NULL;
ALTER TABLE [asrs].[TRANSACTIONHISTORY] ALTER COLUMN sToLocation varchar(17) NULL;

-- Increase the length of sStationName column of STATION table
-- 4 --> 10
ALTER TABLE [asrs].[STATION] ALTER COLUMN sStationName varchar(10) NOT NULL;

-- Changing LOCATION_CH_iLocationType and STATION_CH_iStationType contraint by adding more types  

ALTER TABLE [asrs].[LOCATION] DROP CONSTRAINT [LOCATION_CH_iLocationType]
ALTER TABLE [asrs].[LOCATION]  WITH CHECK ADD  CONSTRAINT [LOCATION_CH_iLocationType] CHECK  (([iLocationType]>=(10) AND [iLocationType]<=(22)))
ALTER TABLE [asrs].[LOCATION] CHECK CONSTRAINT [LOCATION_CH_iLocationType]

ALTER TABLE [asrs].[STATION] DROP CONSTRAINT [STATION_CH_iStationType]
ALTER TABLE [asrs].[STATION]  WITH CHECK ADD  CONSTRAINT [STATION_CH_iStationType] CHECK  (([iStationType]>=(220) AND [iStationType]<=(239)))
ALTER TABLE [asrs].[STATION] CHECK CONSTRAINT [STATION_CH_iStationType]
-- Add a new column into WRXTOHOST to manage original message's sequence number
ALTER TABLE [asrs].[WRXTOHOST] ADD iOriginalSequence int NULL;
ALTER TABLE [asrs].[WRXTOHOST] ADD sent datetime2 NULL;
ALTER TABLE [asrs].[WRXTOHOST] ADD acked int NULL;
ALTER TABLE [asrs].[WRXTOHOST] ADD retryCount int NULL;
ALTER TABLE [asrs].[WRXTOHOST] WITH CHECK ADD CONSTRAINT [WRXTOHOSTATION_CH_acked] CHECK  (([acked]=(0) OR [acked]=(1) OR [acked]=(2) OR [acked]=(3)))

-- Create a new sequence for sequence number(1 ~ 32767 range)
CREATE SEQUENCE SACWrxtoHostRequest AS int MINVALUE 1 MAXVALUE 32767 CYCLE CACHE;

ALTER TABLE [asrs].[PURCHASEORDERHEADER] ALTER COLUMN [sFinalSortLocationID] varchar(10) NULL;
ALTER TABLE  [asrs].[LOAD] ALTER COLUMN sFinalSortLocationID varchar(10);
--NEED to drop the CONSTRAINT and FK before running following statements
ALTER TABLE [asrs].[DEDICATEDLOCATION]  ALTER COLUMN sAddress varchar(10);
ALTER TABLE  [asrs].[LOCATiON] ALTER COLUMN sAddress varchar(10);

DROP VIEW VIEW_LOADSCREEN;

CREATE VIEW VIEW_LOADSCREEN
(
    IID,
    SPARENTLOAD,
    SLOADID,
    SWAREHOUSE,
    SADDRESS,
    SSHELFPOSITION,
    SROUTEID,
    SCONTAINERTYPE,
    ILOADMOVESTATUS,
    DMOVEDATE,
    SLOADMESSAGE,
    ILOADPRESENCECHECK,
    IHEIGHT,
    ILENGTH,
    IWIDTH,
    SRECOMMENDEDZONE,
    SDEVICEID,
    SNEXTWAREHOUSE,
    SNEXTADDRESS,
    SNEXTSHELFPOSITION,
    SFINALWAREHOUSE,
    SFINALADDRESS,
    IAMOUNTFULL,
    SMCKEY,
    SBCRDATA,
    FWEIGHT,
    IGROUPNO,
    DMODIFYTIME,
    SADDMETHOD,
    SUPDATEMETHOD,
	SCURRENTADDRESS,
    LIID,
    SITEM,
    SLOT,
    SORDERID,
    FCURRENTQUANTITY,
	SLINEID
)
AS SELECT 
	   ld."IID",
           ld."SPARENTLOAD",
           ld."SLOADID",
           ld."SWAREHOUSE",
           ld."SADDRESS",
           ld."SSHELFPOSITION",
           ld."SROUTEID",
           ld."SCONTAINERTYPE",
           ld."ILOADMOVESTATUS",
           ld."DMOVEDATE",
           ld."SLOADMESSAGE",
           ld."ILOADPRESENCECHECK",
           ld."IHEIGHT",
           ld."ILENGTH",
           ld."IWIDTH",
           ld."SRECOMMENDEDZONE",
           ld."SDEVICEID",
           ld."SNEXTWAREHOUSE",
           ld."SNEXTADDRESS",
           ld."SNEXTSHELFPOSITION",
           ld."SFINALWAREHOUSE",
           ld."SFINALADDRESS",
           ld."IAMOUNTFULL",
           ld."SMCKEY",
           ld."SBCRDATA",
           ld."FWEIGHT",
           ld."IGROUPNO",
           ld."DMODIFYTIME",
           ld."SADDMETHOD",
           ld."SUPDATEMETHOD",
	   ld."SCURRENTADDRESS",
           li.iid    AS liid,
           li.sitem,
           li.slot,
           li.sorderid,
           li.fcurrentquantity,
		   li.sLineID
     FROM asrs.Load ld, asrs.LoadLineItem li
     WHERE ld.sloadid = li.sloadid;


IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[asrs].[MOVECOMMAND]') AND type in (N'U'))
DROP TABLE [asrs].[MOVECOMMAND]
GO

/****** Object:  Table [asrs].[MOVECOMMAND]    Script Date: 8/06/2023 9:54:40 am ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [asrs].[MOVECOMMAND](
	[iID] [bigint] IDENTITY(1,1) NOT NULL,
	[sDeviceID] [varchar](10) NOT NULL,
	[sLoadID] [varchar](10)  NULL,
	[sOrderID] [varchar](30) NULL,
	[sGlobalID] [varchar](10) NULL,
	[sItemID] [varchar](12) NULL,
	[sFlightNum] [varchar](8) NULL,
	[dFlightSTD] [datetime2](7) NULL,
	[sFinalSortLocationID] [varchar](12) NULL,
	[sFrom] [varchar](10) NOT NULL,
	[sToDest] [varchar](10) NOT NULL,
	[sCommand] [varchar](100) NULL,
	[iCmdStatus] [int] NOT NULL,
	[iCmdMoveType] [int] NOT NULL,
	[iCmdOrderType] [int] NOT NULL,
	[dCreatedDate] [datetime2](7) NULL,
	[dLastModifyDate] [datetime2](7) NULL,
	[sAddMethod] [varchar](2000) NULL,
	[sUpdateMethod] [varchar](2000) NULL,
 CONSTRAINT [MOVECOMMAND_PK] PRIMARY KEY NONCLUSTERED 
(
	[iID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [EBSWRXJDATA]
) ON [EBSWRXJDATA]
GO

DROP VIEW VIEW_WORK_MAINTENANCE;
CREATE VIEW VIEW_WORK_MAINTENANCE
(
    IID,
    SLOADID,
    SDEVICEID,
	SFROM,
	STODEST,
	ICMDMOVETYPE,
	ICMDSTATUS,
    SORDERID,
	SGLOBALID,
	SITEMID,
	SFLIGHTNUM,
	DFLIGHTSTD,
	SFINALSORTLOCATIONID,
	SCOMMAND,
	ICMDORDERTYPE,
	DCREATEDDATE,
	DLASTMODIFYDATE,
    SADDMETHOD,
    SUPDATEMETHOD
)
AS SELECT 
		   cmd."IID",
		   cmd."SLOADID",
		   cmd."SDEVICEID",
		   cmd."SFROM",
		   cmd."STODEST",
		   cmd."ICMDMOVETYPE",
		   cmd."ICMDSTATUS",
		   cmd."SORDERID",
		   cmd."SGLOBALID",
		   cmd."SITEMID",
		   cmd."SFLIGHTNUM",
		   cmd."DFLIGHTSTD",
		   cmd."SFINALSORTLOCATIONID",
		   cmd."SCOMMAND",
		   cmd."ICMDORDERTYPE",
		   cmd."DCREATEDDATE",
		   cmd."DLASTMODIFYDATE",
           cmd."SADDMETHOD",
           cmd."SUPDATEMETHOD"
      FROM asrs.MoveCommand cmd;	

--- add colummn in loadlineitem table (US - 927)

ALTER TABLE  [asrs].[LOADLINEITEM] add dExpectedDate [datetime2](7) NULL;

--- add colummn in port table (US - 1052)

ALTER TABLE [asrs].[PORT] add iEnableWrapping INTEGER DEFAULT (1) WITH VALUES;

-----for US 1164---

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[asrs].[[OCCUPANCY]]') AND type in (N'U'))
DROP TABLE [asrs].[OCCUPANCY]

GO

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [asrs].[OCCUPANCY](
 [iID] [bigint] IDENTITY(1,1) NOT NULL,
	[dLastMovementTime] [datetime2](7) NULL,
	[iAvailableCount] [int] NOT NULL,
	[iOccupiedCount] [int] NOT NULL,
	[iUnavailableCount] [int] NOT NULL,
	[iStdEmptyTrayCount] [int]  NULL,
	[iOOGEmptyTrayCount] [int]  NULL,
	[iStdBagOnTrayCount] [int]  NULL,
	[iOOGBagOnTrayCount] [int]  NULL,
	[iStdTrayStackCount] [int]  NULL,
	[iOOGTrayStackCount] [int]  NULL,
	[iOtherContainerTypeCount] [int]  NULL,
	CONSTRAINT [OCCUPANCY_PK] PRIMARY KEY NONCLUSTERED 
(
	[iID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [EBSWRXJDATA]
) ON [EBSWRXJDATA]
GO

-- KR: extending iWarehouseType in the [WAREHOUSE] table adding 65 and 66 

Alter TABLE [asrs].[WAREHOUSE]  drop CONSTRAINT [WAREHOUSE_CH_iWarehouseType]
GO
ALTER TABLE [asrs].[WAREHOUSE]  WITH CHECK ADD  CONSTRAINT [WAREHOUSE_CH_iWarehouseType] CHECK  (([iWarehouseType]=(64) OR [iWarehouseType]=(63) OR [iWarehouseType]=(65) OR [iWarehouseType]=(66) ))
GO
ALTER TABLE [asrs].[WAREHOUSE] CHECK CONSTRAINT [WAREHOUSE_CH_iWarehouseType]


-- Create Load Transaction History Table (US - 1136)

/***** Object:  Table [asrs].[LOADTRANSACTIONHISTORY]    Script Date: 22/05/2023 09:54:31 am *****/
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[asrs].[[LOADTRANSACTIONHISTORY]]') AND type in (N'U'))

DROP TABLE [asrs].[LOADTRANSACTIONHISTORY]
GO

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [asrs].[LOADTRANSACTIONHISTORY](
    [iID] [bigint] IDENTITY(1,1) NOT NULL,
    [sLoadID] [varchar](10) NULL,      -- (Display)
    [sContainerType][varchar](12)  NULL, -- defulat = 0  -- (Display)
	[sBarcode][varchar] (12) NULL,
    [sCarrier] [varchar] (3) NULL,
    [sGlobalID][varchar](10)  NULL, -- defulat = 0  -- (Display)
    [sFlightNum][varchar](8) NULL,-- defulat = 0  -- (Display)
    [dFlightSTD][datetime2](7) NULL,-- Format: YYYYMMDDHHMMSS - Example: 20221201134500 - defulat = 0 -- (Display)
	[dLastExpiryDate][datetime2](7) NULL,
	[dExpectedReceiptDate][datetime2](7) NULL,
	[sArrivalAisleID][varchar] (10) NULL,
	[sStorageLocationID][varchar] (10) NULL,
	[sStorageLifterID][varchar] (10) NULL,
	[dStorageLoadAtEBSDate][datetime2] (7) NULL,
	[dStorageLoadPickedbyLifterDate][datetime2] (7) NULL,
	[dStorageLoadDroppedbyLifterDate] [datetime2] (7) NULL,
	[sStorageShuttleID][varchar] (10) NULL,
	[dStorageLoadPickedbyShuttleDate][datetime2] (7) NULL,
	[dStorageLoadDroppedbyshuttleDate][datetime2] (7) NULL,
	[dRetrievalOrderDate][datetime2] (7) NULL,
	[dRetrievalShuttleID][varchar] (10) NULL,
	[dRetrievalLoadPickedbyShuttleDate][datetime2] (7) NULL,
	[dRetrievalLoadDroppedbyshuttleDate][datetime2] (7) NULL,
	[sRetrievalLifterID][varchar] (10) NULL,
	[dRetrievalLoadPickedbyLiffterDate][datetime2] (7) NULL, 
	[dRetrievalLoadDroppedbyLiffterDate][datetime2] (7) NULL,
	[sRetrievalLocationID][varchar] (10) NULL,
	[iStorageDuration][int] NULL,
	[iDwelveTime][int] NULL,
	[iRetrievalDuration][int] NULL,
	[iStorageLifterWaitingTime][int] NULL,
	[iStorageShuttleWaitingTime][int] NULL,
	[iRetrievalLifterWaitingTime][int] NULL,
	[iRetrievalShuttleWaitingTime][int] NULL,

CONSTRAINT [LOADTRANSACTIONHISTORY_PK] PRIMARY KEY NONCLUSTERED 
(
    [iID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [EBSWRXJDATA]
) ON [EBSWRXJDATA]

GO
