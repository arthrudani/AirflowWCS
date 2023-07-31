REM ** SHOW LOCKS ** 

SELECT OBJECT_NAME, STATE, S.SID, S.serial#, CREATED, machine, S.program, ORACLE_USERNAME, S.osuser, s.module, s.action
  FROM sys.v_$locked_object LO, sys.all_objects AO, sys.v_$session S
 WHERE LO.object_id = AO.object_id
   AND S.SID = LO.session_id
   ORDER BY created, s.SID, s.serial#, object_name;
	   
REM ** KILL SESSION ** -> ALTER SYSTEM KILL SESSION 'sid,serial#';

ALTER SYSTEM KILL SESSION '104,4914';	   
	   
REM ** SHOW WRx-J USERS **
	   
SELECT s.SID, p.spid, machine, s.username, s.osuser, s.program, s.module, s.action
FROM   v$process p,
       v$session s
WHERE  p.addr = s.paddr AND s.username='ASRS'
ORDER BY machine, module



-- Alternate Lock display
SELECT s.username USER_NAME, owner OBJ_OWNER, object_name, object_type, s.osuser,
    DECODE(l.block,
        0, 'Not Blocking',
        1, 'Blocking',
        2, 'Global'
        ) STATUS,
    DECODE(v.locked_mode,
        0, 'None',
        1, 'Null',
        2, 'Row-S (SS)',
        3, 'Row-X (SX)',
        4, 'Share',
        5, 'S/Row-X (SSX)',
        6, 'Exclusive', TO_CHAR(lmode)
        ) MODE_HELD,
    s.sid as session_id, s.serial#,
    s.machine, s.module, s.action
FROM gv$locked_object v, dba_objects d, gv$lock l, gv$session s
WHERE v.object_id = d.object_id
  AND v.object_id = l.id1
  AND v.session_id = s.sid
ORDER BY s.username, v.session_id;


