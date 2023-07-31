INSERT INTO PORT
   (SPORTNAME, SDEVICEID, IDIRECTION, ILASTSEQUENCE, ICOMMUNICATIONMODE, SSERVERNAME, SSOCKETNUMBER, IRETRYINTERVAL, ISNDKEEPALIVEINTERVAL, IRCVKEEPALIVEINTERVAL)
 VALUES
   ('SRC1-MosPort', 'SRC1-Mos', 103, 0, 183, 'localhost', '7501', 5000, 60000, 70000);
INSERT INTO PORT
   (SPORTNAME, SDEVICEID, IDIRECTION, ILASTSEQUENCE, ICOMMUNICATIONMODE, SSERVERNAME, SSOCKETNUMBER, IRETRYINTERVAL, ISNDKEEPALIVEINTERVAL, IRCVKEEPALIVEINTERVAL)
 VALUES
   ('SRC2-MosPort', 'SRC2-Mos', 103, 0, 183, 'localhost', '7502', 5000, 60000, 70000);
INSERT INTO PORT
   (SPORTNAME, SDEVICEID, IDIRECTION, ILASTSEQUENCE, ICOMMUNICATIONMODE, SSERVERNAME, SSOCKETNUMBER, IRETRYINTERVAL, ISNDKEEPALIVEINTERVAL, IRCVKEEPALIVEINTERVAL)
 VALUES
   ('SRC1-Port', 'SRC1', 103, 0, 183, 'localhost', '6211', 5000, 60000, 70000);
INSERT INTO PORT
   (SPORTNAME, SDEVICEID, IDIRECTION, ILASTSEQUENCE, ICOMMUNICATIONMODE, SSERVERNAME, SSOCKETNUMBER, IRETRYINTERVAL, ISNDKEEPALIVEINTERVAL, IRCVKEEPALIVEINTERVAL)
 VALUES
   ('SRC2-Port', 'SRC2', 103, 0, 183, 'localhost', '6212', 5000, 60000, 70000);
COMMIT;
