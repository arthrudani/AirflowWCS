var autoRefresh = false;

var contextMenuOptions = (contextMenuOptions === undefined) ? 
		[
			{title: "Show Location", cmd: "location" },
			{title: "Show Moves", cmd: "moves" }
		]
		: contextMenuOptions;

function startAutoRefresh()
{
	// reimplement
	tid = setTimeout(table.ajax.reload, 8000); // repeat
}

function toggleRefresh()
{
	autoRefresh = !autoRefresh;
}

function onDrillDown(loadId){
	// No-op for load mover
}

$(document).ready(function() {

	var table = $('#ajaxTable').DataTable();

	// Custom export to force address/position as text.  See AjaxTableTag.
	for (var i = 0; i < table.init().columns.length; i++) {
		if ($(table.column(i).header()).html().indexOf("Address") >= 0) {
			forceTextIdx.push(i);
		} else if ($(table.column(i).header()).html().indexOf("Position") >= 0) {
			forceTextIdx.push(i);
		}
	}

	function searchOnEnter(e)
	{
		if (e.keyCode == 13)
		{
			$("#divExecuting").show();
			searchLoads();
		}
	}

	$("#loadId").on('keyup', searchOnEnter);
	$("#mcKey").on('keyup', searchOnEnter);
	$("#address").on('keyup', searchOnEnter);
	$("#shelfPosition").on('keyup', searchOnEnter);

	$("#searchLoadsButton").click(function() {
		$("#divExecuting").show();
		//$('#load-filtering-form').submit()
		searchLoads();
	});

	/**
	 * Context action popups.
	 * @param loadId
	 **/
	function showModifyLoad(loadId)
	{
		clearModifyForm();

		$.post('/airflowwcs/load/find?load='+loadId, $(this).serialize(), function(response){
			for(var key in response){
				if(response.hasOwnProperty(key)){
					$("#"+key+"Mod").html(response[key]);
					$("#"+key+"Mod").val(response[key]);
					$("#modify-modal #"+key).val(response[key]);
				}
			}
		});
		$('#modify-modal').modal('show');
	}

	function showRetrieveLoads(loadIds){
		console.log("Retrieve " + loadIds);
		$('#retrieveLoad').html(loadIds);
		$('#retrieveLoad').val(loadIds);
		$('#retrieveLoad').tooltip(loadIds);
		$('#retrievePriority').val(5);
		$('#retrieve-modal').modal('show');
	}

	function showRetrieveLoad(loadId){
		console.log("Retrieve " + loadId);
		$('#retrieveLoad').html(loadId);
		$('#retrieveLoad').val(loadId);
		$('#retrievePriority').val(5);
		$('#retrieve-modal').modal('show');
	}

	function deleteLoads(loadId){
		$.confirm({
			title: 'Confirm Delete',
			content: 'Are you sure you want to delete Loads <strong>' + loadId +'</strong>?',
			buttons: {
				confirm: function () {
					$("#divExecuting").show();
					$.post('/airflowwcs/load/delete?load='+loadId, $(this).serialize(), function(response) {
						$("#divExecuting").hide();
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

	function deleteLoad(loadId){
		$.confirm({
			title: 'Confirm Delete',
			content: 'Are you sure you want to delete Load <strong>' + loadId +'</strong>?',
			buttons: {
				confirm: function () {
					$("#divExecuting").show();
					$.post('/airflowwcs/load/delete?load='+loadId, $(this).serialize(), function(response) {
						$("#divExecuting").hide();
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

	function markLoadQCHOLD(loadId){
		$.confirm({
			title: 'Confirm QCHOLD',
			content: 'Are you sure you want mark Load <strong>' + loadId +'</strong> as QCHOLD?',
			buttons: {
				confirm: function () {
					$("#divExecuting").show();
					$.post('/airflowwcs/load/markQcHold?load='+loadId, $(this).serialize(), function(response) {
						$("#divExecuting").hide();
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

	function markLoadAVAIL(loadId){
		$.confirm({
			title: 'Confirm AVAILABLE',
			content: 'Are you sure you want mark Load <strong>' + loadId +'</strong> as AVAILABLE?',
			buttons: {
				confirm: function () {
					$("#divExecuting").show();
					$.post('/airflowwcs/load/markAvail?load='+loadId, $(this).serialize(), function(response) {
						$("#divExecuting").hide();
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

	function showMoves(loadId){
		tableLoadMove.ajax.url('/airflowwcs/move/listByLoadId/'+loadId).load();
		$('#load-moves-modal').modal('show');
		setTimeout(tableLoadMove.ajax.reload, 500);
	}

	function showLocation(loadId){
		tableLoadLocation.ajax.url('/airflowwcs/location/locationByLoadId/'+loadId).load();
		$('#load-location-modal').modal('show');
		setTimeout(tableLoadLocation.ajax.reload, 500);
	}


	function makeOldest(loadId){
		alert('oldest ' + loadId);
	}

	function searchLoads()
	{
		table.ajax.url('/airflowwcs/load/listSearch?'+$("#load-filtering-form").serialize()).load(function() {
			$("#divExecuting").hide();
		});
	}

	/************************************************************
	 * 	 CLEAR MODIFY SCREEN FIELDS
	 ************************************************************/
	function clearModifyForm()
	{
		$('#containerTypeMod').html('');
		$('#weightMod').html('');
		$('#heightMod').html('');
		$('#warehouseMod').html('');
		$('#addressMod').html('');
		$('#shelfPositionMod').html('');
		$('#nextWarehouseMod').html('');
		$('#nextAddressMod').html('');
		$('#nextShelfPositionMod').html('');
		$('#finalWarehouseMod').html('');
		$('#finalAddressMod').html('');
		$('#routeIdMod').html('');
		$('#moveStatusMod').html('');
		$('#messageMod').html('');
		$('#barcodeMod').html('');
		$('#sAmountFullMod').html('');
		$('#lpCheckMod').html('');
		$('#deviceIdMod').html('');
		$('#recZoneMod').html('');

		$('#containerTypeMod').val('');
		$('#weightMod').val('');
		$('#heightMod').val('');
		$('#warehouseMod').val('');
		$('#addressMod').val('');
		$('#nextWarehouseMod').val('');
		$('#nextAddressMod').val('');
		$('#finalWarehouseMod').val('');
		$('#finalAddressMod').val('');
		$('#routeIdMod').val('');
		$('#moveStatusMod').val('');
		$('#messageMod').val('');
		$('#barcodeMod').val('');
		$('#sAmountFullMod').val('');
		$('#lpCheckMod').val('');
		$('#deviceIdMod').val('');
		$('#recZoneMod').val('');

		$('#moveStatusMod').val(null).trigger('change');
		$('#moveStatus').val(null).trigger('change');
	}

	$(document).contextmenu({
		delegate: ".dataTable td",
		menu: contextMenuOptions,
		select: function(event, ui) {

			var idText = table.row(ui.target).id();
			var celltext = ui.target.text();
			switch(ui.cmd){
			case "modify":
				showModifyLoad(idText);
				break;
			case "delete":
				if (selectedRows.length > 1)
					deleteLoads(selectedRows);
				else
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
			case "retrieve":
				if (selectedRows.length > 1)
					showRetrieveLoads(selectedRows);
				else
					showRetrieveLoad(idText);
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
				ui.target.click();
		}
	});

	$('#load-add-form').submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#add-modal .close").click()  // close modal
			alertOnResponse(response); // alert if success or fail
			table.ajax.reload(); // reload the results
			table.draw();
		});
		return false;
	});

	$("#load-modify-form").submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#modify-modal .close").click()
			alertOnResponse(response);
			table.ajax.reload();
			table.draw();
		});
		return false;
	});

	$("#load-retrieve-form").submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#retrieve-modal .close").click()
			alertOnResponse(response);
			table.ajax.reload();
			table.draw();
		});
		return false;
	});

	$('#load-detail-form').submit(function(){

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

	$('#load-add-button').click(function(){
		$('#load-add-form').submit();
	});

	$('#load-modify-button').click(function(){
		$('#load-modify-form').submit();
	});

	$('#load-retrieve-button').click(function(){
		$('#load-retrieve-form').submit();
	});
} );

