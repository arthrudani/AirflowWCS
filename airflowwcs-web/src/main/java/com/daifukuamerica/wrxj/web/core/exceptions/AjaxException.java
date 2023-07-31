package com.daifukuamerica.wrxj.web.core.exceptions;

/**
 * An attempt to create an interim ajax exception for incomplete ajax calls
 * to bypass server error codes being generated. 
 * 
 * Author: dystout
 * Created : Feb 2, 2018
 *
 */
public class AjaxException extends Exception
{

	private static final long serialVersionUID = -4610305170368657094L;

	public AjaxException(){ 
		super(); 
	}
	
	public AjaxException(String message){
		super(message); 
	}
}
