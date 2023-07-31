package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class EBSLocationData extends LocationData
{
  // Formalize Pseudo-Column Names.

	  public static final String PRIMARYSEARCHORDER_NAME         = "IPRIMARYSEARCHORDER";
	  public static final String SECONDARYSEARCHORDER_NAME       = "ISECONDARYSEARCHORDER";

	/*---------------------------------------------------------------------------
	                 Database fields for PurchaseOrderLine table.
	  ---------------------------------------------------------------------------*/

	  private int    iPrimarySearchOrder    = 1;
	  private int    iSecondarySearchOrder  = 1;                                      
	
	  public EBSLocationData()
	  {
	    super();
	  }

	  /**
	   * This helps in debugging when we want to print the whole structure.
	   */
	  @Override
	  public String toString()
	  {
		  String s = "iPrimarySearchOrder: " + getPrimarySearchOrder() + SKDCConstants.EOL_CHAR +
				  	 "iSecondarySearchOrder: " + getSecondarySearchOrder() + SKDCConstants.EOL_CHAR;

			    return(s + super.toString());
	  }

	  

	  /**
	   * Resets the data in this class to the default.
	   */
	  @Override
	  public void clear()
	  {
	    super.clear();                     // Pull in default behaviour.

	    iPrimarySearchOrder    = 1;
		iSecondarySearchOrder  = 1;       
	  }
	  
	  
	  /*---------------------------------------------------------------------------
      Column value get methods go here.
	  ---------------------------------------------------------------------------*/


	  public int getPrimarySearchOrder()
	  {
		  return(iPrimarySearchOrder);
	  }
	  
	  public int getSecondarySearchOrder()
	  {
		  return(iSecondarySearchOrder);
	  }

	  /*---------------------------------------------------------------------------
	   ******** Column Setting methods go here. ********
		---------------------------------------------------------------------------*/

	  public void setPrimarySearchOrder(int inPrimarySearchOrder)
	  {
		  iPrimarySearchOrder = inPrimarySearchOrder;
		  addColumnObject(new ColumnObject(PRIMARYSEARCHORDER_NAME, Integer.valueOf(iPrimarySearchOrder)));
	  }


	  public void setSecondarySearchOrder(int inSecondarySearchOrder)
	  {
		  iSecondarySearchOrder = inSecondarySearchOrder;
		  addColumnObject(new ColumnObject(SECONDARYSEARCHORDER_NAME, Integer.valueOf(iSecondarySearchOrder)));
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
		  
		  if (isColName.equals(PRIMARYSEARCHORDER_NAME))
		  {
			  setPrimarySearchOrder(((Integer)ipColValue));
		  }
		  else if (isColName.equals(SECONDARYSEARCHORDER_NAME))
		  {
			  setSecondarySearchOrder(((Integer)ipColValue));
		  }
		  else
		  {
			  vnRetValue = super.setField(isColName, ipColValue);
		  }

		  return vnRetValue;
	  }
}
