
var autoRefresh = false; 
var isAdmin = false; 

function startAutoRefresh() 
{
	// reimplement  	 
	tid = setTimeout(table.ajax.reload, 8000); // repeat 
}

function toggleRefresh()
{
	autoRefresh = !autoRefresh;
}


$(document).ready(function() {
	
	var table = $('#ajaxTable').DataTable();
	
//	table.ajax.success(function() {
//		$("#divExecuting").hide();
//	});

	$("#address").on('keyup', function (e) {
	    if (e.keyCode == 13) 
	    {
	    	$("#divExecuting").show(); 
	    	searchLoads();
	    }
	});
	
	
	$("#searchAlertsButton").click(function() {
		$("#divExecuting").show(); 
		//$('#load-filtering-form').submit()
		searchLoads();
		
    });
   
   
    /**
	 * Context action popups. 
	 * shows Add Load form
	 **/
    function showAddLoad()
    {
    	//show the add load dialog box
		$('#add-modal').modal('show');
    }
	/**
	 * Context action popups. 
	 * @param loadId
	 *
	function showModifyLoad(loadId)
	{ 
	
		clearModifyForm();
		$.post('/airflowwcs/load/find?load='+loadId, $(this).serialize(), function(response){
		
			for(var key in response){
	    		if(response.hasOwnProperty(key)){
	    			$("#"+key+"Mod").html(response[key]);
	    			$("#"+key+"Mod").val(response[key]);
	    			$("#" +key+ " option[value='" + response[key] + "']").prop('selected', true);

	    		}
	    	}
		});
		$('#modify-modal').modal('show'); 
	}*/
	

	
	/*function deleteAlert(alertId){
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
	}*/
	
	function searchLoads()
	{	
		table.ajax.url('/airflowwcs/alerts/listSearch?description='+$("#description").val()).load(function() {
			$("#divExecuting").hide();
		}); 

	}	
	
	/************************************************************
	 * 	 CLEAR MODIFY SCREEN FIELDS
	 ************************************************************/
	/*
	
	function clearModifyForm()
	{
		$('#description').html('');
	}
	
	if(isAdmin) //admin specific context menu
	{
	
	
		//KR: ContextMenu on the main screen
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: "Add Load", cmd: "addLoad"},
				{title: "Modify Load", cmd: "modify"},
//				{title: "Mark Load QCHOLD", cmd: "qchold"},
//				{title: "Mark Load AVAILABLE", cmd: "avail"},
				{title: "Delete Load", cmd: "delete" },
				{title: "----"},
				{title: "Show Details", cmd: "details" },
				{title: "Show Location", cmd: "location" },
				{title: "Show Moves", cmd: "moves" },
				{title: "----"},
				{title: "Retrieve", cmd: "retrieve" }
				//{title: "Make Oldest", cmd: "oldest" }
				], 
				select: function(event, ui) {

					var idText = table.row(ui.target).id(); 
					var celltext = ui.target.text();
					switch(ui.cmd){
					case "addLoad":
						showAddLoad();
						break;
					case "modify":
						showModifyLoad(idText); 
						break; 					
					case "qchold":
						markLoadQCHOLD(idText); 
						break; 
					case "avail":
						markLoadAVAIL(idText); 
						break; 
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
					case "retrieve":
						showRetrieveLoad(idText);
						//getRetrieveDest(idText);
						break; 
					case "oldest": 
						makeOldest(idText); 
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
					var celltext = ui.target.text();
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
				},
				beforeOpen: function(event, ui) {
					var $menu = ui.menu,
					$target = ui.target,
					extraData = ui.extraData;

				}
		});
	}
	

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
	
	$('#load-add-button').click(function(){
		$('#load-add-form').submit();
	});
	
	$('#load-modify-button').click(function(){
		$('#load-modify-form').submit();
	});
	
	*/

} );

