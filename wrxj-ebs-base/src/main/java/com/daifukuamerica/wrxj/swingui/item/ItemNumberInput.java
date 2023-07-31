package com.daifukuamerica.wrxj.swingui.item;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import javax.swing.JPanel;

/**
 * <B>Description:</B> ItemNumberInput provides either a specialized 
 * Combo Box or a Text Field, depending upon the UseItemComboBox configuration
 * property.
 * 
 * <P>The default behavior is to provide a Text Field.</P>
 *
 * @author       mandrus<BR>
 * @version      2.0   Refactored from ItemComboBox and added Text Field support
 * 
 * <P>Copyright (c) 2007 by Daifuku America Corporation</P>
 */
@SuppressWarnings("serial")
public class ItemNumberInput extends JPanel
{
  private StandardInventoryServer mpInvServer;
  private boolean mzAddBlank = false;
  private SKDCComboBox  mpComboBox;
  private SKDCTextField mpTextField;
  private boolean mzUseComboBox = false;
  
  private SKDCTextField mpItemDesc;
  private Component mpNextComponent;
  private boolean mzSetNextFocus = false;
  private String msSearch = "";

  /**
   * Constructor
   * 
   * @param ipInvServer - The inventory server to use to generate lists
   * @param izAddBlank  - Add a blank item to an non-searching list 
   */
  public ItemNumberInput(StandardInventoryServer ipInvServer,
      boolean izAddBlank, boolean izLimitLength)
  {
    super();
    setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    
    mzUseComboBox = Application.getBoolean("UseItemComboBox", false);
    
    if (ipInvServer == null)
    {
      throw new NullPointerException("Inventory Server is null");
    }
    mpInvServer = ipInvServer;
    mzAddBlank = izAddBlank;
    
    int vnItemLength = DBInfo.getFieldLength(ItemMasterData.ITEM_NAME);
    int vnDisplayLength = vnItemLength;
    if (izLimitLength)
    {
      vnDisplayLength = Math.min(vnItemLength, 15);
    }
    if (mzUseComboBox)
    {
      mpComboBox = new SKDCComboBox();
      String vsPrototype = "";
      for (int i = 0; i < vnDisplayLength; i++) vsPrototype += "9";
      mpComboBox.setPrototypeDisplayValue(vsPrototype);
      mpComboBox.setRequestFocusEnabled(true);
      mpComboBox.addKeyListener(new ItemKeyListener());
      itemFill();
      add(mpComboBox);
    }
    else
    {
      mpTextField = new SKDCTextField(vnDisplayLength);
      mpTextField.setMaxColumns(vnItemLength);
      add(mpTextField);
    }
  }
  
  /**
   * Link this item input to a description box.  Adds a listener to populate
   * the description box as an item is chosen.
   * 
   * @param ipItemDesc
   */
  public void linkDescription(SKDCTextField ipItemDesc)
  {
    mpItemDesc = ipItemDesc;
    
    addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          updateDescription();
        }
      });
    
    if (!mzUseComboBox)
      mpTextField.addFocusListener(new FocusListener()
        {
          public void focusGained(FocusEvent e) {}
    
          public void focusLost(FocusEvent e)
          {
            updateDescription();
          }
        });
    
    updateDescription();
  }
  
  /**
   * This method allows you to set focus to any component after the item desc is updated
   * as long as the itemtext length is greater than 0.
   * @param ipNextComponent next focusable component
   */
  public void setNextFocusComponent(Component ipNextComponent)
  {
    mzSetNextFocus = true;
    mpNextComponent = ipNextComponent;
  }
  
  /**
   * update the linked description
   */
  private void updateDescription()
  {
    if (mpItemDesc != null)
    {
      String vsItem = getText();
      String vsDesc = "";
      
      if (vsItem.length() > 0)
      {
        try
        {
          vsDesc = mpInvServer.getItemMasterDescription(vsItem);
          if (vsDesc == null)
          {
            vsDesc = "Item not found.";
          }
        }
        catch (DBException dbe)
        {
          vsDesc = dbe.getMessage();
        }
      }
      mpItemDesc.setText(vsDesc);
      if(mzSetNextFocus && vsItem.length() > 0)
      {
        mpNextComponent.requestFocus();
      }
    }
  }
  
  /**
   * Key Listener class for item combo box
   */
  private class ItemKeyListener implements KeyListener
  {
    char mcLastChar = ' ';
    
    public void keyPressed(KeyEvent arg0)
    {
      char vcChar = arg0.getKeyChar();
      if (vcChar == '\b')
      {
        /*
         * Double backspace clears search
         */
        if (mcLastChar == '\b')
        {
          msSearch = "";
        }
        /*
         * Backspace removes one character
         */
        else if (msSearch.length() > 0)
        {
          msSearch = msSearch.substring(0,msSearch.length()-1);
        }
        itemFill();
      }
      else if (((vcChar >= '(') && (vcChar <= 'z')) || (vcChar == ' '))
      {
        msSearch = msSearch + arg0.getKeyChar();
        itemFill();
      }
      mcLastChar = vcChar;
      /*
       * Ignore everything else
       */
    }

    public void keyReleased(KeyEvent arg0) {}
    public void keyTyped(KeyEvent arg0) {}
  };

  /**
   *  Method to populate the item combo box.
   *
   *  @param srch Name to match.
   */
  void itemFill()
  {
    try
    {
      List vpItemList = mpInvServer.getItemMasterNameList(msSearch);
      
      /*
       * If new search criteria empties the list, ignore it
       */
      if ((vpItemList.size() == 0) && (msSearch.length() > 0))
      {
        msSearch = msSearch.substring(0,msSearch.length()-1);
        Toolkit.getDefaultToolkit().beep();
        return;
      }
      
      /*
       * Add a blank line if necessary
       */
      if (((mzAddBlank) && (msSearch.length() == 0))
              || (vpItemList.size() == 0))
      {
        vpItemList.add(0, "");
      }
      mpComboBox.setComboBoxData(vpItemList);
      mpComboBox.setSelectedIndex(0);
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
    }
  }

  /**
   * Select an item in the list or set the text
   * 
   * @param isItem
   */
  public void setText(String isItem)
  {
    setSelectedItem(isItem);
  }
  
  /**
   * Select an item in the list or set the text
   * 
   * @param isItem
   */
  public void setSelectedItem(String isItem)
  {
    if (mzUseComboBox)
      mpComboBox.setSelectedItem(isItem);
    else
      mpTextField.setText(isItem);
    
    updateDescription();
  }
  
  /**
   * Reset the combo box
   */
  public void reset()
  {
    itemFill();
  }

  /**
   * Set the enabled property
   * @param izEnabled
   */
  @Override
  public void setEnabled(boolean izEnabled)
  {
    if (mzUseComboBox)
      mpComboBox.setEnabled(izEnabled);
    else
      mpTextField.setEnabled(izEnabled);
  }
  
  /**
   * If this is a combo box, sets the index.
   * <BR>If this is a text field, clears the field.
   * @param inIndex
   */
  public void setSelectedIndex(int inIndex)
  {
    if (mzUseComboBox)
      mpComboBox.setSelectedIndex(inIndex);
    else
      mpTextField.setText("");
  }
  
  /**
   * Request focus
   */
  @Override
  public void requestFocus()
  {
    if (mzUseComboBox)
      mpComboBox.requestFocus();
    else
      mpTextField.requestFocus();
  }
  
  /**
   * Returns the contents of the input field
   * @return
   */
  public String getText()
  {
    if (mzUseComboBox)
      return mpComboBox.getText();
    else
    {
      mpTextField.setText(mpInvServer.swapItemSynonymIfEntered(mpTextField.getText()));
      return mpTextField.getText();
    }
  }

  /**
   * Returns the contents of the input field
   * @return
   */
  public String getSelectedItem()
  {
    if (mzUseComboBox)
      return mpComboBox.getText();
    else
      return mpTextField.getText();
  }
  
  /**
   * If this is an combo box, adds an ItemListener
   * <BR>If this is a text field, does nothing.
   * @param ipL
   */
  public void addItemListener(ItemListener ipL)
  {
    if (mzUseComboBox)
      mpComboBox.addItemListener(new ItemActionListener(ipL));
    else
      mpTextField.addActionListener(new ItemActionListener(ipL));
  }
  
  /**
   * If this is an combo box, adds an ItemListener
   * <BR>If this is a text field, does nothing.
   * @param ipL
   */
  public void addActionListener(ActionListener ipL)
  {
    if (mzUseComboBox)
      mpComboBox.addItemListener(new ItemActionListener(ipL));
    else
      mpTextField.addActionListener(new ItemActionListener(ipL));
  }
  
  @Override
  public void addKeyListener(KeyListener ipKeyListener)
  {
    if(!mzUseComboBox)
    {
      mpTextField.addKeyListener(ipKeyListener);
    }
  }
  
  /**
   * An attempt to use the item listener on a text field
   */
  private class ItemActionListener implements ActionListener, ItemListener
  {
    ActionListener mpAL = null;
    ItemListener   mpIL = null;
    
    public ItemActionListener(ActionListener ipL)
    {
      super();
      mpAL = ipL;
    }
    
    public ItemActionListener(ItemListener ipL)
    {
      super();
      mpIL = ipL;
    }

    public void actionPerformed(ActionEvent e)
    {
      if (mpAL == null)
        mpIL.itemStateChanged(null);
      else
        mpAL.actionPerformed(e);
    }

    public void itemStateChanged(ItemEvent e)
    {
      if (mpIL == null)
        mpAL.actionPerformed(null);
      else
        mpIL.itemStateChanged(e);
    }
  }
  
  /**
   * Add Focus Listeners
   */
  @Override
  public synchronized void addFocusListener(FocusListener ipFL)
  {
    if (mzUseComboBox)
      mpComboBox.addFocusListener(ipFL);
    else
      mpTextField.addFocusListener(ipFL);
  }
  
  @Override
  public synchronized void removeFocusListener(FocusListener ipFL)
  {
    if(mzUseComboBox)
    {
      mpComboBox.removeFocusListener(ipFL);
    }
    else
    {
      mpTextField.removeFocusListener(ipFL);
    }
  }
}
