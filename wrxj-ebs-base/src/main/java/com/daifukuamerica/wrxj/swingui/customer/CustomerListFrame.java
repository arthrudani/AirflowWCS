package com.daifukuamerica.wrxj.swingui.customer;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCustomerServer;
import com.daifukuamerica.wrxj.dbadapter.data.CustomerData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ListSelectionModel;

/**
 * A screen class for displaying a list of containers.
 *
 * @author pdm
 * @version 1.0
 */
@SuppressWarnings("serial")
public class CustomerListFrame extends SKDCListFrame
{
  StandardCustomerServer mpCustomerServer = Factory.create(StandardCustomerServer.class);

 /**
  *  Create Customer list frame.
  *
  */
  public CustomerListFrame()
  {
    super("Customer");
    sktable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setSearchData("Customer", DBInfo.getFieldLength(CustomerData.CUSTOMER_NAME));
    setSearchByName("");
  }

 /**
  *  Method to filter by name. Refreshes display.
  *
  *  @param s Customer type to search for.
  */
  public void setSearchByName(String isCustomer)
  {
    try
    {
      refreshTable(mpCustomerServer.getCustomerData(CustomerData.CUSTOMER_NAME, isCustomer));
    }
    catch (DBException ve)
    {
      ve.printStackTrace(System.out);
      displayError("Database Error: " + ve);
    }
  }

  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateCustomer vpUpdateCustomer = Factory.create(UpdateCustomer.class, "Add Customer");
    addSKDCInternalFrameModal(vpUpdateCustomer, buttonPanel,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ipEvent)
          {
            String prop = ipEvent.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              setSearchByName("");
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
    if (isSelectionValidForDelete(false))
    {
      String vsCustomer = sktable.getCurrentRowDataField(CustomerData.CUSTOMER_NAME).toString();
      try
      {
        String conf_mesg = "Delete Customer " + vsCustomer + " ";
        if (displayYesNoPrompt(conf_mesg, "Delete Confirmation"))
        {
          mpCustomerServer.deleteCustomer(vsCustomer);
          displayInfoAutoTimeOut("Customer \"" + vsCustomer + "\" Deleted", "Delete Result");
        }
      }
      catch (DBException ve)
      {
        displayError("Failed to delete " + ve.getMessage(), "Delete Result");
      }
      refreshButtonPressed();
    }
  }

  /**
   *  Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {
    if (isSelectionValidForModify(false))
    {
      String vsCustomer = sktable.getCurrentRowDataField(CustomerData.CUSTOMER_NAME).toString();
      UpdateCustomer vpUpdateCustomer = Factory.create(UpdateCustomer.class, "Modify Customer");
      vpUpdateCustomer.setModify(vsCustomer.toString());
      addSKDCInternalFrameModal(vpUpdateCustomer, buttonPanel,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ipEvent)
            {
              String prop = ipEvent.getPropertyName();
              if (prop.equals(FRAME_CHANGE))
              {
                setSearchByName("");
              }
            }
          });
    }
  }

  /**
   * Search
   */
  @Override
  protected void searchButtonPressed()
  {
    setSearchByName(getEnteredSearchText());
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
    return CustomerListFrame.class;
  }
}