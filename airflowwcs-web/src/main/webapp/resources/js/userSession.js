$(document).ready(function() {
	
	updateCounts(); 

}); 

function updateCounts()
{
	$.get( "/airflowwcs/usersession/count", function( data ) {
	    $("#num-active-login").html("<strong>"+data["Active"]+"</strong>"); 
	    $("#num-admin-login").html("<strong>"+data["Admin"]+"</strong>"); 
	    $("#num-unique-login").html("<strong>"+data["Unique"]+"</strong>"); 
	    $("#num-dup-login").html("<strong>"+data["Duplicate"]+"</strong>"); 
	});
}