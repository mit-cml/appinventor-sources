@echo off
rem Copyright 2012 Google Inc. All Rights Reserved.
rem
rem Tool for Google Cloud Endpoints.

setlocal

set SDK_LIB=%~dp0..\lib
set JAR_FILE1=%SDK_LIB%\opt\tools\appengine-local-endpoints\v1\appengine-local-endpoints.jar
set JAR_FILE2=%SDK_LIB%\opt\user\appengine-endpoints\v1\appengine-endpoints.jar

if NOT EXIST "%JAR_FILE1%" (
  echo %JAR_FILE1% not found
  exit /B 1
)

if NOT EXIST "%JAR_FILE2%" (
  echo "%JAR_FILE2%" not found
  exit /B 1
)

set CLASSPATH="%JAR_FILE1%";"%JAR_FILE2%";"%SDK_LIB%\shared\servlet-api.jar";"%SDK_LIB%\appengine-tools-api.jar";"%SDK_LIB%\opt\user\datanucleus\v1\jdo2-api-2.3-eb.jar";"%SDK_LIB%\user\*"

java -cp %CLASSPATH% com.google.api.server.spi.tools.EndpointsTool %*

endlocal
