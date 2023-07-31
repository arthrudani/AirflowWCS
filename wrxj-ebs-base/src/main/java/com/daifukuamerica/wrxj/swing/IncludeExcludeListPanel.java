package com.daifukuamerica.wrxj.swing;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.EtchedBorder;

/**
 * Description:<BR>
 *    Base class for setting up a two-column include/exclude list panel.  Two lists are displayed
 *    in a <code>JPanel</code> with buttons between which allow the items in the list to be moved
 *    from one list to the other and back.  This panel is built using the <code>GridBagLayout</code>.
 *
 * @author       R.M.
 * @version      1.0
 */
public class IncludeExcludeListPanel extends JPanel
{
  private static final long serialVersionUID = 0L;

  private GridBagConstraints gbconst;
  protected DefaultListModel excTable;
  protected DefaultListModel incTable;
  protected JScrollPane      excScroll;
  protected JScrollPane      incScroll;
  protected JPanel           excPanel;
  protected JPanel           incPanel;
  protected JPanel           buttonPanel;
  protected SKDCLabel        excTitle;
  protected SKDCLabel        incTitle;
  protected SKDCButton       incButton;
  protected SKDCButton       excButton;
  protected boolean          sort = true;
  protected JList            excBox;
  protected JList            incBox;
  
  public static final boolean EXCLUDE = false;
  public static final boolean INCLUDE = true;
   
 /**
  *  Default constructor.
  */
  public IncludeExcludeListPanel()
  {
    this(null, null);
  }

 /**
  *  Convenience constructor for panel.
  */
  public IncludeExcludeListPanel(ListModel exc, ListModel inc)
  {
    this.excTable = (DefaultListModel)exc;
    this.incTable = (DefaultListModel)inc;
    excBox = new JList(excTable);
    incBox = new JList(incTable);
    
    gbconst = new GridBagConstraints();
    gbconst.anchor = GridBagConstraints.CENTER;
    gbconst.weightx = 0.2;
    gbconst.weighty = 0.8;
    gbconst.gridwidth = GridBagConstraints.REMAINDER;

    excScroll = new JScrollPane(excBox);
    excScroll.setPreferredSize(new Dimension(75,150));
    excScroll.setBorder(new EtchedBorder());

    incScroll = new JScrollPane(incBox);
    incScroll.setPreferredSize(new Dimension(75,150));
    incScroll.setBorder(new EtchedBorder());

    excPanel = new JPanel(new GridBagLayout());
    excTitle = new SKDCLabel();
    excPanel.add(excTitle, gbconst);
    excPanel.add(excScroll, gbconst);

    incButton = new SKDCButton(">");
    
    incButton.addEvent(SKDCGUIConstants.ADD_BTN, new ButtonListener());
    excButton = new SKDCButton("<");
    excButton.addEvent(SKDCGUIConstants.DELETE_BTN, new ButtonListener());
    
    buttonPanel = new JPanel(new GridBagLayout());
    buttonPanel.add(incButton, gbconst);
    buttonPanel.add(excButton, gbconst);
    
    incPanel = new JPanel(new GridBagLayout());
    incTitle = new SKDCLabel();
    incPanel.add(incTitle, gbconst);
    incPanel.add(incScroll, gbconst);
    
    this.add(excPanel);
    this.add(buttonPanel);
    this.add(incPanel);

//    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//    inputPanel = new JPanel(new GridBagLayout());
//    buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//
//                                       // Create Grid bag constraints for input
//                                       // panel.
//    gbconst = new GridBagConstraints();
//    gbconst.insets = new Insets(4, 4, 10, 4);
//    add(inputPanel);
//    add(Box.createVerticalStrut(2));       
  }

  /**
   *  Method sets the label at the top of the list columns.
   *
   *  @param include <code>boolean</code> indicating whether for include list or exclude list.
   *  @param title <code>String</code> containing the title.
   */
   public void setLabel(boolean include, String title)
   {
     if (include)
     {
       incTitle.setText(title);
     }
     else
     {
       excTitle.setText(title);
     }
   }
  
  /**
   *  Method sets the label at the top of the list columns.
   *
   *  @param value <code>boolean</code> indicating whether or not to sort the lists.
   */
   public void setSort(boolean value)
   {
     sort = value;
   }
  
  /**
   *  Method sets the data.
   */
   public void setData()
   {
//     excBox = new JList(excTable);
//     incBox = new JList(incTable);
   }
  

  /**
   *  Inner listener class for Include/Exclude button events.
   */
  class ButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(SKDCGUIConstants.ADD_BTN))
      {
        while (true)
        {
          int index = excBox.getSelectedIndex();
          if (index == -1)
            break;
          Object o = excBox.getModel().getElementAt(index);
          incTable.addElement(o);
          excTable.remove(index);
        }
        if (sort)
        {
          Object[] temphold = incTable.toArray();
          Arrays.sort(temphold);
          incTable.clear();
          for(int i=0; i < temphold.length; i++)
          {
            incTable.addElement(temphold[i]);
          }
        }
      }
      else if (which_button.equals(SKDCGUIConstants.DELETE_BTN))
      {
        while (true)
        {
          int index = incBox.getSelectedIndex();
          if (index == -1)
            break;
          Object o = incBox.getModel().getElementAt(index);
          excTable.addElement(o);
          incTable.remove(index);
        }
        if (sort)
        {
          Object[] temphold = excTable.toArray();
          Arrays.sort(temphold);
          excTable.clear();
          for(int i=0; i < temphold.length; i++)
          {
            excTable.addElement(temphold[i]);
          }
        }
      }
    }
  }
}
