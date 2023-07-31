function confirmDeleteTracking()
{
	var sel = tableEquipmentTracking.rows( { selected: true } ).data();
	if (sel.length > 0)
	{
		var deviceId = sel[0]['Device ID']
		var equipment = sel[0]['Graphic ID'];
		var trackingId = sel[0]['Tracking ID'];
		var msg = "Delete tracking for <b>" + trackingId + "</b> from <b>" + deviceId + "</b>?";
		confirmDlg("Confirm Tracking Deletion", msg, "Delete", function() {
			deleteTracking(equipment, trackingId);
			confirmDlgComplete();
		});
	}
}

var cmDelete = {
		title: 'Delete',
		action: function(d, i) {
			confirmDeleteTracking();
		}
	};

var trk_menu = [cmDelete];

function addTrackingContextMenu()
{
	d3.selectAll("#tblWrapEquipmentTracking .dataTable td").on("contextmenu",
		d3.contextMenu(trk_menu, {
			onOpen: function() {
				if (!$(this).parent().hasClass('selected'))
					$(this).click();
			}
		})
	);
}

function deleteTracking(deviceId, idText)
{
	postEqTrk('delete', deviceId + '/' + idText);
}

function postEqTrk(trkoption, deviceId)
{
	$.post('/airflowwcs/equipment/loadtracking/' + trkoption + '/' + deviceId,
			$(this).serialize(), function(response) {
		alertOnResponse(response);
	});
}
