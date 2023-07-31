package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;


import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCConstants;


import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Purchase Order Line Data operations.
 *
 * @author       sbw
 * @version      1.0
 * @since 30-May-04
 */
public class EBSPurchaseOrderLineData extends PurchaseOrderLineData
{

	  public static final String HEIGHT_NAME          = "IHEIGHT";

/*---------------------------------------------------------------------------
                 Database fields for PurchaseOrderLine table.
  ---------------------------------------------------------------------------*/
 
  private int mnHeight          = 0;

  public EBSPurchaseOrderLineData()
  {
    super();
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
	  String s = "mnHeight: " + getHeight() + SKDCConstants.EOL_CHAR;

		    return(s + super.toString());
  }

  

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    mnHeight = 0;
  }
  
  


/*---------------------------------------------------------------------------
                       Column value get methods go here.
  ---------------------------------------------------------------------------*/

  /**
   * Fetches Height
   * @return Height as an int
   */
  public int getHeight()
  {
    return(mnHeight);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  

  /**
   * Sets Height Flag value
   */
  public void setHeight(int inHeight)
  {
    mnHeight = inHeight;
    addColumnObject(new ColumnObject(HEIGHT_NAME, Integer.valueOf(mnHeight)));
  }

  /**
   * This generates the string for the field that is changed.
   * 
   * @param isColName
   * @param ipOld
   * @param ipNew
   */
  @Override
  public String getActionDesc(String isColName, Object ipOld, Object ipNew)
  {
    String s = "Field [" + isColName + "]:";

    try
    {
      String vsOld = "";
      String vsNew = "";

      if( isColName.equals(HEIGHT_NAME))
      {
    	  vsOld = DBTrans.getStringValue(isColName, ((Integer)ipOld).intValue());
          vsNew = DBTrans.getStringValue(isColName, ((Integer)ipNew).intValue());
          s = s + " Old [" + vsOld + "] New [" + vsNew + "]";
      }
      else
      {
    	  super.getActionDesc(isColName, ipOld, ipNew);
      }

    }
    catch (NoSuchFieldException e)
    {
      s = s + " #### NoSuchFieldException #### ";
    }
    return s;
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

	    if (isColName.equals(HEIGHT_NAME))
	    {
	      setHeight(((Integer)ipColValue));
	    }
	    else
	    {
	      vnRetValue = super.setField(isColName, ipColValue);
	    }

	    return vnRetValue;
  }
}
