package com.daifukuamerica.wrxj.web.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="EquipmentMonitorTabView")
public class EquipmentTab {
	
	public EquipmentTab()
	{
		
	}
	
	@Id
	@Column(name="sEMGraphicTab")
	private String id; 
	
	@Column(name="sEMStatusID")
	private String status; 
	
	@Column(name="sEMBackground")
	private String backgroundColor; 
	
	@Column(name="sEMForeground")
	private String foregroundColor;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(String foregroundColor) {
		this.foregroundColor = foregroundColor;
	} 
	
	
	

}
