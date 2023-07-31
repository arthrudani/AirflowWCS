package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class EBSStationData extends StationData
{
    // Formalize Pseudo-Column Names.

	public static final String BCSDEVICEID_NAME       = "SBCSDEVICEID";
	public static final String SECONDARYDEVICEID_NAME       = "SSECONDARYDEVICEID";

	/*---------------------------------------------------------------------------
	Database fields for StationData table.
	---------------------------------------------------------------------------*/

	private String msBCSDeviceID          = "";
	private String msSecondaryDeviceID          = "";

	public EBSStationData()
	{
		super();
	}

	/**
	 * 	This helps in debugging when we want to print the whole structure.
	 */
	@Override
	public String toString()
	{
		String s = "msBCSDeviceID: " + getBCSDeviceID() + SKDCConstants.EOL_CHAR + 
				   "msSecondaryDeviceID: " + getSecondaryDeviceID() + SKDCConstants.EOL_CHAR;

		return(s + super.toString());
	}



	/**
	 * 	Resets the data in this class to the default.
	 */
	@Override
	public void clear()
	{
		super.clear();                     // Pull in default behaviour.
		
		msBCSDeviceID = "";
		msSecondaryDeviceID = "";
	}




	/*---------------------------------------------------------------------------
	Column value get methods go here.
	---------------------------------------------------------------------------*/

	/**
	 * Fetches BCSDEVICEID
	 * @return BCSDEVICEID as an String
	 */
	public String getBCSDeviceID()
	{
		return(msBCSDeviceID);
	}
	
	public String getSecondaryDeviceID()
	{
		return(msSecondaryDeviceID);
	}

	/*---------------------------------------------------------------------------
	 ******** Column Setting methods go here. ********
	---------------------------------------------------------------------------*/


	/**
	 * Sets BCSDEVICEID  value
	 */
	public void setBCSDeviceID(String isBCSDeviceID)
	{
		msBCSDeviceID = isBCSDeviceID;
		addColumnObject(new ColumnObject(BCSDEVICEID_NAME, new String(msBCSDeviceID)));
	}
	
	public void setSecondaryDeviceID(String isSecondaryDeviceID)
	{
		msSecondaryDeviceID = isSecondaryDeviceID;
		addColumnObject(new ColumnObject(SECONDARYDEVICEID_NAME, new String(msSecondaryDeviceID)));
	}


	/**
	 *  Required set field method.  This method figures out what column was
	 *  passed to it and sets the value.  This allows us to have a generic
	 *  method for all DB interfaces.
	 */
	@Override
	public int setField(String isColName, Object ipColValue)
	{
		int vnRetValue = 0;

		if (isColName.equals(BCSDEVICEID_NAME))
		{
			setBCSDeviceID(((String)ipColValue));
		}
		else if (isColName.equals(SECONDARYDEVICEID_NAME))
		{
			setSecondaryDeviceID(((String)ipColValue));
		}
		else
		{
			vnRetValue = super.setField(isColName, ipColValue);
		}

		return vnRetValue;
	}
}
