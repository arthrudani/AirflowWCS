---
title: "Inventory Request by Flight [58]"
linkTitle: "Inventory Request by Flight [58]"
weight: 12
type: docs
simple_list: true

---

# Message: Inventory Request by Flight

The Inventory Request by Flight is used by SAC to get the inventory information of all bags associated with the specified flight number.

- **Message Type:** 58
- **Direction:** SAC  → AirflowWCS

<!-- -->

## Inventory Request by Flight - [58]

|Type |Description |Comment|
|-----|------------|------------|
|DWord | Request ID | Unique Request Identifier |
|Char[8]|Flight Number  |Example: QFA1234A|
|Char[14]|Flight Scheduled Date Time - STD|Format: YYYYMMDDHHMMSS - Example: 20221201134500|

