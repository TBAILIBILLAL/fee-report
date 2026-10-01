@echo off
rem Compiles the program and the tests, then runs the database layer checks on a temporary database.
cd /d "%~dp0"
if not exist out-test mkdir out-test
javac -source 8 -target 8 -Xlint:-options -encoding UTF-8 -cp "lib\*" -d out-test src\feereport\*.java test\feereport\*.java || exit /b 1
java -cp "out-test;lib\*" feereport.DataLayerTest
