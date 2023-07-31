---
title: "Link Startup Synchronization [99]"
linkTitle: "Link Startup Synchronization [99]"
weight: 1
type: docs
simple_list: true

---

# Message: Link Startup Synchronization

This message is sent from the WCS to the PLC and from the PLC to the WCS during the initiation of the link between the two systems. It informs the other system of the message serial number at which the sender will begin sending messages.

- **Message Type:** 99
- **Direction:** Airflow WCS → PLC and PLC → Airflow WCS 

There is no data for this message. Only the message header is sent.