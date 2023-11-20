@echo off
:: Solicitar privilégios de administrador
echo Requesting admin rights...
cd /d %~dp0
powershell -Command "Start-Process 'java' -ArgumentList '-jar teste-looca.jar' -Verb RunAs"
