package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception;

public class LocationSearchingFailureException extends Exception
{
    private static final long serialVersionUID = 8671594381541503838L;

    public LocationSearchingFailureException()
    {
        super();
    }
    
    public LocationSearchingFailureException(String detail)
    {
        super(detail);
    }
    
    public LocationSearchingFailureException(String detail, Throwable ex)
    {
        super(detail, ex);
    }
}
