package com.daifukuamerica.wrxj.web.service.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swingui.load.OrderLoad;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.util.UnderConstructionException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.OrderDataModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSOrderServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service layer for order operations. Add/Modify/update/delete operation are
 * performed here with parameters passed from controller layer.
 *
 * Author: dystout
 * Created : Sep 6, 2017
 *
 */
public class OrderService
{
	private static final Logger logger = LoggerFactory.getLogger("ORDERMAINT");
	protected AjaxResponse ajaxResponse; // For client response
	private final String metaId = "OrderHeader";

	/**
	 * Add an OrderHeader
	 *
	 * @param orderDataModel
	 * @return
	 * @throws NoSuchFieldException
	 */
	public AjaxResponse add(OrderDataModel orderDataModel) throws NoSuchFieldException
	{
		StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);
		ajaxResponse = new AjaxResponse();
		OrderHeaderData orderHeaderData = orderDataModel.getOrderHeaderData();
		try
		{
			orderServer.addOrderHeader(orderHeaderData);
		} catch (DBException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Failed to add order: " + orderDataModel.getOrderId());
			logger.error("DB ERROR occured trying to add order: {}", orderDataModel.getOrderId());
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS,
					"Successfully added order " + orderDataModel.getOrderId() + "!\n");
		return ajaxResponse;
	}

	/**
	 * Delete an Order by the string order id
	 *
	 * @param id
	 * @return
	 */
	public AjaxResponse delete(String orderId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);
		try
		{
			orderServer.deleteOrder(orderId);
		} catch (DBException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,
					"A database exception occured while trying to delete order: " + orderId);
			logger.error("Error occured deleting Order: {} ERROR: {}", orderId, e.getMessage());
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted Order: " + orderId + "!\n");
		return ajaxResponse;
	}

	/**
	 * Modify an existing order using form entries & ids
	 *
	 * @param orderDataModel
	 * @return
	 */
	public AjaxResponse modify(OrderDataModel orderDataModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);
		try
		{
			orderDataModel.modifyDate = new Date();
			orderServer.modifyOrderHeader(orderDataModel.getOrderHeaderData());
		} catch (NoSuchFieldException | DBException e)
		{
			logger.error("Error occured modifying Order: {} ERROR: {}", orderDataModel.getOrderId(), e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,
					"There was an exception modifying the order: " + orderDataModel.getOrderId());
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Modified order " + orderDataModel.getOrderId());
		return ajaxResponse;
	}

	/**
	 * Search
	 *
	 * @param dataModel
	 * @return
	 */
	public OrderDataModel searchOrders(OrderDataModel dataModel)
	{
		// TODO: OrderService.searchOrders() - implement
		new UnderConstructionException("Implement me!").printStackTrace(System.err);
		return null;
	}

	/**
	 * List out all order header info and translate int values that need it.
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		OrderHeaderData ohdata = Factory.create(OrderHeaderData.class);
		StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);
		List<Map> orderData = orderServer.getOrderHeaderData(ohdata);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : orderData)
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(orderData);
		return results;
	}

	/**
	 * List orders with search criteria
	 *
	 * @param searchOHData
	 * @param isItem
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(OrderHeaderData searchOHData, String isItem)
			throws DBException, NoSuchFieldException
	{
		TableDataModel results = new TableDataModel();

		OrderLineData searchOLData = Factory.create(OrderLineData.class);
		if (SKDCUtility.isNotBlank(isItem))
			searchOLData.setKey(OrderLineData.ITEM_NAME, isItem);

		TableJoin tjServer = Factory.create(TableJoin.class);
		List<Map> utOHData = tjServer.getOrderSearchList(searchOHData, searchOLData);

		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utOHData)
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		results = new TableDataModel(utOHData);
		return results;
	}

	/**
	 * Mark an order as ready
	 *
	 * @param orderId
	 * @return
	 */
	public AjaxResponse markReady(String orderId)
	{
		AjaxResponse tempResp = new AjaxResponse();
		try
		{
			StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);
			orderServer.readyOrder(orderId);
		}
		catch(Exception e)
		{
			tempResp.setResponse(AjaxResponseCodes.FAILURE, "Error marking order READY: " + e.getMessage());
			logger.error("Error occured marking READY - Order: {} ERROR: {}", orderId, e.getMessage());
		}

		if (tempResp.getResponseCode() == AjaxResponseCodes.DEFAULT)
			tempResp.setResponse(AjaxResponseCodes.SUCCESS, "Successfully marked READY Order: " + orderId + "!<br>");
		return tempResp;
	}

	/**
	 * Mark an order as hold
	 *
	 * @param orderId
	 * @return
	 */
	public AjaxResponse markHold(String orderId)
	{
		AjaxResponse tempResp = new AjaxResponse();
		try
		{
			StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);
			orderServer.holdOrder(orderId);
		}
		catch (Exception e)
		{
			tempResp.setResponse(AjaxResponseCodes.FAILURE, "Error marking order HOLD: " + e.getMessage());
			logger.error("Error occured marking HOLD - Order: {} ERROR: {}", orderId, e.getMessage());
		}

		if (tempResp.getResponseCode() == AjaxResponseCodes.DEFAULT)
			tempResp.setResponse(AjaxResponseCodes.SUCCESS, "Successfully marked HOLD Order: " + orderId + "!<br>");
		return tempResp;
	}

	/**
	 * Allocate an order
	 *
	 * @param orderId
	 * @return
	 */
	public AjaxResponse allocateOrder(String orderId)
	{
		AjaxResponse tempResp = new AjaxResponse();
		try
		{
			StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);
			orderServer.allocateOrder(orderId);
		}
		catch(Exception e)
		{
			tempResp.setResponse(AjaxResponseCodes.FAILURE, "Error ALLOCATING order: " + e.getMessage());
			logger.error("Error occured ALLOCATING Order: {} ERROR: {}", orderId, e.getMessage());
		}

		if (tempResp.getResponseCode() == AjaxResponseCodes.DEFAULT)
			tempResp.setResponse(AjaxResponseCodes.SUCCESS, "Successfully marked READY Order: " + orderId + "!<br>");
		return tempResp;
	}

	/**
	 * Allocate orders
	 *
	 * @param orderIds
	 * @return
	 */
	public AjaxResponse markAllocateOrders(String[] orderIds)
	{
		ajaxResponse = new AjaxResponse(); // store a global response for all order Ids passed
		for (String id : orderIds)
		{
			AjaxResponse tempResp = allocateOrder(id);
			if (tempResp.getResponseCode() == AjaxResponseCodes.SUCCESS)// if successfull append response message for
																		// full listing of actions
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage() + tempResp.getResponseMessage());
			if (tempResp.getResponseCode() == AjaxResponseCodes.FAILURE)
			{ // if an instance fails, record the failure so we can model alert for the error
				ajaxResponse.setResponseCode(AjaxResponseCodes.FAILURE);
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage() + tempResp.getResponseMessage());

			}
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponseCode(AjaxResponseCodes.SUCCESS);
		return ajaxResponse;
	}

	/**
	 * Mark orders ready
	 *
	 * @param orderIds
	 * @return
	 */
	public AjaxResponse markOrdersReady(String[] orderIds)
	{
		ajaxResponse = new AjaxResponse(); // store a global response for all order Ids passed
		for (String id : orderIds)
		{
			AjaxResponse tempResp = markReady(id);
			if (tempResp.getResponseCode() == AjaxResponseCodes.SUCCESS)// if successfull append response message for
																		// full listing of actions
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage() + tempResp.getResponseMessage());
			if (tempResp.getResponseCode() == AjaxResponseCodes.FAILURE)
			{ // if an instance fails, record the failure so we can model alert for the error
				ajaxResponse.setResponseCode(AjaxResponseCodes.FAILURE);
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage() + tempResp.getResponseMessage());

			}
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponseCode(AjaxResponseCodes.SUCCESS);
		return ajaxResponse;
	}

	/**
	 * Delete orders
	 *
	 * @param orderIds
	 * @return
	 */
	public AjaxResponse deleteOrders(String[] orderIds)
	{
		ajaxResponse = new AjaxResponse(); // store a global response for all order Ids passed
		for (String id : orderIds)
		{
			AjaxResponse tempResp = delete(id);
			if (tempResp.getResponseCode() == AjaxResponseCodes.SUCCESS)// if successfull append response message for
																		// full listing of actions
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage() + tempResp.getResponseMessage());
			if (tempResp.getResponseCode() == AjaxResponseCodes.FAILURE)
			{ // if an instance fails, record the failure so we can model alert for the error
				ajaxResponse.setResponseCode(AjaxResponseCodes.FAILURE);
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage() + tempResp.getResponseMessage());

			}
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponseCode(AjaxResponseCodes.SUCCESS);
		return ajaxResponse;
	}

	/**
	 * Hold orders
	 *
	 * @param orderIds
	 * @return
	 */
	public AjaxResponse markOrdersHold(String[] orderIds)
	{
		ajaxResponse = new AjaxResponse(); // store a global response for all order Ids passed
		for (String id : orderIds)
		{
			AjaxResponse tempResp = markHold(id);
			if (tempResp.getResponseCode() == AjaxResponseCodes.SUCCESS)// if successfull append response message for
																		// full listing of actions
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage() + tempResp.getResponseMessage());
			if (tempResp.getResponseCode() == AjaxResponseCodes.FAILURE)
			{ // if an instance fails, record the failure so we can model alert for the error
				ajaxResponse.setResponseCode(AjaxResponseCodes.FAILURE);
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage() + "\n" + tempResp.getResponseMessage());

			}
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponseCode(AjaxResponseCodes.SUCCESS);
		return ajaxResponse;
	}

	/**
	 * Find an order
	 *
	 * @param orderId
	 * @return
	 */
	public OrderDataModel find(String orderId)
	{
		OrderDataModel odm = new OrderDataModel();
		OrderHeaderData ohd = new OrderHeaderData();

		try
		{
			StandardOrderServer orderServer = new StandardOrderServer();
			ohd = orderServer.getOrderHeaderRecord(orderId);
			if (ohd == null)
			{
				odm = null;
			}
			else
			{
				odm = new OrderDataModel(ohd);
			}
		} catch (DBException | NoSuchFieldException | ClassCastException e)
		{
			logger.error("ERROR GETTNG ORDER HEADER DATA for:{}. REASON: {}", orderId, e.getMessage());
		}
		return odm;
	}

	/**
	 * Find a scheduled order
	 *
	 * @param orderId
	 * @return
	 */
	public OrderDataModel findScheduledOrder(String orderId)
	{
		OrderDataModel odm = new OrderDataModel();
		OrderHeaderData ohd = new OrderHeaderData();
		OrderHeader oh = new OrderHeader();

		try
		{
			ohd.setKey(OrderHeaderData.ORDERID_NAME, orderId);
			ohd.setKey(OrderHeaderData.ORDERSTATUS_NAME, DBConstants.SCHEDULED);
			ohd = oh.getElement(ohd, DBConstants.NOWRITELOCK);

			if (ohd == null)
			{
				odm = null;
			}
			else
			{
				odm = new OrderDataModel(ohd);
			}
		} catch (DBException | NoSuchFieldException | ClassCastException e)
		{
			logger.error("ERROR GETTING ORDER HEADER DATA for:{}. REASON: {}", orderId, e.getMessage());
		}
		return odm;
	}

	/**
	 * Modify a load with LoadDataModel {@see StandardLoadServer}
	 *
	 * @see OrderLoad#requestLoad()
	 *
	 * @param req
	 * @param resp
	 * @return {@link AjaxResponse}
	 */
	@SuppressWarnings("rawtypes")
	public AjaxResponse retrieve(OrderDataModel orderDataModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			StandardDeviceServer vpDevServer = Factory.create(StandardDeviceServer.class);
			StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
			StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
			StandardMoveServer vpMoveServer = Factory.create(StandardMoveServer.class);
			StandardOrderServer vpOrdServer = Factory.create(StandardOrderServer.class);
			StandardRouteServer vpRouteServer = Factory.create(StandardRouteServer.class);
			StandardStationServer vpStnServer = Factory.create(StandardStationServer.class);

			int vnPriority = orderDataModel.getPriority();

			// Load validation
			String vsLoadID = orderDataModel.getOrderId();
			// - load must exist
			LoadData vpLoadData = vpLoadServer.getLoad1(vsLoadID);
			if (vpLoadData == null)
			{
				throw new IllegalArgumentException("Load ID " + vsLoadID + " not found!");
			}
			// - load cannot be moving
			if (vpLoadData.getLoadMoveStatus() != DBConstants.NOMOVE)
			{
				throw new IllegalArgumentException("Load ID " + vsLoadID + " is active.  Cannot retrieve!");
			}
			// - load cannot be ordered
			OrderLineData oldata = Factory.create(OrderLineData.class);
			oldata.setKey(OrderLineData.LOADID_NAME, vsLoadID);
			if (vpOrdServer.OrderLineExists(oldata))
			{
				throw new IllegalArgumentException(
						"Load ID " + vsLoadID + " has already been ordered.  Cannot retrieve!");
			}
			// - load cannot have picks TODO or can it?
			List<Map> vpMoves = vpMoveServer.getMoveDataList(vsLoadID);
			if (vpMoves.size() > 0)
			{
				throw new IllegalArgumentException(
						"Load ID " + vsLoadID + " has has assigned picks.  Cannot retrieve!");
			}

			// Destination & route validation
			String vsStation = orderDataModel.getDestStation().split(" ")[0].trim();
			// - station must exist
			StationData vpStationData = vpStnServer.getStation(vsStation);
			if (vpStationData == null)
			{
				throw new IllegalArgumentException("Destination Station " + vsStation + " not found!");
			}
			// - active route must exist
			if (vpRouteServer.getFromToRoute(vpLoadData.getWarehouse(), vpLoadData.getAddress(),
					vpStationData.getWarehouse(), vpStationData.getStationName()) == null)
			{
				throw new IllegalArgumentException("Load has no physical route from " + vpLoadData.getWarehouse() + "-"
						+ vpLoadData.getAddress() + " to " + vsStation);
			}
			// - device must not be inoperable
			if (vpDevServer.getOperationalStatus(vpStationData.getStationName()) == DBConstants.INOP)
			{
				throw new IllegalArgumentException("Device for destination Station " + vsStation + " is Inoperable");
			}
			// - origin location must be available.
			int lcstat = vpLocServer.getLocationStatusValue(vpLoadData.getWarehouse(), vpLoadData.getAddress(),
					vpLoadData.getShelfPosition());
			if (lcstat == DBConstants.LCUNAVAIL)
			{
				// Auto-reset location
				vpLocServer.setLocationStatus(vpLoadData.getWarehouse(), vpLoadData.getAddress(),
						vpLoadData.getShelfPosition(), DBConstants.LCAVAIL, true);
			}

			OrderHeaderData oh = vpOrdServer.buildLoadOrder(vsLoadID, vnPriority, vsStation);
			logger.info("Created Order={} for Load={} to Station={}", oh.getOrderID(), vsLoadID, vsStation);
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Ordered load " + vsLoadID);
		}
		catch (IllegalArgumentException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("LoadService (retrieve) Exception: {}", e.getMessage());
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There was an exception: " + e.getMessage());
			logger.error("LoadService (retrieve) Exception", e);
		}

		return ajaxResponse;
	}
	/**
	 * Modify a load with LoadDataModel {@see StandardLoadServer}
	 *
	 * @see OrderLoad#requestLoad()
	 *
	 * @param req
	 * @param resp
	 * @return {@link AjaxResponse}
	 */
	@SuppressWarnings("rawtypes")
	public AjaxResponse retrieveTray(OrderDataModel orderDataModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
			StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
			StandardOrderServer vpOrdServer = Factory.create(StandardOrderServer.class);

			int vnPriority = orderDataModel.getPriority();
			
			// Load validation
			String vsLoadID = orderDataModel.getOrderId();
			// - load must exist
			LoadData vpLoadData = vpLoadServer.getLoad1(vsLoadID);
			if (vpLoadData == null)
			{
				throw new IllegalArgumentException("Load ID " + vsLoadID + " not found!");
			}
			// - load cannot be moving
			if (vpLoadData.getLoadMoveStatus() != DBConstants.NOMOVE)
			{
				throw new IllegalArgumentException("Load ID " + vsLoadID + " is active.  Cannot retrieve!");
			}
			// - load cannot be ordered
			OrderLineData oldata = Factory.create(OrderLineData.class);
			oldata.setKey(OrderLineData.LOADID_NAME, vsLoadID);
			if (vpOrdServer.OrderLineExists(oldata))
			{
				throw new IllegalArgumentException(
						"Load ID " + vsLoadID + " has already been ordered.  Cannot retrieve!");
			}
		
			// - origin location must be available.
			int lcstat = vpLocServer.getLocationStatusValue(vpLoadData.getWarehouse(), vpLoadData.getAddress(),
					vpLoadData.getShelfPosition());
			if (lcstat == DBConstants.LCUNAVAIL)
			{
				// Auto-reset location
				vpLocServer.setLocationStatus(vpLoadData.getWarehouse(), vpLoadData.getAddress(),
						vpLoadData.getShelfPosition(), DBConstants.LCAVAIL, true);
			}

			OrderHeaderData oh = vpOrdServer.buildLoadOrderForThisLoad(vsLoadID, vnPriority);
			logger.info("Created Order={} for Load={} ", oh.getOrderID(), vsLoadID);
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Ordered load " + vsLoadID);
		}
		catch (IllegalArgumentException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("LoadService (retrieve) Exception: {}", e.getMessage());
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There was an exception: " + e.getMessage());
			logger.error("LoadService (retrieve) Exception", e);
		}

		return ajaxResponse;
	}
	
	@SuppressWarnings("rawtypes")
	public AjaxResponse buildOrderForFlight(String  lot)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			EBSOrderServer vpOrdServer = Factory.create(EBSOrderServer.class);			
			short num = vpOrdServer.buildOrderForFlight(lot);
			String msg ="";
			if( num == 0 )
			{
				msg= " Couldn't find any tray to retrieve for flight "+lot;
			}else
			{
				msg= " Retrieving "+ num + " trays for flight "+lot;
			}
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, msg);
			logger.debug("buildOrderForFlight create num:"+ num );
		}
		catch (IllegalArgumentException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("LoadService (build order) Exception: {}", e.getMessage());
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There was an exception: " + e.getMessage());
			logger.error("LoadService (retrieve) Exception", e);
		}
		return ajaxResponse;
	}

	public AjaxResponse buildOrderForThisTray(String trayID)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			EBSOrderServer vpOrdServer = Factory.create(EBSOrderServer.class);			
			short num = vpOrdServer.buildOrderForTray(trayID);
			String msg ="";
			if( num == 0 )
			{
				msg= " Couldn't find any tray to retrieve: "+trayID;
			}else
			{
				msg= " Retrieving "+ num + " tray: "+trayID;
			}
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, msg);
			logger.debug("buildOrderForFlight create num:"+ num );
		}
		catch (IllegalArgumentException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("LoadService (build order) Exception: {}", e.getMessage());
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There was an exception: " + e.getMessage());
			logger.error("LoadService (retrieve) Exception", e);
		}
		return ajaxResponse;
	}
}
