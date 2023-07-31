package com.daifukuamerica.wrxj.sequencer;

import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import java.util.Random;

/**
 * Rack Random Sequencer 
 *
 * <p><b>Details:</b> A RackRandomSequencer randomizes the sequence of locations
 * within a rack.
 * 
 */
public class RackRandomSequencer extends RackBHLSequencer
{
  /**
   * Get Description.
   *
   * <p><b>Details:</b> This method gets a description of the resulting sequence.</p>
   *
   * @return a <code>String</code> description
   */
  @Override
  public String getDescription()
  {
    return("Rack Random");
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
  @Override
  public String[] sequenceLocations(LocationData ipLocations) throws LocationSequencerException
  {
    String[] vasLocations = super.sequenceLocations(ipLocations);
    Random vpRandom = new Random();
    for (int vnIndex = vasLocations.length - 1; vnIndex >= 0; vnIndex--)
    {
      int vnRandom = vpRandom.nextInt(vasLocations.length);
      String vsTempLocation = vasLocations[vnIndex];
      vasLocations[vnIndex] = vasLocations[vnRandom];
      vasLocations[vnRandom] = vsTempLocation;
    }
    return vasLocations;
  }
}
