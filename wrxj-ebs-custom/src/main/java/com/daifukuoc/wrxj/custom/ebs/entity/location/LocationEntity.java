package com.daifukuoc.wrxj.custom.ebs.entity.location;

import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants;

/**
 * LocationEntity is place holder to have location data
 * 
 * @author Administrator
 * 
 *         DK:30075 - Find full empty location and link to the time slot
 */
public class LocationEntity {

	String address;
	String warehouse;
	int locationType = 0;

	int vacantQty = 0;
	int occupiedQty = 0;
	int reservedQty = 0;// as of now this is not used because occupied qty includes reserved item as
						// well.
	int vacantLimit = 0;

	boolean isOOGBag = false;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getVacantQty() {
		if (getVacantLimit() > 0) {
			if (getVacantLimit() >= getOccupiedQty()) {
				vacantQty = getVacantLimit() - getOccupiedQty();
			}
		}
		return vacantQty;
	}

	private void setVacantQty(int vacantQty) {
		this.vacantQty = vacantQty;
	}

	public int getOccupiedQty() {
		return occupiedQty;
	}

	public void setOccupiedQty(int occupiedQty) {
		this.occupiedQty = occupiedQty;
	}

	public int getReservedQty() {
		return reservedQty;
	}

	public void setReservedQty(int reservedQty) {
		this.reservedQty = reservedQty;
	}

	public boolean isOOGBag() {
		if (locationType > 0 && locationType == EBSDBConstants.Location.LOCATION_TYPE.OVER_SIZE)
			return true;
		else
			return false;
	}

	public String getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(String warehouse) {
		this.warehouse = warehouse;
	}

	public int getVacantLimit() {
		return vacantLimit;
	}

	public void setVacantLimit(int vacantLimit) {
		this.vacantLimit = vacantLimit;
	}

	public int getLocationType() {
		return locationType;
	}

	public void setLocationType(int locationType) {
		this.locationType = locationType;
	}

	public boolean isLocationFullEmpty() {
		boolean isLocationFullEmpty = false;
		if (getVacantLimit() > 0 && getVacantQty() == getVacantLimit()) {
			isLocationFullEmpty = true;
		}
		return isLocationFullEmpty;
	}

	public boolean isLocationOneSpaceEmpty() {
		boolean isLocationOneSpaceEmpty = false;
		if (vacantQty > 0) {
			isLocationOneSpaceEmpty = true;
		}
		return isLocationOneSpaceEmpty;
	}

//	public static Comparator<? super LocationEntity> sortByWarehouse(String warehouse) {
//		return (locationEntity1, locationEntity2) -> warehouse.equals(locationEntity1.getWarehouse()) ? -1
//				: warehouse.equals(locationEntity2.getWarehouse()) ? 1 : 0;
//	}

}
