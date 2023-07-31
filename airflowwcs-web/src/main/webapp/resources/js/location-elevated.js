/*====================================================*/
/* Extensions to location.js for ROLE_ELEVATED users. */
/* Supports MODIFY operation.                         */
/*====================================================*/
function modifyLocation(id) {
	clearModifyForm();

	$.post('/airflowwcs/location/find?' + rowIdToParam(id), $(this).serialize(), function(response) {
		for (var key in response) {
			console.log(response);
			if (response.hasOwnProperty(key)) {
				$("#" + key + "Mod").val(response[key]);
			}
		}
	});
	$('#modify-modal').modal('show');
}

function clearModifyForm() {
	$("#warehouseMod").val("");
	$("#addressMod").val("");
	$("#shelfPositionMod").val("");
	$("#deviceIdMod").val("");
	$("#zoneMod").val("");
	$("#locationStatusMod").val("");
	$("#emptyFlagMod").val("");
	$("#typeMod").val("");
}
function addContextMenu() {
	$(document).contextmenu({
		delegate: ".dataTable td",
		menu: [{ title: "Flush Lane", cmd: "flushLane" },{ title: "Modify", cmd: "modify" },{ title: "Show Load", cmd: "details" }],
		select: function(event, ui) {
			
			var idText = table.row(ui.target).id();
			console.log(table.row(ui.target).data()["Address"]);
			var addressId = table.row(ui.target).data()["Address"];
			var celltext = ui.target.text();

			switch (ui.cmd) {
				case "modify":
					modifyLocation(idText);
					break;
				case "details":
					onDrillDown(idText);
					break;
				case "noop":
					break;
				case "flushLane":
					flushLoads(addressId);
					break;
				default:
					alert("ERROR: option not recognized.");
			}
		},
		beforeOpen: function(event, ui) {
			//var $menu = ui.menu, $target = ui.target, extraData = ui.extraData;
			//console.log(menu);
			console.log(ui.target);
			if (!ui.target.parent().hasClass('selected'))
				ui.target.click();
			if (table.row(ui.target).data()["Position"] > "000") {
				$(this).contextmenu("replaceMenu", [{ title: "Modify", cmd: "modify" }]);
			}
		}
	});
}
function addMenus() {
	$(document).contextmenu({
		delegate: ".dataTable td",
		menu: [{ title: "Modify", cmd: "modify" },{ title: "Show Load", cmd: "details" }],
		select: function(event, ui) {
			
			var idText = table.row(ui.target).id();
			console.log(table.row(ui.target).data()["Address"]);
			var addressId = table.row(ui.target).data()["Address"];
			var celltext = ui.target.text();

			switch (ui.cmd) {
				case "modify":
					modifyLocation(idText);
					break;
				case "details":
					onDrillDown(idText);
					break;
				case "noop":
					break;
				case "flushLane":
					flushLoads(addressId);
					break;
				default:
					alert("ERROR: option not recognized.");
			}
		},
		beforeOpen: function(event, ui) {
			//var $menu = ui.menu, $target = ui.target, extraData = ui.extraData;
			//console.log(menu);
			console.log(ui.target);
			if (!ui.target.parent().hasClass('selected'))
				ui.target.click();
			if (table.row(ui.target).data()["Position"] > "000") {
				$(this).contextmenu("replaceMenu", [{ title: "Modify", cmd: "modify" }]);
			}
		}
	});
}
function flushLoads(addressId) {
	$.confirm({
		title: 'Confirm Flush Lane',
		content: 'Are you sure you want to Flush Load in this Lane <strong>' + addressId + '</strong>?',
		buttons: {
			confirm: function() {
				$("#divExecuting").show();
				if (addressId.length > 0) {
					$.post('/airflowwcs/location/flushLane?addressId=' + addressId, $(this).serialize(), function(response) {
						$("#divExecuting").hide();
						if (response.responseCode == 1) {
							alertInfo("INFO", response.responseMessage);
						}
						else {
							alertError("ERROR", "Unable to flush the lane: Error " + response.responseMessage);
						}
					});
				}
			},
			cancel: function() {
				//do nothing
			}
		}
	});
}

function addModifySubmitListener() {
	$('#modify-button').click(function() {
		$('#modify-form').submit();
	});
	$("#modify-form").submit(function() {
		$("#divExecuting").show();
		$.post($(this).attr('action'), $(this).serialize())
			.done(function(response) {
				$("#divExecuting").hide();
				$("#modify-modal .close").click()
				console.log(response);
				alertOnResponse(response);
				table.ajax.reload();
				table.draw();
			})
			.fail(function(xhr, status, error) {
				$("#divExecuting").hide();
				$("#modify-modal .close").click()
				alertError("ERROR", "Unable to update location: Error " + xhr.status);
			});
		return false;
	});
}
/* Ready */
$(document).ready(function() {	
			$.ajax({
			    type : "GET",
			    url : "/airflowwcs/location/list",
			    success : function(response) {
			    	console.log(response);	
			    	var length = response.data.length;
			    	var warehouseType = null;
			    	for(var  i=0;i<length;i++){
			    		warehouseType = response.data[i].warehouseType;
			    		
			    		if(warehouseType == 227){
			    			addContextMenu();
			    		}else{
			    			addMenus();
			    		}
			    	}
			       		    	  
			       //table.ajax.reload();
			    },
			    error : function(xhr, status, e) {
			    	$("#divExecuting").hide();
			       alertError('Ajax Error', xhr.responseText);
			       table.ajax.reload();
			    }
			});
	
	addModifySubmitListener();
});

