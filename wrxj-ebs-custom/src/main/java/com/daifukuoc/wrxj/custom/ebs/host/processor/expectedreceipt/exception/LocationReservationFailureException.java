package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception;

public class LocationReservationFailureException extends Exception {

    private static final long serialVersionUID = -1868824942934294112L;

    public LocationReservationFailureException() {
        super();
    }

    public LocationReservationFailureException(String detail) {
        super(detail);
    }

    public LocationReservationFailureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
