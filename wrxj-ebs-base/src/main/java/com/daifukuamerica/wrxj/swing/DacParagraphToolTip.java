package com.daifukuamerica.wrxj.swing;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalToolTipUI;

/**
 * Class to create a ToolTip box showing multi-lined text.
 * 
 * @author A.D.
 * @since  03-Dec-2008
 */
public class DacParagraphToolTip extends JToolTip
{
  static final int WORDS_PER_SENTENCE = 6;

  public DacParagraphToolTip()
  {
    setUI(new ParagraphToolTipUI());
  }

  private class ParagraphToolTipUI extends MetalToolTipUI
  {
    String[] masParagraph = new String[0];

   /**
    *  Get general dimension info. about the tooltip box we want to create.
    * @param ipComp a reference to the tool tip component.
    * @return Dimension object taking font size into account.
    */
    @Override
    public Dimension getPreferredSize(JComponent ipComp)
    {
                                       // Get the font's height.
      FontMetrics vpFontMetric = ipComp.getFontMetrics(ipComp.getFont());

      String vsText = ((JToolTip)ipComp).getTipText();
      if (vsText == null) vsText = "";

      int vnMaxLineSize = createParagraph(vpFontMetric, vsText);

      return(new Dimension(vnMaxLineSize + 8,
                           vpFontMetric.getHeight()* masParagraph.length + 6));
    }

   /**
    * Method to create a rectangle containing the paragraph.
    * @param ipGraphic reference to graphics context of the parent component.
    *        This context is used to draw the box and text.
    * @param ipComp reference to JToolTip component.
    */
    @Override
    public void paint(Graphics ipGraphic, JComponent ipComp)
    {
      if (masParagraph.length == 0) return;
                                       // Swing will effectively call
                                       // getPreferredSize now (as defined above).
      Dimension vpBoxDimen = ipComp.getSize();
                                       // Build our rectangle.
      ipGraphic.setColor(ipComp.getBackground());
      ipGraphic.fillRect(0, 0, vpBoxDimen.width, vpBoxDimen.height);
      ipGraphic.setColor(ipComp.getForeground());

                                       // Write the text.
      int vnFontHeight = ipGraphic.getFontMetrics().getHeight();

      for(int vnIdx = 0; vnIdx < masParagraph.length; vnIdx++)
      {
        ipGraphic.drawString(masParagraph[vnIdx], 3, vnFontHeight*(vnIdx+1));
      }
    }

   /**
    * Creates a paragraph from a long string.
    * @param ipFontInfo info about the font being used for the tooltip.
    * @param isLongText the long string to break up into smaller pieces.
    * @return the maximum length from the totality of lines (taking the font
    *         size into account).
    */
    private int createParagraph(FontMetrics ipFontInfo, String isLongText)
    {
      int vnMaxLineSize = 0;
      int vnWords = 0;

      Scanner vpWordScanner = new Scanner(isLongText);
      vpWordScanner.useDelimiter("\\p{Blank}+");
      ArrayList vpList = new ArrayList();
      StringBuilder vpLongString = new StringBuilder();

      while(vpWordScanner.hasNext())
      {
        vpLongString.append(vpWordScanner.next()).append(" ");
        String vsCurrLine = vpLongString.toString();
        int vnCurrLineLength = SwingUtilities.computeStringWidth(ipFontInfo, vsCurrLine);
        vnMaxLineSize = Math.max(vnMaxLineSize, vnCurrLineLength);
//        vnWords++;
        if (++vnWords == WORDS_PER_SENTENCE)
        {
          vpList.add(vsCurrLine);
          vpLongString.setLength(0);
          vnWords = 0;
        }
      }

      if (vpLongString.length() != 0)
        vpList.add(vpLongString.toString());

      masParagraph = (String[])vpList.toArray(new String[0]);

      return(vnMaxLineSize);
    }
  }
}
