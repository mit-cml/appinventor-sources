@echo off
rem Copyright 2009 Google Inc. All Rights Reserved.

rem Launches the AppCfg utility, which allows Google App Engine
rem developers to deploy their application to the cloud.

java -Xmx1100m -cp "%~dp0\..\lib\appengine-tools-api.jar" com.google.appengine.tools.admin.AppCfg %*
