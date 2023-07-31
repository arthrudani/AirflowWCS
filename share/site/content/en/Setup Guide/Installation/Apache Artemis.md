---
title: "Apache Artemis"
linkTitle: "Apache Artemis"
date: 2022-10-18T08:49:09+13:00
weight: 2
type: docs
simple_list: true
---

# Apache Artemis

## Apache-artemis-2.6.2
- In order to run Artemis as a service you have to install .NET Framework 3.5 Features (Turn windows feture on/off)
- Download apache-artemis-2.6.2 from here:  [link](./apache-artemis-2.6.2.zip)
- Unzip to C:\Tools\apache-artemis-2.6.2
- Delete C:\Tools\apache-artemis-2.6.2\WarehouseRx folder
- Open the CMD as Administrator and run the following commands from the CMD
- Run the ("C:\Tools\apache-artemis-2.6.2\WRx-Artemis.bat") 
- To install the Artemis as a service run ("C:\Tools\apache-artemis-2.6.2\WarehouseRx\bin\artemis-service.exe" install) 
- To start the service run ("C:\Tools\apache-artemis-2.6.2\WarehouseRx\bin\artemis-service.exe" start)
- Check if port 61616 is open

If you need to stop or uninstall the service:
- "C:\Tools\apache-artemis-2.6.2\WarehouseRx\bin\artemis-service.exe" stop
- "C:\Tools\apache-artemis-2.6.2\WarehouseRx\bin\artemis-service.exe" uninstall

