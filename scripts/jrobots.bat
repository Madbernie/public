@echo off
set DIR=%~dp0
"%DIR%..\gradlew" :applications:jrobots:run %*
