package com.daifukuamerica.wrxj.sequencer;

import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;

/**
 * Reset Sequencer 
 *
 * <p><b>Details:</b> A ResetSequencer is a place holder that returns an empty
 * string when the locations are sequenced. This tells the <code>LocationServer<\code>
 * to not increment the sequence of locations in the range but set them all to
 * the same value
 * 
 */
public class ResetSequencer implements LocationSequencer
{
  /**
   * Get Description.
   *
   * <p><b>Details:</b> This method gets a description of the resulting sequence.</p>
   *
   * @return a <code>String</code> description
   */
  public String getDescription()
  {
    return ("Reset");
  }

  /**
   * Sequence Locations.
   *
   * <p><b>Details:</b> This method gets an array of sequenced locations given
   * the specified set of locations. If the sequencer is not able to complete
   * sequencing then the resulting array should be set to <code>null</code> and 
   * osReason should be set to a <code>SequencerFailureReason</code> describing 
   * the reason for failure</p>
   *
   * @param ipLocations <code>LocationData</code> containing set of locations
   * @return a sequential <code>String</code> array of locations
   * @throws Location Sequencer Exception
   */
  public String[] sequenceLocations(LocationData ipLocations) throws LocationSequencerException
  {
    // we need to return a non null array - so make an empty array
    String[] vpLocations = {""};
    return vpLocations;
  }
}
