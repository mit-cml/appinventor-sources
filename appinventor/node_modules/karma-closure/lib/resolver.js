// var utils = require('./utils');
var goog = require('./goog');

// TODO(vojta): can we handle provide "same thing provided multiple times" ?
var DependencyResolver = function(logger) {
  var log = logger.create('closure');

  // the state
  var fileMap = {};
  var provideMap = {};

  var updateProvideMap = function(filepath, oldProvides, newProvides) {
    oldProvides.forEach(function(dep) {
      if (provideMap[dep] === filepath) {
        provideMap[dep] = null;
      }
    });

    newProvides.forEach(function(dep) {
      provideMap[dep] = filepath;
    });
  };

  var resolveFile = function(filepath, files, alreadyResolvedMap) {
    if (!fileMap[filepath]) {
      // console.log('IGNORED', filepath);
      files.push(filepath);
      return;
    }

    // resolve all dependencies first
    fileMap[filepath].requires.forEach(function(dep) {
      if (!alreadyResolvedMap[dep]) {
        if (provideMap[dep]) {
          resolveFile(provideMap[dep], files, alreadyResolvedMap);
        } else {
          log.error('MISSING DEPENDENCY:', dep);
          log.error('Did you forget to preprocess your source directory? Or did you leave off ' +
                      'the google closure library deps.js file?');
        }
      }
    });

    files.push(filepath);
    fileMap[filepath].provides.forEach(function(dep) {
      alreadyResolvedMap[dep] = true;
    });
  };

  this.removeFile = function(filepath) {
    if (!fileMap[filepath]) {
      // console.log('Cannot remove unmapped file', filepath);
      return;
    }

    fileMap[filepath].provides.forEach(function(dep) {
      if (provideMap[dep] === filepath) {
        provideMap[dep] = null;
      }
    });
    fileMap[filepath] = null;
  };

  this.updateFile = function(filepath, content) {
    var parsed = goog.parseProvideRequire(content);

    if (!fileMap[filepath]) {
      // console.log('New file', filepath, 'adding to the map.');
      // console.log(parsed);
      updateProvideMap(filepath, [], parsed.provides);
      fileMap[filepath] = parsed;
      return;
    }

    // var diffProvides = utils.diffSorted(fileMap[filepath].provides, parsed.provides);
    // var diffRequires = utils.diffSorted(fileMap[filepath].requires, parsed.requires);

    // if (diffProvides) {
    //   console.log('Provides change in', filepath);
    //   console.log('Added', diffProvides.added);
    //   console.log('Removed', diffProvides.removed);
    // } else {
    //   console.log('No provides change in', filepath);
    // }

    // if (diffRequires) {
    //   console.log('Requires change in', filepath);
    //   console.log('Added', diffRequires.added);
    //   console.log('Removed', diffRequires.removed);
    // } else {
    //   console.log('No requires change in', filepath);
    // }

    updateProvideMap(filepath, fileMap[filepath].provides, parsed.provides);
    fileMap[filepath] = parsed;
  };

  this.resolveFiles = function(files) {
    // console.log('RESOLVING', files);
    // console.log(fileMap);

    var resolvedFiles = [];
    var alreadyResolvedMap = {};

    files.forEach(function(file) {
      resolveFile(file, resolvedFiles, alreadyResolvedMap);
    });

    return resolvedFiles;
  };

  this.loadExternalDeps = function(filepath, content) {
    var parsed = goog.parseDepsJs(filepath, content);

    /* jshint camelcase: false, proto: true */
    fileMap.__proto__ = parsed.fileMap;
    provideMap.__proto__ = parsed.provideMap;
  };
};

module.exports = DependencyResolver;
