package com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder;

import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.Processor;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;

public interface LoadRetriever extends Processor {
    public static final String NAME = "LoadRetriever";

    /**
     * Retrieve the N or all bags for flight on the specified flight scheduled date times
     * 
     * @param retrievalOrderMessageData The received retrieval order message
     * @return The number of retrieved bag, should be >= 0
     * @throws RetrievalOrderFailureException If anything goes wrong
     */
    short retrieve(RetrievalOrderMessageData retrievalOrderMessageData) throws RetrievalOrderFailureException;
}
