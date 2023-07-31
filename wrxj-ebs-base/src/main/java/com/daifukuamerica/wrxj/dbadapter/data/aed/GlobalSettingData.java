package com.daifukuamerica.wrxj.dbadapter.data.aed;

import static com.daifukuamerica.wrxj.dbadapter.data.aed.GlobalSettingEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.dbadapter.WynsoftData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold Wynsoft Global Setting data (AES_SYS_GLOBAL_SETTINGS)
 * 
 * @author mandrus
 */
public class GlobalSettingData extends WynsoftData
{
  // Column widths, since this table does not reside in the Warehouse Rx DB.
  public static final int NAME_LEN                 = 100;
  
  // Columns
  public static final String AREA_NAME                = AREA.getName();
  public static final String DESCRIPTION_NAME         = DESCRIPTION.getName();
  public static final String ID_NAME                  = ID.getName();
  public static final String INSTANCEID_NAME          = INSTANCEID.getName();
  public static final String ISAUTOREFRESHREQUIRED_NAME= ISAUTOREFRESHREQUIRED.getName();
  public static final String ISEDITABLE_NAME          = ISEDITABLE.getName();
  public static final String ISHIDDEN_NAME            = ISHIDDEN.getName();
  public static final String MAXVALUE_NAME            = MAXVALUE.getName();
  public static final String MINVALUE_NAME            = MINVALUE.getName();
  public static final String NAME_NAME                = NAME.getName();
  public static final String PRODUCTID_NAME           = PRODUCTID.getName();
  public static final String RECOMMENDEDVALUE_NAME    = RECOMMENDEDVALUE.getName();
  public static final String REGEX_NAME               = REGEX.getName();
  public static final String TYPEID_NAME              = TYPEID.getName();
  public static final String VALUE_NAME               = VALUE.getName();
  
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public enum GlobalSettingType {
    INTEGER(1),
    STRING(2),
    DATETIME(3),
    BOOLEAN(4),
    DOUBLE(5);
    
    private int id;
    
    private GlobalSettingType(int iid)
    {
      id = iid;
    }
    
    public int getId() {
      return id;
    }
    
    public static GlobalSettingType getById(int iid)
    {
      for (GlobalSettingType gst : GlobalSettingType.values()) {
        if (gst.getId() == iid) {
          return gst;
        }
      }
      return null;
    }
  }
  
  // ------------------- GlobalSetting table data -----------------------------
  private String  msArea;
  private String  msDescription;
  private int     mnId;
  private int     mnInstanceId;
  private boolean mzIsAutoRefreshRequired;
  private boolean mzIsEditable;
  private boolean mzIsHidden;
  private String  msMaxValue;
  private String  msMinValue;
  private String  msName;
  private int     mnProductId;
  private String  msRecommendedValue;
  private String  msRegEx;
  private GlobalSettingType meTypeId;
  private String  msValue;

  //-------------------- GlobalSetting default data ---------------------------
  public GlobalSettingData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, GlobalSettingEnum.class);
  }

  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer();
    for (String sKey : mpColumnMap.keySet()) {
      myString.append(sKey).append(" = ").append(getColumnObject(sKey)).append("; ");
    }
    return(myString.toString() + super.toString());
  }

  @Override
  public boolean equals(AbstractSKDCData absGSD)
  {
    GlobalSettingData gsd = (GlobalSettingData)absGSD;
    return gsd.getId() == getId();
  }


/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public String getArea()                {  return msArea;                   }
  public String getDescription()         {  return msDescription;            }
  public int    getId()                  {  return mnId;                     }
  public int    getInstanceId()          {  return mnInstanceId;             }
  public boolean getIsAutoRefreshRequired(){  return mzIsAutoRefreshRequired;  }
  public boolean getIsEditable()          {  return mzIsEditable;             }
  public boolean getIsHidden()            {  return mzIsHidden;               }
  public String getMaxValue()            {  return msMaxValue;               }
  public String getMinValue()            {  return msMinValue;               }
  public String getName()                {  return msName;                   }
  public int    getProductId()           {  return mnProductId;              }
  public String getRecommendedValue()     {  return msRecommendedValue;        }
  public String getRegEx()               {  return msRegEx;                  }
  public GlobalSettingType getTypeId()   {  return meTypeId;                 }
  public String getValue()               {  return msValue;                  }

/*---------------------------------------------------------------------------
  Special Value Getters
---------------------------------------------------------------------------*/
  public Boolean getValueAsBoolean()
  {
    return Boolean.parseBoolean(msValue);
  }

  public Date getValueAsDate() throws ParseException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
    return sdf.parse(msValue);
  }

  public Double getValueAsDouble() throws NumberFormatException
  {
    return Double.parseDouble(msValue);
  }

  public Integer getValueAsInteger() throws NumberFormatException
  {
    return Integer.parseInt(msValue);
  }

  public String getValueAsString()
  {
    return msValue;
  }

  public Object getValueAsObject() throws ParseException, NumberFormatException
  {
    switch (getTypeId()) {
      case BOOLEAN:
        return getValueAsBoolean();
      case DATETIME:
        return getValueAsDate();
      case DOUBLE:
        return getValueAsDouble();
      case INTEGER:
        return getValueAsInteger();
      case STRING:
        return getValueAsString();
      default:
        return null;
    }
  }
  
/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setArea(String iArea)
  {
    msArea = iArea;
    addColumnObject(new ColumnObject(AREA_NAME, msArea));
  }
  public void setDescription(String iDescription)
  {
    msDescription = iDescription;
    addColumnObject(new ColumnObject(DESCRIPTION_NAME, msDescription));
  }
  public void setId(int iId)
  {
    mnId = iId;
    addColumnObject(new ColumnObject(ID_NAME, mnId));
  }
  public void setInstanceId(int iInstanceId)
  {
    mnInstanceId = iInstanceId;
    addColumnObject(new ColumnObject(INSTANCEID_NAME, mnInstanceId));
  }
  public void setIsAutoRefreshRequired(boolean iIsAutoRefreshRequired)
  {
    mzIsAutoRefreshRequired = iIsAutoRefreshRequired;
    addColumnObject(new ColumnObject(ISAUTOREFRESHREQUIRED_NAME, mzIsAutoRefreshRequired));
  }
  public void setIsEditable(boolean izIsEditable)
  {
    this.mzIsEditable = izIsEditable;
    addColumnObject(new ColumnObject(ISEDITABLE_NAME, mzIsEditable));
  }
  public void setIsHidden(boolean iIsHidden)
  {
    mzIsHidden = iIsHidden;
    addColumnObject(new ColumnObject(ISHIDDEN_NAME, mzIsHidden));
  }
  public void setMaxValue(String iMaxValue)
  {
    msMaxValue = iMaxValue;
    addColumnObject(new ColumnObject(MAXVALUE_NAME, msMaxValue));
  }
  public void setMinValue(String iMinValue)
  {
    msMinValue = iMinValue;
    addColumnObject(new ColumnObject(MINVALUE_NAME, msMinValue));
  }
  public void setName(String iName)
  {
    msName = iName;
    addColumnObject(new ColumnObject(NAME_NAME, msName));
  }
  public void setProductId(int iProductId)
  {
    mnProductId = iProductId;
    addColumnObject(new ColumnObject(PRODUCTID_NAME, mnProductId));
  }
  public void setRecommendedValue(String iRecommendedValue)
  {
    msRecommendedValue = iRecommendedValue;
    addColumnObject(new ColumnObject(RECOMMENDEDVALUE_NAME, msRecommendedValue));
  }
  public void setRegEx(String iRegEx)
  {
    msRegEx = iRegEx;
    addColumnObject(new ColumnObject(REGEX_NAME, msRegEx));
  }
  public void setTypeId(GlobalSettingType ieType)
  {
    this.meTypeId = ieType;
    addColumnObject(new ColumnObject(TYPEID_NAME, ieType));
  }
  public void setTypeId(Integer ieType)
  {
    this.meTypeId = GlobalSettingType.getById(ieType);
    addColumnObject(new ColumnObject(TYPEID_NAME, ieType));
  }
  public void setValue(String iValue)
  {
    msValue = iValue;
    addColumnObject(new ColumnObject(VALUE_NAME, msValue));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null) 
    {
      return super.setField(isColName, ipColValue);
    }
    
    switch ((GlobalSettingEnum)vpEnum)
    {
      case AREA:
        setArea((String)ipColValue);
        break;
      case DESCRIPTION:
        setDescription((String)ipColValue);
        break;
      case ID:
        setId(((Integer)ipColValue).intValue());
        break;
      case INSTANCEID:
        setInstanceId((Integer)ipColValue);
        break;
      case ISAUTOREFRESHREQUIRED:
        setIsAutoRefreshRequired((Boolean)ipColValue);
        break;
      case ISEDITABLE:
        setIsEditable((Boolean)ipColValue);
        break;
      case ISHIDDEN:
        setIsHidden((Boolean)ipColValue);
        break;
      case MAXVALUE:
        setMaxValue((String)ipColValue);
        break;
      case MINVALUE:
        setMinValue((String)ipColValue);
        break;
      case NAME:
        setName((String)ipColValue);
        break;
      case PRODUCTID:
        setProductId((Integer)ipColValue);
        break;
      case RECOMMENDEDVALUE:
        setRecommendedValue((String)ipColValue);
        break;
      case REGEX:
        setRegEx((String)ipColValue);
        break;
      case TYPEID:
        if (ipColValue instanceof GlobalSettingType) {
          setTypeId((GlobalSettingType)ipColValue);
        } else {
          setTypeId((Integer)ipColValue);
        }
        break;
      case VALUE:
        setValue((String)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
