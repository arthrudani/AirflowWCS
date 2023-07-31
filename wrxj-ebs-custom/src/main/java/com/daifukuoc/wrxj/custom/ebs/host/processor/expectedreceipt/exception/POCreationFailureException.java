package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception;

public class POCreationFailureException extends Exception {

    private static final long serialVersionUID = 3689053391178090337L;

    public POCreationFailureException() {
        super();
    }

    public POCreationFailureException(String detail) {
        super(detail);
    }

    public POCreationFailureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
