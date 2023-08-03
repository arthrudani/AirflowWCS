package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.LoadAndLLIEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.jdbc.AmountFullTransMapper;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Title:  Class to handle LoadData Object.
 *   Description : Handles all data for load
 * @author       REA
 * @author       A.D.  Converted to use ColumnObjects for building SQL.
 * @version      1.0
 * @since       04-Jan-02
 */
public class LoadDataAndLLIData extends AbstractSKDCData
{
  public static final String DEFAULT_POSITION_VALUE = "000";
  
  public static final String ADDRESS_NAME           = ADDRESS.getName();
  public static final String AMOUNTFULL_NAME        = AMOUNTFULL.getName();
  public static final String BCRDATA_NAME           = BCRDATA.getName();
  public static final String CONTAINERTYPE_NAME     = CONTAINERTYPE.getName();
  public static final String DEVICEID_NAME          = DEVICEID.getName();
  public static final String FINALADDRESS_NAME      = FINALADDRESS.getName();
  public static final String FINALWAREHOUSE_NAME    = FINALWAREHOUSE.getName();
  public static final String GROUPNO_NAME           = GROUPNO.getName();
  public static final String HEIGHT_NAME            = HEIGHT.getName();
  public static final String LENGTH_NAME            = LENGTH.getName();
  public static final String LOADID_NAME            = LOADID.getName();
  public static final String LOADMESSAGE_NAME       = LOADMESSAGE.getName();
  public static final String LOADMOVESTATUS_NAME    = LOADMOVESTATUS.getName();
  public static final String LOADPRESENCECHECK_NAME = LOADPRESENCECHECK.getName();
  public static final String MCKEY_NAME             = MCKEY.getName();
  public static final String MOVEDATE_NAME          = MOVEDATE.getName();
  public static final String NEXTADDRESS_NAME       = NEXTADDRESS.getName();
  public static final String NEXTSHELFPOSITION_NAME = NEXTSHELFPOSITION.getName();
  public static final String NEXTWAREHOUSE_NAME     = NEXTWAREHOUSE.getName();
  public static final String PARENTLOAD_NAME        = PARENTLOAD.getName();
  public static final String RECOMMENDEDZONE_NAME   = RECOMMENDEDZONE.getName();
  public static final String ROUTEID_NAME           = ROUTEID.getName();
  public static final String SHELFPOSITION_NAME     = SHELFPOSITION.getName();
  public static final String WAREHOUSE_NAME         = WAREHOUSE.getName();
  public static final String WEIGHT_NAME            = WEIGHT.getName();
  public static final String WIDTH_NAME             = WIDTH.getName();
  public static final String FINAL_SORT_LOC_ID_NAME = FINALSORTLOCATION.getName();//US31512 
  public static final String CURRENT_ADDRESS_NAME   = CURRENTADDRESS.getName();
  
  public static final String LOT_NAME 				= LOT.getName();
  public static final String EXPECTED_DATE_NAME 	= EXPECTEDDATE.getName();
  public static final String EXPECTED_RECEIPT_NAME 	= EXPECTEDRECEIPT.getName();
  public static final String EXPIRATION_DATE_NAME 	= EXPIRATIONDATE.getName();
  public static final String POSITION_ID_NAME 		= POSITIONID.getName();
  public static final String HOLD_REASONE_NAME 		= HOLDREASONE.getName();
  public static final String ORDER_ID_NAME 			= ORDERID.getName();
  public static final String ORDER_LOT_NAME 		= ORDERLOT.getName();
  public static final String LINE_ID_NAME 			= LINEID.getName();
  public static final String LAST_CCI_DATE_NAME 	= LASTCCIDATE.getName();
  public static final String AGING_DATE_NAME 		= AGINGDATE.getName();
  public static final String CURRENT_QUANTITY_NAME 	= CURRENTQUANTITY.getName();
  public static final String ALLOCATED_QUANTITY_NAME = ALLOCATEDQUANTITY.getName();
  public static final String PRIORITY_ALLOCATION_NAME = PRIORITYALLOCATION.getName();
  public static final String HOLD_TYPE_NAME 		= HOLDTYPE.getName();
  public static final String GLOBAL_ID_NAME 		= GLOBALID.getName();
  

// -------------------Load Table data -----------------------------
  private String sLoadID            = "";
  private String sParentLoadID      = "";
  private String sMCKey             = "";
  private String sContainerType     = "";
  private String sWarehouse         = "";
  private String sAddress           = "";
  private String sNextWarehouse     = "";
  private String sNextAddress       = "";
  private String sNextShelfPosition = "";
  private String sFinalWarehouse    = "";
  private String sFinalAddress      = "";
  private String sRouteID           = "";
  private String sRecommendedZone   = "";
  private String sShelfPosition     = "";
  private String sLoadMessage       = "";
  private String sDeviceID          = "";
  private String sBCRData           = "";
  private Date   dMoveDate          = new Date();
  private int    iLoadMoveStatus    = DBConstants.NOMOVE;
  private int    iHeight            = 1;
  private int    iLength            = 1;
  private int    iWidth             = 1;
  private int    iAmountFull        = DBConstants.EMPTY;
  private double fWeight            = 0;
  private int    iLoadPresenceCheck = DBConstants.YES;
  private int    iGroupNo           = 0;
  private String sFinalSortLocationID = "";
  private String sCurrentAddress    = "";
  
  private String sLot				=""; 
  private String sPositionId		=""; 
  private String sHoldReasone		=""; 
  private String sOrderId			=""; 
  private String sExpectedReceipt	=""; 
  private String sOrderLot			=""; 
  private String sGlobalId			="";
  private String sLineId			=""; 
  private Date dLastCCIDate 		= new Date(); 
  private Date dAgingDate 			= new Date(); 
  private Date dExpirationDate 		= new Date(); 
  private Date dExpectedDate		= new Date(); 
  private double fCurrentQuantity 	= 0; 
  private double fAllocatedQuantity	= 0;
  private double iPriorityAllocation= 0;
  private int iHoldType				= 0;
	
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();
  private static Map<String, AmountFullTransMapper> mpPartialQtyMap;

  public LoadDataAndLLIData()
  {
    sdf.applyPattern(SKDCConstants.DateFormatString);
    clear();
    initColumnMap(mpColumnMap, LoadAndLLIEnum.class);
  }

  @Override
  public void clear()
  {
    super.clear();
    sLoadID            = "";
    sParentLoadID      = "";
    sMCKey             = "";
    sContainerType     = "";
    sWarehouse         = "";
    sAddress           = "";
    sNextWarehouse     = "";
    sNextAddress       = "";
    sNextShelfPosition = DEFAULT_POSITION_VALUE;
    sFinalWarehouse    = "";
    sFinalAddress      = "";
    sRouteID           = "";
    sRecommendedZone   = "";
    sShelfPosition     = DEFAULT_POSITION_VALUE;
    sLoadMessage       = "";
    sDeviceID          = "";
    sBCRData           = "";
    dMoveDate          = new Date();
    iLoadMoveStatus    = DBConstants.NOMOVE;
    iHeight            = 1;
    iLength            = 1;
    iWidth             = 1;
    iAmountFull        = DBConstants.EMPTY;
    fWeight            = 0;
    iLoadPresenceCheck = DBConstants.YES;
    iGroupNo           = 0;
    sFinalSortLocationID = "";
    sCurrentAddress      = "";
    
    
    sLot				= ""; 
    sPositionId			= ""; 
    sHoldReasone		= ""; 
    sOrderId			= ""; 
    sExpectedReceipt	= ""; 
    sOrderLot			= ""; 
    sGlobalId			= "";
    sLineId				= ""; 
    dLastCCIDate 		= new Date(); 
    dAgingDate 			= new Date(); 
    dExpirationDate 	= new Date(); 
    dExpectedDate		= new Date(); 
    fCurrentQuantity 	= 0; 
    fAllocatedQuantity	= 0;
    iPriorityAllocation = 0;
    iHoldType			= 0;
  }



  @Override
public String toString() {
	return "LoadDataAndLLIData [sLoadID=" + sLoadID + ", sParentLoadID=" + sParentLoadID + ", sMCKey=" + sMCKey
			+ ", sContainerType=" + sContainerType + ", sWarehouse=" + sWarehouse + ", sAddress=" + sAddress
			+ ", sNextWarehouse=" + sNextWarehouse + ", sNextAddress=" + sNextAddress + ", sNextShelfPosition="
			+ sNextShelfPosition + ", sFinalWarehouse=" + sFinalWarehouse + ", sFinalAddress=" + sFinalAddress
			+ ", sRouteID=" + sRouteID + ", sRecommendedZone=" + sRecommendedZone + ", sShelfPosition=" + sShelfPosition
			+ ", sLoadMessage=" + sLoadMessage + ", sDeviceID=" + sDeviceID + ", sBCRData=" + sBCRData + ", dMoveDate="
			+ dMoveDate + ", iLoadMoveStatus=" + iLoadMoveStatus + ", iHeight=" + iHeight + ", iLength=" + iLength
			+ ", iWidth=" + iWidth + ", iAmountFull=" + iAmountFull + ", fWeight=" + fWeight + ", iLoadPresenceCheck="
			+ iLoadPresenceCheck + ", iGroupNo=" + iGroupNo + ", sFinalSortLocationID=" + sFinalSortLocationID
			+ ", sCurrentAddress=" + sCurrentAddress + ", sLot=" + sLot + ", sPositionId=" + sPositionId
			+ ", sHoldReasone=" + sHoldReasone + ", sOrderId=" + sOrderId + ", sExpectedReceipt=" + sExpectedReceipt
			+ ", sOrderLot=" + sOrderLot + ", sGlobalId=" + sGlobalId + ", sLineId=" + sLineId + ", dLastCCIDate="
			+ dLastCCIDate + ", dAgingDate=" + dAgingDate + ", dExpirationDate=" + dExpirationDate + ", dExpectedDate="
			+ dExpectedDate + ", fCurrentQuantity=" + fCurrentQuantity + ", fAllocatedQuantity=" + fAllocatedQuantity
			+ ", iPriorityAllocation=" + iPriorityAllocation + ", iHoldType=" + iHoldType + "]";
}

/**
   *  Method to perform clone of <code>LoadData</code>.
   *
   *  @return copy of <code>LoadData</code>
   */
  @Override
  public LoadDataAndLLIData clone()
  {
    LoadDataAndLLIData vpClonedData = (LoadDataAndLLIData)super.clone();
    vpClonedData.dMoveDate = (Date)dMoveDate.clone();

    return vpClonedData;
  }

  @Override
  public boolean equals(AbstractSKDCData absLD)
  {
    LoadDataAndLLIData ld = (LoadDataAndLLIData)absLD;
    return(ld.getLoadID().equals(this.getLoadID()));
  }

/*---------------------------------------------------------------------------
                              Set Methods.
  ---------------------------------------------------------------------------*/
  public void setLoadID(String isLoadID)
  {
    sLoadID = checkForNull(isLoadID);
    addColumnObject(new ColumnObject(LOADID_NAME, isLoadID));
  }

  public void setParentLoadID(String isParentLoad)
  {
    sParentLoadID = checkForNull(isParentLoad);
    addColumnObject(new ColumnObject(PARENTLOAD_NAME, isParentLoad));
  }

  public void setMCKey(String isMCKey)
  {
    sMCKey = checkForNull(isMCKey);
    addColumnObject(new ColumnObject(MCKEY_NAME, isMCKey));
  }

  public void setContainerType(String isContainer)
  {
    sContainerType = checkForNull(isContainer);
    addColumnObject(new ColumnObject(CONTAINERTYPE_NAME, isContainer));
  }

  public void setWarehouse(String warehouse)
  {
    sWarehouse = checkForNull(warehouse);
    addColumnObject(new ColumnObject(WAREHOUSE_NAME, warehouse));
  }

  public void setAddress(String address)
  {
    sAddress = checkForNull(address);
    addColumnObject(new ColumnObject(ADDRESS_NAME, address));
  }

  public void setShelfPosition(String isShelfPos)
  {
    String vsPosition = checkForNull(isShelfPos);

    if (vsPosition.isEmpty())
      sShelfPosition = DEFAULT_POSITION_VALUE;
    else if (vsPosition.length() < 3)
      sShelfPosition = SKDCUtility.preZeroFill(vsPosition, 3);
    else
      sShelfPosition = vsPosition;

    addColumnObject(new ColumnObject(SHELFPOSITION_NAME, sShelfPosition));
  }

  public void setNextWarehouse(String warehouse)
  {
    sNextWarehouse = checkForNull(warehouse);
    addColumnObject(new ColumnObject(NEXTWAREHOUSE_NAME, warehouse));
  }

  public void setNextAddress(String address)
  {
    sNextAddress = checkForNull(address);
    addColumnObject(new ColumnObject(NEXTADDRESS_NAME, address));
  }

  public void setNextShelfPosition(String isNextShelfPos)
  {
    String vsNextPos = checkForNull(isNextShelfPos);
    if (vsNextPos.isEmpty())
      sNextShelfPosition = DEFAULT_POSITION_VALUE;
    else if (vsNextPos.length() < 3)
      sNextShelfPosition = SKDCUtility.preZeroFill(vsNextPos, 3);
    else
      sNextShelfPosition = vsNextPos;

    addColumnObject(new ColumnObject(NEXTSHELFPOSITION_NAME, sNextShelfPosition));
  }

  public void setFinalWarehouse(String warehouse)
  {
    sFinalWarehouse = checkForNull(warehouse);
    addColumnObject(new ColumnObject(FINALWAREHOUSE_NAME, warehouse));
  }

  public void setFinalAddress(String address)
  {
    sFinalAddress = checkForNull(address);
    addColumnObject(new ColumnObject(FINALADDRESS_NAME, sFinalAddress));
  }

  public void setBCRData(String BCRData)
  {
    sBCRData = checkForNull(BCRData);
    addColumnObject(new ColumnObject(BCRDATA_NAME, BCRData));
  }

  public void setRouteID(String route)
  {
    sRouteID = checkForNull(route);
    addColumnObject(new ColumnObject(ROUTEID_NAME, route));
  }

  public void setRecommendedZone(String isRecZone)
  {
    sRecommendedZone = checkForNull(isRecZone);
    addColumnObject(new ColumnObject(RECOMMENDEDZONE_NAME, isRecZone));
  }

  public void setLoadMessage(String loadMessage)
  {
    sLoadMessage = checkForNull(loadMessage);
    addColumnObject(new ColumnObject(LOADMESSAGE_NAME, sLoadMessage));
  }

  public void setDeviceID(String scheduleDevice)
  {
    sDeviceID = checkForNull(scheduleDevice);
    addColumnObject(new ColumnObject(DEVICEID_NAME, sDeviceID));
  }

  public void setLoadMoveStatus(int loadMoveStatus)
  {
    iLoadMoveStatus = loadMoveStatus;
    addColumnObject(new ColumnObject(LOADMOVESTATUS_NAME, loadMoveStatus));
  }

  public void setHeight(int height)
  {
    iHeight = height;
    addColumnObject(new ColumnObject(HEIGHT_NAME, iHeight));
  }
  
  public void setLength(int length)
  {
    iLength = length;
    addColumnObject(new ColumnObject(LENGTH_NAME, iLength));
  }
  
  public void setWidth(int width)
  {
    iWidth = width;
    addColumnObject(new ColumnObject(WIDTH_NAME, iWidth));
  }
  
  public void setGroupNo(int inGroupNo)
  {
    iGroupNo = inGroupNo;
    addColumnObject(new ColumnObject(GROUPNO_NAME, iGroupNo));
  }

  public void setAmountFull(int amountFull)
  {
    iAmountFull = amountFull;
    addColumnObject(new ColumnObject(AMOUNTFULL_NAME, iAmountFull));
  }

  public void setWeight(double weight)
  {
    fWeight = weight;
    addColumnObject(new ColumnObject(WEIGHT_NAME, fWeight));
  }

  public void setMoveDate(Date moveDate)
  {
    dMoveDate = moveDate;
    addColumnObject(new ColumnObject(MOVEDATE_NAME, moveDate));
  }

  public void setMoveDate()
  {
    setMoveDate(new Date());
  }

  public void setLoadPresenceCheck(int loadPresenceCheck)
  {
    iLoadPresenceCheck = loadPresenceCheck;
    addColumnObject(new ColumnObject(LOADPRESENCECHECK_NAME, iLoadPresenceCheck));
  }
  	//US31512 - Construct and send move order message - Adding final sort location id
	public void setFinalSortLocationID(String finalSortLocationId) {
		sFinalSortLocationID = checkForNull(finalSortLocationId);
		addColumnObject(new ColumnObject(FINAL_SORT_LOC_ID_NAME, sFinalSortLocationID));
	}
	
	public void setCurrentAddress(String currentAddress) {
		sCurrentAddress = checkForNull(currentAddress);
		addColumnObject(new ColumnObject(CURRENT_ADDRESS_NAME, sCurrentAddress));
	}
	
	
	
	public void setLot(String lot) {
		sLot = checkForNull(lot);
		addColumnObject(new ColumnObject(LOT_NAME, sLot));
	}
	
	public void setPositionId(String positionId) {
		sPositionId = checkForNull(positionId);
		addColumnObject(new ColumnObject(POSITION_ID_NAME, sPositionId));
	}
	
	public void setHoldReasone(String holdReasone) {
		sHoldReasone = checkForNull(holdReasone);
		addColumnObject(new ColumnObject(HOLD_REASONE_NAME, sHoldReasone));
	}
	
	public void setOrderId(String orderId) {
		sOrderId = checkForNull(orderId);
		addColumnObject(new ColumnObject(ORDER_ID_NAME, sOrderId));
	}
	public void setExpectedReceipt(String expectedReceipt) {
		sExpectedReceipt = checkForNull(expectedReceipt);
		addColumnObject(new ColumnObject(EXPECTED_RECEIPT_NAME, sExpectedReceipt));
	}
	public void setOrderLot(String orderLot) {
		sOrderLot = checkForNull(orderLot);
		addColumnObject(new ColumnObject(ORDER_LOT_NAME, sOrderLot));
	}
	public void setGlobalId(String globalId) {
		sGlobalId = checkForNull(globalId);
		addColumnObject(new ColumnObject(GLOBAL_ID_NAME, sGlobalId));
	}
	public void setLineId(String lineId) {
		sLineId = checkForNull(lineId);
		addColumnObject(new ColumnObject(LINE_ID_NAME, sLineId));
	}
	public void setLastCCIDate(Date lastCCIDate) {
		dLastCCIDate = lastCCIDate;
		addColumnObject(new ColumnObject(LAST_CCI_DATE_NAME, dLastCCIDate));
	}
	public void setAgingDate(Date agingDate) {
		dAgingDate = agingDate;
		addColumnObject(new ColumnObject(AGING_DATE_NAME, dAgingDate));
	}
	public void setExpirationDate(Date expirationDate) {
		dExpirationDate = expirationDate;
		addColumnObject(new ColumnObject(EXPIRATION_DATE_NAME, dExpirationDate));
	}
	public void setExpectedDate(Date expectedDate) {
		dExpectedDate = expectedDate;
		addColumnObject(new ColumnObject(EXPECTED_DATE_NAME, dExpectedDate));
	}
	public void setCurrentQuantity(double currentQuantity) {
		fCurrentQuantity = currentQuantity;
		addColumnObject(new ColumnObject(CURRENT_QUANTITY_NAME, fCurrentQuantity));
	}
	public void setAllocatedQuantity(double allocatedQuantity) {
		fAllocatedQuantity = allocatedQuantity;
		addColumnObject(new ColumnObject(ALLOCATED_QUANTITY_NAME, fAllocatedQuantity));
	}
	public void setPriorityAllocation(double priorityAllocation) {
		iPriorityAllocation = priorityAllocation;
		addColumnObject(new ColumnObject(PRIORITY_ALLOCATION_NAME, iPriorityAllocation));
	}
	public void setHoldType(int holdType) {
		iHoldType = holdType;
		addColumnObject(new ColumnObject(HOLD_TYPE_NAME, iHoldType));
	}
/*---------------------------------------------------------------------------
                              Get Methods.
  ---------------------------------------------------------------------------*/
  public String getLoadID()
  {
    return sLoadID;
  }
  
  public String getParentLoadID()
  {
    return sParentLoadID;
  }

  public String getMCKey()
  {
    return sMCKey;
  }

  public String getContainerType()
  {
    return sContainerType;
  }

  public String getWarehouse()
  {
    return sWarehouse;
  }

  public String getAddress()
  {
    return sAddress;
  }

  public String getShelfPosition()
  {
    return sShelfPosition;
  }

  public String getNextWarehouse()
  {
    return sNextWarehouse;
  }

  public String getNextAddress()
  {
    return sNextAddress;
  }

  public String getNextShelfPosition()
  {
    return sNextShelfPosition;
  }

  public String getFinalWarehouse()
  {
    return sFinalWarehouse;
  }

  public String getFinalAddress()
  {
    return sFinalAddress;
  }

  public String getBCRData()
  {
    return sBCRData;
  }

  public String getRouteID()
  {
    return sRouteID;
  }

  public int getLoadMoveStatus()
  {
    return iLoadMoveStatus ;
  }

  public int getHeight()
  {
    return iHeight;
  }

  public int getLength()
  {
    return iLength;
  }

  public int getWidth()
  {
    return iWidth;
  }

  public int getAmountFull()
  {
    return iAmountFull;
  }

  public String getRecommendedZone()
  {
    return sRecommendedZone.toString();
  }

  public double getWeight()
  {
    return fWeight;
  }

  public Date getMoveDate()
  {
    return dMoveDate;
  }

  public int getLoadPresenceCheck()
  {
    return iLoadPresenceCheck;
  }

  public String getLoadMessage()
  {
    return sLoadMessage.toString();
  }

  public String getDeviceID()
  {
    return sDeviceID;
  }
  
  public int getGroupNo()
  {
    return iGroupNo;
  }
  
public String getsLot() {
	return sLot;
}

public String getsPositionId() {
	return sPositionId;
}

public String getsHoldReasone() {
	return sHoldReasone;
}

public String getsOrderId() {
	return sOrderId;
}

public String getsExpectedReceipt() {
	return sExpectedReceipt;
}

public String getsOrderLot() {
	return sOrderLot;
}


public String getsGlobalId() {
	return sGlobalId;
}

public String getsLineId() {
	return sLineId;
}

public Date getdLastCCIDate() {
	return dLastCCIDate;
}

public Date getdAgingDate() {
	return dAgingDate;
}

public Date getdExpirationDate() {
	return dExpirationDate;
}


public Date getdExpectedDate() {
	return dExpectedDate;
}


public double getfCurrentQuantity() {
	return fCurrentQuantity;
}

public double getfAllocatedQuantity() {
	return fAllocatedQuantity;
}


public double getiPriorityAllocation() {
	return iPriorityAllocation;
}

public int getiHoldType() {
	return iHoldType;
}



/**
  * Method exists because the amount full translation value may vary by project.
  * This gives us a way to minimize the impact of this.
  * @return
  */
  public int getAmountFullnessTrans()
  {
    return(DBConstants.FULL);
  }
  
  public String getFinalSortLocationID() {
	return sFinalSortLocationID;
  }
  
  public String getCurrentAddress() {
	  return sCurrentAddress;
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

    switch((LoadAndLLIEnum)vpEnum)
    {
      case LOADID:
        setLoadID((String)ipColValue);
        break;

      case PARENTLOAD:
        setParentLoadID((String)ipColValue);
        break;

      case CONTAINERTYPE:
        setContainerType((String)ipColValue);
        break;
        
      case GROUPNO:
        setGroupNo((int)ipColValue);
        break;

      case WAREHOUSE:
        setWarehouse((String)ipColValue);
        break;

      case ADDRESS:
        setAddress((String)ipColValue);
        break;

      case SHELFPOSITION:
        setShelfPosition((String)ipColValue);
        break;

      case NEXTWAREHOUSE:
        setNextWarehouse((String)ipColValue);
        break;

      case NEXTADDRESS:
        setNextAddress((String)ipColValue);
        break;

      case NEXTSHELFPOSITION:
        setNextShelfPosition((String)ipColValue);
        break;

      case FINALWAREHOUSE:
        setFinalWarehouse((String)ipColValue);
        break;

      case FINALADDRESS:
        setFinalAddress((String)ipColValue);
        break;

      case ROUTEID:
        setRouteID((String)ipColValue);
        break;

      case LOADMESSAGE:
        setLoadMessage((String)ipColValue);
        break;

      case DEVICEID:
        setDeviceID((String)ipColValue);
        break;

      case RECOMMENDEDZONE:
        setRecommendedZone((String)ipColValue);
        break;

      case BCRDATA:
        setBCRData((String)ipColValue);
        break;

      case MOVEDATE:
        setMoveDate((Date)ipColValue);
        break;

      case LOADMOVESTATUS:
        setLoadMoveStatus((int)ipColValue);
        break;

      case HEIGHT:
        setHeight((int)ipColValue);
        break;

      case LENGTH:
        setLength((int)ipColValue);
        break;

      case WIDTH:
        setWidth((int)ipColValue);
        break;

      case AMOUNTFULL:
        setAmountFull((int)ipColValue);
        break;

      case WEIGHT:
        setWeight((double)ipColValue);
        break;

      case LOADPRESENCECHECK:
        setLoadPresenceCheck((int)ipColValue);
        break;
        
      case FINALSORTLOCATION:
          setFinalSortLocationID((String)ipColValue);
          break;
        
      case MCKEY:
        setMCKey((String)ipColValue);
      
      case CURRENTADDRESS:
    	setCurrentAddress((String)ipColValue);

      case	LOT:
    	  setLot(ipColValue.toString());
    	  break;
      case	EXPECTEDDATE:
    	  setExpectedDate((Date)ipColValue);
    	  break;
      case	EXPECTEDRECEIPT:
    	  setExpectedReceipt((String)ipColValue);
    	  break;
      case	EXPIRATIONDATE:
    	  setExpirationDate((Date)ipColValue);
    	  break;
      case	POSITIONID:
    	  setPositionId((String)ipColValue);
    	  break;
      case	HOLDREASONE:
    	  setHoldReasone((String)ipColValue);
    	  break;
      case	ORDERID:
    	  setOrderId((String)ipColValue);
    	  break;
      case	ORDERLOT:
    	  setOrderLot((String)ipColValue);
    	  break;
      case	LINEID:
    	  setLineId((String)ipColValue);
    	  break;
      case	LASTCCIDATE:
    	  setLastCCIDate((Date)ipColValue);
    	  break;
      case	AGINGDATE:
    	  setAgingDate((Date)ipColValue);
    	  break;
      case	CURRENTQUANTITY:
    	  setCurrentQuantity((int)ipColValue);
    	  break;
      case	ALLOCATEDQUANTITY:
    	  setAllocatedQuantity((int)ipColValue);
    	  break;
      case	PRIORITYALLOCATION:
    	  setPriorityAllocation((int)ipColValue);
    	  break;
      case	HOLDTYPE:
    	  setHoldType((int)ipColValue);
    	  break;
      case	GLOBALID:
    	  setGlobalId((String)ipColValue);
    	  break;
    }

    return(0);
  }

  /**
  * Tests the BCR data to see if it is equal to ZEROBCRFIELD or is it is blank
  * @return true if the BCR data is equal a valid BCR ie not zero filled or blank
  */

  public boolean isBCRValidLoadID()
  {
    if( (getBCRData().equals(AGCDeviceConstants.ZEROBCRFIELD)) ||
          (getBCRData().length() == 0))
    {
      return false;  // this is not a valid Load ID
    }
    else
    {
      return true;   // This is a valid BCR
    }
  }


  /**
  * Tests the BCR data to see if it is a bad read or a no read
  * @return true if the BCR data is  a bad read or a no read
  */

  public boolean isBCRNR()
  {
    String vsBarcode = getBCRData();
    if ((vsBarcode.equalsIgnoreCase(AGCDeviceConstants.BR_BARCODE)) ||
        (vsBarcode.equalsIgnoreCase(AGCDeviceConstants.NOREAD_BARCODE)) ||
        (vsBarcode.equalsIgnoreCase(AGCDeviceConstants.NR_BARCODE)))
    {
      return true;  // this is A Bad Read
    }
    else
    {
      return false;   // This is a Good Read
    }
  }

 /**
  * Method to get a map of decimal representations of amount full translation values.
  * @return Map of amount full Translation strings to double quantities.
  */
  public static Map<String, AmountFullTransMapper> getAmountFullDecimalMap()
  {
    return(mpPartialQtyMap);
  }
  
  public static String convAmountFullToFractionString(int inAmountFullTran)
  {
    String vsFraction = "";
    
    try
    {
      vsFraction = DBTrans.getStringValue(StationData.AMOUNTFULL_NAME, inAmountFullTran);

      int vnPatternIdx = vsFraction.toLowerCase().indexOf("full");
      if (vnPatternIdx > 0)
      {
        vsFraction = vsFraction.substring(0, vnPatternIdx).trim();
      }
    }
    catch(NoSuchFieldException nsf)
    {
    }
    
    return(vsFraction);
  }

 /**
  *  Method to initialize iAmountFull translation map.
  */
  public static void initAmountFullTransMap()
  {
    if (mpPartialQtyMap != null && !mpPartialQtyMap.isEmpty()) return;
    
    mpPartialQtyMap = new TreeMap<String, AmountFullTransMapper>(new Comparator<String>()
    {
      @Override
      public int compare(String idFirst, String idSecond)
      {
        double vdFirst = AmountFullTransMapper.parseFraction(idFirst);
        double vdSecond = AmountFullTransMapper.parseFraction(idSecond);
        return((vdFirst < vdSecond) ? -1 : (vdFirst > vdSecond) ? 1 : 0);
      }
    });

    try
    {
      String[] vasPartialAmtDesc = DBTrans.getStringList(LoadDataAndLLIData.AMOUNTFULL_NAME);
      for(int vnIdx = 0; vnIdx < vasPartialAmtDesc.length; vnIdx++)
      {
        int vnPatternIdx = vasPartialAmtDesc[vnIdx].toLowerCase().indexOf("full");
        if (vnPatternIdx > 0)
        {
          String vsAmtFull = vasPartialAmtDesc[vnIdx].substring(0, vnPatternIdx).trim();
          
          AmountFullTransMapper vpTranMapper = new AmountFullTransMapper();
          int vnTranValue = DBTrans.getIntegerValue(LoadDataAndLLIData.AMOUNTFULL_NAME, vasPartialAmtDesc[vnIdx]);
          vpTranMapper.setPartialAmtFullTranVal(vnTranValue);
          vpTranMapper.setPartialAmtFullDesc(vasPartialAmtDesc[vnIdx]);
          vpTranMapper.setPartialAmtFullDecimal(AmountFullTransMapper.parseFraction(vsAmtFull));
          mpPartialQtyMap.put(vsAmtFull, vpTranMapper);
        }
      }
    }
    catch(NoSuchFieldException ex)
    {
    }
  }
}
