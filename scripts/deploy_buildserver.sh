#!/bin/bash

appinventor=~/appinventor-sources/appinventor
buildserver=~/buildserver


rm -rf $buildserver
mkdir $buildserver

cd $appinventor/buildserver
ant BuildDeploymentTar
cd $buildserver
mv $appinventor/build/buildserver/BuildServer.tar .
cp $appinventor/misc/buildserver/launch-buildserver .
tar -xf BuildServer.tar

./launch-buildserver
