---
title: "Inventory Response [8]"
linkTitle: "Inventory Response [8]"
weight: 14
type: docs
simple_list: true

---
# Message: Inventory Response

The AirflowWCS will response to the Inventory Request by sending this message back to SAC.
- **Message Type:** 8
- **Direction:** AirflowWCS  → SAC

## Inventory Response [8]

|Type |Description |Comment |
|-----|------------|------------|
|DWord |Request ID |SAC unique Request Identifier|
|Word |Status Flag| 1 = succeed, 2 = Error |
|Word |Array Length|0 if no bags|
|Array[] |Array Of Bags|Empty if no bags|


A bag data is composed of elements below:

|Type |Description | Comment |
|-----|------------|----------|
|DWord |Tray ID  |A unique container ID|
|DWord |Global ID |A unique identifier for a bag in entire system which is expected to be generated by BHS PLC|
|Char[12]|Item ID  |Bag ID or barcode (IATA barcode)|
|Char[8]|Flight Number  |Example: QFA1234A|
|Char[14]|Flight Scheduled Date Time - STD| Format: YYYYMMDDHHMMSS - Example: 20221201134500|
|DWord |Location ID |Storage Location ID|
|Char[3]|Warehouse ID  |The Warehouse identifier - Example: W01|