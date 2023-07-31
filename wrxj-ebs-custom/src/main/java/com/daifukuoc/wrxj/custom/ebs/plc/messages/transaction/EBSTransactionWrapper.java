package com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction;

import com.daifukuamerica.wrxj.dataserver.standard.StandardServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Database transaction wrapper for message processing.
 * 
 * @author ST
 *
 * @param <C> The context class to ue used by sub class.
 */
public abstract class EBSTransactionWrapper<C extends EBSTransactionContext> extends StandardServer {
    private TransactionToken transactionToken;
    private boolean useTransaction;

    /**
     * Constructor.
     * 
     * @param useTransaction true : yes, false : no.
     */
    public EBSTransactionWrapper(boolean useTransaction) {
        super();
        this.useTransaction = useTransaction;
    }

    /**
     * Start transaction. This method is introduced for testing purpose.
     * 
     * @throws DBException
     */
    final public void startLocalTransaction() throws DBException {
        transactionToken = super.startTransaction();
    }

    /**
     * Commit transaction. This method is introduced for testing purpose.
     * 
     * @throws DBException
     */
    final public void commitLocalTransaction() throws DBException {
        super.commitTransaction(transactionToken);
    }

    /**
     * End transaction. This method is introduced for testing purpose.
     */
    final public void endLocalTransaction() {
        super.endTransaction(transactionToken);
    }

    /**
     * Transaction body. When the property useTransaction is set to true, 
     * executeBody is called in a transaction scope, otherwise, not 
     * in a transaction.
     * 
     * @param c Context instance
     * @throws DBException
     */
    public final void execute(C c) throws DBException {
        if (useTransaction) {
            try {
                startLocalTransaction();
                executeBody(c);
                commitLocalTransaction();
            } finally {
                endLocalTransaction();
            }
        } else {
            executeBody(c);
        }
    }

    protected abstract void executeBody(C c) throws DBException;
}
