$(document).ready(function() {
	var table = $('#ajaxTable').DataTable();
	var iId = "";

	$("#work-filtering-form").submit(function(e) {
		e.preventDefault();
	});

	$("#scLoadId").on('keyup', function(e) {
		if (e.keyCode == 13) {
			$("#divExecuting").show();
			searchWork();
		}
	});

	$("#scLot").on('keyup', function(e) {
		if (e.keyCode == 13) {
			$("#divExecuting").show();
			searchWork();
		}
	});
	$("#scLineId").on('keyup', function(e) {
		if (e.keyCode == 13) {
			$("#divExecuting").show();
			searchWork();
		}
	});
	$("#scdeviceId").on('keyup', function(e) {
		if (e.keyCode == 13) {
			$("#divExecuting").show();
			searchWork();
		}
	});

	$("#searchWorkButton").click(function() {
		$("#divExecuting").show();
		searchWork();
	});

	$('#command-modify-button').click(function() {
		$('#command-modify-form').submit();
	});

	addContextMenu();	
});

function searchWork() {
	table.ajax.url("/airflowwcs/work/listSearch?loadId=" + $("#scLoadId").val() + "&lot=" + $("#scLot").val() + "&lineId=" + $("#scLineId").val() + "&deviceId=" + $("#scdeviceId").val()).load(function() {
		$("#divExecuting").hide();
	});
}

function addContextMenu() {
	$(document).contextmenu({
		delegate: ".dataTable td",
		menu: [
			{ title: "Add Command", cmd: "addCommand" }, { title: "Delete", cmd: "delete" }, { title: "Modify", cmd: "modify" }
		],
		select: function(event, ui) {

			var columnData = table.row(ui.target).data();
			if (typeof columnData != "undefined") {
				iId = columnData["IID"];
			}
			switch (ui.cmd) {
				case "addCommand":
					showAddCommand();
					break;

				case "delete":
					deleteCommand(iId);
					break;

				case "modify":
					showModifyCommand(iId);
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
}
function showAddCommand() {
    clearForm();
	$('#add-modal').modal('show');
}
function clearForm() {
	$("#LineID").val("");
	$("#orderId").val("");
	$("#globalId").val("");
	$("#flightNum").val("");
	$("#finalSortLocationID").val("");
	$("#from").val("");
	$("#flightStd").val("");
	$("#loadId").val("");
	$("#toDest").val("");
}
function showModifyCommand(id) {
	//clearForm();

	$.post('/airflowwcs/work/find?id=' + id, $(this).serialize(), function(response) {
		for (var key in response) {
			if (response.hasOwnProperty(key)) {
				if (key == "flightStd") {
					var expDate = new Date(response[key]);
					if (expDate != "Invalid Date") {
						const offset = expDate.getTimezoneOffset();
						expDate = new Date(expDate.getTime() - (offset * 60 * 1000));
						$("#" + key + "Mod").val(expDate.toISOString().split('T')[0]);
						$("#modify-modal #" + key).val(expDate.toISOString().split('T')[0]);
					}
					}else{
						$("#" + key + "Mod").html(response[key]);
						$("#" + key + "Mod").val(response[key]);
						$("#modify-modal #" + key).val(response[key]);
					}
				}
			}
		});
	$('#modify-modal').modal('show');
}

$('#command-add-button').click(function() {
	$('#command-add-form').submit();
	clearForm();
});
$('#searchLoad').click(function() {
	$.get('/airflowwcs/work/byload/' + $("#loadId").val(), $(this).serialize(), function(response) {
		if (response.responseCode == -1) {
			//$("#add-modal .close").click() 
			alertErrorForSearch("ERROR", "Load Data of Load Id " + $("#loadId").val() + " does not exists");
			clearForm();
		}
		else {
			$("#LineID").val(response.data[0]["SLINEID"]);
			$("#orderId").val(response.data[0]["Order Id"]);
			$("#globalId").val(response.data[0]["Global Id"]);
			$("#flightNum").val(response.data[0]["SLOT"]);
			$("#finalSortLocationID").val(response.data[0]["finalShortLocationId"]);
			$("#from").val(response.data[0]["currentAddress"]);
			var expDate = new Date(response.data[0]["DEXPECTEDDATE"]);
			if (expDate != "Invalid Date") {
				const offset = expDate.getTimezoneOffset();
				expDate = new Date(expDate.getTime() - (offset * 60 * 1000));
				$("#flightStd").val(expDate.toISOString().split('T')[0]);
			}

		}

	});

});
function deleteCommand(id) {
	$.confirm({
		title: 'Confirm Delete',
		content: 'Are you sure you want to delete this Command?',
		buttons: {
			confirm: function() {
				$("#divExecuting").show();
				$.post('/airflowwcs/work/delete?id=' + id, $(this).serialize(), function(response) {
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
function alertErrorForSearch(label, message) {
	var label_id = label.replace(/\s/g, '_'); //replace spaces with underscores
	label_id = label_id + getRandomInt(1, 9999); //give a random id for duplicate labeled alerts
	var alert = buildAlert(label_id, message, 'alert-danger', label);
	$('.modal-title').prepend(alert).fadeIn('slow');
	$("#" + label_id + "").fadeTo(2000, 500).slideUp(400, function() {
		$("#" + label_id + "").slideUp(500);
	});
}
$('#command-add-form').submit(function() {
	$.post($(this).attr('action'), $(this).serialize(), function(response) {
		$("#add-modal .close").click()  // close modal
		alertOnResponse(response); // alert if success or fail 
		table.ajax.reload(); // reload the results
		table.draw();
	});
	return false;
});

$("#command-modify-form").submit(function() {
	$.post($(this).attr('action') + "?iId=" + iId, $(this).serialize(), function(response) {
		$("#modify-modal .close").click()
		alertOnResponse(response);
		table.ajax.reload();
		table.draw();
	});
	return false;
});
