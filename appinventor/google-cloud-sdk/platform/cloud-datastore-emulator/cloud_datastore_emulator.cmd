@echo off
rem Copyright 2013 Google Inc. All Rights Reserved.
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.
rem
rem Command-line tool for interacting with Google Cloud Datastore.

setlocal

set GCD_DIR=%~dp0
set DATASTORE_JAR="%GCD_DIR%CloudDatastore.jar"

if NOT EXIST %DATASTORE_JAR% (
  echo %DATASTORE_JAR% not found
  exit /B 1
)

java -cp %DATASTORE_JAR% ^
    com.google.cloud.datastore.emulator.CloudDatastore %* 

endlocal
