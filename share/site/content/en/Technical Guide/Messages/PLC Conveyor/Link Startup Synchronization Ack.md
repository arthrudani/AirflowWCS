---
title: "Link Startup Synchronization Ack [199]"
linkTitle: "Link Startup Synchronization Ack [199]"
weight: 1
type: docs
simple_list: true

---

# Message: Link Startup Synchronization Acknowledgement

This is an acknowledgement message sends by WCS to the PLC and from the PLC to the WCS after receiving the Link Startup Synchronization message.

- **Message Type:** 199
- **Direction:** Airflow WCS → PLC and PLC → Airflow WCS 


|Type |Description |
|-----|------------|
|Word |Status Flags ( see [ Ack Status Values]({{< relref "/Technical Guide/Messages/Common/Ack Status Values.md" >}}) ) |
