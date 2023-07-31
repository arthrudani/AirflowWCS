package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.WrxSequencerEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to represent Sequencing Data.
 *
 * @author       A.D.
 * @version      1.0    10-Feb-05
 */
public class WrxSequencerData extends AbstractSKDCData
{
  public static final String ENDDEVICENAME_NAME      = ENDDEVICENAME.getName();
  public static final String INCREMENTFACTOR_NAME    = INCREMENTFACTOR.getName();
  public static final String RESTARTVALUE_NAME       = RESTARTVALUE.getName();
  public static final String SEQUENCEIDENTIFIER_NAME = SEQUENCEIDENTIFIER.getName();
  public static final String SEQUENCENUMBER_NAME     = SEQUENCENUMBER.getName();
  public static final String SEQUENCETYPE_NAME       = SEQUENCETYPE.getName();
  public static final String STARTVALUE_NAME         = STARTVALUE.getName();

/*---------------------------------------------------------------------------
                 Database fields for WrxSequence table.
  ---------------------------------------------------------------------------*/
  private String sSequenceIdentifier = "";
  private String sEndDeviceName      = "";
  private int    iSequenceType       = DBConstants.HOST_SEQ;
  private int    iSequenceNumber     = 0;
  private int    iIncrementFactor    = 1;
  private int    iStartValue         = 0;
  private int    iRestartValue       = Integer.MAX_VALUE;
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public WrxSequencerData()
  {
    super();
    initColumnMap(mpColumnMap, WrxSequencerEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s =  "sSequenceIdentifier: " + sSequenceIdentifier + SKDCConstants.EOL_CHAR;
           s += "sEndDeviceName: "      + sEndDeviceName      + SKDCConstants.EOL_CHAR;
           s += "iSequenceNumber: "     + iSequenceNumber     + SKDCConstants.EOL_CHAR;
           s += "iIncrementFactor: "    + iIncrementFactor    + SKDCConstants.EOL_CHAR;
           s += "iStartValue: "         + iStartValue         + SKDCConstants.EOL_CHAR;
           s += "iRestartValue: "       + iRestartValue       + SKDCConstants.EOL_CHAR;

    try
    {
      s = s + "iSequenceType:" + DBTrans.getStringValue(SEQUENCETYPE_NAME,
                                                        iSequenceType);
    }
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }
    s += ("\n\n" + super.toString());

    return(s);
  }

  /**
   * Defines equality between two WrxSequencerData objects.
   *
   * @param  absSQ <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>WrxSequencerData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absSQ)
  {
    WrxSequencerData sq = (WrxSequencerData)absSQ;
    return(sSequenceIdentifier.equals(sq.getSequenceIdentifier()) &&
           sEndDeviceName.equals(sq.getEndDeviceName()) &&
           iSequenceType == sq.getSequenceType() &&
           iSequenceNumber == sq.getSequenceNumber() &&
           iIncrementFactor == sq.getIncrementFactor() &&
           iStartValue == sq.getStartValue() &&
           iRestartValue == sq.getRestartValue());
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sSequenceIdentifier    = "";
    iSequenceNumber  = 0;
    iIncrementFactor = 1;
    iStartValue      = 0;
    iRestartValue    = Integer.MAX_VALUE;
  }

/*---------------------------------------------------------------------------
                       Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * @return Sequence Identifier as String.
   */
  public String getSequenceIdentifier()
  {
    return (sSequenceIdentifier);
  }

  /**
   * @return Sequence end-device.
   */
  public String getEndDeviceName()
  {
    return (sEndDeviceName);
  }

  /**
   * @return Sequence type as integer.
   */
  public int getSequenceType()
  {
    return (iSequenceType);
  }

  /**
   * @return Sequence number as integer.
   */
  public int getSequenceNumber()
  {
    return (iSequenceNumber);
  }

  /**
   * @return Sequence Increment Factor number as integer.
   */
  public int getIncrementFactor()
  {
    return (iIncrementFactor);
  }

  /**
   * @return Sequence Start Value number as integer.
   */
  public int getStartValue()
  {
    return (iStartValue);
  }

  /**
   * @return Sequence Restart Value as integer.
   */
  public int getRestartValue()
  {
    return (iRestartValue);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Sequence Name
   */
  public void setSequenceIdentifier(String isSequenceIdentifier)
  {
    sSequenceIdentifier = checkForNull(isSequenceIdentifier);
    addColumnObject(new ColumnObject(SEQUENCEIDENTIFIER_NAME,
        isSequenceIdentifier));
  }

  /**
   * Sets Sequence Number
   */
  public void setSequenceNumber(int inSequenceNumber)
  {
    iSequenceNumber = inSequenceNumber;
    addColumnObject(new ColumnObject(SEQUENCENUMBER_NAME, inSequenceNumber));
  }

  /**
   * Sets end-device name.
   */
  public void setEndDeviceName(String isEndDeviceName)
  {
    sEndDeviceName = isEndDeviceName;
    addColumnObject(new ColumnObject(ENDDEVICENAME_NAME, isEndDeviceName));
  }

  /**
   * Sets Sequence type
   */
  public void setSequenceType(int inSequenceType)
  {
    iSequenceType = inSequenceType;
    addColumnObject(new ColumnObject(SEQUENCETYPE_NAME, inSequenceType));
  }

  /**
   * Sets Sequence Increment Factor
   */
  public void setIncrementFactor(int inIncrementFactor)
  {
    iIncrementFactor = inIncrementFactor;
    addColumnObject(new ColumnObject(INCREMENTFACTOR_NAME, inIncrementFactor));
  }

  /**
   * Sets Sequence Start Value
   */
  public void setStartValue(int inStartValue)
  {
    iStartValue = inStartValue;
    addColumnObject(new ColumnObject(STARTVALUE_NAME, inStartValue));
  }

  /**
   * Sets Sequence Restart Value
   */
  public void setRestartValue(int inRestartValue)
  {
    iRestartValue = inRestartValue;
    addColumnObject(new ColumnObject(RESTARTVALUE_NAME, inRestartValue));
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

    switch((WrxSequencerEnum)vpEnum)
    {
      case SEQUENCEIDENTIFIER:
        setSequenceIdentifier((String)ipColValue);
        break;

      case ENDDEVICENAME:
        setEndDeviceName((String)ipColValue);
        break;

      case SEQUENCETYPE:
        setSequenceType(((Integer)ipColValue).intValue());
        break;

      case SEQUENCENUMBER:
        setSequenceNumber(((Integer)ipColValue).intValue());
        break;

      case INCREMENTFACTOR:
        setIncrementFactor(((Integer)ipColValue).intValue());
        break;

      case STARTVALUE:
        setStartValue(((Integer)ipColValue).intValue());
        break;

      case RESTARTVALUE:
        setRestartValue(((Integer)ipColValue).intValue());
    }

    return(0);
  }
}
