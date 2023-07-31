
function getRoleEntryTemplate(role)
{
	return "<div class=\"row\">\n" 
	+"<div class=\"col-sm-1\"></div>"
	+"<div class=\"col-sm-10\">"
		+"<div class=\"small-card\">"
		 	+"<div class=\"card-container-header\"><center><h4><b>"+role["Group Name"]+"</b></h4></center></div>"
		 			+"<div class=\"card-container\">"
		 				+"<p>" + role["Description"] + "</p>"
		 			+"</div>"
		 +"</div>"
	+"</div>"
	+"<div class=\"col-sm-1\"></div>"
	+"</div>";
}
					

$(document).ready(function() {
	
	$.get( "/airflowwcs/userpreference/authgroup", function( data ) {
		for (var i = 0; i < data.length; i++) {
		    $("#user-permission-body").append(getRoleEntryTemplate(data[i]));
		}
	});
	
	$("#debug-mode-dropdown").on('change', function(){
		updateDebugPreference($("#debug-mode-dropdown").val(), $("#userId").val()); 
	});
	$("#theme-dropdown").on('change', function(){
		updateUserThemePreference($("#theme-dropdown").val(), $("#userId").val()); 
	});
	
	$("#sidebar-lock-dropdown").on('change', function(){
		updateSidebarLockPreference($("#sidebar-lock-dropdown").val(), $("#userId").val()); 
	});
	
	function updateUserThemePreference(theme, userId)
	{
		if(theme==="dark"||theme==="light"||theme==="default"||theme==="blue")
		{
			$.post('/airflowwcs/userpreference/updateTheme?user='+userId+'&val='+theme, $(this).serialize(), function(response) { 
		    	alertOnResponse(response); 
		    	if(response.responseCode==1)
		    	{
		    		location.reload(true); 
		    	}
		    });
		}else
		{
			alertError("INVALID", "Invalid selection: " + theme + " for user ID: " + userId); 
		}
	}
	
	function updateSidebarLockPreference(val, userId)
	{
		if(val==="YES"||val==="NO")
		{
			$.post('/airflowwcs/userpreference/updateLockSidebar?user='+userId+'&val='+val, $(this).serialize(), function(response) { 
		    	alertOnResponse(response); 
		    	if(response.responseCode==1)
		    	{
		    		location.reload(true); 
		    	}
		    });
		} else {
			alertError("INVALID", "Invalid selection: " + val + " for user ID: " + userId); 
		}
	}
	
	function updateDebugPreference(debugVal, userId)
	{
		if(debugVal=="1") {
			console.log('Enabling debug.')
		}
		if(debugVal=="0") {
			console.log('Disabling debug')
		}
		if(debugVal=="1"||debugVal=="0")
		{
			$.post('/airflowwcs/userpreference/updateDebug?user='+userId+'&val='+debugVal, $(this).serialize(), function(response) { 
		    	alertOnResponse(response); 
		    	if(response.responseCode==1)
		    	{
		    		location.reload(true); 
		    	}
		    });
		}
		else
		{
			alertError("INVALID", "Invalid selection: " + debugVal + " for user ID: " + userId); 
		}
	}
});