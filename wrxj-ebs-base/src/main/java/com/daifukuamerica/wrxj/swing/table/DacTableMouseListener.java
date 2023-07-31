package com.daifukuamerica.wrxj.swing.table;

import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *  A class to handle table based mouse events.  In order for the code
 *  to be portable between Unix and Windows for popup menus, three mouse event
 *  methods had to be overridden: <code>mousePressed</code>,
 *  <code>mouseReleased</code> and <code>mouseClicked</code>.
 *
 * @author       A.D.
 * @version      1.0   10/21/02
 * @version      2.0   Made into an external class that can be invoked like
 *                     other Swing event objects.
 * @version			 3.0	 6/29/05
 * 										 Methods for adding copy and paste functionality to 
 * 										 popup menus added.
 */
public abstract class DacTableMouseListener extends MouseAdapter implements SKDCGUIConstants
{
  protected DacTable originatingTable;
  protected SKDCPopupMenu skPopupMenu;
  protected int clickedColumn;

  /**
   *  Required constructor.
   *  @param source the table originating the event.
   */
  public DacTableMouseListener(DacTable sourceTable)
  {
    originatingTable = sourceTable;
    skPopupMenu = createPopup();
  }

  /**
   *  {@inheritDoc}
   */
  @Override
  public void mouseClicked(MouseEvent mEvent)
  {
    switch(mEvent.getClickCount())
    {
      case 1:
        if (skPopupMenu == null) skPopupMenu = createPopup();
        if (skPopupMenu != null) showPopup(mEvent);
        break;

      case 2:
        if (originatingTable.columnAtPoint(mEvent.getPoint()) != -1)
        {
          displayDetail();
        }
    }
  }

  /**
   *  {@inheritDoc}
   */
  @Override
  public void mousePressed(MouseEvent mEvent)
  {
    showPopup(mEvent);
  }

  /**
   *  {@inheritDoc}
   */
  @Override
  public void mouseReleased(MouseEvent mEvent)
  {
    showPopup(mEvent);
  }

  /**
   * Defines contents of a popup menu.
   * @return @{link com.daifukuamerica.wrxj.tool.swing#SKDCPopupMenu SKDCPopupMenu} to be
   *         used as a reference in this object.
   */
  public abstract SKDCPopupMenu definePopup();
  
  /**
   * Displays any user defined detail screen.
   */
  public abstract void displayDetail();
  /*==========================================================================
                         PRIVATE METHODS SECTION
  ==========================================================================*/
  private void showPopup(MouseEvent mEvent)
  {
    clickedColumn = originatingTable.columnAtPoint(mEvent.getPoint());
    if (clickedColumn != -1)
    {
      if (mEvent.isPopupTrigger() && skPopupMenu != null)
      {
        int clickedRow = originatingTable.rowAtPoint(mEvent.getPoint());
        if(!originatingTable.isRowSelected(clickedRow))
          originatingTable.setRowSelectionInterval(clickedRow, clickedRow);
        skPopupMenu.show(originatingTable, mEvent.getX(), mEvent.getY());
      }
    }
  }

  /**
   * Does the pop-up have extra menu items (like Add, Modify, etc)
   * @return
   */
  protected boolean hasMoreMenuItems()
  {
    return true;
  }
  
  /**
   * Wrapper method for <code>definePopup()</code> that adds copy functionality to
   * all popup menus.
   * @return @{link com.daifukuamerica.wrxj.tool.swing#SKDCPopupMenu SKDCPopupMenu}
   * 
   * @author karmstrong
   * @version 3.0 6/29/05
   */
  private SKDCPopupMenu createPopup()
  {
    DacSaveTableListener vpSTL = new DacSaveTableListener(originatingTable);
    
    SKDCPopupMenu menu = definePopup();
    if (hasMoreMenuItems())
    {
      menu.addSeparator();
    }
    menu.add("Copy Cell Contents", true, COPY_BTN, new CellCopyListener());
    menu.add("Print All Rows", PRINTTABLE_BTN, new PrintTableListener());
    menu.add("Save All Rows", SAVETABLE_BTN, vpSTL);
    menu.add("Save Selected Rows", SAVEROWS_BTN, vpSTL);
    return menu;
  }

  /**
   * Auxiliary class for copying text from a table field.
   * @author karmstrong
   * @version 1.0 6/29/05
   */
  private class CellCopyListener implements ActionListener{

    public void actionPerformed(ActionEvent e) {
      copyButtonPressed();
    }
    /**
     * Copy cell contents to Clipboard
     *
     */
    void copyButtonPressed() {
      Object obj = originatingTable.getValueAt(originatingTable.getSelectedRow(),
          clickedColumn);
      StringSelection content = new StringSelection(obj.toString());
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content,content);
    }
  }

  /**
   * <B>Description:</B> Table Printer
   *
   * @author       mandrus
   * @version      1.0
   * 
   * <BR>Copyright (c) 2005 by Daifuku America Corporation
   */
  private class PrintTableListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      printTable();
    }

    void printTable()
    {
      try
      {
        originatingTable.print();
      }
      catch (Exception e)
      {
        System.out.println(e.getMessage() + e.getStackTrace());
      }
    }
  }
}
