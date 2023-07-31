@echo off
sqlcmd -U asrs -P asrs -S localhost\SQLServerDev -i C:\projects\wrxj\base\tsql\scripts\DeleteAllData.tsql
