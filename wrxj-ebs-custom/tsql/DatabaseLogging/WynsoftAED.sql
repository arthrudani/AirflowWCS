/*=======================================================================*/
/* Database logging within the AED database for integrated Wynsoft       */
/* installations.  When using AED, there should be a database purge job  */
/* that is responsible for maintaining these tables.                     */
/*=======================================================================*/


USE [EBSWRXJ]
GO

/****** Object:  Synonym [asrs].[S_AED_WRX_LOG_ERR]    Script Date: 8/8/2020 9:29:20 AM ******/
DROP SYNONYM [asrs].[S_AED_WRX_LOG_ERR]
GO

/****** Object:  Synonym [asrs].[S_AED_WRX_LOG_ERR]    Script Date: 8/8/2020 9:29:20 AM ******/
CREATE SYNONYM [asrs].[S_AED_WRX_LOG_ERR] FOR [asrs].[WRXLOG]
GO


/****** Object:  Synonym [asrs].[S_AED_WRX_LOG_INF]    Script Date: 8/8/2020 9:29:46 AM ******/
DROP SYNONYM [asrs].[S_AED_WRX_LOG_INF]
GO

/****** Object:  Synonym [asrs].[S_AED_WRX_LOG_INF]    Script Date: 8/8/2020 9:29:46 AM ******/
CREATE SYNONYM [asrs].[S_AED_WRX_LOG_INF] FOR [asrs].[WRXLOG]
GO
