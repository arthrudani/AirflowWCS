package com.daifukuamerica.wrxj.util;

/**
 * An individual command line parameter.
 *
 * <p>Each individual command line parameter has a switch value and an
 * optional switch data value.  Any switch that also supplies switch data
 * will seperate the switch from the data with a ":".  For example;
 * <p>
 * <ul>
 * <li>java someClass -test
 * <li>java someClass -test:data
 * </ul>
 *
 * <p>This class simply stores the two possible values of any given switch.
 * The Commands class will parse the command line and store one of these
 * objects for each command line switch given.
 * </p>
 *
 * @see com.pts.util.Commands
 */

public class CommandLineParameter
{
  private String msSwitch;
  private String msSwitchData;

  /**
   * Instantiates a CommandLineParameter object with it's state set to the
   * given switch and switchData values.
   *
   * @param isSwitch       The command line parameters switch string.
   * @param isSwitchData   The command line parameters switch data string.
   */
  public CommandLineParameter(String isSwitch, String isSwitchData)
  {
    msSwitch = isSwitch;
    msSwitchData = isSwitchData;
  }

  /**
   * Returns the command line parameters switch string.
   * 
   * @return The command line parameters switch string.
   */
  public String getSwitch()
  {
    return msSwitch;
  }

  /**
   * Returns the command line parameters switch data.
   * 
   * @return The command line parameters switch data string.
   */
  public String getSwitchData()
  {
    return msSwitchData;
  }

  /**
   * Does an exact string match against the command line parameters switch
   * string.
   * 
   * @param isSwitch value to compare against this objects switch string.
   * @return True if the strings are an exact match, false otherwise.
   */
  public boolean switchEquals(String isSwitch)
  {
    return msSwitch.equals(isSwitch);
  }

  @Override
  public String toString()
  {
    return "switch: " + msSwitch + " switchData: " + msSwitchData;
  }

}