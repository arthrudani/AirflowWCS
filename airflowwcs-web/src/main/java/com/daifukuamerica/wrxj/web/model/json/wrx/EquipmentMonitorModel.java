package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.ArrayList;
import java.util.List;

import com.daifukuamerica.wrxj.web.model.hibernate.EquipmentGraphic;
import com.daifukuamerica.wrxj.web.model.hibernate.EquipmentTab;

public class EquipmentMonitorModel {

	public List<EquipmentGraphic> equipmentGraphics;
	public List<EquipmentTab> equipmentTabs;

	public EquipmentMonitorModel()
	{
		this.equipmentGraphics = new ArrayList<EquipmentGraphic>();
		this.equipmentTabs = new ArrayList<EquipmentTab>();
	}

	public List<EquipmentGraphic> getEquipmentGraphics() {
		return equipmentGraphics;
	}
	public void setEquipmentGraphics(List<EquipmentGraphic> equipmentGraphics) {
		this.equipmentGraphics = equipmentGraphics;
	}
	public List<EquipmentTab> getEquipmentTabs() {
		return equipmentTabs;
	}
	public void setEquipmentTabs(List<EquipmentTab> equipmentTabs) {
		this.equipmentTabs = equipmentTabs;
	}
}
