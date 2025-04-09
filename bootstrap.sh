#!/usr/bin/env bash

dpkg --add-architecture i386

# Install dependencies
apt-get update
apt-get upgrade -y
apt-get install -y libc6:i386 zlib1g:i386 libstdc++6:i386 \
     openjdk-8-jdk zip unzip bzip2 ant adb

# Install App Engine
mkdir -p /opt/appengine
cd /opt/appengine
wget --no-verbose -O /tmp/appengine.zip https://storage.googleapis.com/appengine-sdks/featured/appengine-java-sdk-1.9.68.zip
unzip -o /tmp/appengine.zip

# Install PhantomJS
cd /home/vagrant
export PHANTOM_JS="phantomjs-2.1.1-linux-x86_64"
wget --no-verbose https://bitbucket.org/ariya/phantomjs/downloads/$PHANTOM_JS.tar.bz2
tar -xvf $PHANTOM_JS.tar.bz2 -C /usr/local/share/
ln -sf /usr/local/share/$PHANTOM_JS/bin/phantomjs /usr/local/bin

# Configure shell
echo "export PATH=$PATH:/opt/appengine/appengine-java-sdk-1.9.68/bin" >> /home/vagrant/.bashrc
echo "cd /vagrant/appinventor" >> /home/vagrant/.bashrc

# Configure java
update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java

# Make the auth key in advance
cd /vagrant/appinventor
sudo -u vagrant ant MakeAuthKey

# Helper script for starting App Inventor dev server
cat <<EOF > /usr/local/bin/start_appinventor
ant RunLocalBuildServer &> buildserver.log &
BUILDSERVER=$!
dev_appserver.sh -p 8888 -a 0.0.0.0 appengine/build/war
kill -9 -- -$BUILDSERVER
EOF
chmod +x /usr/local/bin/start_appinventor
