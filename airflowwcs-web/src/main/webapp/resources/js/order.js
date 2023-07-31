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
	
	$("#item").on('keyup', function (e) {
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
	
	$("#confirm-ready-button").on("click", function()
	{
		readyOrderRows();		
	}); 
	
	$("#confirm-hold-button").on("click", function(){
		holdOrderRows();	
	}); 
	
	
	$("#confirm-delete-button").on("click", function(){
		deleteOrderRows(); 		
	}); 
	
	var orderTypes = [];
    $.each($(".scOrderType option:selected"), function(){            
    	orderTypes.push($(this).val());
    });
    
	$('#scOrderType :selected').each(function(i, selected) {
		orderTypes[i] = $(selected).text();
	});
	
	/***************************************************************
	 * 					RIGHT CLICK CONTEXT MENU
	 **************************************************************/
	if(isAdmin) //admin specific context menu
	{
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: "Mark Ready", cmd: "ready"},
				{title: "Hold", cmd: "hold" },
				{title: "Delete", cmd: "delete" },
				{title: "----"},
				{title: "Show Details", cmd: "details" },
				{title: "Show Moves", cmd: "moves" },
				], 
				select: function(event, ui) {
	
					var idText = table.row(ui.target).id(); 
					var celltext = ui.target.text();
					switch(ui.cmd){
					case "ready":
						$("#confirm-ready-modal").modal('show');
						//readyOrderRows(); 
						break; 
					case "hold":
						$("#confirm-hold-modal").modal('show');
						//holdOrderRows(); 
						break;
					case "delete":
						$("#confirm-delete-modal").modal('show');
						//deleteOrderRows(); 
						break;
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
	else
	{
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: "Show Details", cmd: "details" },
				{title: "Show Moves", cmd: "moves" },
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

function readyOrderRows()
{
	if(selectedRows.length >0 )
	{
		$.ajax({
		    type : "POST",
		    url : "/airflowwcs/order/markReady",
		    data : {
		        orderIds: selectedRows //notice that "orderIds" matches the value for @RequestParam
		                   
		    },
		    success : function(response) {
		    	$("#confirm-ready-modal").modal('hide'); 
		       alertOnResponse(response); 
		       table.ajax.reload();
		    },
		    error : function(xhr, status, e) {
		    	$("#confirm-ready-modal").modal('hide'); 
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

function holdOrderRows()
{
	if(selectedRows.length>0)
	{
		$.ajax({
		    type : "POST",
		    url : "/airflowwcs/order/markHold",
		    data : {
		        orderIds: selectedRows //notice that "orderIds" matches the value for @RequestParam
		                  
		    },
		    success : function(response) {
		    	$("#confirm-hold-modal").modal('hide'); 
		       alertOnResponse(response); 
		       table.ajax.reload();
		    },
		    error : function(xhr, status, e) {
		    	
		    	$("#confirm-hold-modal").modal('hide'); 
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

function allocateOrderRows()
{
	if(selectedRows.length>0)
	{
		$.ajax({
		    type : "POST",
		    url : "/airflowwcs/order/allocate",
		    data : {
		        orderIds: selectedRows //notice that "orderIds" matches the value for @RequestParam
		                  
		    },
		    success : function(response) {
		    	$("#allocate-confirm-modal").modal('hide');  
		       alertOnResponse(response); 
		       table.ajax.reload();
		    },
		    error : function(xhr, status, e) {
		    	$("#allocate-confirm-modal").modal('hide'); 
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

function deleteOrderRows()
{
	if(selectedRows.length>0)
	{
		$.ajax({
		    type : "POST",
		    url : "/airflowwcs/order/delete",
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
	table.ajax.url('/airflowwcs/order/listSearch?' + $("#order-filtering-form").serialize()).load(function() 
	{
		$("#divExecuting").hide();
	}); 
}	

function onDrillDown(orderId){
	$.post('/airflowwcs/order/find?order='+orderId, $(this).serialize(), function(response){
		for(var key in response){
    		if(response.hasOwnProperty(key)){
    			$("#"+key).html(response[key]);
    			$("#"+key).val(response[key]); 
    		}
    	}
	});
	tabledetail.ajax.url('/airflowwcs/order/listDetail?order='+orderId).load();
	$('#order-detail-modal').modal('show'); 
	setTimeout(tabledetail.draw, 500);
}

function showMoves(orderId){
	tableOrderMove.ajax.url('/airflowwcs/move/listByOrderId/'+orderId).load(); 
	$('#load-moves-modal').modal('show'); 
	setTimeout(tableOrderMove.draw, 500);
}
