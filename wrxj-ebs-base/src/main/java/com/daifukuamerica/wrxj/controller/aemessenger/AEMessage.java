package com.daifukuamerica.wrxj.controller.aemessenger;

public class AEMessage
{
  private Long mnSource;
  private Long mnTransactionID;
  private Long mnSize;
  private byte[] mabMessageData;
  
  public AEMessage()
  {
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("Source=").append(mnSource)
      .append("; TransactionID=").append(mnTransactionID)
      .append("; Size=").append(mnSize)
      .append("; Body=").append(new String(mabMessageData));
    return sb.toString();
  }
  
  public Long getSource()
  {
    return mnSource;
  }

  public void setSource(long inSource)
  {
    this.mnSource = inSource;
  }

  public Long getTransactionID()
  {
    return mnTransactionID;
  }

  public void setTransactionID(long inTransactionID)
  {
    this.mnTransactionID = inTransactionID;
  }

  public Long getSize()
  {
    return mnSize;
  }

  public void setSize(long inSize)
  {
    this.mnSize = inSize;
  }

  public byte[] getMessageData()
  {
    return mabMessageData;
  }

  public String getMessageDataAsString()
  {
    return new String(mabMessageData);
  }

  public void setMessageBody(byte[] iabMessageData)
  {
    this.mabMessageData = iabMessageData;
  }
}
