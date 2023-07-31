package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception;

public class InvalidExpectedReceiptException extends Exception
{
    private static final long serialVersionUID = -3863370605699259335L;
    
    public InvalidExpectedReceiptException()
    {
        super();
    }
    
    public InvalidExpectedReceiptException(String detail)
    {
        super(detail);
    }
    
    public InvalidExpectedReceiptException(String detail, Throwable ex)
    {
        super(detail, ex);
    }
}
