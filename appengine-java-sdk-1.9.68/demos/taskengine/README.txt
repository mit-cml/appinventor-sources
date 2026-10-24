Google App Engine Java Runtime SDK - TaskEngine Demo


A mobile Time management/Task list app, targeted specifically for iPhone and 
Android browsers.

The front end is an exercise in writing a light weight mobile app with some 
tricks to minimize code size and round trips (DOM programming, resource 
bundling, and style injection).

This app uses GWT RPC, authentication and user accounts using UserService, and 
persistence using JDO.

You can also checkout more recent builds from Task Engine's Google Code site at:
http://code.google.com/p/taskengine


How to build Task Engine:

1. Install Apache ant.
2. Download Java Appengine SDK.
3. Download GWT 2.0 or later.
4. Add the correct path information for the Appengine SDK, and GWT 2.0 
   to 'taskengine/build.properties'.
5. Drop to the command line, and in the taskengine directory type:
   'ant'.


To run TaskEngine locally, type:
'ant runserver'
