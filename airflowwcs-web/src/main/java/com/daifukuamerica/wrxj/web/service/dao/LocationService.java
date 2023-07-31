/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Warehouse;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LocationDataModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Location Service
 */
public class LocationService
{
	private static final Logger logger = LoggerFactory.getLogger("Location");

	private final String metaId = "Location";

	/**
	 * Find a specific location by id
	 *
	 * @param warehouse
	 * @param address
	 * @param position
	 * @return LocationDataModel
	 */
	public LocationDataModel find(String warehouse, String address, String position)
	{
		LocationDataModel ldm = null;
		try
		{
			StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
			LocationData vpLocData = vpLocServer.getLocationRecord(warehouse, address);
			ldm = new LocationDataModel(vpLocData);
		}
		catch (Exception e)
		{
			logger.error("LocationService (find) Exception for warehouse=[{}], address=[{}], position=[{}]", warehouse, address, position, StackTraceFilter.filter(e));
		}
		return ldm;
	}

	/**
	 * List all locations
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		return list(null);
	}

	/**
	 * List locations by key
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TableDataModel list(LocationData ipLocKey) throws DBException, NoSuchFieldException
	{
		if (ipLocKey == null)
		{
			ipLocKey = Factory.create(LocationData.class);
		}
		Warehouse whs = Factory.create(Warehouse.class);
		StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
		//List<Map> utLocationData = vpLocServer.getLocationData(ipLocKey);
        final EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		List<Map> utLocationData = (List<Map>)vpTJLoadHandler.getLocationData(ipLocKey);
		// Table data needs an ID column.  Sometimes address is good enough, but it is not when
		//  1) there are multiple aisles with the same banks
		//  2) the system is using shelf position
		// To work around this, we'll create a composite column warehouse-address-position
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map m : utLocationData)
		{
			String w = (String)m.get(LocationData.WAREHOUSE_NAME);
			String a = (String)m.get(LocationData.ADDRESS_NAME);
			String p = (String)m.get(LocationData.SHELFPOSITION_NAME);
			m = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(m, dbColumns, transColumns, metaId);

			m.put("id", w + "-" + a + "-" + p);
			m.put("warehouseType", whs.getWarehouseType(w));
			//m.put("warehouseType",(String)m.get(LocationData.WAREHOUSE_TYPE));
		}
		//AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(utLocationData, metaId);
		TableDataModel results = new TableDataModel(utLocationData);
		return results;
	}
	
	

	/**
	 * Location list by load ID
	 *
	 * @param isLoadId
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listByLoadId(String isLoadId) throws DBException, NoSuchFieldException
	{
		TableDataModel results = new TableDataModel();

		if (SKDCUtility.isNotBlank(isLoadId))
		{
			LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadId);
			if (vpLoadData != null)
			{
				LocationData vpLocKey = Factory.create(LocationData.class);
				vpLocKey.setKey(LocationData.WAREHOUSE_NAME, vpLoadData.getWarehouse());
				vpLocKey.setKey(LocationData.ADDRESS_NAME, vpLoadData.getAddress());
				vpLocKey.setKey(LocationData.SHELFPOSITION_NAME, vpLoadData.getShelfPosition());
				StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
				List<Map> utLocationData = vpLocServer.getLocationData(vpLocKey);

				AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(utLocationData, metaId);
				results = new TableDataModel(utLocationData);
			}
		}

		return results;
	}

	/**
	 * Modify a location with LocationDataModel
	 *
	 * @param dataModel - LocationDataModel
	 * @return {@link AjaxResponse}
	 */
	public AjaxResponse modify(LocationDataModel dataModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			if (SKDCUtility.isBlank(dataModel.getWarehouse()))
			{
				throw new IllegalArgumentException("Warehouse may not be blank");
			}
			if (SKDCUtility.isBlank(dataModel.getAddress()))
			{
				throw new IllegalArgumentException("Address may not be blank");
			}
			/*
			 * if (SKDCUtility.isBlank(dataModel.getShelfPosition())) { throw new
			 * IllegalArgumentException("Shelf Position may not be blank"); }
			 */

			LocationData vpLocData = Factory.create(LocationData.class);
			vpLocData.setKey(LocationData.WAREHOUSE_NAME, dataModel.getWarehouse());
			vpLocData.setKey(LocationData.ADDRESS_NAME, dataModel.getAddress());
			vpLocData.setWarehouse(dataModel.getWarehouse());
			vpLocData.setAddress(dataModel.getAddress());
			//vpLocData.setKey(LocationData.SHELFPOSITION_NAME, dataModel.getShelfPosition());
			vpLocData.setLocationStatus(dataModel.getLocationStatus());
			vpLocData.setEmptyFlag(dataModel.getEmptyFlag());
			vpLocData.setLocationType(dataModel.getType());

			StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
			vpLocServer.modifyLocation(vpLocData);
		}
		catch (IllegalArgumentException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("ShelfService (modify) Exception for ({})", dataModel.describeLocation(), StackTraceFilter.filter(e));
		}
		catch (DBException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There was a database exception: " + e.getMessage());
			logger.error("ShelfService (modify) Exception for ({})", dataModel.describeLocation(), StackTraceFilter.filter(e));
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Updated location " + dataModel.describeLocation());
		return ajaxResponse;
	}
}
