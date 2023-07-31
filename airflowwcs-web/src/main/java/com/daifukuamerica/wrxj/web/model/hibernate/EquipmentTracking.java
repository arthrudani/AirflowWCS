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
package com.daifukuamerica.wrxj.web.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class to support tracking display
 *
 * @author mandrus
 */
@Entity
@Table(name = "EquipmentMonitorTrackingView")
public class EquipmentTracking
{
	/**
	 * Constructor for Hibernate
	 */
	public EquipmentTracking()
	{
	}

	@Id
	@Column(name = "iid")
	private String iid;

	@Column(name = "sEMGraphicID")
	private String graphicID;

	@Column(name = "sEMDeviceID")
	private String deviceID;

	@Column(name = "sEMTrackingID")
	private String trackingID;

	@Column(name = "sEMBarcode")
	private String barcode;

	@Column(name = "sEMStatus")
	private String status;

	@Column(name = "sEMOrigin")
	private String origin;

	@Column(name = "sEMDestination")
	private String destination;

	@Column(name = "sEMSize")
	private String size;

	public String getId()
	{
		return iid;
	}

	public void setId(String id)
	{
		this.iid = id;
	}

	public String getGraphicID()
	{
		return graphicID;
	}

	public void setGraphicID(String graphicID)
	{
		this.graphicID = graphicID;
	}

	public String getDeviceID()
	{
		return deviceID;
	}

	public void setDeviceID(String deviceID)
	{
		this.deviceID = deviceID;
	}

	public String getTrackingID()
	{
		return trackingID;
	}

	public void setTrackingID(String trackingID)
	{
		this.trackingID = trackingID;
	}

	public String getBarcode()
	{
		return barcode;
	}

	public void setBarcode(String barcode)
	{
		this.barcode = barcode;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getOrigin()
	{
		return origin;
	}

	public void setOrigin(String origin)
	{
		this.origin = origin;
	}

	public String getDestination()
	{
		return destination;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	public String getSize()
	{
		return size;
	}

	public void setSize(String size)
	{
		this.size = size;
	}
}
