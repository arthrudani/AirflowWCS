package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.application.Application;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.InternalFrameEvent;

/**
 * A base screen class for displaying a list of data.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public abstract class SKDCListFrame extends SKDCInternalFrame
{
  protected SKDCUserData      userData              = null;
  protected JPanel            listPanel             = new JPanel();
  protected JPanel            buttonPanel           = getEmptyButtonPanel();
  protected SKDCButton        deleteButton;
  protected SKDCButton        modifyButton;
  protected SKDCButton        addButton;
  protected SKDCButton        closeButton;
  protected SKDCButton        viewButton;
  protected String            dataName              = null;
  protected DacTable          sktable               = null;
  protected SKDCPopupMenu     popupMenu             = new SKDCPopupMenu();
  protected List<Map>         dataList              = new ArrayList<Map>();
  protected JPanel            searchPanel           = null;
  protected SKDCLabel         searchLabel           = new SKDCLabel();
  protected SKDCTextField     searchField           = new SKDCTextField();
  protected SKDCButton        searchButton;
  protected SKDCButton        detailedSearchButton;
  protected SKDCButton        refreshButton;
  protected JPanel            southPanel           = null;
  
  protected SKDCScreenPermissions  ePerms           = null;
  protected Timer             mpRefreshTimer;
  protected int               mnRefreshInterval = 10000;
  protected JCheckBox         mpChkAutoRefresh = 
    new JCheckBox(DacTranslator.getTranslation("Auto-refresh"));

  protected boolean mzDisplayCountOnRefresh = false;
  protected String msSingularRowDescription = "";
  protected String msPluralRowDescription = "";
  private boolean mzHasPopup = false;
  
  private ButtonListener mpButtonListener = new ButtonListener();

  /**
   *  Create a default data list frame.
   */
  public SKDCListFrame()
  {
    this("");
  }

 /**
  *  Create data list frame.
  *
  * @param nameOfData Contains name of data being displayed in list, should
  * match something in the ASRS MetaData table.
  */
  public SKDCListFrame(String nameOfData)
  {
    super();
    setMaximizable(true);
    userData = new SKDCUserData();
    try
    {
      dataName = nameOfData;
      buildScreen();
    }
    catch (DBException e)
    {
      e.printStackTrace();
      this.displayError("Exception", "Error opening Database Connection");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    setDisplaySearchCount(true, nameOfData);
  }

  /**
   * We don't agree with pack()'s size determination 
   */
  @Override
  public void pack()
  {
    super.pack();
    resetSize();
  }
  
  /**
   * Sets screen permissions.
   *
   * <p><b>Details:</b> <code>internalFrameOpened</code> augments the
   * supermethod by setting the screen permissions.</p>
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);
    
    // set up permission for buttons
    setupButtonPermission();
    resetSize();
    sktable.resizeColumns();           // Make sure SKDCTable columns take up
                                       // any remaining space.

    // Make sure every list frame has a pop-up for the table
    if (!mzHasPopup)
    {
      setTableMouseListener();
      mzHasPopup = true;
    }
  }

  /**
   * Set up the permission of buttons
   */
  protected void setupButtonPermission()
  {
    if (ePerms == null)
    {
      ePerms = getPermissions();
    }
    addButton.setAuthorization(ePerms.iAddAllowed);
    modifyButton.setAuthorization(ePerms.iModifyAllowed);
    deleteButton.setAuthorization(ePerms.iDeleteAllowed);
  }

  /**
   * This is a default way to get permissions, used when setCategoryAndOption()
   * is not called.  It should usually be based upon class.
   * 
   * @return <code>SKDCScreenPermissions</code>
   */
  protected SKDCScreenPermissions getPermissions()
  {
    return userData.getOptionPermissionsByClass(getRoleOptionsClass());
  }
  
  /**
   * Get the class name that will be used in the RoleOptions table.  This 
   * method facilitates the getting of permissions when setCategoryAndOption()
   * is not called and the implemented class is different from the baseline
   * class.
   * 
   * @return <code>Class</code>
   */
  protected abstract Class getRoleOptionsClass();
  
  /**
   * Reset the size (after showing/hiding buttons, for instance) 
   */
  protected void resetSize()
  {
    setSize(getStartingWidth(), getStartingHeight());
  }
  
  /**
   * Guesses the title based upon the data name
   */
  protected void setTitleFromDataName()
  {
    String vsTitle = null;
    
    for (char c : dataName.toCharArray())
    {
      if (vsTitle == null)
      {
        vsTitle = "" + c;
      }
      else
      {
        if (Character.isUpperCase(c))
        {
          vsTitle = vsTitle + " ";
        }
        vsTitle = vsTitle + c;
      }
    }
    
    vsTitle = vsTitle + "s";
    
    setTitle(vsTitle);
  }
  
  /**
   *  Method to initialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  protected void buildScreen() throws Exception
  {
    listPanel.setLayout(new BorderLayout());
    
    // create components
    createComponents();
    setTitleFromDataName();
    
    // build the search panel
    searchPanel = createSearchPanel();

    setSearchVisible(false);
  
    // build the button panel
    createButtonPanel();

    southPanel = new JPanel(new BorderLayout());
    southPanel.add(getInfoPanel(), BorderLayout.CENTER);
    southPanel.add(buttonPanel, BorderLayout.SOUTH);
    
    sktable = new DacTable(new DacModel(dataList, dataName));

    getContentPane().add(searchPanel, BorderLayout.NORTH);
    getContentPane().add(sktable.getScrollPane(), BorderLayout.CENTER);
    getContentPane().add(southPanel, BorderLayout.SOUTH);
    
    setAutoRefreshInterval(10);
    mpChkAutoRefresh.setVisible(false);
    addActionListeners();
  }
  
  /**
   * Build search panel
   * @return <code>JPanel</code> the Search panel
   */
  protected JPanel createSearchPanel()
  {
    JPanel vpPanel = getEmptyListSearchPanel();
    
    vpPanel.add(searchLabel, null);
    vpPanel.add(searchField, null);
    vpPanel.add(searchButton, null);
    vpPanel.add(detailedSearchButton, null);
    vpPanel.add(refreshButton, null);
    vpPanel.add(mpChkAutoRefresh, null);
    
    return vpPanel;
  }
  
  /**
   * Create components that are used in the screen
   */
  protected void createComponents()
  {
    // Components for Button panel
    addButton = new SKDCButton("Add", "Add", 'A');
    modifyButton = new SKDCButton("Modify", "Modify", 'M');
    deleteButton = new SKDCButton("Delete", "Delete", 'D');
    viewButton = new SKDCButton("Details", "Details", 'l');
    closeButton = new SKDCButton("Close", "Close", 'C');
    
    setViewButtonVisible(false);
    // test the wrxj.properties to see if the close button should be enabled
    // If undefined set to false.
    setCloseButtonVisible(Application.getBoolean("EnableCloseButton", false));

    // Components for Search panel
    searchLabel.setText("Search");
    searchButton = new SKDCButton("Search", "Search", 'S');
    detailedSearchButton = new SKDCButton("Detailed Search", "Detailed Search", 't');
    refreshButton = new SKDCButton("Refresh", "Refresh", 'R');

    setDetailSearchVisible(false);
  }

  /**
   * Build button panel - add button(s) to button panel.
   */
  protected void createButtonPanel()
  {
    buttonPanel.add(addButton, null);
    buttonPanel.add(modifyButton, null);
    buttonPanel.add(deleteButton, null);
    buttonPanel.add(viewButton, null);
    buttonPanel.add(closeButton, null);
  }
  
  /**
   * Sets the refresh interval
   * 
   * @param inInterval (in seconds)
   */
  protected void setAutoRefreshInterval(int inInterval)
  {
    ActionListener vpRefreshTask = new ActionListener() {
      public void actionPerformed(ActionEvent evt)
      {
        executeAutoRefreshTable();
      }
    };
    mnRefreshInterval = inInterval * 1000;
    if (mpRefreshTimer != null)
    {
      mpRefreshTimer.stop();
    }
    mpRefreshTimer = null;
    mpRefreshTimer = new Timer(mnRefreshInterval, vpRefreshTask);
    startOrStopRefreshTimer();
  }
  
  /**
   * Set whether or not the auto-refresh check box is visible
   * @param izVisible
   */
  protected void setAutoRefreshVisible(boolean izVisible)
  {
    mpChkAutoRefresh.setVisible(izVisible);
  }
  
  /**
   * Set whether or not the auto-refresh check box is checked
   * @param izSelected
   */
  protected void setAutoRefreshSelected(boolean izSelected)
  {
    mpChkAutoRefresh.setSelected(izSelected);
    startOrStopRefreshTimer();
  }

  /**
   * Start or stop the auto-refresh timer based upon the state of the
   * check box
   */
  private void startOrStopRefreshTimer()
  {
    if (mpChkAutoRefresh.isSelected()) 
    {
      if (!mpRefreshTimer.isRunning())
        mpRefreshTimer.start();
    }
    else
    {
      if (mpRefreshTimer.isRunning())
        mpRefreshTimer.stop();
    }
  }
  
  /**
   *  Stop the auto-refresh (when performing add/mod/delete/etc)
   */
  protected void pauseAutoRefresh()
  {
    if (mpRefreshTimer.isRunning())
      mpRefreshTimer.stop();
  }
  
  /**
   *  Start the auto-refresh (after performing add/mod/delete/etc)
   */
  protected void resumeAutoRefresh()
  {
    if (!mpRefreshTimer.isRunning() && mpChkAutoRefresh.isSelected())
      mpRefreshTimer.start();
  }
  
  /**
   * Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    if (mpRefreshTimer != null && mpRefreshTimer.isRunning())
    {
      mpRefreshTimer.stop();
      mpRefreshTimer = null;
    }
    super.cleanUpOnClose();
  }
  
  /*========================================================================*/
  /*  Method stubs for buttons, so that child screens don't always have to  */
  /*  create their own ButtonListener.                                      */
  /*========================================================================*/
  protected void addButtonPressed() {}
  protected void modifyButtonPressed() {}
  protected void deleteButtonPressed() {}
  protected void searchButtonPressed() {}
  protected void detailedSearchButtonPressed() {}
  protected void refreshButtonPressed() { searchButtonPressed(); }
  protected void viewButtonPressed()
  {
    if (isSelectionValid("View", false))
    {
      String vsDisplayThis = 
        msSingularRowDescription.substring(0,1).toUpperCase()
        + msSingularRowDescription.substring(1);
      
      Map m = sktable.getSelectedRowDataArray().get(0);
      DacMapViewer vpDMV = new DacMapViewer("Display " + vsDisplayThis, 
          vsDisplayThis + " Information", m, dataName);
      addSKDCInternalFrameModal(vpDMV, new JPanel[] {searchPanel, buttonPanel });
    }
  }
 
  /**
   *  Button Listener class.
   */
  private class ButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String vsButton = e.getActionCommand();

      if (vsButton.equals(SEARCH_BTN))          searchButtonPressed();
      else if (vsButton.equals(DETSEARCH_BTN))  detailedSearchButtonPressed();
      else if (vsButton.equals(REFRESH_BTN))    refreshButtonPressed();
      else if (vsButton.equals(ADD_BTN))        addButtonPressed();
      else if (vsButton.equals(MODIFY_BTN))     modifyButtonPressed();
      else if (vsButton.equals(DELETE_BTN))     deleteButtonPressed();
      else if (vsButton.equals(CLOSE_BTN))      closeButtonPressed();
      else if (vsButton.equals(VIEW_BTN))       viewButtonPressed();
    }
  }

  /**
   *  Get the default button listener (for pop-up menus) 
   */
  public ActionListener getDefaultListener()
  {
    return mpButtonListener;
  }
  
  /**
   * Add the default methods to the default buttons/fields
   */
  protected void addActionListeners()
  {
    searchButton.addEvent(SEARCH_BTN, mpButtonListener);
    detailedSearchButton.addEvent(DETSEARCH_BTN, mpButtonListener);
    refreshButton.addEvent(REFRESH_BTN, mpButtonListener);
    
    addButton.addEvent(ADD_BTN, mpButtonListener);
    modifyButton.addEvent(MODIFY_BTN, mpButtonListener);
    deleteButton.addEvent(DELETE_BTN, mpButtonListener);
    viewButton.addEvent(VIEW_BTN, mpButtonListener);
    closeButton.addEvent(CLOSE_BTN, mpButtonListener);

    searchField.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        searchButtonPressed();
      }
    });
    mpChkAutoRefresh.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        startOrStopRefreshTimer();
      }
    });
  }
  
  /**
   * Start with a consistent height
   * 
   * @return
   */
  public int getStartingHeight()
  {
    return searchPanel.isVisible() ? 400 : 330;
  }
  
  /**
   *  Method to get the width of the screen.
   *
   *  @return Integer containing screen width
   */
  private int getStartingWidth()
  {
    int screenWidth = 800;
    int vnScreenX   = (int)(getPreferredSize().getWidth());
    int buttonPanelWidth = getButtonPanelWidth();
    int searchPanelWidth = getSearchPanelWidth();

    if (screenWidth < buttonPanelWidth)
    {
      screenWidth = buttonPanelWidth;
    }
    if (screenWidth < searchPanelWidth)
    {
      screenWidth = searchPanelWidth;
    }
    if(screenWidth < vnScreenX)
    {
      screenWidth = vnScreenX;
    }

    return (screenWidth);
  }

 /**
  *  Method to get the width needed for the button panel.
  *
  *  @return Integer containing the preferred panel width
  */
  private int getButtonPanelWidth()
  {
    int panelWidth = 0;
    Component[] comp = buttonPanel.getComponents();

    for(int i = 0; i < comp.length; i++)
    {
      if (comp[i] instanceof JButton)
      {
        if (((JButton)comp[i]).isVisible())
        {
          panelWidth = panelWidth + ((JButton)comp[i]).getWidth() + 10;
        }
      }
    }
    return (panelWidth);
  }

 /**
  *  Method to get the width needed for the search panel.
  *
  *  @return Integer containing the preferred panel width
  */
  private int getSearchPanelWidth()
  {
    int panelWidth = 0;
    Component[] comp = searchPanel.getComponents();

    for(int i = 0; i < comp.length; i++)
    {
      if (comp[i] instanceof JButton ||
          comp[i] instanceof JLabel ||
          comp[i] instanceof JTextField)
      {
        if (((JComponent)comp[i]).isVisible())
        {
          panelWidth = panelWidth + ((JComponent)comp[i]).getWidth() + 10;
        }
      }
    }
    return (panelWidth);
  }
  
 /**
  *  Method to set the search field.
  *
  *  @param label String containing search label.
  *  @param maxSize Int containing maximum size of search text.
  */
  protected void setSearchData(String label, int maxSize)
  {
    if (!label.endsWith(":"))
    {
      label = label + ":";
    }
    searchLabel.setText(label);
    searchField.setColumns(maxSize);
    setSearchVisible(true);
  }

  /**
   *  Method to set the search field with a custom component.
   *  <BR>If you use this, you will need to override (or not use) 
   *  getEnteredSearchText()
   *
   *  @param label String containing search label.
   *  @param ipCustomSearch
   */
  protected void setSearchData(String label, JComponent ipCustomSearch)
  {
    if (!label.endsWith(":"))
    {
      label = label + ":";
    }
    searchLabel.setText(label);
    searchField = null;
    
    searchPanel.add(searchLabel, null);
    searchPanel.add(ipCustomSearch, null);
    searchPanel.add(searchButton, null);
    searchPanel.add(detailedSearchButton, null);
    searchPanel.add(refreshButton, null);
    searchPanel.add(mpChkAutoRefresh, null);
    
    searchLabel.setVisible(true);
    searchButton.setVisible(true);
    refreshButton.setVisible(true);
    searchPanel.setVisible(true);
  }

 /**
  *  Method used to set the search panel visible.
  *
  *  @param visible True or false.
  *
  */
  protected void setSearchVisible(boolean visible)
  {
    searchLabel.setVisible(visible);
    searchButton.setVisible(visible);
    refreshButton.setVisible(visible);
    searchField.setVisible(visible);
    searchPanel.setVisible(visible);
  }

 /**
  *  Method used to set the detailed search button visible.
  *
  *  @param visible True or false.
  *
  */
  protected void setDetailSearchVisible(boolean visible)
  {
    detailedSearchButton.setVisible(visible);
  }

 /**
  *  Method used to set the view button visible.
  *
  *  @param visible True or false.
  *
  */
  protected void setViewButtonVisible(boolean visible)
  {
    viewButton.setVisible(visible);
  }

 /**
  *  Method used to set the close button visible.
  *
  *  @param visible True or false.
  *
  */
  protected void setCloseButtonVisible(boolean visible)
  {
    closeButton.setVisible(visible);
  }
  
  protected void setAddButtonVisible(boolean izVisible)
  {
    addButton.setVisible(izVisible);
  }

  protected void setModifyButtonVisible(boolean izVisible)
  {
    modifyButton.setVisible(izVisible);
  }
  
  protected void setDeleteButtonVisible(boolean izVisible)
  {
    deleteButton.setVisible(izVisible);
  }

  /**
  *  Method to get the data entered in the search field.
  *
  *  @return String containing entered text
  */
  protected String getEnteredSearchText()
  {
    return (searchField.getText().trim());
  }

  /**
   *  Method to auto-refreshes the display.
   */
  protected void executeAutoRefreshTable()
  {
  }

  /**
   * Turn on or off the "x whatevers found" display
   * @param izDisplay
   * @param isSingular
   */
  protected void setDisplaySearchCount(boolean izDisplay, String isSingular)
  {
    setDisplaySearchCount(izDisplay, isSingular, isSingular + "s", false);
  }

  /**
   * Turn on or off the "x whatevers found" display
   * @param izDisplay
   * @param isSingular
   * @param izAllowUpperCase
   */
  protected void setDisplaySearchCount(boolean izDisplay, String isSingular, 
      boolean izAllowUpperCase)
  {
    setDisplaySearchCount(izDisplay, isSingular, isSingular + "s", izAllowUpperCase);
  }

  /**
   * Turn on or off the "x whatevers found" display
   * @param izDisplay
   * @param isSingular
   * @param isPlural
   */
  protected void setDisplaySearchCount(boolean izDisplay, String isSingular, 
      String isPlural)
  {
    setDisplaySearchCount(izDisplay, isSingular, isPlural, false);
  }

  /**
   * Turn on or off the "x whatevers found" display
   * @param izDisplay
   * @param isSingular
   * @param isPlural
   * @param izAllowUpperCase
   */
  protected void setDisplaySearchCount(boolean izDisplay, String isSingular, 
      String isPlural, boolean izAllowUpperCase)
  {
    if (isSingular.trim().length() == 0)
    {
      isSingular = "row";
    }
    mzDisplayCountOnRefresh = izDisplay;
    msSingularRowDescription = 
      izAllowUpperCase ? isSingular : isSingular.toLowerCase();
    msPluralRowDescription = " " + 
      (izAllowUpperCase ? isPlural : isPlural.trim().toLowerCase());
  }

  /**
   *  Method to refreshes display with List.
   *
   *  @param aList List containing data for list.
   */
  protected void refreshTable(List aList)
  {
    sktable.refreshData(aList);
    if (mzDisplayCountOnRefresh)
    {
      displayTableCount();
    }
  }
  
  /**
   * Display the count
   */
  protected void displayTableCount()
  {
    int vnEntries = sktable.getRowCount();
    switch (vnEntries)
    {
      case 0:
        displayInfoAutoTimeOut("No data found");
        break;
        
      case 1:
        displayInfoAutoTimeOut("1 " + msSingularRowDescription + " found");
        break;
        
      default:
        // Make it so you don't need a translation for every possible number
        String vsDisplay = DacTranslator.getTranslation("%d "
            + msPluralRowDescription + " found");
        vsDisplay = vsDisplay.replaceFirst("%d", "" + vnEntries);
        displayInfoAutoTimeOut(vsDisplay);
    }
  }

 /**
  *  Method to get the button panel. Used for adding more buttons to the panel.
  *
  *  @return JPanel containing button panel.
  */
  protected JPanel getButtonPanel()
  {
    return (buttonPanel);
  }

  /*========================================================================*/
  /*  Methods for the pop-up                                                */
  /*========================================================================*/

  /**
   * Default mouse listener.  Should be overridden in most cases. 
   */
  protected void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
      /**
       *  Defines popup menu items for <code>SKDCTable</code>, and adds 
       *  listeners to them.
       */
      @Override
      public SKDCPopupMenu definePopup()
      {
        popupMenu.add("Add"   , ADD_BTN   , getDefaultListener());
        popupMenu.add("Modify", MODIFY_BTN, getDefaultListener());
        popupMenu.add("Delete", DELETE_BTN, getDefaultListener());
        
        if (ePerms == null)
        {
          ePerms = getPermissions();
        }
        if (ePerms != null)
        {
          popupMenu.setAuthorization("Add"   , ePerms.iAddAllowed);
          popupMenu.setAuthorization("Modify", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Delete", ePerms.iDeleteAllowed);
        }

        return(popupMenu);
      }

      /**
       *  Display the Order Line screen.
       */
      @Override
      public void displayDetail()
      {
        viewButtonPressed();
      }
    });
  }
  
  /*========================================================================*/
  /*  Consistency helper messages.  Why duplicate this code 187 times?      */
  /*========================================================================*/
  
  /**
   * Are there row(s) selected?
   * @param isForThis
   * @param izAllowMultipleRows
   * @return
   */
  protected boolean isSelectionValid(String isForThis, boolean izAllowMultipleRows)
  {
    int vnSelectedRows = sktable.getSelectedRowCount();
    if (vnSelectedRows == 0)
    {
      displayInfoAutoTimeOut("No row selected to " + isForThis, "Selection Error");
      return false;
    }
    if (!izAllowMultipleRows && (vnSelectedRows > 1))
    {
      displayInfoAutoTimeOut("Only one row can be selected to " + isForThis 
          + " at a time", "Selection Error");
      return false;
    }
    return true;
  }

  /**
   * Are there row(s) selected?
   * @param izAllowMultipleRows
   * @return
   */
  protected boolean isSelectionValidForModify(boolean izAllowMultipleRows)
  {
    return isSelectionValid("Modify", izAllowMultipleRows);
  }
  
  /**
   * Are there row(s) selected?
   * @param izAllowMultipleRows
   * @return
   */
  protected boolean isSelectionValidForDelete(boolean izAllowMultipleRows)
  {
    return isSelectionValid("Delete", izAllowMultipleRows);
  }
}
