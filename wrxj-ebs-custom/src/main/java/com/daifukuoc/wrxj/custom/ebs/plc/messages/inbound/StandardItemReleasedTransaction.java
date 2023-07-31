package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemreleased.ItemReleasedContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionWrapper;

public class StandardItemReleasedTransaction extends EBSTransactionWrapper<ItemReleasedContext> {

	protected static final Logger logger = Logger.getLogger();
	public StandardItemReleasedTransaction(boolean useTransaction) {
		super(useTransaction);
	}

	@Override
	protected void executeBody(ItemReleasedContext c) throws DBException {
		// TODO: Just update the Load data here
		
	}

}
