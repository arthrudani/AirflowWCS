/**
 * Row ids selected in datatables
 */
var selectedRows =[];

/**
 * Response CODES global
 */
var ERROR   = -1;
var INFO    =  1;
var SUCCESS =  2;
var WARNING = -2; 
var PROMPT = -3; 
var ALTPROMPT = -4;

var FIRST_ALERT = 1; 

/********************************************
 * 	INACTIVITY TIMER FOR SESSION TIMEOUT
 *******************************************/
var timeOut = 1000 * 60 * 15; // 15 Minute activity timeout
var lastActivity = new Date().getTime(); 
var checkTimeout; 
// function for checking last activity time vs timeout 
// & logging out if threshold exceeded
checkTimeOut = function(){
	//console.log("Checking timeout...");
	if(new Date().getTime() > lastActivity + timeOut){
		//console.log("-exceeded session thresholds- LOGGING OUT");
        $("#logout-link").click();// redirect to timeout page
    }else{
    	//console.log("-pass: Last Activity: " + lastActivity);
        window.setTimeout(checkTimeOut, 1000); // check once per second
    }
}


/**
 * Last alert response to check for the last action performed success/fail
 */
var lastResponse = ""; 

/**
 * Listener for backspaces outside textarea or input 
 * will prevent the backspace from navigating back a page
 */
$(document).on("keydown", function (e) {
    if (e.which === 8 && !$(e.target).is("input, textarea")) {
        e.preventDefault();
    }
});

$(document).on("keydown click", function (e){
	lastActivity = new Date().getTime();  
});

/**
 * Perform this on every page load that contains app.js 
 */
$(document).ready(function(){
	
	$("a#logout-link").on("click", function()
	{
		console.log( "LOGGING OUT" );
	}); 
	
	$("#test-message-button").on("click", function(){
		$.confirm({
		    title: 'Enter Message',
		    content: '' +
		    '<form action="" class="formName">' +
		    '<div class="form-group">' +
		    '<label>Enter Message</label>' +
		    '<input id="test-message-contents" type="text" placeholder="Message Text" class="height-entry form-control" required />' +
		    '</div>' +
		    '</form>',
		    buttons: {
		        formSubmit: {
		            text: 'Send Message',
		            btnClass: 'btn-blue',
		            action: function () {
		                if( $("#test-message-contents").val()!=""){
		                	$.post("/airflowwcs/message/testMessage/"+$("#test-message-contents").val(), function(response){
		        				alertOnResponse(response); 
		        				if(response.responseCode==1)
		        				{
		        					//success
		        				}else
		        				{
		        					//error
		        				}
		        			});
		                
		                }else
		                {
		                	$.alert('Please enter a message and try again.');
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
	}); 
	/**
	 * Logout link listener - submit a logout form 
	 */
	$("a#logout-link").click(function(){
		$("#logout-form").submit(); 
		console.log("LOGGING OUT"); 
	});
	
	/**
	 * When sidebar toggle is clicked redraw DataTable table if it exists
	 */
	$(document).on('click','.hoe-sidebar-toggle',function(){
		if(table!=null){ //if there is an instance of datatables on the page currently, redraw it so it's cell's widths will redraw to match
			table.draw(); 
		}

	});
	
	/**
	 * Double clicking on any ajax table row will call a generic 'onDrillDown' method
	 */
	$('#ajaxTable tbody').on('dblclick', 'tr', function () {
		var data = table.row( this ).id();
		//alert( 'You drilled down on '+data +'\'s row' );
		onDrillDown(data); 
	} );

	
	/**
	 * Cross-site-request-forgery tokens for session login. 
	 * This particular token is added to every request header
	 * associated with jquery ajax calls. Ajax requests will all
	 * break if this is not present. 
	 */
	$(function () {
		var token = $("meta[name='_csrf']").attr("content");
		var header = $("meta[name='_csrf_header']").attr("content");
		$(document).ajaxSend(function(e, xhr, options) {
			xhr.setRequestHeader(header, token);
		});
	});
	
	/**
	 * Start inactivity timer
	 */
	$(function () {
		checkTimeOut(); 
	});
	
	/**
	 * Everytime an row item is selected or deselected in the ajaxtable, 
	 * update the array of selected ids with all currently selected. 
	 */
//	if(typeof table !== 'undefined'){
//		if(table!=null){
//			table.on('select.dt', function() {
//				selectedRows = []
//				  table.rows('.selected').every(function(rowIdx) {
//				     selectedRows.push(table.row(rowIdx).id())
//				  })   
//				  console.log(selectedRows);
//				})
//			table.on('deselect.dt', function() {
//				selectedRows = []
//				table.rows('.selected').every(function(rowIdx){
//					selectedRows.push(table.row(rowIdx).id())
//				})
//				console.log(selectedRows); 
//			})
//		}
//	}

	/**
	 * Data tables error mode - Throw javascript errors. See dataTables ref 
	 */
	if($.fn.dataTable)
		$.fn.dataTable.ext.errMode = 'throw';
});


/**
 * Returns a random integer between min (inclusive) and max (inclusive)
 * Using Math.round() will give you a non-uniform distribution!
 */
function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function showLoadingAnimation(tableId)
{
	$('#loading_gif_container'+tableId).html('<img class="loading_gif" src="/airflowwcs/resources/img/reload.gif">'); //replace html with loading gif in loading_gif_container
}

function hideLoadingAnimation(tableId)
{
	setTimeout(function(){$('#loading_gif_container'+tableId).html('');},500); //replace html with nothing after half second
}

/**
 * Place the message in a SUCCESS <li/> in the notification
 * area (top right) 
 * 
 * @param message
 */
function addSuccessNotification(message)
{
	if(FIRST_ALERT == 1){
		$('#notification-area').html('');
		FIRST_ALERT = 0; 
	}
	$('#notification-area').prepend('<li class="success-notification">'+
									'<a href="#"><i class="fa fa-check-circle"'+
									'id="success-notification-icon"></i>'+
									message +'</a></li>'); 
	$('#mobile-notification-area').prepend('<li class="success-notification">'+
			'<a href="#"><i class="fa fa-check-circle"'+
			'id="success-notification-icon"></i>'+
			message +'</a></li>'); 
}
/**
 * Place the message in a SUCCESS <li/> in the notification
 * area (top right) 
 * 
 * @param message
 */
function addInfoNotification(message)
{
	if(FIRST_ALERT == 1){
		$('#notification-area').html('');
		FIRST_ALERT = 0; 
	}
	$('#notification-area').prepend('<li class="info-notification">'+
	'<a href="#"><i class="fa fa-exclamation"'+
	'id="info-notification-icon"></i>'+
	message +'</a></li>'); 
	$('#mobile-notification-area').prepend('<li class="info-notification">'+
			'<a href="#"><i class="fa fa-exclamation"'+
			'id="info-notification-icon"></i>'+
			message +'</a></li>'); 
}
/**
 * Place the message in a SUCCESS <li/> in the notification
 * area (top right) 
 * 
 * @param message
 */
function addWarningNotification(message)
{
	if(FIRST_ALERT == 1){
		$('#notification-area').html('');
		FIRST_ALERT = 0; 
	}
	$('#notification-area').prepend('<li class="warning-notification">'+
	'<a href="#"><i class="fa fa-exclamation-circle"'+
	'id="warning-notification-icon"></i>'+
	message +'</a></li>'); 
	$('#mobile-notification-area').prepend('<li class="warning-notification">'+
			'<a href="#"><i class="fa fa-exclamation-circle"'+
			'id="warning-notification-icon"></i>'+
			message +'</a></li>'); 
}
/**
 * Place the message in a SUCCESS <li/> in the notification
 * area (top right) 
 * 
 * @param message
 */
function addErrorNotification(message)
{
	if(FIRST_ALERT == 1){
		$('#notification-area').html('');
		FIRST_ALERT = 0; 
	}
	$('#notification-area').prepend('<li class="error-notification">'+
	'<a href="#"><i class="fa fa-times"'+
	'id="error-notification-icon"></i>'+
	message +'</a></li>'); 
	$('#mobile-notification-area').prepend('<li class="error-notification">'+
			'<a href="#"><i class="fa fa-times"'+
			'id="error-notification-icon"></i>'+
			message +'</a></li>'); 
}

function notifyOnResponse(response)
{
	if(response.message){
		if(response.responseCode==1)
		{
			addSuccessNotification(response.message); 
		}
		else if(response.responseCode==-1)
		{
			addErrorNotification(response.message); 

		}
		else if(response.responseCode==0)	
		{
			addInfoNotification(response.message);

		}
		else if(response.responseCode==-2)
		{
			addWarningNotification(response.message); 

		}
	}else if(response.responseMessage)
	{
		if(response.responseCode==1)
		{
			addSuccessNotification(response.responseMessage); 

		}
		else if(response.responseCode==-1)
		{
			addErrorNotification(response.responseMessage);  
 
		}
		else if(response.responseCode==0)	
		{
			addInfoNotification(response.responseMessage); 

		}
		else if(response.responseCode==-2)
		{
			addWarningNotification(response.responseMessage); 

		}
	}
}


/**
 * Show alert based on responseCode Value. Will also place a notification 
 * in the top right Notification fly out
 * 
 * @param response
 * @returns
 */
function alertOnResponse(response)
{
	
	if(response.message){
		if(response.responseCode==1)
		{
			alertSuccess("SUCCESS", response.message); 
			lastResponse = "SUCCESS"; 
		}
		else if(response.responseCode==-1)
		{
			alertError("ERROR", response.message); 
			lastResponse = "ERROR"; 
		}
		else if(response.responseCode==0)	
		{
			alertInfo("INFO", response.message);
			lastResponse = "INFO"; 
		}
		else if(response.responseCode==-2 || response.responseCode==-3 || response.responseCode==-4)
		{
			alertWarning("WARNING", response.message); 
			lastResponse = "WARNING"; 
		}
	}else if(response.responseMessage)
	{
		if(response.responseCode==1)
		{
			alertSuccess("SUCCESS", response.responseMessage); 
			lastResponse = "SUCCESS"; 
		}
		else if(response.responseCode==-1)
		{
			alertError("ERROR", response.responseMessage);  
			lastResponse = "ERROR"; 
		}
		else if(response.responseCode==0)	
		{
			alertInfo("INFO", response.responseMessage); 
			lastResponse = "INFO"; 
		}
		else if(response.responseCode==-2 | response.responseCode==-3 || response.responseCode==-4)
		{
			alertWarning("WARNING", response.responseMessage); 
			lastResponse = "WARNING"; 
		}
	}
	
}

/**
 * Info level alert popdown in alert-area 
 * 
 * @param label
 * @param message
 * @returns
 */
function alertInfo(label, message)
{ 
	var label_id = label.replace(/\s/g, '_'); //replace spaces with underscores
	label_id = label_id + getRandomInt(1,9999); //give a random id for duplicate labeled alerts
	var alert = buildAlert(label_id, message, "alert-info", label); 
	$('#alert-area').prepend(alert).fadeIn('slow'); 
    $("#"+label_id+"").fadeTo(10000, 500).slideUp(500, function(){
		$("#"+label_id+"").slideUp(500);
    });   
    addInfoNotification(message); 
}

/**
 * Warning level alert popdown in alert-area 
 * 
 * @param label
 * @param message
 * @returns
 */
function alertWarning(label, message){ 
	var label_id = label.replace(/\s/g, '_'); //replace spaces with underscores
	label_id = label_id + getRandomInt(1,9999); //give a random id for duplicate labeled alerts
	var alert = buildAlert(label_id, message, 'alert-warning', label); 
	$('#alert-area').prepend(alert).fadeIn('slow'); 
    $("#"+label_id+"").fadeTo(10000, 500).slideUp(500, function(){
		$("#"+label_id+"").slideUp(500);
    });   
    addWarningNotification(message); 
}

/**
 * Success level alert popdown in alert-area 
 * 
 * @param label - must be one continuous word for css selectors
 * @param message
 * @returns
 */
function alertSuccess(label, message){ 
	var label_id = label.replace(/\s/g, '_'); //replace spaces with underscores
	label_id = label_id + getRandomInt(1,9999); //give a random id for duplicate labeled alerts
	var alert = buildAlert(label_id, message, 'alert-success', label); 
	$('#alert-area').prepend(alert).fadeIn('slow'); 
    $("#"+label_id+"").fadeTo(10000, 500).slideUp(500, function(){
    			$("#"+label_id+"").slideUp(500);
         });   
    addSuccessNotification(message); 
}

/**
 * Error level alert popdown in alert-area 
 * 
 * @param label
 * @param message
 * @returns
 */
function alertError(label, message){ 
	var label_id = label.replace(/\s/g, '_'); //replace spaces with underscores
	label_id = label_id + getRandomInt(1,9999); //give a random id for duplicate labeled alerts
	var alert = buildAlert(label_id, message, 'alert-danger', label); 
	$('#alert-area').prepend(alert).fadeIn('slow'); 
    $("#"+label_id+"").fadeTo(10000, 500).slideUp(500, function(){
		$("#"+label_id+"").slideUp(500);
    });   
    addErrorNotification(message); 
}

/**
 * Build the alert div on the fly so we can later implement different styling for alerts
 * 
 * @param label
 * @param message
 * @param level
 * @returns
 */
function buildAlert(label_id, message, level, label){ 
	return '<div id="'+label_id+'" class="alert alert-dismissable ' + level + '">'+
			   '<button type="button" class="close" data-dismiss="alert" aria-label="Close">'+
			   '<span aria-hidden="true">&times;</span></button>' +
			   '<strong>' + label + '</strong> | ' + message + 
			   '</div>';
}

/**
 * Execute GET request for the uri and display it in the main-content-body
 * 
 * @param uri
 * @returns
 */
function getRenderMainContent(uri)
{
	$.get(uri, function(response){
		$("#main-content-body").html(response); 
	}); 
}

/**
 * Execute GET request for the uri and show a content-popup containing
 * the response
 * 
 * @param uri - GET request URI
 * @returns
 */
function getRenderPopupContent(uri)
{
	$.get(uri, function(reponse){
		$("#content-popup").html(response); 
		$("#content-popup").show(); 
	});
}

/**
 * Helper for getting formatted current date 
 * month/day/year @ hh:mm:s(s) 
 * 
 * @returns string - formatted current date time
 */
function getDate(){
	var currentdate = new Date(); 
	var datetime =  (currentdate.getMonth()+1)  + "/" //
					+ currentdate.getDate() + "/"
					+ currentdate.getFullYear() + " @ "  
	                + currentdate.getHours() + ":"  
	                + currentdate.getMinutes() + ":" 
	                + currentdate.getSeconds();
	return datetime; 
}

function updateHiddenColumns(tableName, columns){ 
	
	$.post("/airflowwcs/userpreference/updateColumnPref?user=" + userId + "&val=" + generateColumnVisibilityPreference(tableName,columns), function(response){
		notifyOnResponse(response); 
	});
	
}

function generateColumnVisibilityPreference(tableName,columns)
{
	var booleanArrayString = tableName+"-"; 
	for(var i = 0; i<columns.length; i++){
		if(columns[i]===true){
			booleanArrayString += "1"; 
		}else{
			booleanArrayString += "0"; 
		}
		if(i!=columns.length-1){
			booleanArrayString += ","
		}
		
	}
	return booleanArrayString
}

/**
 * Define error handling for ajax requests accross application to 
 * be redirected to our bootstrap alert box
 */
$.ajax({
	  statusCode: {
	    400: function() {
	      alertError('HTTP Status 400', 'Bad request, contact your system administrator');
	    },
	    403: function(){
	      alertError('HTTP Status 403', 'Forbidden, please check login status by refreshing');
	    },
	    404: function(){
		  alertError('HTTP Status 404', 'Request not found, contact your system administrator');
		},
		405: function(){ 
			alertError('HTTP Status 405', 'Request method is invalid, contact your system administrator')
		},
		408: function(){ 
			alertError('HTTP Status 408', 'Request method timed out, contact your system administrator')
		},
	    500: function() {
	      alertError('HTTP Status 500', 'Internal server error, contact your system administrator');
	    }
	  }
});


function alertPopupConfirm(response)
{
	
}

function executeFunctionByName(functionName, context , args) {
	  var args = Array.prototype.slice.call(arguments, 2);
	  var namespaces = functionName.split(".");
	  var func = namespaces.pop();
	  for(var i = 0; i < namespaces.length; i++) {
	    context = context[namespaces[i]];
	  }
	  return context[func].apply(context, args);
}

/* Generic Confirm func */
function confirmPopup(heading, question, cancelButtonTxt, okButtonTxt, callback) {

  var confirmModal = 
    $('<div class="modal fade" id="confirm'+heading+'" >' +        
        '<div class="modal-dialog">' +
        '<div class="modal-content">' +
        '<div class="modal-header">' +
          '<a class="close" data-dismiss="modal" >&times;</a>' +
          '<h3>' + heading +'</h3>' +
        '</div>' +

        '<div class="modal-body">' +
          '<p>' + question + '</p>' +
        '</div>' +

        '<div class="modal-footer">' +
          '<a href="#!" class="btn" data-dismiss="modal">' + 
            cancelButtonTxt + 
          '</a>' +
          '<a href="#!" id="okButton" class="btn btn-primary">' + 
            okButtonTxt + 
          '</a>' +
        '</div>' +
        '</div>' +
        '</div>' +
      '</div>');

  confirmModal.find('#okButton').click(function(event) {
    callback();
    confirmModal.modal('hide');
  }); 

  confirmModal.modal('show');    
};  
/* END Generic Confirm func */


/**
 * Regular expression DataTable row highlighting 
 */
/*var hlBegin = '<strong class="highlight">',  // generic highlight
	successHlBegin = '<strong class="sucessHighlight">',
	failureHlBegin = '<strong class="failureHighlight">'.
	warningHlBegin = '<strong class="warningHighlight">',
    hlEnd = '</strong>';

function removeCellTextHighlight() {
    var row, str,
        rowCount = table.rows().nodes().length;

    for (row=0; row<rowCount; row++) {
        str = table.cell(row, 1).data();
        str = str.replace(/(<([^>]+)>)/ig, '');
        table.cell(row, 1).data(str).draw();
    }        
}

function rowBackgroundHighlight(term,dt,index,color){
	 var row, str,
     rowCount = dt.rows().nodes().length,
     regexp = new RegExp('('+term+')','ig');
  
 for (row=0;row<rowCount;row++) {
     str = table.cell(row, index).data();
     str = str.replace(regexp, function($1, match) { 
        return hlBegin + match + hlEnd;
     })
     dt.cell(row, index).data(str).draw();
 }    
}

function cellTextHighlight(term, dt, index, color) {
    var row, str,
        rowCount = dt.rows().nodes().length,
        regexp = new RegExp('('+term+')','ig');
     
    for (row=0;row<rowCount;row++) {
        str = table.cell(row, index).data();
        str = str.replace(regexp, function($1, match) { 
           return hlBegin + match + hlEnd;
        })
        dt.cell(row, index).data(str).draw();
    }        
}  */  


