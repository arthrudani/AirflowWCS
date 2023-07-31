---
title: "Location Status [60]"
linkTitle: "Location Status [60]"
weight: 7
type: docs
simple_list: true

---

# Message: Location Status

This message is used to report the current status of one or more Locations / Devices/ Equipment. 
A PLC sends this message to Airflow WCS when the status of any equipment changes or when the conveyor storage is full.


<!-- -->
- **Message Type:** 60
- **Direction:** PLC → Airflow WCS
- **Response:** No
- **Acknowledgement:** Yes

<!-- -->

The Location Status message has a variable length. The message includes an array of structures containing Location ID and status information.
## Location Status [60]

|Type |Description |Comment |
|-----|------------|------------|
|Array[] |Array of Location Status | 



A LocationStatus object is composed of 2 Words:

|Type |Description |
|-----|------------|
|DWord |Location or Station ID |
|Word |Status Flags (0 = Online-Available, 1 = Offline-Unavailable , 2 = Conveyor full) |

