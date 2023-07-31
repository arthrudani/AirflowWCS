/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
//import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.Role;
import com.daifukuamerica.wrxj.dbadapter.data.RoleData;
import com.daifukuamerica.wrxj.dbadapter.data.Route;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroup;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroupData;
//import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroup;
//import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroupData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;

/**
 * Use Wrxj server classes/db interfaces to gather selectable dropdown
 * list options for form fields in the view.
 *
 *  TODO - Break out different lists of items into their own methods to
 *  prevent replicated code. im lookin at you init load dropdown bs
 *
 * Author: dystout
 * Created : May 9, 2017
 *
 */
public class UIService
{
	//TODO - separate IKEA specific methods from baseline

	/**
	* Log4j logger: UIService
	*/
	protected static final Logger logger = LoggerFactory.getLogger("UIService");

	/**
	 * Get dropdowns for device forms.  No database access.
	 * 
	 * @param context
	 * @return
	 */
    public Map<String, Object> initDeviceDropDownOptions()
    {
      Map<Integer,String> deviceTypes = null;
      Map<Integer,String> opStatuses = null;
      try
      {
          deviceTypes = getTransValues(DeviceData.DEVICETYPE_NAME);
          opStatuses = getTransValues(DeviceData.OPERATIONALSTATUS_NAME);
      }
      catch (Exception e)
      {
          logger.error("ERROR building drop down options for DEVICE forms", StackTraceFilter.filter(e));
      }

      Map<String, Object> dropdowns = new HashMap<>();
      dropdowns.put("deviceTypes", deviceTypes);
      dropdowns.put("operationalStatuses", opStatuses);
      return dropdowns;
    }
	
	/**
	 * Initialize Load page drop downs
	 * @return
	 */
	public Map<String, Object[]> initLoadDropDownOptions(ServletContext context){
		StandardInventoryServer vpInvServer  = Factory.create(StandardInventoryServer.class, DBConstantsWeb.DB_NAME);
		StandardLocationServer vpLocServer   = Factory.create(StandardLocationServer.class, DBConstantsWeb.DB_NAME);
		StandardRouteServer vpRouteServer    = Factory.create(StandardRouteServer.class, DBConstantsWeb.DB_NAME);
		ZoneGroup vpZoneHandler = Factory.create(ZoneGroup.class);

		List<String> routeList = null;
		String[] deviceList =	null;
		List<String> containerTypes =	null;
		Integer[] heights = {0,1,2,3};
		String[] warehouses =	null;
		String[] moveStatuses = null;
		String[] amountFull = null;
		String[] lpCheck = null;
		String[] zones = null;
		String[] destinations = null;

		try
		{
			routeList     =   vpRouteServer.getRouteNameList("");
			deviceList    =   vpLocServer.getDeviceIDList(false);
			containerTypes =	  vpInvServer.getContainerTypeList();
			heights    =   vpLocServer.getLocationHeights();
			warehouses = vpLocServer.getWarehouseChoices(false);
			lpCheck = DBTrans.getStringList(LoadData.LOADPRESENCECHECK_NAME);
			moveStatuses = DBTrans.getStringList(LoadData.LOADMOVESTATUS_NAME);
			amountFull = DBTrans.getStringList(LoadData.AMOUNTFULL_NAME);
			zones = vpZoneHandler.getDistinctColumnValues(ZoneGroupData.ZONEGROUP_NAME, SKDCConstants.NO_PREPENDER);
			// TODO: Limit based upon load aisle group (need ajax query) and route
			destinations = getLoadOrderDestinations();
		}
		catch (DBException | NoSuchFieldException e)
		{
			logger.error("ERROR building drop down options for LOAD forms", StackTraceFilter.filter(e));
		}

		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();

		dropdowns.put("routeList",routeList.toArray());
		dropdowns.put("deviceList", deviceList);
		dropdowns.put("containerTypes", containerTypes.toArray());
		Arrays.sort(heights);
		dropdowns.put("heights", Arrays.toString(heights).split("[\\[\\]]")[1].split(", "));
		dropdowns.put("warehouses", warehouses);
		dropdowns.put("lpCheck", lpCheck);
		dropdowns.put("moveStatuses", moveStatuses);
		dropdowns.put("amountFull", amountFull);
		dropdowns.put("zoneList", zones);
		dropdowns.put("destinations", destinations);

		return dropdowns;
	}

	/**
	 * Initialize Flush screen page drop downs
	 * @return
	 */
	public Map<String, Object[]> initFlushDropDownOptions(ServletContext context){

		StandardLocationServer vpLocServer   = Factory.create(StandardLocationServer.class, DBConstantsWeb.DB_NAME);

		String[] srcdeviceList =	null;
		String[] srcdeviceListTemp =	null;
		try
		{
			srcdeviceListTemp    =   vpLocServer.getDeviceIDList(false);
			if(srcdeviceListTemp != null && srcdeviceListTemp.length > 0  )
			{
				srcdeviceList = new String[ srcdeviceListTemp.length + 1 ];
				System.arraycopy(srcdeviceListTemp, 0, srcdeviceList, 0,  srcdeviceListTemp.length);
				srcdeviceList[srcdeviceList.length - 1] = "ALL";//Add the last item
				Arrays.sort(srcdeviceList);
			}

		}
		catch (DBException e)
		{
			logger.error("ERROR building drop down options for FLUSH forms", StackTraceFilter.filter(e));
		}

		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		dropdowns.put("srcaisles", srcdeviceList);
	

		return dropdowns;
	}
	
	/**
	 * Initialize Location page drop downs
	 * @return
	 */
	public Map<String, Object> initLocationDropDownOptions(ServletContext context)
	{
		StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
		StandardDeviceServer vpDevServer = Factory.create(StandardDeviceServer.class);

		String[] warehouses =	null;
		String[] devices = null;
		String[] commdevices = null;
		String[] zones = null;
		Map<Integer,String> locStatuses = null;
		Map<Integer,String> locEmptyFlags = null;
		Map<Integer,String> locType = null;

		try
		{
			warehouses = vpLocServer.getWarehouseChoices(true);
			devices = vpLocServer.getDeviceIDList(true);
			commdevices = vpDevServer.getPortDevicesByDeviceNameForWeb(true);
			zones = vpLocServer.getZoneChoiceList(SKDCConstants.ALL_STRING).toArray(new String[0]);
			locStatuses = getTransValues(LocationData.LOCATIONSTATUS_NAME, new int[]
					{ DBConstants.LCAVAIL, DBConstants.LCPROHIBIT, DBConstants.LCUNAVAIL });
			locEmptyFlags = getTransValues(LocationData.EMPTYFLAG_NAME, new int[]
					{ DBConstants.UNOCCUPIED, DBConstants.OCCUPIED, DBConstants.LCRESERVED, DBConstants.LC_DDMOVE, DBConstants.LC_SWAP });
			locType = getTransValues(LocationData.LOCATIONTYPE_NAME, new int[]
					{ DBConstants.LCASRS, DBConstants.LCCONSOLIDATION, DBConstants.LCCONVSTORAGE, DBConstants.LCDEDICATED, 
							DBConstants.LCDEVICE, DBConstants.LCFLOW, DBConstants.LCRECEIVING, DBConstants.LCSHIPPING, DBConstants.LCSTAGING, DBConstants.LCSTATION});
		}
		catch (DBException | NoSuchFieldException e)
		{
			logger.error("ERROR building drop down options for LOCATION forms", StackTraceFilter.filter(e));
		}

		Map<String, Object> dropdowns = new HashMap<>();
		dropdowns.put("warehouses", warehouses);
		dropdowns.put("devices", devices);
		dropdowns.put("commdevices", commdevices);
		dropdowns.put("zones", zones);
		dropdowns.put("locStatuses", locStatuses);
		dropdowns.put("locEmptyFlags", locEmptyFlags);
		dropdowns.put("locType", locType);
		return dropdowns;
	}

	/**
	 * Returns a list of all translations values
	 * @param isField
	 * @return
	 * @throws NoSuchFieldException
	 */
	protected Map<Integer,String> getTransValues(String isField) throws NoSuchFieldException
	{
		return getTransValues(isField, null);
	}

	/**
	 * Returns a list of translations values
	 * @param isField
	 * @return
	 * @throws NoSuchFieldException
	 */
	protected Map<Integer,String> getTransValues(String isField, int[] ianKeys) throws NoSuchFieldException
	{
		Map<Integer,String> selectValues = new LinkedHashMap<>();
		int[] vanKeys = ianKeys;
		if (vanKeys == null)
		{
			vanKeys = DBTrans.getIntegerList(isField);
		}
		String[] vasValues = DBTrans.getStringList(isField, vanKeys);
		for (int i = 0; i < vanKeys.length; i++)
		{
			selectValues.put(vanKeys[i], vasValues[i]);
		}
		return selectValues;
	}

	/**
	 * Get possible order destinations.
	 *
	 * TODO: Move to StandardRouteServer or StandardStationServer for extensibility
	 *
	 * @return
	 * @throws DBException
	 */
	private String[] getLoadOrderDestinations() throws DBException
	{
		RouteData vpRouteKey = Factory.create(RouteData.class);
		vpRouteKey.setKey(RouteData.FROMTYPE_NAME, DBConstants.EQUIPMENT);
		vpRouteKey.setKey(RouteData.DESTTYPE_NAME, DBConstants.STATION);

		Route vpRouteHandler = Factory.create(Route.class);
		String[] vasDestinations = vpRouteHandler.getSingleColumnValues(RouteData.DESTID_NAME, true, vpRouteKey, SKDCConstants.NO_PREPENDER);

		StationData vpStnKey = Factory.create(StationData.class);
		vpStnKey.setInKey(StationData.STATIONNAME_NAME, KeyObject.AND, (Object[])vasDestinations);

		Station vpStnHandler = Factory.create(Station.class);
		List<StationData> vpStations = vpStnHandler.getAllElementsAsData(vpStnKey);
		List<String> vpDropDowns = new ArrayList<>();
		for (StationData vpData : vpStations)
		{
			vpDropDowns.add(vpData.getStationName() + " - " + vpData.getDescription());
		}
		return vpDropDowns.toArray(new String[0]);
	}

	/**
	 * Initialize Order page drop downs
	 * @return
	 */
	public Map<String, Object[]> initOrderDropDownOptions(ServletContext context){
		String[] orderTypes = null;
		String[] orderStatus = null;

		try
		{
			orderTypes = DBTrans.getStringList(OrderHeaderData.ORDERTYPE_NAME);
			orderStatus = DBTrans.getStringList(OrderHeaderData.ORDERSTATUS_NAME);
		}
		catch ( NoSuchFieldException e)
		{
			logger.error("ERROR building drop down options for ORDERMAINT forms", StackTraceFilter.filter(e));
		}

		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();

		dropdowns.put("orderTypes", orderTypes);
		dropdowns.put("orderStatus", orderStatus);
		return dropdowns;
	}

	/**
	 *
	 * @return
	 */
	public Map<String, Object[]> initLoadContextMenus()
	{
		return null; // TODO implement
	}

	/**
	 * Initialize Item Detail page drop downs
	 * @return
	 */
	public Map<String, Object[]> initItemDetailsDropDownOptions()
	{
		Map<String,Object[]> dropdowns  = new HashMap<String, Object[]>();
		dropdowns.put("itemDetailHoldType", getItemHoldTypesOptions());
		return dropdowns;
	}

	/**
	 * Initialize Knapp Message page drop downs
	 * TODO: Is this IKEA specific?
	 * @return
	 */
	public Map<String, Object[]> initKnappMessageDropDownOptions()
	{
		Map<String,Object[]> dropdowns  = new HashMap<String, Object[]>();
		dropdowns.put("messageTypes", getKnappMessageTypes());
		return dropdowns;
	}

	/**
	 * Initialize Pick page drop downs
	 * @return
	 */
	public Map<String, Object[]> initPickDropDownOptions()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		dropdowns.put("stations", getStationOptions());
		dropdowns.put("amountsFull", getAmountFullOptions());
		return dropdowns;
	}

	/**
	 * Initialize Message page drop downs
	 * TODO: Is this IKEA specific?
	 * @return
	 */
	public Map<String, Object[]> initMessageDropDownOptions()
	{
		Map<String,Object[]> dropdowns = new HashMap<String, Object[]>();
		dropdowns.put("stations", getStationOptions());
		return dropdowns;
	}

	/**
	 * Initialize User page drop downs
	 * @return
	 */
	public Map<String, Object[]> initUserDropDownOptions()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		dropdowns.put("roles", getUserRoles());
		dropdowns.put("containerTypes", getContainerTypeOptions());
		return dropdowns;
	}

	/**
	 * Initialize Store page drop downs
	 * @return
	 */
	public Map<String, Object[]> initStoreDropDownOptions()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		dropdowns.put("stations", getStationOptions(
				DBConstants.USHAPE_OUT, DBConstants.PDSTAND, DBConstants.REVERSIBLE, DBConstants.INPUT));
		dropdowns.put("containerTypes",getContainerTypeOptions());
		dropdowns.put("amountsFull", getAmountFullOptions());
		return dropdowns;
	}

	/**
	 * Initialize User Preferences page drop downs
	 * @return
	 */
	public Map<String,Object[]> initUserPreferencesDropDownOptions()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		dropdowns.put("themes", getThemeDropdownOptions());
		dropdowns.put("messageOptions", getCheckWeightOptions());
		return dropdowns;
	}

	/**
	 * Get Item Master options
	 * @return
	 */
	public Map<String, Object[]> initItemMasterDropDownOptions()
	{
		// TODO: implement.  This would probably be better served with an auto-completer.
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		return dropdowns;
	}

	/**
	 * Get User Role options
	 * @return
	 */
	private Object[] getUserRoles()
	{
		String[] userRoles = new String[] { "Administrator", "Operator", "ReadOnly" };
		try
		{
	        Role vpRole = Factory.create(Role.class);
	        RoleData vpRoleData = Factory.create(RoleData.class);
	        vpRoleData.setKey(RoleData.ROLE_NAME, "Master", KeyObject.NOT_EQUAL);
	        vpRoleData.setKey(RoleData.ROLE_NAME, "SKDaifuku", KeyObject.NOT_EQUAL);
	        userRoles = vpRole.getSingleColumnValues(RoleData.ROLE_NAME, true, vpRoleData, SKDCConstants.NO_PREPENDER);
		}
		catch (Exception e)
		{
			logger.error("ERROR getting User Role options for drop downs", StackTraceFilter.filter(e));
		}
		return userRoles;
	}

	/**
	 * Get Amount Full options
	 * @return
	 */
	private Object[] getAmountFullOptions()
	{
		String[] amountFullOptions = new String[0];
		try
		{
			amountFullOptions = DBTrans.getStringList(LoadData.AMOUNTFULL_NAME);
		}
		catch (NoSuchFieldException e)
		{
			logger.error("ERROR getting Amount Full options for drop downs", StackTraceFilter.filter(e));
		}
		return amountFullOptions;
	}

	/**
	 * Get Container Type options
	 * @return
	 */
	public String[] getContainerTypeOptions()
	{
		try
		{
			StandardInventoryServer vpInvServer = Factory.create(StandardInventoryServer.class);
			return vpInvServer.getContainerTypeList().toArray(new String[0]);
		} catch (DBException e)
		{
			logger.error("ERROR getting Container Type options for drop downs", StackTraceFilter.filter(e));
		}
		return new String[0];
	}

	/**
	 * Get Station options
	 * @param ianTypes - Types of stations to return
	 * @return
	 */
	public String[] getStationOptions(int... ianTypes)
	{
		StandardStationServer stationServer = Factory.create(StandardStationServer.class);
		try
		{
			Map<String, String> vpStationOptions = stationServer.getStationsByStationType(ianTypes);
			// Use vpStationOptions.keySet() to get stations + descriptions
			String[] vasStations = vpStationOptions.values().toArray(new String[0]);
			Arrays.sort(vasStations);
			return vasStations;
		}
		catch(DBException e)
		{
			logger.error("ERROR getting Station options for drop downs", StackTraceFilter.filter(e));
		}

		return null;
	}

	/**
	 * Get Item Hold Type options
	 * @return
	 */
	public Object[] getItemHoldTypesOptions()
	{
		int[] vanItemHoldTypes = {DBConstants.ITMAVAIL, DBConstants.ITMHOLD,
	              DBConstants.QCHOLD, DBConstants.SHIPHOLD };

	    String[] vasItemHoldType = new String[vanItemHoldTypes.length];
	    try
	    {
	      for(int i = 0; i < vanItemHoldTypes.length; i++)
	    	vasItemHoldType[i] = DBTrans.getStringValue(LoadLineItemData.HOLDTYPE_NAME, vanItemHoldTypes[i]);
	    }
	    catch(NoSuchFieldException nse)
	    {
			logger.error("ERROR getting Item Hold options for drop downs", StackTraceFilter.filter(nse));
	    }
		return vasItemHoldType;

	}
	
	public Object[] getWorkStatusOptions()
	{
		int[] workStatus = {DBConstants.CMD_READY, DBConstants.CMD_UNKNOWN, DBConstants.CMD_PROCCESSING,
				 DBConstants.CMD_COMMANDED, DBConstants.CMD_COMPLETED, DBConstants.CMD_DELETED, DBConstants.CMD_ERROR };

	    String[] vasworkStatus = new String[workStatus.length];
	    try
	    {
	      for(int i = 0; i < workStatus.length; i++)
	    	  vasworkStatus[i] = DBTrans.getStringValue(MoveCommandData.STATUS_NAME, workStatus[i]);
	    }
	    catch(NoSuchFieldException nse)
	    {
			logger.error("ERROR getting Item Hold options for drop downs", StackTraceFilter.filter(nse));
	    }
		return vasworkStatus;

	}
	public Object[] getWorkOrderTypeOptions()
	{
		int[] workOrderType = {DBConstants.CMD_STORAGE, DBConstants.CMD_RETRIEVAL, DBConstants.CMD_RACK};

	    String[] vasOrderType = new String[workOrderType.length];
	    try
	    {
	      for(int i = 0; i < workOrderType.length; i++)
	    	  vasOrderType[i] = DBTrans.getStringValue(MoveCommandData.ORDERTYPE_NAME, workOrderType[i]);
	    }
	    catch(NoSuchFieldException nse)
	    {
			logger.error("ERROR getting Item Hold options for drop downs", StackTraceFilter.filter(nse));
	    }
		return vasOrderType;

	}
	
	public Object[] getWorkMoveTypeOptions()
	{
		int[] workMoveType = {DBConstants.CMD_DIRECT, DBConstants.CMD_DIRECT_LOC, DBConstants.CMD_STOREAGE_LOC,DBConstants.CMD_LOC_RETRIEVAL};

	    String[] vasMoveType = new String[workMoveType.length];
	    try
	    {
	      for(int i = 0; i < workMoveType.length; i++)
	    	  vasMoveType[i] = DBTrans.getStringValue(MoveCommandData.MOVETYPE_NAME, workMoveType[i]);
	    }
	    catch(NoSuchFieldException nse)
	    {
			logger.error("ERROR getting Item Hold options for drop downs", StackTraceFilter.filter(nse));
	    }
		return vasMoveType;

	}
	/**
	 * Get Knapp Message Type options
	 * TODO: Is this IKEA specific?
	 * @return
	 */
	public Object[] getKnappMessageTypes()
	{
		String[] messagetypes = new String[2];

		messagetypes[0] = "Store Request";
		messagetypes[1] = "Retrieval Request";

		return messagetypes;
	}

	/**
	 * Get Check Weight options
	 * TODO: Is this IKEA specific?s
	 * @return
	 */
	private Object[] getCheckWeightOptions()
	{
		return new String[] { "YES", "NO" };
	}

	/**
	 * Get Theme options
	 * @return
	 */
	private Object[] getThemeDropdownOptions()
	{
		// blue does not work for equipment monitor
		return new String[] { "dark", /*"blue",*/ "light", "default" };
	}

	/**
	 * Get Device options
	 * @return
	 */
	public Object[] getDeviceOptions()
	{
		// TODO: Implement
		return null;
	}
		
	/**
	 * Initialize Pick page drop downs
	 * @return
	 */
	public Map<String, Object> initSchemaDropDownOptions()
	{	
		 final EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		 Map<Integer, String> vpSchmaOptions = null;

	      try
	      {
	    	  vpSchmaOptions = vpTJLoadHandler.getAllSchemaIDList();
	      }
	      catch (Exception e)
	      {
	          logger.error("ERROR building drop down options for DEVICE forms", StackTraceFilter.filter(e));
	      }

	      Map<String, Object> dropdowns = new HashMap<>();
	      dropdowns.put("schemas", vpSchmaOptions);
	      return dropdowns;	      
	}
	
	public Map<String, Object[]> initWorkDropDownOptions(ServletContext context) throws NoSuchFieldException{
		StandardLocationServer vpLocServer   = Factory.create(StandardLocationServer.class, DBConstantsWeb.DB_NAME);
		String[] deviceList =	null;
		try
		{
			deviceList    =   vpLocServer.getDeviceIDList(false);
			
		}
		catch (DBException e)
		{
			logger.error("ERROR building drop down options for WORKMAINT forms", StackTraceFilter.filter(e));
		}

		Map<String, Object[]> dropdowns = new HashMap<>();
		dropdowns.put("deviceList", deviceList);
		dropdowns.put("status", getWorkStatusOptions());
		dropdowns.put("orderType", getWorkOrderTypeOptions());
		dropdowns.put("moveType", getWorkMoveTypeOptions());
		return dropdowns;
	}

}
