function toggleUpdatePassword(){
	var ro = document.getElementById('password').readOnly;
	if(ro)
	{
		document.getElementById('password').readOnly = false;
		document.getElementById('passwordConfirm').readOnly = false;
	}
	else
	{
		document.getElementById('password').readOnly = true;
		document.getElementById('passwordConfirm').readOnly = true;
	}
}

function onDrillDown(userId){
	$.post('/airflowwcs/users/find?user='+userId, $(this).serialize(), function(response){
		for(var key in response){
			if(response.hasOwnProperty(key)){
				$("#"+key+"Mod").html(response[key]);
				$("#"+key+"Mod").val(response[key]);
			}
		}
	});
	$('#modify-modal').modal('show');
}

/* Password Validation */

function validPasswordLength(dlg){
	 return $("#password" + dlg).val().length > 3;
}
function validConfirmPassword(dlg){
	console.log($("#password" + dlg).val());
	console.log($("#confirmPassword" + dlg).val());
	return $("#password" + dlg).val() == $("#confirmPassword" + dlg).val();
}
function checkPassword(dlg){
	var helper = $("#passwordHelp" + dlg);
	helper.empty();
	if(!validPasswordLength(dlg)){
		helper.append("<div class=\"alert alert-danger\"><strong>Invalid password length, password must be at least 4 characters.</strong></div>");
		return false;
	}else if(!validConfirmPassword(dlg)){
		helper.append("<div class=\"alert alert-danger\"><strong>Passwords do not match.</strong></div>");
		return false;
	}else{
		helper.empty();
		return true;
	}
}

$(document).ready(function() {

	/**
	 * Context action popups.
	 * @param loadId
	 **/
	function showModifyUser(userId){
		$.post('/airflowwcs/users/find?user='+userId, $(this).serialize(), function(response){
			for(var key in response){
				if(response.hasOwnProperty(key)){
					$("#"+key+"Mod").val(response[key]);
					$("#"+key+"Mod2").val(response[key]);
				}
			}
		});
		$('#modify-modal').modal('show');
	}

	function deleteUser(userId){
		confirmDlg("Confirm User Deletion", "Delete user " + userId + "?", "Delete User", function() {
			deleteUserConfirmed(userId);
		});
	}
	function deleteUsers(userIds){
		confirmDlg("Confirm User Deletion", "Delete " + userIds.length + " selected users?", "Delete User", function() {
			for (i = 0; i < userIds.length; i++)
			{
				deleteUserConfirmed(userIds[i]);
			}
		});
	}
	function deleteUserConfirmed(userId)
	{
		$.post('/airflowwcs/users/delete?user='+userId, $(this).serialize(), function(response) {
			confirmDlgComplete();
			alertOnResponse(response);
			table.ajax.reload();
		});
	}

	$(document).contextmenu({
		delegate: ".dataTable td",
		menu: [
			{title: "Modify", cmd: "modify"},
			{title: "Delete", cmd: "delete" }
			],
			select: function(event, ui) {

				var idText = table.row(ui.target).id();
				var celltext = ui.target.text();
				switch(ui.cmd){
				case "modify":
					showModifyUser(idText);
					break;
				case "delete":
					if (selectedRows.length > 1)
						deleteUsers(selectedRows);
					else
						deleteUser(idText);
					break;
				default:
					alert("ERROR: option not recognized.");
				}
			},
			beforeOpen: function(event, ui) {
				var $menu = ui.menu,
				$target = ui.target,
				extraData = ui.extraData;
				if (!ui.target.parent().hasClass('selected'))
					$(ui.target).click();
			}
	});

	$('#user-add-form').submit(function(){
		if (checkPassword("")) {
			$.post($(this).attr('action'), $(this).serialize(), function(response){
				$("#add-modal .close").click()  // close modal
				alertOnResponse(response); // alert if success or fail
				table.ajax.reload(); // reload the results
				table.draw();
			});
		}
		return false;
	});
	$("#add-modal").on('hide.bs.modal', function(){
		$('#user-add-form :input').val('')
		$("#user-add-form #role")[0].selectedIndex = 0;
		$("#passwordHelpAdd").empty();
	});

	$("#user-modify-form").submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$("#modify-modal .close").click()
			alertOnResponse(response);
			table.ajax.reload();
			table.draw();
		});
		return false;
	});

	$("#user-password-modify-form").submit(function(){
		$.post($(this).attr('action'), $(this).serialize(), function(response){
			$('#reset-password-modal .close').click()
			$("#modify-modal .close").click()
			alertOnResponse(response);
			table.ajax.reload();
			table.draw();
		});
		return false;
	});

	$('#user-add-button').click(function(){
		$('#user-add-form').submit();
	});

	$('#user-modify-button').click(function(){
		$('#user-modify-form').submit();
	});

	$('#reset-password-button').click(function(){
		$("#passwordHelpMod2").empty();
		$('#reset-password-modal').modal('show');
	});

	$('#user-password-modify-button').click(function(){
		if (checkPassword("Mod2")) {
			$('#user-password-modify-form').submit();
		}
	});

} );

