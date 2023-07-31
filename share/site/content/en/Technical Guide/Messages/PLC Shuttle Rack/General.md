---
title: "General"
linkTitle: "General"
weight: 1
type: docs
simple_list: true

---
# Introduction
Airflow WCS application communicates with one or more  ACPs via TCP sockets connections. The WCS maintains a separate link to each ACP and each one operates independently using the protocol and messages defined in this document.
The Airflow WCS should be setup as a server for TCP/IP connections. This document describes the message format for communications between Airflow WCS and Aisle Control ACPs (ACP).


## Message Overview
Device independent message structures are used for communications between the Airflow WCS system and ACPs.
The following section describes the interface messages format between these systems.

<!-- -->

## Message Format

Each message consists of a fixed length **Message Header** and a **Message Body**.
The content and length of a **Message Body** will depend on the **Message Type**.

Additional wrappings(STX, ETX) are optional and using this option is configurable, as they may be required for a particular ACP type. The byte size of these additional wrappings is not included in the message length.

The Word or DWord data may be sent in the native format (big-endian or little-endian) of the ACP; AirflowWCS is configured to take byte ordering of the ACP into account.

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
|Word |2|Message Type (Messages originating from WCS sent to ACP are numbered 1-49. Messages originating from ACP sent to WCS are numbered 51-99. Acknowledgement messages are numbered the same as the original message and then adding 100.) |
|DWord|4|ID of the device that the message relates to. The device or ACP id will be 4 digits starting from 9000 – 9999|
|Byte |1|Timestamp; Hours in 24 formats(since midnight) |
|Byte |1|Timestamp; Minutes (0 - 59) |
|Word |2|Timestamp; Milliseconds ((seconds x 1000)+ milliseconds) |
|Word |2|Message version number (1 - 99) |

**Total # Bytes = 16**

![](/images/TechnicalGuides/Architecture2.png)

## Serial Number
The serial number for all serial messages is attached to each message before it is transmitted. The serial number is used to verify that no message is lost or repeated in case of a failure of either system or a failure of the communication link. The serial number assigned to a message is always one greater than the serial number of the previous message sent by the same system. The smallest serial number is 0. The largest serial number is 32767. The next serial number after 32767 is 1.
The serial number for the Keep Alive message is not used and is set to 0.

## Acknowledgment of Messages
Each serial message received by the WCS or ACP must be acknowledged after is it journalled or otherwise processed. An acknowledgment means the message was received and validated. A system cannot send another message until it has received an acknowledgment for the prior message.
An acknowledgment is not considered a serial message however it has the same general format, but the serial number field in an acknowledgment is the serial number of the message that is being acknowledged, not a new incremental serial number. An acknowledgment is not assigned a serial number of its own.
Acknowledgments are not serial messages and therefore not acknowledged by the receiving system. Each acknowledgment message acknowledges the receipt of one and only one message. The sender of a message must keep a copy of that message available for re-transmission until an acknowledgment for that message is received.
In the event that one of the systems has sent a message and has not received an acknowledgment for some period of time, the system alarms the condition and breaks the communications link. The ACP then attempts to restart communications and the untransmitted message gets retransmitted as the first message after the initial handshake. When communications are down, the sending system may either temporarily suspend some operations or continue to buffer messages locally.
The timeout value is for the above situation is unspecified, but a typical value is 30 seconds. This timeout value is long enough to allow manual troubleshooting/debugging when needed, but not so long as to interfere with normal operation.
If the system continues operations and the list of buffered messages continues to grow and reaches a set percentage (variable, initially 80 percent) of the total messages that the sender can queue (variable, initially 300), the sender logs an alarm indicating the condition.
If the list of buffered messages reaches the total number of messages that the sender can queue, any new messages are discarded and not sent.
A complete acknowledgment message packet is formatted as follows: (The Additional wrapping characters are optional)

|Filed 			| Length	| Type 		| Value	| Description|
|---------------|-----------|-----------|-------|------------|
|Start of Text	| 	1		| Byte		| 2		|ASCII Start of Text character (STX).|
|Message Header	|  16  		| 			| 		|Message Header details is defined below |
|Message Body 	|   1 		| Byte      | 0-2   |0 = OK, 1 = Error in message content, 2 = serial number error|
|End of Text	| 	1		| Byte		| 3		|ASCII End of Text character (ETX).|



## Acknowledgment Message Actions
Whenever an error is detected in a message by the receiving system, the receiving system sends a acknowledgment with specific error code back to the sending system. The Error Codes are:
1.	There was an error in the serial message content.
2.	The receiving system detected that a serial number was skipped.

If the error was in the serial message content (error code = 1), the serial message is inserted into the text of the negative acknowledgment.
When a receiving system sends a negative acknowledgment, that system should also log a software error (this may help to diagnose the problem).
### Actions for message content error (error code = 1)
Receiver: Ignore the message.
Sender: Do not resend the message. Treat the message sent as having been acknowledged. Log both the original message and the message that was returned with the negative acknowledge. The assumption is that the error was caused by a software problem on one side or the other rather than a communication link fault.
### Actions for serial number error (error code = 2)
Receiver: Process the message. Assume that one or more messages have somehow been lost and re-synchronize to the new serial number. 
Sender: Do not resend the message. Treat the message sent as having been acknowledged. Log the error.


## Startup
In this system, The WCS consists of two separate computer systems configured for hot backup. Only one of the computers runs the WCS application in primary mode at any given time. The WCS primary is the computer node that communicates with the ACPs. A single primary IP address is associated with the WCS primary computer and is used by the ACP to connect with the primary WCS computer. If the WCS switches from one computer to the other, the single primary IP address stays associated with computer acting as the primary WCS.
A communication startup must occur on initial startup and after a failure and recovery of the WCS, an ACP, or the communication link. The ACP tries to establish a communication channel to the WCS computer using the primary IP address. If the connection fails, the ACP periodically retries the connection until the ACP connects to the WCS computer.
Once a communication channel is established, the initial handshake takes place. Two messages comprise the initial handshake. The ACP sends a link startup synchronization message to the WCS and the WCS responds with a link startup synchronization message. The serial number field of the message header for each handshake message contains a value which is one less than the serial number of the next message which will be sent by that side. If either side had previously sent a message that was never acknowledged, that message must be re sent immediately following the initial handshake.
This outline shows briefly the steps of the WCS to ACP communications startup process:
1.	ACP sends connect request to WCS network task.
2.	WCS task accepts or rejects connect request.
3.	Acceptance of connect request causes task to task communication link to be established. Rejection of connect request or other failure causes a timed delay, after which the process reverts to step 1.
4.	ACP sends Link Startup Synchronization message with starting serial number and current time.
5.	WCS sends Link Startup Synchronization message with starting serial number and current time.
6.	ACP and WCS each send one initial acknowledgment.
7.	ACP and WCS process the acknowledgment message received.
8.	ACP and WCS begin transmitting serial messages, starting with the next message which follows the serial number in the link startup synchronization messages.
9.	Transmits continue with increasing serial numbers.
On occasion a system may lose all knowledge of messages received from the other system. When communications are resumed, this system performs a "cold start". This is indicated by the cold starting system sending a serial number of 0 in the link connect to the other system during the communications startup process. Following this, serial numbers from the cold starting system to the other system will begin at 1. Serial numbers from other system to the cold starting system will begin at whatever number the other system would start at in a normal startup. The cold starting system will automatically synchronize to the serial number of the first serial message it receives. From this point on, communications proceed as normal.


## Shutdown
Either the WCS or ACP can initiate a shutdown of communications between the two sides. Shutting down consists of one side or the other breaking the TCP/IPlink.
If the ACP has broken the link, WCS should keep monitoring the communications link, waiting for the ACP to issue a connect request.
If WCS breaks the communications link, the ACP will periodically issue the connect request, attempting to re-establish communications as described in previous Section.
