
var autoRefresh = false; 
var isAdmin = false; 

function startAutoRefresh() 
{	 
	tid = setTimeout(table.ajax.reload, 8000); // repeat 
}

function toggleRefresh()
{
	autoRefresh = !autoRefresh;
}


$(document).ready(function() {
	
	var table = $('#ajaxTable').DataTable();
	
	$('#onOffButton').change(function() {
		var Status="";
	    if($('#onOffButton').is(":checked"))
		{
			Status="ON";
		} 
		else{
			Status="OFF";
		}
		$.confirm({
		    title: 'Confirm change all alert status',
		    content: 'Are you sure you want to change the all alert status?',
		    buttons: {
		        confirm: function () {
		    		$("#divExecuting").show();
		    	    $.post(`/airflowwcs/alerts/changeAllStatus?status=`+Status, $(this).serialize(), function(response) { 
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
		
	});
	
	$("#address").on('keyup', function (e) {
	    if (e.keyCode == 13) 
	    {
	    	$("#divExecuting").show(); 
	    	searchAlerts();
	    }
	});
	
	
	$("#searchAlertsButton").click(function() {
		$("#divExecuting").show(); 
		searchAlerts();
		
    });
   

	
	function changeStatus(activeFlag,alertId){
		$.confirm({
		    title: 'Confirm change alert status',
		    content: 'Are you sure you want to change the alert status?',
		    buttons: {
		        confirm: function () {
		    		$("#divExecuting").show();
		    	    $.post('/airflowwcs/alerts/changeStatus?alert='+alertId+'&status='+activeFlag, $(this).serialize(), function(response) { 
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
	
	function searchAlerts()
	{	
		table.ajax.url('/airflowwcs/alerts/listSearch?description='+$("#description").val()).load(function() {
			$("#divExecuting").hide();
		}); 

	}	
	
	/************************************************************
	 * 	 CLEAR MODIFY SCREEN FIELDS
	 ************************************************************/

	
	if(isAdmin) //admin specific context menu
	{
	
	
		//KR: ContextMenu on the main screen
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: "Change alert status", cmd: "changeStatus"}
				], 
				select: function(event, ui) {

					var iId,activeFlag;
					var columnData = table.row(ui.target).data();
					console.log(columnData);
					if (typeof columnData != "undefined") {
						iId = columnData["iId"];
						activeFlag = columnData["Active flag"]; 
					}
			
					console.log("ID"+iId);
					console.log("Active flag"+activeFlag);
					switch(ui.cmd){
					case "changeStatus":
						changeStatus(activeFlag,iId); 
						break; 					
					
					default:
						alert("ERROR: option not recognized."); 
					}
				}
		});
	}else{
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
			
				{title: "Show Details", cmd: "details" },
				{title: "Show Location", cmd: "location" },
				{title: "Show Moves", cmd: "moves" }
				], 
				select: function(event, ui) {

					var idText = table.row(ui.target).id(); 
					switch(ui.cmd){
					case "details":
						showLoadDetails(idText); 
						break;
					case "location":
						showLocation(idText); 
						break;
					case "moves":
						showMoves(idText); 
						break;
					default:
						alert("ERROR: option not recognized."); 
					}
				}
		});
	}

} );

