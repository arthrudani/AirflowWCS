package com.daifukuamerica.wrxj.dbadapter.data;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.daifukuamerica.wrxj.dbadapter.data.MoveCommandDataEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class MoveCommandData extends AbstractSKDCData {

	public static final String DEVICEID_NAME = DEVICEID.getName();
	public static final String LOADID_NAME = LOADID.getName();
	public static final String FROM_DEST = FROM.getName();
	public static final String TO_DEST = TODEST.getName();
	public static final String COMMAND_NAME = COMMAND.getName();
	public static final String STATUS_NAME = STATUS.getName();
	public static final String MOVETYPE_NAME = MOVETYPE.getName();
	public static final String ORDERID_NAME = ORDERID.getName();
	public static final String GLOBALID_NAME = GLOBALID.getName();
	public static final String ITEMID_NAME = ITEMID.getName();
	public static final String FLIGHTNUM_NAME = FLIGHTNUM.getName();
	public static final String FLIGHTSTD_NAME = FLIGHTSTD.getName();
	public static final String FINAL_SORT_LOC_ID_NAME = FINALSORTLOCATION.getName();
	public static final String ORDERTYPE_NAME = ORDERTYPE.getName();
	public static final String CREATEDDATE_NAME = CREATEDDATE.getName();
	public static final String LASTMODIFYDATE_NAME = LASTMODIFYDATE.getName();

	private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

	/*---------------------------------------------------------------------------
	Database fields for AsrsMetaData table.
	---------------------------------------------------------------------------*/
	private String sDeviceID = "";
	private String sLoadID = "";
	private String sFrom = "";
	private String sToDest = "";
	private String sCommand = "";
	private int iCmdStatus;
	private int iCmdMoveType;
	private String sOrderID = "";
	private String sItemID = "";
	private String sFlightNum = "";
	private Date dFlightSTD = new Date();
	private String sFinalSortLocationID = "";
	private int iCmdOrderType;
	private Date dCreatedDate = new Date();
	private Date dLastModifyDate = new Date();
	private String sGlobalID = "";

	public MoveCommandData() {
		sdf.applyPattern(SKDCConstants.DateFormatString);
		clear();
		initColumnMap(mpColumnMap, MoveCommandDataEnum.class);
	}

	@Override
	public void clear() {
		super.clear();
		sDeviceID = "";
		sLoadID = "";
		sFrom = "";
		sToDest = "";
		sCommand = "";
		iCmdStatus = DBConstants.CMD_READY;
		iCmdMoveType = DBConstants.CMD_DIRECT;
		sOrderID = "";
		sItemID = "";
		sFlightNum = "";
		dFlightSTD = new Date();
		sFinalSortLocationID = "";
		iCmdOrderType = DBConstants.CMD_STORAGE;
		dCreatedDate = new Date();
		dLastModifyDate = new Date();
		sGlobalID = "";
	}

	/**
	 * Method to perform clone of <code>MoveCommandData</code>.
	 *
	 * @return copy of <code>MoveCommandData</code>
	 */
	@Override
	public MoveCommandData clone() {
		MoveCommandData moveCommandData = (MoveCommandData) super.clone();
		return moveCommandData;
	}

	@Override
	public boolean equals(AbstractSKDCData eskdata) {
		if (this == eskdata)
			return (true);
		MoveCommandData md = (MoveCommandData) eskdata;
		return (sDeviceID.equals(md.getDeviceID()) && sLoadID.equals(md.getLoadID()) && sFrom.equals(md.getFrom())
				&& sToDest.equals(md.getToDest()) && sCommand == md.getCommand());
	}

	@Override
	public String toString() {
		return "MoveCommandData [sDeviceID=" + sDeviceID + ", sLoadID=" + sLoadID + ", sFrom=" + sFrom + ", sToDest="
				+ sToDest + ", sCommand=" + sCommand + ", iStatus=" + iCmdStatus + ", iMoveType=" + iCmdMoveType
				+ ", Orderid=" + sOrderID + ", sItemID=" + sItemID + ", sFlightNum=" + sFlightNum + ", sFlightSTD="
				+ dFlightSTD + ", FinalSortLocationID=" + sFinalSortLocationID + ", iOrderType=" + iCmdOrderType
				+ ", dCreatedDT=" + dCreatedDate + ", dLastModifyDT=" + dLastModifyDate + "]";
	}

	public String getDeviceID() {
		return sDeviceID;
	}

	public void setDeviceID(String sDeviceID) {
		this.sDeviceID = checkForNull(sDeviceID);
		addColumnObject(new ColumnObject(DEVICEID_NAME, sDeviceID));
	}

	public String getLoadID() {
		return sLoadID;
	}

	public void setLoadID(String sLoadID) {
		this.sLoadID = checkForNull(sLoadID);
		addColumnObject(new ColumnObject(LOADID_NAME, sLoadID));
	}

	public String getFrom() {
		return sFrom;
	}

	public void setFrom(String sFrom) {
		this.sFrom = checkForNull(sFrom);
		addColumnObject(new ColumnObject(FROM_DEST, sFrom));
	}

	public String getToDest() {
		return sToDest;
	}

	public void setToDest(String sToDest) {
		this.sToDest = checkForNull(sToDest);
		addColumnObject(new ColumnObject(TO_DEST, sToDest));
	}

	public String getCommand() {
		return sCommand;
	}

	public void setCommand(String sCommand) {
		this.sCommand = checkForNull(sCommand);
		addColumnObject(new ColumnObject(COMMAND_NAME, sCommand));
	}

	public int getCmdStatus() {
		return iCmdStatus;
	}

	public void setCmdStatus(int iCmdStatus) {
		this.iCmdStatus = iCmdStatus;
		addColumnObject(new ColumnObject(STATUS_NAME, iCmdStatus));
	}

	public int getCmdMoveType() {
		return iCmdMoveType;
	}

	public void setCmdMoveType(int iCmdMoveType) {
		this.iCmdMoveType = iCmdMoveType;
		addColumnObject(new ColumnObject(MOVETYPE_NAME, iCmdMoveType));
	}

	public String getOrderid() {
		return sOrderID;
	}

	public void setOrderid(String sOrderID) {
		this.sOrderID = sOrderID;
		addColumnObject(new ColumnObject(ORDERID_NAME, sOrderID));
	}

	public String getItemID() {
		return sItemID;
	}

	public void setItemID(String sItemID) {
		this.sItemID = sItemID;
		addColumnObject(new ColumnObject(ITEMID_NAME, sItemID));
	}

	public String getFlightNum() {
		return sFlightNum;
	}

	public void setFlightNum(String sFlightNum) {
		this.sFlightNum = sFlightNum;
		addColumnObject(new ColumnObject(FLIGHTNUM_NAME, sFlightNum));
	}

	public Date getFlightSTD() {
		return dFlightSTD;
	}

	public void setFlightSTD(Date dFlightSTD) {
		this.dFlightSTD = dFlightSTD;
		addColumnObject(new ColumnObject(FLIGHTSTD_NAME, dFlightSTD));
	}

	public String getFinalSortLocationID() {
		return sFinalSortLocationID;
	}

	public void setFinalSortLocationID(String sFinalSortLocationID) {
		this.sFinalSortLocationID = sFinalSortLocationID;
		addColumnObject(new ColumnObject(FINAL_SORT_LOC_ID_NAME, sFinalSortLocationID));
	}

	public int getCmdOrderType() {
		return iCmdOrderType;
	}

	public void setCmdOrderType(int iCmdOrderType) {
		this.iCmdOrderType = iCmdOrderType;
		addColumnObject(new ColumnObject(ORDERTYPE_NAME, iCmdOrderType));
	}
	
	public Date getCreatedDate() {
		return dCreatedDate;
	}

	public void setCreatedDate(Date dCreatedDate) {
		this.dCreatedDate = dCreatedDate;
		addColumnObject(new ColumnObject(CREATEDDATE_NAME, dCreatedDate));
	}
	
	public Date getLastModifyDate() {
		return dLastModifyDate;
	}

	public void setLastModifyDate(Date dLastModifyDate) {
		this.dLastModifyDate = dLastModifyDate;
		addColumnObject(new ColumnObject(LASTMODIFYDATE_NAME, dLastModifyDate));
	}

	public String getGlobalID() {
		return sGlobalID;
	}

	public void setGlobalID(String sGlobalID) {
		this.sGlobalID = sGlobalID;
		addColumnObject(new ColumnObject(GLOBALID_NAME, sGlobalID));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param {@inheritDoc}
	 * @param {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public int setField(String isColName, Object ipColValue) {
		TableEnum vpEnum = mpColumnMap.get(isColName);
		if (vpEnum == null) {
			return super.setField(isColName, ipColValue);
		}

		switch ((MoveCommandDataEnum) vpEnum) {
		case LOADID:
			setLoadID((String) ipColValue);
			break;

		case COMMAND:
			setCommand((String) ipColValue);
			break;

		case TODEST:
			setToDest((String) ipColValue);
			break;

		case FROM:
			setFrom((String) ipColValue);
			break;
		case DEVICEID:
			setDeviceID((String) ipColValue);
			break;
		case STATUS:
			setCmdStatus((int) ipColValue);
			break;
		case MOVETYPE:
			setCmdMoveType((int) ipColValue);
			break;
		case ORDERTYPE:
			setCmdOrderType((int) ipColValue);
			break;
		case ORDERID:
			setOrderid((String) ipColValue);
			break;
		case ITEMID:
			setItemID((String) ipColValue);
			break;
		case FLIGHTNUM:
			setFlightNum((String) ipColValue);
			break;
		case FLIGHTSTD:
			setFlightSTD((Date) ipColValue);
			break;
		case CREATEDDATE:
			setCreatedDate((Date) ipColValue);
			break;
		case LASTMODIFYDATE:
			setLastModifyDate((Date) ipColValue);
			break;
		case GLOBALID:
			setGlobalID((String) ipColValue);
			break;

		case FINALSORTLOCATION:
			setFinalSortLocationID((String) ipColValue);
			break;

		}

		return (0);
	}
}
