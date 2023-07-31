PROMPT ++++++++++++++++++++++++++++++++++++++++++++++++++
PROMPT +    Creating Asrs Data AND Index Tablespaces.   +
PROMPT ++++++++++++++++++++++++++++++++++++++++++++++++++

CREATE TABLESPACE WrxjData
DATAFILE '/usr4/wrxj_db/WrxjData.dbf' SIZE 50M REUSE;

ALTER DATABASE DATAFILE '/usr4/wrxj_db/WrxjData.dbf'
AUTOEXTEND ON NEXT 5M;

CREATE TABLESPACE WrxjIndex
DATAFILE '/usr4/wrxj_db/WrxjIndex.dbf' SIZE 50M REUSE;

ALTER DATABASE DATAFILE '/usr4/wrxj_db/WrxjIndex.dbf'
AUTOEXTEND ON NEXT 5M;
