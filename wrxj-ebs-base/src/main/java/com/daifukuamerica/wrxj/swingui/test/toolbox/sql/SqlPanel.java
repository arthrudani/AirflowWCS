package com.daifukuamerica.wrxj.swingui.test.toolbox.sql;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.TabbedFramePanel;
import com.daifukuamerica.wrxj.swing.UndoEnabledTextArea;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swingui.test.toolbox.TestToolbox;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/*
INSERT INTO dbo.SYSCONFIG (sGroup,sParameterName,sParameterValue,sDescription)
VALUES ('TestToolbox:Panel','Developer','com.daifukuamerica.wrxj.swingui.test.toolbox.sql.SqlPanel','TestToolbox Panel')
*/
/**
 * Rudimentary free-form SQL query tool.  Leaves a lot to be desired, but is 
 * better than nothing.
 * 
 * @author mandrus
 */
@SuppressWarnings("rawtypes")
public class SqlPanel extends TabbedFramePanel implements ActionListener
{
  private static final long serialVersionUID = -6766860697137854490L;

  private DacTable mpTable;
  private List<Map> mpTableData;
  private UndoEnabledTextArea mpSqlArea; 
  private SKDCPopupMenu mpPopupMenu = null;

  public SqlPanel(TestToolbox ipParent)
  {
    super(ipParent, null);
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(600, 400));

    JPanel vpSearchPanel = new JPanel(new BorderLayout());
    vpSearchPanel.setBorder(new EtchedBorder());
    
    mpSqlArea = new UndoEnabledTextArea();
    mpSqlArea.setText("");
    vpSearchPanel.add(mpSqlArea, BorderLayout.CENTER);
    
    SKDCButton vpButton = new SKDCButton("Search");
    vpButton.addActionListener(this);
    vpSearchPanel.add(vpButton, BorderLayout.EAST);
    
    add(vpSearchPanel, BorderLayout.NORTH);
    
    mpTable = new DacTable(new DacModel(new ArrayList<>(), ""));
    mpTable.setDragEnabled(true);
    
    add(mpTable.getScrollPane(), BorderLayout.CENTER);
    
    mpPopupMenu = new SKDCPopupMenu();
    mpTable.addMouseListener(new DacTableMouseListener(mpTable)
    {
      @Override
      public SKDCPopupMenu definePopup()
      {
        return(mpPopupMenu);
      }
      @Override
      public void displayDetail()
      {
      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    List<String> vpColumnNames = new ArrayList<>();

    // Run the query
    DBObject vpDBO = new DBObjectTL().getDBObject();
    try
    {
      DBResultSet rs = vpDBO.execute(mpSqlArea.getText());
      mpTableData = rs.getRows();
      vpColumnNames = rs.getColumns();
    }
    catch (DBException dbe)
    {
      mpParent.logAndDisplayException("Your SQL query failed!", dbe);
      mpTableData = new ArrayList<>();
    }
    
    if (vpColumnNames.size() == 0)
    {
      vpColumnNames.add("No results");
    }
    
    // Replace the table
    // This part could probably be done better from scratch, but this works
    setVisible(false);
    remove(mpTable.getScrollPane());
    mpTable.setModel(new DacModel(mpTableData, vpColumnNames));
    add(mpTable.getScrollPane(), BorderLayout.CENTER);
    setVisible(true);
    mpParent.displayInfoAutoTimeOut(mpTableData.size() + " row" + (mpTableData.size() == 1 ? "" : "s") + " found.");
  }
}
