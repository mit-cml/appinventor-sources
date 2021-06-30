# App Inventor Developer Overview
:octicons-pencil-24:&nbsp;&nbsp;&nbsp;&nbsp;Ellen Spertus (spertus@google.com)   
:octicons-pencil-24:&nbsp;&nbsp;&nbsp;&nbsp;José Dominguez (josmasflores@gmail.com)
---

## Introduction
This document provides a high-level overview of the App Inventor source code, including the toolkit and libraries on which App Inventor depends, the different sub-projects within App Inventor, and information flow during the build process and execution.

There are differences between App Inventor and App Inventor 2, and these will be highlighted when appropriate.

## Background
MIT App Inventor is a rather large system divided in multiple projects, each of which relies on different open source technologies. Before showing what each of those projects are (section 3), we introduce some of the technologies used within them.

### Google Web Toolkit (GWT)
[Google Web Toolkit (GWT)](https://www.google.com/url?q=https://developers.google.com/web-toolkit/overview&sa=D&source=editors&ust=1618564498693000&usg=AOvVaw1l2wFGss8Srl0CYLlIfxJD) allows programmers to write client-server applications in Java without worrying about the details of remote procedure calls (RPCs) except for providing explicit callbacks for RPCs (one for a successful call, one for failure).GWT compiles the client code into JavaScript, which runs within a web browser, and the RPCs run as Java code on the server, with communication done via HTTP.The reader is advised to learn about GWT before delving into the portion of App Inventor that runs in, or responds to requests from, the user’s browser.

### Google App Engine
[Google App Engine (GAE)](https://cloud.google.com/appengine/) is a cloud-computing platform that enables programs written in Java (or Python) to run and maintain data on Google servers.The original internal version of App Inventor was built directly on proprietary Google infrastructure but, for the open source release, was rewritten to use GAE, using the third-party Objectify datastore API.
GWT and GAE play well together, where a GWT server can run on a GAE server. This is how App Inventor works, as shown in Figure 1.

<br>
<figure>
<img src="/assets/fig-1.png"/>
<figcaption>Figure 1</figcaption>
</figure>

!!! info "Figure 1"
    The App Inventor client and server are created with GWT, which converts the front-end code into JavaScript, which is run with the GWT client library in the user’s browser.The back-end runs on the GWT server library as a Google App Engine service, using the third-party Objectify API for data storage

### Android
If you’ve never written an Android application before and if you’re going to work on components, the [Hello World tutorial](https://developer.android.com/training/basics/firstapp) is a good way to get started. If you are serious about writing components, you should also complete the Managing the Activity lifecycle section.

### Schema & Kawa
[Kawa](https://www.google.com/url?q=http://www.gnu.org/software/kawa/&sa=D&source=editors&ust=1618564498696000&usg=AOvVaw03DfQmC_TXD1UWkWavBZDx) is a free implementation of Scheme that compiles to Java byte code, which can be converted by the tool dx into [Dalvik byte code](https://www.google.com/url?q=http://en.wikipedia.org/wiki/Dalvik_(software)&sa=D&source=editors&ust=1618564498696000&usg=AOvVaw3IjHit9HZ5W7OTWFc-lqpa).We use [Scheme](https://en.wikipedia.org/wiki/Scheme_(programming_language)) as the internal representation of users’ programs, and part of the [runtime library](https://github.com/mit-cml/appinventor-sources/tree/master/appinventor/buildserver/src/com/google/appinventor/buildserver/resources/runtime.scm) is written in Scheme.These get compiled down to byte code and linked with our components library (written in Java) and external libraries (written in Java and C/C++) by the build server.

### Blockly
In App Inventor 2, the blocks editor has been integrated into the browser, as opposed to the [Java Web Start](https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/) application that ships with App Inventor. [Blockly](https://developers.google.com/blockly) is an open source library created by Googler Neil Fraser, and it’s written in JavaScript (using SVG).

<br>

## Project & Directories
The App Inventor distribution consists of the following subdirectories, the first seven of which contain source code for sub-projects:

### aiphoneapp
The interpreter that runs on the mobile device or emulator when it is connected to a computer running App Inventor.

### [aiplayapp](https://github.com/mit-cml/appinventor-sources/tree/master/appinventor/aiplayapp)
Another version of the interpreter that runs on the mobile device or emulator when it is connected to a computer running App Inventor. This is what we call the MIT App Inventor Companion.

### [appengine](https://github.com/mit-cml/appinventor-sources/tree/master/appinventor/appengine)
The GWT application that provides the Designer JavaScript code to the client browser and provides supporting server-side functionality, such as storing and retrieving projects and issuing compile requests to the buildserver.

### [blocklyeditor](https://github.com/mit-cml/appinventor-sources/tree/master/appinventor/blocklyeditor)
The Blocks Editor, embedded in the browser and powered by Blockly. Used by both blockseditor and buildserver.

### [buildserver](https://github.com/mit-cml/appinventor-sources/tree/master/appinventor/buildserver)
An http server/servlet that takes a source zip file as input and produces an apk and/or error messages.

### [common](https://github.com/mit-cml/appinventor-sources/tree/master/appinventor/common)
Constants and utility classes used by other sub-projects.

### [components](https://github.com/mit-cml/appinventor-sources/tree/master/appinventor/components)
Code supporting App Inventor components, including annotations, implementations, and scripts for extracting component information needed by other sub-projects.More information can be found in the documents:
- How to Add A Component
- How to Add a Property to a Component

### [docs](https://github.com/mit-cml/appinventor-sources/tree/master/appinventor/docs)
User-level documentation, such as tutorials.

### [lib](https://github.com/mit-cml/appinventor-sources/tree/master/appinventor/lib)
External libraries, such as JUnit, used by the various sub-projects.They are listed below in External Libraries.

<br>

## App Inventor Classic

<br>

## Component Information

### simple_components.json
This file is used by the editor within the Designer in the GWT client (part of the appengine project).Here are the keys, their meanings, and sample values for the AccelerometerSensor.

|      Key       |                                                             Meaning                                                              |                                Sample value                                 |
|:--------------:|:--------------------------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------:|
|      name      |                                                       the component’s name                                                       |                             AccelerometerSensor                             |
|    version     |                 a number incremented whenever the component’s API is changed (such as by adding a new Property)                  |                                      1                                      |
| categoryString |                                       the section on the palette in which it should appear                                       |                                   SENSORS                                   |
|   helpString   |               descriptive HTML text shown when the user clicks the help icon to the right of the component’s name                |  Non-visible component that can detect shaking and measure acceleration...  |
| showOnPalette  | whether to show the component on the palette (currently true for all components except for Form, which is automatically created) |                                    true                                     |
|   nonVisible   |                                false for GUI elements, true for non-GUI elements, such as sensors                                |                                    true                                     |
|    iconName    |                  a path to the component’s icon, relative to `appinventor/appengine/src/com/google/appinventor`                  |                       images/accelerometersensor.png                        |
|   properties   |                               a list containing the name, type, and default value of each property                               | [{ “name”: “Enabled”,   “editorType”: “boolean”,   “defaultValue”: “True”}] |

### simple_components.txt
This file, which simply lists component names alphabetically, one per line, is used by the buildserver when building user projects.

### simple_components_permissions.json
This file, which is used by the buildserver to generate the permissions required by an app, has entries with the names and required permissions of each component,
such as:
``` json
{"name": "Clock", "permissions": []},
{"name": "ContactPicker",
 "permissions": ["android.permission.INTERNET",
"android.permission.READ_CONTACTS"]}
```

### ya_lang_def.xml
This file contains the specification of the blocks used by blockseditor (via blockslib), including both static library functions and components with their methods, properties, and events.Its header and footer come from the static filesOUTPUT_HEADER.txt and OUTPUT_FOOTER.txt in the src/com/google/appinventor/components/scripts/templates directory within the components project.At the time of this writing, ya_lang_def was over 14,000 lines.
Here is a small excerpt:
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- TinyWebDB Component -->
<BlockGenus name="TinyWebDB" initlabel="TinyWebDB" label-unique="yes" editable-label="no" kind="command" is-starter="yes" is-terminator="yes" color="grey">
 <description>
<text>Non-visible component that communicates with a Web service to store and retrieve information.</text>
 </description>
 <LangSpecProperties>
<LangSpecProperty key="ya-kind" value="component" />
<LangSpecProperty key="component-version" value="2" />
<LangSpecProperty key="ya-event-1" value="TinyWebDB-GotValue" />
<LangSpecProperty key="ya-event-2" value="TinyWebDB-ValueStored" />
<LangSpecProperty key="ya-event-3" value="TinyWebDB-WebServiceError" />
<LangSpecProperty key="ya-prop-1" value="ServiceURL/read-write-property/text/" />
<LangSpecProperty key="ya-method-1" value="TinyWebDB-GetValue" />
<LangSpecProperty key="ya-type-method-1" value="Type-TinyWebDB-GetValue" />
<LangSpecProperty key="ya-method-2" value="TinyWebDB-StoreValue" />
<LangSpecProperty key="ya-type-method-2" value="Type-TinyWebDB-StoreValue" />
 </LangSpecProperties>
</BlockGenus>
```

### component-doc.html
This file, generated by the Ant target ComponentDocumentation (which does not appear as a dependency of any other target or task) contains reference documentation in HTML.Descriptions are pulled from annotations or comments in the source code for components.
Here is an excerpt:

=== "HTML Code"

    ``` html
    <h2 id="TinyDB">TinyDB</h2>
    <p>Non-visible component that persistently stores values on the phone.</p>
    <h3>Properties</h3>
    none
    <h3>Events</h3>
    none
    <h3>Methods</h3>
    <dl>
        <dt><code>any GetValue(text tag)</code></dt>
        <dd>Retrieve the value stored under the given tag.</dd>
        <dt><code>StoreValue(text tag, any valueToStore)</code></dt>
        <dd>Store the given value under the given tag.The storage persists on the phone when the app is restarted.</dd>
    </dl>
    ```

=== "Preview"

    <h2 id="TinyDB">TinyDB</h2>
    <p>Non-visible component that persistently stores values on the phone.</p>
    <h3>Properties</h3>
    none
    <h3>Events</h3>
    none
    <h3>Methods</h3>
    <dl>
    <dt><code>any GetValue(text tag)</code></dt>
    <dd>Retrieve the value stored under the given tag.</dd>
    <dt><code>StoreValue(text tag, any valueToStore)</code></dt>
    <dd>Store the given value under the given tag.The storage persists on the phone when the app is restarted.</dd>
    </dl>


<br>

## External Libraries
A number of external libraries are included in the distribution under the subdirectory `appinventor/lib`.Listed by directory,
the are:

|    Library Name    | Descriptions                                                                                                                                                                                                                                                                                                                                                                 |
|:------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|      android       | the [Android SDK](https://developer.android.com/studio), used within the components project to generate:                                                                                                                                                                                                                                                                     |
|  animal\_sniffer   | [Animal Sniffer](https://www.mojohaus.org/animal-sniffer/), used within the blockseditor project’s BlocksEditorTest to ensure that the Blocks Editor does not contain references to any methods added to the Java standard libraries after Java 5 (1.5).  (In the past, we had problems with String.isEmpty() sneaking in.) Note that this is for App Inventor classic only. |
|    ant-contrib     | [Ant-Contrib](http://ant-contrib.sourceforge.net/), which defines the [propertyregex](http://ant-contrib.sourceforge.net/tasks/tasks/propertyregex.html) task used within the macro ai.dojunit in build-common.xml.                                                                                                                                                          |
|     appengine      | the [App Engine Java SDK](https://cloud.google.com/appengine/downloads?csw=1) required by the appengine project.                                                                                                                                                                                                                                                             |
|       args4j       | [Args4j](https://args4j.kohsuke.org/), a parser for command-line options used for the standalone version of the compiler (which is used to generate StarterApp) within the buildserver project.                                                                                                                                                                              |
|      blockly       | this is the source code for the [Blockly](https://developers.google.com/blockly) library in which the blocks editor in App Inventor 2 is based.                                                                                                                                                                                                                              |
| commons-fileupload | part of the [Apache Commons](http://commons.apache.org/), providing the ability to upload projects and individual asset files to the UploadServlet within the appengine project.                                                                                                                                                                                             |
|     commons-io     | part of the [Apache Commons](http://commons.apache.org/), providing the FileUtils class, which is used in BlockSaveFileTest.java in the blockslib project.                                                                                                                                                                                                                   |
|      findbugs      | [FindBugs](http://findbugs.sourceforge.net/), which is only included for its definition of javax.annotation.Nullable, which is [required by the Guava GWT library](https://github.com/google/guava/issues/776).                                                                                                                                                              |
|        gson        | [Gson](https://github.com/google/gson), a Java library to convert between JSON and Java objects, required by keyczar (below).                                                                                                                                                                                                                                                |
|       guava        | [Guava](https://github.com/google/guava), a public version of some of the internal Google libraries on which the original version of App Inventor was written.                                                                                                                                                                                                               |
|        gwt         | [Google Web Toolkit](http://www.gwtproject.org/), used by appengine to create the browser client.                                                                                                                                                                                                                                                                            |
|   gwt\_dragdrop    | [gwt-dnd](https://github.com/fredsa/gwt-dnd), a drag-and-drop library for GWT.  This is used by appengine as part of the browser client.                                                                                                                                                                                                                                     |
|   gwt\_incubator   | the deprecated [Google Web Toolkit Incubator](https://code.google.com/archive/p/google-web-toolkit-incubator/).  The appengine project uses GWTCanvas andColor in the mock component implementation.                                                                                                                                                                         |
|        jdk5        | this includes the API signature file (not library) jdk5.sig to be used by animal\_sniffer (above) to ensure that the Blocks Editor does not contain references to any methods added to the Java standard libraries after Java 5 (1.5).   This was generated by a commented-out target, BuildJdk5Signature, in the build.xml file for blockseditor.                           |
|        json        | the reference Java library to parse [JSON](https://www.json.org/json-en.html).  JSON is used throughout the system:                                                                                                                                                                                                                                                          |
|       junit        | the standard [Java unit testing](https://junit.org/) library (JUnit), used for tests throughout the system.                                                                                                                                                                                                                                                                  |
|       junit4       | [test libraries for Java](https://junit.org/), used on top of JUnit.                                                                                                                                                                                                                                                                                                         |
|    junit-addons    | [JUnit addons](http://junit-addons.sourceforge.net/), specifically the classes [junitx.framework.Assert](http://junit-addons.sourceforge.net/junitx/framework/Assert.html) and [junitx.framework.ListAssert](http://junit-addons.sourceforge.net/junitx/framework/ListAssert.html).                                                                                          |
|        kawa        | the [Kawa language framework](http://www.gnu.org/software/kawa/), whose included compiler is used to convert YAIL/Scheme code to Java byte code.                                                                                                                                                                                                                             |
|      keyczar       | the [Keyczar cryptographic library](https://github.com/google/keyczar) used for generating keys for signing Android apps.                                                                                                                                                                                                                                                    |
|       log4j        | the [log4j logging library](https://logging.apache.org/log4j/2.x/) required by keyczar.                                                                                                                                                                                                                                                                                      |
|   objectify-3.0    | the [objectify-appengine library](https://code.google.com/archive/p/objectify-appengine/), which provides a simpler interface to the Google App Engine datastore.                                                                                                                                                                                                            |
|     powermock      | the [PowerMock testing framework](https://powermock.github.io/), which enables mocking Android classes with final methods.                                                                                                                                                                                                                                                   |
|    responder-iq    | classes supporting testing from the [responder-iq](https://code.google.com/archive/p/responder-iq/) library.  Specifically, we use the classes com.riq.MockHttpServletRequest and com.riq.MockHttpServletResponse in DownloadServletTest.java.                                                                                                                               |
|    tablelayout     | TableLayout used in blockslib for layout within the Blocks Editor, not to be confused with the Android TableLayout class.                                                                                                                                                                                                                                                    |
|      twitter       | [Twitter4j](http://twitter4j.org/en/index.html), an unofficial Java library for the Twitter API, used by the Twitter component.                                                                                                                                                                                                                                              |

<br>

## Programming Style
We generally follow these style guides:

- [x] [Android Code Style Rules](https://source.android.com/setup/contribute/code-style)
- [x] [Sun/Oracle Code Conventions](https://www.oracle.com/java/technologies/cc-java-programming-language.html)
- [x] [Javadoc](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)

An exception is that we use upper-camel-case (e.g., **MoveTo**) for component property, method, and event names.

<br>

## Git Branches
The official App Inventor sources reside on GitHub in [mit-cml/appinventor-sources](https://github.com/mit-cml/appinventor-sources) The primary branches are:

1. [master](https://github.com/mit-cml/appinventor-sources/tree/master) -- The current sources for MIT App Inventor 2
2. [ai1](https://github.com/mit-cml/appinventor-sources/tree/ai1) -- The last version of MIT App Inventor Classic
3. [gh-pages](https://github.com/mit-cml/appinventor-sources/tree/gh-pages) -- This website (GitHub convention for pages)
4. [ucr](https://github.com/mit-cml/appinventor-sources/tree/ucr) -- Upcoming Component Release

Periodically there will be other branches for a specified purpose, usually branches representing longer term development, often separately reviewed. Currently there is an “api4” branch where we are working on changes to support a minSdk of 4 (or greater).

There are two types of releases that we make on our public service. The more significant release we refer to as a “Component Release.” A Component release includes changes to components such that App Inventor programmers will need to update their version of the App Inventor Companion and we also need to update the buildservers (which package apps for people) to be able to use the new/modified component(s). Because projects are marked with the version of the components they were built with, once we perform a component release WE CANNOT GO BACK!!! It is also worth noting that Component releases require work on the part of App Inventor Programmers because they have to update the AI2 Companion on their devices and or within their copy of the Android Emulator. Because of the effort and testing required, we tend to avoid having too many component releases too close together. In general we have been doing a Component Release once per month.

By comparison “non component releases” are where we do not update components. For example changes to the App Inventor blocks layer, or other features of the Website itself. They can usually be backed out if there is a problem and they do require action on the part of App Inventor programmers.

Because we do not wish to do component releases too often, we do not always want to checking changes to the master branch which contain component changes. Instead we have a new branch named “ucr” where we will be merging component changes that pass review. When it comes time to do a Component Release, we will merge the ucr branch into the master branch prior to the release.

<br>

## GitHub and Gerrit Reviews
Changes to MIT App Inventor should be made via the [GitHub Workflow](). In general we will not use GitHub to merge in changes. Instead after reviewing a change the release coordinators will squash your commits down to a single commit which will either be rebase on the master branch (non component change) or onto the ucr branch (component changes). Often as part of this work we will submit your commit to our private “Gerrit” review server which will arrange for automated running of our unit tests via the “Jenkins” continuous integration system. In general you do not need to be aware of this level of detail. Sometimes internal changes bypass the GitHub workflow and are reviewed directly on Gerrit, though this is happening less often now. 
