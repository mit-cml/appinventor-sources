---
title: App Inventor Developer Overview
order: 1
---

- TOC
{:toc}

## 1. Introduction

This document provides a high-level overview of the App Inventor source code, including the toolkits and libraries on which App Inventor depends, the different sub-projects within App Inventor, and information flow during the build process and execution.

For completeness, the original (outdated) version of this document can be found [here](https://docs.google.com/document/u/0/d/1hIvAtbNx-eiIJcTA2LLPQOawctiGIpnnt0AvfgnKBok/pub).

## 2. Background

MIT App Inventor is a rather large system divided in multiple projects, each of which relies on different open source technologies. Before showing what each of those projects are (section 3), we introduce some of the technologies used within them.

### 2.1 Google Web Toolkit

[Google Web Toolkit](http://code.google.com/webtoolkit/) (GWT) allows programmers to write client-server applications in Java without worrying about the details of remote procedure calls (RPCs) except for providing explicit callbacks for RPCs (one for a successful call, one for failure). GWT compiles the client code into JavaScript, which runs within a web browser, and the RPCs run as Java code on the server, with communication done via HTTP. The reader is advised to learn about GWT before delving into the portion of App Inventor that runs in, or responds to requests from, the user's browser.

Note that the original version of App Inventor used a combination of GWT (client and RPC) and App Engine (as a server), but the App Engine code has been deprecated in favour of a PostgreSQL backed custom server.

### 2.2 Jetty Web Server and PostgreSQL

[Jetty](https://jetty.org/index.html) is an Eclipse project that provides an scalable and efficient web server and servlet container. All the backend logic is hosted by Jetty and backed by a PostgresQL database.

The system still follows the original implementation API for the [Objectify](http://code.google.com/p/objectify-appengine/) datastore, which used to the implementation for StorageIO on App Engine. It has since then evolved outside of App Engine at [Objectify](https://github.com/objectify/objectify). The current App Engine implementation is now [Cloud Storage](https://docs.cloud.google.com/appengine/docs/standard/using-cloud-storage), but it is not used anymore.

```mermaid
%%{init: {"flowchart": {"rankSpacing": 40, "nodeSpacing": 2, "subGraphTitleMargin": {"bottom": 12}}}}%%
flowchart LR
  subgraph client["App Inventor Client"]
    direction TB
    gc["<div style='width:125px'>GWT client</div>"]
    wb["<div style='width:125px'>Web browser</div>"]
    gc --> wb
  end
  subgraph server["App Inventor Server"]
    direction TB
    ob["<div style='width:165px'>Objectify/StorageIO</div>"]
    jt["<div style='width:165px'>Jetty Servlet container</div>"]
    ob --> jt
  end
  subgraph db["App Inventor Database"]
    pg[("<div style='width:125px'>PostgreSQL</div>")]
  end
  client <--> server
  server <--> pg
  classDef box fill:#dae8fc,stroke:#333,color:#000
  class gc,wb,ob,jt,pg box
  style client fill:#dae8fc,stroke:#333,color:#d00
  style server fill:#dae8fc,stroke:#333,color:#d00
  style db fill:none,stroke:none,color:#d00

  %% Completely hides the arrows inside the subgraphs to keep them tightly stacked
  linkStyle 0,1 stroke:transparent,stroke-width:0px;
```

_Figure 1: The App Inventor client is created with GWT, which converts the front-end code into JavaScript, which is run with the GWT client library in the user's browser. The back-end runs on the Jetty server as standalone Java service, using the third-party Objectify API for data storage, and using PostgreSQL for storage._

### 2.3 Android and iOS

If you've never written an Android application before and if you're going to work on components, the [Hello, World tutorial](http://developer.android.com/resources/tutorials/hello-world.html) is a good way to get started. If you are serious about writing components, you should also complete the [Managing the Activity lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle) section.

For iOS the [get started](https://developer.apple.com/ios/get-started/) section in their docs will contain all the information needed.

You don't have to be an expert in both platforms, but you will certainly need the knowledge to use the tools so you can test your ideas in the different platforms before you abstract them as an App Inventor component.

### 2.4 Scheme/Kawa

[Kawa](http://www.gnu.org/software/kawa/) is a free implementation of [Scheme](<http://en.wikipedia.org/wiki/Scheme_(programming_language)>) that compiles to Java byte code, which can be converted by the tool dx into [Dalvik bytecode](<http://en.wikipedia.org/wiki/Dalvik_(software)>). We use Scheme as the internal representation of users' programs, and part of [the runtime library](https://github.com/mit-cml/appinventor-sources/blob/master/appinventor/components-ios/src/runtime.scm) is written in Scheme. These get compiled down to byte code and linked with our components library (written in Java) and external libraries (written in Java and C/C++) by the build server.

### 2.5 Blockly

[Blockly](https://github.com/RaspberryPiFoundation/blockly/) is an open source library created by Googler Neil Fraser, and it's written in TypeScript (using SVG). After a number of years at Google it is now hosted by the RaspberryPiFoundation.

## 3. App Inventor projects and directories

The App Inventor distribution lives in the `appinventor/` directory of the repository, and consists of the following subdirectories, which contain source code for sub-projects:

- **aicompanionapp**: the source of the iOS version of the MIT App Inventor Companion (Swift and Objective-C), which is built on top of `components-ios`.
- **aimerger**: a standalone desktop (Swing) tool that merges App Inventor projects, built with its own `build.xml`.
- **aimerger_for_appinv_classic**: a variant of the merger tool for App Inventor classic projects.
- **aiplayapp**: the source of the Android version of the interpreter that runs on the mobile device or emulator when it is connected to a computer running App Inventor. This is what we call the MIT App Inventor Companion.
- **appengine**: the GWT application that provides the Designer JavaScript code to the client browser and provides supporting server-side functionality, such as storing and retrieving projects and issuing compile requests to the buildserver.
- **blocklyeditor**: the Blocks Editor, embedded in the browser and powered by Blockly. Used by both blockseditor and buildserver.
- **buildserver**: an http server/servlet that takes a source zip file as input and produces an apk and/or error messages.
- **common**: constants and utility classes used by other sub-projects.
- **components**: code supporting App Inventor components, including annotations, implementations, and scripts for extracting component information needed by other sub-projects. More information can be found in the documents (Note that these documents are out of date and will be reworked soon):
  - [How to Add A Component](https://docs.google.com/document/pub?id=1xk9dMfczvjbbwD-wMsr-ffqkTlE3ga0ocCE1KOb2wvw)
  - [How to Add a Property to a Component](https://docs.google.com/document/pub?id=14c1X19s6pXYHdDBOJJELcmOghWbm2FPekRvGzX0ENps)
- **components-common**: web assets shared by components, such as the HTML and JavaScript for the personal image and audio classifiers.
- **components-ios**: the iOS implementation of the components, written in Swift (`AIComponentKit`).
- **PlayerApp**: the Swift sources for the iOS Player app, which uses `AIComponentKit` to run App Inventor projects on iOS.
- **schemekit**: an Objective-C Scheme interpreter (`SchemeKit`) used on iOS to run the Scheme/YAIL code generated from the blocks. It is the iOS counterpart of the Kawa runtime described above.

The following subdirectories contain static files, support tooling and dependencies:

- **contrib**: contributor documentation, such as how component reference documentation is written in Javadoc comments.
- **docs**: user-level documentation, such as tutorials, both the Jekyll markdown sources and the static HTML.
- **lib**: external libraries, such as JUnit, used by the various sub-projects. They are listed below in [External Libraries](#5-external-libraries).
- **misc**: assorted helper scripts and small tools, each in its own folder with a README where one exists. Among them are the buildserver launch and monitoring scripts (`buildserver`), a generator that scaffolds new components (`componentcreator`), the `aiStarter` emulator helper (`emulator-support`), translation tooling (`i18n`), the Companion `rendezvous` server (Node.js), and the `checkstyle`, `whitelist`, `splashscreen`, `passwordmail` and `popup` utilities.
- **prebuilts**: prebuilt iOS binaries, currently the WebRTC framework for both device and simulator.

The following are generated or third-party folders, or project files, that you will see next to the sources:

- **build**: the output directory of the Ant build, with one subfolder per project. It is not checked in (it is git-ignored). The component information files described in section 4 are generated here.
- **node_modules**: Node.js dependencies for the JavaScript tests (Karma, Mocha, Chai), as declared in `package.json`.
- **Pods**: CocoaPods dependencies for the iOS projects, as declared in the `Podfile`.
- **Xcode projects**: the `*.xcodeproj` and `*.xcworkspace` folders at the top level (`AICompanionApp`, `AIComponentKit`, `PlayerApp`, `SchemeKit`, `AppInventor`, `Schemekit`) are the Xcode entry points for the iOS code above.

Figure 2 shows where each of the projects' code runs. It is simplified in that it only shows the name of the project (e.g., blocklyeditor) and not specific build targets (e.g., BlocklyCompile). Greater detail can be found in the Ant build.xml files in each project's directory. Also note that the build server can be deployed in any cloud service or locally in a development machine.

```mermaid
%%{init: {"flowchart": {"rankSpacing": 60, "nodeSpacing": 14, "curve": "linear", "subGraphTitleMargin": {"bottom": 6}}}}%%
flowchart LR
  subgraph client["Client computer"]
    direction LR
    subgraph ble["blocklyeditor"]
      b["<div style='width:150px'>Blockly<br>Browser</div>"]
    end
    subgraph gwtc["appengine (client)"]
      g["<div style='width:150px'>GWT client<br>Browser</div>"]
    end
  end

  subgraph phone["Mobile device or emulator"]
    direction LR
    subgraph play["aiplayapp"]
      p["<div style='width:150px'>Companion app<br>Android</div>"]
    end
    subgraph comp["aicompanionapp"]
      c["<div style='width:150px'>Companion app<br>iOS</div>"]
    end
  end

  subgraph rdv["Rendezvous server"]
    subgraph rdvp["misc/rendezvous"]
      r["<div style='width:150px'>Node.js service<br>Docker</div>"]
    end
  end

  subgraph server["App Inventor server"]
    subgraph gwts["appengine (server)"]
      s["<div style='width:190px'>GWT RPC servlets<br>StorageIo<br>Jetty servlet container</div>"]
    end
  end

  subgraph store["App Inventor storage"]
    pg[("<div style='width:110px'>PostgreSQL<br>projects, users</div>")]
    s3b[("<div style='width:110px'>S3<br>assets</div>")]
  end

  subgraph bs["Build server"]
    subgraph bsp["buildserver"]
      k["<div style='width:235px'>Kawa + components library<br>Any cloud service</div>"]
    end
  end

  client <-->|"GWT RPC"| server
  server <-->|"SQL"| pg
  server <-->|"assets"| s3b
  server <-->|"build request / results"| bs
  client <-->|"WebRTC or HTTP"| phone
  client <-->|"pairing"| rdv
  phone <-->|"pairing"| rdv

  classDef layer fill:#dae8fc,stroke:#333,color:#000
  class b,g,p,c,r,s,k,pg,s3b layer
  style client fill:#f2f2f2,stroke:#888,color:#222
  style phone fill:#f2f2f2,stroke:#888,color:#222
  style rdv fill:#f2f2f2,stroke:#888,color:#222
  style server fill:#f2f2f2,stroke:#888,color:#222
  style store fill:#f2f2f2,stroke:#888,color:#222
  style bs fill:#f2f2f2,stroke:#888,color:#222
  style ble fill:#e8f0fb,stroke:#7a9cc6,color:#d00
  style gwtc fill:#e8f0fb,stroke:#7a9cc6,color:#d00
  style play fill:#e8f0fb,stroke:#7a9cc6,color:#d00
  style comp fill:#e8f0fb,stroke:#7a9cc6,color:#d00
  style rdvp fill:#e8f0fb,stroke:#7a9cc6,color:#d00
  style gwts fill:#e8f0fb,stroke:#7a9cc6,color:#d00
  style bsp fill:#e8f0fb,stroke:#7a9cc6,color:#d00
```

_Figure 2: The parts of App Inventor and where each one runs. Project directories are shown in red, and the arrows are labelled with how the parts communicate. The server runs on Jetty and stores data in PostgreSQL and S3 compatible services, the Rendezvous server only pairs the browser with the Companion, and the build server can be deployed in any cloud service._

Figure 3 shows run-time communication among the different sub-projects.

```mermaid
sequenceDiagram
  autonumber
  participant C as Companion<br>(device or emulator)
  participant R as Rendezvous server
  participant B as Browser<br>(GWT client + Blockly)
  participant S as App Inventor server<br>(Jetty)
  participant DB as PostgreSQL + S3
  participant BS as Build server

  rect rgb(232, 240, 251)
    Note over B,DB: Editing a project
    B->>S: Load project (GWT RPC)
    S->>DB: Read project files and assets
    S-->>B: Sources, assets, settings
    B->>S: Save designer and blocks changes
    S->>DB: Store project files and assets
  end

  rect rgb(255, 244, 229)
    Note over C,B: Live testing with the Companion (the server is not involved)
    B->>R: Post offer, keyed by the connection code
    C->>R: Enter the code or scan the QR code
    R-->>C: Browser's offer
    C->>R: Post answer
    R-->>B: Companion's answer
    alt WebRTC available
      B-->>C: Open a direct WebRTC data channel
    else Legacy mode (same network)
      B->>C: HTTP requests to the Companion on port 8001
    end
    B->>C: Blocks as YAIL code, and assets
    C-->>B: Values, errors and events
  end

  rect rgb(233, 245, 233)
    Note over B,BS: Building an app
    B->>S: Build (GWT RPC)
    S->>DB: Read project files
    S->>BS: POST project source zip and a callback URL
    BS->>S: Progress and result via the callback URL
    S->>DB: Store build output
    B->>S: Poll build status
    S-->>B: Download link for the APK or IPA
  end
```

_Figure 3: The main run-time interactions between the parts of App Inventor, in order: editing a project, testing live with a Companion (which does not involve the server), and building an app._

## 4. Component Information Files

Several parts of the system need to know what components exist and their characteristics. This information is generated at build time through a set of annotation processors that run over the components source code and generate files required in order to build the other App Inventor projects. (Here, "project" refers to the parts of the App Inventor codebase, not user-created projects.) These files can be found in the subdirectory `appinventor/build/components`.

### 4.1 simple_components.json

This file is used by the editor within the Designer in the GWT client (part of the appengine project). Here are the keys, their meanings, and sample values for the AccelerometerSensor.

| Key              | Meaning                                                                                                                          | Sample value                                                                     |
| ---------------- | -------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| `name`           | the component's name                                                                                                             | `AccelerometerSensor`                                                            |
| `version`        | a number incremented whenever the component's API is changed (such as by adding a new Property)                                  | `1`                                                                              |
| `categoryString` | the section on the palette in which it should appear                                                                             | `SENSORS`                                                                        |
| `helpString`     | descriptive HTML text shown when the user clicks the help icon to the right of the component's name                              | `"<p>Non-visible component that can detect shaking and measure acceleration..."` |
| `showOnPalette`  | whether to show the component on the palette (currently true for all components except for Form, which is automatically created) | `true`                                                                           |
| `nonVisible`     | false for GUI elements, true for non-GUI elements, such as sensors                                                               | `true`                                                                           |
| `iconName`       | a path to the component's icon, relative to `appinventor/appengine/src/com/google/appinventor`                                   | `images/accelerometersensor.png`                                                 |
| `properties`     | a list containing the name, type, and default value of each property                                                             | `[{ "name": "Enabled", "editorType": "boolean", "defaultValue": "True"}]`        |

### 4.2 simple_components.txt

This file, which simply lists component names alphabetically, one per line, is used by the buildserver when building user projects.

## 5. External Libraries

A number of external libraries are included in the distribution under the subdirectory `appinventor/lib`. We are only listing some notable ones here, as it would be too long for a document like this:

- **android**: the [Android SDK](http://developer.android.com/sdk/index.html), used within the components project to generate:
  - AndroidRuntime: the library providing run-time support for the components.
  - libsForAndroidRuntimeTests
  - the annotation processors, which produce the [component information files](#4-component-information-files) described above.
- **ant-contrib**: [Ant-Contrib](http://ant-contrib.sourceforge.net/), which defines the [propertyregex task](http://ant-contrib.sourceforge.net/tasks/tasks/propertyregex.html) used within the macro `ai.dojunit` in `build-common.xml`.
- **args4j**: [Args4j](http://java.net/projects/args4j/), a parser for command-line options used for the standalone version of the compiler (which is used to generate StarterApp) within the buildserver project.
- **commons-fileupload**: part of the [Apache Commons](http://commons.apache.org/), providing the ability to upload projects and individual asset files to the UploadServlet within the appengine project.
- **commons-io**: part of the [Apache Commons](http://commons.apache.org/), providing the FileUtils class, which is used in BlockSaveFileTest.java in the blockslib project.
- **findbugs**: [FindBugs](http://findbugs.sourceforge.net/), which is only included for its definition of `javax.annotation.Nullable`, which is [required by the Guava GWT library](http://code.google.com/p/guava-libraries/issues/detail?id=776).
- **gson**: [Gson](http://code.google.com/p/google-gson/), a Java library to convert between JSON and Java objects, required by keyczar (below).
- **guava**: [Guava](http://code.google.com/p/guava-libraries/), a public version of some of the internal Google libraries on which the original version of App Inventor was written.
- **gwt**: [Google Web Toolkit](http://code.google.com/webtoolkit/), used by appengine to create the browser client.
- **gwt_dragdrop**: [gwt-dnd](http://code.google.com/p/gwt-dnd/), a drag-and-drop library for GWT. This is used by appengine as part of the browser client.
- **jdk5**: this includes the API signature file (not library) `jdk5.sig` to be used by animal_sniffer (above) to ensure that the Blocks Editor does not contain references to any methods added to the Java standard libraries after Java 5 (1.5). This was generated by a commented-out target, `BuildJdk5Signature`, in the build.xml file for blockseditor.
- **json**: the reference Java library to parse [JSON](http://www.json.org/). JSON is used throughout the system:
  - to encode the components used and their properties in a user project (`Screen1.scm`).
  - by the Form, GameClient, TinyDB, TinyWebDB, Voting, and Web components.
  - to encode the permissions associated with each component (`simple_components_permissions.json`, described above).
- **junit**: the standard [Java unit testing library (JUnit)](http://www.junit.org/), used for tests throughout the system.
- **junit4**: [test libraries for Java](http://code.google.com/p/test-libraries-for-java/), used on top of JUnit.
- **junit-addons**: [JUnit addons](http://junit-addons.sourceforge.net/), specifically the classes [junitx.framework.Assert](http://junit-addons.sourceforge.net/junitx/framework/Assert.html) and [junitx.framework.ListAssert](http://junit-addons.sourceforge.net/junitx/framework/ListAssert.html).
- **kawa**: the [Kawa language framework](http://www.gnu.org/software/kawa/), whose included compiler is used to convert YAIL/Scheme code to Java byte code.
- **keyczar**: the [Keyczar cryptographic library](http://www.keyczar.org/) used for generating keys for signing Android apps.
- **log4j**: the [log4j logging library](http://logging.apache.org/log4j/) required by keyczar.
- **objectify-3.1**: the [objectify-appengine library](http://code.google.com/p/objectify-appengine/), which provides a simpler interface to the Google App Engine datastore.
- **responder-iq**: classes supporting testing from the [responder-iq library](http://code.google.com/p/responder-iq/). Specifically, we use the classes `com.riq.MockHttpServletRequest` and `com.riq.MockHttpServletResponse` in DownloadServletTest.java.
- **tablelayout**: [TableLayout](http://java.sun.com/products/jfc/tsc/articles/tablelayout/), used in blockslib for layout within the Blocks Editor, not to be confused with the Android TableLayout class.

## 6. Programming Style

We generally follow these style guides:

- [Android Code Style Rules](https://source.android.com/source/code-style.html)
- [Sun/Oracle Code Conventions](http://www.oracle.com/technetwork/java/index-135089.html)
- [Javadoc](http://www.oracle.com/technetwork/java/javase/documentation/index-137868.html)

An exception is that we use upper-camel-case (e.g., "MoveTo") for component property, method, and event names.

## 7. Git Branches

The official App Inventor sources reside on GitHub in `mit-cml/appinventor-sources.git`. The primary branches are:

- `master` -- The current sources for MIT App Inventor 2
- `ucr` -- "Upcoming Component Release"

Periodically there will be other branches for a specified purpose, usually branches representing longer term development, often separately reviewed.

There are two types of releases that we make on our public service. The more significant release we refer to as a "Component Release." A Component release includes changes to components such that App Inventor programmers will need to update their version of the App Inventor Companion and we also need to update the buildservers (which package apps for people) to be able to use the new/modified component(s). Because projects are marked with the version of the components they were built with, once we perform a component release WE CANNOT GO BACK!!! It is also worth noting that Component releases require work on the part of App Inventor Programmers because they have to update the AI2 Companion on their devices and or within their copy of the Android Emulator. Because of the effort and testing required, we tend to avoid having too many component releases too close together. In general we have been doing a Component Release once per month.

By comparison "non component releases" are where we do not update components. For example changes to the App Inventor blocks layer, or other features of the Website itself. They can usually be backed out if there is a problem and they do require action on the part of App Inventor programmers.

Because we do not wish to do component releases too often, we do not always want to checking changes to the master branch which contain component changes. Instead we have a new branch named "ucr" where we will be merging component changes that pass review. When it comes time to do a Component Release, we will merge the ucr branch into the master branch prior to the release.
