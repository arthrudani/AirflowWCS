package com.daifukuamerica.wrxj.swingui.recovery;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.LinkedList;
import java.util.List;

/**
 * A screen class for collecting detailed search criteria for a list of
 * loads for recovery.
 *
 * @author mda
 * @version 1.0
 */
public class RecoveryDetailedSearchFrame extends DacInputFrame
{
  private static final long serialVersionUID = 0L;

  protected SKDCTextField     mpTextLoadID;
  protected SKDCTextField     mpTextParentLoadID;
  protected SKDCTranComboBox  mpCBStatus;
  protected SKDCComboBox      mpCBDevice;
//  protected SKDCComboBox      mpCBContainer;
  protected LocationPanel mpRLPLocation;
  protected LocationPanel mpRLPFinalLoc;

  StandardInventoryServer mpInvServer = Factory.create(StandardInventoryServer.class);
  StandardLocationServer  mpLocServer = Factory.create(StandardLocationServer.class);

  /**
   *  Create load search frame.
   *
   */
  public RecoveryDetailedSearchFrame()
  {
    super("Load Search", "Recovery Search Criteria");
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  private void jbInit() throws Exception
  {
    buildScreen();
  }

  /*========================================================================*/
  /*  Methods for display formatting                                        */
  /*========================================================================*/
  /**
   * Builds the screen
   */
  protected void buildScreen()
  {
    int[] vanLoadMoveStatus;
    int[] vanShortLoadMoveStatus;

    mpTextLoadID       = new SKDCTextField(LoadData.LOADID_NAME);
    mpTextParentLoadID = new SKDCTextField(LoadData.PARENTLOAD_NAME);
    mpCBDevice         = new SKDCComboBox();
//    mpCBContainer      = new SKDCComboBox();
    mpRLPLocation      = Factory.create(LocationPanel.class);
    mpRLPFinalLoc      = Factory.create(LocationPanel.class);

    fillCBDevice();
//    fillCBContainer();
    mpRLPLocation.setWarehouseList(LocationPanel.WTYPE_REGULAR, true);
    mpRLPFinalLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR, true);
    try
    {
      /*
       * Load Move Status... For Recovery, always exclude NOMOVE loads
       */
      vanLoadMoveStatus = DBTrans.getIntegerList("iLoadMoveStatus");
      vanShortLoadMoveStatus = new int[vanLoadMoveStatus.length - 2];
      int i = 0;
      for (int j : vanLoadMoveStatus)
      {
        if (j != DBConstants.NOMOVE && j != DBConstants.PICKED)
        {
          vanShortLoadMoveStatus[i++] = j;
        }
      }
      mpCBStatus = new SKDCTranComboBox(LoadData.LOADMOVESTATUS_NAME, vanShortLoadMoveStatus, true);
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }


    addInput("Load:"       , mpTextLoadID);
    addInput("Parent Load:", mpTextParentLoadID);
    addInput("Move Status:", mpCBStatus);
//    addInput("Container:"  , mpCBContainer);
    addInput("Device:"     , mpCBDevice);
    addInput("Location:"   , mpRLPLocation);
    addInput("Destination:", mpRLPFinalLoc);

    useSearchButtons();
  }


  /**
   *  Method to populate the device combo box.
   */
  private void fillCBDevice()
  {
    try
    {
      String [] vasDevices = mpLocServer.getDeviceIDList(true);
      mpCBDevice.setComboBoxData(vasDevices);
    }
    catch (DBException e)
    {
      mpCBDevice.setDisplayAllEnabled(true);
    }
  }

  /**
   *  Method to populate the container type combo box.
   */
//  private void fillCBContainer()
//  {
//    List containerList = mpInvServer.getContainerTypeList("");
//    mpCBContainer.setDisplayAllEnabled(true);
//    mpCBContainer.setComboBoxData(containerList);
//  }

  /*========================================================================*/
  /*========================================================================*/

  /**
   *  Method to clean up as needed at closing.
   *
   */
  @Override
  public void cleanUpOnClose()
  {
    mpLocServer.cleanUp();
    mpLocServer = null;

    mpInvServer.cleanUp();
    mpInvServer = null;
  }

  /*========================================================================*/
  /*  Action methods                                                        */
  /*========================================================================*/

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTextLoadID.setText("");
    mpTextParentLoadID.setText("");
    mpCBDevice.getEditor().setItem("");
//    mpCBContainer.getEditor().setItem("");
    mpRLPFinalLoc.reset();
    mpRLPLocation.reset();
  }

  /**
   *  Action method to handle Search button. Method fires a property change
   *  event so parent frame can refresh its display.
   */
  @Override
  protected void okButtonPressed()
  {
    changed();
  }

  /*========================================================================*/
  /*  Let the caller get the results                                        */
  /*========================================================================*/

  /**
   *  Method to get the entered search criteria as a KeyObject.
   *
   *  @return ColumnObject containing criteria to use in search
   */
  public KeyObject[] getSearchKeyData ()
  {
    List<KeyObject> vpSearchData = new LinkedList<KeyObject>();

    /*
     * Figure out the rest of the search criteria
     * Load...
     */
    String vsLoadID = mpTextLoadID.getText().trim();
    if (vsLoadID.length() > 0)
    {
      KeyObject vpLoadKey = new KeyObject(LoadData.LOADID_NAME, vsLoadID);
      vpLoadKey.setComparison(KeyObject.LIKE);
      vpLoadKey.setConjunction(KeyObject.AND);
      vpSearchData.add(vpLoadKey);
    }

    /*
     * Parent Load...
     */
    String vsParentLoadID = mpTextParentLoadID.getText().trim();
    if (vsParentLoadID.length() != 0)
    {
      KeyObject vpLoadKey = new KeyObject(LoadData.PARENTLOAD_NAME, vsParentLoadID);
      vpLoadKey.setComparison(KeyObject.LIKE);
      vpLoadKey.setConjunction(KeyObject.AND);
      vpSearchData.add(vpLoadKey);
    }

    /*
     * Device...
     */
    String vsDevice = mpCBDevice.getSelectedItem().toString().trim();
    if (vsDevice.length() > 0)
    {
      vpSearchData.add(new KeyObject(LoadData.DEVICEID_NAME, vsDevice));
    }

    /*
     * Container...
     */
//    String vsContainer = mpCBContainer.getSelectedItem().toString().trim();
//    if (vsContainer.length() > 0)
//    {
//      vpSearchData.add(new KeyObject(vpLoadData.getContainerTypeName(), vsContainer));
//    }

    try
    {
      /*
       * Load Move Status... For Recovery, always exclude NOMOVE, PICKED loads
       */
      int vnLoadMoveStatus = mpCBStatus.getIntegerValue();
      if (vnLoadMoveStatus == SKDCConstants.ALL_INT)
      {
        KeyObject vpStatusKey1 = new KeyObject(LoadData.LOADMOVESTATUS_NAME,
          Integer.valueOf(DBConstants.NOMOVE));
        vpStatusKey1.setComparison(KeyObject.NOT_EQUAL);
        vpSearchData.add(vpStatusKey1);

        KeyObject vpStatusKey2 = new KeyObject(LoadData.LOADMOVESTATUS_NAME,
          Integer.valueOf(DBConstants.PICKED));
        vpStatusKey2.setComparison(KeyObject.NOT_EQUAL);
        vpStatusKey2.setConjunction(KeyObject.AND);
        vpSearchData.add(vpStatusKey2);

        KeyObject vpStatusKey3 = new KeyObject(LoadData.LOADMOVESTATUS_NAME,
          Integer.valueOf(DBConstants.STAGED));
        vpStatusKey3.setComparison(KeyObject.NOT_EQUAL);
        vpStatusKey3.setConjunction(KeyObject.AND);
        vpSearchData.add(vpStatusKey3);

        KeyObject vpStatusKey4 = new KeyObject(LoadData.LOADMOVESTATUS_NAME,
          Integer.valueOf(DBConstants.RECEIVED));
        vpStatusKey4.setComparison(KeyObject.NOT_EQUAL);
        vpStatusKey4.setConjunction(KeyObject.AND);
        vpSearchData.add(vpStatusKey4);

      }
      else
      {
        vpSearchData.add(new KeyObject(mpCBStatus.getTranslationName(), mpCBStatus.getIntegerObject()));
      }

      /*
       * Current warehouse/address
       */
      String vsWarehouse = mpRLPLocation.getWarehouseString().trim();
      if (vsWarehouse.length() > 0)
      {
        vpSearchData.add(new KeyObject(LoadData.WAREHOUSE_NAME, vsWarehouse));

        String vsAddress = mpRLPLocation.getAddressString().trim();
        if (vsAddress.length() > 0)
        {
          vpSearchData.add(new KeyObject(LoadData.ADDRESS_NAME, vsAddress));
        }
      }

      /*
       * Final warehouse/address
       */
      String vsFinalWarehouse = mpRLPFinalLoc.getWarehouseString().trim();
      if (vsFinalWarehouse.length() > 0)
      {
        vpSearchData.add(new KeyObject(LoadData.FINALWAREHOUSE_NAME, vsFinalWarehouse));

        String vsFinalAddress = mpRLPFinalLoc.getAddressString().trim();
        if (vsFinalAddress.length() > 0)
        {
          vpSearchData.add(new KeyObject(LoadData.FINALADDRESS_NAME, vsFinalAddress));
        }
      }
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }
    catch (DBException e3)
    {
      e3.printStackTrace(System.out);
    }
    return (KeyObject.toKeyArray(vpSearchData));
  }
}