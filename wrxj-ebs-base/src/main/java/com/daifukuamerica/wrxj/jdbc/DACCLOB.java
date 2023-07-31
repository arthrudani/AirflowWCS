package com.daifukuamerica.wrxj.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * DAC Character Large Object.  This class is used to send packaged info.
 * to the DAC DB interface for CLOBs.
 *
 * @author A.D.
 * @since 18-Dec-2009
 */
public class DACCLOB
{
  private InputStream mpInpStrm;
  private final byte[] mabCLOBBytes;

  public DACCLOB(byte[] iabClob)
  {
    mabCLOBBytes = iabClob;
  }

  public int getDataSize()
  {
    return(mabCLOBBytes.length);
  }

  public InputStream getByteInputStream()
  {
    mpInpStrm = new ByteArrayInputStream(mabCLOBBytes);
    return(mpInpStrm);
  }

  public Reader getByteArrayReader()
  {
    return new InputStreamReader(new ByteArrayInputStream(mabCLOBBytes));
  }

  public void closeStream()
  {
    try { mpInpStrm.close(); } catch(IOException e) {}
  }
}
