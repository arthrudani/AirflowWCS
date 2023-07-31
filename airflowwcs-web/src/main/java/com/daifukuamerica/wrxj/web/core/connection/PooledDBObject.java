package com.daifukuamerica.wrxj.web.core.connection;

import java.io.PrintWriter;
import java.util.Deque;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectState;
import org.apache.commons.pool2.TrackedUse;

import com.daifukuamerica.wrxj.jdbc.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PooledDBObject  implements PooledObject<DBObject>
{

	private  DBObject object;
    private PooledObjectState state = PooledObjectState.IDLE; // @GuardedBy("this") to ensure transitions are valid
    private final long createTime = System.currentTimeMillis();
    private volatile long lastBorrowTime = createTime;
    private volatile long lastUseTime = createTime;
    private volatile long lastReturnTime = createTime;
    private volatile boolean logAbandoned = false;

    private volatile long borrowedCount = 0;

	/**
	* Log4j logger: PooledDBObject
	*/
	private static final Logger logger = LoggerFactory.getLogger(PooledDBObject.class);

	private static DBObject instance;


	/**
     * Create a new instance that wraps the provided object so that the pool can
     * track the state of the pooled object.
     *
     * @param object The object to wrap
     */
    public PooledDBObject(final DBObject object) {
        this.object = object;
    }

    @Override
    public DBObject getObject() {
        return object;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public long getActiveTimeMillis() {
        // Take copies to avoid threading issues
        final long rTime = lastReturnTime;
        final long bTime = lastBorrowTime;

        if (rTime > bTime) {
            return rTime - bTime;
        }
        return System.currentTimeMillis() - bTime;
    }

    @Override
    public long getIdleTimeMillis() {
        final long elapsed = System.currentTimeMillis() - lastReturnTime;
     // elapsed may be negative if:
     // - another thread updates lastReturnTime during the calculation window
     // - System.currentTimeMillis() is not monotonic (e.g. system time is set back)
     return elapsed >= 0 ? elapsed : 0;
    }

    @Override
    public long getLastBorrowTime() {
        return lastBorrowTime;
    }



    /**
     * Get the number of times this object has been borrowed.
     * @return The number of times this object has been borrowed.
     * @since 2.1
     */
    public long getBorrowedCount() {
        return borrowedCount;
    }

    /**
     * Return an estimate of the last time this object was used.  If the class
     * of the pooled object implements {@link TrackedUse}, what is returned is
     * the maximum of {@link TrackedUse#getLastUsed()} and
     * {@link #getLastBorrowTime()}; otherwise this method gives the same
     * value as {@link #getLastBorrowTime()}.
     *
     * @return the last time this object was used
     */
    @Override
    public long getLastUsedTime() {

        return lastUseTime;
    }



    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("Object: ");
        result.append(object.toString());
        result.append(", State: ");
        synchronized (this) {
            result.append(state.toString());
        }
        return result.toString();
        // TODO add other attributes
    }

    @Override
    public synchronized boolean startEvictionTest() {
    	logger.debug("startEvictionTest() called when inspecting DB Object pool");
        if (state == PooledObjectState.IDLE) {
            state = PooledObjectState.EVICTION;
            return true;
        }

        return false;
    }


    /**
     * Allocates the object.
     *
     * @return {@code true} if the original state was {@link PooledObjectState#IDLE IDLE}
     */
    @Override
    public synchronized boolean allocate() {
    	logger.debug("allocate() called");
        if (state == PooledObjectState.IDLE) {
            state = PooledObjectState.ALLOCATED;
            lastBorrowTime = System.currentTimeMillis();
            lastUseTime = lastBorrowTime;
            borrowedCount++;
            return true;
        } else if (state == PooledObjectState.EVICTION) {
            // TODO Allocate anyway and ignore eviction test
            state = PooledObjectState.EVICTION_RETURN_TO_HEAD;
            return false;
        }
        // TODO if validating and testOnBorrow == true then pre-allocate for
        // performance
        return false;
    }

    /**
     * Deallocates the object and sets it {@link PooledObjectState#IDLE IDLE}
     * if it is currently {@link PooledObjectState#ALLOCATED ALLOCATED}.
     *
     * @return {@code true} if the state was {@link PooledObjectState#ALLOCATED ALLOCATED}
     */
    @Override
    public synchronized boolean deallocate() {
    	logger.debug("deallocate() called");
        if (state == PooledObjectState.ALLOCATED ||
                state == PooledObjectState.RETURNING) {
            state = PooledObjectState.IDLE;
            lastReturnTime = System.currentTimeMillis();
            return true;
        }

        return false;
    }

    /**
     * Sets the state to {@link PooledObjectState#INVALID INVALID}
     */
    @Override
    public synchronized void invalidate() {
    	logger.debug("invalidate() called");

    	object = null;

        state = PooledObjectState.INVALID;
    }

    @Override
    public void use() {
        lastUseTime = System.currentTimeMillis();
    }

    @Override
    public void printStackTrace(final PrintWriter writer) {

    }

    /**
     * Returns the state of this object.
     * @return state
     */
    @Override
    public synchronized PooledObjectState getState() {
    	logger.debug("GetState() called{}", state.toString());
        return state;
    }

    /**
     * Marks the pooled object as abandoned.
     */
    @Override
    public synchronized void markAbandoned() {
        state = PooledObjectState.ABANDONED;
    }

    /**
     * Marks the object as returning to the pool.
     */
    @Override
    public synchronized void markReturning() {
        state = PooledObjectState.RETURNING;
    }

    @Override
    public void setLogAbandoned(final boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

	@Override
	public int compareTo(PooledObject<DBObject> arg0)
	{
		final long lastActiveDiff = this.getLastReturnTime() - arg0.getLastReturnTime();
        if (lastActiveDiff == 0) {
            // Make sure the natural ordering is broadly consistent with equals
            // although this will break down if distinct objects have the same
            // identity hash code.
            // see java.lang.Comparable Javadocs
            return System.identityHashCode(this) - System.identityHashCode(arg0);
        }
        // handle int overflow
        return (int)Math.min(Math.max(lastActiveDiff, Integer.MIN_VALUE), Integer.MAX_VALUE);
	}

	@Override
	public boolean endEvictionTest(Deque<PooledObject<DBObject>> arg0)
	{
		logger.debug("endEvictionTest() called");
		if (state == PooledObjectState.EVICTION) {
            state = PooledObjectState.IDLE;
            return true;
        } else if (state == PooledObjectState.EVICTION_RETURN_TO_HEAD) {
            state = PooledObjectState.IDLE;
        }

        return false;
	}

	@Override
	public long getLastReturnTime()
	{
		// TODO Auto-generated method stub
		logger.debug("Get last return time: {}", (object.activeTransaction() ? Long.MAX_VALUE : 0));
		return object.activeTransaction() ? Long.MAX_VALUE:0;
	}


}
