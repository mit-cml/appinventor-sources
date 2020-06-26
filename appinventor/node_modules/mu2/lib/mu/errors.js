exports.fileNotFound = function (root, filename, error) {
  return {
    rootError: error,
    key: 'file_not_found',
    message: 'File not found to compile: ' + path.join(root, filename)
  }
};

exports.templateNotInCache = function (filename) {
  return {
    key: 'template_not_in_cache',
    message: filename + ' was not found in mu\'s cache. Has it been compiled?'
  }
};
