$(document).ready(function() {
	
	updateWarehouseCounts(); 
	updateAisleCounts();
	updateTotalTodayCount();
	updateMovingLoadCount();
	updateGoodInductionCount();
	updateErrorInductionCount();
	
	$("#counts-refresh-button").on('click', function(){
		$("#divExecuting").show();
		updateWarehouseCounts(); 
		updateAisleCounts();
		updateTotalTodayCount();
		updateMovingLoadCount();
		updateGoodInductionCount();
		updateErrorInductionCount();
		$("#divExecuting").hide();
	});

}); 

function updateTotalTodayCount()
{
	$.get( "/airflowwcs/loadcounts/loadCountToday", function( data ) {

	    $("#total_today_count").html("<strong>"+data.total_today_count+"</strong>"); 
	});
}

function updateMovingLoadCount()
{
	$.get( "/airflowwcs/loadcounts/movingLoadCountToday", function( data ) {

	    $("#total_enoute").html("<strong>"+data.total_movingloads_count+"</strong>"); 
	});
}

function updateGoodInductionCount()
{
	$.get( "/airflowwcs/loadcounts/goodInductionCountToday", function( data ) {

	    $("#total_good_inducts").html("<strong>"+data.total_goodinduction_count+"</strong>"); 
	});
}

function updateErrorInductionCount()
{
	$.get( "/airflowwcs/loadcounts/errorInductionCountToday", function( data ) {

	    $("#total_error_inducts").html("<strong>"+data.total_errorinduction_count+"</strong>"); 
	});
}

function updateWarehouseCounts()
{
	$.get( "/airflowwcs/loadcounts/loadCountWarhse", function( data ) {
	    $("#s1_count").html("<strong>"+data.s1_count+"</strong>"); 
	    $("#s2_count").html("<strong>"+data.s2_count+"</strong>"); 
	    $("#ltw_count").html("<strong>"+data.ltw_count+"</strong>"); 
	    $("#knp_count").html("<strong>"+data.knp_count+"</strong>"); 
	    $("#total_count").html("<strong>"+data.total_count+"</strong>"); 
	});
}

function updateAisleCounts()
{
	$.get( "/airflowwcs/loadcounts/loadCountAisle", function( data ) {
	    $("#aisle1_count").html("<strong>"+data.aisle1_count+"</strong>"); 
	    $("#aisle2_count").html("<strong>"+data.aisle2_count+"</strong>"); 
	    $("#aisle3_count").html("<strong>"+data.aisle3_count+"</strong>"); 
	    $("#aisle4_count").html("<strong>"+data.aisle4_count+"</strong>"); 
	    $("#aisle5_count").html("<strong>"+data.aisle5_count+"</strong>"); 
	    $("#aisle6_count").html("<strong>"+data.aisle6_count+"</strong>"); 
	    $("#aisle7_count").html("<strong>"+data.aisle7_count+"</strong>"); 
	    $("#aisle8_count").html("<strong>"+data.aisle8_count+"</strong>"); 
	    $("#aisle9_count").html("<strong>"+data.aisle9_count+"</strong>"); 
	    $("#aisle10_count").html("<strong>"+data.aisle10_count+"</strong>"); 
	    $("#aisle11_count").html("<strong>"+data.aisle11_count+"</strong>"); 
	    $("#aisle12_count").html("<strong>"+data.aisle12_count+"</strong>"); 
	    $("#aisle13_count").html("<strong>"+data.aisle13_count+"</strong>"); 
	    $("#aisle14_count").html("<strong>"+data.aisle14_count+"</strong>"); 
	    $("#aisle15_count").html("<strong>"+data.aisle15_count+"</strong>"); 
	    $("#aisle16_count").html("<strong>"+data.aisle16_count+"</strong>"); 
	    $("#aisle17_count").html("<strong>"+data.aisle17_count+"</strong>"); 
	    $("#aisle18_count").html("<strong>"+data.aisle18_count+"</strong>"); 
	    $("#aisle19_count").html("<strong>"+data.aisle19_count+"</strong>"); 
	    $("#aisle20_count").html("<strong>"+data.aisle20_count+"</strong>"); 
	    $("#aisle21_count").html("<strong>"+data.aisle21_count+"</strong>"); 
	    $("#aisle22_count").html("<strong>"+data.aisle22_count+"</strong>"); 
	    $("#aisle23_count").html("<strong>"+data.aisle23_count+"</strong>"); 
	    $("#aisle24_count").html("<strong>"+data.aisle24_count+"</strong>"); 
	    $("#ltw1_count").html("<strong>"+data.ltw1_count+"</strong>"); 
	    $("#ltw2_count").html("<strong>"+data.ltw2_count+"</strong>"); 
	    $("#ltw3_count").html("<strong>"+data.ltw3_count+"</strong>"); 
	    $("#ltw4_count").html("<strong>"+data.ltw4_count+"</strong>"); 
	    $("#ltw5_count").html("<strong>"+data.ltw5_count+"</strong>"); 
	});
}