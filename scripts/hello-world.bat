@echo off
set DIR=%~dp0
"%DIR%..\gradlew" :applications:hello-world:run %*
