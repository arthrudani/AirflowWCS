package com.daifukuamerica.wrxj.util;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Parses command line String[] args into CommandLineParameter objects and 
 * stores them.
 * 
 * <p>This parser is fairly ridged in it's syntax checking.  The following is
 * the syntax that is expected:
 * <p>
 * <ul>
 * <li>java someClass [ [-|/<switch>[:<switchdata>]] ...]
 * </ul>
 * 
 * <p>Where: [] is optional, <> is you replace with your custom values, ... 
 * is repeated any number of times.  The following is an example of valid 
 * syntax: 
 * <p>
 * <ul>
 * <li>java someClass -verbose -file:c:\tmp\test.dat
 * </ul>
 * 
 * <p>The terminology used in this class and it's corrisponding 
 * CommandLineParameter class to describe a given command line parameter is a
 * "switch" and it's "switch data".  In the above example "verbose" and "file"
 * are switches and "c:\tmp\test.dat" is the "file" switches data.
 *
 * <p>This class is best used by extending it with a class that defines a given
 * set of known command line switches that are possible for a given program
 * and providing user help if unkown switches or invalid syntax is given. See
 * the JtCommands class for an example of this use.
 * </p>
 *
 * @see com.pts.util.test.JtCommands
 * @see com.pts.util.CommandLineParameter
 */
 
public class Commands
{
  private Vector<CommandLineParameter> commands;
  private String[] args;
  private String[] switches;

  /**
   * Instantiates a Commands object with default values.
   */
  public Commands()
  {
    this(null, null);
  }

  /**
   * Instantiates a Commands object with it's state set to the given parameters.
   * 
   * <p>The original command line String[] and the known possible valid switches
   * are stored.</p>
   * 
   * @param iasArgs The original command line argument string array.
   * @param iasSwitches possible valid command line switches.
   */
  public Commands(String[] iasArgs, String[] iasSwitches)
  {
    args = iasArgs;
    switches = iasSwitches;
    commands = new Vector<CommandLineParameter>();
  }

  /**
   * Parses the original command line arguments into CommandLineParameter 
   * objects and stores these objects internally.
   * 
   * <p>The original command line arguments are checked one at a time against
   * the possible valid switches array. As matches are found, the command 
   * line switches along with any associated switch data is stored in a 
   * CommandLineParameter object, which is then stored in a Vector in this 
   * object.
   *
   * <p>If a switch is given that is not found in the possible valid switches
   * array, or if the general syntax of the command line is invalid an
   * IllegalArgumentException will be thrown.
   * </p>
   *
   * @exception IllegalArgumentException If an unknown switch, or invalid 
   *                        syntax is given.
   */
  public void parseCommandLine()
  {
    for (int i=0; i < args.length; i++)
    {
      if (switches == null)
        throw new IllegalArgumentException("A potential Switch array must be given.");

      if (args[i].indexOf("-") != 0)
        if (args[i].indexOf("/") != 0)
          throw new IllegalArgumentException("\"-\" or \"/\" must preceed command line parameters");

      String remaining = args[i].substring(1); // skip the -/

      boolean found = false;
      for (int j=0; j < switches.length; j++)
      {
        if (remaining.indexOf(switches[j]) == 0)
        {
          // see if there is additional switch data
          CommandLineParameter clp = null;
          int index = remaining.indexOf(":");
          if (index != -1)
          {
            clp = new CommandLineParameter(
                    switches[j], remaining.substring(++index));
          }else
            clp = new CommandLineParameter(switches[j], null);
          commands.addElement(clp);
          found = true;
          break;
        }
      }
      if (!found)
        throw new IllegalArgumentException(args[i] + " is an unknown command line switch");
    }
  }

  /**
   * Return the CommandLineParameter associated with the given switch.
   * 
   * <p>The parsing process stores all the given command line parameters in
   * a Vector in this object.  This method searches that Vector for the given
   * switch value and returns the CommandLineParameter that contains this
   * switch, if it exists, otherwise this method returns a null.
   * </p>
   *
   * @param isSwitch  The switch which values are to be returned.
   *
   * @return          The switches values if it is found, otherwise
   *                  null is returned.
   */
  public CommandLineParameter getCommand(String isSwitch)
  {
    if (commands == null)
      return null;

    Enumeration<CommandLineParameter> vpEnum = commands.elements();
    while (vpEnum.hasMoreElements())
    {
      CommandLineParameter clp = vpEnum.nextElement();
      if (clp.switchEquals(isSwitch))
        return clp;
    }
    return null;
  }

  /**
   * Returns true if this object contains a CommandLineParameter for the 
   * given switch, otherwise returns false.
   * 
   * @param isSwitch       The switch to check for.
   * 
   * @return            True if the switch is present, false otherwise.
   */
  public boolean switchPresent(String isSwitch)
  {
    if (getCommand(isSwitch) != null)
      return true;
    return false;
  }

  /**
   * Returns true if this object contains a CommandLineParameter for the 
   * given switch and that switch has swith data, otherwise returns false.
   * 
   * @param isSwitch       The switch to check for.
   * 
   * @return            True if the switch is present, false otherwise.
   */
  public boolean switchDataPresent(String isSwitch)
  {
    if (getSwitchData(isSwitch) == null)
      return false;
    return true;
  }

  /**
   * Returns the switch data associated with the requested switch. If the 
   * switch is not present, null is returned.
   * 
   * @param isSwitch       The switch to check for.
   * 
   * @return            The data if present, null otherwise.
   */
  public String getSwitchData(String isSwitch)
  {
    CommandLineParameter clp = getCommand(isSwitch);
    if (clp == null)
      return null;
    return clp.getSwitchData();
  }

  /**
   * Returns the orignal command line arguments array.
   * @return            The original String[] args.
   */
  public String[] getOriginalArgs()
  {
    return args;
  }

  /**
   * Adds a command to this set of commands.
   * 
   * <p>Simply adds another CommandLineParameter object to the commands 
   * Vector.
   * </p>
   *
   * @param isSwitch       The switch to be added.
   * @param isSwitchData   The switch data to be added.
   */
  public void addCommand(String isSwitch, String isSwitchData)   
  {
    commands.addElement(new CommandLineParameter(isSwitch, isSwitchData));
  }

  /**
   * Sets this objects switches state.
   * 
   * <p>This method is needed because a sub-class must call super in it's 
   * constructor before it can do anything else (including accessing one of
   * it's own instance variables that requires initialization in the super
   * call).  This method should be called by the subclasses right after
   * call the super constructor.
   * </p>
   *
   * @param iasSwitches      The possible valid switches.
   */
  protected void setSwitches(String[] iasSwitches)
  {
    switches = iasSwitches;
  }
}  
