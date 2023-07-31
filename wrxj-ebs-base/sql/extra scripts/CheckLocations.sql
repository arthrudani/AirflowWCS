SELECT 'NO LOAD', location.sWarehouse, location.sAddress
    FROM location 
    WHERE (location.iEmptyFlag = 22) AND (location.iLocationtype = 10)
    AND NOT EXISTS
    (SELECT sloadid FROM load WHERE load.sWarehouse = location.sWarehouse AND
        load.sAddress = location.sAddress);

SELECT 'EXTRA LOAD', location.sWarehouse, location.sAddress
    FROM location
    WHERE location.iEmptyFlag = 21 AND (location.iLocationtype = 10)
    AND EXISTS 
       (SELECT sloadid FROM load WHERE load.sWarehouse = location.sWarehouse AND
        load.sAddress = location.sAddress);

SELECT 'RESERVED-OCCUPIED', location.sWarehouse, location.sAddress
    FROM location
    WHERE location.iEmptyFlag = 23 AND (location.iLocationtype = 10)
    AND EXISTS 
       (SELECT sloadid FROM load WHERE load.sWarehouse = location.sWarehouse AND
        load.sAddress = location.sAddress);

SELECT 'RESERVED-NOT', location.sWarehouse, location.sAddress
    FROM location
    WHERE location.iEmptyFlag = 23 AND (location.iLocationtype = 10)
    AND NOT EXISTS 
       (SELECT sloadid FROM load WHERE
       (load.sNextWarehouse = location.sWarehouse AND load.sNextAddress = location.sAddress) OR
       (load.sFinalWarehouse = location.sWarehouse AND load.sFinalAddress = location.sAddress));
