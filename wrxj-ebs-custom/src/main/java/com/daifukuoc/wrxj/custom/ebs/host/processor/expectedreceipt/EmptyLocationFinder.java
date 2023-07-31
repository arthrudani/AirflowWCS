package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.Processor;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.AlreadyStoredLoadException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.InvalidExpectedReceiptException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadCreationOrUpdateFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationReservationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.NoRemainingEmptyLocationException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.POCreationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.StationSearchingFailureException;

/**
 * An interface responsible for finding an empty location when a load is expected to arrive soon. It returns an entrance
 * station id where the bag should be delivered.
 * 
 * @author LK
 *
 */
public interface EmptyLocationFinder extends Processor {
    public static final String NAME = "EmptyLocationFinder";

    /**
     * Find an empty location for expected receipt message
     * 
     * @param expectedReceiptMessageData the received expected receipt message
     * @return The entrance station of the empty location
     * @throws NoRemainingEmptyLocationException
     * @throws LocationSearchingFailureException
     * @throws InvalidExpectedReceiptException
     * @throws LoadCreationOrUpdateFailureException
     * @throws AlreadyStoredLoadException
     * @throws LoadSearchingFailureException
     * @throws StationSearchingFailureException
     * @throws LocationReservationFailureException
     * @throws POCreationFailureException
     */
    String find(ExpectedReceiptMessageData expectedReceiptMessageData)
            throws NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, LoadSearchingFailureException,
            AlreadyStoredLoadException, StationSearchingFailureException, LocationReservationFailureException,
            POCreationFailureException;
    
    /**
     * Method to handle ER message with requestType update. 
     * 
     * @param expectedReceiptMessageData the received expected receipt message
     * @throws InvalidExpectedReceiptException
     * @throws LoadSearchingFailureException
     * @throws DBException
     */
    void update(ExpectedReceiptMessageData expectedReceiptMessageData) throws InvalidExpectedReceiptException, LoadSearchingFailureException, DBException;
    
    /**
     * Method to handle ER message with requestType cancel
     * 
     * @param expectedReceiptMessageData
     * @throws InvalidExpectedReceiptException
     * @throws LoadSearchingFailureException
     * @throws DBException
     */
    void cancel(ExpectedReceiptMessageData expectedReceiptMessageData) throws InvalidExpectedReceiptException, LoadSearchingFailureException, DBException;
}
