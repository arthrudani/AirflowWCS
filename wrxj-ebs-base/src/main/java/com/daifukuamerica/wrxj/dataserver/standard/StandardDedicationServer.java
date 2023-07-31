package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocation;
import com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocationData;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCode;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.log.LogConsts;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *    This handles transactions that deal with item dedications
 *
 * @author       Michael Andrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2005 by Daifuku America Corporation
 */
public class StandardDedicationServer extends StandardServer
{
  private DedicatedLocation mpDL;
  
  private StandardInventoryServer        mpInvServer;
  private StandardLocationServer         mpLocServer;
  private StandardLoadServer             mpLoadServer;
  private StandardMaintenanceOrderServer mpMOrdServer;
  
  /**
   * 
   */
  public StandardDedicationServer()
  {
    this(null);
  }
  
  

  /**
   * @param keyName
   */
  public StandardDedicationServer(String keyName)
  {
    super(keyName);
    logDebug("Creating StandardDedicationServer");
    
    mpDL = Factory.create(DedicatedLocation.class);
    
    mpInvServer  = Factory.create(StandardInventoryServer.class);
    mpLocServer  = Factory.create(StandardLocationServer.class);
    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpMOrdServer = Factory.create(StandardMaintenanceOrderServer.class);
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardDedicationServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
	  
	  mpDL = Factory.create(DedicatedLocation.class);
	    
	    mpInvServer  = Factory.create(StandardInventoryServer.class);
	    mpLocServer  = Factory.create(StandardLocationServer.class);
	    mpLoadServer = Factory.create(StandardLoadServer.class);
	    mpMOrdServer = Factory.create(StandardMaintenanceOrderServer.class);
  }


  /*========================================================================*/
  /*  Private Helper Functions                                              */
  /*========================================================================*/
  
  /**
   * Validates DedicatedLocationData before adding/modifying it.
   * 
   * @param ipDLData - The data to validate 
   * @throws DBException
   */
  private void validateDLData(String isItem, String isWarehouse, String isAddress) throws DBException
  {
    int viLocType;

    ItemMasterData vpItemMasterData;
    WarehouseData vpWarehouseData;

    logDebug("Pre-validating " + describeDedication(isItem, isWarehouse, isAddress));
    
    /*
     * The Item must exist
     */
    vpItemMasterData = mpInvServer.getItemMasterData(isItem);
    if (vpItemMasterData == null)
    {
      DBException e = new DBException("Item \'" + isItem  + "\' does not exist");
      throw e;
    }
    
    /*
     * The Warehouse must exist
     * (There has got to be an easier way to do this...)
     */
    vpWarehouseData = mpLocServer.getRegularWarehouseElement(isWarehouse);
    if (vpWarehouseData == null)
    {
      DBException e = new DBException("Warehouse \'" + isWarehouse  + "\' does not exist");
      throw e;
    }
    
    /*
     * If there is an Address, it must exist, and be dedicate-able.  
     */
    if (isAddress.length() > 0)
    {
      viLocType = mpLocServer.getLocationTypeValue(isWarehouse, isAddress);
      if (viLocType == -1)
      {
        DBException e = new DBException("Location \'" + isWarehouse + "-" + isAddress +
            "\' does not exist");
        throw e;
      }
      else if (viLocType != DBConstants.LCDEDICATED)
      {
        DBException e = new DBException("Location \'" + isWarehouse + "-" + isAddress +
            "\' cannot be dedicated");
        throw e;
      }
    }
    else
    {
      DBException e = new DBException("Address Cannot be blank");
      throw e;
    }
    
    logDebug("Pre-validating " + describeDedication(isItem, isWarehouse, isAddress) + "...PASSED");
  }
  
  
  /*========================================================================*/
  /**
   * Check an obsolete dedication to see if we can delete it
   *  
   * @param ipDLData
   * @throws DBException
   */
  private void checkObsoleteDedication(DedicatedLocationData ipDLData) throws DBException
  {
    double vdCurrentQty = ipDLData.getCurrentQuantity();
    double vdEnrouteQty = ipDLData.getEnrouteQuantity();
    
//    TransactionToken tt = null;
    
    if ((vdCurrentQty <= 0.0) && (vdEnrouteQty <= 0.0))
    {
      mpDL.deleteDedication(ipDLData);
    }
    // TODO: implement generateUnReplenishmentOrder()
//    else
//    {
//      if (mpDL.isUnreplenishing(ipDLData))
//      {
//        try
//        {
//          tt = startTransaction();
//          generateUnReplenishmentOrder(ipDLData);
//          commitTransaction(tt);
//        }
//        finally
//        {
//          endTransaction(tt);
//        }
//      }
//    }
  }
  
  /*========================================================================*/
  /**
   * Create the description for a replenishment order
   * 
   * @param isItem
   * @param isWarehouse
   * @param isAddress
   * @return
   */
  private String describeReplen(String isItem, String isWarehouse, String isAddress)
  {
    return describeDedication("Replenish: ", isItem, isWarehouse, isAddress);
  }

  /*========================================================================*/
  /**
   * Generate a replenishment order for a dedication
   * 
   * @param ipDLData
   */
  private void generateReplenishmentOrder(DedicatedLocationData ipDLData) 
    throws DBException
  {
    String vsItem = ipDLData.getItem();
    String vsWarehouse = ipDLData.getWarehouse();
    String vsAddress = ipDLData.getAddress();
    
    OrderHeaderData vpOHData = Factory.create(OrderHeaderData.class);
    OrderLineData vpOLData   = Factory.create(OrderLineData.class);

    /*
     * TODO: Add configurations for replenishment order priority and RLC.
     * Currently the values are hard-coded below.
     */
    vpOHData.setPriority(7);
    vpOHData.setReleaseToCode("***");
    vpOHData.setOrderType(DBConstants.REPLENISHMENT);
    vpOHData.setOrderStatus(DBConstants.ALLOCATENOW);
    vpOHData.setDescription(describeReplen(vsItem, vsWarehouse, vsAddress));
    vpOHData.setDestWarehouse(vsWarehouse);
    vpOHData.setDestAddress(vsAddress);
    
    vpOLData.setItem(ipDLData.getItem());
    vpOLData.setOrderQuantity(ipDLData.getMaximumQuantity() - ipDLData.getCurrentQuantity());

    mpMOrdServer.buildOrder(vpOHData, new OrderLineData[] {vpOLData});
  }
  
  /*========================================================================*/
//  /**
//   * Generate an unreplenishment order for a dedication.
//   * <BR>An unreplenishment is basically the same as a normal picking order,
//   * but it has a receiving location as its destination.
//   * 
//   * @param ipDLData - The dedication to deplete
//   */
//  private void generateUnReplenishmentOrder(DedicatedLocationData ipDLData)
//    throws DBException
//  {
//    // TODO: Implement generateUnReplenishmentOrder()
//    
//    throw new DBException("Unreplenishments have not been implemented");
//  }

  /*========================================================================*/
  /**
   * Check to see if there is a replenishment for a given dedication 
   * 
   * @param ipDLData Dedication to check
   * @return true if there is a replenishment, false otherwise
   */
  private boolean replenishmentExists(DedicatedLocationData ipDLData)
  {
    OrderHeader vpOrder = Factory.create(OrderHeader.class);
    OrderHeaderData vpOrderData = Factory.create(OrderHeaderData.class);
    
    try
    {
      vpOrderData.setKey(OrderHeaderData.ORDERTYPE_NAME, Integer.valueOf(DBConstants.REPLENISHMENT));
      vpOrderData.setKey(OrderHeaderData.DESCRIPTION_NAME, describeReplen(ipDLData.getItem(),
                         ipDLData.getWarehouse(), ipDLData.getAddress()));
      vpOrderData.setKey(OrderHeaderData.DESTWAREHOUSE_NAME, ipDLData.getWarehouse());
      vpOrderData.setKey(OrderHeaderData.DESTADDRESS_NAME, ipDLData.getAddress());
      
      vpOrderData = vpOrder.getElement(vpOrderData, DBConstants.NOWRITELOCK);
      if (vpOrderData != null)
      {
        return true;
      }
    }
    catch (DBException vpDBE)
    {
      return false;
    }
    
    return false;
  }
  
  /*========================================================================*/
  /*  The following methods are for Item Dedications                        */
  /*========================================================================*/
 /**
  *  Checks if an item is a dedicated item.
  * @param vsItem the item in question.
  * @return <code>true</code> if the item is dedicated, <code>false</code>
  *         otherwise.
  * @throws DBRuntimeException 
  */
  public boolean isDedicatedItem(String vsItem) throws DBRuntimeException
  {
    DedicatedLocation vpDedLocn = Factory.create(DedicatedLocation.class);
    DedicatedLocationData vpDedLocnData = Factory.create(DedicatedLocationData.class);
    vpDedLocnData.setKey(DedicatedLocationData.ITEM_NAME, vsItem);
    return(vpDedLocn.exists(vpDedLocnData));
  }
  
  /**
   *  See if a location is currently dedicated to an item
   *  
   *  @param isWarehouse The location's warehouse.
   *  @param isAddress The location's address.
   *  @return true is there is a dedication for this location, false otherwise
   *  @throws DBException
   */
  public boolean isLocationDedicated(String isWarehouse, String isAddress) throws DBException
  {
    DedicatedLocation vpDedLocn = Factory.create(DedicatedLocation.class);
    DedicatedLocationData vpDedLocnData = Factory.create(DedicatedLocationData.class);
    vpDedLocnData.setKey(DedicatedLocationData.WAREHOUSE_NAME, isWarehouse);
    vpDedLocnData.setKey(DedicatedLocationData.ADDRESS_NAME, isAddress);
    return(vpDedLocn.exists(vpDedLocnData));
  }

  /**
   *  See if a location is currently dedicated to an item
   *  
   *  @param isWarehouse The location's warehouse.
   *  @param isAddress The location's address.
   *  @param isItem The Item ID.
   *  @return true is there is a dedication for this location to this item, false otherwise
   *  @throws DBException
   */
  public boolean isLocationDedicatedtoItem(String isWarehouse, String isAddress, String isItem)
      throws DBException
  {
    DedicatedLocation vpDedLocn = Factory.create(DedicatedLocation.class);
    DedicatedLocationData vpDedLocnData = Factory.create(DedicatedLocationData.class);
    vpDedLocnData.setKey(DedicatedLocationData.WAREHOUSE_NAME, isWarehouse);
    vpDedLocnData.setKey(DedicatedLocationData.ADDRESS_NAME, isAddress);
    vpDedLocnData.setKey(DedicatedLocationData.ITEM_NAME, isItem);
    return(vpDedLocn.exists(vpDedLocnData));
  }

  /**
   * Add an Item Dedication
   * 
   * @param ipDLData - The dedication to add
   * @return Informational message
   */
  public String addDedication(DedicatedLocationData ipDLData) throws DBException
  {
    DedicatedLocationData vpDLData;
    String vsItem      = ipDLData.getItem();
    String vsWarehouse = ipDLData.getWarehouse();
    String vsAddress   = ipDLData.getAddress();
    String vsReturnString = "";
    String vsDedication = describeDedication(vsItem, vsWarehouse, vsAddress);
    TransactionToken tt = null;
    
    /*
     * Make sure the item, etc are valid
     */
    validateDLData(vsItem, vsWarehouse, vsAddress);
    
    /*
     * Make sure that the dedication does not already exist
     */
    vpDLData = mpDL.getDedicationData(vsItem, vsWarehouse, vsAddress, false);
    if (vpDLData != null)
    {
      throw new DBException(vsDedication + " already exists.");
    }
    
    /*
     * Due to current database constraints, an item may only be dedicated
     * to a warehouse once.
     */
    vpDLData = Factory.create(DedicatedLocationData.class);
    vpDLData.setKey(DedicatedLocationData.ITEM_NAME, vsItem);
    vpDLData.setKey(DedicatedLocationData.WAREHOUSE_NAME, vsWarehouse);
    vpDLData = mpDL.getElement(vpDLData, DBConstants.NOWRITELOCK);
    if (vpDLData != null)
    {
      throw new DBException("Cannot add " + vsDedication + 
          " already has a dedication in " + vsWarehouse + ".");
    }
    
    /*
     * Add the new dedication
     */
    try
    {
      tt = startTransaction();
      logDebug("Adding " + vsDedication);

      mpDL.addDedication(ipDLData);
      replenishDedication(ipDLData);
      
      vsReturnString = "Added " + vsDedication;
      logOperation(LogConsts.OPR_DSVR, vsReturnString);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
    
    return vsReturnString;
  }
  
  /*========================================================================*/
  /**
   * Delete an Item Dedication
   * 
   * @param isItem Key info
   * @param isWarehouse Key info
   * @param isAddress Key info
   * @param ibUnreplenish true to generate unreplenishments, false otherwise
   */
  public void deleteDedication(DedicatedLocationData ipDLData, boolean ibUnreplenish)
    throws DBException
  {
    String vsItem = ipDLData.getItem();
    String vsWarehouse = ipDLData.getWarehouse();
    String vsAddress = ipDLData.getAddress();
    TransactionToken tt = null;
    
    /*
     * Make sure we have the correct current & enroute quantities 
     */
    DedicatedLocationData vpDLData = mpDL.getDedicationData(vsItem, vsWarehouse,
        vsAddress, true);
    if (vpDLData == null)
    {
      throw new DBException(describeDedication(ipDLData) + " NOT FOUND");
    }
    
    double vdCurrentQty = vpDLData.getCurrentQuantity();
    double vdEnrouteQty = vpDLData.getEnrouteQuantity();
      
    if ((vdCurrentQty > 0.0) || (vdEnrouteQty > 0.0)) 
    {
      try
      {
        tt = startTransaction();
        logDebug("Deactivating " + describeDedication(vsItem, vsWarehouse, vsAddress));

        // TODO: implement generateUnReplenishmentOrder()
//        if (ibUnreplenish)
//        {
//          mpDL.unreplenishDedication(ipDLData);
//        }
//        else
        {
          mpDL.deactivateDedication(ipDLData);
        }
        checkObsoleteDedication(ipDLData);
        
        logOperation(LogConsts.OPR_DSVR, "Deactivating " + 
            describeDedication(vsItem, vsWarehouse, vsAddress));

        commitTransaction(tt);
      }
      finally
      {
        endTransaction(tt);
      }
    }
    else
    {
      try
      {
        tt = startTransaction();
        logDebug("Deleting " + describeDedication(vsItem, vsWarehouse, vsAddress));

        mpDL.deleteDedication(ipDLData);
      
        logOperation(LogConsts.OPR_DSVR, "Deleted " + 
            describeDedication(vsItem, vsWarehouse, vsAddress));

        commitTransaction(tt);
      }
      finally
      {
        endTransaction(tt);
      }
    }
  }
  
  /*========================================================================*/
  /**
   * Format the string used to describe a route on a GUI
   */
  public String describeDedication(DedicatedLocationData ipDLData)
  {
    String vsItem = ipDLData.getItem();
    String vsWarehouse = ipDLData.getWarehouse();
    String vsAddress = ipDLData.getAddress();

    return describeDedication(vsItem, vsWarehouse, vsAddress);
  }

  /**
   * Format the string used to describe a route on a GUI
   */
  public String describeDedication(String isItem, String isWarehouse, String isAddress)
  {
    return describeDedication("Dedication: ", isItem, isWarehouse, isAddress);
  }
  
  /**
   * Format the string used to describe a route on a GUI
   */
  private String describeDedication(String isLeader, String isItem, 
      String isWarehouse, String isAddress)
  {
    String vsLocation;
    
    if (isAddress.trim().length() == 0)
    {
      vsLocation = isWarehouse;
    }
    else
    {
      vsLocation = isWarehouse + '-' + isAddress;
    }
    return isLeader + isItem + "@" + vsLocation;
  }
  
  /*========================================================================*/
  /**
   *  Method retrieves Dedicated Location record based on warehouse,
   *  address, item string.
   *  No locks are done.
   *
   * @param isWarehouse <code>String</code> containing Warehouse of location.
   * @param isAddress <code>String</code>  containing Address of location.
   * @param isItem <code>String</code>  item for location.
   * @param ibGetCurrentQty <code>boolean</code> true to read current quantity
   * @return <code>LocationData</code> containing location record.
   * @throws DBException ?
   */
  public DedicatedLocationData getDedicatedLocationRecord(String isItem, 
       String isWarehouse, String isAddress, boolean ibGetCurrentQty)
     throws DBException
  {
    return mpDL.getDedicationData(isItem, isWarehouse, isAddress, ibGetCurrentQty);
  }

  /*========================================================================*/
  /**
   * Get a list of all dedicated locations.
   * 
   * @param boolean izIncludeNonActive If set to <code>true</code> we get all
   *        dedicated locations, if <code>false</code> we get only the active ones.
   *        
   * @return List of <code>DedicatedLocationData</code> objects
   * @throws DBException when there is a database access error.
   */
  public List<Map> getDedicationsList(boolean izIncludeNonActive)
         throws DBException
  {
    DedicatedLocationData mpDLData = Factory.create(DedicatedLocationData.class);

    if (!izIncludeNonActive)
      mpDLData.setKey(DedicatedLocationData.REPLENISHNOW_NAME, DBConstants.DLACTIVE);
      
    return(mpDL.getAllElements(mpDLData));
  }

  /*========================================================================*/
  /**
   * Get a list of Item Dedications based upon key info in dldata
   * 
   * @param ipDLData Key Info
   * @return List of <code>DedicatedLocationData</code> objects
   * @throws DBException
   */
  public List<Map> getDedications(DedicatedLocationData ipDLData) throws DBException
  {
    return mpDL.getAllDedicationsWithQuantities(ipDLData);
  }

  /*========================================================================*/
  /**
   *  Get the Replenish Type of replenishment for this dedicated location.
   *  @param isWarehouse The dedicated location's warehouse.
   *  @param isAddress The dedicated location's address.
   *  @param isItem Optional dedicated item.
   *  @return translation containing primary Replenish type of source used for
   *          replenishment.
   */
  public int getReplenishType(String isWarehouse, String isAddress,
                               String isItem) throws DBException
  {
    return(mpDL.getReplenishType(isWarehouse, isAddress, isItem));
  }

  /*========================================================================*/
  /**
   * Move an item dedication
   * 
   * @param ipFromDLData
   * @param ipToDLData
   * @param ibMoveNow *** CURRENTLY NOT IMPLEMENTED ***
   * @return
   * @throws DBException
   */
  public String moveDedication(DedicatedLocationData ipFromDLData, 
      DedicatedLocationData ipToDLData, boolean ibMoveNow) throws DBException
  {
    String vsReturnString;
    String vsItem = ipFromDLData.getItem();
    String vsFromWarehouse = ipFromDLData.getWarehouse();
    String vsFromAddress = ipFromDLData.getAddress();
    String vsToWarehouse = ipToDLData.getWarehouse();
    String vsToAddress = ipToDLData.getAddress();
    TransactionToken tt = null;
    
    validateDLData(vsItem, vsToWarehouse, vsToAddress);
    
    vsReturnString = "Failed to move " + 
      describeDedication(vsItem, vsFromWarehouse, vsFromAddress);
    
    /*
     * Currently we can't move a dedication within a warehouse.
     * TODO: Figure out a database schema that will allow a move within a warehouse.
     */
    if (vsFromWarehouse.equals(vsToWarehouse))
    {
      throw new DBException("Unable to move an item within a warehouse.");
    }
    
    /*
     * Make sure we have the correct current & enroute quantities for 
     * the from-location
     */
    DedicatedLocationData vpFromDLData = mpDL.getDedicationData(vsItem, 
        vsFromWarehouse, vsFromAddress, true);
    if (vpFromDLData == null)
    {
      throw new DBException(describeDedication(ipFromDLData) + " NOT FOUND");
    }
    
    try
    {
      tt = startTransaction();
      logDebug("Moving " + describeDedication(vsItem, vsFromWarehouse, vsFromAddress) +
          " to " + vsToWarehouse + "-" + vsToAddress);

      /*
       * Add the new
       */
      mpDL.addDedication(ipToDLData);
      
      /*
       * Move the Items
       */
      if (ibMoveNow) 
      {
        /*
         * Move Now only works for specific location to specific location
         */
        if ((vsFromAddress.trim().length() == 0) ||
            (vsToAddress.trim().length() == 0))
        {
          throw new DBException("Move Now only works for location to location moves.");
        }

        /*
         * Create the to-load if it doesn't exist
         */
        String vsToLoad = mpLoadServer.getLoadForLocation(vsToWarehouse, vsToAddress, true);
       
        /*
         * Actually move the items
         */
        LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
        List<Map> vpLLIList = mpDL.getItemsInDedicatedLocation(vpFromDLData);
        for (Map m : vpLLIList)
        {
          vpLLIData.dataToSKDCData(m);
          mpInvServer.transferLoadLineItem(vpLLIData, vsToLoad,
              ReasonCode.getItemLoadTransferReasonCode());
        }
     
        /*
         * Get quantities after the transfers
         */
        vpFromDLData = mpDL.getDedicationData(vsItem, vsFromWarehouse, vsFromAddress, true);
        if (vpFromDLData == null)
        {
          throw new DBException(describeDedication(ipFromDLData) + " NOT FOUND");
        }
        
        /*
         * Deactivate the old dedication
         */
        mpDL.deactivateDedication(vpFromDLData);
      }
      else
      {
        // TODO: Implement generateUnReplenishmentOrder()
        // When the above is completed, uncomment the call below and delete
        // the call to deactivateDedication() below.
        //
        // mpDL.unreplenishDedication(vpFromDLData);

        mpDL.deactivateDedication(vpFromDLData);
      }
      
      /*
       * Delete the old
       */
      checkObsoleteDedication(vpFromDLData);
      
      vsReturnString = "Moved " + describeDedication(vsItem, vsFromWarehouse, vsFromAddress) +
        " to " + vsToWarehouse + "-" + vsToAddress;
      logOperation(LogConsts.OPR_DSVR, vsReturnString);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
    
    return vsReturnString;
  }

  /*========================================================================*/
  
  /**
   * Checks to see if a dedicated location requires replenishment
   * 
   * @param ipItem
   * @param ipWarehouse
   * @param ipAddress
   * @return TRUE replenishment is needed, FALSE otherwise
   * @throws DBException
   */
  public boolean needsReplenishment(String isItem, String isWarehouse, String isAddress)
    throws DBException
  {
    DedicatedLocationData vpDLData = mpDL.getDedicationData(isItem, isWarehouse,
        isAddress, true);
    if (vpDLData == null)
    {
      throw new DBException(describeDedication(isItem, isWarehouse, isAddress) + " NOT FOUND");
    }
    return needsReplenishment(vpDLData);
  }

  /*========================================================================*/
  
  /**
   * Checks to see if a dedicated location requires replenishment
   * 
   * @param ipDLData - DedicatedLocationData with all quantities set
   * @return
   * @throws DBException
   */
  private boolean needsReplenishment(DedicatedLocationData ipDLData)
    throws DBException
  {
    String vsItem = ipDLData.getItem();
    String vsWarehouse = ipDLData.getWarehouse();
    String vsAddress = ipDLData.getAddress();
    TransactionToken tt = null;
    boolean vzNeedsReplen = false;

    String vsLogMessage = "Check replenishment need for " + 
      describeDedication(vsItem, vsWarehouse, vsAddress);
    logDebug(vsLogMessage);

    try
    {
      tt = startTransaction();
      if (mpDL.isObsolete(ipDLData))
      {
        checkObsoleteDedication(ipDLData);
      }
      else if (mpDL.isActive(ipDLData))
      {
        if (ipDLData.getCurrentQuantity() < ipDLData.getMinimumQuantity())
        {
          if (!replenishmentExists(ipDLData))
          {
            vzNeedsReplen = true;
          }
        }
      }
      
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
    return vzNeedsReplen;
  }
  
  /*========================================================================*/

  /**
   *  Send a replenishment request.
   *  This is a convenience method.  Pass in the item/location and it will then 
   *  get the dedication record (with no address) and create a replenishment 
   *  for it if there is a dedication and if it needs replenishment.
   *  
   * @param ipItem - item being checked
   * @param ipWarehouse - location warehouse
   * @throws DBException
   */
  public void replenishDedication(String isItem, String isWarehouse)
    throws DBException
  {
    replenishDedication(isItem, isWarehouse, "");
  }
  
  /**
   *  Send a replenishment request.
   *  This is a convenience method.  Pass in the item/location and it will then 
   *  get the dedication record and create a replenishment for it if there
   *  is a dedication and if it needs replenishment.
   *  
   * @param ipItem - item being checked
   * @param ipWarehouse - location warehouse
   * @param ipAddress - location address
   * @throws DBException
   */
  public void replenishDedication(String isItem, String isWarehouse, String isAddress)
    throws DBException
  {
    DedicatedLocationData vpDLData = getDedicatedLocationRecord(isItem, isWarehouse, isAddress, false);
    if(vpDLData != null)
    {
      replenishDedication(vpDLData);
    }
  }

  /**
   * Send a replenishment request if...
   * <BR>1. The dedication is active
   * <BR>2. The current quantity is less than the minimum quantity
   * <BR>3. There is not already a replenishment order for this dedication
   * 
   * @param ipDLData
   * @throws DBException
   */
  public void replenishDedication(DedicatedLocationData ipDLData)
    throws DBException
  {
    String vsItem = ipDLData.getItem();
    String vsWarehouse = ipDLData.getWarehouse();
    String vsAddress = ipDLData.getAddress();
    TransactionToken tt = null;
    
    /*
     * Make sure we have the correct current & enroute quantities 
     */
    DedicatedLocationData vpDLData = mpDL.getDedicationData(vsItem, vsWarehouse,
        vsAddress, true);
    if (vpDLData == null)
    {
      throw new DBException(describeDedication(ipDLData) + " NOT FOUND");
    }

    /*
     * If this needs a replenishment, generate one
     */
    if (needsReplenishment(vpDLData))
    {
      try
      {
        String vsLogMessage = "Replenishment request for " + 
          describeDedication(vsItem, vsWarehouse, vsAddress);
        logDebug(vsLogMessage);

        tt = startTransaction();

        generateReplenishmentOrder(vpDLData);

        logOperation(LogConsts.OPR_DSVR, vsLogMessage);

        commitTransaction(tt);
      }
      finally
      {
        endTransaction(tt);
      }
    }
  }
  
    
  /*========================================================================*/
  /**
   * Modify an Item Dedication
   * 
   * @param ipDLData - The dedication to modify
   * @return Informational message
   */
  public String updateDedication(DedicatedLocationData ipDLData, boolean izRequestReplenish)
    throws DBException
  {
    String vsReturnString  = "";
    String vsItem = ipDLData.getItem();
    String vsWarehouse = ipDLData.getWarehouse();
    String vsAddress = ipDLData.getAddress();
    TransactionToken tt = null;
    
    validateDLData(vsItem, vsWarehouse, vsAddress);
    
    try
    {
      tt = startTransaction();
      logDebug("Modifying " + describeDedication(vsItem, vsWarehouse, vsAddress));

      mpDL.updateDedication(ipDLData);
      if(izRequestReplenish == true)
      {
        replenishDedication(ipDLData);
      }
      
      vsReturnString = "Modified " + describeDedication(vsItem, vsWarehouse, vsAddress);
      logOperation(LogConsts.OPR_DSVR, vsReturnString);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
    
    return vsReturnString;
  }
  
  /**
   * Modify an Item Dedication
   * 
   * @param ipDLData - The dedication to modify
   * @return Informational message
   */
  public String updateDedicationNoReplenish(DedicatedLocationData ipDLData)
    throws DBException
  {
    String vsReturnString  = "";
    String vsItem = ipDLData.getItem();
    String vsWarehouse = ipDLData.getWarehouse();
    String vsAddress = ipDLData.getAddress();
    TransactionToken tt = null;
    
    validateDLData(vsItem, vsWarehouse, vsAddress);
    
    try
    {
      tt = startTransaction();
      logDebug("Modifying " + describeDedication(vsItem, vsWarehouse, vsAddress));

      mpDL.updateDedication(ipDLData);
      
      vsReturnString = "Modified " + describeDedication(vsItem, vsWarehouse, vsAddress);
      logOperation(LogConsts.OPR_DSVR, vsReturnString);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
    
    return vsReturnString;
  }

  
  /*========================================================================*/
  /**
   * Get a list of Item Dedications based upon key info in dldata
   * 
   * @param isItem Key Info
   * @return List of <code>DedicatedLocationData</code> objects
   * @throws DBException
   */
  public List<Map> getDedicationsByItem(String isItem) throws DBException
  {
    return mpDL.getDedicatedLocationDataListByItem(isItem);
  }

}
