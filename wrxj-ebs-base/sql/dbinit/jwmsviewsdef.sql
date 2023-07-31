Rem jwmsviewsdef.sql - file to create oracle database table views
SPOOL jwmsviewsdef.LOG
SET SQLBLANKLINES ON;

REM ---------------------------------------------------------------------------
REM ----------------------- Add Views Section ---------------------------------
REM----------------------------------------------------------------------------
REM ---------------------------------------------------------------------------

PROMPT ++++++++++++++++++++++++++++++++++++++++++++++++
PROMPT +     Adding MOVE VIEW                         +
PROMPT ++++++++++++++++++++++++++++++++++++++++++++++++


CREATE VIEW MOVEVIEW
(   
	IID,
	IMOVEID,
    SPARENTLOAD,
    SLOADID,
    SWAREHOUSE,
    SADDRESS,
    SITEM,
    SPICKLOT,
    SORDERLOT,
    FPICKQUANTITY,
    IPRIORITY,
    SORDERID,
    SROUTEID,
    IMOVECATEGORY,
    IMOVETYPE,
    IMOVESTATUS,
    DMOVEDATE,
    SSCHEDULERNAME,
    SPICKTOLOADID,
    IAISLEGROUP,
    IMOVESEQUENCE,
    SRELEASETOCODE,
    SLINEID,
    SDEVICEID,
    SDESTWAREHOUSE,
    SDESTADDRESS,
    SNEXTWAREHOUSE,
    SNEXTADDRESS,
    SPOSITIONID
)
AS SELECT
	mv.IID,
    mv.IMOVEID,
    ld.SPARENTLOAD,
    ld.SLOADID,
    ld.SWAREHOUSE,
    ld.SADDRESS,
    mv.SITEM,
    mv.SPICKLOT,
    mv.SORDERLOT,
    mv.FPICKQUANTITY,
    mv.IPRIORITY,
    mv.SORDERID,
    mv.SROUTEID,
    mv.IMOVECATEGORY,
    mv.IMOVETYPE,
    mv.IMOVESTATUS,
    mv.DMOVEDATE,
    dv.SSCHEDULERNAME,
    mv.SPICKTOLOADID,
    lc.IAISLEGROUP,
    lc.IMOVESEQUENCE,
    oh.SRELEASETOCODE ,
    mv.SLINEID,
    mv.SDEVICEID,
    mv.SDESTWAREHOUSE,
    mv.SDESTADDRESS,
    mv.SNEXTWAREHOUSE,
    mv.SNEXTADDRESS,
    mv.SPOSITIONID
FROM asrs.DEVICE dv
     JOIN asrs.LOAD ld ON (dv.SDEVICEID = ld.SDEVICEID)
     JOIN asrs.MOVE mv ON (ld.SLOADID = mv.sLOADID)
     JOIN asrs.LOCATION lc ON (lc.SWAREHOUSE = ld.SWAREHOUSE AND lc.SADDRESS = ld.SADDRESS)
     LEFT OUTER JOIN asrs.ORDERHEADER oh ON (mv.SORDERID = oh.SORDERID);
