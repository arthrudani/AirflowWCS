package com.daifukuamerica.wrxj.log.view;

import com.daifukuamerica.wrxj.log.LogDataModel;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import javax.swing.JScrollPane;

/**
 * @author Stephen Kendorski
 */
public interface LogScrollPane extends LogDataModel
{
  /**
   * Specify the Model Data (in the Model/View/Controller design pattern) to
   * view, whether its width should be re-sized, and this component's parent
   * Frame.
   * 
   * @param ipLogData the log data model
   * @param izResize if true, re-size the underlying JTable width
   * @param ipParent parent Component
   * @return the JScrollPane this object decorates
   */
  public JScrollPane initialize(Object ipLogData, boolean izResize,
      SKDCInternalFrame ipParent);
  
  /**
   * Return the width of the underlying JTable Table Model.
   *
   * @return the width
   */
  public int getTableWidth();
  
  /**
   * Determines whether component is showing on screen. This means
   * that the component is visible (it's frame is not iconified).  The
   * component may be hidden behind another frame, but is still "showing".
   *
   * @return true if the component is showing; false otherwise.
   */
  public boolean isShowing();
  
  /**
   * During a text search through the entries, set "foundEntry" index to
   * the parameter.  If -1, do NOT set the found entry, but display a
   * "not Found" dialog.  If param is >= 0, scroll the view to the entry.
   * If param is -2 do nothing.
   * 
   * @param inFoundEntry index of found entry, -1 if not found, -2 do nothing
   */
  public void setFoundEntry(int inFoundEntry);
}
