---
title: "Flight Data Update [9]"
linkTitle: "Flight Data Update [9]"
weight: 8
type: docs
simple_list: true

---

# Message:Flight Data Update

The Airflow WCS sends this message to ACP to update the final sort location of all the bags in the specified flight in the message. 



- **Message Type:** 9
- **Direction:** Airflow WCS → ACP
- **Response:** No
- **Acknowledgement:** Yes

<!-- -->

## Flight Data Update [9]
|Type |Description |Comment |
|-----|------------|------------|
|Char[8]|Flight Number  |Example: QFA1234|
|DWord |Final Sort Location ID |Where the item is going to be delivered after it is released from storage facility|
