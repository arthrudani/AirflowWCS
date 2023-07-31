var isAdmin = false;

$('#scRange').daterangepicker({
	"timePicker": true,
	"timePicker24Hour": true,
	"timePickerSeconds": true,
	ranges: {
		'Today': [moment().startOf('days'), moment().endOf('days')],
		'Yesterday': [moment().startOf('days').subtract(1, 'days'), moment().subtract(1, 'days').endOf('days')],
		'Last 24 Hours': [moment().subtract(1, 'days'), moment()],
		'Last 7 Days': [moment().subtract(6, 'days'), moment()],
		'Last 30 Days': [moment().subtract(29, 'days'), moment()],
		'This Month': [moment().startOf('month'), moment().endOf('month')],
		'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
	},
	"startDate": moment().add(-24, 'hour'),
	"endDate": moment(),
	locale: {
		format: 'M/DD HH:mm:ss'
	}
});
$('#scRange').on('apply.daterangepicker', function(ev, picker) {
	$('#scStarting').val(picker.startDate.format('YYYYMMDDHHmmss'));
	$('#scEnding').val(picker.endDate.format('YYYYMMDDHHmmss'));
});
$('#scStarting').val($('#scRange').data('daterangepicker').startDate.format('YYYYMMDDHHmmss'));
$('#scEnding').val($('#scRange').data('daterangepicker').endDate.format('YYYYMMDDHHmmss'));

$(document).ready(function(){
	var table = $('#ajaxTable').DataTable();

	$("#history-filtering-form").submit(function(e){
        e.preventDefault();
    });

	$("#scStarting").on('keyup', function (e) {
	    if (e.keyCode == 13)
	    {
	    	$("#divExecuting").show();
	    	searchHistory();
	    }
	});

	$("#scEnding").on('keyup', function (e) {
	    if (e.keyCode == 13)
	    {
	    	$("#divExecuting").show();
	    	searchHistory();
	    }
	});

	$("#scData").on('keyup', function (e) {
	    if (e.keyCode == 13)
	    {
	    	$("#divExecuting").show();
	    	searchHistory();
	    }
	});

	$("#searchHistoryButton").click(function() {
		$("#divExecuting").show();
		searchHistory();
    });
});

function searchHistory()
{
	table.ajax.url("/airflowwcs/equiplogview/listSearch?startingDate="+$("#scStarting").val()+"&endingDate="+$("#scEnding").val()+"&deviceId="+$("#scDeviceId").val()+"&data="+$("#scData").val()).load(function()
	{
		$("#divExecuting").hide();
		table.columns.adjust().draw();
	});
}
