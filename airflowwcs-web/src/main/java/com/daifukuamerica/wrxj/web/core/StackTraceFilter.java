/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to remove noise from ridiculously long stack traces
 *
 * @author mandrus
 */
public class StackTraceFilter
{
	public static final String DEFAULT_FILTER = "daifuku";

	/**
	 * Filter a stack trace with the default filter
	 * @param t
	 * @return
	 */
	public static Throwable filter(Throwable t)
	{
		return filter(t, DEFAULT_FILTER);
	}

	/**
	 * Filter a stack trace with a specified filter
	 * @param t
	 * @param filter - if match, include
	 * @return
	 */
	public static Throwable filter(Throwable t, String filter)
	{
		Pattern p = Pattern.compile(filter);
		List<StackTraceElement> vpFilteredStack = new ArrayList<>();
		int filtered = 0;
		for (StackTraceElement ste : t.getStackTrace())
		{
			Matcher m = p.matcher(ste.toString());
			if (m.find())
			{
				vpFilteredStack.add(ste);
			}
			else
			{
				filtered++;
			}
		}
		if (filtered > 0)
		{
			vpFilteredStack.add(new StackTraceElement(".", ".", "Filtered lines", filtered));
		}
		t.setStackTrace(vpFilteredStack.toArray(new StackTraceElement[0]));
		return t;
	}
}
