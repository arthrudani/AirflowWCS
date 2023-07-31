use [EBSWRXJ];
go

IF OBJECT_ID ('VIEW_EQUIPMENT_STATUS', 'V') IS NOT NULL
DROP VIEW VIEW_EQUIPMENT_STATUS;
go

CREATE VIEW VIEW_EQUIPMENT_STATUS
(   
    DeviceID,
    PhysicalStatus,
    OperationalStatus,
	ErrorCode,
	ErrorDescription
)
AS SELECT
	dv.SDEVICEID, 
    CASE dv.iphysicalstatus
		WHEN 233 THEN 'Online'
		WHEN 234 THEN 'Offline'
		WHEN 235 THEN 'Disconnected'
		WHEN 236 THEN 'Error'
		ELSE 'Unknown'
	END,
	CASE dv.ioperationalstatus
		WHEN 186 THEN 'Online'
		WHEN 187 THEN 'Offline'
		WHEN 188 THEN 'Inoperable'
		ELSE 'Unknown'
	END,
	es.sEMErrorCode, 
	es.sEMErrorText
FROM asrs.DEVICE dv with (nolock)
	JOIN asrs.EQUIPMENTMONITORSTATUS es with (nolock) ON (dv.sDeviceID = es.sEMMCController )
where dv.iDeviceType = 183
go