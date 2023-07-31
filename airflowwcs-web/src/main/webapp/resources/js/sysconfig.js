/**
 * Page specific globals
 */

var isAdmin = false; 

/*****************************************************************
 * 						On page ready setup
 ****************************************************************/
$(document).ready(function(){ 
	
	var ccTable = $('#ajaxTableCCTable').DataTable();
	/**
	 * Disable some buttons when the page is ready, since we cannot inline these in the jsp
	 */
	//$("#release-from-tote-button").prop('disabled', true); 

	
	/**
	 * On click of tabs in bottom area, refresh tables. 
	 */
	$("#controllerTabToggle").on('click', function(){
		// Controller Configs
		window.setTimeout(function(){ tableCCTable.draw();}, 275); 
		//tableCCTable.ajax.reload(); 
	}); 
	$("#sysConfigTabToggle").on('click', function(){
		// System Configs
		window.setTimeout(function(){ tableSysConfigTable.draw();}, 275); 
		//tableSysConfigTable.ajax.reload(); 
	}); 
	
    
	$('#ajaxTableCCTable tbody').on('dblclick', 'tr', function(){

		var d = ccTable.row( this ).data();
		var data = ccTable.row(this).id(); 	

		if(selectedRows.length<=1){
			onCCDrillDown(data, 1); 
		}
		
	});
	
	$("#sysconfig-modify-button").on('click', function(){
		$('#sysconfig-modify-modal').modal('hide');
		$("#divExecuting").show();
		$.post('/airflowwcs/sysconfig/modifyCC?paramId='+$("#propertyNameMod").val()+"&paramValue="+$("#propertyValueMod").val(), $(this).serialize(), function(response){
			$("#divExecuting").hide();
			if(response.responseCode==1)
			{
				alertOnResponse(response); 
				tableCCTable.ajax.reload(); 
			}
			else
			{
				alert(response.responseMessage);
				alertOnResponse(response);
			}
		});

	}); 
	
	/***************************************************************
	 * 					RIGHT CLICK CONTEXT MENU
	 **************************************************************/
	if(isAdmin) //admin specific context menu
	{
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: " "},
				{title: "Modify Value", cmd: "modify" },
				{title: " "},
				], 
				select: function(event, ui) {
	
					var idText = ccTable.row(ui.target).id(); 
					var celltext = ui.target.text();
					var selectedRows = ccTable.rows('.selected').data().length;	
					switch(ui.cmd){
					case "modify":
						onCCDrillDown(idText, selectedRows);
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
	}
	
}); 

function onCCDrillDown(paramId, rows){
	
	
	if(rows==1)
	{
		$.post('/airflowwcs/sysconfig/findCC?paramId='+paramId, $(this).serialize(), function(response){
			for(var key in response)
			{
	    		if(response.hasOwnProperty(key))
	    		{
	    			$("#"+key+"Mod").html(response[key]);
	    			$("#"+key+"Mod").val(response[key]); 
	    		}
	    	}
		});
		$('#sysconfig-modify-modal').modal('show'); 
		$('#propertyValueMod').select();
	}
	else if (rows<1)
	{
		alert("Select a row to modify");
	}
	else
	{
		alert("Multi-select not allowed. Select only 1 row to modify");
	}
		
	
}


