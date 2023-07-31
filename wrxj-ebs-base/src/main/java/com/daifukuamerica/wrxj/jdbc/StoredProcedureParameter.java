package com.daifukuamerica.wrxj.jdbc;

public class StoredProcedureParameter
{
  Object inParam;
  Object outParam;
  
  public StoredProcedureParameter(Object inParam, Object outParam)
  {
    this.inParam = inParam;
    this.outParam = outParam;
  }

  public Object getInParam()
  {
    return inParam;
  }
  public Object getOutParam()
  {
    return outParam;
  }

  public void setInParam(Object inParam)
  {
    this.inParam = inParam;
  }
  public void setOutParam(Object outParam)
  {
    this.outParam = outParam;
  }
}
