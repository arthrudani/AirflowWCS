package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineEnum.EXPIRATIONDATE;

import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class SystemHBData extends AbstractSKDCData
{
    // Formalize Pseudo-Column Names.

	public static final String SYSTEM_NAME     = "SSYSTEM";
	public static final String HBTIME_NAME       = "DHBTIME";

	/*---------------------------------------------------------------------------
	Database fields for StationData table.
	---------------------------------------------------------------------------*/

	private String msSystem          = "";
	private Date mdHBTime          = new Date();

	public SystemHBData()
	{
		super();
	}

	/**
	 * 	This helps in debugging when we want to print the whole structure.
	 */
	
	public String toString()
	{
		String s = "msSystem: " + getSystem() + SKDCConstants.EOL_CHAR + 
				   "mdHBTime: " + getHBTime() + SKDCConstants.EOL_CHAR;

		return(s + super.toString());
	}



	/**
	 * 	Resets the data in this class to the default.
	 */
	public void clear()
	{		
		msSystem = "";
		mdHBTime = new Date();
	}




	/*---------------------------------------------------------------------------
	Column value get methods go here.
	---------------------------------------------------------------------------*/

	
	public String getSystem()
	{
		return(msSystem);
	}
	
	public Date getHBTime()
	{
		return(mdHBTime);
	}

	/*---------------------------------------------------------------------------
	 ******** Column Setting methods go here. ********
	---------------------------------------------------------------------------*/


	/**
	 * Sets BCSDEVICEID  value
	 */
	public void setSystem(String isSystem)
	{
		msSystem = isSystem;
		addColumnObject(new ColumnObject(SYSTEM_NAME, new String(msSystem)) );
	}
	
	public void setHBTime(Date ipHBTime)
	{
		mdHBTime = ipHBTime;
		addColumnObject(new ColumnObject(HBTIME_NAME, mdHBTime) );
	}

	/**
	 *  Required set field method.  This method figures out what column was
	 *  passed to it and sets the value.  This allows us to have a generic
	 *  method for all DB interfaces.
	 */
	public int setField(String isColName, Object ipColValue)
	{
		int vnRetValue = 0;

		if (isColName.equals(SYSTEM_NAME))
		{
			setSystem(((String)ipColValue));
		}
		else if (isColName.equals(HBTIME_NAME))
		{
			setHBTime(((Date)ipColValue));
		}

		return vnRetValue;
	}

	@Override
	public boolean equals(AbstractSKDCData eskdata) {
		// TODO Auto-generated method stub
		return false;
	}
}
