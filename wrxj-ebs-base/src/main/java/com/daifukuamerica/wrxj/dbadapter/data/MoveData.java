package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import static com.daifukuamerica.wrxj.dbadapter.data.MoveEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Title:  Class to handle MoveData Object.
 *   Description : Handles all data for move
 * @author       REA
 * @author       A.D.  Refactored for AbstractSKDCData base class.
 * @version      1.0
 * @since        04-Jan-02
 */
public class MoveData extends AbstractSKDCData
{
  public static final String ADDRESS_NAME        = ADDRESS.getName();
  public static final String AISLEGROUP_NAME     = AISLEGROUP.getName();
  public static final String DESTADDRESS_NAME    = DESTADDRESS.getName();
  public static final String DESTWAREHOUSE_NAME  = DESTWAREHOUSE.getName();
  public static final String DEVICEID_NAME       = DEVICEID.getName();
  public static final String DISPLAYMESSAGE_NAME = DISPLAYMESSAGE.getName();
  public static final String ITEM_NAME           = ITEM.getName();
  public static final String LINEID_NAME         = LINEID.getName();
  public static final String LOADID_NAME         = LOADID.getName();
  public static final String MOVECATEGORY_NAME   = MOVECATEGORY.getName();
  public static final String MOVEDATE_NAME       = MOVEDATE.getName();
  public static final String MOVEID_NAME         = MOVEID.getName();
  public static final String MOVESEQUENCE_NAME   = MOVESEQUENCE.getName();
  public static final String MOVESTATUS_NAME     = MOVESTATUS.getName();
  public static final String MOVETYPE_NAME       = MOVETYPE.getName();
  public static final String NEXTADDRESS_NAME    = NEXTADDRESS.getName();
  public static final String NEXTWAREHOUSE_NAME  = NEXTWAREHOUSE.getName();
  public static final String ORDERID_NAME        = ORDERID.getName();
  public static final String ORDERLOT_NAME       = ORDERLOT.getName();
  public static final String PARENTLOAD_NAME     = PARENTLOAD.getName();
  public static final String POSITIONID_NAME     = POSITIONID.getName();
  public static final String PICKLOT_NAME        = PICKLOT.getName();
  public static final String PICKQUANTITY_NAME   = PICKQUANTITY.getName();
  public static final String PICKTOLOADID_NAME   = PICKTOLOADID.getName();
  public static final String PRIORITY_NAME       = PRIORITY.getName();
  public static final String RELEASETOCODE_NAME  = RELEASETOCODE.getName();
  public static final String ROUTEID_NAME        = ROUTEID.getName();
  public static final String SCHEDULERNAME_NAME  = SCHEDULERNAME.getName();
  public static final String WAREHOUSE_NAME      = WAREHOUSE.getName();
  
/*---------------------------------------------------------------------------
                   Database fields for Move table.
  ---------------------------------------------------------------------------*/
  private String sParentLoad    = "";
  private String sLoadID        = "";
  private String sPickToLoadID  = "";
  private String sItem          = "";
  private String sOrderLot      = "";
  private String sPickLot       = "";
  private String sOrderID       = "";
  private String sSchedulerName = "";
  private String sRouteID       = "";
  private String sDeviceID      = "";
  private String sReleaseToCode = "";
  private String sLineID        = "";
  private String sDestWarehouse = "";
  private String sDestAddress   = "";
  private String sNextWarehouse = "";
  private String sNextAddress   = "";
  private String sWarehouse     = "";
  private String sAddress       = "";
  private String sDisplayMessage = "";
  private String sPositionID    = "";
  private Date   dMoveDate      = new Date();
  private int    iMoveID;
  private int    iAisleGroup;
  private int    iMoveSequence;
  private int    iPriority;
  private int    iMoveType;
  private int    iMoveCategory;
  private int    iMoveStatus;
  private double fPickQuantity;
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();
  

  //-----------------------  default data ---------------------------------
  private int    DEFAULT_PRIORITY      = 5;
  private int    DEFAULT_MOVETYPE      = DBConstants.LOADMOVE;
  private int    DEFAULT_MOVECATEGORY  = DBConstants.PICK_REQUEST;
  private int    DEFAULT_MOVESTATUS    = DBConstants.AVAILABLE;
  private int    DEFAULT_AISLEGROUP    = 0;
  private int    DEFAULT_MOVESEQUENCE  = 0;
  private int    DEFAULT_MOVEID        = 0;
  private double DEFAULT_PICKQUANTITY  = 0;

  /**
   * Constuctor for initializing the data and translation values
   */
  public MoveData()
  {
    super();
    initColumnMap(mpColumnMap, MoveEnum.class);
    clear();
  }

  /**
   * Clears all data
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in the default behaviour

    sDeviceID = "";
    sParentLoad = "";
    sLoadID = "";
    sOrderLot = "";
    sPickLot = "";
    sOrderID = "";
    sRouteID = "";
    sPickToLoadID = "";
    sReleaseToCode = "";
    sLineID = "";
    sDestWarehouse = "";
    sDestAddress = "";
    sNextWarehouse = "";
    sNextAddress = "";
    sWarehouse = "";
    sAddress = "";
    sDisplayMessage = "";
    sSchedulerName = "";
    sItem = "";
    sPositionID = "";

    iMoveID = DEFAULT_MOVEID;
    iMoveSequence = DEFAULT_AISLEGROUP;
    iMoveSequence = DEFAULT_MOVESEQUENCE;
    iPriority = DEFAULT_PRIORITY;
    iMoveType = DEFAULT_MOVETYPE;
    iMoveStatus = DEFAULT_MOVESTATUS;
    iMoveCategory = DEFAULT_MOVECATEGORY;
    dMoveDate.setTime(System.currentTimeMillis());
    fPickQuantity = DEFAULT_PICKQUANTITY;  
    iAisleGroup = DEFAULT_AISLEGROUP;

  }
  
  public void setDefaultColumnsAndValues()
  {
    super.clear();                     // Pull in the default behaviour

    setParentLoad("");
    setLoadID("");
    setOrderLot("");
    setPickLot("");
    setOrderID("");
    setRouteID("");
    setPickToLoadID("");
    setReleaseToCode("");
    setLineID("");
    setDestWarehouse("");
    setDestAddress("");
    setNextWarehouse("");
    setNextAddress("");
    setWarehouse("");
    setAddress("");
    setDisplayMessage("");
    setSchedulerName("");
    setItem("");
    setDeviceID("");
    setSchedulerName("");
    setPositionID("");

    setMoveID(DEFAULT_MOVEID);
    setMoveSequence(DEFAULT_AISLEGROUP);
    setMoveSequence(DEFAULT_MOVESEQUENCE);
    setPriority(DEFAULT_PRIORITY);
    setMoveType(DEFAULT_MOVETYPE);
    setMoveStatus(DEFAULT_MOVESTATUS);
    setMoveCategory(DEFAULT_MOVECATEGORY);
    setAisleGroup(DEFAULT_AISLEGROUP);
    setMoveDate(new Date(System.currentTimeMillis()));
    setPickQuantity(DEFAULT_PICKQUANTITY);

  }

  /**
   * This helps in debugging when we want to print the whole structure.
   *
   * @return this data structure as a string
   */
  @Override
  public String toString()
  {
    String s = "iMoveID:" + iMoveID +
               "\nsParentLoad:" + sParentLoad +
               "\nsLoadID:" + sLoadID +
               "\nsItem:" + sItem +
               "\nsPickLot:" + sPickLot +
               "\nsOrderID:" + sOrderID +
               "\nsRouteID:" + sRouteID +
               "\nsDeviceID:" + sDeviceID +
               "\nsPickToLoadID:" + sPickToLoadID +
               "\nsReleaseToCode:" + sReleaseToCode +
               "\nsLineID:" + sLineID +
               "\nsWarehouse:" + sWarehouse +
               "\nsAddress:" + sAddress +
               "\nsDestWarehouse:" + sDestWarehouse +
               "\nsDestAddress:" + sDestAddress +
               "\nsNextWarehouse:" + sNextWarehouse +
               "\nsNextAddress:" + sNextAddress +              
               "\nsDisplayMessage:" + sDisplayMessage +
               "\nsSchedulerName:" + sSchedulerName +
               "\ndMoveDate:" + sdf.format(dMoveDate) +
               "\niAisleGroup:" + iAisleGroup +
               "\niMoveSequence:" + iMoveSequence +
               "\niPriority:" + iPriority + 
               "\nsPositionID:" + sPositionID +
               "\n";

    try
    {
      s = s + "iMoveType:" + DBTrans.getStringValue(MOVETYPE_NAME, iMoveType) +
              "iMoveCategory:" + DBTrans.getStringValue(MOVECATEGORY_NAME, iMoveCategory) +
              "\niMoveStatus:" + DBTrans.getStringValue(MOVESTATUS_NAME, iMoveStatus) + "\n";
    }
    catch(NoSuchFieldException e)
    {
      s = s + "0\n";
    }

    s = s + "fPickQuantity:" + Double.toString(fPickQuantity) + "\n" +
        super.toString();

    return(s);
  }

  /**
   *  Method to make a deep copy of this object.
   *
   *  @return copy of <code>MoveData</code>.
   */
  @Override
  public MoveData clone()
  {
    MoveData vpClonedData = (MoveData)super.clone();
    vpClonedData.dMoveDate = (Date)dMoveDate.clone();
    return vpClonedData;
  }

  /**
   * Defines equality between two MoveData objects.
   *
   * @param  absMV <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>MoveData</code>
   * @return true iff equal
   */
  @Override
  public boolean equals(AbstractSKDCData absMV)
  {
    if (absMV == null || !(absMV instanceof MoveData))
    {
      return(false);
    }
    MoveData mvdata = (MoveData)absMV;
    return(getMoveID() == mvdata.getMoveID());
  }

/*---------------------------------------------------------------------------
                           Getter Methods go here.
  ---------------------------------------------------------------------------*/
  public int getMoveID()
  {
    return(iMoveID);
  }
  
  public String getParentLoad()
  {
    return sParentLoad;
  }

  public String getLoadID()
  {
    return sLoadID;
  }

  public String getItem()
  {
    return sItem;
  }

  public String getOrderLot()
  {
    return sOrderLot;
  }

  public String getPickLot()
  {
    return sPickLot;
  }

  public String getOrderID()
  {
    return sOrderID;
  }

  public String getRouteID()
  {
    return sRouteID;
  }

  public String getDeviceID()
  {
    return sDeviceID;
  }

  public String getSchedulerName()
  {
    return sSchedulerName;
  }

  public String getPickToLoad()
  {
    return sPickToLoadID;
  }
  
  public String getReleaseToCode()
  {
    return sReleaseToCode;
  }
  
  public String getLineID()
  {
    return sLineID;
  }
  
  public String getDestWarehouse()
  {
    return sDestWarehouse;
  }
  
  public String getDestAddress()
  {
    return sDestAddress;
  }
  
  public String getNextWarehouse()
  {
    return sNextWarehouse;
  }
  
  public String getNextAddress()
  {
    return sNextAddress;
  }

  public String getWarehouse()
  {
    return sWarehouse;
  }
  
  public String getAddress()
  {
    return sAddress;
  }
  
  public String getLocation()
  {
    return sWarehouse + sAddress;
  }
  
  public String getDisplayMessage()
  {
    return sDisplayMessage;
  }
  
  public int getAisleGroup()
  {
    return iAisleGroup;
  }
  
  public int getMoveSequence()
  {
    return iMoveSequence;
  }
  
  public int getMoveCategory()
  {
    return iMoveCategory;
  }
  
  public int getPriority()
  {
    return(iPriority);
  }

  public int getMoveType()
  {
    return(iMoveType);
  }

  public int getMoveStatus()
  {
    return(iMoveStatus);
  }

  public Date getMoveDate()
  {
    return(dMoveDate);
  }

  public double getPickQuantity()
  {
    return(fPickQuantity);
  }
  public String getPositionID()
  {
    return sPositionID;
  }

/*---------------------------------------------------------------------------
                           Setter Methods go here.
  ---------------------------------------------------------------------------*/
  public void setParentLoad(String isParentLoad)
  {
    sParentLoad = checkForNull(isParentLoad);
    addColumnObject(new ColumnObject(PARENTLOAD.getName(), isParentLoad));
  }

  public void setLoadID(String isLoadID)
  {
    sLoadID = checkForNull(isLoadID);
    addColumnObject(new ColumnObject(LOADID.getName(), isLoadID));
  }

  public void setSchedulerName(String isSchedulerName)
  {
    // This is a view column and does not need a column object.
    sSchedulerName = checkForNull(isSchedulerName);
  }

  public void setRouteID(String isRouteID)
  {
    sRouteID = checkForNull(isRouteID);
    addColumnObject(new ColumnObject(ROUTEID.getName(), isRouteID));
  }

  public void setDeviceID(String isDeviceID)
  {
    sDeviceID = checkForNull(isDeviceID);
    addColumnObject(new ColumnObject(DEVICEID.getName(), isDeviceID));
  }

  public void setOrderID(String isOrderID)
  {
    sOrderID = checkForNull(isOrderID);
    addColumnObject(new ColumnObject(ORDERID.getName(), isOrderID));
  }

  public void setOrderLot(String isOrderLot)
  {
    sOrderLot = checkForNull(isOrderLot);
    addColumnObject(new ColumnObject(ORDERLOT.getName(), isOrderLot));
  }

  public void setPickLot(String isPickLot)
  {
    sPickLot = checkForNull(isPickLot);
    addColumnObject(new ColumnObject(PICKLOT.getName(), isPickLot));
  }

  public void setItem(String isItem)
  {
    sItem = checkForNull(isItem);
    addColumnObject(new ColumnObject(ITEM.getName(), isItem));
  }

  public void setPickToLoadID(String isPickToLoadID)
  {
    sPickToLoadID = checkForNull(isPickToLoadID);
    addColumnObject(new ColumnObject(PICKTOLOADID.getName(), isPickToLoadID));
  }
  
  public void setReleaseToCode(String isReleaseToCode)
  {
    sReleaseToCode = checkForNull(isReleaseToCode);
    // This is from the view, not in the table. Don't add a ColumnObject.
    // addColumnObject(new ColumnObject(RELEASETOCODE.getName(), isReleaseToCode));
  }
  
  public void setLineID(String isLineID)
  {
    sLineID = checkForNull(isLineID);
    addColumnObject(new ColumnObject(LINEID.getName(), isLineID));
  }
  
  public void setDestWarehouse(String isDestWarehouse)
  {
    sDestWarehouse = checkForNull(isDestWarehouse);
    addColumnObject(new ColumnObject(DESTWAREHOUSE.getName(), isDestWarehouse));
  }
  
  public void setDestAddress(String isDestAddress)
  {
    sDestAddress = checkForNull(isDestAddress);
    addColumnObject(new ColumnObject(DESTADDRESS.getName(), isDestAddress));
  }
  
  public void setNextWarehouse(String isNextWarehouse)
  {
    sNextWarehouse = checkForNull(isNextWarehouse);
    addColumnObject(new ColumnObject(NEXTWAREHOUSE.getName(), isNextWarehouse));
  }
  
  public void setNextAddress(String isNextAddress)
  {
    sNextAddress = checkForNull(isNextAddress);
    addColumnObject(new ColumnObject(NEXTADDRESS.getName(), isNextAddress));
  }
  
  public void setNextAndDestWarehouse(String isWarehouse)
  {
    sNextWarehouse = checkForNull(isWarehouse);
    addColumnObject(new ColumnObject(NEXTWAREHOUSE.getName(), isWarehouse));
    sDestWarehouse = checkForNull(isWarehouse);
    addColumnObject(new ColumnObject(DESTWAREHOUSE.getName(), isWarehouse));
  }
  
  public void setNextAndDestAddress(String isAddress)
  {
    sNextAddress = checkForNull(isAddress);
    addColumnObject(new ColumnObject(NEXTADDRESS.getName(), isAddress));
    sDestAddress = checkForNull(isAddress);
    addColumnObject(new ColumnObject(DESTADDRESS.getName(), isAddress));
  }

  public void setWarehouse(String isWarehouse)
  {
    sWarehouse = checkForNull(isWarehouse);
    // This is from the view, not in the table. Don't add a ColumnObject.
    // addColumnObject(new ColumnObject(WAREHOUSE.getName(), isWarehouse));
  }

  public void setAddress(String isAddress)
  {
    sAddress = checkForNull(isAddress);
    // This is from the view, not in the table. Don't add a ColumnObject.
    // addColumnObject(new ColumnObject(ADDRESS.getName(), isAddress));
  }

  public void setDisplayMessage(String isDispMsg)
  {
    sDisplayMessage = checkForNull(isDispMsg);
    // Don't add a column object because this is just for passing a message to
    // the requester.
  }

  public void setPositionID(String isPositionID)
  {
    sPositionID = checkForNull(isPositionID);
    addColumnObject(new ColumnObject(POSITIONID.getName(), isPositionID));
  }
  
  public void setMoveID(int inMoveID)
  {
    iMoveID = inMoveID;
    addColumnObject(new ColumnObject(MOVEID.getName(), inMoveID));
  }
  
  public void setAisleGroup(int inAisleGroup)
  {
    iAisleGroup = inAisleGroup;
    // This is from the view, not in the table. Don't add a ColumnObject.
    // addColumnObject(new ColumnObject(AISLEGROUP.getName(), inAisleGroup));
  }
  
  public void setMoveSequence(int inMoveSequence)
  {
    iMoveSequence = inMoveSequence;
    // This is from the view, not in the table. Don't add a ColumnObject.
    // addColumnObject(new ColumnObject(MOVESEQUENCE.getName(), inMoveSequence));
  }
  
  public void setMoveType(int inMoveType)
  {
    try
    {
      DBTrans.getStringValue(MOVETYPE.getName(), inMoveType);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inMoveType = DEFAULT_MOVETYPE;
    }
    iMoveType = inMoveType;
    addColumnObject(new ColumnObject(MOVETYPE.getName(), inMoveType));
  }

  public void setMoveStatus(int inMoveStatus)
  {
    try
    {
      DBTrans.getStringValue(MOVESTATUS.getName(), inMoveStatus);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inMoveStatus = DEFAULT_MOVESTATUS;
    }
    iMoveStatus = inMoveStatus;
    addColumnObject(new ColumnObject(MOVESTATUS.getName(), inMoveStatus));
  }

  public void setMoveCategory(int inMoveCategory)
  {
    try
    {
      DBTrans.getStringValue(MOVECATEGORY.getName(), inMoveCategory);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inMoveCategory = DEFAULT_MOVECATEGORY;
    }
    iMoveCategory = inMoveCategory;
    addColumnObject(new ColumnObject(MOVECATEGORY.getName(), inMoveCategory));
  }

  public void setPriority(int inPriority)
  {
    if (inPriority < 0)
    {
      inPriority = DEFAULT_PRIORITY;
    }
    iPriority = inPriority;
    addColumnObject(new ColumnObject(PRIORITY.getName(), inPriority));
  }

  public void setMoveDate(Date ipMoveDate)
  {
    dMoveDate = ipMoveDate;
    addColumnObject(new ColumnObject(MOVEDATE.getName(), ipMoveDate));
  }

  public void setPickQuantity(double idPickQuantity)
  {
    fPickQuantity = idPickQuantity;
    addColumnObject(new ColumnObject(PICKQUANTITY.getName(), idPickQuantity));
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

    switch((MoveEnum)vpEnum)
    {
      case ITEM:
        setItem((String)ipColValue);
        break;

      case PARENTLOAD:
        setParentLoad((String)ipColValue);
        break;

      case LOADID:
        setLoadID((String)ipColValue);
        break;

      case ORDERLOT:
        setOrderLot((String)ipColValue);
        break;

      case PICKLOT:
        setPickLot((String)ipColValue);
        break;

      case ORDERID:
        setOrderID((String)ipColValue);
        break;

      case ROUTEID:
        setRouteID((String)ipColValue);
        break;

      case DEVICEID:
        setDeviceID((String)ipColValue);
        break;

      case SCHEDULERNAME:
        setSchedulerName((String)ipColValue);
        break;

      case PICKTOLOADID:
        setPickToLoadID((String)ipColValue);
        break;

      case RELEASETOCODE:
        setReleaseToCode((String)ipColValue);
        break;

      case LINEID:
        setLineID((String)ipColValue);
        break;

      case DESTWAREHOUSE:
        setDestWarehouse((String)ipColValue);
        break;

      case DESTADDRESS:
        setDestAddress((String)ipColValue);
        break;

      case NEXTWAREHOUSE:
        setNextWarehouse((String)ipColValue);
        break;

      case NEXTADDRESS:
        setNextAddress((String)ipColValue);
        break;

      case WAREHOUSE:
        setWarehouse((String)ipColValue);
        break;

      case ADDRESS:
        setAddress((String)ipColValue);
        break;

      case MOVEID:
        setMoveID((Integer)ipColValue);
        break;

      case AISLEGROUP:
        setAisleGroup((Integer)ipColValue);
        break;

      case MOVESEQUENCE:
        setMoveSequence((Integer)ipColValue);
        break;

      case PRIORITY:
        setPriority((Integer)ipColValue);
        break;

      case MOVETYPE:
        setMoveType((Integer)ipColValue);
        break;

      case MOVECATEGORY:
        setMoveCategory((Integer)ipColValue);
        break;

      case MOVESTATUS:
        setMoveStatus((Integer)ipColValue);
        break;

      case MOVEDATE:
        setMoveDate((Date)ipColValue);
        break;

      case PICKQUANTITY:
        setPickQuantity((Double)ipColValue);
        break;

      case DISPLAYMESSAGE:
        setDisplayMessage((String)ipColValue);
        break;
        
      case POSITIONID:
        setPositionID((String)ipColValue);
                  
    }

    return(0);
  }
}

