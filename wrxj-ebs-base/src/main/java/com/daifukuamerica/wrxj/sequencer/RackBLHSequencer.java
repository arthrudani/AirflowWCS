package com.daifukuamerica.wrxj.sequencer;

import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.RackLocationParser;
import java.io.IOException;

/**
 * Rack BLH Sequencer 
 *
 * <p><b>Details:</b> A RackBLHSequencer sequences rack locations bottom to 
 * top, left to right and front to back.
 * 
 */
public class RackBLHSequencer extends RackBHLSequencer
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
    return("Rack Bottom to Top - Left to Right - Front to Back");
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
    try
    {
      String[] vasOrder = super.sequenceLocations(ipLocations);
      RackLocationParser vpStartLoc = RackLocationParser.parse(ipLocations.getAddress(), true);
      RackLocationParser vpEndLoc = RackLocationParser.parse(ipLocations.getEndingAddress(), true);
      int vnLocCount = (vpEndLoc.getBayInteger() - vpStartLoc.getBayInteger() + 1) * 
                       (vpEndLoc.getBankInteger() - vpStartLoc.getBankInteger() + 1) * 
                       (vpEndLoc.getTierInteger() - vpStartLoc.getTierInteger() + 1);
      vasLocations = new String[vnLocCount];
      
      for (int vnOrderIndex = 0; vnOrderIndex < vnLocCount; vnOrderIndex++)
      {
        RackLocationParser vpCurLoc = RackLocationParser.parse(vasOrder[vnOrderIndex], true);
        int vnLocationIndex = ((vpCurLoc.getBankInteger() - vpStartLoc.getBankInteger()) * 1) +
                              ((vpCurLoc.getTierInteger() - vpStartLoc.getTierInteger()) * 
                               (vpEndLoc.getBankInteger() - vpStartLoc.getBankInteger() + 1)) +
                              ((vpCurLoc.getBayInteger() - vpStartLoc.getBayInteger()) * 
                               (vpEndLoc.getBankInteger() - vpStartLoc.getBankInteger() + 1) * 
                               (vpEndLoc.getTierInteger() -vpStartLoc.getTierInteger() + 1));
        vasLocations[vnLocationIndex] = vasOrder[vnOrderIndex];
      }
    }
    catch(IOException ioe)
    {
      vasLocations = null;
      throw new LocationSequencerException("Bank, Bay, or Tier must be non-zero! No Locations Modified ");
    }
    
    return vasLocations;
  }
}
