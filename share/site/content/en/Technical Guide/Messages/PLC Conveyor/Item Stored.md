---
title: "Item Stored [52]"
linkTitle: "Item Stored [52]"
weight: 6
type: docs
simple_list: true

---

# Message: Item Stored
This message is used by PLC to notify the Airflow WCS when process of storing a tray/bag/item is completed.

- **Message Type:** 52
- **Direction:** PLC → Airflow WCS
- **Response:** Yes

<!-- -->

## Item Stored [52]
|Type |Description |Comment |
|-----|------------|------------|
|DWord |Order Id |Move order request ID.|
|DWord |Tray ID |A unique container ID|
|DWord |Global ID |	A unique identifier for a bag in entire system which is expected to be generated by BHS PLC |
|Char[12]|Item ID |If Tray Status is Occupied, and Item ID is known(IATA barcode) |
|DWord |Location ID  |Where the item is stored |
|Word |Status Flags  |1 = succeed , 2 = Error , 3 = Bin Full Error |


