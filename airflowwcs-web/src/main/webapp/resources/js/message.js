/**
 * When message page is ready 
 */
$(document).ready(function(){ // on page load complete
	$("#connect").click(function(){ // Set #connect button's onClick event handler
		connect(); 
		$("#message-type").prop("disabled",false);
		$("#connect").prop("disabled",true);
		$("#disconnect").prop("disabled",false);
	}); 
	$("#disconnect").click(function(){ // Set #disconnect button's onClick event handler
		disconnect(); 
		$("#message-type").prop("disabled",true);
		$("#connect").prop("disabled",false);
		$("#disconnect").prop("disabled",true);
	}); 
	$('#message-type').change(function(){
		evalMessageOption($(this).val()); 
	});
	$("#spoof-load-send-button").click(function(){
		alert('sending load arrival message for station: ' + $('#arrivalStation').val() + ' load: ' + $('#arrivalLoad').val()); 
		sendLoadArrival($('#arrivalLoad').val(), $('#arrivalStation').val());
		$('#spoof-load-arrival-modal').modal('hide'); 
	}); 
	$('#spoof-weight-send-button').click(function(){
		alert('sending weight check message for station ' + $('#weightStation').val()); 
		$('#weight-modal').modal('hide'); 
	});
	
	$('#send-message').click(function(){
		evalMessageOption($('#message-type').val());
	}); 
}); 

/**
 * when an option is chosen from the message dropdown evaluate the choice and perform action
 * 
 * @param choice - the option id chosen
 * 
 */
function evalMessageOption(choice)
{
	if(choice == 'loadArrival'){
		$('#spoof-load-arrival-modal').modal('show'); 
		$('#send-message').prop('disabled', false); 
	}
	if(choice== 'getWeight'){
		$('#weight-modal').modal('show'); 
		$('#send-message').prop('disabled', false); 
	}
	if(choice ==''){
		$('#send-message').prop('disabled',true); 
	}
}








