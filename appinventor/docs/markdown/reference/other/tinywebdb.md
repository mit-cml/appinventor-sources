---
title: Creating a Custom TinyWebDB Service
layout: documentation
---

TinyWebDB is an App Inventor component that allows you to store data persistently in a database on the web. Because the data is stored on the web instead of a particular phone, TinyWebDB can be used to facilitate communication between phones and apps (e.g., multi-player games).

By default, the TinyWebDB component stores data on a test service provided by App Inventor, http://tinywebdb.appinventor.mit.edu/ . This service is helpful for testing, but it is shared by all App Inventor users, and it has a limit of 1000 entries. If you use it, your data will be overwritten eventually.

For most apps you write, you'll want to create a custom web service that isn't shared with other App Inventor apps and programmers. You need not be a programmer to do so-- just follow the instructions below and you'll have your own service within minutes.

To create your own web service, follow these instructions:

* Download App Engine for Python at http://code.google.com/appengine/ . After installing it, run the GoogleAppEngineLauncher by clicking its icon.
* Download this [sample code](tinywebdbassets/customtinywebdb.zip). It is a zip file containg the source code for your custom tinywebdb web service
* Unzip the downloaded zip file. It will create a folder named customtinywebdb . You can rename it if you want.
* In the GoogleAppEngineLauncher, choose File | Add Existing Application . Browse to set the Path to the customtinywebdb folder you just unzipped. Then click the Run button. This will launch a test web service that runs on your local machine.
* You can test the service by opening a browser and entering "localhost:8080" as the URL. You'll see the web page interface to your web service. The end-goal of this service is to communicate with a mobile app created with App Inventor. But the service provides a web page interface to the service to help programmers with debugging. You can invoke the get and store operations by hand, view the existing entries, and also delete individual entries
* Your app is not yet on the web, and thus not yet accessible to an App Inventor app. To get it there, you need to upload it to Google's App Engine servers.
  * In the GoogleAppEngineLauncher, choose Dashboard . Enter your Google account information and you'll be taken to an App Engine dashboard.
  * Choose Create an Application . You'll need to specify a globally unique Application Identifier. Remember the Application identifier as you'll need it later. Provide a name to your app and click Create Application to submit. If your Identifier was unique, you now have a new, empty app on Google's servers.
  * Open a text editor on your local computer and open the file app.yaml within the customtinywebdb folder you unzipped. Modify the first line so that the application matches the application identifier you set at Google.
  * In GoogleAppEngineLauncher, choose Deploy and follow the steps for deploying your app.
* Test to see if your app is running on the web. In a browser, enter myapp.appspot.com, only substitute your application identifier for "myapp". The app should look the same as when you ran it on the local test server. Only now, it's on the web and you can access it from your App Inventor for Android app.
Your App Inventor apps can store and retrieve data using your new service. Just do the following:

Drag in a TinyWebDB component into the Component Designer.
Modify the ServiceURL property from the default http://tinywebdb.appinventor.mit.edu/ to your web service.
Any StoreValue operations (blocks) will store data at your service, and any GetValue operations will retrieve from your service.
