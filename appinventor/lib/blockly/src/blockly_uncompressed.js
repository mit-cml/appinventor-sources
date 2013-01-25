"use strict";

(function() {
  var BLOCKLY_DIR;
  var scripts = document.getElementsByTagName('script');
  var re = new RegExp('(.+)[\/]blockly_uncompressed\.js$');
  for (var x = 0, script; script = scripts[x]; x++) {
    var match = re.exec(script.src);
    if (match) {
      BLOCKLY_DIR = match[1];
      break;
    }
  }
  if (!BLOCKLY_DIR) {
    alert('Could not detect Blockly\'s directory name.');
    return;
  }

  document.write('<script type="text/javascript" src="' + BLOCKLY_DIR + '/../closure-library-read-only/closure/goog/base.js"></script>');
  document.write('<script type="text/javascript" src="' + BLOCKLY_DIR + '/blockly_core_deps.js"></script>');
  document.write('<script type="text/javascript">goog.require(\'Blockly.core\');</script>');
})();
