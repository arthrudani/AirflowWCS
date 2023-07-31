var isAdmin = false;
$(document).ready(function() {
	/**
	 * Context action popups.
	 * 
	 * @param loadId
	 * @param item
	 */
	function showModifyItemDetail(loadId, item) {

		$.post('/airflowwcs/itemdetail/find?load=' + loadId + '&item=' + item, $(
				this).serialize(), function(response) {
			for ( var key in response) {
				if (response.hasOwnProperty(key)) {
					$("#" + key + "Mod").html(response[key]);
					$("#" + key + "Mod").val(response[key]);
					$("#" +key+ " option[value='" + response[key] + "']").prop('selected', true);
				}
			}
		});
		$('#item-detail-modify-modal').modal('show');
	}
	/**
	 * Context action popups for add new item.
	 * 
	 */
	function showAddItemDetail() {
		
		$('#item-detail-add-modal').modal('show');
	}
	function deleteItemDetail(loadid,item){

	$.confirm({
		    title: 'Confirm Delete',
		    content: 'Are you sure you want to delete item <strong>' + item +'</strong> from Load <strong>' + loadid +'</strong>?',
		    buttons: {
		        confirm: function () {
		    		$("#divExecuting").show();
		    	    $.post('/airflowwcs/itemdetail/delete?load='+loadid + '&item=' + item, $(this).serialize(), function(response) { 
		    	    	$("#divExecuting").hide();
		    	    	alertOnResponse(response); 
		    			table.ajax.reload();
		    	    });
		        },
		        cancel: function () {
		            //do nothing
		        }
		    }
		});
	}
	
	if(isAdmin) //admin specific context menu
	{
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: "Add Item", cmd: "addItem"},
				{title: "Modify Item", cmd: "modify"}
				//,{title: "Delete Item", cmd: "delete"} KR:TODO
				], 
				select: function(event, ui) {
					var columnData = table.row(ui.target).data();
					var item = "";
					var loadid = "";
					
					if (typeof  columnData != "undefined") {
					 	item =  columnData["BagID"]; 
					 	loadid =  columnData["TrayID"];
					}
					if( ui.cmd != "addItem" ){
						//Only ignore when creating a new item
						if(loadid == "" || item =="" ){
							alert("Select a row first");
							return;
						}
					} 

					switch(ui.cmd){
					case "addItem":
						showAddItemDetail(); //add new item/bag
						break; 
					case "modify":
						showModifyItemDetail(loadid,item); 
						break; 
					case "delete":
						deleteItemDetail(loadid,item); 
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
	$('#item-detail-modify-form').submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#item-detail-modify-modal .close").click()
			alertOnResponse(response); 
			table.ajax.reload(); 
			table.draw(); 
		}); 
		return false; 
	});
	$('#item-detail-modify-button').click(function(){
		
		$('#item-detail-modify-form').submit();
	});
	
	$('#item-detail-add-form').submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#item-detail-add-modal .close").click()
			alertOnResponse(response); 
			table.ajax.reload(); 
			table.draw(); 
		}); 
		return false; 
	});

	$('#item-detail-add-button').click(function(){
		
		$('#item-detail-add-form').submit();
	});
	


});
