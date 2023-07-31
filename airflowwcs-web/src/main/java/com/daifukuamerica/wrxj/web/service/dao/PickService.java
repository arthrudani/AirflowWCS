package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.wrx.PickModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.PickMoveData;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service layer for pick operations. Add/modify/update/delete operations
 * are performed here with parameters passed from controller layer. 
 * 
 * Author: dystout
 * Created : May 16, 2017
 *
 */
public class PickService
{
	
	/**
	* Log4j logger: PickService
	*/
	private static final Logger logger = LoggerFactory.getLogger(PickService.class);

	protected static MoveData MOVE_DATA; 

	protected AjaxResponse ajaxResponse; 
	
	
	

	public PickModel getFirstPick(String stationId)
	{
		
		return null;
	}

	public List<Map> getPicks(LoadData loadData)
	{
		StandardLoadServer sLoadServer = Factory.create(StandardLoadServer.class);
//		mpLoadData = sLoadServer.getOldestLoad(warehouse, address)
		return null;
	}

	/**
	 * Complete a pick with the given move record, performed by the given 
	 * user. 
	 * 
	 * @param pickMoveData - move data to use for the pick 
	 * @param user - user who is completing the pick 
	 * 
	 * @return
	 */
	public AjaxResponse completePick(PickMoveData pickMoveData, User user)
	{
        ajaxResponse = new AjaxResponse();
        MOVE_DATA = pickMoveData; 
	       switch (pickMoveData.getMoveType())
	       {
	         case DBConstants.LOADMOVE:
	           try
	           {
				performLoadPick(user, pickMoveData);
	           } catch (DBException e)
	           {
				logger.error("Error performing Load Move: {}", e.getMessage());
				ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "DBException on LOAD move.");
				e.printStackTrace();
	           }
	           break;
	           
	         case DBConstants.ITEMMOVE:
	           try
	           {
				performItemPick(user, pickMoveData);
	           } catch (DBException e)
	           {
	        	   logger.error("Error performing Item Move: {}", e.getMessage());
	        	   ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "DBException on ITEM move.");
				e.printStackTrace();
	           }
	           break;
	           
	         case DBConstants.CYCLECOUNTMOVE:
	           performCycleCountPick();
	           break;
	           
	         default:
	           ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Not a valid move Type.");
	       }
	    
	       if(ajaxResponse.getResponseCode()==AjaxResponseCodes.DEFAULT)
	    	   ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully picked: " + pickMoveData.getConfirmPickQuantity() 
	    	   							+ " of Item#: " + pickMoveData.getItem());
	    return ajaxResponse; 
	}
	
	 private void performCycleCountPick()
	{
		// TODO Auto-generated method stub
		
	}


	  protected AjaxResponse performItemPick(User user, PickMoveData moveData) throws DBException
	  {
	    ajaxResponse = new AjaxResponse(); 
	    
	    
	    String vsItem = moveData.getItem();
	    String vsLot = moveData.getPickLot();
	    String vsPositionID = moveData.getPositionID();
	    StandardPickServer mpPickServer = Factory.create(StandardPickServer.class);
	    try
	    {
		    mpPickServer.completeItemPick(user.getUserId(), moveData, "", 
					true, moveData.getConfirmPickQuantity(), "");
	    }
	    catch(Exception e)
	    {
	    	logger.error("Error completing Item Pick: {} ERROR MESSAGE: {}", moveData.toString(), e.getMessage());
	    	ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error completing Item Pick: " + e.getMessage());
	    }



	    return ajaxResponse;
	  }

	  
	  /**
	   * I need a JavaDoc
	   * @throws DBException
	   */
	  protected AjaxResponse performLoadPick(User user, PickMoveData moveData) throws DBException
	  {
		StandardPickServer pickServer = Factory.create(StandardPickServer.class);
		StandardStationServer stationServer = Factory.create(StandardStationServer.class);
		StationData stationData = stationServer.getStation(moveData.getAddress()); 
		
		// TODO - implement load pick

		return null; 
	  }

	  
}
