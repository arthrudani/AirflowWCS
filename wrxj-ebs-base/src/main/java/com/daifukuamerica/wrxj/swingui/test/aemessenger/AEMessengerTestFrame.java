/* ***************************************************************************
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.swingui.test.aemessenger;

import com.daifukuamerica.wrxj.controller.aemessenger.AEMessage;
import com.daifukuamerica.wrxj.controller.aemessenger.AEMessengerClientHelper;
import com.daifukuamerica.wrxj.controller.aemessenger.process.json.PingRequest;
import com.daifukuamerica.wrxj.controller.aemessenger.process.json.SampleJsonAeMessageRequest;
import com.daifukuamerica.wrxj.controller.aemessenger.tcp.AEMReadEvent;
import com.daifukuamerica.wrxj.controller.aemessenger.tcp.AEMTcpipReaderWriter;
import com.daifukuamerica.wrxj.dbadapter.data.aed.CommunicationTypeData;
import com.daifukuamerica.wrxj.dbadapter.data.aed.Instance;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunications;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunicationsData;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.UndoEnabledTextArea;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynright.wrxj.app.Product;
import com.wynright.wrxj.app.Wynsoft;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * AE Messenger Test screen
 * 
 * <p>This screen is for testing inbound AE messages to WRx.</p> 
 * 
 Insert into ROLEOPTION (SROLE,SCATEGORY,SOPTION,SICONNAME,SCLASSNAME,IBUTTONBAR,IADDALLOWED,IMODIFYALLOWED,IDELETEALLOWED,IVIEWALLOWED,DMODIFYTIME,SADDMETHOD,SUPDATEMETHOD) 
 values ('SKDaifuku','Developer','AE Messenger Test','/graphics/aeMsgTest.png','test.aemessenger.AEMessengerTestFrame',2,1,1,1,1,null,null,null);
 * 
 * <p>See GES10 or GES14 for an implementation with lots of options.</p>
 * 
 * @author mandrus
 */
@SuppressWarnings("serial")
public class AEMessengerTestFrame extends DacInputFrame
{
  private SKDCTextField mpTxtHost;
  private SKDCIntegerField mpTxtPort;
  private SKDCIntegerField mpTxtSource;
  private SKDCIntegerField mpTxtTranID;
  private SKDCCheckBox mpSplitMessage;
  private UndoEnabledTextArea mpMessage;
  private SKDCButton mpFormat;

  /**
   * Constructor
   */
  public AEMessengerTestFrame()
  {
    super("AE Messenger Test", "");
    buildScreen();
  }

  /**
   * Build the screen
   */
  private void buildScreen()
  {
    mpTxtHost = new SKDCTextField(20, 50);
    mpTxtPort = new SKDCIntegerField(5);
    mpTxtSource = new SKDCIntegerField(5);
    mpTxtTranID = new SKDCIntegerField(10);
    mpSplitMessage = new SKDCCheckBox("One Message per Line");
    mpFormat = new SKDCButton("Format JSON");
    mpFormat.setPreferredSize(new Dimension(109, 20));
    mpFormat.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        formatJson();
      }
    });
    mpMessage = new UndoEnabledTextArea(25, 80);
    JScrollPane vpSP = new JScrollPane(mpMessage);
    vpSP.setPreferredSize(new Dimension(800, 250));
    
    mpTxtHost.setText("localhost");
    mpTxtPort.setValue(1234);
    try
    {
      InstanceData vpInstData = Factory.create(Instance.class).getData(Wynsoft.getInstanceId());
      if (vpInstData == null)
      {
        throw new DBException("AE instance [" + Wynsoft.getInstanceId() + "] is undefined.");
      }
      mpTxtHost.setText(vpInstData.getComputerName());
      mpTxtPort.setValue(vpInstData.getPort());
    }
    catch (Exception e)
    {
      logAndDisplayException(e);
    }
    
    mpTxtSource.setValue(Wynsoft.getInstanceId());
    mpTxtTranID.setValue((int)new Date().getTime());
    
    JPanel vpHostPort = new JPanel();
    vpHostPort.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    vpHostPort.add(mpTxtHost);
    vpHostPort.add(new JLabel(" : "));
    vpHostPort.add(mpTxtPort);
    
    JPanel vpSourceSplit = new JPanel();
    vpSourceSplit.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    vpSourceSplit.add(mpTxtSource);

    JPanel vpSplitFormat = new JPanel();
    vpSplitFormat.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
    vpSplitFormat.add(mpSplitMessage);
    vpSplitFormat.add(mpFormat);
    vpSourceSplit.add(vpSplitFormat);
    
    addInput("Host : Port", vpHostPort);
    addInput("Transaction ID", mpTxtTranID);
    
    JTabbedPane vpTemplates = new JTabbedPane();
    Color vpPlanColor = new Color(180, 255, 255);
    // WRx
    addTemplateTab(vpTemplates, "Communicator"     , vpPlanColor, getCmeButtonPanel(mpTxtSource, mpMessage));
    addTemplateTab(vpTemplates, "Convey"           , vpPlanColor, getCleButtonPanel(mpTxtSource, mpMessage));
    addTemplateTab(vpTemplates, "Order Fulfillment", vpPlanColor, getPpeButtonPanel(mpTxtSource, mpMessage));
    addTemplateTab(vpTemplates, "Visibility"       , vpPlanColor, getVieButtonPanel(mpTxtSource, mpMessage));
    
    addInput("Templates", vpTemplates);
    
    addInput("Source", vpSourceSplit);
    addInput("Message", vpSP);
    
    mpBtnClear.setText("Send and Receive");
    mpBtnSubmit.setText("Send");
  }

  /**
   * Add a tab to the template tabbed panel
   * 
   * @param ipTemplates
   * @param isTabName
   * @param ipTabColor
   * @param ipContent
   */
  protected void addTemplateTab(JTabbedPane ipTemplates, String isTabName,
      Color ipTabColor, JPanel ipContent)
  {
    ipTemplates.add(isTabName, ipContent);
    ipTemplates.setBackgroundAt(ipTemplates.getTabCount() - 1, ipTabColor);
  }
  
  /**
   * Communicator Messages
   * 
   * @param ipSourceFld
   * @param ipMsgFld
   * @return
   */
  protected JPanel getCmeButtonPanel(SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld)
  {
    JPanel vpMainPanel = new JPanel(new BorderLayout());
    
    String vsCheck = checkConfig(Product.CME);
    if (SKDCUtility.isNotBlank(vsCheck))
    {
      JPanel vpErrorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      vpErrorPanel.add(new JLabel(vsCheck));
      vpMainPanel.add(vpErrorPanel, BorderLayout.NORTH);
    }

    int vnProduct = Product.CME.getId();
    int vnInstance = 101;
    
    // TODO: Baseline Messages
    JPanel vpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    vpPanel.add(new ButtonCustom(new PingRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpMainPanel.add(vpPanel, BorderLayout.CENTER);
    
    // Custom Messages
    vpMainPanel.add(
        getCmeCustomButtonPanel(ipSourceFld, ipMsgFld, vnProduct, vnInstance),
        BorderLayout.SOUTH);
    
    return vpMainPanel;
  }
  
  /**
   * Get the custom Communicator messages.  Override to add content.
   * 
   * @param ipSourceFld
   * @param ipMsgFld
   * @param inProduct
   * @param inInstance
   * @return
   */
  protected JPanel getCmeCustomButtonPanel(SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld, int inProduct, int inInstance)
  {
    JPanel vpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    return vpPanel;
  }

  /**
   * Convey Messages
   * 
   * @param ipSourceFld
   * @param ipMsgFld
   * @return
   */
  protected JPanel getCleButtonPanel(SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld)
  {
    JPanel vpMainPanel = new JPanel(new BorderLayout());
    
    String vsCheck = checkConfig(Product.CLE);
    if (SKDCUtility.isNotBlank(vsCheck))
    {
      JPanel vpErrorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      vpErrorPanel.add(new JLabel(vsCheck));
      vpMainPanel.add(vpErrorPanel, BorderLayout.NORTH);
    }

    int vnProduct = Product.CLE.getId();
    int vnInstance = 404;
    
    // TODO: Baseline Messages
    JPanel vpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    vpPanel.add(new ButtonCustom(new PingRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpMainPanel.add(vpPanel, BorderLayout.CENTER);
    
    // Custom Messages
    vpMainPanel.add(
        getCleCustomButtonPanel(ipSourceFld, ipMsgFld, vnProduct, vnInstance),
        BorderLayout.SOUTH);
    
    return vpMainPanel;
  }

  /**
   * Get the custom Convey messages.  Override to add content.
   * 
   * @param ipSourceFld
   * @param ipMsgFld
   * @param inProduct
   * @param inInstance
   * @return
   */
  protected JPanel getCleCustomButtonPanel(SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld, int inProduct, int inInstance)
  {
    JPanel vpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    return vpPanel;
  }

  /**
   * Order Fulfillment Messages
   * 
   * @param ipSourceFld
   * @param ipMsgFld
   * @return
   */
  protected JPanel getPpeButtonPanel(SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld)
  {
    JPanel vpMainPanel = new JPanel(new BorderLayout());
    
    String vsCheck = checkConfig(Product.PPE);
    if (SKDCUtility.isNotBlank(vsCheck))
    {
      JPanel vpErrorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      vpErrorPanel.add(new JLabel(vsCheck));
      vpMainPanel.add(vpErrorPanel, BorderLayout.NORTH);
    }

    int vnProduct = Product.PPE.getId();
    int vnInstance = 1;

    // TODO: Baseline Messages
    JPanel vpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    vpPanel.add(new ButtonCustom(new PingRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpMainPanel.add(vpPanel, BorderLayout.CENTER);
    
    // Custom Messages
    vpMainPanel.add(
        getPpeCustomButtonPanel(ipSourceFld, ipMsgFld, vnProduct, vnInstance),
        BorderLayout.SOUTH);
    
    return vpMainPanel;
  }

  /**
   * Get the custom Order Fulfillment messages.  Override to add content.
   * 
   * @param ipSourceFld
   * @param ipMsgFld
   * @param inProduct
   * @param inInstance
   * @return
   */
  protected JPanel getPpeCustomButtonPanel(SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld, int inProduct, int inInstance)
  {
    JPanel vpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    return vpPanel;
  }
  
  /**
   * Visibility Messages
   * 
   * @param ipSourceFld
   * @param ipMsgFld
   * @return
   */
  protected JPanel getVieButtonPanel(SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld)
  {
    JPanel vpMainPanel = new JPanel(new BorderLayout());
    
    String vsCheck = checkConfig(Product.VIE_WEB);
    if (SKDCUtility.isNotBlank(vsCheck))
    {
      JPanel vpErrorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      vpErrorPanel.add(new JLabel(vsCheck));
      vpMainPanel.add(vpErrorPanel, BorderLayout.NORTH);
    }

    int vnProduct = Product.VIE_WEB.getId();
    int vnInstance = 1;

    // TODO: Baseline Messages
    JPanel vpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    vpPanel.add(new ButtonCustom(new PingRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpPanel.add(new ButtonCustom(new SampleJsonAeMessageRequest(), ipSourceFld,
        ipMsgFld, vnProduct, vnInstance));
    vpMainPanel.add(vpPanel, BorderLayout.CENTER);
    
    // Custom Messages
    vpMainPanel.add(
        getVieCustomButtonPanel(ipSourceFld, ipMsgFld, vnProduct, vnInstance),
        BorderLayout.SOUTH);
    
    return vpMainPanel;
  }

  /**
   * Get the custom Visibility messages.  Override to add content.
   * 
   * @param ipSourceFld
   * @param ipMsgFld
   * @param inProduct
   * @param inInstance
   * @return
   */
  protected JPanel getVieCustomButtonPanel(SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld, int inProduct, int inInstance)
  {
    JPanel vpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    return vpPanel;
  }

  /**
   * Prettify the JSON message
   */
  protected void formatJson()
  {
    try
    {
      String vsMsg = mpMessage.getText();
      if (SKDCUtility.isBlank(vsMsg)) return;
      Gson vpGson = new GsonBuilder().setPrettyPrinting().create();
      vsMsg = vpGson.toJson(new JsonParser().parse(vsMsg));
      mpMessage.setText(vsMsg);
    }
    catch (Exception ex)
    {
      logAndDisplayException("Unable to format JSON", ex);
    }
  }
  
  /**
   * Send the message
   */
  @Override
  protected void okButtonPressed()
  {
    send(false);
  }
  
  /**
   * Send the message and wait for a response
   */
  @Override
  protected void clearButtonPressed()
  {
    send(true);
  }
  
  /**
   * Send the message
   * 
   * @param izHasResponse
   */
  private void send(boolean izHasResponse)
  {
    if (mpSplitMessage.isSelected())
    {
      send(izHasResponse, mpMessage.getText().split("[\\r\\n]+"));
    }
    else
    {
      send(izHasResponse, mpMessage.getText());
    }
  }
  
  /**
   * Send the message(s)
   * 
   * @param izHasResponse
   */
  private void send(boolean izHasResponse, String... iasMessage)
  {
    try
    {
      final AEMTcpipReaderWriter vpClient = new AEMTcpipReaderWriter(
          AEMessengerClientHelper.getProperties(mpTxtHost.getText(),
              mpTxtPort.getValue(), mpTxtSource.getValue()),
          AEMessengerClientHelper.getLogger(getClass().getSimpleName()));
      vpClient.connToServer();
      if (izHasResponse)
      {
        ArrayList<String> vpMsgs = new ArrayList<>();
        for (String s : iasMessage)
        {
          if (SKDCUtility.isNotBlank(s))
            vpMsgs.add(s);
        }
        
        vpClient.start();
        
        // Send the message and listen for a response
        vpClient.registerReadEvent(new AEMReadEvent() {
          @Override
          public void receivedData(AEMTcpipReaderWriter ipChannel, final AEMessage ipMessage)
          {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run()
              {
                // Prettify the return JSON string
                JsonObject json = new JsonParser().parse(ipMessage.getMessageDataAsString()).getAsJsonObject();
                String prettyJson = new GsonBuilder().setPrettyPrinting().create().toJson(json);
                
                // Show the user
                JOptionPane.showMessageDialog(AEMessengerTestFrame.this,
                    "RECV: [" + ipMessage.getTransactionID() + "]\n"
                        + prettyJson,
                    "Received Response", JOptionPane.INFORMATION_MESSAGE);
              }
            });
            if (vpMsgs.isEmpty())
            {
              vpClient.stopThread();
            }
            else
            {
              try
              {
                vpClient.sendMessage(getAndIncrementTranID(), vpMsgs.remove(0));
              }
              catch (Exception e)
              {
                e.printStackTrace();
              }
            }
          }
        });
        vpClient.sendMessage(getAndIncrementTranID(), vpMsgs.remove(0));
        
        // Report when we don't get one
        Timer t = new Timer(true);
        t.schedule(new TimerTask() {
          
          @Override
          public void run()
          {
            if (vpClient.isConnectionAlive())
            {
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                  JOptionPane.showMessageDialog(AEMessengerTestFrame.this,
                      AEMessengerTestFrame.this.getTitle()
                          + ": Stopping response reader due to time out.",
                      "Time Out", JOptionPane.WARNING_MESSAGE);
                }
              });
              vpClient.stopThread();
            }
          }
        }, 30000);
      }
      else
      {
        // No response
        for (String s : iasMessage)
        {
          if (SKDCUtility.isNotBlank(s))
          {
            vpClient.sendMessage(getAndIncrementTranID(), s);
          }
        }
        vpClient.stopThread();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    mpTxtTranID.setValue(mpTxtTranID.getValue() + 1);
  }
  
  /**
   * Get the transaction ID and increment it so it is ready for the next time
   * @return
   */
  protected int getAndIncrementTranID()
  {
    int vnTranID = mpTxtTranID.getValue();
    mpTxtTranID.setValue(vnTranID + 1);
    return vnTranID;
  }

  /**
   * Check the AE Configuration
   * @param inProductID
   * @return
   */
  protected String checkConfig(Product ieProduct)
  {
    try
    {
      Instance vpInstHandler = Factory.create(Instance.class);
      
      // Check the instance config for WRx
      InstanceData vpMe = vpInstHandler.getData(Wynsoft.getInstanceId());
      if (vpMe == null)
      {
        return formatError(
            "Warning: no instance defined for " + Wynsoft.getInstanceId());
      }

      // Check the instance config for requested Product
      List<InstanceData> vpSenders = vpInstHandler.getDataList(ieProduct.getId());
      if (vpSenders.isEmpty())
      {
        return formatError(
            "Warning: no instance defined for " + ieProduct.describe());
      }

      // Check the communications config
      List<Integer> vpSendId = new ArrayList<>();
      for (InstanceData vpData : vpSenders)
      {
        vpSendId.add(vpData.getId());
      }
      InstanceCommunicationsData vpKey = Factory.create(
          InstanceCommunicationsData.class);
      vpKey.setInKey(InstanceCommunicationsData.SENDER_ID_NAME, KeyObject.AND,
          vpSendId.toArray(new Object[0]));
      vpKey.setKey(InstanceCommunicationsData.RECEIVER_ID_NAME,
          Wynsoft.getInstanceId());
      vpKey.setKey(InstanceCommunicationsData.COMMUNICATION_TYPE_ID_NAME,
          CommunicationTypeData.COMM_TYPE_TCP);
      if (!Factory.create(InstanceCommunications.class).exists(vpKey))
      {
        return formatError(String.format(
            "Warning: no TCP communications defined for %1$s (%2$s) to %3$s (%4$d)",
            ieProduct.getDescription(), ieProduct, vpMe.getIdentityName(),
            vpMe.getId()));
      }
    }
    catch (Exception e)
    {
      String vsErrorMsg = "Error checking AE configuration";
      Logger.getLogger().logException(vsErrorMsg, e);
      return formatError(vsErrorMsg);
    }
    return null;
  }

  /**
   * Format the error
   * @param isError
   * @return
   */
  protected String formatError(String isError)
  {
    return "<html><font color=red>" + isError + "</font></html>";
  }
}
