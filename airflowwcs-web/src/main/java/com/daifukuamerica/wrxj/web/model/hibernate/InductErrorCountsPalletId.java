package com.daifukuamerica.wrxj.web.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.google.gson.annotations.SerializedName;

/**
 * 
 *
 */

@Entity
@IdClass(InductErrorCountsPalletIdKey.class)
@Table(name="V_INDUCTERRORS_PALLETID")
public class InductErrorCountsPalletId
{

	@Id
	@Column(name="STATION")
	@SerializedName("Station")
	private Integer stationId; 
	
	@SerializedName("Package ID")
	@Column(name="PACKAGEID")
	private String packageId;	
	
	@SerializedName("Error Count")
	@Column(name="ERRORCOUNT")
	private Integer errorCount;

	public Integer getStationId() {
		return stationId;
	}

	public void setStationId(Integer stationId) {
		this.stationId = stationId;
	}

	public Integer getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}

	public String getPackageId() {
		return packageId;
	}

	public void setPackageId(String packageId) {
		this.packageId = packageId;
	} 

}
