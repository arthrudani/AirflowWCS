
package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;

/**
 * Report message from AGV system containing status info. for each vehicle.
 * The last message indicating the end of the report is the END message.
 *
 * @author A.D.
 * @since  13-May-2009
 */
public class VSRParserImpl extends AbstractMessageParser
{
  public VSRParserImpl()
  {
    super();
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected int setField(String isColumnName, String isColumnValue)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isValidMessageLength(String isMessage)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public StringBuffer getFormattedLogFields()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
