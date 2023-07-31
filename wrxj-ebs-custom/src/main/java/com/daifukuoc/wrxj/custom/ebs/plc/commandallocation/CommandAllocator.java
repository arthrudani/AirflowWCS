package com.daifukuoc.wrxj.custom.ebs.plc.commandallocation;

public interface CommandAllocator {

	void allocate(String stationID);
	void allocateAll();
}
