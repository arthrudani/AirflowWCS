package com.daifukuamerica.wrxj.swingui.recovery.agv;

import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Frame to send AGV commands without the involvement of another system.
 * 
 * @author A.D.
 * @since  08-Jun-2009
 */
public class AGVRecoveryFrame extends SKDCInternalFrame
{
  private final String MOVE_TITLE = "MOV Command";
  private final String SYSTEM_TITLE = "SYS Command";

  private MOVTabPanel      mpMOVTabPanel;
  private SYSTabPanel      mpSYSTabPanel;

  private SKDCButton       mpBtnSubmit;
  private SKDCButton       mpBtnRecover;
  private SKDCButton       mpBtnDelete;
  private SKDCButton       mpBtnCancel;

  private JTabbedPane mpTabbedPane;
  private SKDCScreenPermissions mpPerm;

 /**
  * Default constructor
  */
  public AGVRecoveryFrame()
  {
    super("AGV Command generator");
    mpTabbedPane = new JTabbedPane();
    buildScreen();

    Container vpCont = getContentPane();
    vpCont.add(mpTabbedPane, BorderLayout.CENTER);
    vpCont.add(buildButtonPanel(), BorderLayout.SOUTH);

    if (mpSYSTabPanel != null)
      mpSYSTabPanel.addPropertyChangeListener(AGVTabPanel.SYS_PANEL_EVT,
                                              new SysPanelPropertyListener());
  }

  @Override
  public Dimension getPreferredSize()
  {
    return(new Dimension(900, 400));
  }
  
 /**
  * Build the screen
  */
  private void buildScreen()
  {
    mpMOVTabPanel = Factory.create(MOVTabPanel.class);
    mpTabbedPane.add(MOVE_TITLE, mpMOVTabPanel);
    if (SKDCUserData.isSuperUser())
    {
      mpSYSTabPanel = new SYSTabPanel();
      mpTabbedPane.add(SYSTEM_TITLE, mpSYSTabPanel);
    }

    mpTabbedPane.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent e)
      {
        JTabbedPane vpSelectedTab = (JTabbedPane)e.getSource();
        AGVTabPanel vpTabPanel = (AGVTabPanel)vpSelectedTab.getSelectedComponent();

        if (vpTabPanel == mpMOVTabPanel)
        {
          vpTabPanel.openTabOperations();
          mpBtnSubmit.setText("Add Move");
          mpBtnSubmit.setMnemonic('A');
          mpBtnSubmit.setToolTipText("Add an AGV Move.");

          if (!mpBtnSubmit.isVisible() && mpPerm.iAddAllowed)
          {
            mpBtnSubmit.setVisible(true);
          }
          
          if (!mpBtnDelete.isVisible() && mpPerm.iDeleteAllowed)
          {
            mpBtnCancel.setVisible(true);
            mpBtnDelete.setVisible(true);
          }

          if (!mpBtnRecover.isVisible())
            mpBtnRecover.setVisible(true);
        }
        else
        {
          mpMOVTabPanel.closeTabOperations();
          mpBtnSubmit.setText("Submit");
          mpBtnSubmit.setMnemonic('S');
          mpBtnSubmit.setToolTipText("Submit AGV system-wide command.");
          
          mpBtnRecover.setVisible(false);
          mpBtnDelete.setVisible(false);
          mpBtnCancel.setVisible(false);
          if (!mpBtnSubmit.isVisible() && mpPerm.iAddAllowed)
            mpBtnSubmit.setVisible(true);
        }
      }
    });
  }

  /**
   * Add the default buttons to the button panel
   */
  protected JPanel buildButtonPanel()
  {
    /*
     * Define the default buttons
     */
    mpBtnSubmit = new SKDCButton("Add Move", "Add an AGV Move.", 'A');
    mpBtnDelete = new SKDCButton("Delete Move", "Delete a AGV Move.", 'D');
    mpBtnRecover = new SKDCButton("Recover", "Recovery AGV Move", 'e');
    mpBtnCancel = new SKDCButton("Cancel Move", "Cancel AGV Move", 'C');

    ActionListener vpStandardBtnListener = new StandardButtonListener();
    mpBtnSubmit.addActionListener(vpStandardBtnListener);
    mpBtnRecover.addActionListener(vpStandardBtnListener);
    mpBtnDelete.addActionListener(vpStandardBtnListener);
    mpBtnCancel.addActionListener(vpStandardBtnListener);

    JPanel vpButtonPanel = new JPanel();
    vpButtonPanel.add(mpBtnRecover);
    vpButtonPanel.add(mpBtnSubmit);
    vpButtonPanel.add(mpBtnDelete);
    vpButtonPanel.add(mpBtnCancel);

    SKDCUserData vpUserData = new SKDCUserData();
    mpPerm = vpUserData.getOptionPermissionsByClass(getClass());

    mpBtnSubmit.setAuthorization(mpPerm.iAddAllowed);
    mpBtnDelete.setAuthorization(mpPerm.iDeleteAllowed);
    mpBtnCancel.setAuthorization(mpPerm.iDeleteAllowed);

    return(vpButtonPanel);
  }

  @Override
  protected void okButtonPressed()
  {
    AGVTabPanel vpTabbedPanel = (AGVTabPanel)mpTabbedPane.getSelectedComponent();
    vpTabbedPanel.execDataValidation();
  }

  protected void recoverMove()
  {
    final AGVTabPanel vpTabbedPanel = (AGVTabPanel)mpTabbedPane.getSelectedComponent();
    Thread vpWorkThread = new NamedThread(getClass().getSimpleName() + ".recoverMove()")
    {
      @Override
      public void run()
      {
        vpTabbedPanel.errorCleanUp();
      }
    };

    vpWorkThread.setPriority(Thread.NORM_PRIORITY);
    vpWorkThread.start();
  }

  private void deleteMove()
  {
    AGVTabPanel vpTabbedPanel = (AGVTabPanel)mpTabbedPane.getSelectedComponent();
    vpTabbedPanel.deleteCommand();
  }

  private void cancelMove()
  {
    AGVTabPanel vpTabbedPanel = (AGVTabPanel)mpTabbedPane.getSelectedComponent();
    vpTabbedPanel.cancelCommand();
  }
  
  private class StandardButtonListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      SKDCButton vpPressed = (SKDCButton)e.getSource();
      if (vpPressed == mpBtnSubmit)
      {
        okButtonPressed();
      }
      else if (vpPressed == mpBtnRecover)
      {
        recoverMove();
      }
      else if (vpPressed == mpBtnDelete)
      {
        deleteMove();
      }
      else if (vpPressed == mpBtnCancel)
      {
        cancelMove();
      }
    }
  }

 /**
  * Handle events from System Command panel.
  */
  private class SysPanelPropertyListener implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getSource() == mpSYSTabPanel)
      {
        /*
         * PropertyChangeEvent.newValue = enable/disable flag.
         * PropertyChangeEvent.oldValue = Panel to enable or disable.
         */
        Boolean vpEnableValue = (Boolean)evt.getNewValue();
        JPanel vpPanel = (JPanel)evt.getOldValue();
        enablePanelComponents(vpPanel, vpEnableValue.booleanValue());
      }
    }
  }
}
