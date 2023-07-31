package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import static com.daifukuamerica.wrxj.dbadapter.data.CustomerEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Customer Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.D.
 * @version      1.0
 * @since        01-Sep-01
 */
public class CustomerData extends AbstractSKDCData
{
  public static final String ATTENTION_NAME      = ATTENTION.getName();
  public static final String CITY_NAME           = CITY.getName();
  public static final String CONTACT_NAME        = CONTACT.getName();
  public static final String COUNTRY_NAME        = COUNTRY.getName();
  public static final String CUSTOMER_NAME       = CUSTOMER.getName();
  public static final String DELETEONUSE_NAME    = DELETEONUSE.getName();
  public static final String DESCRIPTION1_NAME   = DESCRIPTION1.getName();
  public static final String DESCRIPTION2_NAME   = DESCRIPTION2.getName();
  public static final String NOTE_NAME           = NOTE.getName();
  public static final String PHONE_NAME          = PHONE.getName();
  public static final String STATE_NAME          = STATE.getName();
  public static final String STREETADDRESS1_NAME = STREETADDRESS1.getName();
  public static final String STREETADDRESS2_NAME = STREETADDRESS2.getName();
  public static final String ZIPCODE_NAME        = ZIPCODE.getName();

  private int    iDeleteOnUse    = DBConstants.NO;
  
  private String sCustomer       = "";
  private String sDescription1   = "";
  private String sDescription2   = "";
  private String sStreetAddress1 = "";
  private String sStreetAddress2 = "";
  private String sCity           = "";
  private String sState          = "";
  private String sCountry        = "";
  private String sZipcode        = "";
  private String sAttention      = "";
  private String sPhone          = "";
  private String sContact        = "";
  private String sNote           = "";
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();
  
  public CustomerData()
  {
    super();
    initColumnMap(mpColumnMap, CustomerEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sCustomer: "         + sCustomer       +
               "\nsDescription1: "   + sDescription1   +
               "\nsDescription2: "   + sDescription2   +
               "\nsStreetAddress1: " + sStreetAddress1 +
               "\nsStreetAddress2: " + sStreetAddress2 +
               "\nsCity: "           + sCity           +
               "\nsState: "          + sState          +
               "\nsCountry: "        + sCountry        +
               "\nsZipcode: "        + sZipcode        +
               "\nsAttention: "      + sAttention      +
               "\nsPhone: "          + sPhone          +
               "\nsContact: "        + sContact        +
               "\nsNote: "           + sNote;

    s += super.toString();

    return(s);
  }

  /**
   * Defines equality between two CustomerData objects.
   *
   * @param  absCI <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>CustomerData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absCI)
  {
    CustomerData ci = (CustomerData)absCI;
    return getCustomer().equals(ci.getCustomer());
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    iDeleteOnUse    = DBConstants.NO;
    sCustomer       = "";
    sDescription1   = "";
    sDescription2   = "";
    sStreetAddress1 = "";
    sStreetAddress2 = "";
    sCity           = "";
    sState          = "";
    sCountry        = "";
    sZipcode        = "";
    sAttention      = "";
    sPhone          = "";
    sContact        = "";
    sNote           = "";
  }

/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Customer ID for the Customer.
   * @return Customer ID as string
   */
  public String getCustomer()
  {
    return sCustomer;
  }

  /**
   * Fetches Customer Description
   * @return Description as string
   */
  public String getDescription1()
  {
    return sDescription1;
  }

  /**
   * Fetches Customer Description
   * @return Description as string
   */
  public String getDescription2()
  {
    return sDescription2;
  }

  /**
   * Fetches Customer Contact name
   * @return Contact as string
   */
  public String getContact()
  {
    return sContact;
  }

  /**
   * Fetches Street Address
   * @return Street Address of this customer.
   */
  public String getStreetAddress1()
  {
    return sStreetAddress1;
  }


  /**
   * Fetches Street Address
   * @return Street Address of this customer.
   */
  public String getStreetAddress2()
  {
    return sStreetAddress2;
  }

  /**
   * Fetches City for the customer
   * @return City as string
   */
  public String getCity()
  {
    return sCity;
  }

  /**
   * Fetches Customer State
   * @return State.
   */
  public String getState()
  {
    return sState;
  }

  /**
   * Fetches Customer Phone
   * @return Phone
   */
  public String getPhone()
  {
    return sPhone;
  }

  /**
   * Fetches Country name.
   * @return Country as string.
   */
  public String getCountry()
  {
    return sCountry;
  }
  
  /**
   * Fetches zip code.
   * @return zip code as a String.
   */
  public String getZipcode()
  {
    return sZipcode;
  }

  /**
   * Fetches Attention-to name
   * @return Attention name as a String.
   */
  public String getAttention()
  {
    return sAttention;
  }

  /**
   * Fetches Note
   * @return note as a String.
   */
  public String getNote()
  {
    return sNote;
  }

  /**
   * Fetches Delete One Use flag
   * @return Delete One Use flag as an integer.
   */
  public int getDeleteOnUse()
  {
    return iDeleteOnUse;
  }


/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Customer value.
   */
  public void setCustomer(String isCustomer)
  {
    sCustomer = checkForNull(isCustomer);
    addColumnObject(new ColumnObject(CUSTOMER_NAME, sCustomer));
  }

  /**
   * Sets Customer Description value.
   */
  public void setDescription1(String isDescription1)
  {
    sDescription1 = checkForNull(isDescription1);
    addColumnObject(new ColumnObject(DESCRIPTION1_NAME, sDescription1));
  }

  /**
   * Sets Customer Description value.
   */
  public void setDescription2(String isDescription2)
  {
    sDescription2 = checkForNull(isDescription2);
    addColumnObject(new ColumnObject(DESCRIPTION2_NAME, sDescription2));
  }

  /**
   * Sets Customer Street Address.
   */
  public void setStreetAddress1(String isStreetAddress1)
  {
    sStreetAddress1 = checkForNull(isStreetAddress1);
    addColumnObject(new ColumnObject(STREETADDRESS1_NAME, sStreetAddress1));
  }

  /**
   * Sets Customer Street Address.
   */
  public void setStreetAddress2(String isStreetAddress2)
  {
    sStreetAddress2 = checkForNull(isStreetAddress2);
    addColumnObject(new ColumnObject(STREETADDRESS2_NAME, sStreetAddress2));
  }

  /**
   * Sets Customer City value.
   */
  public void setCity(String isCity)
  {
    sCity = checkForNull(isCity);
    addColumnObject(new ColumnObject(CITY_NAME, sCity));
  }

  /**
   * Sets Customer State.
   */
  public void setState(String isState)
  {
    sState = checkForNull(isState);
    addColumnObject(new ColumnObject(STATE_NAME, sState));
  }

  /**
   * Sets Customer Phone.
   */
  public void setPhone(String isPhone)
  {
    sPhone = checkForNull(isPhone);
    addColumnObject(new ColumnObject(PHONE_NAME, sPhone));
  }

  /**
   * Sets Customer Contact Name.
   */
  public void setContact(String isContact)
  {
    sContact = checkForNull(isContact);
    addColumnObject(new ColumnObject(CONTACT_NAME, sContact));
  }

  /**
   * Sets Country Name.
   */
  public void setCountry(String isCountry)
  {
    sCountry = checkForNull(isCountry);
    addColumnObject(new ColumnObject(COUNTRY_NAME, sCountry));
  }

  /**
   * Sets Attention Name.
   */
  public void setAttention(String isAttention)
  {
    sAttention = checkForNull(isAttention);
    addColumnObject(new ColumnObject(ATTENTION_NAME, sAttention));
  }

  /**
   * Sets Customer Note.
   */
  public void setNote(String isNote)
  {
    sNote = checkForNull(isNote);
    addColumnObject(new ColumnObject(NOTE_NAME, sNote));
  }

  /**
   * Sets Customer zip code.
   */
  public void setZipcode(String isZipcode)
  {
    sZipcode = checkForNull(isZipcode);
    addColumnObject(new ColumnObject(ZIPCODE_NAME, sZipcode));
  }

  /**
   * Sets Delete On Use flag
   */
  public void setDeleteOnUse(int inDeleteOnUse)
  {
    try
    {
      DBTrans.getStringValue(DELETEONUSE.getName(), inDeleteOnUse);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inDeleteOnUse = DBConstants.NO;
    }
    iDeleteOnUse = inDeleteOnUse;
    addColumnObject(new ColumnObject(DELETEONUSE.getName(), inDeleteOnUse));
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
      return(super.setField(isColName, ipColValue));
    }

    switch((CustomerEnum)vpEnum)
    {
      case CUSTOMER:
        setCustomer((String)ipColValue);
        break;
        
      case DESCRIPTION1:
        setDescription1((String)ipColValue);
        break;

      case DESCRIPTION2:
        setDescription2((String)ipColValue);
        break;

      case STREETADDRESS1:
        setStreetAddress1((String)ipColValue);
        break;

      case STREETADDRESS2:
        setStreetAddress2((String)ipColValue);
        break;

      case CITY:
        setCity((String)ipColValue);
        break;

      case STATE:
        setState((String)ipColValue);
        break;

      case PHONE:
        setPhone((String)ipColValue);
        break;

      case CONTACT:
        setContact((String)ipColValue);
        break;

      case COUNTRY:
        setCountry((String)ipColValue);
        break;

      case ATTENTION:
        setAttention((String)ipColValue);
        break;

      case NOTE:
        setNote((String)ipColValue);
        break;

      case ZIPCODE:
        setZipcode((String)ipColValue);
        break;

      case DELETEONUSE:
        setDeleteOnUse((Integer)ipColValue);
        break;
    }

    return(0);
  }
}
