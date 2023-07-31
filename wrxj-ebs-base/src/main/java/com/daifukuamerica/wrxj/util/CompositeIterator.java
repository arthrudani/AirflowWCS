package com.daifukuamerica.wrxj.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Combines multiple iterators into one. The first iterator is used until it
 * is empty, then the other iterators are used in order.
 */
public class CompositeIterator implements Iterator 
{
  Iterator<?>[] iterArr;
  int pos = 0;

  /**
   * Constructs a composite iterator from two iterators.
   *
   * @param iter1   The first iterator
   * @param iter2   The second iterator
   */
  public CompositeIterator(Iterator iter1, Iterator iter2) 
  {
    if (iter1 == null || iter2 == null) 
      throw new NullPointerException();

    this.iterArr = new Iterator<?>[] { iter1, iter2 };
  }

  /**
   * Returns whether there is an available object. This method only
   * returns false if all iterators have no available objects.
   *
   * @return true if there is an available object.
   */
  public synchronized boolean hasNext() 
  {
    boolean ret;

    if (pos < iterArr.length) 
    {
      ret = iterArr[pos].hasNext();
      while (ret == false && pos + 1 < iterArr.length) 
      {
        pos += 1;
        ret = iterArr[pos].hasNext();
      }
    } else
      ret = false;

    return ret;
  }

  /**
   * Returns the next available object. If the current iterator throws a
   * NoSuchElementException, the next iterator is tried.
   *
   * @return the next available object.
   * @exception NoSuchElementException If all iterators have no more
   *    available objects.
   */  
  public synchronized Object next() 
  {
    Object ret = null;

    if (pos < iterArr.length) 
    {
      NoSuchElementException save = null;
      boolean valid = false;

      try 
      {
        ret = iterArr[pos].next();
        valid = true;
      } catch (NoSuchElementException e) 
      {
        save = e;
      }

      while (!valid && pos + 1 < iterArr.length) 
      {
        pos += 1;

        try 
        {
          ret = iterArr[pos].next();
          valid = true;
        } catch (NoSuchElementException e) 
        {
          save = e;
        }
      }

      if (!valid) 
        throw save;
    } else 
      throw new NoSuchElementException();

    return ret;
  }

  /**
   * Removes from the current underlying collection the last element
   * returned by the current iterator (optional operation). The effect of
   * calling this method is completely determined by the current iterator.
   */
  public synchronized void remove() 
  {
    iterArr[pos].remove();
  }
}
