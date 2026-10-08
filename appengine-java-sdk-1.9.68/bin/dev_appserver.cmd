@echo off
rem Copyright 2009 Google Inc. All Rights Reserved.

rem Launches the Development AppServer.  This utility allows developers
rem to test a Google App Engine application on their local workstation.

java -cp "%~dp0\..\lib\appengine-tools-api.jar" ^
    com.google.appengine.tools.KickStart ^
       com.google.appengine.tools.development.DevAppServerMain %*

