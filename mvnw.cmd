@echo off
rem Maven Wrapper script for Windows
rem Downloads Maven if not present and runs it

setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6"
set "MAVEN_DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"

if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runMaven

echo Downloading Maven 3.9.6...
if not exist "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"

set "TMPFILE=%TEMP%\maven-download.zip"
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_DIST_URL%' -OutFile '%TMPFILE%' }"

if not exist "%TMPFILE%" (
    echo Error: Failed to download Maven
    exit /b 1
)

echo Extracting Maven...
powershell -Command "& { Expand-Archive -Path '%TMPFILE%' -DestinationPath '%MAVEN_HOME%\..' -Force }"
del /f "%TMPFILE%" 2>nul

echo Maven 3.9.6 installed to %MAVEN_HOME%

:runMaven
"%MAVEN_HOME%\bin\mvn.cmd" %*
