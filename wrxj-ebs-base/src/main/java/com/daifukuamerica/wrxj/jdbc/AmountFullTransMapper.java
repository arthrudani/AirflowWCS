package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.util.SKDCUtility;

/**
 * Data class to hold Amount Full Description and corresponding decimal value.
 * @author A.D.
 * @since  27-Mar-2008
 */
public class AmountFullTransMapper
{
  private int    mnPartialAmtFullTranValue;
  private double mdPartialAmtFullDecimal;
  private String msPartialAmtFullDesc;

  public int getPartialAmtFullTranVal()
  {
    return(mnPartialAmtFullTranValue);
  }
  
  public double getPartialAmtFullDecimal()
  {
    return mdPartialAmtFullDecimal;
  }

  public String getPartialAmtFullDesc()
  {
    return msPartialAmtFullDesc;
  }

  public void setPartialAmtFullTranVal(int inTranValue)
  {
    mnPartialAmtFullTranValue = inTranValue;
  }
  
  public void setPartialAmtFullDecimal(double idPartialAmtFullDecimal)
  {
    mdPartialAmtFullDecimal = idPartialAmtFullDecimal;
  }

  public void setPartialAmtFullDesc(String isPartialAmtFullDesc)
  {
    msPartialAmtFullDesc = isPartialAmtFullDesc;
  }
  
  public static double parseFraction(String isFraction)
  {
    String[] vasNumDenom = isFraction.split("/");
    double vdPartAmt = Double.parseDouble(vasNumDenom[0])/Double.parseDouble(vasNumDenom[1]);
    
    return(SKDCUtility.getTruncatedDouble(vdPartAmt));
  }
}
