package com.daifukuamerica.wrxj.sequencer;

/**
 * General Exception Class for Location Sequencing.
 *
 * <p><b>Details:</b> <code>LocationSequencerException</code> is a generic exception
 * class that is currently only serving as a place marker.</p>
 */
public class LocationSequencerException extends Exception
{
  private static final long serialVersionUID = 0L;
  
  public LocationSequencerException()
  {
  }

  public LocationSequencerException(String isDetail)
  {
    super(isDetail);
  }

  public LocationSequencerException(Throwable ieException)
  {
    super(ieException);
  }
}
