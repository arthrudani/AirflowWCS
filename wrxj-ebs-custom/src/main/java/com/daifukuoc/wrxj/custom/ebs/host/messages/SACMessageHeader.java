package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;

public class SACMessageHeader extends AbstractSKDCData {

    private int iMsgLength = 0;
    private int iSeqNo = 0;
    private short iMsgType = 0;
    private String sEquipmentID = "";
    private int iHours = 0;
    private int iMinutes = 0;
    private int iMilliSeconds = 0;
    private int iMsgVersion = 0;
    private String sSeparater = ",";

    /**
     * Default constructor. This constructor finds the correct message formatter to use for this message.
     */
    public SACMessageHeader() {

    }

    /**
     * This helps in debugging when we want to print the whole structure.
     */
    @Override
    public String toString() {
        // KR: to string will return all data in comma separated format (Don't change it)
        String s = iMsgLength + sSeparater + iSeqNo + sSeparater + iMsgType + sSeparater + sEquipmentID + sSeparater
                + +iHours + sSeparater + iMinutes + sSeparater + iMilliSeconds + sSeparater + iMsgVersion;

        return (s);
    }

    /**
     * Resets the data in this class to the default.
     */
    public void clear() { // Pull in default behaviour.

        iMsgLength = 0;
        iSeqNo = 0;
        iMsgType = 0;
        sEquipmentID = "";
        iHours = 0;
        iMinutes = 0;
        iMilliSeconds = 0;
        iMsgVersion = 0;
    }

    /*---------------------------------------------------------------------------
     ******** Column getter methods go here. ********
    ---------------------------------------------------------------------------*/
    public int getMsgLength() {
        return iMsgLength;
    }

    public int getSeqNo() {
        return iSeqNo;
    }

    public short getMsgType() {
        return iMsgType;
    }

    public String getEquipmentID() {
        return sEquipmentID;
    }

    public int getHours() {
        return iHours;
    }

    public int getMinutes() {
        return iMinutes;
    }

    public int getMilliSeconds() {
        return iMilliSeconds;
    }

    public int getMsgVersion() {
        return iMsgVersion;
    }

    /*---------------------------------------------------------------------------
                 ******** Column Setting methods go here. ********
    ---------------------------------------------------------------------------*/

    public void setEquipmentId(String isEquipmentID) {
        sEquipmentID = checkForNull(isEquipmentID);
    }

    public void setMsgType(short inMsgType) {
        iMsgType = inMsgType;
    }

    public void setSeqNo(int inSeqNo) {
        iSeqNo = inSeqNo;
    }

    public void setHours(int inHours) {
        iHours = inHours;
    }

    public void setMinutes(int inMinutes) {
        iMinutes = inMinutes;
    }

    public void setMilliSeconds(int inMilliSeconds) {
        iMilliSeconds = inMilliSeconds;
    }

    public void setMsgLength(int inMsgLength) {
        iMsgLength = inMsgLength;
    }

    public void setMsgVersion(int inMsgVersion) {
        iMsgVersion = inMsgVersion;
    }

    @Override
    public boolean equals(AbstractSKDCData obj) {
        return equals((Object) obj);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SACMessageHeader other = (SACMessageHeader) obj;
        return iHours == other.iHours && iMilliSeconds == other.iMilliSeconds && iMinutes == other.iMinutes
                && iMsgLength == other.iMsgLength && iMsgType == other.iMsgType && iMsgVersion == other.iMsgVersion
                && iSeqNo == other.iSeqNo && Objects.equals(sEquipmentID, other.sEquipmentID)
                && Objects.equals(sSeparater, other.sSeparater);
    }
}
