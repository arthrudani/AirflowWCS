package com.daifukuamerica.wrxj.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that lists objects in an array. If the object array is
 * modififed on another thread during iteration, results are undefined.
 */
public class EnumerationIterator implements Iterator 
{
  Enumeration<Object> mpEnum;

  /**
   * Constructs an iterator for an Enumeration.
   * @param vpEnum the vpEnumeration to iterate over
   */
  public EnumerationIterator(Enumeration<Object> ipEnum) 
  {
    this.mpEnum = ipEnum;
  }

  /**
   * Returns whether there is another object.
   * @return true if there is another object
   */
  public boolean hasNext() 
  {
    return mpEnum.hasMoreElements();
  }

  /**
   * Returns the next object if there is one.
   * @return the next object
   * @throws NoSuchElementException if there is no other object to return
   */
  public Object next() 
  {
    if (mpEnum.hasMoreElements())
      return mpEnum.nextElement();
    throw new NoSuchElementException();
  }

  /**
   * Always throws UnsupportedOperationException, because this iterator
   * does not support removing elements.
   */
  public void remove() 
  {
    throw new UnsupportedOperationException();
  }
}
