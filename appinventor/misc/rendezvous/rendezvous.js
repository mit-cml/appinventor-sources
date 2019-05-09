// Copyright 2013-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

var http = require('http');
var exec = require('child_process').exec;
// var sys = require('sys');
var path = require('path');
var requestHandler = http.IncomingMessage.prototype;
var memcache = require('memcache');
var mc = new memcache.Client();
mc.connect();
var querystring = require('querystring');
var fs = require('fs');
var wp = require('workerpool');
var sqlite3 = require('sqlite3');
var Lock = require('async-lock');

const pool = wp.pool(__dirname + '/logworker.js', {'maxWorkers' : 1});

/**
 * Add a uniform interface for remote address in Node.js
 *
 * From: https://gist.github.com/3rd-Eden/1740741
 * @api private
 */

requestHandler.__defineGetter__('remote', function remote () {
    var connection = this.connection
    , headers = this.headers
    , socket = connection.socket;

    // return early if we are behind a reverse proxy
    if (headers['x-forwarded-for']) {
        return {
            ip: headers['x-forwarded-for']
            , port: headers['x-forwarded-port']
        };
    }

    // regular HTTP servers
    if (connection.remoteAddress) {
        return {
            ip: connection.remoteAddress
            , port: connection.remotePort
        };
    }

    // last possible location..
    return {
        ip: this.socket.remoteAddress || '0.0.0.0'
        , port: this.socket.remotePort || 0
    };
});

var lock = new Lock({timeout: 5000});

var server = function(request, response) {
    var data = "";
    if (request.method == 'POST') { // A phone checking in
        request.on('data', function(chunk) {
            data += chunk.toString();
        });
        request.on('end', function() {
            console.log('POST: url = ' + request.url);
            if (request.url == '/rendezvous/') {
	        data = querystring.parse(data);
	        var key = data.key;
                console.log("POST(rendezvous): key = " + key);
                var version = data.version;
                var api = data.api;
                var ip = request.remote.ip;
                if (!!api && !!ip && !!version) {
                    pool.exec('recordVersion', [ip, api, version]);
                }
	        if (key) {
	            var json = JSON.stringify(data);
	            mc.set('rr-' + key, json, 120); // Save for two minutes.
	        }
	        response.writeHead(200, "OK", { "Content-Type" : "text/plain",
					        "Access-Control-Allow-Origin" : "*",
					        "Access-Control-Allow-Headers" : "origin, content-type"});
	        response.end("OK\n");
            } else if (request.url == '/rendezvous2/') {
                data = JSON.parse(data);
                //      data = querystring.parse(data);
                var key = data['key'];
                var webrtc = data['webrtc'];
                var first = data['first'];
                if (first) {
                    var apiversion = data['apiversion'];
                    if (!apiversion) {
                        apiversion = -1;
                    }
                    pool.exec('recordLog', [request.remote.ip, request.headers['user-agent'], apiversion]).then(function(result) {})
                        .catch(function(err) {
                            console.log("recordLog: " + err.message);
                        });
                }
                if (key) {
                    // In this case we append data to what is in memcache
                    lock.acquire(key, function(done) {
                        mc.get('rr2-' + key, function(err, result) {
                            console.log('Memcache returned: ' + result);
                            if (!result) {
                                result = "[]";
                            }
                            result = JSON.parse(result);
                            result.push(data);
                            result = JSON.stringify(result);
                            mc.set('rr2-' + key, result, 120);
                            done(false, true);
                        });
                    }, function(err, ret) {
                        console.log("in done function");
                        if (err) {
                            console.log("locking Error: " + err);
                        }
                    });
                }
                response.writeHead(200, "OK", { "Content-Type" : "text/plain",
				                "Access-Control-Allow-Origin" : "*",
				                "Access-Control-Allow-Headers" : "origin, content-type"});
                response.end("OK\n");
            } else {
                response.writeHead(404, '', {});
                response.end("");
            }
        });

    } else if (request.method == 'OPTIONS') {
        response.writeHead(200, "OK", { "Content-Type" : "text/html",
				        "Access-Control-Allow-Origin" : "*",
				        "Access-Control-Allow-Headers" : "origin, content-type"});
        response.end("");
    } else { // request.method == 'GET'
        var url = request.url.split('/');
        console.log("GET: url = " + url + ' URL = ' + request.url);
        console.log("GET: url[1] = " + url[1]);
        if (url[1] == 'rendezvous2') {
            var key = url[url.length-1];
            if (key && (key == 'test')) {
                response.writeHead(200, "OK", { "Content-Type" : "application/json",
				                "Access-Control-Allow-Origin" : "*",
                                                "Expires" : "Fri, 01 Jan 1990 00:00:00 GMT",
                                                "Cache-Control" : "no-cache, must-revalidate",
				                "Access-Control-Allow-Headers" : "origin, content-type"});
                response.end("Connection OK\n");
            } else if (key) {
                console.log('ding');
                mc.get('rr2-' + key, function(err, result) {
	            response.writeHead(200, "OK", { "Content-Type" : "application/json",
					            "Access-Control-Allow-Origin" : "*",
                                                    "Expires" : "Fri, 01 Jan 1990 00:00:00 GMT",
                                                    "Cache-Control" : "no-cache, must-revalidate",
					            "Access-Control-Allow-Headers" : "origin, content-type"});
	            console.log(request.remote);
	            response.end(result);
                });
            } else {		// Not found
                response.writeHead(200, "OK", { "Content-Type" : "application/json",
                                                "Cache-Control" : "no-cache, must-revalidate",
                                                "Expires" : "Fri, 01 Jan 1990 00:00:00 GMT",
				                "Access-Control-Allow-Origin" : "*",
				                "Access-Control-Allow-Headers" : "origin, content-type"});
                response.end("");
            }
        } else if (url[1] == 'rendezvous') {
            var key = url[url.length-1];
            console.log("GET(rendezvous): key = " + key);
            if (key && (key == 'test')) {
	        response.writeHead(200, "OK", { "Content-Type" : "application/json",
					        "Access-Control-Allow-Origin" : "*",
                                                "Expires" : "Fri, 01 Jan 1990 00:00:00 GMT",
                                                "Cache-Control" : "no-cache, must-revalidate",
					        "Access-Control-Allow-Headers" : "origin, content-type"});
                response.end("Connection OK\n");
            } else if (key) {
	        mc.get('rr-' + key, function(err, result) {
	            response.writeHead(200, "OK", { "Content-Type" : "application/json",
					            "Access-Control-Allow-Origin" : "*",
                                                    "Expires" : "Fri, 01 Jan 1990 00:00:00 GMT",
                                                    "Cache-Control" : "no-cache, must-revalidate",
					            "Access-Control-Allow-Headers" : "origin, content-type"});
                    //		console.log(request.remote);
	            response.end(result);
	        });
            } else {		// Not found
	        response.writeHead(200, "OK", { "Content-Type" : "application/json",
                                                "Cache-Control" : "no-cache, must-revalidate",
                                                "Expires" : "Fri, 01 Jan 1990 00:00:00 GMT",
					        "Access-Control-Allow-Origin" : "*",
					        "Access-Control-Allow-Headers" : "origin, content-type"});
	        response.end("");
            }
        } else if ( url[1] == '') {
            response.writeHead(200, "OK", { "Content-Type" : "text/html",
                                            "Cache-Control" : "no-cache, must-revalidate",
                                            "Expires" : "Fri, 01 Jan 1990 00:00:00 GMT" });
            response.end("<html>\n" +
                         "<head><title>MIT App Inventor Rendezvous Server</title></head>\n" +
                         "<body>\n" +
                         "<h1>MIT App Inventor Rendezvous Server</h1>\n" +
                         "<P>This is the MIT App Inventor Rendezvous Server, which appears to be\n" +
                         "operating normally.</p></body></html>\n");
        } else {
            response.writeHead(404, '', {});
            response.end("");
        }
    }
};

http.createServer(server).listen(3000);
