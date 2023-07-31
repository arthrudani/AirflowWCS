package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.LinkedList;

/**
 * Description:<BR>
 *  Class to hold Allocation diagnostic information.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 20-Jul-05<BR>
 */
public class AllocationProbe implements Cloneable
{
  private LinkedList<ColumnObject> errorList = new LinkedList<ColumnObject>();

 /**
  * Method to facilitate information gathering as code executes.
  * @param isMethodName The mname
  * @param isErrorMessage 
  */
  public void addProbeDetails(String isMethodName, String isErrorMessage)
  {
    errorList.add(new ColumnObject(isMethodName, isErrorMessage));
  }

 /**
  * Serializes information objects.
  * @return String representing all info. collected.
  */
  @Override
  public String toString()
  {
    String s = "";
    for(ColumnObject cobj : errorList)
    {
      s = s +  cobj.getColumnValue() + SKDCConstants.EOL_CHAR + SKDCConstants.EOL_CHAR;
    }

    return(s);
  }
  
 /**
  * Checks if there is diagnostic info. to report.
  * @return true if some type of diagnostic info. was collected.
  */
  public boolean diagnosticsCollected()
  {
    return(errorList.size() != 0);
  }
  
 /**
  * Clones this object.
  * @return Object representaton of the cloned object.
  */
  @Override
  public Object clone()
  {
    AllocationProbe clonedProbe;
    
    try
    {
      clonedProbe = (AllocationProbe)super.clone();
      LinkedList<ColumnObject> oldErrorList = (LinkedList)errorList.clone();
      for(ColumnObject cobj : oldErrorList)
      {
        clonedProbe.errorList.add((ColumnObject)cobj.clone());
      }
    }
    catch (CloneNotSupportedException e)
    {
      throw new InternalError(e.getMessage());
    }
    
    return(clonedProbe);
  }
  
 /**
  * Method clears any accumulated data.
  */
  public void reset()
  {
    errorList.clear();
  }
}
