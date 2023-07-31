---
title: "Retrieve Flight Response [5]"
linkTitle: "Retrieve Flight Response [5]"
weight: 8
type: docs
simple_list: true

---
# Message: Retrieve Flight Response

AirflowWCS will replay to the request by sending this response message to SAC.

## Retrieve Flight Response [5]

- **Message Type:** 5
- **Direction:** AirflowWCS  → SAC

|Type |Description |Comment |
|-----|------------|------------|
|DWord |Order ID |Unique Order Identifier |
|Word |Status | 1=if all bags are retrieved (Succeed), 2=if process is failed for any reasons (Failed),3=if process is completed but there are shortages (Shortage)|
| 
|Word |Number of missing bags|0 if no missing bags|


