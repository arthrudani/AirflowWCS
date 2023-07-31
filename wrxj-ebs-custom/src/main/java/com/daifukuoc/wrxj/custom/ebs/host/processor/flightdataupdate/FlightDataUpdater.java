package com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate;

import com.daifukuoc.wrxj.custom.ebs.host.messages.FlightDataUpdateMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.Processor;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.exception.FlightDataUpdateFailureException;

/**
 * An interface responsible for updating flight data.
 * 
 * @author LK
 *
 */
public interface FlightDataUpdater extends Processor {
    public static final String NAME = "FlightDataUpdater";

    /**
     * Apply the updated flight data to AirflowWCS
     * 
     * @param flightDataUpdateMessageData
     */
    void update(FlightDataUpdateMessageData flightDataUpdateMessageData) throws FlightDataUpdateFailureException;
}
