var equalSorted = function(a, b) {
  if (a.length !== b.length) {
    return false;
  }

  for (var i = 0, length = a.length; i < length; i++) {
    if (a[i] !== b[i]) {
      return false;
    }
  }

  return true;
};

var diffSorted = function(a, b) {
  var added = [];
  var removed = [];

  var i = 0;
  var j = 0;

  while (i < a.length && j < b.length) {
    if (a[i] === b[j]) {
      i++;
      j++;
    } else if (a[i] < b[j]) {
      removed.push(a[i]);
      i++;
    } else {
      added.push(b[j]);
      j++;
    }
  }

  while (i < a.length) {
    removed.push(a[i]);
    i++;
  }

  while (j < b.length) {
    added.push(b[j]);
    j++;
  }

  return !added.length && !removed.length ? null : {
    added: added,
    removed: removed
  };
};


exports.equalSorted = equalSorted;
exports.diffSorted = diffSorted;
