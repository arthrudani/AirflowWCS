package com.daifukuamerica.wrxj.sequencer;

import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import java.io.IOException;

/**
 * Rack BHL Sequencer 
 *
 * <p><b>Details:</b> A RackBHLSequencer sequences rack locations bottom to 
 * top, front to back and left to right. This is the default sequence since 
 * it is the same sequence that would result from an ascending ASCII sort on 
 * the locations.
 * 
 */
public class RackBHLSequencer implements LocationSequencer
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
    return("Rack (Default) Bottom to Top - Front to Back - Left to Right");
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
  public String[] sequenceLocations(LocationData ipLocations)
      throws LocationSequencerException
  {
    String[] vasLocations;
    Location vpLocation = Factory.create(Location.class);
                                       // Get all sequential addresses in the
                                       // range of banks, bays, and tiers.
    try
    {
      vasLocations = vpLocation.getRackAddressRange(ipLocations.getAddress(),
          ipLocations.getEndingAddress());
    }
    catch(ArrayIndexOutOfBoundsException aiobe)
    {
      vasLocations = null;
      throw new LocationSequencerException("Invalid ending Bank. No Locations Modified ");
    }
    catch(IOException ioe)
    {
      vasLocations = null;
      throw new LocationSequencerException("Bank, Bay, or Tier must be non-zero! No Locations Modified ");
    }
    catch(NumberFormatException nfe)
    {
      vasLocations = null;
      throw new LocationSequencerException("Number Format Error. No Locations Modified ");
    }
    
    return vasLocations;
  }
}
