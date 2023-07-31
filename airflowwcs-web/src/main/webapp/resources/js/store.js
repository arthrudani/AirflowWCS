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
				if(jsonMessage && jsonMessage.mnMessageID && jsonMessage.msSourceStation) //only if these fields aren't null
				{
					if(jsonMessage.mnMessageID==998){ // general load arrival code
						if(jsonMessage.msSourceStation==$("#station").val())
						{
							checkForLoadAtStation($("#station").val()); 
							alertInfo("ARRIVAL","<b>Load Arrival</b> at station: <b>" + $("#station").val()); 
						}
							
					} 
				}
				
				

				// called back every time the client receives a message from the broker for the destination
				$("#messages").append("<tr><td>" + message.body + "</td><td>"+ getDate() +"</td></tr>");
	});
}

/*
 * Error Callback handler for client.connect()
 */
var connectError = function(error){
	console.log("ERROR: " + error); 
}

/**
 * Establish connection with stomp message broker and subscribe to topic.
 * Uses HTTP->WebSocket endpoint upgrade. 
 * 
 * Automatically subscribes to given topic. 
 * 
 * @returns
 */
function connect() {
	
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
	    $("#messages").append("<tr><td>" + str + "</td><td>"+ getDate() +"</td></tr>");
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


/*********************************************************************
 * 								UI Controller
 **********************************************************************/
/**
 * When message page is ready 
 */
$(document).ready(function(){ // on page load complete
	
	/**
	 * When a new station is selected in the dropdown check for load at station and populate fields
	 */
	$("#station").on('change', function(){
		checkForLoadAtStation(this.value);
	});
	
	/**
	 * Event listener for keyup events in the Expected Receipt 
	 * input 
	 */
	$("#expectedReceipt").on('keyup', function (e) {
	    if (e.keyCode == 13) {
	    	checkForExpectedReceipt($("#expectedReceipt").val()); 
	    	return false; 
	    }
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
	    return false;
	});
	
	/**
	 * Release Load confirm Onclick event listener
	 */
	$('#confirm-release-load-button').click(function(){
		alert('Attemping load Release...');
		$('#release-load-form').submit();
		$("#release-load-modal .close").click(); //close modal
	});
	
	/**
	 * Set releaseLoadButton button's onClick event handler
	 * Check if picks are remaining, if so return error, 
	 * if not bring up confirmation release modal
	 */
	$("#releaseLoadButton").click(function(){ 
		var loadId = $('#loadId').val();
		var stationId = $('#station').val(); 
		alertWarning("USER", "Checking picks remaining | load: " + loadId + " station: " + stationId); 
		//check if picks remaining on load before releasing 
		$.post("/airflowwcs/store/picks?loadId="+loadId+"&station="+stationId, $(this).serialize(), function(response){
			if(response.responseCode==-1) // picks are remaining
			{
				alertError("ERROR", response.message);
			}
			if(response.responseCode==1) // no picks remaining, show release confirm
			{
				$("#loadIdConfirm").val(loadId);
				$("#stationConfirm").val(stationId);
				$("#release-load-modal").modal('show'); 
				
				//TODO - implement amount full query for load id 
			}
		});
	}); 
	
	/**
	 * Connect to stomp endpoint w/ws and subscribe to WebTopic
	 */
	connect(); 
	
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
	alert("Checking for load at Station: " + stationId); 
	$.post('/airflowwcs/store/stationLoad?station='+stationId, $(this).serialize(), function(response){
		if(response.loadId)
		{
			$("#loadId").val(response.loadId);
			$("#container").val(response.containerType);
			$("#releaseLoadButton").prop("disabled",false);
			$("#pickScreenButton").prop("disabled",false);
			$("#expectedReceipt").prop("readonly",false);
			table.ajax.url("/airflowwcs/store/find?loadId="+response.loadId); 
			table.ajax.reload();
		}
		else
		{
			alertError("Warning","No Loads have arrived at station: "+ stationId);
			clearData();
		}

	});
}

function checkForExpectedReceipt(erId)
{
	alert("Checking for expected receipt: " + erId); 
	$.post('/airflowwcs/store/find/erExist?erId='+erId, $(this).serialize(), function(response){
		if(response.responseCode)
		{
			alertOnResponse(response); 
			if(response.responseCode==1)
			{
				getDisplayExpectedReceiptDetails(erId);
			}
		}
	}); 
}

function getDisplayExpectedReceiptDetails(erId) // we shouldn't know about the backend like this but im tired right now. 
{
	alert("Showing details for erId:"+erId); 
	$("#expected-receipt-data").html(""); //clear out what was in there before
	$.post('/airflowwcs/store/find/expectedReceipt?erId='+erId, $(this).serialize(), function(response){

		for(var i = 0; i < response.data.length; i++) {
			var data = response.data[i]; 
			$("#expected-receipt-data").append("<tr><td>"+ data.SITEM +"</td><td>"+data.FEXPECTEDQUANTITY +"</td><td>"+data.SORDERID+"</td></tr>");
		}
		
		$("#formExpectedReceiptId").val(erId);	
		$("#er-store-modal").modal("show"); 
	});
}




/**
 * Clear out data in preperation for next load arrival
 * @returns
 */
function clearData()
{
	$("#loadId").val("");
	$("#container").val("");
	$("#releaseLoadButton").prop("disabled",true);
	$("#pickScreenButton").prop("disabled",true);
	$("#expectedReceipt").prop("readonly",true);
	table.ajax.url("/airflowwcs/store/empty"); 
	table.ajax.reload();
}











