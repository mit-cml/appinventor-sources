#!/bin/sh
mkdir /etc/secrets
cd /etc/secrets
unzip /run/appinventor/secrets.zip
cd /root
nginx
cd /opt/appinventor/webapps
export LANG="en_US.UTF-8"
su appinventor -c 'java -jar starter.jar nobuildserver'

