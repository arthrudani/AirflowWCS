package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Description:<BR>
 *    Host data display frame for viewing CLOB data.
 *
 * @author       A.D.
 * @version      1.0      05-Jun-05
 */
@SuppressWarnings("serial")
public class XMLHostDataViewFrame extends SKDCInternalFrame 
{
  private boolean            mzInitialRow;
  private SKDCButton         mpBtnLast;
  private SKDCButton         mpBtnNext;
  private SKDCButton         mpBtnPrev;
  private HostServerDelegate mpTheDelegate;
  private StandardHostServer mpHostServ;
  private ListIterator       mpLstItr;
  private JEditorPane        mpPnlEditPane;
  
 /**
  * Constructor for displaying Host Message Content.
  * @param ipDataInfo a reference to the current type of data object to use for
  *        display.  This parameter is used to determine if the data represents
  *        an inbound or outbound host message. References should be of type
  *        {@link com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData#HostToWrxData HostToWrxData} or
  *        {@link com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData#WrxToHostData WrxToHostData} 
  * @param ipDelegate 
  * @param ipSelectionList 
  */
  public XMLHostDataViewFrame(HostServerDelegate ipDelegate, List ipSelectionList)
  {
    super("Host Data View");
    mpTheDelegate = ipDelegate;
    if (ipSelectionList == null || ipSelectionList.isEmpty())
    {
      displayError("No data selected/found to display...");
      return;
    }
    mpLstItr = ipSelectionList.listIterator();
    
    mpHostServ = Factory.create(StandardHostServer.class, "HostDataView");
                                       // Get content pane of this Internal
                                       // Frame, and add the panel
    Container vpCp = getContentPane();                                     
    vpCp.add(initDataView(), BorderLayout.CENTER);
    vpCp.add(buttonPanel(), BorderLayout.SOUTH);
  }
  
  @Override
  public Dimension getPreferredSize()
  {
    return(new Dimension(700, 600));
  }
 
  private void previousButtonPressed()
  {
    if (mzInitialRow)
    {
      displayInfo("No previous references available.");
      return;
    }
    
    if (mpBtnLast == mpBtnNext)
    {
      if (mpLstItr.hasPrevious())
      {
        mpLstItr.previous();
      }
    }

    byte[] vabXmlData;
    if (mpLstItr.hasPrevious())
    {
      Map vpPreviousMap = (Map)mpLstItr.previous();
      vabXmlData = getXMLData(vpPreviousMap);
      int vnMessageSequence = DBHelper.getIntegerField(vpPreviousMap, "IMESSAGESEQUENCE");
      Date vdMessageDate = DBHelper.getDateField(vpPreviousMap, "DMESSAGEADDTIME");
      String vsMessageID = DBHelper.getStringField(vpPreviousMap, "SMESSAGEIDENTIFIER");
      mpPnlEditPane.setText("Seq: " + vnMessageSequence + 
                            "\nTime: " + vdMessageDate + 
                            "\nMsg Name: " + vsMessageID + 
                            "\nMessage: " + new String(vabXmlData));
    }
    else
    {
      displayInfo("No previous references available.");
    }

    mpBtnLast = mpBtnPrev;
  }
  
  private void nextButtonPressed()
  {
    mzInitialRow = false;

    if (mpBtnLast == mpBtnPrev)
    {
      if (mpLstItr.hasNext())
      {
        mpLstItr.next();
      }
    }
    
    byte[] vabXmlData;
    if (mpLstItr.hasNext())
    {
      Map nextMap = (Map)mpLstItr.next();
      vabXmlData = getXMLData(nextMap);
      int vnMessageSequence = DBHelper.getIntegerField(nextMap, "IMESSAGESEQUENCE");
      Date vdMessageDate = DBHelper.getDateField(nextMap, "DMESSAGEADDTIME");
      String vsMessageID = DBHelper.getStringField(nextMap, "SMESSAGEIDENTIFIER");
      mpPnlEditPane.setText("Seq: " + vnMessageSequence + 
                            "\nTime: " + vdMessageDate + 
                            "\nMsg Name: " + vsMessageID + 
                            "\nMessage: " + new String(vabXmlData));
    }
    else
    {
      displayInfo("No more data available.");
    }
    
    mpBtnLast = mpBtnNext;
  }

 /**
  *  Builds data view panel.
  */
  private JScrollPane initDataView()
  {
    Map vpNextMap = (Map)mpLstItr.next();
    final byte[] vabXmlData = getXMLData(vpNextMap);
    mpPnlEditPane = new JEditorPane();
    mpPnlEditPane.setEditable(false);
    int vnMessageSequence = DBHelper.getIntegerField(vpNextMap, "IMESSAGESEQUENCE");
    Date vdMessageDate = DBHelper.getDateField(vpNextMap, "DMESSAGEADDTIME");
    String vsMessageID = DBHelper.getStringField(vpNextMap, "SMESSAGEIDENTIFIER");
    mpPnlEditPane.setText("Seq: " + vnMessageSequence + 
                          "\nTime: " + vdMessageDate + 
                          "\nMsg Name: " + vsMessageID + 
                          "\nMessage: " + new String(vabXmlData));
    mpPnlEditPane.setSize(700, 600);
    mzInitialRow = true;
    
    return(new JScrollPane(mpPnlEditPane));
  }
  
 /**
  *  Adds Buttons to a panel.
  */
  private JPanel buttonPanel()
  {
    JPanel vpBtnPanel = getEmptyButtonPanel();
    
    mpBtnNext = new SKDCButton(" Next ", " View Next Message ", 'N');
    mpBtnPrev = new SKDCButton(" Previous ", " View Previous Message ", 'P');

    NextActionListener nextActionListener = new NextActionListener();
    mpBtnNext.addEvent(NEXT_BTN, nextActionListener);
    mpBtnPrev.addEvent(PREV_BTN, nextActionListener);

    vpBtnPanel.add(mpBtnPrev);
    vpBtnPanel.add(mpBtnNext);

    return(vpBtnPanel);
  }
  
  private byte[] getXMLData(Map dataMap)
  {
    byte[] vabXmlData = null;
    String vsHostName = DBHelper.getStringField(dataMap, "SHOSTNAME");
    int vnMessageSequence = DBHelper.getIntegerField(dataMap, "IMESSAGESEQUENCE");
    try
    {
      if (mpTheDelegate instanceof HostOutDelegate)
      {
        WrxToHostData wrxToHostData = Factory.create(WrxToHostData.class);
        wrxToHostData.setHostName(vsHostName);
        wrxToHostData.setMessageSequence(vnMessageSequence);
        wrxToHostData.setClobRetrieval(true);
        mpTheDelegate.setInfo(wrxToHostData);
        wrxToHostData = (WrxToHostData)mpHostServ.getDataQueueMessage(mpTheDelegate);
        if (wrxToHostData == null)
        {
          displayInfo("No outbound data found for message sequence " + vnMessageSequence + 
                      " and host " + vsHostName);
        }
        else
        {
          vabXmlData = wrxToHostData.getMessageBytes();
        }
      }
      else
      {
        HostToWrxData vpHostToWrxData = Factory.create(HostToWrxData.class);
        vpHostToWrxData.setHostName(vsHostName);
        vpHostToWrxData.setMessageSequence(vnMessageSequence);
        vpHostToWrxData.setClobRetrieval(true);
        mpTheDelegate.setInfo(vpHostToWrxData);
        vpHostToWrxData = (HostToWrxData)mpHostServ.getDataQueueMessage(mpTheDelegate);
        if (vpHostToWrxData == null)
        {
          displayInfo("No inbound data found for message sequence " + vnMessageSequence + 
                      " and host " + vsHostName);
        }
        else
        {
          vabXmlData = vpHostToWrxData.getMessageBytes();
        }
      }
    }
    catch (DBException e)
    {
      displayError(e.getMessage());
    }
    
    return(vabXmlData);
  }
  
  /**
   *  Button Listener class for processing next and previous message view requests.
   */
  private class NextActionListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String vsWhichButton = e.getActionCommand();
      if (vsWhichButton.equals(NEXT_BTN))
      {
        nextButtonPressed();
      }
      else if (vsWhichButton.equals(PREV_BTN))
      {
        previousButtonPressed();
      }
    }
  }
}
