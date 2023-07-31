<!DOCTYPE html>
<html>
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.4/Chart.js"></script>
<body>
<div class="card shadow rounded-0">
        <div class="card-header rounded-0">
            <div class="d-flex justify-content-between">
                <div class="col-auto">
                    <button class="btn btn-primary rounded-0" style ="margin-left:70%" id="generate_pie">Refresh</button>
                </div>
            </div>
        </div>
        <div class="card-body">
            <div class="container-fluid">
                <canvas id="pie_chart" style="width:100%;max-width:700px;margin-left:30%";></canvas>
            </div>
        </div>
    </div>
<script>
var counts = [];
$( document ).ready(function() {
	getCount();
	
	$('#generate_pie').click(function(){
		executeSP();
		setTimeout(getCount, 500);
	});
});
var xValues = ["Occupied Count", "Unavailable Count", "Available Count"];
var yValues = counts;
var barColors = [
  "#b91d47",
  "#00aba9",
  "#2b5797"
];
let pieChart  = "";
function getCount(){
	$.ajax({
	      url:'/airflowwcs/occupancy/list',
	      dataType:'JSON',
	      success: function(response){
	    	  $(".card-body").css("display","block");
	    	  console.log(response.data.length);
	    	  if(response.data.length>0){
	    	  var Occupied = response.data[0]["Occupied Count"];
	    	  var Unavailable = response.data[0]["Unavailable Count"];
	    	  var Available = response.data[0]["Available Count"];
	    	  counts = [Occupied,Unavailable,Available];
	    	  
	    	  pieChart  = new Chart(document.getElementById('pie_chart').getContext('2d'), {
	    		    type: 'pie',
	    		    data: {
	    		        labels: xValues,
	    		        datasets: [{
	    		          backgroundColor: barColors,
	    		          data: counts
	    		        }]
	    		      },
	    		      options: {
	    		        title: {
	    		          display: true,
	    		          text: "Live Warehouse Occupancy"
	    		        }
	    		      }
	    		});
	      }else{
	    	  $(".card-body").css("display","none");
	      }
	    }
	  });
};
function executeSP() {
	$.ajax({
	      url:'/airflowwcs/occupancy/executeSp',
	      success: function(response){
	    	  console.log("success");
	      }
	});
}
</script>

</body>
</html>
