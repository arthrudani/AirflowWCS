package com.daifukuamerica.wrxj.swingui.user;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

/**
 * Visual representation of password strength, based upon reading and 
 * observations using the password checker on 
 * <A HREF=https://www.microsoft.com/protect/fraud/passwords/checker.aspx>Microsoft's
 * website</A>.  This class seems to produce identical results.
 * 
 * <P>The one at <A HREF=http://www.passwordmeter.com/>PasswordMeter.com</A>
 * seems to be a little more stringent, and includes some explanations.  It also
 * has code (JavaScript), but its GPL.</P>
 * 
 * @author mandrus
 */
public class PasswordStrengthChecker extends JProgressBar
{
  private static final int NONE = 0;
  private static final int WEAK = 25;
  private static final int MEDIUM = 50;
  private static final int GOOD = 75;
  private static final int MAXIMUM = 100;
  
  JPasswordField mpPassword;
  StandardUserServer mpUserServer;
  
  /**
   * Constructor
   * 
   * @param ipLinkedField - linked password field
   * @param ipUserServer - used to get additional password requirements
   */
  public PasswordStrengthChecker(JPasswordField ipLinkedField,
      StandardUserServer ipUserServer)
  {
    setMaximum(100);
    setToolTipText(getToolTipPasswordInfo());
    Dimension vpDim = ipLinkedField.getPreferredSize();
    vpDim.width /= 2;
    setPreferredSize(vpDim);
    BasicProgressBarUI ui = new BasicProgressBarUI() {
      @Override
      protected Color getSelectionBackground()
      {
        return Color.BLACK;
      }
      @Override
      protected Color getSelectionForeground()
      {
        return Color.BLACK;
      }
    };
    setUI(ui);
    setStringPainted(true);
    
    mpPassword = ipLinkedField;
    mpPassword.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e)
      {
        super.keyReleased(e);
        evaluatePassword();
      }
    });
    
    mpUserServer= ipUserServer;

    evaluatePassword();
  }
  
  /**
   * Get the password requirements/suggestions
   */
  public String getToolTipPasswordInfo()
  {
    return "<html>A strong password has 14+ characters and 3 or more of the following:"
    + "<BR>1) Upper case letters"
    + "<BR>2) Lower case letters"
    + "<BR>3) Digits"
    + "<BR>4) Other characters</html>";
  }
  
  /**
   * Evaluate the password so far
   */
  protected void evaluatePassword()
  {
    char[] vacPassword = new String(mpPassword.getPassword()).trim().toCharArray();
    
    // Check customer-specific requirements (baseline has none)
    try
    {
      mpUserServer.checkPasswordRequirements(new String(vacPassword));
    }
    catch (Exception e)
    {
      passwordCheckFailed(e);
      return;
    }
    
    // Check password strength
    if (vacPassword.length == 0)
    {
      updateProgressBar(NONE);
    }
    else if (vacPassword.length < 8)
    {
      updateProgressBar(WEAK);
    }
    else
    {
      int vzLower = 0;
      int vzUpper = 0;
      int vzDigit = 0;
      int vzOther = 0;
      
      for (char c : vacPassword)
      {
        if (Character.isUpperCase(c))
        {
          vzUpper = 1;
        }
        else if (Character.isLowerCase(c))
        {
          vzLower = 1;
        }
        else if (Character.isDigit(c))
        {
          vzDigit = 1;
        }
        else
        {
          vzOther = 1;
        }
      }

      int vnTotal = vzLower + vzUpper + vzDigit + vzOther;
      int vnScore;
      switch (vnTotal)
      {
         case 1:
           vnScore = WEAK;
           break;
         case 2:
           vnScore = MEDIUM;
           break;
         default:
           vnScore = (vacPassword.length < 14) ? GOOD : MAXIMUM;
      }
      updateProgressBar(vnScore);
    }
  }

  /**
   * Clear
   */
  protected void clearProgressBar()
  {
    setValue(0);
    setString("");
    setToolTipText(null);
  }
  
  /**
   * The UserServer password check failed.
   */
  protected void passwordCheckFailed(Exception e)
  {
    setForeground(Color.RED);
    setValue(MAXIMUM);
    setString("Unacceptable: " + e.getMessage());
  }
  
  /**
   * Update the progress bar for the password strength
   * 
   * @param iePS
   */
  protected void updateProgressBar(int inStrength)
  {
    if (inStrength == NONE)
    {
      inStrength = MAXIMUM;
      setForeground(Color.RED);
      setString("None");
    }
    else if (inStrength <= WEAK)
    {
      setForeground(Color.RED);
      setString("Weak");
    }
    else if (inStrength <= MEDIUM)
    {
      setForeground(Color.YELLOW);
      setString("Medium");
    }
    else if (inStrength == MAXIMUM)
    {
      setForeground(Color.GREEN);
      setString("Strong");
    }
    else // Good
    {
      setForeground(Color.GREEN);
      setString("Good");
    }
    setValue(inStrength);
  }

  /**
   * Enable or disable this component
   * 
   * @see javax.swing.JComponent#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean izEnabled)
  {
    super.setEnabled(izEnabled);
    if (izEnabled)
    {
      setToolTipText(getToolTipPasswordInfo());
      evaluatePassword();
    }
    else
    {
      clearProgressBar();
    }
  }
}
