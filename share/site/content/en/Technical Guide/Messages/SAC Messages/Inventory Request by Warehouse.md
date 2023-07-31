---
title: "Inventory Request by Warehouse [59]"
linkTitle: "Inventory Request by Warehouse [59]"
weight: 13
type: docs
simple_list: true

---

# Message: Inventory Request by Warehouse

The Inventory Request by Warehouse is used by SAC to get the inventory information of all bags associated with the specified warehouse.

- **Message Type:** 59
- **Direction:** SAC  → AirflowWCS

<!-- -->

## Inventory Request by Warehouse - [59]

|Type |Description |Comment|
|-----|------------|------------|
|DWord | Request ID | Unique Request Identifier |
|Char[3]|Warehouse ID  |Example: W01|
