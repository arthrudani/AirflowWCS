package com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate.exception;

public class InventoryUpdateFailureException extends Exception {
    private static final long serialVersionUID = -3489902819522664679L;

    public InventoryUpdateFailureException() {
        super();
    }

    public InventoryUpdateFailureException(String detail) {
        super(detail);
    }

    public InventoryUpdateFailureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
