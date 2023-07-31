package com.daifukuamerica.wrxj.common;

/**
 * Misc. helper methods for Enums.
 *
 * @author A.D.
 * @since  16-May-2017
 */
public class EnumUtility
{
  /**
   * Enum Reference finder.
   * @param inValue
   * @return the Enum object represented by the passed in number.
   */
  public static synchronized <E extends DacEnum> E getEnumObject(Class<E> ipEnumClass, int inValue)
  {
    E vpTypeRef = null;
    for(E vpType : ipEnumClass.getEnumConstants())
    {
      if (vpType.getIntValue() == inValue)
      {
        vpTypeRef = vpType;
        break;
      }
    }
    return(vpTypeRef);
  }

  /**
   * Enum Reference finder.
   * @param inValue
   * @return the Enum object represented by the passed in String.
   */
  public static synchronized <E extends DacEnum> E getEnumObject(Class<E> ipEnumClass, String isValue)
  {
    E vpTypeRef = null;

    for(E vpType : ipEnumClass.getEnumConstants())
    {
      if (vpType.getStringValue().equals(isValue))
      {
        vpTypeRef = vpType;
        break;
      }
    }

    return(vpTypeRef);
  }
}
