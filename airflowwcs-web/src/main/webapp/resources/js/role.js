
function showModify(){
	$("#roleMod").val(''); 
	$("#roleDescriptionMod").val(''); 
	var idx = table.cell('.selected', 0).index();
	var data = table.row( idx.row ).data();
	var role = data.Role; 
	var description = data.Description; 
	$("#roleMod").val(role); 
	$("#roleDescriptionMod").val(description); 
    $("#role-modify-cancel-button").focus();
}

function showAdd(){
	$("#add-modal").modal('show'); 
}


function showDelete(){
	$("#roleModDelete").val(''); 
	$("#roleDescriptionModDelete").val(''); 
	var idx = table.cell('.selected', 0).index();
	var data = table.row( idx.row ).data();
	var role = data.Role; 
	var description = data.Description; 
	$("#roleModDelete").val(role); 
	$("#roleDescriptionModDelete").val(description); 
    $("#role-delete-cancel-button").focus();
}
function onDrillDown(){
	var idx = table.cell('.selected', 0).index();
	var data = table.row( idx.row ).data();
	var role = data.Role; 
	var description = data.Description; 
	tableRoleOption.ajax.url('/airflowwcs/role/option/list/'+role).load(); 
	$("#detail-modal").modal('show');
}


$(document).ready(function(){ 
	
	$("#role-add-form").submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#add-modal .close").click()
			alertOnResponse(response); 
			table.ajax.reload(); 
			table.draw(); 
		}); 
		return false; 
	});
	
	$("#role-add-button").on("click", function(){
		$("#role-add-form").submit(); 
	}); 
	
	$("#role-modify-form").submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#edit-modal .close").click()
			alertOnResponse(response); 
			table.ajax.reload(); 
			table.draw(); 
		}); 
		return false; 
	});
	
	$("#role-modify-button").on("click", function(){
		$("#role-modify-form").submit(); 
	}); 
	
	$("#role-delete-form").submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#delete-modal .close").click()
			alertOnResponse(response); 
			table.ajax.reload(); 
			table.draw(); 
		}); 
		return false; 
	});
	
	$("#role-delete-button").on("click", function(){
		$("#role-delete-form").submit(); 
	}); 
	
	$('#delete-modal').on('shown.bs.modal', function() {
		showDelete(); 
	})
	
	$('#edit-modal').on('shown.bs.modal', function() {
		showModify(); 
	})
	$("#detail-modal").on('shown.bs.modal', function(){
		tableRoleOption.columns.adjust().draw();
	})
	if(isAdmin) //admin specific context menu
	{
		$(document).contextmenu({
			delegate: ".dataTable td",
			menu: [
				{title: "Modify", cmd: "modify"},
				{title: "Delete", cmd: "delete" },
				{title: "----"},
				{title: "Show Details", cmd: "details" },
				{title: "----"}
			//	{title: "Retrieve", cmd: "retrieve" },
			//	{title: "Make Oldest", cmd: "oldest" }
				], 
				select: function(event, ui) {

					var idText = table.row(ui.target).id(); 
					var celltext = ui.target.text();
					switch(ui.cmd){
					case "modify":
						$("#edit-modal").modal('show'); 
						showModify(); 
						break; 
					case "delete":
						$("#delete-modal").modal('show'); 
						showDelete(); 
						break;
					case "details":
						var idx = table.cell('.selected', 0).index();
						var data = table.row( idx.row ).data();
						var role = data.Role; 
						var description = data.Description; 
						tableRoleOption.ajax.url('/airflowwcs/role/option/list/'+role).load(); 
						$("#detail-modal").modal('show'); 
						break;
				/**
					case "retrieve":
						showRetrieveLoad(idText); 
						break; 
					case "oldest": 
						makeOldest(idText); 
						break;
				**/
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
	
})
