$(document).ready(function(){ 
	var table = $('#ajaxTable').DataTable();
	
	$('#ajaxTable tbody').on('click', 'tr', function(){
		var data = table.row(this).id(); 
		var columnData = table.row(this).data();
		console.log(columnData);
	});
	
	$("#ready_msg").on("click", function(){
		$("#confirm-ready-modal").modal('show'); 
	}); 
	
	$("#done_msg").on("click", function(){
		$("#confirm-done-modal").modal('show'); 
	}); 
	

	/***************************************************************
	 * 					RIGHT CLICK CONTEXT MENU
	 **************************************************************/
	$(document).contextmenu({
		delegate: ".dataTable td",
		menu: [
			{title: "Mark Ready", cmd: "ready"},
			{title: "Mark Done", cmd: "done" },
			{title: "Delete", cmd: "delete" },
			], 
			select: function(event, ui) {

				var idText = table.row(ui.target).id(); 
				var celltext = ui.target.text();
				switch(ui.cmd){
				case "ready":
					readyMsgRows(); 
					break; 
				case "done":
					doneMsgRows(); 
					break;
				case "delete":
					deleteMsgRows(); 
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
	/***************************************************************
	 * 					END RIGHT CLICK CONTEXT MENU
	 **************************************************************/	
	$('#knapp-add-form').submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#add-modal .close").click()  // close modal
			alertOnResponse(response); // alert if success or fail 
			table.ajax.reload(); // reload the results
			table.draw(); 
		}); 
		return false;
	});
	
	$('#knapp-add-button').click(function(){
		
		$('#knapp-add-form').submit();
	});
}); 

function readyMsgRows()
{
	if(selectedRows.length >0 )
	{
		$.ajax({
		    type : "POST",
		    url : "/airflowwcs/knappmessage/markReady",
		    data : {
		        seqIds: selectedRows //notice that "orderIds" matches the value for @RequestParam
		                   
		    },
		    success : function(response) {
		    	$("#confirm-ready-modal").modal('hide'); 
		       alertOnResponse(response); 
		       table.ajax.reload();
		    },
		    error : function(xhr, status, e) {
		    	$("#confirm-ready-modal").modal('hide'); 
		       alertError('Ajax Error', xhr.responseText);
		       table.ajax.reload();
		    }
		}); 	
	}
	else
	{
		alertError("Selection","No rows selected! Select a row and retry.")
	}
	
}

function doneMsgRows()
{
	if(selectedRows.length>0)
	{
		$.ajax({
		    type : "POST",
		    url : "/airflowwcs/knappmessage/markDone",
		    data : {
		        seqIds: selectedRows //notice that "orderIds" matches the value for @RequestParam
		                  
		    },
		    success : function(response) {
		    	$("#confirm-done-modal").modal('hide'); 
		       alertOnResponse(response); 
		       table.ajax.reload();
		    },
		    error : function(xhr, status, e) {
		    	
		    	$("#confirm-done-modal").modal('hide'); 
		       alertError('Ajax Error', xhr.responseText);
		       table.ajax.reload();
		    }
		}); 
	}
	else
	{
		alertError("Selection","No rows selected! Select a row and retry.")
	}
}

function deleteMsgRows()
{
	if(selectedRows.length>0)
	{
		$.ajax({
		    type : "POST",
		    url : "/airflowwcs/knappmessage/delete",
		    data : {
		        seqIds: selectedRows //notice that "orderIds" matches the value for @RequestParam
		                  
		    },
		    success : function(response) {
		    	$("#delete-modal").modal('hide');  
		       alertOnResponse(response); 
		       table.ajax.reload();
		    },
		    error : function(xhr, status, e) {
		    	$("#delete-modal").modal('hide'); 
		       alertError('Ajax Error', xhr.responseText);
		       table.ajax.reload();
		    }
		});
	}
	else
	{
		alertError("Selection","No rows selected! Select a row and retry.")
	}

}

