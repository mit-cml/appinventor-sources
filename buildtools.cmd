@echo off

:: Run the script inside the appinventor dir
cd appinventor

if /i "%1" equ "doctor" goto doctor

:menu
title Build Tools for App Inventor
cls
echo                                    MENU
echo  - - What do you want to do? - - - - - - - - - - - - - - - - - - - - - - -
echo.
echo    1.Clean Build
echo    2.Make Auth Key
echo.
echo    3.Build App Inventor
echo    4.Build Without Companion
echo    5.Build Companion App
echo    6.Build Extension
echo.
echo    7.Run Local Server
echo    8.Run Super Dev Mode
echo    9.Run Build Server
echo    A.Run Tests
echo.
echo    B.Doctor
echo.
echo    0.Exit
echo.
echo  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
set /p choice=Enter your choice:
if %choice% == 1 goto clean
if %choice% == 2 goto makeauthkey
if %choice% == 3 goto build
if %choice% == 4 goto buildnoplay
if %choice% == 5 goto companion
if %choice% == 6 goto extension
if %choice% == 7 goto localserver
if %choice% == 8 goto sdm
if %choice% == 9 goto buildserver
if /i %choice% == A goto tests
if /i %choice% == B goto doctor
if %choice% == 0 goto eof
goto menu

:clean
title Cleaning Build...
cls
call ant clean
echo.
pause
goto menu

:makeauthkey
title Making Auth Key...
cls
call ant MakeAuthKey
echo.
pause
goto menu

:build
title Building...
cls
call ant all
echo.
pause
goto menu

:buildnoplay
title Building without companion...
cls
call ant noplay
echo.
pause
goto menu

:companion
title Building Companion...
cls
call ant PlayApp
echo.
if /i "%ERRORLEVEL%" equ "0" (
    echo The companion is generated at:
    echo %cd%/appinventor/build/buildserver/MIT AI2 Companion.apk
    echo.
)
pause
goto menu

:extension
title Building Extension...
cls
call ant extensions
echo.
if /i "%ERRORLEVEL%" equ "0" (
    echo The extension is generated at:
    echo %cd%/appinventor/components/build/extensions
    echo.
)
pause
goto menu

:localserver
title Running Local Server...
cls
start java_dev_appserver --address=0.0.0.0 --port=8888 appengine/build/war/
:: wait for 10 seconds for app engine server to start
timeout 10
start http://localhost:8888
echo.
pause
goto menu

:sdm
title Running Super Dev Mode...
cls
start http://localhost:9876
start ant devmode
echo.
pause
goto menu

:buildserver
title Running Build Server...
cls
start ant RunLocalBuildServer
echo.
pause
goto menu

:tests
title Running Tests...
cls
call ant tests
echo.
pause
goto menu

:doctor
title Doctor
cls
echo Diagnosing your system...
echo.
set pass=0
set fail=0
:: Check if Java is installed
where java > nul 2>&1
if /i "%ERRORLEVEL%" equ "0" (
    set /a pass=pass+1
    echo [PASS] Java is installed.
    :: Check Java version
    java -version 2>&1 | find "version ""11." > nul 2>&1
    if /i "%ERRORLEVEL%" equ "0" (
        set /a pass=pass+1
        echo [PASS] Required version of Java is installed.
    ) else (
        set /a fail=fail+1
        echo [FAIL] Required version of Java is not installed or not found on PATH.
        echo _______Please install Java 11 and try again.
    )
) else (
    set /a fail=fail+1
    echo [FAIL] Java is not installed or not found on PATH.
    echo _______Please install Java 11 and try again.
)
:: Check if git is installed
where git > nul 2>&1
if /i "%ERRORLEVEL%" equ "0" (
    set /a pass=pass+1
    echo [PASS] Git is installed.
    :: Check if git submodules are present
    git submodule status lib/blockly lib/closure-library > nul 2>&1
    if /i "%ERRORLEVEL%" equ "0" (
        set /a pass=pass+1
        echo [PASS] Git submodules are properly set up.
    ) else (
        set /a fail=fail+1
        echo [FAIL] Git submodules are not properly set up.
        echo _______Please run `git submodule update --init`
    )
) else (
    set /a fail=fail+1
    echo [FAIL] Git is not installed or not found on PATH.
    echo _______Please install Git and try again.
)
:: Check if gcloud is installed
where gcloud > nul 2>&1
if /i "%ERRORLEVEL%" equ "0" (
    set /a pass=pass+1
    echo [PASS] Google Cloud SDK is installed.
) else (
    set /a fail=fail+1
    echo [FAIL] Google Cloud SDK is not installed or not found on PATH.
    echo _______Download gcloud from here: https://cloud.google.com/appengine/docs/standard/java/download
)
echo.
echo Passed %pass% checks and %fail% failing
:: Exit if run using doctor command
if /i "%1" equ "doctor" goto eof
echo.
pause
goto menu

:eof
