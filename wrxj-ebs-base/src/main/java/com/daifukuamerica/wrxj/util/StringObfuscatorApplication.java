package com.daifukuamerica.wrxj.util;

import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * Graphical front-end to StringObfuscator.
 *
 * <p><b>Details:</b> StringObfuscatorApplication is a stand-<wbr>alone GUI
 * application that allows you to encode and decode strings using
 * <code>StringObfuscator</code>.</p>
 *
 * @author Sharky
 */
final class StringObfuscatorApplication extends JFrame
{
  private static final long serialVersionUID = 0L;

  /**
   * Application entry point.
   *
   * <p><b>Details:</b> <code>main</code> requires and recognizes no input
   * parameters.</p>
   *
   * @param ipsArgs ignored
   */
  public static void main(final String[] ipsArgs)
  {
    final Frame mpTest = new StringObfuscatorApplication();
    mpTest.pack();
    mpTest.setVisible(true);
  }

  private final SKDCLabel mpLabel = new SKDCLabel();

  private final JTextField mpTxtInput = new JTextField();

  private final SKDCButton mpButEncode = new SKDCButton();

  private final SKDCButton mpButDecode = new SKDCButton();

  private final JTextField mpTxtResult = new JTextField();

  private StringObfuscatorApplication()
  {
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    getContentPane().setLayout(new GridBagLayout());
    Dimension vpButtonSize = new Dimension(200,25);
    
    mpLabel.setText("Enter string to encode or decode:");
    mpButEncode.setText("encode");
    mpButEncode.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        mpButEncode_actionPerformed(e);
      }
    });
    mpButEncode.setPreferredSize(vpButtonSize);
    mpButDecode.setText("decode");
    mpButDecode.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        mpButDecode_actionPerformed(e);
      }
    });
    mpButDecode.setPreferredSize(vpButtonSize);
    mpTxtResult.setEnabled(false);
    mpTxtResult.setDisabledTextColor(UIManager.getColor("TextField.foreground"));
    mpTxtResult.setBackground(UIManager.getColor("Button.background"));
    mpTxtResult.setText("Ready.");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    
    GridBagConstraints vpGBC= new GridBagConstraints();
    vpGBC.insets = new Insets(4, 4, 4, 4);
    vpGBC.gridx = 0;
    vpGBC.gridy = 0;
    vpGBC.gridwidth = 2;
    vpGBC.fill = GridBagConstraints.BOTH;
    vpGBC.anchor = GridBagConstraints.EAST;
    vpGBC.weightx = 0.2;
    vpGBC.weighty = 0.8;
    
    getContentPane().add(mpLabel, vpGBC);
    vpGBC.gridy++;
    getContentPane().add(mpTxtInput, vpGBC);
    vpGBC.gridy++;
    vpGBC.gridwidth = 1;
    getContentPane().add(mpButEncode, vpGBC);
    vpGBC.gridx = 1;
    getContentPane().add(mpButDecode, vpGBC);
    vpGBC.gridx = 0;
    vpGBC.gridy++;
    vpGBC.gridwidth = 2;
    getContentPane().add(mpTxtResult, vpGBC);
  }

  void mpButEncode_actionPerformed(final ActionEvent e)
  {
    final String vsDecoded = mpTxtInput.getText();
    mpTxtInput.setText(StringObfuscator.encode(vsDecoded));
    mpTxtResult.setText("Encoding succeeded (" + vsDecoded.length() + " characters).");
  }

  void mpButDecode_actionPerformed(final ActionEvent e)
  {
    try
    {
      final String vsDecoded = StringObfuscator.decode(mpTxtInput.getText());
      mpTxtInput.setText(vsDecoded);
      mpTxtResult.setText("Decoding succeeded (" + vsDecoded.length() + " characters).");
    }
    catch (final IllegalArgumentException ve)
    {
      mpTxtResult.setText("Decoding failed.");
    }
  }

}

