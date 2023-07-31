package com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryrequest.exception;

public class InventoryRequestFailureException extends Exception {
    private static final long serialVersionUID = -3489902819522664679L;

    public InventoryRequestFailureException() {
        super();
    }

    public InventoryRequestFailureException(String detail) {
        super(detail);
    }

    public InventoryRequestFailureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
