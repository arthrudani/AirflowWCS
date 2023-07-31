/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2008 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swing.table;

import com.daifukuamerica.wrxj.swing.DacTranslator;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class DacSaveTableListener implements ActionListener
{
  DacTable mpTable;
  
  public DacSaveTableListener(DacTable ipTable)
  {
    mpTable = ipTable;
  }
  
  @Override
  public void actionPerformed(ActionEvent e)
  {
    saveTable(e.getActionCommand().equals(SKDCGUIConstants.SAVEROWS_BTN));
  }

  /**
   * Get the file name and save the table data
   * @param izSaveOnlySelected
   */
  private void saveTable(boolean izSaveOnlySelected)
  {
    try
    {
      // Get the file name
      JFileChooser vpFC = new JFileChooser();
      vpFC.addChoosableFileFilter(new SaveFileFilter());
      vpFC.setSelectedFile(new File(getDefaultFileName()));
      int vnSD = vpFC.showSaveDialog(vpFC);
      if (vnSD != JFileChooser.APPROVE_OPTION)
      {
        tryToLogResults("Save cancelled by user.");
        return;
      }
      File vpFile = vpFC.getSelectedFile();
      if (vpFile == null)
      {
        tryToLogResults("Save cancelled--no file name selected.");
        return;
      }
      
      // If it exists, confirm replacement
      if (vpFile.exists())
      {
        int resp = JOptionPane.showConfirmDialog(null,
            DacTranslator.getTranslation("Overwrite %s1?", vpFile.getName()), 
            DacTranslator.getTranslation("Save"), 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resp != JOptionPane.YES_OPTION)
        {
          tryToLogResults("Save cancelled by user.");
          return;
        }
      }
      
      // Write the file
      int vnRowsWritten = writeFile(vpFile, izSaveOnlySelected);
      tryToLogResults("Saved " + vnRowsWritten + " rows to "
          + vpFile.getName());
    }
    catch (Exception e)
    {
      tryToLogResults("Error creating file.");
      e.printStackTrace();
    }
  }

  /**
   * Actually write the file
   * 
   * @param ipFile
   * @param izSaveOnlySelected
   * @return number of rows written
   */
  private int writeFile(File ipFile, boolean izSaveOnlySelected)
      throws Exception
  {
    FileOutputStream vpOutput = new FileOutputStream(ipFile);
    SimpleDateFormat vpSDF = new SimpleDateFormat();
    int vnRowsWritten = 0;
    int vanSelectedRows[];
    
    // Get the dimensions of the table
    int vnCols = mpTable.getColumnCount();
    int vnRows = mpTable.getRowCount();
    
    // HEADERS
    String vsRowData = "";
    for (int vnCurrentCol = 0; vnCurrentCol < vnCols; vnCurrentCol++)
    {
      if (vsRowData.length() > 0)
      {
        vsRowData += ",";
      }
      Object o = mpTable.getColumnName(vnCurrentCol);
      if (o instanceof Date)
      {
        vsRowData += vpSDF.format(o); 
      }
      else
      {
        vsRowData += o.toString();
      }
    }
    vsRowData += SKDCConstants.EOL_CHAR;
    vpOutput.write(vsRowData.getBytes());
    
    // DATA
    if (izSaveOnlySelected)
    {
      vanSelectedRows = mpTable.getSelectedRows();
      Arrays.sort(vanSelectedRows);
      for (int i = 0; i < vanSelectedRows.length; i++)
      {
        writeRow(vanSelectedRows[i], vnCols, vpSDF, vpOutput);
      }
      vnRowsWritten = vanSelectedRows.length;
    }
    else
    {
      for (int vnCurrentRow = 0; vnCurrentRow < vnRows; vnCurrentRow++)
      {
        writeRow(vnCurrentRow, vnCols, vpSDF, vpOutput);
      }
      vnRowsWritten = vnRows;
    }
    vpOutput.flush();
    vpOutput.close();
    
    return vnRowsWritten;
  }
  
  /**
   * Write out a given row of data
   * @param inRow
   * @param inCols
   * @param ipOut
   * @throws Exception
   */
  private void writeRow(int inRow, int inCols, SimpleDateFormat ipSDF,
      OutputStream ipOut) throws Exception
  {
    String vsRowData = "";
    for (int vnCurrentCol = 0; vnCurrentCol < inCols; vnCurrentCol++)
    {
      if (vsRowData.length() > 0)
      {
        vsRowData += ",";
      }
      Object o = mpTable.getValueAt(inRow, vnCurrentCol);
      if (o instanceof Date)
      {
        vsRowData += "\"" + ipSDF.format(o) + "\""; 
      }
      else
      {
        String vsValue = o.toString();
        vsRowData += "\"" + vsValue + "\"";
      }
    }
    vsRowData += SKDCConstants.EOL_CHAR;
    ipOut.write(vsRowData.getBytes());
  }
  
  /**
   * Try to give the user some feedback.
   * 
   * @param isMessage
   */
  private String getDefaultFileName()
  {
    String vsFileName = "SavedData";
    Component j = mpTable.getParent();
    while (j != null && !(j instanceof SKDCInternalFrame))
    {
      j = j.getParent();
    }
    if (j != null)
    {
      vsFileName = ((SKDCInternalFrame)j).getTitle();
    }
    SimpleDateFormat vpSDF = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    vsFileName += "_" + vpSDF.format(new Date()) + ".csv";
    vsFileName.replace(' ', '_');
    return vsFileName;
  }

  /**
   * Try to give the user some feedback.
   * 
   * @param isMessage
   */
  private void tryToLogResults(String isMessage)
  {
    Component j = mpTable.getParent();
    while (j != null && !(j instanceof SKDCInternalFrame))
    {
      j = j.getParent();
    }
    if (j != null)
    {
      ((SKDCInternalFrame)j).displayInfoAutoTimeOut(isMessage);
    }
    else
    {
      System.out.println(isMessage);
    }
  }
  
  /**
   * <B>Description:</B> Save file filters
   *
   * @author       mandrus<BR>
   * @version      1.0
   * 
   * <BR>Copyright (c) 2008 by Daifuku America Corporation
   */
  public class SaveFileFilter extends FileFilter
  {
    @Override
    public boolean accept(File ipFile)
    {
      if (ipFile.isDirectory())
      {
        return true;
      }
      
      String vsExtension = ipFile.getName();
      int i = vsExtension.lastIndexOf('.');
      if (i > 0 && i < vsExtension.length() - 1)
      {
        vsExtension = vsExtension.substring(i + 1).toLowerCase();
      }
      return vsExtension.equals("csv");
    }

    @Override
    public String getDescription()
    {
      return "Comma-separated-values files (.csv)";
    }
  }
}
