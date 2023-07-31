use [ebswrxj];
GO

PRINT 'Deleting LOGIN Data'
DELETE FROM asrs.LOGIN;

PRINT 'Deleting EMPLOYEE Data'
DELETE FROM asrs.EMPLOYEE;

PRINT 'Deleting LOADLINEITEM Data'
TRUNCATE TABLE asrs.LOADLINEITEM;

PRINT 'Deleting LOAD Data'
DELETE FROM asrs.LOAD;

PRINT 'Deleting DEDICATEDLOCATION Data'
DELETE FROM asrs.DEDICATEDLOCATION;

PRINT 'Deleting ORDERLINE Data'
DELETE FROM asrs.ORDERLINE;

PRINT 'Deleting ORDERHEADER Data'
DELETE FROM asrs.ORDERHEADER;

PRINT 'Deleting PURCHASEORDERLINE Data'
DELETE FROM asrs.PURCHASEORDERLINE;

PRINT 'Deleting ROLEOPTION Data'
DELETE FROM asrs.ROLEOPTION;

PRINT 'Deleting SYNONYMS Data'
DELETE FROM asrs.SYNONYMS;

PRINT 'Deleting LOCATION Data'
DELETE FROM asrs.LOCATION;

PRINT 'Deleting DEVICE Data'
DELETE FROM asrs.DEVICE;

PRINT 'Deleting VEHICLEMOVE Data'
DELETE FROM asrs.VEHICLEMOVE;

PRINT 'Deleting VEHICLESYSTEMCMD Data'
DELETE FROM asrs.VEHICLESYSTEMCMD;

PRINT 'Deleting ASRSMETADATA Data'
DELETE FROM asrs.ASRSMETADATA;

PRINT 'Deleting CARRIER Data'
DELETE FROM asrs.CARRIER;

PRINT 'Deleting CONTAINERTYPE Data'
DELETE FROM asrs.CONTAINERTYPE;

PRINT 'Deleting CONTROLLERCONFIG Data'
DELETE FROM asrs.CONTROLLERCONFIG;

PRINT 'Deleting CUSTOMER Data'
DELETE FROM asrs.CUSTOMER;

PRINT 'Deleting TRANSACTIONHISTORY Data'
TRUNCATE TABLE asrs.TRANSACTIONHISTORY;

PRINT 'Deleting JVMCONFIG Data'
DELETE FROM asrs.JVMCONFIG;

PRINT 'Deleting HOSTTOWRX Data'
TRUNCATE TABLE asrs.HOSTTOWRX;

PRINT 'Deleting HOSTCONFIG Data'
DELETE FROM asrs.HOSTCONFIG;

PRINT 'Deleting HOSTOUTACCESS Data'
DELETE FROM asrs.HOSTOUTACCESS;

PRINT 'Deleting ITEMMASTER Data'
DELETE FROM asrs.ITEMMASTER;

PRINT 'Deleting LOADWORD Data'
DELETE FROM asrs.LOADWORD;

PRINT 'Deleting MOVE Data'
DELETE FROM asrs.MOVE;

PRINT 'Deleting PORT Data'
DELETE FROM asrs.PORT;

PRINT 'Deleting PURCHASEORDERHEADER Data'
DELETE FROM asrs.PURCHASEORDERHEADER;

PRINT 'Deleting REASONCODE Data'
DELETE FROM asrs.REASONCODE;

PRINT 'Deleting ROLE Data'
DELETE FROM asrs.ROLE;

PRINT 'Deleting ROUTE Data'
DELETE FROM asrs.ROUTE;

PRINT 'Deleting STATION Data'
DELETE FROM asrs.STATION;

PRINT 'Deleting SYSCONFIG Data'
DELETE FROM asrs.SYSCONFIG;

PRINT 'Deleting WAREHOUSE Data'
DELETE FROM asrs.WAREHOUSE;

PRINT 'Deleting WRXSEQUENCER Data'
DELETE FROM asrs.WRXSEQUENCER;

PRINT 'Deleting WRXTOHOST Data'
TRUNCATE TABLE asrs.WRXTOHOST;

PRINT 'Deleting ZONE Data'
DELETE FROM asrs.ZONE;

PRINT 'Deleting ZONEGROUP Data'
DELETE FROM asrs.ZONEGROUP;

go