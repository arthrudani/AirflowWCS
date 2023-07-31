SET DEFINE OFF;
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('AllocationStrategy', 'LineAlloc', 'LineAllocation', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('AllocationStrategy', 'Full Load', 'FullLoadOut', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('Archiver', 'DataType', 'XML', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('Archiver', 'class', 'com.daifukuamerica.wrxj.archive.tranhist.TransactionHistoryArchiver', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('AllocationStrategy', 'Piece', 'PieceAllocation', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('AllocationStrategy', 'BestFit', 'BestFitLoadAllocation', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('AllocationStrategy', 'Cycle Count', 'CycleCountAllocation', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('Login', 'RestrictedAccessWarning00', 'This is a customizable warning.\n', 1, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('Login', 'RestrictedAccessWarning01', 'See the JavaDoc for SysConfig.getRestrictedAccessWarning() for details.\n\n', 1, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('Login', 'RestrictedAccessWarning10', 'Unauthorized access to or use of this system is prohibited.\n', 1, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('Login', 'RestrictedAccessWarning11', 'All access and use may be monitored and recorded.', 1, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'ut-mandrus-l', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'ut-dstout-d', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'myr-wrx-1', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'ut-bhall-l', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'ut-dstout-l', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'ut-bphilllips-l', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'ut-jprojects-s', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'ut-ykang-l', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'ut-jmarquez-l', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'ut-bphillips-l', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'dj2hsxr1', 'su', 2, 2);
Insert into SYSCONFIG
   (SGROUP, SPARAMETERNAME, SPARAMETERVALUE, ISCREENCHANGEALLOWED, IENABLED)
 Values
   ('LastLogin', 'dj2gvxr1', 'su', 2, 2);
COMMIT;
