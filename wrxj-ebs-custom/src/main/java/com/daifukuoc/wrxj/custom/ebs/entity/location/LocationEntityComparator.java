package com.daifukuoc.wrxj.custom.ebs.entity.location;

import java.util.Comparator;

/**
 * Location Entity comparator class to sort the location entity
 * 
 * @author Administrator
 *
 */
public class LocationEntityComparator {

	/**
	 * This comparator sorts the locationEntity by the given warehouse. Making the
	 * given warehouse to be on the top and remaining in the list below
	 * 
	 * @param warehouse
	 * @return
	 */
	public Comparator<? super LocationEntity> priortiseByWarehouse(String warehouse) {
		if (warehouse != null && warehouse.trim().length() > 0) {
			return (locationEntity1, locationEntity2) -> warehouse.equals(locationEntity1.getWarehouse()) ? -1
					: warehouse.equals(locationEntity2.getWarehouse()) ? 1 : 0;
		} else {
			return (locationEntity1, locationEntity2) -> locationEntity1.getWarehouse()
					.compareTo(locationEntity2.getWarehouse());
		}
	}
}
