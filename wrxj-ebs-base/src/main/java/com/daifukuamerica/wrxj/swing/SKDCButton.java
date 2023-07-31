package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

/**
 * Description:<BR>
 *   SK Daifuku button component for screens.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 01-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class SKDCButton extends JButton
        implements SKDCGUIConstants, KeyEventDispatcher
{
  private   boolean allowDisable = true;  // Allow the button to be disabled.
  protected   boolean timingOn = false;
  private boolean showDisabledButtons = false;
  private  boolean authorized = true;

  /**
   *  Create button component.
   *
   */
  public SKDCButton()
  {
   super();
   setupButtonDefaults();
  }

  /**
   *  Create button component with a label.
   *
   *  @param btnlabel Label to be put on button.
   */
  public SKDCButton(String btnlabel)
  {
    this();
    setText(btnlabel);
  }

  /**
   *  Create button component with an Icon.
   *
   *  @param btnIcon Icon to be put on button.
   */
  public SKDCButton(Icon btnIcon)
  {
    super(btnIcon);
  }

 /**
  *  Create button component with an Icon, and tool tip.
  *
  *  @param btnIcon Icon to be put on button.
  *  @param helptext String to be put on button.
  */
  public SKDCButton(Icon btnIcon, String helptext)
  {
    super(btnIcon);
    setToolTipText(helptext);
  }

 /**
  *  Create button component with an action.
  *
  *  @param action Action to be associated with this button.
  */
  public SKDCButton(Action action)
  {
    super(action);
    setupButtonDefaults();
  }

 /**
  *  Create button component with label and tooltip.
  *
  *  @param btnlabel Label to be put on button.
  *  @param helptext Text to show up as tool tip.
  */
  public SKDCButton(String btnlabel, String helptext)
  {
    this(btnlabel);
    this.setToolTipText(helptext);
  }

  /**
   *  Create button component with label, tooltip and hot key.
   *
   *  @param btnlabel Label to be put on button.
   *  @param helptext Text to show up as tool tip.
   *  @param hotkey Character to be used as menu hot key.
   */
  public SKDCButton(String btnlabel, String helptext, char hotkey)
  {
    this(btnlabel, helptext);
    this.setMnemonic(hotkey);
  }

  /**
   * Overridden to translate the button text
   */
  @Override
  public void setText(String isText)
  {
    super.setText(DacTranslator.getTranslation(isText));
  }

  /**
   * Make sure the mnemonic is still useful after translation
   */
  @Override
  public void setMnemonic(char mnemonic)
  {
    if (!getText().toUpperCase().contains("" + Character.toUpperCase(mnemonic)))
    {
      /*
       * This might lead to duplicates, but I'm hoping that this is better than
       * none at all.
       */
      mnemonic = getText().charAt(0);
    }
    super.setMnemonic(mnemonic);
  }

 /**
  * {@inheritDoc}  Create multi-line tooltip for long strings.
  * @return JToolTip instance to Swing tooltip manager.
  */
  @Override
  public JToolTip createToolTip()
  {
    DacParagraphToolTip vpTTip = new DacParagraphToolTip();
    vpTTip.setComponent(this);
    return(vpTTip);
  }

  /**
   *  Method to check key strokes.
   *
   *  @param ke KeyEvent that occurred.
   *  @return boolean of <code>true</code> when KeyEvent should not be dispatched
   *  to other KeyEventDispatchers.
   */
  public boolean dispatchKeyEvent(KeyEvent ke)
  {
    if (this.isFocusOwner() && (ke.getID() == KeyEvent.KEY_PRESSED) &&
       (ke.getKeyCode() == KeyEvent.VK_ENTER))
    {
      this.doClick();
      ke.consume();

      // See JAVA API Specification for returned value
      // Should return true when KeyEvent should not be dispatched to other KeyEventDispatcher
      return true;
    }
    return false;
  }

 /**
  *  Method to add an action listener to an action.
  *
  *  @param actionID Action command string.
  *  @param listener actionListener to be added.
  */
  public void addEvent(String actionID, ActionListener listener)
  {
    if (actionID == null || actionID.length() == 0)
    {
      JOptionPane.showMessageDialog(null, "Invalid actionID passed.",
                                    "Event Add Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }

    this.setActionCommand(actionID);
    this.addActionListener(listener);
  }

 /**
  *  Method to enable / disable the component time out.
  *
  *  @param on Enable value.
  */
    public void enableTimer(boolean on)
    {
      timingOn = on;
    }

 /**
  *  Method to restart the component time out.
  *
  */
    public void restartTimer ()
    {
//    System.out.println("Timer restarted SKDCButton");
      firePropertyChange(FRAME_TIMER_RESTART,"Old","New");
    }

 /**
  *  Method to specify whether component timeout can be disabled.
  *
  *  @param disable_allowed turns it on or off.
  */
    public void disableAllowed(boolean disable_allowed)
    {
      this.allowDisable = disable_allowed;
    }

 /**
  *  Method to get whether disable is allowed or not.
  *
  *  @return boolean containing allow disable value.
  */
    public boolean isDisableAllowed()
    {
      return(this.allowDisable);
    }

    private void setupButtonDefaults()
    {
      this.addFocusListener(new java.awt.event.FocusAdapter()
      {
        @Override
        public void focusGained(FocusEvent fe)
        {
          addKeyListener();
//        System.out.println("---- focusGained" + fe.getClass().getName());
          if (timingOn)
          {
            restartTimer();
          }
        }
        @Override
        public void focusLost(FocusEvent fe)
        {
          removeKeyListener();
        }
      });
   }

   void addKeyListener()
   {
      DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
   }

   void removeKeyListener()
   {
      DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
   }

  /*------------------------------------------------------------------------*/
  //
  // Set button authorization. Determines its characteristics.
  //
  /*------------------------------------------------------------------------*/
  public void setAuthorization(boolean authorization)
  {
    authorized = authorization;
    if (!authorization)
    {
      if (showDisabledButtons)
      {
        super.setEnabled(false);
      }
      else
      {
        this.setVisible(false);
      }
    }
  }

  @Override
  public void setEnabled(boolean enable)
  {
    if (authorized)
    {
      super.setEnabled(enable);
    }
  }

  /**
   * Sets Enabled property with thread safety.
   *
   * <p><b>Details:</b> <code>setEnabledTs</code> sets the <code>Enabled</code>
   * property of the supplied button to the supplied <code>Enabled</code> value.
   * The difference between this implementation and
   * <code>JButton.setEnabled</code> is that this implementation is
   * thread-<wbr>safe.  It can be called from any thread at any time.</p>
   *
   * @param ipButton button whose Enabled property value is being set
   * @param izEnabled new Enabled property value
   */
  public static void setEnabledTs(final JButton ipButton, final boolean izEnabled)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      ipButton.setEnabled(izEnabled);
      return;
    }
    try
    {
      SwingUtilities.invokeAndWait
      ( new Runnable()
        { public void run()
          { ipButton.setEnabled(izEnabled);
          }
        }
      );
    }
    catch (final Exception ve)
    {
      SKDCUtility.rethrow(ve);
    }
  }

}

