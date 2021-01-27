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

## Development

WIP