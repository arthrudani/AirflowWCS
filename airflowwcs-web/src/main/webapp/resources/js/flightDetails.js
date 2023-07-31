 var isAdmin = false; 
 var defaultFlightId = ""; 
 var selectedLoadId;
 var autoRefresh = false; 

/*****************************************************************
 * 						On page ready setup
 ****************************************************************/
 
 function startAutoRefresh() 
{
	// reimplement  	 
	tid = setTimeout(table.ajax.reload, 8000); // repeat 
}

function toggleRefresh()
{
	autoRefresh = !autoRefresh;
}

  $("#searchLoadsButton").click(function() {
		$("#divExecuting").show(); 
		searchLoads();		
  });    

function searchLoads()
{	
	tableFlightDetails.ajax.url('/airflowwcs/flight/listSearch?warehouse='+$("#scwarehouse").val()+"&address="+$("#address").val()+"&deviceId="+$("#scdeviceId").val()+"&loadId="+$("#loadId").val()+"&item="+$("#item").val()).load(function() {
		$("#divExecuting").hide();
	}); 
}

 $(document).ready(function() {

	searchFlightDetailsById();
	var table = $('#ajaxTable').DataTable();

	function searchFlightDetailsById() {
	var fligthId = defaultFlightId;
		tableFlightDetails.ajax.url('/airflowwcs/flight/byFlightId?FlightId='+fligthId).load();
	}
	
	selectedRows = []
	 $(document).contextmenu({
		delegate: ".dataTable td",
		menu: [{title: "Retrieve", cmd: "retrieve" }],
		select: function(event, ui) {	
			
				var idText = tableFlightDetails.row(ui.target).id();		
				var celltext = ui.target.text();	
				selectedLoadId = tableFlightDetails.row(ui.target).id();
				var selectedRows = tableFlightDetails.rows('.selected').length;	
			
				switch(ui.cmd){					
				case "retrieve":
						getRetrieveDest(idText, selectedRows);
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
	
	function getRetrieveDest(trayId, rows)
	{ 
		console.log(rows);
		if(rows === 1)
		{
			if (trayId.length > 0 )
			{
			 	//set the Tray(load) id
				$("#retrieveLoad").val(trayId);
				$('#retrieve-modal').modal('show');
			}
			else
			{
				alert("No Tray Id Selected.");
			}			
		}
		if (rows < 1)
		{
			alert("Select a row to modify");
		}			
	}
   
   $("#load-retrieve-button").click(function() {
		var destSt = $("#retrieveDestination").val();		
		$('#retrieve-modal').modal('hide'); 
		$("#divExecuting").show(); 

		retrieveLoads(destSt);		
    });
    
    function retrieveLoads(destStation)
	{ 		
		$.post('/airflowwcs/flight/retrieveLoads?loadIds='+selectedLoadId+"&destStation="+destStation, $(this).serialize(), function(response)
		{
	    		if(response.responseCode == 1)
	    		{
	    			alertInfo("INFO", response.responseMessage);
	    		}
	    		else
    			{
	    			alert(response.responseMessage);
    			}
	    		$("#divExecuting").hide();
		});
	}
    
});




