var IitFilter = function() {
  var inclusiveMap = {};
  var currentMode = 0;

  var recomputeCurrentMode = function() {
    currentMode = Object.keys(inclusiveMap).reduce(function(previous, key) {
      return Math.max(previous, inclusiveMap[key] || 0);
    }, 0);
  };

  var setFileMode = function(filepath, newMode) {
    var previousMode = inclusiveMap[filepath];

    if (previousMode === newMode) {
      return;
    }

    inclusiveMap[filepath] = newMode;

    if (currentMode > 0 && previousMode === currentMode) {
      recomputeCurrentMode();
    } else {
      currentMode = Math.max(currentMode, newMode);
    }
  };

  this.isInclusive = function() {
    return currentMode > 0;
  };

  this.filter = function(filepath) {
    if (!currentMode || inclusiveMap[filepath] === undefined) {
      return true;
    }

    if (inclusiveMap[filepath] && inclusiveMap[filepath] >= currentMode) {
      return true;
    }

    return false;
  };

  this.removeFile = function(filepath) {
    setFileMode(filepath, 0);
  };

  this.updateFile = function(filepath, content) {
    if (content.indexOf('iit(') !== -1) {
      setFileMode(filepath, 2);
    } else if (content.indexOf('ddescribe(') !== -1) {
      setFileMode(filepath, 1);
    } else {
      this.removeFile(filepath);
    }
  };
};

module.exports = IitFilter;
