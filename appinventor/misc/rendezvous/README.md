node.js version of MIT App Inventor Rendezvous Server
===

This codebase provides the basic functions of the Rendezvous
Server. It also logs to a CouchDB instance the POST's received from
the phone. This database can then be mined for statistics.

### To Run:

Prerequisites:

     * Node.js (tested with version v0.8.21)
     * CouchDB -- for stats
     * npm install memcache
     * npm install nano

Setup a user named "appinv" (should be a system account, but doesn't have to be).
The "appinv" userid's home directory should be in /home/appinv

  - Put rendezvous.js in /home/appinv.
  - Put rendezvous.conf in /etc/init for upstart (Ubuntu 12.04LTS).
  - Put the database URI in /home/appinv/uri.