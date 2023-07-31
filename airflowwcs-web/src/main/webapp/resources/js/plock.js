/**
 * Page specific globals
 */
var isLoadAtToStation = false; 
var isStationLocked = false; 

var subscription = null; 
var subscribed = false; 
var count = 0;
var loadArrived = true;
var qaDescription = "";

/************************************************************
 * 			JMS STOMP CONNECTION/MESSAGE HANDLING
 ***********************************************************/
/*
 * Success Callback handler for client.connect()
 */
var connectSuccess = function(){
	if (count < 1)
	{
	count++;
	alertSuccess("SUCCESS", "Listening for load arrivals and scale events!");
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
				if(jsonMessage && jsonMessage.mnMessageID) //only if these fields aren't null
				{
					if(jsonMessage.mnMessageID==1){ // general load arrival code
						if(jsonMessage.msDestinationStation==$("#station").val())
						{							
							alertInfo("ARRIVAL","<b>Load Arrival</b> at station: <b>" + $("#station").val());
							refreshPickData(); 
							updatePickTables(); 
						}
							
					} 
					if(jsonMessage.mnMessageID==2){ // general Scale Event
						if(jsonMessage.msStation==$("#pickToStationId").val())
						{
							alertInfo("SCALE","<b>Load Weight Check at station: <b>" + $("#pickToStationId").val() +
									"Status: " + jsonMessage.sDataString); 
						}
						if(jsonMessage.msStation==$("#station").val())
						{
							alertInfo("SCALE","<b>Load Weight Check at station: <b>" + $("#station").val() +
									" | Status: " + jsonMessage.sDataString); 
						}
							
					}					
				}
				
				// called back every time the client receives a message from the broker for the destination
				$("#messages").append("<tr style='color:blue; background-color: black;'><td>" + message.body + "</td><td>"+ getDate() +"</td></tr>");
	});
	}
	else
	{
		count = 0;
	}
}

/*
 * Error Callback handler for client.connect()
 */
var connectError = function(error){
	alertError("ERROR","ERROR: " + error); 
	connect();
}


/*****************************************************************
 * 						On page ready setup
 ****************************************************************/
$(document).ready(function(){ 
	
	/**
	 * Disable some buttons when the page is ready, since we cannot inline these in the jsp
	 */
	$('#return-pick-from-load-button').prop('disabled', true);
	$("#complete-pick-to-load-button").prop('disabled', true); 
	$("#confirm-plock-pick-button").prop('disabled', true); 
	$("#markqa-button").prop('disabled',true);
	
	
	
	/**
	 * When a new station is selected in the dropdown check for load at station and populate fields
	 */
	$("#station").on('change', function()
	{
		clearToStationForm();
		refreshPickData();
	    promptClientStationLock($("#station").val()); 
		updatePickTables(); 
	});
	
	/**
	 * When enter key pressed/carriage return sent by RF ->
	 * in To-load "board id" input ->
	 * attempt to create a load with the given board id. 
	 */
	$("#pickToBoardId").on('keyup', function (e) {
	    if (e.keyCode == 13) {
	    	$("#pickToBoardId").val($("#pickToBoardId").val().toUpperCase());
	    	validateToBoard($("#pickToBoardId").val()); 
	    }
	});
	
	/**
	 * When enter key pressed/carriage return sent by RF ->
	 * in "scanned item" input ->
	 * check the scanned item to validate
	 */
	$("#scannedItem").on('keyup', function (e) {
	    if (e.keyCode == 13) {
	    	if($('#scannedItem').val()!='') //makes sure field is not empty before validating
	    		validateScannedItem($("#scannedItem").val()); 
	    }
	});

	/**
	 * When enter key pressed/carriage return sent by RF ->
	 * in "Quantity" input ->
	 * validate the quantity of the item 
	 */
	$("#pickedQty").on('keyup', function (e) {
	    if (e.keyCode == 13) {
	    	validateItemPickQuantity($("#pickedQty").val()); 
	    }
	});

	$('#managerId').on('keyup', function (e) {
	    if (e.keyCode == 13) {
	    	if($('#managerId').val()!='') //makes sure field is not empty before validating
	    		$( "#managerPwd" ).focus(); 
	    	else
	    		alert("ID cannot be blank");
	    }
	});
	
	$('#managerPwd').on('keyup', function (e) {
	    if (e.keyCode == 13) {
	    	if($('#managerPwd').val()=='') //makes sure field is not empty before validating
	    		alert("Password cannot be blank");
	    }
	});
	
	$("#damagedArt").on('click', function(){
		qaDescription = "Damaged Articles on Pallet";
	}); 
	
	$("#wrongArt").on('click', function(){
		qaDescription = "Wrong Articles on Pallet";
	}); 
	/**
	 * On click of tabs in bottom area, refresh tables. 
	 */
	$("#pickFromListTabToggle").on('click', function(){
		// PICK-FROM-LIST
		tableFromMove1.ajax.reload(); 
	}); 
	$("#pickToTabToggle").on('click', function(){
		// PICK TO LOAD LINE ITEM DETAIL
		tableToLoadItem2.ajax.reload(); 
	}); 
	$("#pickFromItemTabToggle").on('click', function(){
		// PICK FROM LOAD LINE ITEM DETAIL
		tableFromLoadItem3.ajax.reload(); 
	}); 
	$("#orderLinePickTabToggle").on('click', function(){
		// PICK FROM LOAD LINE ITEM DETAIL
		tableOrderLineDetail.ajax.reload(); 
	}); 
	
	/**
	 * When we click the "YES" button for the Lock Client to Station popovers
	 */
	$("#confirm-client-lock-button").on('click', function(){
		$.post("/airflowwcs/plock/lockStation?station="+$("#station").val()+"&ipAddress="+$("#ipAddress").val(), function(response){
			alertOnResponse(response); 
			if(response.responseCode==1)
			{
				$('#lock-status').removeClass('fa-unlock').addClass('fa-lock');
				$('#station').prop('disabled', true); 
			}
		});
		$('#confirm-client-lock').modal('hide'); 
		
		var pickToBoard =$.trim($("#pickToBoardId").val());
		var pickItem =$.trim($("#pickFromItemId").val());

		if(pickToBoard.length>0 && pickItem.length>0) // if we have a board id && item for item pick for the to load, refocus on the item scan field. 
		{
			$( "#scannedItem" ).focus();
		}
		else if($('#pickToBoardId').is('[readonly="false"]'))
		{
			$( "#pickToBoardId" ).focus();
		}else{
			$("#return-pick-from-load-button").focus(); 
		}
	});	
	
	$("#deny-client-lock-button").on('click', function(){
		$('#station').val(''); 
		$('#pickToStationId').val(''); 
		clearPickForm(); 
		clearToStationForm(); 
		clearFromStationForm(); 
		updatePickTables(); 
	}); 

	
	/***********************************************************
	 * 					REFRESH BUTTON LISTENERS
	 ***********************************************************/
	$("#stations-refresh-button").on('click', function(){
		refreshPickData(); 
		updatePickTables(); 
	});
	$("#from-station-refresh-button").on('click', function(){
		checkForLoadAtFromStation($("#station").val()); 
	});
	$("#to-station-refresh-button").on('click', function(){
		checkForLoadAtToStation($("#pickToStationId").val()); 
	});
	
	/************************************************************
	 * 					COMPLETE ITEM PICK
	 * Confirm Pick button pressed, submit the spring form
	 ************************************************************/
	$("#confirm-plock-pick-button").on('click', function(){

		   $("#plock-form").submit(); 
	});
	
	$("#exception-ok-button").on('click', function(){
		$('#plock-exception-pick').modal('hide');
		 
		if ($('#shortRadio').prop('checked'))
		{
			shortPick();
		}
		else if ($('#reallocateRadio').prop('checked'))
		{
			reallocatePick()
		}
		else if ($('#damagedRadio').prop('checked'))
		{
			damagedPick()
		}
		else
		{
		   alertWarning("WARN", "No Exception Pick Option Selected")
		}
	}); 
	
	$("#manager-approve-button").on('click', function(){
    	if($('#managerPwd').val()=='') //makes sure field is not empty before validating
    		alert("Password cannot be blank");
    	else if ($('#managerId').val()=='')
    		alert("ID cannot be blank");
    	else
    		getManagerApproval($('#managerId').val(), $('#managerPwd').val(), $("#pickedQty").val())		 
	}); 
	
	$("#qareason-submit-button").on('click', function(){
		 $('#plock-qareason').modal('hide');	
    	 markPalletQA(getFromLoadId(), qaDescription)	
	}); 
	
	$("#markqa-button").on('click', function(){
		$('#plock-qareason').modal('show');		 
	}); 
	
	/**
	 * Pick Form submission  
	 */
	$("#plock-form").submit(function(){
		
		$("#divExecuting").show();

		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#divExecuting").hide();
			alertOnResponse(response); 
			if(response.responseCode==1)
			{
				var hostSendOutMessage = new HostOutSendMessage(); 
				
				if(client) //IF the client prototype is instantiated
				{
					client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(hostSendOutMessage)); //send the message to the WebTopic
					console.log("PlockPick: Pick Complete. Sending HostMesgSend Event | Message: " + hostSendOutMessage); 
				}else
				{
					console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
				}
				if (getCheckWeight()==1)
			    {
					var scaleMessage = new GetWeightMessage(getToStationId(), getPickToPalletId());			
					if(client) //IF the client prototype is instantiated
					{
						client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(scaleMessage)); //send the message to the WebTopic
						console.log("ScaleWeight: JMS Message sent | Message: " + scaleMessage); 
					}else
					{
						console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
					}
			    }
				clearPickForm(); 
				refreshPickData();
				updatePickTables(); 
				setTimeout(delayCheckNextPick,1200);  
			}
			else if(response.responseCode==ERROR) 
			{
				$('#scannedItem').val(''); 
				$('#scannedItem').prop('readonly',false); 
				$('#pickedQty').val(0); 
				$('#scannedItem').focus(); 
			}
			if($('#pickFromItemId').val()=="")
				$('#return-pick-from-load-button').focus(); 
			
			
		}); 
		return false; 
	});
	/************************************************************
	 * 					END COMPLETE ITEM PICK
	 ************************************************************/
	
	
	/************************************************************
	 * 					LOAD RELEASE BUTTONS
	 ************************************************************/
	/**
	 * Release Pick-From load button pressed
	 */
	$("#return-pick-from-load-button").click(function(){
		$("#divExecuting").show();
		$.post("/airflowwcs/plock/releaseFromLoad?fromLoadId="+$("#pickFromLoadPalletId").val()+"&fromBoardId="+$("#pickFromBoardId").val()
				+"&stationId="+$("#station").val()+"&orderId="+$("#pickFromPlockOrderId").val(), $(this).serialize(), function(response){
			$("#divExecuting").hide();
			
			if(response.responseCode==1)
			{
				alertOnResponse(response); 
				releaseFromPallet();
			}
			else if (response.responseCode==-2) //Empty Pallet
			{
				alert(response.responseMessage);
				alertOnResponse(response);
				releaseFromPallet();
			}
			else
			{
				alertOnResponse(response);
			}
		});
	});
	
	/**
	 * Release Pick-To load button pressed
	 */
	$("#complete-pick-to-load-button").click(function(){
		$("#divExecuting").show();
		$.post("/airflowwcs/plock/releaseToLoad?loadId="+$("#pickToLoadPalletId").val()
				+"&stationId="+$("#pickToStationId").val()+"&boardId="+$("#pickToBoardId").val(), $(this).serialize(), function(response){
			$("#divExecuting").hide();
			alertOnResponse(response); 
			if(response.responseCode==1)
			{
				var loadMessage = new LoadEventMessage(null, $("#pickToStationId").val(), null, null, null, null,
						null, null, null, null, null, null, null, null, $("#pickToBoardId").val(), null, null, null, null)	
				

					if(client) //IF the client prototype is instantiated
					{
						client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(loadMessage)); //send the message to the WebTopic
						console.log("PlockPick: JMS Message sent | Message: " + loadMessage); 
					}else
					{
						console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
					}

				/**clearPickForm(); **/
				clearToStationForm(); 
				checkForLoadAtFromStation($("#station").val()); 
				$("#complete-pick-to-load-button").prop("disabled",true);
			}
		});
	});
	
	/************************************************************
	 * 					END LOAD RELEASE BUTTONS
	 ************************************************************/
	
	
	
	/*************************************************************
	 * 				CONNECT TO JMS MESSENGER
	 ************************************************************/
	
	connect();
	
	/*************************************************************
	 * 				END JMS MESSENGER
	 ************************************************************/	
	
}); 

/*******************************************************************************************
 * 									On page load setup 
 *******************************************************************************************/
$(document).load(function(){
	if(subscription)
	{
		alertSuccess('CONNECTED', 'Listening for Load Arrival Events, able to publish Get Weight requests.');
	}
}); 

/**
 * Check for load marked as arrived at station, if present populate the table and fields 
 * with load data details. 
 * 
 * @param stationId
 * @returns
 */
function checkForLoadAtFromStation(stationId)
{
	$.ajax({
        url: '/airflowwcs/plock/findLoad?station='+stationId,
        type: 'POST',
        async: false,
        cache: false,
        timeout: 30000,
        error: function(){
        	alert("ERROR: Station/Load lookup request, response empty.");
            return true;
        },
        success: function(response)
        { 
        	if(response){
    			if(response.loadId)
    			{
    				$("#pickFromLoadPalletId").val(response.loadId); 
    				$("#pickFromBoardId").val(response.boardId);
    				if($("#pickToBoardId").val()=='')
    				{
    					$("#pickToBoardId").prop('readonly', false);
        				$( "#pickToBoardId" ).focus();
    				}
    				$("#markqa-button").prop('disabled',false);
    				$("#return-pick-from-load-button").prop("disabled",false);
    			}
    			else
    			{
    				alertInfo("Warning","NO Load at FROM-station: "+ stationId);
    				clearPickForm();
    				clearFromStationForm(); 
    			}
    		}else
    		{
    			alert("ERROR: Unable to perform Station/Load lookup request, response empty.");
    		}
        }
    });

}

function checkForLoadAtToStation(toStation)
{
	$.ajax({
        url: '/airflowwcs/plock/findLoad?station='+ toStation,
        type: 'POST',
        async: false,
        cache: false,
        timeout: 30000,
        error: function(){
        	alert("ERROR: Unable to retrieve station record for station: <b>" + toStation + "</b>");
            return true;
        },
        success: function(response){ 
        	if(response.loadId)
    		{
    			
    			isLoadAtToStation = true; 
    			$("#pickToLoadPalletId").val(response.loadId);
    			$("#pickToBoardId").val(response.boardId); 
    			$("#pickToBoardId").prop('readonly', true);
    			$("#scannedItem").prop('readonly', false); 
    			$("#scannedItem").focus(); 
    			$("#picks-remaining-container").toggleClass("has-error");
    			$("#pick-item-container").toggleClass("has-success");
    			$("#pick-item-description-container").toggleClass("has-success");
    			$("#complete-pick-to-load-button").prop('disabled',false); 
    			
    			/** CREATE TO-LOAD FOCUS IF PALLET NOT CREATED AT TO STATION **/
    			if(getPickToPalletId()==""){//if we dont have a created pallet id for to load
    				if(!$("#pickToLoadPalletId").hasClass("has-error"))
    					$("#pickToLoadPalletId").toggleClass("has-error");
    				$("#pickToBoardId").focus(); 
    			}
    		}
    		else
    		{
    			alertInfo("Warning","NO Load at TO-station: "+ $('#pickToStationId').val());
    			$("#pickToBoardId").prop('readonly', false); 
    		}
        }
    });
	
	
}

function updatePickTables()
{
	
	// PICK-FROM-LIST
	tableFromMove1.ajax.url('/airflowwcs/move/listByPlockStation/'+getToStationId()).load(); 
	// PICK TO LOAD LINE ITEM DETAIL
	tableToLoadItem2.ajax.url('/airflowwcs/itemdetail/byload/'+getToLoadId()).load(); 
	// PICK FROM LOAD LINE ITEM DETAIL
	tableFromLoadItem3.ajax.url('/airflowwcs/itemdetail/byload/'+getFromLoadId()).load(); 
	// PICK FROM ORDER 
	tableOrderLineDetail.ajax.url('/airflowwcs/order/listDetail?order='+$("#pickToPlockOrderId").val()).load(); 
}

function clearPickFromLoad()
{
	$('#pickFromBoardId').val(''); 
	$('#pickFromLoadPalletId').val(''); 
}

/**
 * Popover the confirmation module for locking the client to the station 
 * 
 * @param station
 * @returns
 */
function promptClientStationLock(station)
{
//	$('#confirm-client-lock').modal('show'); 
//	alertInfo("LOCK","Locking Client to Plock Station " + $("#station").val());	
	$.post("/airflowwcs/plock/lockStation?station="+$("#station").val()+"&ipAddress="+$("#ipAddress").val(), function(response){
		alertOnResponse(response); 
		if(response.responseCode==1)
		{
			$('#lock-status').removeClass('fa-unlock').addClass('fa-lock');
			//$('#station').prop('disabled', true); 
		}
	});
	//$('#confirm-client-lock').modal('hide'); 
	
	var pickToBoard =$.trim($("#pickToBoardId").val());
	var pickItem =$.trim($("#pickFromItemId").val());

	if(pickToBoard.length>0 && pickItem.length>0) // if we have a board id && item for item pick for the to load, refocus on the item scan field. 
	{
		$( "#scannedItem" ).focus();
	}
	else if($('#pickToBoardId').is('[readonly="false"]'))
	{
		$( "#pickToBoardId" ).focus();
	}else{
		$("#return-pick-from-load-button").focus(); 
	}
}

function getConnectedToStationData(fromStation)
{
	$.ajax({
        url: '/airflowwcs/plock/findToStationData?station='+fromStation,
        type: 'POST',
        async: false,
        cache: false,
        timeout: 30000,
        error: function(){
        	alert("ERROR: Unable to retrieve station record for station: <b>" + station + "</b>");
            return true;
        },
        success: function(response){ 
        	if(response)
    		{
    			$('#pickToPlockOrderId').val(response.plockOrderId);
    			$('#pickToStationId').val(response.stationId);
    			return response;
    		}
    		else
    		{
    			alert("ERROR: Unable to retrieve station record for station: <b>" + station + "</b>"); 
    		}
        }
    });

}

function alertLoadArrival()
{
	alertWarning("ARRIVAL", "<b>LOAD ARRIVAL</b> at station <b>"+$('#station').val()+"</b>"); 
}

function findNextPick(loadId)
{
	$.post("/airflowwcs/plock/findNextPick?loadId="+loadId, $(this).serialize(), function(response){
		if(response)
		{
			alertSuccess("Moves Detected ", "Load: " + loadId + " has pending item pick move: " + response.moveID); 
			$('#pickFromPlockOrderId').val(response.orderID); 
			$('#pickFromPlockOrderLineId').val(response.lineID);
			$('#pickFromItemId').val(response.item); 
			$('#pickFromItemArticle').val(response.item.slice(0,8)); // manually strip article from item id (don't want another roundtrip)
			$('#pickFromItemSupplier').val(response.item.slice(8,13)); // manually strip supplier from item id (don't want another roundtrip)
			$('#pickFromItemDivision').val(response.item.slice(13)); // manually strip division from item id (don't want another roundtrip)
			$('#totalRemaining').val(response.pickQuantity); 
			$('#pickFromItemDescription').val(response.itemDescription); 
			$('#carrierId').val(response.carrier); 
			$('#totalAllocated').val(response.allocated);
			$('#totalRemaining').val(response.remaining); 
			$('#totalPicked').val(response.picked); 
		}
		else
		{
			alertInfo("NO PICKS", "NO PICKS FOUND ON FROM-LOAD: " + loadId);
			$("#pickToBoardId").prop('readonly', true); 
			if($("#pickFromBoardId").val()!='')
			{
				$("#return-pick-from-load-button").prop('disabled',false); 
				$("#return-pick-from-load-button").removeClass('btn-warning').addClass('btn-success'); 
			}
			
		}
	});
}

function validateToBoard(boardId)
{
	$.post("/airflowwcs/plock/isValidBoardId?boardId="+boardId, $(this).serialize(), function(response){
		
		if(response.responseCode==1)
		{
			alertOnResponse(response); 
			createPickToLoad(boardId, $('#pickToStationId').val()); 
		}
		else
		{
		   alert(response.responseMessage);
		   alertOnResponse(response);
		   $('#pickToBoardId').select();
		}
	});
}

function validateScannedItem(itemId)
{
	
	$.post("/airflowwcs/plock/isValidItemId?itemId="+itemId
										+"&loadId="+$("#pickFromLoadPalletId").val() 
										+"&orderId=" +$("#pickFromPlockOrderId").val()
										+"&lineId=" +$("#pickFromPlockOrderLineId").val() 
										+"&articleId=" +$("#pickFromItemArticle").val()
										+"&supplierId=" +$("#pickFromItemSupplier").val(),$(this).serialize(), function(response){
		 
		if(response.responseCode==1)
		{
			alertOnResponse(response);
			$("#scannedItem").val(itemId);
			$("#scannedItem").prop('readonly', true);
			$("#pickedQty").prop('readonly', false);
			$("#pickedQty").select(); 
		}
		else
		{
		    alert(response.responseMessage);
		    alertOnResponse(response);
		    $("#scannedItem").select();
		}
	});
}

function validateItemPickQuantity(quantity)
{
	$.post("/airflowwcs/plock/isValidItemQty?loadId="+$("#pickFromLoadPalletId").val()
										+"&orderId=" +$("#pickFromPlockOrderId").val()
										+"&itemId="+$("#pickFromItemArticle").val()+$("#pickFromItemSupplier").val()+$("#pickFromItemDivision").val()
										+"&lineId=" +$("#pickFromPlockOrderLineId").val()
										+"&qty="+quantity, 
										$(this).serialize(), function(response){
		if(response.responseCode==1)
		{
			$("#pickedQty").val(quantity); 
			$("#pickedQty").prop('readonly', true);
			$("#confirm-plock-pick-button").prop('disabled',false); 
			$("#confirm-plock-pick-button").focus(); 
		}
		else if (response.responseCode==2)
		{
			$('#plock-exception-pick').modal('show');
		}
		else if (response.responseCode==3)
		{
			 $('#managerId').val(''); 
			 $('#managerPwd').val(''); 
			$('#plock-approval-pick').modal('show');
		}
		else
		{
			alert(response.responseMessage);
			alertOnResponse(response);
			$("#pickedQty").select();
		}
	});
}

function shortPick()
{
	$("#divExecuting").show();
	$.post("/airflowwcs/plock/shortItemPick", $("#plock-form").serialize(), function(response){	
		$("#divExecuting").hide();
		if(response.responseCode==1)
		{
			alertOnResponse(response);
			var hostSendOutMessage = new HostOutSendMessage(); 
			
			if(client) //IF the client prototype is instantiated
			{
				client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(hostSendOutMessage)); //send the message to the WebTopic
				console.log("PlockPick: SHORT Pick Complete. Sending HostMesgSend Event | Message: " + hostSendOutMessage); 
			}else
			{
				console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
			}
			if (getCheckWeight()==1)
		    {
				var scaleMessage = new GetWeightMessage(getToStationId(), getPickToPalletId());			
				if(client) //IF the client prototype is instantiated
				{
					client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(scaleMessage)); //send the message to the WebTopic
					console.log("ScaleWeight: JMS Message sent | Message: " + scaleMessage); 
				}else
				{
					console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
				}
		    }
			clearPickForm(); 
			refreshPickData();
			updatePickTables(); 
			setTimeout(delayCheckNextPick,1200);  
		}
		else if(response.responseCode==ERROR) 
		{
			alert(response.message);
			alertOnResponse(response);
			$('#scannedItem').val(''); 
			$('#scannedItem').prop('readonly',false); 
			$('#pickedQty').val(0); 
			$('#scannedItem').select(); 
		}
		if($('#pickFromItemId').val()=="")
			$('#return-pick-from-load-button').focus(); 
		
		
	});
}
function damagedPick()
{
	$("#divExecuting").show();
	$.post("/airflowwcs/plock/damageItemPick", $("#plock-form").serialize(), function(response){	
		$("#divExecuting").hide();
		if(response.responseCode==1)
		{
			alertOnResponse(response);
			var hostSendOutMessage = new HostOutSendMessage(); 
			
			if(client) //IF the client prototype is instantiated
			{
				client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(hostSendOutMessage)); //send the message to the WebTopic
				console.log("PlockPick: SHORT Pick Complete. Sending HostMesgSend Event | Message: " + hostSendOutMessage); 
			}else
			{
				console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
			}
			if (getCheckWeight()==1)
		    {
				var scaleMessage = new GetWeightMessage(getToStationId(), getPickToPalletId());			
				if(client) //IF the client prototype is instantiated
				{
					client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(scaleMessage)); //send the message to the WebTopic
					console.log("ScaleWeight: JMS Message sent | Message: " + scaleMessage); 
				}else
				{
					console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
				}
		    }
			clearPickForm(); 
			refreshPickData();
			updatePickTables(); 
			setTimeout(delayCheckNextPick,1200);  
		}
		else if(response.responseCode==ERROR) 
		{
			alert(response.message);
			alertOnResponse(response);
			$('#scannedItem').val(''); 
			$('#scannedItem').prop('readonly',false); 
			$('#pickedQty').val(0); 
			$('#scannedItem').select(); 
		}
		if($('#pickFromItemId').val()=="")
			$('#return-pick-from-load-button').focus(); 
		
		
	});
}
function reallocatePick()
{
	$("#divExecuting").show();
	$.post("/airflowwcs/plock/reallocateItemPick", $("#plock-form").serialize(), function(response){	
		$("#divExecuting").hide();
		if(response.responseCode==1)
		{
			alertOnResponse(response);
			var hostSendOutMessage = new HostOutSendMessage(); 
			
			if(client) //IF the client prototype is instantiated
			{
				client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(hostSendOutMessage)); //send the message to the WebTopic
				console.log("PlockPick: ReAllocate Pick Complete. Sending HostMesgSend Event | Message: " + hostSendOutMessage); 
			}else
			{
				console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
			}
			if (getCheckWeight()==1)
		    {
				var scaleMessage = new GetWeightMessage(getToStationId(), getPickToPalletId());			
				if(client) //IF the client prototype is instantiated
				{
					client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(scaleMessage)); //send the message to the WebTopic
					console.log("ScaleWeight: JMS Message sent | Message: " + scaleMessage); 
				}else
				{
					console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
				}
		    }
			clearPickForm(); 
			refreshPickData();
			updatePickTables(); 
			setTimeout(delayCheckNextPick,1200);  
		}
		else if(response.responseCode==ERROR) 
		{
			alert(response.responseMessage);
			alertOnResponse(response);
			$('#scannedItem').val(''); 
			$('#scannedItem').prop('readonly',false); 
			$('#pickedQty').val(0); 
			$('#scannedItem').select(); 
		}
		if($('#pickFromItemId').val()=="")
			$('#return-pick-from-load-button').focus(); 
		
		
	});
}

function createPickToLoad(boardId, stationId)
{
	$("#divExecuting").show();
	$.post("/airflowwcs/plock/createPlockPickToLoad?boardId="+boardId+"&stationId="+stationId+"&fromPallet="+getFromLoadId(), $(this).serialize(), function(response){
		$("#divExecuting").hide();
		
		if(response.responseCode==1)
		{
			alertOnResponse(response); 
			checkForLoadAtToStation($('#pickToStationId').val());
		}
		else
		{
			alert(response.responseMessage);
			alertOnResponse(response);
			 $('#pickToBoardId').select();
		}
	});
}

function getManagerApproval(userId, password, quantity)
{
	$.post("/airflowwcs/plock/managerApproval?userId="+userId+"&password="+password+"&item="+getItem()+"&qty="+quantity, $(this).serialize(), function(response){
		 
		if(response.responseCode==1)
		{
			$('#plock-approval-pick').modal('hide');
			alertOnResponse(response);
			$("#pickedQty").val(quantity); 
			$("#pickedQty").prop('readonly', true);
			$("#confirm-plock-pick-button").prop('disabled',false); 
			$("#confirm-plock-pick-button").focus(); 
			
		}
		else
		{
		  alert(response.responseMessage);
		  alertOnResponse(response);
		}
		 $('#managerId').val(''); 
		 $('#managerPwd').val(''); 
		
	});
}

function markPalletQA(fromPallet, qaDescription)
{
	$("#divExecuting").show();
	$.post("/airflowwcs/plock/markPalletQA?fromPallet="+fromPallet+"&qaDescription="+qaDescription, $(this).serialize(), function(response){
		$("#divExecuting").hide(); 
		if(response.responseCode==1)
		{
			alertOnResponse(response);
			$("#markqa-button").prop('disabled',true); 
			$('#return-pick-from-load-button').focus(); 			
		}
		else
		{
		  alert(response.responseMessage);
		  alertOnResponse(response);
		}
   	 	qaDescription = "";		
	});
}
/**
 * Clear out all form fields except for the From station and To Station fields
 * forcing us to fetch/refresh other fields on each pick. 
 *
 * 
 * 
 * @returns
 */
function clearPickForm()
{
	$("#pickFromBoardId").val(""); 
	//$("#pickFromPlockOrderId").val(""); TESTING NOT CLEARING PLOCKFROMORDERID FOR THE LOAD RELEASE CHECK?? 
	$("#carrierId").val(""); 
	$("#pickFromItemId").val(""); 
	$('#pickFromItemArticle').val(""); // manually strip article from item id (don't want another roundtrip)
	$('#pickFromItemSupplier').val(""); // manually strip supplier from item id (don't want another roundtrip)
	$('#pickFromItemDivision').val(""); // manually strip division from item id (don't want another roundtrip)
	$("#pickFromItemDescription").val(""); 
	$("#totalPicked").val(0); 
	$("#totalRemaining").val(0); 
	$("#totalAllocated").val(0); 
	$("#scannedItem").val(""); 
	$("#pickedQty").val(0);
	$("#pickFromLoadPalletId").val(""); 
	$("#scannedItem").prop("readonly", true); 
	$("#pickedQty").prop("readonly", true); 
	$('#return-pick-from-load-button').prop('disabled', true);
	$("#return-pick-from-load-button").removeClass('btn-success').addClass('btn-warning'); 
	$("#complete-pick-to-load-button").prop('disabled', true); 
	$("#confirm-plock-pick-button").prop('disabled', true); 
	if ($('#pick-item-container').hasClass('has-success')); 
		$("#pick-item-container").toggleClass("has-success");
	if($("#picks-remaining-container").hasClass("has-error"))
		$("#picks-remaining-container").toggleClass("has-error");
	if($("#pick-item-description-container").hasClass("has-success"))
		$("#pick-item-description-container").toggleClass("has-success");
	if($("pickFromItemId").val()=="")
		$("#scannedItem").prop("read-only",true); 
	
}

function clearToStationForm()
{
	$("#pickToBoardId").prop("readonly", true); 
	$("#pickToBoardId").val(""); 
	$("#pickToLoadPalletId").val(""); 
	$("#pickToPlockOrderId").val(""); 
}

function clearFromStationForm()
{
	$("#pickFromLoadPalletId").val(""); 
	$("#pickFromBoardId").val("");
	$("#pickFromPlockOrderId").val(""); 
	$('#pickFromPlockOrderLineId').val("");
}

function refreshPickData()
{
	/** FROM STATION ***/
	var fromStation = $("#station").val();			
	checkForLoadAtFromStation(fromStation); //checks for load at TO-station in callback handler
	if(fromStation=='')
		console.log('NO STATION IN FROM STATION FIELD FOR LOOKUP'); 
	/** TO STATION ***/
	getConnectedToStationData(fromStation); 
	var toStation = $("#pickToStationId").val(); 
	if(toStation=='')
		console.log('NO STATION IN TO STATION FIELD FOR LOOKUP');
	getAstroMap(fromStation, toStation); 
	checkForLoadAtToStation(toStation); 
	
	/** FROM LOAD ***/		
	var fromLoadId = $("#pickFromLoadPalletId").val(); 
	if(fromLoadId=='')
		console.log('NO FROM LOAD ID IN FIELD FOR LOOKUP'); 
	findNextPick(fromLoadId); 	
	
	
}

function getAstroMap(fromStation, toStation)
{
	$.post("/airflowwcs/plock/findAstroMap?fromStation="+fromStation+"&toStation="+toStation, $(this).serialize(), function(response){
		if(response)
		{
			$('#fromAstroMha').val(response.fromAstroMha);
			$('#fromAstroLocation').val(response.fromAstroLocation); 
			$('#toAstroMha').val(response.toAstroMha); 
			$('#toAstroLocation').val(response.toAstroLocation); 
		}
		else
		{
			//alertError("NO ASTRO MAP", "NO ASTOMAPPING FOUND FOR TO: " + toStation + " FROM: " + fromStation);
			alert("NO ASTOMAPPING FOUND FOR TO: " + toStation + " FROM: " + fromStation);
			$('#fromAstroMha').val('');
			$('#fromAstroLocation').val(''); 
			$('#toAstroMha').val(''); 
			$('#toAstroLocation').val(''); 
			
		}
	});
}

function releaseFromPallet()
{
	var loadMessage = new LoadEventMessage(null, $("#station").val(), null, null, null, null,
			null, null, null, null, null, null, null, null, $("#pickFromBoardId").val(), null, null, null, null)			
	if(client) //IF the client prototype is instantiated
	{
		client.send("jms.topic.WebTopic", {'skipcl':true},JSON.stringify(loadMessage)); //send the message to the WebTopic
		console.log("PlockPick: JMS Message sent | Message: " + loadMessage); 
	}else
	{
		console.log("Error - Unable to establish a connection with JMS - client is null"); // log error to javascript console if not connected
	}
	
	clearPickForm();
	clearPickFromLoad(); 
	clearFromStationForm()
	checkForLoadAtFromStation($("#station").val()); 
	$("#return-pick-from-load-button").prop("disabled",true);
	$("#markqa-button").prop('disabled',true);
	$("#return-pick-from-load-button").removeClass('btn-success').addClass('btn-warning'); 
	checkForLoadAtToStation($("#pickToStationId").val()); 
}

function delayCheckNextPick()
{
	if($('#pickFromItemId').val()=="" || $('#totalRemaining').val()=="0" || $('#totalRemaining').val()==0)
		$('#return-pick-from-load-button').focus(); 
}

function getFromStationId()
{
	return $('#station').val(); 
}

function getToStationId()
{
	return $('#pickToStationId').val(); 
}

function getFromLoadId()
{
	return $('#pickFromLoadPalletId').val(); 
}

function getToLoadId()
{
	return $('#pickToLoadPalletId').val(); 
}

function getItem()
{
	return $('#pickFromItemId').val(); 
}

function getFromOrderId()
{
	return $('#pickFromPlockOrderId').val(); 
}

function getPickToBoardId()
{
	return $('#pickToBoardId').val(); 
}

function getPickToPalletId()
{
	return $('#pickToLoadPalletId').val(); 
}

function getCheckWeight()
{
	return $('#weightCheck').val(); 
}
