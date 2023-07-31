package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception;

public class LoadCreationOrUpdateFailureException extends Exception {
    private static final long serialVersionUID = -3489902819522664679L;

    public LoadCreationOrUpdateFailureException() {
        super();
    }

    public LoadCreationOrUpdateFailureException(String detail) {
        super(detail);
    }

    public LoadCreationOrUpdateFailureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
