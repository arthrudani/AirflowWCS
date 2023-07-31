PROMPT ++++++++++++++++++++++++++++++++++++++++++++++++++
PROMPT +        Creating JBoss DATA Tablespaces         +
PROMPT ++++++++++++++++++++++++++++++++++++++++++++++++++

CREATE TABLESPACE JBossData
DATAFILE 'D:\oracle\product\10.2.0\oradata\JBossJMS\JBossData.dbf' SIZE 10M REUSE;

ALTER DATABASE DATAFILE 'D:\oracle\product\10.2.0\oradata\JBossJMS\JBossData.dbf'
AUTOEXTEND ON NEXT 5M;

CREATE USER jboss IDENTIFIED BY jboss DEFAULT TABLESPACE JBossData;

CREATE PROFILE nolimit LIMIT
  SESSIONS_PER_USER UNLIMITED
  CPU_PER_SESSION UNLIMITED
  CPU_PER_CALL UNLIMITED
  LOGICAL_READS_PER_SESSION UNLIMITED
  IDLE_TIME UNLIMITED
  CONNECT_TIME UNLIMITED;

CREATE ROLE client;
GRANT CREATE SESSION TO client;
GRANT CREATE TABLE TO client;
GRANT CREATE CLUSTER TO client;
GRANT CREATE SEQUENCE TO client;
GRANT CREATE PROCEDURE TO client;

GRANT client TO jboss;
GRANT DBA TO jboss;
GRANT UNLIMITED TABLESPACE TO jboss;
ALTER USER jboss PROFILE nolimit;

ALTER SYSTEM SET PROCESSES=1000 SCOPE=SPFILE;