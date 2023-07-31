var client = null;
var subscription = null; 
var subscribed = false; 

/************************************************************
 * 			JMS STOMP CONNECTION/MESSAGE HANDLING
 ***********************************************************/
/*
 * Success Callback handler for client.connect()
 */
var connectSuccess = function(){
	alertSuccess("SUCCESS", "Listening for load arrivals!");
	subscription = client.subscribe("jms.topic.WebTopic", function(message)
			{
			/**
			 * Execute this logic everytime we recieve a subscribed message
			 */
				subscribed = true; 

				/**
				 * Parse through recieved message 
				 */
				var jsonMessage = JSON.parse(message.body);
				if(jsonMessage && jsonMessage.Event && jsonMessage.STATIONID) //only if these fields aren't null
				{
					if(jsonMessage.Event=="LoadArrival"){ // general load arrival code
						if(jsonMessage.STATIONID==$("#station").val())
						{
							checkForLoadAtStation($("#station").val()); 
							alertInfo("ARRIVAL","<b>Load Arrival</b> at station: <b>" + $("#station").val()); 
						}
							
					} 
				}
				
				// called back every time the client receives a message from the broker for the destination
				$("#messages").append("<tr style='color:blue; background-color: black;'><td>" + message.body + "</td><td>"+ getDate() +"</td></tr>");
	});
}

/*
 * Error Callback handler for client.connect()
 */
var connectError = function(error){
	alertError("ERROR","ERROR: " + error); 
}



/**
 * Establish connection with stomp message broker and subscribe to topic.
 * Uses HTTP->WebSocket endpoint upgrade. 
 * 
 * Automatically subscribes to given topic. 
 * 
 * @returns
 */
function connectToJMS() {
	
	/* Our message broker's HTTP->WebSocket upgrade end-point 
	 * URL = (http://<hostname>:<port>/<context>/stomp)
	 * @link->com.daifukuamerica.wrxj.core.messaging.WebSocketStompConfig 
	 * TODO -mod this to change w/context */
	var stompEndpointUri= '/airflowwcs/stomp';
	
	/* SockJS connection object for our WebSocket handling. 
	 * Falls back on an HTTP polling implementation if 
	 * WebSocket not available in browser*/
	var sockJS_WS  = SockJS(stompEndpointUri); 
	
	/**
	 * Create a stomp client whose websocket connection will 
	 * be handled by SockJS
	 */
	client = Stomp.over(sockJS_WS); 
	/**
	 * Log debug statements out to message viewer table
	 */
	client.debug = function(str) {
	    $("#messages").prepend("<tr><td>" + str + "</td><td>"+ getDate() +"</td></tr>");
	};
	// Connect anonymous - {} as header | Uses callback handlers
	client.connect("guest","guest", connectSuccess, connectError); 

}

/**
 * Disconnect our active subscription and disconnect stomp client
 * @returns
 */
function disconnect()
{
	if(subscription)
	{
		subscription.unsubscribe();
	}
	if(client)
	{
		client.disconnect(function(){
			$("#messages").append("<tr><td> DISCONNECT CALLED</td><td>"+ getDate() +"</td></tr>");	
		});
	}
}

/*****************************************************************
 * 						UI Controller
 ****************************************************************/

$(document).ready(function(){ 
	
	
	/**
	 * When a new station is selected in the dropdown check for load at station and populate fields
	 */
	$("#station").on('change', function(){
		checkForLoadAtStation(this.value);
	});
	
	
	

	/**
	 * When a user enters a quantity to confirm, enable the pickscreen button 
	 */
	$("#confirmPickQuantity").on('input', function(e){
		$("#pickScreenButton").prop('disabled',false);
	});
	
	/**
	 * Pick Form submission  
	 */
	$("#pick-form").submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			alertOnResponse(response); 
			checkForLoadAtStation($("#station").val()); //get pick information for selected station if available
		}); 
		return false; 
	});
	
	/**
	 * "Confirm Pick" button press
	 */
	$("#pickScreenButton").click(function() {
		  alertWarning( "USER","Completing pick..." );
		  $("#pick-form").submit();
		  
	});
	
	/**
	 * Release Load button click listern action
	 */
	$("#releaseLoadButton").click(function() {
		  var loadId = $("#loadIdConfirm").val(); 
		  var stationId = $("#stationConfirm").val(); 
		  alertWarning("USER", "Releasing load: " + loadId + " from station: " + stationId);
		  //even though technically we shouldn't at this point, check if there are remaining picks for the load
		  $.post("/airflowwcs/store/picks?loadId="+loadId+"&station="+stationId, $(this).serialize(), function(response){
				if(response.responseCode==-1) // picks are remaining
				{
					alertError("ERROR", response.message);
				}
				if(response.responseCode==1) // no picks remaining, show release confirm
				{
					$("#release-load-modal").modal("show"); 
				}
		  });
	});
	
	
	/**
	 * Set form submit function for release-load-form onclick confirm-release-load-button 
	 */
	$('#release-load-form').submit(function(){
	    $.post($(this).attr('action'), $(this).serialize(), function(response) {
		    alertOnResponse(response);
		    if(response.responseCode==1){
		    	clearData(); 
		    }
	    }, 'json');
	    setTimeout(function(){checkForLoadAtStation($("#station").val())},7000);
	    return false;
	});
	
	/**
	 * Release Load confirm Onclick event listener
	 */
	$('#confirm-release-load-button').click(function(){
		alertWarning('USER','Attemping load Release...');
		$('#release-load-form').submit();
		$("#release-load-modal .close").click(); //close modal
	});
	
	connectToJMS();
}); 

/**
 * Check for load marked as arrived at station, if present populate the table and fields 
 * with load data details. 
 * 
 * @param stationId
 * @returns
 */
function checkForLoadAtStation(stationId)
{
	clearData(); //clear out any data from previous actions when checking for new load
//	alert("Checking for load at Station: " + stationId); 
	$.post('/airflowwcs/pick/findLoad?station='+stationId, $(this).serialize(), function(response){
		if(response.loadId)
		{
//			alertSuccess("SUCCESS", "Load: " + response.loadId + " is at Station")

			//set release load form values
			$("#loadIdConfirm").val(response.loadId);
			$("#stationConfirm").val(stationId);
			$("#viewLoadId").val(response.loadId); 
			
			getNextPickForLoad(response.loadId);  // get the next pick for the load at the station
			table.ajax.url("/airflowwcs/pick/findPicks?loadId="+response.loadId); 
			table.ajax.reload();
		}
		else
		{
			alertError("Warning","No Loads have arrived at station: "+ stationId);
			clearData();
		}

	});
}

/**
 * Get the next pick/move record for the loadId and set the values in the form
 * 
 * @param loadId
 * @returns
 */
function getNextPickForLoad(loadId)
{
	clearData(); //clear out any data from previous actions when checking for new load
	$.post('/airflowwcs/pick/findNextPick?loadId='+loadId, $(this).serialize(), function(response){
		if(response)
		{
			$("#confirmPickQuantity").prop('readonly',false); // show the pick quantity input
			
//			alert("Order ID: " + response.orderID + " Item: " + response.item + " QTY: " + response.pickQuantity); 
			$("#orderID").val(response.orderID); 
			$("#location").val(response.address);
			$("#pickFromLoad").val(response.address);
			$("#itemDescription").val(response.item);
			$("#item").val(response.item);
			$("#pickLot").val(response.orderLot);
			$("#subLocation").val(""); // TODO - implement sublocation lookup
			$("#pickQuantity").val(response.pickQuantity);
			$("#parentLoad").val(response.parentLoad);
/*			$("#pickToLoadID").val(response.pickToLoadID);*/
			$("#orderLot").val(response.orderLot);
			$("#schedulerName").val(response.schedulerName);
			$("#routeID").val(response.routeID);
			$("#deviceID").val(response.deviceID);
			$("#releaseToCode").val(response.releaseToCode);
			$("#lineID").val(response.lineID);
			$("#destWarehouse").val(response.destWarehouse);
			$("#nextWarehouse").val(response.nextWarehouse);
			$("#nextAddress").val(response.nextAddress);
			$("#warehouse").val(response.warehouse);
			$("#displayMessage").val(response.displayMessage);
			$("#positionID").val(response.positionID);
/*			$("#moveDate").val(response.moveDate);*/
			$("#moveID").val(response.moveID);
			$("#aisleGroup").val(response.aisleGroup);
			$("#moveSequence").val(response.moveSequence);
			$("#priority").val(response.priority);
			$("#moveType").val(response.moveType);
			$("#moveCategory").val(response.moveCategory);
			$("#moveStatus").val(response.moveStatus);
			
			//These are repeated in case we have no moves we still want the previous calls
			//to hold the data for display 
			/**
			 * release load modal
			 */
			$("#loadIdConfirm").val(loadId);
			$("#stationConfirm").val(response.address);
			/**
			 * View load id input not part of form for pick
			 */
			$("#viewLoadId").val(loadId); 
		}
		else
		{
			/**
			 * View load id input not part of form for pick
			 */
			$("#viewLoadId").val(loadId); 
			alertSuccess("COMPLETE", "No remaining picks on load, you may <b>RELEASE LOAD</b>");
			$("#releaseLoadButton").prop('disabled', false); 
		}
		
	});
}

/**
 * Clear out data in preperation for next load arrival
 * @returns
 */
function clearData()
{
	$("#pick-form").trigger("reset");
	$("#release-load-form").trigger("reset"); 
	$("#pickScreenButton").prop('disabled',true);
	$("#pickScreenButton").prop('disabled',true);
	$("#confirmPickQuantity").prop('readonly',true);
	$("#viewLoadId").val("");
	table.ajax.url("/airflowwcs/store/empty"); 
	table.ajax.reload();
}