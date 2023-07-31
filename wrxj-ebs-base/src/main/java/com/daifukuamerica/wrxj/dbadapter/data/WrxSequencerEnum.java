package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the WrxSequencer Table fields.
 *
 *  @author A.D.
 *  @since  18-Nov-2007
 */
public enum WrxSequencerEnum implements TableEnum
{
  SEQUENCEIDENTIFIER("SSEQUENCEIDENTIFIER"),
  ENDDEVICENAME("SENDDEVICENAME"),
  SEQUENCETYPE("ISEQUENCETYPE"),
  SEQUENCENUMBER("ISEQUENCENUMBER"),
  INCREMENTFACTOR("IINCREMENTFACTOR"),
  STARTVALUE("ISTARTVALUE"),
  RESTARTVALUE("IRESTARTVALUE");

  private String msMessageName;

  WrxSequencerEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
