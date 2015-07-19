#!/bin/bash

appinventor=~/Documents/appinventor-sources/appinventor
appengine=~/Documents/appengine/appengine-java-sdk-1.9.3

cd $appinventor

if [[ "$1" = "clean" ]]; then
    echo "Cleaning..."
	ant clean
fi
ant installplay
aiDaemon &
ant
beep -l 1000

$appengine/bin/dev_appserver.sh --port=8888 --address=0.0.0.0 $appinventor/appengine/build/war/
