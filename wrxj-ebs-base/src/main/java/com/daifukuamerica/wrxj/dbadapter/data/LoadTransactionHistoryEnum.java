package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

public enum LoadTransactionHistoryEnum implements TableEnum {
	
	LOADID("SLOADID"),
	CONTAINERTYPE("SCONTAINERTYPE"),
	BARCODE("SBARCODE"),
	CARRIER("SCARRIER"),
	GLOBALID("SGLOBALID"),
	FLIGHTNUM("SFLIGHTNUM"),
	FLIGHTSTD("DFLIGHTSTD"),
	LASTEXPIRYDATE("DLASTEXPIRYDATE"),
	EXPECTEDRECEIPTDATE("DEXPECTEDRECEIPTDATE"),
	ARRIVALAISLEID("SARRIVALAISLEID"),
	STORAGELOCATIONID("SSTORAGELOCATIONID"),
	STORAGELIFTERID("SSTORAGELIFTERID"),
	STORAGELOADATEBSDATE("DSTORAGELOADATEBSDATE"),
	STORAGELOADPICKEDBYLIFTERDATE("DSTORAGELOADPICKEDBYLIFTERDATE"),
	STORAGELOADDROPPEDBYLIFTERDATE("DSTORAGELOADDROPPEDBYLIFTERDATE"),
	STORAGESHUTTLEID("SSTORAGESHUTTLEID"),
	STORAGELOADPICKEDBYSHUTTLEDATE("DSTORAGELOADPICKEDBYSHUTTLEDATE"),
	STORAGELOADDROPPEDBYSHUTTLEDATE("DSTORAGELOADDROPPEDBYSHUTTLEDATE"),
	RETRIEVALORDERDATE("DRETRIEVALORDERDATE"),
	RETRIEVALSHUTTLEID("DRETRIEVALSHUTTLEID"),
	RETRIEVALLOADPICKEDBYSHUTTLEDATE("DRETRIEVALLOADPICKEDBYSHUTTLEDATE"),
	RETRIEVALLOADDROPPEDBYSHUTTLEDATE("DRETRIEVALLOADDROPPEDBYSHUTTLEDATE"),
	RETRIEVALLIFTERID("SRETRIEVALLIFTERID"),
	RETRIEVALLOADPICKEDBYLIFFTERDATE("DRETRIEVALLOADPICKEDBYLIFFTERDATE"),
	RETRIEVALLOADDROPPEDBYLIFFTERDATE("DRETRIEVALLOADDROPPEDBYLIFFTERDATE"),
	RETRIEVALLOCATIONID("SRETRIEVALLOCATIONID"),
	STORAGEDURATION("ISTORAGEDURATION"),
	DWELVETIME("IDWELVETIME"),
	RETRIEVALDURATION("IRETRIEVALDURATION"),
	STORAGELIFTERWAITINGTIME("ISTORAGELIFTERWAITINGTIME"),
	STORAGESHUTTLEWAITINGTIME("ISTORAGESHUTTLEWAITINGTIME"),
	RETRIEVALLIFTERWAITINGTIME("IRETRIEVALLIFTERWAITINGTIME"),
	RETRIEVALSHUTTLEWAITINGTIME("IRETRIEVALSHUTTLEWAITINGTIME");
	
	private String msMessageName;

	LoadTransactionHistoryEnum(String isMessageName) {
		msMessageName = isMessageName;
	}

	@Override
	public String getName() {
		return (msMessageName);
	}

}