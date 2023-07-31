USE [EBSWRXJ]
GO

/****** Object:  View [dbo].[VIEW_LOADSCREEN]    Script Date: 6/12/2021 10:44:21 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


CREATE OR ALTER VIEW [dbo].[VIEW_LOADSCREEN]
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
    LIID,
    SITEM,
    SLOT,
    SORDERID,
    FCURRENTQUANTITY
)
AS SELECT distinct
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
           li.iid    AS liid,
           li.sitem,
           li.slot,
           li.sorderid,
           li.fcurrentquantity
		   FROM asrs.Load ld with (nolock) left join asrs.LoadLineItem li with (nolock)
			on ld.sloadid = li.sloadid;
      --FROM asrs.Load ld with (nolock), asrs.LoadLineItem li with (nolock)
    -- WHERE ld.sloadid = li.sloadid;

GO


