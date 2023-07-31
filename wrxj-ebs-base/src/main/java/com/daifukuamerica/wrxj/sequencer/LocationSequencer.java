package com.daifukuamerica.wrxj.sequencer;

import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;

/**
 * Interface to location sequencer.
 *
 * <p><b>Details:</b> A <code>LocationSequencer</code> encapsulates
 * logic to apply an algorithm to a set of locations and return the
 * locations in a sequential order.</p>
 *
 * <p>To use a <code>LocationSequencer</code>:</p>
 *
 * <ol>
 *   <li>Call <code>getDescription</code> to obtain a description of the
 *     algorithm. This can be used to select a particular sequencer from a list.</li>
 *   <li>Call <code>updateLocationOrder</code> and pass in location data 
 *     to receive an ordered string array of location ids.</li>
 *   <li>Optionally call <code>getImage</code> to obtain a graphical 
 *     representation of the algorithm.
 * </ol>
 */
public interface LocationSequencer
{
  /**
   * Get Description.
   *
   * <p><b>Details:</b> This method gets a description of the resulting sequence.</p>
   *
   * @return a <code>String</code> description
   */
  String getDescription();
  
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
  String[] sequenceLocations(LocationData ipLocations) throws LocationSequencerException;
}



