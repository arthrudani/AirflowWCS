var maxHourVal = 23;
var minVal = 0;

var maxMinuteVal = 59;
var defaultVal = "00";
var defaultSchemaId = "1";

var arrTimeData = [];
 var selectedTimeslot = "1";
/**
 * validate only numbers
 */
function isNumber(evt) {
	evt = (evt) ? evt : window.event;
	var charCode = (evt.which) ? evt.which : evt.keyCode;
	if (charCode > 31 && (charCode < 48 || charCode > 57)) {
		return false;
	}
	return true;
}

function definitelyNaN(val) {
	return isNaN(val && val !== true ? Number(val) : parseFloat(val));
}

function timeValidation(minuteValue, hourValue) {
	if (!definitelyNaN(minuteValue) && !definitelyNaN(hourValue)) {
		if (minuteValue > maxMinuteVal) {
			alertError("ERROR", " Minute values must be less than 59 ");
			return false;
		}
		if (hourValue > maxHourVal) {
			alertError("ERROR", " Hour values must be less than 23 ");
			return false;
		}
	}
	else {
		alertError("ERROR", " Hour and Minute values are required");
		return false;
	}
	return true;
}

function isTimeslotExsist(timeslotValue) {
	console.log(arrTimeData);
	const found = arrTimeData.find(x => x == timeslotValue);
	console.log(found);
	if (found != undefined) {
		alertError("ERROR", "Timeslot already avaialble in the DB, please try add new timeslot");
		return false;
	}
	return true;
}

function getTableData() {
	$("#ajaxTableTimeslots tbody tr").each(function() {
		var currentRow = $(this);
		var col1_value = currentRow.find("td:eq(0)").text();
		arrTimeData.push(col1_value);
	});

	console.log(arrTimeData);
}

function loadTimeslotBySchemaId(schemaId) {
	var table = $('#ajaxTableTimeslots').DataTable();
	table.ajax.url('/airflowwcs/timeslot/list?schemaId=' + schemaId).load();		
}

$('#dropdownSchemas').on('change', function() {
	loadTimeslotBySchemaId(this.value);
	setLableTitle($("#dropdownSchemas :selected").text());

	arrTimeData = [];
	setTimeout(getTableData, 1300);
});

function setLableTitle(selectedSchema) {
	$('#lblSelectedSchema').html("Time slots for Schema : " + selectedSchema);
}

$(document).ready(function() {

	var table = $('#ajaxTableTimeslots').DataTable();
	loadTimeslotBySchemaId(defaultSchemaId)
	setTimeout(getTableData, 1300);

	setLableTitle($("#dropdownSchemas :selected").text());

	$("#confirm-delete-button").on("click", function() {
		deleteTimeslot();
	});

	$(document).contextmenu({	
		delegate: ".dataTable td",
		menu: [{ title: "Remove Time slot", cmd: "delete" }],
		select: function(event, ui) {
		
			var idText = table.row(ui.target).id();
			var celltext = ui.target.text();
			selectedTimeslot = tableTimeslots.row(ui.target).id();

			switch (ui.cmd) {
				case "delete":
					$("#confirm-delete-modal").modal('show');
					break;
				default:
					alert("ERROR: option not recognized.");
			}
		},
		beforeOpen: function(event, ui) {
			var $menu = ui.menu,
				$target = ui.target,
				extraData = ui.extraData;
		}
	});

	function deleteTimeslot() {
		console.log(selectedTimeslot);

		if (selectedTimeslot != "1"){			
			var schemaId = $("#dropdownSchemas").val();
			$.post("/airflowwcs/timeslot/delete?timeSlot=" + selectedTimeslot + "&schemaId=" + schemaId, $(this).serialize(), function(response) {				
				alertOnResponse(response);
				if (response.responseCode == 1) {
					location.reload();
				}
			});
		}
		else {
			alertError("Selection", "No rows selected! Select a row and retry.")
		}
	}

	/* hour value increase function */
	$("#buttomHoursUp").click(function() {
		var hourValue = parseFloat($("#txtHourValue").val());
		if (!definitelyNaN(hourValue)) {
			var newValue = hourValue + 1;
			if (newValue > maxHourVal) {
				$("#txtHourValue").val(defaultVal);
			}
			else {
				if (newValue < 10) {
					$("#txtHourValue").val("0" + newValue);
				}
				else {
					$("#txtHourValue").val(newValue);
				}
			}
		}
		else {
			$("#txtHourValue").val(defaultVal);
		}
	});

	/* hour value decrease function */
	$("#buttomHoursDown").click(function() {
		var hourValue = parseFloat($("#txtHourValue").val());
		if (!definitelyNaN(hourValue)) {
			var newValue = hourValue - 1;
			if (newValue < minVal) {
				$("#txtHourValue").val(maxHourVal);
			}
			else {
				if (newValue < 10) {
					$("#txtHourValue").val("0" + newValue);
				}
				else {
					$("#txtHourValue").val(newValue);
				}
			}
		}
		else {
			$("#txtHourValue").val(defaultVal);
		}
	});

	/* minute value increase function */
	$("#buttomMinUp").click(function() {
		var minuteValue = parseFloat($("#txtMinuteValue").val());
		if (!definitelyNaN(minuteValue)) {
			var newValue = minuteValue + 1;

			if (newValue > maxMinuteVal) {
				$("#txtMinuteValue").val(defaultVal);
			}
			else {
				if (newValue < 10) {
					$("#txtMinuteValue").val("0" + newValue);
				}
				else {
					$("#txtMinuteValue").val(newValue);
				}
			}
		}
		else {
			$("#txtMinuteValue").val(defaultVal);
		}
	});

	/* minute value decrease function */
	$("#buttomMinDown").click(function() {
		var minuteValue = parseFloat($("#txtMinuteValue").val());
		if (!definitelyNaN(minuteValue)) {
			var newValue = minuteValue - 1;

			if (newValue < minVal) {
				$("#txtMinuteValue").val(maxMinuteVal);
			}
			else if (newValue == minVal) {
				$("#txtMinuteValue").val(defaultVal);
			}
			else {
				if (newValue < 10) {
					$("#txtMinuteValue").val("0" + newValue);
				}
				else {
					$("#txtMinuteValue").val(newValue);
				}
			}
		}
		else {
			$("#txtMinuteValue").val(defaultVal);
		}
	});


	/**
	* Sumbit the time slot
	*/
	$("#addTimeslotButton").click(function() {

		var minuteValue = parseFloat($("#txtMinuteValue").val());
		var hourValue = parseFloat($("#txtHourValue").val());

		var timeslotValue = $("#txtHourValue").val() + ":" + $("#txtMinuteValue").val();
		if (timeValidation(minuteValue, hourValue) && isTimeslotExsist(timeslotValue)) {
			saveTimeslot(timeslotValue);
		}
	});

	function saveTimeslot(timeslotValue) {
		var schemaId = $("#dropdownSchemas").val();
		$.post("/airflowwcs/timeslot/saveTimeslot?timeSlot=" + timeslotValue + "&schemaId=" + schemaId, $(this).serialize(), function(response) {
			alertOnResponse(response);
			if (response.responseCode == 1) {
				loadTimeslotBySchemaId(schemaId);
				setTimeout(getTableData, 1300);
			}
		});
	}
});