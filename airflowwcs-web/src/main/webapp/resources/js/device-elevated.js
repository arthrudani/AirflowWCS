function modifyData(id) {
	clearModifyForm();

	$.post('/airflowwcs/device/find?deviceId='+id, $(this).serialize(), function(response){
		for(var key in response){
			console.log(response);
			if(response.hasOwnProperty(key)){
				$("#"+key+"Mod").val(response[key]);
			}
		}
	});
	$('#modify-modal').modal('show');
}
function clearModifyForm() {
	$("#deviceMod").val("");
	$("#deviceTypeMod").val("");
	$("#operationalStatusMod").val("");
}
function addContextMenu() {
	$(document).contextmenu({
		delegate: ".dataTable td",
		menu: [
			{title: "Modify", cmd: "modify"}
			],
			select: function(event, ui) {
				var idText = table.row(ui.target).id();
				var celltext = ui.target.text();
				switch(ui.cmd){
				case "modify":
					modifyData(idText);
					break;
				case "noop":
					break;
				default:
					alert("ERROR: option not recognized.");
				}
			},
			beforeOpen: function(event, ui) {
				var $menu = ui.menu, $target = ui.target, extraData = ui.extraData;
				if (!ui.target.parent().hasClass('selected'))
					ui.target.click();
			}
	});
}
function addModifySubmitListener()
{
	$('#modify-button').click(function(){
		$('#modify-form').submit();
	});
	$("#modify-form").submit(function(){
		$("#divExecuting").show();
		$.post($(this).attr('action'), $(this).serialize())
			.done(function(response){
				$("#divExecuting").hide();
				$("#modify-modal .close").click()
				console.log(response);
				alertOnResponse(response);
				table.ajax.reload();
				table.draw();
			})
			.fail(function(xhr, status, error){
				$("#divExecuting").hide();
				$("#modify-modal .close").click()
				alertError("ERROR", "Unable to update location: Error " + xhr.status);
			});
		return false;
	});
}
$(window).resize(function(){
	setTimeout(function() { table.draw(); }, 75);
});
/* Ready */
$(document).ready(function(){
	addContextMenu();
	addModifySubmitListener();
});
