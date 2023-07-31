var autoRefresh = false; 

var contextMenuOptions = (contextMenuOptions === undefined) ? 
	[
		{title: "Show Items", cmd: "details" },
		{title: "Show Location", cmd: "location" },
		{title: "Show Moves", cmd: "moves" }
	]
	: contextMenuOptions;

function startAutoRefresh() {
	tid = setTimeout(table.ajax.reload, 8000); // repeat
}

function toggleRefresh() {
	autoRefresh = !autoRefresh;
}

function searchOnEnter(e) {
	if (e.keyCode == 13) {
		searchLoads();
	}
}

function startWait()
{
	$("#divExecuting").show();
}

function endWait()
{
	$("#divExecuting").hide();
}

function searchLoads()
{
	startWait(); 
	table.ajax.url("/airflowwcs/recovery2/listSearch?loadId="+$("#loadId").val()
			+"&mcKey="+$("#mcKey").val()
			+"&warehouse="+$("#warehouse").val()
			+"&address="+$("#address").val()
			+"&shelfPosition="+$("#shelfPosition").val()
			+"&nextWarehouse="+$("#nextWarehouse").val()
			+"&nextAddress="+$("#nextAddress").val()
			+"&nextShelfPosition="+$("#nextShelfPosition").val()
			+"&device="+$("#scdeviceId").val())
			.load(function() {
		endWait();
	}); 
}	

$(document).ready(function() {

	var table = $('#ajaxTable').DataTable();
	
	$("#loadId").on('keyup', searchOnEnter);
	$("#mcKey").on('keyup', searchOnEnter);
	$("#address").on('keyup', searchOnEnter);
	$("#shelfPosition").on('keyup', searchOnEnter);
	$("#nextAddress").on('keyup', searchOnEnter);
	$("#nextShelfPosition").on('keyup', searchOnEnter);
	$("#searchLoadsButton").click(searchLoads);

	function startRecovery(loadId)
	{
		$.post("/airflowwcs/recovery2/recover?loadId="+loadId, 
			function(response) {
				alertOnResponse(response);
				executeStep(loadId, response);
			});
	}
	
	function executeStep(loadId, response)
	{
		console.log(response);
		if (response == null)
		{
			$.alert({ title: 'ERROR', content: "Empty results!" });
			return;
		}
		switch (response.responseCode)
		{
		case -4: // AltPrompt
			recover(loadId, response.responseMessage);
			break;
		case -3: // Prompt
			prompt(loadId, response);
			break;
		case -2: // Warning
			$.alert({ title: 'Warning', content: response.responseMessage.replace(/\n/g, "<br>") });
			break;
		case -1: // Error
			$.alert({ title: 'ERROR', content: response.responseMessage.replace(/\n/g, "<br>") });
			break;
		case 0: // Info
			$.alert({ title: 'Info', content: response.responseMessage.replace(/\n/g, "<br>") });
			break;
		case 1: // Success
			$.alert({ title: 'Success', content: response.responseMessage.replace(/\n/g, "<br>") });
			break;
		default:
			$.alert({ title: 'ERROR', content: "Unexpected results!" });
			break;
		}
		
	}
	
	function prompt(loadId, response)
	{
		$.confirm({
			title: "Confirm", 
			content: response.responseMessage, 
			buttons:{
				yes: function(){
					executeStep(loadId, response.yes);
				}, 
				no: function(){
					executeStep(loadId, response.no);
				}
			}
		});
	}
	

	function recover(loadId, recoverUrl)
	{
		if (recoverUrl == "delete")
		{
			deleteLoad(loadId);
		}
		else
		{
			$.post("/airflowwcs/recovery2/" + recoverUrl
					+ "?loadId=" + loadId, function(response) {
				alertOnResponse(response);
				executeStep(loadId, response);
			});
		}
	}
	
	function deleteLoad(loadId) {
		$.confirm({
			title: 'Confirm Delete',
			content: 'Are you sure you want to delete Load <strong>' + loadId +'</strong>?',
			buttons: {
				confirm: function () {
					startWait();
					$.post('/airflowwcs/load/delete?load='+loadId, $(this).serialize(), function(response) { 
						endWait();
						alertOnResponse(response); 
						table.ajax.reload();
					});
				},
				cancel: function () {
					//do nothing
				}
			}
		});
	}
	
	function showLoadDetails(loadId){
		tableLoadDetails.ajax.url('/airflowwcs/itemdetail/byload/'+loadId).load(); 
		$('#drilldown-modal').modal('show'); 
		setTimeout(tableLoadDetails.ajax.reload, 500); // TODO - KLUDGE figure out why the styling is not responding on first load (misaligned columns)
	}

	function showMoves(loadId) {
		tableLoadMove.ajax.url('/airflowwcs/move/listByLoadId/'+loadId).load(); 
		$('#load-moves-modal').modal('show'); 
		setTimeout(tableLoadMove.ajax.reload, 500); 
	}
	
	function showLocation(loadId) {
		tableLoadLocation.ajax.url('/airflowwcs/location/locationByLoadId/'+loadId).load(); 
		$('#load-location-modal').modal('show'); 
		setTimeout(tableLoadLocation.ajax.reload, 500); 
	}
	
	$(document).contextmenu({
		delegate: ".dataTable td",
		menu: contextMenuOptions, 
			select: function(event, ui) {

				var idText = table.row(ui.target).id(); 
				var celltext = ui.target.text();
				switch(ui.cmd){
				case "delete":
					deleteLoad(idText); 
					break;
				case "details":
					showLoadDetails(idText); 
					break;
				case "location":
					showLocation(idText); 
					break;
				case "moves":
					showMoves(idText); 
					break;
				case "recover":
					startRecovery(idText); 
					break; 
				default:
					alert("ERROR: option not recognized."); 
				}
			},
			beforeOpen: function(event, ui) {
				var $menu = ui.menu,
				$target = ui.target,
				extraData = ui.extraData;
				if (!ui.target.parent().hasClass('selected'))
					$(ui.target).click();
			}
	});
	
	$('#load-detail-form').submit(function() {
		table.clear().draw()
		$.post($(this).attr('action'), $(this).serialize(), function(response) {
			table.rows.add(response).draw();  // redraw w/results 
			$("#detail-search-modal .close").click(); //close modal
			alertSuccess('Search Completed', 'Detailed search completed successfully.'); // alert 
		}, 'json');
		return false;
	});
	
	$('#load-detail-search').click(function(){
		$('#load-detail-form').submit();
	});
} );
