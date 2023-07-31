var client = null;
var subscription = null; 
var subscribed = false; 

/**
 * Send a message to the JMS Web Topic to be consumed by WebController(s)
 * @param message
 * @returns
 */
function sendMessage(message)
{
	if(client != null){
		client.send("jms.topic.WebTopic", {priority: 2}, message); 
	}
		
}

/**
 * Spoof a JSON JMS Message to be consumed by web controller that will simulate
 * a load arrival event to test control flow of screens. Will not generate any wrx
 * movement/allocation requests or perform any load presence checks at station. 
 *  
 * @param loadId - load to spoof arrival for
 * @param stationId - station that the arrival will be spoofed at
 * @returns
 */
function sendLoadArrival(loadId, stationId)
{
	    var loadArrival = new Object(); 
	    loadArrival.Event = 'loadevent'; 
	    loadArrival.EvtType = 0; 
	    loadArrival.MsgSndr = 'StompJSClientDebug';
	    loadArrival.MsgData = 0;
	    loadArrival.LOADID = loadId; 
	    loadArrival.STATIONID = stationId; 
	   
	    
		sendMessage(JSON.stringify(loadArrival));
}

/**
 * Success callback for client.connectSuccess()
 * 
 * Will subscribe client to JMS queue/WebTopic 
 * 
 * @param stompClient - the stompJS client over websocket
 * @returns
 */
function subscribe(stompClient)
{
	/*if(!subscription)
	{*/
		subscription = stompClient.subscribe("jms.topic.WebTopic", function(message)
		{
			// called back every time the client receives a message from the broker for the destination
			//Debug these messages to the #message slot in the message debug table
			$("#messages").prepend("<tr style='color:white; background-color: DarkRed;'><td>WEB SOCKET</td><td>SOCKET</td><td>" + message + "</td><td>"+ getDate() +"</td></tr>");
			//call generic (page-specific) onMessage so we can handle messages differently between pages
			onMessage(message); 
		});
		subscribed = true;
	/*}*/
}

/*
 * Success callback for client.connect()
 * 
 * Will log connection to console and call subscribe()
 * 
 * TODO - this gets called twice for some reason on every connect?
 */
function connectSuccess()
{
	$("#messages").prepend("<tr style='color:white; background-color: DarkRed;'><td>WEB SOCKET</td><td>SOCKET</td><td>CONNECT SUCCESS CALLBACK</td><td>"+ getDate() +"</td></tr>");
	console.log("Connect Success!");
	subscribe(client); 
	return true; 
}


/**
 * Establish connection with stomp message broker and subscribe to topic 
 * 
 * @returns
 */
function connect() {
	//our message broker's http->ws upgrade endpoint url (http://<hostname>:<port>/<context>/stomp)
	//@link->com.daifukuamerica.wrxj.core.messaging.WebSocketStompConfig TODO -mod this to change w/context 
	var stompEndpointUri= '/airflowwcs/stomp';
	//instantiate SockJS connection object for our websocket handling. 
	//Falls back on an http polling impl if WebSocket not available in browser
	var sockJS_WS  = SockJS(stompEndpointUri); 
	//Create a stomp client whose websocket connection will be handled by SockJS
	client = Stomp.over(sockJS_WS); 
	
	var headers = {
			login: 'WarehouseRx',
			passcode: 'WarehouseRx', 
			'client-id': 'web-client'
	};
	
	/*
	 * Error Callback handler for client.connect()
	 */
	var connectError = function(error){
		console.log("ERROR: " + error); 
	}
	/**
	 * Log debug statements out to message viewer table
	 */
	client.debug = function(str) {
	    $("#messages").prepend("<tr><td>CLIENT</td><td>Debug</td><td>" + str + "</td><td>"+ getDate() +"</td></tr>");
	};
	// Connect anonymous - {} as header | Uses callback handlers
	//client.connect({}, connectSuccess, connectError); 
	client.connect(headers, connectSuccess, connectError); 


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
		subscribed = false; 
	}
	if(client)
	{
		client.disconnect(function(){
			$("#messages").prepend("<tr><td>MESSAGE Debug</td><td>Debug</td><td> DISCONNECT CALLED</td><td>"+ getDate() +"</td></tr>");	
		});
	}
}



/***********************************************************************************************
							CLIENT-SIDE JMS MESSAGE TEMPLATES
************************************************************************************************/
var CONTROL_EVENT_TYPE 					= 0; 
var STATUS_EVENT_TYPE 				    = 1; 
var UPDATE_EVENT_TYPE  					= 2; 
var LOAD_EVENT_TYPE    					= 3; 
var STATION_EVENT_TYPE 					= 4; 
var EQUIPMENT_EVENT_TYPE 				= 5; 
var COMM_EVENT_TYPE    					= 6; 
var EXCEPTION_EVENT_TYPE 				= 7; 
var HEARTBEAT_REQUEST_EVENT_TYPE 		= 8; 
var HEARTBEAT_RESPONSE_EVENT_TYPE 		= 9; 
var SCHEDULER_EVENT_TYPE 				= 10; 
var ORDER_EVENT_TYPE 					= 11; 
var CONTROLLER_REQUEST_EVENT_TYPE 		= 12; 
var ALLOCATE_EVENT_TYPE					= 13; 
var LOG_EVENT_TYPE 						= 15; 
var HOST_MESG_RECV_EVENT_TYPE 			= 16; 
var HOST_MESG_SEND_EVENT_TYPE 			= 17; 
var STATION_LOAD_EVENT_TYPE 			= 18; 
var ALLOCATION_PROBE_EVENT_TYPE 		= 19; 
var CUSTOM_EVENT_TYPE 					= 20; 

/**
 *  Load Event Message
 * 
 * Construct using function initializer to obtain prototype message
 * 
 */
var LoadEventMessage = function(loadId, sourceStation, destinationStation, sourceLocation, sourceEquipWarehouse, destinationLocation,
								destinationEquipWarehouse, results, status, dimensionInfo,groupNumber,reinputFlag,priorityCategory,
								retrievalCommandDetail, barCode, information, errorCode, subID, msgData)
{
	this.msgType = "LoadEvent"; 
	this.eventType = LOAD_EVENT_TYPE; //constant for load event type
	this.loadId = loadId; 
	this.sourceStation = sourceStation; 
	this.destinationStation = destinationStation; 
	this.sourceLocation = sourceLocation; 
	this.sourceEquipWarehouse = sourceEquipWarehouse; 
	this.destinationLocation = destinationLocation; 
	this.destinationEquipWarehouse = destinationEquipWarehouse; 
	this.results = results;
	this.status = status; 
	this.dimensionInfo = dimensionInfo; 
	this.groupNumber = groupNumber; 
	this.reinputFlag = reinputFlag; 
	this.priorityCategory = priorityCategory; 
	this.retrievalCommandDetail = retrievalCommandDetail; 
	this.barCode = barCode; 
	this.information = information; 
	this.errorCode = errorCode; 
	this.subID = subID; 
	this.msgData = msgData; 
}

var ControlEventMessage = function(machineId, station, barcode, controlInfo, mcKey, dimension, loadInfo)
{
	this.msgType = "ControlEvent"; 
	this.eventType = CONTROL_EVENT_TYPE;
	this.machineId = machineId;
	this.station = station; 
	this.barcode = barcode; 
	this.controlInfo = controlInfo; 
	this.mcKey = mcKey; 
	this.dimension = dimension; 
	this.loadInfo = loadInfo; 
}

var GetWeightMessage = function(stationId, loadId)
{
	this.msgType = "GetWeight"; 
	this.eventType = CUSTOM_EVENT_TYPE; //constant for custom event type
	this.stationId = stationId; 
	this.loadId = loadId;
}

var CustomMessage = function(msgData)
{
	this.msgType = "Custom"; 
	this.eventType = CUSTOM_EVENT_TYPE; //constant for custom event type
	this.msgData = msgData; 
}

var HostOutSendMessage = function()
{
	this.msgType = "HostMessageSend";
	this.eventType = HOST_MESG_SEND_EVENT_TYPE;
}




/****************************************************************************
 * 							JMS Message EXAMPLES
 ***************************************************************************/
/**
 * EXAMPLE function for sending a control message
 * 
 */
function sendTestControlEventMessage()
{
	//Fill prototype message fields
	var controlMessage = new ControlMessage(null, 2133, 'dummyBarcodeData123', null, 'dummyMcKey', null, null); 
	
	if(client) //IF the client prototype is instantiated
	{
		client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(controlMessage)); //send the message to the WebTopic
	}else
	{
		console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
	}
}

/**
 * EXAMPLE function for sending a getWeight message
 * 
 */
function sendTestGetWeightMessage()
{
	//Fill prototype message fields
	var getWeightMessage = new GetWeightMessage(2133,1234567); 

	if(client) // IF the client prototype is instantiated
	{
		client.send("jms.topic.WebTopic", {'skipcl': true}, JSON.stringify(getWeightMessage)); //send contructed messasge to the WebTopic
	}else
	{
		console.log("Error - unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
	}
		
}














