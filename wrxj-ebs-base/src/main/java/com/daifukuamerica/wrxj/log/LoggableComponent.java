package com.daifukuamerica.wrxj.log;

/**
 * Marks loggable UI component.
 * 
 * <p><b>Details:</b> This interface marks a Swing UI component as being one 
 * that should have its own logger.  When events originate from a container 
 * implementing this class, {@link Logger} will provide a separate logger
 * instance for the component.</p>
 * 
 * <p>In WRx-J, the base {@link javax.swing.JInternalFrame} from which all the
 * screens inherit should implement this class.</p>
 * 
 * @author Sharky
 */
public interface LoggableComponent
{
  /**
   * Returns log name.
   * 
   * <p><b>Details:</b> This method returns the name that should be shown for
   * the component's log in human-<wbr>readable interfaces.</p>
   * 
   * <p>Frames implementing this class might simply return the text shown in
   * their title bars, but that is entirely up to the implementor.</p>
   * 
   * @return the log name
   */
  String getLogName();
}
