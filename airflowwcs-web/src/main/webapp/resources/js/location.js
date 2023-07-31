/* Details */
function onDrillDown(id) {
	tableLocationDetails.ajax.url('/airflowwcs/load/byLocation?'+rowIdToParam(id)).load(function(){
		$('#drilldown-modal').modal('show');
		setTimeout(tableLocationDetails.draw, 500);
	});
}
/* Search */
function searchLocations() {
	$("#divExecuting").show();
	var scform = $("#location-filtering-form");
	table.ajax.url(scform.attr('action') + "?" + scform.serialize()).load(function() {
		$("#divExecuting").hide();
	});
}
/* General UI */
function rowIdToParam(id) {
	var params = id.split('-');
	return "warehouse=" + params[0] + "&address=" + params[1] + "&shelfPosition=" + params[2];
}
function addListeners() {
	$("#scPanel input").on('keyup', function (e) {
		if (e.keyCode == 13) {
			searchLocations();
		}
	});
	$("#scWarehouse").change(function () {
		searchLocations();
	});
	$("#scDeviceId").change(function () {
		searchLocations();
	});
	$("#scZone").change(function () {
		searchLocations();
	});
	$("#scLocationStatus").change(function () {
		searchLocations();
	});
	$("#scEmptyFlag").change(function () {
		searchLocations();
	});
	$("#scType").change(function () {
		searchLocations();
	});
	$("#searchLocationsButton").click(function() {
		searchLocations();
	});
}
/* Ready */
$(document).ready(function(){
	addListeners();
});
