package com.daifukuamerica.wrxj.web.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class NotLoggedInException extends Exception
{
	private static final long serialVersionUID = -3332292346834265371L;
	
	public NotLoggedInException(){ 
		super("FORBIDDEN: User not logged in"); 
	}
	
	public NotLoggedInException(String message){
		super(message); 
	}

}
