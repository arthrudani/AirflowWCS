DELETE FROM [asrs].[SYSCONFIG] WHERE sGroup='TimeSliceGroup';

INSERT [asrs].[SYSCONFIG] ([sGroup], [sParameterName], [sParameterValue], [sDescription], [iScreenChangeAllowed], [iEnabled], [sScreenType], [dModifyTime], [sAddMethod], [sUpdateMethod]) 
VALUES 
('TimeSliceGroup', 'TIME_SLICE_HOUR', '03', NULL, 1, 1, NULL, NULL, NULL, NULL),
('TimeSliceGroup', 'TIME_SLICE_MIN',  '00', NULL, 1, 1, NULL, NULL, NULL, NULL);

DELETE FROM [asrs].[SYSCONFIG] WHERE sGroup='LocationGroup';

INSERT [asrs].[SYSCONFIG] ([sGroup], [sParameterName], [sParameterValue], [sDescription], [iScreenChangeAllowed], [iEnabled], [sScreenType], [dModifyTime], [sAddMethod], [sUpdateMethod]) 
VALUES 
('LocationGroup', 'LOC_COUNT_OOG', '20', NULL, 1, 1, NULL, NULL, NULL, NULL),
('LocationGroup', 'LOC_COUNT_STANDARD', '10', NULL, 1, 1, NULL, NULL, NULL, NULL);
