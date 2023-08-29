package com.daifukuamerica.wrxj.web.service.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.swing.SwingUtilities;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daifukuamerica.wrxj.dataserver.standard.StandardAlertServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.Alerts;
import com.daifukuamerica.wrxj.dbadapter.data.AlertsData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadDataAndLLIData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadTransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.LoadTransactionHistoryData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.InKeyObject;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LoadDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.loadAndLLIDataModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLoad;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;

/**
 * LoadTransactionHistory Service to handle any CRUD actions or business logic for Alerts.
 * Validation should happen as much as it can at the Model Validator level with
 * JSR 303 annotations on the models uses for form input, however, if all else
 * fails validation at this level can be accepted.
 *
 * This is an example of the most functional controller to date. Chances are
 * this will become the standard which most controller Services will be built.
 *
 * Author: dystout Created : May 4, 2017
 *
 */
public class LoadTransactionHistoryService
{
	/**
	 * Log4j logger: LoadTransactionHistoryService
	 */
	private static final Logger logger = LoggerFactory.getLogger("LOADTRANSACTIONHISTORY");


	private AjaxResponse ajaxResponse;
	private final String metaId = "LoadTransactionHistory";
	protected DBObject  mpDBObject;

	
	/**
	 * List all LoadTransactionHistory
	 *
	 * @return JSON of LoadTransactionHistoryDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		LoadTransactionHistoryData vpLoadTransactionKey = new LoadTransactionHistoryData();
		StandardInventoryServer loadTransactionServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME);
		EBSInventoryServer ebsInventoryServer=new EBSInventoryServer();
		List<Map> utLoadTransactionData = ebsInventoryServer.getLoadTransactionHistoryList(vpLoadTransactionKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utLoadTransactionData) // TODO this is ugly as hell but I am tired
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(utLoadTransactionData);
		return results;

	}

	/**
	 * List loadTransaction by search criteria
	 * @param description 
	 *
	 * @return JSON of LoadTRansactionHistoryDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(LoadTransactionHistoryData key) throws DBException, NoSuchFieldException
	{
		List<Map> tableData = Factory.create(LoadTransactionHistory.class).getAllElements(key);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for(Map row : tableData)
		{
			row = AsrsMetaDataTransUtil.getInstance().databaseToUiTable(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(tableData);
		return results;
	}
	
	/**
	  *  Method simply ensures database connectivity.  This is useful
	  */
	  protected void ensureDBConnection()
	  {
		if (mpDBObject == null || !mpDBObject.isConnectionActive())
		{
	      mpDBObject = new DBObjectTL().getDBObject();
	      try { mpDBObject.connect(); }
	      catch(DBException e)
	      {
	      }
	    }
	  }  
	  
	  /**
	   * Method to close database connection
	   */
	  protected void removeDBConnection()
	  {
	    boolean threadCheckingOn = true;
	    if(mpDBObject != null)
	    {
	      if(mpDBObject.checkConnected())
	      {
	        try
	        {
	          mpDBObject.disconnect(threadCheckingOn);
	          if ((!threadCheckingOn) ||
	            (!SwingUtilities.isEventDispatchThread()))
	          {
	            mpDBObject = null;
	          }
	        }
	        catch (DBException e)
	        {
	        }
	      }
	    }
	  }

	
}
