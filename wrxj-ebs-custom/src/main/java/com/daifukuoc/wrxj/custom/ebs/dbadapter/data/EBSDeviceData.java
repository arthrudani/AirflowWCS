package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;


import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class EBSDeviceData extends DeviceData
{
	public static final String LOC_SEQ_METHOD_NAME     = "ILOCSEQMETHOD";

	private int    iLocSeqMethod;
	
	public EBSDeviceData()
	{
	  super();
	}
	
	/**
	   * This helps in debugging when we want to print the whole structure.
	   */
	  @Override
	  public String toString()
	  {
		  String s = "iLocSeqMethod: " + getLocSeqMethod() + SKDCConstants.EOL_CHAR;

			    return(s + super.toString());
	  }

	  

	  /**
	   * Resets the data in this class to the default.
	   */
	  @Override
	  public void clear()
	  {
	    super.clear();                     // Pull in default behaviour.

	    iLocSeqMethod    = 1;  
	  }
	  
	  
	  /*---------------------------------------------------------------------------
    Column value get methods go here.
	  ---------------------------------------------------------------------------*/


	  public int getLocSeqMethod()
	  {
		  return(iLocSeqMethod);
	  }
	  
	  /*---------------------------------------------------------------------------
	   ******** Column Setting methods go here. ********
		---------------------------------------------------------------------------*/

	  public void setLocSeqMethod(int inLocSeqMethod)
	  {
		  iLocSeqMethod = inLocSeqMethod;
		  addColumnObject(new ColumnObject(LOC_SEQ_METHOD_NAME, Integer.valueOf(iLocSeqMethod)));
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
		  
		  if (isColName.equals(LOC_SEQ_METHOD_NAME))
		  {
			  setLocSeqMethod(((Integer)ipColValue));
		  }
		  else
		  {
			  vnRetValue = super.setField(isColName, ipColValue);
		  }

		  return vnRetValue;
	  }
}
