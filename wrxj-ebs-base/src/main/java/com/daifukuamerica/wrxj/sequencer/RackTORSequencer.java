package com.daifukuamerica.wrxj.sequencer;

import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;

/**
 * Rack TOR Sequencer
 *
 * <p><b>Details:</b> A RackTORSequencer sequences rack locations top to 
 * bottom, back to front and right to left. This is the reverse of the 
 * default sequence since it is the same sequence that would result from 
 * a descending ASCII sort on the locations.
 * 
 */
public class RackTORSequencer extends RackBHLSequencer
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
    return("Rack Top to Bottom - Back to Front - Right to Left");
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
    String[] vasLocations;
    String[] vasReverseOrder = super.sequenceLocations(ipLocations);
    vasLocations = new String[vasReverseOrder.length];
    for(int lcidx = 0; lcidx < vasReverseOrder.length; lcidx++)
    {                                  
      vasLocations[lcidx] = vasReverseOrder[vasReverseOrder.length - 1 - lcidx].toString();
    }
    return vasLocations;
  }
}
