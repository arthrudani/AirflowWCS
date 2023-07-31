USE [EBSWRXJ]

--Insert new record to ASRSMETADATA for new medata table for flight
--
-- Flight
--
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER,
    DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('Flight', 'FLIGHTS', 'Flight', 'N', 0,
    NULL, NULL, NULL);
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER,
    DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('Flight', 'BAGQUANTITY', 'Bag Quantity', 'N', 1,
    NULL, NULL, NULL);
	
--
-- FlightDetails
--
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('FlightDetails', 'SLOADID', 'TrayID', 'N', 0,NULL, NULL, NULL);
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('FlightDetails', 'SITEM', 'BagID', 'N', 1,NULL, NULL, NULL);
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('FlightDetails', 'SLOT', 'Flight#', 'N', 2,NULL, NULL, NULL);
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('FlightDetails', 'ILOADMOVESTATUS', 'Move Status', 'Y', 3, NULL, NULL, NULL);
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('FlightDetails', 'SWAREHOUSE', 'Warehouse', 'N', 4, NULL, NULL, NULL);
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('FlightDetails', 'SADDRESS', 'Address', 'N', 5, NULL, NULL, NULL);
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER, DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('FlightDetails', 'SDEVICEID', 'Device ID', 'N', 6, NULL, NULL, NULL);
   
   
  -- Timeslot table
--
Insert into [asrs].[ASRSMETADATA]
   (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION, IDISPLAYORDER,
    DMODIFYTIME, SADDMETHOD, SUPDATEMETHOD)
 Values
   ('TimeSlot', 'STARTTIME', 'Time Slot', 'N', 0,
    NULL, NULL, NULL);
	
UPDATE [asrs].[ASRSMETADATA]
SET [SCOLUMNNAME] ='SSTARTTIME'
WHERE [SDATAVIEWNAME] = 'TimeSlot';