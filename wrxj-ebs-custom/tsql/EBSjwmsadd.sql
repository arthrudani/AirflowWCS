use [EBSWRXJ]
go


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding ASRSMETADATA                +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.ASRSMETADATA
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sDataViewName       VARCHAR(25)     NOT NULL,
    sColumnName         VARCHAR(35)     NOT NULL,
    sFullName           VARCHAR(40)     NULL,
    sIsTranslation      VARCHAR(1)      CONSTRAINT [ASRSMETADATA_DF_sIsTranslation] DEFAULT ('N') NULL,
    iDisplayOrder       INT             NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [ASRSMETADATA_PK] PRIMARY KEY (iID),
    CONSTRAINT [ASRSMETADATA_UK] UNIQUE CLUSTERED (sDataViewName, sColumnName)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding CARRIER                     +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.CARRIER
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sCarrierID          VARCHAR(6)      NOT NULL,
    sCarrierName        VARCHAR(20)     NULL,
    sCarrierContact     VARCHAR(25)     NULL,
    sCarrierPhone       VARCHAR(20)     NULL,
    sStationName        VARCHAR(4)      NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [CARRIER_PK] PRIMARY KEY (iID),
    CONSTRAINT [CARRIER_UK] UNIQUE CLUSTERED (sCarrierID)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding CONTAINERTYPE               +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.CONTAINERTYPE
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sContainerType      VARCHAR (15)    NOT NULL,
    fWeight             NUMERIC (13, 3) CONSTRAINT [CONTAINERTYPE_DF_fWeight] DEFAULT ((0.0)) NULL,
    fMaxWeight          NUMERIC (13, 3) CONSTRAINT [CONTAINERTYPE_DF_fMaxWeight] DEFAULT ((0.0)) NULL,
    fContLength         NUMERIC (13, 3) CONSTRAINT [CONTAINERTYPE_DF_fContLength] DEFAULT ((0.0)) NULL,
    fContWidth          NUMERIC (13, 3) CONSTRAINT [CONTAINERTYPE_DF_fContWidth] DEFAULT ((0.0)) NULL,
    fContHeight         NUMERIC (13, 3) CONSTRAINT [CONTAINERTYPE_DF_fContHeight] DEFAULT ((0.0)) NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [CONTAINERTYPE_PK] PRIMARY KEY (iID),
    CONSTRAINT [CONTAINERTYPE_UK] UNIQUE CLUSTERED (sContainerType)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding CONTROLLERCONFIG            +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.CONTROLLERCONFIG
(
   iID                   BIGINT IDENTITY (1, 1) NOT NULL,
    sController           VARCHAR(50)     NOT NULL,
    sPropertyName         VARCHAR(50)     NOT NULL,
    sPropertyValue        VARCHAR(100)    NOT NULL,
    sPropertyDesc         VARCHAR(200)    NULL,
    iScreenChangeAllowed  INTEGER         CONSTRAINT [CONTROLLERCONFIG_DF_iScreenChangeAllowed] DEFAULT 1
                                          CONSTRAINT [CONTROLLERCONFIG_CH_iScreenChangeAllowed] CHECK (iScreenChangeAllowed IN (1, 2))
                                          NOT NULL,
    iEnabled              INTEGER         CONSTRAINT [CONTROLLERCONFIG_DF_iEnabled] DEFAULT 1
                                          CONSTRAINT [CONTROLLERCONFIG_CH_iEnabled] CHECK (iEnabled IN (1, 2))
                                          NOT NULL,
    dModifyTime           DATETIME2       NULL,
    sAddMethod            VARCHAR(2000)   NULL,
    sUpdateMethod         VARCHAR(2000)   NULL,

    CONSTRAINT [CONTROLLERCONFIG_PK] PRIMARY KEY (iID),
    CONSTRAINT [CONTROLLERCONFIG_UK] UNIQUE CLUSTERED (sController, sPropertyName)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding CUSTOMER                    +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.CUSTOMER
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sCustomer           VARCHAR(15)     NOT NULL,
    sDescription1       VARCHAR(40)     NULL,
    sDescription2       VARCHAR(40)     NULL,
    sStreetAddress1     VARCHAR(40)     NULL,
    sStreetAddress2     VARCHAR(40)     NULL,
    sCity               VARCHAR(30)     NULL,
    sState              VARCHAR(30)     NULL,
    sZipcode            VARCHAR(12)     NULL,
    sCountry            VARCHAR(30)     NULL,
    sPhone              VARCHAR(40)     NULL,
    sAttention          VARCHAR(30)     NULL,
    sContact            VARCHAR(30)     NULL,
    sNote               VARCHAR(30)     NULL,
    iDeleteOnUse        INTEGER         CONSTRAINT [CUSTOMER_DF_iDeleteOnUse] DEFAULT 2
                                        CONSTRAINT [CUSTOMER_CH_iDeleteOnUse] CHECK (iDeleteOnUse IN (1, 2))
                                        NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [CUSTOMER_PK] PRIMARY KEY (iID),
    CONSTRAINT [CUSTOMER_UK] UNIQUE CLUSTERED (sCustomer)
    		 ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding TRANSACTIONHISTORY          +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.TRANSACTIONHISTORY
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    iTranCategory       INTEGER         CONSTRAINT [TRANSACTIONHISTORY_CH_iTranCategory] CHECK (iTranCategory BETWEEN 770 AND 774)
                                        NOT NULL,
    iTranType           INTEGER         CONSTRAINT [TRANSACTIONHISTORY_CH_iTranType] CHECK (iTranType BETWEEN 880 AND 913)
                                        NOT NULL,
    dTransDateTime      DATETIME2       CONSTRAINT [TRANSACTIONHISTORY_DF_dTransDateTime] DEFAULT SYSDATETIME(),
    sMachineName        VARCHAR(30)     NULL,
    sItem               VARCHAR(30)     NULL,
    sLot                VARCHAR(30)     NULL,
    sLoadID             VARCHAR(8)      NULL,
    sToLoad             VARCHAR(8)      NULL,
    sLocation           VARCHAR(13)     NULL,
    sToLocation         VARCHAR(13)     NULL,
    sStation            VARCHAR(4)      NULL,
    sToStation          VARCHAR(4)      NULL,
    sDeviceID           VARCHAR(9)      NULL,
    sRouteID            VARCHAR(12)     NULL,
    iAisleGroup         INTEGER         NULL,
    sUserID             VARCHAR(13)     NULL,
    sRole               VARCHAR(20)     NULL,
    sCustomer           VARCHAR(15)     NULL,
    sCarrierID          VARCHAR(6)      NULL,
    dLastCCIDate        DATETIME2       NULL,
    dShipDate           DATETIME2       NULL,
    dAgingDate          DATETIME2       NULL,
    dExpirationDate     DATETIME2       NULL,
    fCurrentQuantity    NUMERIC(13,3)   NULL,
    fExpectedQuantity   NUMERIC(13,3)   NULL,
    fAdjustedQuantity   NUMERIC(13,3)   NULL,
    fReceivedQuantity   NUMERIC(13,3)   NULL,
    fPickQuantity       NUMERIC(13,3)   NULL,
    sOrderID            VARCHAR(30)     NULL,
    sLineID             VARCHAR(12)     NULL,
    sOrderLot           VARCHAR(30)     NULL,
    iOrderType          INTEGER         NULL,
    iHoldType           INTEGER         NULL,
    sReasonCode         VARCHAR(3)      NULL,
    sActionDescription  VARCHAR(300)    NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [TRANSACTIONHISTORY_PK] PRIMARY KEY (iID)
    		 ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding JVMCONFIG                   +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.JVMCONFIG
(
   	iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sJVMIdentifier      VARCHAR(30)     NOT NULL,
    iJVMType            INTEGER         CONSTRAINT [JVMCONFIG_DF_iJVMType] DEFAULT 561
                                        CONSTRAINT [JVMCONFIG_CH_iJVMType] CHECK (iJVMType IN (560, 561))
                                        NOT NULL,
    iJVMStatus          INTEGER         CONSTRAINT [JVMCONFIG_DF_iJVMStatus] DEFAULT 460
                                        CONSTRAINT [JVMCONFIG_CH_iJVMStatus] CHECK (iJVMStatus BETWEEN 460 AND 462)
                                        NOT NULL,
    sServerName         VARCHAR(20)     NOT NULL,
    sJMSTopic           VARCHAR(30)     NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [JVMCONFIG_PK] PRIMARY KEY (iID),
    CONSTRAINT [JVMCONFIG_UK_sJVMIdentifier] UNIQUE CLUSTERED (sJVMIdentifier),
    CONSTRAINT [JVMCONFIG_UK_sJMSTopic] UNIQUE NONCLUSTERED (sJMSTopic)
                     ON [EBSWRxJIndex]

) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding DEVICE                      +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.DEVICE
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sDeviceID           VARCHAR(9)      NOT NULL,
    iDeviceType         INTEGER         CONSTRAINT [DEVICE_DF_iDeviceType] DEFAULT 183
                                        CONSTRAINT [DEVICE_CH_iDeviceType] CHECK (iDeviceType BETWEEN 179 AND 193)
                                        NOT NULL,
    iAisleGroup         INTEGER         CONSTRAINT [DEVICE_DF_iAisleGroup] DEFAULT 0,
    sCommDevice         VARCHAR(9)      NULL,
    iOperationalStatus  INTEGER         CONSTRAINT [DEVICE_DF_iOperationalStatus] DEFAULT 187
                                        CONSTRAINT [DEVICE_CH_iOperationalStatus] CHECK (iOperationalStatus BETWEEN 186 AND 188)
                                        NOT NULL,
    iPhysicalStatus     INTEGER         CONSTRAINT [DEVICE_DF_iPhysicalStatus] DEFAULT 234
                                        CONSTRAINT [DEVICE_CH_iPhysicalStatus] CHECK (iPhysicalStatus BETWEEN 233 AND 236)
                                        NOT NULL,
    iEmulationMode      INTEGER         CONSTRAINT [DEVICE_DF_iEmulationMode] DEFAULT 196
                                        CONSTRAINT [DEVICE_CH_iEmulationMode] CHECK (iEmulationMode BETWEEN 195 AND 198)
                                        NOT NULL,
    sCommSendPort       VARCHAR(20)     NULL,
    sCommReadPort       VARCHAR(20)     NULL,
    sErrorCode          VARCHAR(6)      NULL,
    sNextDevice         VARCHAR(9)      NULL,
    iDeviceToken        INTEGER         CONSTRAINT [DEVICE_DF_iDeviceToken] DEFAULT 236
                                        CONSTRAINT [DEVICE_CH_iDeviceToken] CHECK (iDeviceToken IN (235, 236))
                                        NOT NULL,
    sSchedulerName      VARCHAR(15)     NULL,
    sAllocatorName      VARCHAR(35)     NULL,
    sStationName        VARCHAR(4)      NULL,
    sUserID             VARCHAR(13)     NULL,
    sPrinter            VARCHAR(40)     NULL,
    sWarehouse          VARCHAR(3)      NULL,
    sJVMIdentifier      VARCHAR(30)     NULL,   -- The JVM to which this device
                                                -- belongs on split systems.
												
	iLocSeqMethod       INTEGER         CONSTRAINT [DEVICE_DF_iLocSeqMethod] DEFAULT 1
                                        CONSTRAINT [DEVICE_CH_iLocSeqMethod] CHECK (iLocSeqMethod IN (1,2))
                                        NOT NULL,
												
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [DEVICE_PK] PRIMARY KEY (iID),
    CONSTRAINT [DEVICE_UK] UNIQUE CLUSTERED (sDeviceID)
    			 ON [EBSWRxJIndex]

) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding HOSTTOWRX                   +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.HOSTTOWRX
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sHostName           VARCHAR(25)     NOT NULL,
    dMessageAddTime     DATETIME2       CONSTRAINT [HOSTTOWRX_DF_dMessageAddTime] DEFAULT SYSDATETIME(),
    iMessageSequence    INTEGER         NOT NULL,
    iOriginalSequence   INTEGER         NULL,
    sMessageIdentifier  VARCHAR(50)     NOT NULL,
    sMessage            VARCHAR(MAX)    CONSTRAINT [HOSTTOWRX_DF_sMessage] DEFAULT '',
    iMessageProcessed   INTEGER         CONSTRAINT [HOSTTOWRX_DF_iMessageProcessed] DEFAULT 2
                                        CONSTRAINT [HOSTTOWRX_CH_iMessageProcessed] CHECK (iMessageProcessed IN (1, 2, 3)),
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [HOSTTOWRX_PK] PRIMARY KEY (iID),
    CONSTRAINT [HOSTTOWRX_UK_SEQUENCE] UNIQUE CLUSTERED (iMessageSequence, sHostName),
    CONSTRAINT [HOSTTOWRX_UK_PROCESSED] UNIQUE NONCLUSTERED(iMessageProcessed, iMessageSequence, sHostName)
                     ON [EBSWRxJIndex]

) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding Host Configuration          +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.HOSTCONFIG
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sDataHandler        VARCHAR(40)     NOT NULL,
    sGroup              VARCHAR(40)     NOT NULL,
    sParameterName      VARCHAR(40)     NOT NULL,
    sParameterValue     VARCHAR(100)    NOT NULL,
    iActiveConfig       INTEGER         CONSTRAINT [HOSTCONFIG_DF_iActiveConfig] DEFAULT 2
                                        CONSTRAINT [HOSTCONFIG_CH_iActiveConfig] CHECK (iActiveConfig IN (1, 2)),

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [HOSTCONFIG_PK] PRIMARY KEY (iID),
    CONSTRAINT [HOSTCONFIG_UK] UNIQUE CLUSTERED (sDataHandler, sParameterName, sParameterValue)
                      ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding HostAccess                  +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.HOSTOUTACCESS
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sHostName           VARCHAR(25)     NOT NULL,
    sMessageIdentifier  VARCHAR(50)     NOT NULL,
    iEnabled            INTEGER         CONSTRAINT [HOSTOUTACCESS_DF_iEnabled] DEFAULT 1
                                        NOT NULL,
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [HOSTOUTACCESS_PK] PRIMARY KEY (iID),
    CONSTRAINT [HOSTOUTACCESS_UK] UNIQUE CLUSTERED (sHostName, sMessageIdentifier)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding ITEMMASTER                  +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.ITEMMASTER
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sItem                 VARCHAR(30)   NOT NULL,
    sDescription          VARCHAR(40)   NULL,
    sRecommendedWarehouse VARCHAR(3)    NULL,
    sRecommendedZone      VARCHAR(3)    NULL,
    iHoldType             INTEGER       CONSTRAINT [ITEMMASTER_DF_iHoldType] DEFAULT 168
                                        CONSTRAINT [ITEMMASTER_CH_iHoldType] CHECK (iHoldType IN (168, 169, 170))
                                        NOT NULL,
    iDeleteAtZeroQuantity INTEGER       CONSTRAINT [ITEMMASTER_DF_iDeleteAtZeroQuantity] DEFAULT 2
                                        CONSTRAINT [ITEMMASTER_CH_iDeleteAtZeroQuantity] CHECK (iDeleteAtZeroQuantity IN (1, 2))
                                        NOT NULL,
    dLastCCIDate          DATETIME2     CONSTRAINT [ITEMMASTER_DF_dLastCCIDate] DEFAULT SYSDATETIME(),
    iPiecesPerUnit        INTEGER       CONSTRAINT [ITEMMASTER_DF_iPiecesPerUnit]DEFAULT 1
                                        NOT NULL,
    fCCIPointQuantity     NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fCCIPointQuantity] DEFAULT 0.0
                                        NOT NULL,
    fDefaultLoadQuantity  NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fDefaultLoadQuantity] DEFAULT 1.0
                                        NOT NULL,
    sOrderRoute           VARCHAR(12)   NULL,
    fItemWeight           NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fItemWeight] DEFAULT 0.0
                                        NOT NULL,
    fItemLength           NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fItemLength] DEFAULT 0.0
                                        NOT NULL,
    fItemHeight           NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fItemHeight] DEFAULT 0.0
                                        NOT NULL,
    fItemWidth            NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fItemWidth] DEFAULT 0.0
                                        NOT NULL,
    fCaseWeight           NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fCaseWeight] DEFAULT 0.0
                                        NOT NULL,
    fCaseLength           NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fCaseLength] DEFAULT 0.0
                                        NOT NULL,
    fCaseHeight           NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fCaseHeight] DEFAULT 0.0
                                        NOT NULL,
    fCaseWidth            NUMERIC(13,3) CONSTRAINT [ITEMMASTER_DF_fCaseWidth] DEFAULT 0.0
                                        NOT NULL,
    iExpirationRequired   INTEGER       CONSTRAINT [ITEMMASTER_DF_iExpirationRequired] DEFAULT 2
                                        CONSTRAINT [ITEMMASTER_CH_iExpirationRequired] CHECK (iExpirationRequired IN (1, 2))
                                        NOT NULL,
    iStorageFlag          INTEGER       CONSTRAINT [ITEMMASTER_DF_iStorageFlag] DEFAULT 242
                                        CONSTRAINT [ITEMMASTER_CH_iStorageFlag] CHECK (iStorageFlag BETWEEN 242 AND 245)
                                        NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [ITEMMASTER_PK] PRIMARY KEY (iID),
    CONSTRAINT [ITEMMASTER_UK] UNIQUE CLUSTERED (sItem)
    	 ON [EBSWRxJIndex]

) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding LOADWORD                    +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.LOADWORD
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    iWordSequence       INTEGER         CONSTRAINT [LOADWORD_DF_iWordSequence] DEFAULT 0
                                        NOT NULL,
    sLoadWord           VARCHAR(4)      NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [LOADWORD_PK] PRIMARY KEY (iID),
    CONSTRAINT [LOADWORD_UK] UNIQUE CLUSTERED (iWordSequence)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding MOVE                        +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.MOVE
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    iMoveID             INTEGER         NOT NULL,
    sParentLoad         VARCHAR(8)      NOT NULL,
    sLoadID             VARCHAR(8)      NOT NULL,
    sItem               VARCHAR(30)     NULL,
    sPickLot            VARCHAR(30)     NULL,
    sOrderLot           VARCHAR(30)     NULL,
    fPickQuantity       NUMERIC(13,3)   CONSTRAINT [MOVE_DF_fPickQuantity] DEFAULT 0.0
                                        NOT NULL,
    iPriority           INTEGER         CONSTRAINT [MOVE_DF_iPriority] DEFAULT 7
                                        NOT NULL,
    sOrderID            VARCHAR(30)     NULL,
    sRouteID            VARCHAR(12)     NULL,
    iMoveCategory       INTEGER         CONSTRAINT [MOVE_DF_iMoveCategory] DEFAULT 129
                                        CONSTRAINT [MOVE_CH_iMoveCategory] CHECK (iMoveCategory BETWEEN 125 AND 129)
                                        NOT NULL,
    iMoveType           INTEGER         CONSTRAINT [MOVE_DF_iMoveType] DEFAULT 61
                                        CONSTRAINT [MOVE_CH_iMoveType] CHECK (iMoveType BETWEEN 60 AND 64)
                                        NOT NULL,
    iMoveStatus         INTEGER         CONSTRAINT [MOVE_DF_iMoveStatus] DEFAULT 32
                                        CONSTRAINT [MOVE_CH_iMoveStatus] CHECK (iMoveStatus IN (32, 33))
                                        NOT NULL,
    dMoveDate           DATETIME2       CONSTRAINT [MOVE_DF_dMoveDate] DEFAULT SYSDATETIME(),
    sPickToLoadID       VARCHAR(8)      NULL,
    sLineID             VARCHAR(12)     NULL,
    sPositionID         VARCHAR(3)      NULL,
    sDeviceID           VARCHAR(9)      NULL,
    sDestWarehouse      VARCHAR(3)      NULL,
    sDestAddress        VARCHAR(9)      NULL,
    sNextWarehouse      VARCHAR(3)      NULL,
    sNextAddress        VARCHAR(9)      NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [MOVE_PK] PRIMARY KEY (iID),
    CONSTRAINT [MOVE_UK] UNIQUE CLUSTERED (iMoveID)
    		 ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding PORT                        +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.PORT
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sPortName             VARCHAR(20)   NOT NULL,
    sDeviceID             VARCHAR(9)    NOT NULL,
    iDirection            INTEGER       CONSTRAINT [POROUTE_DF_iDirection] DEFAULT 103
                                        CONSTRAINT [POROUTE_CH_iDirection] CHECK (iDirection IN (101, 102, 103))
                                        NOT NULL,
    iLastSequence         INTEGER       CONSTRAINT [POROUTE_DF_iLastSequence] DEFAULT 0,
    iCommunicationMode    INTEGER       CONSTRAINT [POROUTE_DF_iCommunicationMode] DEFAULT 182
                                        CONSTRAINT [POROUTE_CH_iCommunicationMode] CHECK (iCommunicationMode IN (182, 183))
                                        NOT NULL,
    sServerName           VARCHAR(20)   NOT NULL,
    sSocketNumber         VARCHAR(4)    NOT NULL,
    iRetryInterval        INTEGER       CONSTRAINT [POROUTE_DF_iRetryInterval] DEFAULT 5000
                                        NOT NULL,
    iSndKeepAliveInterval INTEGER       CONSTRAINT [POROUTE_DF_iSndKeepAliveInterval] DEFAULT 60000
                                        NOT NULL,
    iRcvKeepAliveInterval INTEGER       CONSTRAINT [POROUTE_DF_iRcvKeepAliveInterval] DEFAULT 70000
                                        NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [PORT_PK] PRIMARY KEY (iID),
    CONSTRAINT [PORT_UK] UNIQUE CLUSTERED (sPortName, sDeviceID)
     		ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding PURCHASEORDERHEADER         +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.PURCHASEORDERHEADER
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sOrderID             VARCHAR(30)    NOT NULL,
    dExpectedDate        DATETIME2      CONSTRAINT [PURCHASEORDERHEADER_DF_dExpectedDate] DEFAULT SYSDATETIME(),
    iPurchaseOrderStatus INTEGER        CONSTRAINT [PURCHASEORDERHEADER_DF_iPurchaseOrderStatus] DEFAULT 24
                                        CONSTRAINT [PURCHASEORDERHEADER_CH_iPurchaseOrderStatus] CHECK (iPurchaseOrderStatus BETWEEN 24 AND 31)
                                        NOT NULL,
    sStoreStation        VARCHAR(4)     NOT NULL DEFAULT '',
    sVendorID            VARCHAR(15)    NULL,
    dLastActivityTime    DATETIME2      CONSTRAINT [PURCHASEORDERHEADER_DF_dLastActivityTime] DEFAULT SYSDATETIME(),
    iHostLineCount       INTEGER        CONSTRAINT [PURCHASEORDERHEADER_DF_iHostLineCount] DEFAULT 1
                                        NOT NULL,
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [PURCHASEORDERHEADER_PK] PRIMARY KEY (iID),
    CONSTRAINT [PURCHASEORDERHEADER_UK] UNIQUE CLUSTERED (sOrderID)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding REASONCODE                  +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.REASONCODE
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    iReasonCategory     INTEGER         CONSTRAINT [REASONCODE_DF_iReasonCategory] DEFAULT 10
                                        CONSTRAINT [REASONCODE_CH_iReasonCategory] CHECK ( iReasonCategory BETWEEN 10 AND 12)
                                        NOT NULL,
    sReasonCode         VARCHAR(3)      NOT NULL,
    sDescription        VARCHAR(40)     NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [REASONCODE_PK] PRIMARY KEY (iID),
    CONSTRAINT [REASONCODE_UK] UNIQUE CLUSTERED (iReasonCategory, sReasonCode)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding ROLE                        +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.ROLE
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sRole               VARCHAR(20)     NOT NULL,
    sRoleDescription    VARCHAR(40)     NOT NULL,
    iRoleType           INTEGER         CONSTRAINT [ROLE_DF_iRoleType] DEFAULT 55
                                        CONSTRAINT [ROLE_CH_iRoleType] CHECK (iRoleType IN (55, 56, 57))
                                        NOT NULL,
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [ROLE_PK] PRIMARY KEY (iID),
    CONSTRAINT [ROLE_UK] UNIQUE CLUSTERED (sRole)
    		 ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding ROUTE                       +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.ROUTE
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sRouteID            VARCHAR(12)     NOT NULL,
    sFromID             VARCHAR(4)      NOT NULL,
    sDestID             VARCHAR(4)      NOT NULL,
    iFromType           INTEGER         CONSTRAINT [ROUTE_DF_iFromType] DEFAULT 232
                                        CONSTRAINT [ROUTE_CH_iFromType] CHECK (iFromType IN (231, 232, 233))
                                        NOT NULL,
    iDestType           INTEGER         CONSTRAINT [ROUTE_DF_iDestType] DEFAULT 232
                                        CONSTRAINT [ROUTE_CH_iDestType] CHECK (iDestType IN (231, 232, 233))
                                        NOT NULL,
    iRouteOnOff         INTEGER         CONSTRAINT [ROUTE_DF_iRouteOnOff] DEFAULT 35
                                        CONSTRAINT [ROUTE_CH_iRouteOnOff] CHECK (iRouteOnOff IN (35, 36))
                                        NOT NULL,
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [ROUTE_PK] PRIMARY KEY (iID),
    CONSTRAINT [ROUTE_UK] UNIQUE CLUSTERED (sRouteID, sFromID, sDestID)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding STATION                     +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.STATION
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sStationName          VARCHAR(4)    NOT NULL,
    sWarehouse            VARCHAR(3)    NOT NULL,
    sDescription          VARCHAR(40)   NULL,
    iStationType          INTEGER       CONSTRAINT [STATION_DF_iStationType] DEFAULT 224
                                        CONSTRAINT [STATION_CH_iStationType] CHECK (iStationType BETWEEN 220 AND 233)
                                        NOT NULL,
    sAllocationType       VARCHAR(30)   NULL,
    iDeleteInventory      INTEGER       CONSTRAINT [STATION_DF_iDeleteInventory] DEFAULT 1
                                        CONSTRAINT [STATION_CH_iDeleteInventory] CHECK (iDeleteInventory IN (1, 2))
                                        NOT NULL,
    sDefaultRoute         VARCHAR(12)   NULL,
    sLinkRoute            VARCHAR(12)   NULL,
    sRejectRoute          VARCHAR(12)   NULL,
    sDeviceID             VARCHAR(9)    NULL,
    sStationScale         VARCHAR(4)    NULL,
    sLoadPrefix           VARCHAR(4)    NULL,
    sOrderPrefix          VARCHAR(4)    NULL,
    iArrivalRequired      INTEGER       CONSTRAINT [STATION_DF_iArrivalRequired] DEFAULT 1
                                        CONSTRAINT [STATION_CH_iArrivalRequired] CHECK (iArrivalRequired IN (1, 2))
                                        NOT NULL,
    iMaxAllowedEnroute    INTEGER       CONSTRAINT [STATION_DF_iMaxAllowedEnroute] DEFAULT 0,
    iMaxAllowedStaged     INTEGER       CONSTRAINT [STATION_DF_iMaxAllowedStaged] DEFAULT 0,
    sPrinter              VARCHAR(40)   NULL,
    iAutoLoadMovementType INTEGER       CONSTRAINT [STATION_DF_iAutoLoadMovementType] DEFAULT 174
                                        CONSTRAINT [STATION_CH_iAutoLoadMovementType] CHECK (iAutoLoadMovementType BETWEEN 174 AND 181)
                                        NOT NULL,
    iAutoOrderType        INTEGER       CONSTRAINT [STATION_DF_iAutoOrderType] DEFAULT 340
                                        CONSTRAINT [STATION_CH_iAutoOrderType] CHECK (iAutoOrderType BETWEEN 340 AND 342)
                                        NOT NULL,
    iAllocationEnabled    INTEGER       CONSTRAINT [STATION_DF_iAllocationEnabled] DEFAULT 1
                                        CONSTRAINT [STATION_CH_iAllocationEnabled] CHECK (iAllocationEnabled IN (1, 2))
                                        NOT NULL,
    iStatus               INTEGER       CONSTRAINT [STATION_DF_iStatus] DEFAULT 202  -- STORERETRIEVE
                                        CONSTRAINT [STATION_CH_iStatus] CHECK (iStatus BETWEEN 200 AND 202)
                                        NOT NULL,
    iBidirectionalStatus  INTEGER       CONSTRAINT [STATION_DF_iBidirectionalStatus] DEFAULT 404  -- RETRIEVE
                                        CONSTRAINT [STATION_CH_iBidirectionalStatus] CHECK (iBidirectionalStatus BETWEEN 400 AND 407)
                                        NOT NULL,
    iCaptive              INTEGER       CONSTRAINT [STATION_DF_iCaptive] DEFAULT 179
                                        CONSTRAINT [STATION_CH_iCaptive] CHECK (iCaptive BETWEEN 179 AND 181)
                                        NOT NULL,
    iConfirmLot           INTEGER       CONSTRAINT [STATION_DF_iConfirmLot] DEFAULT 2
                                        CONSTRAINT [STATION_CH_iConfirmLot] CHECK (iConfirmLot IN (1, 2))
                                        NOT NULL,
    iConfirmLocation      INTEGER       CONSTRAINT [STATION_DF_iConfirmLocation] DEFAULT 2
                                        CONSTRAINT [STATION_CH_iConfirmLocation] CHECK (iConfirmLocation IN (1, 2))
                                        NOT NULL,
    iConfirmLoad          INTEGER       CONSTRAINT [STATION_DF_iConfirmLoad] DEFAULT 2
                                        CONSTRAINT [STATION_CH_iConfirmLoad] CHECK (iConfirmLoad IN (1, 2))
                                        NOT NULL,
    iConfirmItem          INTEGER       CONSTRAINT [STATION_DF_iConfirmItem] DEFAULT 2
                                        CONSTRAINT [STATION_CH_iConfirmItem] CHECK (iConfirmItem IN (1, 2))
                                        NOT NULL,
    iConfirmQty           INTEGER       CONSTRAINT [STATION_DF_iConfirmQty] DEFAULT 1
                                        CONSTRAINT [STATION_CH_iConfirmQty] CHECK (iConfirmQty IN (1, 2))
                                        NOT NULL,
    sContainerType        VARCHAR(15)   NULL,
    iPhysicalStatus       INTEGER       CONSTRAINT [STATION_DF_iPhysicalStatus] DEFAULT 234
                                        CONSTRAINT [STATION_CH_iPhysicalStatus] CHECK (iPhysicalStatus IN (233, 234))
                                        NOT NULL,
    iOrderStatus          INTEGER       CONSTRAINT [STATION_DF_iOrderStatus] DEFAULT 233
                                        CONSTRAINT [STATION_CH_iOrderStatus] CHECK (iOrderStatus IN (230, 233))
                                        NOT NULL,
    iPoReceiveAll         INTEGER       CONSTRAINT [STATION_DF_iPoReceiveAll] DEFAULT 2
                                        CONSTRAINT [STATION_CH_iPoReceiveAll] CHECK (iPoReceiveAll IN (1, 2))
                                        NOT NULL,
    iCCIAllowed           INTEGER       CONSTRAINT [STATION_DF_iCCIAllowed] DEFAULT 2
                                        CONSTRAINT [STATION_CH_iCCIAllowed] CHECK (iCCIAllowed IN (1, 2))
                                        NOT NULL,
    fWeight               NUMERIC(13,3) CONSTRAINT [STATION_DF_fWeight] DEFAULT 0.0,
    iHeight               INTEGER       CONSTRAINT [STATION_DF_iHeight] DEFAULT 1 NULL,
    iAmountFull           INTEGER       CONSTRAINT [STATION_DF_iAmountFull] DEFAULT 238
                                        CONSTRAINT [STATION_CH_iAmountFull] CHECK (iAmountFull BETWEEN 234 AND 238)
                                        NOT NULL,
    iPriorityCategory     INTEGER       CONSTRAINT [STATION_DF_iPriorityCategory] DEFAULT 2
                                        CONSTRAINT [STATION_CH_iPriorityCategory] CHECK (iPriorityCategory IN (1, 2, 9)),
    iReInputFlag          INTEGER       CONSTRAINT [STATION_DF_iReInputFlag] DEFAULT 0
                                        CONSTRAINT [STATION_CH_iReInputFlag] CHECK (iReInputFlag IN (0, 1)),

    iRetrieveCommandDetail INTEGER      CONSTRAINT [STATION_DF_iRetrieveCommandDetail] DEFAULT 1
                                        CONSTRAINT [STATION_CH_iRetrieveCommandDetail] CHECK (iRetrieveCommandDetail BETWEEN 0 AND 3),
    sItem                 VARCHAR(30)   NULL,
    sLot                  VARCHAR(30)   NULL,
    fOrderQuantity        NUMERIC(13,3) CONSTRAINT [STATION_DF_fOrderQuantity] DEFAULT 0.0
                                        NOT NULL,
    iAllowRoundRobin      INTEGER       CONSTRAINT [STATION_DF_iAllowRoundRobin] DEFAULT 2
                                        CONSTRAINT [STATION_CH_iAllowRoundRobin] CHECK (iAllowRoundRobin IN (1, 2))
                                        NOT NULL,
-- sReplenishSources specifies the types of locations that this station can replenish
-- from.  The order of the listing indicates most preferred to least preferred.
-- The format for this column is a comma delimited sequence of location types.
    sReplenishSources   VARCHAR(12)     NULL,
    iSimulate           INTEGER         CONSTRAINT [STATION_DF_iSimulate] DEFAULT 36
                                        CONSTRAINT [STATION_CH_iSimulate] CHECK (iSimulate IN (35,36))
                                        NOT NULL,
    iSimInterval        INTEGER         CONSTRAINT [STATION_DF_iSimInterval] DEFAULT 0
                                        NOT NULL,
    iCustomAction       INTEGER         CONSTRAINT [STATION_DF_iCustomAction] DEFAULT 0
                                        NOT NULL,
    sRecommendedZone    VARCHAR(3)      NULL,
    sReprStationName    VARCHAR(4)      NULL,
    
    
    sBCSDeviceID        VARCHAR(9)    	NULL,
    sSecondaryDeviceID  VARCHAR(9)    	NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [STATION_PK] PRIMARY KEY (iID),
    CONSTRAINT [STATION_UK] UNIQUE CLUSTERED (sStationName, sWarehouse)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding SYSCONFIG                   +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.SYSCONFIG
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sGroup                VARCHAR(100)  NOT NULL,
    sParameterName        VARCHAR(100)  NOT NULL,
    sParameterValue       VARCHAR(100)  NOT NULL,
    sDescription          VARCHAR(40)   NULL,
    iScreenChangeAllowed  INTEGER       CONSTRAINT [SYSCONFIG_DF_iScreenChangeAllowed] DEFAULT 1
                                        CONSTRAINT [SYSCONFIG_CH_iScreenChangeAllowed] CHECK (iScreenChangeAllowed IN (1, 2))
                                        NOT NULL,
    iEnabled              INTEGER       CONSTRAINT [SYSCONFIG_DF_iEnabled] DEFAULT 1
                                        CONSTRAINT [SYSCONFIG_CH_iEnabled] CHECK (iEnabled IN (1, 2))
                                        NOT NULL,
    sScreenType           VARCHAR(50)   NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [SYSCONFIG_PK] PRIMARY KEY (iID),
    CONSTRAINT [SYSCONFIG_UK] UNIQUE CLUSTERED (sGroup, sParameterName)
               ON [EBSWRxJIndex]

) ON [EBSWRxJData];

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding VEHICLEMOVE                 +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.VEHICLEMOVE
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sLoadID             VARCHAR(8)      NOT NULL,
    iSequenceNumber     INTEGER         CONSTRAINT [VEHICLEMOVE_DF_iSequenceNumber] DEFAULT 0
                                        NOT NULL,
    iAGVLoadStatus      INTEGER         CONSTRAINT [VEHICLEMOVE_DF_iAGVLoadStatus] DEFAULT 260
                                        CONSTRAINT [VEHICLEMOVE_CH_iAGVLoadStatus] CHECK (iAGVLoadStatus BETWEEN 260 AND 271)
                                        NOT NULL,
    dStatusChangeTime   DATETIME2       CONSTRAINT [VEHICLEMOVE_DF_dStatusChangeTime] DEFAULT SYSDATETIME(),
    sVehicleID          VARCHAR(3),
    sRequestID          VARCHAR(10),
    sCurrStation        VARCHAR(4)      NOT NULL,
    sDestStation        VARCHAR(4),
    sDualLoadMoveSeq    VARCHAR(11)     CONSTRAINT [VEHICLEMOVE_DF_sDualLoadMoveSeq] DEFAULT 'DEFAULT', -- used for dual load moves.
    iNotifyHost         INTEGER         CONSTRAINT [VEHICLEMOVE_DF_iNotifyHost] DEFAULT 1
                                        CONSTRAINT [VEHICLEMOVE_CH_iNotifyHost] CHECK (iNotifyHost IN (1, 2)),
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [VEHICLEMOVE_PK] PRIMARY KEY (iID),
    CONSTRAINT [VEHICLEMOVE_UK_SEQUENCE] UNIQUE (iSequenceNumber),
    CONSTRAINT [VEHICLEMOVE_UK_LOADID] UNIQUE CLUSTERED (sLoadID)
    		 ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding VEHICLESYSTEMCMD            +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.VEHICLESYSTEMCMD
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    iSequenceNumber     INTEGER         CONSTRAINT [VEHICLESYSTEMCMD_DF_iSequenceNumber] DEFAULT 0
                                        NOT NULL,
    dStatusChangeTime   DATETIME2       CONSTRAINT [VEHICLESYSTEMCMD_DF_dStatusChangeTime] DEFAULT SYSDATETIME(),
    sSystemMessageID    VARCHAR(3)      NOT NULL,
    iCommandStatus      INTEGER         CONSTRAINT [VEHICLESYSTEMCMD_DF_iCommandStatus] DEFAULT 140
                                        CONSTRAINT [VEHICLESYSTEMCMD_CH_iCommandStatus] CHECK (iCommandStatus BETWEEN 140 AND 144)
                                        NOT NULL,
    sCommandValue       VARCHAR(20),

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [VEHICLESYSTEMCMD_PK] PRIMARY KEY (iID),
    CONSTRAINT [VEHICLESYSTEMCMD_UK] UNIQUE CLUSTERED (iSequenceNumber)
                   ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding WAREHOUSE                   +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.WAREHOUSE
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sSuperWarehouse     VARCHAR(3)      NULL,
    sWarehouse          VARCHAR(3)      NOT NULL,
    sDescription        VARCHAR(40)     NULL,
    iWarehouseType      INTEGER         CONSTRAINT [WAREHOUSE_DF_iWarehouseType] DEFAULT 64
                                        CONSTRAINT [WAREHOUSE_CH_iWarehouseType] CHECK (iWarehouseType IN (63, 64))
                                        NOT NULL,
    iWarehouseStatus    INTEGER         CONSTRAINT [WAREHOUSE_DF_iWarehouseStatus] DEFAULT 240
                                        CONSTRAINT [WAREHOUSE_CH_iWarehouseStatus] CHECK (iWarehouseStatus IN (240, 241))
                                        NOT NULL,
    iOneLoadPerLoc      INTEGER         CONSTRAINT [WAREHOUSE_DF_iOneLoadPerLoc] DEFAULT 2
                                        CONSTRAINT [WAREHOUSE_CH_iOneLoadPerLoc] CHECK (iOneLoadPerLoc IN (1, 2))
                                        NOT NULL,
    sEquipWarehouse     VARCHAR(1)      CONSTRAINT [WAREHOUSE_DF_sEquipWarehouse] DEFAULT '0'
                                        NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [WAREHOUSE_PK] PRIMARY KEY (iID),
    CONSTRAINT [WAREHOUSE_UK_WAREHOUSE] UNIQUE (sWarehouse),
    CONSTRAINT [WAREHOUSE_UK_SUPER] UNIQUE (sSuperWarehouse, sWarehouse)
                     ON [EBSWRxJIndex]

) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding WRXSEQUENCER                +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.WRXSEQUENCER
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sSequenceIdentifier VARCHAR(15) NOT NULL,
    sEndDeviceName      VARCHAR(25) NOT NULL,
    iSequenceType       INTEGER     CONSTRAINT [WRXSEQUENCER_DF_iSequenceType] DEFAULT 370
                                    CONSTRAINT [WRXSEQUENCER_CH_iSequenceType] CHECK (iSequenceType BETWEEN 370 AND 375),
    iSequenceNumber     INTEGER     CONSTRAINT [WRXSEQUENCER_DF_iSequenceNumber] DEFAULT 0
                                    NOT NULL,
    iIncrementFactor    INTEGER     CONSTRAINT [WRXSEQUENCER_DF_iIncrementFactor] DEFAULT 1
                                    NOT NULL,
    iStartValue         INTEGER     CONSTRAINT [WRXSEQUENCER_DF_iStartValue] DEFAULT 0
                                    NOT NULL,
    iRestartValue       INTEGER     NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [WRXSEQUENCER_PK] PRIMARY KEY (iID),
    CONSTRAINT [WRXSEQUENCER_UK] UNIQUE CLUSTERED (sSequenceIdentifier, sEndDeviceName, iSequenceType)
                     ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding WRXTOHOST                   +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.WRXTOHOST
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sHostName           VARCHAR(25)     NOT NULL,
    dMessageAddTime     DATETIME2       CONSTRAINT [WRXTOHOSTATION_DF_dMessageAddTime] DEFAULT SYSDATETIME(),
    iMessageSequence    INTEGER         NOT NULL,
    sMessageIdentifier  VARCHAR(50)     NULL,
    sMessage            VARCHAR(MAX)    CONSTRAINT [WRXTOHOSTATION_DF_sMessage] DEFAULT '',
    iMessageProcessed   INTEGER         CONSTRAINT [WRXTOHOSTATION_DF_iMessageProcessed] DEFAULT 2
                                        CONSTRAINT [WRXTOHOSTATION_CH_iMessageProcessed] CHECK (iMessageProcessed IN (1, 2, 3)),
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [WRXTOHOST_PK] PRIMARY KEY (iID),
    CONSTRAINT [WRXTOHOST_UK_SEQUENCE] UNIQUE CLUSTERED (iMessageSequence, sHostName),
    CONSTRAINT [WRXTOHOST_UK_PROCESSED] UNIQUE (iMessageProcessed, iMessageSequence, sHostName)
                     ON [EBSWRxJIndex]

) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding ZONE                        +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.ZONE
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sZone               VARCHAR(3)      NOT NULL,
    sDescription        VARCHAR(40)     NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [ZONE_PK] PRIMARY KEY (iID),
    CONSTRAINT [ZONE_UK] UNIQUE CLUSTERED (sZone)
    			 ON [EBSWRxJIndex]
) ON [EBSWRxJData];


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding ZONEGROUP                   +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.ZONEGROUP
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sZoneGroup          VARCHAR(3)      NOT NULL,
    iPriority           INTEGER         CONSTRAINT [ZONEGROUP_DF_iPriority] DEFAULT 0
                                        NOT NULL,
    sZone               VARCHAR(3)      NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [ZONEGROUP_PK] PRIMARY KEY (iID),
    CONSTRAINT [ZONEGROUP_UK] UNIQUE CLUSTERED (sZoneGroup, iPriority)
    			 ON [EBSWRxJIndex]
) ON [EBSWRxJData];

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding EQUIPMENTMONITORSTATUS      +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE [asrs].[EQUIPMENTMONITORSTATUS](
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sEMGraphicID        varchar(30)     NOT NULL,
    sEMAltGraphicID     varchar(30)     NOT NULL,
    sEMDescription      varchar(50)     NULL,
    sEMBehavior         varchar(30)     NULL,
    sEMStatusID         varchar(12)     NULL,
    sEMStatusText1      varchar(20)     NULL,
    sEMStatusText2      varchar(20)     NULL,
    sEMErrorCode        varchar(10)     NULL,
    sEMErrorText        varchar(100)    NULL,
    sEMErrorSet         varchar(10)     NULL,
    sEMMCController     varchar(30)     NULL,
    sEMMCID             varchar(50)     NULL,
    sEMMOSController    varchar(30)     NULL,
    sEMMOSID            varchar(50)     NULL,
    sEMDeviceID         varchar(10)     NULL,
    sEMStationID        varchar(10)     NULL,
    iEMCanTrack         int             CONSTRAINT [EQUIPMENTMONITORSTATUS_CH_iEMCanTrack] CHECK  (([iEMCanTrack]=(2) OR [iEMCanTrack]=(1)))
                                        CONSTRAINT [EQUIPMENTMONITORSTATUS_DF_iEMCanTrack] DEFAULT ((1)) 
                                        NOT NULL,
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,
	
    CONSTRAINT [EQUIPMENTMONITORSTATUS_PK] PRIMARY KEY (iID),
    CONSTRAINT [EQUIPMENTMONITORSTATUS_UK] UNIQUE CLUSTERED (sEMGraphicID ASC)
    			 ON [EBSWRxJIndex]
) ON [EBSWRxJData];
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding EQUIPMENTMONITORSTATUSTYPE  +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE [asrs].[EQUIPMENTMONITORSTATUSTYPE](
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sEMStatusID          varchar(12)    NOT NULL,
    sEMStatusDescription varchar(50)    NULL,
    iEMDisplayPriority   int            NOT NULL,
    sEMBackground        varchar(7)     NULL,
    sEMForeground        varchar(7)     NULL,
	
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [EQUIPMENTMONITORSTATUSTYPE_PK] PRIMARY KEY (iID),
    CONSTRAINT [EQUIPMENTMONITORSTATUSTYPE_UK] UNIQUE CLUSTERED ([sEMStatusID] ASC)
    			 ON [EBSWRxJIndex]
) ON [EBSWRxJData];
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding EQUIPMENTMONITORTAB         +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE [asrs].[EQUIPMENTMONITORTAB](
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sEMGraphicTab       varchar(30)     NOT NULL,
    sEMGraphicID        varchar(30)     NOT NULL,
	
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,
	
    CONSTRAINT [EQUIPMENTMONITORTAB_PK] PRIMARY KEY (iID)
    			 ON [EBSWRxJIndex]
) ON [EBSWRxJData]
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding EQUIPMENTMONITORTRACKING    +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE [asrs].[EQUIPMENTMONITORTRACKING](
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sEMGraphicID        varchar(30)     NOT NULL,
    sEMDeviceID         varchar(10)     NULL,
    sEMTrackingID       varchar(20)     NULL,
    sEMBarcode          varchar(100)    NULL,
    sEMStatus           varchar(100)    NULL,
    sEMOrigin           varchar(30)     NULL,
    sEMDestination      varchar(30)     NULL,
    sEMSize             varchar(3)      NULL,
	
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,
	
    CONSTRAINT [EQUIPMENTMONITORTRACKING_PK] PRIMARY KEY (iID)
    			 ON [EBSWRxJIndex]
) ON [EBSWRxJData]
GO

-- ---------------------------------------------------------------------------
-- ---------------------------------------------------------------------------
-- ------------ TABLES WITH FOREIGN KEY DEPENDENCIES GO HERE -----------------
-- ---------------------------------------------------------------------------
-- ---------------------------------------------------------------------------

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding EMPLOYEE                    +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.EMPLOYEE
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sUserID             VARCHAR(13)     NOT NULL,
    sUserName           VARCHAR(30)     NULL,
    sRole               VARCHAR(20)     NOT NULL,
    sPassword           VARCHAR(30)     NULL,
    dPasswordExpiration DATETIME2       NULL,
    sReleaseToCode      VARCHAR(3)      NULL,
    sLanguage           VARCHAR(20)     NULL,
    iRememberLastLogin  INTEGER         CONSTRAINT [EMPLOYEE_DF_iRememberLastLogin] DEFAULT 2
                                        CONSTRAINT [EMPLOYEE_CH_iRememberLastLogin] CHECK (iRememberLastLogin IN (1, 2))
                                        NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [EMPLOYEE_PK] PRIMARY KEY (iID),
    CONSTRAINT [EMPLOYEE_UK] UNIQUE CLUSTERED (sUserID)
    			 ON [EBSWRxJIndex]
) ON [EBSWRxJData];

ALTER TABLE asrs.EMPLOYEE ADD CONSTRAINT [EMPLOYEE_FK_ROLE] FOREIGN KEY (sRole) REFERENCES asrs.ROLE(sRole);
GO

CREATE INDEX EMPLOYEE_IDX_ROLE ON asrs.EMPLOYEE (sRole) ;
GO


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding LOCATION                    +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.LOCATION
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sWarehouse          VARCHAR(3)      NOT NULL,
    sAddress            VARCHAR(9)      NOT NULL,
-- Shelf Position is known as the "address" in the MC-AGC Comm. Spec. For
-- baseline default this to zero (this was hard-coded in the message before).
-- For an actual impl. of this field in a project, it should be moved into a
-- separate table.
    sShelfPosition      VARCHAR(3)      CONSTRAINT [LOCATION_DF_sShelfPosition] DEFAULT '000',
    sZone               VARCHAR(3)      NULL,
    iLocationStatus     INTEGER         CONSTRAINT [LOCATION_DF_iLocationStatus] DEFAULT 29
                                        CONSTRAINT [LOCATION_CH_iLocationStatus] CHECK (iLocationStatus BETWEEN 29 AND 31)
                                        NOT NULL,
    iLocationType       INTEGER         CONSTRAINT [LOCATION_DF_iLocationType] DEFAULT 10
                                        CONSTRAINT [LOCATION_CH_iLocationType] CHECK (iLocationType BETWEEN 10 AND 19)
                                        NOT NULL,
    iEmptyFlag          INTEGER         CONSTRAINT [LOCATION_DF_iEmptyFlag] DEFAULT 21
                                        CONSTRAINT [LOCATION_CH_iEmptyFlag] CHECK (iEmptyFlag BETWEEN 21 AND 25)
                                        NOT NULL,
    sDeviceID           VARCHAR(9)      NULL,
    iHeight             INTEGER         CONSTRAINT [LOCATION_DF_iHeight] DEFAULT 1,
    iAssignedLength     INTEGER         CONSTRAINT [LOCATION_DF_iAssignedLength] DEFAULT ((0)) NULL,
    iSearchOrder        INTEGER         CONSTRAINT [LOCATION_DF_iSearchOrder] DEFAULT 1,
    iAisleGroup         INTEGER         CONSTRAINT [LOCATION_DF_iAisleGroup] DEFAULT 0,
    iAllowDeletion      INTEGER         CONSTRAINT [LOCATION_DF_iAllowDeletion] DEFAULT 1
                                        CONSTRAINT [LOCATION_CH_iAllowDeletion] CHECK (iAllowDeletion IN (1, 2))
                                        NOT NULL,
    iMoveSequence       INTEGER         CONSTRAINT [LOCATION_DF_iMoveSequence] DEFAULT 0, -- Used by MoveView
    iLocationDepth      INTEGER         CONSTRAINT [LOCATION_DF_iLocationDepth] DEFAULT 1
                                        CONSTRAINT [LOCATION_CH_iLocationDepth] CHECK (iLocationDepth BETWEEN 1 AND 3)
                                        NOT NULL,
    sLinkedAddress      VARCHAR(9)      NULL,
    iSwapZone           INTEGER         CONSTRAINT [LOCATION_DF_iSwapZone] DEFAULT 0,

    iPrimarySearchOrder        INTEGER         CONSTRAINT [LOCATION_DF_iPrimarySearchOrder] DEFAULT 1,
    iSecondarySearchOrder      INTEGER         CONSTRAINT [LOCATION_DF_iSecondarySearchOrder] DEFAULT 1,
  
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [LOCATION_PK] PRIMARY KEY (iID),
    CONSTRAINT [LOCATION_UK] UNIQUE CLUSTERED (sWarehouse, sAddress)
                      ON [EBSWRxJIndex]
) ON [EBSWRxJData];


ALTER TABLE asrs.LOCATION ADD CONSTRAINT [LOCATION_FK_WAREHOUSE] FOREIGN KEY (sWarehouse) REFERENCES asrs.WAREHOUSE(sWarehouse);
ALTER TABLE asrs.LOCATION ADD CONSTRAINT [LOCATION_FK_DEVICE] FOREIGN KEY (sDeviceID) REFERENCES asrs.DEVICE(sDeviceID);
GO

CREATE INDEX LOCATION_IDX_DEVICE ON asrs.LOCATION (sDeviceID) ;
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding DEDICATEDLOCATION           +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.DEDICATEDLOCATION
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sItem               VARCHAR(30)     NOT NULL,
    sWarehouse          VARCHAR(3)      NOT NULL,
    sAddress            VARCHAR(9)      NOT NULL,
    fMinimumQuantity    NUMERIC(13,3)   CONSTRAINT [DEDICATEDLOCATION_DF_fMinimumQuantity] DEFAULT 0
                                        NOT NULL,
    fMaximumQuantity    NUMERIC(13,3)   CONSTRAINT [DEDICATEDLOCATION_DF_fMaximumQuantity] DEFAULT 1
                                        NOT NULL,
    iDedicatedType      INTEGER         CONSTRAINT [DEDICATEDLOCATION_DF_iDedicatedType] DEFAULT 88
                                        CONSTRAINT [DEDICATEDLOCATION_CH_iDedicatedType] CHECK (iDedicatedType BETWEEN 88 AND 90)
                                        NOT NULL,
    iReplenishType      INTEGER         CONSTRAINT [DEDICATEDLOCATION_DF_iReplenishType] DEFAULT 87
                                        CONSTRAINT [DEDICATEDLOCATION_CH_iReplenishType] CHECK (iReplenishType IN (85, 86, 87, 90))
                                        NOT NULL,
    iReplenishNow       INTEGER         CONSTRAINT [DEDICATEDLOCATION_DF_iReplenishNow] DEFAULT 11
                                        CONSTRAINT [DEDICATEDLOCATION_CH_iReplenishNow] CHECK (iReplenishNow BETWEEN 11 AND 14)
                                        NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [DEDICATEDLOCATION_PK] PRIMARY KEY (iID),
    CONSTRAINT [DEDICATEDLOCATION_UK] UNIQUE CLUSTERED (sItem, sWarehouse, sAddress)
    			ON [EBSWRxJIndex]
) ON [EBSWRxJData];


ALTER TABLE asrs.DEDICATEDLOCATION ADD CONSTRAINT [DEDICATEDLOCATION_FK_LOCATION] FOREIGN KEY (sWarehouse, sAddress) REFERENCES asrs.LOCATION(sWarehouse, sAddress);
ALTER TABLE asrs.DEDICATEDLOCATION ADD CONSTRAINT [DEDICATEDLOCATION_FK_ITEMMASTER] FOREIGN KEY (sItem) REFERENCES asrs.ITEMMASTER(sItem);
GO

CREATE INDEX DEDICATEDLOCATION_IDX_LOCATION ON asrs.DEDICATEDLOCATION (sWarehouse, sAddress);
CREATE INDEX DEDICATEDLOCATION_IDX_ITEM ON asrs.DEDICATEDLOCATION (sItem);
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding LOAD                        +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.LOAD
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sParentLoad         VARCHAR(8)      NOT NULL,
    sLoadID             VARCHAR(8)      NOT NULL,
    sWarehouse          VARCHAR(3)      NOT NULL,
    sAddress            VARCHAR(9)      NOT NULL,
    sShelfPosition      VARCHAR(3)      CONSTRAINT [LOAD_DF_sShelfPosition] DEFAULT '000',
    sRouteID            VARCHAR(12)     NULL,
    sContainerType      VARCHAR(15)     NOT NULL,
    iLoadMoveStatus     INTEGER         CONSTRAINT [LOAD_DF_iLoadMoveStatus] DEFAULT 224
                                        CONSTRAINT [LOAD_CH_iLoadMoveStatus] CHECK (iLoadMoveStatus BETWEEN 219 AND 246)
                                        NOT NULL,
    dMoveDate           DATETIME2       CONSTRAINT [LOAD_DF_dMoveDate] DEFAULT SYSDATETIME(),
    sLoadMessage        VARCHAR(40)     NULL,
    iLoadPresenceCheck  INTEGER         CONSTRAINT [LOAD_DF_iLoadPresenceCheck] DEFAULT 1
                                        CONSTRAINT [LOAD_CH_iLoadPresenceCheck] CHECK (iLoadPresenceCheck IN (1, 2))
                                        NOT NULL,
    iHeight             INTEGER         CONSTRAINT [LOAD_DF_iHeight] DEFAULT 1,
    iLength             INTEGER         CONSTRAINT [LOAD_DF_iLength] DEFAULT 1,
    iWidth              INTEGER         CONSTRAINT [LOAD_DF_iWidth] DEFAULT 1,
    sRecommendedZone    VARCHAR(3)      NULL,
    sDeviceID           VARCHAR(9)      NULL,
    sNextWarehouse      VARCHAR(3)      NULL,
    sNextAddress        VARCHAR(9)      NULL,
    sNextShelfPosition  VARCHAR(3)      CONSTRAINT [LOAD_DF_sNextShelfPosition] DEFAULT '000',
    sFinalWarehouse     VARCHAR(3)      NULL,
    sFinalAddress       VARCHAR(9)      NULL,
    iAmountFull         INTEGER         CONSTRAINT [LOAD_DF_iAmountFull] DEFAULT 234
                                        CONSTRAINT [LOAD_CH_iAmountFull] CHECK (iAmountFull BETWEEN 234 AND 238)
                                        NOT NULL,
    sMCKey              VARCHAR(8)      NULL,
    sBCRData            VARCHAR(30)     NULL,
    fWeight             NUMERIC(13,3)   CONSTRAINT [LOAD_DF_fWeight] DEFAULT 0.0,
    iGroupNo            INTEGER         CONSTRAINT [LOAD_DF_iGroupNo] DEFAULT 0,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [LOAD_PK] PRIMARY KEY (iID),
    CONSTRAINT [LOAD_UK_PARENT] UNIQUE (sParentLoad, sLoadID),
    CONSTRAINT [LOAD_UK_LOADID] UNIQUE CLUSTERED (sLoadID)
                          ON [EBSWRxJIndex]
) ON [EBSWRxJData];


ALTER TABLE asrs.LOAD ADD CONSTRAINT [LOAD_FK_LOCATION] FOREIGN KEY (sWarehouse, sAddress) REFERENCES asrs.LOCATION(sWarehouse, sAddress);
GO

CREATE INDEX LOAD_IDX_MCKEY ON asrs.LOAD (sWarehouse, sAddress);
CREATE INDEX LOAD_IDX_ADDRESS ON asrs.LOAD (sMCKey);
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding LOADLINEITEM                +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.LOADLINEITEM
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sItem               VARCHAR(30)     NOT NULL,
    sLot                VARCHAR(30)     NULL,
    sLoadID             VARCHAR(8)      NOT NULL,
    sLineID             VARCHAR(12)     NULL,
    sPositionID         VARCHAR(3)      NULL,
    dLastCCIDate        DATETIME2       CONSTRAINT [LOADLINEITEM_DF_dLastCCIDate] DEFAULT SYSDATETIME()
                                        NOT NULL,
    dAgingDate          DATETIME2       CONSTRAINT [LOADLINEITEM_DF_dAgingDate] DEFAULT SYSDATETIME()
                                        NOT NULL,
    dExpirationDate     DATETIME2       CONSTRAINT [LOADLINEITEM_DF_dExpirationDate] DEFAULT SYSDATETIME()
                                        NOT NULL,
    fCurrentQuantity    NUMERIC(13,3)   CONSTRAINT [LOADLINEITEM_DF_fCurrentQuantity] DEFAULT 0.0
                                        NOT NULL,
    fAllocatedQuantity  NUMERIC(13,3)   CONSTRAINT [LOADLINEITEM_DF_fAllocatedQuantity] DEFAULT 0.0
                                        NOT NULL,
    sOrderID            VARCHAR(30)     NULL,
    sOrderLot           VARCHAR(30)     NULL,
    iHoldType           INTEGER         CONSTRAINT [LOADLINEITEM_DF_iHoldType] DEFAULT 168
                                        CONSTRAINT [LOADLINEITEM_CH_iHoldType] CHECK (iHoldType BETWEEN 168 AND 172)
                                        NOT NULL,
    iPriorityAllocation INTEGER         CONSTRAINT [LOADLINEITEM_DF_iPriorityAllocation] DEFAULT 2
                                        CONSTRAINT [LOADLINEITEM_CH_iPriorityAllocation] CHECK (iPriorityAllocation IN (1, 2))
                                        NOT NULL,
    sHoldReason         VARCHAR(3)      NULL,
    sExpectedReceipt    VARCHAR(30)     NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [LOADLINEITEM_PK] PRIMARY KEY CLUSTERED (iID),
    CONSTRAINT [LOADLINEITEM_UK] UNIQUE (sLoadID, sItem, sLot, sLineID, sPositionID, sOrderID, sOrderLot)
                ON [EBSWRxJIndex]
) ON [EBSWRxJData];

ALTER TABLE asrs.LOADLINEITEM ADD CONSTRAINT [LOADLINEITEM_FK_LOAD] FOREIGN KEY (sLoadID) REFERENCES asrs.LOAD(sLoadID);
ALTER TABLE asrs.LOADLINEITEM ADD CONSTRAINT [LOADLINEITEM_FK_ITEMMASTER] FOREIGN KEY (sItem) REFERENCES asrs.ITEMMASTER(sItem);
GO

CREATE INDEX LOADLINEITEM_IDX_LOAD ON asrs.LOADLINEITEM (sLoadID);
CREATE INDEX LOADLINEITEM_IDX_ITEM ON asrs.LOADLINEITEM (sItem);
GO


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding LOGIN                       +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.LOGIN
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sUserID             VARCHAR(13)     NOT NULL,
    sRole               VARCHAR(20)     NULL,
    dLoginTime          DATETIME2       CONSTRAINT [LOGIN_DF_dLoginTime] DEFAULT SYSDATETIME(),
    sMachineName        VARCHAR(30)     NOT NULL,
    sIPAddress          VARCHAR(15)     NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [LOGIN_PK] PRIMARY KEY (iID),
    CONSTRAINT [LOGIN_UK] UNIQUE CLUSTERED (sUserID, sMachineName)
                        ON [EBSWRxJIndex]
) ON [EBSWRxJData];

ALTER TABLE asrs.LOGIN ADD CONSTRAINT [LOGIN_FK_EMPLOYEE] FOREIGN KEY (sUserID) REFERENCES asrs.EMPLOYEE(sUserID);
GO


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding ORDERHEADER                 +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.ORDERHEADER
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sOrderID             VARCHAR(30)    NOT NULL,
    dOrderedTime         DATETIME2      CONSTRAINT [ORDERHEADER_DF_dOrderedTime] DEFAULT SYSDATETIME()
                                        NOT NULL,
    dScheduledDate       DATETIME2      CONSTRAINT [ORDERHEADER_DF_dScheduledDate] DEFAULT SYSDATETIME()
                                        NOT NULL,
    dShortOrderCheckTime DATETIME2      CONSTRAINT [ORDERHEADER_DF_dShortOrderCheckTime] DEFAULT SYSDATETIME()
                                        NOT NULL,
    iPriority            INTEGER        CONSTRAINT [ORDERHEADER_DF_iPriority] DEFAULT 5
                                        NOT NULL,
    iOrderType           INTEGER        CONSTRAINT [ORDERHEADER_DF_iOrderType] DEFAULT 1
                                        CONSTRAINT [ORDERHEADER_CH_iOrderType] CHECK (iOrderType IN (1, 2, 4, 8, 16))
                                        NOT NULL,
    sDestinationStation  VARCHAR(4)     NULL,
    sDescription         VARCHAR(40)    NULL,
    iOrderStatus         INTEGER        CONSTRAINT [ORDERHEADER_DF_iOrderStatus] DEFAULT 229
                                        CONSTRAINT [ORDERHEADER_CH_iOrderStatus] CHECK (iOrderStatus BETWEEN 229 AND 243)
                                        NOT NULL,
    iNextStatus          INTEGER        CONSTRAINT [ORDERHEADER_DF_iNextStatus] DEFAULT 230
                                        CONSTRAINT [ORDERHEADER_CH_iNextStatus] CHECK (iNextStatus BETWEEN 229 AND 243)
                                        NOT NULL,
    sOrderMessage        VARCHAR(256)   NULL,
    sReleaseToCode       VARCHAR(3)     NULL,
    sCarrierID           VARCHAR(6)     NULL,
    sDestWarehouse       VARCHAR(3)     NULL,
    sDestAddress         VARCHAR(9)     NULL,
    sShipCustomer        VARCHAR(15)    NULL,
    iHostLineCount       INTEGER        CONSTRAINT [ORDERHEADER_DF_iHostLineCount] DEFAULT 1
                                        NOT NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [ORDERHEADER_PK] PRIMARY KEY (iID),
    CONSTRAINT [ORDERHEADER_UK] UNIQUE CLUSTERED (sOrderID)
                        ON [EBSWRxJIndex]
) ON [EBSWRxJData];

ALTER TABLE asrs.ORDERHEADER ADD CONSTRAINT [ORDERHEADER_FK_CUSTOMER] FOREIGN KEY (sShipCustomer) REFERENCES asrs.CUSTOMER(sCustomer);
GO


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding ORDERLINE                   +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.ORDERLINE
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sOrderID           VARCHAR(30)   NOT NULL,
    sItem              VARCHAR(30)   NULL,
    sOrderLot          VARCHAR(30)   NULL,
    sLineID            VARCHAR(12)   NULL,
    sRouteID           VARCHAR(12)   NULL,
    fOrderQuantity     NUMERIC(13,3) CONSTRAINT [ORDERLINE_DF_fOrderQuantity] DEFAULT 0.0
                                     NOT NULL,
    fAllocatedQuantity NUMERIC(13,3) CONSTRAINT [ORDERLINE_DF_fAllocatedQuantity] DEFAULT 0.0
                                     NOT NULL,
    fPickQuantity      NUMERIC(13,3) CONSTRAINT [ORDERLINE_DF_fPickQuantity] DEFAULT 0.0
                                     NOT NULL,
    fShipQuantity      NUMERIC(13,3) CONSTRAINT [ORDERLINE_DF_fShipQuantity] DEFAULT 0.0
                                     NOT NULL,
    sLoadID            VARCHAR(8)    NULL,
    sDescription       VARCHAR(40)   NULL,
    sContainerType     VARCHAR(15)   NULL,
    sWarehouse         VARCHAR(3)    NULL,  -- Maintenance by warehouse.
    sBeginWarehouse    VARCHAR(3)    NULL,  -- Maintenance by Location range.
    sBeginAddress      VARCHAR(9)    NULL,  --  ...
    sEndingWarehouse   VARCHAR(3)    NULL,  --  ...
    sEndingAddress     VARCHAR(9)    NULL,  --  ...
    iHeight            INTEGER       CONSTRAINT [ORDERLINE_DF_iHeight] DEFAULT 1,
    iLineShy           INTEGER       CONSTRAINT [ORDERLINE_DF_iLineShy] DEFAULT 2
                                     CONSTRAINT [ORDERLINE_CH_iLineShy] CHECK (iLineShy IN (1, 2))
                                     NOT NULL,
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [ORDERLINE_PK] PRIMARY KEY (iID),
    CONSTRAINT [ORDERLINE_UK] UNIQUE CLUSTERED (sItem, sOrderLot, sLineID, sOrderID, sLoadID)
                        ON [EBSWRxJIndex]
) ON [EBSWRxJData];

ALTER TABLE asrs.ORDERLINE ADD CONSTRAINT [ORDERLINE_FK_ORDERHEADER] FOREIGN KEY (sOrderID) REFERENCES asrs.ORDERHEADER(sOrderID);
ALTER TABLE asrs.ORDERLINE ADD CONSTRAINT [ORDERLINE_FK_ITEMMASTER] FOREIGN KEY (sItem) REFERENCES asrs.ITEMMASTER(sItem);
GO

CREATE INDEX ORDERLINE_IDX_ORDERID ON asrs.ORDERLINE (sOrderID);
GO


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding PURCHASEORDERLINE           +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.PURCHASEORDERLINE
(
   iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sOrderID            VARCHAR(30)     NOT NULL,
    sItem               VARCHAR(30)     NULL,
    sLot                VARCHAR(30)     NULL,
    sLineID             VARCHAR(12)     NULL,
    fExpectedQuantity   NUMERIC(13,3)   CONSTRAINT [PURCHASEORDERLINE_DF_fExpectedQuantity] DEFAULT 1
                                        NOT NULL,
    fReceivedQuantity   NUMERIC(13,3)   CONSTRAINT [PURCHASEORDERLINE_DF_fReceivedQuantity] DEFAULT 0
                                        NOT NULL,
    fCaseQuantity       NUMERIC(13,3)   CONSTRAINT [PURCHASEORDERLINE_DF_fCaseQuantity] DEFAULT 0.0
                                        NOT NULL,
    iInspection         INTEGER         CONSTRAINT [PURCHASEORDERLINE_DF_iInspection] DEFAULT 2
                                        CONSTRAINT [PURCHASEORDERLINE_CH_iInspection] CHECK (iInspection IN (1, 2))
                                        NOT NULL,
    dExpirationDate     DATETIME2       CONSTRAINT [PURCHASEORDERLINE_DF_dExpirationDate] DEFAULT SYSDATETIME()
                                        NOT NULL,
    sHoldReason         VARCHAR(3)      NULL,
    sRouteID            VARCHAR(12)     NULL,
    sLoadID             VARCHAR(8)      NULL,


    iHeight               INTEGER       CONSTRAINT [PURCHASEORDERLINE_DF_iHeight] DEFAULT 1 NULL,

    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [PURCHASEORDERLINE_PK] PRIMARY KEY (iID),
    CONSTRAINT [PURCHASEORDERLINE_UK] UNIQUE CLUSTERED (sItem, sLot, sLineID, sOrderID)
                        ON [EBSWRxJIndex]
) ON [EBSWRxJData];

ALTER TABLE asrs.PURCHASEORDERLINE ADD CONSTRAINT [PURCHASEORDERLINE_FK_PURCHASEORDERHEADER] FOREIGN KEY (sOrderID) REFERENCES asrs.PURCHASEORDERHEADER(sOrderID);
GO

CREATE INDEX PURCHASEORDERLINE_IDX_ORDER ON asrs.PURCHASEORDERLINE (sOrderID);
GO


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding ROLEOPTION                  +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.ROLEOPTION
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sRole               VARCHAR(20)     NOT NULL,
    sCategory           VARCHAR(15)     NOT NULL,
    sOption             VARCHAR(20)     NOT NULL,
    sIconName           VARCHAR(25)     NULL,
    sClassName          VARCHAR(110)    NOT NULL,
    iButtonBar          INTEGER         CONSTRAINT [ROLE_DF_iButtonBar] DEFAULT 2
                                        CONSTRAINT [ROLE_CH_iButtonBar] CHECK (iButtonBar IN (1, 2))
                                        NOT NULL,
    iAddAllowed         INTEGER         CONSTRAINT [ROLE_DF_iAddAllowed] DEFAULT 2
                                        CONSTRAINT [ROLE_CH_iAddAllowed] CHECK (iAddAllowed IN (1, 2))
                                        NOT NULL,
    iModifyAllowed      INTEGER         CONSTRAINT [ROLE_DF_iModifyAllowed] DEFAULT 2
                                        CONSTRAINT [ROLE_CH_iModifyAllowed] CHECK (iModifyAllowed IN (1, 2))
                                        NOT NULL,
    iDeleteAllowed      INTEGER         CONSTRAINT [ROLE_DF_iDeleteAllowed] DEFAULT 2
                                        CONSTRAINT [ROLE_CH_iDeleteAllowed] CHECK (iDeleteAllowed IN (1, 2))
                                        NOT NULL,
    iViewAllowed        INTEGER         CONSTRAINT [ROLE_DF_iViewAllowed] DEFAULT 1
                                        CONSTRAINT [ROLE_CH_iViewAllowed] CHECK (iViewAllowed IN (1, 2))
                                        NOT NULL,
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [ROLEOPTION_PK] PRIMARY KEY (iID),
    CONSTRAINT [ROLEOPTION_UK] UNIQUE CLUSTERED (sRole, sCategory, sOption)
                        ON [EBSWRxJIndex]
) ON [EBSWRxJData];

ALTER TABLE asrs.ROLEOPTION ADD CONSTRAINT [ROLEOPTION_FK_ROLE] FOREIGN KEY (sRole) REFERENCES asrs.ROLE(sRole);
GO

CREATE INDEX ROLEOPTION_IDX_ROLE ON asrs.ROLEOPTION (sRole);
GO


PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding SYNONYMS                    +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.SYNONYMS
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sItem               VARCHAR(30),
    sSynonym            VARCHAR(30),
	
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,
	
    CONSTRAINT [SYNONYMS_PK] PRIMARY KEY (iID),
    CONSTRAINT [SYNONYMS_UK] UNIQUE CLUSTERED (sSynonym)
                       ON [EBSWRxJIndex]
) ON [EBSWRxJData];

ALTER TABLE asrs.SYNONYMS ADD CONSTRAINT [SYNONYMS_FK_ITEMMASTER] FOREIGN KEY (sItem) REFERENCES asrs.ITEMMASTER(sItem);
GO

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding SYSTEM_HB                   +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.SYSTEMHB
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sSystem             VARCHAR(30) 	NULL,
    dHBTime         	DATETIME2       NULL
) ON [EBSWRxJData];

PRINT '++++++++++++++++++++++++++++++++++++++++'
PRINT '+   Adding PLCTOWRX                   +'
PRINT '++++++++++++++++++++++++++++++++++++++++'

CREATE TABLE asrs.PLCTOWRX
(
    iID                 BIGINT IDENTITY (1, 1) NOT NULL,
    sPortName           VARCHAR(25)     NOT NULL,
    dMessageAddTime     DATETIME2       CONSTRAINT [PLCTOWRX_DF_dMessageAddTime] DEFAULT SYSDATETIME(),
    iMessageSequence    INTEGER         NOT NULL,
    iOriginalSequence   INTEGER         NULL,
    sMessageIdentifier  VARCHAR(50)     NOT NULL,
    sMessage            VARCHAR(MAX)    CONSTRAINT [PLCTOWRX_DF_sMessage] DEFAULT '',
    iMessageProcessed   INTEGER         CONSTRAINT [PLCTOWRX_DF_iMessageProcessed] DEFAULT 2
                                        CONSTRAINT [PLCTOWRX_CH_iMessageProcessed] CHECK (iMessageProcessed IN (1, 2, 3)),
    dModifyTime         DATETIME2       NULL,
    sAddMethod          VARCHAR(2000)   NULL,
    sUpdateMethod       VARCHAR(2000)   NULL,

    CONSTRAINT [PLCTOWRX_PK] PRIMARY KEY (iID),
    CONSTRAINT [PLCTOWRX_UK_SEQUENCE] UNIQUE CLUSTERED (iMessageSequence, sPortName),
    CONSTRAINT [PLCTOWRX_UK_PROCESSED] UNIQUE NONCLUSTERED(iMessageProcessed, iMessageSequence, sPortName)
                     ON [EBSWRxJIndex]

) ON [EBSWRxJData];

GO

