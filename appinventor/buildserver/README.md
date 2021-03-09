# App Inventor Buildserver

## Quick Setup

1. Build the buildserver using `ant`:
`$ ant`

2. Run the buildserver using `ant`:
`$ ant RunLocalBuildServer`

App Inventor buildserver should be now running in port 9990 by default. To test it, just make a POST request with a
zip file in the body to the URL `http://localhost:9990/buildserver/build-from-zip`. Something like the following works:

```
curl --data-binary  @$HOME/MyDownloads/ImageUpload.zip  --output foo.apk --dump-header headers.out \
http://localhost:9990/buildserver/build-from-zip?uname=$USER
```

Additionally, this buildserver can be run without a web server through the command line. To do so, just run the
following command:

`$ ant RunMain -Dzip.file=$HOME/MyDownloads/ImageUpload.zip -Duser.name=$USER -Doutput.dir=/tmp`

## File Structure

The steps in this buildserver have been divided into different **tasks**. Each of these tasks execute a certain step in
the process of exporting AIA files as APK files.

There are two different entry points for the buildserver: through a web endpoint, or using command line:

- **`BuildServer`**: web server for the buildserver. It enables dynamical export of AIA files, by providing an
endpoint which receives AIA files to later export them as APK files. It runs using Grizzly, by default on port 9990.  
- **`Main`**: command line program to build apps from a local ZIP file. An alternative to the web server, which
enables programmatically builds of local files without exposing a port.

Once any of the following choices run, they call **`ProjectBuilder`**, which prepares the input file for the compile
process, including creating a KeyStore if none was provided. This class is also responsible of creating a new
**`Compiler`** object, adding all needed steps and creating its context. Then, just launches the process into a new
thread to compile the app.

All steps required to build the app are contained in the `tasks` package. Each step runs a specific task by extending
the **`Task`** interface. Additionally, they all require the **`BuildType`** annotation to specify which format is it
allowed to run on.  
The `execute` method in tasks receive the **`CompilerContext`** object, built back in `ProjectBuilder`, which tasks
modify after applying changes. It contains critical information required for the build process to run, like paths for
each file, components required, or needed libraries, for example.
