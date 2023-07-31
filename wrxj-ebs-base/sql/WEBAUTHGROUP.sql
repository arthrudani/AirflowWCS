Insert into ASRS.WEBAUTHGROUP
   (ID, NAME, DESCRIPTION)
 Values
   (1, 'ROLE_ADMIN', 'Has access to nearly every portion of the application except the status consoles. ');
Insert into ASRS.WEBAUTHGROUP
   (ID, NAME, DESCRIPTION)
 Values
   (2, 'ROLE_MASTER', 'Has access to everything and everyone. Big brother. ');
Insert into ASRS.WEBAUTHGROUP
   (ID, NAME, DESCRIPTION)
 Values
   (3, 'ROLE_USER', 'Picking, system screens, and perform picking action.');
Insert into ASRS.WEBAUTHGROUP
   (ID, NAME, DESCRIPTION)
 Values
   (4, 'ROLE_READONLY', 'Has access to system information screens to read data. No picking or configuration access. ');
Insert into ASRS.WEBAUTHGROUP
   (ID, NAME, DESCRIPTION)
 Values
   (5, 'ROLE_ELEVATED', 'Elevated Picking, system screens, and elevated picking actions. ');
COMMIT;
