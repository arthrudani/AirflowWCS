var mapUrl = ctx + "/resources/layout/map.svg"; 
var width = 1900; 
var height = 1100; 
var main_map_svg = d3.select("#mapContainer"); 

var contextMenuSelectionId; 

// build a context menu from the selected ID field??? not working because i believe this to be built on the page load for all
// contextMenuSelectionId is undefined when the menu appears on click event. 
var menu = [
	{
		title: 'Menu item' + contextMenuSelectionId
		,
		action: function(d, i) {
			console.log('First item for '+ contextMenuSelectionId + ' clicked!');
			console.log('The data for this item is: ' + contextMenuSelectionId);
		},
		disabled: false // optional, defaults to false
	},
	{
		title: 'Item #2',
		action: function(d, i) {
			console.log('Second item for '+ contextMenuSelectionId + ' clicked!');
			console.log('The data for this item is: ' + contextMenuSelectionId);
		}
	}
]



main_map_svg 
.append("svg")
.attr("width", width)
.attr("height", height)
.attr("id", "inner_map");

var inner_map_svg = d3.select("#inner_map"); 
//create a rectangle that will catch pointer and click events in background
//of overlayed map svg which will allow for zooming and panning
inner_map_svg.append("rect")
.attr("width", width)
.attr("height", height)
.attr("id","zoom-rect")
.style("fill", "none")
.style("pointer-events", "all")
.call(d3.zoom() 
.scaleExtent([1 / 2, 4])
.on("zoom", zoomed));
//callback function to retrieving the map svg and append to the zoom rect
function loadMapXmlResponse(loaded_svg){
	console.log(loaded_svg); 
	d3.select("#inner_map").node().appendChild(loaded_svg.firstChild);
	selectable = main_map_svg.selectAll(".selectableMapComponent"); 
	//add context menu to every svg element that has the class "selectableMapComponent"
	selectable.on("contextmenu", 
		
		d3.contextMenu(menu, {
			onOpen: function() {
				contextMenuSelectionId = this.id; 
			},
			onClose: function() {
				console.log('Menu has been closed.');
			}
		})
		 ); 	
	
	selectable.on("click", function(){
		$('#equipment-layout').layout('collapse','east');
		var equipmentId = this.id; 
		var i = 0; 
		equipmentId = equipmentId.replace(/-/g, function(match){ // we have to translate back to colon seperated graphic id
			i++; 
			if(i==2) 
				return ":"; 
			else if(i==3)
				return ","; 
			else 
				return match; 
		}); 
		//get the single equipment status details
		$.get('/airflowwcs/equipment/status/device/'+equipmentId, $(this).serialize(), function(response){
			console.log(response);
			$("#equipmentIdDetail").val(response.id);
			$("#equipmentDescriptionDetail").val(response.description);
			$("#equipmentStatusDetail").val(response.statusId);
			if(response.statusText || response.statusText2){
				var s = response.statusText;
				var s2 = response.statusText2;
				s = s + " | " + s2;
				$("#equipmentStatusTextDetail").val(s);
			}
				
			$("#equipmentErrorCodeDetail").val(response.errorCode);
			$("#equipmentErrorTextDetail").val(response.errorText);
		}); 
		console.log("CLICKED " + equipmentId); 
		$('#equipment-layout').layout('expand','east');
	}); 
	
	startEquipmentMonitorLoop(); 
}
/// load the url and use callback function to append
d3.svg(mapUrl).then(loadMapXmlResponse);

var g; 
var selectable; 



function zoomed() {
	g = main_map_svg.selectAll("g"); 
	g.attr("transform", d3.event.transform);
}

$(document).ready(function () {
  
    
    g = main_map_svg.selectAll("g"); 
   

	$('#equipment-layout').layout('collapse','west'); 
	$('#equipment-layout').layout('collapse','east'); 
	

});


function pollEquipmentStatus()
{
	$.get('/airflowwcs/equipment/status/all', $(this).serialize(), function(response){
		
		if(response.equipmentGraphics)
		{
			var equipmentGraphicArr = response.equipmentGraphics; 
			var i; 
			for(i=0; i<equipmentGraphicArr.length; i++)
			{
				if(equipmentGraphicArr[i].id)
				{
					var graphicId = equipmentGraphicArr[i].id.replace(":", "-");  // since css identifiers can't be colons, lets replace them with hyphens
					//console.log(graphicId); 
					if(graphicId.includes("Fullness")) // FULLNESS INDICATOR ENTRY
					{
						d3.select("svg text#" + graphicId + "-text")
							.html(equipmentGraphicArr[i].statusText2); // set text of fullness indicators
						d3.select("svg rect#" + graphicId)
							.style("fill", equipmentGraphicArr[i].backgroundColor); // set background color of fullness indicators
					}
					
					if(graphicId.includes("Swap")) // SWAP ZONE GRAPHIC ENTRY
					{
						d3.select("svg text#" + graphicId + "-text")
							.html(equipmentGraphicArr[i].statusText2); // set text of fullness indicators
						d3.select("svg rect#" + graphicId)
							.style("fill", equipmentGraphicArr[i].backgroundColor); // set background color of fullness indicators
				
					}
					
					if(graphicId.includes("LayerInput") || graphicId.includes("LayerOutput") || graphicId.includes("Shuttle") ) // SWAP ZONE GRAPHIC ENTRY
					{
						d3.select("svg #" + graphicId)
							.style("fill", equipmentGraphicArr[i].backgroundColor); // set background color of fullness indicators
					}
					
					
					
					
				}	
					
			}
		}
		if(response.equipmentTabs)
		{
			var equipmentTabArr = response.equipmentTabs; 
			var i; 
			for(i=0; i<equipmentTabArr.length; i++)
			{
				if(equipmentTabArr[i].id)
				{
					var tabId = equipmentTabArr[i].id.replace(":", "-"); 
					//console.log(tabId); 
				}	
					
			}
		}
	}); 
}
var timedFunction; 
function startEquipmentMonitorLoop()
{
	timedFunction = setInterval(function(){pollEquipmentStatus()}, 4000); 
}



