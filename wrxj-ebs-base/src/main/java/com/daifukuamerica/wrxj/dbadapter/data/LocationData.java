package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.LocationEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Location Data operations.
 *
 * @author       A.D.
 * @version      1.0
 * @since       21-Jan-01
 */
public class LocationData extends AbstractSKDCData
{
  public static final String ADDRESS_NAME        = ADDRESS.getName();
  public static final String AISLEGROUP_NAME     = AISLEGROUP.getName();
  public static final String ALLOWDELETION_NAME  = ALLOWDELETION.getName();
  public static final String ASSIGNEDLENGTH_NAME = ASSIGNEDLENGTH.getName();
  public static final String DEVICEID_NAME       = DEVICEID.getName();
  public static final String EMPTYFLAG_NAME      = EMPTYFLAG.getName();
  public static final String HEIGHT_NAME         = HEIGHT.getName();
  public static final String LINKEDADDRESS_NAME  = LINKEDADDRESS.getName();
  public static final String LOCATIONDEPTH_NAME  = LOCATIONDEPTH.getName();
  public static final String LOCATIONSTATUS_NAME = LOCATIONSTATUS.getName();
  public static final String LOCATIONTYPE_NAME   = LOCATIONTYPE.getName();
  public static final String MOVESEQUENCE_NAME   = MOVESEQUENCE.getName();
  public static final String SEARCHORDER_NAME    = SEARCHORDER.getName();
  public static final String SHELFPOSITION_NAME  = SHELFPOSITION.getName();
  public static final String SWAPZONE_NAME       = SWAPZONE.getName();
  public static final String WAREHOUSE_NAME      = WAREHOUSE.getName();
  public static final String ZONE_NAME           = ZONE.getName();
  public static final String WAREHOUSE_TYPE      = WAREHOUSETYPE.getName();

/*---------------------------------------------------------------------------
                   Database fields for Location table.
  ---------------------------------------------------------------------------*/
  private String sWarehouse      = "";
  private String sAddress        = "";
  private String sShelfPosition  = "";
  private String sEndingAddress  = "";  // Not in database
  private String sZone           = "";
  private String sDeviceID       = "";
  private int    iLocationStatus = DBConstants.LCAVAIL;
  private int    iLocationType   = DBConstants.LCASRS;
  private int    iEmptyFlag      = DBConstants.UNOCCUPIED;
  private int    iHeight         = 1;
  private int    iSearchOrder    = 1;
  private int    iAisleGroup     = 0;
  private int    iAllowDeletion  = DBConstants.YES;
  private int    iMoveSequence   = 0;
  private int    iLocationDepth  = DBConstants.LC_SINGLE;
  private String sLinkedAddress  = "";
  private int    iSwapZone       = 0;
  private int    iLinkedBank     = 0;
  private int    iAssignedLength = 0;
  private static final Map<String, TableEnum> mpColumnMap = 
      new ConcurrentHashMap<String, TableEnum>();

  /**
   * Public Constructor
   */
  public LocationData()
  {
    super();
    initColumnMap(mpColumnMap, LocationEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sWarehouse:" + sWarehouse +
               "\nsAddress:" + sAddress +
               "\nsShelfPosition:" + sShelfPosition +
               "\nsZone:" + sZone +
               "\nsDeviceID:" + sDeviceID + "\n";

    try
    {
      s = s + "iLocationStatus:"
            + DBTrans.getStringValue(LOCATIONSTATUS_NAME, iLocationStatus)
            + "\nLocationType:"
            + DBTrans.getStringValue(LOCATIONTYPE_NAME, iLocationType)
            + "\niEmptyFlag:"
            + DBTrans.getStringValue(EMPTYFLAG_NAME, iEmptyFlag)
            + "\niAllowDeletion:"
            + DBTrans.getStringValue(ALLOWDELETION_NAME, iAllowDeletion)
            + "\niLocationDepth:"
            + DBTrans.getStringValue(LOCATIONDEPTH_NAME, iLocationDepth);
    }
    catch(NoSuchFieldException e)
    {
      s = s + " #### NoSuchFieldException #### ";
    }

    s = s + "\nsLinkedAddress:" + sLinkedAddress 
          + "\niSwapZone:" + iSwapZone
          + "\niAssignedLength:" + iAssignedLength;
    
    s = s + "\niHeight:" + iHeight +
               "\niSearchOrder:" + iSearchOrder +
               "\niAisleGroup:" + iAisleGroup +
               "\niMoveSequence:" + iMoveSequence +
               "\n";
    s += super.toString();

    return(s);
  }

  /**
   * Defines equality between two LocationData objects.
   *
   * @param  absLC <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>LocationData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absLC)
  {
    LocationData lc = (LocationData)absLC;
    return(lc.getWarehouse().equals(this.getWarehouse()) &&
           lc.getAddress().equals(this.getAddress()));
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sWarehouse = "";
    sAddress = "";
    sShelfPosition = "";
    sZone = "";
    sDeviceID = "";
    iLocationType = DBConstants.LCASRS;
    iLocationStatus = DBConstants.LCAVAIL;
    iEmptyFlag = DBConstants.UNOCCUPIED;
    iHeight = 1;
    iSearchOrder = 0;
    iAisleGroup = 0;
    iMoveSequence = 0;
    iAllowDeletion = DBConstants.YES;
    iLocationDepth = DBConstants.LC_SINGLE;
    sLinkedAddress = "";
    iSwapZone = 0;
    iAssignedLength = 0;
  }

  /**
   * This generates the string for the field that is changed.
   */
  @Override
  public String getActionDesc(String isColName, Object ipOld, Object ipNew)
  {
    String s = "Field [" + isColName + "]:";
    
    try
    {
      String vsOld = "";
      String vsNew = "";
      
      // Construct Action Description string
      switch((LocationEnum)mpColumnMap.get(isColName))
      {
      case EMPTYFLAG:
      case LOCATIONSTATUS:
      case LOCATIONTYPE:
        vsOld = DBTrans.getStringValue(isColName, ((Integer)ipOld).intValue());
        vsNew = DBTrans.getStringValue(isColName, ((Integer)ipNew).intValue());
        break;
        
      default:
        vsOld = ipOld.toString();
        vsNew = ipNew.toString();
      }
      s = s + " Old [" + vsOld + "] New [" + vsNew + "]";
    }
    catch(NoSuchFieldException e)
    {
      s = s + " #### NoSuchFieldException #### ";
    }
    return s;
  }
  
  /*---------------------------------------------------------------------------
   * Column value get methods go here. These methods do some basic checking
   * in most cases to return the default value in case something is not set
   * correctly.
   *-------------------------------------------------------------------------*/

  /**
   * Fetches Warehouse
   * 
   * @return warehouse as string
   */
  public String getWarehouse()
  {
    return(sWarehouse);
  }

  /**
   * Fetches Location Address
   * @return Address as string
   */
  public String getAddress()
  {
    return(sAddress);
  }

  public String getShelfPosition()
  {
    return sShelfPosition;
  }

  /**
   * Fetches Ending Location Address
   * @return Address as string
   */
  public String getEndingAddress()
  {
    return(sEndingAddress);
  }

  /**
   * Fetches zone
   * @return zone as string
   */
  public String getZone()
  {
    return(sZone);
  }

  /**
   * Fetches Device ID
   * @return device id. as string.
   */
  public String getDeviceID()
  {
    if (sDeviceID == null) return("");
    return(sDeviceID);
  }

  /**
   * Fetches Location Status
   * @return location status as integer. Return Default of LCAVAIL if not set.
   */
  public int getLocationStatus()
  {
    return(iLocationStatus);
  }

  /**
   * Fetches Empty Flag
   * @return empty flag as integer. Return Default of UNOCCUPIED if not set.
   */
  public int getEmptyFlag()
  {
    return(iEmptyFlag);
  }

  /**
   * Fetches Location Type
   * @return Location Type as integer. Return Default of LCASRS if not set.
   */
  public int getLocationType()
  {
    return(iLocationType);
  }

  /**
   * Fetches Height
   * @return height as integer. Return Default of 1 if not set.
   */
  public int getHeight()
  {
    return(iHeight);
  }

  /**
   * Fetches Location Search Order
   * @return search order as integer
   */
  public int getSearchOrder()
  {
    return(iSearchOrder);
  }

  /**
   * Fetches Aisle Group
   * @return aisle group as integer
   */
  public int getAisleGroup()
  {
    return(iAisleGroup);
  }

  /**
   * Fetches MoveSequence
   * @return MoveSequence as integer
   */
  public int getMoveSequence()
  {
    return(iMoveSequence);
  }
  
  /**
   * Fetches Allow Deletion flag.
   * @return allow deletion as integer
   */
  public int getAllowDeletion()
  {
    return(iAllowDeletion);
  }

  /**
   * Get the Double-Deep Location Depth
   * @return translation value
   */
  public int getLocationDepth()
  {
    return iLocationDepth;
  }
  
  /**
   * Get the Double-Deep Linked Address
   * @return
   */
  public String getLinkedAddress()
  {
    return sLinkedAddress;
  }
  
  /**
   * Get the Double-Deep Swap Zone
   * @return
   */
  public int getSwapZone()
  {
    return iSwapZone;
  }
  
  /**
   * Get the Double-Deep Linked Bank
   * @return
   */
  public int getLinkedBank()
  {
    return iLinkedBank;
  }
  
  /**
   * Get the Assigned Length
   * @return
   */
  public int getAssignedLength()
  {
    return iAssignedLength;
  }
  
  /*---------------------------------------------------------------------------
   * Column Setting methods go here.
   *-------------------------------------------------------------------------*/
  
  /**
   * Sets Warehouse value.
   */
  public void setWarehouse(String isWarehouse)
  {
    sWarehouse = checkForNull(isWarehouse);
    addColumnObject(new ColumnObject(WAREHOUSE.getName(), sWarehouse));
  }

  /**
   * Sets Address value.
   */
  public void setAddress(String isAddress)
  {
    sAddress = checkForNull(isAddress);
    addColumnObject(new ColumnObject(ADDRESS.getName(), sAddress));
  }

  public void setShelfPosition(String isShelfPos)
  {
    sShelfPosition = checkForNull(isShelfPos);
    addColumnObject(new ColumnObject(SHELFPOSITION_NAME, sShelfPosition));
  }

  /**
   * Sets Ending Address value.
   * <BR>This is a helper field for various location-related utilities.  It has
   * no database field.
   */
  public void setEndingAddress(String isEndingAddress)
  {
    sEndingAddress = checkForNull(isEndingAddress);
  }

  /**
   * Sets zone value.
   */
  public void setZone(String isZone)
  {
    sZone = checkForNull(isZone);
    addColumnObject(new ColumnObject(ZONE.getName(), sZone));
  }

  /**
   * Sets Device ID value
   */
  public void setDeviceID(String isDeviceID)
  {
    sDeviceID = checkForNull(isDeviceID);
    addColumnObject(new ColumnObject(DEVICEID.getName(), sDeviceID));
  }

  /**
   * Sets Location Status value
   */
  public void setLocationStatus(int inLocationStatus)
  {
    try
    {
      DBTrans.getStringValue(LOCATIONSTATUS.getName(), inLocationStatus);
    }
    catch(NoSuchFieldException e)
    {
      // Passed value wasn't valid. Default it
      inLocationStatus = DBConstants.LCAVAIL;
    }
    iLocationStatus = inLocationStatus;
    addColumnObject(new ColumnObject(LOCATIONSTATUS.getName(), iLocationStatus));
  }

  /**
   * Sets Location Type value
   */
  public void setLocationType(int inLocationType)
  {
    try
    {
      DBTrans.getStringValue(LOCATIONTYPE.getName(), inLocationType);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inLocationType = DBConstants.LCASRS;
    }
    iLocationType = inLocationType;
    addColumnObject(new ColumnObject(LOCATIONTYPE.getName(), iLocationType));
  }

  /**
   * Sets Empty Flag value
   */
  public void setEmptyFlag(int inEmptyFlag)
  {
    try
    {
      DBTrans.getStringValue(EMPTYFLAG.getName(), inEmptyFlag);
    }
    catch(NoSuchFieldException e)
    {
      // Passed value wasn't valid. Default it
      inEmptyFlag = DBConstants.UNOCCUPIED;
    }
    iEmptyFlag = inEmptyFlag;
    addColumnObject(new ColumnObject(EMPTYFLAG.getName(), iEmptyFlag));
  }

  /**
   * Sets Height value
   */
  public void setHeight(int inHeight)
  {
    iHeight = inHeight;
    addColumnObject(new ColumnObject(HEIGHT.getName(), iHeight));
  }

  /**
   * Sets Location Search Order value
   */
  public void setSearchOrder(int inSearchOrder)
  {
    iSearchOrder = inSearchOrder;
    addColumnObject(new ColumnObject(SEARCHORDER.getName(), iSearchOrder));
  }

  /**
   * Sets Aisle Group value
   */
  public void setAisleGroup(int inAisleGroup)
  {
    iAisleGroup = inAisleGroup;
    addColumnObject(new ColumnObject(AISLEGROUP.getName(), iAisleGroup));
  }

  /**
   * Sets MoveSequence value
   */
  public void setMoveSequence(int inMoveSequence)
  {
    iMoveSequence = inMoveSequence;
    addColumnObject(new ColumnObject(MOVESEQUENCE.getName(), iMoveSequence));
  }
  
  /**
   * Sets Allow Deletion flag value.
   */
  public void setAllowDeletion(int inAllowDeletion)
  {
    if (inAllowDeletion != DBConstants.YES && inAllowDeletion != DBConstants.NO)
    {
      inAllowDeletion = DBConstants.YES;            // Default it if it's unknown.
    }
    iAllowDeletion = inAllowDeletion;
    addColumnObject(new ColumnObject(ALLOWDELETION.getName(), iAllowDeletion));
  }
  
  /**
   * Set the Double-Deep Location Depth
   * 
   * @param inLocationDepth
   */
  public void setLocationDepth(int inLocationDepth)
  {
    try
    {
      DBTrans.getStringValue(LOCATIONDEPTH_NAME, inLocationDepth);
    }
    catch(NoSuchFieldException e)
    {
      // Passed value wasn't valid. Default it
      inLocationDepth = DBConstants.LC_SINGLE;
    }
    iLocationDepth = inLocationDepth;
    addColumnObject(new ColumnObject(LOCATIONDEPTH_NAME, iLocationDepth));
  }
  
  /**
   * Set the Double-Deep Linked Address
   * @param isLinkedAddress
   */
  public void setLinkedAddress(String isLinkedAddress)
  {
    sLinkedAddress = isLinkedAddress;
    addColumnObject(new ColumnObject(LINKEDADDRESS_NAME, sLinkedAddress));
  }

  /**
   * Sets the Double-Deep Linked Bank value.
   * <BR>This is a helper field for various location-related utilities.  It has
   * no database field.
   */
  public void setLinkedBank(int inLinkedBank)
  {
    iLinkedBank = inLinkedBank;
  }

  /**
   * Set the Double-Deep Swap Zone
   * 
   * @param inSwapZone
   */
  public void setSwapZone(int inSwapZone)
  {
    iSwapZone = inSwapZone;
    addColumnObject(new ColumnObject(SWAPZONE_NAME, iSwapZone));
  }

  /**
   * Set the Assigned Length
   * 
   * @param inAssignedLength
   */
  public void setAssignedLength(int inAssignedLength)
  {
    iAssignedLength = inAssignedLength;
    addColumnObject(new ColumnObject(ASSIGNEDLENGTH_NAME, iAssignedLength));
  }

  /**
   * {@inheritDoc}
   * @param {@inheritDoc}
   * @param {@inheritDoc}
   * @return {@inheritDoc}
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return super.setField(isColName, ipColValue);
    }

    switch((LocationEnum)vpEnum)
    {
      case WAREHOUSE:
        setWarehouse((String)ipColValue);
        break;

      case ADDRESS:
        setAddress((String)ipColValue);
        break;

      case SHELFPOSITION:
        setShelfPosition((String)ipColValue);
        break;

      case ZONE:
        setZone((String)ipColValue);
        break;

      case DEVICEID:
        setDeviceID((String)ipColValue);
        break;
                                  
      case LOCATIONSTATUS:
        setLocationStatus((Integer)ipColValue);
        break;

      case LOCATIONTYPE:
        setLocationType((Integer)ipColValue);
        break;

      case EMPTYFLAG:
        setEmptyFlag((Integer)ipColValue);
        break;

      case ALLOWDELETION:
        setAllowDeletion((Integer)ipColValue);
        break;

      case HEIGHT:
        setHeight((Integer)ipColValue);
        break;

      case SEARCHORDER:
        setSearchOrder((Integer)ipColValue);
        break;

      case AISLEGROUP:
        setAisleGroup((Integer)ipColValue);
        break;
                                  
      case MOVESEQUENCE:
        setMoveSequence((Integer)ipColValue);
        break;
        
      case LOCATIONDEPTH:
        setLocationDepth((Integer)ipColValue);
        break;

      case LINKEDADDRESS:
        setLinkedAddress((String)ipColValue);
        break;

      case SWAPZONE:
        setSwapZone((Integer)ipColValue);
        break;

      case ASSIGNEDLENGTH:
        setAssignedLength((Integer)ipColValue);
        break;
    }

    return(0);
  }
}
