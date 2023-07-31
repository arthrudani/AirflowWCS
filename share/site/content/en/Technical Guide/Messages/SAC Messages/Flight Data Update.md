---
title: "Flight Data Update [57]"
linkTitle: "Flight Data Update [57]"
weight: 9
type: docs
simple_list: true

---

# Message:Flight Data Update

The SAC sends this message to Airflow WCS to update the flight info for all associated bags.


- **Message Type:** 57
- **Direction:** SAC → Airflow WCS

<!-- -->

## Flight Data Update [57]
|Type |Description |Comment |
|-----|------------|------------|
|Char[8]|Flight Number  |Example: QFA1234A|
|Char[14]|Flight Scheduled Date Time - STD | Format:  YYYYMMDDHHMMSS - Example: 20221201134500|
|Char[14]|Default Retrieval Date Time |Format:  YYYYMMDDHHMMSS - Example: 20221201134500|
|DWord |Final Sort Location ID |Where the item is going to be delivered after it is released from storage facility|
