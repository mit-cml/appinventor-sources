// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

var wp = require('workerpool');
var sqlite3 = require('sqlite3');
var Lock = require('async-lock');

// Note: The code below runs in an other process via workerpool
var nodb = false;
var db = null;
var lock = new Lock({timeout: 5000});

var setupdb = function() {
    if (db === null) {
        db = new sqlite3.Database('/data/data.sqlite3',
                                  sqlite3.OPEN_READWRITE|sqlite3.OPEN_CREATE,
                                  function(err) {
                                      if (err) {
                                          nodb = true;
                                          throw err;
                                      }
                                  });
        db.serialize(function() {
            db.run("create table if not exists log (time timestamp, ip text, useragent text, apiversion text)");
            db.run("create table if not exists apilog (date text, ip text, version text, apiversion text, count)");
        });
    }
};

var recordLog = function(ip, userAgent, apiversion) {
    setupdb();
    if (nodb) {
        return;
    }
    db.run("insert into log (time, ip, useragent, apiversion) values (?, ?, ?, ?)",
           [new Date().toISOString(), ip, userAgent, apiversion]);
};

var recordVersion = function(ip, apiversion, versionname) {
    var date = new Date().toJSON().substring(0, 10);
    var count;
    setupdb();
    if (nodb) {
        return;
    }
    var key = ip + versionname + apiversion;
    lock.acquire(key, function(done) {
        db.all("select rowid,count from apilog where ip = ? and version = ? and apiversion = ? and date = ?",
               [ip, versionname, apiversion, date],
               function(err, rows) {
                   var count = 0;
                   var row;
                   if (err) {
                       console.log(err);
                   } else {
                       if (rows.length > 1) {
                           console.log("rows = " + rows.length + " ip = " + ip
                                       + " version = " + version + " apiversion = "
                                       + apiversion + " date = " + date);
                       } else {
                           if (rows.length != 0) {
                               row = rows[0];
                               count = row.count + 1;
                               db.run("update apilog set count = ? where rowid = ?",
                                      [count, row.rowid], function(err) {
                                          done();
                                      });

                           } else {
                               db.run("insert into apilog (ip, version, apiversion, date, count) "
                                      + " values (?, ?, ?, ?, ?)",
                                      [ip, versionname, apiversion, date, 1],
                                      function(err) {
                                          done();
                                      });
                           }
                       }
                   }
               });
    });
};

// create a worker and register public functions
wp.worker({
    recordLog : recordLog,
    recordVersion: recordVersion
});
