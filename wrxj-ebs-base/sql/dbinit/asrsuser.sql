
DROP USER asrs CASCADE;

CREATE USER asrs IDENTIFIED BY asrs DEFAULT TABLESPACE WRxJData;

DROP PROFILE nolimit;

CREATE PROFILE nolimit limit
  sessions_per_user UNLIMITED
  cpu_per_session UNLIMITED
  cpu_per_call UNLIMITED
  logical_reads_per_session UNLIMITED
  idle_time UNLIMITED
  connect_time UNLIMITED
  password_life_time UNLIMITED;

DROP ROLE client;

CREATE ROLE client;
GRANT CREATE SESSION TO client;
GRANT CREATE TABLE TO client;
GRANT CREATE CLUSTER TO client;
GRANT CREATE SEQUENCE TO client;
GRANT CREATE PROCEDURE TO client;


GRANT client TO asrs;
GRANT DBA TO asrs;
GRANT UNLIMITED TABLESPACE TO asrs;
ALTER USER asrs PROFILE nolimit;
