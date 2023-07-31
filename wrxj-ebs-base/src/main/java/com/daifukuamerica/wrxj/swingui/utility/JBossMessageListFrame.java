package com.daifukuamerica.wrxj.swingui.utility;

import static com.daifukuamerica.wrxj.swingui.utility.JBossMonitorFrame.JBOSS_DB;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.util.List;
import java.util.Map;

/**
 * <B>Description:</B> Simple screen to check the JBoss message queue size
 * 
 * <P>Copyright (c) 2010 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class JBossMessageListFrame extends SKDCListFrame
{
  private static final String JBOSS_QUERY_MSG1 = "SELECT MESSAGEID, DESTINATION FROM JMS_MESSAGES ORDER BY MESSAGEID";
  private static final String JBOSS_QUERY_MSG2 = "SELECT MESSAGEBLOB FROM JMS_MESSAGES WHERE MESSAGEID=";
  private static final String MESSAGE_NAME = "MESSAGEBLOB";

  private DBObject mpJBossDBObj;
  
  /**
   * Constructor
   */
  public JBossMessageListFrame()
  {
    super("JBossMessage");
    JBossMessageCellRenderer vpCellRenderer = new JBossMessageCellRenderer();
    sktable.setDefaultRenderer(Integer.class, vpCellRenderer);
    sktable.setDefaultRenderer(Double.class, vpCellRenderer);
    sktable.setDefaultRenderer(String.class, vpCellRenderer);

    if (Application.getString(JBOSS_DB + ".driver") == null)
    {
      Application.setString(JBOSS_DB + ".driver", "oracle.jdbc.driver.OracleDriver");
      Application.setString(JBOSS_DB + ".url", "jdbc:oracle:thin:@localhost:1521:JBossJMS");
      Application.setString(JBOSS_DB + ".user", "JBoss");
      Application.setString(JBOSS_DB + ".password", "JBoss");
      Application.setString(JBOSS_DB + ".maximum", "3");
    }
    mpJBossDBObj = new DBObjectTL().getDBObject(JBOSS_DB);
    mpJBossDBObj.setMaxRows(1000);
    try
    {
      mpJBossDBObj.connect();
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
    
    setDisplaySearchCount(true, "JBoss message", true);
    
    addButton.setVisible(false);
    modifyButton.setVisible(false);
    deleteButton.setVisible(false);
    buttonPanel.add(refreshButton);
    refreshButton.setVisible(true);
    
    refreshButtonPressed();
  }

  /*========================================================================*/
  /*  OVERRIDDEN METHODS                                                    */
  /*========================================================================*/
  
  /**
   * Class for Role Options
   * 
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#getRoleOptionsClass()
   */
  @Override
  protected Class getRoleOptionsClass()
  {
    return JBossMessageListFrame.class;
  }
  
  /**
   * Refreshed button pressed
   * 
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#refreshButtonPressed()
   */
  @Override
  protected void refreshButtonPressed()
  {
    try
    {
      List<Map> vpResults = mpJBossDBObj.execute(JBOSS_QUERY_MSG1).getRows();
      for (Map m : vpResults)
      {
        try
        {
          byte[] vabMessage = DBHelper.readBlob(JBOSS_DB, MESSAGE_NAME,
              JBOSS_QUERY_MSG2 + m.get("MESSAGEID"));
          m.put(MESSAGE_NAME, bytesToString(vabMessage));
        }
        catch (DBException dbe)
        {
          m.put(MESSAGE_NAME, "Message consumed before BLOB data was read.");
        }
      }
      
      refreshTable(vpResults);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }
  
  /**
   * 
   * @see com.daifukuamerica.wrxj.swing.SKDCInternalFrame#shutdownFrame()
   */
  @Override
  protected void shutdownFrame()
  {
    try
    {
      if (mpJBossDBObj.isConnectionActive())
      {
        mpJBossDBObj.disconnect();
      }
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
    super.shutdownFrame();
  }

  /*========================================================================*/
  /*  PRIVATE METHODS                                                       */
  /*========================================================================*/

  /**
   * Get a String representation of a byte array
   * 
   * @param iabBytes
   * @return
   */
  private String bytesToString(byte[] iabBytes)
  {
    StringBuffer vpSB = new StringBuffer(iabBytes.length);
    for (byte b : iabBytes)
    {
      if (b == 0)
      {
        vpSB.append("_");
      }
      else if (b > 0x20)
      {
        vpSB.append((char)b);
      }
      else if (b < 0)
      {
        vpSB.append("[0x");
        vpSB.append(Integer.toHexString(b).substring(6).toUpperCase());
        vpSB.append("]");
      }
      else
      {
        vpSB.append("[0x");
        if (b < 16)
        {
          vpSB.append("0");
        }
        vpSB.append(Integer.toHexString(b).toUpperCase());
        vpSB.append("]");
      }
    }
    
    // Attempt to parse
    String vsReturn = "<html>" + vpSB.toString() + "</html>";
    vsReturn = vsReturn.replaceFirst(
        getReplaceRegexString("[0xAC][0xED]_[0x05]"),
        getReplaceToString("[START]"));
    vsReturn = vsReturn.replaceFirst(
        getReplaceRegexString("[0x04][0x03]_[0x09]WRxJTopic____[0x01]"),
        getReplaceToString("[TOPIC]"));

    // Last byte is length
    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("[0x01][0x09][0xFF][0xFF][0xFF][0xFF]_[0x09]__[0x01][0x07]_[0x04]"),
        getReplaceToString("[MSG]"));
    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("[0x01][0x09][0xFF][0xFF][0xFF][0xFF]_[0x09]__[0x01][0x07]_[0x05]"),
        getReplaceToString("[MSG]"));
    
    // Last byte is length
    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("___[0x04][0x07]_[0x17]"),
        getReplaceToString("[HEADER]"));
    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("___[0x04][0x07]_[0x18]"),
        getReplaceToString("[HEADER]"));
    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("___[0x04][0x07]_[0x19]"),
        getReplaceToString("[HEADER]"));
    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("___[0x04][0x07]_[0x1A]"),
        getReplaceToString("[HEADER]"));

    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("___[0x06]_[0x07]EvtType[0x02]___"),
        getReplaceToString("GREEN", "(EvtType)"));

    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("_[0x09]MsgTxTime[0x03]__[0x01]+[0x0D]"),
        getReplaceToString("GREEN", "(MsgTxTime)"));

    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("_[0x05]Event[0x07]_"),
        getReplaceToString("GREEN", "(Event)"));

    int vnStatus1 = vsReturn.lastIndexOf("</font>") + 7;
    int vnStatus2 = vsReturn.indexOf("_", vnStatus1);
    String vsMessageL = vsReturn.substring(vnStatus1, vnStatus2);
    String vsMessage = vsReturn.substring(vnStatus1+6, vnStatus2);
    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString(vsMessageL),
        getReplaceToString("#8A2BE2", vsMessage));

    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("_[0x07]MsgSndr[0x07]_"),
        getReplaceToString("GREEN", "(MsgSndr)"));

    int vnSender1 = vsReturn.lastIndexOf("</font>") + 7;
    int vnSender2 = vsReturn.indexOf("_", vnSender1);
    String vsSenderL = vsReturn.substring(vnSender1, vnSender2);
    String vsSender = vsReturn.substring(vnSender1+6, vnSender2);
    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString(vsSenderL),
        getReplaceToString("#8A2BE2", vsSender));

    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("_[0x09]MsgSndrCG[0x07]_[0x05]Ctlrs_"),
        getReplaceToString("GREEN", "(MsgSndrCG-Ctlrs)"));

    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("[0x07]MsgData[0x03]_"),
        getReplaceToString("GREEN", "(MsgData)"));

    // Markers
    vsReturn = vsReturn.replaceAll(
        getReplaceRegexString("[0x01]"),
        getReplaceToString("RED", "[0x01]"));
    
    // Return
    return vsReturn;
  }
  
  /**
   * Escape regex characters
   * 
   * @param isReplace
   * @return
   */
  private String getReplaceRegexString(String isReplace)
  {
    String vsRegex = "";
    for (char c : isReplace.toCharArray())
    {
      switch (c)
      {
        case '[':
        case ']':
        case '+':
        case '-':
          vsRegex += '\\';
          // intentional fall-through
        default:
          vsRegex += c;
      }
    }
    return vsRegex;
  }

  /**
   * Get a string with formatting
   * @param isColor
   * @param isTitle
   * @return
   */
  private String getReplaceToString(String isColor, String isTitle)
  {
    return "<font COLOR=" + isColor + ">" + isTitle + "</font>";
  }

  /**
   * Get a string with formatting
   * 
   * @param isTitle
   * @return
   */
  private String getReplaceToString(String isTitle)
  {
    return getReplaceToString("BLUE", isTitle);
  }
}
