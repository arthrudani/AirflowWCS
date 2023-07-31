package com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.exception;

public class FlightDataUpdateFailureException extends Exception {
    private static final long serialVersionUID = -3489902819522664679L;

    public FlightDataUpdateFailureException() {
        super();
    }

    public FlightDataUpdateFailureException(String detail) {
        super(detail);
    }

    public FlightDataUpdateFailureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
