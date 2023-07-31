package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * Description:<BR>
 *    Base class for setting up a two-column input panel.  By "two-column panel"
 *    is meant a <code>JPanel</code> which has a column for labels, and a column for input
 *    boxes (whatever those boxes may be -- JTextBox, or JComboBox etc).  This
 *    panel is built using the <code>GridBagLayout</code>.
 *
 * @author       A.D.
 * @version      1.0
 */
@SuppressWarnings("serial")
public abstract class SKDCDialogPanel extends JPanel
{
  private int                        maxWidth, maxHeight;
  private String                     txtTitle = "";
  private Map<SKDCLabel, ? extends JComponent> columnDefs;
  private int                        titleLength;
  private boolean                    showInputAreaBorder;
  private boolean                    usingTitledBorder;
  private boolean                    hideButtonPanel;
  private JPanel                     inputPanel;
  private JPanel                     buttonPanel;
  private GridBagConstraints         gbconst;
  private ActionListener             buttonListener;
  private Map<String, SKDCButton>    buttonMap;
  protected SKDCInternalFrame        internalFrameRef;
 
 /**
  *  Default constructor.
  */
  public SKDCDialogPanel()
  {
    this(null);
  }

 /**
  *  Constructor to build the SKDC Panel
  */
  public SKDCDialogPanel(SKDCInternalFrame ipInternalFrameRef)
  {
    internalFrameRef = ipInternalFrameRef;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    inputPanel = new JPanel(new GridBagLayout());
    buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

                                       // Create Grid bag constraints for input
                                       // panel.
    gbconst = new GridBagConstraints();
    gbconst.insets = new Insets(4, 4, 10, 4);
    add(inputPanel);
    add(Box.createVerticalStrut(2));       
  }

 /**
  *  Method sets frame reference for window owning this panel.
  *
  *  @param ipInternalFrameRef <code>SKDCInternalFrame</code> containing frame
  *         reference.
  */
  public void setContainerFrameRef(SKDCInternalFrame ipInternalFrameRef)
  {
    internalFrameRef = ipInternalFrameRef;
  }
  
 /**
  *  Method that executes the panel build.  This method must be called last after
  *  all state is set for this instance.
  */
  protected void buildPanel()
  {
    setColumnDefinitions();
    usingTitledBorder = displayInputAreaBorder();
    if (columnDefs != null && !columnDefs.isEmpty())
    {
      insertLabels();
      insertFields();
    }
    
    if (!hideButtonPanel && buttonMap != null && !buttonMap.isEmpty())
    {
      insertButtons();
    }
    setPreferredSize(new Dimension(maxWidth+25, maxHeight+30));
  }
  
  /**
   * Method that executes the panel build.  This method must be called last after
  *  all state is set for this instance.
   * @param inColNum the number of columns per row for a group fields
   */
  protected void buildPanel(int inColNum)
  {
    setColumnDefinitions();
    usingTitledBorder = displayInputAreaBorder();
    if (columnDefs != null && !columnDefs.isEmpty())
    {
      insertLabels(inColNum);
      insertFields(inColNum);
    }
    
    if (!hideButtonPanel && buttonMap != null && !buttonMap.isEmpty())
    {
      insertButtons();
    }
    setPreferredSize(new Dimension(maxWidth+25, maxHeight+30));
  }
  
 /**
  *  Method displays border around designated input area (the area where
  *  text boxes, drop-down lists etc. are inserted).
  *  
  *  @param izShowInputAreaBorder <code>boolean</code> of <code>true</code>
  *         indicates that borders will be displayed around primary input box.
  */
  protected void setInputAreaBorder(boolean izShowInputAreaBorder)
  {
    showInputAreaBorder = izShowInputAreaBorder;
  }
  
 /**
  *  Method sets reference to user supplied <code>Map</code> of Column
  *  Definitions.
  */
  protected void setColumnDefinitions()
  {
    columnDefs = initDisplayColumns();
  }
  
 /**
  *  Method sets title string as either a border title or normal title.  The
  *  following table shows the interdependency of the title string with the
  *  setInputAreaBorder method call which enables or disables input area borders:
  *
  *         <CENTER>
  *            <TABLE border>
  *            <TR>
  *               <TH BGCOLOR = '#CCFFFF'>showInputAreaBorder Flag</TH>
  *               <TH BGCOLOR = '#CCFFFF'>Title String Value</TH>
  *               <TH BGCOLOR = '#CCFFFF'>Meaning</TH>
  *            </TR>
  *            <TR>
  *               <TD ALIGN = 'CENTER'>true</TD>
  *               <TD ALIGN = 'CENTER'>set to non-null</TD>
  *               <TD ALIGN = 'CENTER'>Display titled border</TD>
  *            </TR>
  *            <TR>
  *                <TD ALIGN = 'CENTER'>true</TD>
  *               <TD ALIGN = 'CENTER'>not set</TD>
  *                <TD ALIGN = 'CENTER'>Display border</TD>
  *            </TR>
  *            <TR>
  *               <TD ALIGN = 'CENTER'>false</TD>
  *               <TD ALIGN = 'CENTER'>set to non-null</TD>
  *                <TD ALIGN = 'CENTER'>Display title, but no border</TD>
  *            </TR>
  *            <TR>
  *               <TD ALIGN = 'CENTER'>false</TD>
  *               <TD ALIGN = 'CENTER'>not set</TD>
  *               <TD ALIGN = 'CENTER'>No title and border</TD>
  *            </TR>
  *            </TABLE>
  *         </CENTER>
  *
  * @param sTitle the title string
  */
  protected void setTitleString(String sTitle)
  {
    titleLength = (sTitle == null) ? 0 : sTitle.trim().length();
    txtTitle = (titleLength > 0) ? sTitle : "";
  }

 /**
  *  Enables or disables a row in this panel.
  *  @param enabled <code>boolean</code> of true means enable components.
  *  @param componentName <code>Component</code> carrying left-hand column
  *         component. This parameter usually represents a JLabel.
  *  @param componentValue <code>Component</code> carrying right-hand column
  *         component. This parameter usually represents a JPanel, JComboBox etc.
  */
  protected void enableRow(boolean enabled, Component componentName,
                           Component componentValue)
  {
    enableRow(enabled, componentName, componentValue, null);
  }

 /**
  *  Enables or disables a row in this panel.
  *  @param enabled <code>boolean</code> of true means enable components.
  *  @param componentName <code>Component</code> carrying left-hand column
  *         component. This parameter usually represents a JLabel.
  *  @param componentValue <code>Component</code> carrying right-hand column
  *         component. This parameter usually represents a JPanel, JComboBox etc.
  *  @param excludedComp <code>Component</code> carrying component to exclude
  *         from the enable/disable operation.
  */
  protected void enableRow(boolean enabled, Component componentName,
                           Component componentValue, Component excludedComp)
  {
    if (componentName instanceof JLabel && componentName != excludedComp)
        componentName.setEnabled(enabled);
    else if (componentName instanceof JPanel)
      enablePanelComponents((JPanel)componentName, null, enabled);

    Component[] myExcludeList = (excludedComp == null) ? null
                                                       : new Component[] {excludedComp};
    if (componentValue instanceof JPanel)
      enablePanelComponents((JPanel)componentValue, myExcludeList, enabled);
    else if (isPartOfIncludeList(componentValue))
      componentValue.setEnabled(enabled);
  }

 /**
  *  Method provides for enabling or disabling all components of this panel.
  *
  *  @param enabled <code>boolean</code> of <code>true</code> <b>(default
  *         behaviour)</b> enables the panel. <code>false</code> disables
  *         it.
  */
  protected void enableAllComponents(boolean enabled)
  {
    enablePanelComponents(this, null, enabled);
  }

 /**
  *  Method provides for enabling or disabling a input panel.  By "input" panel
  *  is meant the area containing labels and input fields.
  *
  *  @param enabled <code>boolean</code> of <code>true</code> <b>(default
  *         behaviour)</b> enables the panel. <code>false</code> disables
  *         it.
  */
  protected void enableInputPanel(boolean enabled)
  {
    enablePanelComponents(inputPanel, null, enabled);
  }

 /**
  *  Method provides for enabling or disabling a button panel.
  *
  *  @param enabled <code>boolean</code> of <code>true</code> <b>(default
  *         behaviour)</b> enables the button panel. <code>false</code> disables
  *         it.
  */
  protected void enableButtonPanel(boolean enabled)
  {
    enablePanelComponents(buttonPanel, null, enabled);
  }

 /**
  *  Method provides for showing or hiding button panel.
  *
  *  @param displayPanel <code>boolean</code> of <code>true</code> <b>(default
  *         behaviour)</b> displays the button panel. <code>false</code> hides it.
  */
  protected void showButtonPanel(boolean displayPanel)
  {
    hideButtonPanel = !displayPanel;
  }

 /**
  *  Method installs a default button listener for the default button panel.
  *  Default button listeners are set up for Search, Clear, and Close buttons.
  */
  protected void setButtonListener()
  {
    Map<String, SKDCButton> myButtonMap = new LinkedHashMap<String,SKDCButton>();
    myButtonMap.put(SKDCGUIConstants.OK_BTN, new SKDCButton(" Ok ", "Execute Action", 'O'));
    myButtonMap.put(SKDCGUIConstants.RESET_BTN, new SKDCButton("  Reset ", "Reset fields to defaults.", 'R'));
    myButtonMap.put(SKDCGUIConstants.CLOSE_BTN, new SKDCButton(" Close ", "Close this window.", 'l'));
    setButtonListener(getDefaultButtonListener(), myButtonMap);
  }

 /**
  *  Method installs a user specified button listener, and displays buttons.
  *
  *  @param ipButtonListener <code>ActionListener</code> containing reference to user
  *         defined Button Listener.
  *  @param ipButtons <code>Map</code> containing list of buttons user wants
  *         to add.  The Map key name is used as an id. string for the button,
  *         and the map values are used as the label for the button.
  */
  protected void setButtonListener(ActionListener ipButtonListener, Map<String, SKDCButton> ipButtons)
  {
    buttonListener = ipButtonListener;
    buttonMap = ipButtons;
  }

/*==========================================================================
                  Private Methods go in this section.
  ==========================================================================*/
  private void defaultPanelComponentSettings(JPanel panel)
  {
    Component[] comp = panel.getComponents();

    for(int i = 0; i < comp.length; i++)
    {                                  // If the component is enabled, reset
      if (comp[i].isEnabled())         // values.
      {
        if (comp[i] instanceof SKDCDoubleField)
        {
          ((SKDCDoubleField)comp[i]).setValue(0.0);
        }
        else if (comp[i] instanceof SKDCIntegerField)
        {
          ((SKDCIntegerField)comp[i]).setValue(0);
        }
        else if (comp[i] instanceof JTextField)
        {
          ((SKDCTextField)comp[i]).setText("");
        }
        else if (comp[i] instanceof SKDCTranComboBox)
        {
          ((SKDCTranComboBox)comp[i]).resetDefaultSelection();
        }
        else if (comp[i] instanceof SKDCCalendar)
        {
          ((SKDCCalendar)comp[i]).setCalendar(Calendar.getInstance());
        }
        else if (comp[i] instanceof JComboBox)
        {
          ((JComboBox)comp[i]).setSelectedIndex(0);
        }
        else if (comp[i] instanceof JCheckBox)
        {
          JCheckBox chkBox = (JCheckBox)comp[i];
          if (chkBox.isSelected()) chkBox.doClick();
        }
        else if (comp[i] instanceof JRadioButton)
        {
          JRadioButton radButton = (JRadioButton)comp[i];
          if (radButton.isSelected()) radButton.doClick();
        }
        else if (comp[i] instanceof LocationPanel)
        {
          ((LocationPanel)comp[i]).reset();
        }
        else if (comp[i] instanceof JPanel)
        {
          defaultPanelComponentSettings((JPanel)comp[i]);
        }
      }
    }
  }

  /**
  *  Enables or disables all components in a panel.  Components specified in
  *  an exclusion list are not modified.
  */
  private void enablePanelComponents(JPanel panel, Component[] excludeList,
                                     boolean enabled)
  {
    if (panel == null) return;

    Component[] comp = panel.getComponents();

    for(int i = 0; i < comp.length; i++)
    {
      if (comp[i] instanceof JButton)
      {
        if (comp[i] instanceof SKDCButton)
        {
          if (!((SKDCButton)comp[i]).isDisableAllowed())
          {
            continue;
          }
        }
        comp[i].setEnabled(enabled);
      }
      else if (isPartOfIncludeList(comp[i]) &&
               !isPartOfCallerExclusionList(comp[i], excludeList))
      {
        comp[i].setEnabled(enabled);
      }
      else if (comp[i] instanceof JPanel)
      {
        enablePanelComponents((JPanel)comp[i], excludeList, enabled);
      }
    }
  }

  private boolean isPartOfIncludeList(Component comp)
  {
    return(comp instanceof JTextField || comp instanceof JLabel    ||
           comp instanceof JComboBox  || comp instanceof JCheckBox ||
           comp instanceof JRadioButton);
  }

  private boolean isPartOfCallerExclusionList(Component comp,
                                              Component[] excludeList)
  {
    if (excludeList == null || excludeList.length == 0) return(false);

    boolean rtn = false;
    String[] myClass = SKDCUtility.getTokens(comp.getClass().getName(), ".");
    String myClassName = myClass[myClass.length-1];

    for(int k = 0; k < excludeList.length; k++)
    {
      String[] excludeClass = SKDCUtility.getTokens(excludeList[k].getClass().getName(), ".");
      String excludeClassName = excludeClass[excludeClass.length-1];
      if (myClassName.equals(excludeClassName))
      {
        rtn = true;
        break;
      }
    }

    return(rtn);
  }

 /**
  *  Method displays a border for the primary input area on the panel.
  *
  *  @return <code>boolean</code> of <code>true</code> if the border used was
  *  a titled border. <code>false</code> otherwise.
  */
  private boolean displayInputAreaBorder()
  {
    boolean vzUsingTitledBorder = false;

    if (showInputAreaBorder)
    {
      Border theBorder = BorderFactory.createEtchedBorder();
      if (titleLength > 0)
      {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(theBorder,
                                                                     txtTitle);
        inputPanel.setBorder(titledBorder);
        vzUsingTitledBorder = true;
      }
      else
      {
        inputPanel.setBorder(theBorder);
      }
    }

    return(vzUsingTitledBorder);
  }

  private void insertButtons()
  {
    SKDCButton buttonRef = null;
    buttonPanel.setBorder(BorderFactory.createEtchedBorder());
    Iterator<String> btnItr = buttonMap.keySet().iterator();
    while(btnItr.hasNext())
    {
      String sActionID = btnItr.next();
      buttonRef = buttonMap.get(sActionID);
      buttonRef.addEvent(sActionID, buttonListener);
      buttonPanel.add(buttonRef);
    }
    maxHeight += buttonPanel.getMinimumSize().getHeight();
    if (buttonRef != null)
      maxHeight += 2*buttonRef.getInsets().bottom;
    add(buttonPanel);
  }
  
 /**
  *  Insert label column into JPanel.  <b>Note:</b><i> if the label column's
  *  corresponding field column (normally a JComponent or Component) is set to
  *  <b>not</b> be visible, the label will not be visible either.</i>
  */
  private void insertLabels()
  {
    gbconst.gridx = 0;
    gbconst.gridy = 0;
    if (!usingTitledBorder && titleLength > 0)
    {
      gbconst.gridwidth = GridBagConstraints.REMAINDER;
      gbconst.anchor = GridBagConstraints.CENTER;
      inputPanel.add(new SKDCHeaderLabel(txtTitle), gbconst);
      gbconst.gridy = 1;
    }
    gbconst.gridwidth = 1;
    gbconst.anchor = GridBagConstraints.EAST;
    gbconst.weightx = 0.2;
    gbconst.weighty = 0.7;

    int maxLabelWidth = titleLength;
    Iterator<SKDCLabel> nameItr = columnDefs.keySet().iterator();
    while(nameItr.hasNext())
    {
      SKDCLabel theLabel = nameItr.next();
      inputPanel.add(theLabel, gbconst);
      gbconst.gridy++;
      Dimension compDimen = (theLabel).getPreferredSize();
      if (maxLabelWidth < compDimen.width) maxLabelWidth = compDimen.width;
    }

    maxWidth = maxLabelWidth;
  }
  
  /**
   *  Insert label column into JPanel.  <b>Note:</b><i> if the label column's
   *  corresponding field column (normally a JComponent or Component) is set to
   *  <b>not</b> be visible, the label will not be visible either.</i>
   * @param inColNum the number of columns per row in a group
   */
  private void insertLabels(int inColNum)
  {
    gbconst.gridx = 0;
    gbconst.gridy = 0;
    if (!usingTitledBorder && titleLength > 0)
    {
      gbconst.gridwidth = GridBagConstraints.REMAINDER;
      gbconst.anchor = GridBagConstraints.CENTER;
      inputPanel.add(new SKDCHeaderLabel(txtTitle), gbconst);
      gbconst.gridy = 1;
    }
    gbconst.gridy--;
    gbconst.gridwidth = 1;
    gbconst.anchor = GridBagConstraints.EAST;
    gbconst.weightx = 0.2;
    gbconst.weighty = 0.7;
  
    int maxLabelWidth = titleLength;
    int vnColNum = 0;
    Iterator<SKDCLabel> nameItr = columnDefs.keySet().iterator();
    while(nameItr.hasNext())
    {
      SKDCLabel theLabel = nameItr.next();
      // If the label has no text, check if the number of columns has been reached.
      // If it has, increase the row number.
      if (theLabel.getText().length() > 0)
      {
        gbconst.gridy++;
        inputPanel.add(theLabel, gbconst);
        Dimension compDimen = (theLabel).getPreferredSize();
        if (maxLabelWidth < compDimen.width) maxLabelWidth = compDimen.width;
      }
      else
      {
        vnColNum++;
        if (vnColNum >= inColNum)
        {
          vnColNum = 0;
          gbconst.gridy++;
        }
      }
    }
  
    maxWidth = maxLabelWidth;
  }

  private void insertFields()
  {
    gbconst.gridy = (!usingTitledBorder && titleLength > 0) ? 1 : 0;
    gbconst.gridx = 1;
    gbconst.anchor = GridBagConstraints.WEST;

    Iterator<SKDCLabel> nameItr = columnDefs.keySet().iterator();
    int maxInputBoxWidth = 0, maxInputBoxHeight = 0;
    while(nameItr.hasNext())
    {
      Object theInputBox = columnDefs.get(nameItr.next());
      inputPanel.add((JComponent)theInputBox, gbconst);
      gbconst.gridy++;

      Dimension compDimen = ((JComponent)theInputBox).getPreferredSize();
      if (maxInputBoxWidth < compDimen.width) maxInputBoxWidth = compDimen.width;
      maxInputBoxHeight += compDimen.height;
      maxInputBoxHeight += 3*((JComponent)theInputBox).getInsets().bottom;
    }

     maxWidth += maxInputBoxWidth;
     maxHeight = maxInputBoxHeight;
  }

  private void insertFields(int inColNum)
  {
    gbconst.gridy = (!usingTitledBorder && titleLength > 0) ? 1 : 0;
    gbconst.gridx = 1;
    gbconst.anchor = GridBagConstraints.WEST;

    gbconst.gridy--;
    int saved_left = gbconst.insets.left;

    Iterator<SKDCLabel> nameItr = columnDefs.keySet().iterator();
    int maxInputBoxWidth = 0, maxInputBoxHeight = 0;
    int vnColNum = 0;
    int vnXOffset = 0;
    while(nameItr.hasNext())
    {
      SKDCLabel theLabel = nameItr.next();
      Object theInputBox = columnDefs.get(theLabel);
      // If the field has no label, try to place it in the same row as previous one.
      if (theLabel.getText().length() == 0)
      {
        gbconst.insets.left += vnXOffset;
        vnColNum++;
        // If number of columns has been reached, place it to next row.
        if (vnColNum >= inColNum)
        {
          vnColNum = 0;
          gbconst.insets.left = saved_left;
          gbconst.gridy++;
        }
      }
      else
      {
        gbconst.gridy++;
        gbconst.insets.left = saved_left;
      }
      inputPanel.add((JComponent)theInputBox, gbconst);
      
      Dimension compDimen = ((JComponent)theInputBox).getPreferredSize();
      if (maxInputBoxWidth < compDimen.width) maxInputBoxWidth = compDimen.width;
      maxInputBoxHeight += compDimen.height;
      maxInputBoxHeight += 3*((JComponent)theInputBox).getInsets().bottom;
      vnXOffset = compDimen.width;
    }

     maxWidth += maxInputBoxWidth;
     maxHeight = maxInputBoxHeight;
  }

  private ActionListener getDefaultButtonListener()
  {
    return(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String which_button = e.getActionCommand();
        if (which_button.equals(SKDCGUIConstants.RESET_BTN))
        {
          resetButtonPressed();
        }
        else if (which_button.equals(SKDCGUIConstants.OK_BTN))
        {
          okButtonPressed();
        }
        else if (which_button.equals(SKDCGUIConstants.CLOSE_BTN))
        {
          closeButtonPressed();
        }
      }
    });
  }
  
 /**
  *  Default method to close frame.
  */
  protected void closeButtonPressed()
  {
    if (internalFrameRef != null)
    {
      try { internalFrameRef.setClosed(true); } catch(PropertyVetoException pv) {}
    }    
  }

 /**
  *  Method to execute desired action.
  */
  protected void okButtonPressed()
  {
    JOptionPane.showInternalMessageDialog(this, "Please override the OK method",
                                          "Check Button", JOptionPane.INFORMATION_MESSAGE);
  }
  
 /**
  *  Default method to clear all input fields, and set combo-boxes to default
  *  values.
  */
  protected void resetButtonPressed()
  {
    defaultPanelComponentSettings(inputPanel);
  }

 /**
  *  Method initialises display columns for this Panel.
  *
  *  @return <code>Map</code> of column data.
  */
  abstract protected Map<SKDCLabel, ? extends JComponent> initDisplayColumns();
}
