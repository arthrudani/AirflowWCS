package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup;

import java.util.Objects;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;

/**
 * The context class for {@link PLCItemPickedUpTransaction}
 * 
 * @author ST
 *
 */
public class ItemPickedUpContext implements EBSTransactionContext {
    private String loadID;
    private String fromLocation;
    private String toLocation;
    private boolean success = false;

    public ItemPickedUpContext(String loadID, String fromLocation, String toLocation) {
        super();
        this.loadID = loadID;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    public String getLoadID() {
        return loadID;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromLocation, loadID, toLocation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemPickedUpContext other = (ItemPickedUpContext) obj;
        return Objects.equals(fromLocation, other.fromLocation) && Objects.equals(loadID, other.loadID)
                && Objects.equals(toLocation, other.toLocation);
    }

    @Override
    public String toString() {
        return "PLCItemPickUpContext [loadID=" + loadID + ", fromLocation=" + fromLocation + ", toLocation="
                + toLocation + "]";
    }
}
