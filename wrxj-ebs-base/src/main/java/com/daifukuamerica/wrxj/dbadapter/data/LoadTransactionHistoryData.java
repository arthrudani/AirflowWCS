package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.LoadTransactionHistoryEnum.*;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description:<BR>
 * Title:  Class to handle LoadData Object.
 * Description : Handles all data for load transaction history
 * @author       MT  Converted to use ColumnObjects for building SQL.
 * @version      1.0
 * @since       22-May-23
 */
public class LoadTransactionHistoryData extends AbstractSKDCData {
	
	public static final String LOADID_NAME = LOADID.getName();
	public static final String CONTAINERTYPE_NAME = CONTAINERTYPE.getName();
	public static final String BARCODE_NAME = BARCODE.getName();
	public static final String CARRIER_NAME = CARRIER.getName();
	public static final String  GLOBALID_NAME = GLOBALID.getName();
	public static final String FLIGHTNUM_NAME = FLIGHTNUM.getName();
	public static final String FLIGHTSTD_NAME = FLIGHTSTD.getName();
	public static final String LASTEXPIRYDATE_NAME = LASTEXPIRYDATE.getName();
	public static final String EXPECTEDRECEIPTDATE_NAME = EXPECTEDRECEIPTDATE.getName();
	public static final String ARRIVALAISLEID_NAME = ARRIVALAISLEID.getName();
	public static final String STORAGELOCATIONID_NAME = STORAGELOCATIONID.getName();
	public static final String STORAGELIFTERID_NAME = STORAGELIFTERID.getName();
	public static final String STORAGELOADATEBSDATE_NAME = STORAGELOADATEBSDATE.getName();
	public static final String STORAGELOADPICKEDBYLIFTERDATE_NAME = STORAGELOADPICKEDBYLIFTERDATE.getName();
	public static final String STORAGELOADDROPPEDBYLIFTERDATE_NAME = STORAGELOADDROPPEDBYLIFTERDATE.getName();
	public static final String STORAGESHUTTLEID_NAME = STORAGESHUTTLEID.getName();
	public static final String STORAGELOADPICKEDBYSHUTTLEDATE_NAME = STORAGELOADPICKEDBYSHUTTLEDATE.getName();
	public static final String STORAGELOADDROPPEDBYSHUTTLEDATE_NAME = STORAGELOADDROPPEDBYSHUTTLEDATE.getName();
	public static final String RETRIEVALORDERDATE_NAME = RETRIEVALORDERDATE.getName();
	public static final String RETRIEVALSHUTTLEID_NAME = RETRIEVALSHUTTLEID.getName();
	public static final String RETRIEVALLOADPICKEDBYSHUTTLEDATE_NAME = RETRIEVALLOADPICKEDBYSHUTTLEDATE.getName();
	public static final String RETRIEVALLOADDROPPEDBYSHUTTLEDATE_NAME = RETRIEVALLOADDROPPEDBYSHUTTLEDATE.getName();
	public static final String RETRIEVALLIFTERID_NAME = RETRIEVALLIFTERID.getName();
	public static final String RETRIEVALLOADPICKEDBYLIFFTERDATE_NAME = RETRIEVALLOADPICKEDBYLIFFTERDATE.getName();
	public static final String RETRIEVALLOADDROPPEDBYLIFFTERDATE_NAME = RETRIEVALLOADDROPPEDBYLIFFTERDATE.getName();
	public static final String RETRIEVALLOCATIONID_NAME = RETRIEVALLOCATIONID.getName();
	public static final String STORAGEDURATION_NAME = STORAGEDURATION.getName();
	public static final String DWELVETIME_NAME = DWELVETIME.getName();
	public static final String RETRIEVALDURATION_NAME = RETRIEVALDURATION.getName();
	public static final String STORAGELIFTERWAITINGTIME_NAME = STORAGELIFTERWAITINGTIME.getName();
	public static final String STORAGESHUTTLEWAITINGTIME_NAME = STORAGESHUTTLEWAITINGTIME.getName();
	public static final String RETRIEVALLIFTERWAITINGTIME_NAME = RETRIEVALLIFTERWAITINGTIME.getName();
	public static final String RETRIEVALSHUTTLEWAITINGTIME_NAME = RETRIEVALSHUTTLEWAITINGTIME.getName();
	public static final String ISCOMPLETED_NAME = ISCOMPLETED.getName();
	public static final String ERTIMEOUTDATE_NAME = ERTIMEOUTDATE.getName();
	

	// -------------------Load Transaction History Table data -----------------------------
	private String sLoadID          				= "";
	private String sContainerType 					= "";
	private String sBarcode							= "";
	private String sCarrier							= "";
	private String sGlobalID						= "";
	private String sFlightNum 						= "";
	private Date dFlightSTD							;
	private Date dLastExpiryDate					;
	private Date dExpectedReceiptDate				=new Date();
	private String sArrivalAisleID					= "";
	private String sStorageLocationID				= "";
	private String sStorageLifterID					= "";
	private Date dStorageLoadAtEBSDate				;
	private Date dStorageLoadPickedbyLifterDate 	;
	private Date dStorageLoadDroppedbyLifterDate 	;
	private String sStorageShuttleID				= "";
	private Date dStorageLoadPickedbyShuttleDate 	;
	private Date dStorageLoadDroppedbyshuttleDate 	;
	private Date dRetrievalOrderDate 				;
	private String dRetrievalShuttleID 				= "";
	private Date dRetrievalLoadPickedbyShuttleDate 	;
	private Date dRetrievalLoadDroppedbyshuttleDate ;
	private String sRetrievalLifterID 				= "";
	private Date dRetrievalLoadPickedbyLiffterDate 	;
	private Date dRetrievalLoadDroppedbyLiffterDate ;
	private String sRetrievalLocationID 			= "";
	private long iStorageDuration 					= 0l;
	private long iDwelveTime 						= 0l;
	private long iRetrievalDuration 					= 0l;
	private long iStorageLifterWaitingTime 			= 0l;
	private long iStorageShuttleWaitingTime 			= 0l;
	private long iRetrievalLifterWaitingTime 		= 0l;
	private long iRetrievalShuttleWaitingTime 		= 0l;
	private int iIsCompleted							= 0;
	private Date dERTimeoutDate						;
	
	
	// These are not data fields.  They're here to support weirdness in the 
	// transaction history screens.  Someday that will hopefully be changed.
	private Date mpStartingDate;
	private Date mpEndingDate;
	  
	private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();
	
	public LoadTransactionHistoryData() {
		sdf.applyPattern(SKDCConstants.DateFormatString);
		clear();
		initColumnMap(mpColumnMap, LoadTransactionHistoryEnum.class);
	}
	
	@Override
	public String toString() {
		String s = 
				"sLoadID: " + sLoadID +
				"\nsContainerType: " + sContainerType +
				"\nsBarcode: " + sBarcode +
				"\nsCarrier: " + sCarrier +
				"\nsGlobalID: " + sGlobalID +
				"\nsFlightNum: " + sFlightNum +
				"\ndFlightSTD: " + (Objects.isNull(dFlightSTD) ? null : sdf.format(dFlightSTD)) +
				"\ndLastExpiryDate: " + (Objects.isNull(dLastExpiryDate) ? null : sdf.format(dLastExpiryDate)) +
				"\ndExpectedReceiptDate: " + (Objects.isNull(dExpectedReceiptDate) ? null : sdf.format(dExpectedReceiptDate)) +
				"\nsArrivalAisleID: " + sArrivalAisleID +
				"\nsStorageLocationID: " + sStorageLocationID +
				"\nsStorageLifterID: " + sStorageLifterID +
				"\ndStorageLoadAtEBSDate: " + (Objects.isNull(dStorageLoadAtEBSDate) ? null : sdf.format(dStorageLoadAtEBSDate)) +
				"\ndStorageLoadPickedbyLifterDate: " + (Objects.isNull(dStorageLoadPickedbyLifterDate) ? null : sdf.format(dStorageLoadPickedbyLifterDate)) +
				"\ndStorageLoadDroppedbyLifterDate: " + (Objects.isNull(dStorageLoadDroppedbyLifterDate) ? null : sdf.format(dStorageLoadDroppedbyLifterDate)) +
				"\nsStorageShuttleID: " + sStorageShuttleID +
				"\ndStorageLoadPickedbyShuttleDate: " + (Objects.isNull(dStorageLoadPickedbyShuttleDate) ? null : sdf.format(dStorageLoadPickedbyShuttleDate)) +
				"\ndStorageLoadDroppedbyshuttleDate: " + (Objects.isNull(dStorageLoadDroppedbyshuttleDate) ? null : sdf.format(dStorageLoadDroppedbyshuttleDate)) +
				"\ndRetrievalOrderDate: " + (Objects.isNull(dRetrievalOrderDate) ? null : sdf.format(dRetrievalOrderDate)) +
				"\ndRetrievalShuttleID: " + dRetrievalShuttleID +
				"\ndRetrievalLoadPickedbyShuttleDate: " + (Objects.isNull(dRetrievalLoadPickedbyShuttleDate) ? null : sdf.format(dRetrievalLoadPickedbyShuttleDate)) +
				"\ndRetrievalLoadDroppedbyshuttleDate: " + (Objects.isNull(dRetrievalLoadDroppedbyshuttleDate) ? null : sdf.format(dRetrievalLoadDroppedbyshuttleDate)) +
				"\nsRetrievalLifterID: " + sRetrievalLifterID +
				"\ndRetrievalLoadPickedbyLiffterDate: " + (Objects.isNull(dRetrievalLoadPickedbyLiffterDate) ? null : sdf.format(dRetrievalLoadPickedbyLiffterDate)) +
				"\ndRetrievalLoadDroppedbyLiffterDate: " + (Objects.isNull(dRetrievalLoadDroppedbyLiffterDate) ? null : sdf.format(dRetrievalLoadDroppedbyLiffterDate)) +
				"\nsRetrievalLocationID: " + sRetrievalLocationID +
				"\niStorageDuration: " + iStorageDuration +
				"\niDwelveTime: " + iDwelveTime +
				"\niRetrievalDuration: " + iRetrievalDuration +
				"\niStorageLifterWaitingTime: " + iStorageLifterWaitingTime +
				"\niStorageShuttleWaitingTime: " + iStorageShuttleWaitingTime +
				"\niRetrievalLifterWaitingTime: " + iRetrievalLifterWaitingTime +
				"\niRetrievalShuttleWaitingTime: " + iRetrievalShuttleWaitingTime +
				"\niIsCompleted: " + iIsCompleted +
				"\ndERTimeoutDate: " + dERTimeoutDate;
		super.toString();
		return s;
	}
	
	@Override
	public void clear() {
		super.clear();
		sLoadID = "";
		sContainerType = "";
		sBarcode = "";
		sCarrier = "";
		sGlobalID = "";
		sFlightNum = "";
		sArrivalAisleID = "";
		sStorageLocationID = "";
		sStorageLifterID = "";
		sStorageShuttleID = "";
		dRetrievalShuttleID = "";
		sRetrievalLifterID = "";
		sRetrievalLocationID = "";
		iStorageDuration = 0;
		iDwelveTime = 0;
		iRetrievalDuration = 0;
		iStorageLifterWaitingTime = 0;
		iStorageShuttleWaitingTime = 0;
		iRetrievalLifterWaitingTime = 0;
		iRetrievalShuttleWaitingTime = 0;
		iIsCompleted = 0;
	}
	
	@Override
	public boolean equals(AbstractSKDCData eskdata) {
		LoadTransactionHistoryData lth = (LoadTransactionHistoryData) eskdata;
		return (sLoadID.equals(lth.getLoadID()) && sBarcode.equals(lth.getBarcode()) && sGlobalID.equals(lth.getGlobalID()));
	}

	public String getLoadID() {
		return sLoadID;
	}

	public void setLoadID(String loadID) {
		sLoadID = loadID;
	    addColumnObject(new ColumnObject(LOADID_NAME, sLoadID));
	}

	public String getBarcode() {
		return sBarcode;
	}

	public void setBarcode(String barcode) {
		sBarcode = barcode;
	    addColumnObject(new ColumnObject(BARCODE_NAME, sBarcode));
	}

	public String getContainerType() {
		return sContainerType;
	}

	public void setContainerType(String containerType) {
		sContainerType = containerType;
		addColumnObject(new ColumnObject(CONTAINERTYPE_NAME, sContainerType));
	}

	public String getCarrier() {
		return sCarrier;
	}

	public void setCarrier(String carrier) {
		sCarrier = carrier;
		addColumnObject(new ColumnObject(CARRIER_NAME, sCarrier));
	}

	public String getGlobalID() {
		return sGlobalID;
	}

	public void setGlobalID(String globalID) {
		sGlobalID = globalID;
		addColumnObject(new ColumnObject(GLOBALID_NAME, sGlobalID));
	}

	public String getFlightNum() {
		return sFlightNum;
	}

	public void setFlightNum(String flightNum) {
		sFlightNum = flightNum;
		addColumnObject(new ColumnObject(FLIGHTNUM_NAME, sFlightNum));
	}

	public Date getFlightSTD() {
		return dFlightSTD;
	}

	public void setFlightSTD(Date flightSTD) {
		dFlightSTD = flightSTD;
		addColumnObject(new ColumnObject(FLIGHTSTD_NAME, dFlightSTD));
	}

	public Date getLastExpiryDate() {
		return dLastExpiryDate;
	}

	public void setLastExpiryDate(Date lastExpiryDate) {
		dLastExpiryDate = lastExpiryDate;
		addColumnObject(new ColumnObject(LASTEXPIRYDATE_NAME, dLastExpiryDate));
	}

	public Date getExpectedReceiptDate() {
		return dExpectedReceiptDate;
	}

	public void setExpectedReceiptDate(Date expectedReceiptDate) {
		dExpectedReceiptDate = expectedReceiptDate;
		addColumnObject(new ColumnObject(EXPECTEDRECEIPTDATE_NAME, dExpectedReceiptDate));
	}

	public String getArrivalAisleID() {
		return sArrivalAisleID;
	}

	public void setArrivalAisleID(String arrivalAisleID) {
		sArrivalAisleID = arrivalAisleID;
		addColumnObject(new ColumnObject(ARRIVALAISLEID_NAME, sArrivalAisleID));
	}

	public String getStorageLocationID() {
		return sStorageLocationID;
	}

	public void setStorageLocationID(String storageLocationID) {
		sStorageLocationID = storageLocationID;
		addColumnObject(new ColumnObject(STORAGELOCATIONID_NAME, sStorageLocationID));
	}

	public String getStorageLifterID() {
		return sStorageLifterID;
	}

	public void setStorageLifterID(String storageLifterID) {
		sStorageLifterID = storageLifterID;
		addColumnObject(new ColumnObject(STORAGELIFTERID_NAME, sStorageLifterID));
	}

	public Date getStorageLoadAtEBSDate() {
		return dStorageLoadAtEBSDate;
	}

	public void setStorageLoadAtEBSDate(Date storageLoadAtEBSDate) {
		dStorageLoadAtEBSDate = storageLoadAtEBSDate;
		addColumnObject(new ColumnObject(STORAGELOADATEBSDATE_NAME, dStorageLoadAtEBSDate));
	}

	public Date getStorageLoadPickedbyLifterDate() {
		return dStorageLoadPickedbyLifterDate;
	}

	public void setStorageLoadPickedbyLifterDate(Date storageLoadPickedbyLifterDate) {
		dStorageLoadPickedbyLifterDate = storageLoadPickedbyLifterDate;
		addColumnObject(new ColumnObject(STORAGELOADPICKEDBYLIFTERDATE_NAME, dStorageLoadPickedbyLifterDate));
	}

	public Date getStorageLoadDroppedbyLifterDate() {
		return dStorageLoadDroppedbyLifterDate;
	}

	public void setStorageLoadDroppedbyLifterDate(Date storageLoadDroppedbyLifterDate) {
		dStorageLoadDroppedbyLifterDate = storageLoadDroppedbyLifterDate;
		addColumnObject(new ColumnObject(STORAGELOADDROPPEDBYLIFTERDATE_NAME, dStorageLoadDroppedbyLifterDate));
	}

	public String getStorageShuttleID() {
		return sStorageShuttleID;
	}

	public void setStorageShuttleID(String storageShuttleID) {
		sStorageShuttleID = storageShuttleID;
		addColumnObject(new ColumnObject(STORAGESHUTTLEID_NAME, sStorageShuttleID));
	}

	public Date getStorageLoadPickedbyShuttleDate() {
		return dStorageLoadPickedbyShuttleDate;
	}

	public void setStorageLoadPickedbyShuttleDate(Date storageLoadPickedbyShuttleDate) {
		dStorageLoadPickedbyShuttleDate = storageLoadPickedbyShuttleDate;
		addColumnObject(new ColumnObject(STORAGELOADPICKEDBYSHUTTLEDATE_NAME, dStorageLoadPickedbyShuttleDate));
	}

	public Date getStorageLoadDroppedbyshuttleDate() {
		return dStorageLoadDroppedbyshuttleDate;
	}

	public void setStorageLoadDroppedbyshuttleDate(Date storageLoadDroppedbyshuttleDate) {
		dStorageLoadDroppedbyshuttleDate = storageLoadDroppedbyshuttleDate;
		addColumnObject(new ColumnObject(STORAGELOADDROPPEDBYSHUTTLEDATE_NAME, dStorageLoadDroppedbyshuttleDate));
	}

	public Date getRetrievalOrderDate() {
		return dRetrievalOrderDate;
	}

	public void setRetrievalOrderDate(Date retrievalOrderDate) {
		dRetrievalOrderDate = retrievalOrderDate;
		addColumnObject(new ColumnObject(RETRIEVALORDERDATE_NAME, dRetrievalOrderDate));
	}

	public String getRetrievalShuttleID() {
		return dRetrievalShuttleID;
	}

	public void setRetrievalShuttleID(String retrievalShuttleID) {
		dRetrievalShuttleID = retrievalShuttleID;
		addColumnObject(new ColumnObject(RETRIEVALSHUTTLEID_NAME, dRetrievalShuttleID));
	}

	public Date getRetrievalLoadPickedbyShuttleDate() {
		return dRetrievalLoadPickedbyShuttleDate;
	}

	public void setRetrievalLoadPickedbyShuttleDate(Date retrievalLoadPickedbyShuttleDate) {
		dRetrievalLoadPickedbyShuttleDate = retrievalLoadPickedbyShuttleDate;
		addColumnObject(new ColumnObject(RETRIEVALLOADPICKEDBYSHUTTLEDATE_NAME, dRetrievalLoadPickedbyShuttleDate));
	}

	public Date getRetrievalLoadDroppedbyshuttleDate() {
		return dRetrievalLoadDroppedbyshuttleDate;
	}

	public void setRetrievalLoadDroppedbyshuttleDate(Date retrievalLoadDroppedbyshuttleDate) {
		dRetrievalLoadDroppedbyshuttleDate = retrievalLoadDroppedbyshuttleDate;
		addColumnObject(new ColumnObject(RETRIEVALLOADDROPPEDBYSHUTTLEDATE_NAME, dRetrievalLoadDroppedbyshuttleDate));
	}

	public String getRetrievalLifterID() {
		return sRetrievalLifterID;
	}

	public void setRetrievalLifterID(String retrievalLifterID) {
		sRetrievalLifterID = retrievalLifterID;
		addColumnObject(new ColumnObject(RETRIEVALLIFTERID_NAME, sRetrievalLifterID));
	}

	public Date getRetrievalLoadPickedbyLiffterDate() {
		return dRetrievalLoadPickedbyLiffterDate;
	}

	public void setRetrievalLoadPickedbyLiffterDate(Date retrievalLoadPickedbyLiffterDate) {
		dRetrievalLoadPickedbyLiffterDate = retrievalLoadPickedbyLiffterDate;
		addColumnObject(new ColumnObject(RETRIEVALLOADPICKEDBYLIFFTERDATE_NAME, dRetrievalLoadPickedbyLiffterDate));
	}

	public Date getRetrievalLoadDroppedbyLiffterDate() {
		return dRetrievalLoadDroppedbyLiffterDate;
	}

	public void setRetrievalLoadDroppedbyLiffterDate(Date retrievalLoadDroppedbyLiffterDate) {
		dRetrievalLoadDroppedbyLiffterDate = retrievalLoadDroppedbyLiffterDate;
		addColumnObject(new ColumnObject(RETRIEVALLOADDROPPEDBYLIFFTERDATE_NAME, retrievalLoadDroppedbyLiffterDate));
	}

	public String getRetrievalLocationID() {
		return sRetrievalLocationID;
	}

	public void setRetrievalLocationID(String retrievalLocationID) {
		sRetrievalLocationID = retrievalLocationID;
		addColumnObject(new ColumnObject(RETRIEVALLOCATIONID_NAME, sRetrievalLocationID));
	}

	public long getStorageDuration() {
		return iStorageDuration;
	}

	public void setStorageDuration(long storageDuration) {
		iStorageDuration = storageDuration;
		addColumnObject(new ColumnObject(STORAGEDURATION_NAME, iStorageDuration));
	}

	public long getDwelveTime() {
		return iDwelveTime;
	}

	public void setDwelveTime(long dwelveTime) {
		iDwelveTime = dwelveTime;
		addColumnObject(new ColumnObject(DWELVETIME_NAME, iDwelveTime));
	}

	public long getRetrievalDuration() {
		return iRetrievalDuration;
	}

	public void setRetrievalDuration(long retrievalDuration) {
		iRetrievalDuration = retrievalDuration;
		addColumnObject(new ColumnObject(RETRIEVALDURATION_NAME, iRetrievalDuration));
	}

	public long getStorageLifterWaitingTime() {
		return iStorageLifterWaitingTime;
	}

	public void setStorageLifterWaitingTime(long storageLifterWaitingTime) {
		iStorageLifterWaitingTime = storageLifterWaitingTime;
		addColumnObject(new ColumnObject(STORAGELIFTERWAITINGTIME_NAME, iStorageLifterWaitingTime));
	}

	public long getStorageShuttleWaitingTime() {
		return iStorageShuttleWaitingTime;
	}

	public void setStorageShuttleWaitingTime(long storageShuttleWaitingTime) {
		iStorageShuttleWaitingTime = storageShuttleWaitingTime;
		addColumnObject(new ColumnObject(STORAGESHUTTLEWAITINGTIME_NAME, iStorageShuttleWaitingTime));
	}

	public long getRetrievalLifterWaitingTime() {
		return iRetrievalLifterWaitingTime;
	}

	public void setRetrievalLifterWaitingTime(long retrievalLifterWaitingTime) {
		iRetrievalLifterWaitingTime = retrievalLifterWaitingTime;
		addColumnObject(new ColumnObject(RETRIEVALLIFTERWAITINGTIME_NAME, iRetrievalLifterWaitingTime));
	}

	public long getRetrievalShuttleWaitingTime() {
		return iRetrievalShuttleWaitingTime;
	}

	public void setRetrievalShuttleWaitingTime(long retrievalShuttleWaitingTime) {
		iRetrievalShuttleWaitingTime = retrievalShuttleWaitingTime;
		addColumnObject(new ColumnObject(RETRIEVALSHUTTLEWAITINGTIME_NAME, iRetrievalShuttleWaitingTime));
	}
	
	public int getIsCompleted() {
		return iIsCompleted;
	}

	public void setIsCompleted(int isCompleted) {
		iIsCompleted = isCompleted;
		addColumnObject(new ColumnObject(ISCOMPLETED_NAME, isCompleted));
	}
	
	
	
	public Date getERTimeoutDate() {
		return dERTimeoutDate;
	}

	public void setERTimeoutDate(Date eRTimeoutDate) {
		dERTimeoutDate = eRTimeoutDate;
		addColumnObject(new ColumnObject(ERTIMEOUTDATE_NAME, dERTimeoutDate));
	}
	
	public Date getStartingDate()
	{
		return mpStartingDate;
	}
	  
	public Date getEndingDate()
	{	
		return mpEndingDate;
	}
	public void setDateRangeKey(Date ipStartDate, Date ipEndDate)
	  {
	    setBetweenKey(FLIGHTSTD_NAME, ipStartDate, ipEndDate);
	    mpStartingDate = ipStartDate;
	    mpEndingDate = ipEndDate;
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

		switch ((LoadTransactionHistoryEnum) vpEnum) {
		case LOADID:
			setLoadID((String) ipColValue);
			break;
		case CONTAINERTYPE:
			setContainerType((String) ipColValue);
			break;
		case BARCODE:
			setBarcode((String) ipColValue);
			break;
		case CARRIER:
			setCarrier((String) ipColValue);
			break;
		case GLOBALID:
			setGlobalID((String) ipColValue);
			break;
		case FLIGHTNUM:
			setFlightNum((String) ipColValue);
			break;
		case FLIGHTSTD:
			setFlightSTD((Date) ipColValue);
			break;
		case LASTEXPIRYDATE:
			setLastExpiryDate((Date) ipColValue);
			break;
		case EXPECTEDRECEIPTDATE:
			setExpectedReceiptDate((Date) ipColValue);
			break;
		case ARRIVALAISLEID:
			setArrivalAisleID((String) ipColValue);
			break;
		case STORAGELOCATIONID:
			setStorageLocationID((String) ipColValue);
			break;
		case STORAGELIFTERID:
			setStorageLifterID((String) ipColValue);
			break;
		case STORAGELOADATEBSDATE:
			setStorageLoadAtEBSDate((Date) ipColValue);
			break;
		case STORAGELOADPICKEDBYLIFTERDATE:
			setStorageLoadPickedbyLifterDate((Date) ipColValue);
			break;
		case STORAGELOADDROPPEDBYLIFTERDATE:
			setStorageLoadDroppedbyLifterDate((Date) ipColValue);
			break;
		case STORAGESHUTTLEID:
			setStorageShuttleID((String) ipColValue);
			break;
		case STORAGELOADPICKEDBYSHUTTLEDATE:
			setStorageLoadPickedbyShuttleDate((Date) ipColValue);
			break;
		case STORAGELOADDROPPEDBYSHUTTLEDATE:
			setStorageLoadDroppedbyshuttleDate((Date) ipColValue);
			break;
		case RETRIEVALORDERDATE:
			setRetrievalOrderDate((Date) ipColValue);
			break;
		case RETRIEVALSHUTTLEID:
			setRetrievalShuttleID((String) ipColValue);
			break;
		case RETRIEVALLOADPICKEDBYSHUTTLEDATE:
			setRetrievalLoadPickedbyShuttleDate((Date) ipColValue);
			break;
		case RETRIEVALLOADDROPPEDBYSHUTTLEDATE:
			setRetrievalLoadDroppedbyshuttleDate((Date) ipColValue);
			break;
		case RETRIEVALLIFTERID:
			setRetrievalLifterID((String) ipColValue);
			break;
		case RETRIEVALLOADPICKEDBYLIFFTERDATE:
			setRetrievalLoadPickedbyLiffterDate((Date) ipColValue);
			break;
		case RETRIEVALLOADDROPPEDBYLIFFTERDATE:
			setRetrievalLoadDroppedbyLiffterDate((Date) ipColValue);
			break;
		case RETRIEVALLOCATIONID:
			setRetrievalLocationID((String) ipColValue);
			break;
		case STORAGEDURATION:
			setStorageDuration((int) ipColValue);
			break;
		case DWELVETIME:
			setDwelveTime((int) ipColValue);
			break;
		case RETRIEVALDURATION:
			setRetrievalDuration((int) ipColValue);
			break;
		case STORAGELIFTERWAITINGTIME:
			setStorageLifterWaitingTime((int) ipColValue);
			break;
		case STORAGESHUTTLEWAITINGTIME:
			setStorageShuttleWaitingTime((int) ipColValue);
			break;
		case RETRIEVALLIFTERWAITINGTIME:
			setRetrievalLifterWaitingTime((int) ipColValue);
			break;
		case RETRIEVALSHUTTLEWAITINGTIME:
			setRetrievalShuttleWaitingTime((int) ipColValue);
			break;
		case ISCOMPLETED:
			setIsCompleted((int) ipColValue);
			break;
		case ERTIMEOUTDATE:
			setERTimeoutDate((Date) ipColValue);
			break;
		}

		return (0);
	}
	
}
