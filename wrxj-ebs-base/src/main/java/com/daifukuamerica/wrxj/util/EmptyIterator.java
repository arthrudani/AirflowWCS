package com.daifukuamerica.wrxj.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptyIterator<E> implements Enumeration<E>, Iterator<E>
{
  public EmptyIterator()
  {
  }

/* **************************************************************************
* Iteration implementation
****************************************************************************/

  public boolean hasNext()
  {
    return false;
  }

  public E next()
  {
    throw new NoSuchElementException();
  }

  public void remove()
  {
    throw new UnsupportedOperationException();
  }

/* **************************************************************************
* Enumeration implementation
****************************************************************************/

  public boolean hasMoreElements()
  {
    return false;
  }
  
  public E nextElement()
  {
    throw new NoSuchElementException();
  }

  public int size()
  {
    return 0;
  }
}  