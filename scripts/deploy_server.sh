#!/bin/bash

appinventor=~/Documents/appinventor-sources/appinventor
appengine=~/Documents/appengine/appengine-java-sdk-1.8.7
appenginename=isense-ai2

cd $appinventor

ant

$appengine/bin/appcfg.sh -A $appenginename update $appinventor/appengine/build/war/
