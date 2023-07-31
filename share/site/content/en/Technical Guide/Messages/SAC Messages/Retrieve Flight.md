---
title: "Retrieve Flight [55]"
linkTitle: "Retrieve Flight [55]"
weight: 7
type: docs
simple_list: true

---

# Message: Retrieve Flight

The Retrieval Order message used by SAC to request releasing all bags or specific number of bags for a flight.

- **Message Type:** 55
- **Direction:** SAC  → AirflowWCS

<!-- -->

## Retrieve Flight Request - [55]

|Type |Description |Comment|
|-----|------------|------------|
|DWord | Order ID | Unique Order Identifier |
|Char[8]|Flight Number  |Example: QFA1234A|
|Char[14]|Flight Scheduled Date Time - STD| Format: YYYYMMDDHHMMSS - Example: 20221201134500|
|Word |Number of Bags to retrieve |0=All|


