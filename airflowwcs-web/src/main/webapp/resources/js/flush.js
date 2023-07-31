

/*****************************************************************
 * 						On page ready setup
 ****************************************************************/
$(document).ready(function(){ 



	/************************************************************
	 * 					Flush Button
	 ************************************************************/
	/**
	 * Flush Loads from selected aisle
	 */
	$("#flushAisleButton").click(function(){
		$.post("/airflowwcs/flush/flushLoads?srcAisle="+$("#srcAisle").val(), $(this).serialize(), function(response){
			alertOnResponse(response); 
			if(response.responseCode==1)
			{

			}
		});

	});

	
	
}); 


function getConnectedToStationData(fromStation)
{
	$.ajax({
        url: '/airflowwcs/scale/findToStationData?station='+fromStation,
        type: 'POST',
        async: false,
        cache: false,
        timeout: 30000,
        error: function(){
        	alertError("Warning", "Unable to retrieve station record for station: <b>" + station + "</b>");
            return true;
        },
        success: function(response){ 
        	if(response)
    		{
    			$('#toStation').val(response.stationId);
    			return response;
    		}
    		else
    		{
    			alertError("Warning", "Unable to retrieve station record for station: <b>" + station + "</b>"); 
    		}
        }
    });

}

function getSrcAisle()
{
	return $('#srcAisle').val(); 
}




