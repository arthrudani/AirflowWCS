package com.daifukuamerica.wrxj.swing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class SKDCFileChooserFrame extends SKDCInternalFrame
{
  private static final long serialVersionUID = 0L;

  //  private Color newColor = null;
  private JFileChooser chooser = null;
  private boolean multiSelectionEnabled = false;

  public SKDCFileChooserFrame(String isTitle)
  {
    super(isTitle);
  }

  public SKDCFileChooserFrame()
  {
    this("");
  }

  public List getFileChoices(String filePath)
  {
    chooser = new JFileChooser(filePath);
    chooser.setMultiSelectionEnabled(multiSelectionEnabled);
    String fileName = "";
    List fileNameList = null;
    SKDCFileFilter filter = new SKDCFileFilter();
    filter.setDescription("Warehouse Rx Log Files");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File file = chooser.getCurrentDirectory();
      filePath = file.getPath();
      File[] files = chooser.getSelectedFiles();
      fileNameList = new ArrayList();
      for (int i = 0; i < files.length; i++)
      {
        fileName = files[i].getName();
        fileName = filePath +  File.separator + fileName;
        fileNameList.add(fileName);
      }
    }
    return fileNameList;
  }

  public void setMultiSelectionEnabled(boolean b)
  {
    multiSelectionEnabled = b;
  }

  /**
   * Indicates that no system gateway is needed.
   *
   * <p><b>Details:</b> <code>getSystemGatewayNeeded</code> returns
   * <code>false</code> to indicate that no system gateway is needed by this
   * frame.  This method is called by the superclass during initialization.</p>
   *
   * @return false
   */
  @Override
  protected boolean getSystemGatewayNeeded() {return false;}

}

class SKDCFileFilter extends FileFilter
{
  private String description = "";
  public SKDCFileFilter()
  {
  }

  public void setDescription(String s)
  {
    description = s;
  }

  @Override
  public String getDescription()
  {
    return description;
  }

  @Override
  public boolean accept(File pathname)
  {
    return true;
  }

}

