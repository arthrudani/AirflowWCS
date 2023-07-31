/*==========================================================================*/
/* Add WRxJ-Web Views to a MICROSOFT SQL SERVER database 					*/
/*==========================================================================*/
USE [WRxJ]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding V_USER_AUTH_GROUPS          +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
GO
CREATE VIEW asrs.V_USER_AUTH_GROUPS
(
	USERID,
	GROUPNAMES
)
AS
	SELECT distinct username as USERID,
			STUFF((SELECT ',' + AUTH_GROUP FROM asrs.WEBUSERAUTHGROUP wuag2 where wuag1.USERNAME = wuag2.USERNAME ORDER BY AUTH_GROUP FOR XML PATH('')), 1, 1, '') AS GROUPNAMES
			FROM asrs.WEBUSERAUTHGROUP wuag1
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding V_USER                      +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
GO
CREATE VIEW asrs.V_USER
(
	ID,
	USERNAME,
	WRXROLE,
	GROUPNAMES
)
AS
	SELECT u.SUSERID	AS ID,
		   u.SUSERNAME  AS USERNAME,
		   u.SROLE	  AS WRXROLE,
		   g.GROUPNAMES AS GROUPNAMES
	  FROM EMPLOYEE u LEFT JOIN V_USER_AUTH_GROUPS g ON u.SUSERID = g.USERID;
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding V_USER_SESSION              +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
GO
CREATE VIEW asrs.V_USER_SESSION
(
	SUSERID,
	DLOGINTIME,
	SMACHINENAME,
	SIPADDRESS,
	GROUPNAMES
)
AS
	SELECT a.SUSERID,
		   a.DLOGINTIME,
		   a.SMACHINENAME,
		   a.SIPADDRESS,
		   wag.GROUPNAMES
	  FROM (  SELECT l.SUSERID,
					 l.DLOGINTIME,
					 l.SMACHINENAME,
					 l.SIPADDRESS
				FROM LOGIN l
					 LEFT JOIN WEBUSERAUTHGROUP h ON l.SUSERID = h.USERNAME
			GROUP BY l.SUSERID,
					 l.DLOGINTIME,
					 l.SMACHINENAME,
					 l.SIPADDRESS) a
		   LEFT JOIN V_USER wag ON a.SUSERID = wag.ID;
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding EquipmentMonitorTabView     +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
GO
CREATE view asrs.EquipmentMonitorTabView as
with WorstStatus as
(
	select eqt.sEMGraphicTab, max(st.iEMDisplayPriority) as iWorstPriority
	from EquipmentMonitorTab eqt
	inner join asrs.EQUIPMENTMONITORSTATUS s on s.sEMGraphicID = eqt.sEMGraphicID
	inner join asrs.EQUIPMENTMONITORSTATUSTYPE st on s.sEMStatusID = st.sEMStatusID
	group by eqt.sEMGraphicTab
)
select sEMGraphicTab, st.sEMStatusID, st.sEMBackground, st.sEMForeground
	from WorstStatus ws
	inner join asrs.EQUIPMENTMONITORSTATUSTYPE st on ws.iWorstPriority = st.iEMDisplayPriority
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding EquipmentMonitorTrackingView+'
PRINT '++++++++++++++++++++++++++++++++++++++++'
GO
create view asrs.EquipmentMonitorTrackingView as
SELECT t.iid
	  , s.sEMAltGraphicID as SEMGRAPHICID
	  , t.sEMDeviceID
	  , sEMTrackingID
	  , sEMBarcode
	  , sEMStatus
	  , sEMOrigin
	  , sEMDestination
	  , sEMSize
  FROM asrs.EQUIPMENTMONITORTRACKING t
  inner join asrs.EQUIPMENTMONITORSTATUS s on t.sEMGraphicID = s.sEMGraphicID
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding EquipmentMonitorView        +'
PRINT '++++++++++++++++++++++++++++++++++++++++'
GO
CREATE view asrs.EquipmentMonitorView as
	select s.sEMAltGraphicID as sEMGraphicID, s.sEMBehavior, s.sEMDescription, s.sEMStatusID, st.sEMStatusDescription, s.sEMStatusText1, s.sEMStatusText2, s.sEMErrorCode, s.sEMErrorText
		, st.sEMBackground, st.sEMForeground, s.sEMMOSID, s.iEMCanTrack
		, sum(case when t.sEMTrackingID is not null then 1 else 0 end) as iTrackingCount
	from asrs.EQUIPMENTMONITORSTATUS s
	left join asrs.EQUIPMENTMONITORSTATUSTYPE st on s.sEMStatusID = st.sEMStatusID
	left join asrs.EquipmentMonitorTracking t on t.sEMGraphicID = s.sEMGraphicID
	group by s.sEMAltGraphicID, s.sEMBehavior, s.sEMDescription, s.sEMStatusID, st.sEMStatusDescription, s.sEMStatusText1, s.sEMStatusText2, s.sEMErrorCode, s.sEMErrorText, st.sEMBackground, st.sEMForeground, s.sEMMOSID, s.iEMCanTrack
GO
