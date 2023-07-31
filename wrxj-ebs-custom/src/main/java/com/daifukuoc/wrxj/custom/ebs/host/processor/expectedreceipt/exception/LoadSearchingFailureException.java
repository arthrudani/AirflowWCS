package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception;

public class LoadSearchingFailureException extends Exception {
    
    private static final long serialVersionUID = 5108075719533794925L;

    public LoadSearchingFailureException() {
        super();
    }

    public LoadSearchingFailureException(String detail) {
        super(detail);
    }

    public LoadSearchingFailureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
