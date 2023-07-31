package com.daifukuamerica.wrxj.swingui.item;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.Synonym;
import com.daifukuamerica.wrxj.dbadapter.data.SynonymData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacLargeListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of item synonyms.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ItemSynonymListFrame extends DacLargeListFrame
{
  StandardInventoryServer mpInvServer = Factory.create(StandardInventoryServer.class);
  protected SynonymData mpFilterData = Factory.create(SynonymData.class);
  
  private static final String SEARCH_BY_ITEM = "Item:";
  private static final String SEARCH_BY_SYNONYM = "Synonym:";
  protected boolean mzSearchByItem = false;
  protected Synonym     mpSynonym = Factory.create(Synonym.class);

  /**
   *  Create item synonyms list frame.
   */
  public ItemSynonymListFrame()
  {
    super("ItemSynonym");
    setSearchData(SEARCH_BY_SYNONYM, DBInfo.getFieldLength(SynonymData.SYNONYM_NAME));
    setDetailSearchVisible(true);
    detailedSearchButton.setText("Change Search");
    detailedSearchButton.setToolTipText("Change Search");
    detailedSearchButton.setMnemonic('C');
    setDisplaySearchCount(true, "item synonym");
  }

  /**
   * Search when the frame is opened
   * 
   * @param ipEvent
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    searchButtonPressed();
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    mpInvServer.cleanUp();
    mpInvServer = null;
  }

  /**
   *  Method to set the search criteria filter.
   *
   *  @param isItem String containing item to use in search.
   */
  protected void setItemFilter(String isItem)
  {
    mpFilterData.clear();
    if (isItem.length() > 0)
    {
      mpFilterData.setKey(SynonymData.ITEM_NAME, isItem + "%", KeyObject.LIKE);
    }
    mpFilterData.addOrderByColumn(SynonymData.ITEM_NAME);
    mpFilterData.addOrderByColumn(SynonymData.SYNONYM_NAME);
  }

  /**
   *  Method to set the search criteria filter.
   *
   *  @param isSynonym String containing synonym to use in search.
   */
  protected void setSynonymFilter(String isSynonym)
  {
    mpFilterData.clear();
    if (isSynonym.length() > 0)
    {
      mpFilterData.setKey(SynonymData.SYNONYM_NAME, isSynonym + "%",
          KeyObject.LIKE);
    }
    mpFilterData.addOrderByColumn(SynonymData.ITEM_NAME);
    mpFilterData.addOrderByColumn(SynonymData.SYNONYM_NAME);
  }

  /**
   *  Method to filter by extended search. Refreshes display.
   */
  protected void refreshTable()
  {
    if (mzSearchByItem)
    {
      setItemFilter(searchField.getText());
    }
    else
    {
      setSynonymFilter(searchField.getText());
    }
    startSearch(mpSynonym, mpFilterData);
  }

  /**
   *  Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    refreshTable();
  }

  /**
   *  Action method to handle swap search button.
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    mzSearchByItem = !mzSearchByItem;
    if (mzSearchByItem)
    {
      setSearchData(SEARCH_BY_ITEM,
          DBInfo.getFieldLength(SynonymData.ITEM_NAME));
    }
    else
    {
      setSearchData(SEARCH_BY_SYNONYM,
          DBInfo.getFieldLength(SynonymData.SYNONYM_NAME));
    }
  }
  
  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateItemSynonym updateItemSynonym = new UpdateItemSynonym("Add Item Synonym");
    addSKDCInternalFrameModal(updateItemSynonym, buttonPanel,
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
              refreshTable();
          }

      }
    });
  }

  /**
   *  Action method to handle Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to Delete", "Selection Error");
      return;
    }

    if (displayYesNoPrompt("Do you really want to Delete\n"
        + "all selected Item Synonyms", "Delete Confirmation"))
    {
      String[] delSynonymList = null;
                                       // Get selected list of item synonyms
      delSynonymList = sktable.getSelectedColumnData("sSynonym");

      int delCount = 0;
      int[] deleteIndices = sktable.getSelectedRows();
      for(int row = 0; row < totalSelected; row++)
      {
        try
        {
          mpInvServer.deleteSynonym(delSynonymList[row]);
          delCount++;
        }
        catch(DBException exc)
        {
          displayError(exc.getMessage(), "Delete Error");
                                       // De-Select the troubling row!
          sktable.deselectRow(deleteIndices[row]);
        }
      }
      if (delCount != totalSelected)
      {
        displayInfo("Deleted " +  delCount + " of " + totalSelected +
                    " selected rows",
                    "Delete Result");
      }
      else
      {
        displayInfoAutoTimeOut("Deleted " + delCount + " of " + totalSelected
            + " selected rows", "Delete Result");
      }
      sktable.deleteSelectedRows();    // Update the display.
    }
  }

  /**
   *  Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {
    if (sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row can be Modified at a time", "Selection Error");
      return;
    }
    Object cObjItem = sktable.getCurrentRowDataField(SynonymData.ITEM_NAME);
    Object cObjSynonym = sktable.getCurrentRowDataField(SynonymData.SYNONYM_NAME);
    if (cObjItem != null)
    {
      UpdateItemSynonym updateItemSynonym = new UpdateItemSynonym("Modify Item Synonym");
      updateItemSynonym.setModify(cObjItem.toString(),cObjSynonym.toString());
      addSKDCInternalFrameModal(updateItemSynonym, buttonPanel,
        new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshTable();
            }
      }
    });
    }
    else
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
  }

  /**
   * Get the class name that will be used in the RoleOptions table.  This 
   * method facilitates the getting of permissions when setCategoryAndOption()
   * is not called and the implemented class is different from the baseline
   * class.
   * 
   * @return <code>Class</code>
   */
  @Override
  protected Class getRoleOptionsClass()
  {
    return ItemSynonymListFrame.class;
  }
}