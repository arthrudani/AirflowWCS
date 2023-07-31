---
title: "Introduction"
linkTitle: "Introduction"
weight: 1
type: docs
simple_list: true

---

# Introduction

**AirflowWCS** is a Early Bag Managment System which is installed as a windows service and has Web & Desktop UI interface.
<br/>
It is able to communicate with other devices or systems using different communication portocols like database, FTP or TCP/IP. 
<!--
Following is the recommandation when TCP/IP is choosen as communication portocols:

 - PLC - AirflowWCS is the TCP server, and the PLC are TCP Clients (Byte ordering also can be configured to be big-endian or little-endian as per native format of the PLC).
 - SAC - AirflowWCS is the TCP client and the SAC is TCP server.
 - SmartFlow - AirflowWCS is the TCP server and the SmartFlow is TCP client.
- SCADA - AirflowWCS is the TCP Server, and the SCADA are TCP Clients. -->  

## Purpose

This document defines the message specification protocol for the communication between AirflowWCS and other devices.

![](/images/TechnicalGuides/Intro1.png)

<!--
## Standard

All software produced for the project will confirm to the Daifuku coding and design standards. These documents are located in the company SharePoint system.
-->
