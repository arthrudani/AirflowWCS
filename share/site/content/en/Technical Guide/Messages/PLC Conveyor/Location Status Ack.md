---
title: "Location Status Ack [160]"
linkTitle: "Location Status Ack [160]"
weight: 7
type: docs
simple_list: true

---

# Message: Location Status Acknowledgement

This is an acknowledgement message sends by Airflow WCS to PLC after receiving the Location Status message.

- **Message Type:** 160
- **Direction:** Airflow WCS → PLC  


|Type |Description |
|-----|------------|
|Word |Status Flags ( see [ Ack Status Values]({{< relref "/Technical Guide/Messages/Common/Ack Status Values.md" >}}) ) |
