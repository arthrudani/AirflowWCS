package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception;

public class StationSearchingFailureException extends Exception {

    private static final long serialVersionUID = 7945770031294580778L;

    public StationSearchingFailureException() {
        super();
    }

    public StationSearchingFailureException(String detail) {
        super(detail);
    }

    public StationSearchingFailureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
