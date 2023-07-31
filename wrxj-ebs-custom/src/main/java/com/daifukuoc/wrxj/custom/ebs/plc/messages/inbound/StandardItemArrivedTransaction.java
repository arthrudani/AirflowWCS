package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived.ItemArrivedContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionWrapper;

public class StandardItemArrivedTransaction extends EBSTransactionWrapper<ItemArrivedContext> {

	protected static final Logger logger = Logger.getLogger();
	public StandardItemArrivedTransaction(boolean useTransaction) {
		super(useTransaction);
	}

	@Override
	protected void executeBody(ItemArrivedContext c) throws DBException {
		// TODO Auto-generated method stub
		
	}

}
