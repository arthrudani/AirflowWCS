package com.daifukuamerica.wrxj.web.service.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadDataAndLLIData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.InKeyObject;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
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
 * Load Service to handle any CRUD actions or business logic for Loads.
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
public class LoadService
{
	/**
	 * Log4j logger: LoadService
	 */
	private static final Logger logger = LoggerFactory.getLogger("LOAD");


	private AjaxResponse ajaxResponse;
	private final String metaId = "Load";
	private final String flightmetaId = "Flight";
	private final String flightDetailsmetaId = "FlightDetails";

	/**
	 * Add a load to wrxj database via LoadDataModel
	 *
	 * TODO - remove some of this validation and put it into a validator class
	 *
	 * @param loadAndLLIDataModel
	 *            - load to be added
	 * @return {@link AjaxResponse}
	 * @throws NoSuchFieldException
	 * @throws DBException 
	 */
	public AjaxResponse add(loadAndLLIDataModel loadAndLLIDataModel) throws NoSuchFieldException, DBException
	{
		StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
		ajaxResponse = new AjaxResponse();
		LoadData loadData = loadAndLLIDataModel.getLoadData();
		LoadLineItemData LLIData = loadAndLLIDataModel.getLoadLineItemData();
		LLIData.setLoadID(loadAndLLIDataModel.getLoadId());
		System.out.println(LLIData);
		try
		{
		    // Rudimentary validation
            if (SKDCUtility.isBlank(loadAndLLIDataModel.getLoadId()))
            {
              throw new Exception("Load ID cannot be blank!");
            }
            if (SKDCUtility.isBlank(loadAndLLIDataModel.getAddress()))
            {
              throw new Exception("Address cannot be blank!");
            }
			mpLoadServer.addLoad(loadData);
			mpLoadServer.addLoadLineItem(LLIData);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Failed to add load: " + e.getMessage());
			e.printStackTrace();
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully added load");
		}

		return ajaxResponse;
	}

	/**
	 * Delete a load with the specified ID
	 *
	 * @param id
	 * @return {@link AjaxResponse}
	 * @throws DBException
	 */
	public AjaxResponse delete(String loadId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		//StandardInventoryServer mpInvServer = new StandardInventoryServer();
		EBSInventoryServer mpInvServer = new EBSInventoryServer();
		try
		{
			mpInvServer.deleteLoadWithAllData(loadId, "");
		}
		catch (DBException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "A database exception has occured");
			logger.error("LoadService (delete) Exception: {}", e.getMessage());
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted load: " + loadId);
			logger.info("Deleted Load ID : {}", loadId);
		}
		return ajaxResponse;
	}

	/**
	 * Modify a load with LoadDataModel {@see StandardLoadServer}
	 *
	 * @param req
	 * @param resp
	 * @return {@link AjaxResponse}
	 */
	public AjaxResponse modify(LoadDataModel loadDataModel)

	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
		try
		{
			if (loadDataModel.getLoadDate() == null)
				loadDataModel.setLoadDate(new Date());
			// StandardLoadServer.updateLoadData is a TERRIBLE method that swallows exceptions
			// Try to avoid some false successes
			if (SKDCUtility.isBlank(loadDataModel.getWarehouse()))
			{
			  throw new Exception("Warehouse cannot be blank!");
			}
			if (SKDCUtility.isBlank(loadDataModel.getNextWarehouse()) && SKDCUtility.isNotBlank(loadDataModel.getNextAddress()))
            {
              throw new Exception("Next Warehouse cannot be blank when Next Address is not blank!");
            }
            if (SKDCUtility.isBlank(loadDataModel.getFinalWarehouse()) && SKDCUtility.isNotBlank(loadDataModel.getFinalAddress()))
            {
              throw new Exception("Final Warehouse cannot be blank when Final Address is not blank!");
            }
			mpLoadServer.updateLoadData(loadDataModel.getLoadData(), false);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error modifying load: " + e.getMessage());
			logger.error("LoadService (modify) Exception: {}", e.getMessage());
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Updated load " + loadDataModel.getLoadId());
		return ajaxResponse;
	}

	/**
	 * Find a specific load by ID
	 *
	 * @param loadId
	 * @return LoadDataModel
	 */
	public LoadDataModel findLoad(String loadId)
	{
		StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
		LoadData loadData = mpLoadServer.getLoad(loadId);
		LoadDataModel ldm = null;
		try
		{
			ldm = new LoadDataModel(loadData);
			ldm.setAmountFull(ldm.getAmountFull()); // this will set our string
													// value for the amount full
		}
		catch (NoSuchFieldException e)
		{
			logger.error("LoadService (findLoad) Exception: {}", e.getMessage());
		}

		return ldm;
	}

	/**
	 * Search all loads for matching criteria present in LoadDataModel
	 *
	 * @param ldm
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel searchLoads(LoadDataModel ldm)
	{
		// Results
		List<Map> results = null;
		TableDataModel tableDataModel;
		// Search
		// Search keys
		LoadData vpLoadData = new LoadData();
		LoadLineItemData vpLLIData = new LoadLineItemData();

		// Servers
		StandardInventoryServer mpInvServer = new StandardInventoryServer();
		StandardLoadServer mpLoadServer = new StandardLoadServer();
		if (ldm.getLoadId() != null)
		{
			KeyObject loadKey = new KeyObject(LoadData.LOADID_NAME, ldm.getLoadId());
			loadKey.setComparison(KeyObject.LIKE);
			vpLoadData.addKeyObject(loadKey);

		}
		if (ldm.getParentLoad() != null && ldm.getParentLoad() != "")
			vpLoadData.addKeyObject(new KeyObject(LoadData.PARENTLOAD_NAME, ldm.getParentLoad()));
		if (ldm.getMoveStatus() != null && ldm.getMoveStatus() != "")
			vpLoadData.addKeyObject(new KeyObject(LoadData.LOADMOVESTATUS_NAME, ldm.getMoveStatus()));
		if (ldm.getContainerType() != null && ldm.getContainerType() != "")
			vpLoadData.addKeyObject(new KeyObject(LoadData.CONTAINERTYPE_NAME, ldm.getContainerType()));
		if (ldm.getZone() != null && ldm.getZone() != "")
			vpLoadData.addKeyObject(new KeyObject(LoadData.RECOMMENDEDZONE_NAME, ldm.getZone()));
		if (ldm.getDeviceId() != null && ldm.getDeviceId() != "")
			vpLoadData.addKeyObject(new KeyObject(LoadData.DEVICEID_NAME, ldm.getDeviceId()));
		if (ldm.getWarehouse() != null && ldm.getWarehouse() != "")
			vpLoadData.addKeyObject(new KeyObject(LoadData.WAREHOUSE_NAME, ldm.getWarehouse()));
		if (ldm.getFinalWarehouse() != null && ldm.getFinalWarehouse() != "")
			vpLoadData.addKeyObject(new KeyObject(LoadData.FINALWAREHOUSE_NAME, ldm.getFinalWarehouse()));

		// if key is not empty, get matching loads
		if (vpLLIData.getKeyArray().length > 0)
		{
			try
			{
				List<Map> vpLLIList = mpInvServer.getLoadLineItemDataList(vpLLIData);
				if (vpLLIList.size() == 0)
				{
					vpLoadData.addKeyObject(new KeyObject(LoadData.LOADID_NAME, ""));
				} else
				{
					// Matches, but may not be unique if multiple items per load
					// Note: Oracle croaks if there are > 1000 loads in the set
					Set<String> vpLoadSet = new TreeSet<String>();
					for (Map m : vpLLIList)
					{
						vpLLIData.clear();
						vpLLIData.dataToSKDCData(m);
						vpLoadSet.add(vpLLIData.getLoadID());
					}
					vpLoadData.addKeyObject(new InKeyObject(LoadData.LOADID_NAME, vpLoadSet.toArray()));
				}
			} catch (DBException e)
			{
				logger.error("LoadService (searchLoads) Exception: {}", e.getMessage());
			}
		}

		try
		{
			results = mpLoadServer.getLoadDataList(vpLoadData);
		}
		catch (DBException e)
		{
			logger.error("LoadService (searchLoads) Exception: {}", e.getMessage());
		}
		tableDataModel = new TableDataModel(results);

		return tableDataModel;

	}

	/**
	 * List all Loads
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel list(ServletContext context) throws DBException, NoSuchFieldException
	{

		LoadData vpLoadKey = new LoadData();
		StandardLoadServer loadServer = new StandardLoadServer(DBConstantsWeb.DB_NAME);
		List<Map> utLoadData = loadServer.getLoadDataList(vpLoadKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utLoadData) // TODO this is ugly as hell but I am tired
									// come back to this
		{
//			row.replace(LoadData.AMOUNTFULL_NAME, WrxjConnection.getInstance().DBTrans
//					.getStringValue(LoadData.AMOUNTFULL_NAME, (int) row.get(LoadData.AMOUNTFULL_NAME)));
//			row.replace(LoadData.LOADPRESENCECHECK_NAME, WrxjConnection.getInstance().DBTrans
//					.getStringValue(LoadData.LOADPRESENCECHECK_NAME, (int) row.get(LoadData.LOADPRESENCECHECK_NAME)));
			row.replace(LoadData.LOADMOVESTATUS_NAME,DBTrans.getStringValue("ILOADMOVESTATUS", (int) row.get(LoadData.LOADMOVESTATUS_NAME)));
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(utLoadData);
	    /*DBObjectPoolUtil.returnDBObject(context, dbo);*/
		return results;

	}

	/**
	 * List loads by search criteria
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(LoadData searchLDData, String isItem) throws DBException, NoSuchFieldException
	{
		TableDataModel results = new TableDataModel();
		
		Load vpLoadHandler = Factory.create(Load.class);
		//TODO - Implement search by item in baseline
        final EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
        final List<Map> utLoadData = (List<Map>)vpTJLoadHandler.getLoadListWeb(searchLDData, isItem, null);
		//List<Map> utLoadData = vpLoadHandler.getAllElements(searchLDData);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utLoadData)
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		 results = new TableDataModel(utLoadData);
		return results;
	}

	public boolean isPicksRemainingOnLoad(String loadId) throws DBException
	{
		StandardMoveServer mpMoveServ = Factory.create(StandardMoveServer.class);
		boolean picksRemaining = (mpMoveServ.getMoveCount("", loadId, "") > 0);

		return (picksRemaining);
	}

	/**
	 * Validate load for release & release loadId from stationId
	 *
	 * @param loadId
	 *            - load to release
	 * @param stationId
	 *            - station to release from
	 * @param amountFull
	 *            - amount full on released load (to update)
	 * @return AjaxReponse - pass/fail response
	 * @throws DBException
	 */
	public AjaxResponse releaseLoad(String loadId, String stationId, int amountFull) throws DBException
	{

		StandardLoadServer mpLoadServ = Factory.create(StandardLoadServer.class);
		StandardStationServer mpStationServ = Factory.create(StandardStationServer.class);
		StandardPickServer mpPickServ = Factory.create(StandardPickServer.class);
		ajaxResponse = new AjaxResponse();
		if (!mpLoadServ.loadExists(loadId))
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Load: " + loadId + " does not exist!");
		}
		if (isPicksRemainingOnLoad(loadId))
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,
					"Load: " + loadId + " has picks remaining. Use pick screen to complete work before releasing.");
		}
		// TODO - implement isAnyItemNotAccepted()

		mpLoadServ.setLoadAmountFull(loadId, amountFull);
		StationData vpStationData = mpStationServ.getStation(stationId);
		String releaseErrorMessage = mpPickServ.releaseLoad(loadId, vpStationData);
		if (releaseErrorMessage != null)
		{
			logger.error("Error releasing load: {}", releaseErrorMessage);
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to release load: " + releaseErrorMessage);
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS,
					"Successfully released LOAD: " + loadId + " from STATION: " + stationId);
			logger.info("Load: {} released from Station: {}", loadId, stationId);
		}

		return ajaxResponse;
	}

	/**
	 * Get the load data object at the given PLOCK station
	 *
	 * @param station
	 * @return String - loadId at station
	 */
	public LoadData getLoadDataAtStation(String station)
	{
		StandardStationServer mpStationServ = Factory.create(StandardStationServer.class);
		StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);

		String vsStnWhs = mpStationServ.getStationWarehouse(station);
		LoadData vpLoadData = null;
		try
		{
			vpLoadData = vpLoadServ.getOldestLoad(vsStnWhs, station, DBConstants.ARRIVED); // check to see if there are
																							// loads in ARRIVED status
		}
		catch (DBException e)
		{
			logger.error("Error getting load data at station: {} Message: {}", station, e.getMessage(), e);
		}

		return vpLoadData;
	}
	
	public AjaxResponse flushLoads(String locationAddress, String userId) throws DBException
	{	
		EBSLoadServer vpLoadServer   = Factory.create(EBSLoadServer.class, DBConstantsWeb.DB_NAME);
		ajaxResponse = new AjaxResponse();
		
		if (locationAddress.isEmpty())
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Aisle selection is empty!");
			return ajaxResponse;
		}
		else
		{
			//KR: commented out to check it out later why cause an error
			vpLoadServer.flushAisle(locationAddress);

			// Error Response
			//logger.error("User ID[' + userId + '] | Error flushing  aisle: " + srcAisle);
			//ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to flush loads in aisle: " + + srcAisle);
			
			// Success Response
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully flushed Address : " + locationAddress);
			logger.info("User ID[' + userId + '] | Successfully flushed Address: {}", locationAddress);			
		}
		return ajaxResponse;
	}
	
	/**
	 * List all Loads
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listFlight() throws DBException, NoSuchFieldException
	{
		TableDataModel results = new TableDataModel();
		final EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		final List<Map> flightData = (List<Map>)vpTJLoadHandler.getFlightList();
		
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(flightmetaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, flightmetaId);
		for (Map row : flightData) 
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, flightmetaId);
		}
		results = new TableDataModel(flightData);
		return results;

	}
	
	/**
	 * List Flight Loads by Id
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listFlightDetailById(String flightId) throws DBException, NoSuchFieldException
	{
		TableDataModel results = new TableDataModel();
		final EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		final List<Map> flightDetailsData = (List<Map>)vpTJLoadHandler.getFlightDetailsList(flightId);
		
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(flightDetailsmetaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, flightDetailsmetaId);
		for (Map row : flightDetailsData) 
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, flightDetailsmetaId);
		}
		results = new TableDataModel(flightDetailsData);
		return results;
	}
	
	/**
	 * List flights by search criteria
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearchFlights(LoadData searchLDData, String isItem, String flightId) throws DBException, NoSuchFieldException
	{
		TableDataModel results = new TableDataModel();

		final EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
        final List<Map> utLoadData = (List<Map>)vpTJLoadHandler.getLoadListWeb(searchLDData, isItem, flightId);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(flightDetailsmetaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, flightDetailsmetaId);
		for (Map row : utLoadData)
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, flightDetailsmetaId);
		}
		 results = new TableDataModel(utLoadData);
		return results;
	}

	public TableDataModel getLoadByAddress(String address) throws DBException, NoSuchFieldException {
		TableDataModel results = new TableDataModel();
		EBSLoad ebsLoad = new EBSLoad();
		List<Map> utLoadData = ebsLoad.getLoadsByAddress(address);
		AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(utLoadData, metaId);
		results = new TableDataModel(utLoadData);
		return results;
	}
	
	

}
