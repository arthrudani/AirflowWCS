--delete unknown locations
delete from  asrs.LOCATION where saddress in ('6151','6251','6651','6751')
-- corrctiong the location type
update asrs.LOCATION set sZone ='OOG', iLocationType = 22  where saddress in ('6152','6252',  '6652','6752')

-- WHS left side
update asrs.LOCATION set iSearchOrder = 1 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6141' 
update asrs.LOCATION set iSearchOrder = 2 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6241'
update asrs.LOCATION set iSearchOrder = 3 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6131'
update asrs.LOCATION set iSearchOrder = 4 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6231'
update asrs.LOCATION set iSearchOrder = 5 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6121'
update asrs.LOCATION set iSearchOrder = 6 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6221'
update asrs.LOCATION set iSearchOrder = 7 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6111'
update asrs.LOCATION set iSearchOrder = 8 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6211'
-- WHS right side
update asrs.LOCATION set iSearchOrder = 9 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6112' 
update asrs.LOCATION set iSearchOrder = 10 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6212'
update asrs.LOCATION set iSearchOrder = 11 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6122' 
update asrs.LOCATION set iSearchOrder = 12 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6222'
update asrs.LOCATION set iSearchOrder = 13 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6132' 
update asrs.LOCATION set iSearchOrder = 14 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6232'
update asrs.LOCATION set iSearchOrder = 15 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6142' 
update asrs.LOCATION set iSearchOrder = 16 where iLocationType = 10 and sWarehouse ='WHS' and sAddress='6242'

-- WHN right side
update asrs.LOCATION set iSearchOrder = 17 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6612' 
update asrs.LOCATION set iSearchOrder = 18 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6712'
update asrs.LOCATION set iSearchOrder = 19 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6622' 
update asrs.LOCATION set iSearchOrder = 20 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6722'
update asrs.LOCATION set iSearchOrder = 21 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6632' 
update asrs.LOCATION set iSearchOrder = 22 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6732'
update asrs.LOCATION set iSearchOrder = 23 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6642' 
update asrs.LOCATION set iSearchOrder = 24 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6742'

--WHN left side
update asrs.LOCATION set iSearchOrder = 25 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6641' 
update asrs.LOCATION set iSearchOrder = 26 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6741'
update asrs.LOCATION set iSearchOrder = 27 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6631' 
update asrs.LOCATION set iSearchOrder = 28 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6731'
update asrs.LOCATION set iSearchOrder = 29 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6621' 
update asrs.LOCATION set iSearchOrder = 30 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6721'
update asrs.LOCATION set iSearchOrder = 31 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6611' 
update asrs.LOCATION set iSearchOrder = 32 where iLocationType = 10 and sWarehouse ='WHN' and sAddress='6711'