@echo off
chcp 65001 >nul
cd /d "%~dp0"
set MAVEN_OPTS=-Xms256m -Xmx512m
"C:\Apache\apache-maven-3.9.12\bin\mvn.cmd" %*
