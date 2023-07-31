package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;

/**
 * Report message from AGV containing info. about Queued Move Requests. This
 * message is received for each queued load move.  The last message indicating
 * the end of the report is the END message.
 *
 * @author A.D.
 * @since  13-May-2009
 */
public class QMRParserImpl extends AbstractMessageParser
{
  public QMRParserImpl()
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
