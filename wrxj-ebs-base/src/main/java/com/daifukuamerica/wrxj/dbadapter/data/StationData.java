package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.StationEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.messageformat.MessageConstants;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StationData extends AbstractSKDCData
{
  public static final String ALLOCATIONENABLED_NAME    = ALLOCATIONENABLED.getName();
  public static final String ALLOCATIONTYPE_NAME       = ALLOCATIONTYPE.getName();
  public static final String ALLOWROUNDROBIN_NAME      = ALLOWROUNDROBIN.getName();
  public static final String AMOUNTFULL_NAME           = AMOUNTFULL.getName();
  public static final String ARRIVALREQUIRED_NAME      = ARRIVALREQUIRED.getName();
  public static final String AUTOLOADMOVEMENTTYPE_NAME = AUTOLOADMOVEMENTTYPE.getName();
  public static final String AUTOORDERTYPE_NAME        = AUTOORDERTYPE.getName();
  public static final String BIDIRECTIONALSTATUS_NAME  = BIDIRECTIONALSTATUS.getName();
  public static final String CAPTIVE_NAME              = CAPTIVE.getName();
  public static final String CCIALLOWED_NAME           = CCIALLOWED.getName();
  public static final String CONFIRMITEM_NAME          = CONFIRMITEM.getName();
  public static final String CONFIRMLOAD_NAME          = CONFIRMLOAD.getName();
  public static final String CONFIRMLOCATION_NAME      = CONFIRMLOCATION.getName();
  public static final String CONFIRMLOT_NAME           = CONFIRMLOT.getName();
  public static final String CONFIRMQTY_NAME           = CONFIRMQTY.getName();
  public static final String CONTAINERTYPE_NAME        = CONTAINERTYPE.getName();
  public static final String CUSTOMACTION_NAME         = CUSTOMACTION.getName();
  public static final String DEFAULTROUTE_NAME         = DEFAULTROUTE.getName();
  public static final String DELETEINVENTORY_NAME      = DELETEINVENTORY.getName();
  public static final String DESCRIPTION_NAME          = DESCRIPTION.getName();
  public static final String DEVICEID_NAME             = DEVICEID.getName();
  public static final String HEIGHT_NAME               = HEIGHT.getName();
  public static final String ITEM_NAME                 = ITEM.getName();
  public static final String LINKROUTE_NAME            = LINKROUTE.getName();
  public static final String LOADPREFIX_NAME           = LOADPREFIX.getName();
  public static final String LOT_NAME                  = LOT.getName();
  public static final String MAXALLOWEDENROUTE_NAME    = MAXALLOWEDENROUTE.getName();
  public static final String MAXALLOWEDSTAGED_NAME     = MAXALLOWEDSTAGED.getName();
  public static final String ORDERPREFIX_NAME          = ORDERPREFIX.getName();
  public static final String ORDERQUANTITY_NAME        = ORDERQUANTITY.getName();
  public static final String ORDERSTATUS_NAME          = ORDERSTATUS.getName();
  public static final String PHYSICALSTATUS_NAME       = PHYSICALSTATUS.getName();
  public static final String PORECEIVEALL_NAME         = PORECEIVEALL.getName();
  public static final String PRINTER_NAME              = PRINTER.getName();
  public static final String RECOMMENDEDZONE_NAME      = RECOMMENDEDZONE.getName();
  public static final String REINPUTFLAG_NAME          = REINPUTFLAG.getName();
  public static final String REJECTROUTE_NAME          = REJECTROUTE.getName();
  public static final String REPLENISHSOURCES_NAME     = REPLENISHSOURCES.getName();
  public static final String REPRSTATIONNAME_NAME      = REPRSTATIONNAME.getName();
  public static final String PRIORITYCATEGORY_NAME     = PRIORITYCATEGORY.getName();
  public static final String RETRIEVECOMMANDDETAIL_NAME= RETRIEVECOMMANDDETAIL.getName();
  public static final String SIMINTERVAL_NAME          = SIMINTERVAL.getName();
  public static final String SIMULATE_NAME             = SIMULATE.getName();
  public static final String STATIONNAME_NAME          = STATIONNAME.getName();
  public static final String STATIONSCALE_NAME         = STATIONSCALE.getName();
  public static final String STATIONTYPE_NAME          = STATIONTYPE.getName();
  public static final String STATUS_NAME               = STATUS.getName();
  public static final String WAREHOUSE_NAME            = WAREHOUSE.getName();
  public static final String WEIGHT_NAME               = WEIGHT.getName();

  private String sStationName      = "";
  private String sDescription      = "";
  private String sWarehouse        = "";
  private String sLinkRoute        = "";
  private String sDefaultRoute     = "";
  private String sRejectRoute      = "";
  private String sDeviceID         = "";
  private String sStationScale     = "";
  private String sLoadPrefix       = "";
  private String sOrderPrefix      = "";
  private String sPrinter          = "";
  private String sContainerType    = "";
  private String sItem             = "";
  private String sLot              = "";
  private String sReplenishSources = "";
  private String sRecommendedZone  = "";
  private String sAllocationType   = "";
  private int    iStationType;
  private int    iPriorityCategory;
  private int    iRetrieveCommandDetail;
  private int    iReinputFlag;
  private int    iArrivalRequired;
  private int    iMaxAllowedEnroute;
  private int    iMaxAllowedStaged;
  private int    iStatus;
  private int    iBidirectionalStatus;
  private int    iPhysicalStatus;
  private int    iCaptive;
  private int    iConfirmLot;
  private int    iConfirmLocation;
  private int    iConfirmLoad;
  private int    iConfirmItem;
  private int    iConfirmQty;
  private int    iDeleteInventory;
  private int    iStationOrderStatus;
  private int    iHeight;
  private int    iAmountFull;
  private int    iAutoLoadMovementType;
  private int    iAutoOrderType;
  private int    iAllocationEnabled;
  private int    iPoReceiveAll;
  private int    iCCIAllowed;
  private int    iAllowRoundRobin;
  private int    iSimulate;
  private int    iSimInterval;
  private int    iCustomAction;
  private double fWeight;
  private double fOrderQuantity;
  private String msReprStationName;  //representative station, facilitate grouping of stns for status reporting
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public StationData()
  {
    initColumnMap(mpColumnMap, StationEnum.class);
    sdf.applyPattern(SKDCConstants.DateFormatString);
    clear();
  }
/**
 * Clear all of the data to their default values.
 */
  @Override
  public void clear()
  {
    super.clear();
    sStationName      = "";
    sDescription      = "";
    sWarehouse        = "";
    sLinkRoute        = "";
    sDefaultRoute     = "";
    sRejectRoute      = "";
    sDeviceID         = "";
    sStationScale     = "";
    sLoadPrefix       = "";
    sOrderPrefix      = "";
    sPrinter          = "";
    sContainerType    = "";
    sItem             = "";
    sLot              = "";
    sReplenishSources = "";  
    sRecommendedZone  = "";
    sAllocationType   = "";
    msReprStationName = "";
    iStationType         = DBConstants.INPUT;
    iPriorityCategory   = MessageConstants.PLANNED_RETRIEVAL;
    iRetrieveCommandDetail = MessageConstants.UNIT_RETRIEVAL;
    iReinputFlag         = MessageConstants.NO_REINPUT;                  
    iArrivalRequired     = 1;
    iMaxAllowedEnroute   = 5;
    iMaxAllowedStaged    = 5;
    iStatus              = DBConstants.STNOFFLINE;
    iBidirectionalStatus = DBConstants.RETRIEVEMODE;
    iPhysicalStatus      = DBConstants.OFFLINE;
    iCaptive             = DBConstants.NONCAPTIVE;
    iCCIAllowed          = DBConstants.NO;
    iConfirmLot          = DBConstants.NO;
    iConfirmLocation     = DBConstants.NO;
    iConfirmLoad         = DBConstants.NO;
    iConfirmItem         = DBConstants.NO;
    iConfirmQty          = DBConstants.YES;
    iDeleteInventory     = DBConstants.YES;
    iStationOrderStatus  = DBConstants.READY;
    iHeight              = 1;
    iAmountFull          = DBConstants.FULL;
    iAutoLoadMovementType= DBConstants.AUTO_MOVE_OFF;
    iAutoOrderType       = DBConstants.AUTO_ORDER_OFF;
    iAllocationEnabled   = DBConstants.YES;
    iPoReceiveAll        = DBConstants.YES;
    iAllowRoundRobin     = DBConstants.YES;
    iSimulate            = DBConstants.OFF;
    iSimInterval         = 0;
    iCustomAction        = 0;
    fWeight              = 0;
    fOrderQuantity       = 0.0;
  }
  
  /**
   * Create a string for all data values
   */
  @Override
  public String toString()
  {
    String s;

    s = "sStationName: "            + sStationName +
        "\nsWarehouse: "            + sWarehouse +
        "\nsDescription: "          + sDescription +
        "\nsReplenishSources: "     + sReplenishSources +
        "\niStationType: "          + iStationType +
        "\nsLinkRoute: "            + sLinkRoute +
        "\nsDefaultRoute: "         + sDefaultRoute +
        "\nsRejectRoute: "          + sRejectRoute +
        "\nsRecommendedZone: "      + sRecommendedZone +
        "\nsDeviceID: "             + sDeviceID +
        "\nsStationScale: "         + sStationScale +
        "\nsLoadPrefix: "           + sLoadPrefix +
        "\nsOrderPrefix "           + sOrderPrefix +
        "\nsAllocationType "        + sAllocationType +
        "\niArrivalRequired: "      + iArrivalRequired +
        "\niMaxAllowedEnroute: "    + iMaxAllowedEnroute +
        "\niMaxAllowedStaged: "     + iMaxAllowedStaged +
        "\niRetrievalPriority: "    + iPriorityCategory +
        "\niRetrieveCommandDetail: "+ iRetrieveCommandDetail +
        "\niReinputFlag: "          + iReinputFlag +
        "\nsPrinter: "              + sPrinter +
        "\niStatus: "               + iStatus +
        "\niCCIAllowed: "           + iCCIAllowed +
        "\niBidirectionalStatus: "  + iBidirectionalStatus +
        "\niCaptive: "              + iCaptive +
        "\niConfirmLot: "           + iConfirmLot +
        "\niConfirmLocation: "      + iConfirmLocation +
        "\niConfirmLoad: "          + iConfirmLoad +
        "\niConfirmItem: "          + iConfirmItem +
        "\niConfirmQty: "           + iConfirmQty +
        "\nsContainerType: "        + sContainerType +
        "\niStationOrderStatus: "   + iStationOrderStatus +
        "\niDeleteInventory: "      + iDeleteInventory +
        "\nfWeight: "               + fWeight +
        "\niHeight: "               + iHeight +
        "\niAmountFull: "           + iAmountFull +
        "\niAllocationEnabled: "    + iAllocationEnabled +
        "\niAutoLoadMovementType: " + iAutoLoadMovementType +
        "\niAutoOrderType: "        + iAutoOrderType +
        "\niPhysicalStatus: "       + iPhysicalStatus +
        "\niPoReceiveAll: "         + iPoReceiveAll +
        "\niAllowRoundRobin: "      + iAllowRoundRobin +
        "\niSimulate: "             + iSimulate +
        "\niSimInterval: "          + iSimInterval +
        "\nsItem: "                 + sItem +
        "\nsLot: "                  + sLot +
        "\nfPickQuantity:"          + Double.toString(fOrderQuantity) +
        "\niCustomAction: "         + iCustomAction;
    
    s += super.toString();

    return(s);
  }
  
  @Override
  public boolean equals(AbstractSKDCData absST)
  {
    StationData st = (StationData)absST;

    return(st.getStationName().equals(getStationName()) &&
           st.getDeviceID().equals(getDeviceID()));
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
      switch((StationEnum)mpColumnMap.get(isColName))
      {
      case ALLOCATIONENABLED:
      case ALLOWROUNDROBIN:
      case ARRIVALREQUIRED:
      case AUTOLOADMOVEMENTTYPE:
      case AUTOORDERTYPE:
      case BIDIRECTIONALSTATUS:
      case CAPTIVE:
      case STATIONTYPE:
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
  
  public boolean isConventionalStation() 
  {
    if (iStationType == DBConstants.CONSOLIDATION ||
        iStationType == DBConstants.CONVEYOR || 
        iStationType == DBConstants.SHIPPING)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

/**
 * get the station Name
 * @return String stationName
 */
  public String getStationName()
  {
    return(sStationName);
  }

/**
 * Gets the list of replenishment sources.
 * @return comma delimited string of replenishment sources.
 */
  public String getReplenishSources()
  {
    return(sReplenishSources);
  }
  
/**
 * Get the Station Description
 * @return String description
 */
  public String getDescription()
  {
    return sDescription;
  }
/**
 * Get the warehouse of the station
 * @return String warehouse
 */
  public String getWarehouse()
  {
    return sWarehouse;
  }

/**
 * Get the type of station
 * @return int of the type of station
 */
  public int getStationType()
  {
    return iStationType;
  }
  
/**
 * Get the type of station
 * @return int of the type of station
 */
  public int getCCIAllowed()
  {
    return iCCIAllowed;
  }
  
/**
 * Get Auto Load movement type.
 * @return int of Load movement types.
 */
  public int getAutoLoadMovementType()
  {
    return iAutoLoadMovementType;
  }

  public int getAutoOrderType()
  {
    return iAutoOrderType;
  }

  public String getLinkRoute()
  {
    return sLinkRoute;
  }

  public String getDefaultRoute()
  {
    return sDefaultRoute;
  }
  
  public String getRejectRoute()
  {
    return sRejectRoute;
  }

  public String getDeviceID()
  {
    return sDeviceID;
  }

  public String getLoadPrefix()
  {
    return sLoadPrefix;
  }

  public String getOrderPrefix()
  {
    return sOrderPrefix;
  }

  public int getArrivalRequired()
  {
    return iArrivalRequired ;
  }

  public int getMaxAllowedEnroute()
  {
    return iMaxAllowedEnroute;
  }

  public int getMaxAllowedStaged()
  {
    return iMaxAllowedStaged;
  }

  public int getDeleteInventory()
  {
    return iDeleteInventory;
  }

  public String getPrinter()
  {
    return sPrinter;
  }

  public int getRetrievalPriority()
  {
    return iPriorityCategory;
  }

  public int getRetrieveCommandDetail()
  {
    return iRetrieveCommandDetail;
  }

  public int getReinputFlag()
  {
    return iReinputFlag;
  }

  public int getStatus()
  {
    return iStatus ;
  }

  public int getBidirectionalStatus()
  {
    return iBidirectionalStatus ;
  }
  
  public int getPhysicalStatus()
  {
    return iPhysicalStatus ;
  }

  public int getCaptive()
  {
    return iCaptive;
  }

  public int getConfirmLot()
  {
    return iConfirmLot ;
  }

  public int getConfirmLocation()
  {
    return iConfirmLocation ;
  }

  public int getConfirmLoad()
  {
    return iConfirmLoad ;
  }

  public int getConfirmItem()
  {
    return iConfirmItem ;
  }

  public int getConfirmQty()
  {
    return iConfirmQty;
  }
  
  public String getContainerType()
  {
    return sContainerType.toString();

  }

  public int getOrderStatus()
  {
    return iStationOrderStatus;
  }

  public double getWeight()
  {
    return fWeight;
  }

  public int getHeight()
  {
    return iHeight;
  }

  public int getAmountFull()
  {
    return iAmountFull;
  }

  public String getItem()
  {
    return sItem;
  }
  
  public String getLot()
  {
    return sLot;
  }

  public String getAllocationType()
  {
    return(sAllocationType);
  }
  
  public int getAllocationEnabled()
  {
    return(iAllocationEnabled);
  }

  public double getOrderQuantity()
  {
    return(fOrderQuantity);
  }

  public int getPoReceiveAll()
  {
    return(iPoReceiveAll);
  }

  public int getAllowRoundRobin()
  {
    return(iAllowRoundRobin);
  }

  public int getSimulate()
  {
    return iSimulate;
  }
  
  public int getSimInterval()
  {
    return iSimInterval;
  } 
  
  public String getStationScale()
  {
  	return sStationScale;
  }
  
  /**
   * Custom Action - easy way to "extend" Station types, in the absence of a
   * useful station object to extend
   *  
   * @return  custom action translation.
   */
  public int getCustomAction()
  {
    return iCustomAction;
  }
  
  public String getRecommendedZone()
  {
    return sRecommendedZone;
  }
    
  public String getReprStationName()
  {
    return(msReprStationName);
  }
/*===========================================================================
                               SETTER METHODS
  ===========================================================================*/
/**
 * Set the stationName
 * @param isStationName String to set the station name to
 */
  public void setStationName(String isStationName)
  {
    sStationName = checkForNull(isStationName);
    addColumnObject(new ColumnObject(STATIONNAME.getName(), sStationName));
  }

/**
 * Set the description data
 * @param isDescription String to set the description to
 */
  public void setDescription(String isDescription)
  {
    sDescription = checkForNull(isDescription);
    addColumnObject(new ColumnObject(DESCRIPTION.getName(), sDescription));
  }

/**
 * Set the warehouse 
 * @param isWarehouse String to set the warehouse to/
 */
  public void setWarehouse(String isWarehouse)
  {
    sWarehouse = checkForNull(isWarehouse);
    addColumnObject(new ColumnObject(WAREHOUSE.getName(), sWarehouse));
  }
  
  public void setReplenishSources(String isReplenishSources)
  {
    sReplenishSources = isReplenishSources;
    addColumnObject(new ColumnObject(REPLENISHSOURCES.getName(), sReplenishSources));
  }

  public void setRetrievalPriority(int inRetrievalPriority)
  {
    iPriorityCategory = inRetrievalPriority;
    addColumnObject(new ColumnObject(PRIORITYCATEGORY.getName(), iPriorityCategory));
  }

  public void setRetrieveCommandDetail(int inRetrieveCommandDetail)
  {
    iRetrieveCommandDetail = inRetrieveCommandDetail;
    addColumnObject(new ColumnObject(RETRIEVECOMMANDDETAIL.getName(), iRetrieveCommandDetail));
  }

  public void setReinputFlag(int inReinputFlag)
  {
    iReinputFlag = inReinputFlag;
    addColumnObject(new ColumnObject(REINPUTFLAG.getName(), iReinputFlag));
  }

/**
 * Set the Station type u-shaped in , output, PD etc
 * @param inStationType int type to set the station to
 */
  public void setStationType(int inStationType)
  {
    try
    {
      DBTrans.getStringValue(STATIONTYPE.getName(), inStationType);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inStationType = DBConstants.INPUT;
    }
    iStationType = inStationType;
    addColumnObject(new ColumnObject(STATIONTYPE.getName(), iStationType));
  }

  public void setAutoLoadMovementType(int inAutoLoadMovementType)
  {
    iAutoLoadMovementType = inAutoLoadMovementType;
    addColumnObject(new ColumnObject(AUTOLOADMOVEMENTTYPE.getName(), iAutoLoadMovementType));
  }

  public void setAutoOrderType(int inAutoOrderType)
  {
    iAutoOrderType = inAutoOrderType;
    addColumnObject(new ColumnObject(AUTOORDERTYPE.getName(), iAutoOrderType));
  }

  public void setAllowRoundRobin(int inAllowRoundRobin)
  {
    iAllowRoundRobin = inAllowRoundRobin;
    addColumnObject(new ColumnObject(ALLOWROUNDROBIN.getName(), iAllowRoundRobin));
  }
  
  public void setCCIAllowed(int inCCIAllowed)
  {
    iCCIAllowed = inCCIAllowed;
    addColumnObject(new ColumnObject(CCIALLOWED.getName(), iCCIAllowed));
  }
  
  public void setLinkRoute(String isLinkRoute)
  {
    sLinkRoute = checkForNull(isLinkRoute);
    addColumnObject(new ColumnObject(LINKROUTE.getName(), sLinkRoute));
  }

  public void setDefaultRoute(String isDefaultRoute)
  {
    sDefaultRoute = checkForNull(isDefaultRoute);
    addColumnObject(new ColumnObject(DEFAULTROUTE.getName(), sDefaultRoute));
  }

  public void setRejectRoute(String isRejectRoute)
  {
    sRejectRoute = checkForNull(isRejectRoute);
    addColumnObject(new ColumnObject(REJECTROUTE.getName(), sRejectRoute));
  }

  public void setDeviceID(String isDeviceID)
  {
    sDeviceID = checkForNull(isDeviceID);
    addColumnObject(new ColumnObject(DEVICEID.getName(), sDeviceID));
  }

  public void setLoadPrefix(String isLoadPrefix)
  {
    sLoadPrefix = checkForNull(isLoadPrefix);
    addColumnObject(new ColumnObject(LOADPREFIX.getName(), sLoadPrefix));
  }

  public void setOrderPrefix(String isOrderPrefix)
  {
    sOrderPrefix = checkForNull(isOrderPrefix);
    addColumnObject(new ColumnObject(ORDERPREFIX.getName(), sOrderPrefix));
  }

  public void setArrivalRequired(int inArrivalRequired)
  {
    iArrivalRequired = inArrivalRequired;
    addColumnObject(new ColumnObject(ARRIVALREQUIRED.getName(), iArrivalRequired));
  }

  public void setMaxAllowedEnroute(int inMaxAllowedEnroute)
  {
    iMaxAllowedEnroute = inMaxAllowedEnroute;
    addColumnObject(new ColumnObject(MAXALLOWEDENROUTE.getName(), iMaxAllowedEnroute));
  }

  public void setMaxAllowedStaged(int inMaxAllowedStaged)
  {
    iMaxAllowedStaged = inMaxAllowedStaged;
    addColumnObject(new ColumnObject(MAXALLOWEDSTAGED.getName(), iMaxAllowedStaged));
  }

  public void setReprStationName(String isReprStationName)
  {
    msReprStationName = isReprStationName;
    addColumnObject(new ColumnObject(REPRSTATIONNAME.getName(), msReprStationName));
  }

  public void setDeleteInventory(int inDeleteInventory)
  {
    iDeleteInventory = inDeleteInventory;
    addColumnObject(new ColumnObject(DELETEINVENTORY.getName(), iDeleteInventory));
  }

  public void setPrinter(String isPrinter)
  {
    sPrinter = checkForNull(isPrinter);
    addColumnObject(new ColumnObject(PRINTER.getName(), sPrinter));
  }

  public void setStatus(int inStatus)
  {
    iStatus = inStatus;
    addColumnObject(new ColumnObject(STATUS.getName(), iStatus));
  }

  public void setBidirectionalStatus(int inBidirectionalStatus)
  {
    iBidirectionalStatus = inBidirectionalStatus;
    addColumnObject(new ColumnObject(BIDIRECTIONALSTATUS.getName(), iBidirectionalStatus));
  }

  public void setPhysicalStatus(int inPhysicalStatus)
  {
    iPhysicalStatus = inPhysicalStatus;
    addColumnObject(new ColumnObject(PHYSICALSTATUS.getName(), iPhysicalStatus));
  }

  public void setCaptive(int inCaptive)
  {
    iCaptive = inCaptive;
    addColumnObject(new ColumnObject(CAPTIVE.getName(), iCaptive));
  }

  public void setConfirmLot(int inConfirmLot)
  {
    iConfirmLot = inConfirmLot;
    addColumnObject(new ColumnObject(CONFIRMLOT.getName(), iConfirmLot));
  }

  public void setConfirmLocation(int inConfirmLocation)
  {
    iConfirmLocation = inConfirmLocation;
    addColumnObject(new ColumnObject(CONFIRMLOCATION.getName(), iConfirmLocation));
  }

  public void setConfirmLoad(int inConfirmLoad)
  {
    iConfirmLoad = inConfirmLoad;
    addColumnObject(new ColumnObject(CONFIRMLOAD.getName(), iConfirmLoad));
  }

  public void setConfirmItem(int inConfirmItem)
  {
    iConfirmItem = inConfirmItem;
    addColumnObject(new ColumnObject(CONFIRMITEM.getName(), iConfirmItem));
  }

  public void setConfirmQty(int inConfirmQty)
  {
    iConfirmQty = inConfirmQty;
    addColumnObject(new ColumnObject(CONFIRMQTY.getName(), iConfirmQty));
  }

  public void setContainerType(String isContainerType)
  {
    sContainerType = checkForNull(isContainerType);
    addColumnObject(new ColumnObject(CONTAINERTYPE.getName(), sContainerType));
  }

  public void setOrderStatus(int inStationOrderStatus)
  {
    iStationOrderStatus = inStationOrderStatus;
    addColumnObject(new ColumnObject(ORDERSTATUS.getName(), iStationOrderStatus));
  }

  public void setWeight(double idWeight)
  {
    fWeight = idWeight;
    addColumnObject(new ColumnObject(WEIGHT.getName(), fWeight));
  }

  public void setHeight(int inHeight)
  {
    iHeight = inHeight;
    addColumnObject(new ColumnObject(HEIGHT.getName(), iHeight));
  }

  public void setAmountFull(int inAmountFull)
  {
    iAmountFull = inAmountFull;
    addColumnObject(new ColumnObject(AMOUNTFULL.getName(), iAmountFull));
  }

  public void setItem(String isItem)
  {
    sItem = checkForNull(isItem);
    addColumnObject(new ColumnObject(ITEM.getName(), sItem));
  }

  public void setLot(String isLot)
  {
    sLot = checkForNull(isLot);
    addColumnObject(new ColumnObject(LOT.getName(), sLot));
  }
  
  public void setAllocationType(String isAllocationType)
  {
    sAllocationType = isAllocationType;
    addColumnObject(new ColumnObject(ALLOCATIONTYPE.getName(), sAllocationType));
  }

  public void setAllocationEnabled(int inAllocationEnabled)
  {
    try
    {
      DBTrans.getStringValue(ALLOCATIONENABLED.getName(), inAllocationEnabled);
    }
    catch(NoSuchFieldException e)
    {
      inAllocationEnabled = DBConstants.YES;
    }
    iAllocationEnabled = inAllocationEnabled;
    addColumnObject(new ColumnObject(ALLOCATIONENABLED.getName(), iAllocationEnabled));
  }

  public void setOrderQuantity(double ifOrderQuantity)
  {
    fOrderQuantity = ifOrderQuantity;
    addColumnObject(new ColumnObject(ORDERQUANTITY.getName(), fOrderQuantity));
  }

  public void setPoReceiveAll(int inPoReceiveAll)
  {
    try
    {
      DBTrans.getStringValue(PORECEIVEALL.getName(), inPoReceiveAll);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inPoReceiveAll = DBConstants.YES;
    }
    iPoReceiveAll = inPoReceiveAll;
    addColumnObject(new ColumnObject(PORECEIVEALL.getName(), iPoReceiveAll));
  }

  public void setSimulate(int inSimulate)
  {
    iSimulate = inSimulate;
    addColumnObject(new ColumnObject(SIMULATE.getName(), iSimulate));
  }
  
  public void setSimInterval(int inSimInterval)
  {
    iSimInterval = inSimInterval;
    addColumnObject(new ColumnObject(SIMINTERVAL.getName(), iSimInterval));
  }

  public void setCustomAction(int inCustomAction)
  {
    iCustomAction = inCustomAction;
    addColumnObject(new ColumnObject(CUSTOMACTION.getName(), iCustomAction));
  }

  public void setRecommendedZone(String isRecZone)
  {
    sRecommendedZone = checkForNull(isRecZone);
    addColumnObject(new ColumnObject(RECOMMENDEDZONE.getName(), sRecommendedZone));
  }
  
  public void setStationScale(String isStationScale)
  {
  	sStationScale = isStationScale;
  	addColumnObject(new ColumnObject(STATIONSCALE.getName(), sStationScale));
  }

  /**
   * {@inheritDoc}
   * @param isColName the column name.
   * @param ipColValue the column value.
   * @return {@inheritDoc}
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return(super.setField(isColName, ipColValue));
    }

    switch((StationEnum)vpEnum)
    {
      case ITEM:
        setItem((String)ipColValue);
        break;

      case LOT:
        setLot((String)ipColValue);
        break;

      case STATIONNAME:
        setStationName((String)ipColValue);
        break;

      case DESCRIPTION:
        setDescription((String)ipColValue);
        break;

      case REPLENISHSOURCES:
        setReplenishSources((String)ipColValue);
        break;

      case WAREHOUSE:
        setWarehouse((String)ipColValue);
        break;
                                   
      case DEFAULTROUTE:              
        setDefaultRoute((String)ipColValue);
        break;

      case LINKROUTE:
        setLinkRoute((String)ipColValue);
        break;

      case REJECTROUTE:
        setRejectRoute((String)ipColValue);
        break;

      case DEVICEID:
        setDeviceID((String)ipColValue);
        break;

      case LOADPREFIX:
        setLoadPrefix((String)ipColValue);
        break;

      case ORDERPREFIX:
        setOrderPrefix((String)ipColValue);
        break;

      case PRINTER:
        setPrinter((String)ipColValue);
        break;

      case CONTAINERTYPE:
        setContainerType((String)ipColValue);
        break;

      case STATIONSCALE:
      	setStationScale((String)ipColValue);
      	break;

      case ALLOCATIONTYPE:
        setAllocationType((String)ipColValue);
        break;

      case RECOMMENDEDZONE:
        setRecommendedZone((String)ipColValue);
        break;
                                  // Set the Group Station.
      case REPRSTATIONNAME:
        setReprStationName((String)ipColValue);
        break;

      case STATIONTYPE:
        setStationType((Integer)ipColValue);
        break;
        
      case ARRIVALREQUIRED:
        setArrivalRequired((Integer)ipColValue);
        break;

      case MAXALLOWEDENROUTE:
        setMaxAllowedEnroute((Integer)ipColValue);
        break;

      case MAXALLOWEDSTAGED:
        setMaxAllowedStaged((Integer)ipColValue);
        break;

      case STATUS:
        setStatus((Integer)ipColValue);
        break;

      case BIDIRECTIONALSTATUS:
        setBidirectionalStatus((Integer)ipColValue);
        break;

      case CAPTIVE:
        setCaptive((Integer)ipColValue);
        break;

      case CONFIRMLOT:
        setConfirmLot((Integer)ipColValue);
        break;

      case CONFIRMLOCATION:
        setConfirmLocation((Integer)ipColValue);
        break;

      case CONFIRMLOAD:
        setConfirmLoad((Integer)ipColValue);
        break;

      case CONFIRMITEM:
        setConfirmItem((Integer)ipColValue);
        break;

      case CONFIRMQTY:
        setConfirmQty((Integer)ipColValue);
        break;

      case ORDERSTATUS:
        setOrderStatus((Integer)ipColValue);
        break;

      case WEIGHT:
        setWeight((Double)ipColValue);
        break;

      case DELETEINVENTORY:
        setDeleteInventory((Integer)ipColValue);
        break;

      case HEIGHT:
        setHeight((Integer)ipColValue);
        break;

      case AMOUNTFULL:
        setAmountFull((Integer)ipColValue);
        break;

      case AUTOLOADMOVEMENTTYPE:
        setAutoLoadMovementType((Integer)ipColValue);
        break;

      case AUTOORDERTYPE:
        setAutoOrderType((Integer)ipColValue);
        break;

      case PHYSICALSTATUS:
        setPhysicalStatus((Integer)ipColValue);
        break;

      case ALLOCATIONENABLED:
        setAllocationEnabled((Integer)ipColValue);
        break;

      case ORDERQUANTITY:
        setOrderQuantity((Double)ipColValue);
        break;

      case PORECEIVEALL:
        setPoReceiveAll((Integer)ipColValue);
        break;

      case CCIALLOWED:
        setCCIAllowed((Integer)ipColValue);
        break;

      case ALLOWROUNDROBIN:
        setAllowRoundRobin((Integer)ipColValue);
        break;

      case SIMULATE:
        setSimulate((Integer)ipColValue);
        break;

      case SIMINTERVAL:
        setSimInterval((Integer)ipColValue);
        break;

      case CUSTOMACTION:
        setCustomAction((Integer)ipColValue);
        break;

      case PRIORITYCATEGORY:
        setRetrievalPriority((Integer)ipColValue);
        break;

      case RETRIEVECOMMANDDETAIL:
        setRetrieveCommandDetail((Integer)ipColValue);
        break;

      case REINPUTFLAG:
        setReinputFlag((Integer)ipColValue);
    }
    return(0);
  }
}
