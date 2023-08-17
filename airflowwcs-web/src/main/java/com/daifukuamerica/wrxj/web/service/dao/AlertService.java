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
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
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
 * Alert Service to handle any CRUD actions or business logic for Alerts.
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
public class AlertService
{
	/**
	 * Log4j logger: AlertService
	 */
	private static final Logger logger = LoggerFactory.getLogger("ALERT");


	private AjaxResponse ajaxResponse;
	private final String metaId = "Alert";
	protected DBObject  mpDBObject;

	
	/**
	 * List all Alerts
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		AlertsData vpAlertKey = new AlertsData();
		StandardAlertServer alertServer = new StandardAlertServer(DBConstantsWeb.DB_NAME);
		List<Map> utAlertData = alertServer.getAlertDataList(vpAlertKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utAlertData) // TODO this is ugly as hell but I am tired
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(utAlertData);
		return results;

	}

	/**
	 * List alerts by search criteria
	 * @param description 
	 *
	 * @return JSON of AlertDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(AlertsData alertData) throws DBException, NoSuchFieldException
	{
		TableDataModel results = new TableDataModel();
		
		Alerts vpAlert = Factory.create(Alerts.class);
		//TODO - Implement search by item in baseline
        final EBSTableJoin vpAlertHandler = new EBSTableJoin();
        final List<Map> utAlertData = (List<Map>)vpAlertHandler.getAlertListWeb(alertData);
		////List<Map> utLoadData = vpLoadHandler.getAllElements(searchLDData);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utAlertData)
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		 results = new TableDataModel(utAlertData);
		return results;
	}

	public AjaxResponse changeStatus(String alert, String status) {
		try {
			AjaxResponse ajaxResponse= new AjaxResponse();
			TransactionToken ttok = null;
			ensureDBConnection();
			ttok = mpDBObject.startTransaction();
			EBSTableJoin vpTJAlertHandler = new EBSTableJoin();
			if(vpTJAlertHandler.changeAlertStatus(alert,status)) {
				ajaxResponse.setResponse(1,"Successfully edited");
			}
			mpDBObject.commitTransaction(ttok);
			mpDBObject.endTransaction(ttok);
			removeDBConnection();
		}
		catch(Exception e) {
			ajaxResponse.setResponse(0,"Can not change status");
		}
		
		return ajaxResponse;
	}
	
	public AjaxResponse changeAllStatus(int status) {
		try {
			AjaxResponse ajaxResponse= new AjaxResponse();
			TransactionToken ttok = null;
			ensureDBConnection();
			ttok = mpDBObject.startTransaction();
			EBSTableJoin vpTJAlertHandler = new EBSTableJoin();
			if(vpTJAlertHandler.changeAllAlertStatus(status)) {
				ajaxResponse.setResponse(1,"Successfully edited");
			}
			mpDBObject.commitTransaction(ttok);
			mpDBObject.endTransaction(ttok);
			removeDBConnection();
		}
		catch(Exception e) {
			ajaxResponse.setResponse(0,"Can not change status");
		}
		
		return ajaxResponse;
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
