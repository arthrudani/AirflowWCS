SET DEFINE OFF;
Insert into CONTROLLERCONFIG
   (SCONTROLLER, SPROPERTYNAME, SPROPERTYVALUE, SPROPERTYDESC, ISCREENCHANGEALLOWED, 
    IENABLED, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('WebMessageController', 'class', 'com.daifukuamerica.wrxj.web.stomp.controller.WebMessageController', '', '1', 
    '1', '', '', '');
Insert into CONTROLLERCONFIG
   (SCONTROLLER, SPROPERTYNAME, SPROPERTYVALUE, SPROPERTYDESC, ISCREENCHANGEALLOWED, 
    IENABLED, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('WebMessageController', 'type', 'WebMessageController', 'Message Relay to Web Clients', '1', 
    '1', '', '', '');
COMMIT;
