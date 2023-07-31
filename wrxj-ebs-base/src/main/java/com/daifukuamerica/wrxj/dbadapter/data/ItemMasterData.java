package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import static com.daifukuamerica.wrxj.dbadapter.data.ItemMasterEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Item Master Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.T.
 * @version      1.0
 */
public class ItemMasterData extends AbstractSKDCData
{
  public static final String CASEHEIGHT_NAME           = CASEHEIGHT.getName();
  public static final String CASELENGTH_NAME           = CASELENGTH.getName();
  public static final String CASEWEIGHT_NAME           = CASEWEIGHT.getName();
  public static final String CASEWIDTH_NAME            = CASEWIDTH.getName();
  public static final String CCIPOINTQUANTITY_NAME     = CCIPOINTQUANTITY.getName();
  public static final String DEFAULTLOADQUANTITY_NAME  = DEFAULTLOADQUANTITY.getName();
  public static final String DELETEATZEROQUANTITY_NAME = DELETEATZEROQUANTITY.getName();
  public static final String DESCRIPTION_NAME          = DESCRIPTION.getName();
  public static final String EXPIRATIONREQUIRED_NAME   = EXPIRATIONREQUIRED.getName();
  public static final String HOLDTYPE_NAME             = HOLDTYPE.getName();
  public static final String ITEM_NAME                 = ITEM.getName();
  public static final String ITEMHEIGHT_NAME           = ITEMHEIGHT.getName();
  public static final String ITEMLENGTH_NAME           = ITEMLENGTH.getName();
  public static final String ITEMWEIGHT_NAME           = ITEMWEIGHT.getName();
  public static final String ITEMWIDTH_NAME            = ITEMWIDTH.getName();
  public static final String LASTCCIDATE_NAME          = LASTCCIDATE.getName();
  public static final String ORDERROUTE_NAME           = ORDERROUTE.getName();
  public static final String PIECESPERUNIT_NAME        = PIECESPERUNIT.getName();
  public static final String RECOMMENDEDWAREHOUSE_NAME = RECOMMENDEDWAREHOUSE.getName();
  public static final String RECOMMENDEDZONE_NAME      = RECOMMENDEDZONE.getName();
  public static final String STORAGEFLAG_NAME          = STORAGEFLAG.getName();

  private String sItem                 = "";
  private String sDescription          = "";
  private String sRecommendedWarehouse = "";
  private String sRecommendedZone      = "";
  private String sOrderRoute           = "";
  private int iExpirationRequired = DBConstants.NO;
  private int iHoldType = DBConstants.ITMAVAIL;
  private int iDeleteAtZeroQuantity = DBConstants.NO;
  private int iPiecesPerUnit = 1;
  private double fCCIPointQuantity;
  private double fDefaultLoadQuantity;
  private double fItemWeight = 0.0;
  private double fItemLength = 0.0;
  private double fItemWidth  = 0.0;
  private double fItemHeight = 0.0;
  private double fCaseWeight = 0.0;
  private double fCaseLength = 0.0;
  private double fCaseWidth  = 0.0;
  private double fCaseHeight = 0.0;
  private int iStorageFlag = DBConstants.MIXALL;
  private Date dLastCCIDate = new Date();
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public ItemMasterData()
  {
    super();
    initColumnMap(mpColumnMap, ItemMasterEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sItem:" + sItem +
               "\nsDescription:" + sDescription +
               "\nsRecommendedWarehouse:" + sRecommendedWarehouse +
               "\nsRecommendedZone:" + sRecommendedZone +
               "\nsOrderRoute:" + sOrderRoute +
               "\niPiecesPerUnit:" + iPiecesPerUnit +
               "\nfDefaultLoadQuantity:" + fDefaultLoadQuantity +
               "\nfItemWeight:" + fItemWeight +
               "\nfItemLength:" + fItemLength +
               "\nfItemHeight:" + fItemHeight +
               "\nfItemWidth:" + fItemWidth +
               "\nfCaseWeight:" + fCaseWeight +
               "\nfCaseLength:" + fCaseLength +
               "\nfCaseHeight:" + fCaseHeight +
               "\nfCaseWidth:" + fCaseWidth +
               "\nfCCIPointQuantity:" + fCCIPointQuantity +
               "\ndLastCCIDate:" + sdf.format(dLastCCIDate) +
               "\n";
    try
    {
      s = s + "iHoldType:" + DBTrans.getStringValue(HOLDTYPE_NAME, iHoldType) +
               "\niDeleteAtZeroQuantity:" + DBTrans.getStringValue(DELETEATZEROQUANTITY_NAME, iDeleteAtZeroQuantity) +
               "\niStorageFlag:" + DBTrans.getStringValue(STORAGEFLAG_NAME, iStorageFlag) +
               "\niExpirationRequired:" + DBTrans.getStringValue(EXPIRATIONREQUIRED_NAME, iExpirationRequired) +
               "\n";
    }
    catch(NoSuchFieldException e)
    {
      s= s + "0";
      e.printStackTrace();
    }

    s += super.toString();

    return(s);
  }

  /**
   *  Method to make a deep copy of this object.
   *
   *  @return copy of <code>ItemMasterData</code>.
   */
  @Override
  public ItemMasterData clone()
  {
    ItemMasterData vpClonedData = (ItemMasterData)super.clone();
    vpClonedData.dLastCCIDate = dLastCCIDate == null ? null : (Date)dLastCCIDate.clone();
    return vpClonedData;
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();
    sItem = "";
    sDescription = "";
    sRecommendedWarehouse = "";
    sRecommendedZone = "";
    sOrderRoute = "";

    iHoldType             = DBConstants.ITMAVAIL;
    iDeleteAtZeroQuantity = DBConstants.NO;
    iPiecesPerUnit        = 1;
    fItemWeight = 0.0;
    fItemLength = 0.0;
    fItemWidth  = 0.0;
    fItemHeight = 0.0;
    fCaseWeight = 0.0;
    fCaseLength = 0.0;
    fCaseWidth  = 0.0;
    fCaseHeight = 0.0;
    iStorageFlag          = DBConstants.MIXALL;
    iExpirationRequired = DBConstants.NO;

    fCCIPointQuantity     = 0;
    fDefaultLoadQuantity  = 1.0;
    if (dLastCCIDate != null)
      dLastCCIDate.setTime(System.currentTimeMillis());
  }

  /**
   * Defines equality between two ItemMasterData objects.
   *
   * @param  absIM <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>ItemMasterData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absIM)
  {
    ItemMasterData im = (ItemMasterData)absIM;
    return im.sItem.equals(sItem);
  }

/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Item
   * @return Item as string
   */
  public String getItem()
  {
    return sItem;
  }
  /**
   * Fetches Last CCI Date
   * @return Last CCI Date as Date
   */
  public Date getLastCCIDate()
  {
    return dLastCCIDate;
  }
  /**
   * Fetches Hold Type
   * @return Hold Type as integer
   */
  public int getHoldType()
  {
    return iHoldType;
  }
  /**
   * Fetches Recommended Warehouse
   * @return Recommended Warehouse as string
   */
  public String getRecommendedWarehouse()
  {
    return sRecommendedWarehouse.toString();
  }
  /**
   * Fetches Recommended Zone
   * @return Recommended Zone as string
   */
  public String getRecommendedZone()
  {
    return sRecommendedZone;
  }
  /**
   * Fetches Order Route ID
   * @return Order Route ID as string
   */
  public String getOrderRoute()
  {
    return sOrderRoute;
  }
  /**
   * Fetches Description
   * @return Description as string
   */
  public String getDescription()
  {
    return sDescription;
  }
  /**
   * Fetches Delete At Zero Quantity
   * @return Delete At Zero Quantity as integer
   */
  public int getDeleteAtZeroQuantity()
  {
    return iDeleteAtZeroQuantity;
  }
  /**
   * Fetches ExpirationRequired
   * @return ExpirationRequired as integer
   */
  public int getExpirationRequired()
  {
    return iExpirationRequired;
  }
  /**
   * Fetches CCI Point Quantity
   * @return CCI Point Quantity as double
   */
  public double getCCIPointQuantity()
  {
    return fCCIPointQuantity;
  }
  /**
   * Fetches Default Load Quantity
   * @return Default Load Quantity as double
   */
  public double getDefaultLoadQuantity()
  {
    return fDefaultLoadQuantity;
  }
  /**
   * Fetches Pieces Per Unit
   * @return Pieces Per Unit as integer
   */
  public int getPiecesPerUnit()
  {
    return iPiecesPerUnit;
  }
  /**
   * Fetches Weight
   * @return Weight as double
   */
  public double getWeight()
  {
    return fItemWeight;
  }
  /**
   * Fetches Length
   * @return Length as double
   */
  public double getItemLength()
  {
    return fItemLength;
  }
  /**
   * Fetches Height
   * @return Height as double
   */
  public double getItemHeight()
  {
    return fItemHeight;
  }
  /**
   * Fetches Width
   * @return Width as double
   */
  public double getItemWidth()
  {
    return fItemWidth;
  }
  /**
   * Fetches CaseWeight
   * @return CaseWeight as double
   */
  public double getCaseWeight()
  {
    return fCaseWeight;
  }
  /**
   * Fetches CaseLength
   * @return CaseLength as double
   */
  public double getCaseLength()
  {
    return fCaseLength;
  }
  /**
   * Fetches CaseHeight
   * @return CaseHeight as double
   */
  public double getCaseHeight()
  {
    return fCaseHeight;
  }
  /**
   * Fetches CaseWidth
   * @return CaseWidth as double
   */
  public double getCaseWidth()
  {
    return fCaseWidth;
  }
  /**
   * Fetches Storage Flag
   * @return Storage Flag as integer
   */
  public int getStorageFlag()
  {
    return iStorageFlag;
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Item value.
   */
  public void setItem(String isItem)
  {
    sItem = checkForNull(isItem);
    addColumnObject(new ColumnObject(ITEM.getName(), sItem));
  }
  /**
   * Sets Item Description value.
   */
  public void setDescription(String isDescription)
  {
    sDescription = checkForNull(isDescription);
    addColumnObject(new ColumnObject(DESCRIPTION.getName(), sDescription));
  }
  /**
   * Sets Recommended Warehouse value.
   */
  public void setRecommendedWarehouse(String isRecommendedWarehouse)
  {
    sRecommendedWarehouse = checkForNull(isRecommendedWarehouse);
    addColumnObject(new ColumnObject(RECOMMENDEDWAREHOUSE.getName(), sRecommendedWarehouse));
  }
  /**
   * Sets Recommended Zone value.
   */
  public void setRecommendedZone(String isRecommendedZone)
  {
    sRecommendedZone = checkForNull(isRecommendedZone);
    addColumnObject(new ColumnObject(RECOMMENDEDZONE.getName(), sRecommendedZone));
  }
  /**
   * Sets Order Route ID value.
   */
  public void setOrderRoute(String isOrderRoute)
  {
    sOrderRoute = checkForNull(isOrderRoute);
    addColumnObject(new ColumnObject(ORDERROUTE.getName(), sOrderRoute));
  }
  /**
   * Sets Hold Type value.
   */
  public void setHoldType(int inHoldType)
  {
    iHoldType = inHoldType;
    addColumnObject(new ColumnObject(HOLDTYPE.getName(), iHoldType));
  }
  /**
   * Sets ExpirationRequired value.
   */
  public void setExpirationRequired(int inExpirationRequired)
  {
    iExpirationRequired = inExpirationRequired;
    addColumnObject(new ColumnObject(EXPIRATIONREQUIRED.getName(), 
        iExpirationRequired));
  }
  /**
   * Sets Delete A tZero Quantity value.
   */
  public void setDeleteAtZeroQuantity(int inDeleteAtZeroQuantity)
  {
    iDeleteAtZeroQuantity = inDeleteAtZeroQuantity;
    addColumnObject(new ColumnObject(DELETEATZEROQUANTITY.getName(),
        iDeleteAtZeroQuantity));
  }
  /**
   * Sets Last CCI Date value.
   */
  public void setLastCCIDate(Date ipLastCCIDate)
  {
    dLastCCIDate = ipLastCCIDate;
    addColumnObject(new ColumnObject(LASTCCIDATE.getName(), dLastCCIDate));
  }
  /**
   * Sets PiecesPerUnit value.
   */
  public void setPiecesPerUnit(int inPiecesPerUnit)
  {
    iPiecesPerUnit = inPiecesPerUnit;
    addColumnObject(new ColumnObject(PIECESPERUNIT.getName(),
        iPiecesPerUnit));
  }
  /**
   * Sets Weight value.
   */
  public void setWeight(double idItemWeight)
  {
    fItemWeight = idItemWeight;
    addColumnObject(new ColumnObject(ITEMWEIGHT.getName(), fItemWeight));
  }
  /**
   * Sets Length value.
   */
  public void setItemLength(double idItemLength)
  {
    fItemLength = idItemLength;
    addColumnObject(new ColumnObject(ITEMLENGTH.getName(), fItemLength));
  }
  /**
   * Sets Height value.
   */
  public void setItemHeight(double idItemHeight)
  {
    fItemHeight = idItemHeight;
    addColumnObject(new ColumnObject(ITEMHEIGHT.getName(), fItemHeight));
  }
  /**
   * Sets Width value.
   */
  public void setItemWidth(double idItemWidth)
  {
    fItemWidth = idItemWidth;
    addColumnObject(new ColumnObject(ITEMWIDTH.getName(), fItemWidth));
  }
  /**
   * Sets CaseWeight value.
   */
  public void setCaseWeight(double idCaseWeight)
  {
    fCaseWeight = idCaseWeight;
    addColumnObject(new ColumnObject(CASEWEIGHT.getName(), fCaseWeight));
  }
  /**
   * Sets CaseLength value.
   */
  public void setCaseLength(double idCaseLength)
  {
    fCaseLength = idCaseLength;
    addColumnObject(new ColumnObject(CASELENGTH.getName(), fCaseLength));
  }
  /**
   * Sets CaseHeight value.
   */
  public void setCaseHeight(double idCaseHeight)
  {
    fCaseHeight = idCaseHeight;
    addColumnObject(new ColumnObject(CASEHEIGHT.getName(), fCaseHeight));
  }
  /**
   * Sets CaseWidth value.
   */
  public void setCaseWidth(double idCaseWidth)
  {
    fCaseWidth = idCaseWidth;
    addColumnObject(new ColumnObject(CASEWIDTH.getName(), fCaseWidth));
  }
  /**
   * Sets Storage Flag value.
   */
  public void setStorageFlag(int inStorageFlag)
  {
    iStorageFlag = inStorageFlag;
    addColumnObject(new ColumnObject(STORAGEFLAG.getName(), iStorageFlag));
  }
  /**
   * Sets CCI Point Quantity value.
   */
  public void setCCIPointQuantity(double idCCIPointQuantity)
  {
    fCCIPointQuantity = idCCIPointQuantity;
    addColumnObject(new ColumnObject(CCIPOINTQUANTITY.getName(), 
        fCCIPointQuantity));
  }
  /**
   * Sets Default Load Quantity value.
   */
  public void setDefaultLoadQuantity(double idDefaultLoadQuantity)
  {
    fDefaultLoadQuantity = idDefaultLoadQuantity;
    addColumnObject(new ColumnObject(DEFAULTLOADQUANTITY.getName(), 
        fDefaultLoadQuantity));
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

    switch((ItemMasterEnum)vpEnum)
    {
      case ITEM:
        setItem((String)ipColValue);
        break;

      case DESCRIPTION:
        setDescription((String)ipColValue);
        break;

      case RECOMMENDEDWAREHOUSE:
        setRecommendedWarehouse((String)ipColValue);
        break;

      case RECOMMENDEDZONE:
        setRecommendedZone((String)ipColValue);
        break;
                                    
      case ORDERROUTE:
        setOrderRoute((String)ipColValue);
        break;
                                    
      case LASTCCIDATE:
        setLastCCIDate(((Date)ipColValue));
        break;
                                   
      case CCIPOINTQUANTITY:
        setCCIPointQuantity(((Double)ipColValue).doubleValue());
        break;

      case DEFAULTLOADQUANTITY:
        setDefaultLoadQuantity(((Double)ipColValue).doubleValue());
        break;

      case HOLDTYPE:
        setHoldType(((Integer)ipColValue).intValue());
        break;

      case DELETEATZEROQUANTITY:
        setDeleteAtZeroQuantity(((Integer)ipColValue).intValue());
        break;

      case PIECESPERUNIT:
        setPiecesPerUnit(((Integer)ipColValue).intValue());
        break;

      case ITEMWEIGHT:
        setWeight(((Double)ipColValue).doubleValue());
        break;

      case ITEMLENGTH:
        setItemLength(((Double)ipColValue).doubleValue());
        break;

      case ITEMHEIGHT:
        setItemHeight(((Double)ipColValue).doubleValue());
        break;

      case ITEMWIDTH:
        setItemWidth(((Double)ipColValue).doubleValue());
        break;

      case CASEWEIGHT:
        setCaseWeight(((Double)ipColValue).doubleValue());
        break;

      case CASELENGTH:
        setCaseLength(((Double)ipColValue).doubleValue());
        break;

      case CASEHEIGHT:
        setCaseHeight(((Double)ipColValue).doubleValue());
        break;

      case CASEWIDTH:
        setCaseWidth(((Double)ipColValue).doubleValue());
        break;

      case STORAGEFLAG:
        setStorageFlag(((Integer)ipColValue).intValue());
        break;
                                    
      case EXPIRATIONREQUIRED:
        setExpirationRequired(((Integer)ipColValue).intValue());
    }
    return(0);
  }
}
