@echo off
set batdir=%~dp0
echo %batdir%
java -jar %batdir%/ar_telemetry_converter.jar %*