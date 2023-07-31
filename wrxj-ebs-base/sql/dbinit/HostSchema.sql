REM *****************************************************************
REM               Create Host related schema names.
REM *****************************************************************
set sqlblanklines on;
connect sys@wrxj/asrs as sysdba
create user wrxjhost identified by asrs 
       default tablespace USERS
       temporary tablespace TEMP;
grant create session to wrxjhost;
grant dba to wrxjhost;
ALTER PROFILE DEFAULT LIMIT PASSWORD_LIFE_TIME UNLIMITED;


REM *****************************************************************
REM                   Create Host tables
REM *****************************************************************
connect wrxjhost@wrxj/asrs

REM *****************************************************************
REM               Create Host related Table Spaces.
REM *****************************************************************
CREATE TABLESPACE HostData
DATAFILE 'C:\oracle\product\10.2.0\oradata\wrxj\HostData.dbf' SIZE 10M REUSE;
ALTER DATABASE DATAFILE 'C:\oracle\product\10.2.0\oradata\wrxj\HostData.dbf'
AUTOEXTEND ON NEXT 5M;

CREATE TABLE WrxToHost
(
    dHostModifyTime     TIMESTAMP(6) DEFAULT SYSTIMESTAMP NOT NULL,
    iMessageSequence    INTEGER      NOT NULL,
    sMessageIdentifier  VARCHAR(50 CHAR)  NULL,
    sMessage            CLOB         DEFAULT empty_clob(),
    CONSTRAINT hst_out_pk PRIMARY KEY (iMessageSequence)
                     USING INDEX TABLESPACE HostData
) TABLESPACE HostData;

CREATE TABLE HostToWrx
(
    dHostModifyTime     TIMESTAMP(6) DEFAULT SYSTIMESTAMP NOT NULL,
    iMessageSequence    INTEGER      NOT NULL,
    sMessageIdentifier  VARCHAR(50 CHAR)  NOT NULL,
    sMessage            CLOB         DEFAULT empty_clob(),
    CONSTRAINT hst_in_pk PRIMARY KEY (iMessageSequence)
                        USING INDEX TABLESPACE HostData
) TABLESPACE HostData;

