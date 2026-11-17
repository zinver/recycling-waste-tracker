@echo off
rem Compile and launch the Recycling & Waste Tracker.
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
del sources.txt
java -cp out com.recycletracker.ui.MainApp