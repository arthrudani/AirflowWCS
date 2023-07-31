package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.LoadEnum.*;

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
public class LoadData extends AbstractSKDCData
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
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();
  private static Map<String, AmountFullTransMapper> mpPartialQtyMap;

  public LoadData()
  {
    sdf.applyPattern(SKDCConstants.DateFormatString);
    clear();
    initColumnMap(mpColumnMap, LoadEnum.class);
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
  }

  @Override
  public String toString()
  {
    String s = "sLoadID:" + sLoadID +
               "\nsParentLoadID:" + sParentLoadID +
               "\nsMCKey:" + sMCKey +
               "\nsContainerType:" + sContainerType +
               "\nsWarehouse:" + sWarehouse +
               "\nsAddress:" + sAddress +
               "\nsShelfPosition:" + sShelfPosition +
               "\nsNextWarehouse:" + sNextWarehouse +
               "\nsNextAddress:" + sNextAddress +
               "\nsNextShelfPosition:" + sNextShelfPosition +
               "\nsFinalWarehouse:" + sFinalWarehouse +
               "\nsFinalAddress:" + sFinalAddress +
               "\nsRouteID:" + sRouteID +
               "\nsZone:" + sRecommendedZone +
               "\ndMoveDate:" + sdf.format(dMoveDate) +
               "\nsLoadMessage:" + sLoadMessage +
               "\nsDeviceID:" + sDeviceID +
               "\nsBCRData:" + sBCRData + 
               "\nsFinalSortLocationID:" + sFinalSortLocationID + 
               "\nsCurrentAddress:" + sCurrentAddress + "\n";

    try
    {
      s = s + "iLoadMoveStatus:" + DBTrans.getStringValue("iLoadMoveStatus", iLoadMoveStatus) +
               "\niAmountFull:" + DBTrans.getStringValue("iAmountFull", iAmountFull) +
               "\niLoadPresenceCheck:" + DBTrans.getStringValue("iLoadPresenceCheck", iLoadPresenceCheck) + "\n";
    }
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }
    s = s + "iHeight:" + iHeight +
            "\niLength:" + iLength +
            "\niWidth:" + iWidth +
            "\niGroupNo:" + iGroupNo +
            "\nfWeight:" + fWeight +
            super.toString();

    return(s);
  }

  /**
   *  Method to perform clone of <code>LoadData</code>.
   *
   *  @return copy of <code>LoadData</code>
   */
  @Override
  public LoadData clone()
  {
    LoadData vpClonedData = (LoadData)super.clone();
    vpClonedData.dMoveDate = (Date)dMoveDate.clone();

    return vpClonedData;
  }

  @Override
  public boolean equals(AbstractSKDCData absLD)
  {
    LoadData ld = (LoadData)absLD;
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

    switch((LoadEnum)vpEnum)
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
        setGroupNo((Integer)ipColValue);
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
        setLoadMoveStatus((Integer)ipColValue);
        break;

      case HEIGHT:
        setHeight((Integer)ipColValue);
        break;

      case LENGTH:
        setLength((Integer)ipColValue);
        break;

      case WIDTH:
        setWidth((Integer)ipColValue);
        break;

      case AMOUNTFULL:
        setAmountFull((Integer)ipColValue);
        break;

      case WEIGHT:
        setWeight((Double)ipColValue);
        break;

      case LOADPRESENCECHECK:
        setLoadPresenceCheck((Integer)ipColValue);
        break;
        
      case FINALSORTLOCATION:
          setFinalSortLocationID((String)ipColValue);
          break;
        
      case MCKEY:
        setMCKey((String)ipColValue);
      
      case CURRENTADDRESS:
    	setCurrentAddress((String)ipColValue);

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
      String[] vasPartialAmtDesc = DBTrans.getStringList(LoadData.AMOUNTFULL_NAME);
      for(int vnIdx = 0; vnIdx < vasPartialAmtDesc.length; vnIdx++)
      {
        int vnPatternIdx = vasPartialAmtDesc[vnIdx].toLowerCase().indexOf("full");
        if (vnPatternIdx > 0)
        {
          String vsAmtFull = vasPartialAmtDesc[vnIdx].substring(0, vnPatternIdx).trim();
          
          AmountFullTransMapper vpTranMapper = new AmountFullTransMapper();
          int vnTranValue = DBTrans.getIntegerValue(LoadData.AMOUNTFULL_NAME, vasPartialAmtDesc[vnIdx]);
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
