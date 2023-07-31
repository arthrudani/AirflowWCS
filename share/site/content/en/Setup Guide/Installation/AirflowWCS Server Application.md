---
title: "AirflowWCS Server Application"
linkTitle: "AirflowWCS Server Application"
date: 2022-10-18T08:49:09+13:00
weight: 5
type: docs
simple_list: true
---

# AirflowWCS Server Application

## Where to find installers
- You can find the latest AirflowWCS installers (distribution-0.0.1-SNAPSHOT.zip) at `\\bcs.local\Software Builds\Installer\WCS`


## A Fresh Installation

### Copy the server application installer
- Create a new folder, `C:\daifuku\AirflowWCS`.
- unzip the installer and copy the content of folder distribution-0.0.1-SNAPSHOT.zip->0.0.1-SNAPSHOT\server\wrxj-ebs-custom-0.0.1-SNAPSHOT.zip\0.0.1-SNAPSHOT to the above folder
- The `C:\daifuku\AirflowWCS` should now have the following folders ( bin, configs, lib, logs, and tools)
- If the database is installed in different PC or VM you need to change the Instance Name propetry from `localhost` to IP address where the database is installed in this file `C:\daifuku\AirflowWCS\configs\wrxj.properties`  **NOTE: Please don't change the username and password** 
- Open `C:\daifuku\AirflowWCS\tools\yajsw\conf\wrapper.conf` and make sure the following values are set correctly for the current user. 
  - `wrapper.app.account=UserName`
  - `wrapper.app.password=PassWordOfUser`
- Opend the CMD as administrator 
- Run `C:\daifuku\AirflowWCS\tools\yajsw\bat\installService.bat` to install
- Run `C:\daifuku\AirflowWCS\tools\yajsw\bat\startService.bat`   to start

### To Check 
- Open the Windows Service Management and check if AirflowWCS Service is running
- Check if port 5882 is open
- Check if logs are created under the folder `C:\daifuku\AirflowWCS\logs' 

## Installing updated version
- Make a backup
- Opend the CMD as administrator
- Run `C:\daifuku\AirflowWCS\tools\yajsw\bat\stop_service.bat`
- Make sure the srevice is stopped and port 5882 is closed. 
- Run `C:\daifuku\AirflowWCS\tools\yajsw\bat\uninstallService.bat` to remove the service
- Follow the fresh installation instruction

