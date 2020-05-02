// TODO(vojta): cache resolved
var initClosureDependencyResolver = function(emitter, fileList, resolver, iit, files) {

  // load var CLOSURE_NO_DEPS = true to disable this closure bullshit
  files.unshift({
    pattern: __dirname + '/no_deps_bullshit.js',
    included: true,
    served: true,
    watched: false
  });

  // whenever file list changes, resolve the deps - update files.included
  emitter.on('file_list_modified', function(files) {
    var includedPaths = files.included.map(function(file) {
      return file.originalPath;
    });

    if (iit.isInclusive()) {
      includedPaths = includedPaths.filter(iit.filter);
    }

    var resolvedPaths = resolver.resolveFiles(includedPaths);
    var servedFiles = files.served;

    // TODO(vojta): change files.served to be a map, rather than an array
    files.included = resolvedPaths.map(function(filepath) {
      for (var i = 0, length = servedFiles.length; i < length; i++) {
        if (servedFiles[i].originalPath === filepath) {
          return servedFiles[i];
        }
      }

      console.error('NOT SERVED FILE', filepath);
      var externalFile = {
        path: filepath,
        originalPath: filepath,
        contentPath: filepath,
        isUrl: false,
        // TODO(vojta): cache mtime and restat when deps.js changes ?
        // TODO(votja): use the last synced CL number in google3 ?
        mtime: new Date()
      };

      files.served.push(externalFile);
      return externalFile;
    });
  });

  // monkey-patch fileList to get notified when file is removed
  var originalRemoveFile = fileList.removeFile;
  fileList.removeFile = function(filepath, done) {
    resolver.removeFile(filepath);
    iit.removeFile(filepath);

    return originalRemoveFile.call(fileList, filepath, done);
  };

  return resolver;
};

initClosureDependencyResolver.$inject = ['emitter', 'fileList', 'x:resolver', 'x:iit-filter',
    'config.files'];


var createPreprocesor = function(resolver) {
  return function(content, file, done) {
    resolver.updateFile(file.originalPath, content);
    done(content);
  };
};

createPreprocesor.$inject = ['x:resolver'];


var createDepsPreprocesor = function(resolver) {
  return function(content, file, done) {
    resolver.loadExternalDeps(file.originalPath, content);
    done(content);
  };
};

createDepsPreprocesor.$inject = ['x:resolver'];


var createIitPreprocesor = function(iit) {
  return function(content, file, done) {
    iit.updateFile(file.originalPath, content);
    done(content);
  };
};

createIitPreprocesor.$inject = ['x:iit-filter'];

// PUBLISH DI MODULE
module.exports = {
  'framework:closure': ['factory', initClosureDependencyResolver],
  'preprocessor:closure': ['factory', createPreprocesor],
  'preprocessor:closure-deps': ['factory', createDepsPreprocesor],
  'preprocessor:closure-iit': ['factory', createIitPreprocesor],

  // TODO(vojta): these should be internal
  'x:resolver': ['type', require('./resolver')],
  'x:iit-filter': ['type', require('./iit')]
};
