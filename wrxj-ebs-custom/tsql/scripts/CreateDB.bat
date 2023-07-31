@echo off
mkdir C:\SQLServerDB\wrxj\log
mkdir C:\SQLServerDB\wrxj\data
sqlcmd -S UT-ADASGUPTA-L\SQLSERVERDEV -i C:\projects\wrxj\base\tsql\scripts\CreateDB.tsql
sqlcmd -S UT-ADASGUPTA-L\SQLSERVERDEV -i C:\projects\wrxj\base\tsql\scripts\CreateUser.tsql
