/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.service.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStatusServer;
import com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorStatus;
import com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorStatusData;
import com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorTracking;
import com.daifukuamerica.wrxj.dbadapter.data.EquipmentMonitorTrackingData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.model.hibernate.EquipmentGraphic;
import com.daifukuamerica.wrxj.web.model.hibernate.EquipmentTab;
import com.daifukuamerica.wrxj.web.model.hibernate.EquipmentTracking;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.EquipmentMonitorModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;

/**
 * Base Equipment service for interacting and gathering data of the equipment
 *
 *
 * Author: dystout
 * Created : Sep 5, 2018
 *
 */
public class EquipmentService
{
	private static final Logger logger = LoggerFactory.getLogger("EQUIPMENTSTATUS");

	private static final String OFFLINE = StatusEventDataFormat.STATUS_OFFLINE.toUpperCase();
	private static final String STOPPED = StatusEventDataFormat.STATUS_STOPPED.toUpperCase();

//	@Autowired
//	private JMSProducer jmsProducer;


	/*======================================================================*/
	/* Status																*/
	/*======================================================================*/

	/**
	 * Get SPECIFIED Equipment statuses in the form of a large JSON, which is a
	 * description of specified equipment statuses.
	 *
	 * @param deviceId
	 * @return
	 */
	public EquipmentGraphic getEquipmentStatus(String deviceId)
	{
		EquipmentGraphic eg = new EquipmentGraphic();
		SessionFactory factory = HibernateUtils.getSessionFactory();
		Session session = factory.getCurrentSession();
		try
		{
			/**
			 * Query for all graphical status representations
			 */
			if (!session.getTransaction().isActive())
				session.getTransaction().begin();

			String hql = "from EquipmentGraphic e where e.id = '" + deviceId + "'";

			TypedQuery<EquipmentGraphic> graphicsQuery = session.createQuery(hql, EquipmentGraphic.class);
			List<EquipmentGraphic> graphicsResult = graphicsQuery.getResultList();
			if (graphicsResult.size() > 0)
				eg = graphicsResult.get(0);

			session.getTransaction().commit();
		}
		catch (Exception e)
		{
			logger.error("Error getting equipment status for Device ID=[{}]", deviceId, e);
			session.getTransaction().rollback();
		}
		finally
		{
			session.close();
		}
		return eg;
	}

	/**
	 * Get ALL Equipment statuses in the form of a large JSON, which is a list of statuses and tabs
	 * to display.
	 *
	 * @return EquipmentMonitorModel
	 */
	public EquipmentMonitorModel getEquipmentStatuses()
	{

		EquipmentMonitorModel emm = new EquipmentMonitorModel();
		SessionFactory factory = HibernateUtils.getSessionFactory();
		Session session = factory.getCurrentSession();
		try
		{
			/**
			 * Query for all graphical status representations
			 */
			if (!session.getTransaction().isActive())
				session.getTransaction().begin();

			String hql = "from EquipmentGraphic e where 1=1";

			TypedQuery<EquipmentGraphic> graphicsQuery = session.createQuery(hql, EquipmentGraphic.class);
			List<EquipmentGraphic> graphicsResult = graphicsQuery.getResultList();
			ArrayList<EquipmentGraphic> graphics = new ArrayList<EquipmentGraphic>();
			graphics.addAll(graphicsResult);

			session.getTransaction().commit();
			session.close();

//			factory = HibernateUtils.getSessionFactory();
			session = factory.getCurrentSession();
			if (!session.getTransaction().isActive())
				session.getTransaction().begin();

			/**
			 * Query for all tabular area status representations
			 */
			String thql = "from EquipmentTab e where 1=1";

			TypedQuery<EquipmentTab> tabQuery = session.createQuery(thql, EquipmentTab.class);
			List<EquipmentTab> tabsResult = tabQuery.getResultList();
			ArrayList<EquipmentTab> tabs = new ArrayList<EquipmentTab>();
			tabs.addAll(tabsResult);

			session.getTransaction().commit();

			emm.setEquipmentGraphics(graphics);
			emm.setEquipmentTabs(tabs);

		}
		catch (Exception e)
		{
			logger.error("Error getting equipment status", e);
			session.getTransaction().rollback();
		}
		finally
		{
			session.close();
		}
		return emm;
	}

	/**
	 * Equipment details in table format
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel getEquipmentDetailList()
	{
		logger.trace("Status List");

//		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns("EquipmentDetail", true);

		List<Map> tableData = new ArrayList<>();

		SessionFactory factory = HibernateUtils.getSessionFactory();
		Session session = factory.getCurrentSession();
		try
		{
			/**
			 * Query for all graphical status representations
			 */
			if (!session.getTransaction().isActive())
				session.getTransaction().begin();

			String hql = "from EquipmentGraphic e where 1=1";

			TypedQuery<EquipmentGraphic> graphicsQuery = session.createQuery(hql, EquipmentGraphic.class);
			List<EquipmentGraphic> graphicsResult = graphicsQuery.getResultList();

			session.getTransaction().commit();

			for (EquipmentGraphic statusGraphic : graphicsResult)
			{
				// TODO: Figure out a way to build the maps without hard-coding database-configurable headers
				Map<String, String> row = new HashMap<String, String>();
				row.put("Equipment ID", statusGraphic.getId());
				row.put("Status", statusGraphic.getStatusId());
				row.put("Description", statusGraphic.getStatusText());
				row.put("Status Detail", statusGraphic.getStatusText2());
				row.put("Error Code", statusGraphic.getErrorCode());
				row.put("Error Detail", statusGraphic.getErrorText());
				tableData.add(row);
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting equipment status", e);
			session.getTransaction().rollback();
		}
		finally
		{
			session.close();
		}

		return new TableDataModel(tableData);
	}

	/*======================================================================*/
	/* Error Guide															*/
	/*======================================================================*/
	/**
	 * Get the error guide for a given error
	 * @param deviceId
	 * @param errorCode
	 * @return
	 */
	public String getErrorGuide(String deviceId, String errorCode) throws Exception
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		String vsErrorSet = "AS21";
		if (vpData != null)
		{
			vsErrorSet = vpData.getErrorSet();
		}
		return Factory.create(StandardStatusServer.class).getErrorGuidance(vsErrorSet, errorCode);
	}

	/*======================================================================*/
	/* Control for System													*/
	/*======================================================================*/

	/**
	 * Start System
	 *
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse turnAllSystemEquipmentOn() throws DBException
	{
		logger.info("Starting system");

		Set<String> vpMCControllers = new HashSet<>();
		Set<String> vpMOSControllers = new HashSet<>();
		getControllers(vpMCControllers, vpMOSControllers);

		// MC Port
		for (String vsMCController : vpMCControllers)
		{
			logger.info("Starting system -> {}", vsMCController);
			publishControlEvent("" + ControlEventDataFormat.CHAR_START_DEVICE, ControlEventDataFormat.TEXT_MESSAGE,
					vsMCController);
		}

		// MOS Port
		for (String vsMOSController : vpMOSControllers)
		{
			logger.info("Starting system -> {}", vsMOSController);
			publishControlEvent(ControlEventDataFormat.TEXT_MOS_START_EQUIPMENT, ControlEventDataFormat.MOS_START_AISLE,
					vsMOSController);
		}

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Sent system start command");
	}

	/**
	 * Stop System
	 *
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse turnAllSystemEquipmentOff() throws DBException
	{
		logger.info("Stopping system");

		Set<String> vpMCControllers = new HashSet<>();
		Set<String> vpMOSControllers = new HashSet<>();
		getControllers(vpMCControllers, vpMOSControllers);

		// MC Port
		for (String vsMCController : vpMCControllers)
		{
			logger.info("Stopping system -> {}", vsMCController);
			publishControlEvent("" + ControlEventDataFormat.CHAR_STOP_DEVICE, ControlEventDataFormat.TEXT_MESSAGE,
					vsMCController);
		}

		// MOS Port
		for (String vsMOSController : vpMOSControllers)
		{
			logger.info("Stopping system -> {}", vsMOSController);
			publishControlEvent(ControlEventDataFormat.TEXT_MOS_STOP_EQUIPMENT, ControlEventDataFormat.MOS_STOP_AISLE,
					vsMOSController);
		}

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Sent system stop command");
	}

	/**
	 * System error reset
	 *
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse resetAllErrors() throws DBException
	{
		logger.info("Resetting system errors");

		Set<String> vpMCControllers = new HashSet<>();
		Set<String> vpMOSControllers = new HashSet<>();
		getControllers(vpMCControllers, vpMOSControllers);

		// MOS Port
		for (String vsMOSController : vpMOSControllers)
		{
			logger.info("Resetting error -> {}", vsMOSController);
			publishControlEvent(ControlEventDataFormat.TEXT_MOS_RESET_ERROR, ControlEventDataFormat.MOS_RESET_ERROR,
					vsMOSController);
		}

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Sent system error reset command");
	}

	/**
	 * System alarm silence
	 *
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse silenceAllAlarms() throws DBException
	{
		logger.info("Silencing system alarm");

		Set<String> vpMCControllers = new HashSet<>();
		Set<String> vpMOSControllers = new HashSet<>();
		getControllers(vpMCControllers, vpMOSControllers);

		// MOS Port
		for (String vsMOSController : vpMOSControllers)
		{
			logger.info("Silencing alarm -> {}", vsMOSController);
			publishControlEvent(ControlEventDataFormat.TEXT_MOS_SILENCE_ALARM, ControlEventDataFormat.MOS_SILENCE_ERROR,
					vsMOSController);
		}

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Sent system alarm silence command");
	}


	/*======================================================================*/
	/* Control for Individual Equipment										*/
	/*======================================================================*/

	/**
	 * Disconnect
	 *
	 * @param deviceId - Web Graphic ID
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse disconnectSingleEquipment(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.warn("Disconnecting {} @ {}", vpData.getMOSID(), vpData.getMOSController());
		publishControlEvent(ControlEventDataFormat.getMosMachineCommand(vpData.getMOSID()),
				ControlEventDataFormat.MOS_DISCONNECT, vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Sent disconnect for " + vpData.getMOSID());
	}

	/**
	 * Request latch clear
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse latchClearSingleEquipment(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.warn("Requesting latch clear for {} @ {}", vpData.getMOSID(), vpData.getMOSController());
		publishControlEvent(ControlEventDataFormat.getMosMachineCommand(vpData.getMOSID()),
				ControlEventDataFormat.MOS_LATCH_CLEAR, vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested latch clear for " + vpData.getMOSID());
	}

	/**
	 * Reconnect
	 *
	 * @param deviceId - Web Graphic ID
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse reconnectSingleEquipment(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Reconnecting {} @ {}", vpData.getMOSID(), vpData.getMOSController());
		publishControlEvent(ControlEventDataFormat.getMosMachineCommand(vpData.getMOSID()),
				ControlEventDataFormat.MOS_RECOVER_DATA, vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Sent reconnect for " + vpData.getMOSID());
	}

	/**
	 * Reset one
	 * @param deviceId
	 * @return
	 */
	public AjaxResponse resetError(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Requesting error reset for {} @ {}", vpData.getMOSID(), vpData.getMOSController());
		publishControlEvent(ControlEventDataFormat.getMosMachineCommand(vpData.getMOSID()),
				ControlEventDataFormat.MOS_RESET_ERROR, vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested error reset for " + vpData.getMOSID());
	}

	/**
	 * Send barcode info to a device
	 *
	 * @param deviceId
	 * @param barcode
	 * @return
	 */
	public AjaxResponse sendBarcodeToSingleEquipment(String deviceId, String barcode) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.warn("Sending barcode data [{}] to {} @ {}", barcode, vpData.getMOSID(), vpData.getMOSController());
		publishControlEvent(ControlEventDataFormat.getMosBcrDataCommand(vpData.getMOSID(), barcode),
				ControlEventDataFormat.MOS_SEND_BAR_CODE, vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Sent barcode for " + vpData.getMOSID());
	}

	/**
	 * Request that the device save logs
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse saveDeviceLog(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Requesting save logs for {}", vpData.getMCController());
		publishControlEvent(ControlEventDataFormat.TEXT_MOS_SAVE_LOGS, ControlEventDataFormat.MOS_SAVE_ALL_LOGS,
				vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested save logs for " + vpData.getMCController());
	}

	/**
	 * Silence one
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse silenceEquipmentAlarm(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Requesting alarm silence for {} @ {}", vpData.getMOSID(), vpData.getMOSController());
		publishControlEvent(ControlEventDataFormat.getMosMachineCommand(vpData.getMOSID()),
				ControlEventDataFormat.MOS_SILENCE_ERROR, vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested alarm silence for " + vpData.getMOSID());
	}

	/**
	 * Start one
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse startSingleEquipment(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Requesting start for {} @ {}", vpData.getMOSID(), vpData.getMOSController());
		publishControlEvent(ControlEventDataFormat.getMosMachineCommand(vpData.getMOSID()),
				ControlEventDataFormat.MOS_START_EQUIP, vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested start for " + vpData.getMOSID());
	}

	/**
	 * Stop one
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse stopSingleEquipment(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Requesting start for {} @ {}", vpData.getMOSID(), vpData.getMOSController());
		publishControlEvent(ControlEventDataFormat.getMosMachineCommand(vpData.getMOSID()),
				ControlEventDataFormat.MOS_STOP_EQUIP, vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested stop for " + vpData.getMOSID());
	}

	/*======================================================================*/
	/* control - Control for SRC											*/
	/*======================================================================*/

	/**
	 * Start one SRC
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse srcOnline(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Requesting SRC Online for {} @ {}", vpData.getMOSID(), vpData.getMCController());
		publishControlEvent("" + ControlEventDataFormat.CHAR_START_DEVICE,
				ControlEventDataFormat.TEXT_MESSAGE, vpData.getMCController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested SRC Online for " + vpData.getMCController());
	}

	/**
	 * Stop one SRC
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse srcOffline(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Requesting SRC Offline for {} @ {}", vpData.getMOSID(), vpData.getMCController());
		// send simultaneous stop first
        publishControlEvent("" + ControlEventDataFormat.CHAR_STOP_EQUIPMENT,
                ControlEventDataFormat.TEXT_MESSAGE, vpData.getMCController());
		publishControlEvent("" + ControlEventDataFormat.CHAR_STOP_DEVICE,
				ControlEventDataFormat.TEXT_MESSAGE, vpData.getMCController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested SRC Offline for " + vpData.getMCController());
	}

	/**
	 * Test MOS communications
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse testMcComm(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Requesting comm test for {}", vpData.getMCController());
	    publishControlEvent("" + ControlEventDataFormat.CHAR_COMM_TEST,
	            ControlEventDataFormat.TEXT_MESSAGE, vpData.getMCController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested comm test for " + vpData.getMCController());
	}

	/**
	 * Test MOS communications
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse testMosComm(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpData = altToStatusData(deviceId);
		logger.info("Requesting comm test for {}", vpData.getMOSController());
	    publishControlEvent(ControlEventDataFormat.TEXT_MOS_COMM_TEST,
	            ControlEventDataFormat.MOS_COMM_TEST, vpData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested comm test for " + vpData.getMOSController());
	}


	/*======================================================================*/
	/* Tracking																*/
	/*======================================================================*/

	/**
	 * Delete tracking
	 * <br>TODO: This should probably have device ID, too
	 *
	 * @param trackingId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse deleteLoadTracking(String deviceId, String trackingId) throws DBException
	{
		EquipmentMonitorStatusData vpStatusData = altToStatusData(deviceId);
		if (vpStatusData == null)
		{
			return new AjaxResponse(AjaxResponseCodes.FAILURE, "Graphic [" + deviceId + "] not found!");
		}
		if (!vpStatusData.getStatusID().equals(OFFLINE) && !vpStatusData.getStatusID().equals(STOPPED))
		{
			return new AjaxResponse(AjaxResponseCodes.FAILURE, "Device [" + vpStatusData.getMOSID() + "] must be offline to delete tracking!");
		}
		EquipmentMonitorTrackingData vpTrackData = toTrackingData(vpStatusData.getGraphicID(), trackingId);
		if (vpTrackData == null)
		{
			return new AjaxResponse(AjaxResponseCodes.FAILURE, "Tracking for [" + trackingId + "] not found!");
		}

		logger.info("Requesting tracking deletion for [{}] on [{}] from graphic [{}]", trackingId, vpStatusData.getMOSID(), deviceId);

		publishControlEvent(ControlEventDataFormat.getMosTrackingDeleteCommand(vpStatusData.getMOSID(), trackingId),
				ControlEventDataFormat.MOS_DELETE_TRACK, vpStatusData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS, "Requested delete for [" + trackingId + "] on [" + vpStatusData.getMOSID() + "]");
	}

	/**
	 * Turn tracking polling off
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse disableLoadTrackingSingleEquipment(String deviceId) throws DBException
	{
		EquipmentMonitorStatusData vpStatusData = altToStatusData(deviceId);
		publishControlEvent(ControlEventDataFormat.TEXT_MOS_STOP_POLLING, ControlEventDataFormat.MOS_STOP_POLLING,
				vpStatusData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS,
				"Requested tracking off for " + vpStatusData.getMOSController());
	}

	/**
	 * Turn tracking polling on
	 *
	 * @param deviceId
	 * @return
	 * @throws DBException
	 */
	public AjaxResponse enableLoadTrackingSingleEquipment(String deviceId) throws DBException
	{
		// Note: The Swing version turns off tracking polling for the previous device if
		// needed, but we can't really do that here because we don't keep state
		EquipmentMonitorStatusData vpStatusData = altToStatusData(deviceId);
		publishControlEvent(ControlEventDataFormat.TEXT_SHM_ALL_STATUSES, ControlEventDataFormat.SHM_STATUS_REQUEST,
				SKDCConstants.SYSTEM_HEALTH_MONITOR);
		publishControlEvent(ControlEventDataFormat.TEXT_MOS_START_POLLING, ControlEventDataFormat.MOS_START_POLLING,
				vpStatusData.getMOSController());

		return new AjaxResponse(AjaxResponseCodes.SUCCESS,
				"Requested tracking on for " + vpStatusData.getMOSController());
	}

	/**
	 * Get SPECIFIED equipment tracking in the form of a large JSON, which is a list
	 * of statuses and tabs to display.
	 *
	 * @param graphicId
	 * @return EquipmentTrackingModel
	 */
	public List<EquipmentTracking> getEquipmentTracking(String graphicId) throws Exception
	{
		List<EquipmentTracking> trackingResult;

		SessionFactory factory = HibernateUtils.getSessionFactory();
		Session session = factory.getCurrentSession();
		try
		{
			/**
			 * Query for tracking
			 */
			if (!session.getTransaction().isActive())
				session.getTransaction().begin();

			TypedQuery<EquipmentTracking> trackingQuery;
			StringBuilder hql = new StringBuilder("from EquipmentTracking e");
			if (SKDCUtility.isBlank(graphicId))
			{
				trackingQuery = session.createQuery(hql.toString(), EquipmentTracking.class);
			}
			else
			{
				hql.append(" where e.graphicID=:graphicId");
				trackingQuery = session.createQuery(hql.toString(), EquipmentTracking.class)
						               .setParameter("graphicId", graphicId);
			}

			trackingResult = trackingQuery.getResultList();

			session.getTransaction().commit();
		}
		catch (Exception e)
		{
			session.getTransaction().rollback();
			throw e;
		}
		finally
		{
			session.close();
		}
		return trackingResult;
	}

	/**
	 * Equipment tracking in table format
	 * @param graphicId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel getEquipmentTrackingAsTableData(String graphicId) throws Exception
	{
		logger.trace("Tracking List [{}]", graphicId);

		List<Map> tableData = new ArrayList<>();
		List<EquipmentTracking> graphicsResult = getEquipmentTracking(graphicId);
		for (EquipmentTracking statusGraphic : graphicsResult)
		{
			// TODO: Figure out a way to build the maps without hard-coding database-configurable headers
			Map<String, String> row = new HashMap<String, String>();
			row.put("ID", statusGraphic.getId());
			row.put("Graphic ID", statusGraphic.getGraphicID());
			row.put("Device ID", statusGraphic.getDeviceID());
			row.put("Tracking ID", statusGraphic.getTrackingID());
			row.put("Status", statusGraphic.getStatus());
			row.put("Origin", statusGraphic.getOrigin());
			row.put("Destination", statusGraphic.getDestination());
			row.put("Size", statusGraphic.getSize());
			tableData.add(row);
		}
		return new TableDataModel(tableData);
	}

	/*======================================================================*/
	/* Utility																*/
	/*======================================================================*/

	/**
	 * Convert the web alt graphic ID to an equipment status
	 *
	 * @param isAltGraphicId
	 * @return
	 * @throws DBException
	 * @throws NoSuchElementException - if no record found
	 */
	private EquipmentMonitorStatusData altToStatusData(String isAltGraphicId) throws DBException, NoSuchElementException
	{
		EquipmentMonitorStatusData vpKey = Factory.create(EquipmentMonitorStatusData.class);
		vpKey.setKey(EquipmentMonitorStatusData.ALTGRAPHICID_NAME, isAltGraphicId);

		EquipmentMonitorStatus vpHandler = Factory.create(EquipmentMonitorStatus.class);
		EquipmentMonitorStatusData vpData = vpHandler.getElement(vpKey, DBConstants.NOWRITELOCK);
		if (vpData == null)
		{
			throw new NoSuchElementException("No matching data found for [" + isAltGraphicId + "]");
		}
		return vpData;
	}

//	/**
//	 * Convert the web alt graphic ID to an equipment status
//	 *
//	 * @param isGraphicId
//	 * @return
//	 * @throws DBException
//	 * @throws NoSuchElementException - if no record found
//	 */
//	private EquipmentMonitorStatusData toStatusData(String isGraphicId) throws DBException, NoSuchElementException
//	{
//		EquipmentMonitorStatusData vpKey = Factory.create(EquipmentMonitorStatusData.class);
//		vpKey.setKey(EquipmentMonitorStatusData.GRAPHICID_NAME, isGraphicId);
//
//		EquipmentMonitorStatus vpHandler = Factory.create(EquipmentMonitorStatus.class);
//		EquipmentMonitorStatusData vpData = vpHandler.getElement(vpKey, DBConstants.NOWRITELOCK);
//		if (vpData == null)
//		{
//			throw new NoSuchElementException("No matching data found for [" + isGraphicId + "]");
//		}
//		return vpData;
//	}

	/**
	 * Convert the tracking ID to tracking data
	 *
	 * @param isDeviceId
	 * @param isTrackingId
	 * @return
	 * @throws DBException
	 * @throws NoSuchElementException - if no record found
	 */
	private EquipmentMonitorTrackingData toTrackingData(String isDeviceId, String isTrackingId)
			throws DBException, NoSuchElementException
	{
		EquipmentMonitorTrackingData vpKey = Factory.create(EquipmentMonitorTrackingData.class);
		vpKey.setKey(EquipmentMonitorTrackingData.GRAPHICID_NAME, isDeviceId);
		vpKey.setKey(EquipmentMonitorTrackingData.TRACKINGID_NAME, isTrackingId);

		EquipmentMonitorTracking vpTrackHandler = Factory.create(EquipmentMonitorTracking.class);
		EquipmentMonitorTrackingData vpData = vpTrackHandler.getElement(vpKey, DBConstants.NOWRITELOCK);
		if (vpData == null)
		{
			throw new NoSuchElementException("No matching data found for [" + isTrackingId + "]");
		}
		return vpData;
	}

	/**
	 * Get all of the controllers in the system
	 * @param isMCControllers
	 * @param isMOSControllers
	 * @throws DBException
	 */
	private void getControllers(Set<String> ipMCControllers, Set<String> ipMOSControllers) throws DBException
	{
		if (ipMCControllers == null || ipMOSControllers == null)
		{
			throw new IllegalArgumentException("Input sets must not be null");
		}

		EquipmentMonitorStatusData vpKey = Factory.create(EquipmentMonitorStatusData.class);
		vpKey.addOrderByColumn(EquipmentMonitorStatusData.MCCONTROLLER_NAME);
		vpKey.addOrderByColumn(EquipmentMonitorStatusData.MOSCONTROLLER_NAME);

		EquipmentMonitorStatus vpHandler = Factory.create(EquipmentMonitorStatus.class);
		for (EquipmentMonitorStatusData vpData : vpHandler.getAllElementsAsData(vpKey))
		{
			if (SKDCUtility.isNotBlank(vpData.getMOSController()))
			{
				ipMOSControllers.add(vpData.getMOSController());
			}
			else if (SKDCUtility.isNotBlank(vpData.getMCController()))
			{
				ipMOSControllers.add(vpData.getMCController());
			}
		}
	}

	/**
	 * Send a control event
	 *
	 * @param sEvent
	 * @param iEvent
	 * @param sCKN
	 */
	private void publishControlEvent(String sEvent, int iEvent, String sCKN)
	{
		// WRx SystemGateway style
		SystemGateway sg = null;
		try
		{
			sg = SystemGateway.create(com.daifukuamerica.wrxj.log.Logger.getLogger());
			sg.publishControlEvent(sEvent, iEvent, sCKN);
		}
		catch (Throwable e)
		{
			logger.error("Error sending message", e);
		}
		finally
		{
			if (sg != null)
				SystemGateway.destroy(sg);
		}

		// Spring JMS Producer style
//		try
//		{
//			jmsProducer.publishArtemisEvent(
//					null,
//					null,
//					sEvent,
//					iEvent,
//					MessageEventConsts.CONTROL_EVENT_TYPE,
//					MessageEventConsts.CONTROL_EVENT_TYPE_TEXT + sCKN,
//					null);
//		}
//		catch (Throwable e)
//		{
//			logger.error("Error sending message", e);
//		}
	}
}
