PROMPT ++++++++++++++++++++++++++++++++++++++++++++++++++
PROMPT +    Creating WRxJ Data AND INDEX Tablespaces.   +
PROMPT ++++++++++++++++++++++++++++++++++++++++++++++++++
CREATE TABLESPACE WRxJData
DATAFILE 'C:\Oracle\Oracle12c\oradata\wrxj\WRxJData.dbf' SIZE 50M REUSE;

ALTER DATABASE DATAFILE 'C:\Oracle\Oracle12c\oradata\wrxj\WRxJData.dbf'
AUTOEXTEND ON NEXT 5M;

CREATE TABLESPACE WRxJIndex
DATAFILE 'C:\Oracle\Oracle12c\oradata\wrxj\WRxJIndex.dbf' SIZE 50M REUSE;

ALTER DATABASE DATAFILE 'C:\Oracle\Oracle12c\oradata\wrxj\WRxJIndex.dbf'
AUTOEXTEND ON NEXT 5M;
