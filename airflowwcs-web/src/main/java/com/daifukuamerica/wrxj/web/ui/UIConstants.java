package com.daifukuamerica.wrxj.web.ui;

/**
 * @author dystout
 * Date: Sep 24, 2016
 *
 * Description: UI Constants for convenience where jstl or el is not possible.
 *
 * TODO - refactor errors to seperate constants and remove html formatting (should be rendered on view side)
 */
public interface UIConstants
{

	/*****************************
	 * WRxJ Error message wrappers
	 *****************************/

	final String W_FONT = "</font>";

	/********* ERRORS **********/

	final String W_ERR_MSG = "<font class=\"errorHelpText\">";

	/******** SUCCESS *********/

	final String W_SUCCEED_MSG = "<font class=\"successHelpText\">";


	/**********************
	 * User UI Components
	 **********************/

	/********* ERRORS ********/

	final String DB_EXCEPTION = "<font class=\"errorHelpText\">Database Exception. See logs.</font>";
	final String DB_CONNECTION = "<font class=\"errorHelpText\">Connection Error</font>";
	final String SESSION_INVALID = "<center><font class=\"errorHelpText\">Session invalid, please login.</font></center>";
	final String LOGIN_INVALID = "<center><font class=\"errorHelpText\">Login invalid, please try again.</font></center>";
	final String LOGIN_EXPIRED = "<center><font class=\"errorHelpText\">Login has Expired</font></center>";
	final String LOGIN_IN_USE = "<center><font class=\"errorHelpText\">Login in use - Logout from terminal</font></center>";

	/*********** VIEW NAMES ******/
	final String VIEW_ALERT = "wrxj/alert/alert";
	final String VIEW_CONTAINER = "wrxj/container/container";
	final String VIEW_DEVICE = "wrxj/device/device";
	final String VIEW_DISPATCHID = "wrxj/dispatchid/dispatchid";
	final String VIEW_EQUIPMENT = "wrxj/equipment/equipment";
	final String VIEW_EQUIP_LOGVIEW = "wrxj/equiplogview/equiplogview";
	final String VIEW_HOST_LOGVIEW = "wrxj/hostlogview/hostlogview";
	final String VIEW_FLUSH = "wrxj/flush/flush";
	final String VIEW_ITEM = "wrxj/item/item";
	final String VIEW_ITEMDETAILS = "wrxj/itemdetails/itemDetails";
	final String VIEW_ITEMDETAILS_BYLOAD = "wrxj/itemdetails/itemDetailsByLoad";
	final String VIEW_ITEMDETAILS_BYLOADLINEITEM = "wrxj/itemdetails/itemDetailsByLoadLineItem";	
	final String VIEW_LOAD = "wrxj/load/load";
	final String VIEW_LOADTRANSACTIONHISTORY = "wrxj/history/loadtransactionhistory";
	final String VIEW_LOCATION = "wrxj/location/location";
	final String VIEW_LOGIN = "login";
	final String VIEW_LOGVIEW = "wrxj/logview/logview";
	final String VIEW_MESSAGE = "wrxj/message/message";
	final String VIEW_MOVE = "wrxj/move/move";
	final String VIEW_ORDER = "wrxj/ordermaintenance/order";
	final String VIEW_PICK = "wrxj/pick/pick";
	final String VIEW_PLAYGROUND = "wrxj/playground/playground";
	final String VIEW_PORT = "wrxj/port/port";
	final String VIEW_PURCHASEORDER = "wrxj/expected/expectedReceipts";
	final String VIEW_RECOVERY = "wrxj/recovery/recovery";
	final String VIEW_RECOVERY2 = "wrxj/recovery2/recovery";
	final String VIEW_REPORTS_LOADCOUNTS = "wrxj/reportloadcounts/loadcounts";
	final String VIEW_WRXROLE = "wrxj/wrxrole/wrxrole";
	final String VIEW_ROUTE = "wrxj/route/route";
	final String VIEW_ROLE = "wrxj/role/role";
	final String VIEW_STORE = "wrxj/store/store";
	final String VIEW_SYSCONFIG = "wrxj/sysconfig/sysconfig";
	final String VIEW_TRANSACTIONHISTORY = "wrxj/history/history";
	final String VIEW_USER_PERMISSION = "wrxj/userpermission/userpermission";
	final String VIEW_USERS = "wrxj/usermanagement/userManagement";
	final String VIEW_USER_PREFERENCES = "wrxj/userpreference/userPreference";
	final String VIEW_USER_SESSION = "wrxj/usersession/usersession";
	final String VIEW_WEB_MANAGEMENT = "wrxj/webmanagement/webmanagement";
	final String VIEW_WAREHOUSE = "wrxj/warehouse/warehouse";
	final String VIEW_WELCOME = "wrxj/main";
	final String VIEW_ZONE_DEFINITION = "wrxj/zone/zone";
	final String VIEW_ZONE_GROUP = "wrxj/zone/zoneGroup";
    final String VIEW_SUPPORT = "wrxj/help/support";
    final String VIEW_FLIGHT = "wrxj/flight/flight";
    final String VIEW_FLIGHT_DETAILS = "wrxj/flight/flightDetails";
    final String VIEW_TIMESLOTS_CONFIG = "wrxj/timeslot/timeslot";
    final String VIEW_WORK = "wrxj/workmaintenance/work";
    final String VIEW_EQUIPMENTS = "wrxj/equipments/equipment";
    final String VIEW_OCCUPANCY = "wrxj/occupancy/occupancy";
    
	/***** TEMPLATE VIEW NAMES *****/
	final String VIEW_TEMPLATE_USER_INFO = "classpath:templates/userinfo.html";

	/** UI THEME CONSTANTS */

	final String UI_THEME_DEFAULT = "default";
	final String UI_THEME_LIGHT = "light";
	final String UI_THEME_DARK = "dark";

}
