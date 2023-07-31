var isAdmin = false; 

$(document).ready(function(){ 
	var table = $('#ajaxTable').DataTable();
	
	$("#scOrderId").on('keyup', function (e) {
	    if (e.keyCode == 13) 
	    {
	    	$("#divExecuting").show(); 
	    	searchOrders();
	    }
	});
	
	$("#scLoadId").on('keyup', function (e) {
	    if (e.keyCode == 13) 
	    {
	    	$("#divExecuting").show(); 
	    	searchOrders();
	    }
	});
	
	$("#scItem").on('keyup', function (e) {
	    if (e.keyCode == 13) 
	    {
	    	$("#divExecuting").show(); 
	    	searchOrders();
	    }
	});
	
	$("#searchOrdersButton").click(function() {
		$("#divExecuting").show(); 
		searchOrders();		
    });
	
	$('#ajaxTable tbody').on('click', 'tr', function(){
		var data = table.row(this).id(); 
		if(selectedRows.length<=1){
			$('#orderIdDisplay').val(data); 
		}else{
			$('#orderIdDisplay').val("MULTIPLE")
		}
	});
	
	/***************************************************************
	 * 					RIGHT CLICK CONTEXT MENU
	 **************************************************************/
	if(isAdmin) //admin specific context menu
	{
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: "Delete", cmd: "delete" },
				{title: "----"},
				{title: "Show Details", cmd: "details" },
				], 
				select: function(event, ui) {
	
					var idText = table.row(ui.target).id(); 
					var celltext = ui.target.text();
					switch(ui.cmd){
					case "delete":
						$("#confirm-delete-modal").modal('show');
						deleteOrderRows(); 
						break;
					case "details":
						onDrillDown(idText); 
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
	else
	{
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: "Show Details", cmd: "details" },
				], 
				select: function(event, ui) {
	
					var idText = table.row(ui.target).id(); 
					var celltext = ui.target.text();
					switch(ui.cmd){
					case "details":
						onDrillDown(idText); 
						break;
					case "moves":
						showMoves(idText); 
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
	/***************************************************************
	 * 					END RIGHT CLICK CONTEXT MENU
	 **************************************************************/	
	
	/* $("div#toolbar-buttons").append('<label class="checkbox-inline"><span class="label label-default">Auto Refresh</span><input id="auto_refresh_toggle" type="checkbox" name="auto_refresh_toggle" checked></label>');    
	 $("[name='auto_refresh_toggle']").bootstrapSwitch(); 
	 
	 $("#auto_refresh_toggle").on("click", function(){ // starts as true, toggle on click of animated toggle TODO - come back to see why sometimes click during animation yields button ui displaying wrong condition
		 	refreshEnabled =!refreshEnabled; 
	 }); */
	
}); 


function deleteOrderRows()
{
	if(selectedRows.length>0)
	{
		$.ajax({
		    type : "POST",
		    url : "/airflowwcs/expected/delete",
		    data : {
		        orderIds: selectedRows //notice that "orderIds" matches the value for @RequestParam
		                  
		    },
		    success : function(response) {
		    	$("#confirm-delete-modal").modal('hide');  
		       alertOnResponse(response); 
		       table.ajax.reload();
		    },
		    error : function(xhr, status, e) {
		    	$("#confirm-delete-modal").modal('hide'); 
		       alertError('Ajax Error', xhr.responseText);
		       table.ajax.reload();
		    }
		});
	}
	else
	{
		alertError("Selection","No rows selected! Select a row and retry.")
	}
}

function searchOrders()
{
	table.ajax.url('/airflowwcs/expected/listSearch?' + $("#order-filtering-form").serialize()).load(function() 
	{
		$("#divExecuting").hide();
	}); 

}	

function onDrillDown(orderId){
	$.post('/airflowwcs/expected/find?order='+orderId, $(this).serialize(), function(response){
		console.log(response); 
		for(var key in response){
    		if(response.hasOwnProperty(key)){
    			$("#"+key).html(response[key]);
    			$("#"+key).val(response[key]); 
    		}
    	}
	});
	tabledetail.ajax.url('/airflowwcs/expected/listDetail?order='+orderId).load();
	$('#order-detail-modal').modal('show'); 
	setTimeout(tabledetail.ajax.reload, 500); // TODO - KLUDGE figure out why the styling is not responding on first load (misaligned columns) 
}

