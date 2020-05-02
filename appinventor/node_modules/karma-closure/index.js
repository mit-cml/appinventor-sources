var chokidar = require('chokidar');
var fs = require('q-io/fs');

// inputs
var WATCH = ['test-app/js', 'test-app/test'];
var INCLUDE = ['test-app/test/main.js'];

var DependencyResolver = require('./lib/resolver');
var resolver = new DependencyResolver();

var pendingTimer = null;
var scheduleResolving = function() {
  if (pendingTimer) {
    return;
  }

  pendingTimer = setTimeout(function() {
    pendingTimer = null;
    console.log('RESOLVED FILES');
    console.log(resolver.resolveFiles(INCLUDE));
  }, 500);
};

// kick off watching
var watcher = chokidar.watch(WATCH, {persistent: true});

['change', 'add'].forEach(function(eventName) {
  watcher.on(eventName, function(path) {
    console.log(eventName, path);
    fs.read(path).then(function(content) {
      resolver.updateFile(path, content);
      scheduleResolving();
    });
  }, function(e) {
    console.error(e.stack);
  });
});
