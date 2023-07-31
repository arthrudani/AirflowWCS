package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import java.text.NumberFormat;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 * Description:<BR>
 *    Input verifier for Bank, Bay, and Tier fields.  This class will lead
 *    zero-fill an input field if it isn't filled in all the way.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created:  25-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class BankBayTierVerifier extends InputVerifier
{
  private boolean required_input;
  private String  mesg = "";

  public BankBayTierVerifier(boolean izRequiredInput)
  {
    super();
    required_input = izRequiredInput;
  }
                                  // If they give no argument, assume
  public BankBayTierVerifier()    // non-mandatory field.
  {
    this(false);
  }

  @Override
  public boolean shouldYieldFocus(JComponent input_comp)
  {
    if (verify(input_comp)) return(true);

/*---------------------------------------------------------------------------
    ******* Solaris-Java1.4 bug work around: first disable the current
            verifier instance, display the message and then reactivate
            the verifier.  Otherwise we get into infinite recursion since
            shouldYieldFocus is called repeatedly once the focus comes back
            from the JOptionPane.
---------------------------------------------------------------------------*/
    input_comp.setInputVerifier(null);
    JOptionPane.showMessageDialog(null, mesg, "Entry Error",
                                  JOptionPane.ERROR_MESSAGE);
    input_comp.setInputVerifier(this);

    return(false);
  }

  @Override
  public boolean verify(JComponent input_comp)
  {
    SKDCIntegerField intField = (SKDCIntegerField)input_comp;
    int inplen = intField.getText().trim().length();

    boolean rtn = true;             // Default return value.

    if (required_input == true)
    {
      if (inplen == 0)
      {
        /*
         *  Don't make the user stuck until (s)he inputs a random number
         */
        inplen = 1;              
        intField.setText("1");
//        mesg = "Entry required";
//        rtn = false;
      }
      else if (intField.getValue() == 0)
      {
        mesg = "Entry must be non-zero";
        rtn = false;
      }                           // Make sure input is 3 characters long
    }

    if (rtn != false)               // If we already haven't failed!
    {
      if (inplen > 0 && inplen < DBConstants.LNBANK)
      {
        NumberFormat nf3 = NumberFormat.getInstance();
        nf3.setMinimumIntegerDigits(DBConstants.LNBANK);
        intField.setText(nf3.format(intField.getValue()));
      }
    }

    return(rtn);
  }
}
