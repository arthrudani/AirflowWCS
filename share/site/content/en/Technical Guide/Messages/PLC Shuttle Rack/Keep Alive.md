---
title: "Keep Alive [1]"
linkTitle: "Keep Alive [1]"
weight: 1
type: docs
simple_list: true

---

# Message: Keep Alive

The Keep Alive request must be sent by TCP/IP Client periodically within a defined keep-alive timeout interval to keep a connection to the TCP/IP Server.

The Keep Alive message contains a flag to confirm that Client is currently active. The response is to confirm that the Server also is alive and active.

- **Message Type:** 1
- **Direction:** Airflow WCS ↔ ACP
- **Response:** Yes

<!-- -->

## Keep Alive Request

|Type |Description |
|-----|------------|
|Word |Active flag. (0=Not active, 1= Active) |


<!-- -->


## Keep Alive Response

|Type |Description |
|-----|------------|
|Word |Active flag. (0=Not active, 1= Active)|

