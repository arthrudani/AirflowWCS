use [EBSWRXJ];
delete from .[asrs].[ORDERLINE];
delete from .[asrs].[ITEMMASTER];
SET IDENTITY_INSERT [asrs].[ITEMMASTER] ON



INSERT INTO [asrs].[ITEMMASTER]
([iID]
,[sItem]
,[sDescription]
,[iHoldType]
,[iDeleteAtZeroQuantity]
,[dLastCCIDate]
,[iPiecesPerUnit]
)
VALUES
(1,'Empty_Tray_Stack','Empty Tray Stack',168,2,GETDATE(),1),
(2,'OOG_Empty_Tray_Stack','OOG Empty Tray Stack',168,2,GETDATE(),1),
(3,'Bag_On_Tray','Bag On Tray',168,2,GETDATE(),1),
(4,'OOG_Bag_On_Tray','OOG Bag On Tray',168,2,GETDATE(),1),
(5,'Bag','Bag Only',168,2,GETDATE(),1),
(6,'Unknown_Item','Unknown Item',168,2,GETDATE(),1) ;
SET IDENTITY_INSERT [asrs].[ITEMMASTER] OFF;



SELECT * FROM [EBSWRXJ].[asrs].[ITEMMASTER] order by 1;