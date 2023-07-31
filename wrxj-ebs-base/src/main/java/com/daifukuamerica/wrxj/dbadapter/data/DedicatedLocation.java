package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * <B>Title:</B> Class to handle Dedicated Location Object.<BR>
 * <B>Description:</B> Handles all reading and writing database for DedicatedLocation<BR>
 *  
 * @author       Michael Andrus
 * @version      1.0  16-Nov-04
 * 
 * <BR>Copyright (c) 2005 by Daifuku America Corporation
 */
public class DedicatedLocation extends BaseDBInterface
{
  private DedicatedLocationData mpDLData;

  public DedicatedLocation()
  {
    super("DedicatedLocation");
    mpDLData = Factory.create(DedicatedLocationData.class);
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpDLData   = null;
  }

  /*========================================================================*/
  /*  Testers                                                               */
  /*========================================================================*/
  public boolean isObsolete(DedicatedLocationData ipDLData)
  {
    int viReplenish = ipDLData.getReplenishNow();
    
    if ((viReplenish == DBConstants.DLINACTIVE) ||
        (viReplenish == DBConstants.DLUNREPLEN))
    {
      return true;
    }
    return false;
  }
  
  public boolean isUnreplenishing(DedicatedLocationData ipDLData)
  {
    return ipDLData.getReplenishNow() == DBConstants.DLUNREPLEN;
  }

  public boolean isWaiting(DedicatedLocationData ipDLData)
  {
    return ipDLData.getReplenishNow() == DBConstants.DLWAIT;
  }
  
  public boolean isActive(DedicatedLocationData ipDLData)
  {
    return ipDLData.getReplenishNow() == DBConstants.DLACTIVE;
  }

  /*========================================================================*/
  /*  Getters                                                               */
  /*========================================================================*/
  /**
   * Gets all requested elements, then populates the current quantity
   */
  public List<Map> getAllDedicationsWithQuantities(DedicatedLocationData ipDLData)
    throws DBException
  {
    List<Map> vpAllList = getAllElements(ipDLData);
    Double vdCurrent;
    Double vdEnroute;
    DedicatedLocationData vpDLData = Factory.create(DedicatedLocationData.class);
    
    /*
     * Get the current quantity for each row
     */
    for (Map<String,Object> m : vpAllList)
    {
      vpDLData.dataToSKDCData(m);
      
      /*
       * Get the current quantity
       */
      vdCurrent = getCurrentQuantity(vpDLData);
      m.remove(DedicatedLocationData.CURRENTQUANTITY_NAME);
      m.put(DedicatedLocationData.CURRENTQUANTITY_NAME, Double.valueOf(vdCurrent));

      /*
       * Get the enroute quantity
       */
      vdEnroute = getEnrouteQuantity(vpDLData);
      m.remove(DedicatedLocationData.ENROUTEQUANTITY_NAME);
      m.put(DedicatedLocationData.ENROUTEQUANTITY_NAME, Double.valueOf(vdEnroute));
    }
    
    return(vpAllList);
  }
  
  
  /**
   * Get the DedicatedLocationData for a specified Item/Warehouse/Address
   * 
   * @param isItem
   * @param isWarehouse
   * @param isAddress
   * @return
   * @throws DBException
   */
  public DedicatedLocationData getDedicationData(String isItem, 
      String isWarehouse, String isAddress, boolean ibGetQuantity)	throws DBException
  {
    DedicatedLocationData vpDLData;
    
    mpDLData.clear();
    mpDLData.setKey(DedicatedLocationData.ITEM_NAME, isItem);
    mpDLData.setKey(DedicatedLocationData.WAREHOUSE_NAME, isWarehouse);
    mpDLData.setKey(DedicatedLocationData.ADDRESS_NAME, isAddress);
  
    vpDLData = getElement(mpDLData, DBConstants.NOWRITELOCK);
    if ((ibGetQuantity) && (vpDLData != null))
    {
      vpDLData.setCurrentQuantity(getCurrentQuantity(vpDLData));
      vpDLData.setEnrouteQuantity(getEnrouteQuantity(vpDLData));
    }
    return(vpDLData);
  }

  
  /**
   * Gets the current quantity for a dedication
   * 
   * @param ipDLData
   * @return
   * @throws DBException
   */
  public double getCurrentQuantity(DedicatedLocationData ipDLData) throws DBException
  {
    double vdCurrentQuantity = 0.0;
    
    if (ipDLData != null)
    {
      try
      {
        StringBuilder vpSql = new StringBuilder("SELECT sum(ID.fCurrentQuantity) as fCurrentQuantity ");
        vpSql.append("FROM LoadLineItem ID, Load LD  ");
        vpSql.append("WHERE ID.sLoadID = LD.sLoadID ");
        vpSql.append("AND ID.sItem = \'" + ipDLData.getItem() + "\' ");
        vpSql.append("AND LD.sWarehouse = \'" + ipDLData.getWarehouse() + "\' ");
        if (ipDLData.getAddress().length() != 0)
        {
          vpSql.append("AND LD.sAddress = \'" + ipDLData.getAddress() + "\' ");
        }
      
        vdCurrentQuantity = getDoubleColumn("fCurrentQuantity", vpSql.toString());
      }
      catch (NullPointerException e) {}
    }
    
    return vdCurrentQuantity;
  }
  
  
  /**
   * Gets the enroute quantity for a dedication
   * 
   * @param ipDLData
   * @return
   * @throws DBException
   */
  public double getEnrouteQuantity(DedicatedLocationData ipDLData) throws DBException
  {
    double vdEnrouteQuantity = 0.0;
    
    if (ipDLData != null)
    {
      try
      {
        StringBuilder vpSql = new StringBuilder("SELECT sum(OL.fOrderQuantity) as fEnrouteQuantity ");
        vpSql.append("FROM OrderHeader OH, OrderLine OL  ");
        vpSql.append("WHERE OH.sOrderID = OL.sOrderID ");
        vpSql.append(" AND OH.iOrderType = " + DBConstants.REPLENISHMENT);
        vpSql.append(" AND OL.sItem = \'" + ipDLData.getItem() + "\' ");
        vpSql.append(" AND OH.sDestWarehouse = \'" + ipDLData.getWarehouse() + "\' ");
        if (ipDLData.getAddress().length() != 0)
        {
          vpSql.append("AND OH.sDestAddress = \'" + ipDLData.getAddress() + "\' ");
        }
      
        vdEnrouteQuantity = getDoubleColumn("fEnrouteQuantity", vpSql.toString());
      }
      catch (NullPointerException e) {}
    }
    
    return vdEnrouteQuantity;
  }

  
  /**
   * Returns a list of LoadLineItems that are in a dedicated location
   * 
   * @param ipDLData
   * @return
   */
  public List<Map> getItemsInDedicatedLocation(DedicatedLocationData ipDLData)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ID.* ");
    vpSql.append("FROM LoadLineItem ID, Load LD  ");
    vpSql.append("WHERE ID.sLoadID = LD.sLoadID ");
    vpSql.append("AND ID.sItem = \'" + ipDLData.getItem() + "\' ");
    vpSql.append("AND LD.sWarehouse = \'" + ipDLData.getWarehouse() + "\' ");
    if (ipDLData.getAddress().length() != 0)
    {
      vpSql.append("AND LD.sAddress = \'" + ipDLData.getAddress() + "\' ");
    }

    return fetchRecords(vpSql.toString());
  }
  
  
  /**
   *  Get the Replenishment Type of replenishment for this dedicated location.
   *  @param isWarehouse The dedicated location's warehouse.
   *  @param isAddress The dedicated location's address.
   *  @param isItem Optional dedicated item.
   *  @return translation containing replenishment type of source used for
   *          replenishment.
   */
  public int getReplenishType(String isWarehouse, String isAddress,
                               String isItem) throws DBException
  {
    String vsItemSQL = "";
    if (isItem.trim().length() != 0)
      vsItemSQL = "AND sItem = \'" + isItem + "\' ";
      
    StringBuilder vpSql = new StringBuilder("SELECT " + DedicatedLocationData.REPLENISHTYPE_NAME + " FROM DedicatedLocation ")
             .append("WHERE sWarehouse = '").append(isWarehouse).append("' AND ")
             .append("sAddress = '").append(isAddress).append("' ")
             .append(vsItemSQL);
             
    return getIntegerColumn(DedicatedLocationData.REPLENISHTYPE_NAME, vpSql.toString());
  }
  
  /*========================================================================*/
  /*  Listers                                                               */
  /*========================================================================*/
  public List<Map> getDedicatedLocationDataList() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM DedicatedLocation ");
    vpSql.append("ORDER BY sItem");
    return fetchRecords(vpSql.toString());
  }
  
  public List<Map> getDedicatedLocationDataListByItem(String item) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM DedicatedLocation ");
    vpSql.append("WHERE sItem = \'" + item + "\' ");
    vpSql.append("ORDER BY sWarehouse, sAddress");
    return fetchRecords(vpSql.toString());
  }

  public List<Map> getDedicatedLocationDataListByLoc(String isWarehouse, String isAddress)
	  throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM DedicatedLocation ");
    vpSql.append("WHERE sWarehouse = \'" + isWarehouse + "\' ");
    vpSql.append("AND sAddress like \'" + isAddress + "%\' ");
    vpSql.append("ORDER BY sWarehouse, sAddress");
    return fetchRecords(vpSql.toString());
  }

  public List<Map> getEmptyDedicatedLocationList(String isWarehouse)
    throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM Location ");
    vpSql.append("WHERE sWarehouse = \'" + isWarehouse + "\' ");
    vpSql.append("AND iLocationType = " + DBConstants.LCDEDICATED + " ");
    vpSql.append("AND iEmptyFlag = " + DBConstants.UNOCCUPIED + " ");
    vpSql.append("AND sAddress not in ");
    vpSql.append("(select sAddress from DedicatedLocation where sWarehouse='" + isWarehouse +"') ");
    vpSql.append("ORDER BY sWarehouse, sAddress");
    return fetchRecords(vpSql.toString());
  }
  /*========================================================================*/
  /*  Adders                                                                */
  /*========================================================================*/

  /**
   * Add an item dedication.  "Fixes" invalid quantities.
   * 
   * @param dlData The dedication to add
   * @throws DBException
   */
  public void addDedication(DedicatedLocationData ipDLData)
    throws DBException
  {
    prepData(ipDLData);
    addElement(ipDLData);
  }
  
  /*========================================================================*/
  /*  Updaters                                                              */
  /*========================================================================*/
  
  /**
   * Tells a dedication to deactivate itself
   * 
   * @param ipDLData
   * @throws DBException
   */
  public void deactivateDedication(DedicatedLocationData ipDLData) throws DBException
  {
    ipDLData.setReplenishNow(DBConstants.DLINACTIVE);
    updateDedication(ipDLData);
  }

  /**
   * Tells a dedication to unreplenish itself
   * 
   * @param ipDLData
   * @throws DBException
   */
  public void unreplenishDedication(DedicatedLocationData ipDLData) throws DBException
  {
    ipDLData.setReplenishNow(DBConstants.DLUNREPLEN);
    updateDedication(ipDLData);
  }
  
  /**
   * Updates a dedication
   *  
   * @param ipDLData
   * @throws DBException
   */
  public void updateDedication(DedicatedLocationData ipDLData)
  	throws DBException
  {
    prepData(ipDLData);
    
    int vnAddParam = 1;

    StringBuffer vsModCmd = new StringBuffer();
    vsModCmd.append("UPDATE DedicatedLocation SET fMinimumQuantity = ?, ")
            .append(" fMaximumQuantity = ?, iDedicatedType = ?, ")
            .append(" iReplenishType = ?, iReplenishNow = ? ")
            .append(" WHERE sItem = ? AND sWarehouse = ? ");
     
     if (ipDLData.getAddress().length() == 0)
     {
       vsModCmd.append(" AND sAddress is null ");
       vnAddParam = 0;
     }
     else
     {
       vsModCmd.append(" AND sAddress = ? ");
     }
     
     Object[] vapParams = new Object[7 + vnAddParam];
     int i = 0;
     vapParams[i++] = ipDLData.getMinimumQuantity();
     vapParams[i++] = ipDLData.getMaximumQuantity();
     vapParams[i++] = ipDLData.getDedicatedType();
     vapParams[i++] = ipDLData.getReplenishType();
     vapParams[i++] = ipDLData.getReplenishNow();
     vapParams[i++] = ipDLData.getItem();
     vapParams[i++] = ipDLData.getWarehouse();
     if (vnAddParam == 1) vapParams[i++] = ipDLData.getAddress();

     execute(vsModCmd.toString(), vapParams);
  }


  /*========================================================================*/
  /*  Deleters                                                              */
  /*========================================================================*/
  
  /**
   * Tells a dedication to delete itself.  If the dedication's current 
   * quantity is >0, it just sets itself to inactive.
   * 
   * @param ipDLData
   * @throws DBException
   */
  public void deleteDedication(DedicatedLocationData ipDLData) throws DBException
  {
    String vsItem      = ipDLData.getItem();
    String vsWarehouse = ipDLData.getWarehouse();
    String vsAddress   = ipDLData.getAddress();

    StringBuffer vsDelCmd = new StringBuffer("DELETE DedicatedLocation ");
    vsDelCmd.append("WHERE sItem = ? AND sWarehouse = ? ");
    if (vsAddress.length() == 0)
    {
      vsDelCmd.append(" AND sAddress is null ");
      execute(vsDelCmd.toString(), vsItem, vsWarehouse);
    }
    else
    {
      vsDelCmd.append(" AND sAddress = ? ");
      execute(vsDelCmd.toString(), vsItem, vsWarehouse, vsAddress);
    }

    mpDLData.clear();

    if (vsAddress.length() > 0)
    {
      List<Map> vpWaitingList = getDedicatedLocationDataListByLoc(vsWarehouse, vsAddress);
      DedicatedLocationData vpDLData = new DedicatedLocationData();
      DedicatedLocationData vpWaitingDLData = null;
      boolean vbActivateWaitingDL = true;
      
      if (vpWaitingList != null)
      {
        while (vpWaitingList.size() > 0)
        {
          vpDLData.dataToSKDCData(vpWaitingList.get(0));
          switch (vpDLData.getReplenishNow())
          {
            case DBConstants.DLWAIT:
              vpWaitingDLData = (DedicatedLocationData) vpDLData.clone();
              break;
              
            case DBConstants.DLINACTIVE:
            case DBConstants.DLUNREPLEN:
              vbActivateWaitingDL = false;
              break;
          }
          vpWaitingList.remove(0);
        }
        
        if (vbActivateWaitingDL && (vpWaitingDLData != null))
        {
          vpWaitingDLData.setReplenishNow(DBConstants.DLACTIVE);
          updateDedication(vpWaitingDLData);
        }
      }
    }
  }
  
  public void deleteDedication() throws DBException
  {
    deleteDedication(mpDLData);
  }

  
  /*========================================================================*/
  /*  private helper functions                                              */
  /*========================================================================*/

  /**
   * Fixes quantities.  Done here so that the setters don't have to be called
   * in a particular order.
   * 
   * @param ipDLData - The data to check/fix
   */
  private void prepData(DedicatedLocationData ipDLData)
  {
    double vdMin       = ipDLData.getMinimumQuantity();
    double vdMax       = ipDLData.getMaximumQuantity();
    
    /*
     * The Max must exceed the min
     */
    if (vdMax <= vdMin)
    {
      ipDLData.setMaximumQuantity(vdMin + 1);
    }
    
    /*
     * There is no wait state for warehouse dedications
     */
    if (ipDLData.getAddress().length() == 0)
    {
      if (ipDLData.getReplenishNow() == DBConstants.DLWAIT)
      {
        ipDLData.setReplenishNow(DBConstants.DLACTIVE);
      }
    }
  }
  
  /**
   *  Method to see if any Dedicated Locations have invalid items
   *
   * @return StringBuffer Order IDs.
   */
  public List<Map> getDedicatedLocationsWithInvalidItemMaster( ) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sWarehouse, sAddress, sItem FROM DedicatedLocation ")
             .append("WHERE sItem NOT IN (Select sItem FROM ITEMMASTER)");
                 
    return fetchRecords(vpSql.toString());
  }
   
  /**
   * Retrieves a String[] list of all Dedicated Locations with 
   * an incorrect warehouse address 
   *
   * @return reference to an String[] of Order Numbers    
   *         
   * @exception DBException
   */
  public List<Map> getDedicatedLocationsWithInvalidLocation() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sWarehouse, sAddress, sItem FROM DedicatedLocation ")
             .append(" WHERE DedicatedLocation.sWarehouse NOT IN")
             .append(" (SELECT SWAREHOUSE FROM LOCATION) OR")
             .append(" DedicatedLocation.sAddress NOT IN")
             .append(" (SELECT SADDRESS FROM LOCATION)");

    return fetchRecords(vpSql.toString());
  }
}
