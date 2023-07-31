

$(document).ready(function() {
	
	var table = $('#ajaxTable').DataTable();
		
	$("#searchItemsButton").click(function() {
		$("#divExecuting").show(); 
		searchItems();		
    });
	
	$("#minQty").on('keyup', function (e) {
	    if (e.keyCode == 13) {
	    	if (!isNaN($("#minQty").val()))
	    	{
	    		$("#maxQty").select(); 
	    	}
	    	else
    		{
    		   alert("Value must be numeric(Integer). Zero or greater.");
    		   $("#minQty").select();
    		}
	    }
	});
	
	$("#maxQty").on('keyup', function (e) {
	    if (e.keyCode == 13) {
	    	if (!isNaN($("#maxQty").val()))
	    	{
	    		var min = parseInt($("#minQty").val());
	    		var max = parseInt($("#maxQty").val());
	    		
	    		if (min > max)
	    		{
	    			alert("Min value cannot be greater than Max value.");
	    		}
	    		else
	    		{
	    			$("#minmax-modify-button").focus(); 
	    		}
	    	}
	    	else
    		{
    		   alert("Value must be numeric(Integer). Zero or greater.");
    		   $("#maxQty").select();
    		}
	    }
	});
	
	$("#defaultToteQty").on('keyup', function (e) {
	    if (e.keyCode == 13) {
	    	if (!isNaN($("#defaultToteQty").val()))
	    	{
	    		$("#toteqty-modify-button").focus();  
	    	}
	    	else
    		{
    		   alert("Value must be numeric(Integer). Zero or greater.");
    		   $("#defaultToteQty").select();
    		}
	    }
	});
	
	function showModifyMinMax()
	{ 
		clearMinMaxModal();
		$('#minmax-modify-modal').modal('show'); 
	}
	
	$("#minmax-modify-button").click(function() 
	{
		var min = parseInt($("#minQty").val());
		var max = parseInt($("#maxQty").val());
		
		if (min > max)
		{
			alert("Min value cannot be greater than Max value.");
		}
		else
		{
			$("#minmax-modify-modal").modal('hide');
			$("#divExecuting").show(); 
			modifyMinMaxRows();	
		}
    });

	function showModifyToteQty()
	{ 
		clearToteQtyModal();
		$('#toteqty-modify-modal').modal('show'); 
	}
	
	$("#toteqty-modify-button").click(function() 
	{
    	if (!isNaN($("#defaultToteQty").val()))
    	{
    		$("#toteqty-modify-modal").modal('hide');
    		$("#divExecuting").show(); 
    		modifyDefToteQtyRows();	
    	}
    	else
		{
 		   alert("Value must be numeric(Integer). Zero or greater.");
		   $("#defaultToteQty").select();		
		}
    });
	
	
	function searchItems()
	{
    		
		table.ajax.url('/airflowwcs/item/listSearch?item='+$("#item").val()+"&articleNo="+$("#article").val()+"&supplier="+$("#supplier").val()+"&division="+$("#division").val()+"&description="+$("#description").val()).load(function() {
			$("#divExecuting").hide();
		}); 

	}	
	
	/************************************************************
	 * 	 CLEAR MODIFY SCREEN FIELDS
	 ************************************************************/
	function clearMinMaxModal()
	{
		$('#minQty').val(''); 
		$('#maxQty').val(''); 
	}
	
	function clearToteQtyModal()
	{
		$('#defaultToteQty').val(''); 
	}
	
	function modifyMinMaxRows()
	{
		if(selectedRows.length>0)
		{
			$.ajax({
			    type : "POST",
			    url : "/airflowwcs/item/modifyMinMax",
			    data : {
			        itemIds: selectedRows, 
			        minQty:	  $("#minQty").val(),
			        maxQty:   $("#maxQty").val()
			    },
			    success : function(response) {			    	  
			    	$("#divExecuting").hide();
			       alertOnResponse(response); 
			       table.ajax.reload();
			    },
			    error : function(xhr, status, e) {
			    	$("#divExecuting").hide();
			       alertError('Ajax Error', xhr.responseText);
			       table.ajax.reload();
			    }
			});
		}
		else
		{
			$("#divExecuting").hide();
			alertError("Selection","No rows selected! Select a row and retry.")
		}
	}

	function modifyDefToteQtyRows()
	{
		if(selectedRows.length>0)
		{
			$.ajax({
			    type : "POST",
			    url : "/airflowwcs/item/modifyToteQty",
			    data : {
			        itemIds: selectedRows, 
			        toteQty:	  $("#defaultToteQty").val()
			    },
			    success : function(response) {			    	  
			    	$("#divExecuting").hide();
			       alertOnResponse(response); 
			       table.ajax.reload();
			    },
			    error : function(xhr, status, e) {
			    	$("#divExecuting").hide();
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
	
	function modifyDOHAutoRows()
	{
		$("#divExecuting").show();
		if(selectedRows.length>0)
		{
			$.ajax({
			    type : "POST",
			    url : "/airflowwcs/item/modifyDOHAuto",
			    data : {
			        itemIds: selectedRows
			    },
			    success : function(response) {			    	  
			    	$("#divExecuting").hide();
			       alertOnResponse(response); 
			       table.ajax.reload();
			    },
			    error : function(xhr, status, e) {
			    	$("#divExecuting").hide();
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
	
	function modifyDOHManualRows()
	{
		$("#divExecuting").show();
		if(selectedRows.length>0)
		{
			$.ajax({
			    type : "POST",
			    url : "/airflowwcs/item/modifyDOHManual",
			    data : {
			        itemIds: selectedRows
			    },
			    success : function(response) {			    	  
			    	$("#divExecuting").hide();
			       alertOnResponse(response); 
			       table.ajax.reload();
			    },
			    error : function(xhr, status, e) {
			    	$("#divExecuting").hide();
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
	
	if(isAdmin) //admin specific context menu
	{
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: "Modify Min/Max Qty", cmd: "minmax"},
				{title: "Modify Default Tote Qty", cmd: "toteqty"},
			//	{title: "Delete", cmd: "delete" },
				{title: "----"},
				{title: "Mark DOH - Automatically Update", cmd: "dohAuto"},
				{title: "Mark DOH - Manually Update", cmd: "dohManual"}

				], 
				select: function(event, ui) {

					var idText = table.row(ui.target).id(); 
					var celltext = ui.target.text();
					switch(ui.cmd){
					case "minmax":
						showModifyMinMax(); 
						break; 
					case "dohAuto":
						modifyDOHAutoRows();
						break;
					case "dohManual":
						modifyDOHManualRows();
						break;
					case "toteqty":
						showModifyToteQty();
						break;
					case "delete":
						deleteItem(idText); 
						break;
					default:
						alert("ERROR: option not recognized."); 
						break;
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
			
				{title: "----"}
				], 
				select: function(event, ui) {

					var idText = table.row(ui.target).id(); 
					var celltext = ui.target.text();
					switch(ui.cmd){
					default:
						break;
					}
				},
				beforeOpen: function(event, ui) {
					var $menu = ui.menu,
					$target = ui.target,
					extraData = ui.extraData;

				}
		});
	}
	
	$("#item-modify-form").submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#modify-modal .close").click()
			alertOnResponse(response); 
			table.ajax.reload(); 
			table.draw(); 
		}); 
		return false; 
	});


	
	$('#item-modify-button').click(function(){
		
		$('#item-modify-form').submit();
	});

} );

