package com.daifukuamerica.wrxj.swingui.move;

import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.InternalFrameEvent;

/**
 * <B>Description:</B> Screen to display moves by priority and device
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class MovesByDeviceList extends SKDCListFrame
{
  // Custom search input
  SKDCTranComboBox mpMoveTypeCombo;
  
  /**
   * Constructor
   */
  public MovesByDeviceList()
  {
    super("MovesByDevice");
    
    // Custom search
    try
    {
      mpMoveTypeCombo = new SKDCTranComboBox(MoveData.MOVETYPE_NAME, getMoveTypeList(), true);
      setSearchData("Move Type", mpMoveTypeCombo);
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException("Error populating move type combo", nsfe);
    }
    
    // Set up the list
    setDisplaySearchCount(false, "entry", "entries");
    sktable.allowOneRowSelection(true);

    // Hide unnecessary buttons
    addButton.setAuthorization(false);
    modifyButton.setAuthorization(false);
    deleteButton.setAuthorization(false);
    
    getButtonPanel().setVisible(false);
  }
  
  /**
   * Get the MoveTypes to display.  Default is for an item-mover system.
   * @return
   */
  protected int[] getMoveTypeList()
  {
    // Load Mover
    // return new int[] { DBConstants.LOADMOVE };
    
    // Item Mover
    return new int[] { DBConstants.CYCLECOUNTMOVE, DBConstants.EMPTYMOVE,
        DBConstants.ITEMMOVE, DBConstants.LOADMOVE };
  }

  /**
   * Auto-refresh when the frame is opened
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);
    searchButtonPressed();
  }
  
  /**
   *  Method to filter by extended search. Refreshes display.
   */
  @Override
  public void searchButtonPressed()
  {
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        try
        {
          int vnMoveType = mpMoveTypeCombo.getIntegerValue();
          
          Move vpMove = Factory.create(Move.class);
          refreshTable(vpMove.getMoveReportList(vnMoveType));
          
          // Get the Total from the last (Total) line
          Integer vpMoves = (Integer)sktable.getRowData(
              sktable.getRowCount()-1).get("ITOTAL");
          displayInfoAutoTimeOut("" + vpMoves + (vpMoves == 1 ? "s" : "")
              + " moves found");
        }
        catch (Exception e)
        {
          logAndDisplayException(e);
        }
      }
    });
  }
  
  /**
   *  Defines popup menu items for <code>DacTable</code>, and adds listeners
   *  to them.
   */
  @Override
  protected void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
      @Override
      public SKDCPopupMenu definePopup()
      {
        popupMenu.add("Display Moves", SHOWDETAIL_BTN, new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            displayMoveDetail();
          }
        });
        return(popupMenu);
      }
      /**
       *  Display the Order Line screen.
       */
      @Override
      public void displayDetail()
      {
        displayMoveDetail();
      }
    });
  }
   
  /**
   * Display the moves
   */
  private void displayMoveDetail()
  {
    List<KeyObject> searchData = new LinkedList<KeyObject>();

    String vsWarehouse = sktable.getSelectedRowData().get(MoveData.DEVICEID_NAME).toString();
    if (!vsWarehouse.equals("Total"))
    {
      searchData.add(new KeyObject(MoveData.DEVICEID_NAME, vsWarehouse));
    }
    try
    {
      searchData.add(new KeyObject(MoveData.MOVETYPE_NAME, mpMoveTypeCombo.getIntegerValue()));
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
    }

    MoveListFrame vpMoveFrame = Factory.create(MoveListFrame.class);
    vpMoveFrame.setAllowDuplicateScreens(true);
    vpMoveFrame.setFilter(KeyObject.toKeyArray(searchData));
    addSKDCInternalFrameModal(vpMoveFrame);
    return;
  }

  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#getRoleOptionsClass()
   */
  @Override
  protected Class getRoleOptionsClass()
  {
    return MovesByDeviceList.class;
  }
}
