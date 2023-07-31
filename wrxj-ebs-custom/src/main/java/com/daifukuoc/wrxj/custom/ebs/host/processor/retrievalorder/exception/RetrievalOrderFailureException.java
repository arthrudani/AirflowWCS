package com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception;

public class RetrievalOrderFailureException extends Exception {
    private static final long serialVersionUID = -3489902819522664679L;

    public RetrievalOrderFailureException() {
        super();
    }

    public RetrievalOrderFailureException(String detail) {
        super(detail);
    }

    public RetrievalOrderFailureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
