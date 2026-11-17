#!/usr/bin/env bash
# Compile and launch the Recycling & Waste Tracker.
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.recycletracker.ui.MainApp