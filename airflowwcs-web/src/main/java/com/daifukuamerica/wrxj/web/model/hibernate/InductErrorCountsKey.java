package com.daifukuamerica.wrxj.web.model.hibernate;

import java.io.Serializable;


/**
 *
 */

public class InductErrorCountsKey implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 13L;

	public InductErrorCountsKey(){
		//noarg
	}
	
	public InductErrorCountsKey(Integer stationId){
		this.stationId=stationId; 
	}

	private Integer stationId; 

	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
        if (!(obj instanceof InductErrorCountsKey)) return false;
		if(obj instanceof InductErrorCountsKey){
			InductErrorCountsKey sessionKey =(InductErrorCountsKey)obj;
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
