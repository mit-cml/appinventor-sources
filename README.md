# Welcome to MIT App Inventor

## Introduction

Learn more about [MIT App Inventor](http://appinventor.mit.edu).

This code is designed to be run in Google's App Engine. MIT runs a
public instance that all are welcome to use to build App Inventor
Applications. You do not need to compile or use this code if you wish
to build MIT App Inventor applications.

We provide this code for reference and for experienced people who wish
to operate their own App Inventor instance and/or contribute to the project.

This code is tested and known to work with Java 8.

## Contributors

The best way to go about integrating changes in App Inventor is to start a conversation in the [Open Source forum](https://community.appinventor.mit.edu/c/open-source-development/10) about whatever you intend to change or add.

We use ***very brief and informal*** design documents with descriptions of the proposed changes and screenshots of how the functionality would look like and behave, in order to gather as much feedback from the community, as early as possible. We generally use shared Google docs for this (with permissions to add comments), but any format that is accessible from a web browser (and allows comments) would do.

If you have skipped this step and have gone ahead and made your changes already, feel free to open a pull request, but don't be too surprised if we ask you to go back and document it in a design document. Remember that the main goal of doing this is ***to gather as much feedback, as early as possible***. We will also possibly ask you to put an instance with your changes on [appspot](http://appspot.com), and provide a modified Companion app (if that applies) so that reviewers can play with the changes before looking at the source.

Check out our open source [site](http://appinventor.mit.edu/appinventor-sources/) to find a lot more information about the project and how to contribute to it.

## Setup Instructions (Vagrant)

The easiest way to get a development environment up and running is to use the provided Vagrantfile. Install [Vagrant](https://vagrantup.com) and open a terminal in the root directory of this repository. Run the following commands

```bash
vagrant plugin install vagrant-vbguest  # optionally for virtualbox users, and only once
vagrant up                              # initializes the VM
```

It may take a few minutes for Vagrant to initialize as it will pull down a virtual machine image from the Internet and configure it with all of the App Inventor dependencies. Subsequent start-ups will be faster. Next, enter the virtual machine by running:

```bash
vagrant ssh
```

This should open up a terminal within the virtual machine in the directory `/vagrant/appinventor`. This directory is the same as the `appinventor` directory in this repository, shared between your host machine and the virtual machine. Any changes made on one side will be visible in the other. This allows you to edit files on your host machine with your preferred editor, while keeping the build environment relegated to the virtual machine. To build App Inventor, you may now run:

```bash
ant
```

and to run App Inventor:

```bash
start_appinventor
```

Press Ctrl+C to quit the server. Enter exit at the prompt to leave the virtual machine. To reclaim resources when you are not actively developing, you can run `vagrant halt` to stop the virtual machine. To completely remove the virtual machine, run `vagrant destroy`. If you destroy the VM, you will need to start these instructions from the top.

Note 1: For macOS users, if you are using VirtualBox and get any error while initializing the VM it may be due to security restrictions in System Preferences, consider reading [this](https://medium.com/@Aenon/mac-virtualbox-kernel-driver-error-df39e7e10cd8) article. 

Note 2: If it seems like none of the dependencies are installed in the VM, run ```vagrant provision```.

For better performance, consider using the manual instructions.

## Setup Instructions (iOS Support)

Building MIT App Inventor Companion for iOS requires an Apple
Macintosh computer running macOS 12 or later with Xcode 14 or later
installed. While earlier versions may work we provide no support for
building on versions below the ones stated. To install on a device,
you **must** have a valid Apple Developer account license and have
added the relevant mobile provisioning profiles from the Developer
portal to your Xcode organizer (see Apple's website on instructions on
how to do this).

To build the MIT App Inventor companion, you will need to create a
file called AICompanionApp.xcconfig in the components-ios directory
that sets your development team. The easiest way to do this is to copy
the AICompanionApp.xcconfig.sample file and edit it. Alternatively,
create a file with the following line:

```conf
DEVELOPMENT_TEAM = ID
```

where ID is the development team ID shown in the Apple Developer
Portal. This ID is unique to your developer account (individual or
organization).

## Setup Instructions (Manual)

This is a quick guide to get started with the sources. More detailed instructions can be found [here](https://docs.google.com/document/pub?id=1Xc9yt02x3BRoq5m1PJHBr81OOv69rEBy8LVG_84j9jc), a slide show can be seen [here](http://josmas.github.io/contributingToAppInventor2/#/), and all the [documentation](http://appinventor.mit.edu/appinventor-sources/#documentation) for the project is available in our [site](http://appinventor.mit.edu/appinventor-sources/).

### Dependencies

You will need a full Java JDK (version 8, OpenJDK preferred; JRE is not enough) and Python to compile and run the servers.

You will also need a copy of the [Google Cloud SDK](https://cloud.google.com/appengine/docs/standard/java/download) for Java and [ant](http://ant.apache.org/).

If you want to make changes to the source, you are going to need to run an automated test suite, and for that you will also need [phantomjs](http://phantomjs.org/). Have a look at the testing section for more information.

Note 1: If you are working on a 64-bit linux system, you need to install 32-bit version of: glibc(to get a 32-bit version of ld-linux.so), zlib and libstdc++.

If you are on a Debian-based distribution(Ubuntu), use:

    $ sudo apt-get install libc6:i386 zlib1g:i386 libstdc++6:i386

If you are on an RPM-based distribution(Fedora), use:

    $ sudo dnf install glibc.i686 zlib.i686 libstdc++.i686

Note 2: Certain Java 8 features, such as lambda expressions, are not supported on Android, so please don't use them in your changes to the source code.

### Forking or cloning

Consider ***forking*** the project if you want to make changes to the sources. If you simply want to run it locally, you can simply ***clone*** it.

#### Forking

If you decide to fork, follow the [instructions](https://help.github.com/articles/fork-a-repo) given by github. After that you can clone your own copy of the sources with:

    $ git clone https://github.com/YOUR_USER_NAME/appinventor-sources.git

Make sure you change *YOUR_USER_NAME* to your user name.

Configuring a remote pointing to this repository is also a good idea if you are forking:

    $ cd appinventor-sources
    $ git remote add upstream https://github.com/mit-cml/appinventor-sources.git

Finally, you will also have to make sure that you are ignoring files that need ignoring:

    $ cp sample-.gitignore .gitignore

### Checkout dependencies

App Inventor uses Blockly, the web-based visual programming editor from Google, as a core part of its editor. Blockly core is made available to App Inventor as a git submodule. The first time after forking or cloning the repository, you will need to perform the following commands:

    $ git submodule update --init

For developers who will be working on Blockly within the context of App Inventor, the preferred checkout procedure is to perform a `git submodule init`, edit the `.git/config` file to use the read/write SSH URL for [MIT CML's Blockly fork](https://github.com/mit-cml/blockly) instead of the public read-only HTTPS URL assumed by default (to support pushing changes). After changing `.git/config`, a `git submodule update` will pull the repository.

If you need to switch back to a branch that does contains the Blockly and Closure Library sources in the tree, you will need to run the command:

    $ git submodule deinit --all

to clear out the submodules ___before switching branches___. When switching back, you will need to repeat the initialization and update procedure above.

### Troubleshooting common installation issues

Run this command to run a self-diagnosis of your environment. This command tries to figure out common installation issues and offers you a solution to fix them yourself. Make sure this passes all the checks before you proceed further.

#### Linux and macOS

```bash
./buildtools doctor
```

#### Windows

```bash
buildtools doctor
```

## Compiling

Before compiling the code, an [auth key](https://docs.google.com/document/pub?id=1Xc9yt02x3BRoq5m1PJHBr81OOv69rEBy8LVG_84j9jc#h.yikyg2e1rfut) is needed. You can create one by running the following commands:

    $ cd appinventor
    $ ant MakeAuthKey

Once the key is in place, type the following to compile (from the appinventor folder):

    $ ant

You will see a lot of stuff in the terminal and after a few minutes (it can take from 2 to 10 minutes, depending on your machine specs) you should see a message saying something like *Build Successful*.

### Notes on compiling for iOS

If you are compiling on a Mac and **are not** interested in building
the companion for iOS, you must set the property `skip.ios` to `true`,
for example:

```bash
ant -Dskip.ios=true
```

iOS builds will automatically be skipped on other operating systems.

We generally use Xcode for iOS development. Open the
AppInventor.xcworkspace file to view the Xcode workspace. This
workspace includes three projects:

* SchemeKit: A Scheme implementation for iOS built on Picrin with
  additions to support foreign function calls to Objective-C and
  Swift. This also implements some basic types from App Inventor
  including YailList and YailDictionary.
* AIComponentKit: App Inventor component implementations, mostly
  written in Swift.
* AICompanionApp: The App Inventor companion written in Swift.

In Xcode you can run the AICompanionApp on your device by selecting
the AICompanionApp target's Debug scheme and pressing the Run button.

For more information about iOS support, please see
[README.ios.md](README.ios.md).

## Running the Server(s)

There are two servers in App Inventor, the main server that deals with project information, and the build server that creates apk files. More detailed information can be found in the [App Inventor Developer Overview](https://docs.google.com/document/d/1hIvAtbNx-eiIJcTA2LLPQOawctiGIpnnt0AvfgnKBok/pub) document.

### Running the main server

    $ your-google-cloud-SDK-folder/bin/java_dev_appserver.sh
            --port=8888 --address=0.0.0.0 appengine/build/war/

Make sure you change *your-google-cloud-SDK-folder* to wherever in your hard drive you have placed the Google Cloud SDK.

### Running the build server

The build server can be run from the terminal by typing:

    $ cd appinventor/buildserver
    $ ant RunLocalBuildServer

Note that you will only need to run the build server if you are going to build an app as an apk. You can do all the layout and programming without having the build server running, but you will need it to download the apk.

### Accessing your local server

You should now be up and running; you can test this by pointing your browser to:

    http://localhost:8888

Before entering or scanning the QR code in the Companion, check the box labeled "Use Legacy Connection".

### Running tests

The automated tests depend on [Phantomjs](http://phantomjs.org/). Make sure you install it and add it to your path. After that, you can run all tests by typing the following in a terminal window:

    $ ant tests

### Building Release Code

Release builds with optimizations turned on for the web components of the system can be done by passing `-Drelease=true` to `ant`, e.g.:

```
ant -Drelease=true noplay
```

The release configuration sets the following additional options:

- Blockly Editor is compiled with SIMPLE optimizations (instead of RAW)
- App Engine YaClient module is compiled without `<collapse-all-properties/>` to create per-language/browser builds
- App Engine YaClient module is compiled with optimization tuned to 9 and with 8 threads

### Hot-reloading GWT code with 'Super Dev Mode'

1. Run `ant devmode`
2. [Run the main server](#running-the-main-server).
3. Open http://localhost:9876 (*GWT CodeServer*) and drag the two bookmarklets (*Dev Mode On & Off*) to the bookmarks bar.
4. Open http://localhost:8888 (*App Engine server*)
5. To see changes "live":
   1. Save your changes in file.
   2. Click on the *"Dev Mode On"* bookmarklet.
   3. A popup will be shown with a button to compile `ode` module.
   4. Press that button to compile. (That button is actually a bookmarklet. So you can drag this button to the bookmarks bar as well. This will come handy for subsequent compilations)
   5. After that, *GWT CodeServer* will compile the module incrementally.
   6. Refresh the page and that's it! The changes are live.

Logs can be found at http://localhost:9876/log/ode and SourceMaps at http://localhost:9876/sourcemaps/ode

## Need Help?

Join [our community](https://community.appinventor.mit.edu/).
