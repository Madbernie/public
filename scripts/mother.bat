@echo off
set DIR=%~dp0
"%DIR%..\gradlew" :applications:mother:run %*
