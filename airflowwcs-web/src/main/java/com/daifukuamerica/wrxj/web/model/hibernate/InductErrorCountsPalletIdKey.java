package com.daifukuamerica.wrxj.web.model.hibernate;

import java.io.Serializable;


/**
 *
 */

public class InductErrorCountsPalletIdKey implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 13L;

	public InductErrorCountsPalletIdKey(){
		//noarg
	}
	
	public InductErrorCountsPalletIdKey(Integer stationId){
		this.stationId=stationId; 
	}

	private Integer stationId; 

	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
        if (!(obj instanceof InductErrorCountsPalletIdKey)) return false;
		if(obj instanceof InductErrorCountsPalletIdKey){
			InductErrorCountsPalletIdKey sessionKey =(InductErrorCountsPalletIdKey)obj;
			return stationId == sessionKey.stationId;
		}else{
			return false; 
		}
	}
	
	@Override
	public int hashCode(){
		return stationId.hashCode(); 
	}

	public Integer getStationId() {
		return stationId;
	}

	public void setStationId(Integer stationId) {
		this.stationId = stationId;
	}
	
}
