package com.daifukuamerica.wrxj.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;


/**
 * <P><B>Description:</B> Combines a Component and a Modify CheckBox</P>
 * 
 * @author       Y. Kang<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2009 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class SKDCComboPanel extends JPanel
{
  JComponent mpComponent;
  JCheckBox  mpCheckBox;

  /**
   * Constructor
   * @param ipChoices
   */
  public SKDCComboPanel(JComponent ipComponent)
  {
    super();
    
    mpComponent = ipComponent;
    mpComponent.setEnabled(false);
    
    buildPanel();
  }

  /**
   * 
   */
  private void buildPanel()
  {
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    mpCheckBox = new JCheckBox("Modify");
    mpCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          mpComponent.setEnabled(mpCheckBox.isSelected());
          if (mpCheckBox.isSelected())
          {
            mpComponent.requestFocus();
          }
        }
      });
    
    mpComponent.setEnabled(false);
    
    add(mpComponent);
    add(Box.createHorizontalStrut(10));
    add(mpCheckBox);
  }
  
  /**
   * 
   * @return
   */
  public boolean isCheckBoxSelected()
  {
    return mpCheckBox.isSelected();
  }
  
  /**
   * Add an ActionListener to the CheckBox
   * @param ipListener
   */
  public void addActionListener(ActionListener ipListener)
  {
    mpCheckBox.addActionListener(ipListener);
  }
  
  /**
   * Enable/Disable all components.
   */
  @Override
  public void setEnabled(boolean izEnabled)
  {
    // only enable component if izEnabled = true and check box has been selected
    if (izEnabled && isCheckBoxSelected())
    {
      mpComponent.setEnabled(true);
    }
    else
    {
      mpComponent.setEnabled(false);
    }

    mpCheckBox.setEnabled(izEnabled);
  }
}
