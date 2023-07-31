package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception;

public class AlreadyStoredLoadException extends Exception {

    private static final long serialVersionUID = 2476070180137308623L;

    public AlreadyStoredLoadException() {
        super();
    }

    public AlreadyStoredLoadException(String detail) {
        super(detail);
    }

    public AlreadyStoredLoadException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
