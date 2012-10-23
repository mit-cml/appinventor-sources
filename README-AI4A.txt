App Inventor for All

App Inventor for All (ai4a) is a version of the App Inventor server. This version 
runs on Windows in addition to Linux and Mac. 

You can install and run ai4a V1.4 using the same setup as the MIT servers. You do need to
se JAVA_HOME to the home of your Java JDK installation. The server runs well locally without
internet. You can get the jar files and some simple scripts on sourceforge:
  http://sourceforge.net/projects/ai4a-configs/
The ai4a servers have been used by all the community, in fact various configurations of the
ai4a servers have been downloaded over 1500 times. The most recent, version (V1.4), has been
downloaded 180 times so far.

Information about the various ai4a servers is available on the ai4a group:
  https://groups.google.com/forum/?fromgroups#!forum/ai4a
You can find out what is going on with the ai4a servers and more in the group.

Gary Frederick
ai4a/Jefferson Software

Changes:

V1.4 October 24, 2012

server/project/youngandroid/YoungAndroidProjectService.java
  set limit on source zip to 50 Mb

blockslib/src/openblocks/yacodeblocks/YABlockCompiler.java
  add Windows support

buildserver/build.xml
  ${lib.dir}/android/tools support Windows aapt.exe

buildserver/src/com/google/appinventor/buildserver/Compiler.java
  add Windows suppport
  get jarsigner location from environment variable
  left previous code commented out to compare - remove when appropriate

lib/android/tools/windows/aapt
  deleted (did not work) and aapt.exe for Windows added
