package com.daifukuamerica.wrxj.device.agv.messages;

/**
 * Description:<BR>
 *  Class for holding a field and its associated length.  This class allows
 *  us to build an array containing multiple similar field values but perhaps
 *  with a different field lengths.  A Map would not allow this.
 *
 *  @author   A.D.
 *  @version  1.0
 *  @since    19-May-2009
 */
public class FieldTemplate
{
  private Object mpField  = null;
  private int mnFieldLength = 0;

  public FieldTemplate(Object isField, int inFieldLength)
  {
    super();
    mpField  = isField;
    mnFieldLength = inFieldLength;
  }

  @Override
  public String toString()
  {
    String vsStr = "  Field = " + mpField +
                   "\n  FieldLength = " + mnFieldLength + "\n";

    return(vsStr);
  }

  public void setField(Object isField)
  {
    mpField = isField;
  }

  public void setFieldLength(int inFieldLen)
  {
    mnFieldLength = inFieldLen;
  }

  public Object getField()
  {
    return(mpField);
  }

  public int getFieldLength()
  {
    return(mnFieldLength);
  }
}
