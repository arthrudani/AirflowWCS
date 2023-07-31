var selectedUsers = [];
var selectedDeleteUsers = [];

function onDrillDown(data)
{
	showDetail(data);
}

/**
 * Show the detail for a currently selected permission group.
 * First hide the other details that may be shown and update
 * UI elements with new data.
 *
 * @param data
 */
function showDetail(data)
{
	hidePermissionDetailCards()
	switch(data){
		case 'ROLE_ADMIN':
			selectedRole = 'ROLE_ADMIN';
			$("#role-admin-card").fadeIn( "fast" );
			break;
		case 'ROLE_ELEVATED':
			selectedRole = 'ROLE_ELEVATED';
			$("#role-elevated-card").fadeIn( "fast" );
			break;
		case 'ROLE_MASTER':
			selectedRole = 'ROLE_MASTER';
			$("#role-master-card").fadeIn( "fast" );
			break;
		case 'ROLE_READONLY':
			selectedRole = 'ROLE_READONLY';
			$("#role-readonly-card").fadeIn( "fast" );
			break;
		case 'ROLE_USER':
			selectedRole = 'ROLE_USER';
			$("#role-user-card").fadeIn( "fast" );
			break;
		default:
			selectedRole = 'NONE_SELECTED';
			$("#role-unknown-card").fadeIn( "fast" );
			break;
	}
	updateUsersInGroupTable(selectedRole);
	updateAddUserGroupFields(selectedRole);
	updateDeleteUserGroupFields(selectedRole);

}

/**
 * A chained function to update the page UI elements with the given group
 * name data.
 *
 * @param group
 */
function updatePageWithGroupDetails(group)
{
	updateUsersInGroupTable(group);
	updateAddUserGroupFields(group);
	updateDeleteUserGroupFields(group);
	updateGroupUserCounts();
	selectedUsers=[];
	selectedDeleteUsers=[];
}

/**
 * Add the name of the given role to the 'revoke user from group' detail modal.
 * @param group
 * @returns
 */
function updateDeleteUserGroupFields(group)
{
	$("#group-delete-name-title").html("<strong>"+group+"</strong>");
	$("#group-delete-name-div").html("<strong>"+group+"</strong>");
	tableDeleteUsers.ajax.url('/airflowwcs/userpermission/users/'+group).load();
}

/**
 * Update the ajax URL of the Users "In Group" table at bottom of page with the
 * given group name.
 *
 * @param group
 * @returns
 */
function updateUsersInGroupTable(group)
{
	tableUsersInGroup.ajax.url('/airflowwcs/userpermission/users/'+group).load();
}

/**
 * Add the name of the given role to the 'add user to group' detail modal.
 * @param group
 * @returns
 */
function updateAddUserGroupFields(group)
{
	$("#group-select-name-title").html("<strong>"+group+"</strong>");
	$("#group-select-name-div").html("<strong>"+group+"</strong>");
	tableAddUsers.ajax.url('/airflowwcs/userpermission/users/notGroup/'+selectedRole).load();
}

/**
 * Hide all the permission detail cards in the page.
 *
 */
function hidePermissionDetailCards()
{
	$("#role-admin-card").css("display", "none");
	$("#role-elevated-card").css("display", "none");
	$("#role-master-card").css("display", "none");
	$("#role-readonly-card").css("display", "none");
	$("#role-unknown-card").css("display", "none");
	$("#role-user-card").css("display", "none");

}

/**
 * Ajax request GET the count of users in each AuthGroup.
 * Display the results in the page. AlertOnResponse
 *
 */
function updateGroupUserCounts()
{
	$.ajax({
		url: '/airflowwcs/userpermission/count/users',
		type: 'GET',
		async: true,
		cache: false,
		timeout: 30000,
		error: function(){
			alert("ERROR: Unable to retrieve counts for User Groups: ");
			return true;
		},
		success: function(response){
			console.log(response);
			for(var i = 0; i<response.length; i++)
			{
				var row = response[i];
				console.log("ROW | " + row);
				$("#" + row['Group Name'] +"_COUNT").html(row['Count']);
			}
		}
	});
}

function getSelected(selectingTable)
{
	var selected = [];
	selectingTable.rows('.selected').every(function(rowIdx) {
		selected.push(selectingTable.row(rowIdx).id());
	});
	return selected;
}


/**
 * On page load/Ready
 */
$(document).ready(function(){
	tableUserPermissionGroup.select.style( 'single' );
	tableUsersInGroup.buttons('0').disable(); // Add
	tableUsersInGroup.buttons('2').disable(); // Delete

	/**
	 * Table listener - whenever a row is clicked in the table get the description
	 * field in the table and place it in the detail card that will be shown.
	 */
	$('#ajaxTableUserPermissionGroup tbody').on('click', 'tr', function(){
		var selectedGroup = getSelected(tableUserPermissionGroup);
		var data;
		if (selectedGroup.length == 0) {
			data = "NONE_SELECTED";	
			tableUsersInGroup.buttons('0').disable(); // Add
			tableUsersInGroup.buttons('2').disable(); // Delete
		} else {
			data = selectedGroup[0];
			tableUsersInGroup.buttons('0').enable(); // Add
			tableUsersInGroup.buttons('2').enable(); // Delete
		}
		onDrillDown(data);
	});

	/**
	 * Every time an row item is selected or deselected in the ADD USER TO GROUP ajaxtable,
	 * update the array of selected ids with all currently selected.
	 */
	if(typeof tableAddUsers !== 'undefined'){
		if(tableAddUsers!=null){
			tableAddUsers.on('select.dt', function() {
				selectedUsers = getSelected(tableAddUsers);
			});
			tableAddUsers.on('deselect.dt', function() {
				selectedUsers = getSelected(tableAddUsers);
			});
		}
	}
	
	/**
	 * Every time a row item is selected or deselected in the REVOKE USER FROM GROUP ajaxtable,
	 * update the array of selected ids with all currently selected.
	 */
	if(typeof tableDeleteUsers !== 'undefined'){
		if(tableDeleteUsers!=null){
			tableDeleteUsers.on('select.dt', function() {
				selectedDeleteUsers = getSelected(tableDeleteUsers);
			});
			tableDeleteUsers.on('deselect.dt', function() {
				selectedDeleteUsers = getSelected(tableDeleteUsers);
			});
		}
	}

	/**
	 * Listener for add usere to group button click
	 */
	$("#user-grant-button").on("click", function(){
		if (selectedUsers.length == 0) {
			$("#add-modal").modal('hide');
			alertWarning('Selection Error', 'No users selected.');
		} else {
			$.ajax({
				type : "POST",
				url : "/airflowwcs/userpermission/users/add/"+selectedRole,
				data : {
					users: selectedUsers //notice that "orderIds" matches the value for @RequestParam
				},
				success : function(response) {
					$("#add-modal").modal('hide');
					alertOnResponse(response);
					updatePageWithGroupDetails(selectedRole);
				},
				error : function(xhr, status, e) {
					$("#add-modal").modal('hide');
					alertError('Ajax Error', xhr.responseText);
					updatePageWithGroupDetails(selectedRole);
				}
			});
		}
	});

	/**
	 * Listener for revoke users from group button click
	 */
	$("#user-revoke-button").on("click", function(){
		if (selectedDeleteUsers.length == 0) {
			$("#delete-modal").modal('hide');
			alertWarning('Selection Error', 'No users selected.');
		} else {
			$.ajax({
				type : "POST",
				url : "/airflowwcs/userpermission/users/revoke/"+selectedRole,
				data : {
					users: selectedDeleteUsers //notice that "orderIds" matches the value for @RequestParam
				},
				success : function(response) {
					$("#delete-modal").modal('hide');
					alertOnResponse(response);
					updatePageWithGroupDetails(selectedRole);
				},
				error : function(xhr, status, e) {
					$("#delete-modal").modal('hide');
					alertError('Ajax Error', xhr.responseText);
					updatePageWithGroupDetails(selectedRole);
				}
			});
		}
	});

	/**
	 * 				COLUMN REDRAW HACK
	 * after clicking slight delay then redraw tables to fix styling problems
	 * pretty visible on modals, less visible in tabs/page load
	 *
	 */
	$("#in-group-tab").on("click", function(){
		window.setTimeout(function(){ tableUsersInGroup.draw();}, 175);
	});

	$("#all-users-tab").on("click", function(){
		window.setTimeout(function(){ tableAllUsers.draw();}, 75);
	});

	$('#add-modal').on('shown.bs.modal', function (e) {
		window.setTimeout(function(){ tableAddUsers.draw();}, 75);
	});

	$('#delete-modal').on('shown.bs.modal', function (e) {
		window.setTimeout(function(){ tableDeleteUsers.draw();}, 75);
	});

	$("#all-users-tab").on('shown.bs.tab', function (e) {
		window.setTimeout(function(){ tableAllUsers.draw();}, 75);
	});

	updateGroupUserCounts(); // Get the counts of each group for display on the page load
});