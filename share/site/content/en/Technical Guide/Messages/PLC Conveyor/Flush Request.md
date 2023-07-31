---
title: "Flush Request [3]"
linkTitle: "Flush Request [3]"
weight: 4
type: docs
simple_list: true

---

# Message: Flush Request

The Flush message is sent by AirflowWCS to PLC requesting flushing all or some of the items/bags in the specified lane. The PLC will acknowledge the request immediately by sending a response message. 

- **Message Type:** 3
- **Direction:** AirflowWCS → PLC
- **Response:** Yes

<!-- -->
## Flush Request [3]
|Type |Description |Comments |
|-----|------------|------------|
|DWord |Order Id |Move order request ID. Contains the Order ID that was sent from the SAC to the WCS. Allows filtering of all messages between SAC to WCS to ACP for a given store or retrieve request. |
|DWord| Lane ID  |Unique Device ID |
|Word | Quantity | Number of items to be released (0 = All)|
|Word | Request Type |0 = Process,1 = Cancel|

