@echo off
rem Compiles the sources and packages FeeReport.jar. Needs a JDK (javac, jar) on the PATH.
cd /d "%~dp0"
if not exist out mkdir out
javac -source 8 -target 8 -Xlint:-options -encoding UTF-8 -cp "lib\*" -d out src\feereport\*.java || exit /b 1
jar cfm FeeReport.jar manifest.txt -C out . || exit /b 1
echo Built FeeReport.jar
