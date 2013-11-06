rm -rf ~/for-BuildServer
mkdir ~/for-BuildServer

cd ../appinventor/buildserver/
ant BuildDeploymentTar
cd ~/for-BuildServer
mv ~/appinventor-sources/appinventor/build/buildserver/BuildServer.tar .
cp ~/appinventor-sources/appinventor/misc/buildserver/launch-buildserver .
tar -xf BuildServer.tar

./launch-buildserver
