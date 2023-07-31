package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored.ItemStoredContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionWrapper;

public class StandardItemStoredTransaction extends EBSTransactionWrapper<ItemStoredContext> {

	protected static final Logger logger = Logger.getLogger();
	public StandardItemStoredTransaction(boolean useTransaction) {
		super(useTransaction);
	}

	@Override
	protected void executeBody(ItemStoredContext plcItemStoredContext) throws DBException {
		
		EBSInventoryServer mpEBSInventoryServer = Factory.create(EBSInventoryServer.class);
        try {
        	mpEBSInventoryServer.processStoreComplete(plcItemStoredContext);
	        	
        } catch (DBException e) {
            logger.logError("Error while Processing PLC Store completion message, Error:" + e.getMessage());
            logger.logException(e);
        }
	}

}
