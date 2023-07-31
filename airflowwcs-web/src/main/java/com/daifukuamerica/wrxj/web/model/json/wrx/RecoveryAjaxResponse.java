/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.model.json.wrx;

import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;

/**
 * Class to encapsulate the UI flow for recovery
 * @author mandrus
 */
public class RecoveryAjaxResponse extends AjaxResponse
{
	private RecoveryAjaxResponse yes;
	private RecoveryAjaxResponse no;

	public RecoveryAjaxResponse()
	{
	}

	public RecoveryAjaxResponse(Integer responseCode, String responseMessage)
	{
		super(responseCode, responseMessage);
	}

	public RecoveryAjaxResponse(String responseMessage, RecoveryAjaxResponse yes, RecoveryAjaxResponse no)
	{
		super(AjaxResponseCodes.PROMPT, responseMessage);
		setYes(yes);
		setNo(no);
	}

	public RecoveryAjaxResponse getYes()
	{
		return yes;
	}

	public void setYes(RecoveryAjaxResponse yes)
	{
		this.yes = yes;
	}

	public RecoveryAjaxResponse getNo()
	{
		return no;
	}

	public void setNo(RecoveryAjaxResponse no)
	{
		this.no = no;
	}
}
