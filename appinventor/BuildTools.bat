@echo off
color 0b
title Build Tools
set var=%cd%
echo.
echo   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
echo   *                                                                       *
echo   *                              BUILD TOOLS                              *
echo   *                       by Pavitra + Barreeeiroo                        *
echo   *                                                                       *
echo   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
echo.
echo   - -  Path Variables  - - - - - - - - - - - - - - - - - - - - - - - - - -
echo    AppInventor: %var%
echo    AppEngine: %AppEnginePath%
echo    JDK: %JAVA_HOME%
echo    Ant: %ANT_HOME%
echo.
pause
goto menu

:menu
title Build Tools
set var=%cd%
cls
echo                                    MENU
echo  - -  What you want to do?  - - - - - - - - - - - - - - - - - - - - - - - -
echo.
echo    1.Clean Build
echo    2.Make Auth Key
echo    3.Build App Inventor
echo    4.Build Without Companion
echo    5.Run Local Server
echo    6.Run Super Dev Mode
echo    7.Run Build Server
echo    9.Build Companion App
echo    10.Build Extension
echo    11.Run PhantomJS Tests
echo    12.Build Merger App
echo    13.Export Strings
echo    14.Logcat
echo    15.Count Lines
echo    16.Generate OdeMessages
echo    17.Generate Component Docs
echo.
echo    20.Force Build Companion App (this can take some time)
echo    21.Build and Run Servers (4,5,7)
echo.
echo  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
set /p choice=Enter your choice:
if %choice% == 1 goto clean
if %choice% == 2 goto makeauthkey
if %choice% == 3 goto build
if %choice% == 4 goto buildnoplay
if %choice% == 5 goto localserver
if %choice% == 6 goto sdm
if %choice% == 7 goto buildserver
if %choice% == 9 goto companion
if %choice% == 10 goto extension
if %choice% == 11 goto tests
if %choice% == 12 goto merger
if %choice% == 13 goto transifex
if %choice% == 14 goto logcat
if %choice% == 15 goto cloc
if %choice% == 16 goto genOdeMsg
if %choice% == 17 goto genCompDocs
if %choice% == 20 goto beforeForceCompanion
if %choice% == 21 goto buildRunServers

cls
echo Invalid Input!
pause
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
title Building...
cls
call ant noplay
echo.
pause
goto menu

:localserver
title Running Local Server...
cls
start http://localhost:8888
start %AppEnginePath%/bin/dev_appserver.cmd --port=8888 --address=0.0.0.0 appengine/build/war/
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

:companion
title Building Companion...
cls
call ant PlayApp
echo.
if /i "%ERRORLEVEL%" equ "0" (
    echo The companion is generated at:
    echo build/buildserver/Kodular.apk
    echo.
)
pause
goto menu

:beforeForceCompanion
title Preparing for Building Companion...
cls
call ant clean
goto forceCompanion

:forceCompanion
title Building Companion... This can take some time
cls
call ant PlayApp || goto forceCompanion
echo.
if /i "%ERRORLEVEL%" equ "0" (
    echo The companion is generated at:
    echo build/buildserver/Kodular.apk
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
    echo components/build/extensions
    echo.
)
pause
goto menu

:buildRunServers
title Building and Running Servers...
cls
call ant noplay
start %AppEnginePath%/bin/dev_appserver.cmd --port=8888 --address=0.0.0.0 appengine/build/war/
start http://localhost:8888
start ant RunLocalBuildServer
echo.
pause
goto menu

:tests
title Running Tests...
cls
call ant tests
echo.
echo Tests were been evaluated.
echo.
pause
goto menu

:merger
title Building Merger...
cls
call ant AIMergerApp
echo.
pause
goto menu

:transifex
title Exporting Strings...
cls
call cd misc/translator && python script.py
echo.
pause
goto menu

:logcat
title Running Logcat...
cls
call adb logcat -c && adb logcat > logcat.txt
echo.
pause
goto menu

:cloc
title Counting lines...
cls
call "misc/cloc/cloc.exe" ../
echo.
pause
goto menu

:genOdeMsg
title Generating OdeMessages...
cls
echo Running noplay to get the build log...
echo.
call ant > log.txt 2>&nul
echo.
echo log.txt file created. Now extracting messages...
call powershell -File misc/GenOdeMsg.ps1 > odemsg.txt
echo.
echo odemsg.txt file generated.
echo.
pause
goto menu

:genCompDocs
title Generating Components Docs...
cls
cd components
call ant ComponentDocumentation
echo.
echo build/components/component_docs.json file created.
echo.
pause
cd ..
goto menu
