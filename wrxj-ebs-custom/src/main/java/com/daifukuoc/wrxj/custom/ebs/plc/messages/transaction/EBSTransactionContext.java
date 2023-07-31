package com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction;

/**
 * The top level interface for {@link EBSTransactionWrapper} sub classes.
 * 
 * @author ST
 *
 */
public interface EBSTransactionContext {
    void setSuccess(boolean success);

    boolean isSuccess();

}
