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
package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

/**
 * Data Model for log viewer
 *
 * @author mandrus
 */
public class LogViewerDataModel
{
	public Date startingDate;
	public Date endingDate;
	public String description;

	/**
	 * Constructor
	 */
	public LogViewerDataModel()
	{
	}

	/*==================================================================*/
	/* Accessors 														*/
	/*==================================================================*/
	public Date getStartingDate()
	{
		return startingDate;
	}

	public void setStartingDate(Date startingDate)
	{
		this.startingDate = startingDate;
	}

	public Date getEndingDate()
	{
		return endingDate;
	}

	public void setEndingDate(Date endingDate)
	{
		this.endingDate = endingDate;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}
