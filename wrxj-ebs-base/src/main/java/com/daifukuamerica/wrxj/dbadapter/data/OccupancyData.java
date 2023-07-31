package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.AVAILABLECOUNT;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.LASTMOVEMENTTIME;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.OCCUPIEDCOUNT;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.OOGBAGONTRAYCOUNT;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.OOGEMPTYTRAYCOUNT;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.OOGTRAYSTACKCOUNT;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.OTHERCONTAINERTYPECOUNT;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.STDBAGONTRAYCOUNT;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.STDEMPTYTRAYCOUNT;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.STDTRAYSTACKCOUNT;
import static com.daifukuamerica.wrxj.dbadapter.data.OccupancyDataEnum.UNAVAILABLECOUNT;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class OccupancyData  extends AbstractSKDCData{

	public static final String LASTMOVEMENT_TIME = LASTMOVEMENTTIME.getName();
	public static final String AVAILABLE_COUNT = AVAILABLECOUNT.getName();
	public static final String OCCUPIED_COUNT = OCCUPIEDCOUNT.getName();
	public static final String UNAVAILABLE_COUNT = UNAVAILABLECOUNT.getName();
	public static final String STDEMPTYTRAY_COUNT = STDEMPTYTRAYCOUNT.getName();
	public static final String OOGEMPTYTRAY_COUNT = OOGEMPTYTRAYCOUNT.getName();
	public static final String STDBAGONTRAY_COUNT = STDBAGONTRAYCOUNT.getName();
	public static final String OOGBAGONTRAY_COUNT = OOGBAGONTRAYCOUNT.getName();
	public static final String STDTRAYSTACK_COUNT = STDTRAYSTACKCOUNT.getName();
	public static final String OOGTRAYSTACK_COUNT = OOGTRAYSTACKCOUNT.getName();
	public static final String OTHERCONTAINERTYPE_COUNT = OTHERCONTAINERTYPECOUNT.getName();

	private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

	/*---------------------------------------------------------------------------
	Database fields for AsrsMetaData table.
	---------------------------------------------------------------------------*/
	private Date dLastMovementTime  = new Date();
	private int iAvailableCount;
	private int iOccupiedCount;
	private int iUnavailableCount;
	private int iStdEmptyTrayCount;
	private int iOOGEmptyTrayCount;
	private int iStdBagOnTrayCount;
	private int iOOGBagOnTrayCount;
	private int iStdTrayStackCount;
	private int iOOGTrayStackCount;
	private int iOtherContainerTypeCount;
	
	public OccupancyData() {
		sdf.applyPattern(SKDCConstants.DateFormatString);
		clear();
		initColumnMap(mpColumnMap, OccupancyDataEnum.class);
	}
	
	@Override
	public void clear() {
		super.clear();
		dLastMovementTime  = new Date();
		iAvailableCount=0;
		iOccupiedCount=0;
		iUnavailableCount=0;
		iStdEmptyTrayCount=0;
		iOOGEmptyTrayCount=0;
		iStdBagOnTrayCount=0;
		iOOGBagOnTrayCount=0;
		iStdTrayStackCount=0;
		iOOGTrayStackCount=0;
		iOtherContainerTypeCount= 0;
	}
	
	@Override
	public OccupancyData clone() {
		OccupancyData occupancyData = (OccupancyData) super.clone();
		return occupancyData;
	}
	
	public Date getLastMovementTime() {
		return dLastMovementTime;
	}

	public void setLastMovementTime(Date dLastMovementTime) {
		this.dLastMovementTime = dLastMovementTime;
	}

	public int getAvailableCount() {
		return iAvailableCount;
	}

	public void setAvailableCount(int iAvailableCount) {
		this.iAvailableCount = iAvailableCount;
	}

	public int getOccupiedCount() {
		return iOccupiedCount;
	}

	public void setOccupiedCount(int iOccupiedCount) {
		this.iOccupiedCount = iOccupiedCount;
	}

	public int getUnavailableCount() {
		return iUnavailableCount;
	}

	public void setUnavailableCount(int iUnavailableCount) {
		this.iUnavailableCount = iUnavailableCount;
	}

	public int getiStdEmptyTrayCount() {
		return iStdEmptyTrayCount;
	}

	public void setStdEmptyTrayCount(int iStdEmptyTrayCount) {
		this.iStdEmptyTrayCount = iStdEmptyTrayCount;
	}

	public int getStdBagOnTrayCount() {
		return iStdBagOnTrayCount;
	}

	public void setStdBagOnTrayCount(int iStdBagOnTrayCount) {
		this.iStdBagOnTrayCount = iStdBagOnTrayCount;
	}

	public int getOOGBagOnTrayCount() {
		return iOOGBagOnTrayCount;
	}

	public void setOOGBagOnTrayCount(int iOOGBagOnTrayCount) {
		this.iOOGBagOnTrayCount = iOOGBagOnTrayCount;
	}

	public int getStdTrayStackCount() {
		return iStdTrayStackCount;
	}

	public void setStdTrayStackCount(int iStdTrayStackCount) {
		this.iStdTrayStackCount = iStdTrayStackCount;
	}

	public int getOOGTrayStackCount() {
		return iOOGTrayStackCount;
	}

	public void setOOGTrayStackCount(int iOOGTrayStackCount) {
		this.iOOGTrayStackCount = iOOGTrayStackCount;
	}

	public int getOtherContainerTypeCount() {
		return iOtherContainerTypeCount;
	}

	public void setOtherContainerTypeCount(int iOtherContainerTypeCount) {
		this.iOtherContainerTypeCount = iOtherContainerTypeCount;
	}
	
	public int getOOGEmptyTrayCount() {
		return iOOGEmptyTrayCount;
	}

	public void setOOGEmptyTrayCount(int iOOGEmptyTrayCount) {
		this.iOOGEmptyTrayCount = iOOGEmptyTrayCount;
	}

	@Override
	public boolean equals(AbstractSKDCData eskdata) {
		if (this == eskdata)
			return (true);
		OccupancyData other = (OccupancyData) eskdata;
		return Objects.equals(dLastMovementTime, other.dLastMovementTime) && iAvailableCount == other.iAvailableCount
				&& iOOGBagOnTrayCount == other.iOOGBagOnTrayCount && iOOGTrayStackCount == other.iOOGTrayStackCount
				&& iOccupiedCount == other.iOccupiedCount && iOtherContainerTypeCount == other.iOtherContainerTypeCount
				&& iStdBagOnTrayCount == other.iStdBagOnTrayCount && iStdEmptyTrayCount == other.iStdEmptyTrayCount
				&& iStdTrayStackCount == other.iStdTrayStackCount && iUnavailableCount == other.iUnavailableCount && iOOGEmptyTrayCount == other.iOOGEmptyTrayCount;
	}

	
	
	@Override
	public String toString() {
		return "OccupancyData [dLastMovementTime=" + dLastMovementTime + ", iAvailableCount=" + iAvailableCount
				+ ", iOccupiedCount=" + iOccupiedCount + ", iUnavailableCount=" + iUnavailableCount
				+ ", iStdEmptyTrayCount=" + iStdEmptyTrayCount + ", iOOGEmptyTrayCount=" + iOOGEmptyTrayCount
				+ ", iStdBagOnTrayCount=" + iStdBagOnTrayCount + ", iOOGBagOnTrayCount=" + iOOGBagOnTrayCount
				+ ", iStdTrayStackCount=" + iStdTrayStackCount + ", iOOGTrayStackCount=" + iOOGTrayStackCount
				+ ", iOtherContainerTypeCount=" + iOtherContainerTypeCount + "]";
	}

	@Override
	public int setField(String isColName, Object ipColValue) {
		TableEnum vpEnum = mpColumnMap.get(isColName);
		if (vpEnum == null) {
			return super.setField(isColName, ipColValue);
		}

		switch ((OccupancyDataEnum) vpEnum) {
		case LASTMOVEMENTTIME:
			setLastMovementTime((Date) ipColValue);
			break;

		case AVAILABLECOUNT:
			setAvailableCount((int)ipColValue);
			break;

		case OCCUPIEDCOUNT:
			setOccupiedCount((int)ipColValue);
			break;

		case UNAVAILABLECOUNT:
			setUnavailableCount((int)ipColValue);
			break;
			
		case STDEMPTYTRAYCOUNT:
			setStdEmptyTrayCount((int)ipColValue);
			break;
			
		case OOGEMPTYTRAYCOUNT:
			setOOGEmptyTrayCount((int)ipColValue);
			break;
			
		case STDBAGONTRAYCOUNT:
			setStdBagOnTrayCount((int)ipColValue);
			break;
			
		case OOGBAGONTRAYCOUNT:
			setOOGBagOnTrayCount((int)ipColValue);
			break;
			
		case STDTRAYSTACKCOUNT:
			setStdTrayStackCount((int)ipColValue);
			break;
			
		case OOGTRAYSTACKCOUNT:
			setOOGTrayStackCount((int)ipColValue);
			break;
			
		case OTHERCONTAINERTYPECOUNT:
			setOtherContainerTypeCount((int)ipColValue);
			break;
		}

		return (0);
	}
	

}
