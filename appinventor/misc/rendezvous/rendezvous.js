var http = require('http');
var requestHandler = http.IncomingMessage.prototype;
var memcache = require('memcache');
var mc = new memcache.Client();
mc.connect();
var querystring = require('querystring');
var fs = require('fs')

/*
 *  Database Handler
 *
 */

var db = { 'connection' : null,
	   'insert' : function(key, ua, ip) {
	       var entry;
	       try {
		   var data = { 'key' : key, 'ip' : ip, 'ua' : ua,
				'ts' : (new Date()).toISOString()};
		   this.queue.push(data);
		   if (!this.connection) {
		       console.log('Opening a new connection.');
		       if (!this.uri) {
			   console.log("Do not have URI yet, queuing.");
		       } else {
			   var nano = require('nano')(this.uri);
			   this.connection = nano.use('companion');
		       }
		   }
		   while (entry = this.queue.pop())
		       this.connection.insert(data);
	       } catch (err) {
		   console.log(err);
		   console.log(err.stack);
		   this.connection = null; // Force reconnect next time
	       }
	   },
	   'queue' : []
	 }

fs.readFile('/home/appinv/uri', function(err, data) {
    if (err)
	throw err;
    db.uri = data.toString();
});

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
    }
  }

  // regular HTTP servers
  if (connection.remoteAddress) {
    return {
        ip: connection.remoteAddress
      , port: connection.remotePort
    };
  }

  // in node 0.4 the remote address for https servers was in a different
  // location
  if (socket.remoteAddress) {
    return {
        ip: socket.remoteAddress
      , port: socket.remotePort
    };
  }

  // last possible location..
  return {
      ip: this.socket.remoteAddress || '0.0.0.0'
    , port: this.socket.remotePort || 0
  }
});

var server = function(request, response) {
    var data = "";
    if (request.method == 'POST') { // A phone checking in
	request.on('data', function(chunk) {
	    data += chunk.toString();
	});
	request.on('end', function() {
	    data = querystring.parse(data);
	    var key = data['key'];
	    if (key) {
		var json = JSON.stringify(data);
		mc.set('rr-' + key, json, 120); // Save for two minutes.
	    }
	    db.insert(key, request.headers['user-agent'], request.remote.ip);
	    response.writeHead(200, "OK", { "Content-Type" : "text/plain",
					    "Access-Control-Allow-Origin" : "*",
					    "Access-Control-Allow-Headers" : "origin, content-type"});
	    response.end("OK\n");
	});
    } else if (request.method == 'OPTIONS') {
	response.writeHead(200, "OK", { "Content-Type" : "text/html",
					"Access-Control-Allow-Origin" : "*",
					"Access-Control-Allow-Headers" : "origin, content-type"});
	response.end("");
    } else {
	var url = request.url.split('/');
	var key = url[url.length-1];
	if (key) {
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
    }
}

http.createServer(server).listen(3000);
