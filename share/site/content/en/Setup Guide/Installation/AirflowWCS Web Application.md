---
title: "AirflowWCS Web Application"
linkTitle: "AirflowWCS Web Application"
date: 2022-10-18T08:49:18+13:00
weight: 6
type: docs
simple_list: true
---

# AirflowWCS Web Application

## Installation to Tomcat's webapps directory
- Stop the Apache Tomcat service from the Windows Service Management
- If this is NOT a fresh installation, make a backup of the existing AirflowWCS Web installation
- If this is NOT a fresh installation, delete the existing folder C:\Tools\apache-tomcat-9.0.26\webapps\AirflowWCS and file C:\Tools\apache-tomcat-9.0.26\webapps\airflowwcs.war
- Copy the file airflowwcs.war from installation zip file distribution-0.0.1-SNAPSHOT.zip->0.0.1-SNAPSHOT\webapp\airflowwcs-web-0.0.1-SNAPSHOT.zip\0.0.1-SNAPSHOT to the C:\Tools\apache-tomcat-9.0.26\webapps folder
- If the AirflowWCS database is installed in different server you need to update the connection string (See the section below for more info ).
- Start the  Apache Tomcat service from the Windows Service Management
- Visit `http://localhost:8080/airflowwcs` using web browser to check if AirflowWCS Web Application is running

## MS SQL DB connection configuration
- The following is the default configuration for MS SQL DB connection specified in `tomcat_installation_directory\webapps\airflowwcs\WEB-INF\classes\hibernate.cfg.xml`.
  - Instance name in this : `<property name="hibernate.connection.url">jdbc:sqlserver://localhost:1433;instanceName=localhost;databaseName=AirflowWCS</property>`
- The following is the default configuration for MS SQL DB connection specified in `tomcat_installation_directory\webapps\airflowwcs\WEB-INF\classes\wrxj.properties`.
  - Instance name in this : `SQLServer.url=jdbc:sqlserver://localhost:1433;instanceName=localhost;databaseName=AirflowWCS`
  
  
 **NOTE: Please don't change the username and password**