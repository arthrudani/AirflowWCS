
DROP USER ReadOnly CASCADE;

CREATE USER ReadOnly IDENTIFIED BY ReadOnly DEFAULT TABLESPACE WRxJData;

GRANT CREATE SESSION TO ReadOnly;

REM ----------------------------------------------
REM -- This allows selection from ALL tables.   --
REM -- If you want to limit it, then do so with --
REM -- GRANT SELECT ON TableName TO ReadOnly;   --
REM -- instead of the following line.           --
REM ----------------------------------------------

GRANT SELECT ANY TABLE TO ReadOnly;