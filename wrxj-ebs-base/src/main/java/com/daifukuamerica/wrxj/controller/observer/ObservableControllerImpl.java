package com.daifukuamerica.wrxj.controller.observer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Stephen Kendorski
 */
public class ObservableControllerImpl extends Observable
{
  
  private Map<String, List<Object>> observers = new HashMap<String, List<Object>>();

  private String keyName = null;
  
  private String controllerGroupName = null;

  private int intData = 0;
  
  private String stringData = null;

  public ObservableControllerImpl()
  {
  }

  /**
   * Adds an observer to the set of observers for this object, provided that it
   * is not the same as some observer already in the set. The order in which
   * notifications will be delivered to multiple observers is not specified.
   * See the class comment.
   *
   * @param selector the inter-process-communication event filter to use.
   * @param o an observer to be added.
   * @return true if added observer was the first for the specified selector
   */
  public boolean addObserver(String selector, Observer o)
  {
    boolean result = false;
    List<Object> eventObservers = observers.get(selector);
    if (eventObservers == null)
    {
      eventObservers = new ArrayList<Object>();
      // First entry in list is the Event Selector String.
      eventObservers.add(selector);
      observers.put(selector, eventObservers);
      result = true;
    }
    eventObservers.add(o);
    return result;
  }

  /**
   * @return selector if deleted observer was the last for the specified selector
   */
  public String removeObserver(Observer o)
  {
    String result = null;
    Collection<List<Object>> eventObservers = observers.values();
    if (eventObservers != null)
    {
      Iterator<List<Object>> eventObserversIterator = eventObservers.iterator();
      while (eventObserversIterator.hasNext())
      {
        List<Object> observerList = eventObserversIterator.next();
        if (observerList.contains(o))
        {
          observerList.remove(o);
          if (observerList.size() == 1)
          {
            // Only entry in the list is the First entry in list (the Event
            // Selector String).  OK to clear the list and remove it from the
            // obververs.
            String selector = (String)observerList.get(0);
            observerList.clear();
            observers.remove(selector);
            result = selector;
          }
          break;
        }
      }
    }
    return result;
  }

  public void notifyObservers(String selector)
  {
    List<Object> eventObservers = observers.get(selector);
    if (eventObservers != null)
    {
      Iterator<Object> eventObserversIterator = eventObservers.iterator();
      // First entry in list is the Event Selector String - skip over it.
      eventObserversIterator.next();
      while (eventObserversIterator.hasNext())
      {
        Observer observer = (Observer)eventObserversIterator.next();
        observer.update(this, null);
      }
    }
  }

  public boolean hasObservers(String selector)
  {
    return observers.containsKey(selector);
  }

  public String getControllerGroupName()
  {
    return controllerGroupName;
  }

  public void setControllerGroupName(String s)
  {
    controllerGroupName = s;
    setChanged();
  }

  public String getKeyName()
  {
    return keyName;
  }

  public void setKeyName(String s)
  {
    keyName = s;
    setChanged();
  }

  public int getIntData()
  {
    return intData;
  }

  public void setIntData(int value)
  {
    intData = value;
    setChanged();
  }

  public String getStringData()
  {
    return stringData;
  }

  public void setStringData(String s)
  {
    stringData = s;
    setChanged();
  }

}

