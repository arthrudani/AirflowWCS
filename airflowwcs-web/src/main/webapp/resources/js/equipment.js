/***************************************
 * 	  START Configurable properties
 ***************************************/

//map resource URL
//var mapUrl = ctx + "/resources/layout/map-ebs.svg";
var mapUrl = ctx + "/resources/layout/map-keneccot.svg";

//refresh rate of equipment monitor screen (in ms)
var equipmentRefreshRate = 4000;


//width and height of the svg zoom area FOR KENECOTT
 var width = 1600;
 var height = 800;

//width and height of the svg zoom area FOR BALDOR
//var width = 1400;
//var height = 800;

//default auto-scroll preference
var autoScrollCommandOutput = false;

/***************************************
 * 		END Configurable properties
 ***************************************/

/**
 * Extend the tabs methods available to jquery easy-ui
 * to allow the styling of tabs by index. In order to use
 * follow this pattern:
 *
 *    $('#center-tabs').tabs('setTabStyle', {
		     which: 3,
		     height: 60,
		     background: 'green',
		     color: '#fff',
		      narrow: true,
		     plain: true,
		     tabHeight: 60
		});
 */
$.extend($.fn.tabs.methods, {
	   setTabStyle: function(jq, param) {
	     return jq.each(function() {
	       var opts = $(this).tabs('options');
	       var tab = $(this).tabs('getTab', param.which).panel('options').tab.find('.tabs-inner');
	       tab.css(param);
	       tab._outerHeight(param.height);
	       var margin = opts.tabHeight - param.height;
	       tab.css({
	         lineHeight: tab[0].style.height,
	         marginTop: (opts.tabPosition == 'top' ? margin + 'px' : 0),
	         marginBottom: (opts.tabPosition == 'bottom' ? margin + 'px' : 0)
	       });
	     });
	   }
	 });

// create an string includes utility for IE 11 compatibility
if (!String.prototype.includes) {
    String.prototype.includes = function(search, start) {
      if (typeof start !== 'number') {
        start = 0;
      }

      if (start + search.length > this.length) {
        return false;
      } else {
        return this.indexOf(search, start) !== -1;
      }
    };
  }



// Utility for replacing all string instances of regex search with replacement string
String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.split(search).join(replacement);
};
//pause updates when the page is not visible
var pageIsVisible = true;
//selected id for context menu selections
var contextMenuSelectionId;
//hovered id for refreshing hover menus
var hoverSelectionId;
//selected id for the east panel detail screen
var detailSelectionId;
//selected id for the equipment tracking table
var trackingSelectionId;
//has load tracking been selected for an equipment peice
var isLoadTrackingEnabled = false;
//div for map svg
var main_map_svg = d3.select("#mapContainer");
// Status Model representing of status of equipment
// StatusModel[equipmentId] returns model for equipment status
var StatusModel = new Object();

var tooplTipDiv = d3.select("body").append("div")
.attr("id", "equipmentTooltip")
.style("opacity", 0);

//On Mouse hover over object in svg action
//Pop-up tool-tip div and populate with StatusModel data
//use 'this' to access svg element data such as this.id
function onMouseOver(d){
    var elementId = this.id;

    if(elementId.includes("-text")) // if this is a text hover, replace with id of parent
    	elementId = elementId.replace("-text","");

    if (typeof StatusModel[elementId] != "undefined")
    {
	    var tooltipDiv = d3.select("#equipmentTooltip");
	    hoverSelectionId = elementId;
	    tooltipDiv.transition()
	       .duration(200)
	       .style("opacity", 1);

	    tooltipDiv.html(getEqTooltipText(elementId))
	       .style("left", function(d)
	    		   {
	    	   			return d3.event.pageX + "px";
	    		   })
	       .style("top", function(d){
	           return d3.event.pageY - this.offsetHeight - 17  + "px"
	       })
	       .style("color", "#333333");
    }
}

//refresh the tooltop div with updated data for the element's id in the status model
function refreshTooltipData(id)
{
	 d3.select("#equipmentTooltip").html(getEqTooltipText(id));
}

// Tooltip text contents
function getEqTooltipText(id)
{
	var tt =  "<div style='cursor:pointer'>"
			+ "<font style='font-size: 20px;'>" + StatusModel[id].description + "</font>"
			+ "<div class='row'>"
			+ 	"<div class='col-sm-12'>"
			+ 		"STATUS: <span style='background-color: " + StatusModel[id].backgroundColor
			+ ";color: " + StatusModel[id].foregroundColor + ";'>&nbsp;" + StatusModel[id].statusId + "&nbsp;";
	if (StatusModel[id].errorCode || StatusModel[id].errorText)
		tt += StatusModel[id].errorCode + "&nbsp;</span><br/>ERROR: " + StatusModel[id].errorText;
	else
		tt += "</span>";
	if (StatusModel[id].trackCount > 0)
		tt += "<br/>TRACKING: " + StatusModel[id].trackCount + " loads";
	tt += " </div></div></div>";
	return tt;
}


//When mouse moves away from a hover action and disengages focus
//on a SVG element. Slowly fade away the tool-tip div using opacity 0 transition
function onMouseOut(d){
	var element = this.id;
	if(element.includes("-text"))
		element.replace("-text","");
	var elementd = this.data;
    var tooltipDiv = d3.select("#equipmentTooltip");
    tooltipDiv.transition()
          .duration(500)
          .style("opacity", 0);
}

/***************************************************************
 * UI for Error Guidance
 ***************************************************************/
function enableGuidance(equipmentId)
{
	$("#eg1-equipment").val(equipmentId);
	$("#eg1-errorcode").val(StatusModel[equipmentId].errorCode);
	$('#eg1-dialog').modal('show');
}
function guideButtonPressed()
{

	getAndDisplayErrorGuide($("#eg1-equipment").val(), $("#eg1-errorcode").val());
	$('#eg1-dialog').modal('hide');
}
$('#eg1-dialog').on('shown.bs.modal', function () {
	$('#eg1-errorcode').trigger('focus')
});
$('#eg1-button').button().on( "click", guideButtonPressed);
$("#eg1-errorcode").on('keyup', function (e) { if (e.keyCode == 13) { guideButtonPressed(); } });
function getAndDisplayErrorGuide(eq, errorcode)
{
	$.ajax({
		   url:'/airflowwcs/equipment/guide/' + eq + "?errorCode=" + errorcode,
		   type:'GET',
		   error:  function(jqXHR, textStatus, errorThrown)
		   {
			   var response = new Object;
			   response.responseCode = -1;
			   response.message="Error " + jqXHR.status + " reading guidance";;
			   alertOnResponse(response);
		       $('#eg2-title').html("Error Guidance: " + errorcode);
		       $('#eg2-errorguide').html(extractBody(jqXHR.responseText));
		       $('#eg2-dialog').modal('show');
		   },
		   success: function(data){
		       $('#eg2-title').html("Error Guidance: " + errorcode);
		       $('#eg2-errorguide').html(extractBody(data));
		       $('#eg2-dialog').modal('show');
		   }
		});
}
function extractBody(htmltxt)
{
	var start = htmltxt.indexOf("<body>");
	var end = htmltxt.indexOf("</body>");
	if (start > 0 && end > 0)
		return htmltxt.substring(start + 6, end);
	else
		return htmltxt;
}

/***************************************************************
 * UI for Send Barcode
 ***************************************************************/
function enableBarcode(equipmentId)
{
	$("#bc-bcr").val(equipmentId);
	$('#bc-dialog').modal('show');
}
function sendBCButtonPressed()
{
	sendBarCode($("#bc-bcr").val(), $("#bc-bcdata").val());
	$('#bc-dialog').modal('hide');
}
$('#bc-dialog').on('shown.bs.modal', function () {
	$('#bc-bcdata').trigger('focus')
});
$('#bc-button').button().on( "click", sendBCButtonPressed);
$("#bc-bcdata").on('keyup', function (e) { if (e.keyCode == 13) { sendBCButtonPressed(); } });

/***************************************************************
 * UI for Tracking
 ***************************************************************/
function disableTrackingUI(equipmentId)
{
	$("#tracking-area").collapse('hide');
	$("#enable-load-tracking").collapse('show');
	$("#disable-load-tracking").collapse('hide');
	isLoadTrackingEnabled = false;
}
function enableTrackingUI(equipmentId)
{
	if(StatusModel)
	{
		refreshEquipmentDetailPanel(equipmentId);
	}
	$('#equipment-layout').layout('collapse','east');
	$('#equipment-layout').layout('expand','east');
	$("#tracking-area").collapse('show');
	$("#enable-load-tracking").collapse('hide');
	$("#disable-load-tracking").collapse('show');
	$("span#currently-tracked").html("<b>"+StatusModel[equipmentId].mosId+"</b>");
	isLoadTrackingEnabled = true;
}
var trackingResizeEventId;
function trackingResizeEvent()
{
	clearTimeout(trackingResizeEventId);
	trackingResizeEventId = setTimeout(trackingResize, 250);
}
function trackingResize()
{
	// Fix header size
	if (isLoadTrackingEnabled) tableEquipmentTracking.ajax.reload();
}
function addTrackingContextMenu()
{
	if (isAdmin)
	{
		d3.selectAll("#tblWrapEquipmentTracking .dataTable td").on("contextmenu",
			d3.contextMenu(trk_menu, {
				onOpen: function() {
					if (!$(this).parent().hasClass('selected'))
						$(this).click();
				}
			})
		);
	}
}
function confirmDeleteTracking()
{
	var sel = tableEquipmentTracking.rows( { selected: true } ).data();
	if (sel.length > 0)
	{
		var deviceId = sel[0]['Device ID']
		var equipment = sel[0]['Graphic ID'];
		var trackingId = sel[0]['Tracking ID'];
		var msg = "Delete tracking for <b>" + trackingId + "</b> from <b>" + deviceId + "</b>?";
		confirmDlg("Confirm Tracking Deletion", msg, "Delete", function() {
			deleteTracking(equipment, trackingId);
			confirmDlgComplete();
		});
	}
}

/***************************************************************
 * 		  		MENUS FOR CONTEXT SPECIFIC ACTIONS
 ***************************************************************/

// Tracking
function disableLoadTracking(deviceId)
{
	disableTrackingUI(deviceId);
	postEqTrk('disable', deviceId);
	refreshEnabledEquipmentTracking = false;
}
function enableLoadTracking(deviceId)
{
	enableTrackingUI(deviceId);
	postEqTrk('enable', deviceId);
	refreshEnabledEquipmentTracking = true;
	// Pull all tracking from the controller, but filter on the equipment
	tableEquipmentTracking.search(StatusModel[deviceId].mosId);
	tableEquipmentTracking.ajax.reload();
	// Only pull tracking for the equipment
//	tableEquipmentTracking.ajax.url('/airflowwcs/equipment/loadtracking/list?deviceId='+deviceId).load(function() {
//		$("#divExecuting").hide();
//	});
}
function deleteTracking(deviceId, idText)
{
	postEqTrk('delete', deviceId + '/' + idText);
}

function postEqTrk(trkoption, deviceId)
{
	$.post('/airflowwcs/equipment/loadtracking/' + trkoption + '/' + deviceId,
			$(this).serialize(), function(response) {
		alertOnResponse(response);
	});
}

// Control/Recovery - System Level
function resetSystemErrors()			{	postEqCtrl('resetallerrors');	}
function silenceSystemAlarms()			{	postEqCtrl('silenceallalarms');	}
function startSystem()					{	postEqCtrl('system/on'); 	}
function stopSystem()					{	postEqCtrl('system/off'); 	}

// Control/Recovery - SRC Level
function srcOffline(deviceId)			{	postEqCtrl('src/off/'+deviceId);	}
function srcOnline(deviceId)			{	postEqCtrl('src/on/'+deviceId);	}
function testMcComm(deviceId)			{	postEqCtrl('testmc/'+deviceId);	}
function testMosComm(deviceId)			{	postEqCtrl('testmos/'+deviceId);	}

// Control/Recovery - Equipment Level
function disconnectDevice(deviceId)		{	postEqCtrl('disconnect/'+deviceId);	}
function guidance(errorCode)			{	postEqCtrl('guidance/'+errorCode);	}
function latchClear(deviceId)			{	postEqCtrl('latchclear/'+deviceId);	}
function reconnectDevice(deviceId)		{	postEqCtrl('reconnect/'+deviceId);	}
function resetError(deviceId)			{	postEqCtrl('reseterror/'+deviceId);	}
function saveLogs(deviceId)				{	postEqCtrl('savelog/'+deviceId);	}
function silenceAlarm(deviceId)			{	postEqCtrl('silencealarm/'+deviceId);	}
function sendBarCode(deviceId, barcode)	{	postEqCtrl('sendBarcode/'+deviceId +'?barcode='+barcode);	}
function startDevice(deviceId)			{	postEqCtrl('start/'+deviceId);	}
function stopDevice(deviceId)			{	postEqCtrl('stop/'+deviceId);	}

function postEqCtrl(destination)
{
	$.post('/airflowwcs/equipment/control/'+destination, $(this).serialize(), function(response){
		alertOnResponse(response);
	});
}


/***************************************************************
 * Context Menu Options
 ***************************************************************/
var cmBarCode =	{
	title: 'Send Barcode',
	action: function(d, i) {
		enableBarcode(contextMenuSelectionId);
	}
};
var cmDelete = {
		title: 'Delete',
		action: function(d, i) {
			confirmDeleteTracking();
		}
	};
var cmDisconnect = {
	title: 'Disconnect',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Disconnect Equipment:</b> Sending stop for ID: " + contextMenuSelectionId + ".");
		disconnectDevice(contextMenuSelectionId);
	}
};
var cmDivider = {
    divider: true
};
var cmGuidance =	{
		title: 'Error Guidance',
		action: function(d, i) {
			enableGuidance(contextMenuSelectionId);
		}
	};
var cmLatchClear = {
	title: 'Latch Clear',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Latch Clear:</b> Sending Latch Clear command for ID: " + contextMenuSelectionId + ".");
		latchClear(contextMenuSelectionId);
	}
};
var cmNoOp ={
	title: 'No action available',
	action: function(d, i) {},
	disabled: true
};
var cmReconnect = {
	title: 'Reconnect',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Reconnect Equipment:</b> Sending reconnect for ID: " + contextMenuSelectionId + ".");
		reconnectDevice(contextMenuSelectionId);
	}
};
var cmResetError = {
	title: 'Reset Error',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Resetting Error:</b> Error for ID: " + contextMenuSelectionId + " being reset.");
		resetError(contextMenuSelectionId);
	},
	disabled: false // optional, defaults to false
};
var cmSaveLogs = {
	title: 'Save Logs',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Save Logs:</b> Exporting logs for ID: " + contextMenuSelectionId + ".");
		saveLogs(contextMenuSelectionId);
	}
};
var cmSilence = {
	title: 'Silence Alarm',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Silencing Alarms:</b> Silencing for ID: " + contextMenuSelectionId + ".");
		silenceAlarm(contextMenuSelectionId);
	}
};
var cmSrcOn = {
	title: 'SRC Online',
	action: function(d, i) {
		printInfoToCommandOutput("<b>SRC Online:</b> Sending SRC Online for ID: " + contextMenuSelectionId + ".");
		srcOnline(contextMenuSelectionId);
	}
};
var cmSrcOff = {
	title: 'SRC Offline',
	action: function(d, i) {
		printInfoToCommandOutput("<b>SRC Offline:</b> Sending SRC Offline for ID: " + contextMenuSelectionId + ".");
		srcOffline(contextMenuSelectionId);
	}
};
var cmStart = {
	title: 'Start',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Start Equipment:</b> Sending Start for ID: " + contextMenuSelectionId + ".");
		startDevice(contextMenuSelectionId);
	}
};
var cmStop = {
	title: 'Stop',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Stop Equipment:</b> Sending stop for ID: " + contextMenuSelectionId + ".");
		stopDevice(contextMenuSelectionId);
	}
};
var cmTestMos = {
	title: 'Test MOS Comm.',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Test MOS Comm:</b> Testing MOS Comm for ID: " + contextMenuSelectionId );
		testMosComm(contextMenuSelectionId);
	},
	disabled: false // optional, defaults to false
};
var cmTrackingOn = {
	title: 'Load Tracking ON',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Enable Load Tracking:</b> Beginning load tracking for ID: " + contextMenuSelectionId + ".");
		enableLoadTracking(contextMenuSelectionId);
	}
};
var cmTrackingOff = {
	title: 'Load Tracking OFF',
	action: function(d, i) {
		printInfoToCommandOutput("<b>Disable Load Tracking:</b> Disabling load tracking for ID: " + contextMenuSelectionId + ".");
		disableLoadTracking(contextMenuSelectionId);
	}
};

/***************************************************************
 * Context Menus based upon behavior
 ***************************************************************/
var tracking_menu_options = [cmTrackingOn, cmTrackingOff,cmDivider];

var bcr_menu = [cmBarCode];
var crane_menu = [cmGuidance,cmResetError,cmSilence,cmDivider,cmStart,cmStop,cmDivider,cmSrcOn,cmSrcOff,cmDivider,cmDisconnect,cmReconnect,cmDivider,cmSaveLogs,cmTestMos];
var conveyor_menu = [cmGuidance,cmResetError,cmSilence,cmDivider,cmStart,cmStop,cmDivider,cmSrcOn,cmSrcOff,cmDivider,cmLatchClear,cmDivider,cmDisconnect,cmReconnect,cmDivider,cmSaveLogs,cmTestMos];
var empty_menu = [];
var fullness_menu = [];
var port_menu = [];
var swap_menu = [];
var trk_menu = [cmDelete];

//Append the Map SVG to the #mapContainer
main_map_svg
.append("svg")
.attr("width", width)
.attr("height", height)
.attr("id", "inner_map");

//inner_map div for zoom  & pan
var inner_map_svg = d3.select("#inner_map");

//create a rectangle that will catch pointer and click events in background
//of overlayed map svg which will allow for zooming and panning
inner_map_svg.append("rect")
.attr("width", width)
.attr("height", height)
.attr("id","zoom-rect")
.style("fill", "none")
.style("pointer-events", "all")
.call(d3.zoom()
.scaleExtent([1 / 2, 4])
.on("zoom", zoomed));

//return a menu for the current item, generated based
//on the graphics behavior and if the graphic has
// load tracking available to enable/disable.
function generateDropdownMenuOptions(graphic)
{
	var gen_menu = [];

//	if (graphic.canTrack==1)
//	{
//		gen_menu = gen_menu.concat(tracking_menu_options);
//	}

	if(graphic.behavior)
	{
		if (graphic.behavior.includes("Bcr"))
			gen_menu = gen_menu.concat(bcr_menu);
		if (graphic.behavior.includes("Crane"))
			gen_menu = gen_menu.concat(crane_menu);
		if (graphic.behavior.includes("Conveyor"))
			gen_menu = gen_menu.concat(conveyor_menu);
		if (graphic.behavior.includes("Fullness"))
			gen_menu = gen_menu.concat(fullness_menu);
		if (graphic.behavior.includes("Port"))
			gen_menu = gen_menu.concat(port_menu);
		if (graphic.behavior.includes("SwapZone"))
			gen_menu = gen_menu.concat(swap_menu);

		if (gen_menu.length == 0)
			gen_menu.push(cmNoOp);
		if (gen_menu[gen_menu.length-1] == cmDivider)
			gen_menu.pop();
		return gen_menu;
	}
	else
	{
		if(graphic.canTrack==1)
			return tracking_menu_options;
		else
			return empty_menu;
	}
}

/**
 * User the status model to fill the east panel information for the
 * given equipmentId.
 *
 * @param equipmentId
 * @returns true on success
 */
function refreshEquipmentDetailPanel(equipmentId)
{
	if(equipmentId.includes("-text"))
		equipmentId = equipmentId.replace("-text","");
	detailSelectionId = equipmentId;

	$("#equipmentIdDetail").val(StatusModel[equipmentId].id);
	$("#equipmentDescriptionDetail").val(StatusModel[equipmentId].description);
	$("#equipmentStatusDetail").css({'background-color' : StatusModel[equipmentId].backgroundColor});
	$("#equipmentStatusDetail").css({'color' : StatusModel[equipmentId].foregroundColor});
	$("#equipmentStatusDetail").val(StatusModel[equipmentId].statusId);

	// Disable or fill the status text area dependend on contents
	if(StatusModel[equipmentId].statusText || StatusModel[equipmentId].statusText2)
	{
		$("#equipmentStatusTextDetail").removeAttr("disabled");
		var s = StatusModel[equipmentId].statusText;
		var s2 = StatusModel[equipmentId].statusText2;
		s = s + " | " + s2;
		$("#equipmentStatusTextDetail").val(s);
	}
	else{
		$("#equipmentStatusTextDetail").attr("disabled", "disabled");
		$("#equipmentStatusTextDetail").val("");
	}
	// Disable or fill the error code area dependent on contents
	if(StatusModel[equipmentId].errorCode)
	{
		$("#equipmentErrorCodeDetail").removeAttr("disabled");
		$("#equipmentErrorCodeDetail").val(StatusModel[equipmentId].errorCode);
	}
	else{
		$("#equipmentErrorCodeDetail").attr("disabled", "disabled");
		$("#equipmentErrorCodeDetail").val("-");
	}
	// Disable or fill the error text area dependent on contents
	if(StatusModel[equipmentId].errorText)
	{
		$("#equipmentErrorTextDetail").removeAttr("disabled");
		$("#equipmentErrorTextDetail").val(StatusModel[equipmentId].errorText);
	}
	else{
		$("#equipmentErrorTextDetail").attr("disabled", "disabled");
		$("#equipmentErrorTextDetail").val("-");
	}

	if(!isLoadTrackingEnabled)
	{
		if(StatusModel[equipmentId].canTrack == 1)
		{
			$('#enable-load-tracking').collapse('show');
		}else{
			$('#enable-load-tracking').collapse('hide');
			$('#disable-load-tracking').collapse('hide');
		}
	}


	return true;
}

/**
 * SVG response (loaded_svg) is processed by appending nodes
 * to div#inner_map DOM object and click and mouseover listeners
 * are added to the nodes.
 *
 * The long-poll refresh loop to update the equipment graphic starts
 * at the end of this method.
 *
 * @param loaded_svg
 */
function loadMapXmlResponse(loaded_svg){
	//console.log(loaded_svg);

	//append all nodes to the inner_map zoom div
	d3.select("#inner_map").node().appendChild(loaded_svg.firstChild);

	// select all the elements in the SVG with selectableMapComponent css class
	selectable = main_map_svg.selectAll(".selectableMapComponent");

	//add a click listener to each selectable SVG item;
	// query the statusmodel for the clicked items' detail
	// info update and pop out the east panel
	selectable.on("click", function(){
		if (!isLoadTrackingEnabled) {
			$('#equipment-layout').layout('collapse','east'); // close the panel if it is open already
			var equipmentId = this.id; // get the id of the clicked item
			if(StatusModel)
			{
				refreshEquipmentDetailPanel(equipmentId);
			}
			$('#equipment-layout').layout('expand','east'); // expand east panel
		}
		else if ($('#east-area:hidden').length > 0)
			$('#equipment-layout').layout('expand','east'); // expand east panel
	});



	//add mouseover and out listeners to the selectable SVG items
	selectable.on("mouseover", onMouseOver)
    .on("mouseout", onMouseOut);


	//Start the Equipment refreshing cycle after a 2 second delay (to allow render & loading)
	setTimeout(startEquipmentMonitorLoop(), 2000);
}

/***********************************************************
 *  Fetch & load the SVG URL given in the configurables and use
 *  loadMapXmlResponse callback function to process the result
 ****************************************************************/
d3.svg(mapUrl).then(loadMapXmlResponse);

var g;
var selectable;


// Zoom function to transform all elements on zoom
function zoomed() {
	g = main_map_svg.selectAll("g");
	g.attr("transform", d3.event.transform);
}

// When document has loaded, get all g elements from SVG and collapse panels.
// set listener for tab click for refresh of datatables set uri
$(document).ready(function () {
    g = main_map_svg.selectAll("g");
	$('#equipment-layout').layout('collapse','west');
	$('#equipment-layout').layout('collapse','east');

	$('#enable-load-tracking-button').on('click', function(){
		enableLoadTracking(detailSelectionId);
	});

	$('#disable-load-tracking-button').on('click', function(){
		disableLoadTracking(detailSelectionId);
	});

	//reload the table when the tab is clicked
	$("a.tabs-inner").on('click', function(){ tableEquipmentDetail.ajax.reload() });


	$('#auto-scroll').change(function() {
		autoScrollCommandOutput = !autoScrollCommandOutput;
	});

	$("a#start-system").click(function() {
		startSystem();
	});

	$("a#stop-system").click(function() {
		stopSystem();
	});

	$("a#reset-system-errors").click(function() {
		resetSystemErrors();
	});

	$("a#silence-system-alarms").click(function() {
		silenceSystemAlarms();
	});

	$("#equipment-layout").css("visibility", "visible");

	tableEquipmentTracking.on('draw', function() {
		addTrackingContextMenu();
	});
	refreshEnabledEquipmentTracking = false;

	$(document).on('visibilitychange', function() {
		if (document.visibilityState == 'hidden')
		{
//			console.log("Pausing updates--page is hidden");
			pageIsVisible = false;
			clearInterval(timedFunction);
			refreshEnabledEquipmentTracking = false;
		}
		else if (pageIsVisible == false)
		{
//			console.log("Resuming updates--page is not hidden");
			pageIsVisible = true;
			startEquipmentMonitorLoop();
			if (isLoadTrackingEnabled)
			{
				refreshEnabledEquipmentTracking = true;
				tableEquipmentTracking.ajax.reload();
			}
		}
	});
});

// If user presses ESC close any d3 context menus
$( document ).on( 'keydown', function ( e ) {
    if ( e.keyCode === 27 ) { // ESC
    	d3.contextMenu('close');
    }
});



// Get a timeStamp formatted with HH:MM:SS
function timeStamp() {
// Create a date object with the current time
  var now = new Date();

// Create an array with the current month, day and time
  var date = [ now.getMonth() + 1, now.getDate(), now.getFullYear() ];

// Create an array with the current hour, minute and second
  var time = [ now.getHours(), now.getMinutes(), now.getSeconds() ];

// Determine AM or PM suffix based on the hour
  var suffix = ( time[0] < 12 ) ? "AM" : "PM";

// Convert hour from military time
  time[0] = ( time[0] < 12 ) ? time[0] : time[0] - 12;

// If hour is 0, set it to 12
  time[0] = time[0] || 12;

// If seconds and minutes are less than 10, add a zero
  for ( var i = 1; i < 3; i++ ) {
    if ( time[i] < 10 ) {
      time[i] = "0" + time[i];
    }
  }

// Return the formatted string
  return time.join(":") + " " + suffix;
}

//print message with ERROR level styling to command output window
function printErrorToCommandOutput(message)
{
	printToCommandOutput("<p style=\"font-family:courier; color: red;\"><b>" + timeStamp() + "</b>: " + message + " </p>");
}

//print message with INFO level styling to command output window
function printInfoToCommandOutput(message)
{
	printToCommandOutput("<p style=\"font-family: courier; color: yellow;\">"+ timeStamp() + ": " + message + "</p>");
}

// Print HTML formatted message to the command output window
function printToCommandOutput(message)
{
	$("#command-output").append(message);
	if(autoScrollCommandOutput)
		scrollSmoothToBottom("command-output");
}

/***************************************************************
 * 		   FUNCTIONS FOR REFRESHING THE MONITOR SCREEN
 ***************************************************************/

// Use JSON response to apply styling & animation to the SVG
function applyEquipmentGraphicStatus(response)
{
	var equipmentGraphicArr = response.equipmentGraphics;
	var i;
	var length = equipmentGraphicArr.length;
	printToCommandOutput("<p style=\"font-family:courier;\"><b>" + timeStamp() + "</b>: " + length + " status reports received.</p>");
	var inError = false;  // default to not in error - set in error on condition of statusid
	for(i=0; i<equipmentGraphicArr.length; i++)
	{
		//null check
		if(equipmentGraphicArr[i].id)
		{
			var graphic = equipmentGraphicArr[i];
			//TODO - change database ID's to not have : in id field
			// since css identifiers can't be colons commas or slashes, lets replace them with hyphens
			var graphicId = equipmentGraphicArr[i].id.replaceAll(":", "-");
			graphicId = graphicId.replaceAll("/", "-");
			graphicId = graphicId.replaceAll(",", "-");
			// set this status model's mapping to the updated graphic
			StatusModel[graphicId] = graphic;
			// bind the data to the SVG graphic
			d3.select("svg #" + graphicId).data(graphic);

			// get a dropdown menu suitable for this graphic's behavior
			var behaviorDropdownOptions = generateDropdownMenuOptions(graphic);

			//set context menu specific to the behavior of the graphic
			d3.select("svg #" + graphicId).on("contextmenu",
					d3.contextMenu(behaviorDropdownOptions, {
						onOpen: function() {
							contextMenuSelectionId = this.id;
						},
						onClose: function() {
					}})
				);


			//attempt to select and set a context menu for any text elements that may be associated with this Id
			var select_text = d3.select("svg text#" + graphicId + "-text");
			if(!select_text.empty())
		    {
				d3.select("svg text#" + graphicId+"-text").on("contextmenu",
						d3.contextMenu(behaviorDropdownOptions, {
							onOpen: function() {
								contextMenuSelectionId = this.id.replaceAll("-text","");
							},
							onClose: function() {
						}})
					);
		    }
			// TODO - Remove - debug ------
			var selection = d3.select("svg #" + graphicId).data;
			var selectionDesc = selection.description;
			// TODO - END Remove - debug ------

			// Set error status on status id and add notifications where necessary
			if(graphic.statusId == 'ERROR')
			{
				inError = true;
				printErrorToCommandOutput("EQUIPMENT ERROR: " + graphicId + " is in an ERROR State. ");
				//addErrorNotification("EQUIPMENT ERROR: " + graphicId + " is in an ERROR State. ");

			}

			// null background color check for setting default color - or set give background color
			if(!graphic.backgroundColor)
			{
				d3.select("svg #" + graphicId).transition().duration(1500).style("fill", "#C0C1B9");
			}
			else
			{
				d3.select("svg #" + graphicId).transition().duration(1500).style("fill", graphic.backgroundColor);
			}

			// behavior null check
			if(graphic.behavior)
			{
				//** Set FULLNESS INDICATORS text and text color //
				if(graphic.behavior.includes("Fullness") )
				{
					d3.select("svg text#" + graphicId + "-text")
						.text(graphic.statusText2); // set text of fullness indicators
					d3.select("svg text#" + graphicId + "-text").transition().duration(1500)
						.style("fill", graphic.foregroundColor); // set text color of fullness indicators
				}
				//*** Set SWAP ZONES text and text color  *//
				if(graphic.behavior.includes("Swap"))
				{
					d3.select("svg text#" + graphicId + "-text")
						.text(graphic.statusText2); // set text of fullness indicators
					d3.select("svg text#" + graphicId + "-text").transition().duration(1500)
						.style("fill", graphic.foregroundColor); // set text color of fullness indicators

				}
				//*** Set CRANE text and text color  *//
				if(graphic.behavior.includes("Crane"))
				{
					d3.select("svg text#" + graphicId + "-text")
					    .text(graphic.statusText); // set text of fullness indicators
					d3.select("svg text#" + graphicId + "-text").transition().duration(1500)
					    .style("fill", graphic.foregroundColor); // set text color of fullness indicators
				}
				if(graphic.behavior.includes("Port"))
				{
					if(graphic.statusId!="ONLINE" && graphic.statusId!="RUNNING")
					{
						$("li#host-connection-status").removeClass("connected-background");
						$("li#host-connection-status").addClass("not-connected-background");
						$("span#host-connection-status-detail").html("ERROR");
					}else
					{
						$("li#host-connection-status").addClass("connected-background");
						$("li#host-connection-status").removeClass("not-connected-background");
						$("span#host-connection-status-detail").html("OK");
					}
				}
			}
		}

	}
	/* If we have an active hover, update it with newly acquired data */
	if(hoverSelectionId)
		refreshTooltipData(hoverSelectionId);
	if(detailSelectionId)
		refreshEquipmentDetailPanel(detailSelectionId);


	/**
	 * Set Header background color to RED if in error
	 */
	if(inError)
	{

		/* $("div.hoe-right-header").attr("style","background-color: #A63232 !important;"); */
		$('#center-tabs').tabs('setTabStyle', {
		     which: 0,
		     background: '#A63232',
		     color: '#fff'
			});
	}
	else
	{
		/* $("div.hoe-right-header").attr("style","background-color:#101011 !important;"); */
		$('#center-tabs').tabs('setTabStyle', {
		     which: 0,
		     background: 'none',
		     color: '#000',
			});
	}
	return true;
}

// Use JSON response to style the equipment monitor tabs
function applyEquipmentTabStatus(response)
{
	var equipmentTabArr = response.equipmentTabs;
	var i;
	for(i=0; i<equipmentTabArr.length; i++)
	{
		if(equipmentTabArr[i].id)
		{
			var tabId = equipmentTabArr[i].id.replace(":", "-");
		}
	}
	return true;
}

//Scroll the div with id to the bottom of it's height (used for command output window)
function scrollSmoothToBottom (id) {
	   var div = document.getElementById(id);
	   $('#' + id).animate({
	      scrollTop: div.scrollHeight - div.clientHeight
	   }, 500);
}


//REST call to poll equipment status & apply styling changes with
function pollEquipmentStatus()
{
	if (pageIsVisible)
	{
		$.get('/airflowwcs/equipment/status/all', $(this).serialize(), function(response){
			$("#connection-status").removeClass( "not-connected-background");
			$("#connection-status").addClass( "connected-background");
			$("#connection-status-detail").html("OK");
			if(response.equipmentGraphics)
			{
				applyEquipmentGraphicStatus(response);
			}
			if(response.equipmentTabs)
			{
				applyEquipmentTabStatus(response);
			}
			lastActivity = new Date().getTime();
		}).fail(
				function(){
					printToCommandOutput("<p style=\"color: red !important; font-family:courier;\"><b>" + timeStamp() + "</b>: CONNECTION ERROR! Unable to retrieve status reports.</p>");
					$("#connection-status").removeClass( "connected-background");
					$("#connection-status").addClass( "not-connected-background");
					$("#connection-status-detail").html("ERROR");
				}
		);
	}
}

// equipment poll timer function
var timedFunction;

// Loop the equipment status screen poll function on 6 second interval
function startEquipmentMonitorLoop()
{
	pollEquipmentStatus(); // load immediately on first poll
	timedFunction = setInterval(function(){pollEquipmentStatus()}, equipmentRefreshRate);
}


