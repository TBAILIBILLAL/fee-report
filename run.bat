@echo off
rem Starts the program. The database file feereport.db is created in this folder on first run.
cd /d "%~dp0"
start "" javaw -jar FeeReport.jar
