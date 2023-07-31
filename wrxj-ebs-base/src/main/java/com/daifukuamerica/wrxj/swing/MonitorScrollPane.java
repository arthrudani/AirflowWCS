/*
 * Created on Jan 9, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.daifukuamerica.wrxj.swing;

import java.awt.Color;
import java.util.Observer;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * @author Stephen Kendorski
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface MonitorScrollPane extends MonitorDataModel, Observer

{
  /**
   * Specify the logger and this component's parent Frame.
   *
   * @param parent parent Component
   * @return the JScrollPane this object decorates
   */
  public JScrollPane initialize(SKDCInternalFrame parent);
  /**
   * Specify the Controller Group in the view.
   * 
   * @param isGroupName the name of the group to view
   */
  public void setGroupName(String isGroupName);
  /**
   * Assign a foreground color to all rows associated with the specified key.
   *
   * @param key the selection
   * @param color the Color for the foreground
   */
  public void addSelectedKey(int key, Color color);
  /**
   * Notifies all listeners that all cell values in the table's rows may have
   * changed. The number of rows may also have changed and the JTable should
   * redraw the table from scratch. The structure of the table (as in the
   * order of the columns) is assumed to be the same.
   */
  public void fireDataChanged();
  /**
   * Fetch the list index of the last item to updated.
   * 
   * @return the index, -1 if not in list.
   */
  public int getUpdatedIndex();
  /**
   * Return the width of the underlying JTable Table Model.
   *
   * @return the width
   */
  public int getTableWidth();
  /**
   * Return the nderlying JTable Table Model.
   *
   * @return the table Model
   */
  public JTable getTable();
}
