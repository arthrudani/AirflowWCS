/*==========================================================================*/
/* Add WRxJ-Web Tables to an ORACLE database 								*/
/*==========================================================================*/

PROMPT '++++++++++++++++++++++++++++++++++++++++'
PROMPT '+   Adding V_USER_AUTH_GROUPS          +'
PROMPT '++++++++++++++++++++++++++++++++++++++++'
CREATE OR REPLACE FORCE EDITIONABLE VIEW "ASRS"."V_USER_AUTH_GROUPS" ("USERID", "GROUPNAMES") AS 
  SELECT USERNAME AS USERID,
               LTRIM (
                   MAX (SYS_CONNECT_BY_PATH (AUTH_GROUP, ', '))
                       KEEP (DENSE_RANK LAST ORDER BY curr),
                   ', ')
                   AS GROUPNAMES
          FROM (SELECT USERNAME,
                       AUTH_GROUP,
                       ROW_NUMBER ()
                           OVER (PARTITION BY USERNAME ORDER BY AUTH_GROUP)
                           AS curr,
                         ROW_NUMBER ()
                             OVER (PARTITION BY USERNAME ORDER BY AUTH_GROUP)
                       - 1
                           AS prev
                  FROM WEBUSERAUTHGROUP)
      GROUP BY USERNAME
    CONNECT BY prev = PRIOR curr AND USERNAME = PRIOR USERNAME
    START WITH curr = 1;

PROMPT '++++++++++++++++++++++++++++++++++++++++'
PROMPT '+   Adding V_USER                      +'
PROMPT '++++++++++++++++++++++++++++++++++++++++'
CREATE OR REPLACE VIEW asrs.V_USER
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

PROMPT '++++++++++++++++++++++++++++++++++++++++'
PROMPT '+   Adding V_USER_SESSION              +'
PROMPT '++++++++++++++++++++++++++++++++++++++++'
CREATE OR REPLACE VIEW asrs.V_USER_SESSION
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

PROMPT '++++++++++++++++++++++++++++++++++++++++'
PROMPT '+   Adding EquipmentMonitorTabView     +'
PROMPT '++++++++++++++++++++++++++++++++++++++++'
CREATE OR REPLACE view asrs.EquipmentMonitorTabView as
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
;

PROMPT '++++++++++++++++++++++++++++++++++++++++'
PROMPT '+   Adding EquipmentMonitorTrackingView+'
PROMPT '++++++++++++++++++++++++++++++++++++++++'
create OR REPLACE view asrs.EquipmentMonitorTrackingView as
SELECT t.id
	  , s.sEMAltGraphicID as "sEMGraphicID"
	  , t.sEMDeviceID
	  , sEMTrackingID
	  , sEMBarcode
	  , sEMStatus
	  , sEMOrigin
	  , sEMDestination
	  , sEMSize
  FROM asrs.EQUIPMENTMONITORTRACKING t
  inner join asrs.EQUIPMENTMONITORSTATUS s on t.sEMGraphicID = s.sEMGraphicID
;

PROMPT '++++++++++++++++++++++++++++++++++++++++'
PROMPT '+   Adding EquipmentMonitorView        +'
PROMPT '++++++++++++++++++++++++++++++++++++++++'
CREATE OR REPLACE view asrs.EquipmentMonitorView as
	select s.sEMAltGraphicID as sEMGraphicID, s.sEMBehavior, s.sEMDescription, s.sEMStatusID, st.sEMStatusDescription, s.sEMStatusText1, s.sEMStatusText2, s.sEMErrorCode, s.sEMErrorText
		, st.sEMBackground, st.sEMForeground, s.sEMMOSID, s.iEMCanTrack
		, sum(case when t.sEMTrackingID is not null then 1 else 0 end) as iTrackingCount
	from asrs.EQUIPMENTMONITORSTATUS s
	left join asrs.EQUIPMENTMONITORSTATUSTYPE st on s.sEMStatusID = st.sEMStatusID
	left join asrs.EquipmentMonitorTracking t on t.sEMGraphicID = s.sEMGraphicID
	group by s.sEMAltGraphicID, s.sEMBehavior, s.sEMDescription, s.sEMStatusID, st.sEMStatusDescription, s.sEMStatusText1, s.sEMStatusText2, s.sEMErrorCode, s.sEMErrorText, st.sEMBackground, st.sEMForeground, s.sEMMOSID, s.iEMCanTrack
;
