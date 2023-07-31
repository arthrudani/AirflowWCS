package com.daifukuamerica.wrxj.swing;

import javax.swing.SwingConstants;

/**
 * Description:<BR>
 *    Class to create Header Labels in a consistent way.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 01-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class SKDCHeaderLabel extends SKDCLabel
{
  private static final long serialVersionUID = 0L;

    public SKDCHeaderLabel(String label_text)
    {
        super("  " + label_text + "  ");
        this.setHorizontalAlignment(SwingConstants.CENTER);
    }
}
