package com.daifukuamerica.wrxj.device.agv.communication;

/**
 * Interface that watches for tcp data.
 * @author A.D.
 * @since  26-May-2009
 */
public interface TCPEventListener
{
  public void receivedData(String isData);
}
