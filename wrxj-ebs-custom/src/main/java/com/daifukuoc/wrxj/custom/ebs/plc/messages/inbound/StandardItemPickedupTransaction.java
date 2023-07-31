package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup.ItemPickedUpContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionWrapper;

public class StandardItemPickedupTransaction extends EBSTransactionWrapper<ItemPickedUpContext> {

	protected static final Logger logger = Logger.getLogger();
	public StandardItemPickedupTransaction(boolean useTransaction) {
		super(useTransaction);
	}

	@Override
	protected void executeBody(ItemPickedUpContext itemPickedupContext) throws DBException {
		
		
	}

}
