/********************
 *  Recovery Type indicator constants. Indicates which  
 *  recovery types should show up for given load status(s)
 */
var ltw_pickup_recovery_options = ["Complete Movement", "Re-Schedule Deposit","Re-Schedule Pickup"]; 
var ltw_deposit_schedule_options = ["Reschedule Deposit"]; 
var ltw_deposit_schedule_options = ["Reschedule Pickup"];
var ltw_deposit_move_options = ["Complete Movement"]; 
var arrive_pending_recovery_options = ["Auto-pick Load"]; 
var prime_recovery_options = ["Re-send Load Move", "Re-send Arrival"]; 
var arrived_recovery_options = ["Auto-pick Load"]; 
var arrival_pending_options = ["Re-send Load Arrival"]; 
var id_pending_recovery_options = ["Delete Pending Load"]; 
var moving_error_recovery_options = ["Complete Movement","Re-Schedule Crane"]; 
var retrieving_recovery_options = ["Complete Movement"]; 
var store_recovery_options = ["Cancel Store", "Reschedule Store" ]; 
var retrieve_pending_recovery_options = ["Set to NO MOVE"]; 
var bin_recovery_options = ["Not implemented"]; 
var reject_options = ["Reject Load"]
var reject_options = ["Re-store Load"]
var empty_options = [""]; 

function popupErrorNoFunctionDefined()
{ 
	$.alert({
	    title: 'No Method',
	    content: 'No recovery method has been toggled for this load. Please toggle a recovery method switch from NO to YES and click START RECOVERY.',
	});
}

/**
 * Page initialization
 */
$(document).ready(function(){
	
	/******************************************************
	 *                  BUTTON LISTENERS
	 ******************************************************/
	/***
	 * ARRIVED - Start Recovery button
	 */
	$("#recover-arrived-button").click(function() {
		var is_auto_pick = document.getElementById("auto-pick-method").checked;
		if(!is_auto_pick){
			popupErrorNoFunctionDefined(); 
		}else{
			$.confirm({
			    title: 'Confirm Auto-Pick',
			    content: 'Are you sure you want to auto-pick Load <strong>' + $("#recover-load").val() +'</strong>?',
			    buttons: {
			        confirm: function () {
			        	$.post("/airflowwcs/recovery/autoPick?loadId="+$("#recover-load").val(), function(response){
							alertOnResponse(response); 
							if(response.responseCode==1)
							{
								hideAllSettingsCards(); 
								table.ajax.reload(); 
							}
						});
			        },
			        cancel: function () {
			            //do nothing
			        }
			    }
			});
			
		}
	});
	
	
	/***
	 * ARRIVED - Start Recovery button
	 */
	$("#recovery-message-button").click(function() {
		
			$.confirm({
			    title: 'Send Recovery Message',
			    content: 'Type of message to send?',
			    buttons: {
			        bufferclear: {
			        	text: 'Buffer Clear', 
			        	action: function () {
			        		
			        		
			        		$.confirm({
			        		    title: 'Enter Device ID destination',
			        		    content: '' +
			        		    '<form action="" class="formName">' +
			        		    '<div class="form-group">' +
			        		    '<label>Device ID</label>' +
			        		    '<input type="text" placeholder="Device ID" class="name form-control" id="recover-message-device-id" required />' +
			        		    '</div>' +
			        		    '</form>',
			        		    buttons: {
			        		        formSubmit: {
			        		            text: 'Submit',
			        		            btnClass: 'btn-blue',
			        		            action: function () {
			        		                var device = $('#recover-message-device-id').val();
			        		                if(!device){
			        		                    $.alert('Provide a device ID');
			        		                    return false;
			        		                }
			        		                $.post("/airflowwcs/message/testBufferClear?deviceId="+$("#recover-message-device-id").val(), function(response){
												alertOnResponse(response); 			
												if(response.responseCode==1)
												{
													table.ajax.reload(); 
												}
											});
			        		                
			        		            }
			        		        },
			        		        cancel: function () {
			        		            //close
			        		        },
			        		    },
			        		    onContentReady: function () {
			        		        // bind to events
			        		        var jc = this;
			        		        this.$content.find('form').on('submit', function (e) {
			        		            // if the user submits the form by pressing enter in the field.
			        		            e.preventDefault();
			        		            jc.$$formSubmit.trigger('click'); // reference the button and click it
			        		        });
			        		    }
			        		});
			        		
			        		
			        		
			    
			        		}
			        	
			        },
			        cancel: function () {
			            //do nothing
			        }
			    }
			});
			
		
	});

	/***
	 * Bin - Start Recovery button
	 */
	$("#recover-bin-button").click(function() {
		$.alert({
		    title: 'No Method',
		    content: 'No recovery method has been implemented for this type.',
		});
/*		var is_auto_pick = document.getElementById("auto-pick-method").checked;
		if(!is_auto_pick){
			$.alert({
			    title: 'No Method',
			    content: 'No recovery method has been toggled for this load. Please select a recovery method.',
			});
		}else{
			$.post("/airflowwcs/recovery/autoPick?loadId="+$("#recover-load").val(), function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
				}
			});
		}*/
	});
	
	/***
	 * Id Pending - Start Recovery button
	 */
	$("#recover-id-pending-button").click(function() {
		var is_load_delete = document.getElementById("id-pending-delete-method").checked;
		if(!is_load_delete){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/load/delete?load="+$("#recover-load").val(), function(response){ //delete the load
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	
	/***
	 * LTW - REJECT - Start Recovery button
	 */
	$("#recover-ltw-reject-button").click(function() {
		var is_load_delete = document.getElementById("ltw-reject-method").checked;
		if(!is_load_delete){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/ltwReject?loadId="+$("#recover-load").val(), function(response){ //reject the load
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	
	/***
	 * LTW - RE-STORE - Start Recovery button
	 */
	$("#recover-ltw-restore-button").click(function() {
		var is_load_restore = document.getElementById("ltw-restore-method").checked;
		if(!is_load_restore){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/ltwRestore?loadId="+$("#recover-load").val(), function(response){ //reject the load
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	
	/***
	 * LTW Deposit MOVE - Start Recovery button
	 */
	$("#recover-ltw-move-button").click(function() {
		var is_auto_move = document.getElementById("ltw-move-method").checked;
		if(!is_auto_move){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/completeMove?loadId="+$("#recover-load").val(), function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	/***
	 * LTW Pickup - Start Recovery button
	 */
	$("#recover-ltw-schedule-pickup-button").click(function() {
		var is_schedule = document.getElementById("ltw-pickup-method").checked;
		if(!is_schedule){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/rescheduleLtwPickup?loadId="+$("#recover-load").val(), function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	/***
	 * LTW Deposit  - Start Recovery button
	 */
	$("#recover-ltw-schedule-deposit-button").click(function() {
		var is_schedule = document.getElementById("ltw-deposit-method").checked;
		if(!is_schedule){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/rescheduleLtwDeposit?loadId="+$("#recover-load").val(), function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	/***
	 * Move Pending - Start Recovery button
	 */
	$("#recover-move-pending-button").click(function() {
		var is_auto_pick = document.getElementById("auto-pick-method").checked;
		if(!is_auto_pick){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/autoPick?loadId="+$("#recover-load").val(), function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	/***
	 * Move - Start Recovery button
	 */
	$("#recover-move-button").click(function() {
		var is_auto_pick = document.getElementById("auto-pick-method").checked;
		if(!is_auto_pick){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/autoPick?loadId="+$("#recover-load").val(), function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	/***
	 * Prime Move - Start Recovery button
	 */
	$("#recover-prime-move-button").click(function() {
		var is_prime_method = document.getElementById("prime-method").checked;
		if(!is_prime_method){
			popupErrorNoFunctionDefined(); 
		}else{
			$.confirm({
			    title: 'Move Load Command - CONVEYOR',
			    content: 'Do you want to resend the move load command to the prime conveyor for load ' + $("#recover-load").val() +'?',
			    buttons: {
			        yes: function () { // Send move load command to prime conveyor
			        	//recover primemove with prime conveyor message
			        	$.post("/airflowwcs/recovery/recoverPrimeMove?loadId="+$("#recover-load").val()+"&isSendConveyor=1&isSendArrival=0", function(response){
							alertOnResponse(response); 
							if(response.responseCode==1)
							{
								//success
								hideAllSettingsCards(); 
							}
						});
			        },
			        no: function () { // Do not send move load command to prime conveyor
			        	hideAllSettingsCards(); 
			        	$.confirm({
						    title: 'Arrival Command - CONVEYOR',
						    content: '"Do you want to send the arrival command from the prime conveyor; for load ' + $("#recover-load").val() +'?',
						    buttons: {
						        yes: function () { // Send arrival command from prime conveyor
						        	$.post("/airflowwcs/recovery/recoverPrimeMove?loadId="+$("#recover-load").val()+"&isSendConveyor=0&isSendArrival=1", function(response){
										alertOnResponse(response); 
										if(response.responseCode==1)
										{
											//success
											hideAllSettingsCards(); 
										}
									});
						        	
						        },
						        no: function () { // Do nothing - manual
						        	$.alert({
						        	    title: 'Manual',
						        	    content: 'Perform manual recovery.',
						        	});
						        }
						    }
						});
			        	
			        }
			    }
			});
		}
	});
	/***
	 * recover loads in Arrival Pending status - Start Recovery button
	 */
	$("#recover-arrival-pending-button").click(function() {
		var is_resend_arrival = document.getElementById("method-resend-arrival").checked;
		if(!is_resend_arrival){
			popupErrorNoFunctionDefined(); 
		}else{
			$.confirm({
			    title: 'Enter Load Height',
			    content: '' +
			    '<form action="" class="formName">' +
			    '<div class="form-group">' +
			    '<label>Enter height</label>' +
			    '<input type="text" placeholder="Height (0,1)" class="height-entry form-control" required />' +
			    '</div>' +
			    '</form>',
			    buttons: {
			        formSubmit: {
			            text: 'Set Height',
			            btnClass: 'btn-blue',
			            action: function () {
			                var height = this.$content.find('.height-entry').val();
			                if(height && (height == 0 || height ==1)){
			                	$.post("/airflowwcs/recovery/recoverArrivalPending?loadId="+$("#recover-load").val()+"&height="+height, function(response){
			        				alertOnResponse(response); 
			        				if(response.responseCode==1)
			        				{
			        					//success
			        				}
			        			});
			                
			                }else
			                {
			                	$.alert('Invalid height, please enter 1 or 0 and try again.');
			                }
			                
			            }
			        },
			        cancel: function () {
			            //close
			        },
			    },
			    onContentReady: function () {
			        // bind to events
			        var jc = this;
			        this.$content.find('form').on('submit', function (e) {
			            // if the user submits the form by pressing enter in the field.
			            e.preventDefault();
			            jc.$$formSubmit.trigger('click'); // reference the button and click it
			        });
			    }
			});
		}
	});
	/***
	 * Retrieve Pending - Start Recovery button
	 */
	$("#recover-retrieve-pending-button").click(function() {
		var is_auto_pick = document.getElementById("auto-pick-method").checked;
		if(!is_auto_pick){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/autoPick?loadId="+$("#recover-load").val(), function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	/***
	 * Retrieve - Start Recovery button
	 */
	$("#recover-retrieve-button").click(function() {
		var is_auto_pick = document.getElementById("auto-pick-method").checked;
		if(!is_auto_pick){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/autoPick?loadId="+$("#recover-load").val(), function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	/***
	 * Store Pending - Start Recovery button
	 */
	$("#recover-store-pending-button").click(function() {
		var is_auto_pick = document.getElementById("auto-pick-method").checked;
		if(!is_auto_pick){
			popupErrorNoFunctionDefined(); 
		}else{
			$.post("/airflowwcs/recovery/autoPick?loadId="+$("#recover-load").val(), function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
		}
	});
	/***
	 * Store - Start Recovery button
	 */
	$("#recover-store-button").click(function() {
		var is_auto_pick = document.getElementById("auto-pick-method").checked;
		if(!is_auto_pick){
			popupErrorNoFunctionDefined(); 
		}else{
			
			$.post("/airflowwcs/recovery/autoPick?loadId="+$("#recover-load").val()+"&height="+height, function(response){
				alertOnResponse(response); 
				if(response.responseCode==1)
				{
					//success
					hideAllSettingsCards(); 
				}
			});
            return false;
		}
	});
	
	
	
	/**
	 * "Delete" button press in load card - delete load after confirmation
	 */
	$("#recovery-delete-load-button").click(function() {
		$.confirm({
		    title: 'Confirm Delete',
		    content: 'Are you sure you want to delete load ' + $("#recover-load").val() +'?',
		    buttons: {
		        confirm: function () {
		        	$.post("/airflowwcs/load/delete?load="+$("#recover-load").val(), function(response){
						alertOnResponse(response); 
						if(response.responseCode==1)
						{
							hideAllSettingsCards(); 
							table.ajax.reload(); 
						}
					});
		        },
		        cancel: function () {
		            //do nothing
		        }
		    }
		});
	});
	
	/*
	 * When "view details" button clicked show/update the load details panel
	 */
	$('#recovery-view-load-details-button').on('click',function(){
		updateLoadDetailsPanel(); 
	}); 
	
	/**
	 * When row in table is clicked update form fields with selected row data
	 */
	$('#ajaxTable tbody').on('click', 'tr', function(){
		// get selected table data 
		var data = table.row(this).id(); 
		var idx = table.cell('.selected', 0).index();
		var row = table.row( idx.row ).data();
		
		//keep users from selecting/recovering more than one row at a time
		if(selectedRows.length<=1){ 
			$('#recover-load').val(data); 
			$('#recover-warehouse').val(row.Warehouse); 
			$('#recover-address').val(row.Address); 
			$('#recover-device').val(row['Device ID']); 
			$('#recover-status').val(row['Move Status']); 
		}else{
			$('#recover-load').val("MULTIPLE"); 
			$('#recover-warehouse').val("-"); 
			$('#recover-address').val("-"); 
			$('#recover-device').val("-"); 
			$('#recover-status').val("-"); 
		}
		// update which recovery option is shown base on move status column data 
		updateRecoverySelection(row['Move Status']);  
	});
	
}); 

/**
 * Update items in the collapse-able load detail panel
 */
function updateLoadDetailsPanel()
{
	window.setTimeout(function(){ tableItemDetail.draw();}, 75); // after wait, update the column draw spacings for buggy displays
}



/**
 * Determine/display recovery options for use based on selected row move_status. 
 * 
 * Disallow multiple selection with popup. 
 * 
 */
function updateRecoverySelection(move_status)
{
	if(selectedRows.length>1)
	{
		
			$.confirm({ //display confirm pop-up to inform of de-selecting multiple loads
			    title: 'Multiple Selection!',
			    content: 'Only one load can be recovered at a time.',
			    buttons: {
			        confirm: function () {
			        	table.rows('.selected').deselect(); //deslect anything with .selected css class
			        	$('#recover-load').val("None Selected"); // clear form
			    		$('#recover-warehouse').val("-"); 
			    		$('#recover-address').val("-"); 
			    		$('#recover-device').val("-"); 
			    		$('#recover-status').val("-"); 
			        }
			    }
			});
		
	}
	hideAllSettingsCards(); // hide all others before displaying selected 
	switch (move_status) { //show and update recovery methods with move status
		case "LTW Pickup":		
		case "LTW Pickup Error":	
			promptLTWPickupRecovery(); 
	        break;
		case "LTW Deposit":		
		case "LTW Deposit Error":	
		case "LTW Deposit Pending":	// use popup first to determine if the load has been deposited from LTW 
			promptLTWDepositRecovery(); 
	        break;
		case "Prime Move":		
			updateRecoveryMethods(prime_recovery_options); 
			showPrimeRecoverySettings(); 
	        break;
		case "Arrived":
			showNotImplementedSettings(); // NOT IMPLEMENTED
			updateRecoveryMethods(["None"]); 
			break;
		case "Arrival Pending":
			showNotImplementedSettings(); // NOT IMPLEMENTED
			updateRecoveryMethods(["None"]); 
			break;
		case "ID Pending":
			showNotImplementedSettings(); // NOT IMPLEMENTED
			updateRecoveryMethods(["None"]); 
			break;
		case "Move Error":
		case "Moving":
		case "Move Sent":
			updateRecoveryMethods(moving_error_recovery_options); 
			showMoveRecoverySettings(); 
			break;
		case "Retrieve Error":		
		case "Retrieving":
		case "Retrieve Message Sent":	
			showRetrieveSettings(); 
			updateRecoveryMethods(retrieving_recovery_options); 
	        break;   
		case "Store Error":
		case "Storing":		
			promptStoreRecovery(); 
//			showStoreSettings(); 
//			updateRecoveryMethods(ltw_pickup_recovery_options); 
	        break;
		case "Store Sent":	
			promptRescheduleStore(); 
			break; 
		case "Store Pending":	
			showStorePendingSettings(); 
			updateRecoveryMethods(store_recovery_options); 
	        break;
		case "Move Pending":
			showMovePendingSettings(); 
			updateRecoveryMethods(ltw_pickup_recovery_options); 
			break;
		case "Retrieve Pending":
			showNotImplementedSettings(); // NOT IMPLEMENTED
			updateRecoveryMethods(["None"]); 
	        break;
		case "Bin Full Error":
		case "Size Mismatch Error":	
			showNotImplementedSettings(); // NOT IMPLEMENTED
			updateRecoveryMethods(["None"]); 
			break;
			//TODO unimplemented recovery methods. 
			/*case "ArrivalPending":
				break;
			case "Building": 
				break;
			case "Consolidated": 
				break;
			case "Consolidating":
				break;
			case "Error": 
				break;
			case "No Move":
				break;
			case "Picked": 
				break;
			case "Received and Checked":		
		        break;
			case "Received":		
		        break;
			case "Shipping":		
		        break;
			case "ShipWait":		
		        break;
			case "Staged":		
		        break;
			case "Swap Pending":		
		        break;
			case "Waiting for location":		
		        break;*/
		default: // selected move status recovery option not found. 
			showNotImplementedSettings(); 
			updateRecoveryMethods(["None"]); 
			break;
			
	}
}

function promptStoreRecovery()
{
	$.post("/airflowwcs/recovery/storeInfo?loadId="+$("#recover-load").val(), function(response){
		alertOnResponse(response); 
		if(response.responseCode==1) // has load been stored? prompt
		{
			$.confirm({
				title: "Is Load Stored?", 
				content: "Has load "  + $("#recover-load").val() + " been physically stored at the location" + $('#recover-next-address').val() + "?" , 
				buttons:{
					yes: function(){
						$.post("/airflowwcs/recovery/ltwStoreComplete?loadId="+$("#recover-load").val(), function(response){
							alertOnResponse(response); 
						});
					}, 
					no: function(){
						promptRescheduleStore(); 
					}
				}
				
			}); 
		}else if(response.responseCode==-3) // should not be storing, cancel store? 
		{
			$.confirm({
				title: "Cancelling Store", 
				content: "Load "+ $("#recover-load").val()+" should not be stored in it's current state, cancelling the store recovery..", 
				buttons:{
					ok: function(){
						$.alert('Cancelled Store!');
					}
				}
				
			}); 
		}else if(response.responseCode==-1){ //there are older loads recover them first
			$.alert({
				title: 'ERROR: Older Loads Exist',
				content:'There are older loads, recover them first'
				
			}); 
		}else if(response.responseCode==0){
			$.alert({
				title: 'ERROR: Invalid Selection',
				content:'This is an invalid recovery selection, please try again'
				
			}); 
		}
	});
}

function promptRescheduleStore()
{
	
	$.confirm({
		title: "Rechedule Store?", 
		content:"Do you want to reschedule storage of load: " + $("#recover-load").val() + "?", 
		buttons:{
			yes: function(){
				$.post("/airflowwcs/recovery/ltwRescheduleStore?loadId="+$("#recover-load").val(), function(response){
					alertOnResponse(response); 
				}); 
			}, 
			no: function(){
				$.alert("Recovery cancelled."); 
			}
		}
	});
}


/**
 * Once LTW has been selected prompt the initial data collection for whether
 * the load has been already picked up by the crane? 
 * 
 * If yes, prompt for if the load has been deposited. 
 * 
 * Show context menus based on selection 
 * @returns
 */
function promptLTWPickupRecovery()
{
	updateRecoveryMethods(ltw_pickup_recovery_options); 
	$.confirm({
	    title: 'LTW Pickup Recovery',
	    content: 'Has load ' + $("#recover-load").val() +' been PICKED UP by the Crane?',
	    buttons: {
	        yes: function () { // has been picked up
	        	$.confirm({
				    title: 'Deposited',
				    content: 'Has load ' + $("#recover-load").val() +' been DEPOSITED at destination by the Crane?',
				    buttons: {
				        yes: function () { // has been deposited
				        	showLTWMoveRecoverySettings(); // choices to auto-move the load location
				        	updateRecoveryMethods(ltw_deposit_move_options); 
				        },
				        no: function () { // has NOT been deposited
				        	$.confirm({
							    title: 'Deposited',
							    content: 'Do you want to reject load ' + $("#recover-load").val() +'?',
							    buttons: {
							        yes: function () { // has been deposited
							        	//show reject
							        	showLTWRejectSettings(); 
							        	updateRecoveryMethods(reject_options);
							        },
							        no: function () { // has NOT been deposited
							        	//show restore
							        	showLTWRestoreSettings(); 
							        	updateRecoveryMethods(restore_options);
							        }
							    }
							});
				        	

				        }
				    }
				});

	        },
	        no: function () { // has NOT been picked up.
	        	showLTWScheduleRecoveryPickupSettings(); // choices to reschedule crane to pickup load
	        	updateRecoveryMethods(ltw_deposit_schedule_options); 
	        }
	    }
	});
}

function promptLTWDepositRecovery()
{
	$.confirm({
	    title: 'LTW Deposit Recovery',
	    content: 'Has load ' + $("#recover-load").val() +' been PICKED UP by the Crane?',
	    buttons: {
	        yes: function () { // has been picked up
	        	$.confirm({
				    title: 'Deposited',
				    content: 'Has load ' + $("#recover-load").val() +' been DEPOSITED at destination by the Crane?',
				    buttons: {
				        yes: function () { // has been deposited in location
				        	//change load location
				        	showLTWMoveRecoverySettings(); // choices to auto-move the load location
				        	updateRecoveryMethods(ltw_deposit_move_options);
				        },
				        no: function () { // has NOT been deposited in location
				        	//reject
				        	showLTWRejectSettings(); 
				        	updateRecoveryMethods(reject_options);
				        }
				    }
				});
	        
	        },
	        no: function () { // has NOT been picked up
	        	showLTWScheduleRecoveryDepositSettings(); // choices to reschedule crane to pickup load
	        	updateRecoveryMethods(ltw_deposit_schedule_options); 
	        }
	    }
	});
}






/**
 * Hide all other recovery cards.
 * Toggle all switchs to off 
 * Used when refreshing new recovery options. 
 */
function hideAllSettingsCards() 
{
	$("#settings-arrived").hide();
	$("#settings-arrival-pending").hide();
	$("#settings-bin").hide();
	$("#settings-id-pending").hide(); 
	$("#settings-ltw-deposit").hide(); 
	$("#settings-ltw-pickup").hide(); 
	$("#settings-move-pending").hide(); 
	$("#settings-move").hide(); 
	$("#settings-none-selected").hide(); 
	$("#settings-not-implemented").hide(); 
	$("#settings-prime-move").hide(); 
	$("#settings-retrieve-pending").hide(); 
	$("#settings-retrieve").hide(); 
	$("#settings-store-pending").hide(); 
	$("#settings-store").hide(); 
	$("#settings-ltw-move").hide(); 
	$("#settings-ltw-schedule").hide(); 
	$("#settings-ltw-reject").hide(); 
	$("#settings-ltw-recovery").hide(); 
	$("#settings-ltw-schedule-pickup").hide(); 
	$("#settings-ltw-schedule-deposit").hide(); 
	toggleAllMethodOff(); 
}

/**
 * Toggles all switches in recovery method box to OFF 
 * @returns nil
 */
function toggleAllMethodOff()
{
    $('#auto-pick-method').bootstrapToggle('off');  
    $('#method-resend-arrival').bootstrapToggle('off');  
    $('#id-pending-delete-method').bootstrapToggle('off');  
    $('#ltw-move-method').bootstrapToggle('off');  
    $('#ltw-reject-method').bootstrapToggle('off');  
    $('#ltw-pickup-method').bootstrapToggle('off');  
    $('#ltw-deposit-method').bootstrapToggle('off');  
    $('#prime-method').bootstrapToggle('off'); 
}

function onTableDraw(datatable)
{
	updatePage(datatable); 
}
function showNotImplementedSettings()
{
	$("#settings-not-implemented").show(); 
}
function showNoneSelectedSettings()
{
	hideAllSettingsCards(); 
	$("#settings-none-selected").show(); 
}

function showLTWScheduleRecoveryPickupSettings()
{
	hideAllSettingsCards(); 
	$("#settings-ltw-schedule-pickup").show(); 
}
function showLTWScheduleRecoveryDepositSettings()
{
	hideAllSettingsCards(); 
	$("#settings-ltw-schedule-deposit").show(); 
}
//function showLTWPickupRecoverySettings()
//{
//	$("#settings-ltw-schedule").show(); 
//}

function showLTWMoveRecoverySettings()
{
	hideAllSettingsCards(); 
	$("#settings-ltw-move").show(); 
}

function showLTWRejectSettings()
{
	hideAllSettingsCards(); 
	$("#settings-ltw-reject").show(); 
}

function showLTWRestoreSettings()
{
	hideAllSettingsCards(); 
	$("#settings-ltw-restore").show(); 
}

function showLTWScheduleRecoverySettings()
{
	hideAllSettingsCards(); 
	$("#settings-ltw-schedule").show(); 
}

function showPrimeRecoverySettings()
{
	hideAllSettingsCards(); 
	$("#settings-prime-move").show(); 
}
function showIdPendingRecoverySettings()
{
	hideAllSettingsCards(); 
	$("#settings-id-pending").show(); 
}

function showArrivedRecoverySettings()
{
	hideAllSettingsCards(); 
	$("#settings-arrived").show(); 
}

function showArrivePendingRecoverySettings()
{
	hideAllSettingsCards(); 
	$("#settings-arrival-pending").show(); 
}
function showMoveRecoverySettings()
{
	hideAllSettingsCards(); 
	$("#settings-move").show(); 
}

function showRetrieveSettings()
{
	hideAllSettingsCards(); 
	$("#settings-retrieve").show(); 
}

function showStoreSettings()
{
	hideAllSettingsCards(); 
	$("#settings-store").show(); 
}

function showStorePendingSettings()
{
	hideAllSettingsCards(); 
	$("#settings-store-pending").show(); 
}

function showMovePendingSettings()
{
	hideAllSettingsCards(); 
	$("#settings-move-pending").show(); 
}

function showRetrievePendingSettings()
{
	hideAllSettingsCards(); 
	$("#settings-retrieve-pending").show(); 
}

function showBinRecoverySettings()
{
	hideAllSettingsCards(); 
	$("#settings-bin").show(); 
}

/**
 * Display the Options for recovery in a button list next to the recovery method settings panel. 
 * Context sensitive for which status the load is in, will evaluate status type 
 * @param recovery_options
 * @returns
 */
function updateRecoveryMethods(recovery_options)
{
	$("#recovery-options-list").html("<h4>Recovery Type(s)</h4>"); 
	for(i=0;i<recovery_options.length;i++)
	{
		$("#recovery-options-list").append("<button type=\"button\" class=\"list-group-item\"><h5>"+recovery_options[i]+"</h5></button>"); 
	}
}

function updatePage(datatable)
{
	//lets not update the page with values if we have multiple rows selected
	if(selectedRows.length>1 || selectedRows.length == 0){
		return false;
	}
	var row = datatable.row( "#"+selectedRows[0] ).data();
	if(selectedRows.length<=1){
		$('#recover-load').val(row['Load ID']);  
		$('#recover-warehouse').val(row.Warehouse); 
		$('#recover-address').val(row.Address); 
		$('#recover-device').val(row['Device ID']); 
		$('#recover-status').val(row['Move Status']); 
		$('#recover-next-address').val(row['Next Address']); 
	}else{
		$('#recover-load').val("MULTIPLE"); 
		$('#recover-warehouse').val("-"); 
		$('#recover-address').val("-"); 
		$('#recover-device').val("-"); 
		$('#recover-status').val("-"); 
		$('#recover-next-address').val(""); 
	}
}