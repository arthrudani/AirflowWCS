package com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalitemorder;

import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderItemMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.Processor;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;

public interface LoadItemRetriever extends Processor {
    public static final String NAME = "LoadItemRetriever";

    short retrieve(RetrievalOrderItemMessageData retrievalOrderItemMessageData ) throws RetrievalOrderFailureException;
}
