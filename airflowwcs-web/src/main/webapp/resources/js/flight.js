var isAdmin = false;

/*****************************************************************
 * 						On page ready setup
 ****************************************************************/

/* Details */
function getFlightDetails(id) {
	var flightDetailsUrl = "viewDetails?flightId=" + id;
	window.location.href = flightDetailsUrl; //"viewDetails/"+id;
}


$(document).ready(function() {
	var table = $('#ajaxTableFlights').DataTable();
	searchFlights();

	function searchFlights() {
		tableFlights.ajax.url('/airflowwcs/flight/list').load();
	}

	$('#ajaxTableFlights tbody').on('dblclick', 'tr', function() {
		var d = table.row(this).length;
		var data = table.row(this).id();
		if (d = 1) {
			getFlightDetails(data);
		}
	});


	$(document).contextmenu({
		delegate: ".dataTable td",
		menu: [{ title: "viewDetails", cmd: "flightDetails" }, { title: "Retrieve Flight", cmd: "retrieve" }],
		select: function(event, ui) {
			var idText = table.row(ui.target).id();
			var celltext = ui.target.text();
			switch (ui.cmd) {
				case "flightDetails":
					getFlightDetails(idText);
					break;
				case "retrieve":
					retrieveFlight(idText);
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

	function getRetrieveDest(loadId) {
		if (selectedRows.length > 0) {
			//set the load id
			$("#retrieveLoad").val(loadId);
			$('#retrieve-modal').modal('show');
		}
		else {
			alert("No Loads Selected.");
		}
	}

	$("#load-retrieve-button").click(function() {
		var destSt = $("#retrieveDestination").val();

		$('#retrieve-modal').modal('hide');
		$("#divExecuting").show();

		retrieveLoads(destSt);
	});

	function retrieveLoads(destStation) {
		var loadIds = selectedRows;
		$.post('/airflowwcs/flight/retrieveLoads?loadIds=' + loadIds + "&destStation=" + destStation, $(this).serialize(), function(response) {
			if (response.responseCode == 1) {
				alertInfo("INFO", response.responseMessage);
			}
			else {
				alert(response.responseMessage);
			}
			$("#divExecuting").hide();
		});
	}
	function retrieveFlight(lot) {
	$.confirm({
		title: 'Confirm Retrieve',
		content: 'Are you sure you want to retrieve data for this Flight?',
		buttons: {
			confirm: function() {
				$("#divExecuting").show();
				$.post('/airflowwcs/flight/makeOrderForFlight?lot=' + lot, $(this).serialize(), function(response) {
					$("#divExecuting").hide();
					console.log(response);
					alertOnResponse(response);
					table.ajax.reload();
				});
			},
			cancel: function() {
				//do nothing
			}
		}
	});
}


});




