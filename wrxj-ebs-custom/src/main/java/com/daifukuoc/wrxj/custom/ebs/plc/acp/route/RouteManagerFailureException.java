package com.daifukuoc.wrxj.custom.ebs.plc.acp.route;

public class RouteManagerFailureException extends Exception{

	private static final long serialVersionUID = -3489902819522664679L;
	
	public RouteManagerFailureException() {
		super();
	}
	
	public RouteManagerFailureException(String detail) {
		super(detail);
	}
	
	public RouteManagerFailureException(String detail, Throwable ex) {
		super(detail, ex);
	}
}
