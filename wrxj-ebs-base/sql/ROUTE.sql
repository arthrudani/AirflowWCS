Insert into ROUTE
   (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, IROUTEONOFF)
 Values
   ('0201', 'SRC2', '0201', 231, 232, 35);
Insert into ROUTE
   (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, IROUTEONOFF)
 Values
   ('0201', '0201', '0202', 232, 232, 35);
Insert into ROUTE
   (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, IROUTEONOFF)
 Values
   ('0101', 'SRC1', '0101', 231, 232, 35);
Insert into ROUTE
   (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, IROUTEONOFF)
 Values
   ('0201', '0202', 'SRC2', 232, 231, 35);
Insert into ROUTE
   (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, IROUTEONOFF)
 Values
   ('0101', '0101', 'SRC1', 232, 231, 35);
Insert into ROUTE
   (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, IROUTEONOFF)
 Values
   ('SHIP', 'SRC1', '0101', 231, 232, 35);
Insert into ROUTE
   (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, IROUTEONOFF)
 Values
   ('SHIP', 'SRC2', '0201', 231, 232, 35);
Insert into ROUTE
   (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, IROUTEONOFF)
 Values
   ('SHIP', '0101', 'SHIP', 232, 232, 35);
Insert into ROUTE
   (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, IROUTEONOFF)
 Values
   ('SHIP', '0201', 'SHIP', 232, 232, 35);
COMMIT;
