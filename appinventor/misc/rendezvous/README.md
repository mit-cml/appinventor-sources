node.js version of MIT App Inventor Rendezvous Server
===

This codebase provides the basic functions of the Rendezvous
Server. It also logs to a CouchDB instance the POST's received from
the phone. This database can then be mined for statistics.

This is the code that we use for the public MIT Server

### To Run:

Prerequisites:

     * Node.js (tested with version v0.12.12)
     * npm install sqlite3
     * npm install memcache

Setup a user named "appinv" (should be a system account, but doesn't have to be).
The "appinv" userid's home directory should be in /home/appinv

  - Put rendezvous.js in /home/appinv.
  - Put rendezvous.conf in /etc/init for upstart (Ubuntu 12.04LTS).
  - Create rendezvous.sqlite in /home/appinv
    * sqlite3 rendezvous.sqlite <<EOF
      CREATE TABLE log (time timestamp, ip text, useragent text);
      EOF

Note: If you do not create rendezvous.sqlite, the server will work
just fine, it just won't create any log records. Also note that the
apache server you put in front of nodejs may well have logs.

If you do create the logging database, be sure to pay attention to it
and prune it from time to time, otherwise it will grow in size without
bound until it fills your disk. You have been warned!