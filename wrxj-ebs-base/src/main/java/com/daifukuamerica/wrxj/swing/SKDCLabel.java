package com.daifukuamerica.wrxj.swing;

import javax.swing.JLabel;

/**
 * Description:<BR>
 *    Class to create Labels with a consistent look and feel.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 01-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class SKDCLabel extends JLabel
{
  private static final long serialVersionUID = 0L;
  private String msUntranslated;

  public SKDCLabel(String label_text, int inOrientation)
  {
    super(label_text, inOrientation);
  }

  public SKDCLabel(String label_text)
  {
    super(label_text);
  }

  public SKDCLabel()
  {
    this("");
  }

  /**
   * Overridden to translate the button text
   */
  @Override
  public void setText(String isText)
  {
    if (isText == null)
    {
      super.setText(null);
      return;
    }
    if (msUntranslated == null)
      msUntranslated = "";
    if (!msUntranslated.equals(isText))
    {
      super.setText(DacTranslator.getTranslation(isText));
      msUntranslated = new String(isText);
    }
  }
}
