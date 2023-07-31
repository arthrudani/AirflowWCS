package com.daifukuamerica.wrxj.sequencer;

import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.RackLocationParser;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 * <B>Description:</B> Location sequencer by crane speed (ignores high/low speed
 * zones).
 * 
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 * 
 * @author mandrus
 * @version 1.0
 */
public class SpeedSequencer implements LocationSequencer
{
  /**
   * @see com.daifukuamerica.wrxj.sequencer.LocationSequencer#getDescription()
   */
  @Override
  public String getDescription()
  {
    return "Speed (single point of entry)";
  }

  /**
   * @see com.daifukuamerica.wrxj.sequencer.LocationSequencer#sequenceLocations(com.daifukuamerica.wrxj.dbadapter.data.LocationData)
   */
  @Override
  public String[] sequenceLocations(LocationData ipLocations)
      throws LocationSequencerException
  {
    try
    {
      RackLocationParser vpStartLoc = RackLocationParser.parse(
          ipLocations.getAddress(), true);
      RackLocationParser vpEndLoc = RackLocationParser.parse(
          ipLocations.getEndingAddress(), true);
      int vnLocCount = (vpEndLoc.getBankInteger() - vpStartLoc.getBankInteger() + 1) * 
                       (vpEndLoc.getBayInteger() - vpStartLoc.getBayInteger() + 1) * 
                       (vpEndLoc.getTierInteger() - vpStartLoc.getTierInteger() + 1);
      String[] vasLocations = new String[vnLocCount];

      // Get additional information for the sequencer
      Object vpOriginBay = JOptionPane.showInputDialog(null,
          "Enter the origin bay", "Question", JOptionPane.QUESTION_MESSAGE);
      int vnOriginBay = Integer.parseInt(vpOriginBay.toString());

      Object vpOriginTier = JOptionPane.showInputDialog(null,
          "Enter the origin tier", "Question", JOptionPane.QUESTION_MESSAGE);
      int vnOriginTier = Integer.parseInt(vpOriginTier.toString());

      Object vpBayWeight = JOptionPane.showInputDialog(null,
          "Enter the bay weight", "Question", JOptionPane.QUESTION_MESSAGE);
      int vnBayWeight = Integer.parseInt(vpBayWeight.toString());

      Object vpTierWeight = JOptionPane.showInputDialog(null,
          "Enter the tier weight", "Question", JOptionPane.QUESTION_MESSAGE);
      int vnTierWeight = Integer.parseInt(vpTierWeight.toString());

      // Figure out the weight of each bay/tier
      int vnBays = vpEndLoc.getBayInteger() - vpStartLoc.getBayInteger() + 1;
      int vnTiers = vpEndLoc.getTierInteger() - vpStartLoc.getTierInteger() + 1;
      LocWeightInfo vanWeights[] = new LocWeightInfo[vnBays * vnTiers];
      int i = 0;
      for (int x = vpStartLoc.getBayInteger(); x <= vpEndLoc.getBayInteger(); x++)
      {
        for (int y = vpStartLoc.getTierInteger(); y <=  vpEndLoc.getTierInteger(); y++)
        {
          vanWeights[i++] = new LocWeightInfo(getLocWeight(x, y, vnOriginBay,
              vnOriginTier, vnBayWeight, vnTierWeight), x, y);
        }
      }
      Arrays.sort(vanWeights);
      
      // Build the return list
      int vnStartBank = vpStartLoc.getBankInteger();
      int vnEndBank = vpEndLoc.getBankInteger();
      NumberFormat nf3 = NumberFormat.getInstance();
      nf3.setMinimumIntegerDigits(DBConstants.LNBANK);
      int vnNext = 0;
      for (i = 0; i < vanWeights.length; i++)
      {
        for (int vnBank = vnStartBank; vnBank <= vnEndBank; vnBank++)
        {
          vasLocations[vnNext++] = nf3.format(vnBank)
              + nf3.format(vanWeights[i].mnBay)
              + nf3.format(vanWeights[i].mnTier);
        }
      }
      return vasLocations;
    }
    catch (IOException ioe)
    {
      throw new LocationSequencerException(
          "Bank, Bay, or Tier must be non-zero! No Locations Modified ");
    }
  }

  /**
   * Get the weight of a location
   * 
   * @param inBay
   * @param inTier
   * @param inOriginBay
   * @param inOriginTier
   * @param inBayWeight
   * @param inTierWeight
   * @return
   */
  private int getLocWeight(int inBay, int inTier, int inOriginBay,
      int inOriginTier, int inBayWeight, int inTierWeight)
  {
    int vnXWeight = Math.abs(inOriginBay - inBay) * inBayWeight;
    int vnYWeight = Math.abs(inOriginTier - inTier) * inTierWeight;
    return Math.max(vnXWeight, vnYWeight);
  }
  
  /**
   * <B>Description:</B> Helper class for sorting locations
   */
  private class LocWeightInfo implements Comparable
  {
    int mnWeight;
    int mnBay;
    int mnTier;
    
    /**
     * 
     */
    public LocWeightInfo(int inWeight, int inBay, int inTier)
    {
      mnWeight = inWeight;
      mnBay = inBay;
      mnTier = inTier;
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Object o)
    {
      return mnWeight - ((LocWeightInfo)o).mnWeight;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      return "" + mnBay + "-" + mnTier + ": " + mnWeight;
    }
  }
}
