---
title: "Retrieve Item [54]"
linkTitle: "Retrieve Item [54]"
weight: 5
type: docs
simple_list: true

---

# Message: Retrieve Item 

The Retrieve Item message used by SAC to request releasing specific number of trays/bags/items from the storage.


- **Message Type:** 54
- **Direction:** SAC  → AirflowWCS

<!-- -->

## Retrieve Item Request - [54]

|Type |Description |Comment|
|-----|------------|------------|
|DWord | Order ID | Unique Order Identifier | 
|Word |Array Length |Number of items in the Array|
|Array[] |Array of tray/bag/object | Array of the trays/bags/items described in next section|


<!-- -->

An item/bag/object data is composed of elements below:


|Type |Description | Comment |
|-----|------------|----------|
|DWord |Tray ID  |
|DWord |Global ID |
|Char[12]|Item ID  |Bag ID or barcode (IATA barcode)|
|DWord |Final Sort Location ID| Location where the bag is going to be delivered after being released from storage| 
