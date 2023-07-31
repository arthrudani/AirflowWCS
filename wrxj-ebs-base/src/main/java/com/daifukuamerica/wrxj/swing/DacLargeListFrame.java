package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

/**
 * <B>Description:</B> Large List frame
 * 
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 * 
 * @author mandrus
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public abstract class DacLargeListFrame extends SKDCListFrame
{
  private static final long serialVersionUID = 6606709193902212776L;
  
  public static final int DEFAULT_ROWS_PER_PAGE = 1000;
  private int mnRowsPerPage = -1;
  
  private List<List<Map>> mpPageLists = new ArrayList<List<Map>>();
  private int mnCurrentPage = 0;
  private int mnTotalCount = 0;

  protected BaseDBInterface mpDBInt = null;
  protected AbstractSKDCData mpSearch = null;

  protected SKDCButton mpBtnPrevData;
  protected SKDCButton mpBtnNextData;

  private ActionListener mpButtonListener = new LargeListButtonListener();

  /**
   * 
   */
  public DacLargeListFrame()
  {
    this("");
  }

  /**
   * @param isNameOfData
   */
  public DacLargeListFrame(String isNameOfData)
  {
    super(isNameOfData);
    setLargeListFrame();
  }

  /*========================================================================*/
  /* Support for very large lists                                           */
  /*========================================================================*/

  /**
   * This is a large list frame. Add the Previous and Next buttons.
   */
  private void setLargeListFrame()
  {
    mpBtnPrevData = new SKDCButton("" + (char)0x25C4);
    mpBtnNextData = new SKDCButton("" + (char)0x25BA);

    mpBtnPrevData.setVisible(false);
    mpBtnNextData.setVisible(false);

    mpBtnPrevData.addEvent(PREV_BTN, mpButtonListener);
    mpBtnNextData.addEvent(NEXT_BTN, mpButtonListener);

    JPanel vpPrevNextPanel = new JPanel(new BorderLayout());
    vpPrevNextPanel.add(mpBtnPrevData, BorderLayout.WEST);
    vpPrevNextPanel.add(getInfoPanel(), BorderLayout.CENTER);
    vpPrevNextPanel.add(mpBtnNextData, BorderLayout.EAST);

    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(vpPrevNextPanel, BorderLayout.CENTER);
    vpSouthPanel.add(buttonPanel, BorderLayout.SOUTH);

    getContentPane().add(vpSouthPanel, BorderLayout.SOUTH);
  }

  /**
   * Get the previous page of data
   */
  protected void prevButtonPressed()
  {
    mnCurrentPage--;
    // Pass a copy because the table messes with the one it has
    List<Map> vpNewData = new ArrayList<Map>();
    vpNewData.addAll(mpPageLists.get(mnCurrentPage));
    refreshTable(vpNewData);
  }

  /**
   * Get the next page of data
   */
  protected void nextButtonPressed()
  {
    try
    {
      mnCurrentPage++;
      if (mnCurrentPage >= mpPageLists.size())
      {
        mpPageLists.add(mpDBInt.fetchNextLargeRecordListEntries());
      }
      // Pass a copy because the table messes with the one it has
      List<Map> vpNewData = new ArrayList<Map>();
      vpNewData.addAll(mpPageLists.get(mnCurrentPage));
      refreshTable(vpNewData);
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error fetching next records.", dbe);
    }
  }

  /**
   * Button Listener class.
   */
  private class LargeListButtonListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String vsButton = e.getActionCommand();

      if (vsButton.equals(PREV_BTN))
        prevButtonPressed();
      else if (vsButton.equals(NEXT_BTN))
        nextButtonPressed();
    }
  }

  /**
   * Start a new search
   * 
   * @param ipDBInt
   * @param ipSearch
   */
  protected void startSearch(BaseDBInterface ipDBInt, AbstractSKDCData ipSearch)
  {
    startSearch(ipDBInt, ipSearch, Application.getInt("RowsPerPage",
        DEFAULT_ROWS_PER_PAGE));
  }

  /**
   * Start a new search
   * 
   * @param ipDBInt
   * @param ipSearch
   * @param inRowsPerPage
   */
  protected void startSearch(BaseDBInterface ipDBInt,
      AbstractSKDCData ipSearch, int inRowsPerPage)
  {
    mpDBInt = ipDBInt;
    mpSearch = ipSearch;
    mnRowsPerPage = inRowsPerPage;

    try
    {
      mnCurrentPage = -1;
      mpPageLists.clear();
      mnTotalCount = mpDBInt.getCount(mpSearch);
      mpDBInt.initializeLargeRecordList(mnRowsPerPage, mpSearch);
      nextButtonPressed();
    }
    catch (DBException exc)
    {
      logAndDisplayException(exc);
    }
  }

  /**
   * Display the count and enable/disable the previous and next buttons
   * 
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#displayTableCount()
   */
  @Override
  protected void displayTableCount()
  {
    int vnStart = (mnCurrentPage * mnRowsPerPage);
    int vnEnd = vnStart + Math.min(sktable.getRowCount(), mnRowsPerPage);

    if (mnCurrentPage == 0 && vnEnd == mnTotalCount)
    {
      super.displayTableCount();
    }
    else
    {
      // Make it so you don't need a translation for every possible number
      String vsDisplay = DacTranslator.getTranslation("Displaying %d to %d of %d"
          + msPluralRowDescription);
      vsDisplay = vsDisplay.replaceFirst("%d", "" + (vnStart+1));
      vsDisplay = vsDisplay.replaceFirst("%d", "" + vnEnd);
      vsDisplay = vsDisplay.replaceFirst("%d", "" + mnTotalCount);
      displayInfoAutoTimeOut(vsDisplay);
    }

    mpBtnPrevData.setVisible(mnCurrentPage != 0);
    mpBtnNextData.setVisible(mnTotalCount > vnEnd);
  }
  
  @Override
  public void cleanUpOnClose()
  {
    if (mpDBInt != null)
      mpDBInt.closeLargeRecordList();
    super.cleanUpOnClose();
  }
}
