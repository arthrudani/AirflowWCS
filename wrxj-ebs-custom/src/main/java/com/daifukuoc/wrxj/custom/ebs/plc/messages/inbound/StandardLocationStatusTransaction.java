package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.LocationStatusContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionWrapper;

public class StandardLocationStatusTransaction extends EBSTransactionWrapper<LocationStatusContext> {

	protected static final Logger logger = Logger.getLogger();
	public StandardLocationStatusTransaction(boolean useTransaction) {
		super(useTransaction);
	}

	@Override
	protected void executeBody(LocationStatusContext c) throws DBException {
		// TODO Auto-generated method stub
		
	}

}
