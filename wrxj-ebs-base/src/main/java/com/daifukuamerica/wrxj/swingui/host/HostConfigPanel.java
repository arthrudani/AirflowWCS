package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Description:<BR>
 *    Panel class for configuring a Host interface.
 *
 * @author       A.D.
 * @version      1.0      18-Jun-2007
 */
@SuppressWarnings("serial")
public class HostConfigPanel extends JPanel
{
  protected HostConfigData mpConfigData;
  protected DacTable     mpTable;
  protected DBHelper      mpDBHelper;

  public HostConfigPanel()
  {
    super(new BorderLayout());
    mpConfigData = Factory.create(HostConfigData.class);
    
    mpTable = new DacTable(new DacModel(new ArrayList<Map>(),
                                          getClass().getSimpleName()));
    add(mpTable.getScrollPane(), BorderLayout.CENTER);
  }
  
  public HostConfigPanel(SKDCButton ipCommitButton)
  {
    super(new BorderLayout());
    mpConfigData = Factory.create(HostConfigData.class);
    
    mpTable = new DacTable(new DacModel(new ArrayList<Map>(),
                                          getClass().getSimpleName()));
    add(mpTable.getScrollPane(), BorderLayout.CENTER);
    JPanel vpButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    vpButtonPanel.setBorder(BorderFactory.createEtchedBorder());
    vpButtonPanel.add(ipCommitButton);
    add(vpButtonPanel, BorderLayout.SOUTH);
  }
 /**
   * Method to update the display Host configuration information.
   * 
   * @param ipDataList a list of data to display.
   */
  public void update(List<Map> ipDataList)
  {
    mpTable.refreshData(ipDataList);
  }
  
  /**
   * Method returns all of the table information
   * @return
   */
  public List<Map> getAllRows()
  {
    List<Map> vpDataList = null;
    vpDataList = mpTable.getTableData();
    return vpDataList;
  }
  
  /**
   * Method commits an edit in a table
   */
  public void commitEdit()
  {
    int vnRow = mpTable.getSelectedRow();
    if(vnRow >= 0)
    {
      mpTable.commitEdit(vnRow, HostConfigData.PARAMETERVALUE_NAME);
    }
  }
  
  /**
   * Method sets a column so that it can be edited
   * @param isDBColumnName the name of the column for editing.
   */
  public void setColumnToEdit(String isDBColumnName)
  {
    mpTable.setEditableColumn(isDBColumnName);
  }
  

  /**
   * Method disables editing of rows in  specified column
   * @param isDBColumnName the database column name
   * @param isPattern the value to exclude from edit
   */
  public void setNonEditableRowsByColumn(String isDBColumnName, String... isPattern)
  {
    mpTable.setNonEditableRowsByColumnPattern(isDBColumnName, isPattern);
  }

 /**
  *  Method gets all selected rows of data.
  * @return
  */
  public List<? extends AbstractSKDCData> getSelectedRowData()
  {
    List<HostConfigData> vpDataList = null;
    int vnSelectedRowCount = mpTable.getSelectedRowCount();
    
    if (vnSelectedRowCount > 0)
    {
      List<Map> vpSelectList = mpTable.getSelectedRowDataArray();
      vpDataList = DBHelper.convertData(vpSelectList, HostConfigData.class);
    }
    
    return(vpDataList);
  }
  
}
