package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception;

public class NoRemainingEmptyLocationException extends Exception 
{
    private static final long serialVersionUID = -3489902819522664679L;

    public NoRemainingEmptyLocationException()
    {
        super();
    }
    
    public NoRemainingEmptyLocationException(String detail)
    {
        super(detail);
    }
    
    public NoRemainingEmptyLocationException(String detail, Throwable ex)
    {
        super(detail, ex);
    }
}
