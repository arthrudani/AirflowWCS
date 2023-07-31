package com.daifukuoc.wrxj.custom.ebs.plc.acp.route;

public class EBSAddress {

	private String type;
	private String bank;
	private String bay;
	private String level;

	public EBSAddress() {
		
	}
	
	public EBSAddress(String sAddress) {
		
		this.type = isNull(sAddress) ? null : parseType(sAddress);
		this.bank = isNull(sAddress) ? null : parseBank(sAddress);
		this.bay = isNull(sAddress) ? null : parseBay(sAddress);
		this.level = isNull(sAddress) ? null : parseLevel(sAddress);
	}
	
	public EBSAddress(String type, String bank, String bay, String level) {
		this.type = type;
		this.bank = bank;
		this.bay = bay;
		this.level = level;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getBay() {
		return bay;
	}

	public void setBay(String bay) {
		this.bay = bay;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
	
	public String getAddress() {
		return this.type + this.bank + this.bay + this.level;
	}
	
	public static String parseType(String sAddress) {
		return sAddress.substring(0, 2);
	}

	public static String parseBank(String sAddress) {
		return sAddress.substring(2,5);
	}

	public static String parseBay(String sAddress) {
		return sAddress.substring(5,8);
	}

	public static String parseLevel(String sAddress) {
		return sAddress.substring(8);
	}
	
	private boolean isNull(String address) {
		return address == null || address.isEmpty();
	}
	
}
