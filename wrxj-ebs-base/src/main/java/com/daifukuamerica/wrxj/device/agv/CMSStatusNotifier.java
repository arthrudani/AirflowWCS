package com.daifukuamerica.wrxj.device.agv;

/**
 * Interface to notify MOS and controlling system of CMS related data.
 *
 * @author A.D.
 * @since  30-Jun-2009
 */
public interface CMSStatusNotifier
{
  public static final String CMS_IN_ERROR = "4";
  public static final String CMS_ERROR_RESET = "6";

 /**
  * Right now we only report if the CMS is availabler or not.  Currently, an
  * Unavailability/Error state constitutes:
  * <ul>
  *   <li>Any AGV station being offline.</li>
  *   <li>Any Vehicle fault occurring.</li>
  *   <li>Communication link to CMS being down.</li>
  * </ul>
  *
  * @param izAvailable {@code true} indicates CMS is available.
  */
  public void notifyCMSAvailable(boolean izAvailable);
 /**
  * Method notifies controlling system of Load being delivered to destination.
  * @param isDestStation
  * @param isLoadID
  * @throws AGVException if there is a database error.
  */
  public void notifyLoadArrival(String isDestStation, String isLoadID)
         throws AGVException;
 /**
  * Method notifies controlling system of Load being Picked up from source.
  * @param isLoadID the load id. being picked up
  * @throws AGVException if there is a database error.
  */
  public void notifyLoadPickupComplete(String isLoadID)
		throws AGVException;
}
