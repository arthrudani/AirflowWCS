package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.Date;

/* Description:<BR>
 *  Ship Complete outbound message builder.
 *
 * @author       A.D.
 * @version      1.0     03/02/05
 */
public class ShipComplete extends MessageOut 
{
  private final String TRAN_DATE_NAME      = "dTransactionTime";
  private final String ORDERID_NAME        = "sOrderID";
  private final String ITEM_NAME           = "sItem";
  private final String LOT_NAME            = "sLot";
  private final String SHIP_QTY_NAME       = "fQuantity";
  private final String LOADID_NAME         = "sLoadID";
  private final String DEST_STATION_NAME   = "sDestStation";
  private final String WAREHOUSE_NAME      = "sWarehouse";
  private final String ADDRESS_NAME        = "sAddress";
  private final String PRIORITY_NAME       = "iPriority";
  private final String RELEASETO_CODE_NAME = "sReleaseToCode";
  private final String DEVICE_NAME         = "sDeviceID";
  private final String TRACKING_NUM_NAME   = "sTrackingNumber";
  private final String LINEID_NAME         = "sLineID";
  private final String CARRIERID_NAME      = "sCarrierID";
  private final String WEIGHT_NAME         = "fWeight";
  private final String FREIGHT_CHARGE_NAME = "fFreightCharge";
  private final String DISCCOUNT_NAME      = "fDiscountCharge";
  private final String FUELSURCHARGE_NAME  = "fFuelSurcharge";
  private final String INSURANCE_NAME      = "fInsuranceSurcharge";
  private final String USERID_NAME         = "sUserID";

 /**
  * Default constructor. This constructor finds the correct message formatter to
  * use for this message.
  */
  public ShipComplete()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(TRAN_DATE_NAME, new Date()),
      new ColumnObject(ORDERID_NAME, ""),
      new ColumnObject(ITEM_NAME, ""),
      new ColumnObject(LOT_NAME, ""),
      new ColumnObject(SHIP_QTY_NAME, Double.valueOf(0.0)),
      new ColumnObject(LOADID_NAME, ""),
      new ColumnObject(DEST_STATION_NAME, ""),
      new ColumnObject(WAREHOUSE_NAME, ""),
      new ColumnObject(ADDRESS_NAME, ""),
      new ColumnObject(PRIORITY_NAME, Integer.valueOf(0)),
      new ColumnObject(RELEASETO_CODE_NAME, ""),
      new ColumnObject(DEVICE_NAME, ""),
      new ColumnObject(USERID_NAME, ""),
      new ColumnObject(TRACKING_NUM_NAME, ""),
      new ColumnObject(LINEID_NAME, ""),
      new ColumnObject(CARRIERID_NAME, ""),
      new ColumnObject(WEIGHT_NAME, Double.valueOf(0.0)),
      new ColumnObject(FREIGHT_CHARGE_NAME, Double.valueOf(0.0)),
      new ColumnObject(DISCCOUNT_NAME, Double.valueOf(0.0)),
      new ColumnObject(FUELSURCHARGE_NAME, Double.valueOf(0.0)),
      new ColumnObject(INSURANCE_NAME, Double.valueOf(0.0))
    };

    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.SHIP_COMPLETE;
  }

  public void setTransactionTime(Date dAddDateTime)
  {
    ColumnObject.modify(TRAN_DATE_NAME, dAddDateTime, messageFields);
  }
  public void setOrderID(String sOrderID)
  {
    ColumnObject.modify(ORDERID_NAME, sOrderID, messageFields);
  }
  
  public void setItem(String sItem)
  {
    ColumnObject.modify(ITEM_NAME, sItem, messageFields);
  }

  public void setLot(String sLot)
  {
    ColumnObject.modify(LOT_NAME, sLot, messageFields);
  }

  public void setShipQuantity(double shipQuantity)
  {
    ColumnObject.modify(SHIP_QTY_NAME,
                        SKDCUtility.getTrucatedDoubleObj(shipQuantity),
                        messageFields);
  }
  
  public void setShipLoadID(String sLoadID)
  {
    ColumnObject.modify(LOADID_NAME, sLoadID, messageFields);
  }
  
  public void setDestinationStation(String sShippingStation)
  {
    ColumnObject.modify(DEST_STATION_NAME, sShippingStation, messageFields);
  }

  public void setShippingWarehouse(String sShippingWarehouse)
  {
    ColumnObject.modify(WAREHOUSE_NAME, sShippingWarehouse, messageFields);
  }

  public void setShippingAddress(String sShippingAddress)
  {
    ColumnObject.modify(ADDRESS_NAME, sShippingAddress, messageFields);
  }

  public void setOrderPriority(int iOrderPriority)
  {
    ColumnObject.modify(PRIORITY_NAME, Integer.valueOf(iOrderPriority), messageFields);
  }

  public void setReleaseToCode(String sReleaseToCode)
  {
    ColumnObject.modify(RELEASETO_CODE_NAME, sReleaseToCode, messageFields);
  }

  public void setTerminalID(String sDeviceID)
  {
    ColumnObject.modify(DEVICE_NAME, sDeviceID, messageFields);
  }
  
  public void setUserID(String sUserID)
  {
    ColumnObject.modify(USERID_NAME, sUserID, messageFields);
  }
  
  public void setTrackingNumber(String sTrackingNumber)
  {
    ColumnObject.modify(TRACKING_NUM_NAME, sTrackingNumber, messageFields);
  }

  public void setOrderLineID(String sLineID)
  {
    ColumnObject.modify(LINEID_NAME, sLineID, messageFields);
  }

  public void setCarrierID(String sCarrierID)
  {
    ColumnObject.modify(CARRIERID_NAME, sCarrierID, messageFields);
  }

  public void setWeight(double fWeight)
  {
    ColumnObject.modify(WEIGHT_NAME,
                        SKDCUtility.getTrucatedDoubleObj(fWeight),
                        messageFields);
  }

  public void setFreightCharge(double fFreightCharge)
  {
    ColumnObject.modify(FREIGHT_CHARGE_NAME,
                        SKDCUtility.getTrucatedDoubleObj(fFreightCharge),
                        messageFields);
  }

  public void setDiscountCharge(double fDiscountCharge)
  {
    ColumnObject.modify(DISCCOUNT_NAME,
                        SKDCUtility.getTrucatedDoubleObj(fDiscountCharge),
                        messageFields);
  }

  public void setFuelSurcharge(double fFuelSurcharge)
  {
    ColumnObject.modify(FUELSURCHARGE_NAME,
                        SKDCUtility.getTrucatedDoubleObj(fFuelSurcharge),
                        messageFields);
  }

  public void setInsuranceSurcharge(double fInsuranceSurcharge)
  {
    ColumnObject.modify(INSURANCE_NAME,
                        SKDCUtility.getTrucatedDoubleObj(fInsuranceSurcharge),
                        messageFields);
  }
}
