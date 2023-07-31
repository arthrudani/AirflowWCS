package com.daifukuamerica.wrxj.swingui.emulation;

import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.device.port.PortConsts;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCRadioButton;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.time.SkDateTime;
import com.daifukuamerica.wrxj.util.SkdRtException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class ControlsDeviceEmulationFrame extends SKDCInternalFrame
{
  protected int mnTabCount = 4;

  /**
   * TabbedPaneDemo - constructor and initialization
   *
   */
  public ControlsDeviceEmulationFrame()
  {
    JTabbedPane vpTabPane = new JTabbedPane();
    Dimension vpPanelSize = new Dimension(530, 635);

    for (int i = 0; i < mnTabCount; i++)
    {
      JComponent vpPanel = new ControlsPanel().buildPanel("Connection " + (i+1));
      vpPanel.setPreferredSize(vpPanelSize);
      vpTabPane.addTab("Connection " + (i+1), null, vpPanel, "");
      vpTabPane.setMnemonicAt(i, KeyEvent.VK_1 + i);
    }

    //Add the tabbed pane to this panel.
    getContentPane().add(vpTabPane);
  }
  
  /**
   * Get a JPanel with a BoxLayout
   * 
   * @param whataxis
   * @return
   */
  protected JPanel buildBox(int whataxis)
  {
    JPanel thisbox = new JPanel();
    thisbox.setLayout(new BoxLayout(thisbox, whataxis));
    return(thisbox);
  }
    
  /**
   * Class ControlsPanel -This is the object used to fill each tabbed panel
   * because We want a separate connection for each panel
   */
  class ControlsPanel 
  {
                    // General Variables etc. used through out
    private String            RADIO_CONNECT   = "RConnect";
    private String            RADIO_LISTEN    = "RListen";
    private String            RADIO_SCROLL_ON = "RSCROLLON";
    private String            RADIO_SCROLL_OFF= "RSCROLLOFF";
    private String            IP_RADIO_IP     = "IP_RIP";
    private String            IP_RADIO_HOST   = "IP_RHost";
    private String            MSG_RADIO_AR    = "MSG_RADIO_AR";
    private String            MSG_RADIO_ST    = "MSG_RADIO_ST";
    private String            MSG_RADIO_FC    = "MSG_RADIO_FC";
    private String            MSG_RADIO_OR    = "MSG_RADIO_OR";
    private String            MSG_RADIO_TM    = "MSG_RADIO_TM";
    private String            MSG_RADIO_TR    = "MSG_RADIO_TR";
    private String            MSG_RADIO_FREE  = "MSG_RADIO_FF";
    private String            BTN_CONNECT     = "BtnConnect";
    private String            BTN_DISCONNECT  = "BtnDisConnect";
    private String            BTN_SEND        = "BtnSend";
    private String            BTN_CLEAR       = "BtnClear";
    private int cpSequence;
    private int cpARLoadSequence;
    private int Xaxis = BoxLayout.X_AXIS;
    private int Yaxis = BoxLayout.Y_AXIS;
    static final String STATUS_TIME_FORMAT = "ddMMM HH:mm:ss.SSS";
    protected String lastAR;
    protected String testMessage = "TMTestMsgFromPLC";
    protected Point viewpoint = new Point();
    protected int viewx = 1;
    protected int viewy = 1;
    protected int numlogs = 0;
    protected boolean sendingAR = false;
    protected boolean sendingTM = false;
    protected JViewport viewport;
    
                         // Socket Connection Stuff
    protected String hostName;
    protected String portID;
    protected String retryInterval = "1000";
    protected String portType;
    protected Socket cpSocket;
    protected ServerSocket cpListenServerSocket;
    protected InputStream  inputStream;
    protected OutputStream outputStream;
    
                         // Buttons and Listener
    private ButtonListener buttonListener = new ButtonListener();
    protected SKDCButton    cpbtnConnect = new SKDCButton("Connect");
    protected SKDCButton    cpbtnDisconnect = new SKDCButton("Disconnect");
    protected SKDCButton    cpbtnSend = new SKDCButton("Send");
    protected SKDCButton    cpbtnClear = new SKDCButton("Clear Logging");

                         // Text Fields      
    protected SKDCIntegerField cpTxtIP1 = new SKDCIntegerField(3);
    protected SKDCIntegerField cpTxtIP2 = new SKDCIntegerField(3);
    protected SKDCIntegerField cpTxtIP3 = new SKDCIntegerField(3);
    protected SKDCIntegerField cpTxtIP4 = new SKDCIntegerField(3);
    protected SKDCIntegerField cpTxtPort = new SKDCIntegerField(7);
    protected SKDCTextField cpTxtHostName = new SKDCTextField(15);
    protected SKDCTextField cpTxtFreeForm = new SKDCTextField(28);
    protected SKDCTextField cpTxtLoad1 = new SKDCTextField(8);
    protected SKDCTextField cpTxtLoad2 = new SKDCTextField(8);
    protected SKDCTextField cpTxtStation = new SKDCTextField(4);
    protected SKDCTextField cpTxtStatus = new SKDCTextField(2); 
    
                               // Text Area and Scroll Pane
    protected JScrollPane   cpScrollArea = new JScrollPane();
    protected JTextArea     cpTextArea = new JTextArea("");

                                // Labels
    protected SKDCLabel     cpIPLabel = new SKDCLabel("            Host IP Address: ");
    protected SKDCLabel     cpHostLabel = new SKDCLabel("Host: ");
    protected SKDCLabel     cpPortLabel = new SKDCLabel("   Port Number: ");    
    protected SKDCLabel     cpStatusLabel = new SKDCLabel("Unknown");
    protected SKDCLabel     cpSettingLabel = new SKDCLabel("Settings");
    protected SKDCLabel     cpMessageTypeLabel = new SKDCLabel("AR ");
    protected String        cpSendMsgType = "AR";
    protected SKDCLabel     cpFreeFormLabel = new SKDCLabel("Free Form:");
    protected SKDCLabel     cpMsgBlank1 = new SKDCLabel("  ");
    protected SKDCLabel     cpMsgBlank2 = new SKDCLabel("  ");
    protected SKDCLabel     cpMsgBlank3 = new SKDCLabel("  ");
    protected SKDCLabel     cpNumLogsLabel = new SKDCLabel(" Logs: 0");
    protected SKDCLabel     cpMsgFormatLabel = new SKDCLabel(
     "<STX> SEQN  Msg           Load1                  Load2         Station   Status  <ETX>          ");
    protected SKDCLabel     cpConnectStatusLabel = new SKDCLabel("Connection Status:  ");
    protected SKDCLabel     cpConnectionLabel = new SKDCLabel("Connection");
    protected SKDCLabel     cpLoggingFieldsLabel = new SKDCLabel();
    protected SKDCLabel     cpCurrentStateLabel = new SKDCLabel("  Current State");
    
                                  // Check Boxes
    protected SKDCCheckBox  cpChkBxContinAR =   new SKDCCheckBox("Repeating AR");
    protected SKDCCheckBox  cpChkBxContinTM =   new SKDCCheckBox("Repeating TM");
    protected SKDCCheckBox  cpChkBxAutoSendFC = new SKDCCheckBox("Auto Send FC");
    protected SKDCCheckBox  cpChkBxAutoSendTR = new SKDCCheckBox("Auto Send TR");
    protected SKDCCheckBox  cpChkBxAutoSendST = new SKDCCheckBox("Auto Send ST");
    
                                 // Radio Buttons and Button Groups for Socket Connections
    protected ButtonGroup cpSocketGroup = new ButtonGroup();
    protected ButtonGroup cpIPGroup = new ButtonGroup();
    protected SKDCRadioButton  cpRBtnIP = new SKDCRadioButton("", 'I');
    protected SKDCRadioButton  cpRBtnHostName = new SKDCRadioButton("", 'H');
    protected SKDCRadioButton  cpRBtnConnect = new SKDCRadioButton("Connect", 'C');
    protected SKDCRadioButton  cpRBtnListen = new SKDCRadioButton( "Listen", 'L');

                                 // Radio Buttons and Button Groups for Scroll Tracking
    protected ButtonGroup cpScrollGroup = new ButtonGroup();
    protected SKDCRadioButton  cpRBtnScrollOff = new SKDCRadioButton("Off", 'F');
    protected SKDCRadioButton  cpRBtnScrollOn = new SKDCRadioButton("Tracking Scroll On", 'O');
    
                                 //  Radio Buttons and Button Groups for Messaging 
    protected ButtonGroup cpMsgGroup = new ButtonGroup();
    protected SKDCRadioButton  cpRBtnMsgAR = new SKDCRadioButton("AR", 'a');  // Arrival
    protected SKDCRadioButton  cpRBtnMsgST = new SKDCRadioButton("ST", 's');  // Status
    protected SKDCRadioButton  cpRBtnMsgOR = new SKDCRadioButton("OR", 'o');  // Order
    protected SKDCRadioButton  cpRBtnMsgFC = new SKDCRadioButton("FC", 'f');  // Flush Complete
    protected SKDCRadioButton  cpRBtnMsgTM = new SKDCRadioButton("TM", 'r');  // Test Message
    protected SKDCRadioButton  cpRBtnMsgTR = new SKDCRadioButton("TR", 't');  // Test Response
    protected SKDCRadioButton  cpRBtnFreeForm = new SKDCRadioButton("FreeForm", 'f');  // Free Style Message
    protected Font myFont =  new Font("Monospaced", Font.BOLD, 12);
    protected Font myPlainFont = new Font("Monospaced", Font.PLAIN, 12);

    /**
     * Constructor and initialization 
     */
    public ControlsPanel()
    {
      cpSequence = 0;
      cpARLoadSequence = 0;
              // Do the IP/Host radio group
      cpRBtnIP.eventListener(IP_RADIO_IP, buttonListener);
      cpIPGroup.add(cpRBtnIP);
                    
      cpRBtnHostName.eventListener(IP_RADIO_HOST, buttonListener);
      cpIPGroup.add(cpRBtnHostName);
      
              // Do the Socket connection type radio group
      cpRBtnConnect.eventListener(RADIO_CONNECT, buttonListener);
      cpSocketGroup.add(cpRBtnConnect);
            
      cpRBtnListen.eventListener(RADIO_LISTEN, buttonListener);
      cpSocketGroup.add(cpRBtnListen);
      
              // Do the Message type radio group
      cpRBtnMsgAR.eventListener(MSG_RADIO_AR, buttonListener);
      cpMsgGroup.add(cpRBtnMsgAR);
            
      cpRBtnMsgST.eventListener(MSG_RADIO_ST, buttonListener);
      cpMsgGroup.add(cpRBtnMsgST);
            
      cpRBtnMsgOR.eventListener(MSG_RADIO_OR, buttonListener);
      cpMsgGroup.add(cpRBtnMsgOR);
            
      cpRBtnMsgFC.eventListener(MSG_RADIO_FC, buttonListener);
      cpMsgGroup.add(cpRBtnMsgFC);
            
      cpRBtnMsgTR.eventListener(MSG_RADIO_TR, buttonListener);
      cpMsgGroup.add(cpRBtnMsgTR);
            
      cpRBtnMsgTM.eventListener(MSG_RADIO_TM, buttonListener);
      cpMsgGroup.add(cpRBtnMsgTM);
      
      cpRBtnFreeForm.eventListener(MSG_RADIO_FREE, buttonListener);
      cpMsgGroup.add(cpRBtnFreeForm);
      
             // Do the Scroll Tracking radio group
      cpRBtnScrollOn.eventListener(RADIO_SCROLL_ON, buttonListener);
      cpScrollGroup.add(cpRBtnScrollOn);
                  
      cpRBtnListen.eventListener(RADIO_SCROLL_OFF, buttonListener);
      cpScrollGroup.add(cpRBtnScrollOff);

      cpTextArea.setTabSize(2);
      cpbtnSend.setEnabled(false);
      cpbtnDisconnect.setEnabled(false);
         
    }
    
   /**
    * Set the port connection status and enable or disable the send button
    * @param newStatus
    */ 

    public void setStatus(String newStatus)
    {
      cpStatusLabel.setText(newStatus);
      if(newStatus.equals("CONNECTED"))
      {
        cpbtnSend.setEnabled(true);
        cpbtnConnect.setEnabled(false);
        cpbtnDisconnect.setEnabled(true);
        setCurrentState(newStatus);
      }
      else
      {
        cpbtnSend.setEnabled(false);
      }
      if(newStatus.equals("CONNECTING"))
      {
        cpbtnConnect.setEnabled(false);
        cpbtnDisconnect.setEnabled(true);
      }
      if(newStatus.equals("DISCONNECTED"))
      {
        setCurrentState(newStatus);
        cpbtnDisconnect.setEnabled(false);
        cpbtnConnect.setEnabled(true);
      }
      
    }
    
    /**
     * Add messages we send or receive to our scrolled logging area
     * @param newMsg
     */
    protected void addLog(String newMsg)
    {
      String tmpString;

//      tmpString = newMsg + "\n";

      if(cpTextArea.getText().trim().length() < 1)
      {
        tmpString = newMsg;
      }
      else
      {
        tmpString = "\n" + newMsg;
      }


      cpTextArea.append(tmpString);

      numlogs = numlogs + 1;
      cpNumLogsLabel.setText(" Logs: " + numlogs);

      if(cpRBtnScrollOn.isSelected())
      {
        viewy = Integer.valueOf(cpTextArea.getHeight() - viewport.getHeight()).intValue();
        viewx = 1;
        viewpoint.setLocation(viewx, viewy);
        viewport.setViewPosition(viewpoint);
        viewport.scrollRectToVisible(viewport.getViewRect());
      }
    }
    
    protected void clearLogging()
    {
      viewx = 1;
      viewy = 1;
      numlogs = 0;
      cpNumLogsLabel.setText(" Logs: 0");
      cpTextArea.setText("");
    }
  
    protected void setCurrentState(String msg)
    {

      cpCurrentStateLabel.setText(" " + msg);
    }      
    /**
     * Builds the message to send based on radio buttons and fields etc.
     */ 
    protected void buildMessage()
    {
      String load1 = cpTxtLoad1.getText() + "        ";
      String load2 = cpTxtLoad2.getText() + "        ";
      String stn   = cpTxtStation.getText() + "    ";
      String status= cpTxtStatus.getText() + "  ";
      String freeform = cpTxtFreeForm.getText() + "                            ";
      

      String msgToSend;
      if(cpSendMsgType.equals("FF"))
      {
        msgToSend = freeform.substring(0,28);
        SkDateTime curDate = new SkDateTime(STATUS_TIME_FORMAT);
                          
        String logmsg = curDate.getCurrentDateTimeAsString() + "  " + msgToSend.substring(0,4) 
                        + "  " +  cpSendMsgType + "   DEVICE  " + msgToSend;
        transmitData(msgToSend);
        addLog(logmsg);
        return;
      }
      else
      {
        if(cpSendMsgType.equals("AR"))
        {
          msgToSend =cpSendMsgType + load1.substring(0,8) +
                                    load2.substring(0,8) + stn.substring(0,4) + status.substring(0,2);

          if(cpChkBxContinAR.isSelected() && sendingAR == false)
          {
            sendingAR = true;
            lastAR = msgToSend;
            ContinuousARThread thisAR = new ContinuousARThread();
            thisAR.start();
          }

        }
        else
        {
          
          msgToSend = cpSendMsgType + load1.substring(0,8) +
                       stn.substring(0,4) + status.substring(0,2);
        }
        if(cpSendMsgType.equals("TM") && sendingTM == false)
        {
          sendingTM = true;
          ContinuousTMThread thisTM = new ContinuousTMThread();
          thisTM.start();
        }

      }

      sendMessage(msgToSend);             

    }      
    /**
     * Gets the latest sequence number and appends it and logs the message
     * @param message
     */

    protected void sendMessage(String message)
    {
      String sequence = getSequence();
      String dataToSend = sequence + message;
                        
      SkDateTime curDate = new SkDateTime(STATUS_TIME_FORMAT);
                          
      String logmsg = curDate.getCurrentDateTimeAsString() + "  " + sequence + "  " + 
                      message.substring(0,2) + "   DEVICE  " + dataToSend;
      transmitData(dataToSend);
      addLog(logmsg);
    }
    
    /**
     * Does the actual transmission of the data over the socket
     * @param msg
     */             
    protected void transmitData(String msg)
    {
      int stxlength = 1;
      int etxlength = 1;
      int msglength;
      int writelength;
      byte[] stxbytes = {PortConsts.STX};
      byte[] etxbytes = {PortConsts.ETX};
      byte[] msgbytes = msg.getBytes();
      byte[] outputByteBuffer = new byte[PortConsts.COM_DEVICE_OUTPUT_BUFFER_SIZE];
      
      msglength = msg.length();
      System.arraycopy(stxbytes, 0,
                       outputByteBuffer, 0, stxlength);
      System.arraycopy(msgbytes, 0,
                     outputByteBuffer, stxlength, msglength);
      System.arraycopy(etxbytes, 0,
                       outputByteBuffer, stxlength + msglength, etxlength);
      writelength = msg.length() + stxlength + etxlength;
      try
      {     
//        addLog("Writing message: '" + msg + "' to socket");     
        outputStream.write(outputByteBuffer, 0, writelength);

      }
      catch(IOException ioe)
      {
        addLog("Error Writing message: " + msg + " to socket: " + ioe.getMessage());
        setCurrentState("Error Writing message: " + msg + " to socket: " + ioe.getMessage());
        setStatus("ERROR");
      }
      catch(Throwable e)
      {
        throw new SkdRtException(
          "Socket TCP/IP send failed: " + e.getMessage(), e);
      }
    }   
    /**
     * Logs and Processes Received messages
     * @param rcvMsg
     */  
    protected void receiveMessage(String rcvMsg)
    {
      String load1;
      String load2;
      String stn;
      String status;
      String msgType = rcvMsg.substring(4,6);
      String inboundSequence = rcvMsg.substring(0,4);
      String tmpType = "XX";
      boolean autosend = false;
      
      if(msgType.equals("AR"))
      {
        load1 = rcvMsg.substring(6,14);
        load2 = rcvMsg.substring(14,22);
        if(load2.equals(""));
        stn = rcvMsg.substring(22,26);
        status = rcvMsg.substring(26,28);
      }
      else
      {
        load1 = rcvMsg.substring(6,14);
        stn = rcvMsg.substring(14,18);
        status = rcvMsg.substring(18,20);
      } 
                          
      SkDateTime curDate = new SkDateTime(STATUS_TIME_FORMAT);
                          
      String logmsg = curDate.getCurrentDateTimeAsString() + "  " + inboundSequence + "  " + 
                      msgType + "   HOST    " + rcvMsg;
      addLog(logmsg);
      
                  // Now if we have autoSend check boxes...go ahead and send those messages
      if(msgType.equals("TM")  && cpChkBxAutoSendTR.isSelected())
      {          
                    // send a Test Response Here
        tmpType = "TR";
        autosend = true;
   
      }
      else if(msgType.equals("FL")  && cpChkBxAutoSendFC.isSelected())
      {
                    // Send a Flush Complete message here       
        load1 = "00000000";
        tmpType = "FC";
        status = "00";
        autosend = true;
      }
      else if(msgType.equals("ST")  && cpChkBxAutoSendST.isSelected())
      {
                    // Send a Status message here
        tmpType = "ST";
        load1 = "00000000";
          autosend = true;

        }
        if(autosend == true)
        {
          String msgToSend = tmpType + load1 +  stn + status;                         
          sendMessage(msgToSend);
        }
      }
      
      /**
       * getSequence -
       * gets the next sequence number and converts it to a 4 character string and check 
       * correct values of the sequence number making sure it doesn't get too large etc.
       * @return
       */
      private String getSequence()
      {
        String zeros = "0000";
      String sSequence;
      if(cpSequence < 10)
      {
        sSequence = zeros.substring(0,3) + cpSequence;
      }
      else if(cpSequence >= 10 && cpSequence < 100)
      {
        sSequence = zeros.substring(0,2) + cpSequence;
      }
      else if(cpSequence >= 100 && cpSequence < 1000)
      {
        sSequence = zeros.substring(0,1) + cpSequence;
      }
      else
      {
        // it must be greater than 1000 so no zero padding
        sSequence = "" + cpSequence;
      }
        
      cpSequence++;
      if(cpSequence > 9999)
      {
        cpSequence = 0;
      }
      return (sSequence);
    }
      
    /**
     * Build the Panel for this pane with all it's various sections, buttons etc.
     * @param panelText
     * @return
     */ 
    public JPanel buildPanel(String panelText)
    {
      if (panelText.equals(""))    // Just done so the "variable not used" warning does not appear
      {
        ;
      }
                // Get the Connection Panel
      JPanel connectionPanel = getConnectionPanel();

                // Get the Messaging Panel
      JPanel messagePanel = getMessagePanel();

                // Get the logging Panel
      JPanel loggingPanel = getLoggingPanel();

                // Add the connection area, the messaging area and the logging area
                // to a panel and then add that panel to the main panel we are returning
      JPanel allThree = buildBox(Yaxis);
      allThree.add(connectionPanel);
      allThree.add(messagePanel);
      allThree.add(loggingPanel);
    
      JPanel newPanel = new JPanel( new GridBagLayout()); 
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(1, 1, 1, 1);
      
             // Specifications applicable to all
             
             // columns and rows go in this section
      gbc.gridwidth = 1;         // how many cells per row
      gbc.gridheight = 1;        // how many cells per column
      gbc.weightx = 1;  
      gbc.weighty = 1;

                                         // Define the columns.

      gbc.anchor = GridBagConstraints.NORTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      newPanel.setPreferredSize(new Dimension(1000,500));
      
             // Add the connection area label
      newPanel.add(allThree, gbc);
      return(newPanel);
    }
    
    /**
     * getConnectionPanel - Build the "Connection" Section panel
     * @return
     */     
    protected JPanel getConnectionPanel()
    {
      JPanel cpanel = new JPanel(new GridBagLayout());
      cpanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), "Connection"));
                                         // Create Grid bag constraints.
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(2, 2, 2, 2);
             // Specifications applicable to all
             
             // columns and rows go in this section
      gbc.gridwidth = 1;         // how many cells per row
      gbc.gridheight = 1;        // how many cells per column
      gbc.weightx = 0.2;  
      gbc.weighty = 0.5;
     
                          // Build Various Boxes to add together to make the panel look good
      JPanel ipBox = buildBox(Xaxis);
               //      Add the IP address Label 
      ipBox.add(cpIPLabel);  
               // Add the IP address field 
      cpTxtIP1.setEnabled(false);
      cpTxtIP2.setEnabled(false);
      cpTxtIP3.setEnabled(false);
      cpTxtIP4.setEnabled(false);
      ipBox.add(cpTxtIP1);
      ipBox.add(cpTxtIP2);
      ipBox.add(cpTxtIP3);
      ipBox.add(cpTxtIP4);
 
        JPanel hostBox = buildBox(Xaxis);
                  //      Add the IP address and the host name 
      hostBox.add(cpHostLabel);  
      hostBox.add(cpTxtHostName);
            
      JPanel iphostBox = buildBox(Yaxis);
                //      Add the IP address and the host name        
      iphostBox.add(hostBox);
      iphostBox.add(ipBox); 

      
      JPanel ipPickBox = buildBox(Yaxis);
      

                //      Add the IP address Label    
      cpRBtnHostName.setSelected(true);
      ipPickBox.add(cpRBtnHostName);
      ipPickBox.add(cpRBtnIP);

      
      JPanel allHostBox = buildBox(Xaxis);      
      allHostBox.add(ipPickBox);
      allHostBox.add(iphostBox);
      
      
      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      JPanel portBox = buildBox(Xaxis);
             // Add the Port Label
      portBox.add(cpPortLabel);  
            // Add the Port Field
      portBox.add(cpTxtPort);
      cpanel.add(portBox, gbc);
      
      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      cpanel.add(allHostBox, gbc);
      

      JPanel statusBox = buildBox(Xaxis);
                 // Add the Connection Status Label
      statusBox.add(cpConnectStatusLabel);
                 // Add the Actual Status Label "Field"
      statusBox.add(cpStatusLabel);       

      gbc.anchor = GridBagConstraints.CENTER;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      cpanel.add(statusBox, gbc);
    
                  // Make the connection radio group into a panel to add here                   
      JPanel conRadioPanel = buildBox(Xaxis);

      cpRBtnConnect.setSelected(true);
      conRadioPanel.add(cpRBtnConnect);
      conRadioPanel.add(cpRBtnListen);

      conRadioPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),
                                                            "Connection Type"));
                                                            
               // Add the Connection Type Radio Group              
      JPanel connButtons = buildBox(Xaxis);
      connButtons.add(conRadioPanel);
      cpbtnConnect.addEvent(BTN_CONNECT, buttonListener);
      cpbtnDisconnect.addEvent(BTN_DISCONNECT, buttonListener);
      connButtons.add(cpbtnConnect);
      connButtons.add(cpbtnDisconnect);
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      cpanel.add(connButtons, gbc);        

      return(cpanel);
    }
    
    /**
     * getMessagePanel - Build the "messaging" Section panel
     * @return
     */       
    protected JPanel getMessagePanel()
    {

         // FIRST....Make the Message radio group into a panel to add later 
                          
      JPanel msgRadioPanel = new JPanel(new GridBagLayout());
//        msgRadioPanel.setSize(100,100);
      GridBagConstraints gb2 = new GridBagConstraints();
      gb2.fill = GridBagConstraints.BOTH;
      gb2.weightx = 1.0;
                 // First Row
      cpRBtnMsgAR.setSelected(true);
      msgRadioPanel.add(cpRBtnMsgAR, gb2);     // AR
      gb2.gridwidth = GridBagConstraints.RELATIVE;     
      msgRadioPanel.add(cpRBtnMsgFC, gb2);     // FC
      gb2.gridwidth = GridBagConstraints.REMAINDER;
      msgRadioPanel.add(cpRBtnMsgOR, gb2);     // OR
                 // Next Row
      gb2.weightx = 1.0;
      gb2.gridwidth = GridBagConstraints.LINE_START; 
      msgRadioPanel.add(cpRBtnMsgST, gb2);
      gb2.gridwidth = GridBagConstraints.RELATIVE; 
      msgRadioPanel.add(cpRBtnMsgTR, gb2);
      gb2.gridwidth = GridBagConstraints.REMAINDER;
      msgRadioPanel.add(cpRBtnMsgTM, gb2);
                 // Next Row
      gb2.weightx = 0;
      gb2.gridwidth = GridBagConstraints.REMAINDER;
      msgRadioPanel.add(cpRBtnFreeForm, gb2);
                 

      msgRadioPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
                          "Message Type", TitledBorder.CENTER, TitledBorder.TOP));                                                   
                                                   
         // NOW...set up a panel for the settings checkboxes
                          
      JPanel msgCBoxPanel = new JPanel(new GridBagLayout());

      msgCBoxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
                           "Settings", TitledBorder.CENTER, TitledBorder.TOP));
                           
      JPanel autoSendBox = buildBox(Yaxis);
                      //      Add the Auto Send TR checkbox        
      cpChkBxAutoSendTR.setSelected(true);
      autoSendBox.add(cpChkBxAutoSendTR);
                      //      Add the Auto Send ST checkbox        
      cpChkBxAutoSendST.setSelected(true);
      autoSendBox.add(cpChkBxAutoSendST);
                      //      Add the Auto Send FC checkbox
      cpChkBxAutoSendFC.setSelected(true);
      autoSendBox.add(cpChkBxAutoSendFC);
    
                       // Add a "repeating" box
      JPanel repeatingBox = buildBox(Yaxis);

                      //      Add continuous TM
      repeatingBox.add(cpChkBxContinTM);
                       // Add continuous AR
      repeatingBox.add(cpChkBxContinAR);
                       // Add a Blank line to keep symetrical
      repeatingBox.add(cpMsgBlank1);
      
                    // NOW Create the settings box made up of these two other boxes
      JPanel settingsBox = buildBox(Xaxis);
      settingsBox.add(autoSendBox);
      settingsBox.add(repeatingBox);
      

               // Add the Setting Box  
      gb2.gridwidth = GridBagConstraints.REMAINDER;
      msgCBoxPanel.add(settingsBox, gb2);               

      //
      // NOW...set up a panel for the Actual Message Sending
      //                    
      JPanel msgSendPanel = new JPanel(new GridBagLayout());

                                   
      GridBagConstraints g3 = new GridBagConstraints();
      g3.anchor = GridBagConstraints.NORTH;
      g3.gridwidth = GridBagConstraints.REMAINDER;
      msgSendPanel.add(cpMsgBlank2, g3);
      g3.gridwidth = GridBagConstraints.REMAINDER;
      msgSendPanel.add(cpMsgFormatLabel, g3);

                // Add the text entry fields      
      g3.anchor = GridBagConstraints.CENTER;       
      JPanel fieldb = buildBox(Xaxis);
                 // Message Type
      fieldb.add(cpMessageTypeLabel);
               // Load1
      fieldb.add(cpTxtLoad1);
              
               // Load 2
      fieldb.add(cpTxtLoad2);
              
               // Station
      fieldb.add(cpTxtStation);
              
           // Status  
      fieldb.add(cpTxtStatus); 
        
      msgSendPanel.add(fieldb, g3);
      msgSendPanel.add(cpFreeFormLabel, g3);
        
      JPanel freeb = buildBox(Xaxis);   
               //      Add a field for freeform typing of a message
      cpTxtFreeForm.setEnabled(false);
      freeb.add(cpTxtFreeForm);
      
      msgSendPanel.add(freeb, g3);


      cpbtnSend.addEvent(BTN_SEND, buttonListener);
      msgSendPanel.add(cpbtnSend, g3);
                                                
         //                                                   
         // NOW...set up the Main Messaging panel  
         //                                                 
      JPanel mpanel = new JPanel(new GridBagLayout());

      mpanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), "Messaging"));
      GridBagConstraints gbc = new GridBagConstraints();

             // Specifications applicable to all            
             // columns and rows go in this section
      gbc.gridwidth = 1;         // how many cells per row
      gbc.gridheight = 1;        // how many cells per column
      gbc.weightx = 1;  

                                         // Define the columns and rows.
      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridwidth = 1;

      gbc.gridheight = 1;

      mpanel.add(msgRadioPanel, gbc);
                // Add the Check Box Panel
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      mpanel.add(msgCBoxPanel, gbc);
                // Add the Send Panel
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.gridheight = GridBagConstraints.REMAINDER;
      mpanel.add(msgSendPanel, gbc);

      return(mpanel);
    }
   
    /**
     * getLoggingPanel - Build the "Logging" Section panel
     * @return
     */
    protected JPanel getLoggingPanel()
    {
      // Set up the Panel for this frame.

      JPanel lpanel = new JPanel(new GridBagLayout());
      lpanel.setPreferredSize(new Dimension(650,200));
      lpanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), "Logging"));
      
      GridBagConstraints gbc = new GridBagConstraints();

             // Specifications applicable to all
             
             // columns and rows go in this section
      gbc.gridwidth = 1;         // how many cells per row
      gbc.weightx = 0.2;  


                         // Define the columns and rows.

      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridy = 0;  
      
      JPanel scrollbox = buildBox(Xaxis);
      scrollbox.add(cpRBtnScrollOff);
      cpRBtnScrollOn.setSelected(true);
      scrollbox.add(cpRBtnScrollOn);
      

      
      JPanel clearnum = buildBox(Xaxis);
      cpbtnClear.addEvent(BTN_CLEAR, buttonListener);
      clearnum.add(cpbtnClear);
      clearnum.add(cpNumLogsLabel);
  
      JPanel scrollclear = buildBox(Yaxis);
      scrollclear.add(scrollbox);
      cpbtnClear.addEvent(BTN_CLEAR, buttonListener);
      scrollclear.add(clearnum);
          
      JPanel labelsbox = buildBox(Yaxis);
      labelsbox.add(cpMsgBlank3);
      labelsbox.add(cpCurrentStateLabel);
      
      JPanel buttonText = buildBox(Xaxis);
                  //      Add the Clear Logging Button to the Right

      buttonText.add(scrollclear);  
                  //      Add the Current State Text label next
      buttonText.add(labelsbox); 

      lpanel.add(buttonText, gbc);
      gbc.gridy = 2;
                             // Add the Logging Columns label next

      cpLoggingFieldsLabel.setFont(myFont);
      cpLoggingFieldsLabel.setText(
      "       Date         Seq#  Msg  From                Data            ");

      lpanel.add(cpLoggingFieldsLabel, gbc); 
      gbc.gridy = 3; 
                             // Add the Text Area where messages will be logged

      cpTextArea.setEditable(false);
      cpTextArea.setFont(myPlainFont);
      cpScrollArea.setPreferredSize(new Dimension(500,100));


      cpScrollArea.setMinimumSize(new Dimension(500,100));

      cpScrollArea.getViewport().add(cpTextArea, null);
      viewport = cpScrollArea.getViewport();
      lpanel.add(cpScrollArea, gbc);          

      return lpanel;
    } 
    
    /**
     *  disconnectTheSocket - Method to disconnect from the socket and set the status accordingly
     */
    protected void disconnectTheSocket()
    {
      try
      {
        if(cpSocket != null && cpSocket.isConnected())
        {
          cpSocket.close();
        }
      }
      catch(IOException ioe)
      {
        // Can't do anything anyway
      }
      try
      {
        if(cpListenServerSocket != null)
        {
          cpListenServerSocket.close();
        }
      }
      catch(IOException ioe)
      {
        // Can't do anything anyway
      }
      cpSocket = null;
      cpListenServerSocket = null;
      setStatus("DISCONNECTED");
    }  
             
    /**
     *   Method to setup and connect to the socket and set the status accordingly
     * @param sConType - connection type (Listen or Connect)
     * @param ipAdd - ipaddress to connect to
     * @param port - port to connect to.
     */
    protected void createSocketConnection()
    {      
      if(cpRBtnConnect.isSelected())
      {
        if(cpRBtnHostName.isSelected())
        {
          hostName = cpTxtHostName.getText();
          if(hostName.trim().length() < 1)
          {
            //displayError("Invalid Host Name...Please Enter a valid Host Name");
            setCurrentState("Invalid Host Name...Please Enter a valid Host Name");
            return;
          }
  
        }
        else
        {
          if((cpTxtIP1.getText().trim().length() < 1) ||
             (cpTxtIP2.getText().trim().length() < 1) ||
             (cpTxtIP3.getText().trim().length() < 1) ||
             (cpTxtIP4.getText().trim().length() < 1) )
          {
            //displayError("Invalid IP Address...Please Enter a valid IP Address");
            setCurrentState("Invalid IP Address...Please Enter a valid IP Address");
            return;
          }
          hostName = cpTxtIP1.getText() + "." + cpTxtIP2.getText() + "." + 
                     cpTxtIP3.getText() + "." + cpTxtIP4.getText();
        }
      }
      if(cpTxtPort.getText().trim().length() < 1)
      {
        //displayError("Invalid Port...Please Enter a valid Port");
        setCurrentState("Invalid Port...Please Enter a valid Port");
        return;
      }
      portID = cpTxtPort.getText();
      setCurrentState("Connecting....Please Wait");
      setStatus("CONNECTING");
      if(cpRBtnConnect.isSelected())
      {
        portType = "Connect";
        connectToSocket(hostName, portID);
      }
      else
      {
        portType = "Listen";
        createListenSocket(portID);
      }
   }
    
    
   /**
    * connectToSocket - Do the Socket Connection to a "Connect" Socket
    * @param host
    * @param port
    */
   protected void connectToSocket(String host, String port)
   {
     ConnectThread connector = new ConnectThread(port, host, cpSocket);
     Thread connectThread = new NamedThread(connector);
     connectThread.setName("Connect_Socket_Creation_Thread");
     connectThread.start();
   }
   /**
    * connectToSocket - Do the Socket Connection to a "Listen" Socket
    * @param port
    */   
   protected void createListenSocket(String port)
   {
     ListenConnectionThread listnr = new ListenConnectionThread(port, cpListenServerSocket, cpSocket);
     Thread lisnThread = new NamedThread(listnr);
     lisnThread.setName("Connect_Socket_Creation_Thread");
     lisnThread.start();
   }
    
   /**
     *  Inner listener class for button events.
     */
   private class ButtonListener implements ActionListener
   {
     public void actionPerformed(ActionEvent e)
     {
       String which_button = e.getActionCommand();
       if(which_button.substring(0,5).equals("MSG_R"))
       {
         if(which_button.equals(MSG_RADIO_FREE))
         {
           cpTxtFreeForm.setEnabled(true);
           cpTxtLoad1.setEnabled(false);
           cpTxtLoad2.setEnabled(false);
           cpTxtStation.setEnabled(false);
           cpTxtStatus.setEnabled(false);
         }
         else
         {
           cpTxtFreeForm.setEnabled(false);
           cpTxtLoad1.setEnabled(true);
           cpTxtLoad2.setEnabled(true);
           cpTxtStation.setEnabled(true);
           cpTxtStatus.setEnabled(true); 
         }
       }
       else if(which_button.substring(0,3).equals("IP_"))
       {
         if(which_button.equals(IP_RADIO_IP))
         {
           cpTxtIP1.setEnabled(true);
           cpTxtIP2.setEnabled(true);
           cpTxtIP3.setEnabled(true);
           cpTxtIP4.setEnabled(true);
           cpTxtHostName.setEnabled(false);
         }
         else
         {
           cpTxtIP1.setEnabled(false);
           cpTxtIP2.setEnabled(false);
           cpTxtIP3.setEnabled(false);
           cpTxtIP4.setEnabled(false);
           cpTxtHostName.setEnabled(true);
         }
       }
       
       if(which_button.equals(RADIO_CONNECT))
       {
         setCurrentState(" Connect Radio Button Pressed");
         /*
         enablePanelComponents(itemPanel, true);
         enablePanelComponents(locationPanel, false);
         enablePanelComponents(warehousePanel, false);
         txtItem.requestFocus();
         */
       }
       else if (which_button.equals(RADIO_LISTEN))
       {
         setCurrentState(" Listen Radio Button Pressed");
       }
       else if (which_button.equals(MSG_RADIO_AR))
       {
         cpSendMsgType = ("AR");
         cpMessageTypeLabel.setText(cpSendMsgType + " ");
       }
       else if (which_button.equals(MSG_RADIO_ST))
       {
         cpSendMsgType = ("ST");
         cpMessageTypeLabel.setText(cpSendMsgType + " ");
       }
       else if (which_button.equals(MSG_RADIO_FC))
       {
         cpSendMsgType = ("FC");
         cpMessageTypeLabel.setText(cpSendMsgType + " ");
       }
       else if (which_button.equals(MSG_RADIO_OR))
       {
         cpSendMsgType = ("OR");
         cpMessageTypeLabel.setText(cpSendMsgType + " ");
       }
       else if (which_button.equals(MSG_RADIO_TM))
       {
         cpSendMsgType = ("TM");
         cpMessageTypeLabel.setText(cpSendMsgType + " ");
       }
       else if (which_button.equals(MSG_RADIO_TR))
       {
         cpSendMsgType = ("TR");
         cpMessageTypeLabel.setText(cpSendMsgType + " ");
       }
       else if (which_button.equals(MSG_RADIO_FREE))
       {
         cpSendMsgType = ("FF");
         cpMessageTypeLabel.setText(cpSendMsgType + " ");
       }
       else if (which_button.equals(BTN_CLEAR))
       {
         setCurrentState(" Clear Logging Button Pressed");
         clearLogging();
       }
       else if (which_button.equals(BTN_CONNECT))
       { 
         setCurrentState(" Connect Button Pressed");
         createSocketConnection();
       }
       else if (which_button.equals(BTN_SEND))
       {
         setCurrentState(" Send Button Pressed");       
         buildMessage();
       }
       else if (which_button.equals(BTN_DISCONNECT))
       {
         setCurrentState(" Disconnect Button Pressed");
         disconnectTheSocket();
       }
     }
   } // End of ButtonListener

   /**
    * ContinuousARThread - Thread for timing and sending repeating AR messages
    */
   class ContinuousARThread extends NamedThread
   {
     @Override
    public void run()
     {
       String msgToSend;
       String load1;
       String load2;
       String station = lastAR.substring(20,24);
       
       String zeros = "00000000";
       String mstring;
       String nstring;
       String load1part = lastAR.substring(2,4);
       String load2part = lastAR.substring(10,12);
       
       int i;
       int m = cpARLoadSequence;
       cpARLoadSequence++;
       int n = cpARLoadSequence;
       cpARLoadSequence++;
       cpARLoadSequence++;
       for(i=0;i<10000;i++)
       {
         try
         {
           sleep(5000);
        
           mstring = "" + m;
           nstring = "" + n;
           load1 = load1part + zeros.substring(0, 8 - load1part.length() - mstring.length())
                                                                                + mstring;              
           load2 = load2part + zeros.substring(0, 8 - load2part.length() - nstring.length())
                                                                                 + nstring;
           msgToSend = "AR" + load1 + load2 + station + "00";              
           if(cpChkBxContinAR.isSelected())
           {
             sendMessage(msgToSend);
           }
           else
           {
             sendingAR = false;
             break;
           }
           m = cpARLoadSequence;
           cpARLoadSequence++;
           n = cpARLoadSequence;            cpARLoadSequence++;
//           System.out.println("This is the Continuous AR Thread\n");
         }
         catch(Exception InterruptedException)
         {
           System.out.println("Interrupted in the Continuous AR thread\n");
         }
       }
     }
   } // End of ContinuousARThread
   /**
    * ContinuousTMThread - Thread for timing and sending repeating TM messages
    */   
   class ContinuousTMThread extends NamedThread
   {
     @Override
    public void run()
     {
       int i;
       for(i=0;i<10000;i++)
       {
         try
         {
           sleep(5000);
           if(cpChkBxContinTM.isSelected())
           {
             sendMessage(testMessage);
           }
           else
           {
             sendingTM = false;
             break;
           }
//              System.out.println("This is the Continuous TM Thread\n");
         }
         catch(Exception InterruptedException)
         {
           System.out.println("Interrupted in the Continuous TM thread\n");
         }
       }
     }
   } // End of ContinuousTMThread
 
   /**
    * SocketReceiver - Thread for receiving messages from a socket
    */   
   class SocketReceiver implements Runnable
   {
     private InputStream serverStream;
     private boolean goingDown = false;
     
     public SocketReceiver(InputStream ipServerStream)
     {
       serverStream = ipServerStream;
     }
     
     public void close()
     {
       goingDown = true;
     }

     public void readStream()
     {
       try
       {
//          int CONTROLS_MESSAGE_LENGTH = 28;
         char[] cArray = new char[1000];
         char[] saveBuffer = new char[1000];
         BufferedReader reader =
           new BufferedReader(new InputStreamReader(serverStream));
         // get input stream
         while (!goingDown)
         {
           if(cpStatusLabel.getText().equals("DISCONNECTED"))
           {
             break;
           }
           if(!reader.ready())
           {
             Thread.sleep(500);
             continue;
           }
//             System.out.println("Reader is ready for a read ");
           boolean reading = true;
           boolean gotStart = false;
           int i = 0;
           while(reading)
           {
             if(reader.read(cArray, 0, 1) < 1)
             {
               continue;
             }
             if(gotStart == true)
             {
               if(cArray[0] == PortConsts.ETX)
               {
                 reading = false;
               }
               else
               {
                 saveBuffer[i] = cArray[0];
                 i++;
               }
             }
             else if(cArray[0] == PortConsts.STX)
             {
               i = 0;
               gotStart = true;
             }
            }
           String line = new String(saveBuffer, 0, i);
           byte[] bytes = line.getBytes();
           if (bytes.length > 1)
           {
//             addLog("Just Received the following line on the Socket: " + line);
             receiveMessage(line);
           }
         }
       } 
       catch (Exception e)
       {
         setCurrentState("Socket Receive ERROR: " + e.getMessage());
         addLog("Socket Receive ERROR: " + e.getMessage());
         setStatus("ERROR");
       }      
       addLog("SocketReceiver Socket: Exiting.");
     }

     public void run()
     {      
       setStatus("CONNECTED");
       sendMessage(testMessage);
       readStream();
     } // run()
   } // End of SocketReceiver

   /**
    * ListenConnectionThread - Thread for setting up a Listening Socket 
    * in order to release the application from waiting for it to happen
    */   
   class ListenConnectionThread implements Runnable
   {
     protected String myport;
     protected ServerSocket mylistenSocket;
     protected Socket myacceptSocket;

     
     public ListenConnectionThread(String port, ServerSocket lSocket, Socket aSocket)
     {
       myport = port;
       mylistenSocket = lSocket;
       myacceptSocket = aSocket;
     }
     
     public void close()
     {
       ;
     }

     public void createIt()
     {
       try
       {
         if(cpListenServerSocket == null)
         {
           cpListenServerSocket = new ServerSocket(Integer.valueOf(myport).intValue());
         }
         cpListenServerSocket.setSoTimeout(60000);
         cpSocket = cpListenServerSocket.accept();
         
         outputStream = cpSocket.getOutputStream();
         SocketReceiver receiver = new SocketReceiver(cpSocket.getInputStream());
         Thread receiverThread = new NamedThread(receiver);
         receiverThread.setName("Listen_Socket_Inbound_Listener");
         receiverThread.start();
       }
       catch(IOException ioe)
       {
         addLog("Listen Socket Error: " + ioe.getMessage());
         setCurrentState("Socket Error: " + ioe.getMessage());
         setStatus("ERROR");
       }
       catch(Throwable e)
       {
         addLog("Error Creating Listen Socket: " + e.getMessage());
         setCurrentState("Listen Socket Error: " + e.getMessage());
         setStatus("ERROR");
       }
     }
     public void run()
     {      
       createIt();
     } // run()
   } // End of ListenConnectionThread
   /**
    * ConnectThread - Thread for setting up a connection to a socket
    */   
   class ConnectThread implements Runnable
   {
     protected String myHost;     
     protected String myport;
     protected Socket myconnectSocket;
     
     public ConnectThread(String port, String host, Socket cSocket)
     {
       myport = port;
       myHost = host;
       myconnectSocket = cSocket;
     }
     
     public void close()
     {
       ;
     }
          
     public void createIt()
     {
       try
       {
         cpSocket = new Socket(myHost, Integer.valueOf(myport).intValue());
         outputStream = cpSocket.getOutputStream();
         SocketReceiver receiver = new SocketReceiver(cpSocket.getInputStream());
         Thread receiverThread = new NamedThread(receiver);
         receiverThread.setName("Connect_Socket_Inbound_Listener");
         receiverThread.start();
       }
       catch(IOException ioe)
       {
         addLog("Error Connecting to socket: " + ioe.getMessage());
         setCurrentState("Socket Error: " + ioe.getMessage());
         setStatus("ERROR");
       }
       catch(Throwable e)
       {
         throw new SkdRtException(
           "Connect_Socket_Inbound_Listener: TCP/IP setup failed: " + e.getMessage(), e);
       }
     }
     
     public void run()
     {   
       createIt();
     } // run()
   } // End of ConnectThread
  } // End of ControlsPanel
} // End of ControlsDeviceEmulation 