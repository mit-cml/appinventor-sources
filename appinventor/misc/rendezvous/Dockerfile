FROM ubuntu:16.04
MAINTAINER Jeffrey I. Schiller <jis@mit.edu>
RUN apt-get update && \
    apt-get install -y memcached nano-tiny nodejs nodejs-legacy npm sqlite3 supervisor
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN mkdir /opt/rendezvous

ADD supervisord.conf /etc/supervisor/supervisord.conf
ADD rendezvous.js /opt/rendezvous/rendezvous.js
ADD logworker.js /opt/rendezvous/logworker.js
ADD package.json /opt/rendezvous/package.json
RUN (cd /opt/rendezvous;npm install .)

VOLUME /data

CMD supervisord


