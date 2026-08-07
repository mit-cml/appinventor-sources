This ZIP file contains the MIT App Inventor Standalone code

Requirements

You will need the Java Runtime Environment version 7. This code may
work with Java 8, but it has not been tested. It should work with
Windows 7 (Windows 8 is untested), MacOS and Linux. We do our testing
under Linux.

Because of Java memory limitations on Windows, you may not be able to
support as many people on a Windows system as the same hardware could
support running Linux.

Installation Instructions

Unzip this file to the location of your choice. Copy the
“appinventor.ini.sample” file to “appinventor.ini” and edit it for
your installation. In particular you need to set the “storage”
location to a directory (it will be created if it doesn’t exist) where
you want your projects stored. You will want to backup the storage
directory from time to time.

Your storage directory can be a sub-directory of the location where
you unpack the stand alone ZIP file, but we recommend that it be
someplace else, just to make sure that an upgrade won’t accidentally
delete it or otherwise damage it.

Accounts are setup by sending an email with a password reset
link. Therefore you need to set your mailserver information in the
appinventor.ini file. The “host” variable should point to the name of
your mail server. Similarly the “user” and “password” variables should
contain the account name and password to login with. Some systems
require that you use TLS to send mail. If this is the case set the
“starttls” variable to “true.”

The “port” variable points to the port that your mail server accepts
mail “submission.” On most systems this is 587, but some may use the
SMTP port which is 25.

Once your appinventor.ini file is setup, you are ready to start the
system. From a command prompt in the directory where you unzipped this
file, issue the command “java -jar starter.jar” (depending on how your
system is setup, you may have to use the full path to the java binary).

If sucessful, the App Inventor system will start, both the webservice
and a buildserver for packaging apps. Typeing control-C will shutdown
the servers.

NOTE: The server will listen on port 8888. So you cannot to it by
going to http://<yourservername>:8888.

You can use a webserver such as Apache or Nginx to proxy port 80 to
port 8888 if desired. You can also install MIT App Inventor in a
“docker” container and forward the host port 80 to the containers port
8888. [1] Note: We may distribute a Docker Image at some point.

Updating/Upgrading

When you receive a new version of the stand alone ZIP file, you can
safely over-write the current directly. It will not touch the
appinventor.ini file which you configured. If changes are required to
it, we will indicate it in the release notes.

Registration and Expiration

It is important that you run the most up-to-date version of App
Inventor. This ensures that people can download projects from our
public service and upload them to your server and have them work as
expected. It also ensures that people who obtain the MIT AI2 Companion
from the Play Store will be running a compatible version.

To ensure an up-to-date system, we have implemented an expiration date
in the system. This date will be displayed when you run the Starter
program. People will receive a warning that a version is about to
expire 10 days before the actual expiration. After the expiration
date, MIT App Inventor will not load.

In order to help us understand how many people are running a stand
alone server, we have also implemented a registration system. The
first time after you install MIT App Inventor stand along for the
first time you will be required to register your copy.

We recommend that immediately after installing MIT App Inventor Stand
Alone, you create your own account on the system by pointing your
browser at your newly installed server and requesting a password
change link. If you have properly setup your mail server, you should
receive an e-mail message with the password reset link.

Clicking the password link in the email message will take you to your
server where you can establish your password and login to the server.

On a newly installed server, you should receive a registration dialog
box. This box will direct you to go to:

   http://register.appinventor.mit.edu.

It will also provide a 7-8 digit “site code.” Go to the registration
site (URL above) and enter this site code along with your name and
email address. We will also ask you for the likely number of end-users
on your server. This number is not currently enforced, but might be in
the future.

If all goes well, the registration server will send you an email with
your “Auth Code”. You then enter this code into the dialog box from
your server. If correct, the dialog will dismiss and your site will be
registered.

[1] See http://docker.com for more information on Docker.
