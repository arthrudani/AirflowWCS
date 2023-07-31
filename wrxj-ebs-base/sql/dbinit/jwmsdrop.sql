/*****************************************************************************
        NAME:  JWMSDROP
      AUTHOR:  A.D.
        DATE:  15-Oct-2008
     PURPOSE:  PL/SQL script to drop all tables for WarehouseRx.  Any table that
               is part of the WRXJDATA tablespace will be droppped.
  ****************************************************************************/
SET SERVEROUTPUT ON SIZE 30000;
CLEAR SCREEN
PROMPT
PROMPT Please Wait...
PROMPT

DECLARE
  CURSOR curs_Tables IS
    SELECT * FROM user_tables WHERE tablespace_name = 'WRXJDATA' or temporary = 'Y';
  v_Table curs_Tables%ROWTYPE;
  
  CURSOR curs_Views IS
    SELECT * FROM user_views;
  v_View curs_Views%ROWTYPE;
  
BEGIN
  DBMS_OUTPUT.PUT_LINE('=================  Dropping Tables  =================');

  FOR v_Table IN curs_Tables LOOP
    EXECUTE IMMEDIATE 'DROP TABLE '||v_Table.table_name||' CASCADE CONSTRAINTS';
    DBMS_OUTPUT.PUT_LINE('DROPPED '||v_Table.table_name);
  END LOOP;
  
  DBMS_OUTPUT.PUT_LINE('=================  Dropping Views  =================');
  
  FOR v_View IN curs_Views LOOP
    EXECUTE IMMEDIATE 'DROP VIEW '||v_View.view_name;
    DBMS_OUTPUT.PUT_LINE('DROPPED '||v_View.view_name);
  END LOOP;
  
  DBMS_OUTPUT.PUT_LINE('==============  Dropping TH Sequence  ==============');
  EXECUTE IMMEDIATE 'DROP SEQUENCE TH_SEQUENCE';
    DBMS_OUTPUT.PUT_LINE('DROPPED TH_SEQUENCE');

  EXECUTE IMMEDIATE 'PURGE RECYCLEBIN';
  
END;
.
/   -- Required in sqlplus, ignore the error in Toad.
