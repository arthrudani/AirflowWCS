package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCRadioButton;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * Description:<BR>
 *    Primary frame for Transaction Viewing.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 05-May-03<BR>
 *     Copyright (c) 2003<BR>
 *     Company:  SK Daifuku Corporation
 */
@SuppressWarnings("serial")
public class TranHistoryMain extends SKDCInternalFrame
{
  private SKDCRadioButton[] radioButtons;
  private SKDCButton        btnArchive;
  private ButtonGroup       radioGroup;
  private Map               panelCache;
  private ActionListener    radioListener;
  private ActionListener    buttonListener;
  private Integer           lastDisplay;
  private JPanel            radioPanel;
  private static final int  ALL_TRAN = 1947;
  TransactionHistoryView tranView;

  public TranHistoryMain()
  {
    this(false);
  }

  public TranHistoryMain(boolean DEBUGMODE)
  {
    super("Transaction History");
    setMaximizable(true);
    panelCache = new HashMap();
                                       // Listener for Radio buttons.
    radioListener = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        buildView(e.getActionCommand());
      }
    };
                                       // Listener for view Buttons.
    buttonListener = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String which_button = e.getActionCommand();

        if (which_button.equals(SKDCGUIConstants.SEARCH_BTN))
        {
          tranView.searchButtonPressed();
        }
        else if (which_button.equals(SKDCGUIConstants.DETSEARCH_BTN))
        {
          detailSearchButtonPressed();
        }
        else if (which_button.equals(SKDCGUIConstants.ARCHIVE_BTN))
        {
          archiveButtonPressed();
        }
      }
    };

    radioPanel = buildRadioButtonPanel();
    this.getContentPane().add(radioPanel, BorderLayout.WEST);
  }

  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(895, 440));
  }

  @Override
  public void cleanUpOnClose()
  {
    panelCache.clear();
    panelCache = null;
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  void detailSearchButtonPressed()
  {
    TransactionSearch detailedSearch = tranView.getSearchFrameInstance();
    addSKDCInternalFrameModal(detailedSearch, new JPanel[] {tranView, radioPanel},
                              new SearchFrameReturnListener());
  }

  void archiveButtonPressed()
  {
    int[] tranCategories = DBTrans.getIntegerList(TransactionHistoryData.TRANCATEGORY_NAME);
    addSKDCInternalFrameModal(new ArchiveFrame(SKDCConstants.XMLINTERFACE, tranCategories),
                              new JPanel[] {tranView, radioPanel},
                              new SearchFrameReturnListener());
  }

  void buildView(String sCategory)
  {
    Integer displayKey;
    if (sCategory.equals("All"))
    {
      displayKey = ALL_TRAN;
    }
    else
    {
      try
      {
        displayKey = DBTrans.getIntegerObject(TransactionHistoryData.TRANCATEGORY_NAME,
                                              sCategory);
      }
      catch(NoSuchFieldException exc)
      {
        System.out.println("Invalid translation::::" + exc);
        return;
      }
    }

    Container cp = this.getContentPane();
    if (!panelCache.containsKey(displayKey))
    {
      switch(displayKey.intValue())
      {
        case ALL_TRAN:
          tranView = new AllHistoryViewImpl(SKDCConstants.DBINTERFACE,
              buttonListener);
          break;
          
        case DBConstants.LOAD_TRAN:
          tranView = new LoadHistoryViewImpl(SKDCConstants.DBINTERFACE,
                                             buttonListener);
          break;

        case DBConstants.ORDER_TRAN:
          tranView = new OrderHistoryViewImpl(SKDCConstants.DBINTERFACE,
                                              buttonListener);
          break;

        case DBConstants.USER_TRAN:
          tranView = new UserHistoryViewImpl(SKDCConstants.DBINTERFACE,
                                             buttonListener);
          break;

        case DBConstants.INVENTORY_TRAN:
          tranView = new InventoryHistoryViewImpl(SKDCConstants.DBINTERFACE,
                                                  buttonListener);
          break;

        case DBConstants.SYSTEM_TRAN:
          tranView = new SystemHistoryViewImpl(SKDCConstants.DBINTERFACE,
                                                  buttonListener);
          break;
      }
      tranView.setParentFrame(this);
      panelCache.put(displayKey, tranView);
    }
    
    if (lastDisplay != null)
    {
      TransactionHistoryView oldPanel = (TransactionHistoryView)panelCache.get(lastDisplay);
      oldPanel.setVisible(false);
      cp.remove(oldPanel);
    }
    tranView = (TransactionHistoryView)panelCache.get(displayKey);
    cp.add(tranView, BorderLayout.CENTER);
    (tranView).setVisible(true);

    lastDisplay = displayKey;
  }

/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/
  private JPanel buildRadioButtonPanel()
  {
    String[] sTrncat;

    radioGroup = new ButtonGroup();
    JPanel vpRadioPanel = new JPanel();
    vpRadioPanel.setLayout(new BoxLayout(vpRadioPanel, BoxLayout.Y_AXIS));

    try
    {
      sTrncat = DBTrans.getStringList(TransactionHistoryData.TRANCATEGORY_NAME);
    }
    catch(NoSuchFieldException exc)
    {
      System.out.println(exc);
      return(vpRadioPanel);              // Just return an empty panel.
    }

    radioButtons = new SKDCRadioButton[sTrncat.length+1];
    radioButtons[0] = new SKDCRadioButton("All", 'A', true);
    radioButtons[0].eventListener("All", radioListener);
    radioGroup.add(radioButtons[0]);
    vpRadioPanel.add(radioButtons[0]);
    vpRadioPanel.add(Box.createVerticalStrut(12));
    for(int tnIdx = 0; tnIdx < sTrncat.length; tnIdx++)
    {
      radioButtons[tnIdx+1] = new SKDCRadioButton(sTrncat[tnIdx],
                                                sTrncat[tnIdx].charAt(0),
                                                false);
      radioButtons[tnIdx+1].eventListener(sTrncat[tnIdx], radioListener);
      radioGroup.add(radioButtons[tnIdx+1]);
      vpRadioPanel.add(radioButtons[tnIdx+1]);
      vpRadioPanel.add(Box.createVerticalStrut(12));
    }
    vpRadioPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),
                                                          "Tran. Category"));
    
    // Display initial radio button selected view.
    radioButtons[1].setSelected(true);
    buildView(radioButtons[1].getText());

    JPanel westPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbconst = new GridBagConstraints();
    gbconst.insets = new Insets(1, 1, 30, 1);

    gbconst.gridx = 0;
    gbconst.gridy = 0;
    gbconst.anchor = GridBagConstraints.CENTER;
    westPanel.add(vpRadioPanel, gbconst);

    if (SKDCUserData.isSuperUser())
    {
      JPanel vpButtonPanel = new JPanel();
      btnArchive = new SKDCButton(" Archive ", "Archive Transactions.", 'A');
      btnArchive.addEvent(SKDCGUIConstants.ARCHIVE_BTN, buttonListener);
      vpButtonPanel.add(btnArchive, JPanel.CENTER_ALIGNMENT);
      gbconst.gridy = GridBagConstraints.RELATIVE;
      gbconst.insets = new Insets(30, 1, 10, 1);
      westPanel.add(vpButtonPanel, gbconst);
    }

    return(westPanel);
  }

  private class SearchFrameReturnListener implements PropertyChangeListener
  {
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(SKDCGUIConstants.FRAME_CHANGE))
      {
        tranView.refreshData((TransactionHistoryData)pcevt.getNewValue());
      }
      else if (prop_name.equals(SKDCGUIConstants.FRAME_CLOSING))
      {
                                       // Special case for Inventory History View.
        if (tranView instanceof InventoryHistoryViewImpl)
        {
          ((InventoryHistoryViewImpl)tranView).mpPickCountField.setEnabled(false);
          ((InventoryHistoryViewImpl)tranView).mpStoreCountField.setEnabled(false);
        }
      }
    }
  }
}
