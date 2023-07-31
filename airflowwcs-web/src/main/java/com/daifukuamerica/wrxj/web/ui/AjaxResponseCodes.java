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
package com.daifukuamerica.wrxj.web.ui;

import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;

/**
 * Response codes to return to client on ajax requests for pass/fail or other client side action
 *
 * Used by {@link AjaxResponse} and descendant classes
 *
 * Author: dystout
 * Created : Apr 6, 2017
 */
public interface AjaxResponseCodes
{
	final int ALTPROMPT = -4;
	final int PROMPT = -3;
	final int WARNING = -2;
	final int FAILURE = -1;
	final int INFO = 0;
	final int SUCCESS = 1;
	final int SHORTPICK = 2;
	final int OVERPICK = 3;
	final int ISLASTPICK = 4;
	final int DEFAULT = -666;
}
