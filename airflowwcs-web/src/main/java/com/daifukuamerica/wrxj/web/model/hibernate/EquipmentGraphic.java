package com.daifukuamerica.wrxj.web.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="EquipmentMonitorView")
public class EquipmentGraphic {

	public EquipmentGraphic()
	{
		//for hibernate
	}

	@Id
	@Column(name="sEMGraphicID")
	private String id;

	@Column(name="sEMBehavior")
	private String behavior;

	@Column(name="sEMDescription")
	private String description;

	@Column(name="sEMStatusID")
	private String statusId;

	@Column(name="sEMStatusText1")
	private String statusText;

	@Column(name="sEMStatusText2")
	private String statusText2;

	@Column(name="sEMErrorCode")
	private String errorCode;

	@Column(name="sEMErrorText")
	private String errorText;

	@Column(name="sEMBackground")
	private String backgroundColor;

	@Column(name="sEMForeground")
	private String foregroundColor;

	@Column(name="sEMMOSID")
	private String mosId;

	@Column(name="iEMCanTrack")
	private Integer canTrack;

	@Column(name="iTrackingCount")
	private Integer trackCount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBehavior() {
		return behavior;
	}

	public void setBehavior(String behavior) {
		this.behavior = behavior;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public String getStatusText2() {
		return statusText2;
	}

	public void setStatusText2(String statusText2) {
		this.statusText2 = statusText2;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
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

	public String getMosId()
	{
		return mosId;
	}

	public void setMosId(String mosId)
	{
		this.mosId = mosId;
	}

	public Integer getCanTrack() {
		return canTrack;
	}

	public void setCanTrack(Integer canTrack) {
		this.canTrack = canTrack;
	}

	public Integer getTrackCount() {
		return trackCount;
	}

	public void setTrackCount(Integer trackCount) {
		this.trackCount = trackCount;
	}
}
