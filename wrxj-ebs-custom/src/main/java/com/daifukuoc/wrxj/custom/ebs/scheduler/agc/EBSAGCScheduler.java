package com.daifukuoc.wrxj.custom.ebs.scheduler.agc;

import java.util.List;
import java.util.Random;

import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSDeviceServer;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.scheduler.agc.AGCScheduler;

public class EBSAGCScheduler extends AGCScheduler
{

	  protected EBSDeviceServer mpEBSDeviceServer;

	  int mnRandomInterval = 1000;
	  Random mpRandom = new Random();
	 
	/**
	   * Public constructor for Factory
	   *
	   * @param isName
	   */
	  public EBSAGCScheduler(String isName)
	  {
	    super(isName);
	  }

	  /**
	   * Method to Initialize everything need to run the AGCSCHEDULER.
	   */
	  @Override
	  public void startup()
	  {
	    super.startup();
	    logger.logDebug(getClass().getSimpleName() + ".startup() - Start "
	        + getSchedulerName());

	    mpEBSDeviceServer = Factory.create(EBSDeviceServer.class);

	    logger.logDebug(getClass().getSimpleName() + ".startup() - End");
	  }
	/**
	   * Look for retrieve pending loads for a station and send messages to get
	   * those loads to retrieve.  Also send a message to the allocator to check to
	   * see if we need more loads staged.
	   *
	   * @param ipSD
	   * @throws DBException
	   */
	  protected void retrieveAndStageForStation(StationData ipSD)
	    throws DBException
	  {
	    List<LoadEventDataFormat> vpList = getLoadToRetrieve(ipSD);
	    for (LoadEventDataFormat vpLEDF : vpList)
	    {
	      String vsLocation = mpLoadServer.getLoadLocation(vpLEDF.getLoadID());
	      String[] vasLoc = Location.parseLocation(vsLocation);
	      String vsDevice = mpLocServer.getLocationDeviceId(vasLoc[0], vasLoc[1]);
	      
	      // MCM, EBS
	      // If Device is Inoperable, use the Secondary Crane
	      if( mpEBSDeviceServer.isDeviceInoperable( vsDevice ) )
	      {
	    	  String sSecondaryDeviceID = mpEBSDeviceServer.getSecondaryDeviceID( vsDevice );
	    	  if( sSecondaryDeviceID != null && !sSecondaryDeviceID.isEmpty() )
	    	  {
	    		  vsDevice = sSecondaryDeviceID;
	    	  }
	    	  else
	    	  {
	    		  logger.logError(getClass().getSimpleName() + " Incorrect Secondary DeviceID configuration for " + vsDevice);
	    		  return;
	    	  }
	      }
	 	     
	      try
	      {  
	    		  int vnInterval = mpRandom.nextInt(mnRandomInterval);
	    	      Thread.sleep(vnInterval);
	      }
	      catch (InterruptedException e) {}
	      
	      String loadRetrieveCommand = vpLEDF.createStringToSend();
	      if( loadRetrieveCommand.length() > 0)
	      {
	        String vsCollaborator = getCollaboratorFromDevice(vsDevice);
	        publishLoadEventMove(loadRetrieveCommand, vsCollaborator);
	      }
	    }

	    // just took a staged load do we need another
	    checkIfStationNeedsMoreStagedLoads(ipSD.getStationName());
	  }

	  
	  /**
	   * See if there are any loads to schedule a store from this station
	   *
	   * @param isStationName the station to check
	   */
	  protected void checkIfStationHasLoadToStore(String isStationName)
	  {
	    StationData vpSD = mpStationServer.getStation(isStationName);
	    if (vpSD == null)
	    {
	      logger.logException(new DBException("Station \"" + isStationName
	          + "\" not found!"));
	    }
	    else
	    {
		    String vsNewCommand = getLoadToStore(vpSD);
		    
	    	String vsDevice = vpSD.getDeviceID();
	    	  // MCM, EBS
		      // If Device is Inoperable, use the Secondary Crane
		      if( mpEBSDeviceServer.isDeviceInoperable( vpSD.getDeviceID() ) )
		      {
		    	  String sSecondaryDeviceID;
		    	  try 
		    	  {
		    		  sSecondaryDeviceID = mpEBSDeviceServer.getSecondaryDeviceID( vpSD.getDeviceID() );
		    	  
		    		  if( sSecondaryDeviceID != null && !sSecondaryDeviceID.isEmpty() )
		    		  {
		    			  vsDevice = sSecondaryDeviceID;
		    		  }
		    		  else
		    		  {
		    			  logger.logError(getClass().getSimpleName() + " Incorrect Secondary DeviceID configuration for " + vsDevice);
		    			  return;
		    		  }
		    	  } 
		    	  catch (DBException e) 
		    	  {
					// TODO Auto-generated catch block
					e.printStackTrace();
		    	  }
		      }
	    	
	      String vsCollaborator = getCollaboratorFromDevice(vsDevice);
	      if (vsNewCommand.length() > 0)
	      {
	        transmitLoadEvent(vsNewCommand, vsCollaborator);
	      }
	    }
	  }
	  
	  /**
	   * load has just arrived at the output station Update the load and publish a
	   * message to the pick screen of load arrival.
	   *
	   * @param ipLEDF decoded LoadEventDataFormat message
	   */
	   public void processFinalArrival(LoadEventDataFormat ipLEDF)
	   {
	     try
	     {
	    	 try
	    	 {  
	    		  int vnInterval = mpRandom.nextInt(mnRandomInterval);
	    	      Thread.sleep(vnInterval);
	    	 }
	    	 catch (InterruptedException e) {}
	    	 
	       String stationName = ipLEDF.getSourceStation();
	       String newCommand = mpSchedServer.updateLoadForFinalArrival(ipLEDF);
	       if( newCommand.length() > 0)
	       {
	         publishLoadEvent(newCommand,0);
	       }
	       checkIfStationHasLoadToRetrieve(stationName);

	       int vnStationType = mpStationServer.getStationType(stationName);
	       if (vnStationType == DBConstants.PDSTAND ||
	           vnStationType == DBConstants.REVERSIBLE)
	       {
	         checkIfStationHasLoadToStore(stationName);
	       }
	     }
	     catch (DBException dbe)
	     {
	       // TODO: Better exception handling
	       logger.logException(dbe);
	     }
	   }

}
