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
package com.daifukuamerica.wrxj.web.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.model.hibernate.EquipmentGraphic;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.EquipmentMonitorModel;
import com.daifukuamerica.wrxj.web.service.dao.EquipmentService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/equipment")
public class EquipmentController
{
	private static final Logger logger = LoggerFactory.getLogger("EQUIPMENTSTATUS");

	private static final String UNDEFINED = "undefined";

	@Autowired
	EquipmentService equipmentService;

	/*======================================================================*/
	/* view - Status														*/
	/*======================================================================*/

	/**
	 * View the Equipment Monitor page
	 * @return String - logical view name of device view
	 */
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "EQUIPMENT MONITOR");
		String[] highlightConditions = {"Status,(ONLINE),highlight-cell-background-success","Status,(ERROR),highlight-cell-background-failure", "Status,(UNKNOWN),highlight-cell-background-warning", "Status,(DISCONNECT),highlight-cell-background-warning"};
		model.addAttribute("regexHighlights",  highlightConditions);
		return UIConstants.VIEW_EQUIPMENT;
	}

	/**
	 * List all status
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/status/all", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public EquipmentMonitorModel pollAllEquipmentStatus(Model model)
	{
		return equipmentService.getEquipmentStatuses();
	}

	/**
	 * List status for one device
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/status/device/{deviceId}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public EquipmentGraphic pollEquipmentStatus(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		return equipmentService.getEquipmentStatus(deviceId);
	}

	/**
	 * Get initial status data
	 * @return
	 */
	@RequestMapping(value="/status/list", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String getEquipmentStatusList()
	{
		TableDataModel tdm = new TableDataModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tdm =  equipmentService.getEquipmentDetailList();
		}
		catch (Exception e)
		{
			logger.error("Error getting equipment status listing", StackTraceFilter.filter(e));
		}
		return new Gson().toJson(tdm);
	}


	/*======================================================================*/
	/* control - Control for System											*/
	/*======================================================================*/

	/**
	 * System Start
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/control/system/on", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse systemOn(Model model)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.turnAllSystemEquipmentOn();
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing system start", StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * System Stop
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/control/system/off", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse systemOff(Model model)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.turnAllSystemEquipmentOff();
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing system stop", StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Silence all alarms
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/control/silenceallalarms", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse silenceAllAlarms(Model model)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.silenceAllAlarms();
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing system alarm silence", StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * System error reset
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/control/resetallerrors", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse resetAllErrors(Model model)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.resetAllErrors();
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing system error reset", StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}


	/*======================================================================*/
	/* control - Control for Individual Equipment							*/
	/*======================================================================*/

	/**
	 * Disconnect by device
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/disconnect/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse disconnectSingleEquipment(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.disconnectSingleEquipment(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing disconnect for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Disconnect by device
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/guide/{deviceId}", method=RequestMethod.GET, produces="text/html; charset=utf-8")
	@ResponseBody
	public String errorGuide(Model model, @PathVariable(value="deviceId") String deviceId, @RequestParam(value="errorCode") String errorCode)
	{
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			return equipmentService.getErrorGuide(deviceId, errorCode);
		}
		catch (Exception e)
		{
			String vsMessage = "Error processing error guide request for error [" + errorCode + "] on device ["
					+ deviceId + "]";
			logger.error(vsMessage, StackTraceFilter.filter(e));
			return vsMessage;
		}
	}

	/**
	 * Request latch clear
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/latchclear/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse latchClearSingleEquipment(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.latchClearSingleEquipment(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing latch clear for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Reconnect by device
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/reconnect/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse reconnectSingleEquipment(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.reconnectSingleEquipment(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing reconnect for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Reset error by device
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/reseterror/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse resetError(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.resetError(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing error reset for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Save logs
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/savelog/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse saveLogSingleEquipment(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.saveDeviceLog(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing save logs for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Send barcode information to a device
	 *
	 * @param model
	 * @param deviceId
	 * @param barcode
	 * @return
	 */
	@RequestMapping(value="/control/sendBarcode/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse sendBarcodeToSingleEquipment(Model model, @PathVariable(value="deviceId") String deviceId, @RequestParam(value="barcode") String barcode)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.sendBarcodeToSingleEquipment(deviceId, barcode);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing send barcode for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Silence one alarm
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/silencealarm/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse silenceEquipmentAlarm(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.silenceEquipmentAlarm(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing silence for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Start a device
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/start/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse startSingleEquipment(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.startSingleEquipment(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing stop for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Stop a device
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/stop/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse stopSingleEquipment(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.stopSingleEquipment(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing stop for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}


	/*======================================================================*/
	/* control - Control for SRC											*/
	/*======================================================================*/

	/**
	 * Turn the SRC online
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/src/on/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse srcOnline(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.srcOnline(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing SRC Online for graphic [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Turn the SRC offline
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/src/off/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse srcOffline(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.srcOffline(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing SRC Offline for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Test MC port
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/testmc/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse testMcComm(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.testMcComm(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing MC comm test for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Test MOS port
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/control/testmos/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse testMosComm(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.testMosComm(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing MOS comm test for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}


	/*======================================================================*/
	/* Tracking																*/
	/*======================================================================*/

	/**
	 * Enable tracking
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/loadtracking/enable/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse enableLoadTrackingSingleEquipment(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.enableLoadTrackingSingleEquipment(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing tracking enable request for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Disable tracking
	 *
	 * @param model
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value="/loadtracking/disable/{deviceId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse disableLoadTrackingSingleEquipment(Model model, @PathVariable(value="deviceId") String deviceId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.disableLoadTrackingSingleEquipment(deviceId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing tracking disable request for [{}]", deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Delete tracking
	 *
	 * @param model
	 * @param trackingId
	 * @return
	 */
	@RequestMapping(value="/loadtracking/delete/{deviceId}/{trackingId}", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse deleteLoadTracking(Model model, @PathVariable(value="deviceId") String deviceId,
			@PathVariable(value="trackingId") String trackingId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = equipmentService.deleteLoadTracking(deviceId, trackingId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
			logger.error("Error processing tracking delete request for [{}] for graphic [{}]", trackingId, deviceId, StackTraceFilter.filter(e));
		}

		if (ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.INFO, "No logic executed");
		return ajaxResponse;
	}

	/**
	 * Tracking Data
	 * @return
	 */
	@RequestMapping(value="/loadtracking/list", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String getEquipmentTracking(@RequestParam(value="deviceId") Optional<String> deviceId)
	{
		String graphicId = null;
		TableDataModel tdm = new TableDataModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			if (deviceId.isPresent() && SKDCUtility.isNotBlank(deviceId.get()) && !deviceId.get().equals(UNDEFINED))
			{
				graphicId = deviceId.get();
			}

			tdm =  equipmentService.getEquipmentTrackingAsTableData(graphicId);
		}
		catch (Exception e)
		{
			logger.error("Error getting equipment status for deviceId=[{}]", graphicId, StackTraceFilter.filter(e));
		}
		return new Gson().toJson(tdm);
	}
}
