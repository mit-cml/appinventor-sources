@echo off
rem Copyright 2013 Google Inc. All Rights Reserved.

echo %CmdCmdLine% | %WINDIR%\System32\find /i "%~0" >nul
SET INTERACTIVE=%ERRORLEVEL%

echo Welcome to the Google Cloud SDK!

IF "%CLOUDSDK_COMPONENT_MANAGER_SNAPSHOT_URL%"=="" (
  GOTO SETENABLEDELAYED
  ) ELSE (
    echo WARNING: You have set the environment variable
    echo CLOUDSDK_COMPONENT_MANAGER_SNAPSHOT_URL to
    echo %CLOUDSDK_COMPONENT_MANAGER_SNAPSHOT_URL%. This may cause installation
    echo to fail. If installation fails, run "SET
    echo CLOUDSDK_COMPONENT_MANAGER_SNAPSHOT_URL=" and try again.
  )

:SETENABLEDELAYED
SETLOCAL EnableDelayedExpansion

rem install.bat lives in the root of the Cloud SDK installation directory.
SET CLOUDSDK_ROOT_DIR=%~dp0

IF "%CLOUDSDK_PYTHON%"=="" (
  SET BUNDLED_PYTHON=!CLOUDSDK_ROOT_DIR!\platform\bundledpython\python.exe
  IF EXIST !BUNDLED_PYTHON! (
    SET CLOUDSDK_PYTHON=!BUNDLED_PYTHON!
  ) ELSE (
    FOR %%i in (python.exe) do (SET CLOUDSDK_PYTHON=%%~$PATH:i)
  )
)
IF "%CLOUDSDK_PYTHON%"=="" (
  echo.
  echo To use the Google Cloud SDK, you must have Python installed and on your PATH.
  echo As an alternative, you may also set the CLOUDSDK_PYTHON environment variable
  echo to the location of your Python executable.
  "%COMSPEC%" /C exit 1
) ELSE (
  rem copy_bundled_python.py will make a copy of the Python interpreter if it's
  rem bundled in the Cloud SDK installation and report the location of the new
  rem interpreter. We want to use this copy to install the Cloud SDK, since the
  rem bundled copy can't modify itself.
  FOR /F "delims=" %%i in (
    '""%COMSPEC%" /C ""!CLOUDSDK_PYTHON!" "!CLOUDSDK_ROOT_DIR!\lib\gcloud.py""" components copy-bundled-python'
  ) DO (
    SET CLOUDSDK_PYTHON=%%i
  )
  "%COMSPEC%" /C ""!CLOUDSDK_PYTHON!" "!CLOUDSDK_ROOT_DIR!\bin\bootstrapping\install.py" %*"
)

IF _%INTERACTIVE%_==_0_ (
  IF _%CLOUDSDK_CORE_DISABLE_PROMPTS%_==__ (
    echo Google Cloud SDK installer will now exit.
    PAUSE
  )
)

"%COMSPEC%" /C exit %ERRORLEVEL%
