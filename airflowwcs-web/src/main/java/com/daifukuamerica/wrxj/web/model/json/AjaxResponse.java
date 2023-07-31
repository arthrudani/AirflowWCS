
/**
 * 
 */
package com.daifukuamerica.wrxj.web.model.json;

import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.google.gson.annotations.SerializedName;

/**
 * Generalized Ajax Response - Returns an error code with a message to our
 * client in the form of a popup or other notification. 
 * 
 * 		responsCode: {@link AjaxResponseCodes}
 * 		responseMessage: "WHATEVA U DESIRE... im tired..."
 * 
 * Author: dystout
 * Created : Apr 6, 2017
 * 
 */
public class AjaxResponse
{
	@SerializedName("responseCode")
	private Integer responseCode = AjaxResponseCodes.DEFAULT;
	
	@SerializedName("responseMessage")
	private String responseMessage = ""; 
	
	public AjaxResponse()
	{
		//
	}
	
	public AjaxResponse(Integer responseCode, String responseMessage)
	{
		this.responseCode = responseCode; 
		this.responseMessage = responseMessage; 
	}

	public Integer getResponseCode()
	{
		return responseCode;
	}

	public void setResponseCode(Integer rCode)
	{
		this.responseCode = rCode;
	}

	public String getResponseMessage()
	{
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage)
	{
		this.responseMessage = responseMessage;
	}
	
	public void setResponse(Integer responseCode, String responseMessage){
		this.responseCode = responseCode; 
		this.responseMessage = responseMessage; 
	}
	
	

}