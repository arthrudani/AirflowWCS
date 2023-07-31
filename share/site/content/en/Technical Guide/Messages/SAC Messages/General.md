---
title: "General"
linkTitle: "General"
weight: 1
type: docs
simple_list: true

---
# Introduction
AirflowWCS application communicates with the SAC via TCP sockets connections. The Airflow WCS will be a client in this TCP/IP connections. 
## Message Overview
Device independent message structures are used for communications between two systems.The following section describes the interface messages format between these systems.



<!-- -->

## Message Format

Each message consists of a fixed length **Message Header** and a **Message Body**.
The content and length of a **Message Body** will depend on the **Message Type**.

Additional wrappings(STX, ETX) are optional and using this option is configurable, as they may be required for a particular SAC system. The byte size of these additional wrappings is not included in the message length.

The Airflow WCS uses only the big-endian byte order format in the communication.

### Additional wrapping Example 
As mentioned earlier each message structure or packet consists of header and body and might start and end with a special character (STX and ETX). 
A complete message structure with these wrapping characters shown in table below:

|Filed 			| Length	| Type 		| Value	| Description|
|---------------|-----------|-----------|-------|------------|
|Start of Text	| 	1		| Byte		| 2		|ASCII Start of Text character (STX).|
|Message Header	|  16  		| 			| 		|Message Header details is defined below |
|Message Body	| Variable	| Variable	| 		|The details of each message are defined in Messages section|
|End of Text	| 	1		| Byte		| 3		|ASCII End of Text character (ETX).|

## Data types are described below.

|Type |Description |
|-----|------------|
|Byte |Unsigned 8-bit |
|Char |Signed 8-bit Character |
|Word |Unsigned 16-bit |
|DWord |Unsigned 32-bit |



## Message Header

All messages start with the following header.

|Type |# Bytes|Description |
|-----|----|------------|
|Word |2|Message Length in bytes (Header + Body but not additional wrapping) |
|Word |2|Message Sequence/Serial number  |
|Word |2|Message Type (Messages originating from WCS sent to SAC are numbered 1-49. Messages originating from SAC sent to WCS are numbered 51-99. Acknowledgement messages are numbered the same as the original message and then adding 100.) |
|DWord|4|ID of the device that the message relates to. The SAC Id will be 4 digits which can be between( 8000 – 8888 )|
|Byte |1|Timestamp; Hours in 24 formats(since midnight) |
|Byte |1|Timestamp; Minutes (0 - 59) |
|Word |2|Timestamp; Milliseconds ((seconds x 1000)+ milliseconds) |
|Word |2|Message version number (1 - 99)|

**Total # Bytes = 16**

![](/images/TechnicalGuides/Architecture2.png)




